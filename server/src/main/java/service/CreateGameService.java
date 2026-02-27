package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.*;
import model.*;

import static helpers.HelperFunctions.isBlank;
import static helpers.HelperFunctions.validateLogin;

public class CreateGameService {
    private final DataAccess dataAccess;
    public CreateGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResponse createGame(String authToken, CreateGameRequest req) {
        validateLogin(authToken, dataAccess);
        try {
            if (isBlank(req.gameName)) {
                throw new BadRequestException("Error: bad request");
            }
            GameData gameData = dataAccess.createGame(req.gameName);
            return new CreateGameResponse(gameData.getGameID());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
