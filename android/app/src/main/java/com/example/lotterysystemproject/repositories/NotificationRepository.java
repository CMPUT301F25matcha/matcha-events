package com.example.matchamonday.repositories;

import java.util.List;

public interface NotificationRepository {

    // Notify selected entrants ("you won the lottery")
    void notifySelectedEntrants(String eventId, List<String> entrantIds,
                                RepositoryCallback<Void> callback);

    // Notify rejected entrants ("you lost the lottery")
    void notifyRejectedEntrants(String eventId, List<String> entrantIds,
                                RepositoryCallback<Void> callback);

    // Organizer → all entrants on waiting list
    void notifyWaitingList(String eventId, RepositoryCallback<Void> callback);

    // Organizer → selected entrants
    void notifyChosen(String eventId, RepositoryCallback<Void> callback);

    // Organizer → cancelled entrants
    void notifyCancelled(String eventId, RepositoryCallback<Void> callback);

    // Admin reviews notification logs
    void getNotificationLog(RepositoryCallback<List<String>> callback);
}

