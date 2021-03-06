package ru.geekbrains.fileserver_netty;


import ClientServer.FileInfo.FileInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.fileserver_netty.models.ClientChatState;
import ru.geekbrains.fileserver_netty.models.Network;

import java.io.IOException;
import java.util.List;


public class ClientChatNetty extends Application {


    private ClientChatState state = ClientChatState.AUTHENTICATION;
    private Stage primaryStage;
    private Stage authDialogStage;

    private Network network;
    private ViewController viewController;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientChatNetty.class.getResource("/manager.fxml"));

        Parent root = loader.load();
        viewController = loader.getController();

        primaryStage.setTitle("File Server Storage");
        primaryStage.setScene(new Scene(root, 800, 400));
        primaryStage.setResizable(false);

        network = new Network(this);
        if (!network.connect()) {
            showNetworkError("", "Failed to connect to server", primaryStage);
        }

        viewController.setNetwork(network);
        viewController.setStage(primaryStage);

        network.waitMessages(viewController);

        primaryStage.setOnCloseRequest(event -> {
          network.close();
        });
        openAuthDialog();

    }

    private void openAuthDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientChatNetty.class.getResource("/authDialog.fxml"));
        AnchorPane parent = loader.load();

        authDialogStage = new Stage();
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        authDialogStage.initOwner(primaryStage);

        AuthController authController = loader.getController();
        authController.setNetwork(network);

        authDialogStage.setScene(new Scene(parent));
        authDialogStage.setOnCloseRequest(event ->{
            network.close();
        });
        authDialogStage.show();
    }
    public static void showNetworkConfirmation(String errorDetails, String errorTitle, Stage dialogStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.setTitle("Successful");
        alert.setHeaderText(errorTitle);
        alert.setContentText(errorDetails);
        alert.showAndWait();
    }

    public static void showNetworkError(String errorDetails, String errorTitle, Stage dialogStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.setTitle("Network Error");
        alert.setHeaderText(errorTitle);
        alert.setContentText(errorDetails);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public ClientChatState getState() {
        return state;
    }

    public void activeChatDialog(String nickname, String remotePath, List<FileInfo> files) {
        primaryStage.setTitle(nickname);
        viewController.updateRemoteList(nickname ,remotePath,files);
        state = ClientChatState.CHAT;
        authDialogStage.close();
        primaryStage.show();

    }
}