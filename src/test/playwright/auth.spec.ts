import { expect, test } from '@playwright/test';

/** Login y logout, sin sesión guardada. */
test.describe('Autenticación', () => {
  test('el socio inicia sesión con sus credenciales', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('username').fill('user');
    await page.getByTestId('password').fill('user');
    await page.getByTestId('submit').click();

    await expect(page.locator('#home-logged-message')).toHaveText('Está conectado como "user".');
  });

  test('con una contraseña incorrecta muestra un error y no entra', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('username').fill('user');
    await page.getByTestId('password').fill('clave-incorrecta');
    await page.getByTestId('submit').click();

    await expect(page.getByTestId('loginError')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('cerrar sesión vuelve al estado sin usuario', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('username').fill('user');
    await page.getByTestId('password').fill('user');
    await page.getByTestId('submit').click();
    await expect(page.locator('#home-logged-message')).toBeVisible();

    await page.getByTestId('accountMenu').click();
    await page.getByTestId('logout').click();

    await expect(page.locator('#home-logged-message')).toHaveCount(0);
    await expect(page.getByTestId('login')).toBeAttached();
  });
});
