package com.sigepac.administracion.infrastructure.adapters.in.rest;

import com.sigepac.administracion.application.port.in.ActualizarDeudaUseCase;
import com.sigepac.administracion.application.port.in.RegistrarDeudaUseCase;
import com.sigepac.administracion.application.port.in.command.ActualizarDeudaCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarDeudaCommand;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.DeudaRequest;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.DeudaResponse;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.DeudaUpdateRequest;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Deudas (administración / alquiler) de un negocio. */
@RestController
public class DeudaController {

    private final RegistrarDeudaUseCase registrar;
    private final ActualizarDeudaUseCase actualizar;

    public DeudaController(RegistrarDeudaUseCase registrar, ActualizarDeudaUseCase actualizar) {
        this.registrar = registrar;
        this.actualizar = actualizar;
    }

    @PostMapping("/api/negocios/{negocioId}/deudas")
    public ResponseEntity<DeudaResponse> crear(@PathVariable long negocioId, @RequestBody DeudaRequest r) {
        var deuda = registrar.registrar(new RegistrarDeudaCommand(negocioId, r.concepto(), r.anio(), r.mes(), r.valor()));
        return ResponseEntity.created(URI.create("/api/deudas/" + deuda.getId())).body(DeudaResponse.de(deuda));
    }

    @PutMapping("/api/deudas/{id}")
    public DeudaResponse actualizar(@PathVariable long id, @RequestBody DeudaUpdateRequest r) {
        return DeudaResponse.de(actualizar.actualizar(new ActualizarDeudaCommand(id, r.valor())));
    }
}
