-- =============================================
-- 智能校园外卖系统 - 新增表迁移脚本
-- 功能：AI智能推荐 + 预约点餐错峰取餐
-- =============================================

USE campus_ordering;

-- =============================================
-- 1. 用户画像表
-- =============================================
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `taste_tags` JSON COMMENT '口味标签 ["辣","清淡","甜"]',
    `cuisine_preferences` JSON COMMENT '菜系偏好权重 {"川菜":0.8,"粤菜":0.3}',
    `allergy_info` JSON COMMENT '过敏信息 ["花生","海鲜"]',
    `dislike_ingredients` JSON COMMENT '不喜欢的食材 ["香菜","葱"]',
    `avg_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平均消费金额',
    `order_count` INT DEFAULT 0 COMMENT '历史订单数',
    `preferred_time_slots` JSON COMMENT '常用时段 ["12:00","18:30"]',
    `preferred_shops` JSON COMMENT '常去店铺ID列表',
    `health_goal` TINYINT DEFAULT 3 COMMENT '健康目标: 1-减脂 2-增肌 3-均衡',
    `daily_calorie_target` INT DEFAULT 2000 COMMENT '每日目标热量(千卡)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';

-- =============================================
-- 2. 菜品营养表
-- =============================================
CREATE TABLE IF NOT EXISTS `dish_nutrition` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `dish_id` BIGINT NOT NULL UNIQUE COMMENT '菜品ID',
    `calories` INT DEFAULT 0 COMMENT '热量(千卡)',
    `protein` DECIMAL(5,1) DEFAULT 0 COMMENT '蛋白质(克)',
    `carbs` DECIMAL(5,1) DEFAULT 0 COMMENT '碳水化合物(克)',
    `fat` DECIMAL(5,1) DEFAULT 0 COMMENT '脂肪(克)',
    `fiber` DECIMAL(5,1) DEFAULT 0 COMMENT '膳食纤维(克)',
    `sodium` INT DEFAULT 0 COMMENT '钠(毫克)',
    `sugar` DECIMAL(5,1) DEFAULT 0 COMMENT '糖(克)',
    `tags` JSON COMMENT '营养标签 ["高蛋白","低脂","素食","无辣"]',
    `ingredients` JSON COMMENT '主要食材 ["鸡肉","花生","辣椒"]',
    `allergens` JSON COMMENT '过敏原 ["花生","大豆"]',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_dish_id` (`dish_id`),
    FOREIGN KEY (`dish_id`) REFERENCES `dish`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品营养表';

-- =============================================
-- 3. 预约时段表
-- =============================================
CREATE TABLE IF NOT EXISTS `pickup_timeslot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `date` DATE NOT NULL COMMENT '日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `capacity` INT DEFAULT 20 COMMENT '该时段最大订单数',
    `booked_count` INT DEFAULT 0 COMMENT '已预约数量',
    `discount_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率 0.85表示85折',
    `peak_level` TINYINT DEFAULT 1 COMMENT '繁忙等级: 1-低峰(绿) 2-平峰(黄) 3-高峰(红)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-关闭 1-开放 2-已满',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_shop_datetime` (`shop_id`, `date`, `start_time`),
    INDEX `idx_shop_date` (`shop_id`, `date`),
    INDEX `idx_date_status` (`date`, `status`),
    FOREIGN KEY (`shop_id`) REFERENCES `shop`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约时段表';

-- =============================================
-- 4. 预约订单表
-- =============================================
CREATE TABLE IF NOT EXISTS `reservation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL UNIQUE COMMENT '订单ID',
    `timeslot_id` BIGINT NOT NULL COMMENT '时段ID',
    `pickup_code` VARCHAR(6) NOT NULL COMMENT '取餐码(6位数字)',
    `pickup_date` DATE NOT NULL COMMENT '取餐日期',
    `pickup_start_time` TIME NOT NULL COMMENT '取餐开始时间',
    `pickup_end_time` TIME NOT NULL COMMENT '取餐结束时间',
    `estimated_ready_time` DATETIME COMMENT '预计完成时间',
    `actual_ready_time` DATETIME COMMENT '实际完成时间',
    `pickup_time` DATETIME COMMENT '实际取餐时间',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '错峰优惠金额',
    `status` TINYINT DEFAULT 0 COMMENT '0-待制作 1-制作中 2-已完成待取 3-已取餐 4-超时未取 5-已取消',
    `reminder_sent` TINYINT DEFAULT 0 COMMENT '是否已发送提醒: 0-未发送 1-已发送',
    `timeout_handled` TINYINT DEFAULT 0 COMMENT '超时是否已处理',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_timeslot_id` (`timeslot_id`),
    INDEX `idx_pickup_code` (`pickup_code`),
    INDEX `idx_pickup_date` (`pickup_date`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`timeslot_id`) REFERENCES `pickup_timeslot`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- =============================================
-- 5. 推荐日志表
-- =============================================
CREATE TABLE IF NOT EXISTS `recommendation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_id` VARCHAR(50) COMMENT '会话ID',
    `recommend_type` VARCHAR(20) NOT NULL COMMENT '推荐类型: home-首页 search-搜索 cart-购物车 detail-详情页',
    `recommend_reason` VARCHAR(100) COMMENT '推荐原因',
    `dish_ids` JSON NOT NULL COMMENT '推荐的菜品ID列表',
    `dish_scores` JSON COMMENT '各菜品的推荐分数',
    `clicked_dish_id` BIGINT COMMENT '用户点击的菜品ID',
    `ordered_dish_id` BIGINT COMMENT '最终下单的菜品ID',
    `click_time` DATETIME COMMENT '点击时间',
    `order_time` DATETIME COMMENT '下单时间',
    `scene_context` JSON COMMENT '场景上下文 {"time":"12:00","weather":"晴","day_of_week":1}',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_recommend_type` (`recommend_type`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐日志表';

-- =============================================
-- 6. 每日营养摄入记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `nutrition_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `date` DATE NOT NULL COMMENT '日期',
    `total_calories` INT DEFAULT 0 COMMENT '总热量(千卡)',
    `total_protein` DECIMAL(6,1) DEFAULT 0 COMMENT '总蛋白质(克)',
    `total_carbs` DECIMAL(6,1) DEFAULT 0 COMMENT '总碳水(克)',
    `total_fat` DECIMAL(6,1) DEFAULT 0 COMMENT '总脂肪(克)',
    `total_fiber` DECIMAL(6,1) DEFAULT 0 COMMENT '总膳食纤维(克)',
    `total_sodium` INT DEFAULT 0 COMMENT '总钠(毫克)',
    `meal_count` INT DEFAULT 0 COMMENT '当日餐数',
    `order_ids` JSON COMMENT '当日订单ID列表',
    `calorie_goal` INT DEFAULT 2000 COMMENT '当日热量目标',
    `goal_achieved` TINYINT DEFAULT 0 COMMENT '是否达标: 0-未达标 1-达标 2-超标',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_date` (`user_id`, `date`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_date` (`date`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日营养摄入记录表';

-- =============================================
-- 7. 店铺时段配置表（商家配置用）
-- =============================================
CREATE TABLE IF NOT EXISTS `shop_timeslot_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL UNIQUE COMMENT '店铺ID',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用预约功能',
    `slot_duration` INT DEFAULT 15 COMMENT '时段间隔(分钟)',
    `default_capacity` INT DEFAULT 20 COMMENT '默认每时段容量',
    `peak_hours` JSON COMMENT '高峰时段 [{"start":"11:30","end":"12:30"},{"start":"17:30","end":"18:30"}]',
    `off_peak_discount` DECIMAL(3,2) DEFAULT 0.85 COMMENT '低峰折扣率',
    `normal_discount` DECIMAL(3,2) DEFAULT 0.95 COMMENT '平峰折扣率',
    `advance_days` INT DEFAULT 3 COMMENT '可提前预约天数',
    `pickup_timeout_minutes` INT DEFAULT 30 COMMENT '取餐超时时间(分钟)',
    `auto_generate` TINYINT DEFAULT 1 COMMENT '是否自动生成时段',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`shop_id`) REFERENCES `shop`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺时段配置表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 为现有菜品添加营养信息
INSERT INTO `dish_nutrition` (`dish_id`, `calories`, `protein`, `carbs`, `fat`, `fiber`, `sodium`, `tags`, `ingredients`, `allergens`) VALUES
(1, 350, 25.0, 15.0, 18.0, 2.0, 800, '["高蛋白","微辣"]', '["鸡胸肉","花生","干辣椒","青椒"]', '["花生"]'),
(2, 280, 18.0, 20.0, 12.0, 3.0, 650, '["均衡"]', '["猪肉","木耳","胡萝卜","青椒"]', '[]'),
(3, 180, 12.0, 8.0, 10.0, 1.5, 400, '["低脂","素食可选"]', '["鸡蛋","番茄"]', '["鸡蛋"]'),
(4, 260, 20.0, 10.0, 14.0, 2.5, 550, '["高蛋白"]', '["猪肉","青椒"]', '[]'),
(5, 130, 3.0, 28.0, 0.5, 0.5, 5, '["主食","低脂"]', '["大米"]', '[]'),
(6, 140, 0.0, 35.0, 0.0, 0.0, 45, '["饮品"]', '["碳酸水","糖"]', '[]'),
(7, 450, 30.0, 45.0, 15.0, 3.0, 1200, '["高蛋白","主食"]', '["牛肉","面条","香菜"]', '["麸质"]'),
(8, 320, 15.0, 50.0, 8.0, 4.0, 600, '["均衡","主食"]', '["面条","鸡蛋","番茄"]', '["鸡蛋","麸质"]'),
(9, 280, 12.0, 30.0, 12.0, 2.0, 500, '["小吃"]', '["猪肉","面粉","韭菜"]', '["麸质"]'),
(10, 60, 0.5, 15.0, 0.0, 0.0, 20, '["饮品","低热量"]', '["乌梅","山楂","冰糖"]', '[]');

-- 为测试用户创建画像
INSERT INTO `user_profile` (`user_id`, `taste_tags`, `cuisine_preferences`, `avg_order_amount`, `order_count`, `preferred_time_slots`, `health_goal`, `daily_calorie_target`) VALUES
(2, '["微辣","咸香"]', '{"川菜":0.7,"家常菜":0.8}', 25.00, 15, '["12:00","18:00"]', 3, 2000),
(3, '["清淡","少油"]', '{"粤菜":0.6,"面食":0.9}', 18.00, 8, '["12:30","19:00"]', 1, 1500);

-- 为测试店铺创建时段配置
INSERT INTO `shop_timeslot_config` (`shop_id`, `enabled`, `slot_duration`, `default_capacity`, `peak_hours`, `off_peak_discount`, `normal_discount`) VALUES
(1, 1, 15, 25, '[{"start":"11:30","end":"12:30"},{"start":"17:30","end":"18:30"}]', 0.85, 0.95),
(2, 1, 15, 20, '[{"start":"11:30","end":"12:30"},{"start":"17:30","end":"18:30"}]', 0.85, 0.95);

-- 为今天和未来3天生成示例时段（店铺1）
INSERT INTO `pickup_timeslot` (`shop_id`, `date`, `start_time`, `end_time`, `capacity`, `discount_rate`, `peak_level`) VALUES
-- 今天的时段
(1, CURDATE(), '11:00', '11:15', 25, 0.85, 1),
(1, CURDATE(), '11:15', '11:30', 25, 0.95, 2),
(1, CURDATE(), '11:30', '11:45', 25, 1.00, 3),
(1, CURDATE(), '11:45', '12:00', 25, 1.00, 3),
(1, CURDATE(), '12:00', '12:15', 25, 1.00, 3),
(1, CURDATE(), '12:15', '12:30', 25, 1.00, 3),
(1, CURDATE(), '12:30', '12:45', 25, 0.95, 2),
(1, CURDATE(), '12:45', '13:00', 25, 0.95, 2),
(1, CURDATE(), '13:00', '13:15', 25, 0.85, 1),
(1, CURDATE(), '17:00', '17:15', 25, 0.85, 1),
(1, CURDATE(), '17:15', '17:30', 25, 0.95, 2),
(1, CURDATE(), '17:30', '17:45', 25, 1.00, 3),
(1, CURDATE(), '17:45', '18:00', 25, 1.00, 3),
(1, CURDATE(), '18:00', '18:15', 25, 1.00, 3),
(1, CURDATE(), '18:15', '18:30', 25, 1.00, 3),
(1, CURDATE(), '18:30', '18:45', 25, 0.95, 2),
(1, CURDATE(), '18:45', '19:00', 25, 0.95, 2),
(1, CURDATE(), '19:00', '19:15', 25, 0.85, 1);

COMMIT;
