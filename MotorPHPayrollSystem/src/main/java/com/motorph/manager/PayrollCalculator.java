package com.motorph.manager;
/**
 *
 * @author Timothy Justin Gacula
 */

import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;

public class PayrollCalculator {

    private static final double SSS_DEDUCTION = 1125.0 / 2;
    private static final double PHILHEALTH_DEDUCTION = 375.0 / 2;
    private static final double PAGIBIG_DEDUCTION = 100.0 / 2;
    private static final double WITHHOLDING_TAX_EXEMPTION = 1600.0;
    private static final double WITHHOLDING_TAX_BRACKET_THRESHOLD = 20833.0;
    private static final double WITHHOLDING_TAX_RATE = 0.20;

    public double computeGrossPay(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }

    public double computeDeductions(double grossPay) {
        double withholdingTax = computeWithholdingTax(grossPay);

        return SSS_DEDUCTION + PHILHEALTH_DEDUCTION + PAGIBIG_DEDUCTION + withholdingTax;
    }

    public double computeWithholdingTax(double grossPay) {
        double taxableIncome = grossPay - WITHHOLDING_TAX_EXEMPTION;

        if (taxableIncome > WITHHOLDING_TAX_BRACKET_THRESHOLD) {
            return (taxableIncome - WITHHOLDING_TAX_BRACKET_THRESHOLD) * WITHHOLDING_TAX_RATE / 2;
        }

        return 0.0;
    }

    public double computeNetPay(double grossPay, double deductions) {
        return grossPay - deductions;
    }

    public PayrollRecord createPayrollRecord(Employee employee, double hoursWorked, String payCoverage) {
        double grossPay = computeGrossPay(hoursWorked, employee.getHourlyRate());
        double deductions = computeDeductions(grossPay);
        double netPay = computeNetPay(grossPay, deductions);

        return new PayrollRecord(
                employee.getEmployeeId(),
                employee.getFullName(),
                payCoverage,
                hoursWorked,
                grossPay,
                deductions,
                netPay
        );
    }
}