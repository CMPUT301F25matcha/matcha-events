package com.example.lotterysystemproject.testfakes;

/**
 * Test-only profile store that uses FakeKeyValueStore.
 * Keys mirror the production "UserPrefs" usage.
 */
public class TestProfileStore {
    private final FakeKeyValueStore kv;

    public TestProfileStore(FakeKeyValueStore kv) { this.kv = kv; }

    public void saveUser(String id, String name, String email, String phone, String role, boolean signedUp) {
        kv.putString("userId", id);
        kv.putString("userName", name);
        kv.putString("userEmail", email);
        kv.putString("userPhone", phone);
        kv.putString("userRole", role);
        kv.putBoolean("signedUp", signedUp);
    }

    /** US 01.02.04 — delete profile clears all local keys. */
    public void deleteUser(String userId) {
        kv.remove("userId");
        kv.remove("userName");
        kv.remove("userEmail");
        kv.remove("userPhone");
        kv.remove("userRole");
        kv.remove("signedUp"); // or set to false if you prefer
    }

    // getters for assertions
    public String  getUserId()   { return kv.getString("userId", null); }
    public String  getUserName() { return kv.getString("userName", null); }
    public String  getUserEmail(){ return kv.getString("userEmail", null); }
    public String  getUserPhone(){ return kv.getString("userPhone", null); }
    public String  getUserRole() { return kv.getString("userRole", null); }
    public boolean isSignedUp()  { return kv.getBoolean("signedUp", false); }
}
