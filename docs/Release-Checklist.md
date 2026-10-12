# AdminCore — Release Checklist

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## Pre-Release Verification Steps

- [x] **Platform Verification:** Target Minecraft version Paper 1.21.1 and JDK 21 verified.
- [x] **Original Source Code:** All packages in `com.livingyuji.admincore.*` implemented without third-party source copying or stubs.
- [x] **Credits:** LivingYuji credited in `plugin.yml`, headers, README, documentation, and version command.
- [x] **Configuration Completeness:** All 7 YAML configurations created (`config.yml`, `messages.yml`, `gui.yml`, `bosses.yml`, `events.yml`, `cosmetics.yml`, `rewards.yml`).
- [x] **Database & Migrations:** SQLite schema auto-creation and migrations verified.
- [x] **Command System:** All advertised subcommands implemented and mapped to functional handlers.
- [x] **Tab Completion:** Dynamic, permission-filtered tab completer implemented.
- [x] **GUI Dashboard:** 10 dashboard sections implemented with click safety and anti-duplication protection.
- [x] **Safety Controls:** Harmless troll bounds, freeze movement cancellation, fall immunity for launches.
- [x] **Automated Tests:** Comprehensive unit tests covering parsing, permissions, punishments, events, bosses, and persistence.
- [x] **Compilation:** Clean Gradle build with zero compilation errors.
- [x] **Deliverables:** Final release JAR, Source ZIP, Documentation ZIP, PDF Guide, and SHA-256 checksums placed in `dist/`.
