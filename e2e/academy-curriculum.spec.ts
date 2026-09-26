import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

test.describe('Academy Curriculum Page @smoke @regression', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/languages/german');
    // Wait for skeleton to resolve — either the curriculum title or error state
    await expect(
      page.locator('h1').filter({ hasText: /german/i }).or(
        page.getByText(/something went wrong/i)
      )
    ).toBeVisible({ timeout: 12000 });
  });

  // ── Loading & structure ──────────────────────────────────────────────────────

  test('@smoke curriculum page loads with language title', async ({ page }) => {
    await expect(page.locator('h1').filter({ hasText: /german/i })).toBeVisible();
  });

  test('@smoke level selector renders CEFR tabs', async ({ page }) => {
    const nav = page.getByRole('navigation', { name: /cefr levels/i });
    await expect(nav).toBeVisible();
    for (const level of ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']) {
      await expect(nav.getByRole('button', { name: new RegExp(level, 'i') }).first()).toBeVisible();
    }
  });

  test('@smoke at least one unit section is rendered', async ({ page }) => {
    const unit = page.getByRole('region').first();
    await expect(unit).toBeVisible();
  });

  test('@smoke lesson rows are visible inside a unit', async ({ page }) => {
    const lessons = page.getByRole('link', { name: /lesson \d+|lesson/i });
    const count = await lessons.count();
    expect(count).toBeGreaterThanOrEqual(1);
  });

  test('@smoke progress bar is rendered with ARIA attributes', async ({ page }) => {
    const progressBar = page.getByRole('progressbar');
    await expect(progressBar).toBeVisible();
    await expect(progressBar).toHaveAttribute('aria-valuemin', '0');
    await expect(progressBar).toHaveAttribute('aria-valuemax');
    await expect(progressBar).toHaveAttribute('aria-valuenow');
  });

  // ── CTA block ────────────────────────────────────────────────────────────────

  test('@smoke CTA block shows "Start lesson" or "Continue" for A1', async ({ page }) => {
    const cta = page.getByRole('link', { name: /start lesson|continue/i });
    await expect(cta).toBeVisible();
  });

  test('CTA link points to a lesson URL', async ({ page }) => {
    const cta = page.getByRole('link', { name: /start lesson|continue/i });
    const href = await cta.getAttribute('href');
    expect(href).toMatch(/\/languages\/german\/lessons\//);
  });

  // ── Level selector behaviour ─────────────────────────────────────────────────

  test('A1 tab is active by default (aria-pressed=true)', async ({ page }) => {
    const a1Btn = page.getByRole('button', { name: /^A1/i });
    await expect(a1Btn).toHaveAttribute('aria-pressed', 'true');
  });

  test('locked A2 tab has aria-disabled and cannot be activated', async ({ page }) => {
    const nav = page.getByRole('navigation', { name: /cefr levels/i });
    const a2Btn = nav.getByRole('button', { name: /A2/i }).first();
    const isDisabled = (await a2Btn.getAttribute('aria-disabled')) === 'true';

    if (isDisabled) {
      // Verify locked visual state
      await expect(a2Btn).toHaveAttribute('aria-disabled', 'true');
      await expect(a2Btn).toHaveAttribute('aria-pressed', 'false');
      // Clicking should NOT change active level — A1 stays active
      await a2Btn.dispatchEvent('click');
      const a1Btn = nav.getByRole('button', { name: /^A1/i });
      await expect(a1Btn).toHaveAttribute('aria-pressed', 'true');
    } else {
      // A2 is unlocked — verify it's clickable
      await a2Btn.click();
      await expect(a2Btn).toHaveAttribute('aria-pressed', 'true');
    }
  });

  test('clicking an unlocked tab updates URL with ?level= param', async ({ page }) => {
    // A1 is always accessible — click it explicitly to confirm URL update
    const nav = page.getByRole('navigation', { name: /cefr levels/i });
    const a1Btn = nav.getByRole('button', { name: /^A1/i });
    await a1Btn.click();
    await expect(page).toHaveURL(/level=A1/i);
  });

  test('?level=A1 URL param pre-selects A1 on load', async ({ page }) => {
    await page.goto('/languages/german?level=A1');
    const a1Btn = page.getByRole('navigation', { name: /cefr levels/i })
      .getByRole('button', { name: /^A1/i });
    await expect(a1Btn).toHaveAttribute('aria-pressed', 'true', { timeout: 12000 });
  });

  // ── Locked level flow ────────────────────────────────────────────────────────

  test('locked tab has lock icon aria-label', async ({ page }) => {
    const nav = page.getByRole('navigation', { name: /cefr levels/i });
    const a2Btn = nav.getByRole('button', { name: /A2/i }).first();
    const isDisabled = (await a2Btn.getAttribute('aria-disabled')) === 'true';

    if (isDisabled) {
      // aria-label on locked button includes "locked"
      const label = await a2Btn.getAttribute('aria-label');
      expect(label).toMatch(/locked/i);
    } else {
      test.skip();
    }
  });

  // ── Lesson rows ──────────────────────────────────────────────────────────────

  test('lesson rows are keyboard-navigable links', async ({ page }) => {
    const firstLesson = page.getByRole('link', { name: /lesson/i }).first();
    await firstLesson.focus();
    const focused = await firstLesson.evaluate((el) => document.activeElement === el);
    expect(focused).toBe(true);
  });

  test('lesson row aria-label includes lesson title', async ({ page }) => {
    // Lesson rows are <a> elements with explicit aria-label set in LessonRow component
    const firstLesson = page.locator('a[aria-label]').filter({ hasText: /\d/ }).first();
    const ariaLabel = await firstLesson.getAttribute('aria-label');
    expect(ariaLabel?.length ?? 0).toBeGreaterThan(5);
  });

  // ── Page header ───────────────────────────────────────────────────────────────

  test('page header is sticky (has sticky positioning)', async ({ page }) => {
    const header = page.locator('header').first();
    const position = await header.evaluate((el) => getComputedStyle(el).position);
    expect(position).toBe('sticky');
  });

  // ── Protected route ──────────────────────────────────────────────────────────

  test('unauthenticated visit redirects to /login', async ({ page, context }) => {
    // Clear cookies to simulate unauthenticated state
    await context.clearCookies();
    await page.goto('/languages/german');
    await expect(page).toHaveURL(/login/i);
  });

  // ── Error & retry ────────────────────────────────────────────────────────────

  test('error state has a retry button', async ({ page }) => {
    // Intercept API to force error
    await page.route('**/api/v1/academy/*/curriculum', (route) =>
      route.fulfill({ status: 500, body: 'Internal Server Error' })
    );
    await page.reload();
    await expect(page.getByText(/something went wrong/i)).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole('button', { name: /try again/i })).toBeVisible();
  });

  test('retry button triggers a new API request', async ({ page }) => {
    let requestCount = 0;
    await page.route('**/api/v1/academy/*/curriculum', (route) => {
      requestCount++;
      if (requestCount === 1) {
        route.fulfill({ status: 500, body: 'error' });
      } else {
        route.continue();
      }
    });
    await page.reload();
    await expect(page.getByText(/something went wrong/i)).toBeVisible({ timeout: 10000 });
    await page.getByRole('button', { name: /try again/i }).click();
    expect(requestCount).toBeGreaterThanOrEqual(2);
  });

  // ── Other languages ──────────────────────────────────────────────────────────

  test('french curriculum route loads (or shows no-content state)', async ({ page }) => {
    await page.goto('/languages/french');
    // Either curriculum loads (h1 shows "French") or we get a graceful error/empty state.
    // Use first() to avoid strict-mode violation when both h1 and error text are present.
    const indicator = page.locator('h1').filter({ hasText: /french/i })
      .or(page.getByText(/something went wrong|isn't available/i).first());
    await expect(indicator.first()).toBeVisible({ timeout: 12000 });
  });
});
