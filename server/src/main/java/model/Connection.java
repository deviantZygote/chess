package model;

import io.javalin.websocket.WsContext;

import java.util.Objects;

public class Connection {
    public final String username;
    public final WsContext ws;

    public Connection(String username, WsContext ws) {
        this.username = username;
        this.ws = ws;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Connection that)) {
            return false;
        }
        return Objects.equals(username, that.username) &&
                Objects.equals(ws, that.ws);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, ws);
    }
}
