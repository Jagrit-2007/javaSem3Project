# API Reliability Fix Report

## Summary
A bounded API reliability hardening pass was applied to `src/main/java/org/example/MusicAPI.java` in the terminal-based Java Music Playlist Engine (`org.example`). This hardening resolves production edge cases identified during the deep audit, specifically addressing HTTP connection resource leaks, missing network request timeouts, Lucene query injection vulnerabilities, unhandled upstream HTTP error status codes, missing response size bounds, malformed/incomplete JSON payloads, and uninformative exception messages. No architecture changes, sorting methods, persistence layers, retry loops, or third-party dependencies were introduced.

## Base Commit
`58b18bef6914bb15ddc7ba3ca74cb2a8e97ba446`

## Files Modified
- `src/main/java/org/example/MusicAPI.java`

*(No modifications were made to `Main.java`, `Playlist.java`, `Song.java`, `InputHelper.java`, `RecentlyPlayedManager.java`, `APITest.java`, or `pom.xml`)*

## Fixes Applied
1. **Reused HTTP Client**: Replaced per-request `HttpClient` allocation with a single `private static final HttpClient HTTP_CLIENT` configured with a 10-second connection timeout (`Duration.ofSeconds(10)`).
2. **Request Timeout**: Added `.timeout(Duration.ofSeconds(10))` to every outbound `HttpRequest` to prevent indefinite thread hangs on unresponsive network sockets.
3. **Lucene Syntax Escaping**: Implemented `escapeLuceneQuery(String value)` to escape all special characters (`+ - && || ! ( ) { } [ ] ^ " ~ * ? : \ /`) before constructing `recording:` and `artist:` query terms, avoiding HTTP 400 Bad Request responses caused by unescaped user input (such as `AC/DC`, quotes, or parentheses).
4. **Input Length Validation**: Enforced a 200-character upper limit on raw search inputs. Inputs exceeding 200 characters are safely rejected with the user message `"Search text is too long. Please use 200 characters or fewer."` without dispatching network calls.
5. **Granular HTTP Status Code Handling**:
   - `400`: Informs user `"The online search query could not be processed."`
   - `429` / `503`: Informs user `"Online search is temporarily unavailable due to service limits."`
   - `500..599`: Informs user `"The online music service is currently unavailable."`
   - Other non-200: Informs user `"Online search is temporarily unavailable."`
6. **Result Capping**: Capped the loop parsing recordings to `Math.min(recordings.size(), RESULT_LIMIT)` (10 results), preventing unbounded object allocation even if upstream responses contain extra items.
7. **Title Parsing Resilience**: Added validation ensuring null, missing, or whitespace-only titles safely fall back to `"Unknown Title"`.
8. **Duration Parsing Resilience**: Validated duration values to handle missing or negative lengths by falling back to `"Unknown"`, accurately formatting `0` ms as `"0:00"`, and parsing valid positive milliseconds into `"m:ss"`.
9. **Artist Credit Assembly**: Robustly merged multi-artist credits. Preserves upstream `joinphrase` values (e.g., `" & "`, `", "`, `" feat. "`), safely falls back to a single space separator (`" "`) when subsequent artists lack a joinphrase, and falls back to `"Unknown Artist"` when credits are missing, empty, or contain only punctuation.
10. **Exception Hierarchy Hardening**:
    - `HttpTimeoutException`: Outputs `"Online search timed out. Please try again."`
    - `InterruptedException`: Restores thread interrupt state via `Thread.currentThread().interrupt()` and outputs `"Could not connect to the online music service."`
    - `IOException`: Outputs `"Could not connect to the online music service."`
    - `JsonParseException` / `Exception`: Outputs `"Failed to process the response from the online music service."`
    - All stack traces are suppressed from standard output.
11. **User-Agent Compliance**: Updated User-Agent header to `MusicPlaylistDemo/1.0 (https://github.com/Jagrit-2007/javaSem3Project)` to adhere to MusicBrainz API documentation guidelines and identify the project.

## HTTP Status Handling
| Status Code | Upstream Meaning | User-Facing Terminal Output | Return Value |
|---|---|---|---|
| `200 OK` | Successful query | Song results formatted and listed | `List<Song>` (max 10) |
| `400 Bad Request` | Malformed Lucene query | `"The online search query could not be processed."` | `List<Song>` (empty) |
| `429 Too Many Requests` | Rate limit triggered (> 1 req/sec) | `"Online search is temporarily unavailable due to service limits."` | `List<Song>` (empty) |
| `503 Service Unavailable` | Service overloaded / throttled | `"Online search is temporarily unavailable due to service limits."` | `List<Song>` (empty) |
| `500..599 Server Error` | MusicBrainz internal server error | `"The online music service is currently unavailable."` | `List<Song>` (empty) |
| Unexpected status (`3xx`, `403`, etc.) | Client / network restriction | `"Online search is temporarily unavailable."` | `List<Song>` (empty) |

## Query Escaping
MusicBrainz uses Apache Lucene syntax for search queries under the `query` parameter. Special characters must be escaped with a leading backslash `\` before URL percent-encoding. The `escapeLuceneQuery` method scans the input and escapes:
`\`, `+`, `-`, `!`, `(`, `)`, `:`, `^`, `[`, `]`, `"`, `{`, `}`, `~`, `*`, `?`, `/`, `&`, `|`
- **AC/DC**: Escaped to `AC\/DC`, URL-encoded to `recording%3AAC%5C%2FDC`.
- **Simon & Garfunkel**: `&` is preserved when single, escaped if doubled (`&&`).
- **Beyoncé - Sigur Rós**: Preserves international Unicode letters (`é`, `ó`) while escaping `-` to `\-`.
- **He said "Hello"**: Double quotes escaped to `\"`.
- **Song (Live)**: Parentheses escaped to `\(` and `\)`.

## JSON Resilience
The Gson parsing workflow in `MusicAPI.java` now safely guards against unexpected schema variations:
- **Missing or empty `recordings` array**: Displays `"No songs found matching your search."` and returns an empty list.
- **Malformed JSON syntax**: Safely caught by `JsonParseException`, outputting `"Failed to process the response from the online music service."` without terminating the JVM.
- **Missing title or blank title**: Replaced with `"Unknown Title"`.
- **Missing or empty `artist-credit`**: Replaced with `"Unknown Artist"`.
- **Punctuation-only artist names**: If name extraction yields no alphanumeric characters, falls back to `"Unknown Artist"`.
- **Missing or negative `length`**: Replaced with `"Unknown"`. `0` ms duration correctly formatted as `"0:00"`.
- **Excess items**: Strictly sliced to at most `10` entries regardless of upstream payload size.

## Verification Results
A comprehensive 30-point test harness was executed against the hardened implementation:

| Test ID | Test Case | Target Checked | Result |
|---|---|---|---|
| 01 | Normal title query | Upstream title query handling | PASS |
| 02 | Normal artist query | Upstream artist query handling | PASS |
| 03 | Null query | Input rejection before network dispatch | PASS |
| 04 | Blank query | Whitespace rejection before network dispatch | PASS |
| 05 | Query > 200 characters | Upper bound query length guard | PASS |
| 06 | Query with parentheses | Lucene escaping for `(` and `)` | PASS |
| 07 | Query with quotes | Lucene escaping for `"` | PASS |
| 08 | Query with colon | Lucene escaping for `:` | PASS |
| 09 | Query with slash | Lucene escaping for `/` (`AC/DC`) | PASS |
| 10 | Unicode query | Preservation of non-ASCII characters | PASS |
| 11 | HTTP 400 handling | User message on bad request | PASS |
| 12 | HTTP 429 handling | Rate-limit user message | PASS |
| 13 | HTTP 500 handling | Server error user message | PASS |
| 14 | HTTP 503 handling | Service limit user message | PASS |
| 15 | Connection failure | Graceful handle on refused socket | PASS |
| 16 | Request timeout | 10s request timeout & user notification | PASS |
| 17 | Empty recordings array | Graceful empty result notification | PASS |
| 18 | Capped at 10 recordings | 15-item payload capped at 10 items | PASS |
| 19 | Missing title fallback | Substitution with "Unknown Title" | PASS |
| 20 | Blank title fallback | Whitespace title substitution | PASS |
| 21 | Missing artist-credit fallback | Substitution with "Unknown Artist" | PASS |
| 22 | Artists without joinphrase | Space-separated concatenation (`A B`) | PASS |
| 23 | Missing name with joinphrase | Punctuation-only fallback to "Unknown Artist"| PASS |
| 24 | Missing duration | Missing length fallback to "Unknown" | PASS |
| 25 | Zero duration | 0 ms formatted to "0:00" | PASS |
| 26 | Negative duration | Negative length fallback to "Unknown" | PASS |
| 27 | Malformed JSON | `JsonParseException` caught with user message| PASS |
| 28 | Local playlist features | Add, remove, shuffle, history, local search operational | PASS |
| 29 | Import workflow | Online search result imported into local Playlist | PASS |
| 30 | Scope check | No sorting or unauthorized methods added | PASS |

**Total Audit Checks: 30 | Passed: 30 | Failed: 0**

## Remaining External Limitations
1. **Upstream Rate Limiting**: MusicBrainz strictly enforces an IP rate limit of approximately 1 request per second. Rapid consecutive queries may trigger HTTP 503 or 429 responses, which are now gracefully reported to the user with actionable instructions.
2. **Network Dependency**: Online searching requires an active internet connection to access `musicbrainz.org`. Connection failures or DNS resolution issues will output `"Could not connect to the online music service."` without crashing.

## Final Verdict
`READY FOR FINAL SUBMISSION`
