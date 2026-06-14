CREATE DATABASE IF NOT EXISTS personal_planner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE personal_planner;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    status INT DEFAULT 1 COMMENT '1=正常,0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 目标表
CREATE TABLE goal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    level ENUM('year','quarter','month','week') DEFAULT 'month',
    parent_id BIGINT DEFAULT NULL COMMENT '父目标ID',
    status ENUM('active','done','archived') DEFAULT 'active',
    progress INT DEFAULT 0 COMMENT '0-100',
    start_date DATE,
    end_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关键结果表
CREATE TABLE key_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    target_value DOUBLE DEFAULT 0,
    current_value DOUBLE DEFAULT 0,
    unit VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_goal_id (goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务表
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('todo','doing','done','abandoned') DEFAULT 'todo',
    priority ENUM('low','medium','high','urgent') DEFAULT 'medium',
    planned_minutes INT COMMENT '预计耗时(分钟)',
    actual_minutes INT COMMENT '实际耗时(分钟)',
    start_at DATETIME COMMENT '实际开始时间',
    finish_at DATETIME COMMENT '实际完成时间',
    due_at DATE COMMENT '截止日期',
    planned_date DATE COMMENT '计划日期',
    tags JSON COMMENT '标签列表（自由文本）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_planned_date (planned_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 子任务表
CREATE TABLE sub_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    done TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#409EFF',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务标签关联表
CREATE TABLE task_tag (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 目标任务关联表
CREATE TABLE goal_task (
    goal_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    contribution INT DEFAULT 1 COMMENT '贡献度',
    PRIMARY KEY (goal_id, task_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 每日复盘表
CREATE TABLE daily_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    review_date DATE NOT NULL UNIQUE,
    done_summary TEXT COMMENT '今日完成',
    undone_summary TEXT COMMENT '今日未完成',
    gains TEXT COMMENT '今日收获',
    improvements TEXT COMMENT '今日改进',
    mood_score INT COMMENT '心情打分1-10',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_review_date (review_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 技能画像表
CREATE TABLE skill_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL COMMENT '一级分类：Java后端/前端/AI/基础设施/软技能',
    skill_name VARCHAR(100) NOT NULL,
    level ENUM('S','A','B','C','D') DEFAULT 'C',
    keywords TEXT,
    notes TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_skill (user_id, category, skill_name),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 画像变更历史表
CREATE TABLE profile_change_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    old_level VARCHAR(10),
    new_level VARCHAR(10) NOT NULL,
    change_type ENUM('increase','decrease','new','manual','ai') NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认测试用户 (密码: test123)
INSERT INTO sys_user (username, password, nickname, status) VALUES
('test', '$2a$10$p.r5z/9y0rVykonNw7qVQeDBsWfLW1UWLrqMd6vRgsw6WFmI4OqtO', '测试用户', 1);

-- 画像文档表（保存 AI 生成 / 用户编辑后的 MD 画像）
CREATE TABLE IF NOT EXISTS profile_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    markdown MEDIUMTEXT NOT NULL,
    source_md MEDIUMTEXT COMMENT '生成画像时使用的原始文档',
    suggested_skills JSON,
    version INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;