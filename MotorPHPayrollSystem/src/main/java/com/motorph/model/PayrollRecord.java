package com.motorph.model;

/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.manager.CsvUtil;
import java.util.Locale;

public class PayrollRecord {

    private final String employeeId;
    private final String employeeName;
    private final String payCoverage;
    private final double hoursWorked;
    private final double grossPay;
    private final double deductions;
    private final double netPay;

    public PayrollRecord(String employeeId, String employeeName, String payCoverage,
                         double hoursWorked, double grossPay, double deductions, double netPay) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payCoverage = payCoverage;
        this.hoursWorked = hoursWorked;
        this.grossPay = grossPay;
        this.deductions = deductions;
        this.netPay = netPay;
    }

    public String toCsvString() {
        return CsvUtil.toCsvRow(
                employeeId,
                employeeName,
                payCoverage,
                String.format(Locale.US, "%.2f", hoursWorked),
                String.format(Locale.US, "%.2f", grossPay),
                String.format(Locale.US, "%.2f", deductions),
                String.format(Locale.US, "%.2f", netPay)
        );
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getPayCoverage() {
        return payCoverage;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getDeductions() {
        return deductions;
    }

    public double getNetPay() {
        return netPay;
    }

    public String getGrossPayDisplay() {
        return String.format(Locale.US, "Php %,.2f", grossPay);
    }

    public String getDeductionsDisplay() {
        return String.format(Locale.US, "Php %,.2f", deductions);
    }

    public String getNetPayDisplay() {
        return String.format(Locale.US, "Php %,.2f", netPay);
    }
}