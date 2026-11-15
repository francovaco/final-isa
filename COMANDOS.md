# Comandos

Todos se ejecutan desde la carpeta del repo (`final-isa`). Requisito: **Docker Desktop abierto**.

## Levantar y apagar

| Para                                                        | Comando                                         |
| ----------------------------------------------------------- | ----------------------------------------------- |
| Levantar todo (app + PostgreSQL + ELK)                      | `./start.sh`                                    |
| Reconstruir la imagen de la app (después de cambiar código) | `./start.sh --build`                            |
| Apagar (los datos quedan guardados)                         | `docker compose stop`                           |
| Borrar todo, datos incluidos                                | `docker compose down -v`                        |
| Ver el estado de los servicios                              | `docker compose ps`                             |
| Ver los logs de la app                                      | `docker compose logs -f app`                    |
| Levantar solo app + base de datos (sin ELK)                 | `docker compose up -d --no-deps postgresql app` |

La primera vez, `./start.sh` crea el archivo `.env` con contraseñas al azar y construye la imagen de la app (unos minutos).

## Rutas

| Qué                       | URL                                                                            | Usuario                            |
| ------------------------- | ------------------------------------------------------------------------------ | ---------------------------------- |
| App (PWA)                 | http://localhost:8080                                                          | `admin` / `admin`, `user` / `user` |
| Nueva reserva (con clima) | http://localhost:8080/reserva/new                                              | `user` / `user`                    |
| Kibana                    | http://localhost:5601                                                          | `elastic` / `changeme`             |
| Dashboard de Kibana       | http://localhost:5601/app/dashboards#/view/clubreservas-operacion              |                                    |
| Reglas de alerta          | http://localhost:5601/app/management/insightsAndAlerting/triggersActions/rules |                                    |
| Discover (logs)           | http://localhost:5601/app/discover (data view "ClubReservas logs")             |                                    |
| Elasticsearch             | http://localhost:9200                                                          | `elastic` / `changeme`             |
| Health de la app          | http://localhost:8080/management/health                                        |                                    |

## ELK

| Para                                                                     | Comando                    |
| ------------------------------------------------------------------------ | -------------------------- |
| Generar tráfico (llena el dashboard y dispara la alerta de fuerza bruta) | `./elk/generar-trafico.sh` |
| Probar la alerta "La app no envía logs" (esperar ~3 minutos)             | `docker compose stop app`  |
| Volver a levantar la app                                                 | `docker compose start app` |

Las alertas disparadas se ven en el panel "Alertas disparadas" del dashboard.

## Tests E2E (Playwright)

Con la app levantada (`./start.sh`). La primera vez: `npm install` y `npx playwright install chromium`.

| Para                                           | Comando                          |
| ---------------------------------------------- | -------------------------------- |
| Correr los tests (sin ventana)                 | `npm run e2e`                    |
| Ver el navegador paso a paso                   | `npm run e2e:visual`             |
| Ver el navegador más lento                     | `SLOWMO=1500 npm run e2e:visual` |
| Modo interactivo (elegir tests, ver cada paso) | `npm run e2e:ui`                 |
| Abrir el reporte HTML de la última corrida     | `npm run e2e:reporte`            |
| Correr un solo archivo                         | `npx playwright test reservas`   |

## Tests de unidad e integración

Requieren JDK 21 (Java 25 no es compatible con JHipster 8.11) y Node 22.

| Para                                                       | Comando                                 |
| ---------------------------------------------------------- | --------------------------------------- |
| Backend: unidad + integración (usa Docker para PostgreSQL) | `./mvnw verify`                         |
| Backend: solo tests de unidad                              | `./mvnw test`                           |
| Backend: un test                                           | `./mvnw test -Dtest=ReservaServiceTest` |
| Frontend (Jest) + lint                                     | `npm test`                              |
| Reporte de cobertura del backend                           | `target/site/jacoco/index.html`         |

Si el Java por defecto no es el 21: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` (macOS).

## Desarrollo (sin Docker para la app)

| Para                                                     | Comando     |
| -------------------------------------------------------- | ----------- |
| Backend en perfil dev (H2, logs a ELK si está levantado) | `./mvnw`    |
| Frontend con recarga automática (http://localhost:9000)  | `npm start` |

En dev el service worker está desactivado: la PWA se prueba con `./start.sh`.

## PWA

- Instalar: en Chrome, ícono de instalar a la derecha de la barra de direcciones (http://localhost:8080).
- Probar offline: DevTools (F12) → **Network** → **Offline** y recargar.
- Ver el service worker y las cachés: DevTools → **Application** → Service Workers / Cache Storage.
- Borrar la caché: DevTools → **Application** → **Storage** → **Clear site data**.
