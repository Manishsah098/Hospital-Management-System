package com.smartcare;

import com.smartcare.enums.Gender;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Doctor;
import com.smartcare.model.Patient;
import com.smartcare.service.DoctorService;
import com.smartcare.service.PatientService;
import com.smartcare.service.impl.DoctorServiceImpl;
import com.smartcare.service.impl.PatientServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unit Test Suite for Patient and Doctor Services and Business Validation.
 */
public class PatientDoctorServiceTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running SmartCare Tests: Patient & Doctor Logic");
        System.out.println("==================================================");

        testPatientValidation();
        testPatientAgeCalculation();
        testDoctorValidation();

        System.out.println("==================================================");
        System.out.println("All Patient & Doctor Logic Tests Passed!");
        System.out.println("==================================================");
    }

    private static void testPatientValidation() {
        System.out.print("Testing Patient Validation Rules... ");
        PatientService service = new PatientServiceImpl();

        // 1. Future DOB check
        Patient invalidDobPatient = new Patient(0, "PAT-9999", "Test Patient",
                LocalDate.now().plusDays(10), Gender.MALE, "O+", "9876543210", "test@test.com", "Address");
        try {
            service.registerPatient(invalidDobPatient);
            assert false : "Future DOB should have thrown ValidationException";
        } catch (ValidationException expected) {
            // Expected
        }

        // 2. Invalid Phone check
        Patient invalidPhonePatient = new Patient(0, "PAT-9998", "Test Patient",
                LocalDate.of(2000, 1, 1), Gender.FEMALE, "B+", "123", "test@test.com", "Address");
        try {
            service.registerPatient(invalidPhonePatient);
            assert false : "Short phone number should have thrown ValidationException";
        } catch (ValidationException expected) {
            // Expected
        }

        System.out.println("PASSED");
    }

    private static void testPatientAgeCalculation() {
        System.out.print("Testing Patient Age Period Calculation... ");
        Patient p = new Patient();
        p.setDateOfBirth(LocalDate.now().minusYears(25));
        assert p.getAge() == 25 : "Expected age to be 25, got: " + p.getAge();
        System.out.println("PASSED");
    }

    private static void testDoctorValidation() {
        System.out.print("Testing Doctor Validation Rules... ");
        DoctorService service = new DoctorServiceImpl();

        // 1. Negative fee check
        Doctor invalidFeeDoctor = new Doctor();
        invalidFeeDoctor.setFullName("Dr. Invalid");
        invalidFeeDoctor.setSpecialization("Cardiology");
        invalidFeeDoctor.setLicenseNumber("LIC-12345");
        invalidFeeDoctor.setPhone("9876543210");
        invalidFeeDoctor.setConsultationFee(new BigDecimal("-100.00"));

        try {
            service.registerDoctor(invalidFeeDoctor);
            assert false : "Negative consultation fee should have thrown ValidationException";
        } catch (ValidationException expected) {
            // Expected
        }

        System.out.println("PASSED");
    }
}
