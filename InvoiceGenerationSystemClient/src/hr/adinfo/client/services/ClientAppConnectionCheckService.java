/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppServerAppClient;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.LocalServerMasterLocalServerClient;
import hr.adinfo.client.MasterLocalServer;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.licence.Licence;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.FocusManager;
import javax.swing.JLabel;

/**
 *
 * @author Matej
 */
public class ClientAppConnectionCheckService {
	
	public static final Color errorColorRed = new Color(1.0f, 0.7f, 0.7f);
	
	private static Date lastServerOnlineDate;
		
	public static void Init(){
		lastServerOnlineDate = new Date();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					CheckConnection();
					
					if(Licence.IsMasterLocalServer() && ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTING_ADMIN_SERVER_STATUS_NOTIFICATION.ordinal())){
						long diffInMS = new Date().getTime() - lastServerOnlineDate.getTime();
						if(diffInMS > 1000 * 60 * 60){
							SendServerOfflineNotification();
						}
					}
					
					try {
						Thread.sleep(4000);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckConnection(){
		boolean newIsConnectionValid = true;
		if(Licence.IsMasterLocalServer()){
			if(ClientAppServerAppClient.GetInstance() == null || !ClientAppServerAppClient.GetInstance().IsValid()){
				newIsConnectionValid = false;
				if(MasterLocalServer.GetInstance() != null){
					MasterLocalServer.GetInstance().isMasterSynced = false;
					ClientAppLogger.GetInstance().LogMessage("E601");
				} else {
					ClientAppLogger.GetInstance().LogMessage("E602");
				}
			} else {
				lastServerOnlineDate = new Date();
			}
			if(LocalServerMasterLocalServerClient.GetInstance() == null || !LocalServerMasterLocalServerClient.GetInstance().IsValid()){
				newIsConnectionValid = false;
				ClientAppLogger.GetInstance().LogMessage("E603");
			}
			if(MasterLocalServer.GetInstance() != null && MasterLocalServer.GetInstance().isMasterSynced == false){
				newIsConnectionValid = false;
				ClientAppLogger.GetInstance().LogMessage("E604");
			}
		} else if(Licence.IsLocalServer()){
			if(LocalServerMasterLocalServerClient.GetInstance() == null || !LocalServerMasterLocalServerClient.GetInstance().IsValid()){
				newIsConnectionValid = false;
				ClientAppLogger.GetInstance().LogMessage("E605");
			}
		} else {
			if(ClientAppLocalServerClient.GetInstance() == null || !ClientAppLocalServerClient.GetInstance().IsValid()){
				newIsConnectionValid = false;
				ClientAppLogger.GetInstance().LogMessage("E606");
			}
		}
		
		if(newIsConnectionValid){
			SetInternetConnectionMessage("");
		} else if(!newIsConnectionValid){
			SetInternetConnectionMessage("NEMA INTERNETSKE VEZE");
		}
	}
	
	private static void SetInternetConnectionMessage(String message){
		Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
		if(activeWindow != null){
			List<Component> compList = Utils.GetAllComponents(activeWindow);
			for (Component comp : compList) {
				if("jLabelInternetConnection".equals(comp.getName())){
					((JLabel)comp).setText(message);
				}
			}
		}
	}
	
	private static void SendServerOfflineNotification(){
		String emailTarget = "office.accable.servis@gmail.com, office.accable@gmail.com";
		
		final String username = "noreply.accable@gmail.com";
        final String password = "noreply123";
        final String subject = "NEMA VEZE SA SERVEROM - " + Licence.GetOfficeAddress();
		
        Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
			new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget));
            message.setSubject(subject);
            message.setText(subject);
            Transport.send(message);
			lastServerOnlineDate = new Date();
        } catch (Exception e) {
			ClientAppLogger.GetInstance().LogError(e);
        }
	}
}
