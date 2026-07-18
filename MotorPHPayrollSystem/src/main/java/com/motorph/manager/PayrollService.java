package com.motorph.manager;

import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class PayrollService {

    private static final DateTimeFormatter COVERAGE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private final AttendanceManager attendanceManager;
    private final EmployeeManager employeeManager;
    private final PayrollCalculator payrollCalculator;
    private final PayrollRecordManager payrollRecordManager;

    public PayrollService(
            AttendanceManager attendanceManager,
            EmployeeManager employeeManager,
            PayrollCalculator payrollCalculator,
            PayrollRecordManager payrollRecordManager
    ) {
        this.attendanceManager = attendanceManager;
        this.employeeManager = employeeManager;
        this.payrollCalculator = payrollCalculator;
        this.payrollRecordManager = payrollRecordManager;
    }

    public PayrollComputationResult computePayroll(
            String employeeId,
            String coverage,
            String manualHoursText
    ) throws IOException {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }

        if (coverage == null || coverage.trim().isEmpty()) {
            throw new IllegalArgumentException("Pay coverage is required.");
        }

        Employee employee = employeeManager.findEmployeeById(employeeId.trim());
        if (employee == null) {
            throw new IllegalArgumentException("Employee ID not found.");
        }

        double hoursWorked = resolveHoursWorked(
                employee.getEmployeeId(),
                coverage.trim(),
                manualHoursText
        );

        PayrollRecord payrollRecord =
                payrollCalculator.createPayrollRecord(employee, hoursWorked, coverage.trim());

        double withholdingTax =
                payrollCalculator.computeWithholdingTax(payrollRecord.getGrossPay());

        payrollRecordManager.savePayrollRecord(payrollRecord);

        return new PayrollComputationResult(
                employee,
                coverage.trim(),
                payrollRecord.getHoursWorked(),
                payrollRecord.getGrossPay(),
                payrollRecord.getDeductions(),
                withholdingTax,
                payrollRecord.getNetPay(),
                payrollRecord
        );
    }

    private double resolveHoursWorked(
            String employeeId,
            String coverage,
            String manualHoursText
    ) throws IOException {

        if (manualHoursText != null && !manualHoursText.trim().isEmpty()) {
            try {
                double hoursWorked = Double.parseDouble(manualHoursText.trim());

                if (hoursWorked < 0) {
                    throw new IllegalArgumentException("Manual hours cannot be negative.");
                }

                return hoursWorked;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Manual hours must be a valid number.");
            }
        }

        LocalDate[] period = parseCoverageDates(coverage);
        return attendanceManager.computeHoursWorked(employeeId, period[0], period[1]);
    }

    private LocalDate[] parseCoverageDates(String coverage) {
        try {
            String[] parts = coverage.trim().split(",");

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid pay coverage format. Use example: June 1-15, 2024"
                );
            }

            String monthAndDays = parts[0].trim();
            String yearText = parts[1].trim();

            String[] firstParts = monthAndDays.split(" ");
            if (firstParts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid pay coverage format. Use example: June 1-15, 2024"
                );
            }

            String month = firstParts[0].trim();
            String[] dayRange = firstParts[1].split("-");

            if (dayRange.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid pay coverage format. Use example: June 1-15, 2024"
                );
            }

            String startText = month + " " + dayRange[0].trim() + ", " + yearText;
            String endText = month + " " + dayRange[1].trim() + ", " + yearText;

            LocalDate startDate = LocalDate.parse(startText, COVERAGE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endText, COVERAGE_FORMATTER);

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException(
                        "Pay coverage end date cannot be earlier than start date."
                );
            }

            return new LocalDate[]{startDate, endDate};
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid pay coverage format. Use example: June 1-15, 2024"
            );
        }
    }
}