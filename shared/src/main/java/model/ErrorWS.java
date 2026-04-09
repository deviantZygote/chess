package model;

public record ErrorWS(
        ServerMessage.ServerMessageType serverMessageType,
        String errorMessage
) {}