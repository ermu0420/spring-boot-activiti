package com.ermu.springbootactiviti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@ServletComponentScan
@EnableAutoConfiguration(exclude={org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.activiti.spring.boot.SecurityAutoConfiguration.class})
public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ServletInitializer.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(ServletInitializer.class,args);
    }
}