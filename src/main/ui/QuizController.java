package main.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import main.ClientMain;
import main.model.Quiz;
import main.model.Question;
import java.util.List;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class QuizController {

    @FXML private Label quizTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label progressLabel;
    
    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;
    
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private ToggleGroup optionsGroup;
    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;
    private List<Integer> userAnswers = new ArrayList<>();

    private Timeline timeline;
    private int timeSeconds;

    @FXML
    public void initialize() {
        optionsGroup = new ToggleGroup();
        optionA.setToggleGroup(optionsGroup);
        optionB.setToggleGroup(optionsGroup);
        optionC.setToggleGroup(optionsGroup);
        optionD.setToggleGroup(optionsGroup);
    }

    public void loadQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        this.currentQuestionIndex = 0;
        
        this.userAnswers.clear();
        if (quiz.getQuestions() != null) {
            for (int i = 0; i < quiz.getQuestions().size(); i++) {
                userAnswers.add(-1);
            }
        }

        if (prevButton != null) prevButton.setVisible(false);
        nextButton.setVisible(true);
        submitButton.setVisible(false);
        submitButton.setDisable(false);

        quizTitleLabel.setText(quiz.getTitle());
        
        int minutes = quiz.getTimeLimit() > 0 ? quiz.getTimeLimit() : 45;
        startTimer(minutes); 

        showQuestion(0);
    }

    private void startTimer(int minutes) {
        this.timeSeconds = minutes * 60;
        updateTimerLabel();

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), event -> {
                timeSeconds--;
                updateTimerLabel();
                
                // HẾT GIỜ -> NỘP LUÔN (Không cần hỏi xác nhận)
                if (timeSeconds <= 0) {
                    timeline.stop();
                    System.out.println("[TIMER] Hết giờ! Tự động nộp bài...");
                    performSubmission(); // Gọi hàm nộp ngay lập tức
                }
            })
        );
        
        timeline.playFromStart();
    }

    private void updateTimerLabel() {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("Thời gian: %02d:%02d", minutes, seconds));
        
        if (timeSeconds < 60) {
            timerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            timerLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= currentQuiz.getQuestions().size()) return;

        Question q = currentQuiz.getQuestions().get(index);
        
        questionTextLabel.setText("Câu " + (index + 1) + ": " + q.getQuestionText());
        progressLabel.setText("Câu " + (index + 1) + " / " + currentQuiz.getQuestions().size());

        List<String> opts = q.getOptions();
        optionA.setText("A. " + (opts.size() > 0 ? opts.get(0) : ""));
        optionB.setText("B. " + (opts.size() > 1 ? opts.get(1) : ""));
        optionC.setText("C. " + (opts.size() > 2 ? opts.get(2) : ""));
        optionD.setText("D. " + (opts.size() > 3 ? opts.get(3) : ""));

        optionsGroup.selectToggle(null); 
        int selected = userAnswers.get(index);
        if (selected == 0) optionA.setSelected(true);
        if (selected == 1) optionB.setSelected(true);
        if (selected == 2) optionC.setSelected(true);
        if (selected == 3) optionD.setSelected(true);

        if (prevButton != null) prevButton.setVisible(index > 0);

        if (index == currentQuiz.getQuestions().size() - 1) {
            nextButton.setVisible(false);
            submitButton.setVisible(true);
        } else {
            nextButton.setVisible(true);
            submitButton.setVisible(false);
        }
    }
    
    private void saveCurrentAnswer() {
        int selected = -1;
        if (optionA.isSelected()) selected = 0;
        if (optionB.isSelected()) selected = 1;
        if (optionC.isSelected()) selected = 2;
        if (optionD.isSelected()) selected = 3;
        
        if (currentQuestionIndex < userAnswers.size()) {
            userAnswers.set(currentQuestionIndex, selected);
        }
    }

    @FXML
    private void handleNextButton() {
        saveCurrentAnswer();
        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }
    
    @FXML
    private void handlePrevButton() {
        saveCurrentAnswer();
        currentQuestionIndex--;
        showQuestion(currentQuestionIndex);
    }

    @FXML
    private void handleSubmitButton() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận nộp bài");
        alert.setHeaderText("Bạn có chắc chắn muốn nộp bài không?");
        alert.setContentText("Sau khi nộp, bạn sẽ không thể sửa lại đáp án.");
        
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.setAlwaysOnTop(true);

        Optional<ButtonType> result = alert.showAndWait();
        
 
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performSubmission();
        }
    }

    private void performSubmission() {
        if (timeline != null) {
            timeline.stop();
        }
        
        if (submitButton.isDisabled()) return; 
        submitButton.setDisable(true); 
        
        saveCurrentAnswer(); 
        System.out.println("Nộp bài! Đáp án: " + userAnswers);

        ClientMain.sendSubmit(currentQuiz.getId(), userAnswers);
        
        main.ui.QuizView.close();
    }
}