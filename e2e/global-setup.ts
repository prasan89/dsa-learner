import { FullConfig } from '@playwright/test';
import { execSync } from 'child_process';

async function globalSetup(_config: FullConfig) {
  // Clear login rate-limit keys so the test suite can log in without hitting the 5/15min limit
  try {
    execSync('docker compose exec -T redis redis-cli DEL "login_attempts:172.19.0.1" "login_attempts:127.0.0.1" "login_attempts:::1" 2>/dev/null', {
      cwd: process.cwd() + '/..',
      stdio: 'ignore',
    });
  } catch {
    // non-fatal — test will proceed; if rate-limited, first few tests fail
  }
}

export default globalSetup;
