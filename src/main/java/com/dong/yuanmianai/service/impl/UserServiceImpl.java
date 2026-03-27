package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.BusinessException;
import com.dong.yuanmianai.mapper.UserMapper;
import com.dong.yuanmianai.mapper.UserRoleMapper;
import com.dong.yuanmianai.mapper.RefreshTokenMapper;
import com.dong.yuanmianai.model.dto.user.UserQueryRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.enums.UserRoleEnum;
import com.dong.yuanmianai.model.vo.LoginUserVO;
import com.dong.yuanmianai.model.vo.UserVO;
import com.dong.yuanmianai.model.entity.UserRole;
import com.dong.yuanmianai.model.entity.RefreshToken;
import com.dong.yuanmianai.service.UserService;
import com.dong.yuanmianai.utils.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.Date;

/**
 * 用户服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "dong";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final int REFRESH_TOKEN_TIMEOUT_SECONDS = 60 * 60 * 24 * 7;

    @jakarta.annotation.Resource
    private UserRoleMapper userRoleMapper;

    @jakarta.annotation.Resource
    private RefreshTokenMapper refreshTokenMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleCode("USER");
            userRoleMapper.insert(userRole);
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        return buildLoginResponse(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            return buildLoginResponse(user);
        }
    }

    @Override
    public LoginUserVO refreshLoginToken(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "refreshToken 不能为空");
        }
        Object loginIdObj = SaManager.getSaTokenDao().get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (loginIdObj == null) {
            RefreshToken dbToken = refreshTokenMapper.selectOne(new QueryWrapper<RefreshToken>()
                    .eq("refresh_token", refreshToken)
                    .gt("expire_at", new Date())
                    .last("limit 1"));
            if (dbToken != null) {
                loginIdObj = dbToken.getUserId();
            }
        }
        if (loginIdObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "refreshToken 已失效");
        }
        long userId = Long.parseLong(String.valueOf(loginIdObj));
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        }
        return buildLoginResponse(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = StpUtil.getLoginIdAsLong();
        User currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        if (!StpUtil.isLogin()) {
            return null;
        }
        long userId = StpUtil.getLoginIdAsLong();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = getLoginUserPermitNull(request);
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        Long count = userRoleMapper.selectCount(new QueryWrapper<UserRole>()
                .eq("user_id", user.getId())
                .eq("role_code", "ROOT"));
        return count != null && count > 0;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        StpUtil.logout();
        // 清理该用户的 refresh token（简化策略：按用户 id 前缀全清）
        // 这里用随机 token 方案，无法枚举删除，依赖过期自动失效。
        // 如需严格回收，可落库持久化并按用户维度管理。
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    private LoginUserVO buildLoginResponse(User user) {
        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        SaManager.getSaTokenDao().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                String.valueOf(user.getId()),
                REFRESH_TOKEN_TIMEOUT_SECONDS
        );
        refreshTokenMapper.insert(buildRefreshToken(user.getId(), refreshToken));
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setAccessToken(accessToken);
        loginUserVO.setRefreshToken(refreshToken);
        long timeout = StpUtil.getTokenTimeout();
        loginUserVO.setExpiresIn(timeout > 0 ? timeout : 0L);
        return loginUserVO;
    }

    private RefreshToken buildRefreshToken(Long userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setRefreshToken(token);
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TIMEOUT_SECONDS * 1000L));
        refreshToken.setCreatedTime(new Date());
        return refreshToken;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 id 非法");
        }
        User dbUser = this.getById(userId);
        if (dbUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserRole(UserRoleEnum.BAN.getValue());
        boolean updated = this.updateById(updateUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "禁用用户失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserAsRoot(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 id 非法");
        }
        User dbUser = this.getById(userId);
        if (dbUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        UserRole rootRole = userRoleMapper.selectOne(new QueryWrapper<UserRole>()
                .eq("user_id", userId)
                .eq("role_code", "ROOT")
                .last("limit 1"));
        if (rootRole == null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleCode("ROOT");
            userRoleMapper.insert(userRole);
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserRole(UserRoleEnum.ADMIN.getValue());
        boolean updated = this.updateById(updateUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "设置 ROOT 失败");
        }
    }
}
