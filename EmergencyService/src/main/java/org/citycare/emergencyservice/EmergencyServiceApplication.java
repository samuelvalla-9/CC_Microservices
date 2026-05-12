package org.citycare.emergencyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableFeignClients
@EnableAsync
public class EmergencyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmergencyServiceApplication.class, args);
        System.out.println("Emergency Service Started");
    }

}
