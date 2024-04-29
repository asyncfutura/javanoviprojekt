/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.certificates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 *
 * @author Matej
 */
public class CertificateManager {
	public static void SaveCertificate (String alias, byte[] certBytes) throws FileNotFoundException, IOException{
		if(certBytes == null)
			return;
		
		File file = new File("certificates/" + alias + ".certificate");
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(certBytes);
		fos.flush();
		fos.close();
	}
	
	public static X509Certificate GetCertificate(String alias) throws FileNotFoundException, CertificateException, IOException{
		File certFile = new File("certificates/" + alias + ".certificate");
		CertificateFactory certificateFactoryX509 = CertificateFactory.getInstance("X.509");
		InputStream inputStream = new FileInputStream(certFile);
		X509Certificate certificate = (X509Certificate) certificateFactoryX509.generateCertificate(inputStream);
		inputStream.close();
		
		return certificate;
	}
}
