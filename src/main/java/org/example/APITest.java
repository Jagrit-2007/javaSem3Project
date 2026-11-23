
package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class APITest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== LIVE MUSIC SEARCH =====");
        System.out.println("1. Search by Song Title");
        System.out.println("2. Search by Artist");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter search keyword: ");
        String keyword = sc.nextLine();
        System.out.println("\nSearching online...");
        ArrayList<Song> results =
                MusicAPI.searchSongs(keyword, choice);
        if (results.isEmpty()) {
            System.out.println("No songs found!");
        } else {
            System.out.println("\n===== SEARCH RESULTS =====");
            for (int i = 0; i < results.size(); i++) {
                System.out.println("\nSong " + (i + 1));
                results.get(i).display();
            }
        }
        sc.close();
    }
}
