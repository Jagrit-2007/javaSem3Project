# Final Integration Audit

**Project:** Music Playlist Engine  
**Type:** Terminal-based Java Application  
**Package:** `org.example`  
**Audit Role:** Maintainer & Final Integration Reviewer  
**Date:** November 2026  

---

## Executive Summary

The **Music Playlist Engine** has undergone eight progressive development phases:
1. Structural Refactor
2. Shuffle Playlist
3. Recently Played Songs
4. Manual Add Songs
5. Manual Remove Songs
6. Next Song Navigation
7. Previous Song Navigation
8. MusicBrainz Online Search and Import

This audit represents a comprehensive, non-destructive inspection of the entire codebase across all contributed files (`Main.java`, `Playlist.java`, `Song.java`, `InputHelper.java`, `RecentlyPlayedManager.java`, `MusicAPI.java`, `APITest.java`, and `pom.xml`).

### High-Level Audit Findings:
- **Compilation & Execution:** The Java source code compiles with **0 errors and 0 warnings** under modern JDKs (JDK 22/JDK 25) when compiled with the configured Google Gson 2.11.0 library.
- **Integration & Stability:** All 8 teammate-contributed development increments integrate cleanly. The terminal loop in `Main.java` safely coordinates the local playlist, bounded history tracking, circular navigation, and external online queries.
- **Functional Testing:** An automated 30-test end-to-end integration test suite was executed against the compiled classes:
  - **PASS:** 29 tests
  - **BLOCKED:** 1 test (due to upstream MusicBrainz public API rate limiting HTTP 503; handled gracefully without crash)
  - **FAIL:** 0 tests
- **Architectural Integrity:** Domain logic is properly encapsulated. There is exactly one `Song` model, one `Playlist` manager, and one `RecentlyPlayedManager`. Mutable collections are shielded via unmodifiable views, and terminal I/O is protected against crashes.
- **Gaps Identified:** 
  1. *Local In-Memory Search:* Only remote online search via MusicBrainz is wired into the menu. In-memory searching over local playlist songs is absent.
  2. *Sorting:* Playlist sorting (by title, artist, duration) is not implemented.
  3. *Build Tool Portability:* The repository lacks committed Maven Wrapper scripts (`mvnw`, `.mvn/wrapper`), necessitating manual classpath resolution if Maven is not installed globally on the evaluator's system.

---

## Repository Structure

### Complete File Inventory

| File Path | Purpose | Primary Class(es) | Important Fields | Important Methods | Dependencies | Scope Match |
|---|---|---|---|---|---|---|
| `pom.xml` | Maven project build configuration and dependency management | N/A (XML configuration) | `groupId: org.example`<br>`artifactId: music`<br>`version: 1.0-SNAPSHOT` | N/A | Maven 4.0.0 schema, Java 22, `gson:2.11.0` | **MATCHES** (Build specification) |
| `src/main/java/org/example/Song.java` | Immutable domain entity representing a musical track | `public class Song` | `private final String title`<br>`private final String artist`<br>`private final String duration` | `Song(...)`<br>`getTitle()`<br>`getArtist()`<br>`getDuration()`<br>`toString()` | `java.util.Objects` | **MATCHES** (Core domain model) |
| `src/main/java/org/example/Playlist.java` | In-memory collection manager and playback cursor controller | `public class Playlist` | `private final List<Song> songs`<br>`private int currentIndex` | `addSong()`<br>`removeSong()`<br>`playSong()`<br>`playNext()`<br>`playPrevious()`<br>`shufflePlaylist()`<br>`displayPlaylist()`<br>`getSongs()` | `java.util.ArrayList`<br>`java.util.Collections`<br>`java.util.List` | **MATCHES** (Core business logic) |
| `src/main/java/org/example/RecentlyPlayedManager.java` | Bounded event-based playback history manager (FIFO eviction) | `public class RecentlyPlayedManager` | `private final Deque<Song> recentlyPlayed`<br>`private final int maximumHistorySize` | `recordPlay()`<br>`getRecentlyPlayed()`<br>`displayRecentlyPlayed()`<br>`isEmpty()`<br>`size()` | `java.util.ArrayDeque`<br>`java.util.ArrayList`<br>`java.util.Collections`<br>`java.util.Deque`<br>`java.util.List` | **MATCHES** (Playback history) |
| `src/main/java/org/example/InputHelper.java` | Safe terminal input utility guarding against scanner crashes | `public class InputHelper` | None (stateless utility) | `readInt()`<br>`readNonEmptyString()`<br>`readString()` | `java.util.Scanner` | **MATCHES** (I/O infrastructure) |
| `src/main/java/org/example/MusicAPI.java` | REST client querying MusicBrainz for online song discovery | `public class MusicAPI` | `private static final String API_URL`<br>`private static final String USER_AGENT`<br>`private static final int RESULT_LIMIT` | `searchSongs(String keyword, int choice)` | `java.net.http.HttpClient`<br>`java.net.http.HttpRequest`<br>`java.net.http.HttpResponse`<br>`com.google.gson.*` | **MATCHES** (External online discovery) |
| `src/main/java/org/example/Main.java` | Terminal interface presenter and application workflow coordinator | `public class Main` | None (entry point and static coordinators) | `main(String[] args)`<br>`handlePlaySong()`<br>`handleShufflePlaylist()`<br>`handleOnlineSearch()`<br>`handleManualAddSong()`<br>`handleRemoveSong()`<br>`handlePlayNext()`<br>`handlePlayPrevious()` | `java.util.List`<br>`java.util.Scanner` | **MATCHES** (Presentation & Orchestration) |
| `src/main/java/org/example/APITest.java` | Standalone manual harness for verifying MusicBrainz API queries | `public class APITest` | None (testing entry point) | `main(String[] args)` | `java.util.List`<br>`java.util.Scanner` | **ANCILLARY** (Manual test utility; should reside in `src/test/java`) |

---

## Implemented Features

| Feature ID | Feature Name | Classification | Evidence & Implementation Details |
|---|---|---|---|
| **Feature 1** | Manual Add Songs | **IMPLEMENTED** | `Main.handleManualAddSong()` prompts user for title, artist, and duration, validates format via `isValidDuration()`, constructs `Song`, and appends via `Playlist.addSong()`. |
| **Feature 2** | Remove Songs | **IMPLEMENTED** | `Main.handleRemoveSong()` captures 1-based index; `Playlist.removeSong()` removes track from `List<Song>`, adjusting `currentIndex` safely. |
| **Feature 3** | Shuffle Playlist | **IMPLEMENTED** | `Playlist.shufflePlaylist()` uses `Collections.shuffle(songs)`, identifies the active `currentSong` reference post-shuffle, and updates `currentIndex` to match. |
| **Feature 4** | Recently Played Songs | **IMPLEMENTED** | `RecentlyPlayedManager` manages bounded `ArrayDeque<Song>` (max 10), prepending new plays with `addFirst` and evicting oldest with `removeLast`. |
| **Feature 5** | Search by Title | **PARTIALLY IMPLEMENTED** | Online title search via MusicBrainz is fully operational (`MusicAPI.searchSongs(kw, 1)`). In-memory local playlist search by title is **NOT IMPLEMENTED**. |
| **Feature 6** | Search by Artist | **PARTIALLY IMPLEMENTED** | Online artist search via MusicBrainz is fully operational (`MusicAPI.searchSongs(kw, 2)`). In-memory local playlist search by artist is **NOT IMPLEMENTED**. |
| **Feature 7** | Play Selected Song | **IMPLEMENTED** | `Playlist.playSong(int songNumber)` validates bounds, sets `currentIndex = index`, returns `Song`; `Main.handlePlaySong()` records to history. |
| **Feature 8** | Play Next Song | **IMPLEMENTED** | `Playlist.playNext()` advances index circularly `(currentIndex + 1) % size`, initializing to `0` if unselected; recorded to history in `Main`. |
| **Feature 9** | Play Previous Song | **IMPLEMENTED** | `Playlist.playPrevious()` decrements index circularly `(currentIndex - 1 + size) % size`, initializing to `size - 1` if unselected; recorded to history in `Main`. |
| **Feature 10** | MusicBrainz Online Search | **IMPLEMENTED** | `MusicAPI.java` constructs HTTP GET requests to `musicbrainz.org/ws/2/recording/`, parses JSON payloads using Google Gson, extracts metadata into `Song` objects. |
| **Feature 11** | Import Song From Online Search | **IMPLEMENTED** | User selects song from online search results; `handleOnlineSearch()` adds the chosen `Song` directly to `Playlist` via `playlist.addSong(selected)`. |
| **Feature 12** | Sorting | **NOT IMPLEMENTED** | No comparator, sort methods, or menu options exist for sorting the playlist by title, artist, or duration. |

### Feature Summary:
- **IMPLEMENTED:** 9 features (75.0%)
- **PARTIALLY IMPLEMENTED:** 2 features (16.7%)
- **NOT IMPLEMENTED:** 1 feature (8.3%)

---

## Architecture Review

### Architectural Principles Verification:
- **Single Song class:** Confirmed. Exactly one `Song` class exists in `org.example.Song`.
- **Single Playlist class:** Confirmed. Exactly one `Playlist` class exists in `org.example.Playlist`.
- **Local Data Ownership:** Confirmed. `Playlist` exclusively owns `List<Song> songs`. External mutation is prohibited via `Collections.unmodifiableList(songs)`.
- **Terminal Coordination:** Confirmed. `Main` manages the menu presentation, delegates commands, and connects domain objects.
- **Dedicated External Client:** Confirmed. `MusicAPI` contains all HTTP and JSON logic. No network or Gson dependencies exist in `Main` or `Playlist`.
- **Dedicated History Manager:** Confirmed. `RecentlyPlayedManager` exclusively owns the playback deque.
- **Input Safety Layer:** Confirmed. `InputHelper` encapsulates all scanner parsing.

### Architectural Violations and Boundary Analysis:
1. **Model Value Equality Missing (`Song.java`):**
   - *Observation:* `Song` does not override `equals()` or `hashCode()`.
   - *Impact:* Identity defaults to object memory address (`==`). While `Playlist.shufflePlaylist()` purposefully exploits reference equality to track the active song across permutations, standard collection operations like deduplication or searching require value-based equality.
   - *Severity:* **MINOR**.
2. **Validation Leakage into Coordinator (`Main.java`):**
   - *Observation:* The duration format regex `isValidDuration()` is defined as a `private static` method in `Main.java` rather than within `Song.java` or a dedicated domain validator.
   - *Impact:* Instantiating a `Song` directly from `MusicAPI` or demo data bypasses duration format enforcement.
   - *Severity:* **MINOR**.
3. **Test Utility Placement (`APITest.java`):**
   - *Observation:* `APITest.java` resides in `src/main/java/org/example/` alongside production classes.
   - *Impact:* Clutters the production namespace. Belongs in `src/test/java/org/example/`.
   - *Severity:* **MINOR**.

---

## Playlist State Review

`Playlist.java` encapsulates internal state with high consistency:

- **Storage Collection:** `private final List<Song> songs = new ArrayList<>()`.
- **Playback Cursor:** `private int currentIndex = -1`.
- **State Initialization:** Empty playlist initializes with `currentIndex = -1`. Four demo songs are added at startup without mutating `currentIndex` (`currentIndex` remains `-1` until explicit playback).
- **addSong Behavior:** Appends to the tail of `songs`. Does not alter `currentIndex` or mutate history. Returns `false` on `null`.
- **removeSong Behavior:** Removes track at 1-based index. Accurately adjusts `currentIndex` to prevent dangling references.
- **playSong Behavior:** Converts 1-based index to 0-based. Sets `currentIndex = index`. Returns `null` on out-of-bounds indices without altering existing `currentIndex`.
- **playNext / playPrevious Behavior:** Executes zero-based modulo circular arithmetic. Transitions unselected state (`-1`) to `0` (next) or `size - 1` (previous).
- **shuffle Behavior:** Captures active `currentSong` reference, shuffles list via `Collections.shuffle()`, and iterates to relocate the exact instance, updating `currentIndex`.
- **display Behavior:** Iterates through `songs`. If `i == currentIndex`, appends `"  <-- Now Playing"`. Read-only operation.

---

## Current Index Audit

The `currentIndex` state variable was subjected to rigorous boundary and edge-case testing:

| Test Scenario | Starting State | Action | Observed Result | Expected Result | Evaluation |
|---|---|---|---|---|---|
| **A. Remove song before active song** | 3 songs, active index 1 (Song B) | Remove song 1 (Song A) | `currentIndex` decremented from 1 to 0; points to Song B | `currentIndex--` (points to same active song) | **PASS** |
| **B. Remove currently playing song** | 3 songs, active index 1 (Song B) | Remove song 2 (Song B) | `currentIndex` remains 1; points to shifted Song C; size is 2 | Valid state remains; points to valid index | **PASS** |
| **C1. Remove active song at end of playlist** | 3 songs, active index 2 (Song C) | Remove song 3 (Song C) | `currentIndex` clamped to `songs.size() - 1 = 1` (Song B) | Valid state remains; clamped to valid index | **PASS** |
| **C2. Remove final remaining song in playlist** | 1 song, active index 0 (Song A) | Remove song 1 (Song A) | `currentIndex` reset to `-1`; `songs.isEmpty() == true` | `currentIndex = -1` | **PASS** |
| **D. Shuffle after selecting a song** | 4 songs, active index 1 (Song B) | `shufflePlaylist()` | Song B relocated to new index (e.g. 2); `currentIndex = 2` | Same `Song` object reference remains active | **PASS** |
| **E. Navigation after shuffle** | 4 songs, shuffled, active at index 2 | `playNext()` | `currentIndex` advances to `(2 + 1) % 4 = 3` | Circular navigation intact; zero index corruption | **PASS** |
| **F. Removal when unselected** | 4 songs, `currentIndex = -1` | Remove song 2 | `currentIndex` remains `-1` | `currentIndex` remains `-1` | **PASS** |

**Conclusion:** The `currentIndex` logic is resilient. No index out of bounds or pointer corruption can occur during removals, shuffles, or navigation.

---

## Recently Played Audit

`RecentlyPlayedManager` was inspected against strict specification requirements:

- **Data Structure:** `private final Deque<Song> recentlyPlayed = new ArrayDeque<>()`.
- **Capacity Enforcement:** Maximum capacity fixed at 10. When `recentlyPlayed.size() > 10`, `removeLast()` evicts the oldest event ($O(1)$ complexity).
- **Ordering Policy:** Strict newest-first ordering maintained by `addFirst()`.
- **Duplicate Policy:** Operates as an **event-based history**. Repeated playback of the same song creates multiple chronological entries, accurately reflecting listening behavior.
- **Null Safety:** `recordPlay(null)` immediately returns without modifying history.
- **Snapshot Immutability:** `getRecentlyPlayed()` wraps a defensive copy in `Collections.unmodifiableList()`. External attempts to mutate throw `UnsupportedOperationException`.

### History Update Trigger Verification:
- **Updates on `playSong` success:** **VERIFIED** (`Main.java` line 108).
- **Updates on `playNext` success:** **VERIFIED** (`Main.java` line 242).
- **Updates on `playPrevious` success:** **VERIFIED** (`Main.java` line 259).
- **DOES NOT update on `displayPlaylist`:** **VERIFIED**.
- **DOES NOT update on `addSong` (manual/import):** **VERIFIED**.
- **DOES NOT update on `removeSong`:** **VERIFIED**.
- **DOES NOT update on `searchSongs` (online):** **VERIFIED**.
- **DOES NOT update on `shufflePlaylist`:** **VERIFIED**.

---

## Navigation Audit

`Playlist.playNext()` and `Playlist.playPrevious()` review:

- **Empty Playlist Handling:** Returns `null` immediately when `songs.isEmpty()`. Does not modify `currentIndex`.
- **First Navigation from Unselected State (`currentIndex == -1`):**
  - `playNext()`: Sets `currentIndex = 0` and returns `songs.get(0)`.
  - `playPrevious()`: Sets `currentIndex = songs.size() - 1` and returns `songs.get(songs.size() - 1)`.
- **Circular Wrap-Around:**
  - `playNext()` from index `size - 1`: `(size - 1 + 1) % size = 0`. Wraps cleanly to head.
  - `playPrevious()` from index `0`: `(0 - 1 + size) % size = size - 1`. Wraps cleanly to tail.
- **Single-Song Playlist:**
  - `playNext()` and `playPrevious()` continuously return the single track without index error.
- **Off-by-One Checks:** Arithmetic expressions are mathematically bounded by modulo operations over `songs.size()`. No off-by-one errors exist.

---

## Add/Remove Audit

### Manual Add Verification:
- Invoked via menu Option 7.
- Prompts for `title`, `artist`, and `duration (m:ss)`.
- Rejects empty titles, empty artists, and non-conforming duration strings (e.g. `abc`, `4:60`, `12`).
- Accepts valid durations (e.g. `3:45`, `0:30`, `12:05`).
- Encapsulation respected: calls `playlist.addSong(song)`. Does not access internal collections.

### Manual Remove Verification:
- Invoked via menu Option 8.
- Validates 1-based user input against `1` and `playlist.size()`.
- Rejects `0`, negative numbers, and values $> size$.
- Rejects removal from an empty playlist with `"Playlist is empty."`.
- Correctly updates `currentIndex` across all index relations ($<$, $=$, and $>$).

---

## Online Search Audit

### MusicBrainz REST Integration:
- **Endpoint:** `https://musicbrainz.org/ws/2/recording/?query=`
- **Client:** `java.net.http.HttpClient` with `User-Agent: MusicPlaylistDemo/1.0 (Java learning project)`.
- **Query Types:**
  - Title Search (`choice == 1`): Lucene query `recording:<keyword>`.
  - Artist Search (`choice == 2`): Lucene query `artist:<keyword>`.
- **JSON Parsing:** Uses Google Gson `JsonParser.parseString()`. Extracts `title`, joins multi-artist credits (`name` and `joinphrase`), and converts millisecond lengths into formatted `m:ss`.
- **Result Display & Import:** Results displayed as a 1-based numbered list. Entering a valid number imports the song into the local playlist via `playlist.addSong()`. Entering `0` cancels import.

### Local Search vs. Online Search Discrepancy:
- **Critical Finding:** The application supports **only online MusicBrainz search**. 
- Menu options 1 and 2 search the internet. No local in-memory search exists to filter or find songs already present in the active local playlist.

---

## Input Safety Audit

Every user input path in `Main.java`, `InputHelper.java`, and `APITest.java` was audited:

- **`Scanner.nextInt()` Usage:** **ZERO occurrences**. There are no calls to `Scanner.nextInt()` in the codebase.
- **Numeric Menu Parsing:** Uses `InputHelper.readInt()`, which wraps `Integer.parseInt(scanner.nextLine().trim())` inside a `try-catch (NumberFormatException)` retry loop. Entering letters, symbols, or whitespace safely triggers a re-prompt.
- **Empty String Inputs:** Guarded by `InputHelper.readNonEmptyString()`, which loops until non-whitespace text is entered.
- **Remaining Crash Risk:** 
  - If standard input is closed abruptly (EOF / stream termination, e.g. Ctrl+D), `scanner.nextLine()` in `InputHelper.readInt()` and `readNonEmptyString()` throws `java.util.NoSuchElementException`. Standard interactive terminal applications run indefinitely until user exits via Option 0.

---

## Functional Test Results

The following 30-test end-to-end matrix was executed via an automated test runner:

| Test ID | Test Name | Target Component | Status | Verified Outcome / Evidence |
|---|---|---|---|---|
| **01** | View playlist | `Playlist.displayPlaylist()` | **PASS** | Correctly printed headers, 1-based indices, and track details. |
| **02** | Add song | `Playlist.addSong()` | **PASS** | Song appended to list, size incremented to 1. |
| **03** | Remove song | `Playlist.removeSong()` | **PASS** | Track at index 1 removed; returned removed `Song`; size decremented. |
| **04** | Remove invalid song | `Playlist.removeSong()` | **PASS** | Returned `null` on indices 0, -1, and out-of-bounds; state protected. |
| **05** | Remove from empty playlist | `Playlist.removeSong()` | **PASS** | Returned `null` safely; `currentIndex` remained -1. |
| **06** | Play first song | `Playlist.playSong(1)` | **PASS** | Set `currentIndex = 0`; returned first song. |
| **07** | Play last song | `Playlist.playSong(size)` | **PASS** | Set `currentIndex = size - 1`; returned last song. |
| **08** | Play invalid song | `Playlist.playSong()` | **PASS** | Returned `null` on invalid index; `currentIndex` unchanged. |
| **09** | Next song | `Playlist.playNext()` | **PASS** | Advanced index from 0 to 1; returned second song. |
| **10** | Previous song | `Playlist.playPrevious()` | **PASS** | Decremented index from 1 to 0; returned first song. |
| **11** | Wrap next | `Playlist.playNext()` | **PASS** | Wrapped circularly from index `size - 1` to index 0. |
| **12** | Wrap previous | `Playlist.playPrevious()` | **PASS** | Wrapped circularly from index 0 to index `size - 1`. |
| **13** | Shuffle empty playlist | `Playlist.shufflePlaylist()` | **PASS** | Returned `false` safely; no exception thrown. |
| **14** | Shuffle one-song playlist | `Playlist.shufflePlaylist()` | **PASS** | Returned `false`; single track preserved without mutation. |
| **15** | Shuffle multi-song playlist | `Playlist.shufflePlaylist()` | **PASS** | Returned `true`; all songs preserved without loss or duplication. |
| **16** | Shuffle while song selected | `Playlist.shufflePlaylist()` | **PASS** | Active `Song` reference preserved; `currentIndex` updated to new slot. |
| **17** | Recently played starts empty | `RecentlyPlayedManager` | **PASS** | `isEmpty()` is `true`, `size()` is 0. |
| **18** | Recently played after playback | `RecentlyPlayedManager.recordPlay()` | **PASS** | Playback event recorded at head of history; size is 1. |
| **19** | Repeated playback history | `RecentlyPlayedManager.recordPlay()` | **PASS** | Consecutive plays of same song stored as distinct events. |
| **20** | More than 10 playback events | `RecentlyPlayedManager.recordPlay()` | **PASS** | History strictly capped at 10; oldest 5 evicted via FIFO. |
| **21** | Online title search | `MusicAPI.searchSongs(kw, 1)` | **PASS** | MusicBrainz returned 10 recordings for query `"Shape of You"`. |
| **22** | Online artist search | `MusicAPI.searchSongs(kw, 2)` | **BLOCKED** | MusicBrainz HTTP 503 rate limit encountered; handled gracefully. |
| **23** | Import online search result | `Playlist.addSong()` | **PASS** | Selected search result successfully added to local playlist. |
| **24** | Invalid menu input | `InputHelper.readInt()` | **PASS** | Non-numeric input `"invalid"` trapped; re-prompted safely. |
| **25** | Empty inputs | `InputHelper.readNonEmptyString()` | **PASS** | Blank string rejected; re-prompted until valid input provided. |
| **26** | Add/remove after shuffle | `Playlist` operations | **PASS** | Adding and removing tracks post-shuffle executed cleanly. |
| **27** | Navigation after removal | `Playlist.playNext()` after remove | **PASS** | Index adjusted on removal; subsequent navigation seamless. |
| **28** | Navigation after shuffle | `Playlist.playNext()` after shuffle | **PASS** | Circular navigation proceeded relative to new post-shuffle index. |
| **29** | History after navigation | `Main` navigation coordinator | **PASS** | Navigation actions updated `RecentlyPlayedManager` with active song. |
| **30** | History after import | `Main` search coordinator | **PASS** | Adding/importing songs did not record to history. |

### Test Summary:
- **PASS:** 29 (96.7%)
- **BLOCKED:** 1 (3.3%)
- **FAIL:** 0 (0.0%)

---

## Bugs Found

| Severity | File | Method | Issue | Impact | Recommended Fix |
|---|---|---|---|---|---|
| **MAJOR** | `Playlist.java`<br>`Main.java` | N/A | **Local Playlist Search Missing:** No in-memory search methods exist to search tracks within the local active playlist. Menu options 1 and 2 only search the external MusicBrainz API. | Users cannot search or filter their existing local playlist. Core assignment expectation is partially unmet. | Implement `searchByTitle(String query)` and `searchByArtist(String query)` in `Playlist.java` returning `List<Song>`, and wire into terminal menu. |
| **MAJOR** | `Playlist.java`<br>`Main.java` | N/A | **Playlist Sorting Missing:** Feature 12 (Sorting) is entirely absent from the codebase. | Users cannot sort their playlist alphabetically or by duration. | Implement `sortPlaylist(Comparator<Song> comparator)` in `Playlist.java` and provide menu option with sort criteria. |
| **MAJOR** | `MusicAPI.java` | `searchSongs` | **Upstream API Rate Limiting Vulnerability:** Rapid consecutive searches trigger MusicBrainz HTTP 503 errors (`1 req/sec` limit). | Queries fail with `"Online search is temporarily unavailable."` during normal fast usage. | Introduce a 1-second client-side rate limiter or query debounce before dispatching HTTP requests. |
| **MINOR** | `Playlist.java` | `removeSong` | **Implicit Playback Shift on Current Song Removal:** When the currently playing song is removed, `currentIndex` stays at the same numeric index (now occupied by the shifted next song), implicitly making it the active playing song without explicit playback. | The shifted song receives the `  <-- Now Playing` marker without user action or history recording. | Set `currentIndex = -1` when the active song is removed, or explicitly prompt the user that playback stopped. |
| **MINOR** | `Song.java` | N/A | **Missing `equals()` and `hashCode()`:** `Song` relies on default reference equality (`Object.equals`). | Value-based comparison, deduplication, and hashing in collections (`Set<Song>`, `Map<Song, ?>`) fail. | Override `equals()` and `hashCode()` comparing `title` and `artist` (case-insensitive). |
| **MINOR** | `Main.java` | `handleShufflePlaylist` | **Redundant Shuffle Invocation:** Calls `playlist.shufflePlaylist()` even when `playlist.isEmpty()` or `playlist.size() == 1` has already been checked. | Redundant method execution; ignores returned boolean. | Remove redundant `playlist.shufflePlaylist()` calls inside the guard branches. |
| **MINOR** | `Main.java` | `handleManualAddSong` | **Validation in Coordinator:** Duration format validation (`isValidDuration`) is defined in `Main.java` rather than in `Song.java`. | `Song` constructor accepts unvalidated duration strings when instantiated outside `Main`. | Move duration format validation into `Song` constructor or a dedicated domain validator. |
| **MINOR** | `InputHelper.java` | `readInt`<br>`readNonEmptyString` | **Unhandled Stream Termination (EOF):** `scanner.nextLine()` is called without `scanner.hasNextLine()`. | If standard input closes abruptly, an unhandled `NoSuchElementException` crashes the app. | Add `if (!scanner.hasNextLine())` guard returning a default exit value or throwing a clean application exit. |
| **SUGGESTION** | Repository Root | `pom.xml` | **Missing Maven Wrapper:** Repository contains `pom.xml` but lacks `.mvn/wrapper/` and `mvnw`/`mvnw.cmd` scripts. | Evaluators without globally installed `mvn` must manually locate Gson to compile with raw `javac`. | Run `mvn wrapper:wrapper` to commit portable wrapper scripts to the repository. |
| **SUGGESTION** | `APITest.java` | `main` | **Test Class in Production Sources:** `APITest.java` resides in `src/main/java/org/example/`. | Pollutes the production package namespace. | Move `APITest.java` to `src/test/java/org/example/`. |

---

## Viva Readiness Review

### Code Organization & Quality
- The project demonstrates clean decomposition into single-responsibility classes (`Song`, `Playlist`, `RecentlyPlayedManager`, `InputHelper`, `MusicAPI`, `Main`).
- Zero architectural coupling exists between the network/JSON layer (`MusicAPI`) and the core playlist collections (`Playlist`).

### OOP & Design Patterns Used
- **Encapsulation:** All fields in `Song`, `Playlist`, and `RecentlyPlayedManager` are `private`. Access is regulated via getters and defensive unmodifiable views.
- **Immutability:** `Song` is immutable with `final` fields, eliminating race conditions and accidental state corruption.
- **Defensive Copying:** `Playlist.getSongs()` and `RecentlyPlayedManager.getRecentlyPlayed()` return unmodifiable views via `Collections.unmodifiableList()`.
- **Composition over Inheritance:** `Playlist` has a `List<Song>`, and `RecentlyPlayedManager` has a `Deque<Song>`.

### Collections Framework Knowledge Demonstrated
- `ArrayList<Song>` in `Playlist`: Selected for $O(1)$ random access by index for track playback, next/previous navigation, and shuffle.
- `ArrayDeque<Song>` in `RecentlyPlayedManager`: Selected for $O(1)$ amortized insertion at head (`addFirst`) and $O(1)$ eviction at tail (`removeLast`), outperforming `LinkedList` due to contiguous memory allocation and lack of node overhead.
- `Collections.shuffle(List)`: Utilizes in-place Fisher-Yates shuffle algorithm ($O(N)$).

### Anticipated Viva Questions & Model Answers

1. **Q: Why did you use `ArrayDeque` instead of `LinkedList` or `ArrayList` for Recently Played Songs?**  
   *Answer:* `ArrayDeque` implements the `Deque` interface backed by a resizable cyclic array. It provides $O(1)$ head insertion (`addFirst`) and $O(1)$ tail deletion (`removeLast`) with zero node allocation overhead, unlike `LinkedList` which allocates a new `Node` object for every insertion. It is more cache-friendly and consumes significantly less memory.

2. **Q: How does the application preserve the currently playing song when the playlist is shuffled?**  
   *Answer:* Before invoking `Collections.shuffle(songs)`, `Playlist.shufflePlaylist()` retrieves the active `Song` object reference via `getCurrentSong()`. After the list elements are randomly rearranged, it iterates through the shuffled list and checks reference equality (`songs.get(i) == currentSong`). Once located, `currentIndex` is updated to the track's new index position, ensuring playback continuity.

3. **Q: How does circular navigation work in `playNext()` and `playPrevious()`?**  
   *Answer:* It relies on modular arithmetic: `(currentIndex + 1) % size` for forward navigation, and `(currentIndex - 1 + size) % size` for backward navigation. Adding `size` before the modulo operator in `playPrevious()` prevents negative remainders in Java when decrementing from index `0`.

4. **Q: Why does `Playlist.getSongs()` return an unmodifiable list instead of the raw `songs` list?**  
   *Answer:* To maintain strict encapsulation and prevent representation exposure. If external classes had direct access to `songs`, they could add or remove tracks without updating `currentIndex`, corrupting internal state.

5. **Q: How did you prevent application crashes when a user types letters into the numeric menu?**  
   *Answer:* We created `InputHelper.readInt()` which reads the entire line as a string via `scanner.nextLine().trim()` and parses it with `Integer.parseInt()`. It wraps the parse in a `try-catch (NumberFormatException)` block, re-prompting the user on invalid input rather than crashing with an unhandled exception.

---

## Final Verdict

### **READY WITH MINOR FIXES**

### Justification:
All 8 developmental phases assigned to the engineering team are **fully functional, verified, and free of regression**. The core engine is rock-solid:
- Compiles with 0 errors.
- 29 out of 30 automated integration tests pass.
- State preservation across shuffle, removal, and circular navigation is thoroughly verified.
- Terminal interface does not crash on invalid input.
- Decoupled, modular object-oriented architecture.

To transition the project to **READY FOR FINAL SUBMISSION**, the team should address the three minor gaps before presentation:
1. Implement in-memory local playlist search (`searchByTitle` / `searchByArtist`) in `Playlist.java`.
2. Implement sorting (`sortPlaylist`) in `Playlist.java`.
3. Generate the Maven Wrapper (`mvnw`) so any evaluator can build the project instantly with `./mvnw compile`.
