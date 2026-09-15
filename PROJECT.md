# SKB-Music

> **SKB Dev Ecosystem · Project Identity Card**

## Identity

| Field | Value |
|-------|-------|
| Project ID | `skb-music` |
| Display Name | SKB Music |
| Package | `com.skb.music` |
| Type | Android App (Kotlin + Compose + Room) |
| Domain | Media (local music player) |
| versionName | `2.2.0` ✅ |
| versionCode | `3` |
| Status | **Active** |
| Author | SKB Dev |
| GitHub | [skbsakib500/SKB-Music](https://github.com/skbsakib500/SKB-Music) |
| Branch | `main` |
| Kotlin files | 36 |

## What Is This

SKB-Music is a **privacy-first local music player** for Android.

- Media3 ExoPlayer playback
- Room database (library, playlists, favorites, recents)
- MVVM (viewmodel / domain / data layers)
- Dynamic color from album art (Palette)
- Lyrics (.lrc + embedded)
- ReplayGain, spectrum visualizer
- 10-band EQ, bass boost, virtualizer
- No internet. No tracking. No cloud.

## Position In Ecosystem

    SKB-Music (this)          ← audio player
    SKB-Player                ← video player (sibling)
    Mimi-Android              ← Life OS delivery
    Agent-Mimi                ← Life OS brain
    Mimi-Forever              ← backup vault

## Tech Stack

| Layer | Tech |
|-------|------|
| Language | Kotlin 1.9.x |
| UI | Compose BOM 2024.09.02 + Material3 |
| Media | Media3 ExoPlayer 1.4.1 |
| Storage | Room 2.6.1 (KSP) + DataStore 1.1.1 |
| Image | Coil 2.7.0 + palette-ktx |
| Nav | Navigation Compose 2.8.0 |
| Build | AGP 8.5.2, JDK 17 |
| SDK | min 24, target 34 |

_Last updated: 2026-09-16_
