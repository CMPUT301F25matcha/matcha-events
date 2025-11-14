package com.example.lotterysystemproject.firebasemanager;

import androidx.lifecycle.LiveData;
import com.example.lotterysystemproject.models.Entrant;

import java.util.List;

/**
 * Repository interface for managing entrant data and lottery operations.
 */
public interface EntrantRepository {

    // ===================== CALLBACK INTERFACES =====================

    /**
     * Callback for lottery draw completion.
     */
    interface OnLotteryCompleteListener {
        void onComplete(List<Entrant> winners);
        void onFailure(String error);
    }

    /**
     * Callback for generic success or failure actions.
     */
    interface OnActionCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Callback for replacement entrant draw.
     */
    interface OnReplacementDrawnListener {
        void onSuccess(Entrant replacement);
        void onFailure(String error);
    }

    interface OnUserInfoListener {
        void onSuccess(String hostId, String hostName, String role);
        void onFailure(String error);
    }

    // ===================== ENTRANT OPERATIONS =====================

    /**
     * Returns LiveData list of entrants for the given event ID.
     * @param eventId ID of the event to fetch entrants for.
     * @return LiveData containing list of entrants.
     */
    LiveData<List<Entrant>> getEntrants(String eventId);

    /**
     * Performs a random lottery draw to select entrants.
     * @param eventId ID of the event for lottery draw.
     * @param count Number of entrants to select.
     * @param listener Callback to report completion or errors.
     */
    void drawLottery(String eventId, int count, OnLotteryCompleteListener listener);

    /**
     * Cancels an entrant's participation by ID.
     * @param entrantId ID of the entrant to cancel.
     * @param listener Callback to signal success or failure.
     */
    void cancelEntrant(String entrantId, OnActionCompleteListener listener);

    /**
     * Draws a replacement entrant from the waiting list.
     * @param eventId ID of the event for replacement draw.
     * @param listener Callback to report result or errors.
     */
    void drawReplacement(String eventId, OnReplacementDrawnListener listener);

    /**
     * gets the current user info
     * @param deviceId
     * @param listener
     */
    void getCurrentUserInfo(String deviceId, OnUserInfoListener listener);
}