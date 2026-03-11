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
import static helpers.HelperFunctions.isBlank;
import org.mindrot.jbcrypt.BCrypt;

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
            String hashedPassword = BCrypt.hashpw(req.password, BCrypt.gensalt());
            dataAccess.createUser (new UserData(req.username, hashedPassword, req.email));

            String token = UUID.randomUUID().toString();
            dataAccess.createAuth(new AuthData(token, req.username));

            return new RegisterResponse(req.username, token);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
