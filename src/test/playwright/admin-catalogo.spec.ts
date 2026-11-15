import { expect, test } from '@playwright/test';

import { ApiAdmin, SESION, unico } from './soporte/api';

test.use({ storageState: SESION.admin });

/** El admin da de alta un deporte y una cancha desde la interfaz. */
test.describe('Catálogo (admin)', () => {
  const sufijo = unico();
  const deporte = `Tenis E2E ${sufijo}`;
  const cancha = `Cancha E2E ${sufijo}`;

  test.afterAll(async ({ request }) => {
    const api = await ApiAdmin.crear(request);
    await api.borrarCancha(await api.buscarIdPorNombre('canchas', cancha), await api.buscarIdPorNombre('deportes', deporte));
  });

  test('crea un deporte y una cancha y los ve en los listados', async ({ page }) => {
    // Deporte
    await page.goto('/deporte');
    await page.getByTestId('entityCreateButton').click();
    await page.getByTestId('nombre').fill(deporte);
    await page.getByTestId('jugadoresPorEquipo').fill('2');
    await page.getByTestId('entityCreateSaveButton').click();
    await expect(page).toHaveURL(/\/deporte$/);
    await expect(page.getByTestId('entityTable').filter({ hasText: deporte })).toBeVisible();

    // Cancha de ese deporte
    await page.goto('/cancha');
    await page.getByTestId('entityCreateButton').click();
    await page.getByTestId('nombre').fill(cancha);
    await page.getByTestId('superficie').selectOption('POLVO_DE_LADRILLO');
    await page.getByTestId('techada').check();
    await page.getByTestId('precioPorHora').fill('20000');
    await page.getByTestId('activa').check();
    await page.getByTestId('deporte').selectOption({ label: deporte });
    await page.getByTestId('entityCreateSaveButton').click();

    await expect(page).toHaveURL(/\/cancha$/);
    const fila = page.getByTestId('entityTable').filter({ hasText: cancha });
    await expect(fila).toBeVisible();
    await expect(fila).toContainText(deporte);
  });
});
