package com.kivislime.filestorage;

import org.springframework.boot.SpringApplication;

public class TestFileStorageAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(FileStorageAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
