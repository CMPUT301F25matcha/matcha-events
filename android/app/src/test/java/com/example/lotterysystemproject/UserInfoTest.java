package com.example.lotterysystemproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.lotterysystemproject.controllers.UserInfo;
import com.example.lotterysystemproject.models.User;

import org.junit.Before;
import org.junit.Test;

public class UserInfoTest {

    private UserInfo controller;

    @Before
    public void setUp() {
        controller = new UserInfo();
    }

    @Test
    public void validation_fails_when_name_missing() {
        User model = new User("", "", "user@example.com", "1234567890");
        assertFalse(controller.validate(model));
    }

    @Test
    public void validation_fails_when_email_missing() {
        User model = new User("", "Alice", "", "1234567890");
        assertFalse(controller.validate(model));
    }

    @Test
    public void validation_passes_when_phone_missing_optional() {
        User model = new User("", "Alice", "user@example.com", "");
        assertTrue(controller.validate(model));
    }

    @Test
    public void validation_passes_with_all_fields_present() {
        User model = new User("", "Alice", "user@example.com", "1234567890");
        assertTrue(controller.validate(model));
    }
}


