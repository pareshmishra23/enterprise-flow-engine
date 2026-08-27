package com.efe.traderecon.ikasan.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IkasanWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index.html");
        registry.addRedirectViewController("/console", "/index.html");
        registry.addRedirectViewController("/console/", "/index.html");
        registry.addRedirectViewController("/ikasan", "/index.html");
        registry.addRedirectViewController("/ikasan/", "/index.html");
    }
}
