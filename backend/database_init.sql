-- =========================================
-- 企业微信会议录音转写系统
-- 数据库初始化脚本
-- =========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS meeting_recording DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE meeting_recording;

-- =========================================
-- 用户表
-- =========================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企业微信用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `avatar` VARCHAR(500) COMMENT '用户头像',
    `mobile` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `department` VARCHAR(200) COMMENT '部门',
    `position` VARCHAR(100) COMMENT '职位',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =========================================
-- 设备表
-- =========================================
CREATE TABLE IF NOT EXISTS `device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '设备唯一标识',
    `device_serial` VARCHAR(100) NOT NULL UNIQUE COMMENT '设备序列号',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_key` VARCHAR(200) COMMENT '设备密钥',
    `status` VARCHAR(20) DEFAULT 'offline' COMMENT '设备状态：online/offline',
    `last_online_time` TIMESTAMP NULL COMMENT '最后在线时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_device_serial` (`device_serial`),
    INDEX `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- =========================================
-- 设备绑定关系表
-- =========================================
CREATE TABLE IF NOT EXISTS `device_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    `binding_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '绑定关系ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `device_serial` VARCHAR(100) NOT NULL COMMENT '设备序列号',
    `user_id` VARCHAR(64) NULL COMMENT '企业微信用户ID（主要用户）',
    `user_ids` VARCHAR(500) NULL COMMENT '多个用户ID（逗号分隔）',
    `user_names` VARCHAR(1000) NULL COMMENT '多个用户名（逗号分隔）',
    `binding_type` VARCHAR(20) DEFAULT 'manual' COMMENT '绑定类型：manual/auto',
    `bind_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `last_update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_binding_device_serial` (`device_serial`),
    INDEX `idx_binding_device_id` (`device_id`),
    INDEX `idx_binding_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备绑定关系表';

-- =========================================
-- 会议记录表
-- =========================================
CREATE TABLE IF NOT EXISTS `meeting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会议ID',
    `meeting_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '会议唯一标识',
    `title` VARCHAR(200) NOT NULL COMMENT '会议标题',
    `meeting_date` TIMESTAMP NOT NULL COMMENT '会议日期',
    `duration` VARCHAR(50) NULL COMMENT '会议时长',
    `participants` INT DEFAULT 0 COMMENT '参与人数',
    `status` VARCHAR(20) DEFAULT 'processing' COMMENT '状态：processing/completed/failed',
    `summary` TEXT NULL COMMENT '会议摘要',
    `summary_status` VARCHAR(20) DEFAULT 'pending' COMMENT '摘要状态：pending/processing/completed/failed',
    `audio_url` VARCHAR(500) NULL COMMENT '录音文件URL',
    `audio_file_path` VARCHAR(500) NULL COMMENT '录音文件路径',
    `device_serial` VARCHAR(100) NULL COMMENT '设备序列号',
    `user_id` VARCHAR(64) NULL COMMENT '创建用户ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`),
    INDEX `idx_meeting_user_id` (`user_id`),
    INDEX `idx_meeting_device_serial` (`device_serial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议记录表';

-- =========================================
-- 会议标签表
-- =========================================
CREATE TABLE IF NOT EXISTS `meeting_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`),
    INDEX `idx_tag_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议标签表';

-- =========================================
-- 转写记录表
-- =========================================
CREATE TABLE IF NOT EXISTS `transcript` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转写ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `speaker` VARCHAR(100) NULL COMMENT '说话人',
    `time` VARCHAR(20) NULL COMMENT '时间点',
    `text` TEXT NULL COMMENT '转写文本',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_transcript_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转写记录表';

-- =========================================
-- 推送记录表
-- =========================================
CREATE TABLE IF NOT EXISTS `push_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '推送ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '接收用户ID',
    `push_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    `push_status` VARCHAR(20) DEFAULT 'success' COMMENT '推送状态：success/failed',
    `error_message` TEXT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`),
    INDEX `idx_push_meeting_id` (`meeting_id`),
    INDEX `idx_push_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送记录表';

-- =========================================
-- 测试数据（可选，根据需要执行）
-- =========================================
-- INSERT INTO `device` (`device_id`, `device_serial`, `device_name`, `device_key`, `status`) VALUES
-- ('DEV_001', 'SN123456', '会议室录音设备1', 'KEY123456', 'offline'),
-- ('DEV_002', 'SN789012', '会议室录音设备2', 'KEY789012', 'offline');
