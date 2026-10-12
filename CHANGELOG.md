# Changelog

All notable changes to **AdminCore** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Developer Credit:** LivingYuji  
**Copyright:** (c) 2026 LivingYuji. All rights reserved.

---

## [1.0.0] - 2026-10-12

### Added
- **Core Framework & Plugin Architecture**:
  - Modular subsystem design with dedicated managers and lifecycle handlers.
  - Granular `admincore.*` permission tree.
  - Multi-file YAML configuration engine (`config.yml`, `messages.yml`, `gui.yml`, `bosses.yml`, `events.yml`, `cosmetics.yml`, `rewards.yml`).
  - Thread-safe asynchronous SQLite storage layer with automatic schema migrations.
  - Color-formatting engine supporting color codes, hex colors, and custom prefix tags.

- **Main Command System**:
  - Unified `/ac` and `/admincore` root commands with tab completion and argument validation.
  - Subcommands for help, GUI dashboard, version, status, module toggles, configuration reload, and debugging.

- **Admin Control Center GUI**:
  - 10 dashboard sections: Player Management, Moderation Center, Fun & Troll Tools, Boss Manager, Event Manager, Cosmetics Manager, Rewards, Server Tools, Logs & Statistics, Plugin Information.
  - Pagination, search, skull heads, sound feedback, cancellation of invalid inventory manipulations (drag, shift-click, number swaps).
  - Confirmation modals for destructive actions.

- **Player Utilities**:
  - Player info inspector (`/ac info`), inventory viewer (`/ac invsee`), ender chest viewer (`/ac endersee`).
  - Safe teleports (`/ac tp`, `/ac tphere`, `/ac tpall`) and previous location rewind (`/ac back`).
  - Utility commands: `/ac heal`, `/ac feed`, `/ac fly`, `/ac god`, `/ac speed`, `/ac vanish`, `/ac freeze`, `/ac spectate`, `/ac effect`.
  - Comprehensive staff mode (`/ac staffmode`) preserving original inventory, gamemode, flight, and locations.

- **Fun and Troll Tools**:
  - Harmless, permission-controlled troll actions: `/ac troll fakejoin`, `/ac troll fakeleave`, `/ac troll lightning`, `/ac troll launch`, `/ac troll spin`, `/ac troll sound`, `/ac troll title`, `/ac troll particles`.
  - Safety controls: `/ac troll stop`, `/ac troll clear` to immediately halt ongoing effects.
  - Non-destructive prank presets with configurable cooldowns and duration bounds.

- **Custom Boss System**:
  - Configurable boss framework with dynamic BossBars, phase shifts, and abilities.
  - Abilities: Area Knockback, Minion Summoning, Projectile Barrage, Damage Resistance, Telegraphed Ground Strikes.
  - Management commands: `/ac boss list`, `info`, `spawn`, `remove`, `setspawn`, `reload`, `killall`.
  - Clean entity tracking and restart-safe persistence.

- **Event Management System**:
  - Full state machine: `SETUP` -> `WAITING` -> `RUNNING` -> `ENDING` -> `FINISHED`.
  - 7 Event Types: Last Man Standing (LMS), King of the Hill (KOTH), Sumo, Parkour Race, Treasure Hunt, Supply Drop, Mob Wave Arena.
  - Comprehensive lifecycle management: lobby countdowns, arena boundaries, automated scoring, rewards, leaderboards, cleanup.

- **Cosmetics Engine**:
  - Permission-aware cosmetic trails, ambient auras, join effects, kill effects, and celebrations.
  - In-game cosmetics selector GUI and persistent player unlock tracking.

- **Moderation Suite**:
  - Punishment management: warnings, mutes, kicks, temporary/permanent bans, unbans.
  - Player history audit (`/ac history`), staff notes (`/ac note`), player report system (`/ac report`, `/ac reports`).
  - Active punishment enforcement with expiration checks.

- **Rewards and Economy**:
  - Reward tables supporting items, console commands, and optional Vault economy payouts.
  - Claim tracking and duplicate reward prevention.

- **Discord Webhook Integration**:
  - Asynchronous HTTP webhook sender for punishments, staff actions, event highlights, and boss defeats.
  - Zero sensitive token leaks in console logs.

- **Audit Logging and Diagnostics**:
  - Persistent staff action logs, search filter (`/ac logs search`), and export (`/ac logs export`).
