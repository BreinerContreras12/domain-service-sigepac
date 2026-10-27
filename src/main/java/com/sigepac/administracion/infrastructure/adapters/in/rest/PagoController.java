package com.sigepac.administracion.infrastructure.adapters.in.rest;

import com.sigepac.administracion.application.port.in.AnularPagoUseCase;
import com.sigepac.administracion.application.port.in.RegistrarPagoUseCase;
import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.ComprobantePagoResponse;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.PagoRequest;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.PagoResponse;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Pagos de un negocio. Un pago no se edita ni se borra: se anula. */
@RestController
public class PagoController {

    private final RegistrarPagoUseCase registrar;
    private final AnularPagoUseCase anular;

    public PagoController(RegistrarPagoUseCase registrar, AnularPagoUseCase anular) {
        this.registrar = registrar;
        this.anular = anular;
    }

    @PostMapping("/api/negocios/{negocioId}/pagos")
    public ResponseEntity<ComprobantePagoResponse> pagar(@PathVariable long negocioId, @RequestBody PagoRequest r) {
        var comprobante = registrar.registrar(new RegistrarPagoCommand(negocioId, r.fecha(), r.monto()));
        return ResponseEntity.created(URI.create("/api/pagos/" + comprobante.pago().getId()))
                .body(ComprobantePagoResponse.de(comprobante));
    }

    @PostMapping("/api/pagos/{id}/anulacion")
    public PagoResponse anular(@PathVariable long id) {
        return PagoResponse.de(anular.anular(id));
    }
}
