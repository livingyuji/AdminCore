/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventStateTest {

    @Test
    @DisplayName("AdminEvent should follow proper state machine and participant tracking")
    void testEventLifecycle() {
        AdminEvent event = new AdminEvent(
                "lms_test", EventType.LMS, "LMS Arena", null, null, 2, 10, null
        );

        assertEquals(EventState.SETUP, event.getState());

        event.setState(EventState.WAITING);
        assertEquals(EventState.WAITING, event.getState());

        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        assertTrue(event.addParticipant(player1));
        assertTrue(event.addParticipant(player2));
        assertEquals(2, event.getParticipants().size());
        assertEquals(2, event.getAliveParticipants().size());

        // State change to RUNNING
        event.setState(EventState.RUNNING);
        assertEquals(EventState.RUNNING, event.getState());

        // Cannot add participants when running
        UUID player3 = UUID.randomUUID();
        assertFalse(event.addParticipant(player3));

        // Elimination
        event.eliminate(player1);
        assertEquals(1, event.getAliveParticipants().size());
        assertTrue(event.getAliveParticipants().contains(player2));

        // Ending & reset
        event.setState(EventState.ENDING);
        event.reset();
        assertEquals(EventState.WAITING, event.getState());
        assertEquals(0, event.getParticipants().size());
    }
}
