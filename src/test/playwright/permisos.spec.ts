import { expect, test } from '@playwright/test';

import { ApiAdmin, SESION, unico } from './soporte/api';

test.use({ storageState: SESION.socio });

/** Un socio no puede entrar a la administración ni al ABM del catálogo. */
test.describe('Permisos del socio', () => {
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

  test('no ve el menú de administración', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('accountMenu')).toBeVisible();
    await expect(page.getByTestId('adminMenu')).toHaveCount(0);
  });

  for (const ruta of ['/admin/user-management', '/deporte/new', '/cancha/new']) {
    test(`no puede entrar a ${ruta} escribiendo la URL`, async ({ page }) => {
      await page.goto(ruta);
      await expect(page).toHaveURL(/\/accessdenied/);
      await expect(page.locator('.alert-danger')).toHaveText('No tiene permisos para acceder a la página.');
    });
  }

  test('ve las canchas pero sin botones para crear, editar o borrar', async ({ page }) => {
    await page.goto('/cancha');
    await expect(page.getByTestId('entityTable').first()).toBeVisible();
    await expect(page.getByTestId('entityCreateButton')).toHaveCount(0);
    await expect(page.getByTestId('entityEditButton')).toHaveCount(0);
    await expect(page.getByTestId('entityDeleteButton')).toHaveCount(0);
  });
});
