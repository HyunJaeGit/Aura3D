package com.aura.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * AppConfig 클래스
 * 가상 스레드 기반의 스케줄러를 설정합니다.
 */
@Configuration
public class AppConfig {

    /**
     * 가상 스레드를 사용하는 TaskScheduler 빈 등록
     * ConcurrentTaskScheduler는 이미 존재하는 ExecutorService를 래핑하여
     * 스프링의 TaskScheduler 기능을 사용할 수 있게 해줍니다.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        // 1. Java 21의 가상 스레드를 사용하는 스케줄 전용 실행기를 생성합니다.
        ScheduledExecutorService virtualWorker = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("Aura-Monitor-").factory()
        );

        // 2. 생성한 가상 스레드 실행기를 스프링의 스케줄러 구조에 입힙니다.
        return new ConcurrentTaskScheduler(virtualWorker);
    }
}