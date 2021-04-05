package ClientServer;


import ClientServer.FileInfo.FileInfo;
import ClientServer.commands.*;

import java.io.Serializable;
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

    private Command(){}

    /**
     * Команада запрос на авторизацию от клиента
     * @param login
     * @param password
     * @return
     */
    public static Command authCommand(String login, String password){
        Command command = new Command();
        command.type = CommandType.AUTH;
        command.data = new AuthCommandData(login, password);
        return command;

    }

    /**
     * Команда подтвержение авторизации на сервере
     * @param username
     * @param files
     * @return
     */
    public static Command authOkCommand(String username, List<FileInfo> files) {
        Command command = new Command();
        command.type = CommandType.AUTH_OK;
        command.data = new AuthOkCommandData(username, files);
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

    /**
     * Команда от клиента на получение списка файлов по текщей дирректории
     * @param requestDir
     * @return
     */
    public static Command updateUserPath(String requestDir){
        Command command = new Command();
        command.type = CommandType.REQUEST_DIR;
        command.data = new RequestUserDir(requestDir);
        return command;
    }

    /**
     * Команда на обновление каталога файлов на клиенте
     * @param files
     * @param currentPath
     * @return
     */
    public static Command requestDirOk( List<FileInfo> files, String currentPath){
        Command command = new Command();
        command.type = CommandType.REQUEST_DIR_OK;
        command.data = new RequestDirOkData(files, currentPath);
        return command;
    }

    /**
     * Метод формирования команды запроса на создания потока для отправки файла со сторный клинета на сервер
     * @param filePath - путь в который нужно положить файл на сервере
     * @param fileName - название файла
     * @return
     */
    public static Command fileSendCommand(String filePath, String fileName, long fileParts){
        Command command = new Command();
        command.type = CommandType.FILE_UPLOAD_REQEST;
        command.data = new FileSendCommandData(filePath,fileName, fileParts);
        return command;
    }

    /**
     * Команда готовности сервера на прием файла, отдельный поток поднят и обработчик переведен в цикл чтения частей файла из сокета.
     * @return
     */
    public static Command requestTransmiterOk(){
        Command command = new Command();
        command.type = CommandType.REQUEST_TRANSMITTER_OK;
        return command;
    }

    /**
     * Команда готовности сервера отдать файл
     * @param fileParts - количество частей которые должен принять клиент
     * @return
     */
    public static Command requestDownloadOk(long fileParts){
        Command command = new Command();
        command.type = CommandType.REQUEST_DOWNLOAD_OK;
        command.data = new FileSendCommandData("","", fileParts);
        return command;
    }

    /**
     * Запрос на получение файла с сервера
     * @param filePath
     * @param fileName
     * @param fileParts
     * @return
     */
    public static Command fileReceiveCommand(String filePath, String fileName, long fileParts){
        Command command = new Command();
        command.type = CommandType.FILE_DOWNLOAD_REQEST;
        command.data = new FileSendCommandData(filePath,fileName, fileParts);
        return command;
    }

    public static Command closeConnection(){
        Command command = new Command();
        command.type = CommandType.CLOSE_CONNECTION;
        return command;
    }

    public static Command fileDataCommand(long filePosition, byte[] data){
        Command command = new Command();
        command.type = CommandType.FILE_DATA;
        command.data = new FileDataCommand(filePosition, data);
        return command;
    }
}
