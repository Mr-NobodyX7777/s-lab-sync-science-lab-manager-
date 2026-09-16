package com.labmanager.ui;

import com.labmanager.dao.EquipmentDAO;
import com.labmanager.model.Equipment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class EquipmentPanel extends JPanel {

    private final EquipmentDAO dao = new EquipmentDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Category", "Qty", "Location", "Condition", "Last Maint."}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = UITheme.field();
    private final JTextField categoryField = UITheme.field();
    private final JTextField qtyField = UITheme.field();
    private final JTextField locationField = UITheme.field();
    private final JComboBox<String> conditionBox = new JComboBox<>(new String[]{"Good", "Needs Repair", "Damaged", "Out of Service"});
    private final JTextField maintField = UITheme.field(); // yyyy-mm-dd, optional

    private Integer selectedId = null;

    public EquipmentPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Equipment & Locations");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout());
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTableHeader(table.getTableHeader());
        UITheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelection();
        });
        JScrollPane sp = new JScrollPane(table);
        UITheme.styleScrollPane(sp);
        sp.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(sp, BorderLayout.CENTER);
        center.add(tableCard, BorderLayout.CENTER);

        JPanel formCard = UITheme.card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(300, 0));

        JLabel formTitle = new JLabel("Equipment Details");
        formTitle.setFont(UITheme.FONT_HEADING.deriveFont(16f));
        formTitle.setForeground(UITheme.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(16));

        addFormField(formCard, "Name", nameField);
        addFormField(formCard, "Category", categoryField);
        addFormField(formCard, "Quantity", qtyField);
        addFormField(formCard, "Location (room/shelf)", locationField);
        addFormField(formCard, "Condition", conditionBox);
        addFormField(formCard, "Last Maintenance (yyyy-mm-dd)", maintField);

        formCard.add(Box.createVerticalStrut(10));
        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow1.setOpaque(false);
        btnRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton addBtn = UITheme.primaryButton("\u2795 Add");
        JButton updateBtn = UITheme.neutralButton("\u270F\uFE0F Update");
        btnRow1.add(addBtn);
        btnRow1.add(updateBtn);
        formCard.add(btnRow1);

        formCard.add(Box.createVerticalStrut(8));
        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow2.setOpaque(false);
        btnRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton deleteBtn = UITheme.dangerButton("\uD83D\uDDD1\uFE0F Delete");
        JButton clearBtn = UITheme.neutralButton("\uD83D\uDD04 Clear");
        btnRow2.add(deleteBtn);
        btnRow2.add(clearBtn);
        formCard.add(btnRow2);

        center.add(formCard, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());
        clearBtn.addActionListener(e -> clearForm());

        refresh();
    }

    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel l = UITheme.label(label.toUpperCase());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (field instanceof JComboBox) UITheme.styleField((JComboBox<?>) field);
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
    }

    private void loadSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) model.getValueAt(row, 0);
        nameField.setText(String.valueOf(model.getValueAt(row, 1)));
        categoryField.setText(String.valueOf(model.getValueAt(row, 2)));
        qtyField.setText(String.valueOf(model.getValueAt(row, 3)));
        locationField.setText(String.valueOf(model.getValueAt(row, 4)));
        conditionBox.setSelectedItem(String.valueOf(model.getValueAt(row, 5)));
        Object m = model.getValueAt(row, 6);
        maintField.setText(m == null ? "" : String.valueOf(m));
    }

    private void clearForm() {
        selectedId = null;
        nameField.setText("");
        categoryField.setText("");
        qtyField.setText("");
        locationField.setText("");
        conditionBox.setSelectedIndex(0);
        maintField.setText("");
        table.clearSelection();
    }

    private Integer parseQty() {
        try {
            return Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void addItem() {
        if (nameField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer qty = parseQty();
        if (qty == null) {
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Equipment e = new Equipment(0, nameField.getText().trim(), categoryField.getText().trim(), qty,
                locationField.getText().trim(), String.valueOf(conditionBox.getSelectedItem()), maintField.getText().trim());
        runAsync(() -> dao.insert(e));
    }

    private void updateItem() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an item from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer qty = parseQty();
        if (qty == null) {
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Equipment e = new Equipment(selectedId, nameField.getText().trim(), categoryField.getText().trim(), qty,
                locationField.getText().trim(), String.valueOf(conditionBox.getSelectedItem()), maintField.getText().trim());
        runAsync(() -> dao.update(e));
    }

    private void deleteItem() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an item from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this equipment item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int id = selectedId;
        runAsync(() -> dao.delete(id));
    }

    private interface DbOp { void run() throws SQLException; }

    private void runAsync(DbOp op) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String error = null;
            @Override protected Void doInBackground() {
                try { op.run(); } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(EquipmentPanel.this, "Database error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                clearForm();
                refresh();
            }
        };
        worker.execute();
    }

    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Equipment> list = null;
            String error = null;
            @Override protected Void doInBackground() {
                try { list = dao.getAll(); } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(EquipmentPanel.this, "Failed to load equipment: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                model.setRowCount(0);
                for (Equipment e : list) {
                    model.addRow(new Object[]{e.getId(), e.getName(), e.getCategory(), e.getQuantity(),
                            e.getLocation(), e.getConditionStatus(), e.getLastMaintenance()});
                }
            }
        };
        worker.execute();
    }
}
