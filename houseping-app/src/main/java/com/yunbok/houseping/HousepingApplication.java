package com.yunbok.houseping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HousepingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HousepingApplication.class, args);
    }

}
