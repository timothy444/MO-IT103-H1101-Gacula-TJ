package com.motorph.manager;

/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.model.Employee;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeManager {

    private static final Logger logger = Logger.getLogger(EmployeeManager.class.getName());

    private Path databaseFile;
    private final LinkedHashMap<String, Employee> employeeRecords;
    private String headerLine;

    public EmployeeManager() {
        this(Path.of("EmployeeDatabase.csv"));
    }

    public EmployeeManager(Path databaseFile) {
        this.databaseFile = databaseFile;
        this.employeeRecords = new LinkedHashMap<>();
        this.headerLine = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";
    }

    public void loadEmployees() throws IOException {
        employeeRecords.clear();

        if (Files.notExists(databaseFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(databaseFile, StandardCharsets.UTF_8)) {
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

                String[] parsedData = CsvUtil.parseCsvLine(currentLine);

                if (parsedData.length >= 19) {
                    try {
                        Employee newEmployee = new Employee(parsedData);
                        employeeRecords.put(newEmployee.getEmployeeId(), newEmployee);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Skipping invalid employee row: {0}", currentLine);
                    }
                }
            }
        }
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeRecords.values());
    }

    public Employee findEmployeeById(String employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeRecords.get(employeeId.trim());
    }

    public boolean employeeExists(String employeeId) {
        return findEmployeeById(employeeId) != null;
    }

    public void saveOrUpdateEmployee(Employee employee) throws IOException {
        employeeRecords.put(employee.getEmployeeId(), employee);
        saveDatabase();
    }

    public void addEmployee(Employee employee) throws IOException {
        employeeRecords.put(employee.getEmployeeId(), employee);
        saveDatabase();
    }

    public void deleteEmployeePermanently(String employeeId) throws IOException {
        employeeRecords.remove(employeeId);
        saveDatabase();
    }

    public void deleteEmployee(String employeeId) throws IOException {
        deleteEmployeePermanently(employeeId);
    }

    private void saveDatabase() throws IOException {
        Path parent = databaseFile.toAbsolutePath().getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(databaseFile, StandardCharsets.UTF_8)) {
            writer.write(headerLine);
            writer.newLine();

            for (Employee employee : employeeRecords.values()) {
                writer.write(employee.convertToCSV());
                writer.newLine();
            }
        }
    }
}