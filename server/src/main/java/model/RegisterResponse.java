package model;

public class RegisterResponse {
    public String username;
    public String authToken;

    public RegisterResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }
}
