package helpers;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.UnauthorizedException;
import model.*;

public class HelperFunctions {
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static AuthData validateLogin(String authToken, DataAccess dataAccess) {
        try {
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }

            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new UnauthorizedException("Error: unauthorized");
            } else {
                return authData;
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
