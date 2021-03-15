package client;

import client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


import java.io.IOException;

public class RegController {

  //  private static final String AUTH_CMD = "/auth"; // "/auth login password"

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public TextField nicknameField;

    private AuthController controller;

    private Network network;

    @FXML
    public void executeRegNew(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        String nickname = nicknameField.getText();
        if (login == null || login.isEmpty() || password == null || password.isEmpty() || nickname == null || nickname.isEmpty()) {
            ClientChat.showNetworkError("Логин и пароль обязательны!", "Валидация", null);
            return;
        }
        System.out.println("Create new user");


        try {
            network.sendNewUserCommand(login,password,nickname);
        } catch (IOException e) {
            ClientChat.showNetworkError(e.getMessage(), "Auth error!", null);
            e.printStackTrace();
        }
    }
    @FXML
    public void executeRegUpdate(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        String nickname = nicknameField.getText();
        if (login == null || login.isEmpty() || password == null || password.isEmpty() || nickname == null || nickname.isEmpty()) {
            ClientChat.showNetworkError("Логин и пароль обязательны!", "Валидация", null);
            return;
        }
        System.out.println("Update User");

        try {
            network.sendUpdateUserCommand(login,password,nickname);
        } catch (IOException e) {
            ClientChat.showNetworkError(e.getMessage(), "Auth error!", null);
            e.printStackTrace();
        }

    }

    public void setController(AuthController controller) {
        //this.network = network;
        this.controller = controller;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}