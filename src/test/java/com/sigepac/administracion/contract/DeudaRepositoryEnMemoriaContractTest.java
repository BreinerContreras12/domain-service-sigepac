package com.sigepac.administracion.contract;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.testsupport.DeudaRepositoryEnMemoria;

class DeudaRepositoryEnMemoriaContractTest extends DeudaRepositoryContractTest {
    private final DeudaRepositoryEnMemoria repo = new DeudaRepositoryEnMemoria();
    private long seq = 0;
    @Override protected DeudaRepositoryPort repo() { return repo; }
    @Override protected long crearNegocio(String local) { return ++seq; }
}
