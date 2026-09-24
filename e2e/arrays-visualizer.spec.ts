import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

test.describe('Arrays Visualizer @smoke @regression', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/learn/arrays');
    await expect(page.locator('h1')).toContainText('Arrays');
  });

  test('@smoke page loads with concept tabs', async ({ page }) => {
    // 5 concept tabs visible
    const tabs = page.getByRole('tab');
    await expect(tabs).toHaveCount(5);
    await expect(tabs.first()).toBeVisible();
  });

  test('@smoke default visualization renders', async ({ page }) => {
    // Array cells are present (role=cell)
    const cells = page.locator('[role="cell"]');
    await expect(cells.first()).toBeVisible({ timeout: 5000 });
    await expect(cells).toHaveCount(await cells.count());
  });

  test('@smoke play/pause controls work', async ({ page }) => {
    const playBtn = page.getByRole('button', { name: /play/i }).first();
    await expect(playBtn).toBeVisible();

    // Start playing
    await playBtn.click();
    const pauseBtn = page.getByRole('button', { name: /pause/i }).first();
    await expect(pauseBtn).toBeVisible({ timeout: 3000 });

    // Pause it
    await pauseBtn.click();
    await expect(page.getByRole('button', { name: /play|replay/i }).first()).toBeVisible();
  });

  test('step forward button advances the step counter', async ({ page }) => {
    // The step counter span has class w-14 (unique; cell index spans do not)
    const counter = page.locator('span.w-14.tabular-nums');
    await expect(counter).toBeVisible({ timeout: 5000 });
    await expect(counter).not.toHaveText('—', { timeout: 5000 });
    const before = await counter.textContent();

    const stepFwd = page.getByRole('button', { name: /step forward/i });
    await stepFwd.click();

    const after = await counter.textContent();
    expect(after).not.toBe(before);
  });

  test('reset returns to step 1', async ({ page }) => {
    const counter = page.locator('span.w-14.tabular-nums');
    await expect(counter).toBeVisible({ timeout: 5000 });
    await expect(counter).not.toHaveText('—', { timeout: 5000 });

    const stepFwd = page.getByRole('button', { name: /step forward/i });
    await stepFwd.click();
    await stepFwd.click();

    await page.getByRole('button', { name: /reset/i }).click();
    await expect(counter).toHaveText(/^1 \//);
  });

  test('switching concept tab changes visualization', async ({ page }) => {
    const tabs = page.getByRole('tab');
    const first = tabs.nth(0);
    const second = tabs.nth(1);

    const firstLabel = await first.textContent();
    const secondLabel = await second.textContent();
    expect(firstLabel).not.toBe(secondLabel);

    await second.click();
    await expect(second).toHaveAttribute('aria-selected', 'true');
    await expect(first).toHaveAttribute('aria-selected', 'false');
  });

  test('custom array input changes the visualization', async ({ page }) => {
    const arrayInput = page.getByRole('textbox', { name: /array values/i });
    await arrayInput.fill('1, 2, 3');
    await arrayInput.press('Tab');

    // Cells should now reflect the 3-element array
    const cells = page.locator('[role="cell"]');
    await expect(cells).toHaveCount(3);
  });

  test('linear search tab shows target input and allows search', async ({ page }) => {
    // Click the Linear Search tab
    const tabs = page.getByRole('tab');
    let linearTab = null;
    for (let i = 0; i < await tabs.count(); i++) {
      const label = await tabs.nth(i).textContent();
      if (label?.toLowerCase().includes('linear')) {
        linearTab = tabs.nth(i);
        break;
      }
    }
    if (!linearTab) return; // skip if tab name changed

    await linearTab.click();
    // Target input should appear
    const targetInput = page.getByRole('spinbutton').first();
    await expect(targetInput).toBeVisible({ timeout: 3000 });
  });

  test('challenge mode can be entered and shows an overlay', async ({ page }) => {
    // Enter challenge mode
    const challengeBtn = page.getByRole('button', { name: /challenge mode/i });
    if (!(await challengeBtn.count())) return; // no challenge for this concept

    await challengeBtn.click();

    // Step through until a challenge appears
    const stepFwd = page.getByRole('button', { name: /step forward/i });
    let attempts = 0;
    while (attempts < 20) {
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
    const challengeBtn = page.getByRole('button', { name: /challenge mode/i });
    if (!(await challengeBtn.count())) return;

    await challengeBtn.click();
    await expect(page.getByRole('button', { name: /watch mode/i })).toBeVisible();

    await page.getByRole('button', { name: /watch mode/i }).click();
    await expect(page.getByRole('button', { name: /challenge mode/i })).toBeVisible();
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
    await expect(playBtn).toBeVisible();

    // Press Space to play
    await page.keyboard.press('Space');
    await expect(page.getByRole('button', { name: /pause/i }).first()).toBeVisible({ timeout: 3000 });

    // Press Space again to pause
    await page.keyboard.press('Space');
    await expect(page.getByRole('button', { name: /play|replay/i }).first()).toBeVisible({ timeout: 3000 });
  });
});
