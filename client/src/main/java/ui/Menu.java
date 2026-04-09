package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.ResponseException;
import client.ServerFacade;
import client.WebSocketConnectionException;
import client.WebSocketFacade;
import model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Menu {
    private STATE state = STATE.LOGGED_OUT;
    public final Scanner scanner;
    private final ServerFacade serverFacade;
    private String authToken = "";
    private ChessGame.TeamColor teamColor = null;
    private ChessGame chessGame = null;
    private GetGamesResponse gamesResponse = null;
    private WebSocketFacade webSocketFacade;
    private final String serverUrl;
    private final Object consoleLock = new Object();
    private GameData targetGameData = null;
    private Set<ChessPosition> highlightedSquares = new HashSet<>();






    public Menu () {
        this.scanner = new Scanner(System.in);
        serverUrl = "http://localhost:8080";
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public void setChessGame(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public ChessGame getChessGame() {
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

    public void updateGame(ChessGame chessGame) {
        synchronized (consoleLock) {
            setChessGame(chessGame);
            printToTerminal("\n");
            drawBoard();
            printPrompt();
        }
    }

    public void printToTerminal(String message) {
        synchronized (consoleLock) {
            System.out.print(message);
        }
    }

    public void printPrompt() {
        synchronized (consoleLock) {
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
                case WATCH_GAME:
                    System.out.print("\nOBSERVER MENU: ");
                    break;

            }
        }
    }

    public void printAsyncMessage(String message) {
        synchronized (consoleLock) {
            System.out.print("\n" + message);
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
                case WATCH_GAME:
                    System.out.print("\nOBSERVER MENU: ");
                    break;
            }
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
        } else if (Pattern.matches("(?i)^(lm|leave\\s+match)$", input)) {
            leaveMatch();
        } else if (Pattern.matches("(?i)^(db|draw\\s+board)$", input)) {
            drawBoard();
        } else if (Pattern.matches("(?i)^(wg|watch\\s+game)\\s+(\\d+)$", input)) {
            watchGame(input);
        } else if (Pattern.matches("(?i)^(spt|show\\s+player\\s+turn)$", input)) {
            showPlayerTurn();
        } else if (Pattern.matches("(?i)^(spm|show\\s+piece\\s+moves)\\s+(\\S+)$", input)) {
            showPieceMoves(input);
        }  else if (Pattern.matches("(?i)^(mp|move\\s+piece)\\s+(\\S+)\\s+(\\S+)$", input)) {
            makeMove(input);
        }  else {
            printToTerminal("Your input failed to match an option");
        }
    }

    private void makeMove(String input) {

        Pattern pattern = Pattern.compile("(?i)^(mp|move\\s+piece)\\s+(\\S+)\\s+(\\S+)$");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            printToTerminal("\nUsage: mp <from> <to>\n");
            return;
        }

        ChessPosition start = convertStringToChessPosition(matcher.group(2));
        ChessPosition end = convertStringToChessPosition(matcher.group(3));

        if (start == null || end == null) {
            printToTerminal("\nInvalid coordinates.\n");
            return;
        }

        if (this.getTeamColor() == null) {
            printToTerminal("\nObservers can't make moves.\n");
            return;
        }

        if (this.getTeamColor() != this.getChessGame().getTeamTurn()) {
            printToTerminal("\nIt's not your turn. Please wait.\n");
            return;
        }

        ChessPiece chessPiece = this.getChessGame().getBoard().getPiece(start);
        if (chessPiece == null) {
            printToTerminal("\nNo piece at that position.\n");
            return;
        }

        if (chessPiece.getTeamColor() != this.getTeamColor()) {
            printToTerminal("\nYou can only move your own pieces.\n");
            return;
        }

        Collection<ChessMove> validMoves = getChessGame().validMoves(start);
        if (validMoves == null || validMoves.isEmpty()) {
            printToTerminal("\nNo valid moves for that piece.\n");
            return;
        }

        List<ChessMove> matchingMoves = new ArrayList<>();
        for (ChessMove move : validMoves) {
            if (move.getEndPosition().equals(end)) {
                matchingMoves.add(move);
            }
        }

        if (matchingMoves.isEmpty()) {
            printToTerminal("\nInvalid move.\n");
            return;
        }

        ChessMove selectedMove;

        if (matchingMoves.size() == 1) {
            selectedMove = matchingMoves.get(0);
        } else {
            printToTerminal("\nPromote to (q, r, b, n): ");
            String promotionInput = scanner.nextLine().trim().toLowerCase();

            ChessPiece.PieceType promotionType;
            switch (promotionInput) {
                case "q":
                    promotionType = ChessPiece.PieceType.QUEEN;
                    break;
                case "r":
                    promotionType = ChessPiece.PieceType.ROOK;
                    break;
                case "b":
                    promotionType = ChessPiece.PieceType.BISHOP;
                    break;
                case "n":
                    promotionType = ChessPiece.PieceType.KNIGHT;
                    break;
                default:
                    printToTerminal("\nInvalid promotion choice.\n");
                    return;
            }

            selectedMove = null;
            for (ChessMove move : matchingMoves) {
                if (move.getPromotionPiece() == promotionType) {
                    selectedMove = move;
                    break;
                }
            }

            if (selectedMove == null) {
                printToTerminal("\nInvalid promotion selection.\n");
                return;
            }
        }

        MakeMoveWS makeMoveWS = new MakeMoveWS(
                UserGameCommand.CommandType.MAKE_MOVE,
                getAuthToken(),
                targetGameData.getGameID(),
                selectedMove
        );

        webSocketFacade.send(makeMoveWS);
    }

    private void showPieceMoves(String input) {
        Pattern pattern = Pattern.compile("(?i)^(spm|show\\s+piece\\s+moves)\\s+(\\S+)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String stringPosition = matcher.group(2);
            ChessPosition targetPosition = convertStringToChessPosition(stringPosition);
            if (targetPosition == null) {
                printToTerminal("\n" + stringPosition + " not a valid position.\nTry again\n");
            } else {
                Collection<ChessMove> validMoves = new ArrayList<>();
                validMoves.addAll(this.getChessGame().validMoves(targetPosition));
                if (validMoves.isEmpty()) {
                    printToTerminal("\nNo valid moves for that piece.\n");
                    return;
                }

                highlightedSquares.clear();

                for (ChessMove validMove : validMoves) {
                    highlightedSquares.add(validMove.getEndPosition());
                }

                drawBoard();

                highlightedSquares.clear();
            }
        }
    }

    private ChessPosition convertStringToChessPosition(String input) {
        if (input == null) {
            return null;
        }

        input = input.trim().toLowerCase();

        if (input.length() != 2) {
            return null;
        }

        char file = input.charAt(0);
        char rank = input.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        int column = file - 'a' + 1;
        int row = rank - '0';

        return new ChessPosition(row, column);
    }


    private void showPlayerTurn() {
        printToTerminal("It is " + this.getChessGame().getTeamTurn().toString().toLowerCase() + "'s turn");
    }

    private void leaveMatch() {
        LeaveWS leaveWS = new LeaveWS(
                UserGameCommand.CommandType.LEAVE,
                getAuthToken(),
                targetGameData.getGameID()
        );

        webSocketFacade.send(leaveWS);
        this.setState(STATE.LOGGED_IN);
        this.setTeamColor(null);
        this.setChessGame(null);
        printToTerminal("\n\nLeaving match\n\n");
    }

    private void register() {

        printToTerminal("Enter your email\n");
        String email = scanner.nextLine();

        printToTerminal("Enter a new username\n");
        String username = scanner.nextLine();


        String password = "";
        String confirmPassword = "";
        do {
            printToTerminal("Enter a new password\n");
            password = scanner.nextLine();
            printToTerminal("re-enter your password\n");
            confirmPassword = scanner.nextLine();

            if (!confirmPassword.equals(password) ||
                    password.isEmpty() ||
                    confirmPassword.isEmpty()) {
                printToTerminal("Your passwords aren't matching or are empty.\nTry again.\n");
            }
        } while (!confirmPassword.equals(password) ||
                password.isEmpty() ||
                confirmPassword.isEmpty());

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);

        try {
            RegisterResponse registerResponse = this.serverFacade.register(registerRequest);
            setAuthToken(registerResponse.authToken);
            setState(STATE.LOGGED_IN);
            printToTerminal(String.format
                    ("\nHello %s. You are registered and logged in.\n\nType help for options\n\n",
                    username));
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
    }

    private void printHelp () {
        switch (this.state) {
            case LOGGED_OUT:
                printToTerminal("\nAvailable Commands:\n");
                printToTerminal("help : h\n");
                printToTerminal("quit : q\n");
                printToTerminal("login : li\n");
                printToTerminal("register : r\n");
                break;
            case LOGGED_IN:
                printToTerminal("\nAvailable Commands:\n");
                printToTerminal("help : h\n");
                printToTerminal("logout : lo\n");
                printToTerminal("create game : cg <gameName>\n");
                printToTerminal("list games : lg\n");
                printToTerminal("join game : jg <gameId> <color|c>\n");
                printToTerminal("watch game : wg <gameId>\n");
                break;
            case IN_GAME:
                printToTerminal("\nAvailable Commands:\n");
                printToTerminal("show piece moves : spm <positionX> (ie. spm e4)\n");
                printToTerminal("move piece : mp <positionX> <positionY> (ie. mp e4 e5)\n");
                printToTerminal("show player turn : spt\n");
                printToTerminal("draw board : db\n");
                printToTerminal("leave match : lm\n");
                printToTerminal("logout : lo\n");
                break;
            case WATCH_GAME:
                printToTerminal("\nAvailable Commands:\n");
                printToTerminal("draw board : db\n");
                printToTerminal("show player turn : spt\n");
                printToTerminal("show piece moves : spm <positionX> (ie. spm e4)\n");
                printToTerminal("leave match : lm\n");
                printToTerminal("logout : lo\n");
                break;
        }

    }

    private void login() {
        printToTerminal("Enter your username\n");
        String username = scanner.nextLine();
        printToTerminal("Enter your password\n");
        String password = scanner.nextLine();

        LoginRequest loginRequest = new LoginRequest(username, password);

        try {
            LoginResponse loginResponse = this.serverFacade.login(loginRequest);
            setAuthToken(loginResponse.authToken);
            setState(STATE.LOGGED_IN);
            printToTerminal(String.format
                    ("\nHello %s you're logged in.\n\nType help for options\n\n",
                    username));
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
    }

    public void handleUnauthorized() {
        setAuthToken(null);
        setState(STATE.LOGGED_OUT);
        printToTerminal("\nYou were logged out!\n\n");
        printHelp();
    }

    private void logout() {
        try {
            this.serverFacade.logout(getAuthToken());
            setAuthToken("");
            setState(STATE.LOGGED_OUT);
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
        printToTerminal("\nGoodbye, you're logged out.\n\nType help for options\n\n");
    }

    private void createGame(String input) {
        try {
            String gameName = "";
            String[] args = input.split(" ");
            if (args.length > 2) {
                gameName = args[2];
            } else if (args.length == 1) {
                printToTerminal("\n\nYou don't have a game name specified, try again.\n\n");
            } else if (args.length == 2) {
                gameName = args[1];
            }
            CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
            this.serverFacade.createGame(createGameRequest, this.getAuthToken());
            printToTerminal(String.format("\nGame %s created\n\n",gameName));
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
    }

    private void listGames() {
        try {
            this.gamesResponse = this.serverFacade.getGames(this.getAuthToken());
            int gameNumber = 0;
            for (GameData game : this.gamesResponse.games) {
                gameNumber++;
                printToTerminal(String.format("\nGame ID: %s\n", gameNumber));
                printToTerminal(String.format("Game Name: %s\n", game.getGameName()));

                if (game.getWhiteUsername() == null) {
                    printToTerminal("White Player: OPEN\n");
                } else {
                    printToTerminal(String.format("White Player: %s\n", game.getWhiteUsername()));
                }

                if (game.getBlackUsername() == null) {
                    printToTerminal("Black Player: OPEN\n");
                } else {
                    printToTerminal(String.format("Black Player: %s\n", game.getBlackUsername()));
                }
            }
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
    }

    private void joinGame(String input) {
        try {
            Pattern pattern = Pattern.compile("(?i)^(jg|join\\s+game)\\s+(\\d+)\\s+(white|black|w|b)$");
            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                String clientDisplayId = matcher.group(2);

                matchAndGetGame(clientDisplayId);

                String colorInput = matcher.group(3).toLowerCase();

                ChessGame.TeamColor color;

                if (colorInput.equals("w") || colorInput.equals("white")) {
                    color = ChessGame.TeamColor.WHITE;
                } else {
                    color = ChessGame.TeamColor.BLACK;
                }

                JoinGameRequest request = new JoinGameRequest(color, targetGameData.getGameID());
                serverFacade.joinGame(request, getAuthToken());

                webSocketFacade = new WebSocketFacade(serverUrl, this);
                ConnectWS connectWS = new ConnectWS(
                        UserGameCommand.CommandType.CONNECT,
                        getAuthToken(),
                        targetGameData.getGameID()
                );

                try {
                    webSocketFacade.connect(connectWS);
                } catch (WebSocketConnectionException e) {
                    printToTerminal(e.getMessage());
                    return;
                }

                setState(STATE.IN_GAME);
                setTeamColor(color);
                printToTerminal(String.format("Joined Game: %s\nPress h for commands:\n\n", clientDisplayId));
            }

        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
    }

    private void drawBoard() {
        if (this.getChessGame() == null) {
            printToTerminal("\nNo game loaded!\n");
            return;
        }
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
        ChessPosition currentPosition = new ChessPosition(row, col);
        boolean highlighted = highlightedSquares.contains(currentPosition);
        boolean lightSquare = (row + col) % 2 == 1;

        if (highlighted) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        } else if (lightSquare) {
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

    private void watchGame(String input) {
        Pattern pattern = Pattern.compile("(?i)^(wg|watch\\s+game)\\s+(\\d+)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String clientDisplayId = matcher.group(2);
            matchAndGetGame(clientDisplayId);

            webSocketFacade = new WebSocketFacade(serverUrl, this);
            ConnectWS connectWS = new ConnectWS(
                    UserGameCommand.CommandType.CONNECT,
                    getAuthToken(),
                    targetGameData.getGameID()
            );

            try {
                webSocketFacade.connect(connectWS);
            } catch (WebSocketConnectionException e) {
                printToTerminal(e.getMessage());
                return;
            }

            setState(STATE.WATCH_GAME);
            printToTerminal(String.format("\n\nWatching Game: %s\nPress h for commands:\n\n", clientDisplayId));
        }
    }

    private void matchAndGetGame (String stringGameId) {
        int clientDisplayId = Integer.parseInt(stringGameId);

        if (gamesResponse == null) {
            printToTerminal("\n\nFirst List the available games\n\nPress h for commands:\n\n");
            return;
        }
        try {
            targetGameData = gamesResponse.games.get(clientDisplayId - 1);
            setChessGame(targetGameData.getChessGame());
        } catch (IndexOutOfBoundsException e) {
            printToTerminal(String.format("\n\nCheck your game Number: %s\nPress h for commands:\n\n",
                    clientDisplayId));
        }
    }

    public void closeSocket(String reason) {
        setState(STATE.LOGGED_IN);
        this.setChessGame(null);
        printToTerminal(reason);
        printPrompt();
    }

    private void printQuit () {
        printToTerminal("Thanks for playing!\n");
    }
}
