package main.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import main.model.Quiz;

public class QuizView {

    private static Stage quizStage;

    public static void show(Quiz quiz) {
        // Nếu cửa sổ cũ còn, đóng nó đi để tạo mới (reset trạng thái)
        if (quizStage != null) {
            quizStage.close();
        }

        try {
            FXMLLoader loader = new FXMLLoader(QuizView.class.getResource("/main/ui/QuizView.fxml"));
            Parent root = loader.load();

            QuizController controller = loader.getController();
            controller.loadQuiz(quiz); 

            quizStage = new Stage();
            quizStage.setTitle(quiz.getTitle());
            quizStage.setScene(new Scene(root));
            
            // Cấu hình full screen không viền
            quizStage.initStyle(StageStyle.UNDECORATED);
            quizStage.setAlwaysOnTop(true);

            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            quizStage.setX(screenBounds.getMinX());
            quizStage.setY(screenBounds.getMinY());
            quizStage.setWidth(screenBounds.getWidth());
            quizStage.setHeight(screenBounds.getHeight());

            quizStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void close() {
        if (quizStage != null) {
            quizStage.close();
            quizStage = null;
        }
    }
}