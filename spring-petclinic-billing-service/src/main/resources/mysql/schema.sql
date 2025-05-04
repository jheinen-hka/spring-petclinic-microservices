CREATE DATABASE IF NOT EXISTS petclinic;
CREATE USER IF NOT EXISTS 'pc'@'%' IDENTIFIED BY 'pc';
GRANT ALL PRIVILEGES ON petclinic.* TO 'pc'@'%';

USE petclinic;

CREATE TABLE IF NOT EXISTS bill (
    id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id INT(11) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    visit_id INT(11) NOT NULL,
    visit_date DATE,
    amount DOUBLE,
    description VARCHAR(255),
    issue_date DATE,
    status VARCHAR(30),
    INDEX(customer_id),
    INDEX(visit_id),
    INDEX(issue_date)
) ENGINE=InnoDB;
