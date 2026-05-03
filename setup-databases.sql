-- CityCare Database Setup Script
-- Creates all required databases for microservices

CREATE DATABASE IF NOT EXISTS citycare_auth;
CREATE DATABASE IF NOT EXISTS citycare_citizen;
CREATE DATABASE IF NOT EXISTS citycare_emergency;
CREATE DATABASE IF NOT EXISTS citycare_facility;
CREATE DATABASE IF NOT EXISTS citycare_patient_treatment;
CREATE DATABASE IF NOT EXISTS citycare_compliance;
CREATE DATABASE IF NOT EXISTS citycare_notification;

-- Show created databases
SHOW DATABASES LIKE 'citycare_%';
