/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Custom InventoryHolder to securely identify AdminCore menus and avoid item tampering.
 *
 * @author LivingYuji
 */
public class AdminCoreHolder implements InventoryHolder {

    private final MenuType menuType;
    private final int page;
    private final Object context;
    private Inventory inventory;

    public AdminCoreHolder(MenuType menuType, int page, Object context) {
        this.menuType = menuType;
        this.page = page;
        this.context = context;
    }

    public AdminCoreHolder(MenuType menuType) {
        this(menuType, 0, null);
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public int getPage() {
        return page;
    }

    public Object getContext() {
        return context;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
