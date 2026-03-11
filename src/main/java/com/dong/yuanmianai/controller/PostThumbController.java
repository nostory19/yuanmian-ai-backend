package com.dong.yuanmianai.controller;

import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.BusinessException;
import com.dong.yuanmianai.model.dto.postthumb.PostThumbAddRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.service.PostThumbService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子点赞接口
 *
 * @author <a href="">程序员远行</a>
 * @from <">公众号：所谓远行Misnearch</a>
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
            HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
