import { expect, Page } from '@playwright/test';
export const E2E_EMAIL = process.env.E2E_EMAIL || 'e2e-user@example.com';
export const E2E_PASSWORD = process.env.E2E_PASSWORD || 'Test@1234';
export async function login(page: Page) {
  await page.goto('/login');
  await page.locator('input[name="email"]').fill(E2E_EMAIL);
  await page.locator('input[name="password"]').fill(E2E_PASSWORD);
  await page.getByRole('button', { name: /sign in/i }).click();
  await expect(page).toHaveURL(/dashboard/i);
}
export async function logout(page: Page) {
  const button = page.getByRole('button', { name: /logout|sign out/i }).first();
  if (await button.count()) {
    await button.click();
    await page.waitForURL(/login/i, { timeout: 10000 });
  } else {
    await page.goto('/login');
  }
}
