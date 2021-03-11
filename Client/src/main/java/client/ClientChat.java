package client;

import client.models.ChatHistoryBuilder;
import client.models.ClientChatState;
import client.models.Network;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientChat extends Application {

    public static final List<String> USERS_TEST_DATA = new ArrayList<>();
    private Stage regStage;
    private RegController regController;

    private ClientChatState state = ClientChatState.AUTHENTICATION;
    private Stage primaryStage;
    private Stage authDialogStage;

    private ChatHistoryBuilder historyBuilder;
    private Network network;
    private ViewController viewController;

   /* public static void openRegDialog() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientChat.class.getResource("/regDialog.fxml"));
        AnchorPane parent = loader.load();

        regDialogStage = new Stage();
        regDialogStage.initModality(Modality.WINDOW_MODAL);
        regDialogStage.initOwner(primaryStage);

        AuthController authController = loader.getController();
        authController.setNetwork(network);

        regDialogStage.setScene(new Scene(parent));
        regDialogStage.show();
    }*/

    public void updateUsers(List<String> users) {
        viewController.usersList.setItems(FXCollections.observableList(users));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientChat.class.getResource("/view_lesson7.fxml"));

        Parent root = loader.load();
        viewController = loader.getController();

        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root, 600, 400));
        viewController.getTextField().requestFocus();

        network = new Network(this);
        if (!network.connect()) {
            showNetworkError("", "Failed to connect to server", primaryStage);
        }

        viewController.setNetwork(network);
        viewController.setStage(primaryStage);

        network.waitMessages(viewController);

        primaryStage.setOnCloseRequest(event -> {
            try {
                network.sendMessage("/end");
            } catch (IOException e) {
                e.printStackTrace();
            }
            network.close();
        });
        //regController.setNetwork(network);
        openAuthDialog();
    }

    private void openAuthDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientChat.class.getResource("/authDialog.fxml"));
        AnchorPane parent = loader.load();

        authDialogStage = new Stage();
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        authDialogStage.initOwner(primaryStage);

        AuthController authController = loader.getController();
        authController.setNetwork(network);

        authDialogStage.setScene(new Scene(parent));
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

    public void setState(ClientChatState state) {
        this.state = state;
    }

    public void activeChatDialog(String nickname) {
        historyBuilder = new ChatHistoryBuilder(nickname);
        viewController.setHistoryBuilder(historyBuilder);
        network.setHistoryBuilder(historyBuilder);
        primaryStage.setTitle(nickname);
        state = ClientChatState.CHAT;
        authDialogStage.close();
        primaryStage.show();
        viewController.getTextField().requestFocus();
    }
}