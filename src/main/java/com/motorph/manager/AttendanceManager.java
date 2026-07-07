package com.motorph.manager;
/**
 *
 * @author Timothy Justin Gacula
 */

import com.motorph.model.Attendance;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceManager {

    private final File attendanceFile = new File("Attendance.csv");
    private final List<Attendance> attendanceRecords = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public void loadAttendance() throws IOException {
        attendanceRecords.clear();

        if (!attendanceFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(attendanceFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length >= 6) {
                    String employeeId = data[0].trim();
                    String date = data[3].trim();
                    String timeIn = data[4].trim();
                    String timeOut = data[5].trim();

                    double hoursWorked = calculateHoursWorked(timeIn, timeOut);
                    Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut, hoursWorked);
                    attendanceRecords.add(attendance);
                }
            }
        }
    }

    private double calculateHoursWorked(String timeInStr, String timeOutStr) {
        try {
            LocalTime timeIn = LocalTime.parse(timeInStr, timeFormatter);
            LocalTime timeOut = LocalTime.parse(timeOutStr, timeFormatter);

            long minutesWorked = Duration.between(timeIn, timeOut).toMinutes();

            if (minutesWorked < 0) {
                minutesWorked += 1440;
            }

            if (minutesWorked >= 300) {
                minutesWorked -= 60;
            }

            return Math.max(0, minutesWorked / 60.0);
        } catch (DateTimeParseException e) {
            return 0.0;
        }
    }

    private LocalDate parseAttendanceDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public double computeTotalHoursForEmployee(String employeeId) {
        double totalHours = 0.0;

        for (Attendance record : attendanceRecords) {
            if (record.getEmployeeId().equals(employeeId)) {
                totalHours += record.getHoursWorked();
            }
        }

        return totalHours;
    }

    public double computeHoursWorkedForPeriod(String employeeId, LocalDate startDate, LocalDate endDate) {
        double totalHours = 0.0;

        for (Attendance record : attendanceRecords) {
            if (!record.getEmployeeId().equals(employeeId)) {
                continue;
            }

            LocalDate attendanceDate = parseAttendanceDate(record.getDate());
            if (attendanceDate == null) {
                continue;
            }

            boolean withinRange =
                    (attendanceDate.isEqual(startDate) || attendanceDate.isAfter(startDate))
                    && (attendanceDate.isEqual(endDate) || attendanceDate.isBefore(endDate));

            if (withinRange) {
                totalHours += record.getHoursWorked();
            }
        }

        return totalHours;
    }

    public List<Attendance> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }

    public double computeHoursWorked(String empId) {
        try {
            loadAttendance();
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0;
        }

        return computeTotalHoursForEmployee(empId);
    }

    public double computeHoursWorked(String empId, LocalDate startDate, LocalDate endDate) {
        try {
            loadAttendance();
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0;
        }

        return computeHoursWorkedForPeriod(empId, startDate, endDate);
    }
}