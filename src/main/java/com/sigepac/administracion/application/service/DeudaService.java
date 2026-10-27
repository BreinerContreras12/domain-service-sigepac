package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.ActualizarDeudaUseCase;
import com.sigepac.administracion.application.port.in.RegistrarDeudaUseCase;
import com.sigepac.administracion.application.port.in.command.ActualizarDeudaCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarDeudaCommand;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.exception.DeudaDuplicadaException;
import com.sigepac.administracion.domain.exception.DeudaNoEncontradaException;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;

/** HU-9: registrar y actualizar deudas (administración o alquiler) de un negocio. */
public class DeudaService implements RegistrarDeudaUseCase, ActualizarDeudaUseCase {

    private final NegocioRepositoryPort negocios;
    private final DeudaRepositoryPort deudas;

    public DeudaService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas) {
        this.negocios = negocios;
        this.deudas = deudas;
    }

    @Override
    public Deuda registrar(RegistrarDeudaCommand c) {
        Negocio negocio = negocios.buscarPorId(c.negocioId())
                .orElseThrow(() -> new NegocioNoEncontradoException(c.negocioId()));
        negocio.asegurarActivo();
        ConceptoDeuda concepto = ConceptoDeuda.desde(c.concepto());
        Periodo periodo = Periodo.de(c.anio(), c.mes());
        Dinero valor = Dinero.de(c.valor());
        if (deudas.existe(negocio.getId(), concepto, periodo)) {
            throw new DeudaDuplicadaException(concepto, periodo);
        }
        return deudas.guardar(Deuda.registrar(negocio.getId(), concepto, periodo, valor));
    }

    @Override
    public Deuda actualizar(ActualizarDeudaCommand c) {
        Deuda deuda = deudas.buscarPorId(c.deudaId()).orElseThrow(() -> new DeudaNoEncontradaException(c.deudaId()));
        deuda.actualizarValor(Dinero.de(c.valor()));
        return deudas.guardar(deuda);
    }
}
