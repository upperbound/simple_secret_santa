package com.github.upperbound.secret_santa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * <p> Used to look for a {@link #CONFIG_FILE_NAME} to use its properties instead of default ones </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
public class ConfigPostProcessor implements EnvironmentPostProcessor {
    private final static String CONFIG_FILE_NAME = "config.properties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String outerConfigFile = environment.getProperty("outer.config.file", CONFIG_FILE_NAME);
        if (outerConfigFile.toLowerCase().startsWith("file:") || outerConfigFile.toLowerCase().startsWith("classpath:")) {
            try {
                environment.getPropertySources().addFirst(new ResourcePropertySource(outerConfigFile));
                log.info("config file '{}' loaded", outerConfigFile);
            } catch (IOException e) {
                log.warn("unable to load config file '{}': {}", outerConfigFile, e.getMessage());
            }
            return;
        }
        try {
            environment.getPropertySources().addFirst(new ResourcePropertySource("file:" + outerConfigFile));
            log.info("config file '{}' loaded", outerConfigFile);
        } catch (IOException e) {
            log.warn("config file '{}' not found, trying to find it within classpath", outerConfigFile);
            try {
                environment.getPropertySources().addFirst(new ResourcePropertySource("classpath:" + outerConfigFile));
                log.info("config file '{}' loaded from classpath", outerConfigFile);
            } catch (IOException ex) {
                log.warn("unable to load config file '{}': {}", outerConfigFile, ex.getMessage());
            }
        }
    }
}
