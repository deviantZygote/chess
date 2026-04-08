package model;

public record JoinGameWS(WSCommands commandType, String authToken, int gameID)
        implements ConnectGameWS {}