#!/usr/bin/env python3
"""Original, deterministic Aetherium audio: no vanilla mob samples.

Requires Python 3 standard library and FFmpeg (libvorbis). The CI build
generates OGG assets before Gradle packages its resources.
"""
from pathlib import Path
import math
import random
import shutil
import struct
import subprocess
import tempfile
import wave

RATE = 16000
DEST = Path('src/main/resources/assets/aetherium/sounds')
# name: duration, low frequency, high frequency, inharmonic ratio, noise,
# attack seconds, release seconds, flutter rate, synthesis type
SOUNDS = {
    'astral_ambient':       (2.30, 82, 62, 1.503, .095, .23, .55, 1.5, 'drone'),
    'astral_hurt':          (.65, 510, 105, 2.051, .40, .012, .24, 9.0, 'crack'),
    'astral_death':         (2.70, 190, 39, 1.505, .30, .026, .60, 2.4, 'collapse'),
    'astral_laser_charge':  (1.32, 92, 540, 2.037, .12, .12, .085, 14.0, 'rise'),
    'astral_laser_fire':    (1.10, 145, 172, 1.991, .33, .014, .22, 21.0, 'buzz'),
    'astral_dash':          (.65, 340, 88, 1.715, .28, .008, .23, 12.0, 'rush'),
    'astral_mirage':        (.88, 190, 310, 1.624, .16, .04, .28, 11.0, 'shimmer'),
    'astral_nova':          (1.04, 130, 42, 2.112, .35, .015, .32, 8.0, 'collapse'),
    'guardian_ambient':     (2.20, 62, 53, 1.681, .14, .25, .63, .8, 'drone'),
    'guardian_hurt':        (.64, 325, 75, 2.765, .44, .004, .25, 8.0, 'crack'),
    'guardian_death':       (2.90, 115, 33, 2.224, .36, .01, .85, 1.4, 'collapse'),
    'guardian_windup':      (.85, 104, 250, 1.671, .16, .09, .15, 10.0, 'rise'),
    'guardian_charge':      (.75, 170, 280, 1.407, .28, .01, .24, 15.0, 'rush'),
    'guardian_sweep':       (.69, 500, 72, 1.843, .37, .004, .25, 12.0, 'rush'),
    'guardian_mirage':      (.90, 112, 370, 2.014, .18, .06, .29, 16.0, 'shimmer'),
    'stalker_ambient':      (1.12, 225, 135, 1.833, .16, .06, .35, 8.0, 'shimmer'),
    'stalker_hurt':         (.51, 360, 85, 1.934, .31, .007, .25, 17.0, 'crack'),
    'stalker_death':        (1.28, 200, 45, 2.141, .23, .012, .43, 7.0, 'collapse'),
    'lumivelo_hurt':       (.59, 780, 260, 1.505, .10, .012, .24, 5.0, 'chime'),
    'lumivelo_death':      (1.31, 660, 125, 1.661, .09, .027, .52, 3.0, 'chime'),
}


def synthesize(name, spec, output):
    duration, start, end, ratio, grit, attack, release, flutter, shape = spec
    rng = random.Random('aetherium-original-' + name)
    n = int(RATE * duration)
    p0 = p1 = p2 = 0.0
    filtered = 0.0
    samples = []
    for i in range(n):
        t = i / RATE
        progress = t / duration
        if shape in ('collapse', 'crack', 'rush'):
            curve = (1.0 - progress) ** 2.0
        elif shape == 'rise':
            curve = progress ** 1.65
        else:
            curve = progress
        freq = (start + (end - start) * curve) * (
            1.0 + (.004 if shape == 'chime' else .012)
            * math.sin(2.0 * math.pi * flutter * t))
        p0 += 2.0 * math.pi * freq / RATE
        p1 += 2.0 * math.pi * freq * ratio / RATE
        p2 += 2.0 * math.pi * freq * (ratio + .712) / RATE
        tone = .52 * math.sin(p0) + .25 * math.sin(p1) + .12 * math.sin(p2)
        white = rng.uniform(-1.0, 1.0)
        filtered += .18 * (white - filtered)
        air = .25 * white + .75 * filtered
        if shape in ('crack', 'collapse', 'rush'):
            air *= (1.0 - progress) ** 1.4
        elif shape == 'rise':
            air *= .35 + progress * .65
        if shape == 'buzz':
            tone *= .82 + .14 * math.sin(2.0 * math.pi * 20.0 * t)
        if shape == 'shimmer':
            tone *= .77 + .20 * math.sin(2.0 * math.pi * 7.2 * t)
        signal = tone + grit * air
        envelope = max(0.0, min(1.0, t / attack) * min(1.0, (duration - t) / release))
        if shape == 'drone':
            envelope *= .82 + .12 * math.sin(2.0 * math.pi * .7 * t)
        elif shape in ('crack', 'collapse', 'chime'):
            envelope *= math.exp(-progress * (1.2 if shape == 'collapse' else 1.75))
        samples.append(signal * envelope)
    # Every file fades naturally; a quiet echo adds depth to ambient and death cues.
    if shape in ('drone', 'shimmer', 'collapse', 'chime'):
        delay = int(.095 * RATE)
        for i in range(delay, n):
            samples[i] += .12 * samples[i - delay]
    peak = max(max(abs(v) for v in samples), 1e-9)
    scale = .64 / peak
    pcm = b''.join(struct.pack('<h', int(max(-1.0, min(1.0, v * scale)) * 32767))
                   for v in samples)
    with tempfile.TemporaryDirectory() as temp:
        wav_path = Path(temp) / 'source.wav'
        with wave.open(str(wav_path), 'wb') as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            wav.writeframes(pcm)
        subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y',
                        '-i', str(wav_path), '-c:a', 'libvorbis', '-q:a', '0',
                        str(output)], check=True)


def main():
    if not shutil.which('ffmpeg'):
        raise SystemExit('Original Aetherium sound generation requires FFmpeg.')
    DEST.mkdir(parents=True, exist_ok=True)
    for name, spec in SOUNDS.items():
        path = DEST / (name + '.ogg')
        synthesize(name, spec, path)
        print('Generated Aetherium audio:', path, path.stat().st_size, 'bytes')


if __name__ == '__main__':
    main()
