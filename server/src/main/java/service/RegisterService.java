package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import model.AuthData;
import model.RegisterRequest;
import model.RegisterResponse;
import model.UserData;
import java.util.UUID;

public class RegisterService {

    private final DataAccess dataAccess;

    public RegisterService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResponse register(RegisterRequest req) {
        if (isBlank(req.username) || isBlank(req.password) || isBlank(req.email)) {
            throw new BadRequestException("Error: bad request");
        }

        try {
            if (dataAccess.getUser(req.username) != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            dataAccess.createUser (new UserData(req.username, req.password, req.email));

            String token = UUID.randomUUID().toString();
            dataAccess.createAuth(new AuthData(token, req.username));

            return new RegisterResponse(req.username, token);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
