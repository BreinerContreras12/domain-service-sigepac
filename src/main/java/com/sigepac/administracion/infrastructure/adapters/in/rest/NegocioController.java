package com.sigepac.administracion.infrastructure.adapters.in.rest;

import com.sigepac.administracion.application.port.in.BuscarNegociosUseCase;
import com.sigepac.administracion.application.port.in.ConsultarEstadoCuentaUseCase;
import com.sigepac.administracion.application.port.in.ConsultarNegocioUseCase;
import com.sigepac.administracion.application.port.in.DarDeBajaNegocioUseCase;
import com.sigepac.administracion.application.port.in.EditarNegocioUseCase;
import com.sigepac.administracion.application.port.in.RegistrarNegocioUseCase;
import com.sigepac.administracion.application.port.in.command.BuscarNegociosQuery;
import com.sigepac.administracion.application.port.in.command.EditarNegocioCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarNegocioCommand;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.EstadoCuentaResponse;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.NegocioRequest;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.NegocioResponse;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.PaginaNegociosResponse;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Negocios de la plaza: alta, edición, baja, listado y búsqueda. */
@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    private final RegistrarNegocioUseCase registrar;
    private final EditarNegocioUseCase editar;
    private final DarDeBajaNegocioUseCase darDeBaja;
    private final ConsultarNegocioUseCase consultar;
    private final ConsultarEstadoCuentaUseCase estadoCuenta;
    private final BuscarNegociosUseCase buscar;

    public NegocioController(RegistrarNegocioUseCase registrar, EditarNegocioUseCase editar,
                             DarDeBajaNegocioUseCase darDeBaja, ConsultarNegocioUseCase consultar,
                             ConsultarEstadoCuentaUseCase estadoCuenta, BuscarNegociosUseCase buscar) {
        this.registrar = registrar;
        this.editar = editar;
        this.darDeBaja = darDeBaja;
        this.consultar = consultar;
        this.estadoCuenta = estadoCuenta;
        this.buscar = buscar;
    }

    @PostMapping
    public ResponseEntity<NegocioResponse> crear(@RequestBody NegocioRequest r) {
        var negocio = registrar.registrar(new RegistrarNegocioCommand(
                r.nombre(), r.local(), r.documentoPropietario(), r.nombrePropietario(), r.telefonoPropietario()));
        return ResponseEntity.created(URI.create("/api/negocios/" + negocio.getId())).body(NegocioResponse.de(negocio));
    }

    /** Listado y búsqueda en un solo endpoint: sin filtros devuelve todos los negocios activos, paginados. */
    @GetMapping
    public PaginaNegociosResponse listar(@RequestParam(required = false) String nombre,
                                         @RequestParam(required = false) String local,
                                         @RequestParam(required = false) String documento,
                                         @RequestParam(required = false) Boolean conSaldo,
                                         @RequestParam(defaultValue = "false") boolean incluirInactivos,
                                         @RequestParam(defaultValue = "0") int pagina,
                                         @RequestParam(defaultValue = "20") int tamano) {
        return PaginaNegociosResponse.de(buscar.buscar(
                new BuscarNegociosQuery(nombre, local, documento, conSaldo, incluirInactivos, pagina, tamano)));
    }

    @GetMapping("/{id}")
    public NegocioResponse obtener(@PathVariable long id) {
        return NegocioResponse.de(consultar.porId(id));
    }

    @PutMapping("/{id}")
    public NegocioResponse editar(@PathVariable long id, @RequestBody NegocioRequest r) {
        return NegocioResponse.de(editar.editar(new EditarNegocioCommand(
                id, r.nombre(), r.local(), r.documentoPropietario(), r.nombrePropietario(), r.telefonoPropietario())));
    }

    /** No borra: da de baja (INACTIVO) y conserva el historial. Falla si el negocio aún debe dinero. */
    @DeleteMapping("/{id}")
    public NegocioResponse darDeBaja(@PathVariable long id) {
        return NegocioResponse.de(darDeBaja.darDeBaja(id));
    }

    @GetMapping("/{id}/estado-cuenta")
    public EstadoCuentaResponse estadoCuenta(@PathVariable long id) {
        return EstadoCuentaResponse.de(estadoCuenta.porNegocio(id));
    }
}
