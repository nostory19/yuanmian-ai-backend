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
