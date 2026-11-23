package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles online song search and discovery using the MusicBrainz REST API.
 */
public class MusicAPI {

    private static final String API_URL = "https://musicbrainz.org/ws/2/recording/?query=";
    private static final String USER_AGENT = "MusicPlaylistDemo/1.0 (Java learning project)";
    private static final int RESULT_LIMIT = 10;

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

        try {
            String query;
            if (choice == 1) {
                query = "recording:" + trimmedKeyword;
            } else {
                query = "artist:" + trimmedKeyword;
            }

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = API_URL + encodedQuery + "&fmt=json&limit=" + RESULT_LIMIT;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
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

            for (int i = 0; i < recordings.size(); i++) {
                JsonObject recording = recordings.get(i).getAsJsonObject();

                String title = recording.has("title") && !recording.get("title").isJsonNull()
                        ? recording.get("title").getAsString()
                        : "Unknown Title";

                String artist = "Unknown Artist";
                if (recording.has("artist-credit") && recording.get("artist-credit").isJsonArray()) {
                    JsonArray credits = recording.getAsJsonArray("artist-credit");
                    StringBuilder artistName = new StringBuilder();

                    for (int j = 0; j < credits.size(); j++) {
                        JsonObject credit = credits.get(j).getAsJsonObject();
                        if (credit.has("name") && !credit.get("name").isJsonNull()) {
                            artistName.append(credit.get("name").getAsString());
                        }
                        if (credit.has("joinphrase") && !credit.get("joinphrase").isJsonNull()) {
                            artistName.append(credit.get("joinphrase").getAsString());
                        }
                    }

                    if (artistName.length() > 0) {
                        artist = artistName.toString();
                    }
                }

                String duration = "Unknown";
                if (recording.has("length") && !recording.get("length").isJsonNull()) {
                    long ms = recording.get("length").getAsLong();
                    long seconds = ms / 1000;
                    duration = String.format("%d:%02d", seconds / 60, seconds % 60);
                }

                songs.add(new Song(title, artist, duration));
            }

            if (songs.isEmpty()) {
                System.out.println("No matching songs were found online.");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Could not connect to the online music service.");
        } catch (Exception e) {
            System.out.println("Could not connect to the online music service.");
        }

        return songs;
    }
}
