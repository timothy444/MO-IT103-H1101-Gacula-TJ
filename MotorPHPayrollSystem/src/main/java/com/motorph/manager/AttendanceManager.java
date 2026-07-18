package com.motorph.manager;

/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.model.Attendance;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceManager {

    private static final int EXPECTED_COLUMN_COUNT = 6;
    private static final int MINUTES_PER_DAY = 1440;
    private static final int LUNCH_BREAK_MINUTES = 60;
    private static final int MINUTES_FOR_LUNCH_DEDUCTION = 300;

    private final Path attendancePath;
    private final List<Attendance> attendanceRecords;
    private final DateTimeFormatter timeFormatter;
    private final DateTimeFormatter dateFormatter;

    public AttendanceManager() {
        this(Path.of("Attendance.csv"));
    }

    public AttendanceManager(Path attendancePath) {
        this.attendancePath = attendancePath;
        this.attendanceRecords = new ArrayList<>();
        this.timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        this.dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    }

    public void loadAttendance() throws IOException {
        attendanceRecords.clear();

        if (Files.notExists(attendancePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(attendancePath, StandardCharsets.UTF_8)) {
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

                String[] data = CsvUtil.parseCsvLine(line);

                if (data.length < EXPECTED_COLUMN_COUNT) {
                    continue;
                }

                String employeeId = data[0].trim();
                String date = data[3].trim();
                String timeIn = data[4].trim();
                String timeOut = data[5].trim();

                if (timeIn.isEmpty() || timeOut.isEmpty()) {
                    continue;
                }

                double hoursWorked = calculateHoursWorked(timeIn, timeOut);
                Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut, hoursWorked);
                attendanceRecords.add(attendance);
            }
        }
    }

    private double calculateHoursWorked(String timeInStr, String timeOutStr) {
        try {
            LocalTime timeIn = LocalTime.parse(timeInStr, timeFormatter);
            LocalTime timeOut = LocalTime.parse(timeOutStr, timeFormatter);

            long minutesWorked = Duration.between(timeIn, timeOut).toMinutes();

            if (minutesWorked < 0) {
                minutesWorked += MINUTES_PER_DAY;
            }

            if (minutesWorked >= MINUTES_FOR_LUNCH_DEDUCTION) {
                minutesWorked -= LUNCH_BREAK_MINUTES;
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
        validateEmployeeId(employeeId);

        double totalHours = 0.0;

        for (Attendance record : attendanceRecords) {
            if (record.getEmployeeId().equals(employeeId.trim())) {
                totalHours += record.getHoursWorked();
            }
        }

        return totalHours;
    }

    public double computeHoursWorkedForPeriod(String employeeId, LocalDate startDate, LocalDate endDate) {
        validateEmployeeId(employeeId);

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date.");
        }

        double totalHours = 0.0;

        for (Attendance record : attendanceRecords) {
            if (!record.getEmployeeId().equals(employeeId.trim())) {
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

    public double computeHoursWorked(String employeeId) throws IOException {
        loadAttendance();
        return computeTotalHoursForEmployee(employeeId);
    }

    public double computeHoursWorked(String employeeId, LocalDate startDate, LocalDate endDate) throws IOException {
        loadAttendance();
        return computeHoursWorkedForPeriod(employeeId, startDate, endDate);
    }

    private void validateEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
    }
}