import { test, expect } from '@playwright/test'; import { login } from './helpers/auth'; import { expectNoServerError } from './helpers/navigation';
test('@smoke landing page loads',async({page})=>{await page.goto('/');await expect(page.locator('body')).toBeVisible();await expectNoServerError(page);});
test('@smoke login page loads',async({page})=>{await page.goto('/login');await expect(page.locator('input[name="email"]')).toBeVisible();await expect(page.locator('input[name="password"]')).toBeVisible();});
test('@smoke login reaches dashboard',async({page})=>{await login(page);await expect(page).toHaveURL(/dashboard/i);await expectNoServerError(page);});
test('@smoke dashboard can navigate to learning',async({page})=>{await login(page);const link=page.getByRole('link',{name:/dsa|learning path|problems/i}).first();await expect(link).toBeVisible();await link.click();await expectNoServerError(page);});
