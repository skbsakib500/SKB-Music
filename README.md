# SKB Music

> A privacy-first Android music player.
> Local files. No internet. No tracking. No ads.

[![Build](https://github.com/skbsakib500/SKB-Music/actions/workflows/build.yml/badge.svg)](https://github.com/skbsakib500/SKB-Music/actions)

## Features

- 🎵 Media3 ExoPlayer playback
- 💾 Room database (library, playlists, favorites, recents)
- 🎨 Dynamic gradient from album art
- 📊 Real-time spectrum visualizer
- 🎚️ 10-band equalizer + bass boost + virtualizer
- 🔊 ReplayGain normalization
- 💬 Lyrics (.lrc + embedded)
- 😴 Sleep timer
- ⏩ Playback speed
- 🔔 Custom notification + MediaSession
- 📈 Listening stats
- 🚫 No internet, no ads, no accounts

## Requirements

- Android 7.0+ (API 24)
- Storage permission (audio files)

## Install

Download the latest APK from
[Actions](https://github.com/skbsakib500/SKB-Music/actions)
or build:

    git clone https://github.com/skbsakib500/SKB-Music.git
    cd SKB-Music
    ./gradlew :app:assembleDebug

## Tech Stack

- Kotlin + Jetpack Compose + Material3
- AndroidX Media3 ExoPlayer 1.4.1
- Room 2.6.1 (KSP) + DataStore 1.1.1
- Coil 2.7.0 (images)
- Palette (dynamic color)
- Navigation Compose
- Min SDK 24, Target 34

## Documentation

See `PROJECT.md`, `ARCHITECTURE.md`, `ROADMAP.md`,
`DECISIONS.md`, `KNOWN-ISSUES.md`, `DEVELOPMENT.md`.

## License

TBD (Apache-2.0 or MIT)

## Author

**SKB Dev**
- GitHub: [skbsakib500](https://github.com/skbsakib500)
- Email: msakibalmhamud5@gmail.com
