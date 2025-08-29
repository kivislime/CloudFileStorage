package com.kivislime.filestorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Size(max = 255)
@Pattern(regexp = PathPatterns.DIRECTORY_PARAM, message = "Invalid directory path")
public @interface ValidDirectoryPath {
    String message() default "Invalid directory path";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
