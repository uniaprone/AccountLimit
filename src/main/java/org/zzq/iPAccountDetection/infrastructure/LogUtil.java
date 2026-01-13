package org.zzq.iPAccountDetection.infrastructure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class LogUtil {
    private Logger logger;
    private File logFile;

    public LogUtil(Logger logger, File pluginFolder) {
        this.logger = logger;
        this.logFile = new File(pluginFolder, "log.log");
        ensureLogFileExist();
    }

    private void ensureLogFileExist(){
        if(!logFile.exists()){
            try{
                logFile.createNewFile();
            }catch (IOException e){
                logger.severe("无法创建日志文件: " + e.getMessage());
            }
        }
    }

    public void info(String player, String ip, String message){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formatMessage = String.format("[INFO] [%s] [%s] [%s] %s", timestamp, player, ip, message);
        logToFile(formatMessage);
    }

    public void info(String message){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formatMessage = String.format("[INFO] [%s] %s", timestamp, message);
        logToFile(formatMessage);
    }

    public void warn(String player, String ip, String message){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formatMessage = String.format("[WARM] [%s] [%s] [%s] %s", timestamp, player, ip, message);
        logger.info(formatMessage);
        logToFile(formatMessage);
    }

    public void warn(String message){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formatMessage = String.format("[WARM] [%s] %s", timestamp, message);
        logger.info(formatMessage);
        logToFile(formatMessage);
    }

    public void error(String message){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formatMessage = String.format("[ERROR] [%s] %s", timestamp, message);
        logger.info(formatMessage);
        logToFile(formatMessage);
    }

    public void logToFile(String message) {
        try(FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(message + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            logger.severe("未能写入至文件: " + e.getMessage());
        }
    }
}
