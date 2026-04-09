package model;

public record ServerMessage(
        ServerMessageType serverMessageType
) {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }
}
