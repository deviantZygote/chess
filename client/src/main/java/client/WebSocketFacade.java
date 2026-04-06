package client;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import model.JoinGameWS;

import java.net.URI;

public class WebSocketFacade {
    private String webSocketUrl = "";
    private ChessWebSocketClient client;

    public WebSocketFacade (String url) {
        this.webSocketUrl = url
                .replace("http://", "ws://")
                .replace("https://", "wss://")
                + "/ws";
    }

    public void connect (JoinGameWS joinGameWS) {
        try {
            System.out.println("Connecting to: " + webSocketUrl);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            client = new ChessWebSocketClient(joinGameWS);
            container.connectToServer(client, URI.create(webSocketUrl));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        client.send(message);
    }
}
