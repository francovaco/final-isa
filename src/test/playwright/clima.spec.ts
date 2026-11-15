import { expect, test } from '@playwright/test';

import { SESION, fechaClub } from './soporte/api';

// Sin service worker, para que page.route intercepte el pedido a Open-Meteo
test.use({ storageState: SESION.socio, serviceWorkers: 'block' });

/** El formulario de reserva muestra el pronóstico. La API externa se simula: el test no depende de internet. */
test.describe('Clima', () => {
  test('muestra el pronóstico del día elegido', async ({ page }) => {
    const manana = fechaClub(1);
    await page.route('https://api.open-meteo.com/**', ruta =>
      ruta.fulfill({
        json: {
          daily: {
            time: [manana],
            weather_code: [0],
            temperature_2m_max: [31.4],
            temperature_2m_min: [17.6],
            precipitation_probability_max: [5],
          },
        },
      }),
    );

    await page.goto('/reserva/new');
    await page.getByTestId('inicio').fill(`${manana}T10:00`);

    await expect(page.getByTestId('clima')).toContainText('Despejado');
    await expect(page.getByTestId('clima')).toContainText('18° / 31°');
    await expect(page.getByTestId('clima')).toContainText('5% de probabilidad de lluvia');
  });

  test('si la API del clima falla, el formulario sigue funcionando', async ({ page }) => {
    await page.route('https://api.open-meteo.com/**', ruta => ruta.fulfill({ status: 503 }));

    await page.goto('/reserva/new');

    await expect(page.getByTestId('clima')).toContainText('Pronóstico no disponible');
    await expect(page.getByTestId('cancha')).toBeEnabled();
  });
});
