import { defineConfig, devices } from '@playwright/test';

/**
 * Tests E2E de ClubReservas con Playwright.
 *
 * Corren contra la app ya levantada (por defecto http://localhost:8080, perfil prod):
 *   ./start.sh            o   docker compose up -d --no-deps postgresql app
 *   npm run e2e           (headless)    |   npm run e2e:ui   (modo interactivo)
 *
 * Otra URL:  E2E_BASE_URL=http://otra:8080 npm run e2e
 */
export default defineConfig({
  testDir: './src/test/playwright',
  outputDir: './target/playwright/resultados',
  // Los tests comparten usuarios (admin/user) y reglas como "máximo 3 reservas activas": uno a la vez
  workers: 1,
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  timeout: 60_000,
  reporter: [['list'], ['html', { outputFolder: 'target/playwright/reporte', open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:8080',
    // JHipster marca los elementos con data-cy: getByTestId('username') busca [data-cy="username"]
    testIdAttribute: 'data-cy',
    // Hora y formato del club: las reglas de horario se evalúan en hora de Mendoza
    locale: 'es-AR',
    timezoneId: 'America/Argentina/Mendoza',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    // Inicia sesión una vez como admin y como socio y guarda la sesión para los demás tests
    { name: 'setup', testMatch: /auth\.setup\.ts/ },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],
});
