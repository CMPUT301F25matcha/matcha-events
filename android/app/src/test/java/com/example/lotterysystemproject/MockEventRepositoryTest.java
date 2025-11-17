// ============================================================================
// MockEventRepositoryTest.java
// ============================================================================
package com.example.lotterysystemproject;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.MockEventRepository;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for MockEventRepository
 * Uses Robolectric to mock Android components (Handler, Looper)
 */
@RunWith(RobolectricTestRunner.class)
public class MockEventRepositoryTest {

    private MockEventRepository repository;
    private static final long TIMEOUT_SECONDS = 3;

    @Before
    public void setUp() {
        repository = new MockEventRepository();
    }

    /**
     * Helper method to advance the Robolectric main looper's clock
     * and execute all pending and scheduled (delayed) tasks.
     */
    private void idleMainLooper() {
        // This will advance the clock and run all tasks, including those
        // posted with postDelayed()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks();
    }

    // ==================== USER OPERATIONS ====================

    @Test
    public void testAddUser() throws InterruptedException {
        User user = new User("user1", null, null, null);
        user.setName("Alice");
        user.setEmail("alice@example.com");

        CountDownLatch latch = new CountDownLatch(1);
        repository.addUser(user, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Should not fail: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process pending main thread tasks
        assertTrue("Timeout waiting for addUser", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetUser() throws InterruptedException {
        User user = new User("user1", null, null, null);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        repository.addUser(user, null);
        idleMainLooper();

        CountDownLatch latch = new CountDownLatch(1);
        repository.getUser("user1",
                retrievedUser -> {
                    assertNotNull(retrievedUser);
                    assertEquals("Alice", retrievedUser.getName());
                    assertEquals("alice@example.com", retrievedUser.getEmail());
                    latch.countDown();
                },
                error -> {
                    fail("User should exist: " + error.getMessage());
                    latch.countDown();
                }
        );

        idleMainLooper();
        assertTrue("Timeout waiting for getUser", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetUserNotFound() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        repository.getUser("nonexistent",
                user -> {
                    fail("User should not exist");
                    latch.countDown();
                },
                error -> {
                    assertNotNull(error);
                    assertTrue(error.getMessage().contains("not found"));
                    latch.countDown();
                }
        );

        idleMainLooper();
        assertTrue("Timeout waiting for getUser", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testUpdateUser() throws InterruptedException {
        User user = new User("user1", null, null, null);
        user.setName("Alice");
        repository.addUser(user, null);
        idleMainLooper();

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("email", "alice.new@example.com");
        updates.put("phone", "555-1234");

        CountDownLatch latch = new CountDownLatch(1);
        repository.updateUser("user1", updates, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                repository.getUser("user1",
                        updatedUser -> {
                            assertEquals("alice.new@example.com", updatedUser.getEmail());
                            assertEquals("555-1234", updatedUser.getPhone());
                            latch.countDown();
                        },
                        error -> {
                            fail("Failed to retrieve updated user");
                            latch.countDown();
                        }
                );
                idleMainLooper(); // Process the getUser callback
            }

            @Override
            public void onError(Exception e) {
                fail("Update failed: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process the updateUser callback
        assertTrue("Timeout waiting for updateUser", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDeleteUser() throws InterruptedException {
        User user = new User("user1", null, null, null);
        repository.addUser(user, null);
        idleMainLooper();

        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser("user1", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                repository.getUser("user1",
                        deletedUser -> {
                            fail("User should be deleted");
                            latch.countDown();
                        },
                        error -> {
                            assertNotNull(error);
                            latch.countDown();
                        }
                );
                idleMainLooper(); // Process the getUser callback
            }

            @Override
            public void onError(Exception e) {
                fail("Delete failed: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process the deleteUser callback
        assertTrue("Timeout waiting for deleteUser", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testUpdateUserRoleToOrganizer() throws InterruptedException {
        User user = new User("user1", null, null, null);
        repository.addUser(user, null);
        idleMainLooper();

        CountDownLatch latch = new CountDownLatch(1);
        repository.updateUserRoleToOrganizer("user1", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                repository.getUser("user1",
                        updatedUser -> {
                            assertEquals("organizer", updatedUser.getRole());
                            latch.countDown();
                        },
                        error -> {
                            fail("Failed to retrieve user");
                            latch.countDown();
                        }
                );
                idleMainLooper(); // Process the getUser callback
            }

            @Override
            public void onError(Exception e) {
                fail("Role update failed: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process the role update callback
        assertTrue("Timeout waiting for role update", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== EVENT OPERATIONS ====================

    @Test
    public void testGetAllEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.getAllEvents().observeForever(events -> {
            assertNotNull(events);
            assertTrue(events.size() > 0);
            latch.countDown();
        });

        idleMainLooper();
        assertTrue("Timeout waiting for getAllEvents", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetEventsByCategory() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.getEventsByCategory("Music",
                events -> {
                    assertNotNull(events);
                    assertTrue(events.size() > 0);
                    for (Event e : events) {
                        assertTrue(e.getCategories().contains("Music"));
                    }
                    latch.countDown();
                },
                error -> {
                    fail("Failed to get events by category: " + error.getMessage());
                    latch.countDown();
                }
        );

        idleMainLooper();
        assertTrue("Timeout waiting for getEventsByCategory", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetActiveEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.getActiveEvents(
                events -> {
                    assertNotNull(events);
                    for (Event e : events) {
                        assertTrue(e.isActive());
                    }
                    latch.countDown();
                },
                error -> {
                    fail("Failed to get active events: " + error.getMessage());
                    latch.countDown();
                }
        );

        idleMainLooper();
        assertTrue("Timeout waiting for getActiveEvents", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testGetRecentEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.getRecentEvents(3,
                events -> {
                    assertNotNull(events);
                    assertTrue(events.size() <= 3);
                    latch.countDown();
                },
                error -> {
                    fail("Failed to get recent events: " + error.getMessage());
                    latch.countDown();
                }
        );

        idleMainLooper();
        assertTrue("Timeout waiting for getRecentEvents", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testAddEvent() throws InterruptedException {
        Calendar cal = Calendar.getInstance();
        Event newEvent = new Event(
                "event_test_new",
                "Test Event",
                "Test Description",
                "Test Organizer",
                "host_test",
                cal.getTime(),
                "10:00 AM",
                "Test Location",
                100
        );

        CountDownLatch latch = new CountDownLatch(1);
        repository.addEvent(newEvent, error -> {
            if (error != null) {
                fail("Failed to add event: " + error.getMessage());
            }
            latch.countDown();
        });

        idleMainLooper();
        assertTrue("Timeout waiting for addEvent", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testAddEventDuplicate() throws InterruptedException {
        Calendar cal = Calendar.getInstance();
        Event event1 = new Event("dup_event_001", "Event 1", "Desc", "Org", "host", cal.getTime(), "10:00 AM", "Loc", 50);
        Event event2 = new Event("dup_event_001", "Event 2", "Desc", "Org", "host", cal.getTime(), "10:00 AM", "Loc", 50);

        CountDownLatch latch = new CountDownLatch(2);

        repository.addEvent(event1, error1 -> {
            if (error1 != null) {
                fail("First event should not fail");
            }
            latch.countDown();
        });

        idleMainLooper(); // Process first add

        repository.addEvent(event2, error2 -> {
            assertNotNull(error2);
            assertTrue(error2.getMessage().contains("already exists"));
            latch.countDown();
        });

        idleMainLooper(); // Process second add
        assertTrue("Timeout waiting for duplicate test", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== WAITING LIST OPERATIONS ====================

    @Test
    public void testJoinWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.joinWaitingList("event1", "user_join_001", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Failed to join waiting list: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper();
        assertTrue("Timeout waiting for joinWaitingList", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testJoinWaitingListAlreadyJoined() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        repository.joinWaitingList("event1", "user_duplicate", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("First join should succeed");
                latch.countDown();
            }
        });

        idleMainLooper(); // Process first join

        repository.joinWaitingList("event1", "user_duplicate", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                fail("Should not allow duplicate join");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                assertNotNull(e);
                assertTrue(e.getMessage().contains("Already on waiting list"));
                latch.countDown();
            }
        });

        idleMainLooper(); // Process second join
        assertTrue("Timeout waiting for duplicate join test", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testLeaveWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        repository.joinWaitingList("event1", "user_leave", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Join should succeed");
                latch.countDown();
            }
        });

        idleMainLooper(); // Process join

        repository.leaveWaitingList("event1", "user_leave", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Failed to leave waiting list: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process leave
        assertTrue("Timeout waiting for leaveWaitingList", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testLeaveWaitingListNotOnList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.leaveWaitingList("event1", "user_not_on_list", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed if user not on list");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                assertNotNull(e);
                assertTrue(e.getMessage().contains("not on waiting list"));
                latch.countDown();
            }
        });

        idleMainLooper();
        assertTrue("Timeout waiting for leaveWaitingList error", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    // ==================== REGISTRATION OPERATIONS ====================

    @Test
    public void testUpsertRegistrationOnJoin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.upsertRegistrationOnJoin("user1", "event1", "Test Event", new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Failed to upsert registration: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper();
        assertTrue("Timeout waiting for upsertRegistrationOnJoin", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testListenUserRegistrations() throws InterruptedException {
        repository.upsertRegistrationOnJoin("user1", "event1", "Event 1", null);
        idleMainLooper(); // Process the upsert

        CountDownLatch latch = new CountDownLatch(1);

        repository.listenUserRegistrations("user1", new EventRepository.RegistrationsListener() {
            @Override
            public void onChanged(List registrations) {
                assertNotNull(registrations);
                assertTrue(registrations.size() > 0);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Listener error: " + e.getMessage());
                latch.countDown();
            }
        });

        idleMainLooper(); // Process the listener setup
        assertTrue("Timeout waiting for listenUserRegistrations", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
}