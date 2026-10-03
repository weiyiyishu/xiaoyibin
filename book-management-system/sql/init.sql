-- ============================================================
-- 图书管理系统 数据库初始化脚本（MySQL 8.0+）
-- 在 Navicat / IDEA Database 或命令行中执行本脚本即可
-- 命令行：mysql -u root -p < sql/init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- ------------------------------------------------------------
-- 用户表：存放账号信息（管理员 / 读者）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS library_users;
CREATE TABLE library_users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
  role VARCHAR(20) NOT NULL DEFAULT 'reader' COMMENT '角色：admin 管理员 / reader 读者',
  display_name VARCHAR(100) COMMENT '显示名称',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

-- ------------------------------------------------------------
-- 图书表：存放图书信息
-- ------------------------------------------------------------
DROP TABLE IF EXISTS books;
CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL COMMENT '书名',
  author VARCHAR(255) NOT NULL COMMENT '作者',
  isbn VARCHAR(20) COMMENT 'ISBN',
  category VARCHAR(100) COMMENT '分类',
  description TEXT COMMENT '简介',
  cover_url VARCHAR(500) COMMENT '封面图片 URL',
  publisher VARCHAR(255) COMMENT '出版社',
  publish_date DATE COMMENT '出版日期',
  pages INT COMMENT '页数',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- ------------------------------------------------------------
-- 借阅记录表：记录每次借书 / 还书
-- ------------------------------------------------------------
DROP TABLE IF EXISTS borrow_records;
CREATE TABLE borrow_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '借阅用户 ID',
  book_id BIGINT NOT NULL COMMENT '图书 ID',
  borrow_at DATETIME NOT NULL COMMENT '借出时间',
  due_at DATETIME NOT NULL COMMENT '应还时间',
  return_at DATETIME NULL COMMENT '实际归还时间',
  status VARCHAR(20) NOT NULL DEFAULT 'borrowing' COMMENT '状态：borrowing 借阅中 / returned 已归还',
  KEY idx_user (user_id),
  KEY idx_book (book_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- ------------------------------------------------------------
-- 预置账号（密码均为 123456，BCrypt 加密，已验证可正常登录）
-- 管理员：admin / 123456
-- 读者：reader / 123456
-- ------------------------------------------------------------
INSERT INTO library_users (username, password, role, display_name) VALUES
('admin',  '$2a$10$CBkYWCh8VWaGA9W.XBsq6.g0dmxS0new7e5kSwSeUWJvX0TdEOScK', 'admin',  '系统管理员'),
('reader', '$2a$10$CBkYWCh8VWaGA9W.XBsq6.g0dmxS0new7e5kSwSeUWJvX0TdEOScK', 'reader', '示例读者');

-- ------------------------------------------------------------
-- 示例图书数据
-- ------------------------------------------------------------
INSERT INTO books (title, author, isbn, category, description, stock, pages, publisher) VALUES
('深入理解计算机系统', 'Randal E. Bryant', '9787111544937', '计算机', '本书从程序员的视角详细阐述计算机系统的本质概念，是计算机领域的经典教材。', 5, 737, '机械工业出版社'),
('算法导论', 'Thomas H. Cormen', '9787111407010', '计算机', '全面、深入地介绍了算法理论与实践，是算法领域的权威著作。', 3, 1312, '机械工业出版社'),
('百年孤独', '加西亚·马尔克斯', '9787544253994', '文学', '魔幻现实主义文学的代表作，描写了布恩迪亚家族七代人的传奇故事。', 8, 360, '南海出版公司'),
('活着', '余华', '9787506365437', '文学', '讲述了农村人福贵悲惨的人生遭遇，是余华的代表作之一。', 12, 191, '作家出版社'),
('人类简史', '尤瓦尔·赫拉利', '9787508647357', '历史', '从认知革命、农业革命到科学革命，讲述人类是如何成为地球的主宰者。', 6, 440, '中信出版社'),
('明朝那些事儿', '当年明月', '9787213046423', '历史', '以史料为基础，对明朝十七帝和其他王公权贵和小人物的命运进行全景展示。', 10, 296, '浙江人民出版社'),
('时间简史', '史蒂芬·霍金', '9787535732309', '科学', '探索时间和空间的奥秘，是科普读物中的经典之作。', 4, 245, '湖南科学技术出版社'),
('三体', '刘慈欣', '9787536692930', '科学', '中国当代科幻文学的里程碑作品，讲述了地球文明与三体文明的生死博弈。', 15, 302, '重庆出版社'),
('小王子', '圣埃克苏佩里', '9787544733083', '文学', '一部充满诗意和哲理的童话，适合所有年龄段的读者。', 20, 97, '译林出版社'),
('经济学原理', '曼昆', '9787301150894', '经济', '经济学入门的经典教材，用通俗易懂的语言介绍经济学基本概念。', 2, 526, '北京大学出版社'),
('设计心理学', '唐纳德·诺曼', '9787508648330', '计算机', '讲述设计中的心理学原理，是交互设计领域的必读书。', 7, 248, '中信出版社');
