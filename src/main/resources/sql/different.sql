CREATE DATABASE IF NOT EXISTS different

USE different
-- auto-generated definition
CREATE TABLE course
(
    id INT AUTO_INCREMENT PRIMARY KEY
)COMMENT 'course';

-- auto-generated definition
CREATE TABLE student
(
    id     INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(63) DEFAULT '' NULL,
    age    INT DEFAULT 0  NOT NULL,
    mobile VARCHAR(12) NULL
);
