package client;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import model.*;

public class ServerFacade {
    private final String serverUrl;
    private Gson gson = new Gson();


    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public RegisterResponse register(RegisterRequest registerRequest) throws ResponseException {
        try {
            URL url = configureUrl("/user");
            HttpURLConnection http = configureHttp(url, "POST");
            String jsonRequest = serializeJson(registerRequest);
            writeBody(jsonRequest, http);
            return handleResponse(http, RegisterResponse.class);
        } catch (Exception e) {
            throw new ResponseException("Invalid response from server");
        }
    }

    public void clear() throws ResponseException {
        try {
            URL url = configureUrl("/db");
            HttpURLConnection http = configureHttp(url, "DELETE");
            http.connect();
            handleResponse(http);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to clear");
        }
    }

    public LoginResponse login(LoginRequest loginRequest) throws ResponseException {
        try {
            URL url = configureUrl("/session");
            HttpURLConnection http = configureHttp(url, "POST");
            String jsonRequest = serializeJson(loginRequest);
            writeBody(jsonRequest, http);
            return handleResponse(http, LoginResponse.class);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to login");
        }
    }

    public void logout(String authToken) throws ResponseException {
        try {
            URL url = configureUrl("/session");
            HttpURLConnection http = configureHttp(url, "DELETE");
            http.addRequestProperty("authorization", authToken);
            handleResponse(http);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to logout");
        }
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest , String authToken) throws ResponseException {
        try {
            URL url = configureUrl("/game");
            HttpURLConnection http = configureHttp(url, "POST");
            String jsonRequest = serializeJson(createGameRequest);
            http.addRequestProperty("authorization", authToken);
            writeBody(jsonRequest, http);
            return handleResponse(http, CreateGameResponse.class);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to create game");
        }
    }

    public GetGamesResponse getGames(String authToken) throws ResponseException {
        try {
            URL url = configureUrl("/game");
            HttpURLConnection http = configureHttp(url, "GET");
            http.addRequestProperty("authorization", authToken);
            return handleResponse(http, GetGamesResponse.class);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to get games");
        }
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws ResponseException {
        try {
            URL url = configureUrl("/game");
            HttpURLConnection http = configureHttp(url, "PUT");
            http.addRequestProperty("authorization", authToken);
            String jsonRequest = serializeJson(joinGameRequest);
            writeBody(jsonRequest, http);
            handleResponse(http);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to join game");
        }
    }

    private <T> T handleResponse(HttpURLConnection http, Class<T> responseClass) throws ResponseException {
        try {
            int status = http.getResponseCode();

            if (status == 200) {
                try (InputStream respBody = http.getInputStream();
                     InputStreamReader reader = new InputStreamReader(respBody)) {
                    return gson.fromJson(reader, responseClass);
                }
            } else {
                throw new ResponseException("Request failed: " + status);
            }

        } catch (IOException e) {
            throw new ResponseException("Error: Failed to get http response");
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new ResponseException("Invalid response from server");
        }
    }

    private void handleResponse(HttpURLConnection http) throws ResponseException {
        try {
            int status = http.getResponseCode();

            if (status != 200) {
                throw new ResponseException("Clear failed: " + status);
            }
        } catch (IOException e) {
            throw new ResponseException("Error: Failed to get http response");
        }
    }

    private <T> void writeBody (String jsonRequest, HttpURLConnection http) throws ResponseException {
        try {
            try (OutputStream reqBody = http.getOutputStream();
                 Writer writer = new OutputStreamWriter(reqBody)) {
                writer.write(jsonRequest);
            }
        } catch (Exception e) {
            throw new ResponseException("Error: Failed to write Body");
        }
    }

    private <T> String serializeJson (T requestObj) throws ResponseException {
        try {
            return gson.toJson(requestObj);
        } catch (Exception e) {
            throw new ResponseException("Error: Failed Serializing JSON");
        }
    }

    private HttpURLConnection configureHttp (URL url, String method) throws ResponseException {
        try {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            return http;
        } catch (Exception e) {
            throw new ResponseException("Error: Http connection failed");
        }
    }

    private URL configureUrl (String urlSring) throws ResponseException  {
        try {
            return (URI.create(serverUrl + urlSring)).toURL();
        } catch (IllegalArgumentException e) {
            throw new ResponseException("Error: Illegal argument in server URL");
        } catch (MalformedURLException e) {
            throw new ResponseException("Error: MalformedURL in server URL");
        }
    }

}
