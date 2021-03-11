package client;

import client.models.ChatHistoryBuilder;
import client.models.Network;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


import java.io.IOException;

public class ViewController {

    @FXML
    public ListView<String> usersList;

    @FXML
    private TextArea chatHistory;
    @FXML
    private TextField textField;
    private Network network;
    private Stage primaryStage;

   // private ListView<String> usersList;
   private String selectedRecipient;
   private ChatHistoryBuilder historyBuilder;


    @FXML
    public void initialize() {
 //       usersList.setItems(FXCollections.observableArrayList(Main.USERS_TEST_DATA));
//        sendButton.setOnAction(event -> sendMessage());
//        textField.setOnAction(event -> sendMessage());

        //chatHistory.appendText(historyBuilder.readChatHistory());
        usersList.setItems(FXCollections.observableArrayList(ClientChat.USERS_TEST_DATA));

        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (! cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell ;
        });

    }

    @FXML
    private void sendMessage() {
        String message = textField.getText();
        appendMessage("Я: " + message);
        textField.clear();

        try {
            if (selectedRecipient != null) {
                network.sendPrivateMessage(selectedRecipient, message);
            } else {
                network.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = "Failed to send message";
            ClientChat.showNetworkError(e.getMessage(), errorMessage, primaryStage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void appendMessage(String message) {
        //historyBuilder.writeChatHistory(message);
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public TextField getTextField() {
        return textField;
    }

    public void setHistoryBuilder(ChatHistoryBuilder historyBuilder) {

        //this.historyBuilder = historyBuilder;
        chatHistory.appendText(historyBuilder.readChatHistory());

    }

}
