package com.example.notes.dto;

import jakarta.validation.constraints.NotNull;

/**
 * UserDTO to create new account
 */
public record UserDTO(@NotNull(message = "Name cannot be null") String name,
    @NotNull(message = "Password cannot be null") String password) {
}
