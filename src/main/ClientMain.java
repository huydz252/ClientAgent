package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import main.model.JsonMessage;
import main.model.Quiz;
import main.ui.QuizView;
import main.ui.ScreenLocker;
import main.ui.StudentLoginController;
import main.ui.WaitingController;
import main.util.SystemMonitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ClientMain extends Application {

    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 9999;
    private static Gson gson = new Gson();
    private static PrintWriter writer;
    
    private static Stage loginStage;
    private static StudentLoginController loginController;
    
    private static final List<String> FORBIDDEN_PROCESSES = List.of(
            "chrome.exe",
            "msedge.exe",
            "firefox.exe",
            "coccoc.exe"
        );

    @Override
    public void start(Stage primaryStage) {
    	Platform.setImplicitExit(false); 	//giữ cho JavaFX luôn sống, kể cả khi đóng hết của sổ giao diện
    	loginStage = primaryStage;
    	
    	//kết nối server trc khi hiển thị màn hình login
        new Thread(this::startSocketConnection).start();
        showLoginScreen();
    }
    
    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/ui/StudentLoginView.fxml"));
            Parent root = loader.load();
            
            loginController = loader.getController();

            loginStage.setTitle("Đăng nhập thi");
            loginStage.setScene(new Scene(root));
            loginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSocketConnection() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Đã kết nối tới Server: " + SERVER_IP);
            
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String serverJson;
            while ((serverJson = reader.readLine()) != null) {
            	System.out.println(">> [DEBUG] RAW RECV: " + serverJson);
                try {
                    JsonMessage msg = gson.fromJson(serverJson, JsonMessage.class);
                    
                    //xử lí tin nhắn đến -> ok -> đăng nhập
                    processMessage(msg); 
                } catch (JsonSyntaxException e) {
                    System.out.println("Lỗi JSON: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
            // Nếu không kết nối được, hiện lỗi lên màn hình Login (nếu đang mở)
            Platform.runLater(() -> {
                if (loginController != null) {
                    loginController.setStatus("Không kết nối được Server!", true);
                }
            });
        }
    }
    
 // Hàm static để Controller gọi
    public static void sendLoginRequest(String code, String name) {
        if (writer != null) {
            Map<String, Object> payload = Map.of(
                "studentCode", code,
                "fullName", name
            );
            JsonMessage msg = new JsonMessage("LOGIN_REQUEST", payload);
            writer.println(gson.toJson(msg));
        }
    }
    
    /**
     * Khởi động một luồng riêng để quét tiến trình
     * 5 giây một lần.
     */
    private void startProcessMonitor() {
    	
        Thread monitorThread = new Thread(() -> {
        	
            while (true) {
                try {
                    Thread.sleep(5000); 

                    List<String> runningProcesses = SystemMonitor.getRunningProcesses();
                    String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    
                    for (String forbidden : FORBIDDEN_PROCESSES) {
                        if (runningProcesses.contains(forbidden)) {
                            
                            System.out.println("[MONITOR] Phát hiện vi phạm: " + forbidden);
                            Map<String, Object> payload = Map.of(
                                "processName", forbidden,
                                "machineName", System.getenv("COMPUTERNAME"),
                                "time", timeStamp
                            );
                            JsonMessage alertMsg = new JsonMessage("ALERT_PROCESS_VIOLATION", payload);
                            
                            if (writer != null) {
                                writer.println(gson.toJson(alertMsg));
                            }
                            break; 
                        }
                    }
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        monitorThread.setDaemon(true); 
        monitorThread.start();
    }

    private void processMessage(JsonMessage message) {
    	
        if (message == null || message.getType() == null) return;
                
        switch (message.getType()) {
        
        case "LOGIN_RESPONSE":
            String status = (String) message.getPayload().get("status");
            String msgText = (String) message.getPayload().get("message");
            
            Platform.runLater(() -> {
                if ("SUCCESS".equals(status)) {
                    loginStage.hide();
                    System.out.println(">> Đăng nhập thành công! Đang gửi REGISTER_AGENT...");
                    
                    Platform.runLater(() -> {
                        if (loginStage != null) loginStage.hide();
                        if (loginController != null) loginController.setStatus("Đang vào hệ thống...", false);
                        WaitingController.show(msgText);
                    });
                    sendRegisterAgent();
                    
                    startProcessMonitor();
                    
                } else {
                    // Hiện lỗi lên UI
                    if (loginController != null) {
                        loginController.setStatus(msgText, true);
                    }
                }
            });
            	break;
        
            case "SERVER_CMD_LOCK":
                Platform.runLater(() -> ScreenLocker.show());
                break;
                
            case "SERVER_CMD_UNLOCK":
                Platform.runLater(() -> ScreenLocker.hide());
                break;
                
            case "SERVER_CMD_GET_CONFIG":
                System.out.println("[CLIENT] Nhận lệnh lấy Config. Đang thu thập...");
                Map<String, Object> config = SystemMonitor.getSystemConfig();
                JsonMessage configMsg = new JsonMessage("DATA_CONFIG", config);
                if (writer != null) {
                    writer.println(gson.toJson(configMsg));
                    System.out.println("[CLIENT] Đã gửi DATA_CONFIG về Server.");
                }
                break;
                
            case "SERVER_CMD_GET_PROCESSES":
                System.out.println("[CLIENT] Nhận lệnh lấy Processes. Đang quét...");
                List<String> processes = SystemMonitor.getRunningProcesses();
                Map<String, Object> payload = Map.of("processes", processes);
                JsonMessage procMsg = new JsonMessage("DATA_PROCESS_LIST", payload);
                
                if (writer != null) {
                    writer.println(gson.toJson(procMsg));
                    System.out.println("[CLIENT] Đã gửi DATA_PROCESS_LIST về Server.");
                }
                break;
                
             case "SERVER_CMD_START_QUIZ":
                System.out.println("Nhận được bài thi!!");
                Map<String, Object> payload_quiz = message.getPayload();
                
                //dịch map -> quiz
                java.lang.reflect.Type type = new TypeToken<Quiz>(){}.getType();
                Quiz quiz = gson.fromJson(gson.toJson(payload_quiz.get("quizData")), type);
                
                if(quiz != null) {
                	System.out.println("Đã nhận được bài thi: " + quiz.getSubject());
                	
                	 Platform.runLater(() -> {
                		 WaitingController.close();
                         QuizView.show(quiz);
                     });
                }
                break;
        }
    }
    
    private void sendRegisterAgent() {
        String machineName = System.getenv("COMPUTERNAME");
        if (machineName == null) machineName = "MAY_TEST_UNKNOWN";
        
        Map<String, Object> payload = Map.of("machineName", machineName);
        JsonMessage registerMsg = new JsonMessage("REGISTER_AGENT", payload);
        writer.println(gson.toJson(registerMsg));
    }

	/**
     * Hàm static để QuizController có thể gọi khi nộp bài.
     * Hàm này chịu trách nhiệm đóng gói dữ liệu và gửi qua Socket.
     */
    public static void sendSubmit(int quizId, List<Integer> answers, String submittedAt) {
        if (writer != null) {
            Map<String, Object> payload = Map.of(
                "quizId", quizId,
                "answers", answers,
                "submittedAt", submittedAt
            );
            
            JsonMessage msg = new JsonMessage("SUBMIT_QUIZ", payload);
            
            String jsonMsg = gson.toJson(msg);
            writer.println(jsonMsg);
            
            System.out.println("[CLIENT] Đã nộp bài cho bộ đề ID: " + quizId);
        } else {
            System.out.println("[CLIENT] Lỗi: Không tìm thấy kết nối đến Server (writer is null)");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}