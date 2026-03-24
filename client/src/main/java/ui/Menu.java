package ui;

import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import model.*;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.LOGGED_OUT;
    public final Scanner scanner;
    private final ServerFacade serverFacade;
    private String authToken = "";
    private ChessGame.TeamColor teamColor = null;
    private ChessGame chessGame = null;

    public Menu () {
        this.scanner = new Scanner(System.in);
        this.serverFacade = new ServerFacade("http://localhost:8080");
    }

    private void setChessGame(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    private ChessGame getChessGame() {
        return this.chessGame;
    }

    private void setTeamColor(ChessGame.TeamColor color) {
        this.teamColor = color;
    }

    private ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
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
        } else if (Pattern.matches("(?i)^(cg|create\\s+game)\\s+(\\S+)$", input)) {
            createGame(input);
        } else if (Pattern.matches("(?i)^(lg|list)$", input)) {
            listGames();
        } else if (Pattern.matches("(?i)^(jg|join\\s+game)\\s+(\\d+)\\s+(white|black)$", input)) {
            joinGame(input);
        } else if (Pattern.matches("(?i)^(gb|game\\s+browser)$", input)) {
            this.setState(STATE.LOGGED_IN);
            this.setTeamColor(null);
            System.out.print("\n\nReturning to game browser\n\n");
        }else if (Pattern.matches("(?i)^(db|draw\\s+board)$", input)) {
            // I need to draw the board based on the state of the game and the player color.
            drawBoard();
        }  else {
            System.out.println("Your input failed to match an option");
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
            System.out.printf("\nHello %s. You are registered and logged in.\n\nType help for options\n\n", username);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
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
                System.out.println("join game : jg <gameId> <color|c>");
                System.out.println("watch game : wg <gameId>");
                break;
            case IN_GAME:
                System.out.println("Available Commands:");
                System.out.println("draw board : db");
                System.out.println("game browser : gb");
                System.out.println("logout : lo");
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
            System.out.printf("\nHello %s you're logged in.\n\nType help for options\n\n", username);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
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

    private void createGame(String input) {
        try {
            String gameName = "";
            String[] args = input.split(" ");
            if (args.length > 2) {
                gameName = args[2];
            } else if (args.length == 1) {
                System.out.print("\n\nYou don't have a game name specified, try again.\n\n");
            } else if (args.length == 2) {
                gameName = args[1];
            }
            CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
            CreateGameResponse gameResponse = this.serverFacade.createGame(createGameRequest, this.getAuthToken());
            System.out.printf("\nGame %s created with id: %s\n\n",gameName, gameResponse.gameID);
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listGames() {
        try {
            GetGamesResponse getGamesResponse = this.serverFacade.getGames(this.getAuthToken());
            for (GameData game : getGamesResponse.games) {
                System.out.printf("\nGame ID: %s\n", game.getGameID());
                System.out.printf("Game Name: %s\n", game.getGameName());

                if (game.getWhiteUsername() == null) {
                    System.out.print("White Player: OPEN\n");
                } else {
                    System.out.printf("White Player: %s\n", game.getWhiteUsername());
                }

                if (game.getBlackUsername() == null) {
                    System.out.print("Black Player: OPEN\n");
                } else {
                    System.out.printf("Black Player: %s\n", game.getBlackUsername());
                }
            }
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    private void joinGame(String input) {
        try {
            Pattern pattern = Pattern.compile("(?i)^(jg|join\\s+game)\\s+(\\d+)\\s+(white|black|w|b)$");
            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                int gameId = Integer.parseInt(matcher.group(2));
                String colorInput = matcher.group(3).toLowerCase();

                ChessGame.TeamColor color;

                if (colorInput.equals("w") || colorInput.equals("white")) {
                    color = ChessGame.TeamColor.WHITE;
                } else {
                    color = ChessGame.TeamColor.BLACK;
                }

                JoinGameRequest request = new JoinGameRequest(color, gameId);
                serverFacade.joinGame(request, getAuthToken());

                setState(STATE.IN_GAME);
                setTeamColor(color);
                drawBoard();
                System.out.printf("\n\nJoined Game: %s\nPress h for commands:\n\n", gameId);
            }

        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    private void drawBoard() {
        // will probably change this to a socket game state but for now will just initialize.
        setChessGame(new ChessGame());
        if (this.teamColor == ChessGame.TeamColor.WHITE) {
            drawWhiteOrientation();
        } else {
            drawBlackOrientation();
        }
    }

    private void drawWhiteOrientation() {
        var board = getChessGame().getBoard();

        System.out.print(EscapeSequences.ERASE_SCREEN);

        printColumnHeadersWhite();

        for (int row = 8; row >= 1; row--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println(" " + row + " ");
        }

        printColumnHeadersWhite();
        resetColors();
    }

    private void drawBlackOrientation() {
        var board = getChessGame().getBoard();

        System.out.print(EscapeSequences.ERASE_SCREEN);

        printColumnHeadersBlack();

        for (int row = 1; row <= 8; row++) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.print(" " + row + " ");
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println(" " + row + " ");
        }

        printColumnHeadersBlack();
        resetColors();
    }

    private void printSquare(chess.ChessBoard board, int row, int col) {
        boolean lightSquare = (row + col) % 2 == 0;

        if (lightSquare) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        } else {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        }

        System.out.print(getPieceString(board, row, col));
    }

    private String getPieceString(chess.ChessBoard board, int row, int col) {
        chess.ChessPiece piece = board.getPiece(new chess.ChessPosition(row, col));

        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        String result = EscapeSequences.EMPTY;

        switch (piece.getTeamColor()) {
            case WHITE:
                switch (piece.getPieceType()) {
                    case KING:
                        result = EscapeSequences.WHITE_KING;
                        break;
                    case QUEEN:
                        result = EscapeSequences.WHITE_QUEEN;
                        break;
                    case BISHOP:
                        result = EscapeSequences.WHITE_BISHOP;
                        break;
                    case KNIGHT:
                        result = EscapeSequences.WHITE_KNIGHT;
                        break;
                    case ROOK:
                        result = EscapeSequences.WHITE_ROOK;
                        break;
                    case PAWN:
                        result = EscapeSequences.WHITE_PAWN;
                        break;
                    default:
                        result = EscapeSequences.EMPTY;
                        break;
                }
                break;

            case BLACK:
                switch (piece.getPieceType()) {
                    case KING:
                        result = EscapeSequences.BLACK_KING;
                        break;
                    case QUEEN:
                        result = EscapeSequences.BLACK_QUEEN;
                        break;
                    case BISHOP:
                        result = EscapeSequences.BLACK_BISHOP;
                        break;
                    case KNIGHT:
                        result = EscapeSequences.BLACK_KNIGHT;
                        break;
                    case ROOK:
                        result = EscapeSequences.BLACK_ROOK;
                        break;
                    case PAWN:
                        result = EscapeSequences.BLACK_PAWN;
                        break;
                    default:
                        result = EscapeSequences.EMPTY;
                        break;
                }
                break;

            default:
                result = EscapeSequences.EMPTY;
                break;
        }

        return result;
    }

    private void printColumnHeadersWhite() {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.print("   ");
        System.out.print(" a ");
        System.out.print(" b ");
        System.out.print(" c ");
        System.out.print(" d ");
        System.out.print(" e ");
        System.out.print(" f ");
        System.out.print(" g ");
        System.out.print(" h ");
        System.out.println();
    }

    private void printColumnHeadersBlack() {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.print("   ");
        System.out.print(" h ");
        System.out.print(" g ");
        System.out.print(" f ");
        System.out.print(" e ");
        System.out.print(" d ");
        System.out.print(" c ");
        System.out.print(" b ");
        System.out.print(" a ");
        System.out.println();
    }

    private void resetColors() {
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void printQuit () {
        System.out.println("Thanks for playing!\n");
    }
}
