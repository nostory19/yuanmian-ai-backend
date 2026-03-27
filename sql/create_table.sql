# 数据库初始化
# @author
# @from

-- 创建库
create database if not exists yuanmian_db;

-- 切换库
use yuanmian_db;

-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment 'id' primary key,

    userAccount   varchar(256)                       not null comment '账号',
    userPassword  varchar(512)                       not null comment '密码',

    unionId       varchar(256)                       null comment '微信开放平台id',
    mpOpenId      varchar(256)                       null comment '公众号openId',

    userName      varchar(256)                       null comment '用户昵称',
    userAvatar    varchar(1024)                      null comment '用户头像',
    userProfile   varchar(512)                       null comment '用户简介',

    userRole      tinyint  default 0                 not null comment '用户角色 0-普通用户 1-管理员 2-封禁',

    vipLevel      int      default 0                 not null comment '会员等级 0-普通用户 1-VIP',
    vipExpireTime datetime                         null comment '会员过期时间',

    shareCode     varchar(20)                        null comment '分享码',
    inviteUser    bigint                             null comment '邀请用户 id',

    editTime      datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',

    isDelete      tinyint  default 0                 not null comment '是否删除',

    unique index idx_userAccount (userAccount),
    unique index idx_shareCode (shareCode),
    index idx_inviteUser (inviteUser),
    index idx_unionId (unionId)

    ) comment '用户表' collate = utf8mb4_unicode_ci;


-- 会员订单表
create table if not exists vip_order
(
    id           bigint auto_increment primary key comment '订单id',

    orderNo      varchar(64)                        not null comment '订单号',

    userId       bigint                             not null comment '用户id',

    vipLevel     int                                not null comment '会员等级',

    vipDays      int                                not null comment '会员天数',

    price        decimal(10,2) default 0            comment '支付金额',

    payType      varchar(32)                        null comment '支付方式 alipay/wechat/code',

    payStatus    tinyint       default 0            not null comment '支付状态 0-未支付 1-已支付',

    expireTime   datetime                           null comment '会员到期时间',

    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',

    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',

    index idx_userId (userId),
    unique index idx_orderNo (orderNo)

    ) comment '会员订单表' collate = utf8mb4_unicode_ci;


-- 邀请记录表
create table if not exists invite_record
(
    id             bigint auto_increment primary key comment '记录id',

    inviteUserId   bigint                             not null comment '邀请人id',

    invitedUserId  bigint                             not null comment '被邀请人id',

    rewardStatus   tinyint       default 0            not null comment '奖励状态 0-未奖励 1-已奖励',

    rewardType     varchar(32)                        null comment '奖励类型 vip/points',

    rewardValue    int                                null comment '奖励值',

    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',

    unique index idx_invite (inviteUserId, invitedUserId),
    index idx_invitedUser (invitedUserId)

    ) comment '邀请记录表' collate = utf8mb4_unicode_ci;

-- 题库表
create table if not exists question_bank
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(256)                       null comment '标题',
    description text                               null comment '描述',
    picture     varchar(2048)                      null comment '图片',
    userId      bigint                             not null comment '创建用户 id',
    editTime    datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_title (title)
    ) comment '题库' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(256)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '推荐答案',
    userId     bigint                             not null comment '创建用户 id',

    reviewStatus  int      default 0  not null comment '状态：0-待审核, 1-通过, 2-拒绝',
    reviewMessage varchar(512)        null comment '审核信息',
    reviewerId    bigint              null comment '审核人 id',
    reviewTime    datetime            null comment '审核时间',

    viewNum       int      default 0    not null comment '浏览量',
    thumbNum      int      default 0    not null comment '点赞数',
    favourNum     int      default 0    not null comment '收藏数',

    priority  int  default 0  not null comment '优先级',

    source   varchar(512)  null comment '题目来源',

    needVip  tinyint  default 0  not null comment '仅会员可见（1 表示仅会员可见）',

    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_title (title),
    index idx_userId (userId)
    ) comment '题目' collate = utf8mb4_unicode_ci;

-- 题库题目表（硬删除）
create table if not exists question_bank_question
(
    id             bigint auto_increment comment 'id' primary key,
    questionBankId bigint                             not null comment '题库 id',
    questionId     bigint                             not null comment '题目 id',
    userId         bigint                             not null comment '创建用户 id',
    questionOrder  int  default 0  not null comment '题目顺序（题号）',

    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE (questionBankId, questionId)
    ) comment '题库题目' collate = utf8mb4_unicode_ci;


ALTER TABLE user
    MODIFY COLUMN userRole varchar(256) DEFAULT 'user' NOT NULL COMMENT '用户角色：user/admin/ban';

-- 面试会话表
create table if not exists interview_session
(
    id         bigint auto_increment primary key comment '会话id',
    userId     bigint                             not null comment '用户id',
    jobRole    varchar(128)                       not null comment '面试岗位',
    difficulty varchar(32)                        not null default 'medium' comment '难度',
    status     varchar(32)                        not null default 'ACTIVE' comment '状态',
    startTime  datetime                           null comment '开始时间',
    endTime    datetime                           null comment '结束时间',
    index idx_userId (userId),
    index idx_status (status)
) comment '面试会话表' collate = utf8mb4_unicode_ci;

-- 面试消息表
create table if not exists interview_message
(
    id         bigint auto_increment primary key comment '消息id',
    sessionId  bigint                             not null comment '会话id',
    role       varchar(32)                        not null comment 'user/agent',
    content    text                               not null comment '消息内容',
    agentType  varchar(64)                        null comment 'interviewer/evaluator/follow_up',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_sessionId (sessionId),
    index idx_role (role)
) comment '面试消息表' collate = utf8mb4_unicode_ci;

-- 评估结果表
create table if not exists evaluation_result
(
    id         bigint auto_increment primary key comment '评估id',
    sessionId  bigint                             not null comment '会话id',
    questionId bigint                             null comment '题目id',
    score      int                                not null comment '总分',
    feedback   text                               null comment '反馈',
    dimension  json                               null comment '多维评分',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_sessionId (sessionId),
    index idx_questionId (questionId)
) comment '评估结果表' collate = utf8mb4_unicode_ci;

-- 用户能力画像表
create table if not exists user_profile
(
    userId      bigint primary key comment '用户id',
    skillVector json                               null comment '技能向量',
    updatedTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户能力画像表' collate = utf8mb4_unicode_ci;

-- AI 助手会话表
create table if not exists ai_assistant_sessions
(
    id           bigint auto_increment primary key,
    session_id   varchar(64)                        not null comment '会话ID',
    user_id      bigint                             not null comment '用户ID',
    user_name    varchar(64)                        not null comment '用户名',
    created_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    modify_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    title        varchar(255)                       not null comment '标题',
    daily_routes varchar(512)                       null,
    unique key uk_session_id (session_id),
    key idx_ai_session_user_id (user_id)
) comment 'AI 助手会话表' collate = utf8mb4_unicode_ci;

-- AI 助手消息表
create table if not exists ai_assistant_chat_messages
(
    msg_id      varchar(64)                        not null comment '消息ID' primary key,
    session_id  varchar(64)                        not null comment '会话ID',
    user_id     bigint                             not null comment '用户ID',
    user_name   varchar(64)                        not null comment '用户名',
    role        varchar(32)                        not null comment '角色(user/assistant)',
    content     text                               not null comment '对话内容',
    title       varchar(255)                       null comment '标题',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    modify_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_ai_msg_session_id (session_id),
    key idx_ai_msg_user_id (user_id)
) comment 'AI 助手消息表' collate = utf8mb4_unicode_ci;

-- 角色表
create table if not exists role
(
    id        bigint auto_increment primary key,
    role_code varchar(64) not null,
    role_name varchar(64) not null,
    unique key uk_role_code (role_code)
) comment '角色表' collate = utf8mb4_unicode_ci;

-- 用户角色关联
create table if not exists user_role
(
    id        bigint auto_increment primary key,
    user_id   bigint      not null,
    role_code varchar(64) not null,
    key idx_user_role_user_id (user_id),
    key idx_user_role_role_code (role_code)
) comment '用户角色关联表' collate = utf8mb4_unicode_ci;

-- 刷新令牌表
create table if not exists refresh_token
(
    id            bigint auto_increment primary key,
    user_id       bigint                             not null,
    refresh_token varchar(255)                       not null,
    expire_at     datetime                           not null,
    created_time  datetime default CURRENT_TIMESTAMP not null,
    unique key uk_refresh_token (refresh_token),
    key idx_refresh_user_id (user_id)
) comment '刷新令牌表' collate = utf8mb4_unicode_ci;

-- 权限表
create table if not exists permission
(
    id        bigint auto_increment primary key,
    perm_code varchar(128) not null comment '权限编码，如 ai:chat:read',
    perm_name varchar(128) not null,
    unique key uk_perm_code (perm_code)
) comment '权限表' collate = utf8mb4_unicode_ci;

-- 角色-权限关联表
create table if not exists role_permission
(
    id        bigint auto_increment primary key,
    role_code varchar(64)  not null,
    perm_code varchar(128) not null,
    key idx_rp_role (role_code),
    key idx_rp_perm (perm_code)
) comment '角色权限关联表' collate = utf8mb4_unicode_ci;

-- 角色初始化
insert into role (role_code, role_name)
values ('USER', '普通用户'),
       ('ROOT', '超级管理员')
on duplicate key update role_name = values(role_name);

-- 权限初始化
insert into permission (perm_code, perm_name)
values ('ai:session', '会话列表'),
       ('ai:history', '会话历史'),
       ('ai:chat', '聊天发送与流式'),
       ('question:manage', '题目管理'),
       ('question-bank:manage', '题库管理'),
       ('question-bank-question:manage', '题库题目管理'),
       ('invite-record:manage', '邀请记录管理'),
       ('vip-order:manage', '会员订单管理'),
       ('user:manage', '用户管理'),
       ('user:disable', '禁用用户'),
       ('user:set-root', '设为ROOT')
on duplicate key update perm_name = values(perm_name);

-- USER 角色权限
insert into role_permission (role_code, perm_code)
values ('USER', 'ai:session'),
       ('USER', 'ai:history'),
       ('USER', 'ai:chat')
on duplicate key update perm_code = values(perm_code);

-- ROOT 角色权限（授予全部）
insert into role_permission (role_code, perm_code)
select 'ROOT', p.perm_code
from permission p
on duplicate key update perm_code = values(perm_code);