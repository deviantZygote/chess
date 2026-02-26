package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.*;
import model.*;
import java.util.UUID;
import static helpers.HelperFunctions.isBlank;

public class LoginService {
    private final DataAccess dataAccess;

    public LoginService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResponse login(LoginRequest req) {
        if (req == null || isBlank(req.username) || isBlank(req.password)) {
            throw new BadRequestException("Error: bad request");
        }

        try {
                // check if username / password is valid
                UserData userData = dataAccess.getUser(req.username);
                if ( userData == null ) {
                    throw new UnauthorizedException("Error: unauthorized");
                } else if (userData.password().equals(req.password)) {
                    // log in the validated user
                    String token = UUID.randomUUID().toString();
                    dataAccess.createAuth(new AuthData(token, req.username));
                    return new LoginResponse(req.username, token);
                } else {
                    throw new UnauthorizedException("Error: unauthorized");
                }
            } catch (DataAccessException e) {
            throw new RuntimeException(e);
            }
    }
}
