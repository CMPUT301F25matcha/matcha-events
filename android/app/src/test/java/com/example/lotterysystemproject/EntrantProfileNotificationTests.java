package com.example.lotterysystemproject;
import com.example.lotterysystemproject.Models.NotificationItem;
import com.example.lotterysystemproject.testfakes.FakeKeyValueStore;
import com.example.lotterysystemproject.testfakes.TestProfileStore;
import com.example.lotterysystemproject.testfakes.TestInvitationDecisionRepo;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntrantProfileNotificationTests {
    @Test
    public void deleteProfile_clearsLocalKeys() {
        FakeKeyValueStore kv = new FakeKeyValueStore();
        TestProfileStore store = new TestProfileStore(kv);

        store.saveUser("user-123","June","june@example.com","555-0000","entrant",true);
        store.deleteUser("user-123");

        assertNull(store.getUserId());
        assertNull(store.getUserName());
        assertNull(store.getUserEmail());
        assertNull(store.getUserPhone());
        assertNull(store.getUserRole());
        assertFalse(store.isSignedUp());
    }

    @Test
    public void acceptInvitation_persistsAccepted() {
        TestInvitationDecisionRepo repo = new TestInvitationDecisionRepo(new FakeKeyValueStore());
        String notifId = "event-123:reg-abc";

        repo.save(notifId, NotificationItem.InvitationResponse.ACCEPTED);

        assertEquals(NotificationItem.InvitationResponse.ACCEPTED, repo.load(notifId));
    }

    @Test
    public void declineInvitation_persistsDeclined() {
        TestInvitationDecisionRepo repo = new TestInvitationDecisionRepo(new FakeKeyValueStore());
        String notifId = "event-999:reg-zzz";

        repo.save(notifId, NotificationItem.InvitationResponse.DECLINED);

        assertEquals(NotificationItem.InvitationResponse.DECLINED, repo.load(notifId));
    }

    @Test
    public void clearSingleInvitation_resetsToNone() {
        TestInvitationDecisionRepo repo = new TestInvitationDecisionRepo(new FakeKeyValueStore());
        String notifId = "event-42:reg-001";

        repo.save(notifId, NotificationItem.InvitationResponse.ACCEPTED);
        repo.clearOne(notifId);

        assertEquals(NotificationItem.InvitationResponse.NONE, repo.load(notifId));
    }
}
