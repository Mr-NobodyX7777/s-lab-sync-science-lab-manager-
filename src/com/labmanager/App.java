package com.labmanager;

import com.labmanager.db.Database;
import com.labmanager.db.DbCredentials;
import com.labmanager.db.DbCredentialsStore;
import com.labmanager.ui.DbSetupDialog;
import com.labmanager.ui.LoginFrame;
import com.labmanager.ui.UITheme;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UITheme.applyGlobalDefaults();

        SwingUtilities.invokeLater(App::startup);
    }

    //If this computer already has it's MySQL details saved, load them silently and go straight to the login screen. Otherwise, ask for them once via
    // DbSetupDialog - once that succeeds then its saved for every future launch on this same computer, simple :) Mr.NobodyX7777
     
    private static void startup() {
        DesktopShortcutHelper.maybePromptForShortcut();

        DbCredentials saved = DbCredentialsStore.load();
        if (saved != null) {
            Database.configure(saved.host, saved.port, saved.user, saved.password);
            new LoginFrame().setVisible(true);
            return;
        }

        DbSetupDialog setup = new DbSetupDialog(null);
        setup.setVisible(true); 

        if (setup.isSucceeded()) {
            new LoginFrame().setVisible(true);
        } else {
            System.exit(0); // when the bored and tired user closes the setup dialog without connecting
        }
    }
}
