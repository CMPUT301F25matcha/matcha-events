package com.example.lotterysystemproject.firebasemanager;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.models.User;
import com.example.lotterysystemproject.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Firebase implementation of AdminRepository for production use.
 * Connects to real Firebase Firestore and Storage backend for admin operations.
 */
public class FirebaseAdminRepository implements AdminRepository {

    // ===================== FIREBASE INSTANCES =====================
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    /**
     * Constructor initializes Firebase instances.
     */
    public FirebaseAdminRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // ===================== ACCESSORS =====================

    @Nullable
    @Override
    public FirebaseFirestore getDatabase() {
        return db;
    }

    @Nullable
    @Override
    public FirebaseStorage getStorage() {
        return storage;
    }

    // ===================== EVENT OPERATIONS =====================

    @Override
    public void addEvent(Event eventAdmin, AdminCallback callback) {
        if (eventAdmin == null || eventAdmin.getId() == null || eventAdmin.getId().isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Event or EventID cannot be null"));
            }
            return;
        }

        db.collection("events").document(eventAdmin.getId())
                .set(eventAdmin)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> eventAdminList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Event eventAdmin = doc.toObject(Event.class);
                        if (eventAdmin != null) {
                            eventAdmin.setId(doc.getId());
                            eventAdminList.add(eventAdmin);
                        }
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(eventAdminList);
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

    @Override
    public void listenToAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        db.collection("events").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                if (onError != null) onError.accept(e);
                return;
            }

            List<Event> eventAdminList = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Event eventAdmin = doc.toObject(Event.class);
                    if (eventAdmin != null) {
                        eventAdmin.setId(doc.getId());
                        eventAdminList.add(eventAdmin);
                    }
                }
            }

            if (onSuccess != null) onSuccess.accept(eventAdminList);
        });
    }

    @Override
    public void deleteEvent(String eventId, AdminCallback callback) {
        if (eventId == null || eventId.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Event ID cannot be null or empty"));
            }
            return;
        }

        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    // ===================== USER OPERATIONS =====================

    @Override
    public void deleteUser(String userId, AdminCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("User ID cannot be null or empty"));
            }
            return;
        }

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    // ===================== IMAGE OPERATIONS =====================

    @Override
    public void getAllImages(Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        storage.getReference().child("event_posters")
                .listAll()
                .addOnSuccessListener(listResult -> {
                    List<String> urls = new ArrayList<>();
                    List<StorageReference> items = listResult.getItems();

                    if (items.isEmpty()) {
                        if (onSuccess != null) onSuccess.accept(urls);
                        return;
                    }

                    for (StorageReference item : items) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            urls.add(uri.toString());
                            if (urls.size() == items.size() && onSuccess != null) {
                                onSuccess.accept(urls);
                            }
                        }).addOnFailureListener(e -> {
                            if (onError != null) onError.accept(e);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    @Override
    public void deleteImage(String imageUrl, AdminCallback callback) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e);
                    });
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }

    @Override
    public void deleteMultipleImages(List<String> imageUrls, BiConsumer<Integer, Exception> onComplete) {
        int[] remaining = {imageUrls.size()};
        int[] deleteCount = {0};

        for (String url : imageUrls) {
            deleteImage(url, new AdminCallback() {
                @Override
                public void onSuccess() {
                    deleteCount[0]++;
                    if (--remaining[0] == 0 && onComplete != null) {
                        onComplete.accept(deleteCount[0], null);
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (--remaining[0] == 0 && onComplete != null) {
                        onComplete.accept(deleteCount[0], e);
                    }
                }
            });
        }
    }

    @Override
    public void getAllOrganizers(Consumer<List<User>> onSuccess, Consumer<Exception> onError) {
        db.collection("users")
                .whereEqualTo("role", "organizer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> organizers = new ArrayList<>();
                    for (DocumentSnapshot doc: queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) organizers.add(user);
                    }
                    if (onSuccess != null) onSuccess.accept(organizers);
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept((e));
                });
    }

    @Override
    public void listenToAllOrganizers(Consumer<List<User>> onSuccess, Consumer<Exception> onError) {
        db.collection("users")
                .whereEqualTo("role", "organizer")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        if (onError != null) onError.accept(e);
                        return;
                    }

                    List<User> organizers = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            if (user != null) organizers.add(user);
                        }
                    }
                    if (onSuccess != null) onSuccess.accept(organizers);
                });
    }
}