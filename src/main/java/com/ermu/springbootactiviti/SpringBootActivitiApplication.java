package com.ermu.springbootactiviti;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author Administrator
 */

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class SpringBootActivitiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootActivitiApplication.class, args);
    }

}