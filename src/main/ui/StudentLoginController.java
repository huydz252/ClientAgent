package main.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.ClientMain;

public class StudentLoginController {

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void handleLogin() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            statusLabel.setText("");
            return;
        }

        statusLabel.setText("Đang kiểm tra...");
        loginButton.setDisable(true);

        // Gọi hàm gửi request bên ClientMain
        ClientMain.sendLoginRequest(code, name);
    }

    // Hàm này để ClientMain gọi lại khi có kết quả từ Server
    public void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        loginButton.setDisable(false);
    }
}