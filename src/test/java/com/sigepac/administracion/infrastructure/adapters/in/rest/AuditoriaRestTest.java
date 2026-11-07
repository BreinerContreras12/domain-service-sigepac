package com.sigepac.administracion.infrastructure.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * HU-12 de punta a punta: el usuario de la auditoría sale del "sub" del JWT, no del cuerpo de la petición,
 * y la auditoría solo la ve un ADMIN. El secreto es SOLO de pruebas (el mismo de SigepacRestTest).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "seguridad.jwt.secret-base64=Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==")
class AuditoriaRestTest {

    private static final String SECRETO = "Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==";
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));

    @Autowired
    private MockMvc mvc;

    private String token(String usuario, String... roles) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject(usuario).claim("uid", 7).claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)), Jwts.SIG.HS384).compact();
    }

    private MockHttpServletRequestBuilder con(MockHttpServletRequestBuilder peticion, String token, String json) {
        MockHttpServletRequestBuilder b = peticion.header("Authorization", "Bearer " + token);
        return json == null ? b : b.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    @Test
    @DisplayName("Cada cambio guarda el usuario del token y la hora; el pago rechazado no deja rastro")
    void registraQuienHizoCadaCambio() throws Exception {
        String ana = token("ana", "USER", "ADMIN");
        String luis = token("luis", "ADMIN");

        String cuerpo = mvc.perform(con(post("/api/negocios"), ana,
                        "{\"nombre\":\"Frutería Auditada\",\"local\":\"AUD-1\",\"documentoPropietario\":\"1095550001\","
                                + "\"nombrePropietario\":\"Dueña Prueba\",\"telefonoPropietario\":\"3001112233\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(cuerpo, "$.id")).longValue();

        mvc.perform(con(post("/api/negocios/" + id + "/deudas"), ana,
                "{\"concepto\":\"ADMINISTRACION\",\"anio\":2026,\"mes\":10,\"valor\":100000}"))
                .andExpect(status().isCreated());
        String pago = mvc.perform(con(post("/api/negocios/" + id + "/pagos"), luis,
                        "{\"fecha\":\"" + HOY + "\",\"monto\":40000}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long pagoId = ((Number) JsonPath.read(pago, "$.pago.id")).longValue();
        // un pago mayor al saldo se rechaza y NO debe aparecer en la auditoría
        mvc.perform(con(post("/api/negocios/" + id + "/pagos"), luis, "{\"fecha\":\"" + HOY + "\",\"monto\":999999}"))
                .andExpect(status().isConflict());
        mvc.perform(con(post("/api/pagos/" + pagoId + "/anulacion"), ana, null)).andExpect(status().isOk());

        mvc.perform(con(get("/api/negocios/" + id + "/auditoria"), ana, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].accion").value("NEGOCIO_REGISTRADO"))
                .andExpect(jsonPath("$[0].usuario").value("ana"))
                .andExpect(jsonPath("$[1].accion").value("DEUDA_REGISTRADA"))
                .andExpect(jsonPath("$[2].accion").value("PAGO_REGISTRADO"))
                .andExpect(jsonPath("$[2].usuario").value("luis"))
                .andExpect(jsonPath("$[2].entidadId").value(pagoId))
                .andExpect(jsonPath("$[3].accion").value("PAGO_ANULADO"))
                .andExpect(jsonPath("$[3].usuario").value("ana"))
                .andExpect(jsonPath("$[3].fechaHora").exists());
    }

    @Test
    @DisplayName("La auditoría es solo para ADMIN: sin token 401, otro rol 403, negocio inexistente 404")
    void soloAdmin() throws Exception {
        mvc.perform(get("/api/negocios/1/auditoria")).andExpect(status().isUnauthorized());
        mvc.perform(con(get("/api/negocios/1/auditoria"), token("pepe", "USER"), null)).andExpect(status().isForbidden());
        mvc.perform(con(get("/api/negocios/987654/auditoria"), token("ana", "ADMIN"), null)).andExpect(status().isNotFound());
    }
}
