/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.event;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates the configuration and live runtime state of an AdminCore event.
 *
 * @author LivingYuji
 */
public class AdminEvent {

    private final String id;
    private final EventType type;
    private String displayName;
    private Location lobbyLocation;
    private Location arenaLocation;
    private int minPlayers;
    private int maxPlayers;
    private List<String> rewardCommands;

    private EventState state = EventState.SETUP;
    private final Set<UUID> participants = ConcurrentHashMap.newKeySet();
    private final Set<UUID> aliveParticipants = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> scores = new ConcurrentHashMap<>();
    private int countdownSeconds = 30;

    public AdminEvent(String id, EventType type, String displayName, Location lobbyLocation,
                      Location arenaLocation, int minPlayers, int maxPlayers, List<String> rewardCommands) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.lobbyLocation = lobbyLocation;
        this.arenaLocation = arenaLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.rewardCommands = rewardCommands != null ? rewardCommands : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public EventType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public Location getArenaLocation() {
        return arenaLocation;
    }

    public void setArenaLocation(Location arenaLocation) {
        this.arenaLocation = arenaLocation;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    public void setRewardCommands(List<String> rewardCommands) {
        this.rewardCommands = rewardCommands;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public Set<UUID> getParticipants() {
        return participants;
    }

    public Set<UUID> getAliveParticipants() {
        return aliveParticipants;
    }

    public boolean addParticipant(UUID uuid) {
        if (state != EventState.WAITING || participants.size() >= maxPlayers) {
            return false;
        }
        participants.add(uuid);
        aliveParticipants.add(uuid);
        scores.put(uuid, 0);
        return true;
    }

    public void removeParticipant(UUID uuid) {
        participants.remove(uuid);
        aliveParticipants.remove(uuid);
        scores.remove(uuid);
    }

    public void eliminate(UUID uuid) {
        aliveParticipants.remove(uuid);
    }

    public Map<UUID, Integer> getScores() {
        return scores;
    }

    public void addScore(UUID uuid, int points) {
        scores.merge(uuid, points, Integer::sum);
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int seconds) {
        this.countdownSeconds = seconds;
    }

    public int decrementCountdown() {
        return --this.countdownSeconds;
    }

    public void reset() {
        this.state = EventState.WAITING;
        this.participants.clear();
        this.aliveParticipants.clear();
        this.scores.clear();
        this.countdownSeconds = 30;
    }
}
