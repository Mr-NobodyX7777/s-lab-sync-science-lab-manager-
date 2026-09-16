package com.labmanager.ui;

import com.labmanager.dao.*;
import com.labmanager.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Master "what is happening in the lab" view. */
public class OverviewPanel extends JPanel {

    private final JLabel studentsCount = new JLabel("-");
    private final JLabel teachersCount = new JLabel("-");
    private final JLabel equipmentCount = new JLabel("-");
    private final JLabel chemicalsCount = new JLabel("-");
    // "Color" is a hidden last column carrying the hex string used only to tint each row.
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Experiment", "Date", "Time", "Supervising Teacher", "Status", "Students", "Color"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private static final int COLOR_COL = 6;
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

    public OverviewPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Lab Overview");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("Students", studentsCount, UITheme.ACCENT));
        stats.add(statCard("Teachers", teachersCount, UITheme.ACCENT_DARK));
        stats.add(statCard("Equipment items", equipmentCount, UITheme.WARNING));
        stats.add(statCard("Chemical entries", chemicalsCount, UITheme.SUCCESS));
        center.add(stats, BorderLayout.NORTH);

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        JLabel tableTitle = new JLabel("Experiments & who is doing what, right now");
        tableTitle.setFont(UITheme.FONT_HEADING.deriveFont(15f));
        tableTitle.setForeground(UITheme.TEXT_DARK);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTableHeader(table.getTableHeader());
        UITheme.styleTable(table);
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(COLOR_COL));
        JScrollPane sp = new JScrollPane(table);
        UITheme.styleScrollPane(sp);
        sp.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(sp, BorderLayout.CENTER);

        JButton refreshBtn = UITheme.neutralButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);
        tableCard.add(btnRow, BorderLayout.SOUTH);

        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel p = UITheme.card();
        p.setLayout(new BorderLayout());
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_MUTED);
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(l, BorderLayout.SOUTH);
        return p;
    }

    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int students = 0, teachers = 0, equipment = 0, chemicals = 0;
            List<Experiment> experiments = null;
            String error = null;

            @Override protected Void doInBackground() {
                try {
                    students = new StudentDAO().getAll().size();
                    teachers = new TeacherDAO().getAll().size();
                    equipment = new EquipmentDAO().getAll().size();
                    chemicals = new ChemicalDAO().getAll().size();
                    ExperimentDAO edao = new ExperimentDAO();
                    experiments = edao.getAll();
                } catch (SQLException e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override protected void done() {
                if (error != null) {
                    studentsCount.setText("!");
                    JOptionPane.showMessageDialog(OverviewPanel.this, "Failed to load overview: " + error,
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                studentsCount.setText(String.valueOf(students));
                teachersCount.setText(String.valueOf(teachers));
                equipmentCount.setText(String.valueOf(equipment));
                chemicalsCount.setText(String.valueOf(chemicals));

                model.setRowCount(0);
                for (Experiment ex : experiments) {
                    model.addRow(new Object[]{
                            ex.getTitle(), ex.getExpDate(),
                            ex.getStartTime() + (ex.getEndTime() != null && !ex.getEndTime().isBlank() ? " - " + ex.getEndTime() : ""),
                            ex.getTeacherName() == null ? "(unassigned)" : ex.getTeacherName(),
                            ex.getStatus(), ex.getStudentCount() + " student(s)", ex.getColorHex()
                    });
                }
            }
        };
        worker.execute();
    }
}
