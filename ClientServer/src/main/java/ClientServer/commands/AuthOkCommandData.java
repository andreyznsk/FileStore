package ClientServer.commands;

import java.io.Serializable;
import java.nio.file.Path;

public class AuthOkCommandData implements Serializable {

    private final String username;
    private final String defPafh;

    public AuthOkCommandData(String username, String defPafh) {
        this.username = username;
        this.defPafh = defPafh;
    }

    public String getUsername() {
        return username;
    }
    public String getPath() {
        return defPafh;
    }
}
