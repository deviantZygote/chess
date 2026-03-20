package client;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import model.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public RegisterResponse register(String username, String password, String email) throws ResponseException {
        try {
            RegisterRequest request = new RegisterRequest(username, password, email);

            URL url = (URI.create(serverUrl + "/user")).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);

            try (OutputStream reqBody = http.getOutputStream();
                 Writer writer = new OutputStreamWriter(reqBody)) {
                writer.write(jsonRequest);
            }

            int status = http.getResponseCode();

            if (status == 200) {
                try (InputStream respBody = http.getInputStream();
                     InputStreamReader reader = new InputStreamReader(respBody)) {
                    return gson.fromJson(reader, RegisterResponse.class);
                }
            } else {
                throw new ResponseException("Registration failed: " + status);
            }

        } catch (java.io.IOException e) {
            throw new ResponseException("Unable to communicate with server");
        } catch (IllegalArgumentException e) {
            throw new ResponseException("Invalid server URL");
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new ResponseException("Invalid response from server");
        }
    }
}
