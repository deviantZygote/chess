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

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
