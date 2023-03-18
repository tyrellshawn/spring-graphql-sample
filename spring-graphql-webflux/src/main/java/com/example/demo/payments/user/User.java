package com.example.demo.payments.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
public class User {
    private String email;

    private UUID userId;

private String name;

private String SurName;

private String stripeCustomerId;

private List<UserPermission> permissions;

}
