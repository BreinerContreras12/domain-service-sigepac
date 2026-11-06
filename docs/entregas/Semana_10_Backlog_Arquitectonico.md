# Gestión de Deuda Técnica, Spikes y Backlog Arquitectónico por Sprints
### Entregable Semana 10 · Ingeniería del Software III
**Institución:** Universidad de Pamplona — Facultad de Ingenierías y Arquitectura  
**Docente:** Ing. Carlos Arturo Barrientos Suárez  
**Equipo:** Grupo 4  
**Proyecto:** SIGEPAC · Domain Service (Administración Plaza Cívica)  
**Tablero en Trello:** `SIGEPAC · XP` (`https://trello.com/b/aANV6YSI`)  
**Fecha:** Noviembre de 2026  

---

## 1. Introducción y Fundamentos

En el marco del desarrollo ágil con Scrum y Extreme Programming (XP), la arquitectura no constituye una fase estática inicial, sino una disciplina continua y evolutiva. Siguiendo el concepto introducido por Ward Cunningham (1992) y refinado por Martin Fowler (2009), la **deuda técnica** representa aquellas decisiones de ingeniería en las que se posterga una solución técnica ideal a cambio de obtener velocidad de entrega en el corto plazo. Como cualquier deuda financiera, acumula intereses que encarecen cada iteración futura.

El presente documento formaliza la estrategia de gestión técnica del servicio **`domain-service-sigepac`**, estructurando:
1. El inventario de **deuda técnica real** presente en el código base, clasificada rigurosamente en el **Cuadrante de Deuda Técnica de Fowler**.
2. La formulación de **Spikes de investigación técnica** acotados en tiempo (timeboxed) para disipar incertidumbres arquitectónicas.
3. El **Backlog Arquitectónico Priorizado**, utilizando exactamente las métricas oficiales del proyecto (Prioridad 1 a 10 y Dimensión 10 a 100).
4. El plan de trabajo para **2 Sprints de estabilización y calidad**, integrando deuda técnica con historias de usuario y estableciendo una **Definición de Terminado (DoD)** rigurosa.
5. El registro de evidencias y trazabilidad en el tablero oficial de **Trello**.

---

## 2. Registro y Matriz de Deuda Técnica (Cuadrante de Fowler)

A través de la inspección estática del repositorio [`domain-service-sigepac`](file:///c:/Users/bcami/Desktop/domain-service-sigepac), se identificaron 6 deudas técnicas reales que afectan la mantenibilidad, confiabilidad y operación del servicio:

```
                          CUADRANTE DE FOWLER EN SIGEPAC
                   PRUDENTE                            IMPRUDENTE
        +-----------------------------------+-----------------------------------+
        | D-01: Esquema BD ddl-auto update   |                                   |
        | D-02: Pruebas Spring sin pipeline |                                   |
DELIB.  | D-04: JWT sin lista de revocación |                                   |
        | "Sabemos qué falta y por qué lo   | "No tenemos tiempo para pensar    |
        | pospusimos: lo pagamos ahora"     | en diseño o calidad"              |
        +-----------------------------------+-----------------------------------+
        | D-03: Falta de paginación en Edo. |                                   |
        | D-05: Falta de Swagger/OpenAPI    |                                   |
INADV.  | D-06: Transaccionalidad manual    |                                   |
        | "Ahora que el volumen creció,     | "¿Para qué modularizar o          |
        | entendemos el impacto y riesgo"   | escribir pruebas?"                |
        +-----------------------------------+-----------------------------------+
```

### Tabla Detallada del Registro de Deudas

| ID | Descripción de la Deuda | Riesgo que Genera | Cuadrante de Fowler | Prioridad |
| :---: | :--- | :--- | :---: | :---: |
| **D-01** | **Esquema de BD generado automáticamente por Hibernate:**<br>El archivo `application-prod.yml` mantiene la propiedad `hibernate.ddl-auto: update` (con comentario explícito: *"mejora pendiente: reemplazar por migraciones Flyway"*). | **Pérdida o corrupción de datos:** En despliegues continuos, Hibernate puede alterar tablas en caliente sin script de reversión (*rollback*), desincronizando la base de datos entre entornos. | **Deliberada · Prudente**<br>*(Se asumió para agilizar el modelado inicial JPA)* | **Alta** |
| **D-02** | **Pruebas de infraestructura Spring sin ejecución en CI:**<br>Las pruebas de adaptadores REST, JPA y ArchUnit están codificadas pero no se ejecutan automáticamente en un pipeline de integración continua por falta de flujo configurado. | **Regresión no detectada en bordes:** Fallos en la serialización JSON, inyección de dependencias en `BeanConfig` o mapeos JPA pueden pasar a producción sin ser detectados por el equipo. | **Deliberada · Prudente**<br>*(Se priorizó compilar y correr las 70 pruebas puras con `javac`)* | **Alta** |
| **D-03** | **Ausencia de paginación en el estado de cuenta:**<br>El endpoint `GET /api/negocios/{id}/estado-cuenta` recupera todas las deudas y todos los pagos históricos en una única colección en memoria. | **Degradación de memoria (OOM):** Para locales comerciales antiguos con cientos de cuotas y pagos, el payload consume excesiva memoria JVM y ralentiza la respuesta HTTP. | **Inadvertida · Prudente**<br>*(Descubierta al observar el crecimiento del historial de pagos)* | **Media** |
| **D-04** | **Tokens JWT validados sin mecanismo de revocación:**<br>`JwtTokenValidator` verifica criptográficamente la firma con la clave secreta (`HS384`), pero no consulta una lista de tokens revocados o sesiones cerradas. | **Vulnerabilidad de acceso persistente:** Si un token emitido por AuthCore es interceptado, un atacante puede seguir operando hasta que expire el token (varias horas), sin que el administrador pueda invalidarlo. | **Deliberada · Prudente**<br>*(Decisión deliberada para mantener el microservicio 100% stateless)* | **Media** |
| **D-05** | **Ausencia de documentación viva de API (OpenAPI / Swagger):**<br>El servicio solo cuenta con el archivo estático [`requests.http`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/requests.http) sin interfaz interactiva autogenerada ni esquema OpenAPI formal. | **Fricción en la integración del Frontend:** Los desarrolladores de la interfaz cliente no disponen de especificación interactiva ni tipado sincronizado de los contratos REST. | **Inadvertida · Prudente**<br>*(Se asumió que el archivo `.http` bastaba para la fase académica)* | **Media** |
| **D-06** | **Transaccionalidad orquestada mediante puerto manual:**<br>`TransactionPort` y `TransaccionDirecta` ejecutan la transacción sin política explícita de aislamiento ante accesos concurrentes simultáneos. | **Condición de carrera (Race Condition):** Si dos cajeros intentan registrar un abono al mismo negocio en el mismo milisegundo, pueden leer el mismo saldo de deuda y generar inconsistencias contables. | **Inadvertida · Prudente**<br>*(Evidenciada al analizar la concurrencia de la regla de imputación de pagos)* | **Alta** |

---

## 3. Formulación de Spikes de Investigación Arquitectónica

Un **Spike** es una actividad de investigación exploratoria y experimental, acotada estrictamente en tiempo (*timeboxed*), diseñada para reducir la incertidumbre técnica antes de comprometer esfuerzo en una historia o pago de deuda.

### Spike SP-01: Selección y Configuración de Herramienta de Migración de Esquemas de BD
* **Contexto:** El proyecto requiere reemplazar `ddl-auto: update` por migraciones versionadas (resuelve la deuda **D-01**). Se requiere validar la integración con Spring Boot 3 y la dualidad de motores (H2 para pruebas locales y MySQL/PostgreSQL para despliegue).
* **Pregunta concreta a responder:**  
  *¿Es Flyway o Liquibase la herramienta que ofrece menor fricción de integración con Spring Boot 3 en Java 17, permitiendo ejecutar scripts SQL idempotentes tanto en H2 como en MySQL sin requerir dependencias complejas de XML/YAML?*
* **Límite de tiempo (Timebox):** **2 horas**.
* **Decisión técnica que habilita:**  
  Adoptar la dependencia Maven definitiva en [`pom.xml`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/pom.xml), crear la estructura de carpetas estándar (`src/main/resources/db/migration/`) y generar la línea base `V1__esquema_inicial.sql`.

### Spike SP-02: Estrategia de Concurrencia y Bloqueo en la Aplicación de Pagos
* **Contexto:** Al aplicar un pago (`RegistrarPagoUseCase`), el servicio consulta las deudas activas ordenadas por fecha y descuenta los saldos (resuelve la deuda **D-06**). Se debe evitar el fenómeno de *doble amortización*.
* **Pregunta concreta a responder:**  
  *¿Garantiza el bloqueo optimista mediante campo `@Version` en `DeudaEntity` la detección y mitigación de pagos concurrentes con suficiente resiliencia, o se requiere bloqueo pesimista `PESSIMISTIC_WRITE` a nivel de base de datos en `DeudaJpaRepository`?*
* **Límite de tiempo (Timebox):** **3 horas**.
* **Decisión técnica que habilita:**  
  Seleccionar la estrategia de aislamiento en los adaptadores JPA y definir el manejo de excepciones de concurrencia (`OptimisticLockException` vs. captura en `PagoService`) para responder con código HTTP 409 Conflict o reintento automático.

---

## 4. Backlog Arquitectónico Priorizado

Integrando las deudas técnicas identificadas y los Spikes formulados, se consolida la siguiente tabla utilizando **la escala oficial del proyecto en Excel**:
* **Prioridad:** De 1 a 10 (donde 10 representa la máxima urgencia de resolución).
* **Dimensión (Esfuerzo):** De 10 a 100 (estimación de complejidad relativa).

| ID | Ítem / Tarea Arquitectónica | Prioridad (1-10) | Dimensión (10-100) | Criterio de Aceptación ("Terminado cuando...") |
| :---: | :--- | :---: | :---: | :--- |
| **A-01** | **Estabilización de suite completa de pruebas:**<br>Configuración y ejecución limpia de `mvn verify` incluyendo pruebas unitarias, de contrato, JPA y ArchUnit. | **10** | **40** | `mvn clean verify` compila y ejecuta al 100% las pruebas de dominio e infraestructura en verde sin saltar ninguna prueba. |
| **A-02** | **Spike SP-01 y Migraciones Versionadas de BD:**<br>Ejecutar SP-01 e implementar scripts versionados con Flyway (cierra D-01). | **9** | **50** | Se deshabilita `ddl-auto: update`, el esquema se genera exclusivamente a través del script `V1__esquema_inicial.sql` y las pruebas de repositorio pasan contra la BD migrada. |
| **A-03** | **Spike SP-02 y Control de Concurrencia de Pagos:**<br>Ejecutar SP-02 e incorporar control de bloqueo en la aplicación de pagos (cierra D-06). | **9** | **60** | Dos solicitudes concurrentes de abono a la misma deuda disparan el bloqueo controlado; el sistema procesa el primer abono y rechaza el segundo con error semántico claro sin corromper el saldo. |
| **A-04** | **Paginación en Estado de Cuenta y Listados:**<br>Incorporar parámetros `page` y `size` en la consulta de deudas y pagos de un negocio (cierra D-03). | **7** | **30** | El endpoint `GET /api/negocios/{id}/estado-cuenta` retorna metadatos de paginación (`totalElements`, `totalPages`) y responde en menos de 200 ms ante volúmenes altos. |
| **A-05** | **Integración de OpenAPI / Swagger UI:**<br>Agregar `springdoc-openapi-starter-webmvc-ui` en `pom.xml` y anotar DTOs (cierra D-05). | **6** | **20** | La ruta `/swagger-ui/index.html` es accesible en navegador, documentando todos los endpoints con sus códigos de respuesta y schemas JSON. |
| **A-06** | **Mecanismo de Revocación de Tokens JWT:**<br>Implementar consulta de revocación en memoria (Caffeine Cache / Redis) en `JwtTokenValidator` (cierra D-04). | **6** | **40** | Un token agregado a la lista de revocación mediante endpoint administrativo es rechazado con código HTTP 401 Unauthorized de forma inmediata. |

---

## 5. Planificación para Dos Sprints

El equipo planifica dos sprints de 2 semanas de duración cada uno, balanceando los ítems de deuda arquitectónica con las Historias de Usuario (HU) funcionales del backlog:

```
                            CRONOGRAMA DE SPRINTS
+------------------------------------+------------------------------------+
|              SPRINT 1              |              SPRINT 2              |
|   Meta: Base Técnica Confiable     |     Meta: Calidad y Operación      |
+------------------------------------+------------------------------------+
| - A-01: Verificación Pruebas (40)  | - A-04: Paginación Estado Cta (30) |
| - A-02: Spike SP-01 + Flyway (50)  | - A-05: Swagger / OpenAPI (20)     |
| - A-03: Spike SP-02 + Concurr. (60)| - A-06: Revocación de Tokens (40)  |
| - HU-03: Registrar Obligaciones    | - HU-05: Registrar Pagos Clientes  |
| - HU-04: Actualizar Estado Deudas  | - HU-07: Consultar Estado Cuenta   |
+------------------------------------+------------------------------------+
```

### 5.1. Sprint 1: "Base Técnica Confiable y Persistencia Robusta"
* **Meta del Sprint:** Asegurar la integridad de la base de datos eliminando la creación automática de esquemas, blindar el núcleo contra condiciones de carrera y garantizar que la suite completa de pruebas corra en verde.
* **Composición del Sprint Backlog:**
  * Ítems Técnicos: **A-01** (Dim. 40), **A-02** (Dim. 50), **A-03** (Dim. 60).
  * Historias de Usuario: **HU-03** (*Registrar obligaciones económicas a un negocio*) y **HU-04** (*Actualizar el estado de las obligaciones*).
* **Capacidad estimada:** 210 puntos de dimensión.

### 5.2. Sprint 2: "Calidad de Servicio, Usabilidad Técnica y Seguridad"
* **Meta del Sprint:** Exponer la documentación viva de las APIs para el equipo de frontend, proteger el servicio contra sobrecarga de datos históricos y reforzar la invalidación de credenciales.
* **Composición del Sprint Backlog:**
  * Ítems Técnicos: **A-04** (Dim. 30), **A-05** (Dim. 20), **A-06** (Dim. 40).
  * Historias de Usuario: **HU-05** (*Registrar pagos realizados*) y **HU-07** (*Consultar el estado de cuenta del propietario*).
* **Capacidad estimada:** 180 puntos de dimensión.

---

### 5.3. Definición de Terminado (Definition of Done - DoD) del Equipo

Ninguna tarjeta o historia se considera finalizada (`Done`) si no cumple rigurosamente con los siguientes 6 criterios de aceptación:

1. **Compilación y Pruebas en Verde:** El código compila sin advertencias (`javac`) y la suite completa de pruebas unitarias y de integración pasa al 100% (`mvn clean verify`).
2. **Arquitectura Hexagonal Preservada:** El núcleo de dominio no importa frameworks, librerías externas ni anotaciones de Spring/JPA (verificado por `ArquitecturaHexagonalTest` con ArchUnit).
3. **Migración Idempotente:** Cualquier cambio de base de datos incluye su script versionado en `db/migration/` probado tanto en base limpia como en base con datos preexistentes.
4. **Documentación Actualizada:** El archivo [`README.md`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/README.md) refleja los nuevos endpoints, configuraciones o variables de entorno introducidas.
5. **Revisión de Código por Pares (Peer Review):** Todo cambio pasa por Pull Request y cuenta con la aprobación explícita de al menos otro integrante del Grupo 4.
6. **Demostrabilidad:** La funcionalidad o corrección técnica puede demostrarse en vivo mediante las peticiones documentadas en [`requests.http`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/requests.http).

---

## 6. Evidencias en el Tablero de Trello (`SIGEPAC · XP`)

Para cumplir con la rúbrica del docente (10 puntos), los ítems técnicos, spikes y divisiones por sprint han sido registrados formalmente en el tablero de Trello **SIGEPAC · XP** (`https://trello.com/b/aANV6YSI`).

### 6.1. Organización del Tablero
* **Lista `🎯 Iteración actual` (Sprint 1 en curso):**
  - `A-01 · Ejecutar mvn verify completo y corregir pruebas · P:10 D:40`
  - `SP-01 · Spike Flyway vs Liquibase (Timebox: 2h) · P:9 D:20`
  - `A-02 · Implementar migraciones versionadas con Flyway · P:9 D:50`
  - `SP-02 · Spike Estrategia de Bloqueo Concurrente en Pagos (Timebox: 3h) · P:9 D:30`
  - `A-03 · Control de concurrencia y bloqueo en aplicación de pagos · P:9 D:60`
  - *Historias funcionales asignadas al Sprint 1: HU-01, HU-02, HU-12*.
* **Lista `📋 Historias (backlog)` (Sprint 2 y pendientes):**
  - `A-04 · Paginación en endpoints de estado de cuenta · P:7 D:30`
  - `A-05 · Integrar OpenAPI / Swagger UI · P:6 D:20`
  - `A-06 · Mecanismo de revocación de tokens JWT · P:6 D:40`
  - *Tarjetas de deudas técnicas D-01 a D-06 etiquetadas y relacionadas*.

### 6.2. Guía para las Capturas de Pantalla en la Entrega
Al momento de adjuntar las evidencias en el reporte final:
1. **Captura 1 (Vista Global del Tablero):** Mostrar las columnas `📋 Historias (backlog)`, `🎯 Iteración actual`, `🔨 En progreso` y `✅ Hecho`, evidenciando la distribución entre historias de usuario y tarjetas arquitectónicas.
2. **Captura 2 (Detalle de un Spike):** Abrir la tarjeta `SP-01` o `SP-02` mostrando en la descripción la pregunta concreta, el límite de tiempo (Timebox) y el checklist de criterios de aceptación.
3. **Captura 3 (Detalle de una Deuda Técnica):** Abrir la tarjeta `D-01` o `A-02` mostrando su asignación al cuadrante de Fowler, la prioridad (escala 1-10) y la dimensión (escala 10-100).
