# Contributing to S Lab Sync

Thanks for considering a contribution! This is a small Swing + MySQL desktop
app, so the workflow is intentionally simple.

## Getting set up

1. Follow the **Requirements** and **Running the app** sections of the
   [README](README.md) to get Java, MySQL, and the connector jar in place.
2. Fork the repo and clone your fork.
3. Compile the source directly with `javac` (no build tool is required):

   ```bash
   find src -name "*.java" > sources.txt
   javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar" -d out @sources.txt
   cp -r src/com/labmanager/logo.png out/com/labmanager/logo.png
   ```
4. Run it straight from the compiled classes to test your changes:

   ```bash
   java -cp "out:lib/mysql-connector-j.jar" com.labmanager.App      # Mac/Linux
   java -cp "out;lib/mysql-connector-j.jar" com.labmanager.App      # Windows
   ```

## Project layout

See the **Source code** section of the README for what lives in each package
(`model/`, `dao/`, `db/`, `ui/`).

## Making changes

- Keep new UI code consistent with the existing look — shared colors, fonts,
  and reusable components all live in `UITheme.java` rather than being
  redefined per-screen.
- If you add a new data field or table, remember `SchemaInitializer.java`
  handles first-run schema creation and lightweight migrations (see how the
  `color_hex` column was added for an example).
- Please don't commit the MySQL Connector/J jar, personal `db.properties`
  files, or compiled `.class` files — see `.gitignore`.

## Submitting a change

Open a pull request describing what changed and why. For anything that
touches the database schema or the login flow, please mention how you tested
it (fresh database vs. an existing one).
