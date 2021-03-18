package ClientServer;


import ClientServer.commands.*;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class Command implements Serializable {

    private CommandType type;
    private Object data;

    public CommandType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public static Command authCommand(String login, String password){
        Command command = new Command();
        command.type = CommandType.AUTH;
        command.data = new AuthCommandData(login, password);
        return command;

    }

    public static Command authOkCommand(String username, String defPath, List<FileInfo> files) {
        Command command = new Command();
        command.type = CommandType.AUTH_OK;
        command.data = new AuthOkCommandData(username, defPath, files);
        return command;
    }

    public static Command errorCommand(String errorMessage) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrorCommandData(errorMessage);
        return command;
    }
    public static Command confirmationCommand(String errorMessage) {
        Command command = new Command();
        command.type = CommandType.CONFIRMATION;
        command.data = new ErrorCommandData(errorMessage);
        return command;
    }

    public static Command regNewUserCommand(String login,String password,String nickname ) {
        Command command = new Command();
        command.type = CommandType.CREATE_NEW_USER;
        command.data = new AuthRegData(login, password, nickname);
        return command;
    }

    public static Command regUpdateUserCommand(String login,String password,String nickname ) {
        Command command = new Command();
        command.type = CommandType.UPDATE_USER;
        command.data = new AuthRegData(login, password, nickname);
        return command;
    }


}
