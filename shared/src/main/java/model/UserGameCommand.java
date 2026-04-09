package model;

public record UserGameCommand(
        CommandType commandType,
        String authToken,
        Integer gameID
) {
    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }
}
