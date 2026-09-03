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
    const align = frame.align === 'left' ? 'left' : 'center'
    const rings = (frame.rings ?? [0.5, 0.8, 1.1])
      .map(r => `<span style="width:${Math.round(device.width * r)}px;height:${Math.round(device.width * r)}px;opacity:${(0.26 - 0.05 * (frame.rings ?? []).indexOf(r)).toFixed(3)}"></span>`)
      .join('')

    const vars = [
      `--align:${align}`,
      `--justify:${align === 'left' ? 'flex-start' : 'center'}`,
      `--device-w:${Math.round(device.width * (frame.deviceW ?? 0.66))}px`,
      `--device-x:${Math.round(device.width * (frame.deviceX ?? 0.5))}px`,
      `--device-y:${Math.round(device.height * (frame.deviceY ?? 0.33))}px`,
      `--device-rot:${frame.rotation ?? '0deg'}`,
      `--bloom-x:${Math.round(device.width * (frame.bloomX ?? 0.5))}px`,
      `--bloom-y:${Math.round(device.height * (frame.bloomY ?? 0.66))}px`,
    ].join(';')

    const html = template
      .replaceAll('__WIDTH__', device.width)
      .replaceAll('__HEIGHT__', device.height)
      .replaceAll('__ACCENT__', frame.accent ?? '#B89FFF')
      .replaceAll('__BG__', frame.bg ?? D.bg)
      .replaceAll('__INK__', D.ink)
      .replaceAll('__MUTED__', D.muted)
      .replaceAll('__SHOT_RATIO__', (shotH / shotW).toFixed(6))
      .replaceAll('__RINGS__', rings)
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
