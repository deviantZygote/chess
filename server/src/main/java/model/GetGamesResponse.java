package model;

import java.util.ArrayList;

public class GetGamesResponse {

    public ArrayList<GameData> games;

    public GetGamesResponse(ArrayList<GameData> gamesData) {
        this.games = new ArrayList<>(gamesData);
    }
}
