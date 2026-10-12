# AdminCore — Developer API Reference

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. Accessing AdminCore

Other plugins on the Paper server can access AdminCore via the singleton instance or Bukkit plugin manager:

```java
import com.livingyuji.admincore.AdminCore;

public class MyPluginExtension {
    public void example() {
        AdminCore adminCore = AdminCore.getInstance();
        if (adminCore == null || !adminCore.isEnabled()) {
            return;
        }
        
        // Access subsystems
        var playerManager = adminCore.getPlayerStateManager();
        var moderationManager = adminCore.getModerationManager();
        var bossManager = adminCore.getBossManager();
        var eventManager = adminCore.getEventManager();
    }
}
```

---

## 2. Moderation Manager API

### Checking Player Mute or Ban Status
```java
UUID playerUuid = player.getUniqueId();
Punishment mute = adminCore.getModerationManager().getActiveMute(playerUuid);
if (mute != null) {
    long remaining = mute.getRemainingMillis();
    String reason = mute.getReason();
}
```

### Issuing Disciplinary Actions Programmatically
```java
adminCore.getModerationManager().mute(
    targetUuid,
    "TargetName",
    staffUuid, // null for console
    "StaffName",
    "Spamming in global chat",
    3_600_000L // 1 hour in milliseconds
);
```

---

## 3. Custom Boss Manager API

### Spawning a Boss via Code
```java
Location spawnLocation = player.getLocation();
boolean success = adminCore.getBossManager().spawnBoss("infernal_titan", spawnLocation);
```

### Querying Active Bosses
```java
int activeBossCount = adminCore.getBossManager().getActiveBosses().size();
```

---

## 4. Server Events API

### Checking Active Event State
```java
AdminEvent active = adminCore.getEventManager().getActiveEvent();
if (active != null && active.getState() == EventState.RUNNING) {
    String eventName = active.getDisplayName();
    int playersRemaining = active.getAliveParticipants().size();
}
```

### Joining an Event
```java
boolean joined = adminCore.getEventManager().joinActiveEvent(player);
```
