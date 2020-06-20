CREATE DATABASE IF NOT EXISTS different2

USE different2

-- auto-generated definition
CREATE TABLE student
(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(63) DEFAULT '' NULL,
    age    INT DEFAULT 0  NOT NULL,
    mobile VARCHAR(11) DEFAULT '' NULL
);
