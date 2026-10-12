# AdminCore — Configuration Guide

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. Overview of Configuration Files

AdminCore maintains a clean, separated configuration hierarchy located in `plugins/AdminCore/`:

| File | Purpose | Safe to Reload with `/ac reload`? |
|---|---|---|
| `config.yml` | Core module toggles, database settings, safety parameters, Discord webhook | Yes |
| `messages.yml` | Localization, chat prefixes, message templates, color formats | Yes |
| `gui.yml` | GUI titles, sizes, filler glass materials | Yes |
| `bosses.yml` | Custom boss definitions, attributes, abilities, and rewards | Yes |
| `events.yml` | Server event types, minimum/maximum player limits, locations, rewards | Yes |
| `cosmetics.yml` | Particle trail, ambient aura, and kill effect definitions | Yes |
| `rewards.yml` | Reward bundles, item rewards, Vault economy payouts, console commands | Yes |

---

## 2. config.yml Reference

### Root Settings
```yaml
language: en            # String: Language code (default: en)
debug-mode: false       # Boolean: Enables verbose debug messages in console
```

### Storage Section
```yaml
storage:
  type: SQLITE          # Enum [SQLITE]: Storage backend (default: SQLITE)
```

### Modules Section
Allows selectively toggling individual AdminCore subsystems:
```yaml
modules:
  player-utilities: true # Enables fly, heal, vanish, freeze, teleport commands
  moderation: true       # Enables warn, mute, kick, ban, reports, history
  troll: true            # Enables harmless fun commands (lightning, spin, launch)
  bosses: true           # Enables custom boss manager and ticking loop
  events: true           # Enables server events (LMS, KOTH, Sumo, etc.)
  cosmetics: true        # Enables particle cosmetics and auras
  rewards: true          # Enables reward bundle processing
```

### Discord Integration Section
```yaml
discord:
  enabled: false                                    # Set to true to activate webhook notifications
  webhook-url: "https://discord.com/api/webhooks/..." # Full private Discord webhook URL
  notifications:
    punishments: true    # Post bans, mutes, kicks, warns
    staff-actions: true  # Post major staff actions
    events: true         # Post event winners and starts
    bosses: true         # Post boss defeats
    system: true         # Post plugin enable/disable
```

### Safety Section
```yaml
safety:
  max-troll-duration-seconds: 15 # Integer: Maximum runtime for continuous pranks
  enable-destructive-pranks: false # Boolean: Disables griefing or inventory wipe effects
```

---

## 3. bosses.yml Reference

Define custom bosses under the `bosses` root key:
```yaml
bosses:
  infernal_titan:
    display-name: "&4&lInfernal Titan" # String: Custom entity name & boss bar title
    entity-type: "WITHER_SKELETON"       # Bukkit EntityType enum name
    health: 500.0                       # Double: Maximum boss health
    damage: 15.0                        # Double: Base attack damage
    speed: 0.35                         # Double: Movement speed
    bar-color: "RED"                    # BossBar BarColor: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
    bar-style: "NOTCHED_10"             # BossBar BarStyle: SOLID, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
    abilities:                          # List of abilities: knockback, minions, strike
      - "knockback"
      - "minions"
      - "strike"
    reward-commands:                    # List of console commands run on defeat (%player% placeholder)
      - "give %player% netherite_ingot 2"
      - "xp add %player% 500"
```

---

## 4. events.yml Reference

Define server events under the `events` root key:
```yaml
events:
  lms_main:
    type: "LMS"                         # Supported types: LMS, KOTH, SUMO, PARKOUR, TREASURE_HUNT, SUPPLY_DROP, MOB_ARENA
    display-name: "&c&lLast Man Standing" # Display name in announcements
    min-players: 2                      # Minimum participants required to start
    max-players: 50                     # Maximum player capacity
    reward-commands:                    # Console commands executed for winner (%player%)
      - "give %player% diamond_block 1"
```

---

## 5. cosmetics.yml Reference

```yaml
cosmetics:
  flame_trail:
    name: "&6Flame Walker"             # Display name in selector GUI
    type: "TRAIL"                      # CosmeticType: TRAIL, AURA, KILL_EFFECT, JOIN_EFFECT, CELEBRATION
    particle: "FLAME"                  # Bukkit Particle enum
    icon: "BLAZE_POWDER"               # Bukkit Material enum for GUI icon
    permission: "admincore.cosmetics.flame_trail" # Required permission node
```
