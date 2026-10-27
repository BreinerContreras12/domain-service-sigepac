package com.sigepac.administracion.infrastructure.config;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.application.port.out.TransaccionPort;
import com.sigepac.administracion.application.service.BuscarNegociosService;
import com.sigepac.administracion.application.service.ConsultarEstadoCuentaService;
import com.sigepac.administracion.application.service.DeudaService;
import com.sigepac.administracion.application.service.NegocioService;
import com.sigepac.administracion.application.service.PagoService;
import com.sigepac.administracion.domain.service.AplicacionPagoService;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raíz de composición. El dominio y la aplicación no llevan anotaciones de Spring (Semana 5, DIP):
 * es esta clase, en el borde, la que los construye y les entrega sus dependencias.
 */
@Configuration
public class BeanConfig {

    @Bean
    Clock reloj(@Value("${negocio.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    AplicacionPagoService aplicacionPagoService() {
        return new AplicacionPagoService();
    }

    @Bean
    EstadoCuentaService estadoCuentaService() {
        return new EstadoCuentaService();
    }

    @Bean
    NegocioService negocioService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, EstadoCuentaService estado) {
        return new NegocioService(negocios, deudas, estado);
    }

    @Bean
    DeudaService deudaService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas) {
        return new DeudaService(negocios, deudas);
    }

    @Bean
    PagoService pagoService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, PagoRepositoryPort pagos,
                            TransaccionPort transaccion, Clock reloj, AplicacionPagoService aplicacion,
                            EstadoCuentaService estado) {
        return new PagoService(negocios, deudas, pagos, transaccion, reloj, aplicacion, estado);
    }

    @Bean
    ConsultarEstadoCuentaService consultarEstadoCuentaService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas,
                                                              PagoRepositoryPort pagos, EstadoCuentaService estado) {
        return new ConsultarEstadoCuentaService(negocios, deudas, pagos, estado);
    }

    @Bean
    BuscarNegociosService buscarNegociosService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas,
                                                EstadoCuentaService estado) {
        return new BuscarNegociosService(negocios, deudas, estado);
    }
}
