package main.ui; 

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.stage.Screen; 
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScreenLocker {

	 private static List<Stage> lockStages = new ArrayList<>();
	 private static Thread watcherThread; 
    private static volatile boolean isRunning = false; 

    /**
     * Hàm này PHẢI được gọi từ luồng JavaFX (Platform.runLater)
     */
    public static void show() {
        if (!lockStages.isEmpty()) {
            return;
        }
        
        System.out.println("[SCREEN LOCKER] Đang hiển thị màn hình khóa...");
        
        List<Screen> screens = Screen.getScreens();

        for (Screen screen : screens) {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED); 
            stage.setAlwaysOnTop(true); 
            
            Pane pane = new Pane();
            pane.setStyle("-fx-background-color: white;"); 
            Scene scene = new Scene(pane);
            stage.setScene(scene);

            Rectangle2D bounds = screen.getBounds();
            
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            // -------------------------------------

            stage.show();
            
            lockStages.add(stage);
        }

        isRunning = true;
        watcherThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Platform.runLater(() -> {
                    	for (Stage stage : lockStages) {
                            if (stage != null && !stage.isFocused()) {
                                stage.toFront();
                                stage.requestFocus();
                            }
                        }
                    });
                    
                    Thread.sleep(10000); 	
                } catch (InterruptedException e) {
                    break; 
                }
            }
            System.out.println("[SCREEN LOCKER] Đã dừng luồng canh gác.");
        });
        
        watcherThread.setDaemon(true); 
        watcherThread.start();
    }

    /**
     * Hàm này PHẢI được gọi từ luồng JavaFX (Platform.runLater)
     */
    public static void hide() {
        System.out.println("[SCREEN LOCKER] Đang ẩn màn hình khóa...");
        
        isRunning = false;
        if (watcherThread != null) {
            watcherThread.interrupt(); 
            watcherThread = null;
        }
        
        Platform.runLater(() -> {
            for (Stage stage : lockStages) {
                stage.close();
            }
            lockStages.clear();
        });
    }
}