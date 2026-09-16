package com.labmanager.ui;

import com.labmanager.db.Database;
import com.labmanager.db.DbCredentials;
import com.labmanager.db.DbCredentialsStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel sidebar = UITheme.glassPanel(26);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private static final String OVERVIEW = "OVERVIEW";
    private static final String STUDENTS = "STUDENTS";
    private static final String TEACHERS = "TEACHERS";
    private static final String EQUIPMENT = "EQUIPMENT";
    private static final String CHEMICALS = "CHEMICALS";
    private static final String EXPERIMENTS = "EXPERIMENTS";

    private OverviewPanel overviewPanel;

    public DashboardFrame() {
        setTitle("S Lab Sync - Master Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon appIcon = UITheme.logoIcon(64);
        if (appIcon != null) setIconImage(appIcon.getImage());
        setUndecorated(true);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JPanel root = UITheme.meshBackground();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);

        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 28));

        overviewPanel = new OverviewPanel();
        contentPanel.add(overviewPanel, OVERVIEW);
        contentPanel.add(new StudentsPanel(), STUDENTS);
        contentPanel.add(new TeachersPanel(), TEACHERS);
        contentPanel.add(new EquipmentPanel(), EQUIPMENT);
        contentPanel.add(new ChemicalsPanel(), CHEMICALS);
        contentPanel.add(new ExperimentsPanel(), EXPERIMENTS);

        root.add(contentPanel, BorderLayout.CENTER);
        showCard(OVERVIEW);
    }

    private JPanel buildTitleBar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(10, 16, 0, 12));

        JPanel bar = UITheme.glassPanel(20);
        bar.setLayout(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 44));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 6));

        JLabel titleLabel = new JLabel("  S Lab Sync", UITheme.logoIcon(20), SwingConstants.LEFT);
        titleLabel.setIconTextGap(8);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setFont(UITheme.FONT_SIDEBAR.deriveFont(Font.BOLD, 14f));
        bar.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);
        JButton minBtn = UITheme.windowControlButton("min", false);
        JButton maxBtn = UITheme.windowControlButton("max", false);
        JButton closeBtn = UITheme.windowControlButton("close", true);

        minBtn.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));
        maxBtn.addActionListener(e -> toggleMaximize());
        closeBtn.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        controls.add(minBtn);
        controls.add(maxBtn);
        controls.add(closeBtn);
        bar.add(controls, BorderLayout.EAST);

        Point[] dragStart = new Point[1];
        bar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) toggleMaximize();
            }
        });
        bar.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart[0] == null || getExtendedState() == JFrame.MAXIMIZED_BOTH) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragStart[0].x, loc.y + e.getY() - dragStart[0].y);
            }
        });

        outer.add(bar, BorderLayout.CENTER);
        return outer;
    }

    private void toggleMaximize() {
        setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
    }

    private JPanel buildSidebar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 8));

        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(226, 0));
        sidebar.setBorder(new EmptyBorder(4, 8, 12, 8));

        JLabel logo = new JLabel("  S Lab Sync", UITheme.logoIcon(30), SwingConstants.LEFT);
        logo.setIconTextGap(10);
        logo.setFont(UITheme.FONT_TITLE.deriveFont(19f));
        logo.setForeground(UITheme.TEXT_DARK);
        logo.setBorder(BorderFactory.createEmptyBorder(22, 8, 24, 6));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        addNavButton("\u2302  Overview", OVERVIEW);
        addNavButton("\uD83D\uDC65  Students", STUDENTS);
        addNavButton("\uD83C\uDF93  Teachers", TEACHERS);
        addNavButton("\uD83D\uDD2C  Equipment", EQUIPMENT);
        addNavButton("\u2697  Chemicals", CHEMICALS);
        addNavButton("\uD83D\uDCC5  Experiments", EXPERIMENTS);

        sidebar.add(Box.createVerticalGlue());

        JButton dbBtn = UITheme.sidebarButton("  \uD83D\uDD10  Change MySQL Login", false);
        dbBtn.setFont(UITheme.FONT_SIDEBAR.deriveFont(13f));
        dbBtn.addActionListener(e -> changeMysqlLogin());
        sidebar.add(dbBtn);

        JButton themeBtn = UITheme.sidebarButton(UITheme.darkMode ? "  \u2600  Light Mode" : "  \uD83C\uDF19  Dark Mode", false);
        themeBtn.setFont(UITheme.FONT_SIDEBAR.deriveFont(13f));
        themeBtn.addActionListener(e -> toggleDarkMode());
        sidebar.add(themeBtn);

        JButton exitBtn = UITheme.sidebarButton("  \u23FB  Log out", false);
        exitBtn.setForeground(UITheme.DANGER);
        exitBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        sidebar.add(exitBtn);

        wrapper.add(sidebar, BorderLayout.CENTER);
        return wrapper;
    }

    private void addNavButton(String text, String cardName) {
        JButton btn = UITheme.sidebarButton(text, cardName.equals(OVERVIEW));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> showCard(cardName));
        navButtons.put(cardName, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    /** Lets the person update (or clear) the MySQL login remembered on this computer. */
    private void changeMysqlLogin() {
        DbSetupDialog dialog = new DbSetupDialog(this);
        dialog.prefill(new DbCredentials(Database.getHost(), Database.getPort(), Database.getUser(), ""));
        dialog.setVisible(true); // modal - blocks here until closed

        if (dialog.isSucceeded()) {
            JOptionPane.showMessageDialog(this,
                    "MySQL login updated" +
                            (DbCredentialsStore.exists() ? " and saved for this computer." : " for this session only."),
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void toggleDarkMode() {
        UITheme.setDarkMode(!UITheme.darkMode);
        dispose();
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            UITheme.setNavSelected(entry.getValue(), entry.getKey().equals(name));
        }
        if (name.equals(OVERVIEW) && overviewPanel != null) {
            overviewPanel.refresh();
        }
    }
}
