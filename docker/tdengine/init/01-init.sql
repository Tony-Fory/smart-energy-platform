-- Phase 1: create database
CREATE DATABASE IF NOT EXISTS smart_energy;

-- Phase 3: create super table for energy time-series data
CREATE STABLE IF NOT EXISTS smart_energy.energy_data (
    ts TIMESTAMP,
    voltage DOUBLE,
    current DOUBLE,
    power DOUBLE,
    energy DOUBLE
) TAGS (
    device_id NCHAR(50),
    device_type NCHAR(30)
);
