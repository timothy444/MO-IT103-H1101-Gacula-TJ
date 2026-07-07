/**
*
* @author Timothy Justin Gacula
*/
package com.motorph.manager;

import com.motorph.model.Employee;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EmployeeManager {

    private final File databaseFile = new File("EmployeeDatabase.csv");
    private final LinkedHashMap<String, Employee> employeeRecords;
    private String headerLine;

    public EmployeeManager() {
        this.headerLine = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";
        this.employeeRecords = new LinkedHashMap<>();
    }

    public void loadEmployees() throws IOException {
        employeeRecords.clear();

        if (!databaseFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) {
            String currentLine;
            boolean isFirstLine = true;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    headerLine = currentLine;
                    isFirstLine = false;
                    continue;
                }

                String[] parsedData = parseCsvLine(currentLine);

                if (parsedData.length >= 19) {
                    try {
                        Employee newEmployee = new Employee(parsedData);
                        employeeRecords.put(newEmployee.getEmployeeId(), newEmployee);
                    } catch (Exception e) {
                        System.out.println("Skipping invalid employee row: " + currentLine);
                    }
                }
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                columns.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }

        columns.add(currentField.toString().trim());
        return columns.toArray(String[]::new);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeRecords.values());
    }

    public Employee findEmployeeById(String employeeId) {
        return employeeRecords.get(employeeId);
    }

    public void saveOrUpdateEmployee(Employee employee) throws IOException {
        employeeRecords.put(employee.getEmployeeId(), employee);
        saveDatabase();
    }

    public void addEmployee(Employee employee) throws IOException {
        employeeRecords.put(employee.getEmployeeId(), employee);
        saveDatabase();
    }

    public void softDeleteEmployee(String employeeId) throws IOException {
        Employee employee = employeeRecords.get(employeeId);

        if (employee != null) {
            employee.setEmploymentStatus("Deleted");
            saveDatabase();
        }
    }

    public void deleteEmployeePermanently(String employeeId) throws IOException {
        employeeRecords.remove(employeeId);
        saveDatabase();
    }

    private void saveDatabase() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(databaseFile))) {
            writer.write(headerLine);
            writer.newLine();

            for (Employee employee : employeeRecords.values()) {
                writer.write(employee.convertToCSV());
                writer.newLine();
            }
        }
    }

    public void deleteEmployee(String empId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
