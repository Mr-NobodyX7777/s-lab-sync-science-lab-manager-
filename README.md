# S Lab Sync

![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)
![Build](https://github.com/OWNER/REPO/actions/workflows/build.yml/badge.svg)

> Replace `OWNER/REPO` in the badge above with your actual GitHub
> `username/repository-name` once this is pushed, so the build badge resolves.

<p align="center">
  <img src="assets/logo.png" alt="S Lab Sync logo" width="120">
</p>

A desktop application (Java Swing + MySQL) to manage everything happening in a
school/college science lab: students, teachers, equipment (with locations),
chemical inventory (with quantities), and experiments (who is doing what,
supervised by which teacher, and when).

## Table of Contents

- [Master Login](#master-login)
- [MySQL Login](#mysql-login)
- [1. Requirements](#1-requirements)
- [2. Running the app](#2-running-the-app)
- [3. What's inside](#3-whats-inside)
- [4. Database tables created automatically](#4-database-tables-created-automatically)
- [5. Source code](#5-source-code)
- [6. Turning this into a native installer (optional)](#6-turning-this-into-a-native-installer-optional)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)



## Master Login
- **Username:** `Master`
- **Password:** `7777`

These are the defaults the app ships with — entering them on the login
screen unlocks the full dashboard the first time you run it.

You can change them at any time from the login screen itself: click
**"Change master login"** below the login button, confirm your current
password, then set a new username and password. The new login is saved as
a salted SHA-256 hash (not plain text) to `~/.labmanager/master.properties`
on that computer, so it's remembered for future launches.

## MySQL Login
The **first time the app runs on a computer**, it will ask for that computer's
MySQL username and password in a "Connect to MySQL" window (before the Master
login screen even appears). Once it connects successfully, it remembers those
details on that computer, so nobody has to type them again on future launches.

- To connect to a MySQL server on a different machine, change the "HOST"
  field in that window (defaults to `localhost`).
- If the MySQL login for this computer ever changes, open the dashboard and
  click **"Change MySQL Login"** near the bottom of the sidebar (below Log
  out) to update or clear what's remembered.
- Unchecking "Remember these details on this computer" connects for just
  that session without saving anything.
- The saved details live in a small file at `~/.labmanager/db.properties`
  (i.e. inside your user folder) on that computer. The password in that file
  is lightly obfuscated, not strongly encrypted — treat it like any other
  local app config file, not a secure vault.

---

## 1. Requirements

1. **Java 17 or newer** installed on the computer that will run the app.
   Check with: `java -version`
   If you don't have it, install the free "Java Runtime Environment" (JRE)
   or "Java Development Kit" (JDK) from https://adoptium.net/

2. **MySQL Server** running and reachable (typically `localhost`, port
   `3306`), with a MySQL username/password that can create databases and
   tables. You'll be asked to type these in the first time the app runs on
   this computer (see "MySQL Login" above) — they are no longer hard-coded.

   The app automatically creates a database called `lab_management` and all
   required tables the first time you log in — you do NOT need to run any
   SQL yourself.

3. **MySQL Connector/J** (the official JDBC driver jar), because Java doesn't
   talk to MySQL out of the box. This is a one-time download:
   - Go to: https://dev.mysql.com/downloads/connector/j/
   - Choose "Platform Independent", download the `.zip` or `.tar.gz`
   - Extract it, find the file named like `mysql-connector-j-9.x.x.jar`
   - Copy that file into this app's `lib` folder and **rename it to**
     `mysql-connector-j.jar` (so the launcher can find it).

   Folder should look like:
   ```
   SLabSync.jar
   Run_SLabSync.bat
   Run_SLabSync.sh
   lib/
     mysql-connector-j.jar   <-- you add this file
   ```

---

## 2. Running the app

- **Windows:** double-click `Run_SLabSync.bat`
- **Mac / Linux:** open a terminal in this folder and run `./Run_SLabSync.sh`
  (or double click it if your file manager allows running scripts)

The first launch will create the database and tables automatically once you
log in with Master / 7777. On the very first launch, you'll also be asked
whether to add a Desktop shortcut (Windows only).

If you'd rather run it manually from a terminal:
```
java -cp "SLabSync.jar:lib/mysql-connector-j.jar" com.labmanager.App     # Mac/Linux
java -cp "SLabSync.jar;lib/mysql-connector-j.jar" com.labmanager.App     # Windows
```

---

## 3. What's inside

| Screen        | What you can do |
|---------------|------------------|
| Overview      | Live snapshot: total students/teachers/equipment/chemicals, and a table of every experiment with its supervising teacher, status and student count |
| Students      | Add / update / delete student records (name, class/grade, roll no, phone) |
| Teachers      | Add / update / delete teacher records (name, subject, phone, email) |
| Equipment     | Track instruments: name, category, quantity, **location**, condition, last maintenance date |
| Chemicals     | Track chemical inventory: name, formula, **quantity**, unit, location, expiry date, hazard level |
| Experiments   | Schedule an experiment (title, date, time), assign a **supervising teacher**, select **which students** are doing it (multi-select list), and pick a **color tag** for it (8 quick presets or a full custom color picker). That color highlights the experiment's row here and on the Overview screen, so you can spot related sessions at a glance. |

All data is stored permanently in your local MySQL database (`lab_management`),
so it will still be there the next time you open the app.

> **Already used an earlier version of this app?** No problem — the next time
> you log in, it automatically adds the new `color_hex` column to your
> existing `experiments` table. Your existing data is untouched.

## 4. Database tables created automatically

- `teachers`
- `students`
- `equipment`
- `chemicals`
- `experiments`
- `experiment_students` (links students to the experiment they're doing)

## 5. Source code

Full Java source is included in the `src/` folder if you (or a developer)
ever want to customize the app — e.g. change the master password, add new
fields, or change the MySQL credentials. Package structure:

```
src/com/labmanager/
  App.java                    - entry point; loads saved MySQL details or shows setup dialog
  DesktopShortcutHelper.java  - on first launch, offers to create a Desktop shortcut (Windows)
  db/Database.java            - holds the current MySQL connection details (set at runtime)
  db/DbCredentials.java       - simple host/port/user/password holder
  db/DbCredentialsStore.java  - saves/loads the MySQL login to ~/.labmanager/db.properties
  db/MasterCredentialsStore.java - saves/loads the app's own login (salted hash) to ~/.labmanager/master.properties
  db/SchemaInitializer.java   - creates database & tables on first run
  model/                      - plain data classes (Student, Teacher, etc.)
  dao/                        - database read/write logic for each table
  ui/                         - all Swing screens (DB setup, login, dashboard, panels)
```

The master login is no longer hard-coded. `db/MasterCredentialsStore.java`
saves a salted hash of it to `~/.labmanager/master.properties`, and you can
change it from inside the app via the **"Change master login"** link on the
login screen — see [Master Login](#master-login) above. No recompiling
needed for that; you'd only touch this file to change how/where it's stored.

Building from source (no build tool required — plain `javac`):

```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar" -d out @sources.txt
cp src/com/labmanager/logo.png out/com/labmanager/logo.png
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full dev workflow, including
running straight from the compiled classes.

## 6. Turning this into a native installer (optional)

If you'd like a proper installer (.exe/.msi on Windows, .dmg on Mac, .deb on
Linux) instead of a jar + script, you can run this **on your own machine**
(requires a full JDK, 17+, with `jpackage`):

```
jpackage --input . --name "S Lab Sync" --main-jar SLabSync.jar ^
  --main-class com.labmanager.App --type exe --icon logo.ico
```

(use `--type dmg` on Mac, `--type deb` or `--type rpm` on Linux). This bundles
a private Java runtime with the app so end users don't even need Java
installed separately.

## Documentation

The [`docs/`](docs/) folder has more detail if you want it:

- **[Project Report](docs/Project_Report.pdf)** — what the app does, its
  architecture, and what's good about it.
- **[Setup Guide](docs/Setup_Guide.pdf)** — a step-by-step walkthrough for
  getting a fresh machine ready to run the app (Java, MySQL, the connector
  jar).
- **[Architecture Diagram](docs/Architecture_Diagram.pdf)** — a schematic of
  how the app is put together, from launch to the MySQL database.

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md) for how to
build from source and the general workflow.

## License

This project is licensed under the [MIT License](LICENSE).
