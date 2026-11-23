package org.example;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MusicAPI {

    public static ArrayList<Song> searchSongs(String keyword, int choice) {

        ArrayList<Song> songs = new ArrayList<>();

        try {

            // Search by title or artist
            String query;

            if (choice == 1) {
                query = "recording:" + keyword;
            } else {
                query = "artist:" + keyword;
            }

            String search = URLEncoder.encode(
                    query,
                    StandardCharsets.UTF_8
            );

            // API URL
            String url = "https://musicbrainz.org/ws/2/recording/?query="
                    + search + "&fmt=json&limit=10";

            // Create HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent",
                            "MusicPlaylistDemo/1.0 (Java learning project)")
                    .GET()
                    .build();

            // Send request
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                System.out.println("API request failed: "
                        + response.statusCode());
                return songs;
            }

            // Convert JSON response
            JsonObject json = JsonParser.parseString(response.body())
                    .getAsJsonObject();

            JsonArray recordings = json.getAsJsonArray("recordings");

            // Extract song details
            for (int i = 0; i < recordings.size(); i++) {

                JsonObject recording = recordings.get(i).getAsJsonObject();

                String title = recording.get("title").getAsString();

                String artist = "Unknown";

                if (recording.has("artist-credit")
                        && recording.get("artist-credit").isJsonArray()) {

                    JsonArray credits =
                            recording.getAsJsonArray("artist-credit");

                    StringBuilder artistName = new StringBuilder();

                    for (int j = 0; j < credits.size(); j++) {

                        JsonObject credit = credits.get(j).getAsJsonObject();

                        if (credit.has("name")) {
                            artistName.append(
                                    credit.get("name").getAsString()
                            );
                        }

                        if (credit.has("joinphrase")) {
                            artistName.append(
                                    credit.get("joinphrase").getAsString()
                            );
                        }
                    }

                    if (artistName.length() > 0) {
                        artist = artistName.toString();
                    }
                }

                String duration = "Unknown";

                if (recording.has("length")
                        && !recording.get("length").isJsonNull()) {

                    long ms = recording.get("length").getAsLong();

                    long seconds = ms / 1000;

                    duration = String.format("%d:%02d",
                            seconds / 60, seconds % 60);
                }

                songs.add(new Song(title, artist, duration));
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        }

        return songs;
    }
}
