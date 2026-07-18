package com.motorph.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ClipboardHelper {

    private ClipboardHelper() {
    }

    public static void installTextClipboardShortcuts(JTextField textField) {
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

    public static void installTableCopyShortcut(JTable table) {
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK), "copyTableCell");
        table.getActionMap().put("copyTableCell", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedTableCell(table);
            }
        });
    }

    public static void copySelectedTableCell(JTable table) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        if (row == -1 || col == -1) {
            return;
        }

        Object value = table.getValueAt(row, col);
        String text = value == null ? "" : value.toString();

        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}
