package ui;

import client.ResponseException;
import client.ServerFacade;
import model.LoginRequest;
import model.LoginResponse;
import model.RegisterRequest;
import model.RegisterResponse;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.LOGGED_OUT;
    public final Scanner scanner;
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

    public void printPrompt() {
        switch (this.getState()) {
            case LOGGED_OUT:
                System.out.print("\nSTART MENU: ");
                break;
            case LOGGED_IN:
                System.out.print("\nGAME BROWSER: ");
                break;
            case IN_GAME:
                System.out.print("\nGAME MENU: ");
                break;
        }
    }

    public void processInput (String input) {
        if (Pattern.matches("(?i)help", input) ||
                Pattern.matches("(?i)h", input)) {
            printHelp();
        } else if (Pattern.matches("(?i)quit", input) ||
                Pattern.matches("(?i)q", input)) {
            printQuit();
        } else if (Pattern.matches("(?i)login", input) ||
                Pattern.matches("(?i)li", input)) {
            login();
        } else if (Pattern.matches("(?i)register", input) ||
                Pattern.matches("(?i)r", input)) {
            register();
        } else if (Pattern.matches("(?i)logout", input) ||
                Pattern.matches("(?i)lo", input)) {
            logout();
        }
    }

    private void register() {

        System.out.println("Enter your email");
        String email = scanner.nextLine();

        System.out.println("Enter a new username");
        String username = scanner.nextLine();


        String password = "";
        String confirmPassword = "";
        do {
            System.out.println("Enter a new password");
            password = scanner.nextLine();
            System.out.println("re-enter your password");
            confirmPassword = scanner.nextLine();

            if (!confirmPassword.equals(password) ||
                    password.isEmpty() ||
                    confirmPassword.isEmpty()) {
                System.out.println("Your passwords aren't matching or are empty.\nTry again.\n");
            }
        } while (!confirmPassword.equals(password) ||
                password.isEmpty() ||
                confirmPassword.isEmpty());

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);

        try {
            RegisterResponse registerResponse = this.serverFacade.register(registerRequest);
            setAuthToken(registerResponse.authToken);
            setState(STATE.LOGGED_IN);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
        System.out.printf("\nHello %s. You are registered and logged in.\n\nType help for options\n\n", username);
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

    private void login() {
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
        System.out.printf("\nHello %s you're logged in.\n\nType help for options\n\n", username);
    }

    public enum STATE {
        LOGGED_IN,
        LOGGED_OUT,
        IN_GAME
    }

    private void logout() {
        try {
            this.serverFacade.logout(getAuthToken());
            setAuthToken("");
            setState(STATE.LOGGED_OUT);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
        System.out.print("\nGoodbye, you're logged out.\n\nType help for options\n\n");
    }

    private void printQuit () {
        System.out.println("Thanks for playing!\n");
    }
}
