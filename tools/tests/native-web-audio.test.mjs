import test from "node:test";
import assert from "node:assert/strict";
import {readFileSync} from "node:fs";
import vm from "node:vm";

const source = readFileSync(new URL("../../shared/src/wasmJsMain/resources/webAudio.js", import.meta.url), "utf8");
const settle = () => new Promise(resolve => setImmediate(resolve));

function fixture({failFetch = false, available = true} = {}) {
  const contexts = [];
  const fetched = [];
  class AudioContext {
    constructor() {
      this.state = "running";
      this.destination = {};
      this.sources = [];
      this.closed = 0;
      contexts.push(this);
    }
    decodeAudioData(file, resolve) { resolve({file}); }
    createBufferSource() {
      const source = {playbackRate: {}, connect() {}, disconnect() {}, start() { this.started = true; }, stop() { this.stopped = true; this.onended?.(); }};
      this.sources.push(source);
      return source;
    }
    createGain() { return {gain: {}, connect() {}, disconnect() {}}; }
    createStereoPanner() { return {pan: {}, connect() {}, disconnect() {}}; }
    resume() { this.state = "running"; }
    close() { this.closed++; }
  }
  const sandbox = {console: {error() {}}, fetch: async path => { fetched.push(path); return {ok: !failFetch, status: 503, arrayBuffer: async () => path}; }};
  if (available) sandbox.AudioContext = AudioContext;
  vm.runInNewContext(source, sandbox);
  return {audio: sandbox.MetronomeWebAudio, contexts, fetched, setFetchFailure(value) { failFetch = value; }};
}

test("preview releases its voices without closing the main channel", async () => {
  const {audio, contexts, fetched} = fixture();
  audio.init("main");
  audio.init("preview");
  await settle();
  audio.play("main", "WOOD", 1, 1, 0);
  audio.play("preview", "SOFT", 1.4, .5, 0);
  const [main, preview] = contexts[0].sources;
  assert.equal(preview.buffer.file, "sounds/soft_accent.wav");
  assert.equal(preview.playbackRate.value, 1);
  audio.release("preview");
  assert.equal(preview.stopped, true);
  assert.equal(main.stopped, undefined);
  assert.equal(contexts[0].closed, 0);
  audio.play("main", "STUDIO", 1.4, 1, 0);
  assert.equal(contexts[0].sources.at(-1).buffer.file, "sounds/studio_accent.wav");
  assert.equal(fetched.length, 11);
  audio.release("main");
  assert.equal(contexts[0].closed, 1);
});

test("canceled loading preview cannot play later", async () => {
  const {audio, contexts} = fixture();
  audio.init("main");
  audio.init("preview");
  audio.play("preview", "CLAVE", 1.4, 1, 0);
  audio.stop("preview");
  audio.play("main", "RIM", 1, 1, 0);
  await settle();
  assert.equal(contexts[0].sources.length, 1);
  assert.equal(contexts[0].sources[0].buffer.file, "sounds/rim.wav");
});

test("old decodes cannot replace a newly initialized context", async () => {
  const {audio, contexts} = fixture();
  audio.init("preview");
  audio.release("preview");
  audio.init("main");
  audio.play("main", "CLICK", 1.4, 1, 0);
  await settle();
  assert.equal(contexts.length, 2);
  assert.equal(contexts[0].sources.length, 0);
  assert.equal(contexts[1].sources.length, 1);
  assert.equal(contexts[1].sources[0].playbackRate.value, 1.4);
});

test("failed loading is visible only to the requesting channel", async () => {
  const {audio} = fixture({failFetch: true});
  audio.init("main");
  audio.init("preview");
  audio.play("preview", "SOFT", 1.4, 1, 0);
  await settle();
  assert.equal(audio.takeError("preview"), "Sound resource could not be loaded");
  assert.equal(audio.takeError("main"), null);
  assert.equal(audio.takeError("preview"), null);
});

test("unavailable browser audio reports an actionable failure", () => {
  const {audio} = fixture({available: false});
  audio.init("preview");
  assert.equal(audio.takeError("preview"), "Audio is unavailable in this browser");
  audio.release("preview");
  assert.equal(audio.takeError("preview"), null);
});

test("a new preview reloads its sound after a transient fetch failure", async () => {
  const {audio, contexts, setFetchFailure} = fixture({failFetch: true});
  audio.init("main");
  audio.init("preview", "SOFT");
  audio.play("preview", "SOFT", 1.4, 1, 0);
  await settle();
  assert.equal(audio.takeError("preview"), "Sound resource could not be loaded");
  setFetchFailure(false);
  audio.init("preview", "SOFT");
  audio.play("preview", "SOFT", 1.4, 1, 0);
  await settle();
  assert.equal(audio.takeError("preview"), null);
  assert.equal(contexts[0].sources.at(-1).buffer.file, "sounds/soft_accent.wav");
});
