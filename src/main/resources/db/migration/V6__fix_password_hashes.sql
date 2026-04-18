-- RESQ Database Migration V6 - Fix User Password Hashes
-- Password for all users: admin123
-- Verified hash: $2a$10$Wu5mndNAj7p5n8pBfZYbK.rlBHDZtK3Zyn0AaeVd7k7GwNdTdhXc2

UPDATE users SET password_hash = '$2a$10$Wu5mndNAj7p5n8pBfZYbK.rlBHDZtK3Zyn0AaeVd7k7GwNdTdhXc2';
