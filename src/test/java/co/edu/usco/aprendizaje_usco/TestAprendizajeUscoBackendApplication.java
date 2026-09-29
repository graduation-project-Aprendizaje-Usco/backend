package co.edu.usco.aprendizaje_usco;

import org.springframework.boot.SpringApplication;

public class TestAprendizajeUscoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(AprendizajeUscoBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
