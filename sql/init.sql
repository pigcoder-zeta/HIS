-- =============================================
-- 智慧医疗管理系统 - 数据库初始化脚本
-- Version: 1.0
-- Database: MySQL 8.0+
-- =============================================

CREATE DATABASE IF NOT EXISTS smart_healthcare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_healthcare;

-- =============================================
-- 1. 系统角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码(ROLE_PATIENT/ROLE_DOCTOR/ROLE_PHARMACIST/ROLE_MEDICAL_ADMIN/ROLE_SYSTEM_ADMIN)',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- =============================================
-- 2. 科室表
-- =============================================
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dept_name` VARCHAR(100) NOT NULL COMMENT '科室名称',
    `dept_code` VARCHAR(50) NOT NULL COMMENT '科室编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父科室ID(0表示一级科室)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '科室描述',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '科室位置',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- =============================================
-- 3. 系统用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录名/工号',
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role_id` BIGINT NOT NULL COMMENT '关联角色ID',
    `dept_id` BIGINT DEFAULT NULL COMMENT '所属科室ID(仅医护人员)',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号(AES加密存储)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender` TINYINT DEFAULT 0 COMMENT '性别(0-未知,1-男,2-女)',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-正常)',
    `title` VARCHAR(50) DEFAULT NULL COMMENT '职称(主任医师/副主任医师/主治医师等)',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- =============================================
-- 4. 门诊排班表
-- =============================================
DROP TABLE IF EXISTS `biz_schedule`;
CREATE TABLE `biz_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `dept_id` BIGINT NOT NULL COMMENT '科室ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `time_slot` TINYINT NOT NULL COMMENT '时段(1-上午,2-下午,3-夜诊)',
    `total_quota` INT NOT NULL DEFAULT 0 COMMENT '总号源数',
    `left_quota` INT NOT NULL DEFAULT 0 COMMENT '剩余号源数',
    `fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '挂号费',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-停诊,1-正常)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_dept_date` (`dept_id`, `schedule_date`),
    KEY `idx_date_slot` (`schedule_date`, `time_slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊排班表';

-- =============================================
-- 5. 预约挂号单
-- =============================================
DROP TABLE IF EXISTS `biz_appointment`;
CREATE TABLE `biz_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `schedule_id` BIGINT NOT NULL COMMENT '关联排班ID',
    `doctor_id` BIGINT DEFAULT NULL COMMENT '医生ID(冗余)',
    `dept_id` BIGINT DEFAULT NULL COMMENT '科室ID(冗余)',
    `schedule_date` DATE DEFAULT NULL COMMENT '就诊日期(冗余)',
    `time_slot` TINYINT DEFAULT NULL COMMENT '时段(冗余)',
    `queue_number` INT DEFAULT NULL COMMENT '就诊排号',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待支付,1-已预约,2-就诊中,3-已完成,4-已取消,5-已爽约)',
    `fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '挂号费',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约挂号单';

-- =============================================
-- 6. 电子病历表
-- =============================================
DROP TABLE IF EXISTS `biz_medical_record`;
CREATE TABLE `biz_medical_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `doctor_id` BIGINT NOT NULL COMMENT '接诊医生ID',
    `appointment_id` BIGINT DEFAULT NULL COMMENT '关联挂号单ID',
    `chief_complaint` TEXT COMMENT '主诉(S)',
    `present_illness` TEXT COMMENT '现病史(S)',
    `physical_examination` TEXT COMMENT '查体结果(O)',
    `auxiliary_exam` TEXT COMMENT '辅助检查结果(O)',
    `diagnosis` TEXT COMMENT '初步诊断(A)',
    `treatment_plan` TEXT COMMENT '治疗方案(P)',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-草稿,1-已签名,2-已归档,3-已锁定)',
    `signed_time` DATETIME DEFAULT NULL COMMENT '签名确认时间',
    `signature_code` VARCHAR(255) DEFAULT NULL COMMENT '数字签名',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子病历表';

-- =============================================
-- 7. 电子处方表
-- =============================================
DROP TABLE IF EXISTS `biz_prescription`;
CREATE TABLE `biz_prescription` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_id` BIGINT NOT NULL COMMENT '关联病历ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `doctor_id` BIGINT NOT NULL COMMENT '开方医生ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待审核,1-已审核,2-已发药,3-已驳回)',
    `pharmacist_id` BIGINT DEFAULT NULL COMMENT '审核药师ID',
    `audit_opinion` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '处方总金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子处方表';

-- =============================================
-- 8. 处方明细表
-- =============================================
DROP TABLE IF EXISTS `biz_prescription_item`;
CREATE TABLE `biz_prescription_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
    `drug_id` BIGINT NOT NULL COMMENT '药品ID',
    `drug_name` VARCHAR(200) NOT NULL COMMENT '药品名称(冗余)',
    `dosage` VARCHAR(100) NOT NULL COMMENT '用量(如"0.3g/次")',
    `usage_method` VARCHAR(100) NOT NULL COMMENT '用法(如"口服,每日3次")',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_prescription_id` (`prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- =============================================
-- 9. 药品库存字典
-- =============================================
DROP TABLE IF EXISTS `biz_drug`;
CREATE TABLE `biz_drug` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drug_code` VARCHAR(50) NOT NULL COMMENT '药品国药准字编码',
    `drug_name` VARCHAR(200) NOT NULL COMMENT '药品名称',
    `generic_name` VARCHAR(200) DEFAULT NULL COMMENT '通用名',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '药品分类',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
    `manufacturer` VARCHAR(200) DEFAULT NULL COMMENT '生产厂家',
    `unit` VARCHAR(20) DEFAULT '盒' COMMENT '单位',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `stock_count` INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    `safe_threshold` INT NOT NULL DEFAULT 100 COMMENT '安全库存预警线',
    `expiry_date` DATE DEFAULT NULL COMMENT '有效期至',
    `is_high_risk` TINYINT DEFAULT 0 COMMENT '是否高危药品(0-否,1-是)',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drug_code` (`drug_code`),
    KEY `idx_stock_threshold` (`stock_count`, `safe_threshold`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品库存字典';

-- =============================================
-- 10. 药品出入库流水表
-- =============================================
DROP TABLE IF EXISTS `biz_drug_transaction`;
CREATE TABLE `biz_drug_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drug_id` BIGINT NOT NULL COMMENT '药品ID',
    `type` TINYINT NOT NULL COMMENT '类型(1-入库,2-出库,3-发药扣减,4-退货入库)',
    `quantity` INT NOT NULL COMMENT '数量',
    `before_stock` INT NOT NULL COMMENT '操作前库存',
    `after_stock` INT NOT NULL COMMENT '操作后库存',
    `batch_no` VARCHAR(100) DEFAULT NULL COMMENT '批号',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_drug_id` (`drug_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品出入库流水表';

-- =============================================
-- 11. 体检套餐表
-- =============================================
DROP TABLE IF EXISTS `biz_exam_package`;
CREATE TABLE `biz_exam_package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `package_name` VARCHAR(200) NOT NULL COMMENT '套餐名称',
    `description` TEXT COMMENT '套餐描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    `suitable_crowd` VARCHAR(255) DEFAULT NULL COMMENT '适合人群',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-停用,1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐表';

-- =============================================
-- 12. 体检预约表
-- =============================================
DROP TABLE IF EXISTS `biz_exam_appointment`;
CREATE TABLE `biz_exam_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `package_id` BIGINT NOT NULL COMMENT '套餐ID',
    `appointment_date` DATE NOT NULL COMMENT '预约日期',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待支付,1-已预约,2-体检中,3-已完成,4-已取消)',
    `fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '费用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_appointment_date` (`appointment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检预约表';

-- =============================================
-- 13. 体检报告表
-- =============================================
DROP TABLE IF EXISTS `biz_exam_report`;
CREATE TABLE `biz_exam_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_appointment_id` BIGINT NOT NULL COMMENT '体检预约ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `doctor_id` BIGINT DEFAULT NULL COMMENT '出报告医生ID',
    `content` TEXT COMMENT '报告内容',
    `summary` TEXT COMMENT '总检结论',
    `suggestion` TEXT COMMENT '健康建议',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-待检查,1-检查中,2-已完成,3-已发布)',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告表';

-- =============================================
-- 14. 排班规则表
-- =============================================
DROP TABLE IF EXISTS `biz_schedule_rule`;
CREATE TABLE `biz_schedule_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型(MAX_WEEKLY_HOURS/MIN_REST_DAYS等)',
    `rule_value` VARCHAR(255) NOT NULL COMMENT '规则值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '规则描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班规则表';

-- =============================================
-- 15. 系统日志表
-- =============================================
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `method` VARCHAR(255) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-失败,1-成功)',
    `error_msg` TEXT COMMENT '错误信息',
    `cost_time` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- =============================================
-- 初始化角色数据
-- =============================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
('ROLE_PATIENT', '患者', '系统服务对象，进行医疗服务交互'),
('ROLE_DOCTOR', '门诊医生', '负责患者的日常诊疗工作'),
('ROLE_PHARMACIST', '药房管理员', '负责医院药品物资流转与安全'),
('ROLE_MEDICAL_ADMIN', '医务科管理人员', '统筹医疗资源和医护人员调度'),
('ROLE_SYSTEM_ADMIN', '系统管理员', '维护系统底层运行与数据安全');

-- =============================================
-- 初始化科室数据
-- =============================================
INSERT INTO `sys_department` (`dept_name`, `dept_code`, `parent_id`, `description`, `address`) VALUES
('内科', 'DEPT_INTERNAL', 0, '内科综合门诊', '门诊楼2层A区'),
('神经内科', 'DEPT_NEUROLOGY', 1, '神经系统疾病诊治', '门诊楼2层B区'),
('心血管内科', 'DEPT_CARDIOLOGY', 1, '心血管疾病诊治', '门诊楼2层C区'),
('外科', 'DEPT_SURGERY', 0, '外科综合门诊', '门诊楼3层A区'),
('骨科', 'DEPT_ORTHOPEDICS', 4, '骨科疾病诊治', '门诊楼3层B区'),
('儿科', 'DEPT_PEDIATRICS', 0, '儿童疾病诊治', '门诊楼1层A区'),
('妇产科', 'DEPT_OBSTETRICS', 0, '妇产科综合门诊', '门诊楼4层A区'),
('耳鼻喉科', 'DEPT_ENT', 0, '耳鼻喉疾病诊治', '门诊楼3层C区'),
('眼科', 'DEPT_OPHTHALMOLOGY', 0, '眼科疾病诊治', '门诊楼3层D区'),
('急诊科', 'DEPT_EMERGENCY', 0, '急诊综合救治', '急诊楼1层'),
('药房', 'DEPT_PHARMACY', 0, '药品管理与发放', '门诊楼1层大厅'),
('体检中心', 'DEPT_EXAM_CENTER', 0, '健康体检服务', '体检楼1-3层');

-- =============================================
-- 初始化管理员账户(密码: admin123)
-- =============================================
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role_id`, `phone`, `email`, `gender`, `title`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 5, '13800000000', 'admin@hospital.com', 1, '高级工程师', 1);

-- =============================================
-- 初始化排班规则
-- =============================================
INSERT INTO `biz_schedule_rule` (`rule_name`, `rule_type`, `rule_value`, `description`) VALUES
('周最大工时', 'MAX_WEEKLY_HOURS', '40', '医生单周总排班时长不得超过40小时'),
('最少休息日', 'MIN_REST_DAYS', '2', '每人每周至少休息2天'),
('主任医师半天限制', 'CHIEF_HALF_DAY_LIMIT', '2', '主任医师每周最多排2个半天门诊'),
('取消预约时限', 'CANCEL_DEADLINE_HOURS', '2', '距就诊时间不足2小时不可在线取消预约'),
('同科室同日限制', 'SAME_DEPT_SAME_DAY_LIMIT', '1', '患者同一天同一科室最多预约1次');
