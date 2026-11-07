package com.sigepac.administracion.infrastructure.config;

import com.sigepac.administracion.application.port.out.AuditoriaRepositoryPort;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.application.port.out.TransaccionPort;
import com.sigepac.administracion.application.port.out.UsuarioActualPort;
import com.sigepac.administracion.application.service.Auditor;
import com.sigepac.administracion.application.service.ConsultarAuditoriaService;
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
    Auditor auditor(AuditoriaRepositoryPort auditoria, UsuarioActualPort usuarioActual, Clock reloj) {
        return new Auditor(auditoria, usuarioActual, reloj);
    }

    @Bean
    NegocioService negocioService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, EstadoCuentaService estado,
                                  TransaccionPort transaccion, Auditor auditor) {
        return new NegocioService(negocios, deudas, estado, transaccion, auditor);
    }

    @Bean
    DeudaService deudaService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, TransaccionPort transaccion,
                              Auditor auditor) {
        return new DeudaService(negocios, deudas, transaccion, auditor);
    }

    @Bean
    PagoService pagoService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, PagoRepositoryPort pagos,
                            TransaccionPort transaccion, Clock reloj, AplicacionPagoService aplicacion,
                            EstadoCuentaService estado, Auditor auditor) {
        return new PagoService(negocios, deudas, pagos, transaccion, reloj, aplicacion, estado, auditor);
    }

    @Bean
    ConsultarAuditoriaService consultarAuditoriaService(NegocioRepositoryPort negocios, AuditoriaRepositoryPort auditoria) {
        return new ConsultarAuditoriaService(negocios, auditoria);
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
