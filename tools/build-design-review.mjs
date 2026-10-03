import { createHash } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { execFileSync } from "node:child_process";

const repository = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const source = path.join(repository, "web/design-review");
const site = path.resolve(repository, process.argv[2] ?? "_site");
const output = path.join(site, "design");
const base = "/design/";
const assetRoots = ["assets/", "audio/", "metronome-icon.png", "suby-icon.png", "review/", "suggestions.html"];
const assetPattern = new RegExp("([\"'`])/((?:" + assetRoots.map(value => value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|") + "))", "g");

execFileSync(process.execPath, ["scripts/check-mobile-runtime.mjs"], { cwd: source, stdio: "inherit" });
execFileSync(process.execPath, ["node_modules/typescript/bin/tsc"], { cwd: source, stdio: "inherit" });

const { build } = await import(pathToFileURL(path.join(source, "node_modules/vite/dist/node/index.js")));
await build({
  root: source,
  base,
  build: { outDir: output, emptyOutDir: true },
  plugins: [{
    name: "design-review-asset-base",
    renderChunk(code) {
      return { code: code.replace(assetPattern, (_, quote, resource) => quote + base + resource), map: null };
    },
  }],
});

function filesIn(directory) {
  return readdirSync(directory, { withFileTypes: true }).filter(entry => !entry.isSymbolicLink() && !["node_modules", "dist", ".git"].includes(entry.name)).flatMap(entry => {
    const absolute = path.join(directory, entry.name);
    return entry.isDirectory() ? filesIn(absolute) : [absolute];
  });
}

const resourceUrls = new Set();
for (const file of filesIn(output)) {
  if (!file.endsWith(".html")) continue;
  const html = readFileSync(file, "utf8").replace(/((?:href|src|poster)\s*=\s*["'])\/(?!\/|design\/)/g, "$1" + base);
  writeFileSync(file, html);
  for (const match of html.matchAll(/(?:href|src|poster)\s*=\s*["'](\/design\/[^"']*)/g)) resourceUrls.add(match[1]);
}

for (const resource of resourceUrls) {
  const pathname = decodeURI(resource.split(/[?#]/)[0]);
  const target = path.join(site, pathname);
  if (!existsSync(target) && !existsSync(target + ".html")) throw new Error("Missing design resource: " + resource);
}

for (const resource of [
  "assets/iphone/Bezel.png", "assets/iphone/Keyboard.png", "assets/android/Pixel10.png",
  "assets/android/Keyboard.png", "assets/android/navigation-bar.svg",
  "assets/status/status-icons.svg", "assets/status/ios-status-icons.svg",
  "audio/wood.mp3", "audio/click.mp3", "audio/classic.wav",
  "audio/soft.wav", "audio/rim.wav", "audio/clave.wav", "audio/studio.wav",
  "audio/soft-accent.wav", "audio/rim-accent.wav", "audio/clave-accent.wav", "audio/studio-accent.wav",
]) {
  if (!existsSync(path.join(output, resource))) throw new Error("Missing runtime or sound asset: " + resource);
}

for (const file of filesIn(output).filter(file => file.endsWith(".js"))) {
  assetPattern.lastIndex = 0;
  if (assetPattern.test(readFileSync(file, "utf8"))) throw new Error("Unprefixed design resource in " + file);
}

const hash = createHash("sha256");
for (const file of filesIn(source).filter(file => !file.includes(path.sep + "node_modules" + path.sep)).sort()) {
  hash.update(path.relative(source, file));
  hash.update(readFileSync(file));
}
mkdirSync(output, { recursive: true });
writeFileSync(path.join(output, "build-version.json"), JSON.stringify({
  kind: "interactive-design-review",
  base,
  revision: process.env.GITHUB_SHA ?? execFileSync("git", ["rev-parse", "HEAD"], { cwd: repository, encoding: "utf8" }).trim(),
  sourceHash: hash.digest("hex"),
}, null, 2) + "\n");
console.log("Design review built at " + output + "; verified " + resourceUrls.size + " local page resources.");
