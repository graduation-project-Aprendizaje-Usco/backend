package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Hallazgo 2 de la revisión: sin JWT_SECRET (o con una llave corta) la app no
 * arranca. Antes había un valor por defecto que permitía firmar tokens con una
 * llave pública si staging olvidaba definir la variable.
 */
class JwtPropertiesTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void sinLlaveNoArrancaYExplicaQueFaltaJwtSecret(String llave) {
        assertThatThrownBy(() -> new JwtProperties(llave, 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Falta la variable de entorno JWT_SECRET");
    }

    @Test
    void conLlaveCortaNoArranca() {
        assertThatThrownBy(() -> new JwtProperties("muy-corta", 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mínimo 32 caracteres");
    }

    @Test
    void conDuracionInvalidaNoArranca() {
        assertThatThrownBy(() -> new JwtProperties("una-llave-de-mas-de-32-caracteres-ok", 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void conLlaveDe32CaracteresArranca() {
        assertThatCode(() -> new JwtProperties("una-llave-de-mas-de-32-caracteres-ok", 120))
                .doesNotThrowAnyException();
    }

}
