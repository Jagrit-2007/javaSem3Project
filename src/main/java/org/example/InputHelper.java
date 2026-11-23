package org.example;

import java.util.Scanner;

/**
 * Utility class for safe terminal input handling to prevent runtime crashes.
 */
public class InputHelper {

    /**
     * Safely reads an integer from the scanner, re-prompting on invalid input.
     *
     * @param scanner the terminal scanner
     * @param prompt the prompt to display to the user
     * @return a valid integer entered by the user
     */
    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    /**
     * Safely reads a non-empty string from the scanner, re-prompting on blank input.
     *
     * @param scanner the terminal scanner
     * @param prompt the prompt to display to the user
     * @return a non-empty, trimmed string
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    /**
     * Safely reads a line of text from the scanner without enforcing non-empty input.
     *
     * @param scanner the terminal scanner
     * @param prompt the prompt to display to the user
     * @return the string entered by the user, or empty string on EOF
     */
    public static String readString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }
}
