package com.example.lotterysystemproject.repositories.firebase;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.EntrantRegistrationStatus;
import com.example.lotterysystemproject.models.Registration;
import com.example.lotterysystemproject.repositories.EntrantRegistrationRepository;
import com.example.lotterysystemproject.repositories.RegistrationRepository;
import com.example.lotterysystemproject.repositories.RepositoryCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

/**
 * Coordinates between Entrant and Registration tables
 * Ensures both tables stay in sync during operations
 *
 * This is the HIGH-LEVEL repository that entrants use.
 * RegistrationRepository is the LOW-LEVEL repository for individual operations.
 */
public class FirebaseEntrantRegistrationRepository implements EntrantRegistrationRepository {

    private static final String TAG = "EntrantRegRepo";
    private static final String COLLECTION_ENTRANTS = "entrants";
    private static final String COLLECTION_REGISTRATIONS = "registrations";
    private static final String COLLECTION_EVENTS = "events";

    private final FirebaseFirestore db;
    private final RegistrationRepository registrationRepo;

    public FirebaseEntrantRegistrationRepository(RegistrationRepository registrationRepo) {
        this.db = FirebaseFirestore.getInstance();
        this.registrationRepo = registrationRepo;
    }

    public FirebaseEntrantRegistrationRepository() {
        this(new FirebaseRegistrationRepository());
    }

    // ========================================================================
    // JOIN EVENT (US 01.01.01)
    // Creates BOTH Entrant and Registration records atomically
    // ========================================================================

    @Override
    public void joinEvent(String userId, String eventId, String eventTitle,
                          RepositoryCallback<Void> callback) {
        Log.d(TAG, "joinEvent: userId=" + userId + ", eventId=" + eventId);

        long timestamp = System.currentTimeMillis();

        // Generate IDs
        String entrantId = generateEntrantId(userId, eventId);
        String registrationId = generateRegistrationId(userId, eventId);

        // Create Entrant record
        Entrant entrant = new Entrant();
        entrant.setEntrantId(entrantId);
        entrant.setUserId(userId);
        entrant.setEventId(eventId);
        entrant.setStatus(Entrant.Status.WAITING);
        entrant.setJoinedTimestamp(timestamp);
        entrant.setStatusTimestamp(timestamp);
        entrant.setGeolocationVerified(false); // Set by caller if needed

        // Create Registration record
        Registration registration = new Registration();
        registration.setRegistrationId(registrationId);
        registration.setUserId(userId);
        registration.setEventId(eventId);
        registration.setStatus(Registration.Status.JOINED);
        registration.setEventTitleSnapshot(eventTitle);
        registration.setRegisteredAt(timestamp);
        registration.setUpdatedAt(timestamp);

        // Use batch write for atomicity
        WriteBatch batch = db.batch();

        // TODO: check out how to implement toMap in entrant
        batch.set(db.collection(COLLECTION_ENTRANTS).document(entrantId),
                entrant.toMap());
        // TODO: check out how to implement toMap in registration
        batch.set(db.collection(COLLECTION_REGISTRATIONS).document(registrationId),
                registration.toMap());

        // Note: RegistrationRepository will handle incrementing currentWaitingCount

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully joined event");

                    // Now add to waiting list (which updates denormalized count)
                    registrationRepo.joinWaitingList(eventId, entrant,
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Rollback: delete the records we just created
                                    rollbackJoin(entrantId, registrationId);
                                    callback.onFailure(e);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to join event", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // LEAVE EVENT (US 01.01.02)
    // Updates BOTH Entrant and Registration records atomically
    // ========================================================================

    @Override
    public void leaveEvent(String userId, String eventId,
                           RepositoryCallback<Void> callback) {
        Log.d(TAG, "leaveEvent: userId=" + userId + ", eventId=" + eventId);

        String entrantId = generateEntrantId(userId, eventId);
        String registrationId = generateRegistrationId(userId, eventId);

        // Step 1: Leave waiting list (handled by RegistrationRepository)
        registrationRepo.leaveWaitingList(eventId, entrantId,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Step 2: Delete Registration record
                        db.collection(COLLECTION_REGISTRATIONS)
                                .document(registrationId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully left event");
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete registration", e);
                                    callback.onFailure(e);
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to leave waiting list", e);
                        callback.onFailure(e);
                    }
                });
    }

    // ========================================================================
    // ACCEPT INVITATION (US 01.05.02)
    // Updates BOTH Entrant and Registration to ENROLLED status
    // ========================================================================

    @Override
    public void acceptInvitation(String userId, String eventId,
                                 RepositoryCallback<Void> callback) {
        Log.d(TAG, "acceptInvitation: userId=" + userId + ", eventId=" + eventId);

        String entrantId = generateEntrantId(userId, eventId);
        String registrationId = generateRegistrationId(userId, eventId);
        long timestamp = System.currentTimeMillis();

        // Step 1: Accept in RegistrationRepository (updates Entrant + Event count)
        registrationRepo.acceptInvitation(eventId, entrantId,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Step 2: Update Registration record
                        db.collection(COLLECTION_REGISTRATIONS)
                                .document(registrationId)
                                .update(
                                        "status", Registration.Status.ENROLLED.name(),
                                        "enrolledAt", timestamp,
                                        "updatedAt", timestamp
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully accepted invitation");
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update registration", e);
                                    // Rollback entrant status
                                    rollbackAccept(entrantId, eventId);
                                    callback.onFailure(e);
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to accept in registration repo", e);
                        callback.onFailure(e);
                    }
                });
    }

    // ========================================================================
    // DECLINE INVITATION (US 01.05.03)
    // Updates BOTH Entrant and Registration to CANCELLED/DECLINED status
    // ========================================================================

    @Override
    public void declineInvitation(String userId, String eventId, String reason,
                                  RepositoryCallback<Void> callback) {
        Log.d(TAG, "declineInvitation: userId=" + userId + ", eventId=" + eventId);

        String entrantId = generateEntrantId(userId, eventId);
        String registrationId = generateRegistrationId(userId, eventId);
        long timestamp = System.currentTimeMillis();

        // Step 1: Decline in RegistrationRepository (updates Entrant)
        registrationRepo.declineInvitation(eventId, entrantId,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Step 2: Update Registration record
                        db.collection(COLLECTION_REGISTRATIONS)
                                .document(registrationId)
                                .update(
                                        "status", Registration.Status.DECLINED.name(),
                                        "declineReason", reason,
                                        "updatedAt", timestamp
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully declined invitation");

                                    // Step 3: Draw replacement (US 01.05.01, US 02.05.03)
                                    drawReplacementIfNeeded(eventId, callback);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update registration", e);
                                    callback.onFailure(e);
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to decline in registration repo", e);
                        callback.onFailure(e);
                    }
                });
    }

    // ========================================================================
    // GET USER'S CURRENT STATUS (For UI display)
    // ========================================================================

    @Override
    public void getUserEventStatus(String userId, String eventId,
                                   RepositoryCallback<EntrantRegistrationStatus> callback) {
        Log.d(TAG, "getUserEventStatus: userId=" + userId + ", eventId=" + eventId);

        String entrantId = generateEntrantId(userId, eventId);
        String registrationId = generateRegistrationId(userId, eventId);

        // Fetch both records in parallel
        db.collection(COLLECTION_ENTRANTS).document(entrantId)
                .get()
                .addOnSuccessListener(entrantDoc -> {
                    db.collection(COLLECTION_REGISTRATIONS).document(registrationId)
                            .get()
                            .addOnSuccessListener(regDoc -> {
                                Entrant entrant = entrantDoc.exists() ?
                                        entrantDoc.toObject(Entrant.class) : null;
                                Registration registration = regDoc.exists() ?
                                        regDoc.toObject(Registration.class) : null;

                                EntrantRegistrationStatus status = new EntrantRegistrationStatus(entrant, registration);

                                callback.onSuccess(status);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
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

    /**
     * Draw replacement entrant when someone declines (US 01.05.01)
     */
    private void drawReplacementIfNeeded(String eventId,
                                         RepositoryCallback<Void> callback) {
        registrationRepo.drawReplacement(eventId, new RepositoryCallback<Entrant>() {
            @Override
            public void onSuccess(Entrant replacement) {
                if (replacement != null) {
                    Log.d(TAG, "Drew replacement entrant: " + replacement.getUserId());

                    // Update the replacement's Registration record
                    String regId = generateRegistrationId(replacement.getUserId(), eventId);
                    long timestamp = System.currentTimeMillis();

                    db.collection(COLLECTION_REGISTRATIONS)
                            .document(regId)
                            .update(
                                    "status", Registration.Status.SELECTED.name(),
                                    "selectedAt", timestamp,
                                    "updatedAt", timestamp
                            )
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Updated replacement registration");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to update replacement registration", e);
                                // Still succeed the decline, just log warning
                                callback.onSuccess(null);
                            });
                } else {
                    Log.d(TAG, "No replacement available");
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Failed to draw replacement", e);
                // Don't fail the entire operation if replacement fails
                callback.onSuccess(null);
            }
        });
    }

    /**
     * Rollback join operation if waiting list update fails
     */
    private void rollbackJoin(String entrantId, String registrationId) {
        Log.w(TAG, "Rolling back join operation");

        WriteBatch batch = db.batch();
        batch.delete(db.collection(COLLECTION_ENTRANTS).document(entrantId));
        batch.delete(db.collection(COLLECTION_REGISTRATIONS).document(registrationId));

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Rollback successful"))
                .addOnFailureListener(e -> Log.e(TAG, "Rollback failed", e));
    }

    /**
     * Rollback accept operation if registration update fails
     */
    private void rollbackAccept(String entrantId, String eventId) {
        Log.w(TAG, "Rolling back accept operation");

        db.collection(COLLECTION_ENTRANTS).document(entrantId)
                .update("status", Entrant.Status.INVITED.name())
                .addOnSuccessListener(aVoid -> {
                    // Also decrement enrollment count
                    db.collection(COLLECTION_EVENTS).document(eventId)
                            .update("currentEnrollmentCount",
                                    com.google.firebase.firestore.FieldValue.increment(-1));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Rollback failed", e));
    }
}