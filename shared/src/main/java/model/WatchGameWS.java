package model;

public record WatchGameWS(WSCommands commandType, String authToken, int gameID)
        implements ConnectGameWS {}
