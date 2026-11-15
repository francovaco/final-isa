import { expect, Page, test } from '@playwright/test';

import { ApiAdmin, SESION, fechaClub, unico } from './soporte/api';

test.use({ storageState: SESION.socio });

/**
 * Flujo completo del socio: reservar, chocar con una regla de negocio y cancelar.
 * Cada corrida usa una cancha nueva (creada por API) para no depender de otros datos.
 */
test.describe.serial('Reservas (socio)', () => {
  const cancha = `Cancha Reservas E2E ${unico()}`;
  const manana = fechaClub(1);
  let canchaId: number | undefined;
  let deporteId: number | undefined;

  test.beforeAll(async ({ request }) => {
    const api = await ApiAdmin.crear(request);
    deporteId = await api.crearDeporte(`Deporte ${cancha}`);
    canchaId = await api.crearCancha(cancha, deporteId);
    await api.cancelarReservasActivasDelSocio();
  });

  test.afterAll(async ({ request }) => {
    await (await ApiAdmin.crear(request)).borrarCancha(canchaId, deporteId);
  });

  async function completarReserva(page: Page, hora: string, duracion: string): Promise<void> {
    // Se entra desde el listado, como un usuario (al guardar, la app vuelve a la página anterior)
    await page.goto('/reserva');
    await page.getByTestId('entityCreateButton').click();
    await page.getByTestId('inicio').fill(`${manana}T${hora}`);
    await page.getByTestId('duracionMinutos').selectOption({ label: duracion });
    await page.getByTestId('cancha').selectOption({ label: cancha });
  }

  test('reserva una cancha libre y la ve en "Mis reservas"', async ({ page }) => {
    await completarReserva(page, '10:00', '90 minutos');
    await page.getByTestId('entityCreateSaveButton').click();

    await expect(page).toHaveURL(/\/reserva$/);
    await expect(page.getByTestId('ReservaHeading')).toContainText('Mis reservas');
    const fila = page.getByTestId('entityTable').filter({ hasText: cancha });
    await expect(fila).toContainText('CONFIRMADA');
    await expect(fila).toContainText('90');
  });

  test('no permite reservar un horario superpuesto y muestra el motivo', async ({ page }) => {
    await completarReserva(page, '10:30', '60 minutos');
    await page.getByTestId('entityCreateSaveButton').click();

    await expect(page.locator('.alert-danger')).toContainText('La cancha ya está reservada en ese horario');
    await expect(page).toHaveURL(/\/reserva\/new/);
  });

  test('no deja guardar una reserva en el pasado', async ({ page }) => {
    await page.goto('/reserva/new');
    await page.getByTestId('inicio').fill(`${fechaClub(-1)}T10:00`);
    await page.getByTestId('inicio').blur();

    await expect(page.getByTestId('error-fecha-futura')).toBeVisible();
    await expect(page.getByTestId('entityCreateSaveButton')).toBeDisabled();
  });

  test('cancela su reserva', async ({ page }) => {
    await page.goto('/reserva');
    const fila = page.getByTestId('entityTable').filter({ hasText: cancha });
    page.once('dialog', dialogo => dialogo.accept());
    await fila.getByTestId('entityCancelarButton').click();

    await expect(fila).toContainText('CANCELADA');
    await expect(fila.getByTestId('entityCancelarButton')).toHaveCount(0);
  });
});
