package com.motorph.manager;

import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;
import java.util.Locale;

public class PayrollComputationResult {

    private final Employee employee;
    private final String coverage;
    private final double hoursWorked;
    private final double grossPay;
    private final double deductions;
    private final double withholdingTax;
    private final double netPay;
    private final PayrollRecord payrollRecord;

    public PayrollComputationResult(
            Employee employee,
            String coverage,
            double hoursWorked,
            double grossPay,
            double deductions,
            double withholdingTax,
            double netPay,
            PayrollRecord payrollRecord
    ) {
        this.employee = employee;
        this.coverage = coverage;
        this.hoursWorked = hoursWorked;
        this.grossPay = grossPay;
        this.deductions = deductions;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
        this.payrollRecord = payrollRecord;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getCoverage() {
        return coverage;
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

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getNetPay() {
        return netPay;
    }

    public PayrollRecord getPayrollRecord() {
        return payrollRecord;
    }

    public String toDisplayReport() {
        return String.format(
                Locale.US,
                "PAYROLL COMPUTATION%n%n"
                + "Employee ID: %s%n"
                + "Name: %s%n"
                + "Coverage: %s%n"
                + "Hours Worked: %.2f%n"
                + "Gross Pay: Php %,.2f%n"
                + "Deductions: Php %,.2f%n"
                + "Withholding Tax: Php %,.2f%n"
                + "Net Pay: Php %,.2f",
                employee.getEmployeeId(),
                employee.getFullName(),
                coverage,
                hoursWorked,
                grossPay,
                deductions,
                withholdingTax,
                netPay
        );
    }
}