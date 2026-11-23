package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Manages the history of recently played songs as an event-based history.
 * Newest played songs appear first, up to a configured maximum history size.
 */
public class RecentlyPlayedManager {

    private final Deque<Song> recentlyPlayed;
    private final int maximumHistorySize;

    /**
     * Constructs a RecentlyPlayedManager with the specified maximum history size.
     *
     * @param maximumHistorySize maximum number of history entries to retain (must be > 0)
     * @throws IllegalArgumentException if maximumHistorySize is less than or equal to 0
     */
    public RecentlyPlayedManager(int maximumHistorySize) {
        if (maximumHistorySize <= 0) {
            throw new IllegalArgumentException("Maximum history size must be greater than 0.");
        }
        this.maximumHistorySize = maximumHistorySize;
        this.recentlyPlayed = new ArrayDeque<>();
    }

    /**
     * Records a played song into the history.
     * The newest song is added to the front. If the history size exceeds the maximum,
     * the oldest song is removed. Null songs are ignored.
     *
     * @param song the song that was played
     */
    public void recordPlay(Song song) {
        if (song == null) {
            return;
        }

        recentlyPlayed.addFirst(song);

        if (recentlyPlayed.size() > maximumHistorySize) {
            recentlyPlayed.removeLast();
        }
    }

    /**
     * Returns an unmodifiable snapshot list of recently played songs, newest first.
     *
     * @return unmodifiable list snapshot of recent songs
     */
    public List<Song> getRecentlyPlayed() {
        return Collections.unmodifiableList(new ArrayList<>(recentlyPlayed));
    }

    /**
     * Checks if the history is empty.
     *
     * @return true if no songs have been recorded, false otherwise
     */
    public boolean isEmpty() {
        return recentlyPlayed.isEmpty();
    }

    /**
     * Returns the current number of songs in history.
     *
     * @return count of recorded songs
     */
    public int size() {
        return recentlyPlayed.size();
    }

    /**
     * Returns the configured maximum history size limit.
     *
     * @return maximum history size
     */
    public int getMaximumHistorySize() {
        return maximumHistorySize;
    }

    /**
     * Displays the recently played songs in newest-first order.
     */
    public void displayRecentlyPlayed() {
        System.out.println("\n===== RECENTLY PLAYED =====");
        if (recentlyPlayed.isEmpty()) {
            System.out.println("No songs have been played recently.");
        } else {
            int index = 1;
            for (Song song : recentlyPlayed) {
                System.out.println(index + ". " + song);
                index++;
            }
        }
        System.out.println("===========================");
    }
}
