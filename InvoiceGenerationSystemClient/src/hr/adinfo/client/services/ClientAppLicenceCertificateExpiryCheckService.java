/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.licence.Licence;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class ClientAppLicenceCertificateExpiryCheckService {
			
	public static void Init(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 20);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					CheckForExpiry();
					
					try {
						Thread.sleep(1000L * 60 * 60 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_HOURS.ordinal()));
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckForExpiry(){
		Date licenceExpirationDate = Licence.GetExpirationDate();
		if(licenceExpirationDate == null)
			return;
		
		if(licenceExpirationDate.after(new Date())){
			long secs = (licenceExpirationDate.getTime() - new Date().getTime()) / 1000;
			long hours = secs / 3600;
			long days = hours / 24;
			
			if(days < ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS.ordinal())){
				ClientAppLogger.GetInstance().ShowMessage("Vaša licenca za program ističe za " + days + " dana!" 
						+ System.lineSeparator() + System.lineSeparator() + "Nakon isteka licence, program će prestati sa radom!"
						+ System.lineSeparator() + System.lineSeparator() + "Za produljenje licence molimo kontaktirajte servis.");
			}
		}
		
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = hr.adinfo.client.fiscalization.Fiscalization.GetPrivateCertificatePass(Values.CERT_TYPE_PROD).toCharArray();
			keyStore.load(new ByteArrayInputStream(hr.adinfo.client.fiscalization.Fiscalization.GetPrivateCertificateBytes(Values.CERT_TYPE_PROD)), password);
			if(keyStore.aliases().hasMoreElements()){
				X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement());
				if(cert.getNotAfter().after(new Date())){
					long secs = (cert.getNotAfter().getTime() - new Date().getTime()) / 1000;
					long hours = secs / 3600;
					long days = hours / 24;

					if(days < ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS.ordinal())){
						ClientAppLogger.GetInstance().ShowMessage("Vaš certifikat potreban za fiskalizaciju ističe za " + days + " dana!" 
								+ System.lineSeparator() + System.lineSeparator() + "Nakon isteka certifikata neće biti moguće fiskalizirati račune!"
								+ System.lineSeparator() + System.lineSeparator() + "Za obnovu certifikata molimo kontaktirajte servis.");
					}
				}
			}
		} catch(Exception ex){
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		// Einvoice
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = hr.adinfo.client.fiscalization.ElectronicInvoice.GetPrivateCertificatePass(Values.CERT_TYPE_EINVOICE_PROD).toCharArray();
			keyStore.load(new ByteArrayInputStream(hr.adinfo.client.fiscalization.ElectronicInvoice.GetPrivateCertificateBytes(Values.CERT_TYPE_EINVOICE_PROD)), password);
			if(keyStore.aliases().hasMoreElements()){
				X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement());
				if(cert.getNotAfter().after(new Date())){
					long secs = (cert.getNotAfter().getTime() - new Date().getTime()) / 1000;
					long hours = secs / 3600;
					long days = hours / 24;

					if(days < ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS.ordinal())){
						ClientAppLogger.GetInstance().ShowMessage("Vaš certifikat potreban za izdavanje e-računa ističe za " + days + " dana!" 
								+ System.lineSeparator() + System.lineSeparator() + "Nakon isteka certifikata neće biti moguće izdavati e-račune!"
								+ System.lineSeparator() + System.lineSeparator() + "Za obnovu certifikata molimo kontaktirajte servis.");
					}
				}
			}
		} catch(Exception ex){
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
