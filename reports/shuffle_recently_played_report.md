# Shuffle and Recently Played Feature Report

## 1. Summary

This feature increment delivers Feature 3 (Shuffle Playlist) and Feature 4 (Recently Played Songs) along with the minimal playback-selection contract required for recent history recording in the terminal-based Music Playlist Engine.

Key achievements:
- Implemented `shufflePlaylist()` in `Playlist.java` using `Collections.shuffle` with reference-based preservation of the currently playing song and index tracking.
- Implemented `RecentlyPlayedManager.java` utilizing an `ArrayDeque<Song>` bounded at 10 items, adhering to a strict newest-first event-history model where repeated plays are preserved.
- Implemented the minimal playback contract in `Playlist.java` (`currentIndex`, `playSong(int songNumber)`, `getCurrentSong()`) without prematurely adding navigation methods (`playNext()`/`playPrevious()`).
- Updated `Playlist.displayPlaylist()` to indicate `  <-- Now Playing` without mutating playlist ordering, playback indices, or recent history.
- Integrated Menu Options 4 ("Play a Song"), 5 ("Shuffle Playlist"), and 6 ("View Recently Played") into `Main.java` while preserving all existing online MusicBrainz search and local playlist functionality.
- Maintained complete separation of responsibilities: `Playlist` manages song collection and playback positioning; `RecentlyPlayedManager` manages event history; `Main` coordinates user interactions.
- Zero extra dependencies, frameworks, databases, or deferred teammate features introduced.

---

## 2. Base Commit and Branch

- **Base Commit:** `7ff391d384450ddee58f7303aa47091fc7db95be`
- **Feature Branch:** `feature/shuffle-recently-played`
- **Target Repository:** `origin`

---

## 3. Files Created

1. `src/main/java/org/example/RecentlyPlayedManager.java`
   - Encapsulates bounded event-based song history tracking.
   - Manages FIFO eviction when capacity exceeds 10 items.
   - Provides unmodifiable snapshot lists and formatted terminal output.
2. `reports/shuffle_recently_played_report.md`
   - Comprehensive technical documentation and verification report.

---

## 4. Files Modified

1. `src/main/java/org/example/Playlist.java`
   - Added `currentIndex` state variable initialized to `-1`.
   - Added `playSong(int songNumber)` supporting 1-based user input and 0-based internal indexing.
   - Added `getCurrentSong()` and `getCurrentIndex()` accessors.
   - Added `shufflePlaylist()` with object-reference preservation of the active song.
   - Enhanced `displayPlaylist()` to append `  <-- Now Playing` for the active song.
2. `src/main/java/org/example/Main.java`
   - Initialized `RecentlyPlayedManager` with capacity 10.
   - Added menu options 4, 5, and 6 to terminal interface.
   - Added `handlePlaySong` coordinator method.
   - Added `handleShufflePlaylist` coordinator method.
   - Delegated Option 6 directly to `recentlyPlayedManager.displayRecentlyPlayed()`.

---

## 5. Shuffle Implementation

- **Method Signature:** `public boolean shufflePlaylist()` in `Playlist.java`.
- **Empty and One-Song Behavior:**
  - **Zero songs:** Does not throw an exception; returns `false`. `Main` displays: `"Cannot shuffle an empty playlist."`
  - **One song:** Leaves the list unchanged; returns `false`. `Main` displays: `"At least two songs are required to shuffle."`
- **Collections.shuffle Usage:**
  - When the playlist contains two or more songs, `java.util.Collections.shuffle(songs)` randomly reorders the internal `songs` list.
  - Songs are neither added, removed, nor duplicated.
- **Current-Song Preservation:**
  - Before shuffling, the method captures `Song currentSong = getCurrentSong();`.
  - After `Collections.shuffle(songs)`, if `currentSong != null`, it iterates through `songs` using reference equality (`songs.get(i) == currentSong`) to locate the exact same `Song` instance in its new position and sets `currentIndex = i;`.
  - Reference equality (`==`) is explicitly used instead of `equals()` so duplicate song metadata across distinct tracks is handled safely.
  - If no song was playing (`currentIndex == -1`), `currentIndex` remains `-1`.
- **Effect on History:**
  - Shuffling does not record a song in `RecentlyPlayedManager`. Shuffling is purely an internal playlist reordering operation.
- **Post-Shuffle Main Feedback:**
  - `Main` displays `"Playlist shuffled successfully."` and calls `playlist.displayPlaylist()`, displaying the new ordering and the `  <-- Now Playing` marker on the current song.

---

## 6. Playback Contract

- **`currentIndex`:**
  - Private integer field in `Playlist.java`, initialized to `-1` (indicating no song currently selected).
  - Tracks the zero-based index of the currently active song.
- **`playSong(int songNumber)`:**
  - Accepts user-facing 1-based `songNumber`.
  - Converts using `int index = songNumber - 1;`.
  - Guard checks:
    - If `songs.isEmpty()`, returns `null`.
    - If `songNumber < 1` (`index < 0`), returns `null`.
    - If `songNumber > songs.size()` (`index >= songs.size()`), returns `null`.
  - On valid input:
    - Updates `this.currentIndex = index;`.
    - Returns `songs.get(index);`.
  - Does not print to console.
  - Does not mutate `RecentlyPlayedManager`.
- **`getCurrentSong()`:**
  - Returns `songs.get(currentIndex)` if `currentIndex >= 0 && currentIndex < songs.size()`; otherwise returns `null`.
- **When a Song Counts as Played:**
  - A song counts as played **only** when a valid playback action succeeds in `Main` (Option 4).
  - `Main` inspects the return value of `playlist.playSong(songNumber)`.
  - If non-null, `Main` prints `"Now Playing: " + playedSong` and calls `recentlyPlayedManager.recordPlay(playedSong)`.
  - Merely viewing the playlist, adding/importing a track, performing an online search, or shuffling does **not** trigger playback recording.

---

## 7. Recently Played Implementation

- **`RecentlyPlayedManager`:**
  - Dedicated class encapsulating the history of played tracks.
  - Decoupled from `Playlist.java` and `Main.java`.
- **`ArrayDeque` Implementation:**
  - Uses `private final Deque<Song> recentlyPlayed = new ArrayDeque<>();`.
  - Provides efficient $O(1)$ operations for adding to the head (`addFirst`) and removing from the tail (`removeLast`).
- **Maximum History Limit (10):**
  - Constructor enforces `maximumHistorySize > 0`, throwing `IllegalArgumentException("Maximum history size must be greater than 0.")` on zero or negative values.
  - In `Main.java`, instantiated with limit 10: `new RecentlyPlayedManager(10)`.
- **Newest-First Ordering:**
  - `recordPlay(Song song)` calls `recentlyPlayed.addFirst(song)`.
  - If `recentlyPlayed.size() > maximumHistorySize`, the oldest entry is evicted using `recentlyPlayed.removeLast()`.
- **Repeated-Play Policy:**
  - Recently Played is implemented as an event log rather than a set of unique tracks.
  - If the same song is played multiple times, each playback event is recorded as a distinct entry in the history.
- **Immutable History Access:**
  - `getRecentlyPlayed()` returns `Collections.unmodifiableList(new ArrayList<>(recentlyPlayed))`.
  - Calling mutation methods (`add`, `remove`, `clear`) on the returned list throws `UnsupportedOperationException`.
- **Null Safety:**
  - `recordPlay(null)` immediately returns without modifying state.
- **Formatted Display:**
  - `displayRecentlyPlayed()` outputs:
    ```text
    ===== RECENTLY PLAYED =====
    1. Perfect - Ed Sheeran [4:23]
    2. Believer - Imagine Dragons [3:24]
    ===========================
    ```
  - When history is empty, outputs:
    ```text
    ===== RECENTLY PLAYED =====
    No songs have been played recently.
    ===========================
    ```

---

## 8. Menu Integration

`Main.java` menu layout:

```text
================================
      MUSIC PLAYLIST ENGINE     
================================
1. Search Song Online by Title
2. Search Song Online by Artist
3. View Local Playlist
4. Play a Song
5. Shuffle Playlist
6. View Recently Played
0. Exit
================================
```

Flows implemented:
- **Option 4 ("Play a Song"):**
  1. If playlist is empty, displays `"Playlist is empty."` and returns to menu.
  2. Displays the playlist with current indices and markers.
  3. Prompts: `"Enter song number to play: "`.
  4. Calls `Song playedSong = playlist.playSong(songNumber);`.
  5. If `playedSong == null`, displays `"Invalid song number."` without recording history.
  6. If valid, displays `"Now Playing: " + playedSong` and records via `recentlyPlayedManager.recordPlay(playedSong)`.
- **Option 5 ("Shuffle Playlist"):**
  1. If playlist is empty, invokes `playlist.shufflePlaylist()` and displays `"Cannot shuffle an empty playlist."`.
  2. If playlist has 1 song, invokes `playlist.shufflePlaylist()` and displays `"At least two songs are required to shuffle."`.
  3. If 2+ songs, invokes `playlist.shufflePlaylist()`, displays `"Playlist shuffled successfully."`, and displays the shuffled playlist.
  4. History is not updated.
- **Option 6 ("View Recently Played"):**
  1. Calls `recentlyPlayedManager.displayRecentlyPlayed();`.

---

## 9. Verification Results

| Test | Status | Evidence |
|---|---|---|
| 1. Shuffle an empty playlist | **PASS** | `shufflePlaylist()` returned `false` on empty playlist, no exception thrown. |
| 2. Shuffle a one-song playlist | **PASS** | Returned `false`, 1-song playlist left unchanged with original song. |
| 3. Shuffle a playlist with four songs | **PASS** | Returned `true` on 4-song playlist, reordering songs. |
| 4. Confirm size is unchanged after shuffle | **PASS** | Size remained exactly 4 before and after shuffle. |
| 5. Confirm no Song object is lost | **PASS** | All 4 original Song references present in playlist post-shuffle. |
| 6. Confirm no Song object is duplicated | **PASS** | Unique song count remained 4 distinct references post-shuffle. |
| 7. Play a song and then shuffle | **PASS** | Successfully played track 2 before executing shuffle. |
| 8. Confirm same Song object remains current after shuffle | **PASS** | `getCurrentSong()` returned exact same object reference after shuffle. |
| 9. Confirm currentIndex points to current Song's new location | **PASS** | `currentIndex` updated to match the active song's new index in `songs`. |
| 10. Confirm shuffle does not update recently played | **PASS** | `RecentlyPlayedManager.size()` remained 0 after multiple shuffles. |
| 11. Play song number 1 | **PASS** | `playSong(1)` returned first song, `currentIndex` set to 0. |
| 12. Play the final song number | **PASS** | `playSong(2)` returned last song in 2-song playlist, `currentIndex` set to 1. |
| 13. Attempt song number 0 | **PASS** | `playSong(0)` returned `null`, state protected. |
| 14. Attempt a negative song number | **PASS** | `playSong(-3)` returned `null`, state protected. |
| 15. Attempt a number greater than playlist size | **PASS** | `playSong(3)` on 2-song playlist returned `null`, state protected. |
| 16. Attempt play on an empty playlist | **PASS** | `playSong(1)` on empty playlist returned `null`, `currentIndex` remained -1. |
| 17. Confirm invalid playback does not update history | **PASS** | Out-of-bounds play attempt left `RecentlyPlayedManager` size at 0. |
| 18. Confirm history starts empty | **PASS** | `isEmpty()` is `true`, `size()` is 0, `getRecentlyPlayed()` is empty. |
| 19. Play one song and confirm it becomes history item 1 | **PASS** | Single played song recorded at index 0 of recent history. |
| 20. Play three different songs and confirm newest-first order | **PASS** | Songs [A, B, C] recorded in order yielded history [C, B, A]. |
| 21. Play same song repeatedly and confirm each play is stored | **PASS** | Playing track 3 times produced 3 distinct entries in history. |
| 22. Record more than 10 plays and confirm history remains size 10 | **PASS** | 15 plays recorded; `size()` capped strictly at 10. |
| 23. Confirm oldest event removed when limit exceeded | **PASS** | Recorded Tracks 1-15; Tracks 1-5 evicted, newest is Track 15, oldest retained is Track 6. |
| 24. Confirm recordPlay(null) does nothing | **PASS** | `recordPlay(null)` ignored, history size unchanged. |
| 25. Confirm getRecentlyPlayed() cannot mutate internal history | **PASS** | Calling `.add()` on snapshot threw `UnsupportedOperationException`. |
| 26. Confirm viewing history does not mutate it | **PASS** | Invoking `displayRecentlyPlayed()` left history size and contents identical. |
| 27. Confirm adding/importing a song does not update history | **PASS** | `playlist.addSong(...)` executed; `RecentlyPlayedManager` remained empty. |
| 28. Confirm online search does not update history | **PASS** | Online query decoupled from `RecentlyPlayedManager`; history remained empty. |
| 29. Four demo songs remain available | **PASS** | Demo songs "Perfect", "Shape of You", "Believer", "Blinding Lights" verified. |
| 30. Online title search remains reachable | **PASS** | `MusicAPI.searchSongs(keyword, 1)` callable and verified. |
| 31. Online artist search remains reachable | **PASS** | `MusicAPI.searchSongs(keyword, 2)` callable and verified. |
| 32. Imported online results can still be added when MusicBrainz responds | **BLOCKED** | Live MusicBrainz query returned HTTP 503 rate-limiting during testing; handled safely without crash or uncaught exception. |
| 33. Invalid menu input does not crash | **PASS** | Entering non-numeric text (`"not_a_number"`) safely re-prompted via `InputHelper.readInt`. |
| 34. Local playlist display works | **PASS** | Numbered list formatted properly with `  <-- Now Playing` marker. |
| 35. No add/remove implementation was introduced | **PASS** | Verified via reflection that no remove methods exist in `Playlist`. |
| 36. No next/previous implementation was introduced | **PASS** | Verified via reflection that no `playNext`/`playPrevious` methods exist in `Playlist`. |
| 37. No local search implementation was introduced | **PASS** | Verified via reflection that no local search methods exist in `Playlist`. |
| 38. No sorting implementation was introduced | **PASS** | Verified via reflection that no sorting methods exist in `Playlist`. |
| 39. No unrelated dependency or technology was introduced | **PASS** | `pom.xml` inspected; only Google Gson 2.11.0 present. |
| 40. No generated build artifacts remain in Git changes | **PASS** | `git status` clean of class files, `target/` ignored. |

### Verification Summary
- **PASS:** 39
- **FAIL:** 0
- **BLOCKED:** 1 (Test 32 due to external upstream MusicBrainz HTTP 503 rate limiting; verified handled gracefully)
- **NOT TESTED:** 0

---

## 10. Teammate Integration Contract

### A. Add and Remove Songs Teammate Contract

1. **Existing Method Preservation:**
   - `public boolean addSong(Song song)` in `Playlist.java` must be preserved.
   - Appending a song must not alter `currentIndex` and must not record to `RecentlyPlayedManager`.
2. **Future Song Removal Rules:**
   - When implementing song removal (e.g., `removeSong(int songNumber)` or `removeSong(Song song)`):
     - If the removed song's index is strictly less than `currentIndex`, `currentIndex` must be decremented by 1 (`currentIndex--`).
     - If the removed song is the currently playing song (`index == currentIndex`):
       - If the playlist is now empty, set `currentIndex = -1`.
       - If songs remain, ensure `currentIndex` points to a valid song (e.g. clamp `currentIndex` to `songs.size() - 1`).
     - If the removed song's index is greater than `currentIndex`, `currentIndex` remains unchanged.
     - Removing a song from `Playlist` must **never** delete or alter past events in `RecentlyPlayedManager`. History is an immutable audit log of playback events.

### B. Next and Previous Navigation Teammate Contract

1. **Required Method Signatures:**
   ```java
   public Song playNext()
   public Song playPrevious()
   ```
2. **State and Behavioral Rules:**
   - Both methods must utilize the existing `currentIndex` field in `Playlist.java`. Do not declare a duplicate index variable.
   - Both methods must return `null` if `songs.isEmpty()`.
   - Circular wrapping behavior:
     - `playNext()`: Advances index with wrapping: `currentIndex = (currentIndex + 1) % songs.size()`. If `currentIndex == -1`, it should start at `0`.
     - `playPrevious()`: Decrements index with wrapping: `currentIndex = (currentIndex - 1 + songs.size()) % songs.size()`. If `currentIndex == -1`, it should start at `songs.size() - 1`.
   - Return the newly active `Song` (`songs.get(currentIndex)`).
   - Neither method should print to console or modify `RecentlyPlayedManager` directly inside `Playlist.java`.
3. **Main Coordination Rule:**
   - When invoking navigation from `Main.java`, record the played song into history only when non-null:
     ```java
     Song nextSong = playlist.playNext();
     if (nextSong != null) {
         recentlyPlayedManager.recordPlay(nextSong);
     }
     ```

---

## 11. Deferred Features

The following features were intentionally excluded from this increment and remain reserved for assigned teammates:
1. **Add and Remove Songs:** Manual removal of songs from local playlist.
2. **Next and Previous Navigation:** Sequential circular track navigation (`playNext()`, `playPrevious()`).
3. **Local Playlist Search:** Searching in-memory playlist tracks by title or artist.
4. **Sorting:** Ordering playlist tracks alphabetically or by duration.

---

## 12. Known Limitations

1. **MusicBrainz Upstream Rate Limiting (HTTP 503):** MusicBrainz limits unauthenticated client queries to 1 request per second. Rapid consecutive queries return HTTP 503. Handled gracefully by `MusicAPI` without crashing.
2. **Environment Maven Executable:** The local execution environment does not map `mvn` to global PATH. The project compiles cleanly using the available Apache Maven 3.9.16 executable (`mvn.sh`) or standard `javac` with the Gson library on classpath.

---

## 13. Files Changed

| File | Status | Description |
|---|---|---|
| `src/main/java/org/example/Playlist.java` | Modified | Added shuffle, minimal playback contract, and current song display marker. |
| `src/main/java/org/example/Main.java` | Modified | Added options 4 (Play), 5 (Shuffle), 6 (Recently Played) and handler methods. |
| `src/main/java/org/example/RecentlyPlayedManager.java` | Created | Bounded Deque-based recently played history manager. |
| `reports/shuffle_recently_played_report.md` | Created | Technical architecture and verification report. |

---

## 14. Final Verdict

### **READY FOR MAINTAINER REVIEW**

All assigned requirements for Feature 3 (Shuffle Playlist), Feature 4 (Recently Played Songs), and the minimal playback contract have been implemented cleanly, thoroughly verified with zero regressions, and documented with strict integration boundaries for teammates.
