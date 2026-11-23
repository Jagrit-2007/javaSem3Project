package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the local playlist and its songs.
 */
public class Playlist {

    private final List<Song> songs;
    private int currentIndex = -1;

    public Playlist() {
        this.songs = new ArrayList<>();
    }

    /**
     * Adds a song to the playlist in insertion order.
     * Adding a song does not alter the currentIndex or record playback.
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
     * Removes a song from the playlist using a 1-based song number.
     * Updates currentIndex according to playback preservation rules:
     * - If the last remaining song is removed: currentIndex = -1
     * - If a song before the current song is removed: currentIndex--
     * - If the current song is removed:
     *     - if no songs remain: currentIndex = -1
     *     - if another song now occupies the same index: keep that index
     *     - if the removed current song was the final item: move currentIndex to songs.size() - 1
     * - If a song after the current song is removed: currentIndex remains unchanged
     *
     * Removing a song does not modify RecentlyPlayedManager.
     *
     * @param songNumber 1-based song number
     * @return the removed Song, or null if invalid or playlist is empty
     */
    public Song removeSong(int songNumber) {
        if (songs.isEmpty() || songNumber < 1 || songNumber > songs.size()) {
            return null;
        }

        int index = songNumber - 1;
        Song removedSong = songs.remove(index);

        if (songs.isEmpty()) {
            currentIndex = -1;
        } else if (index < currentIndex) {
            currentIndex--;
        } else if (index == currentIndex) {
            if (currentIndex >= songs.size()) {
                currentIndex = songs.size() - 1;
            }
        }

        return removedSong;
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
     * Returns the currently playing song, or null if no song is selected.
     *
     * @return current Song or null
     */
    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < songs.size()) {
            return songs.get(currentIndex);
        }
        return null;
    }

    /**
     * Returns the current playback index (0-based) or -1 if no song is selected.
     *
     * @return current index or -1
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Selects and plays a song using a 1-based user-facing song number.
     * Converts to 0-based index internally and updates currentIndex.
     * Does not print output and does not update recently played history.
     *
     * @param songNumber 1-based song number
     * @return the selected Song, or null if invalid or playlist is empty
     */
    public Song playSong(int songNumber) {
        int index = songNumber - 1;
        if (songs.isEmpty() || index < 0 || index >= songs.size()) {
            return null;
        }
        this.currentIndex = index;
        return songs.get(index);
    }

    /**
     * Randomly reorders the songs in the playlist.
     * If the playlist has 0 or 1 song, returns false without shuffling.
     * If 2 or more songs, reorders with Collections.shuffle.
     * If a current song is selected, preserves the same Song object reference
     * and updates currentIndex to its new position.
     *
     * @return true if shuffled successfully, false if 0 or 1 song
     */
    public boolean shufflePlaylist() {
        if (songs.size() < 2) {
            return false;
        }

        Song currentSong = getCurrentSong();
        Collections.shuffle(songs);

        if (currentSong != null) {
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i) == currentSong) {
                    currentIndex = i;
                    break;
                }
            }
        }

        return true;
    }

    /**
     * Displays all songs in the local playlist with numbers.
     * If a song is currently selected/playing, marks it with "  <-- Now Playing".
     */
    public void displayPlaylist() {
        System.out.println("\n===== LOCAL PLAYLIST =====");
        if (songs.isEmpty()) {
            System.out.println("Your playlist is empty!");
        } else {
            for (int i = 0; i < songs.size(); i++) {
                if (i == currentIndex) {
                    System.out.println((i + 1) + ". " + songs.get(i) + "  <-- Now Playing");
                } else {
                    System.out.println((i + 1) + ". " + songs.get(i));
                }
            }
        }
        System.out.println("==========================");
    }
}
