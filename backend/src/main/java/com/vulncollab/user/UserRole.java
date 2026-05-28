package com.vulncollab.user;

public enum UserRole {
    USER,
    ADMIN;

    public static UserRole defaultRegistrationRole() {
        return USER;
    }
}
