import { expect, test } from '@playwright/test';

import { ApiAdmin, SESION, unico } from './soporte/api';

test.use({ storageState: SESION.socio, serviceWorkers: 'allow' });

/** La app es una PWA: manifest instalable, service worker y funcionamiento sin conexión. */
test.describe('PWA', () => {
  // Al menos una cancha para que el listado tenga filas (la base puede estar vacía, como en el CI)
  const cancha = `Cancha ${unico()}`;
  let canchaId: number | undefined;
  let deporteId: number | undefined;

  test.beforeAll(async ({ request }) => {
    const api = await ApiAdmin.crear(request);
    deporteId = await api.crearDeporte(`Deporte ${cancha}`);
    canchaId = await api.crearCancha(cancha, deporteId);
  });

  test.afterAll(async ({ request }) => {
    await (await ApiAdmin.crear(request)).borrarCancha(canchaId, deporteId);
  });

  test('publica un manifest instalable con íconos', async ({ request }) => {
    const manifest = await (await request.get('/manifest.webapp')).json();

    expect(manifest.display).toBe('standalone');
    expect(manifest.start_url).toBeTruthy();
    const tamanios = manifest.icons.map((icono: { sizes: string }) => icono.sizes);
    expect(tamanios).toEqual(expect.arrayContaining(['192x192', '512x512']));
    expect(manifest.icons.some((icono: { purpose?: string }) => icono.purpose === 'maskable')).toBe(true);
  });

  test('registra el service worker y muestra las canchas sin conexión', async ({ page, context }) => {
    test.setTimeout(120_000);

    // El service worker se registra cuando la app queda estable (máximo 30 s)
    await page.goto('/');
    await page.waitForFunction(async () => !!(await navigator.serviceWorker.getRegistration()), null, { timeout: 60_000 });
    await page.evaluate(() => navigator.serviceWorker.ready);
    await page.reload();
    await page.waitForFunction(() => !!navigator.serviceWorker.controller);

    // Con conexión: se cargan las canchas (y quedan en la caché del service worker)
    await page.goto('/cancha');
    await expect(page.getByTestId('entityTable').first()).toBeVisible();

    // Sin conexión: la app y los datos salen de la caché
    await context.setOffline(true);
    await page.reload();
    await expect(page.getByTestId('banner-offline')).toBeVisible();
    await expect(page.getByTestId('entityTable').first()).toBeVisible();
    await context.setOffline(false);
  });
});
