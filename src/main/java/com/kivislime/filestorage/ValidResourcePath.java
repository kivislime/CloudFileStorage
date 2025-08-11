package com.kivislime.filestorage;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
@NotBlank
@Size(min = 1, max = 255)
@Pattern(
        regexp = "^(?:[A-Za-z0-9_.\\-]+(?:/[A-Za-z0-9_.\\-]+)*|[A-Za-z0-9_.\\-]+(?:/[A-Za-z0-9_.\\-]+)*/)$",
        message = "Invalid file or directory path"
)
public @interface ValidResourcePath {
    String message() default "Invalid path";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
