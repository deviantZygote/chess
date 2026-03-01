package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.*;
import model.*;
import java.util.ArrayList;
import static helpers.HelperFunctions.validateLogin;

public class JoinGameService {
    private final DataAccess dataAccess;
    public JoinGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void joinGame (String authToken, JoinGameRequest req) {
        validateLogin(authToken, dataAccess);
        try {
            GameData gameData = dataAccess.getGame(req.gameID);
            if (gameData == null) {
                throw new BadRequestException("Error: bad request");
            }
            if (!targetColorOpen(gameData, req)) {
                throw new AlreadyTakenException("Error: already taken");
            }

            AuthData authData = dataAccess.getAuth(authToken);

            dataAccess.assignGamePlayer(req.playerColor, req.gameID, authData.username());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean targetColorOpen(GameData gameData, JoinGameRequest req) {
        ChessGame.TeamColor targetColor = req.playerColor;
        if (req.playerColor != ChessGame.TeamColor.WHITE && req.playerColor != ChessGame.TeamColor.BLACK) {
        throw new BadRequestException("Error: bad request");
        }

        if (targetColor == ChessGame.TeamColor.WHITE && gameData.getWhiteUsername() == null) {
            return true;
        } else if (targetColor == ChessGame.TeamColor.BLACK && gameData.getBlackUsername() == null) {
            return true;
        } else {
            return false;
        }
    }
}
