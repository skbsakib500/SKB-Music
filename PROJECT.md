# SKB-Music

> **SKB Dev Ecosystem · Project Identity Card**
> _Build Once. Track Everything. Update Safely. Remember Forever._

---

## Identity

| Field | Value |
|-------|-------|
| Project ID | `skb-music` |
| Display Name | SKB Music |
| Package | `com.skb.music` |
| Type | Android App (Kotlin + Jetpack Compose + Room) |
| Domain | Media (local music player) |
| versionName | `2.2.0` ✅ (matches commits) |
| versionCode | `3` |
| Status | **Active** |
| Author | SKB Dev |
| GitHub | [skbsakib500/SKB-Music](https://github.com/skbsakib500/SKB-Music) |
| Branch | `main` |
| Kotlin files | 36 |

---

## What Is This

SKB-Music is a **privacy-first local music player** for Android.

- Media3 ExoPlayer playback
- Room database for library, playlists, favorites, recents
- MVVM architecture (viewmodel / domain / data)
- Dynamic color from album art (Palette)
- Lyrics (.lrc files + embedded)
- ReplayGain volume normalization
- Real-time spectrum visualizer
- Equalizer, bass boost, virtualizer
- Sleep timer, playback speed
- No internet. No tracking. No cloud.

---

## Position In Ecosystem

    SKB-Music (this project)   ← audio player, independent media
    SKB-Player                 ← sibling (video focus)

**Not part of Mimi Life OS.** Media is a separate concern.

Sibling ecosystem:
- `Mimi-Android`   — Life OS delivery (WebView + Python)
- `Agent-Mimi`     — Life OS brain (Python)
- `SKB-Player`     — video player (Kotlin + Compose)
- `Mimi-Forever`   — backup vault

---

## Tech Stack

| Layer | Tech |
|-------|------|
| Language | Kotlin 1.9.x |
| UI | Jetpack Compose (BOM 2024.09.02) + Material3 |
| Media | AndroidX Media3 ExoPlayer 1.4.1 |
| Storage | Room 2.6.1 (KSP) + DataStore 1.1.1 |
| Image | Coil 2.7.0 |
| Palette | androidx.palette:palette-ktx |
| Navigation | Navigation Compose 2.8.0 |
| Build | AGP 8.5.2, Kotlin 1.9.x, JDK 17 |
| Min SDK | 24 · Target 34 |
| CI | GitHub Actions (`build.yml`) |

---

## Feature Set

### Core
- Library scan (MediaStore)
- Now Playing screen with controls
- Persistent queue, background playback
- MediaSession + custom notification

### Enhancement
- Equalizer (10-band), bass boost, virtualizer
- ReplayGain (session-aware)
- Spectrum visualizer
- Dynamic gradient (album art palette)
- Lyrics (.lrc + embedded)
- Sleep timer, playback speed
- Listening stats

### Organization
- Library tab (all songs)
- Favorites
- Playlists
- Recents
- Browse (folders/albums/artists)
- Long-press menu, play next / queue

---

## Memory Files

PROJECT.md · ARCHITECTURE.md · ROADMAP.md · CHANGELOG.md ·
DECISIONS.md · KNOWN-ISSUES.md · TODO.md · DEVELOPMENT.md · `.skb/`

_Last updated: 2026-09-16_
