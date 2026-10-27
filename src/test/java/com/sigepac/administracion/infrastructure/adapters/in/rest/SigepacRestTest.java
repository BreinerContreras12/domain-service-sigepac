package com.sigepac.administracion.infrastructure.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prueba de punta a punta (HTTP -> casos de uso -> dominio -> H2) con seguridad JWT real.
 * El secreto de abajo es SOLO de pruebas (48 bytes aleatorios en Base64); no es el de AuthCore.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "seguridad.jwt.secret-base64=Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==")
class SigepacRestTest {

    private static final String SECRETO = "Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==";
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));

    @Autowired
    private MockMvc mvc;

    private String token(String... roles) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject("tester").claim("uid", 99).claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)), Jwts.SIG.HS384).compact();
    }

    private MockHttpServletRequestBuilder con(MockHttpServletRequestBuilder peticion, String token, String json) {
        MockHttpServletRequestBuilder b = peticion.header("Authorization", "Bearer " + token);
        return json == null ? b : b.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private long crearNegocio(String admin, String nombre, String local, String documento) throws Exception {
        String json = "{\"nombre\":\"" + nombre + "\",\"local\":\"" + local + "\",\"documentoPropietario\":\"" + documento
                + "\",\"nombrePropietario\":\"Dueña Prueba\",\"telefonoPropietario\":\"3001112233\"}";
        String cuerpo = mvc.perform(con(post("/api/negocios"), admin, json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    private void crearDeuda(String admin, long negocioId, String concepto, int mes, long valor) throws Exception {
        mvc.perform(con(post("/api/negocios/" + negocioId + "/deudas"), admin,
                "{\"concepto\":\"" + concepto + "\",\"anio\":2026,\"mes\":" + mes + ",\"valor\":" + valor + "}"))
                .andExpect(status().isCreated());
    }

    private String pagoJson(long monto) {
        return "{\"fecha\":\"" + HOY + "\",\"monto\":" + monto + "}";
    }

    @Test
    @DisplayName("Sin token o con token falso: 401")
    void sinCredencialesEs401() throws Exception {
        mvc.perform(get("/api/negocios")).andExpect(status().isUnauthorized());
        mvc.perform(con(get("/api/negocios"), "token.falso.aqui", null)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un USER no es ADMIN: 403 en todo /api")
    void usuarioNormalNoPasa() throws Exception {
        String user = token("USER");
        mvc.perform(con(get("/api/negocios"), user, null)).andExpect(status().isForbidden());
        mvc.perform(con(post("/api/negocios/1/pagos"), user, pagoJson(1))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Flujo completo: negocio, deudas, pago FIFO, estado de cuenta, anulación y baja")
    void flujoCompleto() throws Exception {
        String admin = token("USER", "ADMIN");
        long id = crearNegocio(admin, "Panadería Flujo", "FLU-1", "1090700001");

        // un local activo solo tiene un negocio
        mvc.perform(con(post("/api/negocios"), admin,
                "{\"nombre\":\"Otro\",\"local\":\"flu-1\",\"documentoPropietario\":\"1090700002\",\"nombrePropietario\":\"X\"}"))
                .andExpect(status().isConflict());

        crearDeuda(admin, id, "ADMINISTRACION", 9, 100000);
        crearDeuda(admin, id, "ADMINISTRACION", 10, 100000);
        mvc.perform(con(post("/api/negocios/" + id + "/deudas"), admin,
                "{\"concepto\":\"ADMINISTRACION\",\"anio\":2026,\"mes\":10,\"valor\":5}")).andExpect(status().isConflict());

        // pago de 130.000: cubre septiembre completo y 30.000 de octubre
        String cuerpo = mvc.perform(con(post("/api/negocios/" + id + "/pagos"), admin, pagoJson(130000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoAnterior").value(200000.0))
                .andExpect(jsonPath("$.saldoNuevo").value(70000.0))
                .andExpect(jsonPath("$.pago.aplicaciones.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        long pagoId = ((Number) JsonPath.read(cuerpo, "$.pago.id")).longValue();

        mvc.perform(con(get("/api/negocios/" + id + "/estado-cuenta"), admin, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeudas").value(200000.0))
                .andExpect(jsonPath("$.totalPagado").value(130000.0))
                .andExpect(jsonPath("$.saldo").value(70000.0));

        // no se puede pagar más de lo que se debe
        mvc.perform(con(post("/api/negocios/" + id + "/pagos"), admin, pagoJson(70001)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.containsString("70000")));

        // con saldo pendiente no se da de baja
        mvc.perform(con(delete("/api/negocios/" + id), admin, null)).andExpect(status().isConflict());

        // anular devuelve el saldo; anular dos veces es conflicto
        mvc.perform(con(post("/api/pagos/" + pagoId + "/anulacion"), admin, null))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ANULADO"));
        mvc.perform(con(get("/api/negocios/" + id + "/estado-cuenta"), admin, null))
                .andExpect(jsonPath("$.saldo").value(200000.0));
        mvc.perform(con(post("/api/pagos/" + pagoId + "/anulacion"), admin, null)).andExpect(status().isConflict());

        // pagar todo y entonces sí dar de baja
        mvc.perform(con(post("/api/negocios/" + id + "/pagos"), admin, pagoJson(200000))).andExpect(status().isCreated());
        mvc.perform(con(delete("/api/negocios/" + id), admin, null))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("Listado y búsqueda: filtros, saldo y paginación")
    void listadoYBusqueda() throws Exception {
        String admin = token("ADMIN");
        long a = crearNegocio(admin, "Zapatería Búsqueda", "BUS-1", "1090800001");
        crearNegocio(admin, "Zapatos Búsqueda", "BUS-2", "1090800001");
        crearDeuda(admin, a, "ALQUILER", 10, 50000);

        mvc.perform(con(get("/api/negocios?documento=1090800001"), admin, null))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(con(get("/api/negocios?documento=1090800001&conSaldo=true"), admin, null))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].saldoPendiente").value(50000.0));
        mvc.perform(con(get("/api/negocios?nombre=zapater&documento=1090800001"), admin, null))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(con(get("/api/negocios?documento=1090800001&tamano=1&pagina=1"), admin, null))
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mvc.perform(con(get("/api/negocios?documento=0000000000"), admin, null))
                .andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(con(get("/api/negocios?tamano=0"), admin, null)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Editar un negocio y actualizar el valor de una deuda")
    void editarYActualizar() throws Exception {
        String admin = token("ADMIN");
        long id = crearNegocio(admin, "Café Edición", "EDI-1", "1090900001");
        mvc.perform(con(put("/api/negocios/" + id), admin,
                "{\"nombre\":\"Café Renovado\",\"local\":\"EDI-1\",\"documentoPropietario\":\"1090900001\",\"nombrePropietario\":\"Dueña\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Café Renovado"));

        String cuerpo = mvc.perform(con(post("/api/negocios/" + id + "/deudas"), admin,
                "{\"concepto\":\"ALQUILER\",\"anio\":2026,\"mes\":10,\"valor\":100000}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long deudaId = ((Number) JsonPath.read(cuerpo, "$.id")).longValue();

        mvc.perform(con(put("/api/deudas/" + deudaId), admin, "{\"valor\":120000}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.saldo").value(120000.0));
        mvc.perform(con(post("/api/negocios/" + id + "/pagos"), admin, pagoJson(100000))).andExpect(status().isCreated());
        mvc.perform(con(put("/api/deudas/" + deudaId), admin, "{\"valor\":90000}")).andExpect(status().isConflict());
    }

    @Test
    void inexistentesDan404YDatosInvalidosDan400() throws Exception {
        String admin = token("ADMIN");
        mvc.perform(con(get("/api/negocios/999999"), admin, null)).andExpect(status().isNotFound());
        mvc.perform(con(get("/api/negocios/999999/estado-cuenta"), admin, null)).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/negocios/999999/pagos"), admin, pagoJson(10))).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/pagos/999999/anulacion"), admin, null)).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/negocios"), admin, "{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.startsWith("Faltan campos obligatorios")));
        mvc.perform(con(post("/api/negocios"), admin, "esto no es json")).andExpect(status().isBadRequest());
        long id = crearNegocio(admin, "Negocio Fecha", "FEC-1", "1090600001");
        crearDeuda(admin, id, "ALQUILER", 10, 1000);
        mvc.perform(con(post("/api/negocios/" + id + "/pagos"), admin,
                "{\"fecha\":\"" + HOY.plusDays(1) + "\",\"monto\":500}")).andExpect(status().isBadRequest());
    }
}
