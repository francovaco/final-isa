# Comandos

Desde la carpeta del repo (`final-isa`), con **Docker Desktop abierto**.

## Levantar y apagar

| Para          | Comando               |
| ------------- | --------------------- |
| Levantar todo | `./start.sh`          |
| Apagar todo   | `docker compose stop` |

## Rutas

| Qué       | URL                   | Usuario                            |
| --------- | --------------------- | ---------------------------------- |
| App (PWA) | http://localhost:8080 | `admin` / `admin`, `user` / `user` |
| Kibana    | http://localhost:5601 | `elastic` / `changeme`             |

## Tests E2E (Playwright)

Con la app levantada:

| Para                         | Comando              |
| ---------------------------- | -------------------- |
| Correr los tests             | `npm run e2e`        |
| Ver el navegador paso a paso | `npm run e2e:visual` |
