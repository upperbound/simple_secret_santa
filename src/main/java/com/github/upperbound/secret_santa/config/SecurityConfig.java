package com.github.upperbound.secret_santa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.github.upperbound.secret_santa.model.ParticipantRole;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/home",
                                "/sign_in/**",
                                "/sign_up/**",
                                "/forgot_password/**",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/"
                        ).permitAll()
                        .requestMatchers(
                                "/groups/**"
                        ).hasAnyAuthority(ParticipantRole.ADMIN.getAuthority(), ParticipantRole.SUPERADMIN.getAuthority())
                        .requestMatchers(
                                "/group/**"
                        ).hasAnyAuthority(ParticipantRole.SUPERADMIN.getAuthority())
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/sign_in")
                        .loginProcessingUrl("/do_login")
                        .defaultSuccessUrl("/home", true)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/home")
                        .permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // не кодируем пароль
    }
}
