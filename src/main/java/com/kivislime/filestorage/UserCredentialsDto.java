package com.kivislime.filestorage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

//TODO: синхронизировать размеры ограничения в БД и V1_init.sql?
public record UserCredentialsDto(@NotBlank @Size(min = 2, max = 16) String username,
                                 @NotBlank @Size(min = 2, max = 32) String password) {
}
