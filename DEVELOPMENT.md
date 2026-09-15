# Development Guide — SKB-Music

## Prerequisites

- JDK 17+
- Android SDK 34
- Gradle 8.7 (wrapper recommended)
- Device or emulator (API 24+)

## Build

    ./gradlew :app:assembleDebug
    # → app/build/outputs/apk/debug/app-debug.apk

    ./gradlew :app:assembleRelease
    ./gradlew clean

## Install

    adb install -r app/build/outputs/apk/debug/app-debug.apk

## Project Layout

    app/src/main/java/com/skb/music/
    ├── core/           Constants
    ├── data/           Repository + Song model + Room
    │   └── db/         Entities · Daos · SkbDatabase
    ├── domain/         (reserved)
    ├── library/        (reserved)
    ├── playlist/       (reserved)
    ├── lyrics/         (reserved)
    ├── viewmodel/      MVVM ViewModels
    ├── playback/       PlaybackService + ReplayGain
    ├── equalizer/      AudioEffects + DataStore prefs
    └── ui/
        ├── SkbApp.kt
        ├── theme/
        ├── screens/    8 screens
        └── components/ 8 components

## Adding a Screen

1. `ui/screens/YourScreen.kt` as `@Composable`
2. Add route in `SkbApp.kt` NavHost
3. Add a ViewModel in `viewmodel/`
4. ViewModel talks to `MusicRepository`

## Adding a Room Entity

1. Add `@Entity` class in `data/db/Entities.kt`
2. Add `@Dao` in `data/db/Daos.kt`
3. Bump `@Database(version = N)` in `SkbDatabase.kt`
4. Add migration OR `fallbackToDestructiveMigration()`
5. Expose Flow from DAO → Repository → ViewModel

## Adding a Preference

1. Add key to `EqPreferences` (or new prefs file)
2. Use DataStore `edit { }`
3. Expose Flow
4. Observe in ViewModel

## Audio Effects

Session-aware — see `AudioEffectsManager.kt`.
Always attach to ExoPlayer's `audioSessionId`.
Persist via `EqPreferences`.

## ReplayGain

`ReplayGainManager.kt` — reads RG tags, applies volume.
Referenced from `PlayerViewModel`.
⚠️ Uses reflection for session ID (see KI-M04).

## Commit Convention

    <type>: <summary>

Types: feat, fix, docs, chore, style, refactor, release

Historical style: `feat(v2.2): <description>`

## Release Checklist

- [ ] Bump versionName + versionCode in `app/build.gradle.kts`
- [ ] Update CHANGELOG.md
- [ ] Update ROADMAP.md
- [ ] Update `.skb/state.json`
- [ ] `git commit -m "release: vX.Y.Z"`
- [ ] `git tag vX.Y.Z && git push --tags`
- [ ] GitHub Actions builds APK
- [ ] Archive to `~/Mimi-Forever/backups/`

## Git Safety

**NEVER embed credentials in remote URLs.**

Use:
    gh auth login
    git remote set-url origin https://github.com/USER/REPO.git

Never:
    git remote set-url origin https://TOKEN@github.com/...
    force push
    reset --hard on pushed branches
    delete remote branches
    rewrite published history

## Known Pain Points

1. No DI (KI-M05) — manual wiring
2. No tests (KI-M06)
3. ReplayGain reflection (KI-M04)
4. Empty folders confusion (KI-M01)

## Reference

- Repo: https://github.com/skbsakib500/SKB-Music
- Sibling: `~/SKB-Player`
- Ecosystem: `~/Agent-Mimi`, `~/Mimi-Android`
