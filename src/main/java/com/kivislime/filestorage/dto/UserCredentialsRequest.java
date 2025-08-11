package com.kivislime.filestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCredentialsDto(@NotBlank @Size(min = 2, max = 16) String username,
                                 @NotBlank @Size(min = 2, max = 32) String password) {
}
