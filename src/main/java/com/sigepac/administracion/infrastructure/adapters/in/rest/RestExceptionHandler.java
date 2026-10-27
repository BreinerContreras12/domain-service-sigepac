package com.sigepac.administracion.infrastructure.adapters.in.rest;

import com.sigepac.administracion.domain.exception.DominioException;
import com.sigepac.administracion.domain.exception.DuplicadoException;
import com.sigepac.administracion.domain.exception.NegocioConSaldoException;
import com.sigepac.administracion.domain.exception.NegocioInactivoException;
import com.sigepac.administracion.domain.exception.NoEncontradoException;
import com.sigepac.administracion.domain.exception.PagoExcedeSaldoException;
import com.sigepac.administracion.domain.exception.PagoYaAnuladoException;
import com.sigepac.administracion.domain.exception.ValorDeudaMenorQuePagadoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.ErrorResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduce las excepciones del dominio a códigos HTTP. El dominio no sabe qué es un 404. */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ValorInvalidoException.class)
    ResponseEntity<ErrorResponse> valorInvalido(ValorInvalidoException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<ErrorResponse> noEncontrado(NoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DuplicadoException.class, NegocioInactivoException.class, PagoYaAnuladoException.class,
            ValorDeudaMenorQuePagadoException.class})
    ResponseEntity<ErrorResponse> conflicto(DominioException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PagoExcedeSaldoException.class)
    ResponseEntity<ErrorResponse> pagoExcede(PagoExcedeSaldoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, "Conflict", e.getMessage(),
                List.of("saldo pendiente: " + e.getSaldo().valor().toPlainString())));
    }

    @ExceptionHandler(NegocioConSaldoException.class)
    ResponseEntity<ErrorResponse> negocioConSaldo(NegocioConSaldoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, "Conflict", e.getMessage(),
                List.of("saldo pendiente: " + e.getSaldo().valor().toPlainString())));
    }

    @ExceptionHandler(DominioException.class)
    ResponseEntity<ErrorResponse> dominio(DominioException e) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorResponse> concurrencia(Exception e) {
        LOG.warn("Conflicto de concurrencia: {}", e.toString());
        return responder(HttpStatus.CONFLICT, "Otra operación modificó los mismos datos al mismo tiempo; intente de nuevo");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ErrorResponse> peticionMalFormada(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "La petición está mal formada o le faltan datos");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> inesperado(Exception e) {
        LOG.error("Error no controlado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");   // sin filtrar detalles internos
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), estado.getReasonPhrase(), mensaje));
    }
}
