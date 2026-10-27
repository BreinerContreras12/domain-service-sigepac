package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.ConsultarNegocioUseCase;
import com.sigepac.administracion.application.port.in.DarDeBajaNegocioUseCase;
import com.sigepac.administracion.application.port.in.EditarNegocioUseCase;
import com.sigepac.administracion.application.port.in.RegistrarNegocioUseCase;
import com.sigepac.administracion.application.port.in.command.EditarNegocioCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarNegocioCommand;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.exception.LocalOcupadoException;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Propietario;
import java.util.ArrayList;
import java.util.List;

/** HU-8: alta, edición y baja lógica de negocios. Orquesta: busca, delega al dominio y guarda. */
public class NegocioService implements RegistrarNegocioUseCase, EditarNegocioUseCase,
        DarDeBajaNegocioUseCase, ConsultarNegocioUseCase {

    private final NegocioRepositoryPort negocios;
    private final DeudaRepositoryPort deudas;
    private final EstadoCuentaService estadoCuenta;

    public NegocioService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, EstadoCuentaService estadoCuenta) {
        this.negocios = negocios;
        this.deudas = deudas;
        this.estadoCuenta = estadoCuenta;
    }

    @Override
    public Negocio registrar(RegistrarNegocioCommand c) {
        exigirCampos(c.nombre(), c.local(), c.documentoPropietario(), c.nombrePropietario());
        CodigoLocal local = CodigoLocal.de(c.local());
        Propietario propietario = Propietario.de(c.documentoPropietario(), c.nombrePropietario(), c.telefonoPropietario());
        Negocio nuevo = Negocio.registrar(c.nombre(), local, propietario);
        verificarLocalLibre(local, null);
        return negocios.guardar(nuevo);
    }

    @Override
    public Negocio editar(EditarNegocioCommand c) {
        exigirCampos(c.nombre(), c.local(), c.documentoPropietario(), c.nombrePropietario());
        Negocio negocio = cargar(c.id());
        negocio.asegurarActivo();
        CodigoLocal local = CodigoLocal.de(c.local());
        Propietario propietario = Propietario.de(c.documentoPropietario(), c.nombrePropietario(), c.telefonoPropietario());
        verificarLocalLibre(local, negocio.getId());
        negocio.editar(c.nombre(), local, propietario);
        return negocios.guardar(negocio);
    }

    @Override
    public Negocio darDeBaja(long negocioId) {
        Negocio negocio = cargar(negocioId);
        negocio.darDeBaja(estadoCuenta.saldoDe(deudas.buscarPorNegocio(negocioId)));
        return negocios.guardar(negocio);
    }

    @Override
    public Negocio porId(long negocioId) {
        return cargar(negocioId);
    }

    private Negocio cargar(long id) {
        return negocios.buscarPorId(id).orElseThrow(() -> new NegocioNoEncontradoException(id));
    }

    /** Un local solo puede tener un negocio ACTIVO; al editar, el negocio no compite consigo mismo. */
    private void verificarLocalLibre(CodigoLocal local, Long idPropio) {
        negocios.buscarPorLocalActivo(local)
                .filter(ocupante -> !ocupante.getId().equals(idPropio))
                .ifPresent(ocupante -> {
                    throw new LocalOcupadoException(local, ocupante.getNombre());
                });
    }

    /** Responde con TODOS los campos que faltan, no solo con el primero. */
    private static void exigirCampos(String nombre, String local, String documento, String nombrePropietario) {
        List<String> faltan = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) faltan.add("nombre");
        if (local == null || local.isBlank()) faltan.add("local");
        if (documento == null || documento.isBlank()) faltan.add("documentoPropietario");
        if (nombrePropietario == null || nombrePropietario.isBlank()) faltan.add("nombrePropietario");
        if (!faltan.isEmpty()) {
            throw new ValorInvalidoException("Faltan campos obligatorios: " + String.join(", ", faltan));
        }
    }
}
