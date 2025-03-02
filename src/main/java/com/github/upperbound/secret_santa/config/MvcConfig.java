package com.github.upperbound.secret_santa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.*;

/**
 * <p> All web related configurations </p>
 * @author Vladislav Tsukanov
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    private final static String LOCALE_COOKIE_PARAM_NAME = "spring_secret_santa_locale";
    private final static String LOCALE_REQUEST_PARAM_NAME = "lang";

    /**
     * <p> To store locale info inside a cookie with name {@link #LOCALE_COOKIE_PARAM_NAME} </p>
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver(LOCALE_COOKIE_PARAM_NAME);
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return cookieLocaleResolver;
    }

    /**
     * <p> If a new locale setting comes within the request param {@link #LOCALE_REQUEST_PARAM_NAME} it will be handled </p>
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(LOCALE_REQUEST_PARAM_NAME);
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
