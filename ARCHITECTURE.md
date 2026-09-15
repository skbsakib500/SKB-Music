# Architecture — SKB-Music

## Layers (Clean MVVM)

    UI (Compose)
      SkbApp → screens/ (8) + components/ (8) + theme/
         │
         ▼
    ViewModel (viewmodel/)
      PlayerViewModel · LibraryViewModel
         │
         ▼
    Data (data/)
      MusicRepository (single source of truth)
      Song · MediaStoreScanner · MediaItemMapper
      db/ → Entities · Daos · SkbDatabase (Room)
         │
         ▼
    Playback (playback/)
      PlaybackService (MediaSessionService)
      ReplayGainManager (session-aware)
         │
         ▼
    Audio (equalizer/)
      AudioEffectsManager (EQ · bass · virtualizer)
      EqPreferences (DataStore)

## Data Flow — Scan

    App → LibraryViewModel → MusicRepository
        → MediaStoreScanner.scan()
        → Room.insertAll
        → Flow<List<Song>> → ViewModel → UI

## Data Flow — Play

    Tap SongRow → PlayerViewModel.play(song)
        → Repository.getMediaItem
        → PlaybackService.setMediaItem + play
        → ExoPlayer
        → MediaSession → notification + lock screen

## Storage

| Data | Where |
|------|-------|
| Songs/Albums/Artists | Room |
| Playlists/Favorites/Recents | Room |
| EQ settings | DataStore |
| Album art | Coil cache |
| Lyrics | .lrc + embedded |

## Known Architectural Debt

    1. Empty folders: domain/ library/ playlist/ lyrics/ docs/
    2. No DI framework
    3. ReplayGain uses reflection for session ID
    4. No tests
