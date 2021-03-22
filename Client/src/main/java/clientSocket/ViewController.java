package clientSocket;

import ClientServer.FileInfo.FileInfo;
import ClientServer.FileInfo.FileInfoBuiled;
import clientSocket.models.Network;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ClientServer.FileInfo.FileType.DIRECTORY;


public class ViewController implements Initializable {

   
    private Network network;
    private Stage primaryStage;


    @FXML
   TableView<FileInfo> filesTable;

   @FXML
   TableView<FileInfo> remoteFilesTable;

   @FXML
   ComboBox<String> disksBox;

   @FXML
   TextField pathField;

   @FXML
   TextField remotePathField;

    private static TableCell<FileInfo, Long> call(TableColumn<FileInfo, Long> column) {
        return new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        };
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leftPanel();
        rightPnel();

    }

    private void rightPnel() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("имя Файла");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(170);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(100);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(100);
        remoteFilesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        remoteFilesTable.getSortOrder().add(fileTypeColumn);

        remoteFilesTable.setOnMouseClicked(event-> {
                if (event.getClickCount() == 2) {
                    String requestPath = remoteFilesTable.getSelectionModel().getSelectedItem().getFileName();
                    try {
                        network.sendUpdateRemotePath(requestPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );



        //updateRemoteList(remotePath);
    }

    /**
     * Метод обновения списка фалов и каталогов сервера на стороне клиента
     * @param nickName - текущий Ник
     * @param path - текущий каталог на который смотрит клиент на сервере
     * @param files - коллекция (лист)
     */
    public void updateRemoteList(String nickName,String path, List<FileInfo> files) {

            remotePathField.setText(nickName + "@Server:" + path + "/ $");
            remoteFilesTable.getItems().clear();
            remoteFilesTable.getItems().addAll(files);
            remoteFilesTable.sort();

    }

        private void leftPanel() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("имя Файла");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(170);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(100);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(100);
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for(Path p : FileSystems.getDefault().getRootDirectories()){
            disksBox.getItems().add(p.toString());
        }

        filesTable.setOnMouseClicked(event-> {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
        });
        updateList(Paths.get("."));
    }

    /**
     * Метод обновления левой локальной паели.
     * Строит коллекцию файлов от пути и запсывает путь в верхнюю строку
     * @param path
     */
    public void updateList(Path path){
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfoBuiled::infoBuilder).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Disk not availabl", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateClientDir(){
        try {
            Path path = Paths.get(pathField.getText());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfoBuiled::infoBuilder).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Disk not availabl", ButtonType.OK);
            alert.showAndWait();
        }
    }




    public void setNetwork(Network network) {
        this.network = network;
    }


    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void menuItemFileExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnPathAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if(upperPath != null) updateList(upperPath);
    }

    public void selectDiscAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilename() {
        if (!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void btnRemotePathUpAction(ActionEvent actionEvent) {
        try {
            network.sendUpdateRemotePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyBtn(ActionEvent actionEvent) {
        if (filesTable.isFocused()) {
            System.out.println("Нажат слева");
            if(filesTable.getSelectionModel().getSelectedItem().getType()==DIRECTORY) return;
            String fileName = filesTable.getSelectionModel().getSelectedItem().getFileName();
            StringBuilder str = new StringBuilder();
            str.append(pathField.getText());
            str.append("\\");
            str.append(fileName);
            network.requestSendFile(str.toString(),fileName);

        }
        if (remoteFilesTable.isFocused()) {
            System.out.println("Нажат справа");
            if(remoteFilesTable.getSelectionModel().getSelectedItem().getType()==DIRECTORY) return;
            String srcFileName = remoteFilesTable.getSelectionModel().getSelectedItem().getFileName();
            String targetPath = pathField.getText() + "\\" + srcFileName;
            network.requestReceiveFile(targetPath,srcFileName);
            }
    }
}
