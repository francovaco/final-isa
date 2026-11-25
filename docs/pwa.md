# PWA

La app se puede **instalar** (Chrome muestra el botón en la barra de direcciones) y **funciona sin conexión** con los últimos datos vistos. El service worker se genera con **[Workbox](https://developer.chrome.com/docs/workbox) 7.4**.

## Qué la hace PWA

| Pieza                | Archivo                                       | Detalle                                                                                                  |
| -------------------- | --------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| Service worker       | `workbox.config.cjs`                          | `workbox-build` (modo `generateSW`) genera `sw.js` después de `ng build` (`npm run webapp:sw`)           |
| Registro y versiones | `src/main/webapp/app/core/pwa/pwa.service.ts` | `workbox-window`: registra `sw.js`, avisa de versiones nuevas y borra la caché de datos al cerrar sesión |
| Manifest             | `src/main/webapp/manifest.webapp`             | Nombre, colores, `display: standalone`, íconos 192/512 (_any_ y _maskable_)                              |
| Íconos               | `src/main/webapp/content/images/pwa/`         | Íconos propios + `apple-touch-icon` para iOS                                                             |
| Avisos               | `src/main/webapp/app/layouts/pwa-banner/`     | Banner "Sin conexión" y "Hay una nueva versión → Actualizar"                                             |
| Sin caché HTTP       | `ServiceWorkerWebConfiguration.java`          | `sw.js` se sirve con `Cache-Control: no-cache` para que las versiones nuevas lleguen enseguida           |

El build de producción (`npm run webapp:prod`, el que ejecuta Maven y el Dockerfile) corre `ng build` y después genera `sw.js`. En modo desarrollo (`npm start`) el service worker no se registra: se prueba con `./start.sh`. Solo funciona en `localhost` o con HTTPS.

## Precache

Al instalarse, el service worker guarda la app compilada (JS, CSS, `index.html`, íconos, fuentes: unos 70 archivos) con la revisión de cada uno. Si un deploy cambia algún archivo, Workbox detecta la versión nueva.

Las navegaciones (por ejemplo, abrir `/reserva` sin conexión) se responden con `index.html` del precache (`navigateFallback`), salvo `/api`, `/management` y Swagger.

## Estrategias de caché (runtimeCaching)

Siguiendo el ejemplo `pwa-angular` de la materia:

- **NetworkFirst**: va primero a la red; si tarda más que el timeout o no hay conexión, usa la caché.
- **CacheFirst**: responde desde la caché si la tiene (rápido); va a la red solo si no hay copia o venció.

| Caché              | URLs                                                            | Estrategia   | Configuración       | Por qué                                                                        |
| ------------------ | --------------------------------------------------------------- | ------------ | ------------------- | ------------------------------------------------------------------------------ |
| `api-cuenta`       | `/api/account`, `/management/info`                              | NetworkFirst | timeout 3 s, 1 día  | Sin la cuenta en caché, la app offline cree que no hay sesión y manda al login |
| `api-catalogo`     | `/api/deportes`, `/api/canchas`                                 | NetworkFirst | timeout 3 s, 1 día  | Con conexión se ven los datos al día                                           |
| `api-reservas`     | `/api/reservas`                                                 | NetworkFirst | timeout 3 s, 1 hora | Tienen que estar al día, pero se pueden consultar offline                      |
| `clima-open-meteo` | `https://api.open-meteo.com/**`                                 | CacheFirst   | 1 hora              | API externa: el pronóstico no cambia minuto a minuto                           |
| _(sin caché)_      | `/api/authenticate`, `/api/admin/**`, resto de `/management/**` | —            | —                   | Credenciales y administración                                                  |

Solo se guardan respuestas 200, con un máximo de entradas por caché.

## Versiones nuevas

`sw.js` usa `skipWaiting: false`: la versión nueva queda esperando para no cambiar la app mientras alguien la usa. `workbox-window` emite el evento `waiting` y la app muestra **"Hay una nueva versión → Actualizar"**. Al tocarlo se envía `SKIP_WAITING`, la versión nueva toma el control y la página se recarga.

## Sin conexión

- La app y los datos ya vistos se cargan desde la caché.
- Aparece el banner amarillo **"Sin conexión"**.
- No se puede **guardar ni cancelar** reservas (los botones se deshabilitan): la app es de solo lectura offline.

## Seguridad de la caché

El service worker guarda las respuestas **por URL, no por usuario**. Para que otra persona en el mismo dispositivo no vea las reservas del usuario anterior, al **cerrar sesión** se borran las 4 cachés de datos. La app en sí (precache) queda guardada.

## Equivalencia con el service worker de Angular

La app usaba al principio el service worker de Angular (`@angular/service-worker`, el que trae JHipster y el ejemplo `pwa-angular`). Se reemplazó por Workbox con las mismas estrategias:

| Angular (`ngsw-config.json`)        | Workbox (`workbox.config.cjs`)      |
| ----------------------------------- | ----------------------------------- |
| `strategy: "freshness"`             | `NetworkFirst`                      |
| `strategy: "performance"`           | `CacheFirst`                        |
| `assetGroups` (`prefetch` / `lazy`) | Precache (`globPatterns`)           |
| `navigationUrls`                    | `navigateFallback`                  |
| `SwUpdate` (`VERSION_READY`)        | `workbox-window` (evento `waiting`) |

## Cómo verificarlo

En Chrome, con la app levantada (`./start.sh`) en http://localhost:8080:

1. DevTools → **Application** → **Manifest**: sin errores; abajo, "Installability".
2. **Application** → **Service Workers**: `sw.js` activo.
3. **Application** → **Cache Storage**: `workbox-precache-...` y las cachés `api-cuenta`, `api-catalogo`, `api-reservas`, `clima-open-meteo`.
4. **Network** → **Offline** y recargar: los datos siguen visibles con el banner.

Esto también está automatizado en `src/test/playwright/pwa.spec.ts` (ver [tests.md](tests.md)).
