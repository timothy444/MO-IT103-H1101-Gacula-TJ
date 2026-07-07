package com.motorph.ui;
/**
 *
 * @author Timothy Justin Gacula
 */

import com.motorph.manager.AttendanceManager;
import com.motorph.manager.PayrollCalculator;
import com.motorph.manager.PayrollRecordManager;
import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PayrollPortalMain extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(PayrollPortalMain.class.getName());

    private final AttendanceManager attendanceManager = new AttendanceManager();
    private final PayrollCalculator payrollCalculator = new PayrollCalculator();
    private final PayrollRecordManager payrollRecordManager = new PayrollRecordManager();

    private final List<String[]> employeeRows = new ArrayList<>();
    private final String employeeFilePath = "EmployeeDatabase.csv";

public PayrollPortalMain() {
    initComponents();
    setLocationRelativeTo(null);

    initializeTables();
    employeeTable.getTableHeader().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
    employeeTable.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
    employeeTable.setRowHeight(22);

    initializeFormDefaults();
    initializeStatusFilter();
    wireEvents();
    initializeClipboardSupport();

    loadEmployeeTable();
    loadPayrollRecordTable();
}
    private LocalDate[] parseCoverageDates(String coverage) {
    try {
        String cleaned = coverage.trim();
        String[] parts = cleaned.split(",");

        if (parts.length != 2) {
            return null;
        }

        String monthAndDays = parts[0].trim();   // June 1-15
        String yearText = parts[1].trim();       // 2024

        String[] firstParts = monthAndDays.split(" ");
        if (firstParts.length != 2) {
            return null;
        }

        String month = firstParts[0].trim();     // June
        String[] dayRange = firstParts[1].split("-");
        if (dayRange.length != 2) {
            return null;
        }

        String startText = month + " " + dayRange[0].trim() + ", " + yearText;
        String endText = month + " " + dayRange[1].trim() + ", " + yearText;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

        LocalDate startDate = LocalDate.parse(startText, formatter);
        LocalDate endDate = LocalDate.parse(endText, formatter);

        return new LocalDate[]{startDate, endDate};
    } catch (DateTimeParseException e) {
        return null;
    }
    
    }
private static final double WORK_DAYS_PER_MONTH = 22.0;
private static final double WORK_HOURS_PER_DAY = 8.0;

private double computeHourlyRateFromBasicSalary(double basicSalary) {
    return basicSalary / (WORK_DAYS_PER_MONTH * WORK_HOURS_PER_DAY);
}

private double computeBasicSalaryFromHourlyRate(double hourlyRate) {
    return hourlyRate * WORK_DAYS_PER_MONTH * WORK_HOURS_PER_DAY;
}

private double parseRequiredAmount(String label, String value) {
    try {
        double amount = Double.parseDouble(value.replace(",", "").trim());
        if (amount < 0) {
            throw new NumberFormatException();
        }
        return amount;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, label + " must be a valid non-negative number.");
        throw new IllegalArgumentException(label + " is invalid.");
    }
}

private void wireEvents() {
    deleteEmployeeButton.addActionListener(this::deleteEmployeeButtonActionPerformed);
    deletePayrollRecordButton.addActionListener(this::deletePayrollRecordButtonActionPerformed);
    addEmployeeButton.addActionListener(this::addEmployeeButtonActionPerformed);

    statusFilterComboBox.addActionListener(e -> filterEmployeeTable());

    searchTextField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterEmployeeTable();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterEmployeeTable();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterEmployeeTable();
        }
    });
}

private void initializeClipboardSupport() {
    installTextClipboardShortcuts(employeeIDTextField);
    installTextClipboardShortcuts(employeeNameTextField);
    installTextClipboardShortcuts(payCoverageTextField);
    installTextClipboardShortcuts(manualHoursTextField);
    installTextClipboardShortcuts(searchTextField);

    installTableCopyShortcut(employeeTable);
    installTableCopyShortcut(payrollRecordTable);
}

private void installTextClipboardShortcuts(JTextField textField) {
    textField.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK), "copyText");
    textField.getActionMap().put("copyText", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            textField.copy();
        }
    });

    textField.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK), "cutText");
    textField.getActionMap().put("cutText", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            textField.cut();
        }
    });

    textField.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK), "pasteText");
    textField.getActionMap().put("pasteText", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            textField.paste();
        }
    });
}

private void installTableCopyShortcut(JTable table) {
    table.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK), "copyTableCell");
    table.getActionMap().put("copyTableCell", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            copySelectedTableCell(table);
        }
    });
}

private void copySelectedTableCell(JTable table) {
    int row = table.getSelectedRow();
    int col = table.getSelectedColumn();

    if (row == -1 || col == -1) {
        JOptionPane.showMessageDialog(PayrollPortalMain.this, "Select a table cell first.");
        return;
    }

    Object value = table.getValueAt(row, col);
    String text = value == null ? "" : value.toString();

    StringSelection selection = new StringSelection(text);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

    statusMessageLabel.setText("Copied table cell.");
}

private void initializeFormDefaults() {
    employeeIDTextField.setText("");
    employeeNameTextField.setText("");
    employeeNameTextField.setEditable(true);
    payCoverageTextField.setText("");
    manualHoursTextField.setText("");
    searchTextField.setText("");
    statusMessageLabel.setText("Ready");
}

private void initializeStatusFilter() {
    statusFilterComboBox.setModel(
            new javax.swing.DefaultComboBoxModel<>(
                    new String[]{"All", "Regular", "Probationary"}
            )
    );
}

private String[] parseCsvLine(String line) {
    String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    for (int i = 0; i < values.length; i++) {
        values[i] = values[i].trim().replace("\"", "");
    }
    return values;
}

private String safeValue(String[] row, int index) {
    return index < row.length ? row[index].trim().replace("\"", "") : "";
}

private double parseMoney(String value) {
    try {
        return Double.parseDouble(value.replace(",", "").trim());
    } catch (NumberFormatException e) {
        return 0.0;
    }
}

private Employee findEmployeeFromLoadedTable(String employeeId) {
    String normalizedId = employeeId == null ? "" : employeeId.trim();

    for (String[] row : employeeRows) {
        if (safeValue(row, 0).equalsIgnoreCase(normalizedId)) {
            String id = safeValue(row, 0);
            String lastName = safeValue(row, 1);
            String firstName = safeValue(row, 2);
            String status = safeValue(row, 10);
            String position = safeValue(row, 11);
            double basicSalary = parseMoney(safeValue(row, 13));
            double hourlyRate = parseMoney(safeValue(row, 18));

            return new Employee(id, firstName, lastName, status, position, basicSalary, hourlyRate);
        }
    }

    return null;
}

private void loadEmployeeTable() {
    employeeRows.clear();

    try (BufferedReader reader = new BufferedReader(new FileReader(employeeFilePath))) {
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] data = parseCsvLine(line);
            if (data.length >= 19) {
                employeeRows.add(data);
            }
        }

        filterEmployeeTable();
        statusMessageLabel.setText("Employee database loaded.");
    } catch (Exception e) {
        logger.log(java.util.logging.Level.SEVERE, "Error loading Employee Database.", e);
        JOptionPane.showMessageDialog(this, "Error loading Employee Database.");
        statusMessageLabel.setText("Failed to load employee database.");
    }
}

private void filterEmployeeTable() {
    DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
    model.setRowCount(0);

    String search = searchTextField.getText().trim().toLowerCase();
    String selectedStatus = String.valueOf(statusFilterComboBox.getSelectedItem());

    for (String[] data : employeeRows) {
        String empId = safeValue(data, 0).toLowerCase();
        String lastName = safeValue(data, 1).toLowerCase();
        String firstName = safeValue(data, 2).toLowerCase();
        String fullName = (firstName + " " + lastName).trim().toLowerCase();
        String status = safeValue(data, 10);

        boolean matchesSearch = search.isEmpty()
                || empId.contains(search)
                || firstName.contains(search)
                || lastName.contains(search)
                || fullName.contains(search);

        boolean matchesStatus = "All".equalsIgnoreCase(selectedStatus)
                || status.equalsIgnoreCase(selectedStatus);

        if (matchesSearch && matchesStatus) {
            model.addRow(new Object[]{
                safeValue(data, 0),
                safeValue(data, 1),
                safeValue(data, 2),
                safeValue(data, 3),
                safeValue(data, 4),
                safeValue(data, 5),
                safeValue(data, 6),
                safeValue(data, 7),
                safeValue(data, 8),
                safeValue(data, 9),
                safeValue(data, 10),
                safeValue(data, 11),
                safeValue(data, 12),
                safeValue(data, 13),
                safeValue(data, 14),
                safeValue(data, 15),
                safeValue(data, 16),
                safeValue(data, 17),
                safeValue(data, 18)
            });
        }
    }
}

private void loadPayrollRecordTable() {
    DefaultTableModel model = (DefaultTableModel) payrollRecordTable.getModel();
    model.setRowCount(0);

    List<PayrollRecord> records = payrollRecordManager.loadPayrollRecords();
    for (PayrollRecord pr : records) {
        model.addRow(new Object[]{
            pr.getEmployeeId(),
            pr.getEmployeeName(),
            pr.getPayCoverage(),
            String.format("%.2f", pr.getHoursWorked()),
            String.format("Php %,.2f", pr.getGrossPay()),
            String.format("Php %,.2f", pr.getDeductions()),
            String.format("Php %,.2f", pr.getNetPay())
        });
    }

    payrollRecordCountLabel.setText("Records: " + records.size());
}

private void populateEmployeeName() {
    String empId = employeeIDTextField.getText().trim();

    if (empId.isEmpty()) {
        employeeNameTextField.setText("");
        statusMessageLabel.setText("Enter an employee ID.");
        return;
    }

    Employee emp = findEmployeeFromLoadedTable(empId);
    if (emp != null) {
        employeeNameTextField.setText(emp.getFullName());
        statusMessageLabel.setText("Employee found.");
    } else {
        employeeNameTextField.setText("");
        statusMessageLabel.setText("Employee ID not found.");
    }
}

    private void deletePayrollRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = payrollRecordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a record from the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this specific payroll record?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            payrollRecordManager.deletePayrollRecord(selectedRow);
            loadPayrollRecordTable();
            statusMessageLabel.setText("Payroll record deleted.");
        }
    }

private void deleteEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {
    int selectedRow = employeeTable.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select an employee from the table first.");
        return;
    }

    String empId = String.valueOf(employeeTable.getValueAt(selectedRow, 0)).trim();
    String firstName = String.valueOf(employeeTable.getValueAt(selectedRow, 2)).trim();
    String lastName = String.valueOf(employeeTable.getValueAt(selectedRow, 1)).trim();
    String fullName = firstName + " " + lastName;

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete employee " + empId + " - " + fullName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    boolean removed = employeeRows.removeIf(row -> safeValue(row, 0).trim().equals(empId));

    if (!removed) {
        JOptionPane.showMessageDialog(this, "Employee record was not found in memory.");
        statusMessageLabel.setText("Delete failed.");
        return;
    }

    saveEmployeeRowsToCsv();
    loadEmployeeTable();
    statusMessageLabel.setText("Employee deleted successfully.");
    JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
}

    private void initializeTables() {
        payrollRecordTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Employee ID",
                    "Name",
                    "Coverage",
                    "Hours Worked",
                    "Gross Pay",
                    "Deductions",
                    "Net Pay"
                }
        ));

        employeeTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Employee #",
                    "Last Name",
                    "First Name",
                    "Birthday",
                    "Address",
                    "Phone Number",
                    "SSS #",
                    "Philhealth #",
                    "TIN #",
                    "Pag-ibig #",
                    "Status",
                    "Position",
                    "Immediate Supervisor",
                    "Basic Salary",
                    "Rice Subsidy",
                    "Phone Allowance",
                    "Clothing Allowance",
                    "Gross Semi-monthly Rate",
                    "Hourly Rate"
                }
        ));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        mainTabbedPane = new javax.swing.JTabbedPane();
        employeeFormPanel = new javax.swing.JPanel();
        computePayrollButton = new javax.swing.JButton();
        resetFormButton = new javax.swing.JButton();
        statusMessageLabel = new javax.swing.JLabel();
        employeeIDTextField = new javax.swing.JTextField();
        employeeNameTextField = new javax.swing.JTextField();
        payCoverageTextField = new javax.swing.JTextField();
        manualHoursTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        submittedRecordsPanel = new javax.swing.JPanel();
        payrollRecordsScrollPane = new javax.swing.JScrollPane();
        payrollRecordTable = new javax.swing.JTable();
        deletePayrollRecordButton = new javax.swing.JButton();
        clearPayrollRecordsButton = new javax.swing.JButton();
        payrollRecordCountLabel = new javax.swing.JLabel();
        employeeDatabasePanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        statusFilterComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        employeeTable = new javax.swing.JTable();
        deleteEmployeeButton = new javax.swing.JButton();
        editEmployeeButton = new javax.swing.JButton();
        addEmployeeButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Copperplate Gothic Bold", 0, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("MotorPH");
        jLabel1.setOpaque(true);

        mainTabbedPane.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N

        employeeFormPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        computePayrollButton.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        computePayrollButton.setText("Compute Payroll");
        computePayrollButton.addActionListener(this::computePayrollButtonActionPerformed);

        resetFormButton.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        resetFormButton.setText("Reset Form");
        resetFormButton.addActionListener(this::resetFormButtonActionPerformed);

        statusMessageLabel.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        statusMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusMessageLabel.setText("Ready");

        employeeIDTextField.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        employeeIDTextField.addActionListener(this::employeeIDTextFieldActionPerformed);

        employeeNameTextField.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        employeeNameTextField.addActionListener(this::employeeNameTextFieldActionPerformed);

        payCoverageTextField.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        payCoverageTextField.addActionListener(this::payCoverageTextFieldActionPerformed);

        manualHoursTextField.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        jLabel2.setText("Employee ID:");

        jLabel3.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        jLabel3.setText("Employee Name:");

        jLabel4.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        jLabel4.setText("Pay Coverage:");

        jLabel5.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        jLabel5.setText("Manual Hours:");

        javax.swing.GroupLayout employeeFormPanelLayout = new javax.swing.GroupLayout(employeeFormPanel);
        employeeFormPanel.setLayout(employeeFormPanelLayout);
        employeeFormPanelLayout.setHorizontalGroup(
            employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeeFormPanelLayout.createSequentialGroup()
                .addGap(528, 542, Short.MAX_VALUE)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(employeeFormPanelLayout.createSequentialGroup()
                        .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(computePayrollButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5)))
                        .addGap(63, 63, 63)
                        .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(employeeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(employeeIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(payCoverageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(manualHoursTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(resetFormButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(employeeFormPanelLayout.createSequentialGroup()
                        .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)))
                .addContainerGap(543, Short.MAX_VALUE))
        );
        employeeFormPanelLayout.setVerticalGroup(
            employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeeFormPanelLayout.createSequentialGroup()
                .addGap(292, 292, 292)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(employeeIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(employeeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(payCoverageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manualHoursTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(employeeFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(computePayrollButton)
                    .addComponent(resetFormButton))
                .addGap(18, 18, 18)
                .addComponent(statusMessageLabel)
                .addContainerGap(281, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Employee Form", employeeFormPanel);

        submittedRecordsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        payrollRecordsScrollPane.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 10)); // NOI18N

        payrollRecordTable.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 10)); // NOI18N
        payrollRecordTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Employee ID", "Name", "Coverage", "Hours Worked", "Gross Pay", "Deductions", "Net Pay"
            }
        ));
        payrollRecordsScrollPane.setViewportView(payrollRecordTable);

        deletePayrollRecordButton.setText("Delete Selected");

        clearPayrollRecordsButton.setText("Clear All Records");
        clearPayrollRecordsButton.addActionListener(this::clearPayrollRecordsButtonActionPerformed);

        payrollRecordCountLabel.setText("Records: 0");

        javax.swing.GroupLayout submittedRecordsPanelLayout = new javax.swing.GroupLayout(submittedRecordsPanel);
        submittedRecordsPanel.setLayout(submittedRecordsPanelLayout);
        submittedRecordsPanelLayout.setHorizontalGroup(
            submittedRecordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(submittedRecordsPanelLayout.createSequentialGroup()
                .addComponent(payrollRecordsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1349, Short.MAX_VALUE)
                .addGap(16, 16, 16))
            .addGroup(submittedRecordsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(payrollRecordCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deletePayrollRecordButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearPayrollRecordsButton))
        );
        submittedRecordsPanelLayout.setVerticalGroup(
            submittedRecordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(submittedRecordsPanelLayout.createSequentialGroup()
                .addComponent(payrollRecordsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(submittedRecordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deletePayrollRecordButton)
                    .addComponent(clearPayrollRecordsButton)
                    .addComponent(payrollRecordCountLabel))
                .addContainerGap())
        );

        mainTabbedPane.addTab("Submitted Records", submittedRecordsPanel);

        employeeDatabasePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        employeeDatabasePanel.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 10)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        jLabel6.setText("Search Employee:");

        statusFilterComboBox.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 12)); // NOI18N
        statusFilterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        employeeTable.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 8)); // NOI18N
        employeeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Employee ID", "Last Name", "First Name", "Birthday", "Address", "Phone Number", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position", "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
            }
        ));
        jScrollPane1.setViewportView(employeeTable);

        deleteEmployeeButton.setText("Delete Employee");

        editEmployeeButton.setText("Edit Employee");
        editEmployeeButton.addActionListener(this::editEmployeeButtonActionPerformed);

        addEmployeeButton.setText("Add Employee");
        addEmployeeButton.addActionListener(this::addEmployeeButtonActionPerformed);

        javax.swing.GroupLayout employeeDatabasePanelLayout = new javax.swing.GroupLayout(employeeDatabasePanel);
        employeeDatabasePanel.setLayout(employeeDatabasePanelLayout);
        employeeDatabasePanelLayout.setHorizontalGroup(
            employeeDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeeDatabasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(employeeDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(employeeDatabasePanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(4, 4, 4)
                        .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusFilterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, employeeDatabasePanelLayout.createSequentialGroup()
                        .addGap(0, 906, Short.MAX_VALUE)
                        .addComponent(addEmployeeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editEmployeeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteEmployeeButton)
                        .addGap(88, 88, 88)))
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        employeeDatabasePanelLayout.setVerticalGroup(
            employeeDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeeDatabasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(employeeDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusFilterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(employeeDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addEmployeeButton)
                    .addComponent(deleteEmployeeButton)
                    .addComponent(editEmployeeButton))
                .addGap(19, 19, 19))
        );

        mainTabbedPane.addTab("Employee Database", employeeDatabasePanel);

        jLabel7.setText("jLabel7");

        jLabel8.setText("jLabel8");

        jLabel9.setFont(new java.awt.Font("Leelawadee UI Semilight", 0, 14)); // NOI18N
        jLabel9.setText("MotorPH Payroll System");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(81, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(394, 394, 394)
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(mainTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1367, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65))))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel7)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(547, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(738, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 788, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel7)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(429, Short.MAX_VALUE)
                    .addComponent(jLabel8)
                    .addContainerGap(430, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void employeeIDTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_employeeIDTextFieldActionPerformed
    populateEmployeeName();
    }//GEN-LAST:event_employeeIDTextFieldActionPerformed

    
    private void clearPayrollRecordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearPayrollRecordsButtonActionPerformed
    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Permanently delete ALL payroll history?",
            "Warning",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        payrollRecordManager.deleteAllPayrollRecords();
        loadPayrollRecordTable();
        statusMessageLabel.setText("All payroll records deleted.");
    }    }//GEN-LAST:event_clearPayrollRecordsButtonActionPerformed

    private void editEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEmployeeButtonActionPerformed
    int selectedRow = employeeTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select an employee from the table first.");
        return;
    }

    String selectedId = employeeTable.getValueAt(selectedRow, 0).toString();
    String[] target = null;

    for (String[] row : employeeRows) {
        if (safeValue(row, 0).equals(selectedId)) {
            target = row;
            break;
        }
    }

    if (target == null) {
        JOptionPane.showMessageDialog(this, "Employee record not found.");
        return;
    }

    for (int i = 0; i < 19; i++) {
        String columnName = employeeTable.getColumnName(i);
        String newValue = JOptionPane.showInputDialog(this, "Edit " + columnName + ":", safeValue(target, i));
        if (newValue != null) {
            target[i] = newValue.trim();
        }
    }
    try {
    double basicSalaryValue = parseRequiredAmount("Basic Salary", safeValue(target, 13));
    double hourlyRateValue = parseRequiredAmount("Hourly Rate", safeValue(target, 18));

    if (!safeValue(target, 13).isEmpty()) {
        hourlyRateValue = computeHourlyRateFromBasicSalary(basicSalaryValue);
    } else {
        basicSalaryValue = computeBasicSalaryFromHourlyRate(hourlyRateValue);
    }

    target[13] = String.format(java.util.Locale.US, "%.2f", basicSalaryValue);
    target[18] = String.format(java.util.Locale.US, "%.2f", hourlyRateValue);

} catch (IllegalArgumentException e) {
    return;
}

    saveEmployeeRowsToCsv();
    loadEmployeeTable();
    statusMessageLabel.setText("Employee updated.");
    }//GEN-LAST:event_editEmployeeButtonActionPerformed

    private void payCoverageTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_payCoverageTextFieldActionPerformed
    computePayrollButtonActionPerformed(evt);
    }//GEN-LAST:event_payCoverageTextFieldActionPerformed

    private void resetFormButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetFormButtonActionPerformed
    employeeIDTextField.setText("");
    employeeNameTextField.setText("");
    payCoverageTextField.setText("");
    manualHoursTextField.setText("");
    statusMessageLabel.setText("Ready");
    }//GEN-LAST:event_resetFormButtonActionPerformed

    private void employeeNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_employeeNameTextFieldActionPerformed
    String empId = employeeIDTextField.getText().trim();
    String enteredName = employeeNameTextField.getText().trim();

    if (empId.isEmpty() || enteredName.isEmpty()) {
        statusMessageLabel.setText("Enter both employee ID and employee name.");
        return;
    }

    Employee emp = findEmployeeFromLoadedTable(empId);
    if (emp == null) {
        statusMessageLabel.setText("Employee ID not found.");
        JOptionPane.showMessageDialog(this, "Employee ID not found.");
        return;
    }

    if (emp.getFullName().equalsIgnoreCase(enteredName)) {
        statusMessageLabel.setText("Employee ID and name matched.");
    } else {
        statusMessageLabel.setText("Employee ID and name do not match.");
        JOptionPane.showMessageDialog(this, "Employee ID and name do not match.");
    }
    }//GEN-LAST:event_employeeNameTextFieldActionPerformed

    private void addEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEmployeeButtonActionPerformed
    String employeeId = JOptionPane.showInputDialog(this, "Employee ID:");
    if (employeeId == null || employeeId.trim().isEmpty()) {
        return;
    }

    String normalizedEmployeeId = employeeId.trim();

    if (findEmployeeFromLoadedTable(normalizedEmployeeId) != null) {
        JOptionPane.showMessageDialog(this, "Employee ID already exists.");
        return;
    }

    String lastName = JOptionPane.showInputDialog(this, "Last Name:");
    if (lastName == null) return;

    String firstName = JOptionPane.showInputDialog(this, "First Name:");
    if (firstName == null) return;

    String birthday = JOptionPane.showInputDialog(this, "Birthday:");
    if (birthday == null) return;

    String address = JOptionPane.showInputDialog(this, "Address:");
    if (address == null) return;

    String phone = JOptionPane.showInputDialog(this, "Phone Number:");
    if (phone == null) return;

    String sss = JOptionPane.showInputDialog(this, "SSS #:");
    if (sss == null) return;

    String philhealth = JOptionPane.showInputDialog(this, "Philhealth #:");
    if (philhealth == null) return;

    String tin = JOptionPane.showInputDialog(this, "TIN #:");
    if (tin == null) return;

    String pagibig = JOptionPane.showInputDialog(this, "Pag-ibig #:");
    if (pagibig == null) return;

    String status = JOptionPane.showInputDialog(this, "Status:");
    if (status == null) return;

    String position = JOptionPane.showInputDialog(this, "Position:");
    if (position == null) return;

    String supervisor = JOptionPane.showInputDialog(this, "Immediate Supervisor:");
    if (supervisor == null) return;

    String basicSalaryInput = JOptionPane.showInputDialog(this, "Basic Salary (leave blank if Hourly Rate will be entered):");
    if (basicSalaryInput == null) return;

    String riceSubsidy = JOptionPane.showInputDialog(this, "Rice Subsidy:");
    if (riceSubsidy == null) return;

    String phoneAllowance = JOptionPane.showInputDialog(this, "Phone Allowance:");
    if (phoneAllowance == null) return;

    String clothingAllowance = JOptionPane.showInputDialog(this, "Clothing Allowance:");
    if (clothingAllowance == null) return;

    String grossSemi = JOptionPane.showInputDialog(this, "Gross Semi-monthly Rate:");
    if (grossSemi == null) return;

    String hourlyRateInput = JOptionPane.showInputDialog(this, "Hourly Rate (leave blank if Basic Salary was entered):");
    if (hourlyRateInput == null) return;

    double basicSalaryValue;
    double hourlyRateValue;

    try {
        boolean hasBasic = !basicSalaryInput.trim().isEmpty();
        boolean hasHourly = !hourlyRateInput.trim().isEmpty();

        if (!hasBasic && !hasHourly) {
            JOptionPane.showMessageDialog(this, "Enter either Basic Salary or Hourly Rate.");
            return;
        }

        if (hasBasic) {
            basicSalaryValue = parseRequiredAmount("Basic Salary", basicSalaryInput);
            hourlyRateValue = computeHourlyRateFromBasicSalary(basicSalaryValue);
        } else {
            hourlyRateValue = parseRequiredAmount("Hourly Rate", hourlyRateInput);
            basicSalaryValue = computeBasicSalaryFromHourlyRate(hourlyRateValue);
        }
    } catch (IllegalArgumentException e) {
        return;
    }

    String[] row = new String[]{
        normalizedEmployeeId,
        lastName.trim(),
        firstName.trim(),
        birthday.trim(),
        address.trim(),
        phone.trim(),
        sss.trim(),
        philhealth.trim(),
        tin.trim(),
        pagibig.trim(),
        status.trim(),
        position.trim(),
        supervisor.trim(),
        String.format(java.util.Locale.US, "%.2f", basicSalaryValue),
        riceSubsidy.trim(),
        phoneAllowance.trim(),
        clothingAllowance.trim(),
        grossSemi.trim(),
        String.format(java.util.Locale.US, "%.2f", hourlyRateValue)
    };

    employeeRows.add(row);
    saveEmployeeRowsToCsv();
    loadEmployeeTable();
    statusMessageLabel.setText("Employee added.");
    }//GEN-LAST:event_addEmployeeButtonActionPerformed

    private void computePayrollButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computePayrollButtonActionPerformed
    String empId = employeeIDTextField.getText().trim();
    String manualHoursText = manualHoursTextField.getText().trim();
    String coverage = payCoverageTextField.getText().trim();

    if (empId.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Employee ID is required.");
        return;
    }

    if (coverage.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Pay coverage is required.");
        return;
    }

    Employee currentEmployee = findEmployeeFromLoadedTable(empId);

    if (currentEmployee == null) {
        JOptionPane.showMessageDialog(this, "Employee ID not found.");
        statusMessageLabel.setText("Employee ID not found.");
        return;
    }

    double hoursWorked;

    if (!manualHoursText.isEmpty()) {
        try {
            hoursWorked = Double.parseDouble(manualHoursText);
            if (hoursWorked < 0) {
                JOptionPane.showMessageDialog(this, "Manual hours cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Manual hours must be a valid number.");
            return;
        }
    } else {
        LocalDate[] period = parseCoverageDates(coverage);

        if (period == null) {
            JOptionPane.showMessageDialog(this, "Invalid pay coverage format. Use example: June 1-15, 2024");
            statusMessageLabel.setText("Invalid pay coverage format.");
            return;
        }

        hoursWorked = attendanceManager.computeHoursWorked(empId, period[0], period[1]);
    }

    double grossPay = payrollCalculator.computeGrossPay(hoursWorked, currentEmployee.getHourlyRate());
    double deductions = payrollCalculator.computeDeductions(grossPay);
    double withholdingTax = payrollCalculator.computeWithholdingTax(grossPay);
    double netPay = payrollCalculator.computeNetPay(grossPay, deductions);

    employeeNameTextField.setText(currentEmployee.getFullName());
    statusMessageLabel.setText("Payroll computed successfully.");

    PayrollRecord pr = new PayrollRecord(
            currentEmployee.getEmployeeId(),
            currentEmployee.getFullName(),
            coverage,
            hoursWorked,
            grossPay,
            deductions,
            netPay
    );

    payrollRecordManager.savePayrollRecord(pr);
    loadPayrollRecordTable();

    String report = String.format(
            java.util.Locale.US,
            """
            === PAYROLL COMPUTATION ===
            Employee ID: %s
            Name: %s
            Coverage: %s
            Hours Worked: %.2f

            Gross Pay: Php %,.2f
            Deductions: Php %,.2f
            Withholding Tax: Php %,.2f
            Net Pay: Php %,.2f
            """,
            currentEmployee.getEmployeeId(),
            currentEmployee.getFullName(),
            coverage,
            hoursWorked,
            grossPay,
            deductions,
            withholdingTax,
            netPay
    );

    JOptionPane.showMessageDialog(this, report, "Computation Complete", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_computePayrollButtonActionPerformed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new PayrollPortalMain().setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEmployeeButton;
    private javax.swing.JButton clearPayrollRecordsButton;
    private javax.swing.JButton computePayrollButton;
    private javax.swing.JButton deleteEmployeeButton;
    private javax.swing.JButton deletePayrollRecordButton;
    private javax.swing.JButton editEmployeeButton;
    private javax.swing.JPanel employeeDatabasePanel;
    private javax.swing.JPanel employeeFormPanel;
    private javax.swing.JTextField employeeIDTextField;
    private javax.swing.JTextField employeeNameTextField;
    private javax.swing.JTable employeeTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JTextField manualHoursTextField;
    private javax.swing.JTextField payCoverageTextField;
    private javax.swing.JLabel payrollRecordCountLabel;
    private javax.swing.JTable payrollRecordTable;
    private javax.swing.JScrollPane payrollRecordsScrollPane;
    private javax.swing.JButton resetFormButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JComboBox<String> statusFilterComboBox;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel submittedRecordsPanel;
    // End of variables declaration//GEN-END:variables

private void saveEmployeeRowsToCsv() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(employeeFilePath))) {
        writer.write("Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate");
        writer.newLine();

        for (String[] row : employeeRows) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 19; i++) {
                String value = safeValue(row, i);
                if (value.contains(",")) {
                    value = "\"" + value + "\"";
                }
                sb.append(value);
                if (i < 18) {
                    sb.append(",");
                }
            }

            writer.write(sb.toString());
            writer.newLine();
        }
    } catch (Exception e) {
        logger.log(java.util.logging.Level.SEVERE, "Error saving Employee Database.", e);
        JOptionPane.showMessageDialog(this, "Error saving Employee Database.");
    }
}
}