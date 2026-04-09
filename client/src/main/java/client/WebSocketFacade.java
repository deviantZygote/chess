package client;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.ConnectCommand;
import ui.Menu;

import java.net.URI;

public class WebSocketFacade {
    private String webSocketUrl = "";
    private ChessWebSocketClient client;
    Menu menu = null;

    public WebSocketFacade (String url, Menu menu) {
        this.webSocketUrl = url
                .replace("http://", "ws://")
                .replace("https://", "wss://")
                + "/ws";
        this.menu = menu;
    }

    public void connect(ConnectCommand connectCommand) throws WebSocketConnectionException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            client = new ChessWebSocketClient(connectCommand, menu);
            container.connectToServer(client, URI.create(webSocketUrl));

        } catch (Exception e) {
            throw new WebSocketConnectionException("Error: Failed to make web socket connection ");
        }
    }

    public void send(Object o) {
        client.send(o);
    }
}
