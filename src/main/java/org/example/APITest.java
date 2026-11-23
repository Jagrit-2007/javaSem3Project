package org.example;

import java.util.List;
import java.util.Scanner;

/**
 * Manual test utility to verify the MusicBrainz API integration.
 * This is a temporary test harness and does not contain production playlist logic.
 */
public class APITest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===== LIVE MUSIC SEARCH TEST =====");
        System.out.println("1. Search by Song Title");
        System.out.println("2. Search by Artist");

        int choice = InputHelper.readInt(scanner, "Enter choice: ");

        if (choice != 1 && choice != 2) {
            System.out.println("Invalid choice. Exiting test.");
            scanner.close();
            return;
        }

        String keyword = InputHelper.readNonEmptyString(scanner, "Enter search keyword: ");

        System.out.println("\nSearching online...");
        List<Song> results = MusicAPI.searchSongs(keyword, choice);

        if (!results.isEmpty()) {
            System.out.println("\n===== SEARCH RESULTS =====");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }

        scanner.close();
    }
}
