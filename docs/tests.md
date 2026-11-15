# Tests

| Tipo                  | Herramienta                                    | Total | Propios | Dónde                              |
| --------------------- | ---------------------------------------------- | ----- | ------- | ---------------------------------- |
| Unidad (backend)      | JUnit 5, Mockito, AssertJ, ArchUnit            | 88    | 32      | `src/test/java/**/*Test.java`      |
| Integración (backend) | Spring Boot Test + Testcontainers (PostgreSQL) | 199   | 8       | `src/test/java/**/*IT.java`        |
| Unidad (frontend)     | Jest                                           | 433   | 44      | `src/main/webapp/app/**/*.spec.ts` |
| End-to-end            | Playwright (Chromium)                          | 19    | 19      | `src/test/playwright/`             |

Los tests "propios" son los escritos para este trabajo; el resto los genera JHipster (ABM de entidades, usuarios, seguridad, arquitectura) y se mantienen porque avisan si algo de lo generado se rompe. Todos corren en el CI en cada push (ver [ci-cd.md](ci-cd.md)). Los comandos de E2E están en [COMANDOS.md](../COMANDOS.md). Para los de unidad e integración (requieren JDK 21 y Node 22):

| Para                          | Comando         |
| ----------------------------- | --------------- |
| Backend: unidad + integración | `./mvnw verify` |
| Frontend: Jest + lint         | `npm test`      |

## Tests de unidad (backend)

**`ReservaServiceTest`** (28 tests): las reglas de negocio, sin Spring ni base de datos. Los repositorios se simulan con Mockito y el **reloj está fijo** (`Clock` inyectable), así los resultados no dependen de la fecha real.

- Crear: reserva válida (asigna usuario, estado y fecha de creación), cancha inactiva, duración inválida, pasado, más de 14 días, antes de la apertura, después del cierre, terminar justo a las 23:00, horario que no empieza en punto o y media, cuarta reserva activa, superposición parcial, reserva contigua (no se superpone).
- Superposición: mismo horario, reserva contenida en otra, reservas contiguas.
- Cancelar: socio a tiempo, socio con menos de 2 horas, admin siempre, reserva ajena, reserva ya cancelada.
- Ver: el socio ve la suya y no la ajena; el admin ve todas.

**`RegistroPeticionesFilterTest`** (4 tests): normalización de rutas para Kibana (`/api/reservas/15` → `/api/reservas/{id}`), qué peticiones se registran y el usuario en el MDC.

## Tests de integración (backend)

**`ReservaReglasIT`** (8 tests): las reglas y permisos a través de la API REST, con base de datos real y Spring Security: crear como socio, superposición devuelve 400 con el motivo, el socio solo ve las suyas, el admin ve todas, cancelar la propia, no cancelar la ajena, el socio no puede editar ni borrar, no puede crear deportes.

Los `*ResourceIT` generados por JHipster se adaptaron a las reglas nuevas (corren como admin y `createReserva` usa un horario válido).

## Tests de unidad (frontend)

| Archivo                            | Qué prueba                                                                                   |
| ---------------------------------- | -------------------------------------------------------------------------------------------- |
| `reserva-reglas.spec.ts`           | Validadores de duración y fecha futura, y cuándo se puede cancelar                           |
| `clima.service.spec.ts`            | Pedido a Open-Meteo (sin enviar el token), fecha fuera de rango, API caída, códigos de clima |
| `reserva.component.spec.ts`        | Confirmación y cancelación desde el listado, botón según rol y anticipación                  |
| `reserva-update.component.spec.ts` | Solo canchas activas, el socio no carga usuarios, pronóstico del día                         |
| `pwa.service.spec.ts`              | Estado de conexión, versión nueva y limpieza de la caché de datos                            |
| `login.service.spec.ts`            | Al cerrar sesión se borra la caché de datos                                                  |

## Tests E2E (Playwright)

Corren contra la app real (imagen Docker en perfil prod + PostgreSQL), en la zona horaria del club.

| Archivo                  | Qué prueba                                                                                            |
| ------------------------ | ----------------------------------------------------------------------------------------------------- |
| `auth.setup.ts`          | Inicia sesión como admin y como socio y guarda la sesión para los demás tests                         |
| `auth.spec.ts`           | Login correcto, login con contraseña incorrecta, logout                                               |
| `admin-catalogo.spec.ts` | El admin crea un deporte y una cancha desde la interfaz                                               |
| `reservas.spec.ts`       | El socio reserva, choca con la superposición (ve el motivo), no puede reservar en el pasado y cancela |
| `permisos.spec.ts`       | El socio no ve la administración ni entra al ABM escribiendo la URL                                   |
| `pwa.spec.ts`            | Manifest instalable, service worker activo y canchas visibles sin conexión                            |
| `clima.spec.ts`          | Pronóstico con Open-Meteo **simulado** (`page.route`) y el formulario sigue si la API falla           |

- Cada test **crea y borra sus datos** por la API: corren igual con la base vacía del CI o con datos.
- Usan los atributos `data-cy` que genera JHipster (`testIdAttribute: 'data-cy'`).
- `workers: 1`: comparten usuarios y la regla de máximo 3 reservas activas.
- En el CI, si un test falla se reintenta y queda el _trace_, captura y video en el reporte (artifact `playwright-reporte`).

### Bugs que encontraron los tests

- **E2E del catálogo:** después de crear una cancha, la lista seguía mostrando la versión vieja (la caché del catálogo era _performance_). Se pasó a _freshness_.
- **Revisión de la consola al endurecer la CSP:** Angular cargaba los estilos con un `onload` inline que la CSP bloqueaba (la app quedaba sin estilos). Se desactivó `inlineCritical`.
