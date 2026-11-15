# PWA

La app se puede **instalar** (Chrome muestra el botón en la barra de direcciones) y **funciona sin conexión** con los últimos datos vistos.

## Qué la hace PWA

| Pieza                | Archivo                                       | Detalle                                                                             |
| -------------------- | --------------------------------------------- | ----------------------------------------------------------------------------------- |
| Service worker       | `src/main/webapp/app/app.config.ts`           | `ngsw-worker.js` de Angular, activo solo en producción (`registerWhenStable:30000`) |
| Estrategias de caché | `ngsw-config.json`                            | Qué se cachea y cómo (tabla de abajo)                                               |
| Manifest             | `src/main/webapp/manifest.webapp`             | Nombre, colores, `display: standalone`, íconos 192/512 (_any_ y _maskable_)         |
| Íconos               | `src/main/webapp/content/images/pwa/`         | Íconos propios + `apple-touch-icon` para iOS                                        |
| Estado de la PWA     | `src/main/webapp/app/core/pwa/pwa.service.ts` | Detecta conexión, versión nueva y borra la caché de datos al cerrar sesión          |
| Avisos               | `src/main/webapp/app/layouts/pwa-banner/`     | Banner "Sin conexión" y "Hay una nueva versión → Actualizar"                        |

El service worker solo funciona en `localhost` o con HTTPS, y no se registra en modo desarrollo (`npm start`): se prueba con `./start.sh`.

## Estrategias de caché

Siguiendo el ejemplo `pwa-angular` de la materia:

- **performance**: responde desde la caché si la tiene (rápido); va a la red solo si no hay copia o venció.
- **freshness**: va primero a la red; si tarda más que el `timeout` o no hay conexión, usa la caché.

| Grupo              | URLs                                                            | Estrategia  | Configuración       | Por qué                                                                        |
| ------------------ | --------------------------------------------------------------- | ----------- | ------------------- | ------------------------------------------------------------------------------ |
| `api-cuenta`       | `/api/account`, `/management/info`                              | freshness   | timeout 3 s, 1 día  | Sin la cuenta en caché, la app offline cree que no hay sesión y manda al login |
| `api-catalogo`     | `/api/deportes`, `/api/canchas`                                 | freshness   | timeout 3 s, 1 día  | Con conexión se ven los datos al día (con performance se veían listas viejas)  |
| `api-reservas`     | `/api/reservas`                                                 | freshness   | timeout 3 s, 1 hora | Tienen que estar al día, pero se pueden consultar offline                      |
| `clima-open-meteo` | `https://api.open-meteo.com/**`                                 | performance | 1 hora              | API externa: el pronóstico no cambia minuto a minuto                           |
| _(sin caché)_      | `/api/authenticate`, `/api/admin/**`, resto de `/management/**` | —           | —                   | Credenciales y administración                                                  |

Los archivos de la app (JS, CSS, `index.html`) se cachean al instalar (`prefetch`); imágenes y fuentes, a medida que se usan (`lazy`).

## Sin conexión

- La app y los datos ya vistos se cargan desde la caché.
- Aparece el banner amarillo **"Sin conexión"**.
- No se puede **guardar ni cancelar** reservas (los botones se deshabilitan): la app es de solo lectura offline.

## Seguridad de la caché

El service worker guarda las respuestas **por URL, no por usuario**. Para que otra persona en el mismo dispositivo no vea las reservas del usuario anterior, al **cerrar sesión** se borran todas las cachés de datos (`ngsw:*:data:*`). La app en sí (JS, CSS, íconos) queda cacheada.

## Cómo verificarlo

En Chrome, con la app levantada (`./start.sh`) en http://localhost:8080:

1. DevTools → **Application** → **Manifest**: sin errores; abajo, "Installability".
2. **Application** → **Service Workers**: `ngsw-worker.js` activo.
3. **Application** → **Cache Storage**: las cachés `ngsw:...:data:...` de cada grupo.
4. **Network** → **Offline** y recargar: los datos siguen visibles con el banner.

Esto también está automatizado en `src/test/playwright/pwa.spec.ts` (ver [tests.md](tests.md)).
