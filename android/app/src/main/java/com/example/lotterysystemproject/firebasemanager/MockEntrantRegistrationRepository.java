package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.EntrantRegistrationStatus;
import com.example.lotterysystemproject.models.Registration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of EntrantRegistrationRepository for testing
 * Coordinates between mock Entrant and Registration tables
 */
public class MockEntrantRegistrationRepository implements EntrantRegistrationRepository {

    private static final String TAG = "MockEntrantRegRepo";

    private final Map<String, Entrant> entrants = new ConcurrentHashMap<>();
    private final Map<String, Registration> registrations = new ConcurrentHashMap<>();
    private final RegistrationRepository registrationRepo;

    private boolean simulateDelay = false;
    private long delayMs = 100;

    public MockEntrantRegistrationRepository(RegistrationRepository registrationRepo) {
        this.registrationRepo = registrationRepo;
    }

    public MockEntrantRegistrationRepository() {
        this(new MockRegistrationRepository());
    }

    // ========================================================================
    // CONFIGURATION (for testing)
    // ========================================================================

    public void setSimulateDelay(boolean simulate, long delayMs) {
        this.simulateDelay = simulate;
        this.delayMs = delayMs;
    }

    public void clear() {
        entrants.clear();
        registrations.clear();
        if (registrationRepo instanceof MockRegistrationRepository) {
            ((MockRegistrationRepository) registrationRepo).clear();
        }
    }

    public int getEntrantCount() {
        return entrants.size();
    }

    public int getRegistrationCount() {
        return registrations.size();
    }

    // ========================================================================
    // JOIN EVENT
    // ========================================================================

    @Override
    public void joinEvent(String userId, String eventId, String eventTitle,
                          RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();

                // Generate IDs
                String entrantId = generateEntrantId(userId, eventId);
                String registrationId = generateRegistrationId(userId, eventId);

                // Check if already joined
                if (entrants.containsKey(entrantId)) {
                    callback.onFailure(new Exception("Already joined this event"));
                    return;
                }

                // Create Entrant
                Entrant entrant = new Entrant();
                entrant.setEntrantId(entrantId);
                entrant.setUserId(userId);
                entrant.setEventId(eventId);
                entrant.setStatus(Entrant.Status.WAITING);
                entrant.setJoinedTimestamp(timestamp);
                entrant.setStatusTimestamp(timestamp);
                entrant.setGeolocationVerified(false);

                // Create Registration
                Registration registration = new Registration();
                registration.setRegistrationId(registrationId);
                registration.setUserId(userId);
                registration.setEventId(eventId);
                registration.setStatus(Registration.Status.JOINED);
                registration.setEventTitleSnapshot(eventTitle);
                registration.setRegisteredAt(timestamp);
                registration.setUpdatedAt(timestamp);

                // Store both
                entrants.put(entrantId, entrant);
                registrations.put(registrationId, registration);

                // Add to waiting list via RegistrationRepository
                registrationRepo.joinWaitingList(eventId, entrant,
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d(TAG, "Successfully joined event");
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Rollback
                                entrants.remove(entrantId);
                                registrations.remove(registrationId);
                                callback.onFailure(e);
                            }
                        });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // LEAVE EVENT
    // ========================================================================

    @Override
    public void leaveEvent(String userId, String eventId,
                           RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                String entrantId = generateEntrantId(userId, eventId);
                String registrationId = generateRegistrationId(userId, eventId);

                if (!entrants.containsKey(entrantId)) {
                    callback.onFailure(new Exception("Not joined this event"));
                    return;
                }

                // Leave waiting list
                registrationRepo.leaveWaitingList(eventId, entrantId,
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Remove from local storage
                                entrants.remove(entrantId);
                                registrations.remove(registrationId);

                                Log.d(TAG, "Successfully left event");
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // ACCEPT INVITATION
    // ========================================================================

    @Override
    public void acceptInvitation(String userId, String eventId,
                                 RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                String entrantId = generateEntrantId(userId, eventId);
                String registrationId = generateRegistrationId(userId, eventId);
                long timestamp = System.currentTimeMillis();

                Entrant entrant = entrants.get(entrantId);
                Registration registration = registrations.get(registrationId);

                if (entrant == null || registration == null) {
                    callback.onFailure(new Exception("Registration not found"));
                    return;
                }

                if (entrant.getStatus() != Entrant.Status.INVITED) {
                    callback.onFailure(new Exception("Not invited"));
                    return;
                }

                // Accept via RegistrationRepository
                registrationRepo.acceptInvitation(eventId, entrantId,
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Update local Registration
                                registration.setStatus(Registration.Status.ENROLLED);
                                registration.setEnrolledAt(timestamp);
                                registration.setUpdatedAt(timestamp);
                                registrations.put(registrationId, registration);

                                // Update local Entrant
                                entrant.setStatus(Entrant.Status.ENROLLED);
                                entrant.setStatusTimestamp(timestamp);
                                entrants.put(entrantId, entrant);

                                Log.d(TAG, "Successfully accepted invitation");
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // DECLINE INVITATION
    // ========================================================================

    @Override
    public void declineInvitation(String userId, String eventId, String reason,
                                  RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                String entrantId = generateEntrantId(userId, eventId);
                String registrationId = generateRegistrationId(userId, eventId);
                long timestamp = System.currentTimeMillis();

                Entrant entrant = entrants.get(entrantId);
                Registration registration = registrations.get(registrationId);

                if (entrant == null || registration == null) {
                    callback.onFailure(new Exception("Registration not found"));
                    return;
                }

                // Decline via RegistrationRepository
                registrationRepo.declineInvitation(eventId, entrantId,
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Update local Registration
                                registration.setStatus(Registration.Status.DECLINED);
                                //TODO: Check if needed
//                                registration.setDeclineReason(reason);
                                registration.setUpdatedAt(timestamp);
                                registrations.put(registrationId, registration);

                                // Update local Entrant
                                entrant.setStatus(Entrant.Status.CANCELLED);
                                entrant.setStatusTimestamp(timestamp);
                                entrant.setDeclineReason(reason);
                                entrants.put(entrantId, entrant);

                                // Draw replacement
                                drawReplacementIfNeeded(eventId, callback);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET USER'S CURRENT STATUS
    // ========================================================================

    @Override
    public void getUserEventStatus(String userId, String eventId,
                                   RepositoryCallback<EntrantRegistrationStatus> callback) {
        executeAsync(() -> {
            try {
                String entrantId = generateEntrantId(userId, eventId);
                String registrationId = generateRegistrationId(userId, eventId);

                Entrant entrant = entrants.get(entrantId);
                Registration registration = registrations.get(registrationId);

                EntrantRegistrationStatus status = new EntrantRegistrationStatus();
                status.setEntrant(entrant);
                status.setRegistration(registration);

                callback.onSuccess(status);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String generateEntrantId(String userId, String eventId) {
        return userId + "_" + eventId + "_entrant";
    }

    private String generateRegistrationId(String userId, String eventId) {
        return userId + "_" + eventId;
    }

    private void drawReplacementIfNeeded(String eventId,
                                         RepositoryCallback<Void> callback) {
        registrationRepo.drawReplacement(eventId, new RepositoryCallback<Entrant>() {
            @Override
            public void onSuccess(Entrant replacement) {
                if (replacement != null) {
                    Log.d(TAG, "Drew replacement: " + replacement.getUserId());

                    // Update replacement's Registration
                    String regId = generateRegistrationId(replacement.getUserId(), eventId);
                    Registration reg = registrations.get(regId);

                    if (reg != null) {
                        long timestamp = System.currentTimeMillis();
                        reg.setStatus(Registration.Status.SELECTED);
                        reg.setSelectedAt(timestamp);
                        reg.setUpdatedAt(timestamp);
                        registrations.put(regId, reg);
                    }
                }
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Failed to draw replacement", e);
                callback.onSuccess(null); // Don't fail the decline
            }
        });
    }

    private void executeAsync(Runnable task) {
        if (simulateDelay) {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                task.run();
            }).start();
        } else {
            task.run();
        }
    }

    // ========================================================================
    // TEST HELPER METHODS
    // ========================================================================

    public Entrant getEntrant(String entrantId) {
        return entrants.get(entrantId);
    }

    public Registration getRegistration(String registrationId) {
        return registrations.get(registrationId);
    }

    public void addEntrant(Entrant entrant) {
        entrants.put(entrant.getEntrantId(), entrant);
    }

    public void addRegistration(Registration registration) {
        registrations.put(registration.getRegistrationId(), registration);
    }
}
