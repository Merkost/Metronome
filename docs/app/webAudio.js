/*
 * Web Audio bridge for MetronomePlayerWasm.
 *
 * The Kotlin engine owns the beat schedule, so this only has to make a sound
 * immediately without blocking. Buffers are decoded once at init; play()
 * creates a source node and starts it, which is non-blocking.
 */
(function (global) {
  'use strict';

  var FILES = { WOOD: 'sounds/wood.mp3', CLICK: 'sounds/click.mp3', CLASSIC: 'sounds/metronome.wav' };

  var ctx = null;
  var buffers = {};

  function init() {
    if (ctx) {
      if (ctx.state === 'suspended') ctx.resume();
      return;
    }
    var Ctor = global.AudioContext || global.webkitAudioContext;
    if (!Ctor) return;
    ctx = new Ctor();
    Object.keys(FILES).forEach(function (key) {
      fetch(FILES[key])
        .then(function (r) { return r.arrayBuffer(); })
        .then(function (b) {
          return new Promise(function (res, rej) { ctx.decodeAudioData(b, res, rej); });
        })
        .then(function (decoded) { buffers[key] = decoded; })
        .catch(function (e) { console.error('[metronome] load ' + key, e); });
    });
  }

  function play(sound, rate, gain, pan) {
    if (!ctx) return;
    if (ctx.state === 'suspended') ctx.resume();
    var buf = buffers[sound];
    if (!buf) return;
    var level = gain == null ? 1 : Math.max(0, Math.min(1, gain));
    if (level === 0) return;
    var src = ctx.createBufferSource();
    src.buffer = buf;
    src.playbackRate.value = rate || 1;
    var gainNode = ctx.createGain();
    gainNode.gain.value = level;
    var node = src;
    if (ctx.createStereoPanner) {
      var panner = ctx.createStereoPanner();
      panner.pan.value = Math.max(-1, Math.min(1, pan || 0));
      src.connect(panner);
      node = panner;
    }
    node.connect(gainNode);
    gainNode.connect(ctx.destination);
    src.start();
  }

  function release() {
    if (ctx) ctx.close();
    ctx = null;
    buffers = {};
  }

  global.MetronomeWebAudio = { init: init, play: play, release: release };
})(globalThis);
