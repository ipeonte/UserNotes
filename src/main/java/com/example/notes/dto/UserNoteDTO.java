package com.example.notes.dto;

import jakarta.validation.constraints.NotNull;

/**
 * UserNoteDTO to use when new note is updated or for search
 */
public record UserNoteDTO(@NotNull(message = "Note Id cannot be null") String id,
    @NotNull(message = "Note text cannot be null") String note) {
}
