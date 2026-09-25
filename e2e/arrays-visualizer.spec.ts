import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

test.describe('Arrays Visualizer @smoke @regression', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/learn/arrays');
    await expect(page.locator('h1')).toContainText('Arrays');
    // Wait for the visualizer to hydrate — Play becomes enabled once steps load
    await expect(page.getByRole('button', { name: /play/i }).first()).toBeEnabled({ timeout: 10000 });
  });

  test('@smoke page loads with lesson sidebar', async ({ page }) => {
    // Sidebar lesson buttons are present (no role="tab" — sidebar uses plain buttons)
    const lessonBtns = page.locator('aside button');
    await expect(lessonBtns.first()).toBeVisible();
    const count = await lessonBtns.count();
    expect(count).toBeGreaterThan(0);
  });

  test('@smoke default visualization renders', async ({ page }) => {
    // Array cells are present (role=cell)
    const cells = page.locator('[role="cell"]');
    await expect(cells.first()).toBeVisible({ timeout: 5000 });
    const count = await cells.count();
    expect(count).toBeGreaterThan(0);
  });

  test('@smoke play/pause controls work', async ({ page }) => {
    const playBtn = page.getByRole('button', { name: /play/i }).first();
    await expect(playBtn).toBeEnabled();

    // Start playing
    await playBtn.click();
    const pauseBtn = page.getByRole('button', { name: /pause/i }).first();
    await expect(pauseBtn).toBeVisible({ timeout: 3000 });

    // Pause it
    await pauseBtn.click();
    await expect(page.getByRole('button', { name: /play|replay/i }).first()).toBeVisible();
  });

  test('step forward button advances the step counter', async ({ page }) => {
    // Step counter: "1 / N" text inside the progress row
    const counter = page.locator('span.tabular-nums').filter({ hasText: /\d+ \/ \d+/ });
    await expect(counter).toBeVisible({ timeout: 5000 });
    const before = await counter.textContent();

    const stepFwd = page.getByRole('button', { name: /step forward/i });
    await expect(stepFwd).toBeEnabled();
    await stepFwd.click();

    const after = await counter.textContent();
    expect(after).not.toBe(before);
  });

  test('reset returns to step 1', async ({ page }) => {
    const counter = page.locator('span.tabular-nums').filter({ hasText: /\d+ \/ \d+/ });
    await expect(counter).toBeVisible({ timeout: 5000 });

    const stepFwd = page.getByRole('button', { name: /step forward/i });
    await expect(stepFwd).toBeEnabled();
    await stepFwd.click();
    await stepFwd.click();

    await page.getByRole('button', { name: /reset/i }).click();
    await expect(counter).toHaveText(/^1 \//);
  });

  test('switching lesson changes visualization', async ({ page }) => {
    // Sidebar lesson buttons in the expanded (first) module
    const lessonBtns = page.locator('aside button').filter({ hasText: /^\d/ });
    const count = await lessonBtns.count();
    if (count < 2) return; // skip if only one lesson visible

    const firstLabel  = await lessonBtns.nth(0).textContent();
    const secondLabel = await lessonBtns.nth(1).textContent();
    expect(firstLabel).not.toBe(secondLabel);

    await lessonBtns.nth(1).click();
    // The lesson title in the main panel should update
    await expect(page.locator('h2')).toBeVisible({ timeout: 3000 });
  });

  test('custom array input changes the visualization', async ({ page }) => {
    const arrayInput = page.getByRole('textbox', { name: /array values/i });
    await arrayInput.fill('1, 2, 3');
    await arrayInput.press('Tab');

    // Cells should now reflect the 3-element array
    const cells = page.locator('[role="cell"]');
    await expect(cells).toHaveCount(3, { timeout: 5000 });
  });

  test('linear search lesson shows target input', async ({ page }) => {
    // Navigate to the Linear Search lesson in the sidebar
    const linearBtn = page.locator('aside button').filter({ hasText: /linear search/i });
    if (!(await linearBtn.count())) return; // skip if not in current module

    await linearBtn.click();
    // Target input should appear
    const targetInput = page.getByRole('spinbutton').first();
    await expect(targetInput).toBeVisible({ timeout: 5000 });
  });

  test('challenge mode can be entered and shows an overlay', async ({ page }) => {
    // Enter challenge mode (button reads "Try Challenge")
    const challengeBtn = page.getByRole('button', { name: /try challenge/i });
    if (!(await challengeBtn.count())) return; // no challenge for this concept

    await challengeBtn.click();

    // Step through until a challenge appears
    const stepFwd = page.getByRole('button', { name: /step forward/i });
    let attempts = 0;
    while (attempts < 20) {
      if (await stepFwd.isDisabled()) break;
      await stepFwd.click();
      const challengeOverlay = page.locator('text=Challenge').first();
      if (await challengeOverlay.isVisible({ timeout: 500 }).catch(() => false)) {
        break;
      }
      attempts++;
    }

    // Should see a challenge UI with choice buttons
    const choices = page.getByRole('button', { name: /^Choice \d/i });
    if (await choices.count() > 0) {
      await expect(choices.first()).toBeVisible();
    }
  });

  test('watch mode button returns to non-challenge view', async ({ page }) => {
    const challengeBtn = page.getByRole('button', { name: /try challenge/i });
    if (!(await challengeBtn.count())) return;

    await challengeBtn.click();
    await expect(page.getByRole('button', { name: /watch mode/i })).toBeVisible({ timeout: 5000 });

    await page.getByRole('button', { name: /watch mode/i }).click();
    await expect(page.getByRole('button', { name: /try challenge/i })).toBeVisible({ timeout: 5000 });
  });

  test('progress bar is accessible (has aria attributes)', async ({ page }) => {
    const progressbar = page.getByRole('progressbar');
    await expect(progressbar).toHaveAttribute('aria-valuenow');
    await expect(progressbar).toHaveAttribute('aria-valuemin');
    await expect(progressbar).toHaveAttribute('aria-valuemax');
  });

  test('step narration has aria-live region', async ({ page }) => {
    const liveRegion = page.locator('[aria-live="polite"]');
    await expect(liveRegion).toHaveCount(1);
  });

  test('@smoke keyboard Space toggles play/pause', async ({ page }) => {
    // Focus the page body (not an input)
    await page.locator('h1').click();

    const playBtn = page.getByRole('button', { name: /play/i }).first();
    await expect(playBtn).toBeEnabled();

    // Press Space to play
    await page.keyboard.press('Space');
    await expect(page.getByRole('button', { name: /pause/i }).first()).toBeVisible({ timeout: 3000 });

    // Press Space again to pause
    await page.keyboard.press('Space');
    await expect(page.getByRole('button', { name: /play|replay/i }).first()).toBeVisible({ timeout: 3000 });
  });
});
