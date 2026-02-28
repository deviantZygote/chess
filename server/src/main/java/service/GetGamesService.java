package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.*;
import model.*;

import java.util.ArrayList;
import java.util.Collection;

import static helpers.HelperFunctions.isBlank;
import static helpers.HelperFunctions.validateLogin;

public class GetGamesService {
    private final DataAccess dataAccess;
    public GetGamesService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

public GetGamesResponse getGames (String authToken) {
        validateLogin(authToken, dataAccess);
    try {
        ArrayList<GameData> gamesData = new ArrayList<>();
        gamesData.addAll(dataAccess.getGames());
        GetGamesResponse getGamesResponse = new GetGamesResponse(gamesData);
        return getGamesResponse;
    } catch (DataAccessException e) {
        throw new RuntimeException(e);
    }

    }


}
