package com.example.cunion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@SpringBootConfiguration
@MapperScan("com.example.cunion.mapper")
@EnableScheduling
public class CunionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CunionApplication.class, args);
    }

}
