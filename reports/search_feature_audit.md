# Search Feature Audit Report

## 1. Executive Summary

The "Search Songs by Title or Artist" feature recently pushed to the repository is **not ready**.

While the teammate successfully implemented an online search integration against the external MusicBrainz REST API (using Java HTTP client and Google Gson), the implementation **does not search the user's local playlist**. In-memory search over the playlist does not exist. Furthermore, the repository currently cannot be compiled out-of-the-box using standard `javac` because external dependencies are not bundled and Maven is not installed in the environment. Online searches frequently fail due to unhandled HTTP 503 rate-limiting from MusicBrainz, scanner input crashes on non-numeric input, and the codebase lacks fundamental object-oriented architecture (no `Playlist` class, and `Song` is trapped as a package-private class in `Main.java` without `equals` or `hashCode`).

---

## 2. Repository State

- **Current Git Branch:** `main` (tracking `origin/main`).
- **Working-Tree Status:** Clean (`nothing to commit, working tree clean`).
- **Latest Relevant Commit:**
  - Commit Hash: `09cdb3f67a7f942c999bbf79d617ea1206ca86d3`
  - Author: Deepali <Deepali1-63.becse25@chitkara.edu.in>
  - Date: Tue Nov 24 01:28:37 2026 +0530
  - Commit Message: `Initial commit`
  - Note: This is the single commit present in the repository containing the entire current codebase.
- **Build System:** 
  - Maven configuration exists via `pom.xml` (`com.google.code.gson:gson:2.11.0`).
  - The Maven CLI executable (`mvn`) is **not installed** on the system PATH (`mvn: command not found`).
  - No Maven wrapper scripts (`mvnw`, `mvnw.cmd`, `.mvn/`) are committed to the repository.
  - Plain `javac` without classpath arguments fails with 9 compilation errors due to missing `com.google.gson` packages.
- **Java Version / JDK Requirement:**
  - `pom.xml` specifies `<maven.compiler.source>22</maven.compiler.source>` and `<maven.compiler.target>22</maven.compiler.target>`.
  - `.idea/misc.xml` specifies `languageLevel="JDK_22"` and `project-jdk-name="22"`.
  - Host environment installed JDK: OpenJDK `25.0.5.1` (build `25.0.5.1+2-1-26.04.1-Ubuntu`), compiler `javac 25.0.5.1`.
- **Compile and Run Commands:**
  - *Standard Maven command (when Maven is available):*
    ```bash
    mvn clean compile
    mvn exec:java -Dexec.mainClass="org.example.Main"
    ```
  - *Current javac command (requires external Gson 2.11.0 JAR on classpath):*
    ```bash
    javac -cp /path/to/gson-2.11.0.jar -d out src/main/java/org/example/*.java
    java -cp out:/path/to/gson-2.11.0.jar org.example.Main
    ```
  - *Raw javac command without classpath (fails):*
    ```bash
    javac -d out src/main/java/org/example/*.java
    # Fails: package com.google.gson does not exist (9 errors)
    ```

---

## 3. Current File Structure

```text
javaSem3Project/
├── .git/
├── .gitignore
├── .idea/
│   ├── .gitignore
│   ├── encodings.xml
│   ├── misc.xml
│   └── vcs.xml
├── pom.xml
├── reports/
│   └── search_feature_audit.md
└── src/
    └── main/
        └── java/
            └── org/
                └── example/
                    ├── APITest.java
                    ├── Main.java
                    └── MusicAPI.java
```

---

## 4. Existing Architecture

The existing architecture is procedural and centralized inside `Main.main()`. There is no dedicated `Playlist` class, service layer, or data access layer.

| File / Class | Responsibility | Important Methods | Dependencies / Shared State |
|---|---|---|---|
| `src/main/java/org/example/Main.java`<br>`class Song` | Data model representing a musical track (package-private). | `Song(String title, String artist, String duration)`<br>`void display()` | Holds package-private fields `title`, `artist`, `duration`. No getters, setters, or unique ID. Does not override `equals()` or `hashCode()`. Tightly coupled to console via `display()` calling `System.out.println`. |
| `src/main/java/org/example/Main.java`<br>`class Main` | Application entry point and interactive terminal console interface. | `public static void main(String[] args)` | Manages local variable `ArrayList<Song> playlist` initialized with 4 demo songs. Runs interactive `do-while` menu loop. Dispatches options 1 and 2 to `MusicAPI.searchSongs`. Handles user song selection and adds chosen tracks to `playlist`. Reads input via `Scanner(System.in)`. |
| `src/main/java/org/example/MusicAPI.java`<br>`class MusicAPI` | REST API client querying the online MusicBrainz web service. | `public static ArrayList<Song> searchSongs(String keyword, int choice)` | Stateless utility class. Depends on `java.net.http.HttpClient`, `java.net.http.HttpRequest`, `java.net.http.HttpResponse`, and `com.google.gson.*`. Queries `https://musicbrainz.org/ws/2/recording/`. Converts JSON records into `ArrayList<Song>`. |
| `src/main/java/org/example/APITest.java`<br>`class APITest` | Standalone interactive test harness for `MusicAPI`. | `public static void main(String[] args)` | Duplicate terminal menu specifically for testing `MusicAPI.searchSongs` without initializing the playlist. Uses `Scanner(System.in)`. |

### Key Architectural Characteristics:

- **Song Model:** Defined as package-private `class Song` within `Main.java` (lines 5–23). It lacks private encapsulation, accessors, mutators, and identity methods (`equals`, `hashCode`, `toString`).
- **Playlist Storage:** No `Playlist` class exists. Storage is simply an `ArrayList<Song> playlist` local variable inside `Main.main` (line 31).
- **Terminal Menu:** Implemented directly inside `Main.main` lines 41–130 using a `do-while` loop and `Scanner`.
- **Main Entry-Point:** `org.example.Main.main`. (`org.example.APITest.main` also exists as a secondary entry point).
- **Song Addition:** 
  - Hardcoded demo songs added at startup: `playlist.add(new Song(...))` (lines 34–37).
  - Online search addition: User picks an index from online search results, added via `playlist.add(selected)` (line 89).
  - Manual song entry (typing a title/artist locally) is **not implemented**.
- **Song Removal:** **NOT IMPLEMENTED**. No methods, logic, or menu options exist to remove songs.
- **Navigation (Next / Previous):** **NOT IMPLEMENTED**. No playback cursor, queue, or navigation methods exist.
- **Search Logic Location:** Located in `MusicAPI.searchSongs(String keyword, int choice)` in `MusicAPI.java`. Invoked from `Main.java` (line 62).
- **Collection Type:** `java.util.ArrayList<Song>`.
- **Currently Selected / Playing Song Tracking:** **NOT IMPLEMENTED**. Neither `currentIndex`, `currentSong`, nor playing state exists.
- **Shuffle:** **NOT IMPLEMENTED**.
- **Recently Played History:** **NOT IMPLEMENTED**.
- **Sorting:** **NOT IMPLEMENTED**.
- **Coupling & Shared State Risks:**
  - All playlist state and control flow are trapped inside `Main.main`.
  - The search feature is completely coupled to an external third-party HTTP service (`musicbrainz.org`). It acts as an online importer rather than an internal playlist search engine.

---

## 5. Search-by-Title Implementation

- **Source File:** `src/main/java/org/example/MusicAPI.java`
- **Class Name:** `org.example.MusicAPI`
- **Method Name & Signature:** `public static ArrayList<Song> searchSongs(String keyword, int choice)`
- **Input Source:** User input read via `sc.nextLine()` in `Main.java` (line 56) with parameter `choice = 1`.
- **Return Type:** `java.util.ArrayList<Song>`
- **Search Algorithm & Mechanism:**
  - Constructs a Lucene query string: `query = "recording:" + keyword` (lines 25–26).
  - Encodes the query using `URLEncoder.encode(query, StandardCharsets.UTF_8)`.
  - Appends query to MusicBrainz recording endpoint: `https://musicbrainz.org/ws/2/recording/?query=<query>&fmt=json&limit=10`.
  - Sends an HTTP GET request via `java.net.http.HttpClient` with header `User-Agent: MusicPlaylistDemo/1.0 (Java learning project)`.
  - Checks if `response.statusCode() != 200`; if failed, logs status and returns empty list.
  - Parses JSON response using Google Gson `JsonParser.parseString(...)`.
  - Extracts the `recordings` JSON array.
- **Iteration Approach:** Standard sequential `for` loop over `recordings.size()` (lines 70–122).
- **Matching Type:** Partial, token-based matching evaluated remotely on MusicBrainz servers via Apache Lucene/Solr.
- **Case Sensitivity:** Case-insensitive (handled remotely by MusicBrainz).
- **Whitespace Handling:** Unhandled. `keyword.trim()` is **not** called. Leading/trailing spaces are encoded directly into the query URL (e.g., `%20%20%20Believer%20%20%20`), degrading search relevance.
- **Empty Query Handling:** If `keyword` is `""`, queries `"recording:"`, which causes MusicBrainz to either return HTTP 400 Bad Request or return arbitrary recordings. No client-side validation is performed.
- **Empty Playlist Handling:** The search method does not inspect or interact with the local playlist. It executes the online query identically whether the playlist has 0 or 100 songs. **In-memory playlist search does not exist.**
- **No Matches:** Returns an empty `ArrayList<Song>`. `Main.java` checks `results.isEmpty()` and prints `"No songs found!"` (line 66).
- **Multiple Matches:** Up to 10 recordings are parsed into `Song` objects and returned in the list.
- **Terminal Display:** In `Main.java` (lines 72–76), iterates through `results` printing `Song 1`, `Song 2`, etc., and invokes `Song.display()` for each.
- **State Mutation:** The search method itself is read-only. However, `Main.main` immediately follows search with a prompt to add one of the results to `playlist` via `playlist.add(selected)` (line 89).
- **Time Complexity:** $O(K)$ where $K \le 10$ is the number of parsed results, plus network round-trip latency ($O(\text{HTTP request})$). Independent of local playlist size $N$.
- **Space Complexity:** $O(K)$ auxiliary space for storing up to 10 returned `Song` objects.

---

## 6. Search-by-Artist Implementation

- **Source File:** `src/main/java/org/example/MusicAPI.java`
- **Class Name:** `org.example.MusicAPI`
- **Method Name & Signature:** `public static ArrayList<Song> searchSongs(String keyword, int choice)`
- **Input Source:** User input read via `sc.nextLine()` in `Main.java` (line 56) with parameter `choice = 2`.
- **Return Type:** `java.util.ArrayList<Song>`
- **Search Algorithm & Mechanism:**
  - Evaluates `if (choice == 1) ... else { query = "artist:" + keyword; }` (lines 25–29).
  - Note: If `choice` is any integer other than 1, it silently falls back to artist search without validation.
  - Constructs Lucene query `artist:<keyword>`, URL-encodes it, and sends the request to MusicBrainz.
  - Parses JSON response, extracts recording title, traverses `artist-credit` JSON array to build full artist names (handling `name` and `joinphrase`), and converts duration from milliseconds to `m:ss`.
- **Matching Characteristics:** Identical to title search (remote Lucene token matching, case-insensitive, untrimmed whitespace, no local playlist search).
- **Empty Query Handling:** Queries `"artist:"`, returning arbitrary results or HTTP 400.
- **Complexity:** Time complexity $O(K)$ plus network latency; space complexity $O(K)$ where $K \le 10$.

---

## 7. Menu Integration

Both search types are wired into the terminal menu in `src/main/java/org/example/Main.java`:

- **Option 1 ("Search Songs by Title"):** Fully reachable. Prompts for keyword and executes `MusicAPI.searchSongs(keyword, 1)`.
- **Option 2 ("Search Songs by Artist"):** Fully reachable. Prompts for keyword and executes `MusicAPI.searchSongs(keyword, 2)`.

### Operational Deficiencies in Menu Integration:
1. **Wrong Target Domain:** The menu presents options 1 and 2 under the banner `"===== MUSIC PLAYLIST ======"`. Users naturally expect these options to filter or find songs within their active playlist. Instead, both options search the internet via MusicBrainz and prompt to add new tracks.
2. **Scanner Crash Vulnerability:** Menu selection uses `sc.nextInt()` followed by `sc.nextLine()` (lines 50–51). If a user enters non-integer input (e.g. `title`, `exit`, or a letter), an unhandled `java.util.InputMismatchException` is thrown and immediately crashes the entire program.
3. **Selection Crash Vulnerability:** In the song addition prompt (`Enter song number to add (0 to skip):`), `sc.nextInt()` is used again (line 82) without error trapping.

---

## 8. Verification Results

Verification was performed on OpenJDK 25 using controlled sample input, testing both direct API invocations and interactive application flow:

| # | Test | Status | Actual Result | Evidence / Notes |
|---|---|---|---|---|
| 1 | Exact title search | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries MusicBrainz for `"recording:Shape of You"` and returns 10 recordings. | Verified with query `"Shape of You"`. Does not search local demo songs. |
| 2 | Partial title search | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries MusicBrainz for `"recording:Shape"` and returns partial matches. | Remote Lucene handles token matching when not rate-limited. |
| 3 | Title search with different letter casing | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries `"recording:shape of you"` and returns identical matches to uppercase. | Remote Lucene index is case-insensitive. |
| 4 | Exact artist search | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries `"artist:Ed Sheeran"` and returns recordings by Ed Sheeran. | Verified with `"Ed Sheeran"`. Does not search local playlist. |
| 5 | Partial artist search | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries `"artist:Sheeran"` and returns recordings associated with Sheeran. | Remote Lucene handles artist token matches. |
| 6 | Artist search with different letter casing | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Queries `"artist:ed sheeran"` and returns matching recordings. | Case-insensitive remotely. |
| 7 | Query with leading or trailing spaces | **FAIL** | String is not trimmed locally; URL encodes spaces (e.g. `%20%20Believer%20%20`). | No `.trim()` called in `MusicAPI.java` or `Main.java`. Degrades Lucene query. |
| 8 | Empty query | **FAIL** | Sends `"recording:"` or `"artist:"`. Returns arbitrary recordings or HTTP 400. | No input validation check (`keyword.isEmpty()`). Returns 10 unrelated songs instead of validation warning. |
| 9 | Search with an empty playlist | **NOT IMPLEMENTED** | Online search executes normally; local playlist is never inspected. | Searching an empty local playlist does not exist because local search is absent. |
| 10 | Query with no matching song | **PASS** *(Online)* | Nonsense query returns empty JSON recordings list; displays `"No songs found!"`. | Verified with `"qwxzjklp9876543210nonexistent"`. |
| 11 | Multiple songs by the same artist | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Returns up to 10 distinct recordings by the requested artist. | Verified with `"Ed Sheeran"` (returns 10 tracks). Local demo songs not searched. |
| 12 | Multiple titles containing the same word | **PASS** *(Online)*<br>**NOT IMPLEMENTED** *(Local)* | Returns up to 10 distinct recordings containing the keyword. | Verified with `"Love"` (returns 10 tracks). Local demo songs not searched. |
| 13 | Null handling | **FAIL** | Concatenates `"recording:null"`; searches MusicBrainz for songs titled `"null"`. | Verified: returns songs like `"Null-Null - Ex Nör Säx"`. No null check in `searchSongs`. |
| 14 | Search after a song has been removed | **NOT IMPLEMENTED** | Song removal feature does not exist in the codebase. | No removal method or menu option present anywhere. |
| 15 | Search results after playlist order changes | **NOT IMPLEMENTED** | Playlist reordering / shuffle does not exist in the codebase. | Playlist order is fixed to insertion order; no reordering logic exists. |
| 16 | API Rate Limiting Handling | **FAIL** | Rapid queries trigger MusicBrainz HTTP 503 (`API request failed: 503`), returning empty results. | Swallowed in catch/status check; falsely informs user `"No songs found!"`. |
| 17 | Non-numeric input in terminal menu | **FAIL** | Entering letters or symbols causes unhandled `InputMismatchException` crash. | Verified in `Main.java` lines 50, 82 and `APITest.java` line 14. |

---

## 9. Code Quality Findings

| Severity | File / Method | Finding | Impact | Recommended Future Fix |
|---|---|---|---|---|
| **BLOCKER** | `pom.xml`<br>`MusicAPI.java` | Repository cannot be compiled out-of-the-box with plain `javac`. Maven is not installed on PATH and no Maven Wrapper (`mvnw`) is bundled. | Fresh checkouts fail to compile (`package com.google.gson does not exist`). | Commit the Maven Wrapper (`mvnw`, `.mvn/wrapper/`) to the repository or provide build instructions. |
| **MAJOR** | `MusicAPI.java`<br>`Main.java:61-63` | Teammate implemented an external web search/import tool rather than an in-memory search for the local playlist. | Users cannot search their own playlist tracks. The core assignment specification for the playlist engine is unmet. | Implement local playlist search methods (`searchByTitle`, `searchByArtist`) filtering `playlist`. |
| **MAJOR** | `MusicAPI.java:57-61` | Reliance on public unauthenticated MusicBrainz API with strict 1 req/sec rate limit. HTTP 503 errors are silently caught. | Searches randomly fail with "No songs found!" under normal usage. App cannot function without active internet. | Add rate-limiting / retry backoff, or transition search to operate primarily on local in-memory songs. |
| **MAJOR** | `Main.java:50,82`<br>`APITest.java:14` | Unchecked `Scanner.nextInt()` calls without input validation or exception handling. | Entering non-numeric text causes `java.util.InputMismatchException` and crashes the program immediately. | Wrap `nextInt()` calls in `try-catch` blocks or use `nextLine()` with integer parsing. |
| **MAJOR** | `Main.java:31`<br>`Main.java:25-133` | Monolithic `Main.main` procedure (106 lines) and complete absence of a `Playlist` class. | Playlist state is an unencapsulated local `ArrayList`. Prevents unit testing, modular expansion, or clean integration. | Create a dedicated `Playlist` class encapsulating storage, search, shuffle, and history hooks. |
| **MINOR** | `Main.java:5-23`<br>`class Song` | `Song` class is declared package-private inside `Main.java` with package-private fields and no getters/setters. | Violates OOP encapsulation. Cannot be accessed cleanly from other packages or subpackages. | Move `Song` into its own `Song.java` file, make fields `private`, and provide public getters/setters. |
| **MINOR** | `Main.java:5-23`<br>`class Song` | `Song` does not implement `equals()`, `hashCode()`, or contain a unique identifier (`songId`). | Object equality defaults to memory reference (`==`). Cannot safely detect duplicate tracks or search hits. | Implement `equals()` and `hashCode()` based on title and artist, or assign a unique `songId`. |
| **MINOR** | `MusicAPI.java:23-29` | Missing input validation: `keyword` is not checked for `null` or empty string `""`, and `.trim()` is missing. | Passing `null` searches for `"null"`; passing `""` searches empty string; trailing spaces degrade queries. | Add guard clauses: `if (keyword == null \|\| keyword.trim().isEmpty()) return new ArrayList<>();`. |
| **MINOR** | `MusicAPI.java:25-29` | Silent fallthrough for invalid choice parameters (`choice != 1` automatically defaults to artist search). | Passing unexpected values (e.g. 0, 3, -1) executes artist search without warning. | Validate `choice` explicitly (e.g., throw `IllegalArgumentException` or return empty). |
| **MINOR** | `Main.java:17-22`<br>`Song.display()` | `Song` directly writes to `System.out` via `display()`. | Tightly couples model to console presentation. Prevents GUI, logging, or non-console formatting. | Override `toString()` or create a dedicated presentation/view formatter class. |
| **MINOR** | `APITest.java:7-32` | Redundant test class committed in source tree with duplicated scanner and display logic. | Clutters production source tree; should be an automated test under `src/test/java`. | Move test harnesses to `src/test/java` or delete before final delivery. |
| **SUGGESTION** | `Main.java:34-37` | 4 demo songs are hardcoded inline inside `Main.main`. | Reduces flexibility and pollutes main execution logic. | Extract demo song population into a dedicated helper or data loader. |

---

## 10. Shuffle Integration Readiness

Assessment for **Feature 3: Shuffle Playlist**:

- **Target Collection to Shuffle:**
  - The local `ArrayList<Song> playlist` in `Main.java` (line 31).
- **Natural Method Location:**
  - Naturally belongs in a `Playlist` domain class (e.g., `playlist.shuffle()`). In the current codebase, it would have to be forced into `Main.java` as a static helper or inline code inside `main()`.
- **Current Song / Index Tracking:**
  - Currently **does not exist**. Neither `currentIndex` nor `currentSong` is defined.
- **Impact of Shuffling on Playback Tracking:**
  - Once playback is introduced, calling `Collections.shuffle(playlist)` will reorder the elements. If playback is tracked by an integer `currentIndex`, the currently playing song will abruptly switch to an unrelated track unless `currentIndex` is re-located or `currentSong` is tracked by object reference.
- **Search Dependency on Playlist Order:**
  - The current online `MusicAPI` search does not depend on playlist order. If local playlist search is implemented, shuffling will change the index order of returned matches unless matches are sorted or presented independently.
- **Ambiguity from Duplicate Songs:**
  - Because `Song` lacks `equals()`, `hashCode()`, and a unique ID (`songId`), if a playlist contains identical tracks (e.g. two copies of "Perfect"), locating the specific active track instance post-shuffle is ambiguous.
- **Compatibility with `Collections.shuffle(...)`:**
  - **Yes**. `ArrayList<Song>` implements `java.util.List`, so `Collections.shuffle(playlist)` can be used immediately without collection conversion.
- **Menu Location for Feature:**
  - In `Main.java`, a new menu option (e.g. `4. Shuffle Playlist`) could be inserted into the menu display (lines 43–48), shifting `Exit` to option 5.
- **Integration Risks:**
  - Adding shuffle directly into `Main.main` without refactoring into a `Playlist` class will further degrade code maintainability.
  - Lack of track identity makes preserving playback state post-shuffle error-prone.

---

## 11. Recently Played Integration Readiness

Assessment for **Feature 4: Recently Played Songs**:

- **Definition of "Play Song":**
  - **NOT DEFINED**. The concept of playing a song does not exist anywhere in the codebase.
- **Do Selecting, Displaying, Next, or Previous Count as Playing?**
  - Currently, viewing the playlist (option 3) or adding a song from search does not constitute "playing". Next/previous navigation is not implemented. A distinct playback action must be designed.
- **History Update Hook Location:**
  - A playback method (e.g. `playSong(Song song)` or `selectAndPlay(int index)`) must be created first. The history update hook should be placed inside this playback dispatch method.
- **Stable Identity (`songId`):**
  - **No**. `Song` objects have no `songId` or UUID.
- **`equals()` and `hashCode()` Implementation:**
  - **No**. `Song` inherits default reference identity from `java.lang.Object`.
- **Safe Identification of Duplicate History Entries:**
  - **Unsafe**. Without overriding `equals()`, two distinct `Song` instances representing the same song (e.g., added from search vs. demo) will not be recognized as duplicates.
- **Compatibility with `ArrayDeque<Song>`:**
  - `java.util.ArrayDeque<Song>` is structurally compatible as a bounded LIFO stack or FIFO queue. However, deduplication methods like `deque.remove(song)` rely entirely on `Song.equals()`. Without `equals()`, deduplication will fail to remove previously played instances of the same track.
- **Methods Requiring History Notification:**
  - Currently none exist. Future playback methods (`play`, `playNext`, `playPrevious`) will need to notify the history structure.
- **Integration Risks:**
  - High risk of coupling history tracking directly into `Main.main` if architectural refactoring is omitted.
  - History tracking cannot be completed until the Maintainer defines how songs are selected and played.

---

## 12. Files Changed by This Audit

- `reports/search_feature_audit.md` (newly created audit report)
- `reports/` (newly created directory)

*Confirmation:* **No Java source files, configuration files, or build files were modified, created, deleted, or reformatted during this audit.** The repository working tree remains completely untouched except for this report.

---

## 13. Final Verdict

### **NOT READY — BLOCKERS PRESENT**

The search feature cannot be accepted in its current state. The project fails out-of-the-box compilation under plain `javac` due to unbundled dependencies, the search feature operates on an external web API rather than the local playlist, online searches are fragile due to unhandled HTTP 503 rate limits, terminal inputs are vulnerable to unhandled exceptions, and the codebase lacks core object-oriented structures needed for upcoming features.

---

## 14. Recommended Next Steps

The Maintainer should execute the following steps in order:

1. **Establish Build Portability:**
   - Add the Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`) to the repository so developers can compile and run via `./mvnw clean compile` without requiring a pre-installed system Maven.
2. **Clarify Search Scope & Implement Local Playlist Search:**
   - Resolve whether search was intended to query the user's active playlist or fetch online songs.
   - Implement local in-memory search methods:
     - `searchByTitle(String query)`: case-insensitive partial match against local playlist songs.
     - `searchByArtist(String query)`: case-insensitive partial match against local playlist songs.
   - Retain `MusicAPI` only as an optional "Import Song Online" feature if external discovery is desired.
3. **Refactor Core Object-Oriented Architecture:**
   - Extract `Song` into `src/main/java/org/example/Song.java`.
   - Encapsulate `Song` fields (`private String title, artist, duration`), generate getters, implement `equals()` and `hashCode()`, and add a unique identifier (`songId`).
   - Create a `Playlist` class to manage `List<Song>`, encapsulating add, remove, search, and playback index tracking.
4. **Harden Terminal Input Handling:**
   - Replace unsafe `sc.nextInt()` calls with a robust input helper that catches `InputMismatchException` or parses input with `Integer.parseInt` to prevent crashes on non-numeric input.
5. **Proceed to Shuffle and Recently Played Features:**
   - Once the `Playlist` class and playback concept are established, implement Feature 3 (Shuffle) and Feature 4 (Recently Played) on top of the stabilized foundation.
