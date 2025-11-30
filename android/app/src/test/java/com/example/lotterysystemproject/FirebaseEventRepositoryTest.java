package com.example.lotterysystemproject;

import androidx.lifecycle.LiveData;

import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.FirebaseEventRepository;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.Registration;
import com.example.lotterysystemproject.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.junit.TestFinishedEvent;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseEventRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private FirebaseStorage mockStorage;

    @Mock
    private CollectionReference mockEventsCollection;

    @Mock
    private CollectionReference mockUsersCollection;
    @Mock
    private CollectionReference mockRegistrationsCollection;

    @Mock
    private DocumentReference mockEventDocument;

    @Mock
    private DocumentReference mockUserDocument;

    @Mock
    private DocumentReference mockRegistrationDocument;

    @Mock
    private Task<Void> mockVoidTask;

    @Mock
    private Task<DocumentSnapshot> mockDocumentTask;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private EventRepository.RepositoryCallback mockCallback;

    @Mock
    private Query mockQuery;

    @Mock
    private ListenerRegistration mockListenerRegistration;

    private FirebaseEventRepository repository;

    // Test data
    private final String TEST_EVENT_ID = "test-event-123";
    private final String TEST_USER_ID = "test-user-456";
    private final String TEST_EVENT_TITLE = "Test Event";

    @Before
    public void setUp() {
        // Setup collection references
        when(mockFirestore.collection("events")).thenReturn(mockEventsCollection);
        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);


        // Setup document references
        when(mockEventsCollection.document(anyString())).thenReturn(mockEventDocument);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument);


        repository = new FirebaseEventRepository(mockFirestore, mockStorage);
    }

    @Test
    public void addUser_withValidUser_callsFirestoreSet() {
        User testUser = new User();
        testUser.setId(TEST_USER_ID);

        // Mock Firestore "set" call
        when(mockUserDocument.set(any(User.class))).thenReturn(mockVoidTask);

        // Simulate success callback
        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        }).when(mockVoidTask).addOnSuccessListener(any());

        repository.addUser(testUser, mockCallback);

        verify(mockUserDocument).set(testUser);
        verify(mockCallback).onSuccess();


    }

    @Test
    public void joinWaitingList_callsOnSuccess_whenEventExists() {
        // mock .get()
        when(mockEventDocument.get()).thenReturn(mockDocumentTask);
        when(mockDocumentSnapshot.exists()).thenReturn(true);

        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocumentSnapshot);
            return mockDocumentTask;
        }).when(mockDocumentTask).addOnSuccessListener(any());

        repository.joinWaitingList(TEST_EVENT_ID, TEST_USER_ID, mockCallback);

        verify(mockCallback).onSuccess();
    }



}
