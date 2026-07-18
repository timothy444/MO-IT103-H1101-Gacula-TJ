package com.motorph.ui;

import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;
import java.util.List;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TableRenderHelper {

    private TableRenderHelper() {
    }

    public static void refreshEmployeeTable(
            JTable employeeTable,
            List<Employee> employees,
            String search,
            String selectedStatus
    ) {
        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
        model.setRowCount(0);

        for (Employee emp : employees) {
            String empId = emp.getEmployeeId() == null ? "" : emp.getEmployeeId();
            String lastName = emp.getLastName() == null ? "" : emp.getLastName();
            String firstName = emp.getFirstName() == null ? "" : emp.getFirstName();
            String fullName = (firstName + " " + lastName).trim().toLowerCase();
            String status = emp.getEmploymentStatus() == null ? "" : emp.getEmploymentStatus();

            boolean matchesSearch = search.isEmpty()
                    || empId.toLowerCase().contains(search)
                    || firstName.toLowerCase().contains(search)
                    || lastName.toLowerCase().contains(search)
                    || fullName.contains(search);

            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatus)
                    || status.equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesStatus) {
                model.addRow(new Object[]{
                    emp.getEmployeeId(),
                    emp.getLastName(),
                    emp.getFirstName(),
                    emp.getBirthday(),
                    emp.getAddress(),
                    emp.getPhoneNumber(),
                    emp.getSssNumber(),
                    emp.getPhilhealthNumber(),
                    emp.getTinNumber(),
                    emp.getPagibigNumber(),
                    emp.getEmploymentStatus(),
                    emp.getJobPosition(),
                    emp.getImmediateSupervisor(),
                    String.format(Locale.US, "%.2f", emp.getBasicSalary()),
                    emp.getRiceSubsidy(),
                    emp.getPhoneAllowance(),
                    emp.getClothingAllowance(),
                    emp.getGrossSemiMonthlyRate(),
                    String.format(Locale.US, "%.2f", emp.getHourlyRate())
                });
            }
        }
    }

    public static void loadPayrollRecordTable(
            JTable payrollRecordTable,
            List<PayrollRecord> records,
            javax.swing.JLabel countLabel
    ) {
        DefaultTableModel model = (DefaultTableModel) payrollRecordTable.getModel();
        model.setRowCount(0);

        for (PayrollRecord pr : records) {
            model.addRow(new Object[]{
                pr.getEmployeeId(),
                pr.getEmployeeName(),
                pr.getPayCoverage(),
                String.format(Locale.US, "%.2f", pr.getHoursWorked()),
                String.format(Locale.US, "Php %,.2f", pr.getGrossPay()),
                String.format(Locale.US, "Php %,.2f", pr.getDeductions()),
                String.format(Locale.US, "Php %,.2f", pr.getNetPay())
            });
        }

        if (countLabel != null) {
            countLabel.setText("Records: " + records.size());
        }
    }
}
