/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore;

import com.livingyuji.admincore.boss.BossManager;
import com.livingyuji.admincore.command.AdminCoreCommand;
import com.livingyuji.admincore.command.BossCommandHandler;
import com.livingyuji.admincore.command.CosmeticsCommandHandler;
import com.livingyuji.admincore.command.EventCommandHandler;
import com.livingyuji.admincore.command.GeneralCommandHandler;
import com.livingyuji.admincore.command.ModerationCommandHandler;
import com.livingyuji.admincore.command.PlayerCommandHandler;
import com.livingyuji.admincore.command.TrollCommandHandler;
import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.cosmetics.CosmeticsManager;
import com.livingyuji.admincore.discord.DiscordWebhookService;
import com.livingyuji.admincore.event.EventManager;
import com.livingyuji.admincore.gui.GuiManager;
import com.livingyuji.admincore.integration.LuckPermsIntegration;
import com.livingyuji.admincore.integration.VaultIntegration;
import com.livingyuji.admincore.logging.AuditLogger;
import com.livingyuji.admincore.moderation.ModerationListener;
import com.livingyuji.admincore.moderation.ModerationManager;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.player.PlayerListener;
import com.livingyuji.admincore.player.PlayerStateManager;
import com.livingyuji.admincore.rewards.RewardManager;
import com.livingyuji.admincore.storage.DatabaseService;
import com.livingyuji.admincore.troll.TrollManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Main entry point for the AdminCore plugin.
 * All original AdminCore source code, project documentation, and assets are credited to LivingYuji.
 *
 * @author LivingYuji
 */
public class AdminCore extends JavaPlugin {

    private static AdminCore instance;

    // Core Managers & Services
    private ConfigManager configManager;
    private MessageService messageService;
    private DatabaseService databaseService;
    private PermissionService permissionService;
    private AuditLogger auditLogger;
    private DiscordWebhookService discordService;
    private VaultIntegration vaultIntegration;
    private LuckPermsIntegration luckPermsIntegration;

    // Feature Modules
    private PlayerStateManager playerStateManager;
    private ModerationManager moderationManager;
    private TrollManager trollManager;
    private BossManager bossManager;
    private EventManager eventManager;
    private CosmeticsManager cosmeticsManager;
    private RewardManager rewardManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        getLogger().info("=================================================");
        getLogger().info(" AdminCore v" + getDescription().getVersion());
        getLogger().info(" Developed and credited entirely to: LivingYuji");
        getLogger().info(" Minecraft Paper 1.21.1 | JDK 21");
        getLogger().info("=================================================");

        // 1. Configurations
        configManager = new ConfigManager(this);
        configManager.loadAll();
        messageService = new MessageService(configManager);

        // 2. Storage & Database
        databaseService = new DatabaseService(this);
        try {
            databaseService.initialize();
            getLogger().info("SQLite storage initialized successfully.");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize SQLite database! Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 3. Services & Logging
        permissionService = new PermissionService();
        auditLogger = new AuditLogger(this, databaseService);
        discordService = new DiscordWebhookService(this, configManager);

        // 4. Integrations
        vaultIntegration = new VaultIntegration(this);
        if (vaultIntegration.setupEconomy()) {
            getLogger().info("Hooked into Vault economy.");
        } else {
            getLogger().info("Vault not detected. Economy features operating in standalone mode.");
        }

        luckPermsIntegration = new LuckPermsIntegration(this);
        if (luckPermsIntegration.setupLuckPerms()) {
            getLogger().info("Hooked into LuckPerms permissions API.");
        }

        // 5. Feature Modules
        playerStateManager = new PlayerStateManager(this);
        moderationManager = new ModerationManager(this, databaseService);
        moderationManager.loadActivePunishments();

        trollManager = new TrollManager(this);

        bossManager = new BossManager(this, configManager);
        bossManager.loadConfigs();
        bossManager.startTicking();

        eventManager = new EventManager(this, configManager, databaseService);
        eventManager.loadEvents();

        cosmeticsManager = new CosmeticsManager(this, configManager, databaseService);
        cosmeticsManager.loadCosmetics();
        cosmeticsManager.startParticleTask();

        rewardManager = new RewardManager(this, configManager, vaultIntegration);

        guiManager = new GuiManager(
                this, configManager, permissionService, playerStateManager, moderationManager,
                trollManager, bossManager, eventManager, cosmeticsManager, auditLogger
        );

        // 6. Register Event Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this, playerStateManager), this);
        pm.registerEvents(new ModerationListener(moderationManager), this);
        pm.registerEvents(trollManager, this);
        pm.registerEvents(bossManager, this);
        pm.registerEvents(eventManager, this);
        pm.registerEvents(cosmeticsManager, this);
        pm.registerEvents(guiManager, this);

        // 7. Register Commands
        GeneralCommandHandler generalHandler = new GeneralCommandHandler(
                this, configManager, messageService, permissionService, guiManager, auditLogger, discordService
        );
        PlayerCommandHandler playerHandler = new PlayerCommandHandler(
                messageService, permissionService, playerStateManager, auditLogger
        );
        TrollCommandHandler trollHandler = new TrollCommandHandler(
                messageService, permissionService, trollManager
        );
        BossCommandHandler bossHandler = new BossCommandHandler(
                messageService, permissionService, bossManager
        );
        EventCommandHandler eventHandler = new EventCommandHandler(
                messageService, permissionService, eventManager
        );
        CosmeticsCommandHandler cosmeticsHandler = new CosmeticsCommandHandler(
                messageService, permissionService, cosmeticsManager, guiManager
        );
        ModerationCommandHandler moderationHandler = new ModerationCommandHandler(
                messageService, permissionService, moderationManager, auditLogger, discordService
        );

        AdminCoreCommand mainCommand = new AdminCoreCommand(
                permissionService, guiManager, generalHandler, playerHandler,
                trollHandler, bossHandler, eventHandler, cosmeticsHandler, moderationHandler
        );

        registerCommand("admincore", mainCommand);
        registerCommand("ac", mainCommand);

        // 8. Startup Notification
        if (discordService.isEnabled()) {
            discordService.sendEmbed("system", "AdminCore Started",
                    "AdminCore v" + getDescription().getVersion() + " by LivingYuji has successfully enabled.", 0x00FF88);
        }

        auditLogger.log("SYSTEM", "STARTUP", "AdminCore successfully enabled in " + (System.currentTimeMillis() - startTime) + "ms");
        getLogger().info("AdminCore fully enabled in " + (System.currentTimeMillis() - startTime) + "ms. Developed by LivingYuji.");
    }

    private void registerCommand(String name, AdminCoreCommand executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling AdminCore by LivingYuji...");

        if (discordService != null && discordService.isEnabled()) {
            discordService.sendEmbed("system", "AdminCore Shutting Down",
                    "AdminCore is shutting down.", 0xFF5555);
        }

        // Clean up live player states
        if (playerStateManager != null) {
            playerStateManager.restoreAll();
        }

        // Clean up trolls
        if (trollManager != null) {
            trollManager.clearAll();
        }

        // Clean up bosses
        if (bossManager != null) {
            bossManager.shutdown();
        }

        // Clean up events
        if (eventManager != null) {
            eventManager.shutdown();
        }

        // Clean up cosmetics
        if (cosmeticsManager != null) {
            cosmeticsManager.shutdown();
        }

        // Cancel all plugin tasks
        getServer().getScheduler().cancelTasks(this);

        // Shutdown services
        if (discordService != null) {
            discordService.shutdown();
        }

        if (databaseService != null) {
            databaseService.shutdown();
        }

        getLogger().info("AdminCore disabled cleanly. Goodbye!");
    }

    public static AdminCore getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }

    public TrollManager getTrollManager() {
        return trollManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public DiscordWebhookService getDiscordService() {
        return discordService;
    }
}
