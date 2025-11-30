package com.example.lotterysystemproject.testfakes;

import com.example.lotterysystemproject.models.NotificationItem;

/** Test-only repo for invite decisions (accept/decline) */
public class TestInvitationDecisionRepo {
    private final FakeKeyValueStore kv;
    public TestInvitationDecisionRepo(FakeKeyValueStore kv) { this.kv = kv; }

    private String key(String id) { return "resp_" + id; }

    /*
    public void save(String notifId, NotificationItem.InvitationResponse r) {
        kv.putString(key(notifId), r.name());
    }

    public NotificationItem.InvitationResponse load(String notifId) {
        String raw = kv.getString(key(notifId), NotificationItem.InvitationResponse.NONE.name());
        try { return NotificationItem.InvitationResponse.valueOf(raw); }
        catch (IllegalArgumentException e) { return NotificationItem.InvitationResponse.NONE; }
    }

    public void clearOne(String notifId) { kv.remove(key(notifId)); }
    public void clearAll() { kv.clear(); }

     */
}
