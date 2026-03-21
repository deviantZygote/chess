package ui;

import java.util.Scanner;

public class Menu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String input = "";

        while (!input.equals("quit")) {
            System.out.print("> ");
            input = scanner.nextLine();
            System.out.println(input + ".");
        }

        System.out.println("Goodbye");
    }
}
