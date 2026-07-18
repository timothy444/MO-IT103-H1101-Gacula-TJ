package com.motorph.manager;

/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.model.PayrollRecord;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PayrollRecordManager {

    private static final String HEADER_LINE = "Employee ID,Name,Coverage,Hours Worked,Gross Pay,Deductions,Net Pay";

    private static final int EXPECTED_COLUMN_COUNT = 7;

    private final Path filePath;

    public PayrollRecordManager() {
        this(Path.of("PayrollRecords.csv"));
    }

    public PayrollRecordManager(Path filePath) {
        this.filePath = filePath;
    }

    private String[] parseCsvLine(String line) {
        return CsvUtil.parseCsvLine(line);
    }

    public List<PayrollRecord> loadPayrollRecords() throws IOException {
        List<PayrollRecord> records = new ArrayList<>();

        if (Files.notExists(filePath)) {
            return records;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = parseCsvLine(line);

                if (data.length < EXPECTED_COLUMN_COUNT) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;

                    if (isHeaderLine(data)) {
                        continue;
                    }
                }

                PayrollRecord record = parseRecord(data);
                if (record != null) {
                    records.add(record);
                }
            }
        }

        return records;
    }

    private boolean isHeaderLine(String[] data) {
        try {
            Double.parseDouble(data[3].trim());
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private PayrollRecord parseRecord(String[] data) {
        try {
            return new PayrollRecord(
                    data[0].trim(),
                    data[1].trim(),
                    data[2].trim(),
                    Double.parseDouble(data[3].trim()),
                    Double.parseDouble(data[4].trim()),
                    Double.parseDouble(data[5].trim()),
                    Double.parseDouble(data[6].trim())
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void savePayrollRecord(PayrollRecord record) throws IOException {
        validateRecord(record);

        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        boolean fileExists = Files.exists(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
        )) {
            if (!fileExists) {
                writer.write(HEADER_LINE);
                writer.newLine();
            }

            writer.write(toCsvRow(record));
            writer.newLine();
        }
    }

    public void rewritePayrollRecords(List<PayrollRecord> records) throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        Path tempFile = (parent == null)
                ? Files.createTempFile("payroll-records-", ".tmp")
                : Files.createTempFile(parent, "payroll-records-", ".tmp");

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER_LINE);
                writer.newLine();

                for (PayrollRecord record : records) {
                    validateRecord(record);
                    writer.write(toCsvRow(record));
                    writer.newLine();
                }
            }

            Files.move(
                    tempFile,
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    public void deletePayrollRecord(int index) throws IOException {
        List<PayrollRecord> records = loadPayrollRecords();

        if (index >= 0 && index < records.size()) {
            records.remove(index);
            rewritePayrollRecords(records);
        }
    }

    public void deletePayrollRecords(int[] indices) throws IOException {
        if (indices == null || indices.length == 0) {
            return;
        }

        List<PayrollRecord> records = loadPayrollRecords();

        for (int i = indices.length - 1; i >= 0; i--) {
            int index = indices[i];

            if (index >= 0 && index < records.size()) {
                records.remove(index);
            }
        }

        rewritePayrollRecords(records);
    }

    public void deleteAllPayrollRecords() throws IOException {
        rewritePayrollRecords(new ArrayList<>());
    }

    private void validateRecord(PayrollRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Payroll record cannot be null.");
        }

        if (record.getEmployeeId() == null || record.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }

        if (record.getEmployeeName() == null || record.getEmployeeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty.");
        }

        if (record.getPayCoverage() == null || record.getPayCoverage().trim().isEmpty()) {
            throw new IllegalArgumentException("Pay coverage cannot be empty.");
        }
    }

    private String toCsvRow(PayrollRecord record) {
        return CsvUtil.toCsvRow(
                record.getEmployeeId(),
                record.getEmployeeName(),
                record.getPayCoverage(),
                String.valueOf(record.getHoursWorked()),
                String.valueOf(record.getGrossPay()),
                String.valueOf(record.getDeductions()),
                String.valueOf(record.getNetPay())
        );
    }
}