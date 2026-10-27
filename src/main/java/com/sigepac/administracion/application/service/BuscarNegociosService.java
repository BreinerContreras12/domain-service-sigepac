package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.BuscarNegociosUseCase;
import com.sigepac.administracion.application.port.in.ListarNegociosUseCase;
import com.sigepac.administracion.application.port.in.command.BuscarNegociosQuery;
import com.sigepac.administracion.application.port.in.result.PaginaNegocios;
import com.sigepac.administracion.application.port.in.result.ResumenNegocio;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.FiltroNegocio;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Documento;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * HU-11 (listado) y HU-12 (búsqueda): el listado es una búsqueda sin criterios.
 * Limitación conocida: filtra por saldo y pagina en memoria, aceptable para los locales de una plaza;
 * con miles de negocios habría que llevar esa consulta a la base de datos.
 */
public class BuscarNegociosService implements BuscarNegociosUseCase, ListarNegociosUseCase {

    static final int TAMANO_MAXIMO = 100;

    private final NegocioRepositoryPort negocios;
    private final DeudaRepositoryPort deudas;
    private final EstadoCuentaService estadoCuenta;

    public BuscarNegociosService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, EstadoCuentaService estadoCuenta) {
        this.negocios = negocios;
        this.deudas = deudas;
        this.estadoCuenta = estadoCuenta;
    }

    @Override
    public PaginaNegocios listar(boolean incluirInactivos, int pagina, int tamano) {
        return buscar(new BuscarNegociosQuery(null, null, null, null, incluirInactivos, pagina, tamano));
    }

    @Override
    public PaginaNegocios buscar(BuscarNegociosQuery q) {
        if (q.pagina() < 0) {
            throw new ValorInvalidoException("La página no puede ser negativa");
        }
        if (q.tamano() < 1 || q.tamano() > TAMANO_MAXIMO) {
            throw new ValorInvalidoException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        if (q.nombre() != null && q.nombre().isBlank()) {
            throw new ValorInvalidoException("El nombre a buscar no puede estar vacío");
        }
        CodigoLocal local = q.local() == null ? null : CodigoLocal.de(q.local());
        Documento documento = q.documentoPropietario() == null ? null : Documento.de(q.documentoPropietario());

        List<Negocio> encontrados = negocios.buscar(new FiltroNegocio(
                q.nombre() == null ? null : q.nombre().trim(), local, documento, q.incluirInactivos()));
        Map<Long, Dinero> saldos = estadoCuenta.saldosPorNegocio(
                deudas.buscarPorNegocios(encontrados.stream().map(Negocio::getId).toList()));

        List<ResumenNegocio> filtrados = encontrados.stream()
                .sorted(Comparator.comparing(Negocio::getLocal).thenComparing(Negocio::getId))
                .map(n -> new ResumenNegocio(n, saldos.getOrDefault(n.getId(), Dinero.CERO)))
                .filter(r -> q.conSaldo() == null || r.saldoPendiente().esMayorQueCero() == q.conSaldo())
                .toList();

        long total = filtrados.size();
        int desde = (int) Math.min((long) q.pagina() * q.tamano(), total);
        int hasta = (int) Math.min(desde + (long) q.tamano(), total);
        int paginas = (int) ((total + q.tamano() - 1) / q.tamano());
        return new PaginaNegocios(filtrados.subList(desde, hasta), q.pagina(), q.tamano(), total, paginas);
    }
}
