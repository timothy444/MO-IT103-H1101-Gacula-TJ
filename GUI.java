package com.motorph.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// ==========================================
// 1. DATA MODEL
// ==========================================
class EmployeeRecord {
    String[] rawData;

    String empNo;
    String name;
    String position;
    String status;
    double basicSalary;
    double rate;
    String phone;
    String supervisor;

    EmployeeRecord(String[] rawData) {
        this.rawData = rawData;
        this.empNo = rawData[0].trim();
        this.name = rawData[2].trim() + " " + rawData[1].trim(); 
        this.phone = rawData[5].trim();
        this.status = rawData[10].trim();
        this.position = rawData[11].trim();
        this.supervisor = rawData[12].trim();
        this.basicSalary = Double.parseDouble(rawData[13].replace("\"", "").replace(",", "").trim());
        this.rate = Double.parseDouble(rawData[18].replace("\"", "").replace(",", "").trim());
    }

    void updateData(String fName, String lName, String status, String position, double basicSalary, double rate) {
        this.name = fName.trim() + " " + lName.trim();
        this.status = status;
        this.position = position;
        this.basicSalary = basicSalary;
        this.rate = rate;
        
        this.rawData[1] = lName.trim();
        this.rawData[2] = fName.trim();
        this.rawData[10] = status;
        this.rawData[11] = position;
        this.rawData[13] = String.format("%.2f", basicSalary);
        this.rawData[18] = String.format("%.2f", rate);
    }

    String toCSV() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawData.length; i++) {
            sb.append(GUI.escapeCSV(rawData[i] == null ? "" : rawData[i]));
            if (i < rawData.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}

// ==========================================
// 2. FILE HANDLING MANAGER
// ==========================================
class EmployeeDataManager {
    private final File file = new File("EmployeeDatabase.csv");
    private final LinkedHashMap<String, EmployeeRecord> records = new LinkedHashMap<>();
    private String headerLine = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";

    public void load() throws IOException {
        records.clear();
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { headerLine = line; firstLine = false; continue; }
                String[] p = parseCsvLine(line);
                if (p.length < 19) continue;
                try {
                    EmployeeRecord r = new EmployeeRecord(p);
                    records.put(r.empNo, r);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // Upgraded to perfectly decode "" back to " if they exist inside fields
    public static String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; // skip the escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString().trim());
        return out.toArray(new String[0]);
    }

    public ArrayList<EmployeeRecord> list() { return new ArrayList<>(records.values()); }
    public EmployeeRecord find(String empNo) { return records.get(empNo); }
    
    public void addOrUpdate(EmployeeRecord rec) throws IOException {
        records.put(rec.empNo, rec);
        save();
    }

    public void delete(String empNo) throws IOException {
        records.remove(empNo);
        save();
    }

    public void save() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(headerLine); bw.newLine();
            for (EmployeeRecord r : records.values()) {
                bw.write(r.toCSV()); bw.newLine();
            }
        }
    }
}

// ==========================================
// 3. MAIN GUI APPLICATION
// ==========================================
public class GUI extends JFrame {
    private static final int TABLE_ROW_HEIGHT = 32;
    private static final String PLACEHOLDER_EMP_NO = "e.g. 10001";
    private static final String PLACEHOLDER_EMP_NAME = "e.g. Manuel III Garcia";
    private static final String PLACEHOLDER_PAY_COVERAGE = "e.g. June 1-15, 2026";
    private static final String PLACEHOLDER_SEARCH = "Search by name, employee ID, or position";
    private static final String PATTERN_PAY_COVERAGE = "^[A-Za-z]+ \\d{1,2}-\\d{1,2}, \\d{4}$";

    private final EmployeeDataManager dataManager = new EmployeeDataManager();
    private String[][] EMPLOYEE_DATA;
    private int[] submissionCount = {0};

    private JTextField txtEmpNo, txtEmpName, txtPayCoverage, txtSearch;
    private JLabel empNoHelp, empNameHelp, payHelp;
    private JTable srTable, empTable;
    private DefaultTableModel srTableModel, empTableModel;
    private JLabel lblStatus, lblRecordCount, lblDatabase;
    private JButton btnCompute, btnReset, btnClearAll;
    private JComboBox<String> statusFilter;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private Border normalBorder, glowBorder;

    public GUI() {
        super("MotorPH Payroll Portal");
        setSize(950, 600); 
        setMinimumSize(new Dimension(850, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try { dataManager.load(); } 
        catch (IOException e) { JOptionPane.showMessageDialog(this, "Error loading EmployeeDatabase.csv", "File Error", JOptionPane.ERROR_MESSAGE); }

        refreshEmployeeTableData();
        buildUI();
        loadPayrollRecords();
    }
    
    private void refreshEmployeeTableData() {
        ArrayList<EmployeeRecord> recs = dataManager.list();
        EMPLOYEE_DATA = new String[recs.size()][8];
        for(int i = 0; i < recs.size(); i++) {
            EmployeeRecord r = recs.get(i);
            EMPLOYEE_DATA[i][0] = r.empNo;
            EMPLOYEE_DATA[i][1] = r.name;
            EMPLOYEE_DATA[i][2] = r.position;
            EMPLOYEE_DATA[i][3] = r.status;
            EMPLOYEE_DATA[i][4] = String.format("%,.2f", r.basicSalary);
            EMPLOYEE_DATA[i][5] = String.format("%,.2f", r.rate);
            EMPLOYEE_DATA[i][6] = r.phone;
            EMPLOYEE_DATA[i][7] = r.supervisor;
        }
    }

    // ==========================================
    // 4. PAYROLL COMPUTATION & PERSISTENCE
    // ==========================================
    public static double computeSSS() { return 1125.0 / 2; }
    public static double computePhilHealth() { return 375.0 / 2; }
    public static double computePagIbig() { return 100.0 / 2; }
    public static double computeWithholdingTax(double monthlySalary) {
        double taxableIncome = monthlySalary - 1600.0;
        if (taxableIncome > 20833.0) return ((taxableIncome - 20833.0) * 0.20) / 2;
        return 0.0; 
    }

    public static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void savePayrollRecord(String empNo, String name, String coverage, double gross, double deductions, double net) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("PayrollRecords.csv", true))) {
            bw.write(String.format("%s,%s,%s,%.2f,%.2f,%.2f%n", 
                escapeCSV(empNo), escapeCSV(name), escapeCSV(coverage), gross, deductions, net));
        } catch (IOException ex) {
            System.err.println("Failed to save payroll record.");
        }
    }
    
    private void loadPayrollRecords() {
        File prFile = new File("PayrollRecords.csv");
        if (!prFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(prFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = EmployeeDataManager.parseCsvLine(line); 
                if (p.length >= 6) {
                    submissionCount[0]++;
                    double gross = Double.parseDouble(p[3]);
                    double deduct = Double.parseDouble(p[4]);
                    double net = Double.parseDouble(p[5]);
                    srTableModel.addRow(new Object[]{ submissionCount[0], p[0], p[1], p[2], String.format("₱%,.2f", gross), String.format("₱%,.2f", deduct), String.format("₱%,.2f", net) });
                }
            }
            if(submissionCount[0] > 0) {
                lblRecordCount.setText(srTableModel.getRowCount() + " payroll record(s) saved");
                btnClearAll.setEnabled(true);
            }
        } catch (Exception e) { System.err.println("Error reading PayrollRecords.csv"); }
    }

    // ==========================================
    // 5. HELPER METHODS & UI CONSTRUCTION
    // ==========================================
    private static DocumentListener buildDocumentListener(Runnable onChange) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        };
    }

    private static boolean isValidDayRange(String coverage) {
        try {
            String[] spaceParts = coverage.trim().split(" ");
            if (spaceParts.length < 2) return false;
            String dayRangePart = spaceParts[1].replace(",", "");
            String[] days = dayRangePart.split("-");
            if (days.length != 2) return false;
            int startDay = Integer.parseInt(days[0].trim());
            int endDay = Integer.parseInt(days[1].trim());
            return startDay >= 1 && endDay <= 31 && startDay < endDay;
        } catch (NumberFormatException ex) { return false; }
    }

    private void buildUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel employeeFormTab = new JPanel();
        JPanel submittedRecordsTab = new JPanel();
        JPanel employeeDatabaseTab = new JPanel();

        tabbedPane.addTab("Employee Form", employeeFormTab);
        tabbedPane.addTab("Submitted Records", submittedRecordsTab);
        tabbedPane.addTab("Employee Database", employeeDatabaseTab);
        add(tabbedPane, BorderLayout.CENTER);

        normalBorder = new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(2, 5, 2, 5));
        glowBorder = new CompoundBorder(new LineBorder(new Color(220, 50, 50, 30), 3), new CompoundBorder(new LineBorder(new Color(220, 50, 50, 80), 2), new CompoundBorder(new LineBorder(new Color(220, 50, 50), 1), new EmptyBorder(2, 5, 2, 5))));

        // --- TAB 1: EMPLOYEE FORM ---
        employeeFormTab.setLayout(new BorderLayout()); employeeFormTab.setBackground(new Color(245, 245, 245)); employeeFormTab.setBorder(new EmptyBorder(15, 35, 0, 35));
        JPanel formTopPanel = new JPanel(new BorderLayout()); formTopPanel.setOpaque(false);
        JLabel formTitle = new JLabel("Employee Payroll Form"); formTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        formTopPanel.add(formTitle, BorderLayout.WEST);

        JPanel formContainer = new JPanel(); formContainer.setOpaque(false); formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        txtEmpNo = new JTextField(PLACEHOLDER_EMP_NO); txtEmpNo.setPreferredSize(new Dimension(800, 40)); txtEmpNo.setMaximumSize(new Dimension(850, 40)); txtEmpNo.setForeground(Color.GRAY); txtEmpNo.setBorder(normalBorder);
        empNoHelp = new JLabel("Enter the employee's 5-digit ID number"); empNoHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); empNoHelp.setForeground(Color.GRAY);
        
        txtEmpName = new JTextField(PLACEHOLDER_EMP_NAME); txtEmpName.setPreferredSize(new Dimension(800, 40)); txtEmpName.setMaximumSize(new Dimension(850, 40)); txtEmpName.setForeground(Color.GRAY); txtEmpName.setBorder(normalBorder);
        empNameHelp = new JLabel("Enter the employee's full name as listed for the employee ID."); empNameHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); empNameHelp.setForeground(Color.GRAY);
        
        txtPayCoverage = new JTextField(PLACEHOLDER_PAY_COVERAGE); txtPayCoverage.setPreferredSize(new Dimension(800, 40)); txtPayCoverage.setMaximumSize(new Dimension(850, 40)); txtPayCoverage.setForeground(Color.GRAY); txtPayCoverage.setBorder(normalBorder);
        payHelp = new JLabel("Enter the payroll period: Month D-D, YYYY e.g., June 1-15, 2026"); payHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); payHelp.setForeground(Color.GRAY);

        formContainer.add(Box.createVerticalStrut(17)); formContainer.add(new JLabel("Employee Number *")); formContainer.add(txtEmpNo); formContainer.add(empNoHelp);
        formContainer.add(Box.createVerticalStrut(10)); formContainer.add(new JLabel("Employee Name *")); formContainer.add(txtEmpName); formContainer.add(empNameHelp);
        formContainer.add(Box.createVerticalStrut(10)); formContainer.add(new JLabel("Pay Coverage *")); formContainer.add(txtPayCoverage); formContainer.add(payHelp);
        formContainer.add(Box.createVerticalStrut(25));

        JPanel actionPanel = new JPanel(new BorderLayout()); actionPanel.setOpaque(false);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); buttonPanel.setOpaque(false);
        btnReset = new JButton("Reset Form"); btnCompute = new JButton("Compute Pay");
        btnReset.setEnabled(false); btnCompute.setEnabled(false); btnCompute.setBackground(new Color(230, 230, 230));
        buttonPanel.add(btnReset); buttonPanel.add(btnCompute);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        formContainer.add(actionPanel); formContainer.add(Box.createVerticalStrut(15)); formContainer.add(new JSeparator());

        JPanel statusBar = new JPanel(new BorderLayout()); statusBar.setOpaque(false);
        lblStatus = new JLabel("Ready — Fill in the form and click Compute Pay to begin");
        lblDatabase = new JLabel(dataManager.list().size() + " employees in database", SwingConstants.RIGHT);
        statusBar.add(lblStatus, BorderLayout.WEST); statusBar.add(lblDatabase, BorderLayout.EAST);
        formContainer.add(statusBar);

        DocumentListener fieldValidator = buildDocumentListener(() -> {
            String no = txtEmpNo.getText().trim(), name = txtEmpName.getText().trim(), cov = txtPayCoverage.getText().trim();
            EmployeeRecord rec = dataManager.find(no);
            
            boolean validEmpNo = rec != null && no.matches("^\\d{5}$");
            if(!no.equals(PLACEHOLDER_EMP_NO) && !no.isEmpty()) {
                if(!validEmpNo) { txtEmpNo.setBorder(glowBorder); empNoHelp.setText("Requires a valid 5-digit Employee ID."); empNoHelp.setForeground(Color.RED); }
                else { txtEmpNo.setBorder(normalBorder); empNoHelp.setText("Employee found."); empNoHelp.setForeground(new Color(0,130,0)); }
            }
            
            boolean validName = validEmpNo && rec.name.equalsIgnoreCase(name);
            if(!name.equals(PLACEHOLDER_EMP_NAME) && !name.isEmpty()) {
                if(validEmpNo && !validName) { txtEmpName.setBorder(glowBorder); empNameHelp.setText("Name does not match ID."); empNameHelp.setForeground(Color.RED); }
                else if (validName) { txtEmpName.setBorder(normalBorder); empNameHelp.setText("Valid name."); empNameHelp.setForeground(new Color(0,130,0)); }
            }
            
            boolean validCov = cov.matches(PATTERN_PAY_COVERAGE) && isValidDayRange(cov);
            if(!cov.equals(PLACEHOLDER_PAY_COVERAGE) && !cov.isEmpty()) {
                if(!validCov) { txtPayCoverage.setBorder(glowBorder); payHelp.setText("Invalid format or dates. Use: Month D-D, YYYY"); payHelp.setForeground(Color.RED); }
                else { txtPayCoverage.setBorder(normalBorder); payHelp.setText("Valid coverage format."); payHelp.setForeground(new Color(0,130,0)); }
            }

            boolean valid = validEmpNo && validName && validCov;
            btnCompute.setEnabled(valid);
            btnCompute.setBackground(valid ? new Color(0, 120, 215) : new Color(230, 230, 230));
            btnCompute.setForeground(valid ? Color.WHITE : Color.BLACK);
            btnReset.setEnabled(!no.equals(PLACEHOLDER_EMP_NO) || !name.equals(PLACEHOLDER_EMP_NAME) || !cov.equals(PLACEHOLDER_PAY_COVERAGE));
        });
        
        txtEmpNo.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { if (txtEmpNo.getText().equals(PLACEHOLDER_EMP_NO)) { txtEmpNo.setText(""); txtEmpNo.setForeground(Color.BLACK); } } public void focusLost(FocusEvent e) { if (txtEmpNo.getText().isEmpty()) { txtEmpNo.setText(PLACEHOLDER_EMP_NO); txtEmpNo.setForeground(Color.GRAY); txtEmpNo.setBorder(normalBorder); empNoHelp.setText("Enter the employee's 5-digit ID number"); empNoHelp.setForeground(Color.GRAY); } } });
        txtEmpName.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { if (txtEmpName.getText().equals(PLACEHOLDER_EMP_NAME)) { txtEmpName.setText(""); txtEmpName.setForeground(Color.BLACK); } } public void focusLost(FocusEvent e) { if (txtEmpName.getText().isEmpty()) { txtEmpName.setText(PLACEHOLDER_EMP_NAME); txtEmpName.setForeground(Color.GRAY); txtEmpName.setBorder(normalBorder); empNameHelp.setText("Enter the employee's full name as listed for the employee ID."); empNameHelp.setForeground(Color.GRAY); } } });
        txtPayCoverage.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { if (txtPayCoverage.getText().equals(PLACEHOLDER_PAY_COVERAGE)) { txtPayCoverage.setText(""); txtPayCoverage.setForeground(Color.BLACK); } } public void focusLost(FocusEvent e) { if (txtPayCoverage.getText().isEmpty()) { txtPayCoverage.setText(PLACEHOLDER_PAY_COVERAGE); txtPayCoverage.setForeground(Color.GRAY); txtPayCoverage.setBorder(normalBorder); payHelp.setText("Enter the payroll period: Month D-D, YYYY"); payHelp.setForeground(Color.GRAY); } } });

        txtEmpNo.getDocument().addDocumentListener(fieldValidator); txtEmpName.getDocument().addDocumentListener(fieldValidator); txtPayCoverage.getDocument().addDocumentListener(fieldValidator);

        btnReset.addActionListener(e -> {
            txtEmpNo.setText(PLACEHOLDER_EMP_NO); txtEmpNo.setForeground(Color.GRAY); txtEmpNo.setBorder(normalBorder); empNoHelp.setText("Enter the employee's 5-digit ID number"); empNoHelp.setForeground(Color.GRAY);
            txtEmpName.setText(PLACEHOLDER_EMP_NAME); txtEmpName.setForeground(Color.GRAY); txtEmpName.setBorder(normalBorder); empNameHelp.setText("Enter the employee's full name as listed for the employee ID."); empNameHelp.setForeground(Color.GRAY);
            txtPayCoverage.setText(PLACEHOLDER_PAY_COVERAGE); txtPayCoverage.setForeground(Color.GRAY); txtPayCoverage.setBorder(normalBorder); payHelp.setText("Enter the payroll period: Month D-D, YYYY"); payHelp.setForeground(Color.GRAY);
            btnCompute.setEnabled(false); btnReset.setEnabled(false);
            lblStatus.setText("Ready — Fill in the form and click Compute Pay to begin");
        });

        employeeFormTab.add(formTopPanel, BorderLayout.NORTH); employeeFormTab.add(formContainer, BorderLayout.CENTER);

        // --- TAB 2: SUBMITTED RECORDS ---
        submittedRecordsTab.setLayout(new BorderLayout()); submittedRecordsTab.setBackground(new Color(245, 245, 245)); submittedRecordsTab.setBorder(new EmptyBorder(15, 20, 20, 20));
        String[] srColumns = { "#", "EMP #", "NAME", "COVERAGE", "GROSS PAY", "DEDUCT.", "NET PAY" };
        srTableModel = new DefaultTableModel(srColumns, 0) { @Override public boolean isCellEditable(int row, int col) { return false; } };
        srTable = new JTable(srTableModel); srTable.setRowHeight(32); srTable.getTableHeader().setBackground(new Color(40, 40, 40)); srTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane srScroll = new JScrollPane(srTable);
        JPanel srHeaderPanel = new JPanel(new BorderLayout()); srHeaderPanel.setOpaque(false); srHeaderPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        lblRecordCount = new JLabel("0 payroll records saved"); btnClearAll = new JButton("Clear All Records"); btnClearAll.setEnabled(false);
        srHeaderPanel.add(lblRecordCount, BorderLayout.WEST); srHeaderPanel.add(btnClearAll, BorderLayout.EAST);
        submittedRecordsTab.add(srHeaderPanel, BorderLayout.NORTH); submittedRecordsTab.add(srScroll, BorderLayout.CENTER);

        btnCompute.addActionListener(e -> {
            String empNo = txtEmpNo.getText().trim(), empName = txtEmpName.getText().trim(), coverage = txtPayCoverage.getText().trim();
            EmployeeRecord emp = dataManager.find(empNo);
            
            double monthlySalary = emp.basicSalary, semiMonthlyGross = monthlySalary / 2;
            double sss = computeSSS(), philHealth = computePhilHealth(), pagIbig = computePagIbig(), tax = computeWithholdingTax(monthlySalary);
            double totalDeductions = sss + philHealth + pagIbig + tax;
            double netPay = semiMonthlyGross - totalDeductions;

            submissionCount[0]++;
            lblStatus.setText(submissionCount[0] + " payroll record(s) computed.");
            
            srTableModel.addRow(new Object[]{ submissionCount[0], empNo, empName, coverage, String.format("₱%,.2f", semiMonthlyGross), String.format("₱%,.2f", totalDeductions), String.format("₱%,.2f", netPay) });
            savePayrollRecord(empNo, empName, coverage, semiMonthlyGross, totalDeductions, netPay);
            lblRecordCount.setText(srTableModel.getRowCount() + " payroll record(s) saved");
            btnClearAll.setEnabled(true);
            
            JOptionPane.showMessageDialog(this, String.format("=== PAYROLL REPORT ===\nName: %s\nCoverage: %s\n\nGross Pay: ₱%,.2f\n\nDEDUCTIONS\nSSS: ₱%,.2f\nPhilHealth: ₱%,.2f\nPag-IBIG: ₱%,.2f\nWithholding Tax: ₱%,.2f\nTotal Deductions: ₱%,.2f\n\nNET PAY: ₱%,.2f", empName, coverage, semiMonthlyGross, sss, philHealth, pagIbig, tax, totalDeductions, netPay), "Computation Verified", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnClearAll.addActionListener(e -> { 
            if(JOptionPane.showConfirmDialog(this, "Clear all displayed session records and WIPE history from PayrollRecords.csv?", "Confirm Delete History", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                srTableModel.setRowCount(0);
                lblRecordCount.setText("0 payroll records saved");
                btnClearAll.setEnabled(false);
                submissionCount[0] = 0;
                try { new FileWriter("PayrollRecords.csv", false).close(); } 
                catch (IOException ex) { System.err.println("Failed to clear file."); }
            } 
        });

        // --- TAB 3: EMPLOYEE DATABASE (CRUD) ---
        employeeDatabaseTab.setLayout(new BorderLayout()); employeeDatabaseTab.setBackground(new Color(245, 245, 245)); employeeDatabaseTab.setBorder(new EmptyBorder(15, 20, 10, 20));
        JPanel dbToolbar = new JPanel(new BorderLayout(10, 0)); dbToolbar.setOpaque(false); dbToolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        txtSearch = new JTextField(PLACEHOLDER_SEARCH); txtSearch.setForeground(Color.GRAY);
        statusFilter = new JComboBox<>(new String[]{ "All", "Regular", "Probationary" });
        
        JPanel actionDbPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actionDbPanel.setOpaque(false);
        JButton btnAdd = new JButton("Add Employee"); JButton btnEdit = new JButton("Edit Selected"); JButton btnDelete = new JButton("Delete Selected");
        actionDbPanel.add(btnAdd); actionDbPanel.add(btnEdit); actionDbPanel.add(btnDelete);
        
        JPanel topDbPanel = new JPanel(new BorderLayout()); topDbPanel.setOpaque(false);
        topDbPanel.add(txtSearch, BorderLayout.CENTER); topDbPanel.add(statusFilter, BorderLayout.EAST); topDbPanel.add(actionDbPanel, BorderLayout.SOUTH);
        employeeDatabaseTab.add(topDbPanel, BorderLayout.NORTH);

        empTableModel = new DefaultTableModel(new String[]{ "EMP #", "NAME", "POSITION", "STATUS", "BASIC SALARY" }, 0) { @Override public boolean isCellEditable(int row, int col) { return false; } };
        for (String[] row : EMPLOYEE_DATA) empTableModel.addRow(new Object[]{ row[0], row[1], row[2], row[3], "₱ " + row[4] });
        empTable = new JTable(empTableModel); empTable.setRowHeight(TABLE_ROW_HEIGHT); empTable.getTableHeader().setBackground(new Color(40, 40, 40)); empTable.getTableHeader().setForeground(Color.WHITE);
        sorter = new TableRowSorter<>(empTableModel); empTable.setRowSorter(sorter);
        employeeDatabaseTab.add(new JScrollPane(empTable), BorderLayout.CENTER);

        Runnable applyFilters = () -> {
            String q = txtSearch.getText().equals(PLACEHOLDER_SEARCH) ? "" : txtSearch.getText().trim();
            String stat = (String) statusFilter.getSelectedItem();
            List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
            if (!q.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 1, 2));
            if (!"All".equals(stat)) filters.add(RowFilter.regexFilter("^" + Pattern.quote(stat) + "$", 3));
            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        };
        txtSearch.getDocument().addDocumentListener(buildDocumentListener(applyFilters));
        statusFilter.addActionListener(e -> applyFilters.run());
        
        txtSearch.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(PLACEHOLDER_SEARCH)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); } } public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(PLACEHOLDER_SEARCH); txtSearch.setForeground(Color.GRAY); } } });

        // CRITICAL BUG FIXES APPLIED HERE
        btnAdd.addActionListener(e -> showEmployeeDialog(null));
        btnEdit.addActionListener(e -> {
            int viewRow = empTable.getSelectedRow();
            if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select an employee to edit."); return; }
            int modelRow = empTable.convertRowIndexToModel(viewRow);
            String empNo = (String) empTableModel.getValueAt(modelRow, 0); // Correctly pulls from Model
            showEmployeeDialog(dataManager.find(empNo));
        });
        btnDelete.addActionListener(e -> {
            int viewRow = empTable.getSelectedRow();
            if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select an employee to delete."); return; }
            int modelRow = empTable.convertRowIndexToModel(viewRow);
            String empNo = (String) empTableModel.getValueAt(modelRow, 0); // Correctly pulls from Model
            if (JOptionPane.showConfirmDialog(this, "Permanently delete Employee #" + empNo + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dataManager.delete(empNo);
                    empTableModel.removeRow(modelRow);
                    lblDatabase.setText(dataManager.list().size() + " employees in database");
                } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Error deleting record.", "Error", JOptionPane.ERROR_MESSAGE); }
            }
        });
    }

    private void showEmployeeDialog(EmployeeRecord existing) {
        JDialog dlg = new JDialog(this, existing == null ? "Add Employee" : "Edit Employee", true);
        dlg.setLayout(new BorderLayout()); dlg.setSize(400, 320); dlg.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5)); formPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JTextField fNo = new JTextField(existing != null ? existing.empNo : ""); if(existing != null) fNo.setEditable(false);
        JTextField fFirst = new JTextField(existing != null ? existing.rawData[2] : "");
        JTextField fLast = new JTextField(existing != null ? existing.rawData[1] : "");
        JTextField fPos = new JTextField(existing != null ? existing.position : "");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary"}); if(existing != null) cbStatus.setSelectedItem(existing.status);
        JTextField fSal = new JTextField(existing != null ? String.valueOf(existing.basicSalary) : "");
        JTextField fRate = new JTextField(existing != null ? String.valueOf(existing.rate) : "");

        formPanel.add(new JLabel("Employee No (5 Digits):")); formPanel.add(fNo); 
        formPanel.add(new JLabel("First Name:")); formPanel.add(fFirst);
        formPanel.add(new JLabel("Last Name:")); formPanel.add(fLast); 
        formPanel.add(new JLabel("Position:")); formPanel.add(fPos);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus); 
        formPanel.add(new JLabel("Basic Salary:")); formPanel.add(fSal);
        formPanel.add(new JLabel("Hourly Rate:")); formPanel.add(fRate);
        dlg.add(formPanel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Record");
        btnSave.addActionListener(ev -> {
            if(fNo.getText().trim().isEmpty() || fFirst.getText().trim().isEmpty() || fLast.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Employee No, First Name, and Last Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE); return;
            }
            if (!fNo.getText().trim().matches("^\\d{5}$")) {
                JOptionPane.showMessageDialog(dlg, "Employee No must be exactly 5 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE); return;
            }
            if(existing == null && dataManager.find(fNo.getText().trim()) != null) {
                JOptionPane.showMessageDialog(dlg, "Employee Number already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE); return;
            }
            try {
                double sal = Double.parseDouble(fSal.getText().replace(",", ""));
                double rate = Double.parseDouble(fRate.getText().replace(",", ""));
                
                if (existing == null) {
                    String[] raw = new String[19];
                    for(int i=0; i<19; i++) raw[i] = "N/A";
                    raw[0] = fNo.getText().trim(); raw[1] = fLast.getText().trim(); raw[2] = fFirst.getText().trim();
                    raw[10] = cbStatus.getSelectedItem().toString(); raw[11] = fPos.getText().trim();
                    raw[13] = String.format("%.2f", sal); raw[18] = String.format("%.2f", rate);
                    EmployeeRecord n = new EmployeeRecord(raw);
                    dataManager.addOrUpdate(n);
                    empTableModel.addRow(new Object[]{n.empNo, n.name, n.position, n.status, "₱ " + String.format("%,.2f", n.basicSalary)});
                } else {
                    existing.updateData(fFirst.getText(), fLast.getText(), cbStatus.getSelectedItem().toString(), fPos.getText(), sal, rate);
                    dataManager.addOrUpdate(existing);
                    for(int i = 0; i < empTableModel.getRowCount(); i++) {
                        if(empTableModel.getValueAt(i, 0).equals(existing.empNo)) {
                            empTableModel.setValueAt(existing.name, i, 1);
                            empTableModel.setValueAt(existing.position, i, 2);
                            empTableModel.setValueAt(existing.status, i, 3);
                            empTableModel.setValueAt("₱ " + String.format("%,.2f", existing.basicSalary), i, 4);
                            break;
                        }
                    }
                }
                lblDatabase.setText(dataManager.list().size() + " employees in database");
                dlg.dispose();
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(dlg, "Basic Salary and Hourly Rate must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE); }
              catch (IOException e) { JOptionPane.showMessageDialog(dlg, "Failed to save to CSV file.", "File Error", JOptionPane.ERROR_MESSAGE); }
        });
        btnPanel.add(btnSave); dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}
