package model;

public class LoginResponse {
    public LoginResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }
    public String username;
    public String authToken;
}
