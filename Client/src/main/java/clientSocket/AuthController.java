package clientSocket;

import clientSocket.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.io.IOException;

public class AuthController {

    private static final String AUTH_CMD = "/auth"; // "/auth login password"
    private Stage regStage;
    private RegController regController;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;


    private Network network;

    @FXML
    public void initialize() {
        createRegWindow();
    }

    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            ClientChat.showNetworkError("Логин и пароль обязательны!", "Валидация", null);
            return;
        }


        try {
            network.sendAuthMessage(login, password);
        } catch (IOException e) {
            ClientChat.showNetworkError(e.getMessage(), "Auth error!", null);
            e.printStackTrace();
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
        regController.setNetwork(network);
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/regDialog.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Регистрация");
            regStage.setScene(new Scene(root, 250, 230));
            regStage.initModality(Modality.APPLICATION_MODAL);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    @FXML
    public void executeOpenRegDialog(ActionEvent actionEvent) throws IOException {
        System.out.println("Нажали на кнопку");
        regStage.show();
      //ClientChat.openRegDialog();
    }


}