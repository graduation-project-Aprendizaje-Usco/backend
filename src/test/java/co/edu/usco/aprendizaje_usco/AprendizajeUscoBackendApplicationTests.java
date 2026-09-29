package co.edu.usco.aprendizaje_usco;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AprendizajeUscoBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
