// ============================================================================
// FILE: app/src/test/java/com/example/lotterysystemproject/RepositoryProviderTest.java
// ============================================================================
package com.example.lotterysystemproject;

import static org.junit.Assert.*;

import com.example.lotterysystemproject.firebasemanager.AdminRepository;
import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.MockAdminRepository;
import com.example.lotterysystemproject.firebasemanager.MockEntrantRepository;
import com.example.lotterysystemproject.firebasemanager.MockEventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests for RepositoryProvider
 * Tests the singleton factory pattern for repositories
 */
@RunWith(RobolectricTestRunner.class)
public class RepositoryProviderTest {

    @Before
    public void setUp() {
        RepositoryProvider.resetInstances();
    }

    // ==================== SINGLETON PATTERN TESTS ====================

    @Test
    public void testGetEventRepositorySingleton() {
        EventRepository repo1 = RepositoryProvider.getEventRepository();
        EventRepository repo2 = RepositoryProvider.getEventRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertSame("Should return same instance", repo1, repo2);
    }

    @Test
    public void testGetAdminRepositorySingleton() {
        AdminRepository repo1 = RepositoryProvider.getAdminRepository();
        AdminRepository repo2 = RepositoryProvider.getAdminRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertSame("Should return same instance", repo1, repo2);
    }

    @Test
    public void testGetEntrantRepositorySingleton() {
        EntrantRepository repo1 = RepositoryProvider.getEntrantRepository();
        EntrantRepository repo2 = RepositoryProvider.getEntrantRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertSame("Should return same instance", repo1, repo2);
    }

    // ==================== MOCK MODE TESTS ====================

    @Test
    public void testEventRepositoryIsMock() {
        assertFalse("Should be in mock mode", RepositoryProvider.isUsingFirebase());
        EventRepository repo = RepositoryProvider.getEventRepository();
        assertTrue("Should return MockEventRepository", repo instanceof MockEventRepository);
    }

    @Test
    public void testAdminRepositoryIsMock() {
        assertFalse("Should be in mock mode", RepositoryProvider.isUsingFirebase());
        AdminRepository repo = RepositoryProvider.getAdminRepository();
        assertTrue("Should return MockAdminRepository", repo instanceof MockAdminRepository);
    }

    @Test
    public void testEntrantRepositoryIsMock() {
        assertFalse("Should be in mock mode", RepositoryProvider.isUsingFirebase());
        EntrantRepository repo = RepositoryProvider.getEntrantRepository();
        assertTrue("Should return MockEntrantRepository", repo instanceof MockEntrantRepository);
    }

    // ==================== RESET FUNCTIONALITY ====================

    @Test
    public void testResetInstancesEventRepository() {
        EventRepository repo1 = RepositoryProvider.getEventRepository();
        RepositoryProvider.resetInstances();
        EventRepository repo2 = RepositoryProvider.getEventRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertNotSame("Should create new instance after reset", repo1, repo2);
    }

    @Test
    public void testResetInstancesAdminRepository() {
        AdminRepository repo1 = RepositoryProvider.getAdminRepository();
        RepositoryProvider.resetInstances();
        AdminRepository repo2 = RepositoryProvider.getAdminRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertNotSame("Should create new instance after reset", repo1, repo2);
    }

    @Test
    public void testResetInstancesEntrantRepository() {
        EntrantRepository repo1 = RepositoryProvider.getEntrantRepository();
        RepositoryProvider.resetInstances();
        EntrantRepository repo2 = RepositoryProvider.getEntrantRepository();

        assertNotNull(repo1);
        assertNotNull(repo2);
        assertNotSame("Should create new instance after reset", repo1, repo2);
    }

    // ==================== CROSS-REPOSITORY CONSISTENCY ====================

    @Test
    public void testMultipleRepositoriesCanCoexist() {
        EventRepository eventRepo = RepositoryProvider.getEventRepository();
        EntrantRepository entrantRepo = RepositoryProvider.getEntrantRepository();
        AdminRepository adminRepo = RepositoryProvider.getAdminRepository();

        assertNotNull(eventRepo);
        assertNotNull(entrantRepo);
        assertNotNull(adminRepo);

        // Each should be its own instance
        assertNotSame(eventRepo, entrantRepo);
        assertNotSame(entrantRepo, adminRepo);
        assertNotSame(eventRepo, adminRepo);
    }

    @Test
    public void testInstancePersistenceAcrossMultipleCalls() {
        // Get all repositories multiple times
        EventRepository eventRepo1 = RepositoryProvider.getEventRepository();
        EntrantRepository entrantRepo1 = RepositoryProvider.getEntrantRepository();
        AdminRepository adminRepo1 = RepositoryProvider.getAdminRepository();

        EventRepository eventRepo2 = RepositoryProvider.getEventRepository();
        EntrantRepository entrantRepo2 = RepositoryProvider.getEntrantRepository();
        AdminRepository adminRepo2 = RepositoryProvider.getAdminRepository();

        // All should be the same instances
        assertSame(eventRepo1, eventRepo2);
        assertSame(entrantRepo1, entrantRepo2);
        assertSame(adminRepo1, adminRepo2);
    }

    @Test
    public void testLegacyGetInstanceMethod() {
        EventRepository legacyRepo = RepositoryProvider.getInstance();
        EventRepository newRepo = RepositoryProvider.getEventRepository();

        assertNotNull(legacyRepo);
        assertSame("Legacy method should return same instance", legacyRepo, newRepo);
    }
}