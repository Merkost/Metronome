/*
 * Browser metronome for metronome.merkost.dev
 *
 * Timing mirrors the native engine (see docs/metronome-precision-audit.md):
 * an imprecise waker on the main thread, a precise audio clock for event
 * times, and a cumulative deadline that is never derived from "now".
 *
 * The two failure modes the audit found in the Kotlin engine both have exact
 * browser analogues, and both are avoided here:
 *   M1 open-loop chaining  -> setInterval / recursive setTimeout. Avoided by
 *                             scheduling against AudioContext.currentTime.
 *   M2 integer division    -> 60000 / bpm truncating. Avoided by keeping the
 *                             beat interval a double in seconds.
 */
(function () {
  'use strict';

  var MIN_BPM = 40;
  var MAX_BPM = 220;
  var DEFAULT_BPM = 80;

  var LOOKAHEAD = 0.1; // seconds of audio committed ahead
  var TICK_MS = 25; // waker period; LOOKAHEAD >= 3 * TICK_MS
  var RUN_UP = 0.1; // silence before the first beat

  var TIME_SIGNATURES = [
    { label: '2/4', beats: 2 },
    { label: '3/4', beats: 3 },
    { label: '4/4', beats: 4 },
    { label: '5/4', beats: 5 },
    { label: '6/8', beats: 6 },
    { label: '7/8', beats: 7 }
  ];

  var SUBDIVISIONS = [
    { label: 'Quarter', clicks: 1 },
    { label: 'Eighths', clicks: 2 },
    { label: 'Triplets', clicks: 3 },
    { label: '16ths', clicks: 4 }
  ];

  var SOUNDS = [
    { label: 'Wood', file: 'app/sounds/wood.mp3' },
    { label: 'Click', file: 'app/sounds/click.mp3' },
    { label: 'Classic', file: 'app/sounds/metronome.wav' }
  ];

  // Ported verbatim from MetronomeState.tempoName. The ranges overlap and are
  // evaluated top-down, so shared endpoints belong to the faster marking.
  function tempoName(bpm) {
    if (bpm >= 168 && bpm <= 200) return 'Presto';
    if (bpm >= 120 && bpm <= 168) return 'Allegro';
    if (bpm >= 108 && bpm <= 120) return 'Moderato';
    if (bpm >= 76 && bpm <= 108) return 'Andante';
    if (bpm >= 66 && bpm <= 76) return 'Adagio';
    if (bpm >= 40 && bpm <= 66) return 'Largo';
    return 'Prestissimo';
  }

  function clampBpm(v) {
    return Math.min(MAX_BPM, Math.max(MIN_BPM, Math.round(v)));
  }

  function Metronome() {
    this.ctx = null;
    this.buffers = {};
    this.gain = null;

    this.bpm = DEFAULT_BPM;
    this.signature = TIME_SIGNATURES[2];
    this.subdivision = SUBDIVISIONS[0];
    this.sound = SOUNDS[0];
    this.beats = this.defaultBeats(this.signature.beats);

    this.playing = false;
    this.nextBeatTime = 0;
    this.beatIndex = 0;
    this.timer = null;
    this.visualQueue = [];
    this.onBeat = null;
  }

  // Beat 0 accented, the rest normal — matches the app's default pattern.
  Metronome.prototype.defaultBeats = function (count) {
    var out = [];
    for (var i = 0; i < count; i++) out.push(i === 0 ? 'accented' : 'normal');
    return out;
  };

  Metronome.prototype.ensureContext = function () {
    if (this.ctx) return Promise.resolve();
    var Ctor = window.AudioContext || window.webkitAudioContext;
    if (!Ctor) return Promise.reject(new Error('Web Audio unavailable'));
    this.ctx = new Ctor();
    this.gain = this.ctx.createGain();
    this.gain.gain.value = 0.9;
    this.gain.connect(this.ctx.destination);
    return this.loadSounds();
  };

  Metronome.prototype.loadSounds = function () {
    var self = this;
    return Promise.all(
      SOUNDS.map(function (s) {
        if (self.buffers[s.label]) return Promise.resolve();
        return fetch(s.file)
          .then(function (r) {
            if (!r.ok) throw new Error('fetch ' + s.file + ' -> ' + r.status);
            return r.arrayBuffer();
          })
          .then(function (buf) {
            return new Promise(function (resolve, reject) {
              // Callback form for Safari, which lacks the promise overload.
              self.ctx.decodeAudioData(buf, resolve, reject);
            });
          })
          .then(function (decoded) {
            self.buffers[s.label] = decoded;
          });
      })
    );
  };

  // Decoding and node creation stay off the beat path; start(t) is
  // non-blocking and sample accurate.
  Metronome.prototype.scheduleClick = function (state, when) {
    if (state === 'muted') return;
    var buffer = this.buffers[this.sound.label];
    if (!buffer) return;
    var src = this.ctx.createBufferSource();
    src.buffer = buffer;
    var g = this.ctx.createGain();
    g.gain.value = state === 'accented' ? 1.0 : 0.62;
    // Accents read as accents by pitch as well as level, matching the app.
    src.playbackRate.value = state === 'accented' ? 1.0 : 0.94;
    src.connect(g);
    g.connect(this.gain);
    src.start(when);
  };

  Metronome.prototype.tick = function () {
    var now = this.ctx.currentTime;
    var spb = 60 / this.bpm; // double, seconds — never rounded, never ms
    var clicks = this.subdivision.clicks;

    // Re-anchor after a stall (backgrounded tab, sleep) instead of firing a
    // burst of overdue beats. Mirrors BeatTimeline.advance()'s guard.
    if (this.nextBeatTime < now - spb) {
      this.nextBeatTime = now + 0.02;
      this.beatIndex = 0;
    }

    while (this.nextBeatTime < now + LOOKAHEAD) {
      var state = this.beats[this.beatIndex % this.beats.length];
      this.scheduleClick(state, this.nextBeatTime);
      this.visualQueue.push({ index: this.beatIndex % this.beats.length, time: this.nextBeatTime });

      // Sub-clicks are derived from the beat origin, not from each other, so
      // the sub-millisecond remainder never accumulates (audit note L6).
      for (var i = 1; i < clicks; i++) {
        this.scheduleClick('normal', this.nextBeatTime + (spb * i) / clicks);
      }

      this.nextBeatTime += spb;
      this.beatIndex = (this.beatIndex + 1) % this.beats.length;
    }
  };

  Metronome.prototype.start = function () {
    var self = this;
    return this.ensureContext().then(function () {
      if (self.ctx.state === 'suspended') self.ctx.resume();
      if (self.playing) return;
      self.playing = true;
      self.beatIndex = 0;
      self.visualQueue.length = 0;
      self.nextBeatTime = self.ctx.currentTime + RUN_UP;
      self.tick();
      self.timer = setInterval(function () {
        self.tick();
      }, TICK_MS);
    });
  };

  Metronome.prototype.stop = function () {
    this.playing = false;
    if (this.timer) clearInterval(this.timer);
    this.timer = null;
    this.visualQueue.length = 0;
  };

  Metronome.prototype.toggle = function () {
    return this.playing ? (this.stop(), Promise.resolve()) : this.start();
  };

  Metronome.prototype.setBpm = function (v) {
    this.bpm = clampBpm(v);
  };

  Metronome.prototype.setSignature = function (sig) {
    this.signature = sig;
    this.beats = this.defaultBeats(sig.beats);
    this.beatIndex = 0;
  };

  Metronome.prototype.cycleBeat = function (i) {
    var order = ['accented', 'normal', 'muted'];
    var next = (order.indexOf(this.beats[i]) + 1) % order.length;
    this.beats[i] = order[next];
  };

  // ---- UI ----------------------------------------------------------------

  function init() {
    var root = document.getElementById('player');
    if (!root) return;

    var m = new Metronome();
    var $ = function (sel) {
      return root.querySelector(sel);
    };

    var dotsEl = $('.m-dots');
    var bpmEl = $('.m-bpm');
    var nameEl = $('.m-name');
    var slider = $('.m-slider');
    var playBtn = $('.m-play');
    var sigWrap = $('.m-sigs');
    var subWrap = $('.m-subs');
    var soundWrap = $('.m-sounds');
    var statusEl = $('.m-status');

    function renderDots() {
      dotsEl.innerHTML = '';
      m.beats.forEach(function (state, i) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'm-dot is-' + state;
        b.setAttribute('aria-label', 'Beat ' + (i + 1) + ': ' + state + '. Tap to change.');
        b.addEventListener('click', function () {
          m.cycleBeat(i);
          renderDots();
        });
        dotsEl.appendChild(b);
      });
    }

    function renderTempo() {
      bpmEl.textContent = m.bpm;
      nameEl.textContent = tempoName(m.bpm);
      slider.value = m.bpm;
      slider.setAttribute('aria-valuenow', m.bpm);
    }

    function chipGroup(wrap, items, isActive, onPick) {
      wrap.innerHTML = '';
      items.forEach(function (item) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'm-chip' + (isActive(item) ? ' is-on' : '');
        b.textContent = item.label;
        b.addEventListener('click', function () {
          onPick(item);
        });
        wrap.appendChild(b);
      });
    }

    function renderSigs() {
      chipGroup(sigWrap, TIME_SIGNATURES, function (s) {
        return s.label === m.signature.label;
      }, function (s) {
        m.setSignature(s);
        renderSigs();
        renderDots();
      });
    }

    function renderSubs() {
      chipGroup(subWrap, SUBDIVISIONS, function (s) {
        return s.label === m.subdivision.label;
      }, function (s) {
        m.subdivision = s;
        renderSubs();
      });
    }

    function renderSounds() {
      chipGroup(soundWrap, SOUNDS, function (s) {
        return s.label === m.sound.label;
      }, function (s) {
        m.sound = s;
        renderSounds();
      });
    }

    function setPlayingUi(on) {
      playBtn.classList.toggle('is-playing', on);
      playBtn.setAttribute('aria-label', on ? 'Stop metronome' : 'Start metronome');
      if (!on) {
        Array.prototype.forEach.call(dotsEl.children, function (d) {
          d.classList.remove('is-active');
        });
      }
    }

    // Visual beats are driven off the audio clock, never off the waker, so the
    // flash lands with the click rather than with a timer callback.
    function frame() {
      if (m.playing && m.ctx) {
        var now = m.ctx.currentTime;
        while (m.visualQueue.length && m.visualQueue[0].time <= now) {
          var ev = m.visualQueue.shift();
          Array.prototype.forEach.call(dotsEl.children, function (d, i) {
            d.classList.toggle('is-active', i === ev.index);
          });
        }
      }
      requestAnimationFrame(frame);
    }

    playBtn.addEventListener('click', function () {
      m.toggle()
        .then(function () {
          setPlayingUi(m.playing);
          statusEl.textContent = '';
        })
        .catch(function (err) {
          statusEl.textContent = 'Audio could not start in this browser.';
          console.error('[metronome]', err);
        });
    });

    slider.addEventListener('input', function () {
      m.setBpm(parseInt(slider.value, 10));
      renderTempo();
    });

    root.querySelectorAll('[data-delta]').forEach(function (b) {
      b.addEventListener('click', function () {
        m.setBpm(m.bpm + parseInt(b.getAttribute('data-delta'), 10));
        renderTempo();
      });
    });

    document.addEventListener('keydown', function (e) {
      if (e.target.matches('input, textarea, select, summary')) return;
      if (e.code === 'Space') {
        e.preventDefault();
        playBtn.click();
      } else if (e.key === 'ArrowUp' || e.key === 'ArrowRight') {
        m.setBpm(m.bpm + 1);
        renderTempo();
      } else if (e.key === 'ArrowDown' || e.key === 'ArrowLeft') {
        m.setBpm(m.bpm - 1);
        renderTempo();
      }
    });

    // A tab that loses focus gets its timers clamped to >=1s; stop rather than
    // let the scheduler stutter back to life.
    document.addEventListener('visibilitychange', function () {
      if (document.hidden && m.playing) {
        m.stop();
        setPlayingUi(false);
      }
    });

    renderDots();
    renderTempo();
    renderSigs();
    renderSubs();
    renderSounds();
    requestAnimationFrame(frame);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
