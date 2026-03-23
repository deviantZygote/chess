package ui;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.loggedOut;
    private final Scanner scanner;

    public Menu () {
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Menu menu = new Menu();

        System.out.println("Welcome to CS240 Chess\n");
        String input = "help";

        while (!Pattern.matches("(?i)quit", input) &&
                !Pattern.matches("(?i)q", input)) {
            menu.processInput(input);
            input = menu.scanner.nextLine();
        }
        menu.processInput(input);
    }

    private void processInput (String input) {
        if (Pattern.matches("(?i)help", input) ||
                Pattern.matches("(?i)h", input)) {
            printHelp();
        } else if (Pattern.matches("(?i)quit", input) ||
                Pattern.matches("(?i)q", input)) {
            printQuit();
        } else if (Pattern.matches("(?i)login", input) ||
                Pattern.matches("(?i)li", input)) {
            login(scanner);
        }
    }



    private void printHelp () {
        switch (this.state) {
            case loggedOut:
                System.out.println("Available Commands:");
                System.out.println("help : h");
                System.out.println("quit : q");
                System.out.println("login : li");
                System.out.println("register : r");
                break;
            case loggedIn:
                System.out.println("Available Commands:");
                System.out.println("help : h");
                System.out.println("logout : lo");
                System.out.println("create game : cg <gameName>");
                System.out.println("list games : lg");
                System.out.println("join game : jg <gameId>");
                System.out.println("watch game : wg <gameId>");
                break;
            case inGame:
                break;
        }

    }

    private void login(Scanner scanner) {
        System.out.println("Enter your username");
        String username = scanner.nextLine();
        System.out.println("Enter your password");
        String password = scanner.nextLine();

        System.out.printf("Username: %s\nPassword: %s", username, password);

    }

    private enum STATE {
        LOGGED_IN,
        LOGGED_OUT,
        IN_GAME
    }

    private void printQuit () {
        System.out.println("Thanks for playing!\n");
    }
}
