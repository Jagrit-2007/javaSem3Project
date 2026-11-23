package org.example;

import java.util.Objects;

/**
 * Represents a single song in the music playlist engine.
 */
public class Song {

    private final String title;
    private final String artist;
    private final String duration;

    public Song(String title, String artist, String duration) {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(artist, "Artist cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");

        this.title = title.trim();
        this.artist = artist.trim();
        this.duration = duration.trim();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return title + " - " + artist + " [" + duration + "]";
    }
}
