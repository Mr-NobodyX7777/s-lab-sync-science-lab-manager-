package com.labmanager.db;

//Empty holder for the MySQL connection details a person types into {@code DbSetupDialog}.
// it's just a convenient way to pass them around as a single object --   --   ;)  Mr.NobodyX7777
//                                                                    \___/
public class DbCredentials {
    public final String host;
    public final String port;
    public final String user;
    public final String password;

    public DbCredentials(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }
}
