/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
/**
 *
 * @author Matej
 */
public class ServerAppUpdater {
	
    private static final int UPDATE_CHECK_DELAY_SECONDS = 60 * 10;
    private static final int APP_VERSION = 119;
    private static final String user = "adinfo";
    private static final String pass = "ad1nf01oo%";
    private static final String urlAppVersion = "http://www.app.london.com.hr/serverAppVersion.txt";
    private static final String urlPrefix = "http://www.app.london.com.hr/";
	private static final String updaterCommand = "java -jar InvoiceGenerationSystemUpdater.jar server";
    
	public static void Init(){
		ClearTemp();
		
		// Setup update check
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ServerApp.appClosing)
						break;
					
					CheckForUpdate();
					
					try {
						Thread.sleep(1000 * UPDATE_CHECK_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckForUpdate(){
		ServerAppLogger.GetInstance().ShowMessage("Checking for update..." + System.lineSeparator());
		try {
			Authenticator.setDefault (new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication (user, pass.toCharArray());
				}
			});
			
			URL url = new URL(urlAppVersion);
			Scanner scanner = new Scanner(url.openStream());
			if (scanner.hasNext()) {
				int newAppVersion = Integer.parseInt(scanner.next());
				if(newAppVersion > APP_VERSION){
					ArrayList<String> filesToDownload = new ArrayList<String>();
					while (scanner.hasNext()) {
						filesToDownload.add(scanner.next());
					}
					UpdateServerApp(filesToDownload, urlPrefix);
                }
            }
        } catch (IOException ex) {
            ServerAppLogger.GetInstance().LogError(ex);
        }
        
		ServerAppLogger.GetInstance().ShowMessage("Checking for update done." + System.lineSeparator());
	}
	
	private static void UpdateServerApp(ArrayList<String> filesToDownload, String urlPrefix){
		ServerAppLogger.GetInstance().ShowMessage("Update found! Updating..." + System.lineSeparator());
		try {
			new File("temp").mkdir();
			
			for(int i = 0; i < filesToDownload.size(); ++i){
				InputStream inputStreamServerApp = new URL(urlPrefix + filesToDownload.get(i)).openStream();
				String targetPath = "temp" + File.separator + filesToDownload.get(i);
				Files.copy(inputStreamServerApp, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
			}
			
			String targetWorkingDirectory = Paths.get("temp").toAbsolutePath().toString();
			Process proc = Runtime.getRuntime().exec(updaterCommand, null, new File(targetWorkingDirectory));
			
			if(ServerApp.GetInstance() != null){
				ServerApp.GetInstance().OnAppClose();
			} else {
				System.exit(0);
			}
		} catch (IOException ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	private static void ClearTemp(){
		DeleteDirectory("temp");
	}
	
	private static void DeleteDirectory(String path) {
        File file  = new File(path);
        if(file.isDirectory()){
            String[] childFiles = file.list();
            if(childFiles != null) {
                for (String childFilePath : childFiles) {
                    DeleteDirectory(path + File.separator + childFilePath);
                }
            }
			file.delete();
        } else {
            file.delete();
        }
    }
}
