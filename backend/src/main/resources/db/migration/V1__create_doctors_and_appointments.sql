CREATE TABLE doctors (
 id UUID PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 specialty VARCHAR(70) NOT NULL,
 clinic VARCHAR(120) NOT NULL,
 location VARCHAR(100) NOT NULL,
 experience_years INTEGER NOT NULL CHECK (experience_years >= 0),
 consultation_fee NUMERIC(10,2) NOT NULL CHECK (consultation_fee >= 0),
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE appointments (
 id UUID PRIMARY KEY,
 doctor_id UUID NOT NULL REFERENCES doctors(id),
 appointment_date DATE NOT NULL,
 appointment_time TIME NOT NULL,
 status VARCHAR(20) NOT NULL,
 CONSTRAINT uk_appointment_slot UNIQUE(doctor_id,appointment_date,appointment_time)
);
CREATE INDEX idx_appointment_date ON appointments(appointment_date,doctor_id);
