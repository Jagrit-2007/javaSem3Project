package org.example;

import java.util.List;
import java.util.Scanner;

/**
 * Main application coordinator and terminal interface for the Music Playlist Engine.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Playlist playlist = new Playlist();
        RecentlyPlayedManager recentlyPlayedManager = new RecentlyPlayedManager(10);

        // Populate initial demo songs
        playlist.addSong(new Song("Perfect", "Ed Sheeran", "4:23"));
        playlist.addSong(new Song("Shape of You", "Ed Sheeran", "3:53"));
        playlist.addSong(new Song("Believer", "Imagine Dragons", "3:24"));
        playlist.addSong(new Song("Blinding Lights", "The Weeknd", "3:20"));

        int choice;

        do {
            System.out.println("\n================================");
            System.out.println("      MUSIC PLAYLIST ENGINE     ");
            System.out.println("================================");
            System.out.println("1. Search Song Online by Title");
            System.out.println("2. Search Song Online by Artist");
            System.out.println("3. View Local Playlist");
            System.out.println("4. Play a Song");
            System.out.println("5. Shuffle Playlist");
            System.out.println("6. View Recently Played");
            System.out.println("7. Add Song Manually");
            System.out.println("8. Remove Song");
            System.out.println("9. Next Song");
            System.out.println("10. Previous Song");
            System.out.println("0. Exit");
            System.out.println("================================");

            choice = InputHelper.readInt(scanner, "Enter your choice: ");

            switch (choice) {
                case 1:
                    handleOnlineSearch(scanner, playlist, 1);
                    break;
                case 2:
                    handleOnlineSearch(scanner, playlist, 2);
                    break;
                case 3:
                    playlist.displayPlaylist();
                    break;
                case 4:
                    handlePlaySong(scanner, playlist, recentlyPlayedManager);
                    break;
                case 5:
                    handleShufflePlaylist(playlist);
                    break;
                case 6:
                    recentlyPlayedManager.displayRecentlyPlayed();
                    break;
                case 7:
                    handleManualAddSong(scanner, playlist);
                    break;
                case 8:
                    handleRemoveSong(scanner, playlist);
                    break;
                case 9:
                    handlePlayNext(playlist, recentlyPlayedManager);
                    break;
                case 10:
                    handlePlayPrevious(playlist, recentlyPlayedManager);
                    break;
                case 0:
                    System.out.println("Thank you for using Music Playlist Engine!");
                    break;
                default:
                    System.out.println("Invalid choice! Please select an option from the menu.");
                    break;
            }

        } while (choice != 0);

        scanner.close();
    }

    /**
     * Coordinates playing a song from the playlist and records it to recent history.
     *
     * @param scanner the terminal scanner
     * @param playlist the local playlist
     * @param recentlyPlayedManager the history manager
     */
    private static void handlePlaySong(Scanner scanner, Playlist playlist, RecentlyPlayedManager recentlyPlayedManager) {
        if (playlist.isEmpty()) {
            System.out.println("Playlist is empty.");
            return;
        }

        playlist.displayPlaylist();
        int songNumber = InputHelper.readInt(scanner, "Enter song number to play: ");
        Song playedSong = playlist.playSong(songNumber);

        if (playedSong == null) {
            System.out.println("Invalid song number.");
        } else {
            System.out.println("Now Playing: " + playedSong);
            recentlyPlayedManager.recordPlay(playedSong);
        }
    }

    /**
     * Coordinates shuffling the playlist and displaying the result.
     *
     * @param playlist the local playlist to shuffle
     */
    private static void handleShufflePlaylist(Playlist playlist) {
        if (playlist.isEmpty()) {
            playlist.shufflePlaylist();
            System.out.println("Cannot shuffle an empty playlist.");
        } else if (playlist.size() == 1) {
            playlist.shufflePlaylist();
            System.out.println("At least two songs are required to shuffle.");
        } else {
            boolean success = playlist.shufflePlaylist();
            if (success) {
                System.out.println("Playlist shuffled successfully.");
                playlist.displayPlaylist();
            }
        }
    }

    /**
     * Coordinates the online search flow and allows adding a selected song to the playlist.
     *
     * @param scanner the terminal scanner
     * @param playlist the local playlist to receive songs
     * @param searchType 1 for title search, 2 for artist search
     */
    private static void handleOnlineSearch(Scanner scanner, Playlist playlist, int searchType) {
        String keyword = InputHelper.readNonEmptyString(scanner, "Enter search keyword: ");

        System.out.println("\nSearching online...");
        List<Song> results = MusicAPI.searchSongs(keyword, searchType);

        if (results.isEmpty()) {
            return;
        }

        System.out.println("\n===== SEARCH RESULTS =====");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }

        int selection = InputHelper.readInt(scanner, "\nEnter song number to add (0 to cancel): ");

        if (selection == 0) {
            System.out.println("Song not added.");
        } else if (selection >= 1 && selection <= results.size()) {
            Song selected = results.get(selection - 1);
            playlist.addSong(selected);
            System.out.println("\nSong added to your playlist: " + selected);
        } else {
            System.out.println("Invalid song number! Selection cancelled.");
        }
    }

    /**
     * Coordinates manually adding a song to the playlist.
     *
     * @param scanner the terminal scanner
     * @param playlist the local playlist
     */
    private static void handleManualAddSong(Scanner scanner, Playlist playlist) {
        String title = InputHelper.readString(scanner, "Enter song title: ");
        String artist = InputHelper.readString(scanner, "Enter artist name: ");
        String duration = InputHelper.readString(scanner, "Enter duration (m:ss): ");

        if (title.trim().isEmpty() || artist.trim().isEmpty() || !isValidDuration(duration.trim())) {
            System.out.println("Failed to add song. Title and artist cannot be blank, and duration must be in m:ss format.");
            return;
        }

        Song song = new Song(title.trim(), artist.trim(), duration.trim());
        boolean added = playlist.addSong(song);
        if (added) {
            System.out.println("Song added successfully: " + song);
        } else {
            System.out.println("Failed to add song.");
        }
    }

    /**
     * Validates whether a duration string adheres to m:ss format.
     *
     * @param duration the duration string to validate
     * @return true if valid m:ss format, false otherwise
     */
    private static boolean isValidDuration(String duration) {
        if (duration == null) {
            return false;
        }
        return duration.matches("^\\d+:[0-5]\\d$");
    }

    /**
     * Coordinates removing a song from the playlist by user-selected song number.
     *
     * @param scanner the terminal scanner
     * @param playlist the local playlist
     */
    private static void handleRemoveSong(Scanner scanner, Playlist playlist) {
        if (playlist.isEmpty()) {
            System.out.println("Playlist is empty.");
            return;
        }

        playlist.displayPlaylist();
        int songNumber = InputHelper.readInt(scanner, "Enter song number to remove: ");
        Song removedSong = playlist.removeSong(songNumber);

        if (removedSong == null) {
            System.out.println("Invalid song number.");
        } else {
            System.out.println("Removed: " + removedSong);
        }
    }
    
    /**
     * Coordinates moving to the next song and records it to recent history.
     *
     * @param playlist the local playlist
     * @param recentlyPlayedManager the history manager
     */
    private static void handlePlayNext(Playlist playlist, RecentlyPlayedManager recentlyPlayedManager) {
        Song nextSong = playlist.playNext();

        if (nextSong == null) {
            System.out.println("Playlist is empty.");
        } else {
            System.out.println("Now Playing: " + nextSong);
            recentlyPlayedManager.recordPlay(nextSong);
        }
    }

    /**
     * Coordinates moving to the previous song and records it to recent history.
     *
     * @param playlist the local playlist
     * @param recentlyPlayedManager the history manager
     */
    private static void handlePlayPrevious(Playlist playlist, RecentlyPlayedManager recentlyPlayedManager) {
        Song previousSong = playlist.playPrevious();

        if (previousSong == null) {
            System.out.println("Playlist is empty.");
        } else {
            System.out.println("Now Playing: " + previousSong);
            recentlyPlayedManager.recordPlay(previousSong);
        }
    }
}
