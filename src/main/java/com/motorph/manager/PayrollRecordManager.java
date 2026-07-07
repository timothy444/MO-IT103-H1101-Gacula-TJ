package com.motorph.manager;
/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.model.PayrollRecord;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class PayrollRecordManager {
    private final String filePath = "PayrollRecords.csv";

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
            if (values[i].startsWith("\"") && values[i].endsWith("\"") && values[i].length() >= 2) {
                values[i] = values[i].substring(1, values[i].length() - 1).replace("\"\"", "\"");
            }
        }
        return values;
    }

    public List<PayrollRecord> loadPayrollRecords() {
        List<PayrollRecord> records = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = parseCsvLine(line);

                if (data.length >= 7) {
                    PayrollRecord record = new PayrollRecord(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            Double.parseDouble(data[3].trim()),
                            Double.parseDouble(data[4].trim()),
                            Double.parseDouble(data[5].trim()),
                            Double.parseDouble(data[6].trim())
                    );

                    records.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    public void savePayrollRecord(PayrollRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(escapeCsv(record.getEmployeeId()) + ","
                    + escapeCsv(record.getEmployeeName()) + ","
                    + escapeCsv(record.getPayCoverage()) + ","
                    + record.getHoursWorked() + ","
                    + record.getGrossPay() + ","
                    + record.getDeductions() + ","
                    + record.getNetPay());
            writer.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rewritePayrollRecords(List<PayrollRecord> records) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (PayrollRecord record : records) {
                writer.write(escapeCsv(record.getEmployeeId()) + ","
                        + escapeCsv(record.getEmployeeName()) + ","
                        + escapeCsv(record.getPayCoverage()) + ","
                        + record.getHoursWorked() + ","
                        + record.getGrossPay() + ","
                        + record.getDeductions() + ","
                        + record.getNetPay());
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePayrollRecord(int index) {
        List<PayrollRecord> records = loadPayrollRecords();

        if (index >= 0 && index < records.size()) {
            records.remove(index);
            rewritePayrollRecords(records);
        }
    }

    public void deleteAllPayrollRecords() {
        rewritePayrollRecords(new ArrayList<>());
    }
}