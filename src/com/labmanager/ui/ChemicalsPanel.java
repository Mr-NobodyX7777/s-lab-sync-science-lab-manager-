package com.labmanager.ui;

import com.labmanager.dao.ChemicalDAO;
import com.labmanager.model.Chemical;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ChemicalsPanel extends JPanel {

    private final ChemicalDAO dao = new ChemicalDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Formula", "Qty", "Unit", "Location", "Expiry", "Hazard"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = UITheme.field();
    private final JTextField formulaField = UITheme.field();
    private final JTextField qtyField = UITheme.field();
    private final JComboBox<String> unitBox = new JComboBox<>(new String[]{"g", "kg", "mL", "L", "units"});
    private final JTextField locationField = UITheme.field();
    private final JTextField expiryField = UITheme.field(); // yyyy-mm-dd, optional
    private final JComboBox<String> hazardBox = new JComboBox<>(new String[]{"Low", "Medium", "High", "Flammable", "Corrosive", "Toxic"});

    private Integer selectedId = null;

    public ChemicalsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Chemical Inventory");
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

        JLabel formTitle = new JLabel("Chemical Details");
        formTitle.setFont(UITheme.FONT_HEADING.deriveFont(16f));
        formTitle.setForeground(UITheme.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(16));

        addFormField(formCard, "Name", nameField);
        addFormField(formCard, "Formula", formulaField);
        addFormField(formCard, "Quantity", qtyField);
        addFormField(formCard, "Unit", unitBox);
        addFormField(formCard, "Location (cabinet/shelf)", locationField);
        addFormField(formCard, "Expiry Date (yyyy-mm-dd)", expiryField);
        addFormField(formCard, "Hazard Level", hazardBox);

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
        formulaField.setText(String.valueOf(model.getValueAt(row, 2)));
        qtyField.setText(String.valueOf(model.getValueAt(row, 3)));
        unitBox.setSelectedItem(String.valueOf(model.getValueAt(row, 4)));
        locationField.setText(String.valueOf(model.getValueAt(row, 5)));
        Object exp = model.getValueAt(row, 6);
        expiryField.setText(exp == null ? "" : String.valueOf(exp));
        hazardBox.setSelectedItem(String.valueOf(model.getValueAt(row, 7)));
    }

    private void clearForm() {
        selectedId = null;
        nameField.setText("");
        formulaField.setText("");
        qtyField.setText("");
        unitBox.setSelectedIndex(0);
        locationField.setText("");
        expiryField.setText("");
        hazardBox.setSelectedIndex(0);
        table.clearSelection();
    }

    private Double parseQty() {
        try {
            return Double.parseDouble(qtyField.getText().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void addItem() {
        if (nameField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Double qty = parseQty();
        if (qty == null) {
            JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Chemical c = new Chemical(0, nameField.getText().trim(), formulaField.getText().trim(), qty,
                String.valueOf(unitBox.getSelectedItem()), locationField.getText().trim(),
                expiryField.getText().trim(), String.valueOf(hazardBox.getSelectedItem()));
        runAsync(() -> dao.insert(c));
    }

    private void updateItem() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an item from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Double qty = parseQty();
        if (qty == null) {
            JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Chemical c = new Chemical(selectedId, nameField.getText().trim(), formulaField.getText().trim(), qty,
                String.valueOf(unitBox.getSelectedItem()), locationField.getText().trim(),
                expiryField.getText().trim(), String.valueOf(hazardBox.getSelectedItem()));
        runAsync(() -> dao.update(c));
    }

    private void deleteItem() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an item from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this chemical entry?", "Confirm", JOptionPane.YES_NO_OPTION);
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
                    JOptionPane.showMessageDialog(ChemicalsPanel.this, "Database error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
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
            List<Chemical> list = null;
            String error = null;
            @Override protected Void doInBackground() {
                try { list = dao.getAll(); } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(ChemicalsPanel.this, "Failed to load chemicals: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                model.setRowCount(0);
                for (Chemical c : list) {
                    model.addRow(new Object[]{c.getId(), c.getName(), c.getFormula(), c.getQuantity(),
                            c.getUnit(), c.getLocation(), c.getExpiryDate(), c.getHazardLevel()});
                }
            }
        };
        worker.execute();
    }
}
