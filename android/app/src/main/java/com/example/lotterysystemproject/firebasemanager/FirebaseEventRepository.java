package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;

import com.example.lotterysystemproject.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirebaseEventRepository implements EventRepository {

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public FirebaseEventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public void createEvent(Event event, RepositoryCallback<String> callback) {
        db.collection("events").document(event.getEventId())
                .set(event)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(event.getEventId());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void updateEvent(Event event, RepositoryCallback<Void> callback) {
        db.collection("events").document(event.getEventId())
                .set(event)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        db.collection("events").document(eventId)
                .update("isActive", false)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getEvent(String eventId, RepositoryCallback<Event> callback) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Event event = doc.toObject(Event.class);
                        if (callback != null) callback.onSuccess(event);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("isActive", true)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null) events.add(event);
                    });
                    if (callback != null) callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void filterEvents(String interest, long startTime, long endTime, RepositoryCallback<List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("isActive", true)
                .whereEqualTo("category", interest)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null &&
                                event.getStartDate().getTime() >= startTime &&
                                event.getStartDate().getTime() <= endTime) {
                            events.add(event);
                        }
                    });
                    if (callback != null) callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void uploadEventPoster(String eventId, Uri imageUri, RepositoryCallback<String> callback) {
        StorageReference ref = storage.getReference("event_posters/" + eventId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                db.collection("events").document(eventId)
                                        .update("eventPosterUrl", imageUrl)
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
    public void deleteEventPoster(String eventId, RepositoryCallback<Void> callback) {
        StorageReference ref = storage.getReference("event_posters/" + eventId + ".jpg");

        ref.delete()
                .addOnSuccessListener(v -> {
                    db.collection("events").document(eventId)
                            .update("eventPosterUrl", null)
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
    public void setGeolocationRequired(String eventId, boolean required, RepositoryCallback<Void> callback) {
        db.collection("events").document(eventId)
                .update("geolocationRequired", required)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getEventQrCodeUrl(String eventId, RepositoryCallback<String> callback) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String qrCode = doc.getString("promotionalQrCode");
                        if (callback != null) callback.onSuccess(qrCode);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void listenToEvent(String eventId, RepositoryListener<Event> listener) {
        db.collection("events").document(eventId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        Event event = doc.toObject(Event.class);
                        if (listener != null) listener.onDataChanged(event);
                    }
                });
    }

    @Override
    public void isRegistrationOpen(String eventId, RepositoryCallback<Boolean> callback) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Event event = doc.toObject(Event.class);
                        boolean isOpen = event.isRegistrationOpen();
                        if (callback != null) callback.onSuccess(isOpen);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void deleteEventsByOrganizer(String organizerId, RepositoryCallback<Void> callback) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> {
                        db.collection("events").document(doc.getId())
                                .update("isActive", false);
                    });
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getOrganizerEvents(String organizerId, RepositoryCallback<List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .whereEqualTo("isActive", true)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null) events.add(event);
                    });
                    if (callback != null) callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void searchEvents(String query, RepositoryCallback<List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();

                    querySnapshot.forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null && (
                                event.getName().toLowerCase().contains(lowerQuery) ||
                                        event.getDescription().toLowerCase().contains(lowerQuery) ||
                                        event.getCategory().toLowerCase().contains(lowerQuery)
                        )) {
                            events.add(event);
                        }
                    });
                    if (callback != null) callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void optOutOfNotifications(String userId, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("notificationsEnabled", false)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getNotificationPreferences(String userId, RepositoryCallback<Boolean> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean enabled = doc.getBoolean("notificationsEnabled");
                        if (callback != null) callback.onSuccess(enabled != null ? enabled : true);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void sendCustomNotification(String userId, String title, String message, RepositoryCallback<Void> callback) {
        // This would typically call Firebase Cloud Messaging
        // For now, just log and return success
        if (callback != null) callback.onSuccess(null);
    }

    @Override
    public void sendBulkNotifications(List<String> userIds, String title, String message, RepositoryCallback<Void> callback) {
        // This would typically call Firebase Cloud Messaging in batch
        if (callback != null) callback.onSuccess(null);
    }
}
