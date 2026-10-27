package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.Dinero;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DineroTest {

    @Test
    @DisplayName("Se compara por valor, sin importar cómo se construyó")
    void igualdadPorValor() {
        assertEquals(Dinero.de(1000), Dinero.de("1000"));
        assertEquals(Dinero.de(1000), Dinero.de(new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("Siempre queda con 2 decimales")
    void redondeaADosDecimales() {
        assertEquals(new BigDecimal("10.13"), Dinero.de("10.125").valor());
        assertEquals(new BigDecimal("5.00"), Dinero.de(5).valor());
    }

    @Test
    void sumaYMultiplica() {
        assertEquals(Dinero.de(15000), Dinero.de(5000).multiplicar(3));
        assertEquals(Dinero.de(7500), Dinero.de(5000).sumar(Dinero.de(2500)));
    }

    @Test
    @DisplayName("No existe dinero negativo ni texto que no sea número")
    void rechazaValoresInvalidos() {
        assertThrows(ValorInvalidoException.class, () -> Dinero.de(-1));
        assertThrows(ValorInvalidoException.class, () -> Dinero.de((BigDecimal) null));
        assertThrows(ValorInvalidoException.class, () -> Dinero.de("abc"));
        assertThrows(ValorInvalidoException.class, () -> Dinero.de(10).multiplicar(-2));
    }
}
