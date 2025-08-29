package com.kivislime.filestorage.dto;

import jakarta.validation.constraints.Size;

public record UserCredentialsRequest(
                                     @Size(
                                             min = 3,
                                             max = 20,
                                             message = "The login must contain from {min} to {max} characters")
                                     String username,
                                     @Size(
                                             min = 3,
                                             max = 20,
                                             message = "The password must contain from {min} to {max} characters")
                                     String password) {
}
