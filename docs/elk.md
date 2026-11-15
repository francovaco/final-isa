# Logs con ELK

La app envía sus logs a **Logstash**, que los guarda en **Elasticsearch** (índice `logstash-clubreservas-YYYY.MM.dd`). En **Kibana** hay un dashboard y tres alertas. Todo se levanta y se configura solo con `./start.sh`.

- Kibana: http://localhost:5601 (`elastic` / `changeme`)
- Dashboard: http://localhost:5601/app/dashboards#/view/clubreservas-operacion

## Cómo llegan los logs

1. La app usa `logstash-logback-encoder`: cada log sale como un JSON por TCP a `logstash:5000` (desde el host, `localhost:5001`).
2. Logstash (`elk/logstash/pipeline/logstash.conf`) lo escribe en Elasticsearch con el usuario `logstash_internal`.
3. Una plantilla de índice (`elk/setup/plantilla-indice.json`) define los campos como `keyword` para poder filtrar y agrupar en Kibana.

Si ELK no está levantado, la app funciona igual (los logs se descartan).

## Eventos que registra la app

Cada evento tiene el campo `evento` y datos estructurados. El usuario logueado viaja en todos los logs de una petición (campo `usuario`, vía MDC).

| `evento`            | Nivel | Campos                                                      | Dónde se genera               |
| ------------------- | ----- | ----------------------------------------------------------- | ----------------------------- |
| `RESERVA_CREADA`    | INFO  | `reserva_id`, `cancha`, `duracion`                          | `ReservaService`              |
| `RESERVA_RECHAZADA` | WARN  | `motivo` (ej. `superposicion`), `cancha`                    | `ReservaService`              |
| `RESERVA_CANCELADA` | INFO  | `reserva_id`, `cancha`, `por` (socio/admin)                 | `ReservaService`              |
| `LOGIN_OK`          | INFO  | `usuario`                                                   | `AuthenticateController`      |
| `LOGIN_FALLIDO`     | WARN  | `usuario` (**nunca la contraseña**)                         | `AuthenticateController`      |
| `HTTP`              | INFO  | `metodo`, `ruta` (ids como `{id}`), `status`, `duracion_ms` | `RegistroPeticionesFilter`    |
| `LATIDO`            | INFO  | —                                                           | `LatidoService` (cada minuto) |

Además llegan los logs normales de la app (`level`, `logger_name`, `message`, `stack_trace`).

## Dashboard "ClubReservas - Operación"

12 paneles:

| Panel                                               | Muestra                                                  |
| --------------------------------------------------- | -------------------------------------------------------- |
| Logs · Errores · Reservas creadas · Logins fallidos | Métricas del período elegido                             |
| Logs por nivel                                      | Barras apiladas DEBUG/INFO/WARN/ERROR en el tiempo       |
| Reservas: creadas, rechazadas y canceladas          | Eventos de reservas en el tiempo                         |
| Reservas por cancha                                 | Dona con las reservas creadas por cancha                 |
| Motivos de rechazo                                  | Qué regla de negocio rechazó cada reserva                |
| Logins fallidos por usuario                         | Usuarios con más intentos fallidos                       |
| Endpoints más usados                                | Ruta, método, cantidad de peticiones y duración promedio |
| Alertas disparadas                                  | Documentos del índice `alertas-clubreservas`             |
| Últimos errores y advertencias                      | Logs ERROR/WARN con logger, mensaje y usuario            |

Está exportado en `elk/kibana/objetos/clubreservas.ndjson` y se importa solo al levantar.

## Alertas

Reglas de tipo _Elasticsearch query_ que se evalúan cada minuto (`elk/setup/reglas/`):

| Regla                                | Se dispara cuando                                          |
| ------------------------------------ | ---------------------------------------------------------- |
| **Muchos errores**                   | Más de 5 logs `ERROR` en 5 minutos                         |
| **Posible fuerza bruta en el login** | Más de 5 `LOGIN_FALLIDO` del **mismo usuario** en 1 minuto |
| **La app no envía logs**             | Ningún `LATIDO` en 3 minutos (la app está caída)           |

Cada alerta usa dos conectores (los dos gratuitos en la licencia _basic_):

- **Server log**: la escribe en el log de Kibana.
- **Index**: la guarda en el índice `alertas-clubreservas`, que se muestra en el dashboard.

La alerta de app caída usa el `LATIDO` porque una app sin tráfico no emite logs, y sin él daría falsos positivos.

### Cómo dispararlas

| Alerta         | Cómo                                                            |
| -------------- | --------------------------------------------------------------- |
| Fuerza bruta   | `./elk/generar-trafico.sh` (hace 7 logins fallidos seguidos)    |
| App caída      | `docker compose stop app` y esperar unos 3 minutos              |
| Muchos errores | Se dispara con errores reales de la app (más de 5 en 5 minutos) |

## Setup automático

| Contenedor     | Qué hace                                                                                                |
| -------------- | ------------------------------------------------------------------------------------------------------- |
| `elk-setup`    | Contraseña de `kibana_system`, rol `logstash_writer`, usuario `logstash_internal`, plantilla del índice |
| `kibana-setup` | Data views, importa el dashboard, crea los 2 conectores y las 3 reglas (los recrea con ids fijos)       |

Los dos son idempotentes: corren cada vez que se levanta el compose sin duplicar nada.
