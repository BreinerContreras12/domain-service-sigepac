# Proyecto Integrador · Semana 9
# Taller 9 — El cambio de alcance
**Hoja de trabajo del grupo · Universidad de Pamplona · Ingeniería del Software III**

---

### Datos del Equipo y Proyecto

| Campo | Información |
| :--- | :--- |
| **Grupo:** | Grupo 4 |
| **Proyecto:** | SIGEPAC · Domain Service (Administración Plaza Cívica) |
| **Fecha:** | Noviembre de 2026 |
| **Integrantes:** | Equipo de Desarrollo Grupo 4 |
| **Repositorio analizado:** | `domain-service-sigepac` (Arquitectura Hexagonal en Java 17 / Spring Boot 3) |

> **Tres reglas de oro del taller:**
> 1. *Se mide con cosas que se cuentan:* Archivos modificados, capas tocadas, pruebas que cambian, fases reabiertas. No se inventan horas ni dinero.
> 2. *Si no se puede mostrar en el código, no cuenta:* Cada afirmación está respaldada con el nombre y ruta real del archivo en el repositorio.
> 3. *Un cambio que dolió es un hallazgo valioso:* Se explica objetivamente qué dolió, qué lo absorbió y qué principio arquitectónico o patrón intervino.

---

## Ficha 1 — Cambio 1 · Regla de Negocio

### 1. Descripción del Requerimiento
* **¿Qué pide el cliente?**
  La administración de la plaza cívica establece una nueva política financiera para incentivar el recaudo:
  * Si el inquilino paga dentro de los primeros **10 días calendario** del periodo facturado, se aplica un **descuento por pronto pago del 10%** sobre la deuda de administración (concepto `ADMINISTRACION`).
  * Pasados los 10 días, se cancela el valor pleno.
  * El comprobante y el estado de cuenta deben discriminar explícitamente el valor descontado y el monto neto cancelado.
* **En cascada (fases y documentos a rehacer):**
  * *Fases reabiertas:* Se debe reabrir formalmente la fase de **Requisitos** (análisis del impacto en las historias de pago y facturación) y la fase de **Diseño** (redefinición de diagramas de secuencia del pago y modelo de datos del comprobante).
  * *Documentos a rehacer:* Documento de Especificación de Requisitos de Software (SRS), Matriz de Trazabilidad de Requisitos, Casos de Uso del sistema, Especificación de Pruebas de Aceptación.
  * *Impacto en pruebas:* Se invalida la certificación de la fase de pruebas; es obligatorio ejecutar nuevamente la batería completa de pruebas funcionales y de regresión del sistema completo.
* **En RUP (iteración e hito afectado):**
  * *Iteración:* Se canaliza como una solicitud de cambio (Change Request) incorporada en la **Iteración 3 de la fase de Construcción** (iteración dedicada al módulo de Pagos y Liquidaciones).
  * *Hito afectado:* **No compromete el hito LCA** (Lifecycle Architecture Milestone) de la fase de Elaboración, ya que los puertos y los límites arquitectónicos ya estaban definidos y consolidados. Afecta únicamente el alcance local del hito IOC (Initial Operational Capability).

---

### 2. Archivos tocados en el código por capa

| Capa | Archivos Reales del Repositorio | Detalle del cambio |
| :--- | :--- | :--- |
| **Entrada (REST)** | `infrastructure/adapters/in/rest/dto/response/ComprobanteResponse.java`<br>`infrastructure/adapters/in/rest/dto/request/PagoRequest.java` | Se añade el desglose de `descuentoAplicado` y `valorOriginal` en el comprobante JSON entregado al cliente. |
| **Aplicación** | `application/port/in/command/RegistrarPagoCommand.java`<br>`application/port/in/result/PagoResult.java`<br>`application/service/PagoService.java` | El comando recibe la fecha de transacción; el resultado emite el detalle del descuento y `PagoService` orquesta la llamada al servicio de dominio. |
| **Dominio** | `domain/vo/Dinero.java`<br>`domain/model/Deuda.java`<br>`domain/model/Pago.java`<br>`domain/service/AplicacionPagoService.java` | **Núcleo de la regla:** `Dinero` incorpora el cálculo porcentual (`porcentaje()`); `Deuda` evalúa si el concepto es `ADMINISTRACION` y si califica para descuento; `AplicacionPagoService` aplica el saldo amortizado considerando el pronto pago. |
| **Salida (Persistencia)** | `infrastructure/adapters/out/persistence/entity/PagoEntity.java`<br>`infrastructure/adapters/out/persistence/mapper/PagoMapper.java` | Se añade la columna `descuento_aplicado` en la tabla relacional y su correspondiente mapeo bidireccional. |
| **Pruebas (`src/test`)** | `domain/AplicacionPagoServiceTest.java`<br>`domain/DeudaTest.java`<br>`domain/DineroTest.java`<br>`domain/PagoTest.java`<br>`application/PagoServiceTest.java` | 5 archivos de prueba unitaria deben actualizar sus aserciones fijas para validar el nuevo cálculo de amortización y descuento. |
| **Config. y Documentación** | `README.md`<br>`requests.http` | Se actualizan los ejemplos del endpoint `POST /api/negocios/{id}/pagos` reflejando el comprobante con descuento. |

* **Total de archivos producción:** 9 archivos
* **Total de archivos pruebas y docs:** 7 archivos
* **Capas tocadas:** 4 de código (Entrada, Aplicación, Dominio, Salida) + Pruebas y Docs.

---

### 3. Análisis de Absorción y Dificultad
* **¿Qué lo absorbió?:** El **Servicio de Dominio (`AplicacionPagoService`)** y el **Value Object (`Dinero`)**. La regla de cálculo financiero no se dispersó por controladores ni repositorios; vive exclusivamente dentro del núcleo puro de Java.
* **¿Dolió? ¿Por qué?:** **Dolió moderadamente en la capa de Pruebas.** Aunque el código de producción se modificó limpiamente, las pruebas unitarias existentes (`AplicacionPagoServiceTest`) tenían valores numéricos esperados fijados "a mano" (`Dinero.de(150000)`). Al introducir la regla del 10%, fue necesario recalcular manualmente los balances resultantes de los escenarios de prueba. La arquitectura evitó que el dolor llegara a la persistencia o a la seguridad JWT.

---

## Ficha 2 — Cambio 2 · Infraestructura

### 1. Descripción del Requerimiento
* **¿Qué pide el cliente?**
  Para salir a producción, la plaza cívica exige sustituir la base de datos de desarrollo H2 en memoria por un motor relacional empresarial **MySQL / PostgreSQL** alojado en la infraestructura corporativa, con credenciales inyectadas por variables de entorno y reemplazando `ddl-auto: update` por la ejecución de scripts estructurados.
* **En cascada (fases y documentos a rehacer):**
  * *Fases reabiertas:* Se reabre la fase de **Diseño** (Arquitectura Física y Modelo Físico de Datos relacional) y la fase de **Implementación** (capa de acceso a datos).
  * *Documentos a rehacer:* Documento de Arquitectura de Software (DAS), Diagrama Entidad-Relación físico, Plan de Pruebas de Rendimiento y Concurrencia.
  * *Impacto en pruebas:* Se debe reejecutar la fase completa de Pruebas de Integración y Pruebas No Funcionales (estrés, carga de conexiones y persistencia transaccional).
* **En RUP (iteración e hito afectado):**
  * *Iteración:* Se agenda dentro de la fase de **Transición** (o al cierre de la última iteración de **Construcción**), orientada al despliegue, endurecimiento del entorno (hardening) y migración de ambientes.
  * *Hito afectado:* No afecta la arquitectura lógica ni funcional. Incide en el hito PR (Product Release) asegurando la operatividad en el entorno objetivo.

---

### 2. Archivos tocados en el código por capa

| Capa | Archivos Reales del Repositorio | Detalle del cambio |
| :--- | :--- | :--- |
| **Entrada (REST)** | *Ninguno (0)* | La capa web es 100% agnóstica del mecanismo de almacenamiento. |
| **Aplicación** | *Ninguno (0)* | Los casos de uso interactúan únicamente con interfaces `*RepositoryPort`. |
| **Dominio** | *Ninguno (0)* | El dominio está escrito en Java puro sin ninguna anotación ni dependencia de JPA/SQL. |
| **Salida (Persistencia)** | *Ninguno (0)* (Los adaptadores existentes se conservan idénticos) | `NegocioRepositoryAdapter`, `DeudaRepositoryAdapter` y `PagoRepositoryAdapter` siguen usando Spring Data JPA sin modificaciones de código. |
| **Pruebas (`src/test`)** | `contract/DeudaRepositoryContractTest.java`<br>`contract/NegocioRepositoryContractTest.java`<br>`contract/PagoRepositoryContractTest.java` | Las pruebas de contrato escritas ejecutan exactamente la misma suite contra H2 y contra el adaptador MySQL/Postgres verificando el Principio de Sustitución de Liskov (LSP). |
| **Config. y Documentación** | `pom.xml`<br>`src/main/resources/application.yml`<br>`src/main/resources/application-prod.yml`<br>`README.md` | Se añade el driver en `pom.xml` (`mysql-connector-j` o `postgresql`), se configuran las credenciales en `application-prod.yml` y se documentan las variables en `README.md`. |

* **Total de archivos producción:** 3 archivos (únicamente configuración).
* **Total de archivos pruebas y docs:** 4 archivos (pruebas de contrato verificadoras y README).
* **Capas tocadas:** Solo la frontera de **Configuración/Infraestructura**. Ninguna capa de negocio.

---

### 3. Análisis de Absorción y Dificultad
* **¿Qué lo absorbió?:** Los **Puertos de Salida (`NegocioRepositoryPort`, `DeudaRepositoryPort`, `PagoRepositoryPort`)** y el desacoplamiento de la arquitectura hexagonal mediante el Principio de Inversión de Dependencias (DIP).
* **¿Dolió? ¿Por qué?:** **No dolió nada.** Este es el ejemplo paradigmático donde la Arquitectura Hexagonal amortigua el 100% del impacto. Dado que ni una sola entidad del dominio conoce JPA ni SQL, y que los puertos son abstracciones independientes del motor, cambiar la base de datos se limitó a ajustar cadenas de conexión y drivers en el borde del sistema.

---

## Ficha 3 — Cambio 3 · Alcance

### 1. Descripción del Requerimiento
* **¿Qué pide el cliente?**
  La administración requiere incorporar un nuevo atributo de **Categoría Comercial** obligatorio para cada local (`GASTRONOMIA`, `ARTESANIAS`, `COMERCIO_GENERAL`, `SERVICIOS`) con el fin de zonificar la plaza, y además registrar el usuario/auditor administrativo que autoriza el alta o la baja lógica de un negocio.
* **En cascada (fases y documentos a rehacer):**
  * *Fases reabiertas:* Requiere reabrir en cascada **Requisitos** (HU-01 y HU-08), **Diseño** (contratos OpenAPI, modelo relacional, DTOs y diagramas de clases) e **Implementación**.
  * *Documentos a rehacer:* SRS (especificación de nuevos campos y validaciones de formato), Diccionario de Datos, Diagramas de Arquitectura Lógica, Especificaciones de Pruebas de Sistema.
  * *Impacto en pruebas:* Se debe reabrir la fase de pruebas para revalidar la persistencia de los nuevos campos y la retrocompatibilidad de las consultas existentes.
* **En RUP (iteración e hito afectado):**
  * *Iteración:* Se planifica como una nueva iteración incremental dentro de la fase de **Construcción** (Iteración 2).
  * *Hito afectado:* No altera la arquitectura base (LCA). Es una extensión natural del modelo dentro de los mecanismos arquitectónicos establecidos.

---

### 2. Archivos tocados en el código por capa

| Capa | Archivos Reales del Repositorio | Detalle del cambio |
| :--- | :--- | :--- |
| **Entrada (REST)** | `infrastructure/adapters/in/rest/NegocioController.java`<br>`infrastructure/adapters/in/rest/dto/request/RegistrarNegocioRequest.java`<br>`infrastructure/adapters/in/rest/dto/response/NegocioResponse.java` | Se recibe el campo `categoria` y el identificador de auditor en los JSON del endpoint `/api/negocios`. |
| **Aplicación** | `application/port/in/command/RegistrarNegocioCommand.java`<br>`application/port/in/command/EditarNegocioCommand.java`<br>`application/port/in/RegistrarNegocioUseCase.java`<br>`application/service/NegocioService.java` | Se adaptan los comandos y el caso de uso para transportar el nuevo Value Object de categoría y los datos de auditoría. |
| **Dominio** | `domain/vo/CategoriaNegocio.java` *(nuevo)*<br>`domain/model/Negocio.java` | Creación del Value Object `CategoriaNegocio` con validación de categorías permitidas. Modificación de la entidad `Negocio` y sus métodos de fábrica (`registrar()` y `reconstruir()`). |
| **Salida (Persistencia)** | `infrastructure/adapters/out/persistence/entity/NegocioEntity.java`<br>`infrastructure/adapters/out/persistence/mapper/NegocioMapper.java` | Se añade la columna relacional `categoria` y se actualiza el mapeador bidireccional JPA ↔ Dominio. |
| **Pruebas (`src/test`)** | `domain/NegocioTest.java`<br>`application/NegocioServiceTest.java`<br>`infrastructure/adapters/in/rest/NegocioControllerTest.java`<br>`testsupport/Datos.java` | Se actualizan los builders de prueba en `Datos.java` y las pruebas unitarias que instanciaban negocios sin categoría. |
| **Config. y Documentación** | `README.md`<br>`requests.http` | Se documenta el nuevo atributo en la tabla de endpoints y se actualizan los ejemplos HTTP. |

* **Total de archivos producción:** 10 archivos (incluyendo la nueva clase `CategoriaNegocio.java`).
* **Total de archivos pruebas y docs:** 6 archivos.
* **Capas tocadas:** Todas las capas se tocan de manera vertical, pero de forma aislada y controlada.

---

### 3. Análisis de Absorción y Dificultad
* **¿Qué lo absorbió?:** El diseño basado en **Casos de Uso Segregados (Principio ISP)** y el patrón **Factory Method** en la entidad `Negocio` (`Negocio.registrar(...)`). La inclusión del nuevo Value Object enriqueció el modelo sin alterar los módulos de deuda ni pagos.
* **¿Dolió? ¿Por qué?:** **Dolió en los constructores y objetos de prueba.** Al cambiar la firma del método de fábrica de `Negocio`, múltiples tests que creaban instancias de `Negocio` directamente en `Datos.java` y `NegocioTest.java` arrojaron errores de compilación de inmediato. Esto demostró la importancia de contar con el patrón Object Mother / Test Data Builder para centralizar la instanciación de pruebas y evitar propagación de dolor cuando el modelo evoluciona.

---

## 4. Síntesis y Conclusiones del Taller 9

| Métrica / Observación | Cambio 1 (Regla Negocio) | Cambio 2 (Infraestructura) | Cambio 3 (Alcance) |
| :--- | :---: | :---: | :---: |
| **Archivos de Producción tocados:** | 9 | 3 (configuración) | 10 |
| **Archivos de Prueba/Docs tocados:** | 7 | 4 | 6 |
| **Capas de Código afectadas:** | 4 | 0 (solo frontera) | 5 |
| **¿En qué modelo fue más barato?:** | RUP / Hexagonal | RUP / Hexagonal | RUP / Hexagonal |
| **¿Por qué en Cascada habría sido prohibitivo?:** | Por la reapertura de la fase documental de Requisitos y revalidación total. | Por requerir rediseño físico formal y pruebas de regresión masivas. | Por romper la especificación formal del sistema obligando a rehacer contratos y firmas de aceptación. |
| **Principio o Mecanismo Clave:** | Núcleo de Dominio encapsulado (DDD). | Inversión de Dependencias (DIP) y Puertos. | Segregación de Interfaces (ISP) y Value Objects. |
