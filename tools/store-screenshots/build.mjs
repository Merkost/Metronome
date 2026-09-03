#!/usr/bin/env node
import { readFileSync, writeFileSync, mkdirSync, existsSync, rmSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))
const repo = resolve(here, '..', '..')
const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'

const config = JSON.parse(readFileSync(join(here, 'frames.json'), 'utf8'))
const template = readFileSync(join(here, 'template.html'), 'utf8')

const only = process.argv.slice(2).filter(a => !a.startsWith('-'))
const deviceArg = (process.argv.find(a => a.startsWith('--device=')) || '').split('=')[1]
const devices = Object.entries(config.devices).filter(([k]) => !deviceArg || k === deviceArg)

const outRoot = join(repo, 'artifacts', 'store-screenshots')
const tmp = join(here, '.tmp')
mkdirSync(tmp, { recursive: true })

const esc = s => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

let made = 0
const missing = []

for (const [deviceId, device] of devices) {
  const outDir = join(outRoot, deviceId)
  mkdirSync(outDir, { recursive: true })

  config.frames.forEach((frame, index) => {
    if (only.length && !only.includes(frame.id)) return

    const shot = join(here, frame.screenshot)
    if (!existsSync(shot)) {
      missing.push(`${frame.id} -> ${frame.screenshot}`)
      return
    }

    const badges = frame.badges?.length
      ? `<div class="badges">${frame.badges.map(b => `<span>${esc(b)}</span>`).join('')}</div>`
      : ''

    // intrinsic ratio of the source screenshot, so the crop maths is exact
    const png = readFileSync(shot)
    const shotW = png.readUInt32BE(16)
    const shotH = png.readUInt32BE(20)

    const D = config.defaults
    const rings = (frame.rings ?? [0.6, 0.95, 1.35, 1.8])
      .map(r => `<span style="width:${Math.round(device.width * r)}px;height:${Math.round(device.width * r)}px"></span>`)
      .join('')
    const ticks = Array.from({ length: 9 }, (_, i) =>
      `<i class="${i === 0 ? 'big' : ''}" style="left:${(10 + i * 10).toFixed(2)}%"></i>`).join('')

    const vars = [
      `--device-w:${Math.round(device.width * (frame.deviceW ?? 0.64))}px`,
      `--device-y:${Math.round(device.height * (frame.deviceY ?? 0.32))}px`,
      `--shot-scale:${frame.shotScale ?? 1}`,
      `--shot-pan:${frame.shotPan ?? 0}`,
      `--focus-y:${Math.round(device.height * (frame.focusY ?? 0.72))}px`,
      `--rule-y:${Math.round(device.height * (frame.ruleY ?? 0.25))}px`,
    ].join(';')

    const html = template
      .replaceAll('__WIDTH__', device.width)
      .replaceAll('__HEIGHT__', device.height)
      .replaceAll('__SHOT_RATIO__', (shotH / shotW).toFixed(6))
      .replaceAll('__RINGS__', rings)
      .replaceAll('__TICKS__', ticks)
      .replaceAll('__TITLE__', esc(frame.title))
      .replaceAll('__SUBTITLE__', esc(frame.subtitle))
      .replaceAll('__BADGES__', badges)
      .replaceAll('__SCREENSHOT__', pathToFileURL(shot).href)
      .replace('<body>', `<body style="${vars}">`)

    const page = join(tmp, `${deviceId}-${frame.id}.html`)
    writeFileSync(page, html)

    const n = String(index + 1).padStart(2, '0')
    const out = join(outDir, `${n}-${frame.id}.png`)

    execFileSync(CHROME, [
      '--headless=new',
      '--disable-gpu',
      '--hide-scrollbars',
      '--force-device-scale-factor=1',
      `--window-size=${device.width},${device.height}`,
      `--screenshot=${out}`,
      pathToFileURL(page).href,
    ], { stdio: 'pipe' })

    console.log(`  ${deviceId}/${n}-${frame.id}.png`)
    made++
  })
}

rmSync(tmp, { recursive: true, force: true })

console.log(`\n${made} frame(s) written to artifacts/store-screenshots/`)
if (missing.length) {
  console.log(`\n${missing.length} frame(s) skipped — no source screenshot yet:`)
  for (const m of missing) console.log(`  - ${m}`)
  process.exitCode = 1
}
