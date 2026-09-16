package com.labmanager.ui;

import com.labmanager.db.SchemaInitializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class LoginFrame extends JFrame {

    private static final String MASTER_USER = "Master";
    private static final String MASTER_PASS = "7777";

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("S Lab Sync - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(440, 560);
        setMinimumSize(new Dimension(400, 480));
        setLocationRelativeTo(null);
        ImageIcon logo = UITheme.logoIcon(64);
        if (logo != null) setIconImage(logo.getImage());

        JPanel root = UITheme.meshBackground();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        JButton closeBtn = UITheme.windowControlButton("close", true);
        closeBtn.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        JPanel closeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeRow.setOpaque(false);
        closeRow.add(closeBtn);
        GridBagConstraints topGc = new GridBagConstraints();
        topGc.gridx = 0; topGc.gridy = 0; topGc.anchor = GridBagConstraints.NORTHEAST;
        topGc.weightx = 1; topGc.weighty = 0; topGc.fill = GridBagConstraints.HORIZONTAL;
        topGc.insets = new Insets(10, 0, 0, 10);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 420));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JLabel icon = new JLabel(logo, SwingConstants.CENTER);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        gc.gridy = 0; gc.insets = new Insets(0, 4, 10, 4);
        card.add(icon, gc);

        JLabel title = new JLabel("S Lab Sync", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_DARK);
        gc.gridy = 1; gc.insets = new Insets(0, 4, 2, 4);
        card.add(title, gc);

        JLabel sub = new JLabel("Enter Username and Password", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_SUB);
        sub.setForeground(UITheme.TEXT_MUTED);
        gc.gridy = 2; gc.insets = new Insets(0, 4, 22, 4);
        card.add(sub, gc);

        gc.gridy = 3; gc.insets = new Insets(6, 4, 2, 4);
        card.add(UITheme.label("USERNAME"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 4, 12, 4);
        UITheme.styleField(userField);
        card.add(userField, gc);

        gc.gridy = 5; gc.insets = new Insets(6, 4, 2, 4);
        card.add(UITheme.label("PASSWORD"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 4, 6, 4);
        UITheme.styleField(passField);
        card.add(passField, gc);

        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_LABEL);
        gc.gridy = 7; gc.insets = new Insets(0, 4, 10, 4);
        card.add(statusLabel, gc);

        JButton loginBtn = UITheme.primaryButton("UNLOCK DASHBOARD");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        gc.gridy = 8; gc.insets = new Insets(10, 4, 4, 4);
        card.add(loginBtn, gc);

        JLabel hint = new JLabel("", SwingConstants.CENTER);
        hint.setFont(UITheme.FONT_LABEL);
        hint.setForeground(UITheme.TEXT_MUTED);
        gc.gridy = 9; gc.insets = new Insets(14, 4, 0, 4);
        card.add(hint, gc);

        root.add(closeRow, topGc);
        GridBagConstraints cardGc = new GridBagConstraints();
        cardGc.gridx = 0; cardGc.gridy = 1; cardGc.weightx = 1; cardGc.weighty = 1;
        cardGc.insets = new Insets(0, 0, 40, 0);
        root.add(card, cardGc);

        Point[] dragStart = new Point[1];
        MouseAdapter dragHandler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragStart[0].x, loc.y + e.getY() - dragStart[0].y);
            }
        };
        root.addMouseListener(dragHandler);
        root.addMouseMotionListener(dragHandler);

        loginBtn.addActionListener(this::attemptLogin);
        passField.addActionListener(this::attemptLogin);
    }

    private void attemptLogin(ActionEvent e) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (!user.equals(MASTER_USER) || !pass.equals(MASTER_PASS)) {
            statusLabel.setText("Invalid username or password.");
            return;
        }

        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setText("Connecting to database...");
        setEnabledAll(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String error = null;
            @Override protected Void doInBackground() {
                try {
                    SchemaInitializer.initialize();
                } catch (Exception ex) {
                    error = ex.getMessage();
                }
                return null;
            }
            @Override protected void done() {
                setEnabledAll(true);
                if (error != null) {
                    statusLabel.setForeground(UITheme.DANGER);
                    statusLabel.setText("<html><body style='width:260px'>DB connection failed: " + error + "</body></html>");
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Could not connect to MySQL.\n\n" + error +
                            "\n\nCheck that MySQL is running on localhost:3306\n" +
                            "and that the mysql-connector jar is in the 'lib' folder.",
                            "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
                SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
            }
        };
        worker.execute();
    }

    private void setEnabledAll(boolean enabled) {
        userField.setEnabled(enabled);
        passField.setEnabled(enabled);
    }
}// this whole thing is too boring for me... I have an Idea! I'm gonna make my UI  using  AI ;) Mr.NobodyX7777
 //                                                                             \ get it ?/
