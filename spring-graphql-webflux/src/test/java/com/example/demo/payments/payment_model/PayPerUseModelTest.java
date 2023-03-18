package com.example.demo.payments.payment_model;

import com.example.demo.payments.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PayPerUseModelTest {
    @Test
    void testUserBilling(){
        User user = new User();
        user.setUserId(1);
        uint32_t userId = user.getUserId();
    }
}