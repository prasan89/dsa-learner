import { expect, Page } from '@playwright/test';
export async function openFirstMatchingLink(page:Page,patterns:RegExp[]){for(const pattern of patterns){const link=page.getByRole('link',{name:pattern}).first();if(await link.count()){await link.click();return;}}throw new Error('No navigation link matched: '+patterns.map(String).join(', '));}
export async function expectNoServerError(page:Page){await expect(page.locator('body')).not.toContainText(/internal server error|application error|whitelabel error/i);}
