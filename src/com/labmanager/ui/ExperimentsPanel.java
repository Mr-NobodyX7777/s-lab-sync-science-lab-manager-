package com.labmanager.ui;

import com.labmanager.dao.ExperimentDAO;
import com.labmanager.dao.StudentDAO;
import com.labmanager.dao.TeacherDAO;
import com.labmanager.model.Experiment;
import com.labmanager.model.Student;
import com.labmanager.model.Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Lets the master user schedule an experiment, assign a supervising teacher, and pick which students are doing it. */
public class ExperimentsPanel extends JPanel {

    private final ExperimentDAO expDao = new ExperimentDAO();
    private final TeacherDAO teacherDao = new TeacherDAO();
    private final StudentDAO studentDao = new StudentDAO();

    // Note: "Color" is the last, hidden column - it just carries the hex string for row-tinting, it isn't shown.
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Title", "Date", "Start", "End", "Teacher", "Status", "# Students", "Color"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private static final int COLOR_COL = 8;
    private final JTable table = new JTable(model) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                Object hex = model.getValueAt(row, COLOR_COL);
                Color bg = UITheme.fromHex(hex == null ? null : hex.toString(), UITheme.ACCENT);
                c.setBackground(UITheme.tint(bg, 0.82));
            }
            return c;
        }
    };
    private Color currentColor = UITheme.ACCENT;
    private final JButton colorSwatch = UITheme.colorSwatchButton(UITheme.ACCENT, 30);

    private final JTextField titleField = UITheme.field();
    private final JTextField descField = UITheme.field();
    private final JTextField dateField = UITheme.field();      // yyyy-mm-dd
    private final JTextField startField = UITheme.field();     // e.g. 10:00 AM
    private final JTextField endField = UITheme.field();       // e.g. 11:00 AM
    private final JComboBox<Teacher> teacherBox = new JComboBox<>();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed", "Cancelled"});
    private final JList<Student> studentList = new JList<>();
    private final DefaultListModel<Student> studentListModel = new DefaultListModel<>();

    private Integer selectedId = null;

    public ExperimentsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Experiments & Lab Sessions");
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
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(COLOR_COL));
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

        JLabel formTitle = new JLabel("Experiment Details");
        formTitle.setFont(UITheme.FONT_HEADING.deriveFont(16f));
        formTitle.setForeground(UITheme.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(16));

        addFormField(formCard, "Title", titleField);
        addFormField(formCard, "Description", descField);
        addFormField(formCard, "Date (yyyy-mm-dd)", dateField);
        addFormField(formCard, "Start Time", startField);
        addFormField(formCard, "End Time", endField);
        addFormField(formCard, "Supervising Teacher", teacherBox);
        addFormField(formCard, "Status", statusBox);

        JLabel colorLabel = UITheme.label("COLOR TAG (used to highlight this experiment)");
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(colorLabel);
        formCard.add(Box.createVerticalStrut(6));

        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        colorRow.setOpaque(false);
        colorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        for (Color preset : UITheme.EXPERIMENT_PALETTE) {
            JButton swatch = UITheme.colorSwatchButton(preset, 22);
            swatch.addActionListener(e -> setCurrentColor(preset));
            colorRow.add(swatch);
        }
        JButton customBtn = UITheme.colorSwatchButton(currentColor, 22);
        customBtn.setToolTipText("Custom color...");
        customBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose experiment color", currentColor);
            if (chosen != null) setCurrentColor(chosen);
        });
        colorRow.add(new JLabel("|"));
        colorRow.add(customBtn);
        formCard.add(colorRow);
        formCard.add(Box.createVerticalStrut(4));

        JPanel currentColorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        currentColorRow.setOpaque(false);
        currentColorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentColorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        colorSwatch.setPreferredSize(new Dimension(22, 22));
        currentColorRow.add(colorSwatch);
        currentColorRow.add(UITheme.label("currently selected"));
        formCard.add(currentColorRow);
        formCard.add(Box.createVerticalStrut(12));

        JLabel studentsLabel = UITheme.label("STUDENTS DOING THIS EXPERIMENT (ctrl/cmd+click to multi-select)");
        studentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(studentsLabel);
        formCard.add(Box.createVerticalStrut(4));
        studentList.setModel(studentListModel);
        studentList.setVisibleRowCount(6);
        studentList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane studentScroll = new JScrollPane(studentList);
        UITheme.styleScrollPane(studentScroll);
        studentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        formCard.add(studentScroll);
        formCard.add(Box.createVerticalStrut(12));

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

        formCard.setPreferredSize(new Dimension(330, formCard.getPreferredSize().height));

        JScrollPane formScroll = new JScrollPane(formCard);
        UITheme.styleScrollPane(formScroll);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setPreferredSize(new Dimension(350, 0));
        center.add(formScroll, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addExperiment());
        updateBtn.addActionListener(e -> updateExperiment());
        deleteBtn.addActionListener(e -> deleteExperiment());
        clearBtn.addActionListener(e -> clearForm());

        loadDropdowns();
        refresh();
    }

    private void setCurrentColor(Color c) {
        currentColor = c;
        UITheme.setSwatchColor(colorSwatch, c);
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

    private void loadDropdowns() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Teacher> teachers = null;
            List<Student> students = null;
            String error = null;
            @Override protected Void doInBackground() {
                try {
                    teachers = teacherDao.getAll();
                    students = studentDao.getAll();
                } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) return;
                teacherBox.removeAllItems();
                teacherBox.addItem(null);
                for (Teacher t : teachers) teacherBox.addItem(t);
                studentListModel.clear();
                for (Student s : students) studentListModel.addElement(s);
            }
        };
        worker.execute();
    }

    private void loadSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) model.getValueAt(row, 0);
        titleField.setText(String.valueOf(model.getValueAt(row, 1)));
        dateField.setText(String.valueOf(model.getValueAt(row, 2)));
        startField.setText(String.valueOf(model.getValueAt(row, 3)));
        endField.setText(String.valueOf(model.getValueAt(row, 4)));
        String teacherName = String.valueOf(model.getValueAt(row, 5));
        for (int i = 0; i < teacherBox.getItemCount(); i++) {
            Teacher t = teacherBox.getItemAt(i);
            if (t != null && t.getName().equals(teacherName)) { teacherBox.setSelectedItem(t); break; }
            if (t == null && "(unassigned)".equals(teacherName)) { teacherBox.setSelectedItem(null); }
        }
        statusBox.setSelectedItem(String.valueOf(model.getValueAt(row, 6)));
        Object hex = model.getValueAt(row, COLOR_COL);
        setCurrentColor(UITheme.fromHex(hex == null ? null : hex.toString(), UITheme.ACCENT));

        int id = selectedId;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Student> assigned = null;
            @Override protected Void doInBackground() {
                try { assigned = expDao.getStudentsFor(id); } catch (SQLException ignored) {}
                return null;
            }
            @Override protected void done() {
                studentList.clearSelection();
                if (assigned == null) return;
                List<Integer> indices = new ArrayList<>();
                for (Student s : assigned) {
                    for (int i = 0; i < studentListModel.size(); i++) {
                        if (studentListModel.get(i).getId() == s.getId()) { indices.add(i); break; }
                    }
                }
                int[] arr = indices.stream().mapToInt(Integer::intValue).toArray();
                studentList.setSelectedIndices(arr);
            }
        };
        worker.execute();
    }

    private void clearForm() {
        selectedId = null;
        titleField.setText("");
        descField.setText("");
        dateField.setText("");
        startField.setText("");
        endField.setText("");
        teacherBox.setSelectedItem(null);
        statusBox.setSelectedIndex(0);
        setCurrentColor(UITheme.ACCENT);
        studentList.clearSelection();
        table.clearSelection();
    }

    private void addExperiment() {
        if (titleField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Experiment ex = buildExperimentFromForm();
        List<Integer> studentIds = getSelectedStudentIds();
        runAsync(() -> {
            int newId = expDao.insert(ex);
            expDao.setStudents(newId, studentIds);
        });
    }

    private void updateExperiment() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an experiment from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Experiment ex = buildExperimentFromForm();
        ex.setId(selectedId);
        List<Integer> studentIds = getSelectedStudentIds();
        runAsync(() -> {
            expDao.update(ex);
            expDao.setStudents(ex.getId(), studentIds);
        });
    }

    private void deleteExperiment() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select an experiment from the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this experiment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int id = selectedId;
        runAsync(() -> expDao.delete(id));
    }

    private Experiment buildExperimentFromForm() {
        Experiment ex = new Experiment();
        ex.setTitle(titleField.getText().trim());
        ex.setDescription(descField.getText().trim());
        ex.setExpDate(dateField.getText().trim());
        ex.setStartTime(startField.getText().trim());
        ex.setEndTime(endField.getText().trim());
        Teacher t = (Teacher) teacherBox.getSelectedItem();
        ex.setTeacherId(t == null ? null : t.getId());
        ex.setStatus(String.valueOf(statusBox.getSelectedItem()));
        ex.setColorHex(UITheme.toHex(currentColor));
        return ex;
    }

    private List<Integer> getSelectedStudentIds() {
        List<Integer> ids = new ArrayList<>();
        for (Student s : studentList.getSelectedValuesList()) ids.add(s.getId());
        return ids;
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
                    JOptionPane.showMessageDialog(ExperimentsPanel.this, "Database error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
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
            List<Experiment> list = null;
            String error = null;
            @Override protected Void doInBackground() {
                try {
                    list = expDao.getAll();
                } catch (SQLException e) { error = e.getMessage(); }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(ExperimentsPanel.this, "Failed to load experiments: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                model.setRowCount(0);
                for (Experiment ex : list) {
                    model.addRow(new Object[]{ex.getId(), ex.getTitle(), ex.getExpDate(), ex.getStartTime(),
                            ex.getEndTime(), ex.getTeacherName() == null ? "(unassigned)" : ex.getTeacherName(),
                            ex.getStatus(), ex.getStudentCount(), ex.getColorHex()});
                }
            }
        };
        worker.execute();
    }
}
