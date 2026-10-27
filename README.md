# Domain Service · SIGEPAC (Grupo 4) — avance

Servicio de dominio para administrar una plaza cívica: negocios (locales), deudas de administración y alquiler, pagos,
estado de cuenta, listado y búsqueda (HU-8 a HU-12 del backlog). Arquitectura hexagonal. Valida el JWT que emite
**AuthCore** (firma compartida, HS384). Aplica lo visto en las semanas 5, 6 y 7 de Ingeniería del Software III.

## Estado (leer primero)

| Capa | Estado |
|---|---|
| `domain` + `application` (Java puro) | Compiladas con `javac` y **70 pruebas ejecutadas, todas en verde** |
| `infrastructure` (Spring, JPA, JWT, REST) | **Escrita pero NO compilada ni ejecutada**: el entorno donde se generó no podía descargar dependencias de Maven. Lo primero es correr `mvn verify` |
| Pruebas con Spring (REST, JPA, ArchUnit, JWT) | Escritas, **no ejecutadas** |

Si `mvn verify` muestra algún error de compilación o de prueba en infraestructura, es de esperar y se corrige rápido;
el núcleo no depende de ella.

## Cómo se ve cada semana en el código

| Semana | Tema | Dónde está |
|---|---|---|
| 5 | **S**RP | Un caso de uso por clase (`PagoService`, `DeudaService`…); `JwtTokenValidator` valida y `JwtAuthenticationFilter` solo extrae el token |
| 5 | **O**CP | Nuevos conceptos de deuda o reglas de reparto = clases/valores nuevos; el flujo existente no se toca |
| 5 | **L**SP | `contract/*RepositoryContractTest`: las mismas pruebas pasan el doble en memoria y el adaptador JPA |
| 5 | **I**SP | Casos de uso pequeños en `application/port/in` en vez de una interfaz gigante |
| 5 | **D**IP | `application` solo conoce puertos (`port/out`); `BeanConfig` conecta todo en el borde. ArchUnit lo vigila |
| 6 | Núcleo de dominio | Objetos de valor (`Dinero`, `CodigoLocal`, `Periodo`, `Documento`, `Propietario`), entidades (`Negocio`, `Deuda`, `Pago`), servicios de dominio (`AplicacionPagoService`, `EstadoCuentaService`) |
| 7 | Factory | `registrar()`, `reconstruir()`, `Dinero.de()`, `Periodo.de()`, `CodigoLocal.de()` |
| 7 | Adapter / Mapper | `infrastructure/adapters/out/persistence/mapper` (dominio ↔ entidad JPA) |
| 7 | Repository | Puertos `*RepositoryPort` + `*RepositoryAdapter` |

```
domain/          Java puro: vo, model, service, exception
application/     port/in (casos de uso, comandos, resultados), port/out (repositorios, transacción), service
infrastructure/  config (composición), security (JWT), adapters/in/rest, adapters/out/persistence
```

## Endpoints

Todos exigen `Authorization: Bearer <JWT de AuthCore>` y rol **ADMIN**.

| HU | Método y ruta | Qué hace |
|---|---|---|
| 8 | `POST /api/negocios` · `PUT /api/negocios/{id}` · `GET /api/negocios/{id}` | Alta, edición y consulta de un negocio (local + propietario) |
| 8 | `DELETE /api/negocios/{id}` | **Baja lógica**; se rechaza (409) si el negocio aún debe dinero |
| 9 | `POST /api/negocios/{id}/deudas` · `PUT /api/deudas/{id}` | Registrar deuda (ADMINISTRACION o ALQUILER, por mes) y corregir su valor |
| 10 | `POST /api/negocios/{id}/pagos` | Registra el pago, lo aplica a las deudas más antiguas primero y devuelve el comprobante |
| 10 | `POST /api/pagos/{id}/anulacion` · `GET /api/negocios/{id}/estado-cuenta` | Anular un pago (devuelve los saldos) y ver deudas, pagos y saldo |
| 11 | `GET /api/negocios?pagina=&tamano=&incluirInactivos=` | Listado paginado con saldo pendiente |
| 12 | `GET /api/negocios?nombre=&local=&documento=&conSaldo=` | Búsqueda con filtros combinables (mismo endpoint) |

Errores: 400 dato inválido · 401 sin token · 403 sin permiso · 404 no existe · 409 local ocupado, deuda repetida, pago mayor que el saldo,
pago ya anulado, negocio con saldo o inactivo, conflicto de concurrencia.
Hay ejemplos listos en `requests.http`.

## Ejecutar

```bash
export JWT_SECRET_BASE64='<la misma clave Base64 que usa AuthCore>'   # sin esto la app no arranca, a propósito
mvn verify                      # compila y corre todas las pruebas
mvn spring-boot:run             # puerto 8082, H2 en memoria
# producción: SPRING_PROFILES_ACTIVE=prod DB_URL=... DB_USER=... DB_PASSWORD=...
```

La clave debe tener al menos 48 bytes para HS384. La clave nunca va en el repositorio.

## Decisiones de negocio propuestas (confirmar con el grupo)

1. El propietario es un dato del negocio (documento, nombre, teléfono), no una entidad aparte.
2. Un local solo puede tener **un negocio activo**; al dar de baja un negocio, el local queda libre. La base de datos lo refuerza (`local_activo` único).
3. Una deuda se identifica por **negocio + concepto + período (año/mes)**; no se repite.
4. Un pago se aplica en orden **FIFO**: primero el período más antiguo (desempate por id). Un pago puede cubrir varias deudas y dejar una parcial.
5. **No se admite pago mayor al saldo** (sin saldo a favor). Todo el pago es atómico: o se aplica completo o no se guarda nada.
6. Los pagos **no se editan ni se borran: se anulan**; anular devuelve el saldo a cada deuda y no se puede hacer dos veces.
7. No se baja un negocio con saldo pendiente; el valor de una deuda no puede quedar por debajo de lo ya pagado.
8. Todas las operaciones son de ADMIN (decisión mía; el documento del grupo no define permisos por operación).
9. Moneda: pesos colombianos con 2 decimales; zona horaria `America/Bogota`; la fecha de un pago no puede ser futura.
10. El filtro «con saldo» y la paginación se calculan en memoria sobre los negocios filtrados: correcto para una plaza (cientos de locales), a optimizar con consulta SQL si crece.

## Pendiente

Migraciones Flyway (hoy `ddl-auto: update`), reintento automático ante conflicto de concurrencia, pruebas de concurrencia
reales sobre el bloqueo optimista, mover filtro por saldo y paginación a SQL, y mapear los roles exactos que emite AuthCore.
