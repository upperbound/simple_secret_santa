package com.github.upperbound.secret_santa.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import com.github.upperbound.secret_santa.util.ApplicationParams;

@Configuration
public class MvcConfig implements ApplicationListener<WebServerInitializedEvent> {
    @Autowired
    private ApplicationParams applicationParams;
    @Autowired
    private PostInitializer postInitializer;

    @Override
    @SneakyThrows
    public void onApplicationEvent(WebServerInitializedEvent event) {
        applicationParams.setServerPort(event.getWebServer().getPort());
        postInitializer.init();
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
