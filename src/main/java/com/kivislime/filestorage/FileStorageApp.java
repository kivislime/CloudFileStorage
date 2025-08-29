package com.kivislime.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.kivislime.filestorage")
public class FileStorageApp {
	public static void main(String[] args) {
		SpringApplication.run(FileStorageApp.class, args);
	}
}
