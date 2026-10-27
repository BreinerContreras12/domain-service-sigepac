package com.sigepac.administracion.contract;

import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.testsupport.PagoRepositoryEnMemoria;

class PagoRepositoryEnMemoriaContractTest extends PagoRepositoryContractTest {
    private final PagoRepositoryEnMemoria repo = new PagoRepositoryEnMemoria();
    private long seq = 0;
    @Override protected PagoRepositoryPort repo() { return repo; }
    @Override protected long crearNegocio(String local) { return ++seq; }
    @Override protected long crearDeuda(long negocioId, int mes) { return ++seq; }
}
