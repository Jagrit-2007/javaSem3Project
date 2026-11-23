package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the local playlist and its songs.
 */
public class Playlist {

    private final List<Song> songs;

    public Playlist() {
        this.songs = new ArrayList<>();
    }

    /**
     * Adds a song to the playlist in insertion order.
     *
     * @param song the song to add
     * @return true if the song was added, false if null
     */
    public boolean addSong(Song song) {
        if (song == null) {
            return false;
        }
        return songs.add(song);
    }

    /**
     * Returns an unmodifiable view of the songs in the playlist.
     *
     * @return unmodifiable list of songs
     */
    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public int size() {
        return songs.size();
    }

    /**
     * Displays all songs in the local playlist with numbers.
     */
    public void displayPlaylist() {
        System.out.println("\n===== LOCAL PLAYLIST =====");
        if (songs.isEmpty()) {
            System.out.println("Your playlist is empty!");
        } else {
            for (int i = 0; i < songs.size(); i++) {
                System.out.println((i + 1) + ". " + songs.get(i));
            }
        }
        System.out.println("==========================");
    }
}
