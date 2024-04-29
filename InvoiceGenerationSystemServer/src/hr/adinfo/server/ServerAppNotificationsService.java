/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import hr.adinfo.utils.Utils;
import static hr.adinfo.utils.Values.SERVER_APP_CONTROL_LOCALHOST_PORT;
import static hr.adinfo.utils.Values.SERVER_APP_DATABASE_NAME;
import static hr.adinfo.utils.Values.SERVER_APP_DATABASE_SERVER_PORT;
import static hr.adinfo.utils.Values.SERVER_APP_LOCALHOST;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Matej
 */
public class ServerAppNotificationsService {
	private static final int SERVICE_LOOP_DELAY_SECONDS = 5;
	
	private static Date lastNotifDate;
	private static Connection databaseConnection;
	
	public static void Init(){
		lastNotifDate = new Date();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ServerApp.appClosing)
						break;
					
					CheckForNotification();
					
					try {
						Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckForNotification(){
		String emailTarget1 = GetNotificationsValue("email1");
		String emailTarget2 = GetNotificationsValue("email2");
		String emailTarget3 = GetNotificationsValue("email3");
		String emailTarget4 = GetNotificationsValue("email4");
		if("".equals(emailTarget1) && "".equals(emailTarget2) && "".equals(emailTarget3) && "".equals(emailTarget4))
			return;
		
		Date notifDate1 = null;
		Date notifDate2 = null;
		String prefix1 = GetNotificationsValue("time1");
		String prefix2 = GetNotificationsValue("time2");
		Date nowDate = new Date();
		String sufix = new SimpleDateFormat(":00 dd.MM.yyyy.").format(nowDate);
		
		try {
			notifDate1 = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy.").parse(prefix1 + sufix);
			notifDate2 = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy.").parse(prefix2 + sufix);
		} catch(Exception ex){
			return;
		}
		
		if(nowDate.after(notifDate1) && lastNotifDate.before(notifDate1)){
			lastNotifDate = notifDate1;
			String messageString = GetMessageString();
			if(!"".equals(messageString)){
				SendNotification(messageString, emailTarget1, emailTarget2, emailTarget3, emailTarget4);
			}
		}
		
		if(nowDate.after(notifDate2) && lastNotifDate.before(notifDate2)){
			lastNotifDate = notifDate2;
			String messageString = GetMessageString();
			if(!"".equals(messageString)){
				SendNotification(messageString, emailTarget1, emailTarget2, emailTarget3, emailTarget4);
			}
		}
	}
	
	private static void SendNotification(String messageString, String emailTarget1, String emailTarget2, String emailTarget3, String emailTarget4){
		ServerAppLogger.GetInstance().ShowMessage("Sending daily notification to emails: " 
				+ emailTarget1 + ", " + emailTarget2 + ", " + emailTarget3 + ", " + emailTarget4 + System.lineSeparator());
		
	final String username = "noreply.accable@gmail.com";
        final String password = "noreply123";
        final String subject = "Dnevni izvještaj - neaktivne poslovnice";
		
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

		if(!"".equals(emailTarget1)){
			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget1));
				message.setSubject(subject);
				message.setText(messageString);
				Transport.send(message);
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
        
		if(!"".equals(emailTarget2)){
			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget2));
				message.setSubject(subject);
				message.setText(messageString);
				Transport.send(message);
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(!"".equals(emailTarget3)){
			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget3));
				message.setSubject(subject);
				message.setText(messageString);
				Transport.send(message);
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(!"".equals(emailTarget4)){
			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget4));
				message.setSubject(subject);
				message.setText(messageString);
				Transport.send(message);
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String GetMessageString(){
		String toReturn = "";
		
		try {
			String query = "SELECT OFFICES.ID, COMPANIES.NAME, OFFICES.ADDRESS, OFFICE_NUMBER, OFFICE_TAG, OFFICES.OFFICE_NAME, "
					+ "OFFICES.LAST_PING_DATE, OFFICES.LAST_PING_TIME, COMPANIES.IS_DELETED, OFFICES.IS_DELETED "
				+ "FROM (OFFICES INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "ORDER BY COMPANIES.NAME, OFFICE_NUMBER ASC";
			PreparedStatement preparedStatement = getDatabaseConnection().prepareStatement(query);
			ResultSet result = preparedStatement.executeQuery();
			while (result.next()) {
				if (result.getInt(9) == 1 || result.getInt(10) == 1)
					continue;
				
				Date lastPingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(result.getString(7) + " " + result.getString(8));
				long secs = (new Date().getTime() - lastPingDate.getTime()) / 1000;
				long hours = secs / 3600;
				if(hours < 12)
					continue;
				
				if("".equals(toReturn)){
					toReturn += "Popis neaktivnih poslovnica (Ime firme, redni broj poslovnice, oznaka PP, adresa poslovnice, zadnja aktivnost): " + System.lineSeparator();
				}
				
				toReturn += result.getString(2) + ", " + result.getString(4) + ", " + result.getString(5) + ", " + result.getString(3) + ", Prije " + hours + " sati" + System.lineSeparator();
			}
		} catch (Exception ex){
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return toReturn;
	}
	
	public static String GetNotificationsValue(String name){
		String toReturn = "";
		
		try {
			String query = "SELECT VALUE FROM NOTIFICATIONS WHERE NAME = ?";
			PreparedStatement preparedStatement = getDatabaseConnection().prepareStatement(query);
			preparedStatement.setString(1, name);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				toReturn = result.getString(1);
			}
		} catch (Exception ex){
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return toReturn;
	}
	
	public static boolean SetNotificationsValue(String name, String value){
		boolean success = false;
		
		try {
			String query = "UPDATE LOCAL_VALUES_TABLE SET VALUE = ? WHERE NAME = ?";
			PreparedStatement preparedStatement = getDatabaseConnection().prepareStatement(query);
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, value);
			int result = preparedStatement.executeUpdate();
			if (result != 0) {
				success = true;
			}
		} catch (Exception ex){
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return success;
	}
	
	private static Connection getDatabaseConnection() throws UnknownHostException, SQLException{
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null){
			databaseConnection = Utils.getDatabaseConnection(SERVER_APP_LOCALHOST, SERVER_APP_DATABASE_SERVER_PORT, SERVER_APP_DATABASE_NAME);
		}
		
		return databaseConnection;
	}
}
