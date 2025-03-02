package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.PasswordFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Provided for test purposes only
 * @author Vladislav Tsukanov
 * @deprecated should not be used other than for test purposes.
 */
@Profile("no-password")
@Service
@Deprecated
public class NoPasswordFactory implements PasswordFactory {
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public NoPasswordFactory(PasswordEncoder passwordEncoder,
                             @Value("${app.default-pass}")
                             String defaultPassword) {
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword == null ? "" : defaultPassword;
    }

    /**
     * @param pwd not used
     * @param email not used
     * @param locale not used
     * @return default password, encoded by {@link PasswordEncoder}
     */
    @Override
    public String createPassword(String pwd, String email, Locale locale) {
        return passwordEncoder.encode(defaultPassword);
    }
}
