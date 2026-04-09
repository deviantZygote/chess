package model;

public record ConnectWS(
        UserGameCommand.CommandType commandType,
        String authToken,
        Integer gameID
) {}
