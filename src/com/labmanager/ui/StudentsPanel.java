package com.labmanager.ui;

import com.labmanager.dao.StudentDAO;
import com.labmanager.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentsPanel extends JPanel {

    private final StudentDAO dao = new StudentDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Class/Grade", "Roll No", "Phone"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = UITheme.field();
    private final JTextField classField = UITheme.field();
    private final JTextField rollField = UITheme.field();
    private final JTextField phoneField = UITheme.field();

    private Integer selectedId = null;

    public StudentsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Students");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);

        // Table card
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

        // Form card
        JPanel formCard = UITheme.card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(300, 0));

        JLabel formTitle = new JLabel("Student Details");
        formTitle.setFont(UITheme.FONT_HEADING.deriveFont(16f));
        formTitle.setForeground(UITheme.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(16));

        addFormField(formCard, "Full Name", nameField);
        addFormField(formCard, "Class / Grade", classField);
        addFormField(formCard, "Roll No", rollField);
        addFormField(formCard, "Phone", phoneField);

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

        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        clearBtn.addActionListener(e -> clearForm());

        refresh();
    }

    private void addFormField(JPanel parent, String label, JComponent field) {
        JLabel l = UITheme.label(label.toUpperCase());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
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
        classField.setText(String.valueOf(model.getValueAt(row, 2)));
        rollField.setText(String.valueOf(model.getValueAt(row, 3)));
        phoneField.setText(String.valueOf(model.getValueAt(row, 4)));
    }

    private void clearForm() {
        selectedId = null;
        nameField.setText("");
        classField.setText("");
        rollField.setText("");
        phoneField.setText("");
        table.clearSelection();
    }

    private void addStudent() {
        if (nameField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student s = new Student(0, nameField.getText().trim(), classField.getText().trim(),
                rollField.getText().trim(), phoneField.getText().trim());
        runAsync(() -> dao.insert(s), "Student added.");
    }

    private void updateStudent() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student s = new Student(selectedId, nameField.getText().trim(), classField.getText().trim(),
                rollField.getText().trim(), phoneField.getText().trim());
        runAsync(() -> dao.update(s), "Student updated.");
    }

    private void deleteStudent() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this student?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int id = selectedId;
        runAsync(() -> dao.delete(id), "Student deleted.");
    }

    private interface DbOp { void run() throws SQLException; }

    private void runAsync(DbOp op, String successMsg) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String error = null;
            @Override protected Void doInBackground() {
                try { op.run(); } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(StudentsPanel.this, "Database error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
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
            List<Student> list = null;
            String error = null;
            @Override protected Void doInBackground() {
                try { list = dao.getAll(); } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(StudentsPanel.this, "Failed to load students: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                model.setRowCount(0);
                for (Student s : list) {
                    model.addRow(new Object[]{s.getId(), s.getName(), s.getClassGrade(), s.getRollNo(), s.getPhone()});
                }
            }
        };
        worker.execute();
    }
}
