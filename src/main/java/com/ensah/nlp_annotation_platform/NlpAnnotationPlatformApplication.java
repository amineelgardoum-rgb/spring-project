package com.ensah.nlp_annotation_platform;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
public class NlpAnnotationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(NlpAnnotationPlatformApplication.class, args);
	}
}
 