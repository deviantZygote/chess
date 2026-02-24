package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.RegisterRequest;
import model.RegisterResponse;
import model.UserData;

import java.util.UUID;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResponse register(RegisterRequest req) {
        if (userDAO.exists(req.username)) {
            throw new RuntimeException("already taken");
        }

        userDAO.create(new UserData(req.username, req.password, req.email));

        String token = UUID.randomUUID().toString();
        authDAO.create(new AuthData(token, req.username));

        return new RegisterResponse(req.username, token);
    }
}
