package ui;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.loggedOut;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu();

        System.out.println("Welcome to CS240 Chess\n");
        String input = "help";

        while (!Pattern.matches("(?i)quit", input) &&
                !Pattern.matches("(?i)q", input)) {
            menu.displayMessage(input);
            input = scanner.nextLine();
        }
        menu.displayMessage(input);
    }

    private void displayMessage (String input) {
        if (Pattern.matches("(?i)help", input) ||
                Pattern.matches("(?i)h", input)) {
            printHelp();
        } else if (Pattern.matches("(?i)quit", input) ||
                Pattern.matches("(?i)q", input)) {
            printQuit();
        }
    }

    private void printHelp () {
        switch (this.state) {
            case loggedOut:
                System.out.println("Available Commands:");
                System.out.println("help : h");
                System.out.println("quit : q");
                System.out.println("login : l");
                System.out.println("register : r");
                break;
            case loggedIn:
                break;
            case inGame:
                break;
        }

    }

    private enum STATE {
        loggedIn,
        loggedOut,
        inGame
    }

    private void printQuit () {
        System.out.println("Thanks for playing!\n");
    }
}
