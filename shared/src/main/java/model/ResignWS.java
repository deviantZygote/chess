package model;

public record ResignWS(
        UserGameCommand.CommandType commandType,
        String authToken,
        Integer gameID
) {}
