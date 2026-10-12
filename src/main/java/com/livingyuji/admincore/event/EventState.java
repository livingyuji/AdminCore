/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.event;

/**
 * State machine phases for an AdminCore server event.
 *
 * @author LivingYuji
 */
public enum EventState {
    SETUP,
    WAITING,
    RUNNING,
    ENDING,
    FINISHED
}
