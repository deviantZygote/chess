package client;

import chess.*;
import ui.Menu;

import java.util.regex.Pattern;

public class ClientMain {
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
}
