package com.monentreprise.rh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration MVC : enregistrement des intercepteurs.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestUriInterceptor requestUriInterceptor;

    public WebMvcConfig(RequestUriInterceptor requestUriInterceptor) {
        this.requestUriInterceptor = requestUriInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestUriInterceptor);
    }
}
