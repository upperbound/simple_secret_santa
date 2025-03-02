package com.github.upperbound.secret_santa.service;

import java.util.Locale;

/**
 * Used to create a password for the participant and send a notification if necessary
 * @author Vladislav Tsukanov
 */
public interface PasswordFactory {
    /**
     * @param pwd user defined password, if this method supports it
     * @param email where to send a password, if necessary
     * @param locale for the content of the notification
     * @return generated password
     */
    String createPassword(String pwd, String email, Locale locale);
}
