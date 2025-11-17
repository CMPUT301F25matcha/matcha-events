package com.example.lotterysystemproject;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.MockEntrantRepository;
import com.example.lotterysystemproject.firebasemanager.MockEventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for MockEntrantRepository
 * Uses Robolectric to mock Android components
 */
@RunWith(RobolectricTestRunner.class)
public class MockEntrantRepositoryTest {

    private EntrantRepository entrantRepository;
    private EventRepository eventRepository;
    private static final long TIMEOUT_SECONDS = 5;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        // FIXED: Reset and get singleton instances from RepositoryProvider
        RepositoryProvider.resetInstances();

        // Get the singleton instances - these are the same instances used internally
        entrantRepository = RepositoryProvider.getEntrantRepository();
        eventRepository = RepositoryProvider.getEventRepository();
    }

    // ==================== ENTRANT RETRIEVAL ====================

    @Test
    public void testGetEntrants() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.getEntrants("1").observeForever(entrants -> {
            assertNotNull(entrants);
            assertEquals(130, entrants.size());
            latch.countDown();
        });

        assertTrue("Timeout waiting for getEntrants", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetEntrantsInvalidEventId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.getEntrants("invalid_event_xyz").observeForever(entrants -> {
            assertNotNull(entrants);
            assertEquals(0, entrants.size());
            latch.countDown();
        });

        assertTrue("Timeout waiting for invalid event", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testEntrantStatus() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.getEntrants("1").observeForever(entrants -> {
            assertNotNull(entrants);
            assertTrue(entrants.size() > 0);

            for (Entrant e : entrants) {
                assertEquals(Entrant.Status.WAITING, e.getStatus());
            }
            latch.countDown();
        });

        assertTrue("Timeout waiting for status check", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testEntrantTimestamps() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.getEntrants("1").observeForever(entrants -> {
            assertNotNull(entrants);
            assertTrue(entrants.size() > 0);

            // Check that timestamps are decreasing (older entrants joined first)
            long previousTimestamp = Long.MAX_VALUE;
            for (Entrant e : entrants) {
                assertTrue(e.getJoinedTimestamp() <= previousTimestamp);
                previousTimestamp = e.getJoinedTimestamp();
            }
            latch.countDown();
        });

        assertTrue("Timeout waiting for timestamp check", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== LOTTERY DRAW ====================

    @Test
    public void testDrawLottery() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawLottery("1", 10, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                assertNotNull(winners);
                assertEquals(10, winners.size());
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Lottery draw failed: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for lottery draw", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawLotteryMultipleWinners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawLottery("1", 50, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                assertNotNull(winners);
                assertEquals(50, winners.size());

                // Verify winners are selected
                for (Entrant w : winners) {
                    assertTrue(w.getStatus() == Entrant.Status.ENROLLED ||
                            w.getStatus() == Entrant.Status.INVITED);
                }
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Lottery draw failed: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for multiple winners draw", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawLotteryEmptyWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawLottery("invalid_event_xyz", 5, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                fail("Should not complete with empty waiting list");
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                assertNotNull(error);
                assertTrue(error.contains("No entrants available") || error.contains("empty"));
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for empty lottery draw", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawLotteryEnrollsSomeAutomatically() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawLottery("1", 100, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                int enrolledCount = 0;
                int invitedCount = 0;

                for (Entrant w : winners) {
                    if (w.getStatus() == Entrant.Status.ENROLLED) enrolledCount++;
                    if (w.getStatus() == Entrant.Status.INVITED) invitedCount++;
                }

                // Roughly 1/4 should be enrolled
                assertTrue("Should have enrolled winners", enrolledCount > 0);
                assertTrue("Should have invited winners", invitedCount > 0);
                assertEquals(100, enrolledCount + invitedCount);
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Lottery failed: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for auto-enrollment check", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawLotterySelectsMoreThanAvailable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Try to select more than available (130 mock entrants)
        entrantRepository.drawLottery("1", 150, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                // Should cap at 130
                assertNotNull(winners);
                assertEquals(130, winners.size());
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Should handle over-selection gracefully: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for over-selection test", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== ENTRANT CANCELLATION ====================

    @Test
    public void testCancelEntrant() throws InterruptedException {
        CountDownLatch getLatch = new CountDownLatch(1);
        final String[] entrantId = {null};

        // Step 1: Get an entrant ID
        Observer<List<Entrant>> initialObserver = entrants -> {
            if (entrants != null && !entrants.isEmpty()) {
                entrantId[0] = entrants.get(0).getId();
                getLatch.countDown();
            }
        };

        LiveData<List<Entrant>> liveData = entrantRepository.getEntrants("1");
        liveData.observeForever(initialObserver);

        assertTrue("Timeout getting initial entrants", getLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        liveData.removeObserver(initialObserver);

        // Step 2: Cancel the entrant
        CountDownLatch cancelLatch = new CountDownLatch(1);
        entrantRepository.cancelEntrant(entrantId[0], new EntrantRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                cancelLatch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Cancel failed: " + error);
                cancelLatch.countDown();
            }
        });

        assertTrue("Timeout canceling entrant", cancelLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // Step 3: Verify the cancellation
        CountDownLatch verifyLatch = new CountDownLatch(1);
        Observer<List<Entrant>> verifyObserver = entrants -> {
            if (entrants != null) {
                for (Entrant e : entrants) {
                    if (e.getId().equals(entrantId[0])) {
                        assertEquals(Entrant.Status.CANCELLED, e.getStatus());
                        verifyLatch.countDown();
                        return;
                    }
                }
                fail("Entrant not found after cancellation");
                verifyLatch.countDown();
            }
        };

        liveData.observeForever(verifyObserver);
        assertTrue("Timeout verifying cancellation", verifyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        liveData.removeObserver(verifyObserver);
    }

    @Test
    public void testCancelInvalidEntrant() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.cancelEntrant("invalid_id", new EntrantRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                // Mock implementation may not fail on invalid ID
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                assertNotNull(error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for invalid cancel", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== REPLACEMENT DRAW ====================

    @Test
    public void testDrawReplacement() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawReplacement("1", new EntrantRepository.OnReplacementDrawnListener() {
            @Override
            public void onSuccess(Entrant replacement) {
                assertNotNull(replacement);
                assertEquals(Entrant.Status.INVITED, replacement.getStatus());
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Replacement draw failed: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for replacement draw", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawMultipleReplacements() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);

        // Draw 5 replacements sequentially
        for (int i = 0; i < 5; i++) {
            entrantRepository.drawReplacement("1", new EntrantRepository.OnReplacementDrawnListener() {
                @Override
                public void onSuccess(Entrant replacement) {
                    assertNotNull(replacement);
                    assertEquals(Entrant.Status.INVITED, replacement.getStatus());
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    fail("Replacement draw failed: " + error);
                    latch.countDown();
                }
            });
        }

        assertTrue("Timeout waiting for multiple replacements", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDrawReplacementEmptyWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.drawReplacement("invalid_event", new EntrantRepository.OnReplacementDrawnListener() {
            @Override
            public void onSuccess(Entrant replacement) {
                fail("Should not succeed with empty waiting list");
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                assertNotNull(error);
                assertTrue(error.contains("No entrants") || error.contains("empty"));
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for empty replacement", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== USER INFO ====================

    @Test
    public void testGetCurrentUserInfo() throws InterruptedException {
        // Create a user in EventRepository
        User user = new User("test_device_123", null, null, null);
        user.setName("John Doe");
        user.setRole("entrant");

        CountDownLatch addUserLatch = new CountDownLatch(1);
        eventRepository.addUser(user, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                addUserLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Failed to add user: " + e.getMessage());
                addUserLatch.countDown();
            }
        });

        assertTrue("Timeout adding user", addUserLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // Now try to retrieve the user info
        CountDownLatch getUserLatch = new CountDownLatch(1);
        entrantRepository.getCurrentUserInfo("test_device_123", new EntrantRepository.OnUserInfoListener() {
            @Override
            public void onSuccess(String hostId, String hostName, String role) {
                assertNotNull(hostId);
                assertEquals("John Doe", hostName);
                assertEquals("entrant", role);
                getUserLatch.countDown();
            }

            @Override
            public void onFailure(String error) {
                fail("Failed to get user info: " + error);
                getUserLatch.countDown();
            }
        });

        assertTrue("Timeout getting user info", getUserLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetCurrentUserInfoNotFound() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        entrantRepository.getCurrentUserInfo("nonexistent_user", new EntrantRepository.OnUserInfoListener() {
            @Override
            public void onSuccess(String hostId, String hostName, String role) {
                fail("Should not find nonexistent user");
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                assertNotNull(error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for user not found", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
}