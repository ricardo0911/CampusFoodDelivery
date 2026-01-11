package com.campus.ordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.ordering.mapper")
public class CampusOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOrderingApplication.class, args);
    }
}
