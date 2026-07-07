package com.motorph.model;
/**
 *
 * @author Timothy Justin Gacula
 */

public class Attendance {
    private final String employeeId;
    private final String date;
    private final String timeIn;
    private final String timeOut;
    private final double hoursWorked;

    public Attendance(String employeeId, String date, String timeIn, String timeOut, double hoursWorked) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.hoursWorked = hoursWorked;
    }

    public String getEmployeeId() { return employeeId; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }
    public double getHoursWorked() { return hoursWorked; }
}