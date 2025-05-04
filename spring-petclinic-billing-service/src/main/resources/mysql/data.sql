INSERT IGNORE INTO bill (id, customer_id, customer_name, visit_id, visit_date, amount, description, issue_date, status) VALUES
(1, 101, 'Max Mustermann', 2001, '2025-04-10', 89.99, 'Routine Check-up', '2025-04-11', 'OPEN'),
(2, 102, 'Erika Musterfrau', 2002, '2025-04-12', 129.50, 'Surgery', '2025-04-13', 'PAID'),
(3, 103, 'John Doe', 2003, '2025-04-15', 45.00, 'Vaccination', '2025-04-16', 'CANCELLED');
