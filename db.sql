-- ==========================================================
-- SmartCare: AI-Enhanced Hospital Management System
-- Database Schema: smartcare_hospital
-- Version: 1.0.0
-- MySQL 8.0+ Compatible
-- ==========================================================

CREATE DATABASE IF NOT EXISTS smartcare_hospital
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smartcare_hospital;

-- Disable Foreign Key checks temporarily for clean setup
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users & Authentication
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'LAB_TECHNICIAN', 'PHARMACIST') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Departments
DROP TABLE IF EXISTS departments;
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    head_doctor_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Doctors
DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    department_id INT,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 500.00,
    qualification VARCHAR(100),
    experience_years INT DEFAULT 0,
    available_days VARCHAR(100) DEFAULT 'Mon,Tue,Wed,Thu,Fri',
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL,
    INDEX idx_doctor_specialization (specialization),
    INDEX idx_doctor_department (department_id)
) ENGINE=InnoDB;

-- 4. Patients
DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    blood_group VARCHAR(5),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    allergies TEXT,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_name (full_name),
    INDEX idx_patient_phone (phone),
    INDEX idx_patient_code (patient_code)
) ENGINE=InnoDB;

-- 5. Appointments
DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
    reason_for_visit TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_appointment_doctor_date (doctor_id, appointment_date)
) ENGINE=InnoDB;

-- 6. Medical Records
DROP TABLE IF EXISTS medical_records;
CREATE TABLE medical_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT,
    diagnosis TEXT NOT NULL,
    symptoms TEXT,
    treatment_plan TEXT,
    vital_signs VARCHAR(255), -- e.g. "BP: 120/80, Pulse: 72, Temp: 98.6F"
    doctor_notes TEXT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medrecord_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_medrecord_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_medrecord_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    INDEX idx_medrecord_patient (patient_id)
) ENGINE=InnoDB;

-- 7. Medicines
DROP TABLE IF EXISTS medicines;
CREATE TABLE medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    category VARCHAR(50),
    dosage_form VARCHAR(50), -- Tablet, Syrup, Injection, Capsule
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 20,
    manufacturer VARCHAR(100),
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_medicine_name (medicine_name)
) ENGINE=InnoDB;

-- 8. Prescriptions & Prescription Items
DROP TABLE IF EXISTS prescriptions;
CREATE TABLE prescriptions (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    record_id INT,
    status ENUM('PENDING', 'DISPENSED', 'CANCELLED') DEFAULT 'PENDING',
    instructions TEXT,
    prescribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispensed_at TIMESTAMP NULL,
    CONSTRAINT fk_presc_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_presc_record FOREIGN KEY (record_id) REFERENCES medical_records(record_id) ON DELETE SET NULL
) ENGINE=InnoDB;

DROP TABLE IF EXISTS prescription_items;
CREATE TABLE prescription_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage VARCHAR(50) NOT NULL, -- e.g., "500mg"
    frequency VARCHAR(50) NOT NULL, -- e.g., "Twice daily after food"
    duration_days INT NOT NULL,
    quantity INT NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_pitem_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
    CONSTRAINT fk_pitem_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 9. Laboratory Tests & Lab Results
DROP TABLE IF EXISTS lab_tests;
CREATE TABLE lab_tests (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    test_name VARCHAR(100) NOT NULL,
    test_code VARCHAR(30) UNIQUE,
    category VARCHAR(50),
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    normal_range VARCHAR(100),
    units VARCHAR(30),
    turnaround_time_hours INT DEFAULT 24
) ENGINE=InnoDB;

DROP TABLE IF EXISTS lab_results;
CREATE TABLE lab_results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    test_id INT NOT NULL,
    test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    technician_id INT,
    result_value VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    remarks TEXT,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_labres_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_labres_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_labres_test FOREIGN KEY (test_id) REFERENCES lab_tests(test_id) ON DELETE RESTRICT,
    CONSTRAINT fk_labres_tech FOREIGN KEY (technician_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 10. Rooms, Beds & Inpatient Admissions
DROP TABLE IF EXISTS rooms;
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type ENUM('GENERAL_WARD', 'SEMI_PRIVATE', 'PRIVATE', 'ICU', 'EMERGENCY') NOT NULL,
    daily_charge DECIMAL(10, 2) NOT NULL DEFAULT 1000.00,
    floor_number INT DEFAULT 1,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

DROP TABLE IF EXISTS beds;
CREATE TABLE beds (
    bed_id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    CONSTRAINT uq_room_bed UNIQUE (room_id, bed_number),
    CONSTRAINT fk_bed_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS admissions;
CREATE TABLE admissions (
    admission_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    bed_id INT NOT NULL,
    doctor_id INT NOT NULL,
    admission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discharge_date TIMESTAMP NULL,
    status ENUM('ADMITTED', 'DISCHARGED', 'TRANSFERRED') DEFAULT 'ADMITTED',
    admission_reason TEXT,
    CONSTRAINT fk_adm_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_adm_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE RESTRICT,
    CONSTRAINT fk_adm_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 11. Billing & Bill Items
DROP TABLE IF EXISTS bills;
CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    appointment_id INT,
    admission_id INT,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax_percentage DECIMAL(5, 2) NOT NULL DEFAULT 5.00,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID',
    payment_mode ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'INSURANCE') DEFAULT 'CASH',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    notes TEXT,
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    CONSTRAINT fk_bill_admission FOREIGN KEY (admission_id) REFERENCES admissions(admission_id) ON DELETE SET NULL,
    INDEX idx_bill_status (payment_status)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS bill_items;
CREATE TABLE bill_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    item_description VARCHAR(200) NOT NULL,
    item_type ENUM('CONSULTATION', 'LAB_TEST', 'MEDICINE', 'ROOM_CHARGE', 'PROCEDURE', 'OTHER') NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_bitem_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- SEED DATA
-- Default Passwords are all: Admin@123 (or hashed with Salt)
-- SHA-256(Admin@123 + "smartcare_salt_123") =
-- 4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e
-- ==========================================================

-- Default Users
INSERT INTO users (username, password_hash, salt, full_name, email, phone, role) VALUES
('admin', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'System Administrator', 'admin@smartcare.hospital', '9876543210', 'ADMIN'),
('dr.sharma', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'Dr. Rajesh Sharma', 'dr.sharma@smartcare.hospital', '9876543211', 'DOCTOR'),
('dr.patel', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'Dr. Priya Patel', 'dr.patel@smartcare.hospital', '9876543212', 'DOCTOR'),
('receptionist1', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'Ananya Verma', 'ananya@smartcare.hospital', '9876543213', 'RECEPTIONIST'),
('labtech1', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'Vikram Singh', 'vikram@smartcare.hospital', '9876543214', 'LAB_TECHNICIAN'),
('pharmacist1', '4f5e27a69b764cb5a329ef31846b02a4bf74533083e9cdb05e04cb2a0149021e', 'smartcare_salt_123', 'Sneha Kulkarni', 'sneha@smartcare.hospital', '9876543215', 'PHARMACIST');

-- Departments
INSERT INTO departments (name, description, head_doctor_name) VALUES
('Cardiology', 'Heart, blood vessels, and cardiovascular health department', 'Dr. Rajesh Sharma'),
('Neurology', 'Disorders of the brain, spinal cord, and nervous system', 'Dr. Priya Patel'),
('Orthopedics', 'Bones, joints, ligaments, tendons, and muscles', 'Dr. Amit Roy'),
('General Medicine', 'Primary healthcare, diagnostics, and internal medicine', 'Dr. Sunita Rao'),
('Pediatrics', 'Medical care for infants, children, and adolescents', 'Dr. Neha Gupta');

-- Doctors
INSERT INTO doctors (user_id, full_name, specialization, department_id, license_number, phone, email, consultation_fee, qualification, experience_years) VALUES
(2, 'Dr. Rajesh Sharma', 'Cardiologist', 1, 'MED-IND-2015-8841', '9876543211', 'dr.sharma@smartcare.hospital', 800.00, 'MBBS, MD, DM (Cardiology)', 12),
(3, 'Dr. Priya Patel', 'Neurologist', 2, 'MED-IND-2018-9421', '9876543212', 'dr.patel@smartcare.hospital', 900.00, 'MBBS, MD, DM (Neurology)', 9);

-- Patients
INSERT INTO patients (patient_code, full_name, date_of_birth, gender, blood_group, phone, email, address, emergency_contact_name, emergency_contact_phone, allergies) VALUES
('PAT-1001', 'Rahul Mehta', '1988-05-14', 'MALE', 'O+', '9811223344', 'rahul.mehta@example.com', 'Flat 402, Green Valley Apartments, Mumbai', 'Sunita Mehta', '9811223345', 'Penicillin'),
('PAT-1002', 'Aarav Gupta', '1995-11-20', 'MALE', 'A+', '9822334455', 'aarav.gupta@example.com', '12 Lakeview Residency, Pune', 'Pooja Gupta', '9822334456', 'Sulfa drugs'),
('PAT-1003', 'Kavita Iyer', '1976-02-28', 'FEMALE', 'B+', '9833445566', 'kavita.iyer@example.com', '78 Coral Woods, Bengaluru', 'Ramesh Iyer', '9833445567', 'None');

-- Medicines
INSERT INTO medicines (medicine_name, generic_name, category, dosage_form, unit_price, stock_quantity, reorder_level, manufacturer, expiry_date) VALUES
('Paracetamol 650', 'Acetaminophen', 'Analgesic / Antipyretic', 'Tablet', 25.00, 500, 50, 'Cipla Ltd', '2027-12-31'),
('Amoxicillin 500mg', 'Amoxicillin', 'Antibiotic', 'Capsule', 110.00, 200, 30, 'Sun Pharma', '2026-10-15'),
('Atorvastatin 20mg', 'Atorvastatin', 'Cardiovascular', 'Tablet', 185.00, 150, 25, 'Torrent Pharma', '2028-05-30'),
('Metformin 500mg', 'Metformin HCl', 'Antidiabetic', 'Tablet', 45.00, 300, 40, 'Dr. Reddy Labs', '2027-08-20'),
('Pantoprazole 40mg', 'Pantoprazole', 'Antacid / PPI', 'Tablet', 95.00, 250, 35, 'Alkem Labs', '2027-11-10'),
('Cetirizine 10mg', 'Cetirizine HCl', 'Antihistamine', 'Tablet', 35.00, 400, 40, 'Mankind Pharma', '2028-01-15');

-- Lab Tests
INSERT INTO lab_tests (test_name, test_code, category, cost, normal_range, units, turnaround_time_hours) VALUES
('Complete Blood Count (CBC)', 'LAB-CBC', 'Hematology', 350.00, 'Varies by parameter', '', 12),
('Lipid Profile', 'LAB-LIPID', 'Biochemistry', 650.00, 'Cholesterol < 200', 'mg/dL', 24),
('Fasting Blood Glucose', 'LAB-FBS', 'Biochemistry', 150.00, '70 - 100', 'mg/dL', 6),
('HbA1c Glycated Hemoglobin', 'LAB-HBA1C', 'Biochemistry', 500.00, '4.0 - 5.6', '%', 12),
('Chest X-Ray PA View', 'LAB-XRAY-CH', 'Radiology', 450.00, 'Clear lung fields', '', 4),
('Electrocardiogram (ECG)', 'LAB-ECG', 'Cardiology', 300.00, 'Normal sinus rhythm', '', 2);

-- Rooms & Beds
INSERT INTO rooms (room_number, room_type, daily_charge, floor_number, status) VALUES
('101', 'GENERAL_WARD', 800.00, 1, 'AVAILABLE'),
('102', 'SEMI_PRIVATE', 1800.00, 1, 'AVAILABLE'),
('201', 'PRIVATE', 3500.00, 2, 'AVAILABLE'),
('301', 'ICU', 8000.00, 3, 'AVAILABLE');

INSERT INTO beds (room_id, bed_number, is_occupied) VALUES
(1, 'GW-101-A', FALSE),
(1, 'GW-101-B', FALSE),
(1, 'GW-101-C', FALSE),
(2, 'SP-102-A', FALSE),
(2, 'SP-102-B', FALSE),
(3, 'PV-201-A', FALSE),
(4, 'ICU-301-A', FALSE);
