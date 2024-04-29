/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLogger;
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
public class ClientAppUpdater {
    
    private static final int UPDATE_CHECK_DELAY_SECONDS = 60 * 10;
    private static final int APP_VERSION = 114;
    private static final String user = "adinfo";
    private static final String pass = "ad1nf01oo%";
    private static final String urlAppVersion = "http://www.app.london.com.hr/clientAppVersion.txt";
    private static final String urlAppVersionTest = "http://www.app.london.com.hr/test/clientAppVersion.txt";
    private static final String urlPrefix = "http://www.app.london.com.hr/";
    private static final String urlPrefixTest = "http://www.app.london.com.hr/test/";
	private static final String updaterCommand = "java -jar InvoiceGenerationSystemUpdater.jar client";
    
	private static boolean firstUpdateCheckDone;
	private static boolean updateFound;
	
	public static void Init(){
		firstUpdateCheckDone = false;
		updateFound = false;
		ClearTemp();
		
		// Setup update check
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					CheckForUpdate(false);
					
					try {
						Thread.sleep(1000 * UPDATE_CHECK_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	public static void CheckForTestUpdate(){
		CheckForUpdate(true);
	}
	
	private static void CheckForUpdate(boolean isTest){
		if(updateFound)
			return;
		
		try {
			Authenticator.setDefault (new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication (user, pass.toCharArray());
				}
			});
			
			URL url = new URL(isTest ? urlAppVersionTest : urlAppVersion);
			Scanner scanner = new Scanner(url.openStream());
			if (scanner.hasNext()) {
				firstUpdateCheckDone = true;
				int newAppVersion = Integer.parseInt(scanner.next());
				if(newAppVersion > APP_VERSION){
					ArrayList<String> filesToDownload = new ArrayList<String>();
					while (scanner.hasNext()) {
						filesToDownload.add(scanner.next());
					}
					UpdateClientApp(filesToDownload, isTest ? urlPrefixTest : urlPrefix);
				}
			}
		} catch (Exception ex) {}
	}
	
	private static void UpdateClientApp(ArrayList<String> filesToDownload, String urlPrefix){
		updateFound = true;
		ClientAppLogger.GetInstance().ShowMessage("Novo ažuriranje je dostupno. Aplikacija će se uskoro ponovno pokrenuti!");
		try {
			new File("temp").mkdir();
			
			for(int i = 0; i < filesToDownload.size(); ++i){
				InputStream inputStreamServerApp = new URL(urlPrefix + filesToDownload.get(i)).openStream();
				String targetPath = "temp" + File.separator + filesToDownload.get(i);
				Files.copy(inputStreamServerApp, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
			}
			
			String targetWorkingDirectory = Paths.get("temp").toAbsolutePath().toString();
			Process proc = Runtime.getRuntime().exec(updaterCommand, null, new File(targetWorkingDirectory));
			
			if(ClientApp.GetInstance() != null){
				ClientApp.GetInstance().OnAppClose();
			} else {
				System.exit(0);
			}
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
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
	
	public static void WaitForFirstUpdateCheck(){
		int counter = 0;
		int delayMs = 100;
		int totalCheckTimeMs = 3000;
		int onUpdateCheckTimeMs = 10000;
		
		while(counter < totalCheckTimeMs / delayMs){
			++counter;
			if(ClientApp.appClosing)
				break;

			if(firstUpdateCheckDone)
				break;

			try {
				Thread.sleep(delayMs);
			} catch (InterruptedException ex) {}
		}
		
		counter = 0;
		if(updateFound){
			while(counter < onUpdateCheckTimeMs / delayMs){
				++counter;
				if(ClientApp.appClosing)
					break;

				try {
					Thread.sleep(delayMs);
				} catch (InterruptedException ex) {}
			}
		}
	}
}
