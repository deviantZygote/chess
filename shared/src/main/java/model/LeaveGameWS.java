package model;

public record LeaveGameWS(WSCommands commandType, String authToken, int gameID) {
}
