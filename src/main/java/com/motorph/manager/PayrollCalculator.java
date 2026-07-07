package com.motorph.manager;
/**
 *
 * @author Timothy Justin Gacula
 */

import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;

public class PayrollCalculator {

    public double computeGrossPay(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }

    public double computeDeductions(double grossPay) {
        double sss = 1125.0 / 2;
        double philHealth = 375.0 / 2;
        double pagIbig = 100.0 / 2;
        double withholdingTax = computeWithholdingTax(grossPay);

        return sss + philHealth + pagIbig + withholdingTax;
    }

    public double computeWithholdingTax(double grossPay) {
        double taxableIncome = grossPay - 1600.0;

        if (taxableIncome > 20833.0) {
            return (taxableIncome - 20833.0) * 0.20 / 2;
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