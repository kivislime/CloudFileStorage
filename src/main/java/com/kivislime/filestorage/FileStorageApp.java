package com.kivislime.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//TODO: добавить валидацию starter-validation? Чтобы проверять контроллеры на входе
@SpringBootApplication
public class FileStorageApp {

	public static void main(String[] args) {
		System.out.println("Hello Dikii");
		SpringApplication.run(FileStorageApp.class, args);
	}

}
