package com.github.upperbound.secret_santa.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p> Provides the same logic as
 * an {@link org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator EmailValidator}
 * and supports expression-driven property injection </p>
 * @author Vladislav Tsukanov
 * @see org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator EmailValidator
 */
@Slf4j
@Component
public class EmailValidator extends AbstractEmailValidator<Email> {
    private static Environment environment;
    private Pattern pattern = null;

    @Autowired
    public void init(Environment environment) {
        EmailValidator.environment = environment;
    }

    @Override
    public void initialize(Email constraintAnnotation) {
        super.initialize(constraintAnnotation);
        jakarta.validation.constraints.Pattern.Flag[] flags = constraintAnnotation.flags();
        int intFlags = 0;
        for (jakarta.validation.constraints.Pattern.Flag flag : flags)
            intFlags = intFlags | flag.getValue();

        String regex = environment.resolvePlaceholders(constraintAnnotation.regexp());
        if (!".*".equals(regex) || constraintAnnotation.flags().length > 0) {
            try {
                pattern = Pattern.compile(regex, intFlags);
            } catch (PatternSyntaxException e) {
                log.error("invalid pattern: {}", regex);
                throw e;
            }
        }
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty() || !super.isValid(value, context))
            return false;
        if (pattern == null)
            return true;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.format(
                        environment.resolvePlaceholders(context.getDefaultConstraintMessageTemplate()),
                        value,
                        pattern.pattern()
                )).addConstraintViolation();
        return pattern.matcher(value).matches();
    }
}
