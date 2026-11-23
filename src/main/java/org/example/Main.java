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
}
