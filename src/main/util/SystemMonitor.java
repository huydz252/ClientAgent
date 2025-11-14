package main.util; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.text.DecimalFormat;

public class SystemMonitor {

    /**
     * Thu thập tất cả thông tin hệ thống quan trọng.
     * @return Một Map chứa thông tin.
     */
    public static Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("OS Name", System.getProperty("os.name"));
        config.put("OS Version", System.getProperty("os.version"));
        config.put("OS Arch", System.getProperty("os.arch"));

        config.put("CPU Cores", Runtime.getRuntime().availableProcessors());

        long maxMemory = Runtime.getRuntime().maxMemory(); 
        long totalMemory = Runtime.getRuntime().totalMemory(); 
        long freeMemory = Runtime.getRuntime().freeMemory(); 

        config.put("JVM Max RAM", bytesToMegabytes(maxMemory));
        config.put("JVM Total RAM", bytesToMegabytes(totalMemory));
        config.put("JVM Free RAM", bytesToMegabytes(freeMemory));
        
        return config;
    }

    /**
     * Hàm tiện ích chuyển đổi Bytes sang Megabytes (MB)
     */
    private static String bytesToMegabytes(long bytes) {
        DecimalFormat df = new DecimalFormat("#.##"); 
        return df.format(bytes / (1024.0 * 1024.0)) + " MB";
    }
    
    /**
     * Lấy danh sách các tiến trình đang chạy.
     * @return Một List<String> tên các tiến trình.
     */
    public static List<String> getRunningProcesses() {
        return ProcessHandle.allProcesses()	
            .map(ph -> ph.info().command().orElse("N/A"))
            .map(path -> new File(path).getName()) 
            .filter(name -> !name.isEmpty() && !name.equals("N/A")) 
            .distinct() 
            .sorted() 
            .collect(Collectors.toList());
    }
}