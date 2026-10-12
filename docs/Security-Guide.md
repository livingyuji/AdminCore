# AdminCore — Security Guide & Best Practices

**Author:** LivingYuji  
**Copyright:** © 2026 LivingYuji. All rights reserved.  
**Credit Notice:** AdminCore is an original Minecraft plugin developed by LivingYuji. All original AdminCore source code, project documentation, and project-specific assets created for this project are credited to LivingYuji.

---

## 1. Permission Architecture & Principle of Least Privilege

AdminCore is engineered with strict least-privilege security defaults. All administrative and destructive permissions default to `op` or `false` rather than being open to standard players.

- **Wildcards (`admincore.*`):** Only assign `admincore.*` to trusted server owners or Lead Administrators. Never grant `*` or wildcard permissions to regular moderators or trial helpers.
- **Granular Tiers:** Assign exact permissions based on staff tier (Helper: `admincore.player.freeze`, `admincore.moderation.warn`; Moderator: `admincore.moderation.mute`, `admincore.moderation.kick`, `admincore.player.staffmode`; Admin: `admincore.moderation.ban`, `admincore.reload`).

---

## 2. Inventory & GUI Anti-Tampering

Minecraft inventory packets can be exploited if menus do not strictly identify and authenticate inventory actions:
1. **InventoryHolder Identification:** AdminCore uses a custom `AdminCoreHolder` class rather than relying on GUI title strings or item display names, preventing spoofing.
2. **Action Interception:** All click events (clicks, shift-clicks, double-clicks, number keys, drags) are unconditionally cancelled.
3. **Privilege Re-Authentication:** Clicking an administrative action button inside a GUI re-verifies that the clicking player possesses the exact corresponding `admincore.` permission node before executing the action.

---

## 3. Database & SQL Injection Protection

1. **Prepared Statements:** Every single SQL interaction in AdminCore strictly utilizes `java.sql.PreparedStatement` with parameterized placeholders (`?`). String concatenation is never used for queries.
2. **Thread Safety & Async Execution:** Heavy database read/write queries run asynchronously on dedicated worker daemon threads to ensure the Minecraft main server thread never drops ticks or stutters during database I/O.

---

## 4. Webhook Security & Zero Secret Leaks

1. **URL Sanitization:** Discord webhook URLs are stored strictly in `config.yml`. AdminCore never echoes or prints webhook URLs into public server chat, console logs, or crash traces.
2. **Markdown & JSON Escaping:** User-provided text (punishment reasons, player names) is sanitized and escaped (`TextUtil.escapeDiscord`) to prevent malicious webhook payloads, mention pings, or markdown breakage.

---

## 5. Audit Logging

Every administrative action—punishments, teleports, staff mode toggles, and configuration reloads—is committed to the persistent SQLite `ac_audit_logs` table. Administrators can inspect history at any time with `/ac logs search <text>` or export complete logs with `/ac logs export`.
