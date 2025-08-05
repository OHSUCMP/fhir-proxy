package edu.ohsu.cmp.fhirproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FhirProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhirProxyApplication.class, args);
    }
}
