package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;

import com.example.lotterysystemproject.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUserRepository implements UserRepository {

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public FirebaseUserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public void createOrUpdateUser(User user, RepositoryCallback<Void> callback) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getUserById(String userId, RepositoryCallback<User> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        if (callback != null) {
                            callback.onFailure(new Exception("User not found"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void deleteUser(String userId, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getAllUsers(RepositoryCallback<List<User>> callback) {
        db.collection("users")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) users.add(user);
                    });
                    if (callback != null) callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void updateNotificationPreferences(String userId, boolean enabled, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("notificationsEnabled", enabled)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void uploadProfilePicture(String userId, Uri imageUri, RepositoryCallback<String> callback) {
        StorageReference ref = storage.getReference("profile_pictures/" + userId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                db.collection("users").document(userId)
                                        .update("profilePictureUrl", imageUrl)
                                        .addOnSuccessListener(v -> {
                                            if (callback != null) callback.onSuccess(imageUrl);
                                        })
                                        .addOnFailureListener(e -> {
                                            if (callback != null) callback.onFailure(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void deleteProfilePicture(String userId, RepositoryCallback<Void> callback) {
        StorageReference ref = storage.getReference("profile_pictures/" + userId + ".jpg");

        ref.delete()
                .addOnSuccessListener(v -> {
                    db.collection("users").document(userId)
                            .update("profilePictureUrl", null)
                            .addOnSuccessListener(v2 -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void searchUsers(String query, RepositoryCallback<List<User>> callback) {
        db.collection("users")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();

                    querySnapshot.forEach(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null && (
                                user.getName().toLowerCase().contains(lowerQuery) ||
                                        user.getEmail().toLowerCase().contains(lowerQuery)
                        )) {
                            users.add(user);
                        }
                    });
                    if (callback != null) callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void listenToUser(String userId, RepositoryListener<User> listener) {
        db.collection("users").document(userId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (listener != null) listener.onDataChanged(user);
                    }
                });
    }

    @Override
    public void deactivateAccount(String userId, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("isActive", false)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void exportUserData(String userId, RepositoryCallback<String> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        String jsonData = userToJson(user);
                        if (callback != null) callback.onSuccess(jsonData);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    private String userToJson(User user) {
        return "{\n" +
                "  \"id\": \"" + user.getId() + "\",\n" +
                "  \"name\": \"" + user.getName() + "\",\n" +
                "  \"email\": \"" + user.getEmail() + "\",\n" +
                "  \"phone\": \"" + user.getPhone() + "\",\n" +
                "  \"role\": \"" + user.getRole() + "\"\n" +
                "}";
    }
}
