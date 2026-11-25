/**
 * Service worker de la PWA, generado con Workbox (modo generateSW) después de `ng build`.
 *
 *   npm run webapp:sw      (lo ejecuta solo `npm run webapp:prod`, que es lo que corre el build de Maven)
 *
 * Genera target/classes/static/sw.js:
 *  - precache: la app compilada (JS, CSS, index.html, íconos, fuentes) con su hash de versión
 *  - runtimeCaching: las respuestas de la API y del clima, con la estrategia de cada una
 */
const { generateSW } = require('workbox-build');

const DIA = 24 * 60 * 60;

const config = {
  globDirectory: 'target/classes/static/',
  globPatterns: ['**/*.{js,css,html,ico,png,svg,webapp,woff,woff2,ttf,eot}'],
  // Swagger UI y archivos de soporte no hacen falta offline
  globIgnores: ['swagger-ui/**', '**/*.map', 'sw.js', 'robots.txt'],
  swDest: 'target/classes/static/sw.js',
  // Los archivos de Angular ya llevan el hash en el nombre: no hace falta otro parámetro de versión
  dontCacheBustURLsMatching: /\.[0-9a-f]{16,}\./,
  maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,

  // SPA: cualquier navegación (ej. /reserva) se responde con index.html, también sin conexión
  navigateFallback: '/index.html',
  navigateFallbackDenylist: [/^\/api\//, /^\/management\//, /^\/v3\//, /^\/swagger-ui\//, /^\/h2-console/],

  // La versión nueva espera a que el usuario toque "Actualizar" (workbox-window envía SKIP_WAITING)
  skipWaiting: false,
  clientsClaim: true,
  cleanupOutdatedCaches: true,
  // Un solo archivo sw.js, sin importScripts de otros orígenes
  inlineWorkboxRuntime: true,
  sourcemap: false,
  mode: 'production',

  runtimeCaching: [
    {
      // Sin la cuenta en caché, la app offline cree que no hay sesión y manda al login
      urlPattern: ({ url, sameOrigin }) => sameOrigin && (url.pathname === '/api/account' || url.pathname === '/management/info'),
      handler: 'NetworkFirst',
      options: {
        cacheName: 'api-cuenta',
        networkTimeoutSeconds: 3,
        expiration: { maxEntries: 5, maxAgeSeconds: DIA },
        cacheableResponse: { statuses: [200] },
      },
    },
    {
      // Catálogo: con conexión siempre lo último; sin conexión, la última copia
      urlPattern: ({ url, sameOrigin }) => sameOrigin && /^\/api\/(deportes|canchas)(\/|$)/.test(url.pathname),
      handler: 'NetworkFirst',
      options: {
        cacheName: 'api-catalogo',
        networkTimeoutSeconds: 3,
        expiration: { maxEntries: 50, maxAgeSeconds: DIA },
        cacheableResponse: { statuses: [200] },
      },
    },
    {
      urlPattern: ({ url, sameOrigin }) => sameOrigin && /^\/api\/reservas(\/|$)/.test(url.pathname),
      handler: 'NetworkFirst',
      options: {
        cacheName: 'api-reservas',
        networkTimeoutSeconds: 3,
        expiration: { maxEntries: 50, maxAgeSeconds: 60 * 60 },
        cacheableResponse: { statuses: [200] },
      },
    },
    {
      // API externa del clima: el pronóstico no cambia minuto a minuto
      urlPattern: ({ url }) => url.origin === 'https://api.open-meteo.com',
      handler: 'CacheFirst',
      options: {
        cacheName: 'clima-open-meteo',
        expiration: { maxEntries: 20, maxAgeSeconds: 60 * 60 },
        cacheableResponse: { statuses: [200] },
      },
    },
  ],
};

generateSW(config)
  .then(({ count, size, warnings }) => {
    warnings.forEach(w => console.warn('[workbox]', w));
    console.log(`[workbox] sw.js generado: ${count} archivos en el precache (${(size / 1024).toFixed(0)} KB)`);
  })
  .catch(error => {
    console.error('[workbox] Error generando el service worker:', error);
    process.exit(1);
  });
