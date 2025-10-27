package com.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TerraformSpring251027Application {

    public static void main(String[] args) {
        SpringApplication.run(TerraformSpring251027Application.class, args);
    }

}
