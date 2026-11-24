package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Handles online song search and discovery using the MusicBrainz REST API.
 */
public class MusicAPI {

    private static final String API_URL = "https://musicbrainz.org/ws/2/recording/?query=";
    private static final String USER_AGENT = "MusicPlaylistDemo/1.0 (https://github.com/Jagrit-2007/javaSem3Project)";
    private static final int RESULT_LIMIT = 10;
    private static final int MAX_KEYWORD_LENGTH = 200;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Searches MusicBrainz for songs by title or artist.
     *
     * @param keyword the search term (title or artist name)
     * @param choice 1 for title search, 2 for artist search
     * @return a list of matching Song objects (up to RESULT_LIMIT), or an empty list if none found or on error
     */
    public static List<Song> searchSongs(String keyword, int choice) {
        List<Song> songs = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty() || (choice != 1 && choice != 2)) {
            System.out.println("Please enter a valid title or artist.");
            return songs;
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() > MAX_KEYWORD_LENGTH) {
            System.out.println("Search text is too long. Please use 200 characters or fewer.");
            return songs;
        }

        try {
            String escapedKeyword = escapeLuceneQuery(trimmedKeyword);
            String query;
            if (choice == 1) {
                query = "recording:" + escapedKeyword;
            } else {
                query = "artist:" + escapedKeyword;
            }

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = API_URL + encodedQuery + "&fmt=json&limit=" + RESULT_LIMIT;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 400) {
                System.out.println("The online search query could not be processed.");
                return songs;
            } else if (response.statusCode() == 429 || response.statusCode() == 503) {
                System.out.println("Online search is temporarily unavailable due to service limits.");
                return songs;
            } else if (response.statusCode() >= 500 && response.statusCode() <= 599) {
                System.out.println("The online music service is currently unavailable.");
                return songs;
            } else if (response.statusCode() != 200) {
                System.out.println("Online search is temporarily unavailable.");
                return songs;
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!json.has("recordings") || !json.get("recordings").isJsonArray()) {
                System.out.println("No matching songs were found online.");
                return songs;
            }

            JsonArray recordings = json.getAsJsonArray("recordings");
            if (recordings.isEmpty()) {
                System.out.println("No matching songs were found online.");
                return songs;
            }

            int resultCount = Math.min(recordings.size(), RESULT_LIMIT);
            for (int i = 0; i < resultCount; i++) {
                if (!recordings.get(i).isJsonObject()) {
                    continue;
                }
                JsonObject recording = recordings.get(i).getAsJsonObject();

                String title = "Unknown Title";
                if (recording.has("title") && !recording.get("title").isJsonNull()) {
                    String rawTitle = recording.get("title").getAsString();
                    if (rawTitle != null && !rawTitle.trim().isEmpty()) {
                        title = rawTitle.trim();
                    }
                }

                String artist = "Unknown Artist";
                if (recording.has("artist-credit") && recording.get("artist-credit").isJsonArray()) {
                    JsonArray credits = recording.getAsJsonArray("artist-credit");
                    StringBuilder artistName = new StringBuilder();
                    String pendingJoinphrase = null;

                    for (int j = 0; j < credits.size(); j++) {
                        if (!credits.get(j).isJsonObject()) {
                            continue;
                        }
                        JsonObject credit = credits.get(j).getAsJsonObject();
                        String name = null;
                        if (credit.has("name") && !credit.get("name").isJsonNull()) {
                            String rawName = credit.get("name").getAsString();
                            if (rawName != null && !rawName.trim().isEmpty()) {
                                name = rawName.trim();
                            }
                        }

                        if (name != null) {
                            if (artistName.length() > 0) {
                                if (pendingJoinphrase != null) {
                                    artistName.append(pendingJoinphrase);
                                } else {
                                    artistName.append(" ");
                                }
                            }
                            artistName.append(name);
                            pendingJoinphrase = null;

                            if (credit.has("joinphrase") && !credit.get("joinphrase").isJsonNull()) {
                                pendingJoinphrase = credit.get("joinphrase").getAsString();
                            }
                        }
                    }

                    String finalArtist = artistName.toString().trim();
                    if (!finalArtist.isEmpty()) {
                        artist = finalArtist;
                    }
                }

                String duration = "Unknown";
                if (recording.has("length") && !recording.get("length").isJsonNull()) {
                    try {
                        long ms = recording.get("length").getAsLong();
                        if (ms == 0) {
                            duration = "0:00";
                        } else if (ms > 0) {
                            long seconds = ms / 1000;
                            duration = String.format("%d:%02d", seconds / 60, seconds % 60);
                        }
                    } catch (Exception ignored) {
                        duration = "Unknown";
                    }
                }

                songs.add(new Song(title, artist, duration));
            }

            if (songs.isEmpty()) {
                System.out.println("No matching songs were found online.");
            }

        } catch (HttpTimeoutException e) {
            System.out.println("Online search timed out. Please try again.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Could not connect to the online music service.");
        } catch (IOException e) {
            System.out.println("Could not connect to the online music service.");
        } catch (JsonParseException e) {
            System.out.println("Failed to process the response from the online music service.");
        } catch (Exception e) {
            System.out.println("Failed to process the response from the online music service.");
        }

        return songs;
    }

    /**
     * Escapes special Lucene query syntax characters before constructing MusicBrainz queries.
     * Protects + - && || ! ( ) { } [ ] ^ " ~ * ? : \ / while preserving normal letters,
     * numbers, spaces, apostrophes, ampersands, and Unicode text.
     *
     * @param value raw search string
     * @return escaped search string
     */
    private static String escapeLuceneQuery(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // Check for double operators && and ||
            if (c == '&' && i + 1 < value.length() && value.charAt(i + 1) == '&') {
                sb.append("\\&&");
                i++;
                continue;
            }
            if (c == '|' && i + 1 < value.length() && value.charAt(i + 1) == '|') {
                sb.append("\\||");
                i++;
                continue;
            }
            // Check for single Lucene syntax characters:
            // + - ! ( ) { } [ ] ^ " ~ * ? : \ /
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
                    || c == '{' || c == '}' || c == '[' || c == ']' || c == '^'
                    || c == '"' || c == '~' || c == '*' || c == '?' || c == ':'
                    || c == '/') {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
