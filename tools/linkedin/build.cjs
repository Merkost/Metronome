const path = require('path');
const puppeteer = require(process.env.PUPPETEER ||
  '/Users/merkost/.claude/skills/impeccable/node_modules/puppeteer');

const SRC = `file://${path.join(__dirname, 'banner.html')}`;
const OUT = path.join(__dirname, '..', '..', 'artifacts', 'linkedin');
const W = 1584, H = 396;

(async () => {
  const browser = await puppeteer.launch({
    headless: 'new',
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    args: ['--allow-file-access-from-files', '--font-render-hinting=none'],
  });

  for (const theme of ['light', 'dark']) {
    const page = await browser.newPage();
    await page.setViewport({ width: W, height: H, deviceScaleFactor: 2 });
    await page.goto(SRC, { waitUntil: 'networkidle0' });
    await page.evaluate(t => document.documentElement.dataset.theme = t, theme);
    await page.evaluate(() => Promise.all([
      document.fonts.ready,
      ...[...document.images].map(i => i.complete ? null :
        new Promise(r => { i.onload = i.onerror = r; })),
    ]));
    await new Promise(r => setTimeout(r, 300));

    const out = path.join(OUT, `linkedin-banner-${theme}.png`);
    await page.screenshot({ path: out, clip: { x: 0, y: 0, width: W, height: H } });
    console.log(`✓ ${out} (${W}x${H} @2x)`);
    await page.close();
  }

  await browser.close();
})();
