package com.smarthealthcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智慧医疗管理系统 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.smarthealthcare.mapper")
@EnableScheduling
public class SmartHealthcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHealthcareApplication.class, args);
        System.out.println("========================================");
        System.out.println("  智慧医疗管理系统启动成功!");
        System.out.println("  API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================");
    }
}
