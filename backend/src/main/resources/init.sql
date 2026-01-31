-- 校园订餐系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_ordering DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_ordering;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像',
    `student_id` VARCHAR(50) COMMENT '学号',
    `role` TINYINT DEFAULT 0 COMMENT '角色: 0-顾客, 1-管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商家表
CREATE TABLE IF NOT EXISTS `merchant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `contact_name` VARCHAR(50) COMMENT '联系人姓名',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-审核通过, 2-已驳回, 3-已禁用',
    `reject_reason` VARCHAR(255) COMMENT '驳回原因',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 商家入驻申请表
CREATE TABLE IF NOT EXISTS `merchant_application` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `business_type` VARCHAR(50) COMMENT '经营类型',
    `license_image` VARCHAR(255) COMMENT '营业执照图片',
    `health_cert_image` VARCHAR(255) COMMENT '健康证图片',
    `approval_doc` VARCHAR(255) COMMENT '校园审批文件',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-通过, 2-驳回',
    `reject_reason` VARCHAR(255) COMMENT '驳回原因',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家入驻申请表';

-- 店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `logo` VARCHAR(255) COMMENT '店铺头像',
    `banner` VARCHAR(255) COMMENT '招牌图',
    `description` VARCHAR(500) COMMENT '店铺描述',
    `announcement` VARCHAR(500) COMMENT '店铺公告',
    `business_hours` VARCHAR(100) COMMENT '营业时间',
    `delivery_scope` VARCHAR(255) COMMENT '配送范围',
    `min_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '起送价',
    `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `monthly_sales` INT DEFAULT 0 COMMENT '月销量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-休息中, 1-营业中',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- 菜品分类表
CREATE TABLE IF NOT EXISTS `dish_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS `dish` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `category_id` BIGINT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `image` VARCHAR(255) COMMENT '菜品图片',
    `description` VARCHAR(500) COMMENT '菜品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) COMMENT '原价(特价时显示)',
    `stock` INT DEFAULT 999 COMMENT '库存',
    `stock_warning` INT DEFAULT 10 COMMENT '库存预警值',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `is_special` TINYINT DEFAULT 0 COMMENT '是否特价: 0-否, 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架, 2-待审核',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-驳回',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 菜品规格表
CREATE TABLE IF NOT EXISTS `dish_specification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `name` VARCHAR(50) NOT NULL COMMENT '规格名称',
    `options` JSON COMMENT '规格选项 [{"name":"小份","price":10},{"name":"大份","price":15}]',
    `required` TINYINT DEFAULT 1 COMMENT '是否必选: 0-否, 1-是',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品规格表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `building` VARCHAR(100) COMMENT '楼栋',
    `room` VARCHAR(50) COMMENT '房间号',
    `detail` VARCHAR(255) COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `dish_name` VARCHAR(100) COMMENT '菜品名称',
    `dish_image` VARCHAR(255) COMMENT '菜品图片',
    `specifications` JSON COMMENT '选择的规格',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `shop_name` VARCHAR(100) COMMENT '店铺名称',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `address_id` BIGINT COMMENT '地址ID',
    `address_detail` VARCHAR(255) COMMENT '收货地址详情',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `remark` VARCHAR(255) COMMENT '订单备注',
    `coupon_id` BIGINT COMMENT '使用的优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待支付, 1-待接单, 2-制作中, 3-配送中, 4-已完成, 5-已取消, 6-退款中, 7-已退款',
    `pay_time` DATETIME COMMENT '支付时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `finish_time` DATETIME COMMENT '完成时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) COMMENT '取消原因',
    `estimated_time` INT COMMENT '预计完成时间(分钟)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `dish_name` VARCHAR(100) COMMENT '菜品名称',
    `dish_image` VARCHAR(255) COMMENT '菜品图片',
    `specifications` JSON COMMENT '选择的规格',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `subtotal` DECIMAL(10,2) COMMENT '小计',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT COMMENT '店铺ID(NULL表示平台券)',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-满减券, 2-折扣券',
    `discount_value` DECIMAL(10,2) COMMENT '优惠值(满减金额或折扣率)',
    `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
    `total_count` INT COMMENT '发放总量',
    `remain_count` INT COMMENT '剩余数量',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    `used_time` DATETIME COMMENT '使用时间',
    `order_id` BIGINT COMMENT '使用的订单ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `shop_rating` TINYINT DEFAULT 5 COMMENT '店铺评分(1-5)',
    `delivery_rating` TINYINT DEFAULT 5 COMMENT '配送评分(1-5)',
    `content` TEXT COMMENT '评价内容',
    `images` JSON COMMENT '评价图片',
    `merchant_reply` TEXT COMMENT '商家回复',
    `reply_time` DATETIME COMMENT '回复时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '类型: 1-店铺, 2-菜品',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 投诉/纠纷表
CREATE TABLE IF NOT EXISTS `complaint` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT COMMENT '投诉用户ID',
    `merchant_id` BIGINT COMMENT '投诉商家ID',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-用户投诉, 2-商家申诉',
    `reason` VARCHAR(500) COMMENT '投诉原因',
    `images` JSON COMMENT '证据图片',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已解决',
    `result` VARCHAR(500) COMMENT '处理结果',
    `handler_id` BIGINT COMMENT '处理人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉/纠纷表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_type` TINYINT COMMENT '操作人类型: 1-管理员, 2-商家',
    `operation` VARCHAR(100) COMMENT '操作类型',
    `target_type` VARCHAR(50) COMMENT '目标类型',
    `target_id` BIGINT COMMENT '目标ID',
    `detail` TEXT COMMENT '操作详情',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 插入初始数据
-- 管理员账号
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES 
('admin', '123456', '系统管理员', 1, 1);

-- 测试用户
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `student_id`, `role`, `status`, `balance`) VALUES 
('user1', '123456', '张三', '13800138001', '2024001', 0, 1, 100.00),
('user2', '123456', '李四', '13800138002', '2024002', 0, 1, 50.00);

-- 测试商家
INSERT INTO `merchant` (`username`, `password`, `contact_name`, `phone`, `status`) VALUES 
('merchant1', '123456', '王老板', '13900139001', 1),
('merchant2', '123456', '赵老板', '13900139002', 1);

-- 测试店铺
INSERT INTO `shop` (`merchant_id`, `name`, `description`, `business_hours`, `min_order_amount`, `delivery_fee`, `rating`, `monthly_sales`, `status`) VALUES 
(1, '美味快餐店', '专注校园快餐20年，干净卫生美味', '周一至周日 07:00-22:00', 15.00, 3.00, 4.8, 1520, 1),
(2, '幸福面馆', '手工拉面，汤鲜面劲', '周一至周日 06:30-21:00', 12.00, 2.00, 4.6, 980, 1);

-- 菜品分类
INSERT INTO `dish_category` (`shop_id`, `name`, `sort_order`, `status`) VALUES 
(1, '招牌菜', 1, 1),
(1, '家常菜', 2, 1),
(1, '主食', 3, 1),
(1, '饮品', 4, 1),
(2, '面类', 1, 1),
(2, '小吃', 2, 1),
(2, '饮料', 3, 1);

-- 菜品
INSERT INTO `dish` (`shop_id`, `category_id`, `name`, `description`, `price`, `stock`, `sales`, `status`, `audit_status`) VALUES 
(1, 1, '宫保鸡丁', '经典川菜，鸡肉嫩滑，花生酥脆', 18.00, 100, 520, 1, 1),
(1, 1, '鱼香肉丝', '酸甜可口，肉丝嫩滑', 16.00, 100, 380, 1, 1),
(1, 2, '番茄炒蛋', '家常经典，营养美味', 12.00, 100, 650, 1, 1),
(1, 2, '青椒肉丝', '青椒爽脆，肉丝入味', 15.00, 100, 420, 1, 1),
(1, 3, '米饭', '优质东北大米', 2.00, 500, 1200, 1, 1),
(1, 4, '可乐', '冰镇可口可乐', 3.00, 200, 300, 1, 1),
(2, 5, '牛肉拉面', '手工拉面配秘制牛肉', 22.00, 80, 450, 1, 1),
(2, 5, '西红柿鸡蛋面', '酸甜鲜香，面条爽滑', 15.00, 100, 380, 1, 1),
(2, 6, '煎饺', '香煎水饺，外酥里嫩', 10.00, 60, 280, 1, 1),
(2, 7, '酸梅汤', '自制酸梅汤，消暑解腻', 5.00, 100, 200, 1, 1);

-- 菜品规格示例
INSERT INTO `dish_specification` (`dish_id`, `name`, `options`, `required`, `sort_order`) VALUES 
(1, '份量', '[{"name":"小份","price":0},{"name":"大份","price":5}]', 1, 1),
(1, '辣度', '[{"name":"微辣","price":0},{"name":"中辣","price":0},{"name":"特辣","price":0}]', 1, 2),
(7, '份量', '[{"name":"小碗","price":0},{"name":"大碗","price":4}]', 1, 1),
(7, '加料', '[{"name":"加蛋","price":2},{"name":"加肉","price":5}]', 0, 2);

-- 测试地址
INSERT INTO `address` (`user_id`, `contact_name`, `phone`, `building`, `room`, `detail`, `is_default`) VALUES 
(2, '张三', '13800138001', '1号宿舍楼', '305', '1号宿舍楼305室', 1),
(2, '张三', '13800138001', '图书馆', 'A区', '图书馆A区入口', 0),
(3, '李四', '13800138002', '2号宿舍楼', '512', '2号宿舍楼512室', 1);

-- 系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES 
('platform_name', '校园订餐系统', '平台名称'),
('customer_service_phone', '400-123-4567', '客服电话'),
('order_timeout_minutes', '15', '订单支付超时时间(分钟)'),
('accept_timeout_minutes', '10', '商家接单超时时间(分钟)');

-- 签到记录表
CREATE TABLE IF NOT EXISTS `check_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `check_in_date` DATE NOT NULL COMMENT '签到日期',
    `points_earned` INT DEFAULT 10 COMMENT '获得积分',
    `continuous_days` INT DEFAULT 1 COMMENT '连续签到天数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_date` (`user_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `flash_sale` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `sale_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `stock` INT NOT NULL COMMENT '库存',
    `sold` INT DEFAULT 0 COMMENT '已售',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS `flash_sale_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `flash_sale_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待支付, 1-已支付, 2-已取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 盲盒配置表
CREATE TABLE IF NOT EXISTS `mystery_box` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '盲盒名称',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-早餐, 2-午餐, 3-下午茶, 4-晚餐',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `description` VARCHAR(255) COMMENT '描述',
    `shop_id` BIGINT COMMENT '店铺ID(NULL表示平台盲盒)',
    `dish_count` INT DEFAULT 3 COMMENT '菜品数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒配置表';

-- 盲盒菜品池
CREATE TABLE IF NOT EXISTS `mystery_box_dish` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `box_id` BIGINT NOT NULL COMMENT '盲盒ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `weight` INT DEFAULT 1 COMMENT '权重'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒菜品池';

-- 用户口味偏好表
CREATE TABLE IF NOT EXISTS `user_preference` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `preference_type` VARCHAR(50) NOT NULL COMMENT '偏好类型: spicy/sweet/sour/salty/category',
    `preference_value` VARCHAR(100) NOT NULL COMMENT '偏好值',
    `score` INT DEFAULT 0 COMMENT '偏好分数',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_pref` (`user_id`, `preference_type`, `preference_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户口味偏好表';

-- 动态表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT COMMENT '内容',
    `images` JSON COMMENT '图片列表',
    `dish_id` BIGINT COMMENT '关联菜品ID',
    `shop_id` BIGINT COMMENT '关联店铺ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `comments` INT DEFAULT 0 COMMENT '评论数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `post_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '动态ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 评论表
CREATE TABLE IF NOT EXISTS `post_comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '动态ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT COMMENT '父评论ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 营销活动表
CREATE TABLE IF NOT EXISTS `marketing_campaign` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-流失召回, 2-新品推广, 3-节日活动',
    `target_users` JSON COMMENT '目标用户条件',
    `coupon_id` BIGINT COMMENT '关联优惠券ID',
    `message` VARCHAR(500) COMMENT '推送消息',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-待发送, 2-已发送',
    `send_time` DATETIME COMMENT '发送时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';

-- 库存记录表
CREATE TABLE IF NOT EXISTS `inventory` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `unit` VARCHAR(20) COMMENT '单位',
    `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '当前数量',
    `warning_threshold` DECIMAL(10,2) DEFAULT 10 COMMENT '预警阈值',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存记录表';

-- 库存变动记录
CREATE TABLE IF NOT EXISTS `inventory_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `inventory_id` BIGINT NOT NULL COMMENT '库存ID',
    `change_type` TINYINT COMMENT '变动类型: 1-入库, 2-出库, 3-盘点',
    `change_quantity` DECIMAL(10,2) COMMENT '变动数量',
    `remark` VARCHAR(255) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变动记录';

-- 系统公告表
CREATE TABLE IF NOT EXISTS `announcement` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-系统公告, 2-活动通知, 3-维护通知',
    `target` TINYINT DEFAULT 0 COMMENT '目标: 0-全部, 1-用户, 2-商家',
    `is_popup` TINYINT DEFAULT 0 COMMENT '是否弹窗: 0-否, 1-是',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 平台活动表
CREATE TABLE IF NOT EXISTS `platform_activity` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `description` TEXT COMMENT '活动描述',
    `banner` VARCHAR(255) COMMENT '活动横幅图',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-满减活动, 2-折扣活动, 3-免配送费',
    `rules` JSON COMMENT '活动规则',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-进行中, 2-已结束',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台活动表';

-- 扩展订单表支持预约
ALTER TABLE `order` ADD COLUMN IF NOT EXISTS `is_scheduled` TINYINT DEFAULT 0 COMMENT '是否预约订单';
ALTER TABLE `order` ADD COLUMN IF NOT EXISTS `scheduled_time` DATETIME COMMENT '预约时间';
ALTER TABLE `order` ADD COLUMN IF NOT EXISTS `pickup_code` VARCHAR(10) COMMENT '取餐码';

-- 扩展用户表支持积分
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `points` INT DEFAULT 0 COMMENT '积分';

-- 创建索引
CREATE INDEX idx_user_phone ON `user`(`phone`);
CREATE INDEX idx_merchant_status ON `merchant`(`status`);
CREATE INDEX idx_shop_merchant ON `shop`(`merchant_id`);
CREATE INDEX idx_dish_shop ON `dish`(`shop_id`);
CREATE INDEX idx_dish_category ON `dish`(`category_id`);
CREATE INDEX idx_order_user ON `order`(`user_id`);
CREATE INDEX idx_order_shop ON `order`(`shop_id`);
CREATE INDEX idx_order_status ON `order`(`status`);
CREATE INDEX idx_order_created ON `order`(`created_at`);
CREATE INDEX idx_cart_user ON `cart`(`user_id`);
CREATE INDEX idx_review_shop ON `review`(`shop_id`);
CREATE INDEX idx_check_in_user ON `check_in`(`user_id`);
CREATE INDEX idx_flash_sale_shop ON `flash_sale`(`shop_id`);
CREATE INDEX idx_post_user ON `post`(`user_id`);
CREATE INDEX idx_inventory_shop ON `inventory`(`shop_id`);
