package com.smartenergy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.smartenergy.mapper")
@SpringBootApplication
public class SmartEnergyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartEnergyApplication.class, args);
    }
}
