package com.labmanager.ui;

import com.labmanager.db.Database;
import com.labmanager.db.DbCredentials;
import com.labmanager.db.DbCredentialsStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Asks for this computer's MySQL host / port / username / password.
 *
 * Shown automatically the first time the app runs on a computer (no saved
 * file yet), and also reachable later from the dashboard via
 * "Change MySQL Login" so the person can update or clear what's remembered.
 */
public class DbSetupDialog extends JDialog {

    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("3306");
    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JCheckBox rememberBox = new JCheckBox("Remember these details on this computer", true);
    private final JLabel statusLabel = new JLabel(" ");

    private boolean succeeded = false;

    public DbSetupDialog(Window owner) {
        super(owner, "Connect to MySQL", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(440, 600);
        setMinimumSize(new Dimension(400, 520));
        setLocationRelativeTo(owner);

        JPanel root = UITheme.meshBackground();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        JButton closeBtn = UITheme.windowControlButton("close", true);
        closeBtn.addActionListener(e -> dispose());
        JPanel closeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeRow.setOpaque(false);
        closeRow.add(closeBtn);
        GridBagConstraints topGc = new GridBagConstraints();
        topGc.gridx = 0; topGc.gridy = 0; topGc.anchor = GridBagConstraints.NORTHEAST;
        topGc.weightx = 1; topGc.weighty = 0; topGc.fill = GridBagConstraints.HORIZONTAL;
        topGc.insets = new Insets(10, 0, 0, 10);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 460));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JLabel icon = new JLabel("\uD83D\uDD10", SwingConstants.CENTER); // lock emoji
        icon.setFont(icon.getFont().deriveFont(40f));
        gc.gridy = 0; gc.insets = new Insets(0, 4, 8, 4);
        card.add(icon, gc);

        JLabel title = new JLabel("Connect to MySQL", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_DARK);
        gc.gridy = 1; gc.insets = new Insets(0, 4, 2, 4);
        card.add(title, gc);

        JLabel sub = new JLabel("<html><body style='text-align:center;width:260px'>Enter the MySQL login for this computer. It will be saved here so you won't be asked again.</body></html>", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_SUB);
        sub.setForeground(UITheme.TEXT_MUTED);
        gc.gridy = 2; gc.insets = new Insets(0, 4, 16, 4);
        card.add(sub, gc);

        JPanel hostPortRow = new JPanel(new GridLayout(1, 2, 8, 0));
        hostPortRow.setOpaque(false);
        JPanel hostCol = new JPanel(new BorderLayout());
        hostCol.setOpaque(false);
        JLabel hostLbl = UITheme.label("HOST");
        hostCol.add(hostLbl, BorderLayout.NORTH);
        UITheme.styleField(hostField);
        hostCol.add(hostField, BorderLayout.CENTER);
        JPanel portCol = new JPanel(new BorderLayout());
        portCol.setOpaque(false);
        portCol.add(UITheme.label("PORT"), BorderLayout.NORTH);
        UITheme.styleField(portField);
        portCol.add(portField, BorderLayout.CENTER);
        hostPortRow.add(hostCol);
        hostPortRow.add(portCol);
        gc.gridy = 3; gc.insets = new Insets(6, 4, 12, 4);
        card.add(hostPortRow, gc);

        gc.gridy = 4; gc.insets = new Insets(0, 4, 2, 4);
        card.add(UITheme.label("MYSQL USERNAME"), gc);
        gc.gridy = 5; gc.insets = new Insets(0, 4, 12, 4);
        UITheme.styleField(userField);
        card.add(userField, gc);

        gc.gridy = 6; gc.insets = new Insets(0, 4, 2, 4);
        card.add(UITheme.label("MYSQL PASSWORD"), gc);
        gc.gridy = 7; gc.insets = new Insets(0, 4, 10, 4);
        UITheme.styleField(passField);
        card.add(passField, gc);

        rememberBox.setOpaque(false);
        rememberBox.setFont(UITheme.FONT_LABEL);
        rememberBox.setForeground(UITheme.TEXT_MUTED);
        gc.gridy = 8; gc.insets = new Insets(0, 2, 6, 4);
        card.add(rememberBox, gc);

        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_LABEL);
        gc.gridy = 9; gc.insets = new Insets(0, 4, 8, 4);
        card.add(statusLabel, gc);

        JButton connectBtn = UITheme.primaryButton("SAVE & CONNECT");
        gc.gridy = 10; gc.insets = new Insets(6, 4, 4, 4);
        card.add(connectBtn, gc);

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

        connectBtn.addActionListener(this::attemptConnect);
        passField.addActionListener(this::attemptConnect);
    }

    /** Pre-fills the fields with previously-saved values (used when editing an existing setup). */
    public void prefill(DbCredentials creds) {
        if (creds == null) return;
        hostField.setText(creds.host);
        portField.setText(creds.port);
        userField.setText(creds.user);
        passField.setText(creds.password);
    }

    private void attemptConnect(ActionEvent e) {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (user.isEmpty()) {
            statusLabel.setText("Please enter a MySQL username.");
            return;
        }

        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setText("Testing connection...");
        setEnabledAll(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String error = null;
            @Override protected Void doInBackground() {
                try {
                    Database.testConnection(host, port, user, pass);
                } catch (Exception ex) {
                    error = ex.getMessage();
                }
                return null;
            }
            @Override protected void done() {
                setEnabledAll(true);
                if (error != null) {
                    statusLabel.setForeground(UITheme.DANGER);
                    statusLabel.setText("<html><body style='width:260px'>Connection failed: " + error + "</body></html>");
                    return;
                }

                Database.configure(host, port, user, pass);

                if (rememberBox.isSelected()) {
                    try {
                        DbCredentialsStore.save(new DbCredentials(host, port, user, pass));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(DbSetupDialog.this,
                                "Connected, but could not save these details for next time:\n" + ex.getMessage(),
                                "Couldn't save", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    DbCredentialsStore.clear();
                }

                succeeded = true;
                dispose();
            }
        };
        worker.execute();
    }

    private void setEnabledAll(boolean enabled) {
        hostField.setEnabled(enabled);
        portField.setEnabled(enabled);
        userField.setEnabled(enabled);
        passField.setEnabled(enabled);
        rememberBox.setEnabled(enabled);
    }

    /** True once the person has successfully connected and closed the dialog. */
    public boolean isSucceeded() {
        return succeeded;
    }
}
