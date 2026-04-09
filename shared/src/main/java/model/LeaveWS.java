package model;

public record LeaveWS(
        UserGameCommand.CommandType commandType,
        String authToken,
        Integer gameID
) {}
