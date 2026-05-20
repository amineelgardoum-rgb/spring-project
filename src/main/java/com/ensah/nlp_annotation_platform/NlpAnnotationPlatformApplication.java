package com.ensah.nlp_annotation_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class NlpAnnotationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(NlpAnnotationPlatformApplication.class, args);
	}

}
 