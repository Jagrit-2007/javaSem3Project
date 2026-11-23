package org.example;
import java.util.ArrayList;
import java.util.Scanner;

class Song {

    String title;
    String artist;
    String duration;

    Song(String title, String artist, String duration) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    void display() {
        System.out.println("Title: " + title);
        System.out.println("Artist: " + artist);
        System.out.println("Duration: " + duration);
        System.out.println("----------------------");
    }
}

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ArrayList<Song> playlist = new ArrayList<>();

        // Demo songs
        playlist.add(new Song("Perfect", "Ed Sheeran", "4:23"));
        playlist.add(new Song("Shape of You", "Ed Sheeran", "3:53"));
        playlist.add(new Song("Believer", "Imagine Dragons", "3:24"));
        playlist.add(new Song("Blinding Lights", "The Weeknd", "3:20"));

        int choice;

        do {

            System.out.println("\n===== MUSIC PLAYLIST =====");
            System.out.println("1. Search Songs by Title");
            System.out.println("2. Search Songs by Artist");
            System.out.println("3. View My Playlist");
            System.out.println("4. Exit");

            System.out.print("\nEnter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1 || choice == 2) {

                System.out.print("Enter search keyword: ");
                String keyword = sc.nextLine();

                System.out.println("\nSearching online...");

                // Fetch songs from API
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

                    System.out.print(
                            "Enter song number to add (0 to skip): "
                    );

                    int select = sc.nextInt();
                    sc.nextLine();

                    if (select >= 1 && select <= results.size()) {

                        Song selected = results.get(select - 1);

                        playlist.add(selected);

                        System.out.println(
                                "\nSong added to your playlist!"
                        );

                    } else if (select == 0) {

                        System.out.println("Song not added.");

                    } else {

                        System.out.println("Invalid song number!");
                    }
                }

            } else if (choice == 3) {

                System.out.println("\n===== MY PLAYLIST =====");

                if (playlist.isEmpty()) {

                    System.out.println("Your playlist is empty!");

                } else {

                    for (Song s : playlist) {
                        s.display();
                    }
                }

            } else if (choice == 4) {

                System.out.println("Thank you for using Music Playlist!");

            } else {

                System.out.println("Invalid choice!");
            }

        } while (choice != 4);

        sc.close();
    }
}
