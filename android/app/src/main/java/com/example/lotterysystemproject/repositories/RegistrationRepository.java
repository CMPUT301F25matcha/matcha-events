package com.example.matchamonday.repositories;

import com.example.matchamonday.models.Entrant;
import com.example.matchamonday.models.Registration;

import java.util.List;

public interface RegistrationRepository {

    // Entrant joins waiting list
    void joinWaitingList(String eventId, Entrant entrant,
                         RepositoryCallback<Void> callback);

    // Entrant leaves waiting list
    void leaveWaitingList(String eventId, String entrantId,
                          RepositoryCallback<Void> callback);

    // Organizer views waiting list
    void getWaitingList(String eventId,
                        RepositoryCallback<List<Entrant>> callback);

    // Organizer sets max waiting list size (optional)
    void setMaxEntrants(String eventId, int max,
                        RepositoryCallback<Void> callback);

    // Number of entrants on waiting list
    void getWaitingListCount(String eventId, RepositoryCallback<Integer> callback);

    // Draw selected entrants
    void drawEntrants(String eventId, int numberOfEntrantsToSelect,
                      RepositoryCallback<List<Entrant>> callback);

    // Draw replacement entrant
    void drawReplacement(String eventId,
                         RepositoryCallback<Entrant> callback);

    // Accept invitation
    void acceptInvitation(String eventId, String entrantId,
                          RepositoryCallback<Void> callback);

    // Decline invitation
    void declineInvitation(String eventId, String entrantId,
                           RepositoryCallback<Void> callback);

    // Organizer views selected entrants
    void getSelectedEntrants(String eventId,
                             RepositoryCallback<List<Entrant>> callback);

    // Organizer views declined/cancelled entrants
    void getCancelledEntrants(String eventId,
                              RepositoryCallback<List<Entrant>> callback);

    // Final list of enrolled entrants
    void getFinalEnrolled(String eventId,
                          RepositoryCallback<List<Entrant>> callback);

    // Export final list as CSV
    void exportFinalListCsv(String eventId,
                            RepositoryCallback<String> callback);

    // Get user's registration history (for entrant dashboard)
    void getUserRegistrations(String userId,
                              RepositoryCallback<List<Registration>> callback);

    // Real-time listener for waiting list (organizer dashboard)
    void listenToWaitingList(String eventId,
                             RepositoryListener<List<Entrant>> listener);

    // Real-time listener for selected entrants
    void listenToSelectedEntrants(String eventId,
                                  RepositoryListener<List<Entrant>> listener);

    // Batch operations for lottery (more efficient)
    void updateEntrantsStatus(String eventId, List<String> entrantIds,
                              Entrant.Status newStatus,
                              RepositoryCallback<Void> callback);

    // Check if entrant is on waiting list
    void isOnWaitingList(String eventId, String entrantId,
                         RepositoryCallback<Boolean> callback);
}

