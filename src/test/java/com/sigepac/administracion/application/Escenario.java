package com.sigepac.administracion.application;

import com.sigepac.administracion.application.service.*;
import com.sigepac.administracion.domain.service.AplicacionPagoService;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import com.sigepac.administracion.testsupport.*;

/** Arma los servicios con dobles en memoria: así se prueba la aplicación sin Spring ni base de datos. */
final class Escenario {
    final NegocioRepositoryEnMemoria negocios = new NegocioRepositoryEnMemoria();
    final DeudaRepositoryEnMemoria deudas = new DeudaRepositoryEnMemoria();
    final PagoRepositoryEnMemoria pagos = new PagoRepositoryEnMemoria();
    final EstadoCuentaService estado = new EstadoCuentaService();
    final NegocioService negocioService = new NegocioService(negocios, deudas, estado);
    final DeudaService deudaService = new DeudaService(negocios, deudas);
    final PagoService pagoService = new PagoService(negocios, deudas, pagos, new TransaccionDirecta(),
            Datos.relojFijo(), new AplicacionPagoService(), estado);
    final ConsultarEstadoCuentaService estadoCuentaService = new ConsultarEstadoCuentaService(negocios, deudas, pagos, estado);
    final BuscarNegociosService buscar = new BuscarNegociosService(negocios, deudas, estado);

    com.sigepac.administracion.domain.model.Negocio negocio(String nombre, String local, String doc) {
        return negocioService.registrar(new com.sigepac.administracion.application.port.in.command.RegistrarNegocioCommand(
                nombre, local, doc, "Dueño " + doc, null));
    }

    com.sigepac.administracion.domain.model.Deuda deuda(long negocioId, String concepto, int mes, long valor) {
        return deudaService.registrar(new com.sigepac.administracion.application.port.in.command.RegistrarDeudaCommand(
                negocioId, concepto, 2026, mes, java.math.BigDecimal.valueOf(valor)));
    }
}
