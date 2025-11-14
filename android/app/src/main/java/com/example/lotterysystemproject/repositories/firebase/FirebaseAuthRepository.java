package com.example.matchamonday.repositories.firebase;

import android.content.Context;
import android.provider.Settings;

import com.example.matchamonday.models.User;
import com.example.matchamonday.repositories.AuthRepository;
import com.example.matchamonday.repositories.RepositoryCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class FirebaseAuthRepository implements AuthRepository {

    private final FirebaseFirestore db;
    private final Context context;

    public FirebaseAuthRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    @Override
    public void authenticateDevice(RepositoryCallback<User> callback) {
        String deviceId = getDeviceId();

        db.collection("users").document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        User newUser = new User();
                        newUser.setUserId(deviceId);
                        newUser.setRole("entrant");
                        newUser.setActive(true);

                        db.collection("users").document(deviceId)
                                .set(newUser)
                                .addOnSuccessListener(v -> {
                                    if (callback != null) callback.onSuccess(newUser);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onFailure(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void setUserRole(String userId, String role, RepositoryCallback<Void> callback) {
        if (!role.equals("entrant") && !role.equals("organizer") && !role.equals("admin")) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("Invalid role: " + role));
            }
            return;
        }

        db.collection("users").document(userId)
                .update("role", role)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    private String getDeviceId() {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (androidId == null || androidId.isEmpty()) {
            androidId = UUID.randomUUID().toString().replace("-", "");
        }

        return "usr_" + androidId;
    }
}
