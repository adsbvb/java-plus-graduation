package ru.practicum.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "ru.practicum.service",
        "ru.practicum.client",
        "ru.practicum.dto"
})
public class ServiceApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ServiceApp.class, args);
    }
}
