package com.motorph.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 * Presentation helpers for {@link PayrollPortalMain}: the macOS-friendly theme
 * and the modern GroupLayout arrangements. Kept out of the form class so the UI
 * controller stays focused on wiring and events.
 *
 * <p>All methods are package-private-aware: they read the components declared in
 * {@link PayrollPortalMain} (which live in this same package), so no getters are
 * needed. The layouts are applied at runtime, after the Matisse-generated
 * {@code initComponents()}, overriding the designer layout without touching the
 * {@code .form} file.</p>
 */
public final class PayrollUIHelper {

    // Monochromatic brown & white palette
    private static final Color BROWN_DARK = new Color(0x3E, 0x27, 0x23);
    private static final Color BROWN = new Color(0xD7, 0xCC, 0xC8);
    private static final Color BROWN_MUTED = new Color(0x8D, 0x6E, 0x63);
    private static final Color CREAM = new Color(0xFB, 0xF8, 0xF4);

    private PayrollUIHelper() {
    }

    public static void applyTheme(PayrollPortalMain frame) {
        boolean isMac = System.getProperty("os.name", "").toLowerCase().contains("mac");

        Font systemFont = UIManager.getFont("Label.font");
        if (systemFont == null) {
            systemFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        applyFont(frame, systemFont);

        // Branding header — warm beige & white palette
        frame.jLabel1.setFont(systemFont.deriveFont(Font.BOLD, 30f));
        frame.jLabel1.setForeground(BROWN_DARK);
        frame.jLabel1.setOpaque(false);
        frame.jLabel9.setFont(systemFont.deriveFont(Font.PLAIN, 13f));
        frame.jLabel9.setForeground(BROWN_MUTED);

        // Soft panel padding on a warm off-white "card" background
        Border subtle = BorderFactory.createEmptyBorder(16, 16, 16, 16);
        frame.employeeFormPanel.setBorder(subtle);
        frame.employeeFormPanel.setBackground(CREAM);
        frame.submittedRecordsPanel.setBorder(subtle);
        frame.submittedRecordsPanel.setBackground(CREAM);
        frame.employeeDatabasePanel.setBorder(subtle);
        frame.employeeDatabasePanel.setBackground(CREAM);

        frame.mainTabbedPane.setFont(systemFont.deriveFont(Font.PLAIN, 13f));
        styleTable(frame.employeeTable);
        styleTable(frame.payrollRecordTable);

        if (isMac) {
            frame.addEmployeeButton.putClientProperty("JButton.buttonType", "texturedRounded");
            frame.editEmployeeButton.putClientProperty("JButton.buttonType", "texturedRounded");
            frame.deleteEmployeeButton.putClientProperty("JButton.buttonType", "texturedRounded");
            frame.deletePayrollRecordButton.putClientProperty("JButton.buttonType", "texturedRounded");
            frame.clearPayrollRecordsButton.putClientProperty("JButton.buttonType", "texturedRounded");
            frame.resetFormButton.putClientProperty("JButton.buttonType", "texturedRounded");

            // Fix invisible tab text: Aqua paints the selected tab white, so force a
            // contrasting accent background + white text. Unselected tabs keep Aqua's
            // native (appearance-aware) coloring.
            frame.mainTabbedPane.putClientProperty("JTabbedPane.selectedTabColor", BROWN);
            frame.mainTabbedPane.putClientProperty("JTabbedPane.selectedTabForeground", BROWN_DARK);
        }

        // Primary action: accent-filled button
        frame.computePayrollButton.setBackground(BROWN_MUTED);
        frame.computePayrollButton.setForeground(Color.WHITE);
        frame.computePayrollButton.setOpaque(true);
        frame.computePayrollButton.setContentAreaFilled(true);
        frame.computePayrollButton.setBorderPainted(false);
        frame.computePayrollButton.setFocusPainted(false);
    }

    private static void applyFont(Container container, Font font) {
        for (Component c : container.getComponents()) {
            c.setFont(font.deriveFont(c.getFont().getStyle(), c.getFont().getSize()));
            if (c instanceof Container) {
                applyFont((Container) c, font);
            }
        }
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 12f));
        table.setForeground(BROWN_DARK);
        table.setSelectionBackground(BROWN);
        table.setSelectionForeground(BROWN_DARK);
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 11f));
        table.getTableHeader().setForeground(BROWN_DARK);
        table.getTableHeader().setBackground(BROWN);
    }

    public static void applyModernLayout(PayrollPortalMain frame) {
        buildEmployeeFormLayout(frame);
        buildEmployeeDatabaseLayout(frame);
        buildSubmittedRecordsLayout(frame);
        buildRootLayout(frame);
    }

    private static void buildEmployeeFormLayout(PayrollPortalMain frame) {
        GroupLayout layout = new GroupLayout(frame.employeeFormPanel);
        frame.employeeFormPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.jLabel2)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.employeeIDTextField, PREFERRED_SIZE, 240, PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.jLabel3)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.employeeNameTextField, PREFERRED_SIZE, 240, PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.jLabel4)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.payCoverageTextField, PREFERRED_SIZE, 240, PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.jLabel5)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.manualHoursTextField, PREFERRED_SIZE, 240, PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.computePayrollButton, PREFERRED_SIZE, 160, PREFERRED_SIZE)
                        .addPreferredGap(UNRELATED)
                        .addComponent(frame.resetFormButton, PREFERRED_SIZE, 160, PREFERRED_SIZE))
                    .addComponent(frame.statusMessageLabel, Alignment.CENTER, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.jLabel2)
                    .addComponent(frame.employeeIDTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.jLabel3)
                    .addComponent(frame.employeeNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.jLabel4)
                    .addComponent(frame.payCoverageTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.jLabel5)
                    .addComponent(frame.manualHoursTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.computePayrollButton)
                    .addComponent(frame.resetFormButton))
                .addPreferredGap(UNRELATED)
                .addComponent(frame.statusMessageLabel)
                .addGap(0, 0, Short.MAX_VALUE)
        );

        layout.linkSize(frame.jLabel2, frame.jLabel3, frame.jLabel4, frame.jLabel5);
    }

    private static void buildEmployeeDatabaseLayout(PayrollPortalMain frame) {
        GroupLayout layout = new GroupLayout(frame.employeeDatabasePanel);
        frame.employeeDatabasePanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.jLabel6)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.searchTextField, PREFERRED_SIZE, 220, PREFERRED_SIZE)
                        .addPreferredGap(UNRELATED, 24, Short.MAX_VALUE)
                        .addComponent(frame.statusFilterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                    .addComponent(frame.jScrollPane1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(frame.addEmployeeButton)
                        .addPreferredGap(UNRELATED)
                        .addComponent(frame.editEmployeeButton)
                        .addPreferredGap(UNRELATED)
                        .addComponent(frame.deleteEmployeeButton)))
                .addContainerGap()
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.jLabel6)
                    .addComponent(frame.searchTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    .addComponent(frame.statusFilterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addComponent(frame.jScrollPane1, DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.addEmployeeButton)
                    .addComponent(frame.editEmployeeButton)
                    .addComponent(frame.deleteEmployeeButton))
                .addContainerGap()
        );
    }

    private static void buildSubmittedRecordsLayout(PayrollPortalMain frame) {
        GroupLayout layout = new GroupLayout(frame.submittedRecordsPanel);
        frame.submittedRecordsPanel.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(frame.payrollRecordsScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.payrollRecordCountLabel)
                        .addPreferredGap(UNRELATED, 24, Short.MAX_VALUE)
                        .addComponent(frame.deletePayrollRecordButton)
                        .addPreferredGap(UNRELATED)
                        .addComponent(frame.clearPayrollRecordsButton)))
                .addContainerGap()
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frame.payrollRecordsScrollPane, DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addPreferredGap(UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.payrollRecordCountLabel)
                    .addComponent(frame.deletePayrollRecordButton)
                    .addComponent(frame.clearPayrollRecordsButton))
                .addContainerGap()
        );
    }

    private static void buildRootLayout(PayrollPortalMain frame) {
        Container content = frame.getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        frame.jLabel7.setVisible(false);
        frame.jLabel7.setText("");
        frame.jLabel8.setVisible(false);
        frame.jLabel8.setText("");

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(frame.logoLabel)
                        .addPreferredGap(RELATED)
                        .addComponent(frame.jLabel1)
                        .addGap(14, 14, 14)
                        .addComponent(frame.jLabel9))
                    .addComponent(frame.mainTabbedPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frame.logoLabel)
                    .addComponent(frame.jLabel1)
                    .addComponent(frame.jLabel9))
                .addPreferredGap(UNRELATED)
                .addComponent(frame.mainTabbedPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap()
        );

        frame.setMinimumSize(new java.awt.Dimension(900, 600));
    }
}
