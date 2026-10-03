package com.smartcare;

import com.smartcare.exception.ValidationException;
import com.smartcare.model.Appointment;
import com.smartcare.model.Medicine;
import com.smartcare.model.Prescription;
import com.smartcare.model.PrescriptionItem;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.impl.AppointmentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Unit tests validating Appointment scheduling logic and Prescription calculations.
 */
public class AppointmentPrescriptionTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running SmartCare Tests: Appointments & Rx");
        System.out.println("==================================================");

        testAppointmentPastDateValidation();
        testPrescriptionCalculation();
        testMedicineLowStockCheck();

        System.out.println("==================================================");
        System.out.println("All Appointment & Rx Logic Tests Passed!");
        System.out.println("==================================================");
    }

    private static void testAppointmentPastDateValidation() {
        System.out.print("Testing Past Appointment Validation... ");
        AppointmentService service = new AppointmentServiceImpl();

        Appointment pastAppt = new Appointment(0, 1, 1,
                LocalDate.now().minusDays(2), LocalTime.of(10, 0), "Checkup");

        try {
            service.scheduleAppointment(pastAppt);
            assert false : "Past appointment date should have thrown ValidationException";
        } catch (ValidationException expected) {
            // Expected
        }

        System.out.println("PASSED");
    }

    private static void testPrescriptionCalculation() {
        System.out.print("Testing Prescription Multi-Item Total Calculations... ");
        Prescription p = new Prescription(1, 1, 1, null, "Take after food");

        PrescriptionItem item1 = new PrescriptionItem(1, 1, 1, "Paracetamol 650", "650mg", "1-0-1", 5, 10, "After food");
        item1.setUnitPrice(new BigDecimal("25.00")); // 10 * 25.00 = 250.00

        PrescriptionItem item2 = new PrescriptionItem(2, 1, 2, "Amoxicillin 500mg", "500mg", "1-1-1", 3, 9, "Antibiotic");
        item2.setUnitPrice(new BigDecimal("110.00")); // 9 * 110.00 = 990.00

        p.addItem(item1);
        p.addItem(item2);

        BigDecimal total = p.calculateTotalCost();
        BigDecimal expected = new BigDecimal("1240.00"); // 250 + 990 = 1240

        assert total.compareTo(expected) == 0 : "Expected total " + expected + ", but got: " + total;
        System.out.println("PASSED");
    }

    private static void testMedicineLowStockCheck() {
        System.out.print("Testing Medicine Inventory Low Stock Alert Logic... ");
        Medicine m = new Medicine(1, "Test Drug", "Test Generic", "General", "Tablet",
                new BigDecimal("50.00"), 15, 20, "Pharma Co", LocalDate.now().plusYears(1));

        assert m.isLowStock() : "Medicine with stock 15 and reorder 20 must flag isLowStock() = true";

        m.setStockQuantity(50);
        assert !m.isLowStock() : "Medicine with stock 50 and reorder 20 must flag isLowStock() = false";

        System.out.println("PASSED");
    }
}
