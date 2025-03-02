package com.github.upperbound.secret_santa.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * <p> Provides he same logic as an {@link jakarta.validation.constraints.Email Email} and supports
 * expression-driven property injection for {@link Email#regexp() regexp} and {@link Email#message() message} </p>
 * @author Vladislav Tsukanov
 * @see org.springframework.beans.factory.annotation.Value Value
 * @see jakarta.validation.constraints.Email Email
 * @see EmailValidator
 */
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EmailValidator.class)
public @interface Email {
    /**
     * @see jakarta.validation.constraints.Email#message() Email.message
     */
    String message() default "email '%s' does not match the given mask '%s'";

    /**
     * @see jakarta.validation.constraints.Email#regexp() Email.regexp
     */
    String regexp() default ".*";

    /**
     * @see jakarta.validation.constraints.Email#flags() Email.flags
     */
    Pattern.Flag[] flags() default {};

    /**
     * @see jakarta.validation.constraints.Email#groups() Email.groups
     */
    Class<?>[] groups() default {};

    /**
     * @see jakarta.validation.constraints.Email#payload() Email.payload
     */
    Class<? extends Payload>[] payload() default {};
}
