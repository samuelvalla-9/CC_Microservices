-- CityCare Database Setup Script - Complete
-- Creates all databases matching the actual configuration files

-- Databases with underscores (standard naming)
CREATE DATABASE IF NOT EXISTS citycare_auth;
CREATE DATABASE IF NOT EXISTS citycare_citizen;
CREATE DATABASE IF NOT EXISTS citycare_emergency;
CREATE DATABASE IF NOT EXISTS citycare_facility;
CREATE DATABASE IF NOT EXISTS citycare_patient_treatment;
CREATE DATABASE IF NOT EXISTS citycare_compliance;
CREATE DATABASE IF NOT EXISTS citycare_notification;

-- Databases without underscores or mixed case (from config files)
CREATE DATABASE IF NOT EXISTS CitycareFacility;
CREATE DATABASE IF NOT EXISTS citycarecitizen;
CREATE DATABASE IF NOT EXISTS EmergencyService;
CREATE DATABASE IF NOT EXISTS CitycarePatientTreatment;
CREATE DATABASE IF NOT EXISTS citycareComplianceService;
CREATE DATABASE IF NOT EXISTS citycarenotifications;

-- Show all created databases
SHOW DATABASES LIKE '%citycare%';
SHOW DATABASES LIKE '%Emergency%';
