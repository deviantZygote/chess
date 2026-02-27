package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.*;
import static helpers.HelperFunctions.isBlank;

public class LogoutService {
    private final DataAccess dataAccess;

    public LogoutService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void logout(String authToken) {
        if (isBlank(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        try {
            if (dataAccess.getAuth(authToken) == null) {
                throw new UnauthorizedException("Error: unauthorized");
            }
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
