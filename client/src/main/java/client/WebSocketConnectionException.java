package client;

public class WebSocketConnectionException extends Exception{
    public WebSocketConnectionException(String message) {
        super(message);
    }
    public WebSocketConnectionException(String message, Throwable ex) {
        super(message, ex);
    }
}
