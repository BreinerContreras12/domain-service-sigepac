package com.sigepac.administracion.infrastructure.adapters.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.DeudaRepositoryAdapter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

/**
 * Riesgo del sprint (dominio "Incertidumbre" del PMBOK): dos pagos al mismo tiempo sobre la misma deuda.
 *
 * <p>Se levanta el servicio real (HTTP + seguridad JWT + JPA/H2) y se envían dos pagos desde dos hilos.
 * Para que la carrera ocurra SIEMPRE (y no solo "a veces"), un envoltorio del repositorio de deudas hace que
 * ambos pagos esperen en una barrera justo después de leer el saldo: así los dos leen la misma versión
 * (@Version) antes de que cualquiera guarde. El primero que guarda gana; el segundo recibe 409 y no se guarda nada suyo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "seguridad.jwt.secret-base64=Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==")
class PagosConcurrentesTest {

    private static final String SECRETO = "Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==";
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));
    private static final AtomicLong SECUENCIA = new AtomicLong(System.nanoTime() % 100000);

    /** Cuando no es null, cada lectura de deudas de un negocio espera aquí a la otra. */
    static volatile CyclicBarrier barrera;

    @TestConfiguration
    static class ForzarCarrera {
        @Bean
        @Primary
        DeudaRepositoryPort deudasConBarrera(DeudaRepositoryAdapter real) {
            return new DeudaRepositoryPort() {
                @Override public Deuda guardar(Deuda deuda) { return real.guardar(deuda); }
                @Override public Optional<Deuda> buscarPorId(long id) { return real.buscarPorId(id); }
                @Override public boolean existe(long negocioId, ConceptoDeuda concepto, Periodo periodo) {
                    return real.existe(negocioId, concepto, periodo);
                }
                @Override public List<Deuda> buscarPorNegocios(Collection<Long> ids) { return real.buscarPorNegocios(ids); }
                @Override public List<Deuda> buscarPorNegocio(long negocioId) {
                    List<Deuda> leidas = real.buscarPorNegocio(negocioId);
                    CyclicBarrier b = barrera;
                    if (b != null) {
                        try {
                            b.await(5, TimeUnit.SECONDS);   // los dos pagos ya leyeron el mismo saldo
                        } catch (Exception e) {
                            // si el otro hilo no llega, se sigue sin carrera (la prueba lo detectará)
                        }
                    }
                    return leidas;
                }
            };
        }
    }

    @LocalServerPort
    private int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    @AfterEach
    void quitarBarrera() {
        barrera = null;
    }

    @Test
    @DisplayName("Dos pagos que juntos superan el saldo: uno se aplica (201), el otro 409 y el saldo nunca queda negativo")
    void dosPagosQueJuntosSuperanElSaldo() throws Exception {
        String admin = token("ADMIN");
        long negocio = crearNegocio(admin);
        crearDeuda(admin, negocio, 100000);

        List<HttpResponse<String>> respuestas = pagarAlMismoTiempo(admin, negocio, 60000, 60000);

        assertThat(respuestas).extracting(HttpResponse::statusCode).containsExactlyInAnyOrder(201, 409);
        HttpResponse<String> perdedor = respuestas.stream().filter(r -> r.statusCode() == 409).findFirst().orElseThrow();
        assertThat(perdedor.body()).contains("No se guardó ningún cambio");

        String estado = enviar(admin, "GET", "/api/negocios/" + negocio + "/estado-cuenta", null).body();
        assertThat(numero(estado, "$.totalPagado")).isEqualTo(60000.0);
        assertThat(numero(estado, "$.saldo")).isEqualTo(40000.0);
        assertThat((List<?>) JsonPath.read(estado, "$.pagos")).hasSize(1);
    }

    @Test
    @DisplayName("Dos pagos que sí caben: el segundo igual recibe 409 (leyó un saldo viejo); al reintentar pasa")
    void elPagoRechazadoSePuedeReintentar() throws Exception {
        String admin = token("ADMIN");
        long negocio = crearNegocio(admin);
        crearDeuda(admin, negocio, 100000);

        List<HttpResponse<String>> respuestas = pagarAlMismoTiempo(admin, negocio, 30000, 30000);
        assertThat(respuestas).extracting(HttpResponse::statusCode).containsExactlyInAnyOrder(201, 409);

        // La administradora consulta y reintenta: ahora sí se aplica sobre el saldo real
        assertThat(enviar(admin, "POST", "/api/negocios/" + negocio + "/pagos", pagoJson(30000)).statusCode()).isEqualTo(201);

        String estado = enviar(admin, "GET", "/api/negocios/" + negocio + "/estado-cuenta", null).body();
        assertThat(numero(estado, "$.totalPagado")).isEqualTo(60000.0);
        assertThat(numero(estado, "$.saldo")).isEqualTo(40000.0);
        assertThat((List<?>) JsonPath.read(estado, "$.pagos")).hasSize(2);
    }

    // ---------------------------------------------------------------- utilidades

    private List<HttpResponse<String>> pagarAlMismoTiempo(String admin, long negocio, long monto1, long monto2)
            throws Exception {
        barrera = new CyclicBarrier(2);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Callable<HttpResponse<String>>> pagos = List.of(
                    () -> enviar(admin, "POST", "/api/negocios/" + negocio + "/pagos", pagoJson(monto1)),
                    () -> enviar(admin, "POST", "/api/negocios/" + negocio + "/pagos", pagoJson(monto2)));
            List<HttpResponse<String>> respuestas = new ArrayList<>();
            for (Future<HttpResponse<String>> f : hilos.invokeAll(pagos, 30, TimeUnit.SECONDS)) {
                respuestas.add(f.get());
            }
            return respuestas;
        } finally {
            barrera = null;
            hilos.shutdownNow();
        }
    }

    private long crearNegocio(String admin) throws Exception {
        long n = SECUENCIA.incrementAndGet();
        String json = "{\"nombre\":\"Concurrencia " + n + "\",\"local\":\"CC-" + n + "\",\"documentoPropietario\":\"10"
                + (90000000 + n) + "\",\"nombrePropietario\":\"Dueña Prueba\",\"telefonoPropietario\":\"3001112233\"}";
        HttpResponse<String> r = enviar(admin, "POST", "/api/negocios", json);
        assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
        return ((Number) JsonPath.read(r.body(), "$.id")).longValue();
    }

    private void crearDeuda(String admin, long negocio, long valor) throws Exception {
        HttpResponse<String> r = enviar(admin, "POST", "/api/negocios/" + negocio + "/deudas",
                "{\"concepto\":\"ADMINISTRACION\",\"anio\":2026,\"mes\":9,\"valor\":" + valor + "}");
        assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
    }

    private HttpResponse<String> enviar(String token, String metodo, String ruta, String json) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(20));
        if (json == null) {
            b.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            b.header("Content-Type", "application/json").method(metodo, HttpRequest.BodyPublishers.ofString(json));
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String pagoJson(long monto) {
        return "{\"fecha\":\"" + HOY + "\",\"monto\":" + monto + "}";
    }

    private static double numero(String json, String ruta) {
        return ((Number) JsonPath.read(json, ruta)).doubleValue();
    }

    private static String token(String... roles) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject("tester").claim("uid", 99).claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)), Jwts.SIG.HS384).compact();
    }
}
