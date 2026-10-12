# Contributing to AdminCore

Thank you for your interest in contributing to **AdminCore**!

AdminCore is an original Minecraft plugin developed and maintained by **LivingYuji**.

## Project Guidelines

1. **Credits and Ownership**:
   - All original AdminCore source code, project documentation, and assets are credited to **LivingYuji**.
   - Do not claim ownership of external libraries, PaperMC API, Vault, or LuckPerms.

2. **Code Standards**:
   - Code must target Java 21+ and the Paper API for Minecraft 1.21.1+.
   - Keep classes modular, cohesive, and well-documented.
   - Do not block the Minecraft server main thread with synchronous database queries, network requests, or long computations.
   - Avoid using internal NMS or brittle reflection; rely on supported Paper/Bukkit APIs.

3. **Submitting Changes**:
   - Write automated unit tests for any new utilities, parsing logic, or state transitions.
   - Verify that all Gradle tests pass cleanly before submitting changes:
     ```cmd
     gradlew test
     ```
   - Ensure plugin configurations and permissions are documented in `docs/Commands-and-Permissions.md` and `docs/Configuration-Guide.md`.
