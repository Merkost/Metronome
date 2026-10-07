(function (global) {
  'use strict';

  var FILES = {
    WOOD: { normal: 'sounds/wood.mp3' },
    CLICK: { normal: 'sounds/click.mp3' },
    CLASSIC: { normal: 'sounds/metronome.wav' },
    SOFT: { normal: 'sounds/soft.wav', accent: 'sounds/soft_accent.wav' },
    RIM: { normal: 'sounds/rim.wav', accent: 'sounds/rim_accent.wav' },
    CLAVE: { normal: 'sounds/clave.wav', accent: 'sounds/clave_accent.wav' },
    STUDIO: { normal: 'sounds/studio.wav', accent: 'sounds/studio_accent.wav' }
  };

  var ctx = null;
  var buffers = {};
  var owners = {};
  var sources = {};
  var pending = {};
  var errors = {};
  var loadErrors = {};

  function init(channel, sound) {
    owners[channel] = true;
    delete errors[channel];
    if (ctx) {
      if (ctx.state === 'suspended') resume(channel);
      if (sound && loadErrors[sound]) {
        delete loadErrors[sound];
        loadSound(sound, ctx);
      }
      return;
    }
    var Ctor = global.AudioContext || global.webkitAudioContext;
    if (!Ctor) {
      errors[channel] = "Audio is unavailable in this browser";
      return;
    }
    ctx = new Ctor();
    var loadingContext = ctx;
    Object.keys(FILES).forEach(function (key) { loadSound(key, loadingContext); });
  }

  function loadSound(key, loadingContext) {
      buffers[key] = {};
      Object.keys(FILES[key]).forEach(function (variant) {
        fetch(FILES[key][variant])
          .then(function (response) {
            if (!response.ok) throw new Error('HTTP ' + response.status);
            return response.arrayBuffer();
          })
          .then(function (bytes) {
            return new Promise(function (resolve, reject) {
              loadingContext.decodeAudioData(bytes, resolve, reject);
            });
          })
          .then(function (decoded) {
            if (ctx !== loadingContext) return;
            buffers[key][variant] = decoded;
            Object.keys(pending).forEach(function (owner) {
              var request = pending[owner];
              var requestedVariant = request.rate > 1 && FILES[key].accent ? 'accent' : 'normal';
              if (request.sound === key && requestedVariant === variant) {
                delete pending[owner];
                play(owner, request.sound, request.rate, request.gain, request.pan);
              }
            });
          })
          .catch(function (error) {
            console.error('[metronome] load ' + key, error);
            if (ctx !== loadingContext) return;
            loadErrors[key] = "Sound resource could not be loaded";
            Object.keys(pending).forEach(function (owner) {
              if (pending[owner].sound === key) errors[owner] = loadErrors[key];
            });
          });
      });
  }

  function resume(channel) {
    var resumingContext = ctx;
    try {
      var resumed = ctx.resume();
      if (resumed && resumed.catch) resumed.catch(function (error) {
        console.error('[metronome] resume', error);
        if (ctx === resumingContext && owners[channel]) errors[channel] = "Audio could not be resumed";
      });
    } catch (error) {
      console.error('[metronome] resume', error);
      errors[channel] = "Audio could not be resumed";
    }
  }

  function play(channel, sound, rate, gain, pan) {
    if (!ctx || !owners[channel] || !FILES[sound]) return;
    if (ctx.state === 'suspended') resume(channel);
    var variant = rate > 1 && FILES[sound].accent ? 'accent' : 'normal';
    var buffer = buffers[sound] && buffers[sound][variant];
    if (!buffer) {
      if (loadErrors[sound]) {
        errors[channel] = loadErrors[sound];
        return;
      }
      pending[channel] = { sound: sound, rate: rate, gain: gain, pan: pan };
      return;
    }
    delete pending[channel];
    var level = gain == null ? 1 : Math.max(0, Math.min(1, gain));
    if (level === 0) return;
    var source = ctx.createBufferSource();
    source.buffer = buffer;
    source.playbackRate.value = variant === 'accent' ? 1 : rate || 1;
    var gainNode = ctx.createGain();
    gainNode.gain.value = level;
    var node = source;
    var panner = null;
    if (ctx.createStereoPanner) {
      panner = ctx.createStereoPanner();
      panner.pan.value = Math.max(-1, Math.min(1, pan || 0));
      source.connect(panner);
      node = panner;
    }
    node.connect(gainNode);
    gainNode.connect(ctx.destination);
    if (!sources[channel]) sources[channel] = [];
    sources[channel].push(source);
    source.onended = function () {
      if (sources[channel]) sources[channel] = sources[channel].filter(function (item) { return item !== source; });
      source.disconnect();
      if (panner) panner.disconnect();
      gainNode.disconnect();
    };
    try {
      source.start();
    } catch (error) {
      console.error('[metronome] play ' + sound, error);
      errors[channel] = "Sound could not start";
    }
  }

  function stop(channel) {
    delete pending[channel];
    var active = sources[channel] || [];
    sources[channel] = [];
    active.forEach(function (source) {
      try { source.stop(); } catch (error) { console.error('[metronome] stop', error); }
    });
  }

  function release(channel) {
    stop(channel);
    delete owners[channel];
    delete errors[channel];
    if (Object.keys(owners).length) return;
    if (ctx) ctx.close();
    ctx = null;
    buffers = {};
    sources = {};
    pending = {};
    errors = {};
    loadErrors = {};
  }

  function takeError(channel) {
    var error = errors[channel] || null;
    delete errors[channel];
    return error;
  }

  global.MetronomeWebAudio = { init: init, play: play, stop: stop, release: release, takeError: takeError };
})(globalThis);
