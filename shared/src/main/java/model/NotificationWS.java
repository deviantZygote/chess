package model;

public record NotificationWS(
        WSCommands commandType,
        String message
) {}
