# AdminCore

**All-in-One Minecraft Server Administration, Events, Moderation & Utilities Plugin**

Developed and credited entirely to **LivingYuji**  
Copyright (c) 2026 LivingYuji. All rights reserved.

> "AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji."

---

## Overview

**AdminCore** is an all-in-one administration, staff utilities, custom events, fun effects, cosmetics, moderation, and server management plugin engineered specifically for modern Minecraft Paper servers.

Built with a modular architecture, AdminCore delivers high performance, thread-safe asynchronous database persistence, an intuitive 10-section inventory GUI dashboard, and granular permissions.

---

## Feature Highlights

- **Admin Control Center GUI (`/ac`):** 10-section inventory dashboard with player heads, pagination, sound feedback, and strict anti-tampering protection.
- **Player Utilities:** Player inspector (`/ac info`), inventory viewer (`/ac invsee`), ender chest viewer (`/ac endersee`), vanish, flight, god mode, freeze, teleport tools, and full Staff Mode (`/ac staffmode`) with automatic state backup and restoration.
- **Moderation Suite:** Warnings, temporary/permanent mutes and bans, kicks, punishment history audits, staff notes, and an integrated player reporting queue (`/ac report`, `/ac reports`).
- **Custom Boss System:** Configurable boss templates in `bosses.yml` with dynamic BossBars, proximity detection, and abilities (Shockwave knockback, minion summoning, lightning strikes).
- **Server Events Engine:** 7 supported event types (Last Man Standing, King of the Hill, Sumo, Parkour, Treasure Hunt, Supply Drop, Mob Wave Arena) with a full state machine and automated countdowns.
- **Cosmetics Engine:** Particle trails, ambient orbiting auras, and kill effects with persistent equips.
- **Discord Webhook Integration:** Asynchronous, bot-free webhook notifications for punishments, staff actions, event highlights, and boss defeats.
- **Asynchronous SQLite Storage:** Zero setup required. All data persists reliably in `admincore.db` without blocking the main server thread.
- **Optional Integrations:** Seamless optional hooks into **Vault** (for economy payouts) and **LuckPerms** (for group hierarchy).

---

## System Requirements

- **Server Platform:** Paper 1.21.1 (or compatible Paper 1.21.x build)
- **Java Runtime:** Java Development Kit (JDK) 21 LTS
- **Build System:** Gradle 8.8+

---

## Installation Quick Start

1. Ensure your server runs Java 21 and Paper 1.21.1.
2. Place `AdminCore-1.0.0.jar` into your server's `plugins/` directory.
3. Start the server. AdminCore will generate its configuration files and SQLite database in `plugins/AdminCore/`.
4. Configure permissions using LuckPerms (see [Installation Guide](docs/Installation-Guide.md)).
5. Access the control center in-game with `/ac` or `/admincore`.

---

## Commands & Permissions Overview

Main Commands: `/ac`, `/admincore`  
Permission Prefix: `admincore.`

| Section | Key Commands |
|---|---|
| **General** | `/ac help`, `/ac gui`, `/ac version`, `/ac status`, `/ac reload`, `/ac debug`, `/ac logs` |
| **Player Utilities** | `/ac info`, `/ac invsee`, `/ac endersee`, `/ac tp`, `/ac tphere`, `/ac tpall`, `/ac back`, `/ac heal`, `/ac feed`, `/ac fly`, `/ac god`, `/ac speed`, `/ac vanish`, `/ac freeze`, `/ac spectate`, `/ac effect`, `/ac staffmode` |
| **Fun & Troll** | `/ac troll help`, `/ac troll fakejoin`, `/ac troll fakeleave`, `/ac troll lightning`, `/ac troll launch`, `/ac troll spin`, `/ac troll sound`, `/ac troll title`, `/ac troll particles`, `/ac troll stop`, `/ac troll clear` |
| **Bosses** | `/ac boss list`, `/ac boss info`, `/ac boss spawn`, `/ac boss remove`, `/ac boss setspawn`, `/ac boss reload`, `/ac boss killall` |
| **Events** | `/ac event list`, `/ac event create`, `/ac event start`, `/ac event stop`, `/ac event join`, `/ac event leave`, `/ac event setspawn`, `/ac event setarena` |
| **Cosmetics** | `/ac cosmetics`, `/ac cosmetics trails`, `/ac cosmetics aura`, `/ac cosmetics clear`, `/ac cosmetics give`, `/ac cosmetics reload` |
| **Moderation** | `/ac warn`, `/ac mute`, `/ac unmute`, `/ac kick`, `/ac ban`, `/ac tempban`, `/ac unban`, `/ac history`, `/ac note`, `/ac report`, `/ac reports` |
| **Discord** | `/ac discord test`, `/ac discord status` |

For full command syntax, default access levels, and LuckPerms group configurations, consult [Commands and Permissions Reference](docs/Commands-and-Permissions.md).

---

## Configuration Files

AdminCore organizes its configuration into clean, dedicated YAML files:
- `config.yml`: Core modules, safety settings, storage type, Discord webhook URL.
- `messages.yml`: Localized strings, chat prefix, and color formatting.
- `gui.yml`: Inventory titles, sizes, and filler materials.
- `bosses.yml`: Custom boss templates, stats, abilities, and loot.
- `events.yml`: Server event parameters, player limits, and rewards.
- `cosmetics.yml`: Particle trails and ambient auras.
- `rewards.yml`: Reward bundles, items, Vault money, and console commands.

---

## Building from Source

To compile AdminCore and run the automated test suite:

```cmd
# Run automated tests
gradlew test

# Compile release JAR
gradlew build
```
The compiled plugin JAR will be located at `build/libs/AdminCore-1.0.0.jar`.

---

## Documentation

Full detailed documentation is available in the `docs/` folder:
- [Complete Documentation](docs/AdminCore-Complete-Documentation.md)
- [Installation Guide](docs/Installation-Guide.md)
- [Commands and Permissions](docs/Commands-and-Permissions.md)
- [Configuration Guide](docs/Configuration-Guide.md)
- [Troubleshooting Guide](docs/Troubleshooting.md)
- [Developer API Reference](docs/Developer-API.md)
- [Security Guide](docs/Security-Guide.md)
- [Release Checklist](docs/Release-Checklist.md)

---

## Credits & License

AdminCore is developed and maintained by **LivingYuji**.  
Licensed under the [MIT License](LICENSE).
