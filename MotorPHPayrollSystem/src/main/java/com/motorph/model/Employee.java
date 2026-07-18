package com.motorph.model;

/**
 *
 * @author Timothy Justin Gacula
 */

import com.motorph.manager.CsvUtil;
import java.util.Locale;

public class Employee {

    private String[] rawCsvData;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String employmentStatus;
    private String jobPosition;
    private double basicSalary;
    private double hourlyRate;

    public Employee(String[] parsedData) {
        this.rawCsvData = normalizeRawData(parsedData);
        this.employeeId = getField(0);
        this.lastName = getField(1);
        this.firstName = getField(2);
        this.employmentStatus = getField(10);
        this.jobPosition = getField(11);
        this.basicSalary = parseDoubleField(13);
        this.hourlyRate = parseDoubleField(18);
    }

    public Employee(String employeeId, String firstName, String lastName,
                    String employmentStatus, String jobPosition,
                    double basicSalary, double hourlyRate) {
        this.rawCsvData = new String[19];
        this.employeeId = safe(employeeId);
        this.firstName = safe(firstName);
        this.lastName = safe(lastName);
        this.employmentStatus = safe(employmentStatus);
        this.jobPosition = safe(jobPosition);
        this.basicSalary = basicSalary;
        this.hourlyRate = hourlyRate;

        rawCsvData[0] = this.employeeId;
        rawCsvData[1] = this.lastName;
        rawCsvData[2] = this.firstName;
        rawCsvData[3] = "";
        rawCsvData[4] = "";
        rawCsvData[5] = "";
        rawCsvData[6] = "";
        rawCsvData[7] = "";
        rawCsvData[8] = "";
        rawCsvData[9] = "";
        rawCsvData[10] = this.employmentStatus;
        rawCsvData[11] = this.jobPosition;
        rawCsvData[12] = "";
        rawCsvData[13] = String.format(Locale.US, "%.2f", this.basicSalary);
        rawCsvData[14] = "";
        rawCsvData[15] = "";
        rawCsvData[16] = "";
        rawCsvData[17] = "";
        rawCsvData[18] = String.format(Locale.US, "%.2f", this.hourlyRate);
    }

    private String[] normalizeRawData(String[] parsedData) {
        String[] normalized = new String[19];
        for (int i = 0; i < normalized.length; i++) {
            if (parsedData != null && i < parsedData.length && parsedData[i] != null) {
                normalized[i] = parsedData[i].trim();
            } else {
                normalized[i] = "";
            }
        }
        return normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String getField(int index) {
        if (rawCsvData == null || index < 0 || index >= rawCsvData.length || rawCsvData[index] == null) {
            return "";
        }
        return rawCsvData[index].trim();
    }

    private double parseDoubleField(int index) {
        try {
            return Double.parseDouble(getField(index).replace("\"", "").replace(",", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = safe(employeeId);
        rawCsvData[0] = this.employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = safe(firstName);
        rawCsvData[2] = this.firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = safe(lastName);
        rawCsvData[1] = this.lastName;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = safe(employmentStatus);
        rawCsvData[10] = this.employmentStatus;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = safe(jobPosition);
        rawCsvData[11] = this.jobPosition;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
        rawCsvData[13] = String.format(Locale.US, "%.2f", this.basicSalary);
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
        rawCsvData[18] = String.format(Locale.US, "%.2f", this.hourlyRate);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getBirthday() {
        return getField(3);
    }

    public String getAddress() {
        return getField(4);
    }

    public String getPhoneNumber() {
        return getField(5);
    }

    public String getSssNumber() {
        return getField(6);
    }

    public String getPhilhealthNumber() {
        return getField(7);
    }

    public String getTinNumber() {
        return getField(8);
    }

    public String getPagibigNumber() {
        return getField(9);
    }

    public String getImmediateSupervisor() {
        return getField(12);
    }

    public String getRiceSubsidy() {
        return getField(14);
    }

    public String getPhoneAllowance() {
        return getField(15);
    }

    public String getClothingAllowance() {
        return getField(16);
    }

    public String getGrossSemiMonthlyRate() {
        return getField(17);
    }

    public String convertToCSV() {
        return CsvUtil.toCsvRow(rawCsvData);
    }
}