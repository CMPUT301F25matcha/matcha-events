package com.example.lotterysystemproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.lotterysystemproject.Controllers.UserInfoController;
import com.example.lotterysystemproject.Models.UserModel;

import org.junit.Before;
import org.junit.Test;

public class UserInfoTest {

    private UserInfoController controller;

    @Before
    public void setUp() {
        controller = new UserInfoController();
    }

    @Test
    public void validation_fails_when_name_missing() {
        UserModel model = new UserModel("", "user@example.com", "1234567890");
        assertFalse(controller.validate(model));
    }

    @Test
    public void validation_fails_when_email_missing() {
        UserModel model = new UserModel("Alice", "", "1234567890");
        assertFalse(controller.validate(model));
    }

    @Test
    public void validation_passes_when_phone_missing_optional() {
        UserModel model = new UserModel("Alice", "user@example.com", "");
        assertTrue(controller.validate(model));
    }

    @Test
    public void validation_passes_with_all_fields_present() {
        UserModel model = new UserModel("Alice", "user@example.com", "1234567890");
        assertTrue(controller.validate(model));
    }
}


