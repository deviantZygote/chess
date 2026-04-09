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
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;

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

    public GameData getTargetGameData() {
        return targetGameData;
    }

    public void setTargetGameData(GameData targetGameData) {
        this.targetGameData = targetGameData;
    }

    private GameData targetGameData = null;
    private Set<ChessPosition> highlightedSquares = new HashSet<>();
    private final MenuHelpers menuHelpers = new MenuHelpers();






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
            menuHelpers.printPromptText(this.getState());
        }
    }

    public void printAsyncMessage(String message) {
        synchronized (consoleLock) {
            System.out.print("\n" + message);
            menuHelpers.printPromptText(this.getState());
        }
    }

    public void processInput (String input) {
        menuHelpers.processInput(input, this);
    }

    protected void makeMove(String input) {

        Pattern pattern = Pattern.compile("(?i)^(mp|move\\s+piece)\\s+(\\S+)\\s+(\\S+)$");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            printToTerminal("\nUsage: mp <from> <to>\n");
            return;
        }

        ChessPosition start = menuHelpers.convertStringToChessPosition(matcher.group(2));
        ChessPosition end = menuHelpers.convertStringToChessPosition(matcher.group(3));

        if (!menuHelpers.makeMoveDataValidation(
                start,
                end,
                this.getTeamColor(),
                this.getChessGame().getTeamTurn(),
                this.getChessGame().getBoard().getPiece(start),
                this
        )) {
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

        MakeMoveCommand makeMoveCommand = new MakeMoveCommand(
                getAuthToken(),
                targetGameData.getGameID(),
                selectedMove
        );

        webSocketFacade.send(makeMoveCommand);
    }

    protected void showPieceMoves(String input) {
        Pattern pattern = Pattern.compile("(?i)^(spm|show\\s+piece\\s+moves)\\s+(\\S+)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String stringPosition = matcher.group(2);
            ChessPosition targetPosition = menuHelpers.convertStringToChessPosition(stringPosition);
            if (targetPosition == null) {
                printToTerminal("\n" + stringPosition + " not a valid position.\nTry again\n");
            } else {
                Collection<ChessMove> validMoves = new ArrayList<>();
                try {
                    validMoves.addAll(this.getChessGame().validMoves(targetPosition));
                } catch (NullPointerException e) {
                    printToTerminal("\nNo valid moves for that piece or place.\n");
                    return;
                }
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

    protected void showPlayerTurn() {
        printToTerminal("It is " + this.getChessGame().getTeamTurn().toString().toLowerCase() + "'s turn");
    }

    protected void leaveMatch() {
        LeaveCommand leaveCommand = new LeaveCommand(
                getAuthToken(),
                targetGameData.getGameID()
        );

        webSocketFacade.send(leaveCommand);
        this.setState(STATE.LOGGED_IN);
        this.setTeamColor(null);
        this.setChessGame(null);
        printToTerminal("\n\nLeaving match\n\n");
    }

    protected void register() {

        printToTerminal("Enter your email\n");
        String email = scanner.nextLine();

        printToTerminal("Enter a new username\n");
        String username = scanner.nextLine();


        String password = menuHelpers.gatherPassword(this);

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

    protected void printHelp () {
        menuHelpers.printHelp(this.state, this);
    }

    protected void login() {
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

    protected void logout() {
        try {
            this.serverFacade.logout(getAuthToken());
            setAuthToken("");
            setState(STATE.LOGGED_OUT);
        } catch (ResponseException e) {
            printToTerminal(e.getMessage());
        }
        printToTerminal("\nGoodbye, you're logged out.\n\nType help for options\n\n");
    }

    protected void createGame(String input) {
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

    protected void listGames() {
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

    protected void joinGame(String input) {
        try {
            Pattern pattern = Pattern.compile("(?i)^(jg|join\\s+game)\\s+(\\d+)\\s+(white|black|w|b)$");
            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                String clientDisplayId = matcher.group(2);

                menuHelpers.matchAndGetGame(clientDisplayId, this.gamesResponse, this);

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
                ConnectCommand connectCommand = new ConnectCommand(
                        getAuthToken(),
                        targetGameData.getGameID()
                );

                try {
                    webSocketFacade.connect(connectCommand);
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

    protected void drawBoard() {
        DrawBoard drawBoard = new DrawBoard(this.getChessGame(), this.getTeamColor(), highlightedSquares, this);
    }

    protected void watchGame(String input) {
        Pattern pattern = Pattern.compile("(?i)^(wg|watch\\s+game)\\s+(\\d+)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String clientDisplayId = matcher.group(2);
            menuHelpers.matchAndGetGame(clientDisplayId, this.gamesResponse, this);

            webSocketFacade = new WebSocketFacade(serverUrl, this);
            if (getTargetGameData() == null) {
                printToTerminal("That's not a valid game number");
                return;
            }
            ConnectCommand connectCommand = new ConnectCommand(
                    getAuthToken(),
                    targetGameData.getGameID()
            );

            try {
                webSocketFacade.connect(connectCommand);
            } catch (WebSocketConnectionException e) {
                printToTerminal(e.getMessage());
                return;
            }

            setState(STATE.WATCH_GAME);
            printToTerminal(String.format("\n\nWatching Game: %s\nPress h for commands:\n\n", clientDisplayId));
        }
    }


    public void closeSocket(String reason) {
        setState(STATE.LOGGED_IN);
        this.setChessGame(null);
        printToTerminal(reason);
        printPrompt();
    }

    protected void printQuit () {
        printToTerminal("Thanks for playing!\n");
    }
}
