import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

test.describe('Learn Landing Page @smoke @regression', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/learn');
    await expect(page.locator('h1')).toContainText('Learn', { timeout: 8000 });
  });

  test('@smoke page loads with correct heading', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Learn');
    await expect(page.getByText(/Master Data Structures/i)).toBeVisible();
  });

  test('@smoke learning path bar is visible', async ({ page }) => {
    await expect(page.getByText('Your Learning Path')).toBeVisible();
    // Scope to the learning-path card (rounded-2xl container that has the "Your Learning Path" heading)
    const pathCard = page.locator('.rounded-2xl').filter({ has: page.getByText('Your Learning Path') });
    await expect(pathCard.getByText('Foundations')).toBeVisible();
    // Use nth(1) to get the stage label "Algorithms", not the type-filter button
    await expect(pathCard.getByText('Algorithms')).toBeVisible();
  });

  test('@smoke concept cards render for all categories', async ({ page }) => {
    const cards = page.getByRole('listitem');
    const count = await cards.count();
    expect(count).toBeGreaterThanOrEqual(8);
  });

  test('@smoke arrays card is clickable and links to /learn/arrays', async ({ page }) => {
    const arraysLink = page.getByRole('link', { name: /arrays/i }).first();
    await expect(arraysLink).toBeVisible();
    await arraysLink.click();
    await expect(page).toHaveURL(/\/learn\/arrays/);
  });

  test('type filter — Data Structures shows only data structure cards', async ({ page }) => {
    await page.getByRole('button', { name: /Data Structures/i }).click();
    // Arrays card should still be visible
    await expect(page.getByText('Arrays')).toBeVisible();
    // DP (algorithms) should not be visible
    await expect(page.getByText('Dynamic Programming')).not.toBeVisible();
  });

  test('type filter — Algorithms shows algorithm cards', async ({ page }) => {
    // Button text is "Algorithms (7)" — filter to the one starting with "Algorithms"
    await page.locator('button').filter({ hasText: /^Algorithms/ }).click();
    // Check card headings (h3), not description text which may contain "arrays" as a word
    await expect(page.locator('h3').filter({ hasText: 'Sorting Algorithms' })).toBeVisible();
    await expect(page.locator('h3').filter({ hasText: /^Arrays$/ })).not.toBeVisible();
  });

  test('search filters cards by name', async ({ page }) => {
    const searchInput = page.getByPlaceholder(/search concepts/i);
    await searchInput.fill('hash');
    await expect(page.getByText('Hash Maps')).toBeVisible();
    await expect(page.getByText('Arrays')).not.toBeVisible();
  });

  test('search no results shows empty state', async ({ page }) => {
    const searchInput = page.getByPlaceholder(/search concepts/i);
    await searchInput.fill('xyzabc123notaconcept');
    await expect(page.getByText(/No topics found/i)).toBeVisible();
  });

  test('grid/list view toggle works', async ({ page }) => {
    const listBtn = page.getByRole('button', { name: /list view/i });
    await listBtn.click();
    await expect(listBtn).toHaveAttribute('aria-pressed', 'true');

    const gridBtn = page.getByRole('button', { name: /grid view/i });
    await gridBtn.click();
    await expect(gridBtn).toHaveAttribute('aria-pressed', 'true');
  });

  test('feature pills are visible', async ({ page }) => {
    await expect(page.getByText('Interactive Visualizations')).toBeVisible();
    await expect(page.getByText('AI Mentor Support')).toBeVisible();
  });

  test('arrays breadcrumb on /learn/arrays links back to /learn', async ({ page }) => {
    await page.goto('/learn/arrays');
    // Breadcrumb is a small text link — scope to the breadcrumb nav row (text-xs text-gray-400)
    const breadcrumb = page.locator('.text-gray-400').filter({ has: page.getByRole('link', { name: 'Learn' }) }).first();
    const learnLink = breadcrumb.getByRole('link', { name: 'Learn' });
    await expect(learnLink).toBeVisible();
    await learnLink.click();
    await expect(page).toHaveURL(/\/learn$/);
  });
});
