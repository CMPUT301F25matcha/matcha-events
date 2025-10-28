package com.example.lotterysystemproject.controller;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Manages access to Firebase Firestore and Storage instances.
 *
 * <p>This class implements the Singleton Design pattern to ensure that only one instance of
 * FirebaseManager exists throughout the application.<p>
 *
 * <p>It provides global access to the Firebase Firestore and Firebase Storage services used in
 * the app.</p>
 *
 */
public class FirebaseManager {

    // Reference to the Firebase Firestore database.
    private final FirebaseFirestore db;

    // Singleton instance of FirebaseManager
    private static FirebaseManager instance;

    // Reference to Firebase Storage
    private final FirebaseStorage storage;

    /**
     * Initializes Firestore and Storage instances.
     */
    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Uses double-checked locking to ensure thread-safe
     *
     * @return The singleton instance of FirebaseManager
     */
    public static FirebaseManager getInstance() {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager();
                }
            }
        }
        return instance;
    }

    public FirebaseFirestore getDatabase() {
        return db;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }



}
