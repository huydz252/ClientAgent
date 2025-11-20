package main.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class WaitingController {

    @FXML private Label infoLabel;
    private static Stage stage;

    public void setStudentInfo(String info) {
        infoLabel.setText(info);
    }

    // Hàm static tiện ích để ClientMain gọi cho lẹ
    public static void show(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(WaitingController.class.getResource("/main/ui/WaitingView.fxml"));
            Parent root = loader.load();
            
            WaitingController controller = loader.getController();
            controller.setStudentInfo(message); // Set tên SV vào label

            if (stage == null) {
                stage = new Stage();
                stage.setTitle("Phòng chờ thi");
            }
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void close() {
        if (stage != null) {
            stage.close();
            stage = null;
        }
    }
}