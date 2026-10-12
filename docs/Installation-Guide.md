# AdminCore — Installation & Setup Guide

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. System Requirements

Before installing AdminCore, ensure your hosting environment satisfies these requirements:

| Component | Minimum Requirement | Recommended |
|---|---|---|
| **Java Runtime** | Java Development Kit (JDK) 21 | Eclipse Adoptium Temurin 21 LTS |
| **Server Software** | Paper 1.21.1 | Latest Paper 1.21.x build |
| **Memory** | 2 GB dedicated RAM | 4 GB+ dedicated RAM |
| **Storage Engine** | Built-in SQLite (no setup needed) | Built-in SQLite |
| **Optional Plugins** | Vault (for economy), LuckPerms | LuckPerms 5.4+, EssentialsX Economy |

---

## 2. Step-by-Step Installation

### Step 1: Verify Java 21 Installation
Ensure your server startup script executes with Java 21. Verify from terminal or console:
```cmd
java -version
```
Output must state `openjdk version "21.x.x"` or higher.

### Step 2: Download the Release JAR
Obtain `AdminCore-1.0.0.jar` from the verified `dist/` directory.

### Step 3: Stop the Minecraft Server
Always cleanly stop your Paper server before placing new plugins:
```cmd
stop
```

### Step 4: Install Plugin JAR
Move or copy `AdminCore-1.0.0.jar` into your server's `plugins/` directory:
```
server/
├── paper.jar
├── server.properties
└── plugins/
    └── AdminCore-1.0.0.jar
```

### Step 5: Start the Server
Start your server. AdminCore will generate its configuration directory and default files:
```
plugins/AdminCore/
├── config.yml
├── messages.yml
├── gui.yml
├── bosses.yml
├── events.yml
├── cosmetics.yml
├── rewards.yml
└── admincore.db
```

### Step 6: Verify Startup
Inspect your server startup console logs for the AdminCore initialization banner:
```
[AdminCore] =================================================
[AdminCore]  AdminCore v1.0.0
[AdminCore]  Developed and credited entirely to: LivingYuji
[AdminCore]  Minecraft Paper 1.21.1 | JDK 21
[AdminCore] =================================================
[AdminCore] SQLite storage initialized successfully.
[AdminCore] AdminCore fully enabled. Developed by LivingYuji.
```

---

## 3. LuckPerms Role Configuration Examples

AdminCore features granular permission nodes under the `admincore.` namespace. Here are recommended LuckPerms commands to configure staff groups:

### Helper Group
```
lp group helper permission set admincore.gui true
lp group helper permission set admincore.player.info true
lp group helper permission set admincore.player.freeze true
lp group helper permission set admincore.moderation.warn true
lp group helper permission set admincore.moderation.history true
lp group helper permission set admincore.moderation.reports true
```

### Moderator Group
```
lp group mod permission set admincore.gui true
lp group mod permission set admincore.player.* true
lp group mod permission set admincore.moderation.warn true
lp group mod permission set admincore.moderation.mute true
lp group mod permission set admincore.moderation.kick true
lp group mod permission set admincore.moderation.history true
lp group mod permission set admincore.moderation.notes true
lp group mod permission set admincore.moderation.reports true
lp group mod permission set admincore.player.staffmode true
```

### Event Manager Group
```
lp group eventmanager permission set admincore.gui true
lp group eventmanager permission set admincore.event.manage true
lp group eventmanager permission set admincore.boss.view true
lp group eventmanager permission set admincore.boss.manage true
lp group eventmanager permission set admincore.troll.use true
```

### Administrator Group
```
lp group admin permission set admincore.* true
```

---

## 4. Upgrading AdminCore

1. Cleanly stop the server with `stop`.
2. Back up `plugins/AdminCore/config.yml` and `plugins/AdminCore/admincore.db`.
3. Replace the old `AdminCore.jar` in `plugins/` with the new version.
4. Start the server. AdminCore safely preserves existing configurations and automatically applies SQLite database migrations.
