-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия на сървъра:            10.4.28-MariaDB - Source distribution
-- ОС на сървъра:                osx10.10
-- HeidiSQL Версия:              12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Дъмп на структурата на БД employee_db
CREATE DATABASE IF NOT EXISTS `employee_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `employee_db`;

-- Дъмп структура за таблица employee_db.departments
CREATE TABLE IF NOT EXISTS `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `department_code` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `budget` decimal(15,2) DEFAULT NULL,
  `status` enum('ACTIVE','DISSOLVED','INACTIVE','MERGED','SUSPENDED','UNDER_REVIEW') NOT NULL,
  `parent_department_id` bigint(20) DEFAULT NULL,
  `manager_id` bigint(20) DEFAULT NULL,
  `cost_center` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(50) DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `department_code` (`department_code`),
  KEY `idx_department_code` (`department_code`),
  KEY `idx_department_status` (`status`),
  KEY `idx_department_parent` (`parent_department_id`),
  KEY `idx_department_manager` (`manager_id`),
  CONSTRAINT `fk_department_manager` FOREIGN KEY (`manager_id`) REFERENCES `employees` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_department_parent` FOREIGN KEY (`parent_department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.departments: ~5 rows (приблизителен брой)
INSERT INTO `departments` (`id`, `department_code`, `name`, `description`, `location`, `budget`, `status`, `parent_department_id`, `manager_id`, `cost_center`, `email`, `phone`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
	(1, 'IT', 'Information Technology', 'Responsible for all IT operations and software development', 'Building A, Floor 3', 500000.00, 'ACTIVE', NULL, NULL, NULL, 'it@company.com', '+1-555-0100', '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(2, 'HR', 'Human Resources', 'Manages employee relations, recruitment, and benefits', 'Building B, Floor 2', 200000.00, 'ACTIVE', NULL, 2, NULL, 'hr@company.com', '+1-555-0200', '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(3, 'FIN', 'Finance', 'Handles financial planning, accounting, and budgeting', 'Building B, Floor 1', 300000.00, 'ACTIVE', NULL, 4, NULL, 'finance@company.com', '+1-555-0300', '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(4, 'MKT', 'Marketing', 'Marketing and promotional activities', 'Building A, Floor 2', 250000.00, 'ACTIVE', NULL, 5, NULL, 'marketing@company.com', '+1-555-0400', '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(5, 'OPS', 'Operations', 'Day-to-day business operations', 'Building C, Floor 1', 400000.00, 'ACTIVE', NULL, NULL, NULL, 'operations@company.com', '+1-555-0500', '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL);

-- Дъмп структура за таблица employee_db.employees
CREATE TABLE IF NOT EXISTS `employees` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` varchar(20) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `gender` varchar(10) NOT NULL,
  `address` varchar(500) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `postal_code` varchar(10) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `job_title` varchar(100) NOT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `current_position_id` bigint(20) DEFAULT NULL,
  `manager_id` bigint(20) DEFAULT NULL,
  `hire_date` date NOT NULL,
  `termination_date` date DEFAULT NULL,
  `employment_type` enum('CONTRACT','FULL_TIME','INTERN','PART_TIME','TEMPORARY') NOT NULL,
  `salary` decimal(12,2) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','ON_LEAVE','PROBATION','TERMINATED') NOT NULL,
  `emergency_contact_name` varchar(100) DEFAULT NULL,
  `emergency_contact_phone` varchar(20) DEFAULT NULL,
  `emergency_contact_relationship` varchar(50) DEFAULT NULL,
  `ssn` varchar(11) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `profile_picture_url` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_id` (`employee_id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_employee_email` (`email`),
  KEY `idx_employee_employee_id` (`employee_id`),
  KEY `idx_employee_status` (`status`),
  KEY `idx_employee_department` (`department_id`),
  KEY `idx_employee_manager` (`manager_id`),
  KEY `idx_employee_current_position` (`current_position_id`),
  CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `employees_ibfk_2` FOREIGN KEY (`current_position_id`) REFERENCES `positions` (`id`),
  CONSTRAINT `employees_ibfk_3` FOREIGN KEY (`manager_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.employees: ~6 rows (приблизителен брой)
INSERT INTO `employees` (`id`, `employee_id`, `first_name`, `last_name`, `email`, `phone`, `birth_date`, `gender`, `address`, `city`, `state`, `postal_code`, `country`, `job_title`, `department_id`, `current_position_id`, `manager_id`, `hire_date`, `termination_date`, `employment_type`, `salary`, `status`, `emergency_contact_name`, `emergency_contact_phone`, `emergency_contact_relationship`, `ssn`, `notes`, `profile_picture_url`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
	(1, 'EMP001', 'John', 'Doe', 'john.doe@company.com', '+1-555-1001', '1985-03-15', 'MALE', NULL, NULL, NULL, NULL, NULL, 'Senior Software Engineer', 1, 1, NULL, '2020-01-15', NULL, 'FULL_TIME', 95000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(2, 'EMP002', 'Jane', 'Smith', 'jane.smith@company.com', '+1-555-1002', '1990-07-22', 'FEMALE', NULL, NULL, NULL, NULL, NULL, 'HR Manager', 2, 4, NULL, '2019-03-10', NULL, 'FULL_TIME', 85000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(3, 'EMP003', 'Alice', 'Johnson', 'alice.johnson@company.com', '+1-555-1003', '1988-11-08', 'FEMALE', NULL, NULL, NULL, NULL, NULL, 'Software Engineer', 1, 2, 1, '2021-06-01', NULL, 'FULL_TIME', 75000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(4, 'EMP004', 'Bob', 'Wilson', 'bob.wilson@company.com', '+1-555-1004', '1982-09-30', 'MALE', NULL, NULL, NULL, NULL, NULL, 'Financial Analyst', 3, 6, NULL, '2018-11-20', NULL, 'FULL_TIME', 65000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(5, 'EMP005', 'Carol', 'Brown', 'carol.brown@company.com', '+1-555-1005', '1992-05-14', 'FEMALE', NULL, NULL, NULL, NULL, NULL, 'Marketing Manager', 4, 7, 2, '2022-02-28', NULL, 'FULL_TIME', 80000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(7, 'EMP221', 'Rumen', 'TheDevil', 'rumen@thedevil.com', '+359894344454', NULL, 'MALE', '55 Maksim Gorki St, Sredets, State 8300', NULL, NULL, NULL, NULL, 'PachkovLTD', NULL, NULL, NULL, '2024-01-15', NULL, 'FULL_TIME', 75000.00, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-18 19:13:54', '2025-09-18 19:13:54', NULL, NULL);

-- Дъмп структура за таблица employee_db.employee_position_history
CREATE TABLE IF NOT EXISTS `employee_position_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `position_id` bigint(20) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_current` tinyint(1) DEFAULT 0,
  `reason_for_change` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `change_reason` varchar(500) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `department_at_time` varchar(100) DEFAULT NULL,
  `ending_salary` decimal(12,2) DEFAULT NULL,
  `manager_at_time` varchar(100) DEFAULT NULL,
  `starting_salary` decimal(12,2) DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_eph_employee` (`employee_id`),
  KEY `idx_eph_position` (`position_id`),
  KEY `idx_eph_start_date` (`start_date`),
  KEY `idx_eph_end_date` (`end_date`),
  KEY `idx_eph_is_current` (`is_current`),
  CONSTRAINT `employee_position_history_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `employee_position_history_ibfk_2` FOREIGN KEY (`position_id`) REFERENCES `positions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.employee_position_history: ~0 rows (приблизителен брой)

-- Дъмп структура за таблица employee_db.leave_balances
CREATE TABLE IF NOT EXISTS `leave_balances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `leave_type_id` bigint(20) NOT NULL,
  `year` int(11) NOT NULL,
  `allocated_days` double NOT NULL,
  `used_days` double DEFAULT NULL,
  `pending_days` double DEFAULT NULL,
  `carry_forward_days` double DEFAULT NULL,
  `remaining_days` double DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_leave_year` (`employee_id`,`leave_type_id`,`year`),
  UNIQUE KEY `UK50tc672ft3rsb07c5n3f1xue7` (`employee_id`,`leave_type_id`,`year`),
  KEY `idx_leave_balance_employee` (`employee_id`),
  KEY `idx_leave_balance_year` (`year`),
  KEY `leave_type_id` (`leave_type_id`),
  CONSTRAINT `leave_balances_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_balances_ibfk_2` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.leave_balances: ~10 rows (приблизителен брой)
INSERT INTO `leave_balances` (`id`, `employee_id`, `leave_type_id`, `year`, `allocated_days`, `used_days`, `pending_days`, `carry_forward_days`, `remaining_days`, `created_at`, `updated_at`) VALUES
	(1, 1, 1, 2025, 25, 5, 0, 2, 22, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(2, 1, 2, 2025, 10, 1, 0, 0, 9, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(3, 2, 1, 2025, 25, 8, 3, 0, 14, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(4, 2, 2, 2025, 10, 0, 0, 0, 10, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(5, 3, 1, 2025, 25, 3, 0, 0, 22, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(6, 3, 2, 2025, 10, 2, 0, 0, 8, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(7, 4, 1, 2025, 25, 12, 0, 5, 18, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(8, 4, 2, 2025, 10, 3, 0, 0, 7, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(9, 5, 1, 2025, 25, 4, 0, 0, 21, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(10, 5, 2, 2025, 10, 0, 0, 0, 10, '2025-09-18 14:46:46', '2025-09-18 14:46:46');

-- Дъмп структура за таблица employee_db.leave_documents
CREATE TABLE IF NOT EXISTS `leave_documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `leave_request_id` bigint(20) NOT NULL,
  `document_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `file_size` bigint(20) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `uploaded_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_leave_document_request` (`leave_request_id`),
  KEY `idx_leave_document_name` (`document_name`),
  CONSTRAINT `leave_documents_ibfk_1` FOREIGN KEY (`leave_request_id`) REFERENCES `leave_requests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.leave_documents: ~0 rows (приблизителен брой)

-- Дъмп структура за таблица employee_db.leave_requests
CREATE TABLE IF NOT EXISTS `leave_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `leave_type_id` bigint(20) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `applied_date` date NOT NULL,
  `total_days` double NOT NULL,
  `half_day` tinyint(1) DEFAULT 0,
  `half_day_period` varchar(20) DEFAULT NULL,
  `reason` varchar(1000) NOT NULL,
  `work_handover` varchar(1000) DEFAULT NULL,
  `emergency_contact` varchar(500) DEFAULT NULL,
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED','WITHDRAWN') DEFAULT 'PENDING',
  `approved_by` bigint(20) DEFAULT NULL,
  `approved_at` timestamp NULL DEFAULT NULL,
  `approval_comments` varchar(500) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `is_cancelled` tinyint(1) DEFAULT 0,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `cancelled_by` bigint(20) DEFAULT NULL,
  `cancel_reason` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_leave_request_employee` (`employee_id`),
  KEY `idx_leave_request_dates` (`start_date`,`end_date`),
  KEY `idx_leave_request_status` (`status`),
  KEY `idx_leave_request_applied_date` (`applied_date`),
  KEY `leave_type_id` (`leave_type_id`),
  KEY `approved_by` (`approved_by`),
  KEY `cancelled_by` (`cancelled_by`),
  CONSTRAINT `leave_requests_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_requests_ibfk_2` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_types` (`id`),
  CONSTRAINT `leave_requests_ibfk_3` FOREIGN KEY (`approved_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL,
  CONSTRAINT `leave_requests_ibfk_4` FOREIGN KEY (`cancelled_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.leave_requests: ~0 rows (приблизителен брой)

-- Дъмп структура за таблица employee_db.leave_types
CREATE TABLE IF NOT EXISTS `leave_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `days_allowed` int(11) DEFAULT 0,
  `maximum_consecutive_days` int(11) DEFAULT NULL,
  `minimum_notice_days` int(11) DEFAULT 1,
  `requires_approval` tinyint(1) DEFAULT 1,
  `requires_documents` tinyint(1) DEFAULT 0,
  `carry_forward` tinyint(1) DEFAULT 0,
  `max_carry_forward_days` int(11) DEFAULT 0,
  `active` tinyint(1) DEFAULT 1,
  `applies_to_probation` tinyint(1) DEFAULT 0,
  `pro_rated` tinyint(1) DEFAULT 0,
  `color_code` varchar(7) DEFAULT NULL,
  `display_order` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_leave_type_active` (`active`),
  KEY `idx_leave_type_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.leave_types: ~7 rows (приблизителен брой)
INSERT INTO `leave_types` (`id`, `name`, `description`, `days_allowed`, `maximum_consecutive_days`, `minimum_notice_days`, `requires_approval`, `requires_documents`, `carry_forward`, `max_carry_forward_days`, `active`, `applies_to_probation`, `pro_rated`, `color_code`, `display_order`, `created_at`, `updated_at`) VALUES
	(1, 'Annual Leave', 'Paid annual vacation leave', 25, 15, 1, 1, 0, 1, 0, 1, 0, 0, '#4CAF50', 1, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(2, 'Sick Leave', 'Medical leave for illness', 10, 5, 1, 0, 0, 0, 0, 1, 0, 0, '#F44336', 2, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(3, 'Personal Leave', 'Personal time off', 5, 3, 1, 1, 0, 0, 0, 1, 0, 0, '#FF9800', 3, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(4, 'Maternity Leave', 'Maternity leave for new mothers', 90, 90, 1, 1, 0, 0, 0, 1, 0, 0, '#E91E63', 4, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(5, 'Paternity Leave', 'Paternity leave for new fathers', 14, 14, 1, 1, 0, 0, 0, 1, 0, 0, '#9C27B0', 5, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(6, 'Bereavement Leave', 'Leave for family bereavement', 5, 5, 1, 0, 0, 0, 0, 1, 0, 0, '#607D8B', 6, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(7, 'Study Leave', 'Educational and training leave', 10, 10, 1, 1, 0, 0, 0, 1, 0, 0, '#2196F3', 7, '2025-09-18 14:46:46', '2025-09-18 14:46:46');

-- Дъмп структура за таблица employee_db.permissions
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `resource` varchar(50) NOT NULL,
  `action` varchar(50) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_permission_name` (`name`),
  KEY `idx_permission_resource` (`resource`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.permissions: ~13 rows (приблизителен брой)
INSERT INTO `permissions` (`id`, `name`, `description`, `resource`, `action`, `created_at`) VALUES
	(1, 'USER_READ', 'Read user information', 'USER', 'READ', '2025-09-18 14:46:46'),
	(2, 'USER_WRITE', 'Create and update users', 'USER', 'WRITE', '2025-09-18 14:46:46'),
	(3, 'USER_DELETE', 'Delete users', 'USER', 'DELETE', '2025-09-18 14:46:46'),
	(4, 'EMPLOYEE_READ', 'Read employee information', 'EMPLOYEE', 'READ', '2025-09-18 14:46:46'),
	(5, 'EMPLOYEE_WRITE', 'Create and update employees', 'EMPLOYEE', 'WRITE', '2025-09-18 14:46:46'),
	(6, 'EMPLOYEE_DELETE', 'Delete employees', 'EMPLOYEE', 'DELETE', '2025-09-18 14:46:46'),
	(7, 'DEPARTMENT_READ', 'Read department information', 'DEPARTMENT', 'READ', '2025-09-18 14:46:46'),
	(8, 'DEPARTMENT_WRITE', 'Create and update departments', 'DEPARTMENT', 'WRITE', '2025-09-18 14:46:46'),
	(9, 'DEPARTMENT_DELETE', 'Delete departments', 'DEPARTMENT', 'DELETE', '2025-09-18 14:46:46'),
	(10, 'LEAVE_READ', 'Read leave information', 'LEAVE', 'READ', '2025-09-18 14:46:46'),
	(11, 'LEAVE_WRITE', 'Create and update leave requests', 'LEAVE', 'WRITE', '2025-09-18 14:46:46'),
	(12, 'LEAVE_APPROVE', 'Approve leave requests', 'LEAVE', 'APPROVE', '2025-09-18 14:46:46'),
	(13, 'SYSTEM_ADMIN', 'System administration access', 'SYSTEM', 'ADMIN', '2025-09-18 14:46:46');

-- Дъмп структура за таблица employee_db.positions
CREATE TABLE IF NOT EXISTS `positions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `level` enum('DIRECTOR','ENTRY','EXECUTIVE','JUNIOR','LEAD','MANAGER','MID','SENIOR','SENIOR_MANAGER','VP') NOT NULL,
  `status` enum('ACTIVE','FROZEN','INACTIVE','OBSOLETE','PENDING_APPROVAL') NOT NULL,
  `department_id` bigint(20) NOT NULL,
  `reports_to_position_id` bigint(20) DEFAULT NULL,
  `min_salary` decimal(12,2) DEFAULT NULL,
  `max_salary` decimal(12,2) DEFAULT NULL,
  `min_experience_years` int(11) DEFAULT NULL,
  `max_experience_years` int(11) DEFAULT NULL,
  `required_skills` text DEFAULT NULL,
  `preferred_skills` text DEFAULT NULL,
  `required_qualifications` text DEFAULT NULL,
  `preferred_qualifications` text DEFAULT NULL,
  `pay_grade` varchar(10) DEFAULT NULL,
  `total_headcount` int(11) DEFAULT 1,
  `number_of_openings` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(50) DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_position_title` (`title`),
  KEY `idx_position_level` (`level`),
  KEY `idx_position_department` (`department_id`),
  KEY `idx_position_status` (`status`),
  KEY `reports_to_position_id` (`reports_to_position_id`),
  CONSTRAINT `positions_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `positions_ibfk_2` FOREIGN KEY (`reports_to_position_id`) REFERENCES `positions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.positions: ~8 rows (приблизителен брой)
INSERT INTO `positions` (`id`, `title`, `description`, `level`, `status`, `department_id`, `reports_to_position_id`, `min_salary`, `max_salary`, `min_experience_years`, `max_experience_years`, `required_skills`, `preferred_skills`, `required_qualifications`, `preferred_qualifications`, `pay_grade`, `total_headcount`, `number_of_openings`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
	(1, 'Senior Software Engineer', 'Senior level software development position', 'SENIOR', 'ACTIVE', 1, NULL, 80000.00, 120000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G7', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(2, 'Software Engineer', 'Mid-level software development position', 'MID', 'ACTIVE', 1, NULL, 60000.00, 90000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G6', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(3, 'Junior Software Engineer', 'Entry-level software development position', 'JUNIOR', 'ACTIVE', 1, NULL, 45000.00, 65000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G5', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(4, 'HR Manager', 'Human Resources management position', 'MANAGER', 'ACTIVE', 2, NULL, 70000.00, 95000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'M2', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(5, 'HR Specialist', 'Human Resources specialist position', 'MID', 'ACTIVE', 2, NULL, 50000.00, 70000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G6', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(6, 'Financial Analyst', 'Financial analysis and reporting position', 'MID', 'ACTIVE', 3, NULL, 55000.00, 75000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G6', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(7, 'Marketing Manager', 'Marketing strategy and execution management', 'MANAGER', 'ACTIVE', 4, NULL, 65000.00, 90000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'M2', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL),
	(8, 'Operations Coordinator', 'Daily operations coordination', 'MID', 'ACTIVE', 5, NULL, 45000.00, 65000.00, NULL, NULL, NULL, NULL, NULL, NULL, 'G5', 1, 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46', NULL, NULL);

-- Дъмп структура за таблица employee_db.refresh_tokens
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `expires_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `revoked_at` timestamp NULL DEFAULT NULL,
  `device_info` varchar(255) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_refresh_token` (`token`),
  KEY `idx_refresh_user` (`user_id`),
  CONSTRAINT `refresh_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.refresh_tokens: ~0 rows (приблизителен брой)
INSERT INTO `refresh_tokens` (`id`, `token`, `user_id`, `expires_at`, `created_at`, `revoked_at`, `device_info`, `ip_address`) VALUES
	(1, '9ecf8226-e368-4333-b7c3-ffa04b602ccb', 2, '2025-09-25 18:59:11', '2025-09-18 18:59:11', NULL, NULL, NULL),
	(2, '6626c2fd-7195-4ed3-a354-58b3048db50d', 1, '2025-09-25 19:01:19', '2025-09-18 19:01:19', NULL, NULL, NULL);

-- Дъмп структура за таблица employee_db.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_system_role` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_role_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.roles: ~6 rows (приблизителен брой)
INSERT INTO `roles` (`id`, `name`, `description`, `is_system_role`, `created_at`, `updated_at`) VALUES
	(1, 'SUPER_ADMIN', 'System super administrator with full access', 1, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(2, 'ADMIN', 'System administrator', 1, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(3, 'HR_MANAGER', 'HR Manager with employee management rights', 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(4, 'HR_STAFF', 'HR Staff with limited employee access', 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(5, 'MANAGER', 'Department/Team Manager', 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46'),
	(6, 'EMPLOYEE', 'Regular Employee', 0, '2025-09-18 14:46:46', '2025-09-18 14:46:46');

-- Дъмп структура за таблица employee_db.role_permissions
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.role_permissions: ~34 rows (приблизителен брой)
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
	(1, 1),
	(1, 2),
	(1, 3),
	(1, 4),
	(1, 5),
	(1, 6),
	(1, 7),
	(1, 8),
	(1, 9),
	(1, 10),
	(1, 11),
	(1, 12),
	(1, 13),
	(2, 1),
	(2, 2),
	(2, 3),
	(2, 4),
	(2, 5),
	(2, 6),
	(2, 7),
	(2, 8),
	(2, 9),
	(2, 10),
	(2, 11),
	(2, 12),
	(3, 4),
	(3, 5),
	(3, 6),
	(3, 7),
	(3, 8),
	(3, 9),
	(3, 10),
	(3, 11),
	(3, 12);

-- Дъмп структура за таблица employee_db.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `is_enabled` tinyint(1) DEFAULT 1,
  `is_account_non_expired` tinyint(1) DEFAULT 1,
  `is_account_non_locked` tinyint(1) DEFAULT 1,
  `is_credentials_non_expired` tinyint(1) DEFAULT 1,
  `email_verified` tinyint(1) DEFAULT 0,
  `email_verification_token` varchar(255) DEFAULT NULL,
  `password_reset_token` varchar(255) DEFAULT NULL,
  `password_reset_expires_at` timestamp NULL DEFAULT NULL,
  `last_login_at` timestamp NULL DEFAULT NULL,
  `failed_login_attempts` int(11) DEFAULT 0,
  `locked_until` timestamp NULL DEFAULT NULL,
  `employee_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `employee_id` (`employee_id`),
  KEY `idx_user_username` (`username`),
  KEY `idx_user_email` (`email`),
  KEY `idx_user_employee` (`employee_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.users: ~2 rows (приблизителен брой)
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `first_name`, `last_name`, `is_enabled`, `is_account_non_expired`, `is_account_non_locked`, `is_credentials_non_expired`, `email_verified`, `email_verification_token`, `password_reset_token`, `password_reset_expires_at`, `last_login_at`, `failed_login_attempts`, `locked_until`, `employee_id`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
	(1, 'admin', 'admin@company.com', '$2a$10$DFYuIYcgbNPxkBbC5vQ6UOp2JunHCjfC.LPW1PQVoyna3RYxtlWHq', 'System', 'Administrator', 1, 1, 1, 1, 1, NULL, NULL, NULL, '2025-09-18 19:01:19', 0, NULL, NULL, '2025-09-18 14:46:46', '2025-09-18 19:01:19', NULL, NULL),
	(2, 'testuser', 'test@example.com', '$2a$10$DFYuIYcgbNPxkBbC5vQ6UOp2JunHCjfC.LPW1PQVoyna3RYxtlWHq', 'Test', 'User', 1, 1, 1, 1, 0, '8f371f37-e214-469b-9924-981ab15585e6', NULL, NULL, '2025-09-18 18:59:11', 0, NULL, NULL, '2025-09-18 18:58:47', '2025-09-18 18:59:11', NULL, NULL);

-- Дъмп структура за таблица employee_db.user_roles
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Дъмп данни за таблица employee_db.user_roles: ~2 rows (приблизителен брой)
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
	(1, 1),
	(2, 6);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
