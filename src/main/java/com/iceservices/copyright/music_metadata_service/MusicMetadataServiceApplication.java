package com.iceservices.copyright.music_metadata_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MusicMetadataServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(MusicMetadataServiceApplication.class, args);
	}

}
