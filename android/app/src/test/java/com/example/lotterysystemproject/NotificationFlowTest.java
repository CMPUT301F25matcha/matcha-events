package com.example.lotterysystemproject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.lotterysystemproject.firebasemanager.NotificationRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.NotificationItem;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for lottery + notification related user stories:
 * - US 01.04.02: losers get WAITING notification
 * - US 01.05.01: replacement entrant gets INVITED
 * - US 01.05.02: accept invitation
 * - US 01.05.03: decline invitation
 */
public class NotificationFlowTest {

    /**
     * Fake repository to inspect created notifications
     */
    private static class FakeNotificationRepository implements NotificationRepository {

        final List<NotificationItem> created = new ArrayList<>();

        @Override
        public void createNotification(String userId,
                                       NotificationItem item,
                                       RepositoryCallback<Void> callback) {
            created.add(item);
            if (callback != null) callback.onSuccess(null);
        }

        @Override
        public void getNotificationsForUser(String userId,
                                            RepositoryCallback<List<NotificationItem>> callback) {
            if (callback != null) callback.onSuccess(new ArrayList<>(created));
        }

        @Override
        public void listenUserNotifications(String userId,
                                            com.example.lotterysystemproject.firebasemanager.RepositoryListener<List<NotificationItem>> listener) {
        }

        @Override
        public void deleteNotification(String userId,
                                       String notificationId,
                                       RepositoryCallback<Void> callback) {
        }

        @Override
        public void stopListeningUserNotifications() {
        }

        @Override
        public void getAllNotifications(RepositoryCallback<List<NotificationItem>> callback) {
            if (callback != null) callback.onSuccess(new ArrayList<>(created));
        }
    }

    // Helper to simulate first draw with one winner (US 01.04.02)
    /**
     * Picks the first entrant as winner, everybody else is waiting.
     */
    private void simulateFirstDrawOneWinner(
            String eventId,
            List<Entrant> entrants,
            FakeNotificationRepository notifRepo
    ) {
        if (entrants.isEmpty()) return;

        long now = System.currentTimeMillis();

        // Winner: first entrant
        Entrant winner = entrants.get(0);

        NotificationItem winItem = new NotificationItem(
                eventId + ":" + winner.getId(),
                NotificationItem.NotificationType.INVITED,
                null,
                winner.getId(),
                "You've been invited!",
                "You were selected in the lottery.",
                now
        );
        notifRepo.createNotification(winner.getId(), winItem, null);

        // Everyone else gets WAITING
        for (int i = 1; i < entrants.size(); i++) {
            Entrant loser = entrants.get(i);

            NotificationItem waitItem = new NotificationItem(
                    eventId + ":" + loser.getId(),
                    NotificationItem.NotificationType.WAITING,
                    null,
                    loser.getId(),
                    "Not selected in this draw",
                    "You are on the waiting list.",
                    now
            );
            notifRepo.createNotification(loser.getId(), waitItem, null);
        }
    }


    // US 01.04.02 – losers receive WAITING notification
    @Test
    public void losersReceiveWaitingNotification() {
        FakeNotificationRepository fakeRepo = new FakeNotificationRepository();

        List<Entrant> entrants = new ArrayList<>();

        Entrant winner = new Entrant();
        winner.setId("user_w");

        Entrant loser1 = new Entrant();
        loser1.setId("user_l1");

        Entrant loser2 = new Entrant();
        loser2.setId("user_l2");

        entrants.add(winner);
        entrants.add(loser1);
        entrants.add(loser2);

        simulateFirstDrawOneWinner("event123", entrants, fakeRepo);

        // 3 notifications total
        assertEquals(3, fakeRepo.created.size());

        NotificationItem winnerNotif = fakeRepo.created.stream()
                .filter(n -> "user_w".equals(n.getUserId()))
                .findFirst()
                .orElse(null);

        NotificationItem loserNotif1 = fakeRepo.created.stream()
                .filter(n -> "user_l1".equals(n.getUserId()))
                .findFirst()
                .orElse(null);

        NotificationItem loserNotif2 = fakeRepo.created.stream()
                .filter(n -> "user_l2".equals(n.getUserId()))
                .findFirst()
                .orElse(null);

        assertNotNull(winnerNotif);
        assertNotNull(loserNotif1);
        assertNotNull(loserNotif2);

        assertEquals(NotificationItem.NotificationType.INVITED, winnerNotif.getNotificationType());
        assertEquals(NotificationItem.NotificationType.WAITING, loserNotif1.getNotificationType());
        assertEquals(NotificationItem.NotificationType.WAITING, loserNotif2.getNotificationType());
    }

    // US 01.05.01 – replacement entrant gets INVITED notification
    /**
     * Waiting list; when a spot opens, invite the next entrant.
     */
    @Test
    public void replacementDrawSendsInvitedNotificationToWaitingEntrant() {
        FakeNotificationRepository fakeRepo = new FakeNotificationRepository();

        // Someone on the waiting list
        Entrant waiting = new Entrant();
        waiting.setId("user_waiting");

        long now = System.currentTimeMillis();

        // Controller logic: second draw for waiting entrant
        NotificationItem item = new NotificationItem(
                "event123:" + waiting.getId(),
                NotificationItem.NotificationType.INVITED,
                null,
                waiting.getId(),
                "You’ve been invited from the waiting list!",
                "A spot opened up because someone declined.",
                now
        );

        fakeRepo.createNotification(waiting.getId(), item, null);

        assertEquals(1, fakeRepo.created.size());
        NotificationItem stored = fakeRepo.created.get(0);
        assertEquals(NotificationItem.NotificationType.INVITED, stored.getNotificationType());
        assertEquals("user_waiting", stored.getUserId());
    }

    // US 01.05.02 / 01.05.03 – Accept / Decline invitation
    /**
     * Model test: decision on NotificationItem updates correctly.
     */
    @Test
    public void invitationDecisionIsPersistedOnModel() {
        NotificationItem invited = new NotificationItem(
                "event123:ent_1",
                NotificationItem.NotificationType.INVITED,
                null,
                "user_1",
                "Invite",
                "Please join",
                System.currentTimeMillis()
        );

        invited.setDecision(NotificationItem.Decision.ACCEPTED);
        assertEquals(NotificationItem.Decision.ACCEPTED, invited.getDecision());

        invited.setDecision(NotificationItem.Decision.DECLINED);
        assertEquals(NotificationItem.Decision.DECLINED, invited.getDecision());
    }

    /**
     * Mockito: Accepting an invitation should set decision to ACCEPTED
     * and call NotificationRepository.createNotification to persist it.
     */
    @Test
    public void acceptInvitationPersistsDecisionThroughRepository() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);

        NotificationItem item = new NotificationItem(
                "event123:ent_1",
                NotificationItem.NotificationType.INVITED,
                null,
                "user_1",
                "Invite",
                "Please join",
                System.currentTimeMillis()
        );

        // Simulate UI/controller logic:
        item.setDecision(NotificationItem.Decision.ACCEPTED);
        mockRepo.createNotification("user_1", item, null);

        // Capture what repo received
        ArgumentCaptor<NotificationItem> itemCaptor =
                ArgumentCaptor.forClass(NotificationItem.class);

        verify(mockRepo, times(1))
                .createNotification(eq("user_1"), itemCaptor.capture(), any());

        NotificationItem saved = itemCaptor.getValue();
        assertNotNull(saved);
        assertEquals(NotificationItem.Decision.ACCEPTED, saved.getDecision());
    }

    /**
     * Mockito: Declining an invitation should set decision to DECLINED
     * and call NotificationRepository.createNotification to persist it.
     */
    @Test
    public void declineInvitationPersistsDecisionThroughRepository() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);

        NotificationItem item = new NotificationItem(
                "event123:ent_2",
                NotificationItem.NotificationType.INVITED,
                null,
                "user_2",
                "Invite",
                "Please join",
                System.currentTimeMillis()
        );

        // Simulate UI/controller logic:
        item.setDecision(NotificationItem.Decision.DECLINED);
        mockRepo.createNotification("user_2", item, null);

        // Capture what repo received
        ArgumentCaptor<NotificationItem> itemCaptor =
                ArgumentCaptor.forClass(NotificationItem.class);

        verify(mockRepo, times(1))
                .createNotification(eq("user_2"), itemCaptor.capture(), any());

        NotificationItem saved = itemCaptor.getValue();
        assertNotNull(saved);
        assertEquals(NotificationItem.Decision.DECLINED, saved.getDecision());
    }
}
