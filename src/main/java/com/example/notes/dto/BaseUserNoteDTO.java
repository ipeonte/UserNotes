package com.example.notes.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Base UserNoteDTO to use when new note is created
 */
public record BaseUserNoteDTO(@NotNull(message = "Note cannot be null") String note) {
}
