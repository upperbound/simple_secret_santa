package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.PasswordFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * <p> Used to encode user defined password </p>
 * @author Vladislav Tsukanov
 */
@RequiredArgsConstructor
@Profile({"!no-password & !uuid-password"})
@Service
public class UserDefinedPasswordFactory implements PasswordFactory {
    private final PasswordEncoder passwordEncoder;

    /**
     * @param pwd user defined password
     * @param email not used
     * @param locale not used
     * @return user defined password encoded by {@link PasswordEncoder}
     */
    @Override
    public String createPassword(String pwd, String email, Locale locale) {
        return passwordEncoder.encode(pwd);
    }
}
