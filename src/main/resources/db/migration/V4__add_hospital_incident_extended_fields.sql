-- RESQ Database Migration V4 - Align Hospital and Incident Entities with Schema
-- Adding missing columns to hospitals and incidents tables

-- Updates for hospitals table
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS workload_percentage INTEGER DEFAULT 0;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS emergency_capacity INTEGER DEFAULT 0;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS current_patients INTEGER DEFAULT 0;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS wait_time_minutes INTEGER DEFAULT 0;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(50);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS address VARCHAR(1000);

-- Updates for incidents table
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS dispatched_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS picked_up_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS arrived_hospital_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_name VARCHAR(255);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_age INTEGER;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_gender VARCHAR(50);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_phone VARCHAR(50);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_address VARCHAR(1000);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS medical_history TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS current_problem TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_bp VARCHAR(50);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_pulse INTEGER;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_temperature DOUBLE PRECISION;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_oxygen INTEGER;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_heart_rate INTEGER;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_respiratory_rate INTEGER;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS vitals_recorded_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS emergency_type VARCHAR(100);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS priority_level VARCHAR(100);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS assigned_hospital_id BIGINT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS assigned_hospital_name VARCHAR(255);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_preferred_hospital_id BIGINT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS patient_preferred_hospital_name VARCHAR(255);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS dispatcher_notes TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS caller_name VARCHAR(255);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS caller_phone VARCHAR(50);
