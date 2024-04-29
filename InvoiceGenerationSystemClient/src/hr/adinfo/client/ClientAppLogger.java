/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import javax.swing.JOptionPane;
import hr.adinfo.utils.LoggerInterface;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author Matej
 */
public class ClientAppLogger implements LoggerInterface {

	private static ClientAppLogger logger;
	
	public static ClientAppLogger GetInstance(){
		if(logger == null){
			logger = new ClientAppLogger();
		}
		
		return logger;
	}
	
	@Override
	public void ShowMessage(String message) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message);
			}
		});
	}
	
	public void ShowMessageBlock(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	@Override
	public void ShowErrorLog(Exception ex) {
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
		
		final String message = stackTraceMessage;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message + System.lineSeparator() + ex.toString());
			}
		});
		
		LogToFile(ex.toString() + System.lineSeparator() + stackTraceMessage, "log");
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
		
		LogToFile(ex.toString() + System.lineSeparator() + stackTraceMessage, "log");
	}
	
	@Override
	public void LogMessage(String message) {
		LogToFile(message, "log");
	}
	
	@Override
	public void LogMessageDebug(String message) {
		LogToFile(message, "logDebug");
	}
	
	private synchronized void LogToFile(String message, String folderName){
		String timestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
		message = System.lineSeparator() + System.lineSeparator() + timestamp + System.lineSeparator() + message;
		
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		String filePathString = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + ".txt";
		Path path = Paths.get(filePathString);
		
		try {
			Files.createDirectories(path.getParent());
			if(Files.notExists(path)){
				Files.createFile(path);
			}
			Files.write(path, (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {}
		
		File file = new File(filePathString);
		if(file.length() > /*1048576*/ 248576){
			int sufixCounter = 0;
			String filePathStringNew = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + "-0" + ".txt";
			while (new File(filePathStringNew).exists()){
				++sufixCounter;
				filePathStringNew = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + "-" + sufixCounter + ".txt";
			}
			
			try {
				Files.copy(path, Paths.get(filePathStringNew));
				file.delete();
			} catch (Exception ex){
				ex.printStackTrace();
			}
			
			/*String filePathStringTemp = filePathString + "temp.txt";
			Path pathTemp = Paths.get(filePathStringTemp);
			File fileTemp = new File(filePathStringTemp);
			try {
				Files.copy(path, pathTemp, StandardCopyOption.REPLACE_EXISTING);
				int linesCount = LinesCount(filePathString);
				Scanner fileScanner = new Scanner(fileTemp);
				for(int i = 0; i < linesCount / 2; ++i){
					if(fileScanner.hasNextLine()){
						fileScanner.nextLine();
					}
				}
				FileWriter fileStream = new FileWriter(filePathString);
				BufferedWriter out = new BufferedWriter(fileStream);
				while(fileScanner.hasNextLine()) {
					String next = fileScanner.nextLine();
					out.write(next);
					out.newLine();   
				}
				out.close();
				fileScanner.close();
				fileTemp.delete();
			} catch (Exception ex){
				ex.printStackTrace();
				file.delete();
			}*/
		}
	}
	
	public static int LinesCount(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}
	
	public void LogFiscalizationXMLSent(SOAPMessage message, String invoiceName) {
		String fiscMsg = soapMessageToString(message);
		LogFiscalizationXMLToFile(fiscMsg, "xmlOut", invoiceName);
	}
	
	public void LogFiscalizationXMLReceived(SOAPMessage message, String invoiceName) {
		String fiscMsg = soapMessageToString(message);
		LogFiscalizationXMLToFile(fiscMsg, "xmlIn", invoiceName);
	}
	
	private synchronized void LogFiscalizationXMLToFile(String message, String folderName, String fileName){
		String timestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
		message = System.lineSeparator() + System.lineSeparator() + timestamp + System.lineSeparator() + message;
		
		String filePathString = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + fileName + ".txt";
		Path path = Paths.get(filePathString);
		
		try {
			Files.createDirectories(path.getParent());
			if(Files.notExists(path)){
				Files.createFile(path);
			}
			Files.write(path, (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {}
		
		File file = new File(filePathString);
		if(file.length() > /*1048576*/ 248576){
			String filePathStringTemp = filePathString + "temp.txt";
			Path pathTemp = Paths.get(filePathStringTemp);
			File fileTemp = new File(filePathStringTemp);
			try {
				Files.copy(path, pathTemp, StandardCopyOption.REPLACE_EXISTING);
				int linesCount = LinesCount(filePathString);
				Scanner fileScanner = new Scanner(fileTemp);
				for(int i = 0; i < linesCount / 2; ++i){
					if(fileScanner.hasNextLine()){
						fileScanner.nextLine();
					}
				}
				FileWriter fileStream = new FileWriter(filePathString);
				BufferedWriter out = new BufferedWriter(fileStream);
				while(fileScanner.hasNextLine()) {
					String next = fileScanner.nextLine();
					out.write(next);
					out.newLine();   
				}
				out.close();
				fileScanner.close();
				fileTemp.delete();
			} catch (Exception ex){
				ex.printStackTrace();
				file.delete();
			}
		}
	}
	
	public String soapMessageToString(SOAPMessage message) {
        String result = null;

        if (message != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos); 
                result = baos.toString();
            } catch (Exception e){
				
			} finally {
                if (baos != null)  {
                    try {
                        baos.close();
                    } catch (IOException ioe){}
                }
            }
        }
        return result;
    }
}
