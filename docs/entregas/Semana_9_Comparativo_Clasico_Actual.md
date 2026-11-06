# Comparativo de Arquitectura en Metodologías Clásicas vs. Desarrollo Actual
### Análisis Crítico sobre el Servicio de Dominio SIGEPAC
**Asignatura:** Ingeniería del Software III (Código 167420)  
**Institución:** Universidad de Pamplona — Facultad de Ingenierías y Arquitectura  
**Docente:** Ing. Carlos Arturo Barrientos Suárez  
**Equipo:** Grupo 4  
**Proyecto:** SIGEPAC · Domain Service (Administración Plaza Cívica)  
**Fecha:** Noviembre de 2026  

---

## 1. Introducción y Contexto

El desarrollo de software empresarial se enfrenta permanentemente a una tensión fundamental: **el momento en el cual se toman las decisiones de diseño y el costo que implica modificarlas**. Durante décadas, los modelos de proceso han intentado dar respuesta a este dilema. Como señalaba Winston Royce (1970) en su crítica al flujo secuencial puro, diferir las pruebas y la integración hasta las últimas fases de un proyecto genera un riesgo desmedido, pues es allí donde emergen las fallas estructurales de diseño que resultan catastróficamente costosas de reparar.

En contraposición, el Proceso Unificado de Rational (RUP) estructuró el ciclo de vida en torno a la mitigación temprana de riesgos mediante una arquitectura ejecutable (Kruchten, 1995, 2004). En el presente informe, el **Grupo 4** analiza el servicio backend **`domain-service-sigepac`**, un componente diseñado con **Arquitectura Hexagonal (Puertos y Adaptadores)** en Java 17 y Spring Boot 3 para la gestión de locales comerciales, obligaciones financieras y recaudos de una plaza cívica.

A través de este documento se presenta:
1. El mapeo formal de las actividades del semestre a las cuatro fases de RUP, evidenciado con artefactos reales del repositorio.
2. El análisis de impacto de tres cambios de alcance representativos trazados sobre Cascada, RUP y la Arquitectura Hexagonal.
3. Una comparación crítica, objetiva y autocrítica sobre cómo trabajó realmente el equipo frente a la teoría metodológica.
4. Las conclusiones arquitectónicas sobre cómo los principios SOLID y el aislamiento hexagonal reducen el costo del cambio.

---

## 2. Mapeo del Proyecto a las Fases de RUP

Aunque el proyecto no se gestionó contractualmente bajo el rigor burocrático de RUP, sus cuatro fases fundamentales describen con notable precisión la evolución real que experimentó el sistema a lo largo del periodo académico:

```
+----------------------------------------------------------------------------------------------------+
|                                    CICLO DE VIDA RUP EN SIGEPAC                                    |
+--------------------+-------------------------+-------------------------------+---------------------+
|   FASE DE INICIO   |   FASE DE ELABORACIÓN   |     FASE DE CONSTRUCCIÓN      | FASE DE TRANSICIÓN  |
| (Definición/Alcance| (Arquitectura Base/Risk)| (Implementación HU-8 a HU-12) | (Pruebas, Despliegue|
+--------------------+-------------------------+-------------------------------+---------------------+
| Hito: LCO          | Hito: LCA               | Hito: IOC                     | Hito: PR            |
| (Alcance y Metas)  | (Arquitectura Probada)  | (Capacidad Operativa Inicial) | (Producto Entregado)|
+--------------------+-------------------------+-------------------------------+---------------------+
```

### 2.1. Fase de Inicio (Inception)
* **Objetivo de la fase:** Establecer el alcance del negocio, evaluar la viabilidad técnica y definir los límites del sistema.
* **Actividades realizadas en SIGEPAC:**
  - Levantamiento del problema de la plaza cívica: la necesidad de administrar locales comerciales, cobrar cánones de arrendamiento y expensas comunes de administración, y liquidar pagos de inquilinos.
  - Formulación del Backlog inicial en hoja de cálculo Excel con las Historias de Usuario (HU) priorizadas de la HU-01 a la HU-12, con estimaciones de dimensión (10-100) y prioridad (1-10).
  - Creación del tablero en Trello (**SIGEPAC · XP**) con las listas de flujo.
* **Hito de salida:** *Lifecycle Objectives (LCO)* alcanzado mediante el documento de Anteproyecto y el Product Backlog consensuado.

### 2.2. Fase de Elaboración (Elaboración)
* **Objetivo de la fase:** Mitigar los principales riesgos técnicos y consolidar una **línea base de arquitectura ejecutable**.
* **Actividades realizadas en SIGEPAC:**
  - Modelado de las **Vistas Arquitectónicas 4+1 de Kruchten** (Semana 2).
  - Establecimiento de la integración con el servicio central de autenticación **AuthCore**, acordando la validación stateless de tokens JWT mediante firma compartida HMAC-SHA384 (`HS384`).
  - Creación del **Núcleo de Dominio Hexagonal en Java puro** (Semanas 5 a 7), libre de frameworks, definiendo Value Objects inmutables (`Dinero`, `CodigoLocal`, `Periodo`, `Documento`, `Propietario`), Entidades ricas (`Negocio`, `Deuda`, `Pago`) y Servicios de Dominio (`AplicacionPagoService`, `EstadoCuentaService`).
  - Implementación y ejecución de **70 pruebas unitarias en verde** mediante JUnit 5 ejecutadas con `javac`, demostrando la viabilidad de las reglas antes de conectar la infraestructura.
* **Hito de salida:** *Lifecycle Architecture (LCA)* alcanzado con la compilación exitosa del núcleo de dominio y la verificación del desacoplamiento.

### 2.3. Fase de Construcción (Construction)
* **Objetivo de la fase:** Desarrollar de manera iterativa los componentes restantes y lograr la capacidad operativa del producto.
* **Actividades realizadas en SIGEPAC:**
  - Implementación de los adaptadores de entrada REST (`NegocioController`, `DeudaController`, `PagoController`).
  - Implementación de los adaptadores de salida para persistencia relacional (`NegocioRepositoryAdapter`, `DeudaRepositoryAdapter`, `PagoRepositoryAdapter`) y sus respectivos mapeadores bidireccionales (`mapper/`) desacoplados de JPA.
  - Incorporación del filtro de seguridad `JwtAuthenticationFilter` y el validador criptográfico `JwtTokenValidator`.
  - Configuración del contenedor de inyección en la frontera del sistema (`BeanConfig.java`), respetando el Principio de Inversión de Dependencias (DIP).
* **Hito de salida:** *Initial Operational Capability (IOC)* evidenciado por el cumplimiento de los contratos REST descritos en [`requests.http`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/requests.http).

### 2.4. Fase de Transición (Transition)
* **Objetivo de la fase:** Llevar el sistema a su entorno productivo, verificar la calidad y capacitar a los usuarios.
* **Actividades realizadas en SIGEPAC:**
  - Redacción del manual técnico en el [`README.md`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/README.md) detallando variables de entorno (`JWT_SECRET_BASE64`, `DB_URL`), perfiles (`application.yml`, `application-prod.yml`) y cobertura.
  - Verificación de la suite de pruebas automatizadas mediante `mvn verify`.
  - Entrega de los escenarios de prueba en `requests.http` para la demostración en vivo.
* **Hito de salida:** *Product Release (PR)* alcanzado con la entrega del entregable formal y el repositorio versionado.

---

## 3. Trazado Comparativo de Tres Cambios de Alcance

A partir de los resultados obtenidos en el **Taller 9**, se contrasta el comportamiento del sistema ante tres escenarios de cambio realistas bajo tres ópticas: el modelo en **Cascada**, el modelo **RUP** y la **Arquitectura Hexagonal implementada**.

```
                RESUMEN DE IMPACTO: ARCHIVOS TOCADOS POR CAPA
+--------------------+---------+-------------+---------+--------+---------+--------+
| Tipo de Cambio     | Entrada | Aplicación  | Dominio | Salida | Pruebas | Config |
+--------------------+---------+-------------+---------+--------+---------+--------+
| 1. Regla Negocio   |    2    |      3      |    4    |   2    |    5    |   2    |
| 2. Infraestructura |    0    |      0      |    0    |   0    |    3    |   4    |
| 3. Alcance/Atributo|    3    |      4      |    3    |   2    |    4    |   2    |
+--------------------+---------+-------------+---------+--------+---------+--------+
```

### 3.1. Cambio 1 (Regla de Negocio): Política de Pronto Pago y Descuento
* **Enfoque Cascada:** En Cascada, una modificación en la fórmula de amortización de pagos exige declarar un "cambio de requisitos". Al estar cerradas las fases de Análisis y Diseño, se debe reactivar el comité de control de cambios, rehacer el Documento de Especificación de Requisitos (SRS) para HU-10, actualizar los diagramas de clases formales y volver a certificar la batería completa de pruebas funcionales y de sistema. El costo no está en las líneas de código, sino en la **fricción burocrática y el retrabajo documental**.
* **Enfoque RUP:** RUP gestiona el cambio como una solicitud de cambio (CR) asignada a la iteración en curso de la fase de Construcción. No pone en riesgo el hito de arquitectura (LCA) porque la estructura modular ya prevé la existencia de servicios de liquidación.
* **En el Código Hexagonal:** El cambio afectó directamente a [`AplicacionPagoService.java`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/src/main/java/com/sigepac/administracion/domain/service/AplicacionPagoService.java) y [`Dinero.java`](file:///c:/Users/bcami/Desktop/domain-service-sigepac/src/main/java/com/sigepac/administracion/domain/vo/Dinero.java). La lógica no se fugó hacia los adaptadores web ni de base de datos.
* **Dificultad observada:** Requirió actualizar 5 clases de pruebas unitarias (`*Test.java`) debido a que los saldos esperados estaban codificados con valores fijos.

### 3.2. Cambio 2 (Infraestructura): Migración de H2 en Memoria a MySQL/PostgreSQL
* **Enfoque Cascada:** En una cascada clásica, sustituir el motor de almacenamiento obliga a reabrir la fase de Diseño Físico de Base de Datos y la fase de Implementación. Si el código estuviese acoplado mediante sentencias SQL directas o procedimientos almacenados, el costo de migración implicaría reescribir consultas y repetir pruebas de estrés.
* **Enfoque RUP:** Se categoriza como una actividad no funcional planificada para la fase de Transición, validando la portabilidad ambiental del sistema.
* **En el Código Hexagonal:** Fue el cambio más económico de todos: **cero archivos de código Java modificados**. El cambio quedó encapsulado estrictamente en archivos de configuración (`pom.xml`, `application-prod.yml`). Gracias a los puertos `*RepositoryPort`, el dominio ni siquiera es consciente de que existe una base de datos relacional.

### 3.3. Cambio 3 (Alcance): Categorización Obligatoria de Negocios y Auditoría
* **Enfoque Cascada:** Introduce una ruptura vertical. Al cambiar el modelo de datos de la entidad central `Negocio`, se deben reabrir las fases de Requisitos, Análisis, Diseño e Implementación, invalidando los contratos de interfaz previamente pactados con los clientes del servicio.
* **Enfoque RUP:** Se absorbe como un incremento funcional en una iteración posterior de Construcción, afectando únicamente al caso de uso correspondiente sin desestabilizar los módulos de pagos o deudas.
* **En el Código Hexagonal:** Se modificaron 10 archivos de producción de manera vertical (desde el DTO REST hasta la entidad JPA), pero el principio de Segregación de Interfaces (ISP) evitó que el cambio contaminara los casos de uso no relacionados (`RegistrarPagoUseCase`, `ActualizarEstadoDeudaUseCase`).

---

## 4. Comparación Crítica con la Realidad del Grupo 4

Al confrontar la teoría de los modelos de proceso con la experiencia real del equipo durante el semestre, se evidencian diferencias sustanciales:

| Dimensión | En la Teoría (Cascada / RUP) | En la Práctica Real (Grupo 4) |
| :--- | :--- | :--- |
| **Secuencia de Fases** | Fases delimitadas por hitos formales con documentos firmados al cierre. | **Desarrollo solapado:** Se comenzó a codificar el dominio mientras aún se ajustaba el backlog en Excel y se refinaban los contratos de JWT con AuthCore. |
| **Gestión de Cambios** | Solicitudes formales de cambio analizadas por impacto antes de aprobarse. | **Cambios sobre la marcha:** Ajustes en los endpoints y nombres de atributos discutidos en reuniones informales y aplicados directamente en el repositorio. |
| **Pruebas y Verificación** | Pruebas de integración automatizadas ejecutadas en cada iteración de RUP. | **Deuda técnica deliberada:** Se construyeron 70 pruebas unitarias con `javac` para el dominio, pero las pruebas de integración con Spring Boot se postergaron por limitaciones de conectividad en el entorno inicial de desarrollo. |
| **Control de Configuración** | Líneas base formales y trazabilidad estricta de configuración. | Uso de ramas de Git y tableros en Trello, con sincronización basada en revisiones de pares antes de integrar a la rama principal. |

### Lecciones Aprendidas y Autocrítica:
1. **La ilusión de la Cascada:** Intentar fijar los requisitos de cobro de la plaza cívica en un documento estático inicial habría sido inviable; la comprensión de conceptos como "amortización de deudas antiguas primero" solo maduró cuando se programaron las pruebas de dominio de `AplicacionPagoServiceTest`.
2. **El acierto de RUP y la Arquitectura Ejecutable:** Haber blindado la arquitectura base (Elaboración) antes de construir la capa REST permitió que los adaptadores se desarrollaran sin titubeos, sabiendo que el núcleo ya resolvía las invariantes del negocio.

---

## 5. Conexión con la Arquitectura Hexagonal y Principios SOLID

El ejercicio demuestra que **la arquitectura del software es la variable amortiguadora que determina cuán ágil o cuán rígido puede ser un equipo**, independientemente de la metodología que declare en el papel:

1. **Principio de Responsabilidad Única (SRP):**  
   Cada caso de uso está materializado en una clase independiente (`PagoService`, `DeudaService`, `NegocioService`). Modificar la regla de liquidación de pagos no tuvo ningún efecto colateral sobre la consulta o registro de locales comerciales.
2. **Principio Abierto/Cerrado (OCP) e Inversión de Dependencias (DIP):**  
   El núcleo de dominio depende de abstracciones (`port/out/*RepositoryPort`) y no de implementaciones. Cambiar de base de datos H2 a MySQL o PostgreSQL representó una adición de configuración y no una modificación del código existente, cumpliendo a cabalidad con OCP.
3. **Principio de Sustitución de Liskov (LSP):**  
   Las pruebas de contrato (`contract/*RepositoryContractTest`) garantizan que cualquier adaptador de persistencia (sea el doble en memoria para tests ultrarrápidos o el adaptador JPA para producción) cumpla exactamente con el mismo comportamiento esperado por la aplicación.
4. **Principio de Segregación de Interfaces (ISP):**  
   En lugar de tener una interfaz monolítica de administración, los puertos de entrada (`application/port/in`) definen interfaces pequeñas y específicas (`RegistrarPagoUseCase`, `AnularPagoUseCase`, `ConsultarEstadoCuentaUseCase`). Ningún cliente se ve obligado a depender de métodos que no utiliza.

---

## 6. Conclusiones

1. **La metodología define cuándo descubrimos los problemas; la arquitectura define cuánto nos cuesta solucionarlos.** Trabajar con Cascada en un proyecto de servicios desacoplados habría inflado los costos por sobrecarga documental.
2. **La Arquitectura Hexagonal es el mejor aliado de los enfoques iterativos.** Al mantener el dominio en Java puro y ubicar los frameworks en la periferia, los cambios de infraestructura se reducen a tareas menores de configuración, permitiendo al equipo concentrarse en entregar valor real en cada iteración.
3. **La verdadera agilidad requiere disciplina técnica.** No basta con tener un tablero en Trello y reuniones periódicas; sin pruebas automatizadas, sin objetos de valor inmutables y sin puertos bien definidos, cada iteración acumularía una deuda técnica que terminaría paralizando la evolución del sistema.

---

### Referencias Bibliográficas
* Cunningham, W. (1992). *The WyCash Portfolio Management System*. OOPSLA '92 Experience Report.
* Fowler, M. (2009). *Technical Debt Quadrant*. MartinFowler.com.
* Kruchten, P. (1995). *The 4+1 View Model of Architecture*. IEEE Software, 12(6), 42-50.
* Kruchten, P. (2004). *The Rational Unified Process: An Introduction* (3rd ed.). Addison-Wesley.
* Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
* Royce, W. (1970). *Managing the Development of Large Software Systems*. Proceedings of IEEE WESCON, 26, 1-9.
* Sommerville, I. (2011). *Ingeniería del Software* (9ª ed.). Pearson Educación.
