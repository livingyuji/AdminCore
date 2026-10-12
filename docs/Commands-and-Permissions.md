# AdminCore — Commands & Permissions Reference

**Author & Developer:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. General & Dashboard Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac` | None | `admincore.gui` | OP | Player | Opens the 10-section Admin Control Center GUI. |
| `/ac help` | `[category]` | None | Everyone | Both | Displays category-specific help pages (`general`, `player`, `moderation`, `boss`, `event`). |
| `/ac gui` | None | `admincore.gui` | OP | Player | Direct alias to open the interactive Control Center. |
| `/ac version` | None | None | Everyone | Both | Displays plugin version, credits to LivingYuji, and runtime environment. |
| `/ac status` | None | `admincore.status` | OP | Both | Shows active subsystems, memory diagnostics, and Discord status. |
| `/ac reload` | None | `admincore.reload` | OP | Both | Reloads all 7 configuration files safely from disk. |
| `/ac debug` | None | `admincore.debug` | OP | Both | Toggles verbose diagnostic logging. |

---

## 2. Player Utility Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac info` | `<player>` | `admincore.player.info` | OP | Both | Inspects health, gamemode, coords, states, and ping. |
| `/ac invsee` | `<player>` | `admincore.player.inventory` | OP | Player | Opens target's live inventory. |
| `/ac endersee` | `<player>` | `admincore.player.inventory` | OP | Player | Opens target's ender chest. |
| `/ac tp` | `<player>` | `admincore.player.teleport` | OP | Player | Teleports you to target player. |
| `/ac tphere` | `<player>` | `admincore.player.teleport` | OP | Player | Teleports target player to your location. |
| `/ac tpall` | None | `admincore.player.teleport` | OP | Player | Teleports all online players to you. |
| `/ac back` | None | `admincore.player.teleport` | OP | Player | Returns to your previous teleport or death position. |
| `/ac heal` | `[player]` | `admincore.player.heal` | OP | Both | Restores full health, food, and extinguishes fire. |
| `/ac feed` | `[player]` | `admincore.player.heal` | OP | Both | Restores food bar to full (20). |
| `/ac fly` | `[player]` | `admincore.player.fly` | OP | Both | Toggles flight capability. |
| `/ac god` | `[player]` | `admincore.player.god` | OP | Both | Toggles complete damage invulnerability. |
| `/ac speed` | `<1-10> [player]` | `admincore.player.speed` | OP | Both | Sets walk speed or flight speed (scaled 1-10). |
| `/ac vanish` | `[player]` | `admincore.player.vanish` | OP | Both | Toggles vanish mode; conceals player from non-staff. |
| `/ac freeze` | `<player>` | `admincore.player.freeze` | OP | Both | Freezes or unfreezes player movement. |
| `/ac spectate`| `<player>` | `admincore.player.spectate` | OP | Player | Puts you in Spectator mode targeting the player. |
| `/ac effect` | `<player> <effect> <duration>` | `admincore.player.effect` | OP | Both | Applies a potion effect safely. |
| `/ac staffmode`| None | `admincore.player.staffmode` | OP | Player | Enters staff duty with compass, inspector, freeze wand. |

---

## 3. Fun and Troll Prank Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac troll help` | None | `admincore.troll.use` | OP | Both | Displays troll tool commands and pranks. |
| `/ac troll fakejoin` | `<player>` | `admincore.troll.use` | OP | Both | Broadcasts fake join message. |
| `/ac troll fakeleave`| `<player>` | `admincore.troll.use` | OP | Both | Broadcasts fake quit message. |
| `/ac troll lightning`| `<player>` | `admincore.troll.use` | OP | Both | Strikes harmless visual lightning. |
| `/ac troll launch` | `<player>` | `admincore.troll.use` | OP | Both | Launches player upwards with 8s fall immunity. |
| `/ac troll spin` | `<player>` | `admincore.troll.use` | OP | Both | Harmlessly spins player for 5 seconds. |
| `/ac troll sound` | `<player> <sound>` | `admincore.troll.use` | OP | Both | Plays specific sound effect to target. |
| `/ac troll title` | `<player> <text>` | `admincore.troll.use` | OP | Both | Displays large title on target screen. |
| `/ac troll particles`| `<player> <effect>`| `admincore.troll.use` | OP | Both | Spawns particle burst around target. |
| `/ac troll stop` | `<player>` | `admincore.troll.use` | OP | Both | Immediately halts running pranks on target. |
| `/ac troll clear` | None | `admincore.troll.use` | OP | Both | Cancels all active troll effects across server. |

---

## 4. Custom Boss Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac boss list` | None | `admincore.boss.view` | True | Both | Lists configured bosses and active instances. |
| `/ac boss info` | `<id>` | `admincore.boss.view` | True | Both | Displays boss stats, abilities, and loot. |
| `/ac boss spawn` | `<id>` | `admincore.boss.manage` | OP | Both | Spawns boss at current location or config spawn. |
| `/ac boss setspawn`| `<id>` | `admincore.boss.manage` | OP | Player | Saves current position as default boss spawn point. |
| `/ac boss remove` | `<id>` | `admincore.boss.manage` | OP | Both | Removes all spawned instances of specified boss. |
| `/ac boss killall`| None | `admincore.boss.manage` | OP | Both | Removes all active plugin-managed bosses. |
| `/ac boss reload` | None | `admincore.boss.manage` | OP | Both | Reloads `bosses.yml` templates. |

---

## 5. Server Event Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac event list` | None | `admincore.event.join` | True | Both | Lists all server events and active match state. |
| `/ac event join` | None | `admincore.event.join` | True | Player | Joins the currently open event lobby. |
| `/ac event leave`| None | `admincore.event.join` | True | Player | Leaves the active event lobby. |
| `/ac event create`| `<type> <name>`| `admincore.event.manage` | OP | Both | Creates new event (LMS, KOTH, SUMO, etc.). |
| `/ac event start` | `<name>` | `admincore.event.manage` | OP | Both | Starts event with 30-second broadcast countdown. |
| `/ac event stop` | `[name]` | `admincore.event.manage` | OP | Both | Stops and cleans up active event match. |
| `/ac event setspawn`| `<name>`| `admincore.event.manage` | OP | Player | Sets event waiting lobby position. |
| `/ac event setarena`| `<name>`| `admincore.event.manage` | OP | Player | Sets event competition arena / hill position. |
| `/ac event delete` | `<name>`| `admincore.event.manage` | OP | Both | Deletes event configuration. |

---

## 6. Cosmetics Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac cosmetics` | None | `admincore.cosmetics.use` | True | Player | Opens cosmetics selector GUI. |
| `/ac cosmetics trails`| `<id>` | `admincore.cosmetics.use` | True | Player | Equips a particle trail. |
| `/ac cosmetics aura` | `<id>` | `admincore.cosmetics.use` | True | Player | Equips an ambient aura. |
| `/ac cosmetics clear`| None | `admincore.cosmetics.use` | True | Player | Clears your equipped cosmetics. |
| `/ac cosmetics give` | `<player> <id>` | `admincore.cosmetics.manage` | OP | Both | Equips cosmetic on another player. |
| `/ac cosmetics reload`| None | `admincore.cosmetics.manage` | OP | Both | Reloads `cosmetics.yml`. |

---

## 7. Moderation & Audit Commands

| Command | Arguments | Permission Node | Default | Sender | Description |
|---|---|---|---|---|---|
| `/ac warn` | `<player> <reason>` | `admincore.moderation.warn` | OP | Both | Records a warning on player. |
| `/ac mute` | `<player> <duration> <reason>`| `admincore.moderation.mute` | OP | Both | Mutes player (e.g. 1h, 1d, permanent). |
| `/ac unmute`| `<player>` | `admincore.moderation.mute` | OP | Both | Unmutes player. |
| `/ac kick` | `<player> <reason>` | `admincore.moderation.kick` | OP | Both | Kicks player from the server. |
| `/ac ban` | `<player> <reason>` | `admincore.moderation.ban` | OP | Both | Permanently bans player. |
| `/ac tempban`| `<player> <duration> <reason>`| `admincore.moderation.ban` | OP | Both | Temporarily bans player. |
| `/ac unban` | `<player>` | `admincore.moderation.ban` | OP | Both | Unbans player. |
| `/ac history`| `<player>` | `admincore.moderation.history`| OP | Both | Displays full punishment history. |
| `/ac note` | `<player> <text>` | `admincore.moderation.notes` | OP | Both | Adds private staff note on player. |
| `/ac report`| `<player> <reason>` | None | True | Player | Submits report to online staff and queue. |
| `/ac reports`| None | `admincore.moderation.reports`| OP | Both | Reviews open reports queue. |
| `/ac logs` | `[search]` | `admincore.logs.view` | OP | Both | Searches staff audit logs. |
| `/ac logs export`| None | `admincore.logs.export` | OP | Both | Exports audit logs to text file. |
| `/ac discord test`| None | `admincore.discord.manage` | OP | Both | Sends test notification to webhook. |
