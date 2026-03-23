package ui;

import client.ResponseException;
import client.ServerFacade;
import model.LoginRequest;
import model.LoginResponse;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.LOGGED_OUT;
    private final Scanner scanner;
    private final ServerFacade serverFacade;
    private String authToken = "";

    public Menu () {
        this.scanner = new Scanner(System.in);
        this.serverFacade = new ServerFacade("http://localhost:8080");
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public STATE getState () {
        return this.state;
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
            case LOGGED_OUT:
                System.out.println("Available Commands:");
                System.out.println("help : h");
                System.out.println("quit : q");
                System.out.println("login : li");
                System.out.println("register : r");
                break;
            case LOGGED_IN:
                System.out.println("Available Commands:");
                System.out.println("help : h");
                System.out.println("logout : lo");
                System.out.println("create game : cg <gameName>");
                System.out.println("list games : lg");
                System.out.println("join game : jg <gameId>");
                System.out.println("watch game : wg <gameId>");
                break;
            case IN_GAME:
                break;
        }

    }

    private void login(Scanner scanner) {
        System.out.println("Enter your username");
        String username = scanner.nextLine();
        System.out.println("Enter your password");
        String password = scanner.nextLine();

        LoginRequest loginRequest = new LoginRequest(username, password);

        try {
            LoginResponse loginResponse = this.serverFacade.login(loginRequest);
            setAuthToken(loginResponse.authToken);
            setState(STATE.LOGGED_IN);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
        System.out.printf("Username: %s\nPassword: %s", username, password);

    }

    public enum STATE {
        LOGGED_IN,
        LOGGED_OUT,
        IN_GAME
    }

    private void printQuit () {
        System.out.println("Thanks for playing!\n");
    }
}
