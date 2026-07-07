package com.motorph.model;
/**
 *
 * @author Timothy Justin Gacula
 */
public class Employee {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String employmentStatus;
    private String jobPosition;
    private double basicSalary;
    private double hourlyRate;

    public Employee(String[] parsedData) {
    }

    public Employee(String employeeId, String firstName, String lastName,
                    String employmentStatus, String jobPosition,
                    double basicSalary, double hourlyRate) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employmentStatus = employmentStatus;
        this.jobPosition = jobPosition;
        this.basicSalary = basicSalary;
        this.hourlyRate = hourlyRate;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public char[] convertToCSV() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}