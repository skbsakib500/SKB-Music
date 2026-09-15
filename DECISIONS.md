# Decisions — SKB-Music

## D-001 · Kotlin + Jetpack Compose

**Context:** Modern Android audio player, one developer.

**Decision:** Kotlin + Compose (BOM 2024.09.02) + Material3.

**Why:**
- Declarative UI = fast iteration
- Kotlin coroutines/Flow for reactive audio state
- Material3 + dynamic color ready

**Rejected:**
- Java + XML — verbose
- Flutter — separate toolchain

**Status:** Accepted · 2026-09-15

---

## D-002 · Media3 ExoPlayer

**Context:** Modern playback engine.

**Decision:** AndroidX Media3 1.4.1 + MediaSession.

**Why:**
- AndroidX maintained
- MediaSession integration for notifications/lock-screen
- Stable, well-documented

**Rejected:**
- MediaPlayer — no MediaSession
- ExoPlayer 2.x — deprecated
- VLC — heavy

**Status:** Accepted · 2026-09-15

---

## D-003 · Room (not SQLite raw, not JSON)

**Context:** Library, playlists, favorites, recents.

**Decision:** Room 2.6.1 with KSP.

**Why:**
- Type-safe queries
- `Flow<T>` for reactive UI
- Compile-time verification
- Easy migrations

**Rejected:**
- Raw SQLite — error-prone
- JSON files — no queries
- Realm — heavier

**Status:** Accepted · 2026-09-15

---

## D-004 · DataStore for preferences

**Context:** EQ settings, playback prefs.

**Decision:** DataStore Preferences 1.1.1.

**Why:**
- Coroutine-first
- Type-safe
- Replaces SharedPreferences
- Flow-based observation

**Rejected:**
- SharedPreferences — sync, no Flow
- Room — overkill for key-value

**Status:** Accepted · 2026-09-15

---

## D-005 · MVVM (not MVI, not MVC)

**Context:** Complex state (player, library, EQ).

**Decision:** ViewModel + Repository + Room.

**Why:**
- Standard Android architecture
- Testable (viewmodel independent of UI)
- Survives config changes
- Compose-native (state via StateFlow)

**Rejected:**
- MVI — overkill for this size
- MVC — no separation on Android

**Status:** Accepted · 2026-09-15

---

## D-006 · Palette for dynamic theming

**Context:** Want Now Playing to feel alive, tied to album.

**Decision:** androidx.palette:palette-ktx.

**Why:**
- Extract colors from album art
- Gradient background matches song
- No external service

**Rejected:**
- Fixed theme — boring
- External API — offline app

**Status:** Accepted · 2026-09-15

---

## D-007 · ReplayGain via session ID

**Context:** Normalize volume across tracks.

**Decision:** Read RG tags, apply via audio session.

**Why:**
- Standard for volume normalization
- Session-aware (safe with Media3)

**Trade-off:**
- Uses reflection on Media3 session ID
- Fragile if Media3 internals change

**Status:** Accepted · 2026-09-15

---

## D-008 · Privacy-first (no internet)

**Context:** Music apps often phone home.

**Decision:** No network permissions, no tracking.

**Status:** Accepted · 2026-09-15

---

## D-009 · SKB Project Memory (this release)

**Context:** 9 commits, no docs, empty `docs/` folder.

**Decision:** Same memory pattern as other SKB projects.

**Status:** Accepted · 2026-09-16

---

## Open Questions

- Add Hilt/Koin DI?
- Fill or delete `docs/`?
- Wire `domain/`, `library/`, `playlist/`, `lyrics/`?
