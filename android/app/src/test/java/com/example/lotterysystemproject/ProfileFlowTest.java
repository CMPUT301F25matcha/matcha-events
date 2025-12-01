package com.example.lotterysystemproject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.UserRepository;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.EventHistoryItem;
import com.example.lotterysystemproject.models.User;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests around profile-related user stories:
 * - US 01.02.02 update profile
 * - US 01.02.03 registration history
 * - US 01.02.04 delete profile
 */
public class ProfileFlowTest {

    // US 01.02.02 – Update profile
    /**
     * Updating profile info should send a User with updated fields
     * to UserRepository.createOrUpdateUser.
     */
    @Test
    public void updateProfileCallsRepositoryWithUpdatedUser() {
        UserRepository mockRepo = mock(UserRepository.class);

        // "original" user
        User original = new User();
        original.setId("usr_123");
        original.setName("Old Name");
        original.setEmail("old@example.com");
        original.setPhone("000-0000");

        // Controller builds updated user object
        User updated = new User();
        updated.setId(original.getId());
        updated.setName("New Name");
        updated.setEmail("new@example.com");
        updated.setPhone("555-1234");

        // Simulate screen/controller calling repo
        mockRepo.createOrUpdateUser(updated, null);

        // Capture what was actually sent to the repo
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockRepo, times(1)).createOrUpdateUser(userCaptor.capture(), any());

        User saved = userCaptor.getValue();
        assertNotNull(saved);

        // ID should be preserved
        assertEquals("usr_123", saved.getId());

        // Profile data should be updated
        assertEquals("New Name",       saved.getName());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("555-1234",        saved.getPhone());
    }



    // US 01.02.03 – Registration history mapping
    /**
     * Entrant statuses should map to correct labels in EventHistoryItem.
     */
    @Test
    public void entrantStatusMappedToHistoryLabel() {
        List<EventHistoryItem> items = new ArrayList<>();

        // WAITING
        Entrant waiting = new Entrant();
        waiting.setStatus(Entrant.Status.WAITING);
        items.add(toHistoryItem("Swim Class", waiting));

        // INVITED
        Entrant invited = new Entrant();
        invited.setStatus(Entrant.Status.INVITED);
        items.add(toHistoryItem("Dance", invited));

        // ENROLLED
        Entrant enrolled = new Entrant();
        enrolled.setStatus(Entrant.Status.ENROLLED);
        items.add(toHistoryItem("Piano", enrolled));

        // CANCELLED
        Entrant cancelled = new Entrant();
        cancelled.setStatus(Entrant.Status.CANCELLED);
        items.add(toHistoryItem("Art Camp", cancelled));

        // NULL status (defensive case)
        Entrant unknown = new Entrant();
        unknown.setStatus(null);
        items.add(toHistoryItem("Mystery Event", unknown));

        // Check labels
        assertEquals("On Waiting List", items.get(0).getStatus());
        assertEquals("Invited",items.get(1).getStatus());
        assertEquals("Enrolled",items.get(2).getStatus());
        assertEquals("Cancelled / Declined", items.get(3).getStatus());
        assertEquals("Unknown", items.get(4).getStatus());
    }

    /**
     * Mirror of the label mapping in EventRegistrationHistoryFragment.
     */
    private EventHistoryItem toHistoryItem(String eventName, Entrant entrant) {
        String statusLabel;
        if (entrant.getStatus() != null) {
            switch (entrant.getStatus()) {
                case WAITING:
                    statusLabel = "On Waiting List";
                    break;
                case INVITED:
                    statusLabel = "Invited";
                    break;
                case ENROLLED:
                    statusLabel = "Enrolled";
                    break;
                case CANCELLED:
                default:
                    statusLabel = "Cancelled / Declined";
                    break;
            }
        } else {
            statusLabel = "Unknown";
        }

        // Date not important here
        return new EventHistoryItem(eventName, statusLabel, "-");
    }


    // US 01.02.04 – Delete profile
    /**
     * Deleting a profile should call deactivateAccount(userId) on the repository
     * with a non-null callback.
     */
    @Test
    public void deleteProfileCallsDeactivateAccount() {
        UserRepository mockRepo = mock(UserRepository.class);

        String userId = "usr_123";

        // Real callback instance
        RepositoryCallback<Void> callback = new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }

            @Override
            public void onFailure(Exception e) { }
        };

        // Simulate DeleteProfileActivity's call
        mockRepo.deactivateAccount(userId, callback);

        // Capture the arguments
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<RepositoryCallback> cbCaptor =
                ArgumentCaptor.forClass(RepositoryCallback.class);

        verify(mockRepo, times(1))
                .deactivateAccount(idCaptor.capture(), cbCaptor.capture());

        assertEquals("usr_123", idCaptor.getValue());
        assertNotNull(cbCaptor.getValue());
    }
}
