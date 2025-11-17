package com.example.lotterysystemproject.firebasemanager;

import com.example.lotterysystemproject.models.EntrantRegistrationStatus;

public interface EntrantRegistrationRepository {

    // ADDED: Join waiting list (creates BOTH Entrant and Registration)
    void joinEvent(String userId, String eventId, String eventTitle,
                   RepositoryCallback<Void> callback);

    // ADDED: Leave waiting list (updates both)
    void leaveEvent(String userId, String eventId,
                    RepositoryCallback<Void> callback);

    // ADDED: Accept invitation (updates both)
    void acceptInvitation(String userId, String eventId,
                          RepositoryCallback<Void> callback);

    // ADDED: Decline invitation (updates both)
    void declineInvitation(String userId, String eventId, String reason,
                           RepositoryCallback<Void> callback);

    // ADDED: Get user's current status for an event
    void getUserEventStatus(String userId, String eventId,
                            RepositoryCallback<EntrantRegistrationStatus> callback);
}
