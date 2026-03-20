package model;

public class RegisterRequest {
    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    public String username;
    public String password;
    public String email;
}
