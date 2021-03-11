package Srv;

import java.util.HashMap;
import java.util.Map;

public class BaseAuthService implements AuthService {

    private static Map<User, String> USERS = null;

    @Override
    public void start() {
        System.out.println("Auth service is running");
        USERS = new HashMap<>();
        USERS.put(new User("2", "2", "Perter"), "Peter");
        USERS.put(new User("3", "3", "Alexey"), "Alexey");
        USERS.put(new User("4", "4", "Oleg"), "Oleg");
        USERS.put(new User("1", "1", "Andreyz"), "Andreyz");
    }

    @Override
    public void stop() {
        System.out.println("Auth service has been stopped");

    }

    @Override
    public String getNickByLoginPass(String login, String password) {//Изменил на метод идентификации из БД.

        return USERS.get(new User(login, password, null));
    }
}


