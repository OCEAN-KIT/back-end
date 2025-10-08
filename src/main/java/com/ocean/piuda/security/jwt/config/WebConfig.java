package com.ocean.piuda.security.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        // 프론트 개발
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:4173",
                        "https://localhost:3000",
                        "https://localhost:5173",

                        // 프론트 배포
                        "https://어쩌구저쩌구.vercel.app",

                        //스웨거용
                        "https://www.jungjiyu.com",
                        "http://www.jungjiyu.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 쿠키/인증 포함 시 필요
    }

}