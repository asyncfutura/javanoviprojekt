/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import javax.swing.JOptionPane;
import hr.adinfo.utils.LoggerInterface;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class ServerAppLogger implements LoggerInterface {

	private static ServerAppLogger serverAppLogger;
	
	public static ServerAppLogger GetInstance(){
		if(serverAppLogger == null){
			serverAppLogger = new ServerAppLogger();
		}
		
		return serverAppLogger;
	}
	
	@Override
	public void ShowMessage(String message) {
		if(ServerApp.GetInstance() != null){
			ServerApp.GetInstance().PrintLog(message);
		} else {
			JOptionPane.showMessageDialog(null, message);
		}
	}
	
	@Override
	public void LogMessage(String message) {
		if(ServerApp.GetInstance() != null){
			ServerApp.GetInstance().PrintLog(message);
		}
	}
	
	@Override
	public void LogMessageDebug(String message) {
		LocalDateTime localDateTime = LocalDateTime.now();
		String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss"));
		
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		String filePathString = Paths.get("").toAbsolutePath() + File.separator + "logDebug" + File.separator + year + File.separator + month + File.separator + day + ".txt";
		Path path = Paths.get(filePathString);
		
		try {
			Files.createDirectories(path.getParent());
			if(Files.notExists(path)){
				Files.createFile(path);
			}
			Files.write(path, (timestamp + System.lineSeparator() + message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {}
	}

	@Override
	public void ShowErrorLog(Exception ex) {
		int depth = 10;
		String stackTraceMessage = "";
		for(int i = 0; i < ex.getStackTrace().length && i < depth; ++i){
			stackTraceMessage = stackTraceMessage.concat(ex.getStackTrace()[i].toString() + System.lineSeparator());
		}
		
		stackTraceMessage = stackTraceMessage.concat(System.lineSeparator());
		
		if(ex.getCause() != null){
			for(int i = 0; i < ex.getCause().getStackTrace().length && i < depth; ++i){
				stackTraceMessage = stackTraceMessage.concat(ex.getCause().getStackTrace()[i].toString() + System.lineSeparator());
			}
		}

		if(ServerApp.GetInstance() != null){
			ServerApp.GetInstance().PrintLog(stackTraceMessage + System.lineSeparator() + ex.toString() + System.lineSeparator());
		} else {
			JOptionPane.showMessageDialog(null, stackTraceMessage + System.lineSeparator() + ex.toString());
		}
		
		LogError(ex);
	}
	
	@Override
	public void LogError(Exception ex) {
		int depth = 100;
		String stackTraceMessage = "";
		for(int i = 0; i < ex.getStackTrace().length && i < depth; ++i){
			stackTraceMessage = stackTraceMessage.concat(ex.getStackTrace()[i].toString() + System.lineSeparator());
		}
		
		stackTraceMessage = stackTraceMessage.concat(System.lineSeparator());
		
		if(ex.getCause() != null){
			for(int i = 0; i < ex.getCause().getStackTrace().length && i < depth; ++i){
				stackTraceMessage = stackTraceMessage.concat(ex.getCause().getStackTrace()[i].toString() + System.lineSeparator());
			}
		}

		LogMessageDebug(stackTraceMessage + System.lineSeparator() + ex.toString() + System.lineSeparator());
	}
}