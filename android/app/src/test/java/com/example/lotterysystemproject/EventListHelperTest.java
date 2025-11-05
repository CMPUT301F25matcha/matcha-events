package com.example.lotterysystemproject;

import static org.junit.Assert.assertEquals;

import com.example.lotterysystemproject.Helpers.EventListHelper;
import com.example.lotterysystemproject.Models.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

/**
 * Simple unit tests for the logic within EventListHelper.
 * These tests do not require Mockito or any Android framework dependencies.
 */
public class EventListHelperTest {

    @Test
    public void getButtonState_returnsJoin_whenUserIsNotOnWaitingList() {
        // Arrange
        Event event = new Event("1", "Test Event", "Host", "1", new Date(), "Location");
        String userId = "user123";

        // Act
        int buttonState = EventListHelper.getButtonState(event, userId);

        // Assert
        assertEquals("Button state should be 0 (Join)", 0, buttonState);
    }

    @Test
    public void getButtonState_returnsLeave_whenUserIsOnWaitingList() {
        // Arrange
        Event event = new Event("1", "Test Event", "Host", "1", new Date(), "Location");
        String userId = "user123";
        ArrayList<String> waitingList = new ArrayList<>();
        waitingList.add(userId);
        event.setWaitingList(waitingList); // Put the user on the waiting list

        // Act
        int buttonState = EventListHelper.getButtonState(event, userId);

        // Assert
        assertEquals("Button state should be 1 (Leave)", 1, buttonState);
    }

    @Test
    public void getButtonState_returnsParticipating_whenUserIsParticipant() {
        // Arrange
        Event event = new Event("1", "Test Event", "Host", "1", new Date(), "Location");
        String userId = "user123";
        ArrayList<String> participants = new ArrayList<>();
        participants.add(userId);
        event.setParticipants(participants); // Make the user a participant

        // Act
        int buttonState = EventListHelper.getButtonState(event, userId);

        // Assert
        assertEquals("Button state should be 2 (Participating)", 2, buttonState);
    }

    @Test
    public void getButtonState_returnsJoin_whenUserIsNull() {
        // Arrange
        Event event = new Event("1", "Test Event", "Host", "1", new Date(), "Location");
        String userId = null; // User is not logged in

        // Act
        int buttonState = EventListHelper.getButtonState(event, userId);

        // Assert
        assertEquals("Button state should be 0 (Join) for null user", 0, buttonState);
    }
}
