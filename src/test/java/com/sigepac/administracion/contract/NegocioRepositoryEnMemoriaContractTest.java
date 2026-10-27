package com.sigepac.administracion.contract;

import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.testsupport.NegocioRepositoryEnMemoria;

class NegocioRepositoryEnMemoriaContractTest extends NegocioRepositoryContractTest {
    private final NegocioRepositoryEnMemoria repo = new NegocioRepositoryEnMemoria();
    @Override protected NegocioRepositoryPort repo() { return repo; }
}
