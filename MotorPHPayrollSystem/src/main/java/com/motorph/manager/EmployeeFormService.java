package com.motorph.manager;

import com.motorph.model.Employee;
import java.awt.Component;
import java.util.Locale;
import javax.swing.JOptionPane;

public class EmployeeFormService {

    private static final double WORK_DAYS_PER_MONTH = 22.0;
    private static final double WORK_HOURS_PER_DAY = 8.0;

    public Employee promptNewEmployee(Component parent, EmployeeManager employeeManager) {
        String employeeId = JOptionPane.showInputDialog(parent, "Employee ID");
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }

        String normalizedEmployeeId = employeeId.trim();
        if (employeeManager.findEmployeeById(normalizedEmployeeId) != null) {
            JOptionPane.showMessageDialog(parent, "Employee ID already exists.");
            return null;
        }

        String[] values = promptEmployeeValues(parent, new String[]{
            normalizedEmployeeId, "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", ""
        });

        if (values == null) {
            return null;
        }

        values[0] = normalizedEmployeeId;
        normalizeSalaryFields(parent, values);
        return new Employee(values);
    }

    public Employee promptEditedEmployee(Component parent, Employee existing) {
        if (existing == null) {
            JOptionPane.showMessageDialog(parent, "Employee record not found.");
            return null;
        }

        String[] values = new String[]{
            existing.getEmployeeId(),
            existing.getLastName(),
            existing.getFirstName(),
            existing.getBirthday(),
            existing.getAddress(),
            existing.getPhoneNumber(),
            existing.getSssNumber(),
            existing.getPhilhealthNumber(),
            existing.getTinNumber(),
            existing.getPagibigNumber(),
            existing.getEmploymentStatus(),
            existing.getJobPosition(),
            existing.getImmediateSupervisor(),
            String.valueOf(existing.getBasicSalary()),
            existing.getRiceSubsidy(),
            existing.getPhoneAllowance(),
            existing.getClothingAllowance(),
            existing.getGrossSemiMonthlyRate(),
            String.valueOf(existing.getHourlyRate())
        };

        String[] editedValues = promptEmployeeValues(parent, values);
        if (editedValues == null) {
            return null;
        }

        normalizeSalaryFields(parent, editedValues);
        return new Employee(editedValues);
    }

    private String[] promptEmployeeValues(Component parent, String[] values) {
        String[] labels = new String[]{
            "Employee ID",
            "Last Name",
            "First Name",
            "Birthday",
            "Address",
            "Phone Number",
            "SSS #",
            "Philhealth #",
            "TIN #",
            "Pag-ibig #",
            "Status",
            "Position",
            "Immediate Supervisor",
            "Basic Salary",
            "Rice Subsidy",
            "Phone Allowance",
            "Clothing Allowance",
            "Gross Semi-monthly Rate",
            "Hourly Rate"
        };

        for (int i = 0; i < labels.length; i++) {
            String newValue = JOptionPane.showInputDialog(parent, labels[i], values[i]);
            if (newValue == null) {
                return null;
            }
            values[i] = newValue.trim();
        }

        return values;
    }

    private void normalizeSalaryFields(Component parent, String[] values) {
        boolean hasBasic = !values[13].trim().isEmpty();
        boolean hasHourly = !values[18].trim().isEmpty();

        if (!hasBasic && !hasHourly) {
            JOptionPane.showMessageDialog(parent, "Enter either Basic Salary or Hourly Rate.");
            throw new IllegalArgumentException("Missing salary input.");
        }

        double basicSalaryValue;
        double hourlyRateValue;

        if (hasBasic) {
            basicSalaryValue = parseRequiredAmount(parent, "Basic Salary", values[13]);
            hourlyRateValue = computeHourlyRateFromBasicSalary(basicSalaryValue);
        } else {
            hourlyRateValue = parseRequiredAmount(parent, "Hourly Rate", values[18]);
            basicSalaryValue = computeBasicSalaryFromHourlyRate(hourlyRateValue);
        }

        values[13] = String.format(Locale.US, "%.2f", basicSalaryValue);
        values[18] = String.format(Locale.US, "%.2f", hourlyRateValue);
    }

    private double parseRequiredAmount(Component parent, String label, String value) {
        try {
            double amount = Double.parseDouble(value.replace(",", "").trim());
            if (amount < 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, label + " must be a valid non-negative number.");
            throw new IllegalArgumentException(label + " is invalid.");
        }
    }

    private double computeHourlyRateFromBasicSalary(double basicSalary) {
        return basicSalary / (WORK_DAYS_PER_MONTH * WORK_HOURS_PER_DAY);
    }

    private double computeBasicSalaryFromHourlyRate(double hourlyRate) {
        return hourlyRate * WORK_DAYS_PER_MONTH * WORK_HOURS_PER_DAY;
    }
}