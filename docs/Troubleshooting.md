# AdminCore — Troubleshooting Guide

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. Plugin Startup Issues

### Problem: `UnsupportedClassVersionError: class file version 65.0`
- **Cause:** Your server is running on Java 17 or older. AdminCore targets Java 21 (class file version 65.0).
- **Resolution:** Install OpenJDK 21 (Temurin 21 recommended) and update your server start script (`java -jar paper.jar`) to use Java 21.

### Problem: Plugin Fails to Load with `ClassNotFoundException: io.papermc.paper...`
- **Cause:** Server software is vanilla Bukkit, Spigot, or outdated server software.
- **Resolution:** AdminCore requires Paper 1.21.1+ to support modern Adventure components and performance optimizations.

---

## 2. Configuration & YAML Errors

### Problem: `Cannot load configuration file: ParserException / ScannerException`
- **Cause:** Syntax error in one of the `.yml` files (usually tab indentation instead of 2 spaces, or unquoted colons in text).
- **Resolution:**
  1. Validate your YAML file at [yamllint.com](https://www.yamllint.com/).
  2. Use spaces for indentation, never tabs.
  3. Wrap color-coded messages containing special characters in quotes (e.g. `"&c[Warning]: Don't spam"`).
  4. Run `/ac reload` after fixing.

---

## 3. Database & Storage Issues

### Problem: `SQLite database locked` or `SQLException: out of memory`
- **Cause:** Another process has opened `plugins/AdminCore/admincore.db` with an exclusive lock, or the server has inadequate filesystem permissions.
- **Resolution:** Ensure the Minecraft server process has read/write permissions to its working directory. Close any external SQLite viewers like DB Browser for SQLite before launching the server.

---

## 4. Permissions & Command Denial

### Problem: Players receive `You do not have permission to execute this command`
- **Cause:** The player lacks the required permission node.
- **Resolution:** Check the permission node in `docs/Commands-and-Permissions.md`. In LuckPerms, grant the node:
  ```
  lp user <player> permission set admincore.<node> true
  ```
  Or grant staff group permissions as shown in `docs/Installation-Guide.md`.

---

## 5. GUI & Item Safety

### Problem: Players can click items in GUI or items are moved into their inventory
- **Resolution:** AdminCore enforces strict event cancellation across `InventoryClickEvent` and `InventoryDragEvent` using the `AdminCoreHolder` instance. Ensure you are running the official release JAR without external interfering inventory plugins that uncancel click events.

---

## 6. Discord Webhook Issues

### Problem: No Discord notifications appear
- **Cause 1:** `discord.enabled` is set to `false` in `config.yml`.
- **Cause 2:** The webhook URL in `config.yml` is blank or invalid.
- **Cause 3:** Discord webhook rate limit reached or channel was deleted.
- **Verification:**
  1. Run `/ac discord status` to verify the module is enabled.
  2. Run `/ac discord test` to send a test embed. Check console logs for any HTTP response status codes.
