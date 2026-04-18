-- RESQ Database Migration V5 - Re-seeding users with known credentials
-- Password for all seeded users is: admin123

DELETE FROM users WHERE username IN ('admin', 'dispatcher', 'patient', 'driver');

INSERT INTO users (username, password_hash, role, full_name, is_active) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COMMAND', 'System Administrator', TRUE),
    ('dispatcher', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DISPATCHER', 'Chief Dispatcher', TRUE),
    ('patient', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PATIENT', 'Standard Patient', TRUE),
    ('driver', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DRIVER', 'Ambulance Driver', TRUE);
