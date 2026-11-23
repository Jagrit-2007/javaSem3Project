# Balanced Structure Report

## 1. Summary

The codebase has been refactored into a balanced, modular, and maintainable object-oriented structure suitable for a college submission and viva demonstration. 

The monolithic code in `Main.java` was decomposed into dedicated classes with clear separation of responsibilities:
- `Song` was moved out of `Main.java` into its own class with proper private encapsulation, input trimming, null validation, and a clean `toString()` representation.
- `Playlist` was created to encapsulate the local song collection (`List<Song>`), protecting internal storage with an unmodifiable view and managing insertion and display.
- `InputHelper` was created to provide safe terminal input parsing, eliminating application crashes from invalid or non-numeric inputs.
- `MusicAPI` was refined to preserve MusicBrainz REST API discovery while hardening input validation, error handling, and reporting distinct user messages for network errors, rate limits, and empty result sets.
- `Main` now serves purely as the coordinator and terminal menu presenter, removing raw mutable collections and API parsing logic.

No unrelated technologies, frameworks, or deferred features were introduced.

---

## 2. Files Created

- `src/main/java/org/example/Song.java`: Domain model representing an individual song track with encapsulated attributes.
- `src/main/java/org/example/Playlist.java`: Collection manager encapsulating local playlist storage, insertion, and display.
- `src/main/java/org/example/InputHelper.java`: Safe terminal scanner utility preventing input-parsing crashes.
- `reports/balanced_structure_report.md`: This architecture and verification report.

---

## 3. Files Modified

- `src/main/java/org/example/Main.java`: Removed inner `Song` class and raw `ArrayList<Song>`. Refactored menu loop to use `Playlist` and `InputHelper`. Updated menu labels to clearly identify online search.
- `src/main/java/org/example/MusicAPI.java`: Preserved MusicBrainz HTTP client, Gson parsing, and field extraction. Added constants, input validation, and distinct error messages for different failure modes.
- `src/main/java/org/example/APITest.java`: Updated to use `Song.java` and `InputHelper` for safe manual API testing.

---

## 4. Final Project Structure

```text
src/main/java/org/example/
├── APITest.java       # Manual API testing harness
├── InputHelper.java   # Safe terminal scanner input methods
├── Main.java          # Console menu loop and coordinator
├── MusicAPI.java      # MusicBrainz REST API client and JSON parser
├── Playlist.java      # Local playlist collection and display
└── Song.java          # Immutable, encapsulated song model
```

---

## 5. Class Responsibilities

| Class | Responsibility |
|---|---|
| `Song.java` | Represents an immutable music track (`title`, `artist`, `duration`). Validates non-null inputs, trims whitespace, and formats output via `toString()`. |
| `Playlist.java` | Manages the in-memory playlist (`List<Song>`). Provides safe insertion (`addSong`), unmodifiable access (`getSongs`), size/empty inspection, and formatted display. |
| `InputHelper.java` | Prevents terminal input crashes by safely reading and validating integers (`readInt`) and non-empty strings (`readNonEmptyString`) from `Scanner`. |
| `MusicAPI.java` | Communicates with the external MusicBrainz REST API over HTTP, parses JSON responses using Google Gson, handles HTTP/network errors cleanly, and converts results into `Song` objects. |
| `Main.java` | Coordinates program startup, initializes demo songs, presents the terminal menu, delegates search and display actions, and manages song addition workflow. |
| `APITest.java` | Standalone manual testing utility for inspecting `MusicAPI` responses without running the complete playlist application. |

---

## 6. MusicBrainz API Flow

The online song discovery and import process follows this clear flow:

1. **User Query Input:** The user chooses online search by title or artist in `Main`. `InputHelper.readNonEmptyString()` safely captures and trims the keyword.
2. **HTTP Request Dispatch:** `MusicAPI.searchSongs(keyword, choice)` formats the query (`recording:<keyword>` or `artist:<keyword>`), URL-encodes it with UTF-8, and constructs a standard HTTP GET request with an explicit `User-Agent`.
3. **HTTP Client Communication:** `java.net.http.HttpClient` sends the request asynchronously or synchronously to `https://musicbrainz.org/ws/2/recording/?query=...&fmt=json&limit=10`.
4. **Status & Error Inspection:** `MusicAPI` verifies the HTTP status code. If non-200 (such as rate-limiting 503), it reports `"Online search is temporarily unavailable."` without crashing. Network exceptions trigger `"Could not connect to the online music service."`
5. **Gson Parsing:** When HTTP 200 is received, Google Gson (`JsonParser`) parses the response payload into a `JsonObject` and extracts the `recordings` `JsonArray`.
6. **Song Object Conversion:** Up to 10 recordings are traversed. `MusicAPI` extracts the recording title, joins credit artists, converts millisecond durations into `m:ss`, and instantiates `Song` objects.
7. **Selection & Local Storage:** `Main` displays the matching songs as a numbered list. The user selects a song number via `InputHelper.readInt()`, and `Main` invokes `playlist.addSong(selected)`, persisting the chosen song into the local in-memory playlist.

---

## 7. Verification Results

| Check | Status | Evidence |
|---|---|---|
| 1. Exactly one Song class | **PASS** | `Song.java` is the single declaration; inner class removed from `Main.java`. |
| 2. Song is in Song.java | **PASS** | Verified at `src/main/java/org/example/Song.java`. |
| 3. Playlist owns local collection | **PASS** | `Playlist.java` encapsulates `private final List<Song> songs`. |
| 4. Main does not own raw mutable ArrayList | **PASS** | `Main.java` instantiates and interacts exclusively through `Playlist`. |
| 5. Demo songs available | **PASS** | 4 demo songs added to `Playlist` at startup; verified via option 3. |
| 6. Playlist display works | **PASS** | `playlist.displayPlaylist()` shows numbered formatted songs. |
| 7. Online title search reachable | **PASS** | Menu option 1 triggers title search via `MusicAPI.searchSongs(keyword, 1)`. |
| 8. Online artist search reachable | **PASS** | Menu option 2 triggers artist search via `MusicAPI.searchSongs(keyword, 2)`. |
| 9. Search results displayed as numbered Song objects | **PASS** | Formatted as `(i + 1) + ". " + results.get(i)` using `Song.toString()`. |
| 10. Online result can be added to Playlist | **PASS** (Verified with live response) / **BLOCKED** (During final smoke test due to MusicBrainz HTTP 503 rate limit) | Adding a selected result to the playlist verified when online service responds; during final smoke test, online queries hit MusicBrainz rate limits and gracefully displayed "Online search is temporarily unavailable." |
| 11. Invalid menu text does not crash | **PASS** | Entering `"abc"` prints `"Invalid input! Please enter a valid number."` and re-prompts. |
| 12. Invalid result selection does not crash | **PASS** | Entering non-numbers or out-of-bounds indices is caught and handled safely. |
| 13. Empty search input rejected | **PASS** | `InputHelper.readNonEmptyString` re-prompts with `"Input cannot be empty. Please try again."`. |
| 14. Invalid API search type rejected | **PASS** | `MusicAPI.searchSongs` checks `choice != 1 && choice != 2` and rejects invalid types. |
| 15. Different failure messages produced | **PASS** | Verified distinct messages for empty results, non-200 HTTP codes, network failures, and invalid inputs. |
| 16. getSongs() cannot modify internal playlist | **PASS** | Returns `Collections.unmodifiableList(songs)`; calling `.add()` throws `UnsupportedOperationException`. |
| 17. No shuffle implementation added | **PASS** | Verified: no shuffle code exists in `Playlist` or `Main`. |
| 18. No recently played implementation added | **PASS** | Verified: no history tracking or deque structures added. |
| 19. No navigation implementation added | **PASS** | Verified: no `currentIndex`, `next()`, or `previous()` methods added. |
| 20. No sorting implementation added | **PASS** | Verified: no comparator or sorting methods added. |
| 21. No local search implementation added | **PASS** | Verified: local in-memory playlist search remains deferred. |
| 22. No unnecessary enterprise components | **PASS** | No Spring, Docker, databases, or web servers introduced. |
| 23. No unrelated dependencies added | **PASS** | `pom.xml` retains only the original Google Gson dependency. |
| 24. No build artifacts in Git changes | **PASS** | All test builds executed in `/tmp`; working tree clean of `.class` files. |

---

## 8. Deferred Features

The following features were intentionally excluded from this refactoring task and documented for future implementation:

1. **Add and Remove Songs:**
   - Responsible class: `Playlist.java` (methods: `removeSong(int index)` or `removeSong(Song song)`).
   - Menu interaction belongs in `Main.java`.
2. **Next and Previous Navigation:**
   - Responsible class: `Playlist.java` (tracking `currentIndex`, `nextSong()`, `previousSong()`).
   - Requires explicit definition of when a song is considered "played".
3. **Shuffle Playlist:**
   - Responsible class: `Playlist.java` (`shuffle()`).
   - Must use `Collections.shuffle` on the internal list while preserving or updating `currentIndex`.
4. **Recently Played Songs:**
   - Responsible class: Dedicated `HistoryManager` or helper using `Deque<Song>` (such as `ArrayDeque<Song>`).
   - Must hook into actual playback events rather than display or search addition.
5. **Local Playlist Search by Title or Artist:**
   - Responsible class: `Playlist.java` (`searchByTitle(String)`, `searchByArtist(String)`).
   - Will filter the local in-memory collection without requiring internet access.
6. **Sorting:**
   - Responsible class: `Playlist.java` (using `List.sort` and `Comparator`).

---

## 9. Remaining Issues

1. **Environment Maven Executable:** The execution environment lacks the `mvn` binary on PATH. Compilation currently relies on invoking `javac` with a classpath pointing to the Gson JAR. Adding the official Maven Wrapper (`mvnw` and `.mvn/wrapper/`) will ensure any developer or grader can build the project immediately with `./mvnw compile`.
2. **MusicBrainz Rate Limiting (HTTP 503):** MusicBrainz limits requests to 1 query per second. Consecutive fast queries return HTTP 503. Handled gracefully with user-friendly notices, but an in-memory local search will avoid reliance on remote network conditions for routine operations.

---

## 10. Final Verdict

### **READY FOR MAINTAINER REVIEW**

The project structure is clean, modular, resilient to bad terminal input, strictly adheres to the domain boundary, and sets up a robust foundation for the upcoming teammate tasks.
