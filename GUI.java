package com.motorph.main;

/*
 * Author: Timothy Justin Sonido Gacula
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// ==========================================
// 1. DATA MODELS
// ==========================================
class EmployeeRecord {
    String[] rawData;
    String empNo;
    String name;
    String position;
    String status;
    String phone;
    String supervisor;
    double basicSalary;
    double rate;

    public EmployeeRecord(String[] rawData) {
        this.rawData = rawData;
        this.empNo = rawData[0].trim();
        this.name = rawData[2].trim() + " " + rawData[1].trim(); 
        this.phone = rawData[5].trim();
        this.status = rawData[10].trim();
        this.position = rawData[11].trim();
        this.supervisor = rawData[12].trim();
        
        // Remove quotes and commas for numeric parsing
        String salaryStr = rawData[13].replace("\"", "").replace(",", "").trim();
        String rateStr = rawData[18].replace("\"", "").replace(",", "").trim();
        
        this.basicSalary = Double.parseDouble(salaryStr);
        this.rate = Double.parseDouble(rateStr);
    }

    public void updateData(String fName, String lName, String status, String position, double basicSalary, double rate) {
        this.name = fName.trim() + " " + lName.trim();
        this.status = status;
        this.position = position;
        this.basicSalary = basicSalary;
        this.rate = rate;
        
        // Keep raw array synced so saving works correctly
        this.rawData[1] = lName.trim(); 
        this.rawData[2] = fName.trim();
        this.rawData[10] = status; 
        this.rawData[11] = position;
        this.rawData[13] = String.format(Locale.US, "%.2f", basicSalary);
        this.rawData[18] = String.format(Locale.US, "%.2f", rate);
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawData.length; i++) {
            String field = rawData[i] == null ? "" : rawData[i];
            sb.append(GUI.escapeCSV(field));
            if (i < rawData.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}

class AttendanceRecord {
    LocalDate date;
    LocalTime logIn;
    LocalTime logOut;

    public AttendanceRecord(String dateStr, String inStr, String outStr) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.US);
            
            this.date = LocalDate.parse(dateStr, dateFormatter);
            this.logIn = LocalTime.parse(inStr, timeFormatter);
            this.logOut = LocalTime.parse(outStr, timeFormatter);
        } catch (DateTimeParseException e) {
            // Silently skipping malformed attendance lines allows valid lines to still load
            System.err.println("Warning: Skipping malformed attendance data for date: " + dateStr);
        } 
    }

    public double getHoursWorked() {
        if (logIn == null || logOut == null) {
            return 0.0;
        }
        
        long minutes = Duration.between(logIn, logOut).toMinutes();
        
        // Handle night shifts spanning past midnight
        if (minutes < 0) {
            minutes += 1440; 
        }
        
        // Standard MotorPH deduction: 1 hour lunch break if worked 5 or more hours
        if (minutes >= 300) {
            minutes -= 60; 
        }
        
        return Math.max(0, minutes / 60.0);
    }
}

// ==========================================
// 2. FILE HANDLING MANAGERS
// ==========================================
class EmployeeDataManager {
    private final File file = new File("EmployeeDatabase.csv");
    private final LinkedHashMap<String, EmployeeRecord> records = new LinkedHashMap<>();
    private String headerLine = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";

    public void load() throws IOException {
        records.clear();
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; 
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) { 
                    headerLine = line; 
                    firstLine = false; 
                    continue; 
                }
                
                String[] parsedLine = parseCsvLine(line);
                if (parsedLine.length >= 19) {
                    try { 
                        EmployeeRecord record = new EmployeeRecord(parsedLine); 
                        records.put(record.empNo, record); 
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number format in employee record.");
                    }
                }
            }
        }
    }

    public static String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>(); 
        StringBuilder currentField = new StringBuilder(); 
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { 
                    currentField.append('"'); 
                    i++; 
                } else { 
                    inQuotes = !inQuotes; 
                }
            } else if (c == ',' && !inQuotes) { 
                out.add(currentField.toString().trim()); 
                currentField.setLength(0); 
            } else { 
                currentField.append(c); 
            }
        }
        
        out.add(currentField.toString().trim()); 
        return out.toArray(new String[0]);
    }

    public ArrayList<EmployeeRecord> list() { 
        return new ArrayList<>(records.values()); 
    }
    
    public EmployeeRecord find(String empNo) { 
        return records.get(empNo); 
    }
    
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
            bw.write(headerLine); 
            bw.newLine();
            
            for (EmployeeRecord r : records.values()) { 
                bw.write(r.toCSV()); 
                bw.newLine(); 
            }
        }
    }
}

class AttendanceDataManager {
    private final File file = new File("Attendance.csv");
    private final LinkedHashMap<String, ArrayList<AttendanceRecord>> attendanceMap = new LinkedHashMap<>();

    public void load() {
        attendanceMap.clear();
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; 
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) { 
                    firstLine = false; 
                    continue; 
                }
                
                String[] parsedLine = EmployeeDataManager.parseCsvLine(line);
                if (parsedLine.length >= 6) {
                    String empNo = parsedLine[0].trim();
                    AttendanceRecord rec = new AttendanceRecord(parsedLine[3].trim(), parsedLine[4].trim(), parsedLine[5].trim());
                    
                    attendanceMap.putIfAbsent(empNo, new ArrayList<>());
                    attendanceMap.get(empNo).add(rec);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Attendance.csv: " + e.getMessage());
        }
    }

    public double computeTotalHours(String empNo, int month, int startDay, int endDay, int year) {
        double total = 0;
        ArrayList<AttendanceRecord> records = attendanceMap.get(empNo);
        
        if (records != null) {
            for (AttendanceRecord r : records) {
                if (r.date != null && 
                    r.date.getYear() == year && 
                    r.date.getMonthValue() == month &&
                    r.date.getDayOfMonth() >= startDay && 
                    r.date.getDayOfMonth() <= endDay) {
                    
                    total += r.getHoursWorked();
                }
            }
        }
        return total;
    }
}

// ==========================================
// 3. MAIN GUI APPLICATION
// ==========================================
public class GUI extends JFrame {
    
    // UI Constants
    private static final int TABLE_ROW_HEIGHT = 32;
    private static final String PLACEHOLDER_EMP_NO = "e.g. 10001";
    private static final String PLACEHOLDER_EMP_NAME = "e.g. Manuel III Garcia";
    private static final String PLACEHOLDER_PAY_COVERAGE = "e.g. Month D-D, YYYY"; 
    private static final String PLACEHOLDER_MANUAL_HOURS = "e.g. 85.5 (Optional: Leave blank to auto-compute)";
    private static final String PLACEHOLDER_SEARCH = "Search by name, employee ID, or position";
    private static final String PATTERN_PAY_COVERAGE = "^[A-Za-z]+ \\d{1,2}-\\d{1,2}, \\d{4}$";

    // Data Managers
    private final EmployeeDataManager dataManager = new EmployeeDataManager();
    private final AttendanceDataManager attendanceManager = new AttendanceDataManager();
    
    // State Variables
    private int[] submissionCount = {0};

    // UI Components
    private JTextField txtEmpNo;
    private JTextField txtEmpName;
    private JTextField txtPayCoverage;
    private JTextField txtManualHours; 
    private JTextField txtSearch;
    
    private JLabel empNoHelp;
    private JLabel empNameHelp;
    private JLabel payHelp;
    private JLabel manualHoursHelp;
    
    private JTable srTable;
    private JTable empTable;
    private DefaultTableModel srTableModel;
    private DefaultTableModel empTableModel;
    
    private JLabel lblStatus;
    private JLabel lblRecordCount;
    private JLabel lblDatabase;
    
    private JButton btnCompute;
    private JButton btnReset;
    private JButton btnClearAll;
    private JButton btnDeleteRecord; 
    
    private JComboBox<String> statusFilter;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private Border normalBorder;
    private Border glowBorder;

    public GUI() {
        super("MotorPH Payroll Portal");
        setSize(1000, 700); 
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        buildUI();
        loadDataAsync();
    }
    
    private void loadDataAsync() {
        lblDatabase.setText("Loading Database...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dataManager.load();
                attendanceManager.load();
                return null;
            }
            
            @Override
            protected void done() {
                refreshEmployeeTableData();
                loadPayrollRecords();
                lblDatabase.setText(dataManager.list().size() + " employees in database");
            }
        };
        worker.execute();
    }
    
    // Updated to load ALL 19 columns into the GUI table to meet mentor requirements
    private void refreshEmployeeTableData() {
        empTableModel.setRowCount(0);
        ArrayList<EmployeeRecord> recs = dataManager.list();
        
        for (EmployeeRecord r : recs) {
            empTableModel.addRow(r.rawData);
        }
    }

    // ==========================================
    // 4. PAYROLL COMPUTATION & PERSISTENCE
    // ==========================================
    
    public static double computeSSS() { 
        return 1125.0 / 2; 
    }
    
    public static double computePhilHealth() { 
        return 375.0 / 2; 
    }
    
    public static double computePagIbig() { 
        return 100.0 / 2; 
    }
    
    public static double computeWithholdingTax(double monthlySalary) {
        double taxableIncome = monthlySalary - 1600.0;
        if (taxableIncome > 20833.0) {
            return ((taxableIncome - 20833.0) * 0.20) / 2;
        }
        return 0.0; 
    }

    public static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void savePayrollRecord(String empNo, String name, String coverage, double hours, double gross, double deductions, double net) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("PayrollRecords.csv", true))) {
            bw.write(String.format(Locale.US, "%s,%s,%s,%.2f,%.2f,%.2f,%.2f%n", 
                escapeCSV(empNo), escapeCSV(name), escapeCSV(coverage), hours, gross, deductions, net));
        } catch (IOException ex) {
            System.err.println("Failed to save payroll record to CSV.");
        }
    }

    private void rewritePayrollCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("PayrollRecords.csv", false))) {
            for (int i = 0; i < srTableModel.getRowCount(); i++) {
                String empNo = srTableModel.getValueAt(i, 1).toString();
                String name = srTableModel.getValueAt(i, 2).toString();
                String coverage = srTableModel.getValueAt(i, 3).toString();
                
                double hours = Double.parseDouble(srTableModel.getValueAt(i, 4).toString());
                double gross = Double.parseDouble(srTableModel.getValueAt(i, 5).toString().replace("Php ", "").replace(",", "").trim());
                double deduct = Double.parseDouble(srTableModel.getValueAt(i, 6).toString().replace("Php ", "").replace(",", "").trim());
                double net = Double.parseDouble(srTableModel.getValueAt(i, 7).toString().replace("Php ", "").replace(",", "").trim());

                bw.write(String.format(Locale.US, "%s,%s,%s,%.2f,%.2f,%.2f,%.2f%n", 
                    escapeCSV(empNo), escapeCSV(name), escapeCSV(coverage), hours, gross, deduct, net));
            }
        } catch (Exception ex) {
            System.err.println("Failed to rewrite PayrollRecords.csv: " + ex.getMessage());
        }
    }
    
    private void loadPayrollRecords() {
        File prFile = new File("PayrollRecords.csv");
        if (!prFile.exists()) {
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(prFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = EmployeeDataManager.parseCsvLine(line); 
                    if (p.length >= 6) { 
                        submissionCount[0]++;
                        
                        double hours = p.length >= 7 ? Double.parseDouble(p[3]) : 0.0;
                        double gross = p.length >= 7 ? Double.parseDouble(p[4]) : Double.parseDouble(p[3]);
                        double deduct = p.length >= 7 ? Double.parseDouble(p[5]) : Double.parseDouble(p[4]);
                        double net = p.length >= 7 ? Double.parseDouble(p[6]) : Double.parseDouble(p[5]);
                        
                        srTableModel.addRow(new Object[] { 
                            submissionCount[0], 
                            p[0], 
                            p[1], 
                            p[2], 
                            String.format(Locale.US, "%.2f", hours), 
                            String.format(Locale.US, "Php %,.2f", gross), 
                            String.format(Locale.US, "Php %,.2f", deduct), 
                            String.format(Locale.US, "Php %,.2f", net) 
                        });
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    System.err.println("Warning: Skipping malformed payroll record line.");
                }
            }
            if (submissionCount[0] > 0) {
                lblRecordCount.setText(srTableModel.getRowCount() + " payroll record(s) saved");
                btnClearAll.setEnabled(true);
                btnDeleteRecord.setEnabled(true);
            }
        } catch (IOException e) { 
            System.err.println("Error reading PayrollRecords.csv"); 
        }
    }

    public static int getMonthNumber(String monthName) {
        String[] months = {
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        };
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private static boolean isValidDayRange(String coverage) {
        try {
            String[] spaceParts = coverage.trim().split(" ");
            if (spaceParts.length < 2) {
                return false;
            }
            
            int monthNum = getMonthNumber(spaceParts[0]);
            if (monthNum == -1) {
                return false;
            }

            String[] days = spaceParts[1].replace(",", "").split("-");
            if (days.length != 2) {
                return false;
            }
            
            int startDay = Integer.parseInt(days[0].trim());
            int endDay = Integer.parseInt(days[1].trim());
            int year = Integer.parseInt(spaceParts[2]);

            if (startDay >= endDay) {
                return false;
            }

            LocalDate.of(year, monthNum, startDay);
            LocalDate.of(year, monthNum, endDay);
            
            return true;
        } catch (DateTimeException | NumberFormatException ex) { 
            return false; 
        }
    }

    // ==========================================
    // 5. UI CONSTRUCTION & LISTENERS
    // ==========================================
    
    private static DocumentListener buildDocumentListener(Runnable onChange) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        };
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

        normalBorder = new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1), 
            new EmptyBorder(2, 5, 2, 5)
        );
        
        glowBorder = new CompoundBorder(
            new LineBorder(new Color(220, 50, 50, 30), 3), 
            new CompoundBorder(
                new LineBorder(new Color(220, 50, 50, 80), 2), 
                new CompoundBorder(
                    new LineBorder(new Color(220, 50, 50), 1), 
                    new EmptyBorder(2, 5, 2, 5)
                )
            )
        );

        // --- TAB 1: EMPLOYEE FORM ---
        employeeFormTab.setLayout(new BorderLayout()); 
        employeeFormTab.setBackground(new Color(245, 245, 245)); 
        employeeFormTab.setBorder(new EmptyBorder(15, 35, 0, 35));
        
        JPanel formTopPanel = new JPanel(new BorderLayout()); 
        formTopPanel.setOpaque(false);
        
        JLabel formTitle = new JLabel("Employee Payroll Form"); 
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        formTopPanel.add(formTitle, BorderLayout.WEST);

        JPanel formContainer = new JPanel(); 
        formContainer.setOpaque(false); 
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        // Setup Emp No Field
        txtEmpNo = new JTextField(PLACEHOLDER_EMP_NO); 
        txtEmpNo.setPreferredSize(new Dimension(800, 40)); 
        txtEmpNo.setMaximumSize(new Dimension(850, 40)); 
        txtEmpNo.setForeground(Color.GRAY); 
        txtEmpNo.setBorder(normalBorder);
        
        empNoHelp = new JLabel("Enter the employee's 5-digit ID number"); 
        empNoHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); 
        empNoHelp.setForeground(Color.GRAY);
        
        // Setup Emp Name Field
        txtEmpName = new JTextField(PLACEHOLDER_EMP_NAME); 
        txtEmpName.setPreferredSize(new Dimension(800, 40)); 
        txtEmpName.setMaximumSize(new Dimension(850, 40)); 
        txtEmpName.setForeground(Color.GRAY); 
        txtEmpName.setBorder(normalBorder);
        
        empNameHelp = new JLabel("Enter the employee's full name as listed for the employee ID."); 
        empNameHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); 
        empNameHelp.setForeground(Color.GRAY);
        
        // Setup Pay Coverage Field
        txtPayCoverage = new JTextField(PLACEHOLDER_PAY_COVERAGE); 
        txtPayCoverage.setPreferredSize(new Dimension(800, 40)); 
        txtPayCoverage.setMaximumSize(new Dimension(850, 40)); 
        txtPayCoverage.setForeground(Color.GRAY); 
        txtPayCoverage.setBorder(normalBorder);
        
        payHelp = new JLabel("Enter the payroll period exactly as: Month D-D, YYYY (*Note: Only 2024 attendance data is available)"); 
        payHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); 
        payHelp.setForeground(Color.GRAY);

        // Setup Manual Hours Field
        txtManualHours = new JTextField(PLACEHOLDER_MANUAL_HOURS); 
        txtManualHours.setPreferredSize(new Dimension(800, 40)); 
        txtManualHours.setMaximumSize(new Dimension(850, 40)); 
        txtManualHours.setForeground(Color.GRAY); 
        txtManualHours.setBorder(normalBorder);
        
        manualHoursHelp = new JLabel("Optional: Override automated calculations by entering manual hours."); 
        manualHoursHelp.setFont(new Font("SansSerif", Font.PLAIN, 11)); 
        manualHoursHelp.setForeground(Color.GRAY);

        // Add to layout
        formContainer.add(Box.createVerticalStrut(17)); 
        formContainer.add(new JLabel("Employee Number *")); 
        formContainer.add(txtEmpNo); 
        formContainer.add(empNoHelp);
        
        formContainer.add(Box.createVerticalStrut(10)); 
        formContainer.add(new JLabel("Employee Name *")); 
        formContainer.add(txtEmpName); 
        formContainer.add(empNameHelp);
        
        formContainer.add(Box.createVerticalStrut(10)); 
        formContainer.add(new JLabel("Pay Coverage *")); 
        formContainer.add(txtPayCoverage); 
        formContainer.add(payHelp);

        formContainer.add(Box.createVerticalStrut(10)); 
        formContainer.add(new JLabel("Manual Hours (Optional)")); 
        formContainer.add(txtManualHours); 
        formContainer.add(manualHoursHelp);
        
        formContainer.add(Box.createVerticalStrut(25));

        JPanel actionPanel = new JPanel(new BorderLayout()); 
        actionPanel.setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); 
        buttonPanel.setOpaque(false);
        
        btnReset = new JButton("Reset Form"); 
        btnCompute = new JButton("Compute Pay");
        
        btnReset.setEnabled(false); 
        btnCompute.setEnabled(false); 
        btnCompute.setBackground(new Color(230, 230, 230));
        
        buttonPanel.add(btnReset); 
        buttonPanel.add(btnCompute);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        
        formContainer.add(actionPanel); 
        formContainer.add(Box.createVerticalStrut(15)); 
        formContainer.add(new JSeparator());

        JPanel statusBar = new JPanel(new BorderLayout()); 
        statusBar.setOpaque(false);
        
        lblStatus = new JLabel("Ready — Fill in the form and click Compute Pay to begin");
        lblDatabase = new JLabel("0 employees in database", SwingConstants.RIGHT);
        
        statusBar.add(lblStatus, BorderLayout.WEST); 
        statusBar.add(lblDatabase, BorderLayout.EAST);
        formContainer.add(statusBar);

        // Validation Listeners
        DocumentListener fieldValidator = buildDocumentListener(() -> {
            String no = txtEmpNo.getText().trim();
            String name = txtEmpName.getText().trim();
            String cov = txtPayCoverage.getText().trim();
            EmployeeRecord rec = dataManager.find(no);
            
            boolean validEmpNo = rec != null && no.matches("^\\d{5}$");
            
            if (!no.equals(PLACEHOLDER_EMP_NO) && !no.isEmpty()) {
                if (!validEmpNo) { 
                    txtEmpNo.setBorder(glowBorder); 
                    empNoHelp.setText("Requires a valid 5-digit Employee ID."); 
                    empNoHelp.setForeground(Color.RED); 
                } else { 
                    txtEmpNo.setBorder(normalBorder); 
                    empNoHelp.setText("Employee found."); 
                    empNoHelp.setForeground(new Color(0, 130, 0)); 
                }
            }
            
            boolean validName = validEmpNo && rec.name.equalsIgnoreCase(name);
            
            if (!name.equals(PLACEHOLDER_EMP_NAME) && !name.isEmpty()) {
                if (validEmpNo && !validName) { 
                    txtEmpName.setBorder(glowBorder); 
                    empNameHelp.setText("Name does not match ID."); 
                    empNameHelp.setForeground(Color.RED); 
                } else if (validName) { 
                    txtEmpName.setBorder(normalBorder); 
                    empNameHelp.setText("Valid name."); 
                    empNameHelp.setForeground(new Color(0, 130, 0)); 
                }
            }
            
            boolean validCov = cov.matches(PATTERN_PAY_COVERAGE) && isValidDayRange(cov);
            
            if (!cov.equals(PLACEHOLDER_PAY_COVERAGE) && !cov.isEmpty()) {
                if (!validCov) { 
                    txtPayCoverage.setBorder(glowBorder); 
                    payHelp.setText("Invalid format or dates. Use: Month D-D, YYYY (*Note: Only 2024 attendance data is available)"); 
                    payHelp.setForeground(Color.RED); 
                } else { 
                    txtPayCoverage.setBorder(normalBorder); 
                    payHelp.setText("Valid coverage format. (*Note: Only 2024 attendance data is available)"); 
                    payHelp.setForeground(new Color(0, 130, 0)); 
                }
            }

            boolean valid = validEmpNo && validName && validCov;
            btnCompute.setEnabled(valid);
            btnCompute.setBackground(valid ? new Color(0, 120, 215) : new Color(230, 230, 230));
            btnCompute.setForeground(valid ? Color.WHITE : Color.BLACK);
            btnReset.setEnabled(!no.equals(PLACEHOLDER_EMP_NO) || !name.equals(PLACEHOLDER_EMP_NAME) || !cov.equals(PLACEHOLDER_PAY_COVERAGE));
        });
        
        txtEmpNo.addFocusListener(new FocusAdapter() { 
            @Override
            public void focusGained(FocusEvent e) { 
                if (txtEmpNo.getText().equals(PLACEHOLDER_EMP_NO)) { 
                    txtEmpNo.setText(""); 
                    txtEmpNo.setForeground(Color.BLACK); 
                } 
            } 
            @Override
            public void focusLost(FocusEvent e) { 
                if (txtEmpNo.getText().isEmpty()) { 
                    txtEmpNo.setText(PLACEHOLDER_EMP_NO); 
                    txtEmpNo.setForeground(Color.GRAY); 
                    txtEmpNo.setBorder(normalBorder); 
                    empNoHelp.setText("Enter the employee's 5-digit ID number"); 
                    empNoHelp.setForeground(Color.GRAY); 
                } 
            } 
        });
        
        txtEmpName.addFocusListener(new FocusAdapter() { 
            @Override
            public void focusGained(FocusEvent e) { 
                if (txtEmpName.getText().equals(PLACEHOLDER_EMP_NAME)) { 
                    txtEmpName.setText(""); 
                    txtEmpName.setForeground(Color.BLACK); 
                } 
            } 
            @Override
            public void focusLost(FocusEvent e) { 
                if (txtEmpName.getText().isEmpty()) { 
                    txtEmpName.setText(PLACEHOLDER_EMP_NAME); 
                    txtEmpName.setForeground(Color.GRAY); 
                    txtEmpName.setBorder(normalBorder); 
                    empNameHelp.setText("Enter the employee's full name as listed for the employee ID."); 
                    empNameHelp.setForeground(Color.GRAY); 
                } 
            } 
        });
        
        txtPayCoverage.addFocusListener(new FocusAdapter() { 
            @Override
            public void focusGained(FocusEvent e) { 
                if (txtPayCoverage.getText().equals(PLACEHOLDER_PAY_COVERAGE)) { 
                    txtPayCoverage.setText(""); 
                    txtPayCoverage.setForeground(Color.BLACK); 
                } 
            } 
            @Override
            public void focusLost(FocusEvent e) { 
                if (txtPayCoverage.getText().isEmpty()) { 
                    txtPayCoverage.setText(PLACEHOLDER_PAY_COVERAGE); 
                    txtPayCoverage.setForeground(Color.GRAY); 
                    txtPayCoverage.setBorder(normalBorder); 
                    payHelp.setText("Enter the payroll period exactly as: Month D-D, YYYY (*Note: Only 2024 attendance data is available)"); 
                    payHelp.setForeground(Color.GRAY); 
                } 
            } 
        });

        txtManualHours.addFocusListener(new FocusAdapter() { 
            @Override
            public void focusGained(FocusEvent e) { 
                if (txtManualHours.getText().equals(PLACEHOLDER_MANUAL_HOURS)) { 
                    txtManualHours.setText(""); 
                    txtManualHours.setForeground(Color.BLACK); 
                } 
            } 
            @Override
            public void focusLost(FocusEvent e) { 
                if (txtManualHours.getText().isEmpty()) { 
                    txtManualHours.setText(PLACEHOLDER_MANUAL_HOURS); 
                    txtManualHours.setForeground(Color.GRAY); 
                } 
            } 
        });

        txtEmpNo.getDocument().addDocumentListener(fieldValidator); 
        txtEmpName.getDocument().addDocumentListener(fieldValidator); 
        txtPayCoverage.getDocument().addDocumentListener(fieldValidator);

        btnReset.addActionListener(e -> {
            txtEmpNo.setText(PLACEHOLDER_EMP_NO); 
            txtEmpNo.setForeground(Color.GRAY); 
            txtEmpNo.setBorder(normalBorder); 
            empNoHelp.setText("Enter the employee's 5-digit ID number"); 
            empNoHelp.setForeground(Color.GRAY);
            
            txtEmpName.setText(PLACEHOLDER_EMP_NAME); 
            txtEmpName.setForeground(Color.GRAY); 
            txtEmpName.setBorder(normalBorder); 
            empNameHelp.setText("Enter the employee's full name as listed for the employee ID."); 
            empNameHelp.setForeground(Color.GRAY);
            
            txtPayCoverage.setText(PLACEHOLDER_PAY_COVERAGE); 
            txtPayCoverage.setForeground(Color.GRAY); 
            txtPayCoverage.setBorder(normalBorder); 
            payHelp.setText("Enter the payroll period exactly as: Month D-D, YYYY (*Note: Only 2024 attendance data is available)"); 
            payHelp.setForeground(Color.GRAY);

            txtManualHours.setText(PLACEHOLDER_MANUAL_HOURS); 
            txtManualHours.setForeground(Color.GRAY); 
            
            btnCompute.setEnabled(false); 
            btnReset.setEnabled(false);
            lblStatus.setText("Ready — Fill in the form and click Compute Pay to begin");
        });

        employeeFormTab.add(formTopPanel, BorderLayout.NORTH); 
        employeeFormTab.add(formContainer, BorderLayout.CENTER);

        // --- TAB 2: SUBMITTED RECORDS ---
        submittedRecordsTab.setLayout(new BorderLayout()); 
        submittedRecordsTab.setBackground(new Color(245, 245, 245)); 
        submittedRecordsTab.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        String[] srColumns = { "#", "EMP #", "NAME", "COVERAGE", "HOURS", "GROSS PAY", "DEDUCT.", "NET PAY" };
        srTableModel = new DefaultTableModel(srColumns, 0) { 
            @Override 
            public boolean isCellEditable(int row, int col) { return false; } 
        };
        
        srTable = new JTable(srTableModel); 
        srTable.setRowHeight(32); 
        srTable.getTableHeader().setBackground(new Color(40, 40, 40)); 
        srTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane srScroll = new JScrollPane(srTable);
        
        JPanel srHeaderPanel = new JPanel(new BorderLayout()); 
        srHeaderPanel.setOpaque(false); 
        srHeaderPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        lblRecordCount = new JLabel("0 payroll records saved"); 
        
        btnDeleteRecord = new JButton("Delete Selected");
        btnClearAll = new JButton("Clear All Records"); 
        
        btnDeleteRecord.setEnabled(false);
        btnClearAll.setEnabled(false);
        
        JPanel srButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        srButtonsPanel.setOpaque(false);
        srButtonsPanel.add(btnDeleteRecord);
        srButtonsPanel.add(btnClearAll);
        
        srHeaderPanel.add(lblRecordCount, BorderLayout.WEST); 
        srHeaderPanel.add(srButtonsPanel, BorderLayout.EAST);
        
        submittedRecordsTab.add(srHeaderPanel, BorderLayout.NORTH); 
        submittedRecordsTab.add(srScroll, BorderLayout.CENTER);

        btnCompute.addActionListener(e -> {
            String empNo = txtEmpNo.getText().trim();
            String empName = txtEmpName.getText().trim();
            String coverage = txtPayCoverage.getText().trim();
            String manualHours = txtManualHours.getText().trim();
            
            EmployeeRecord emp = dataManager.find(empNo);
            
            if (emp == null) return; 
            
            String[] covParts = coverage.split(" ");
            int monthNum = getMonthNumber(covParts[0]);
            String[] days = covParts[1].replace(",", "").split("-");
            int year = Integer.parseInt(covParts[2]);
            
            double hoursWorked = 0.0;
            
            if (!manualHours.isEmpty() && !manualHours.equals(PLACEHOLDER_MANUAL_HOURS)) {
                try {
                    hoursWorked = Double.parseDouble(manualHours);
                    if (hoursWorked < 0) {
                        throw new NumberFormatException("Negative hours");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Hours Worked must be a valid positive number. Letters or symbols are not allowed.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            } else {
                hoursWorked = attendanceManager.computeTotalHours(empNo, monthNum, Integer.parseInt(days[0]), Integer.parseInt(days[1]), year);
                
                if (hoursWorked == 0.0) {
                    JOptionPane.showMessageDialog(this, 
                        "Warning: 0 hours worked calculated. Verify Attendance.csv data matches the selected Coverage Period.", 
                        "Zero Hours Warning", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }

            double grossPay = hoursWorked * emp.rate;
            double sss = computeSSS();
            double philHealth = computePhilHealth();
            double pagIbig = computePagIbig();
            double tax = computeWithholdingTax(emp.basicSalary); 
            
            double totalDeductions = sss + philHealth + pagIbig + tax;
            double netPay = grossPay - totalDeductions;

            submissionCount[0]++;
            lblStatus.setText(submissionCount[0] + " payroll record(s) computed.");
            
            srTableModel.addRow(new Object[] { 
                submissionCount[0], 
                empNo, 
                empName, 
                coverage, 
                String.format(Locale.US, "%.2f", hoursWorked), 
                String.format(Locale.US, "Php %,.2f", grossPay), 
                String.format(Locale.US, "Php %,.2f", totalDeductions), 
                String.format(Locale.US, "Php %,.2f", netPay) 
            });
            
            savePayrollRecord(empNo, empName, coverage, hoursWorked, grossPay, totalDeductions, netPay);
            
            lblRecordCount.setText(srTableModel.getRowCount() + " payroll record(s) saved");
            btnClearAll.setEnabled(true);
            btnDeleteRecord.setEnabled(true);
            
            String report = String.format(Locale.US, 
                "=== PAYROLL REPORT ===\nName: %s\nCoverage: %s\nActual Hours Worked: %.2f\nHourly Rate: Php %,.2f\n\nGross Pay: Php %,.2f\n\nDEDUCTIONS\nSSS: Php %,.2f\nPhilHealth: Php %,.2f\nPag-IBIG: Php %,.2f\nWithholding Tax: Php %,.2f\nTotal Deductions: Php %,.2f\n\nNET PAY: Php %,.2f", 
                empName, coverage, hoursWorked, emp.rate, grossPay, sss, philHealth, pagIbig, tax, totalDeductions, netPay);
            JOptionPane.showMessageDialog(this, report, "Computation Verified", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnDeleteRecord.addActionListener(e -> {
            int viewRow = srTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a payroll record to delete from the table.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = srTable.convertRowIndexToModel(viewRow);

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Permanently delete this specific payroll record?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                srTableModel.removeRow(modelRow);

                for(int i = 0; i < srTableModel.getRowCount(); i++) {
                    srTableModel.setValueAt(i + 1, i, 0);
                }
                
                submissionCount[0] = srTableModel.getRowCount();
                lblRecordCount.setText(srTableModel.getRowCount() + " payroll record(s) saved");

                if (srTableModel.getRowCount() == 0) {
                    btnClearAll.setEnabled(false);
                    btnDeleteRecord.setEnabled(false);
                }

                rewritePayrollCSV();
            }
        });

        btnClearAll.addActionListener(e -> { 
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Clear all displayed session records and WIPE history from PayrollRecords.csv?", 
                "Confirm Delete History", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                srTableModel.setRowCount(0);
                lblRecordCount.setText("0 payroll records saved");
                btnClearAll.setEnabled(false);
                btnDeleteRecord.setEnabled(false);
                submissionCount[0] = 0;
                
                try { 
                    new FileWriter("PayrollRecords.csv", false).close(); 
                } catch (IOException ex) { 
                    System.err.println("Failed to clear file."); 
                }
            } 
        });

        // --- TAB 3: EMPLOYEE DATABASE (CRUD) ---
        employeeDatabaseTab.setLayout(new BorderLayout()); 
        employeeDatabaseTab.setBackground(new Color(245, 245, 245)); 
        employeeDatabaseTab.setBorder(new EmptyBorder(15, 20, 10, 20));
        
        JPanel dbToolbar = new JPanel(new BorderLayout(10, 0)); 
        dbToolbar.setOpaque(false); 
        dbToolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        txtSearch = new JTextField(PLACEHOLDER_SEARCH); 
        txtSearch.setForeground(Color.GRAY);
        statusFilter = new JComboBox<>(new String[]{ "All", "Regular", "Probationary" });
        
        JPanel actionDbPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        actionDbPanel.setOpaque(false);
        JButton btnAdd = new JButton("Add Employee"); 
        JButton btnEdit = new JButton("Edit Selected"); 
        JButton btnDelete = new JButton("Delete Selected");
        
        actionDbPanel.add(btnAdd); 
        actionDbPanel.add(btnEdit); 
        actionDbPanel.add(btnDelete);
        
        JPanel topDbPanel = new JPanel(new BorderLayout()); 
        topDbPanel.setOpaque(false);
        topDbPanel.add(txtSearch, BorderLayout.CENTER); 
        topDbPanel.add(statusFilter, BorderLayout.EAST); 
        topDbPanel.add(actionDbPanel, BorderLayout.SOUTH);
        
        employeeDatabaseTab.add(topDbPanel, BorderLayout.NORTH);

        // Updated array to show ALL 19 columns
        String[] dbColumns = {
            "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone Number", 
            "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position", 
            "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance", 
            "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
        };
        
        empTableModel = new DefaultTableModel(dbColumns, 0) { 
            @Override 
            public boolean isCellEditable(int row, int col) { return false; } 
        };
        
        empTable = new JTable(empTableModel); 
        empTable.setRowHeight(TABLE_ROW_HEIGHT); 
        empTable.getTableHeader().setBackground(new Color(40, 40, 40)); 
        empTable.getTableHeader().setForeground(Color.WHITE);
        
        // Turns off auto-resizing so all 19 columns can scroll horizontally without being squished
        empTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Sets a default width for the columns so they are easily readable
        for(int i = 0; i < dbColumns.length; i++) {
            empTable.getColumnModel().getColumn(i).setPreferredWidth(130);
        }
        
        sorter = new TableRowSorter<>(empTableModel); 
        empTable.setRowSorter(sorter);
        
        employeeDatabaseTab.add(new JScrollPane(empTable), BorderLayout.CENTER);

        Runnable applyFilters = () -> {
            String query = txtSearch.getText().equals(PLACEHOLDER_SEARCH) ? "" : txtSearch.getText().trim();
            String stat = (String) statusFilter.getSelectedItem();
            
            List<RowFilter<Object, Object>> filters = new ArrayList<>();
            
            if (!query.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query), 0, 1, 2));
            }
            if (!"All".equals(stat)) {
                // Status is now at index 10 in the 19-column array
                filters.add(RowFilter.regexFilter("^" + Pattern.quote(stat) + "$", 10)); 
            }
            
            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        };
        
        txtSearch.getDocument().addDocumentListener(buildDocumentListener(applyFilters));
        statusFilter.addActionListener(e -> applyFilters.run());
        
        txtSearch.addFocusListener(new FocusAdapter() { 
            @Override
            public void focusGained(FocusEvent e) { 
                if (txtSearch.getText().equals(PLACEHOLDER_SEARCH)) { 
                    txtSearch.setText(""); 
                    txtSearch.setForeground(Color.BLACK); 
                } 
            } 
            @Override
            public void focusLost(FocusEvent e) { 
                if (txtSearch.getText().isEmpty()) { 
                    txtSearch.setText(PLACEHOLDER_SEARCH); 
                    txtSearch.setForeground(Color.GRAY); 
                } 
            } 
        });

        btnAdd.addActionListener(e -> showEmployeeDialog(null));
        
        btnEdit.addActionListener(e -> {
            int viewRow = empTable.getSelectedRow();
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "Select an employee to edit."); 
                return; 
            }
            int modelRow = empTable.convertRowIndexToModel(viewRow);
            String empNo = (String) empTableModel.getValueAt(modelRow, 0); 
            showEmployeeDialog(dataManager.find(empNo));
        });
        
        btnDelete.addActionListener(e -> {
            int viewRow = empTable.getSelectedRow();
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "Select an employee to delete."); 
                return; 
            }
            int modelRow = empTable.convertRowIndexToModel(viewRow);
            String empNo = (String) empTableModel.getValueAt(modelRow, 0); 
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Permanently delete Employee #" + empNo + "?", 
                "Confirm", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dataManager.delete(empNo);
                    empTableModel.removeRow(modelRow);
                    lblDatabase.setText(dataManager.list().size() + " employees in database");
                } catch (IOException ex) { 
                    JOptionPane.showMessageDialog(this, "Error deleting record.", "Error", JOptionPane.ERROR_MESSAGE); 
                }
            }
        });
    }

    private void showEmployeeDialog(EmployeeRecord existing) {
        JDialog dlg = new JDialog(this, existing == null ? "Add Employee" : "Edit Employee", true);
        dlg.setLayout(new BorderLayout()); 
        dlg.setSize(400, 320); 
        dlg.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5)); 
        formPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // --- AUTO GENERATE EMPLOYEE NUMBER LOGIC ---
        String nextEmpNo = "";
        if (existing != null) {
            nextEmpNo = existing.empNo;
        } else {
            int maxId = 10000;
            for (EmployeeRecord rec : dataManager.list()) {
                try {
                    int currentId = Integer.parseInt(rec.empNo);
                    if (currentId > maxId) maxId = currentId;
                } catch (NumberFormatException ignored) {}
            }
            nextEmpNo = String.valueOf(maxId + 1); // Guarantees a unique ID!
        }
        
        JTextField fNo = new JTextField(nextEmpNo); 
        fNo.setEditable(false); // Locked so user cannot duplicate IDs
        
        JTextField fFirst = new JTextField(existing != null ? existing.rawData[2] : "");
        JTextField fLast = new JTextField(existing != null ? existing.rawData[1] : "");
        JTextField fPos = new JTextField(existing != null ? existing.position : "");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary"}); 
        
        if (existing != null) {
            cbStatus.setSelectedItem(existing.status);
        }
        
        JTextField fSal = new JTextField(existing != null ? String.valueOf(existing.basicSalary) : "");
        JTextField fRate = new JTextField(existing != null ? String.valueOf(existing.rate) : "");
        fRate.setEditable(false); // Locked so it is strictly tied to Basic Salary
        
        // --- AUTO-CALCULATE HOURLY RATE LOGIC ---
        fSal.getDocument().addDocumentListener(buildDocumentListener(() -> {
            if (fSal.isFocusOwner()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        double sal = Double.parseDouble(fSal.getText().replace(",", "").trim());
                        double rate = sal / 168.0; // Standard MotorPH divisor
                        fRate.setText(String.format(Locale.US, "%.2f", rate));
                    } catch (Exception ex) {
                        fRate.setText("0.00");
                    }
                });
            }
        }));

        formPanel.add(new JLabel("Employee No (Auto-Generated):")); 
        formPanel.add(fNo); 
        
        formPanel.add(new JLabel("First Name:")); 
        formPanel.add(fFirst);
        
        formPanel.add(new JLabel("Last Name:")); 
        formPanel.add(fLast); 
        
        formPanel.add(new JLabel("Position:")); 
        formPanel.add(fPos);
        
        formPanel.add(new JLabel("Status:")); 
        formPanel.add(cbStatus); 
        
        formPanel.add(new JLabel("Basic Salary:")); 
        formPanel.add(fSal);
        
        formPanel.add(new JLabel("Hourly Rate (Auto-Calculated):")); 
        formPanel.add(fRate);
        
        dlg.add(formPanel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Record");
        
        btnSave.addActionListener(ev -> {
            if (fFirst.getText().trim().isEmpty() || fLast.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "First Name and Last Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE); 
                return;
            }
            try {
                double sal = Double.parseDouble(fSal.getText().replace(",", ""));
                double rate = Double.parseDouble(fRate.getText().replace(",", ""));
                
                if (existing == null) {
                    String[] raw = new String[19];
                    for (int i = 0; i < 19; i++) raw[i] = "N/A"; // Fills empty columns with N/A
                    
                    raw[0] = fNo.getText().trim(); 
                    raw[1] = fLast.getText().trim(); 
                    raw[2] = fFirst.getText().trim();
                    raw[10] = cbStatus.getSelectedItem().toString(); 
                    raw[11] = fPos.getText().trim();
                    raw[13] = String.format(Locale.US, "%.2f", sal); 
                    raw[18] = String.format(Locale.US, "%.2f", rate);
                    
                    EmployeeRecord n = new EmployeeRecord(raw);
                    dataManager.addOrUpdate(n);
                    
                    empTableModel.addRow(n.rawData); // Adds all 19 columns to GUI
                } else {
                    existing.updateData(fFirst.getText(), fLast.getText(), cbStatus.getSelectedItem().toString(), fPos.getText(), sal, rate);
                    dataManager.addOrUpdate(existing);
                    
                    for (int i = 0; i < empTableModel.getRowCount(); i++) {
                        if (empTableModel.getValueAt(i, 0).equals(existing.empNo)) {
                            // Update all 19 columns in the GUI dynamically
                            for(int col = 0; col < 19; col++) {
                                empTableModel.setValueAt(existing.rawData[col], i, col);
                            }
                            break;
                        }
                    }
                }
                lblDatabase.setText(dataManager.list().size() + " employees in database");
                dlg.dispose();
            } catch (NumberFormatException e) { 
                JOptionPane.showMessageDialog(dlg, "Basic Salary must be a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE); 
            } catch (IOException e) { 
                JOptionPane.showMessageDialog(dlg, "Failed to save to CSV file.", "File Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
        
        btnPanel.add(btnSave); 
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}
