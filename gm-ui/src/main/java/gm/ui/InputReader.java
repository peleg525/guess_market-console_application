package gm.ui;

import java.util.Scanner;

/** Collects and validates raw keyboard input. Never crashes on bad input - it just re-prompts. */
class InputReader {

    private final Scanner scanner;

    InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No more input available. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    /** Reads an integer, re-prompting with an explanatory message until valid input is entered. */
    int readInt(String prompt) {
        while (true) {
            String raw = readLine(prompt).trim();
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("\"" + raw + "\" is not a valid whole number. Please try again.");
            }
        }
    }

    /** Reads an integer in the inclusive range [1, maxInclusive], re-prompting until valid. */
    int readSelection(String prompt, int maxInclusive) {
        while (true) {
            int value = readInt(prompt);
            if (value < 1 || value > maxInclusive) {
                System.out.println("Please enter a number between 1 and " + maxInclusive + ".");
                continue;
            }
            return value;
        }
    }

    /** Reads a strictly positive integer, re-prompting until valid. */
    int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value <= 0) {
                System.out.println("Please enter a positive whole number.");
                continue;
            }
            return value;
        }
    }
}
