package com.motorph.ui;
/**
 *
 * @author Timothy Justin Gacula
 */
import com.motorph.manager.AttendanceManager;
import com.motorph.manager.EmployeeFormService;
import com.motorph.manager.EmployeeManager;
import com.motorph.manager.PayrollCalculator;
import com.motorph.manager.PayrollComputationResult;
import com.motorph.manager.PayrollRecordManager;
import com.motorph.manager.PayrollService;
import com.motorph.model.Employee;
import com.motorph.model.PayrollRecord;
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PayrollPortalMain extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(PayrollPortalMain.class.getName());

    private final AttendanceManager attendanceManager = new AttendanceManager();
    private final PayrollCalculator payrollCalculator = new PayrollCalculator();
    private final PayrollRecordManager payrollRecordManager = new PayrollRecordManager();
    private final EmployeeFormService employeeFormService = new EmployeeFormService();
    private final EmployeeManager employeeManager = new EmployeeManager();
    private final PayrollService payrollService =
            new PayrollService(attendanceManager, employeeManager, payrollCalculator, payrollRecordManager);


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
    PayrollUIHelper.applyTheme(this);

    logoLabel = new javax.swing.JLabel();
    java.awt.Image logoImg = drawLogo(40);
    logoLabel.setIcon(new javax.swing.ImageIcon(logoImg));
    getContentPane().add(logoLabel);

    PayrollUIHelper.applyModernLayout(this);
    pack();
    setLocationRelativeTo(null);

    try {
        employeeManager.loadEmployees();
        refreshEmployeeTable();
        statusMessageLabel.setText("Employee database loaded.");
    } catch (Exception e) {
        logger.log(java.util.logging.Level.SEVERE, "Error loading Employee Database.", e);
        JOptionPane.showMessageDialog(this, "Error loading Employee Database.");
        statusMessageLabel.setText("Failed to load employee database.");
    }

    loadPayrollRecordTable();
    }

    private java.awt.Image drawLogo(int targetHeight) {
        int logical = 200;
        double scale = targetHeight / (double) logical;
        int px = targetHeight;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(px, px, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.scale(scale, scale);

        g.setColor(new java.awt.Color(0xFD, 0xFB, 0xF7));
        g.fillOval(5, 5, 190, 190);
        g.setColor(new java.awt.Color(0x4A, 0x3B, 0x2C));
        g.setStroke(new java.awt.BasicStroke(6));
        g.drawOval(5, 5, 190, 190);

        g.setColor(new java.awt.Color(0xF4, 0xEB, 0xD8));
        g.fillOval(15, 15, 170, 170);
        g.setColor(new java.awt.Color(0xA6, 0x8A, 0x64));
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawOval(15, 15, 170, 170);

        g.setColor(new java.awt.Color(0xFD, 0xFB, 0xF7));
        g.fillOval(30, 30, 140, 140);
        g.setColor(new java.awt.Color(0x4A, 0x3B, 0x2C));
        java.awt.Stroke dashed = new java.awt.BasicStroke(2, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 1, new float[]{4, 3}, 0);
        g.setStroke(dashed);
        g.drawOval(30, 30, 140, 140);
        g.setStroke(new java.awt.BasicStroke(1));

        g.setColor(new java.awt.Color(0x8B, 0x73, 0x55));
        g.setFont(new java.awt.Font("Georgia", java.awt.Font.BOLD, 12));
        java.awt.FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth("EST. 2020");
        int centerY = 76;
        int baseline = centerY + fm.getAscent() / 2;
        g.drawString("EST. 2020", logical / 2 - w / 2, baseline);

        g.setColor(new java.awt.Color(0x4A, 0x3B, 0x2C));
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        fm = g.getFontMetrics();
        w = fm.stringWidth("MotorPH");
        centerY = 108;
        baseline = centerY + fm.getAscent() / 2;
        g.drawString("MotorPH", logical / 2 - w / 2, baseline);

        g.setColor(new java.awt.Color(0x8B, 0x73, 0x55));
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 7));
        fm = g.getFontMetrics();
        w = fm.stringWidth("THE FIRST CHOICE FOR FILIPINOS");
        centerY = 136;
        baseline = centerY + fm.getAscent() / 2;
        g.drawString("THE FIRST CHOICE FOR FILIPINOS", logical / 2 - w / 2, baseline);

        g.setColor(new java.awt.Color(0xA6, 0x8A, 0x64));
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 6));
        fm = g.getFontMetrics();
        w = fm.stringWidth("COMPETITIVE & AFFORDABLE");
        centerY = 152;
        baseline = centerY + fm.getAscent() / 2;
        g.drawString("COMPETITIVE & AFFORDABLE", logical / 2 - w / 2, baseline);

        g.setColor(new java.awt.Color(0xA6, 0x8A, 0x64));
        g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_MITER));
        int[][] pts = {{100, 15, 100, 22}, {90, 18, 94, 24}, {110, 18, 106, 24}, {83, 26, 89, 29}, {117, 26, 111, 29}};
        for (int[] p : pts) {
            g.drawLine(p[0], p[1], p[2], p[3]);
        }

        g.dispose();
        return img;
    }

private void wireEvents() {
    deleteEmployeeButton.addActionListener(this::deleteEmployeeButtonActionPerformed);
    deletePayrollRecordButton.addActionListener(this::deletePayrollRecordButtonActionPerformed);

    statusFilterComboBox.addActionListener(e -> refreshEmployeeTable());

    searchTextField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            refreshEmployeeTable();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            refreshEmployeeTable();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            refreshEmployeeTable();
        }
    });
}

    private void initializeClipboardSupport() {
        ClipboardHelper.installTextClipboardShortcuts(employeeIDTextField);
        ClipboardHelper.installTextClipboardShortcuts(employeeNameTextField);
        ClipboardHelper.installTextClipboardShortcuts(payCoverageTextField);
        ClipboardHelper.installTextClipboardShortcuts(manualHoursTextField);
        ClipboardHelper.installTextClipboardShortcuts(searchTextField);

        ClipboardHelper.installTableCopyShortcut(employeeTable);
        ClipboardHelper.installTableCopyShortcut(payrollRecordTable);
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

private void refreshEmployeeTable() {
    String search = searchTextField.getText().trim().toLowerCase();
    String selectedStatus = String.valueOf(statusFilterComboBox.getSelectedItem());
    List<Employee> employees = employeeManager.getAllEmployees();

    TableRenderHelper.refreshEmployeeTable(employeeTable, employees, search, selectedStatus);
}

private void loadPayrollRecordTable() {
    try {
        List<PayrollRecord> records = payrollRecordManager.loadPayrollRecords();
        TableRenderHelper.loadPayrollRecordTable(payrollRecordTable, records, payrollRecordCountLabel);
    } catch (IOException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error loading payroll records.", e);
        payrollRecordCountLabel.setText("Records: 0");
        statusMessageLabel.setText("Failed to load payroll records.");
        JOptionPane.showMessageDialog(this, "Error loading payroll records.");
    }
}

    private void populateEmployeeName() {
        String empId = employeeIDTextField.getText().trim();

        if (empId.isEmpty()) {
            employeeNameTextField.setText("");
            statusMessageLabel.setText("Enter an employee ID.");
            return;
        }

        Employee emp = employeeManager.findEmployeeById(empId);
        if (emp != null) {
            employeeNameTextField.setText(emp.getFullName());
            statusMessageLabel.setText("Employee found.");
        } else {
            employeeNameTextField.setText("");
            statusMessageLabel.setText("Employee ID not found.");
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

    try {
        employeeManager.deleteEmployee(empId);
        refreshEmployeeTable();
        statusMessageLabel.setText("Employee deleted successfully.");
        JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
    } catch (HeadlessException | IOException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error deleting employee.", e);
        JOptionPane.showMessageDialog(this, "Error deleting employee.");
        statusMessageLabel.setText("Delete failed.");
        }
    }

private void deletePayrollRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {
    int[] selectedRows = payrollRecordTable.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(this, "Select at least one record from the table first.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete " + selectedRows.length + " selected payroll record(s)?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        payrollRecordManager.deletePayrollRecords(selectedRows);
        loadPayrollRecordTable();
        statusMessageLabel.setText(selectedRows.length + " payroll record(s) deleted.");
    } catch (IOException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error deleting payroll records.", e);
        statusMessageLabel.setText("Failed to delete payroll records.");
        JOptionPane.showMessageDialog(this, "Error deleting payroll records.");    }
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
        payrollRecordTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        payrollRecordTable.setRowSelectionAllowed(true);
        payrollRecordTable.setColumnSelectionAllowed(false);

        employeeTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Employee ID",
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

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        payrollRecordManager.deleteAllPayrollRecords();
        loadPayrollRecordTable();
        statusMessageLabel.setText("All payroll records deleted.");
    } catch (IOException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error clearing payroll records.", e);
        statusMessageLabel.setText("Failed to clear payroll records.");
        JOptionPane.showMessageDialog(this, "Error clearing payroll records.");
    }    }//GEN-LAST:event_clearPayrollRecordsButtonActionPerformed

    private void editEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEmployeeButtonActionPerformed
    int selectedRow = employeeTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select an employee from the table first.");
        return;
    }

    String selectedId = employeeTable.getValueAt(selectedRow, 0).toString();
    Employee existing = employeeManager.findEmployeeById(selectedId);

    try {
        Employee updatedEmployee = employeeFormService.promptEditedEmployee(this, existing);
        if (updatedEmployee == null) {
            return;
        }

        employeeManager.saveOrUpdateEmployee(updatedEmployee);
        refreshEmployeeTable();
        statusMessageLabel.setText("Employee updated.");
    } catch (HeadlessException | IOException | IllegalArgumentException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error updating employee.", e);
        JOptionPane.showMessageDialog(this, "Error updating employee.");
    }
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

        Employee emp = employeeManager.findEmployeeById(empId);
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
    try {
        Employee employee = employeeFormService.promptNewEmployee(this, employeeManager);
        if (employee == null) {
            return;
        }

        employeeManager.addEmployee(employee);
        refreshEmployeeTable();
        statusMessageLabel.setText("Employee added.");
    } catch (HeadlessException | IOException | IllegalArgumentException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error adding employee.", e);
        JOptionPane.showMessageDialog(this, "Error adding employee."); }
    }//GEN-LAST:event_addEmployeeButtonActionPerformed

    private void computePayrollButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computePayrollButtonActionPerformed
    try {
        PayrollComputationResult result = payrollService.computePayroll(
                employeeIDTextField.getText(),
                payCoverageTextField.getText(),
                manualHoursTextField.getText()
        );

        employeeNameTextField.setText(result.getEmployee().getFullName());
        statusMessageLabel.setText("Payroll computed successfully.");
        loadPayrollRecordTable();

        JOptionPane.showMessageDialog(
                this,
                result.toDisplayReport(),
                "Computation Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    } catch (IllegalArgumentException e) {
        statusMessageLabel.setText(e.getMessage());
        JOptionPane.showMessageDialog(this, e.getMessage());
    } catch (HeadlessException | IOException e) {
        logger.log(java.util.logging.Level.SEVERE, "Error computing payroll.", e);
        statusMessageLabel.setText("Payroll computation failed.");
        JOptionPane.showMessageDialog(this, "Error computing payroll.");
        }
    }//GEN-LAST:event_computePayrollButtonActionPerformed

    /**
     * @param args the command line arguments
     */
public static void main(String args[]) {
    // macOS-friendly setup: native top menu bar, app name, and system light/dark appearance
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.application.name", "MotorPH Payroll");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MotorPH Payroll");
    System.setProperty("apple.awt.application.appearance", "system");

    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
        logger.log(java.util.logging.Level.SEVERE, "Could not set system LookAndFeel.", ex);
    }

    java.awt.EventQueue.invokeLater(() -> new PayrollPortalMain().setVisible(true));
}   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton addEmployeeButton;
    javax.swing.JButton clearPayrollRecordsButton;
    javax.swing.JButton computePayrollButton;
    javax.swing.JButton deleteEmployeeButton;
    javax.swing.JButton deletePayrollRecordButton;
    javax.swing.JButton editEmployeeButton;
    javax.swing.JPanel employeeDatabasePanel;
    javax.swing.JPanel employeeFormPanel;
    javax.swing.JTextField employeeIDTextField;
    javax.swing.JTextField employeeNameTextField;
    javax.swing.JTable employeeTable;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JLabel jLabel6;
    javax.swing.JLabel jLabel7;
    javax.swing.JLabel jLabel8;
    javax.swing.JLabel jLabel9;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JTabbedPane mainTabbedPane;
    javax.swing.JTextField manualHoursTextField;
    javax.swing.JTextField payCoverageTextField;
    javax.swing.JLabel payrollRecordCountLabel;
    javax.swing.JTable payrollRecordTable;
    javax.swing.JScrollPane payrollRecordsScrollPane;
    javax.swing.JButton resetFormButton;
    javax.swing.JTextField searchTextField;
    javax.swing.JComboBox<String> statusFilterComboBox;
    javax.swing.JLabel statusMessageLabel;
    javax.swing.JPanel submittedRecordsPanel;
    javax.swing.JLabel logoLabel;
    // End of variables declaration//GEN-END:variables
}