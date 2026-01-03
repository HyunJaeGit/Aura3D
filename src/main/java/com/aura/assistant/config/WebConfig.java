package com.aura.assistant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * [역할] 정적 리소스 경로 설정
 * 리액트에서 빌드된 JS/CSS 파일과 public에 담긴 3D 모델 파일 경로를 매핑합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 리액트 빌드 파일 (index.js, index.css 등) 매핑
        registry.addResourceHandler("/dist/**")
                .addResourceLocations("classpath:/static/dist/");

        // 2. 3D 모델 파일 (.glb) 매핑
        registry.addResourceHandler("/models/**")
                .addResourceLocations("classpath:/static/dist/models/");

        // 3. 기존 CSS/JS 폴더 매핑
        registry.addResourceHandler("/css/**", "/js/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/");
    }


}