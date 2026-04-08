package model;

public interface ConnectGameWS {
    String authToken();
    int gameID();
    WSCommands commandType();
}
