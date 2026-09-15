# Architecture — SKB-Music

## Layer Diagram (Clean MVVM)

    ┌────────────────────────────────────────────────────────┐
    │  UI LAYER (Compose)                                    │
    │  ────────────────────                                  │
    │  SkbApp (nav host)                                     │
    │  ├── ui/screens/                                       │
    │  │     LibraryScreen · FavoritesScreen                 │
    │  │     PlaylistsScreen · NowPlayingScreen              │
    │  │     RecentsScreen · BrowseScreen                    │
    │  │     EqualizerSheet · StatsScreen                    │
    │  ├── ui/components/                                    │
    │  │     SongRow · SongMenuSheet · SongInfoDialog        │
    │  │     SleepTimerSheet · SpeedSheet · LyricsSheet      │
    │  │     SpectrumVisualizer · PaletteUtil                │
    │  └── ui/theme/                                         │
    │        Theme.kt                                        │
    └──────────────────────┬─────────────────────────────────┘
                           │
    ┌──────────────────────▼─────────────────────────────────┐
    │  VIEWMODEL LAYER                                       │
    │  ───────────────                                       │
    │  viewmodel/                                            │
    │    PlayerViewModel · LibraryViewModel (etc.)           │
    │  - Exposes StateFlow                                   │
    │  - Mediates between UI and repository                  │
    └──────────────────────┬─────────────────────────────────┘
                           │
    ┌──────────────────────▼─────────────────────────────────┐
    │  DATA LAYER (Repository)                               │
    │  ──────────────────────                                │
    │  data/                                                 │
    │    MusicRepository      single source of truth         │
    │    Song                 domain model                   │
    │    MediaStoreScanner    scan device audio              │
    │    MediaItemMapper      Song ↔ MediaItem               │
    │    db/                                                 │
    │      Entities           Room @Entity                   │
    │      Daos               Room @Dao                      │
    │      SkbDatabase        @Database                      │
    └──────────────────────┬─────────────────────────────────┘
                           │
    ┌──────────────────────▼─────────────────────────────────┐
    │  PLAYBACK LAYER                                        │
    │  ──────────────                                        │
    │  playback/                                             │
    │    PlaybackService      MediaSessionService            │
    │    ReplayGainManager    normalization (session-aware)  │
    └──────────────────────┬─────────────────────────────────┘
                           │
    ┌──────────────────────▼─────────────────────────────────┐
    │  AUDIO EFFECTS                                         │
    │  ──────────────                                        │
    │  equalizer/                                            │
    │    AudioEffectsManager  EQ, bass, virtualizer          │
    │    EqPreferences        DataStore persistence          │
    └────────────────────────────────────────────────────────┘

## Directory Map

    SKB-Music/
    ├── .github/workflows/build.yml    CI: build debug APK
    ├── docs/                          ← (empty — reserved)
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/skb/music/
    │       ├── core/Constants.kt
    │       ├── data/
    │       │   ├── MusicRepository.kt
    │       │   ├── Song.kt
    │       │   ├── MediaStoreScanner.kt
    │       │   ├── MediaItemMapper.kt
    │       │   └── db/
    │       │       ├── Entities.kt
    │       │       ├── Daos.kt
    │       │       └── SkbDatabase.kt
    │       ├── domain/                (use cases / models)
    │       ├── library/               (library helpers)
    │       ├── playlist/              (playlist logic)
    │       ├── lyrics/                (LRC parser / loader)
    │       ├── viewmodel/             (MVVM view models)
    │       ├── playback/
    │       │   ├── PlaybackService.kt
    │       │   └── ReplayGainManager.kt
    │       ├── equalizer/
    │       │   ├── AudioEffectsManager.kt
    │       │   └── EqPreferences.kt
    │       └── ui/
    │           ├── SkbApp.kt
    │           ├── theme/Theme.kt
    │           ├── screens/           (8 screens)
    │           └── components/        (8 components)
    └── gradle.properties

## Data Flow — Scan Library

    App start → SkbApp
        │
        ▼
    LibraryViewModel
        │
        ▼
    MusicRepository
        │
        ├── MediaStoreScanner.scan()      → query MediaStore.Audio
        │
        ▼
    Map to Song → DAO.insertAll()
        │
        ▼
    Room Flow<List<Song>> ──► ViewModel ──► UI

## Data Flow — Play a Song

    User taps SongRow in LibraryScreen
        │
        ▼
    PlayerViewModel.play(song)
        │
        ▼
    MusicRepository.getMediaItem(song)
        │
        ▼
    MediaItemMapper.toMediaItem()
        │
        ▼
    PlaybackService.setMediaItem() + prepare() + play()
        │
        ▼
    ExoPlayer plays audio
        │
        ▼
    MediaSession → notification controls + lock screen
        │
        ▼
    NowPlayingScreen observes PlayerViewModel StateFlow

## Audio Effects Chain

    ExoPlayer audio session
        │
        ├── Equalizer (10-band) ─┐
        ├── BassBoost           ─┼── AudioEffectsManager
        ├── Virtualizer         ─┤   (session-aware)
        └── LoudnessEnhancer    ─┘
                                  │
                                  ▼
                        EqPreferences (DataStore)
                        persists per-user settings

## ReplayGain

    ReplayGainManager
        │
        ├── Reads RG tags from media metadata
        ├── Applies volume adjustment to ExoPlayer
        ├── Session-aware: refreshes when audio session changes
        └── Referenced from PlayerViewModel

## Storage

| Data | Where |
|------|-------|
| Songs / Albums / Artists | Room (`SkbDatabase`) |
| Playlists | Room |
| Favorites | Room |
| Recents | Room |
| EQ settings | DataStore (`EqPreferences`) |
| Other prefs | DataStore Preferences |
| Album art | Coil (memory + disk cache) |
| Lyrics | .lrc files + embedded ID3 |

## Threading

- UI: Compose (main thread)
- ViewModels: `viewModelScope` coroutines
- Repository: suspend functions on `Dispatchers.IO`
- Room: `Flow` for reactive queries
- MediaStore scan: `Dispatchers.IO`

## Known Architectural Debt

    1. `docs/` folder is empty — reserved for deep-dives
    2. No DI framework (Hilt/Koin) — manual wiring
    3. `domain/`, `library/`, `playlist/`, `lyrics/`
       folders exist but empty/underused (see folder listing)
    4. Two paths for prefs (Room vs DataStore) — clear
       but needs documentation
    5. ReplayGain relies on session ID reflection (see
       commit f20889a) — fragile across Media3 versions
    6. No tests
