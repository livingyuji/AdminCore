# AdminCore — Complete Technical & User Documentation

**Version:** 1.0.0  
**Author & Developer:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** "AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji."

---

## 1. System Architecture Overview

AdminCore is designed as an all-in-one administration, staff utilities, custom events, fun effects, cosmetics, moderation, and server management plugin for Minecraft servers running Paper 1.21.1+ on Java 21+.

The plugin is structured with clean separation of concerns:
```
com.livingyuji.admincore/
├── AdminCore.java           # Central lifecycle orchestrator and dependency injector
├── command/                 # Modular subcommand routing with tab completion
├── config/                  # Multi-file YAML loading, caching, and colorization
├── gui/                     # Secure inventory dashboard engine with anti-tampering
├── player/                  # Player state management, history, and staff mode
├── moderation/              # Warnings, mutes, kicks, bans, notes, reports, and listeners
├── troll/                   # Harmless fun pranks with safety limits and quick cleanup
├── boss/                    # Configurable custom bosses, boss bars, abilities, loot
├── event/                   # Extensible server events with full state machine
├── cosmetics/               # Particle trails, ambient auras, kill effects, and selector GUI
├── rewards/                 # Reward tables, items, commands, and Vault economy hook
├── discord/                 # Asynchronous HTTP Discord webhook notification engine
├── storage/                 # Asynchronous SQLite storage layer with schema migrations
├── logging/                 # Persistent staff audit logging and disk export
├── integration/             # Graceful optional hooks for Vault and LuckPerms
└── util/                    # Parsing (durations, locations, colors) and time utilities
```

---

## 2. Core Modules & Functionality

### 2.1 Player Utilities Module
Provides staff with rapid, safe player management tools:
- **State Preservation:** When a staff member enters Staff Mode (`/ac staffmode`), their previous inventory, armor, flight state, location, and gamemode are backed up. They receive staff tools (Teleport Compass, Player Inspector, Freeze Wand, Admin Dashboard) and automatic vanish. Exiting restores their original state cleanly.
- **Vanish (`/ac vanish`):** Hides the staff member from non-staff players using Bukkit's player visibility API. Also hides them from newly joined players.
- **God Mode & Flight (`/ac god`, `/ac fly`):** Toggles damage immunity and flight capability.
- **Freeze (`/ac freeze <player>`):** Restricts target player movement (x/z coordinates), cancels attacks, and protects from damage while under investigation.
- **Location Rewind (`/ac back`):** Automatically logs death locations and previous teleport origins for seamless staff navigation.
- **Inventory & Ender Chest Inspection (`/ac invsee`, `/ac endersee`):** Opens target player storage directly.

### 2.2 Moderation Suite
- **Punishments:** Full support for warnings, temporary/permanent mutes, kicks, and temporary/permanent bans.
- **Active Memory Cache:** Active mutes and bans are cached in memory for sub-millisecond chat and login checks, with automatic expiration handling.
- **Player History & Staff Notes:** Records all historical punishments and private staff notes persistently in SQLite.
- **Player Reporting System:** Players can report rule-breakers with `/ac report <player> <reason>`, which broadcasts to online staff and enters the reports management queue.

### 2.3 Custom Boss Framework
- Fully configurable in `bosses.yml`.
- Features real-time `BossBar` tracking with automatic player proximity detection (40-block radius).
- Supported abilities:
  - **Shockwave Knockback:** Knocks nearby players backwards with customizable impulse.
  - **Minion Summoning:** Spawns baby zombies as fighting minions.
  - **Ground Strike:** Calls down visual lightning strikes that inflict burst damage on surrounding players.
- Clean entity cleanup on reload or server shutdown. Defeat awards configurable console commands and item loot.

### 2.4 Server Events Engine
Supports structured server competitions using a strict state machine:
`SETUP` -> `WAITING` -> `RUNNING` -> `ENDING` -> `FINISHED`

Implemented event types:
1. **Last Man Standing (LMS):** Elimination upon death; last survivor claims the prize.
2. **King of the Hill (KOTH):** Players battle within the hill radius. Occupying the hill earns points (+1/sec). First player to reach 60 points wins.
3. **Sumo:** Knockout arena; falling out of the ring or beyond radius eliminates participants.
4. **Parkour Race:** Race to reach the target finish coordinate.
5. **Treasure Hunt:** Scavenger hunt for hidden objectives.
6. **Supply Drop:** Timed crate drops with interactive claims.
7. **Mob Wave Arena:** Wave survival challenge with entity caps and automatic cleanup.

### 2.5 Cosmetics Engine
- Offers particle trails (`Flame Walker`, `Heart Path`, `Void Rift`), ambient orbiting auras (`Angel Halo`, `Dragon Breath`), and kill effects (`Thunder Strike`).
- Ticked efficiently on background intervals (every 4 ticks) to maintain high server tick rates.
- Equips are saved persistently in SQLite and restored upon player re-join.

### 2.6 Discord Webhook Integration
- Dispatches formatted rich embeds asynchronously without blocking the server thread.
- Zero bot setup needed—simply paste your Discord webhook URL in `config.yml`.
- Separate toggle switches for punishments, staff actions, event results, and boss defeats.

### 2.7 Storage & Database
- Default SQLite engine requires zero external configuration. Database file created at `plugins/AdminCore/admincore.db`.
- Automatic table generation and migrations for punishments, staff notes, player reports, cosmetics, audit logs, and event statistics.
- All write operations run on background threads via `DatabaseService`.

---

## 3. In-Game Admin Control Center (GUI)

The Admin Control Center is accessible via `/ac` or `/ac gui`:
1. **Player Management:** Browse online players with skull heads, health, ping, and open quick-action menus.
2. **Moderation Center:** Manage warnings, active mutes/bans, review open reports, and toggle Staff Mode.
3. **Fun & Troll Tools:** Harmless pranks, fake announcements, launches, visual lightning, and emergency stop.
4. **Boss Manager:** List and spawn custom bosses at your location, or remove active bosses.
5. **Event Manager:** Review event types, start lobbies, or stop running matches.
6. **Cosmetics Selector:** Equip or clear particle trails and ambient auras.
7. **Rewards & Economy:** Review configured reward tables and Vault payouts.
8. **Server Tools:** Reload configurations, toggle debug mode, and monitor allocated memory.
9. **Logs & Statistics:** View recent audit actions or export logs to disk.
10. **Plugin Information:** Developer credits to LivingYuji and version metadata.
