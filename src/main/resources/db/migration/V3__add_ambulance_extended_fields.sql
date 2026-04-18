-- RESQ Database Migration V3 - Align Ambulance Entity with Schema
-- Adding all missing columns to the ambulances table

ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS assigned_dispatcher_id BIGINT;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS assigned_dispatcher_name VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS current_patient_name VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS destination_hospital VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS destination_hospital_id BIGINT;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS eta_minutes INTEGER;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS assigned_driver_id BIGINT;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS assigned_driver_name VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS vehicle_number VARCHAR(100);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS vehicle_model VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS base_location VARCHAR(255);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS equipment_level VARCHAR(100);
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS route_to_patient_json TEXT;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS route_to_hospital_json TEXT;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS route_to_patient_progress DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS route_to_hospital_progress DOUBLE PRECISION DEFAULT 0.0;

-- New columns for destination tracking
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS destination_lat DOUBLE PRECISION;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS destination_lng DOUBLE PRECISION;
