package com.github.upperbound.secret_santa.config;

import com.github.upperbound.secret_santa.model.ParticipantRole;
import com.github.upperbound.secret_santa.util.StaticContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * <p> All security related configurations </p>
 * @author Vladislav Tsukanov
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final StaticContext staticContext;

    /**
     * <p> Filter chain for httpSession-based MVC </p>
     */
    @Bean
    public SecurityFilterChain mvcSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/home",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/api/common/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/")
                        .permitAll()
                        .requestMatchers(
                                "/sign_in/**",
                                "/sign_up/**",
                                "/forgot_password/**",
                                "/reset_password/**")
                        .anonymous()
                        .requestMatchers(
                                "/participants/**")
                        .hasAnyAuthority(ParticipantRole.SUPERADMIN.getAuthority())
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/sign_in")
                        .loginProcessingUrl("/do_login")
                        .defaultSuccessUrl("/home")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .build();
    }

    /**
     * <p> To encode password using hash function </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
