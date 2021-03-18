package ClientServer.commands;

import java.io.Serializable;

public class RequestUserDir implements Serializable {

    private final String requestDir;

    public RequestUserDir(String requestDir)  {
        this.requestDir = requestDir;
    }

    public String getRequestDir() {
        return requestDir;
    }
}
