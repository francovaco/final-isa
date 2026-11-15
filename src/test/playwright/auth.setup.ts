import { expect, test as setup } from '@playwright/test';

import { SESION, USUARIOS } from './soporte/api';

/**
 * Inicia sesión por la interfaz como admin y como socio y guarda la sesión (storageState).
 * Se marca "Recordarme" para que el token quede en localStorage, que es lo que guarda Playwright.
 */
for (const quien of ['admin', 'socio'] as const) {
  setup(`iniciar sesión como ${quien}`, async ({ page }) => {
    const { usuario, clave } = USUARIOS[quien];
    await page.goto('/login');
    await page.getByTestId('username').fill(usuario);
    await page.getByTestId('password').fill(clave);
    await page.locator('#rememberMe').check();
    await page.getByTestId('submit').click();

    await expect(page.locator('#home-logged-message')).toContainText(`"${usuario}"`);
    await page.context().storageState({ path: SESION[quien] });
  });
}
