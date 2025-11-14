package main.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import main.model.Quiz;
import main.model.Question;
import java.util.List;
import java.util.ArrayList;

public class QuizController {

    @FXML private Label quizTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label progressLabel;
    
    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private ToggleGroup optionsGroup;
    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;
    
    private List<Integer> userAnswers = new ArrayList<>();

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
        
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            userAnswers.add(-1);
        }

        quizTitleLabel.setText(quiz.getTitle());
        showQuestion(0);
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
        
        userAnswers.set(currentQuestionIndex, selected);
    }

    @FXML
    private void handleNextButton() {
        saveCurrentAnswer();
        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }

    @FXML
    private void handleSubmitButton() {
        saveCurrentAnswer();
        System.out.println("Nộp bài! Đáp án: " + userAnswers);
        
        // (Bước tiếp theo: Gửi userAnswers về Server)
        // ClientMain.sendSubmit(currentQuiz.getId(), userAnswers);
    }
}