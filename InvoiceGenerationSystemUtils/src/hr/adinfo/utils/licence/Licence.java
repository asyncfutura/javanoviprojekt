/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.licence;

import static hr.adinfo.utils.Values.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.Cipher;

/**
 *
 * @author Matej
 */
public class Licence {
	private static final int LICENCE_NOT_EXIST = 0;
	private static final int LICENCE_EXPIRED = 1;
	private static final int LICENCE_NOT_MATCH = 2;
	private static final int LICENCE_CLIENT = 3;
	private static final int LICENCE_LOCAL_SERVER = 4;
	private static final int LICENCE_MASTER_LOCAL_SERVER = 5;
	private static String dbName = null;
	private static String userOib = null;
	private static int officeNumber = -1;
	private static String officeTag = null;
	private static int companyId = -1;
	private static String companyName = null;
	private static String companyAddress = null;
	private static String officeAddress = null;
	private static int cashRegisterNumber = -1;
	private static boolean isControlApp = false;
	private static Date expirationDate = null;
	
	public static boolean IsLicenceActivated(){
		return (GetCurrentLicence() != LICENCE_NOT_EXIST);
	}
	
	public static boolean IsLicenseDateValid(){
		return (GetCurrentLicence() != LICENCE_EXPIRED);
	}
	
	public static boolean IsLicenseComputerIdValid(){
		return (GetCurrentLicence() != LICENCE_NOT_MATCH);
	}
	
	public static boolean IsLocalServer(){
		return (GetCurrentLicence() == LICENCE_LOCAL_SERVER || GetCurrentLicence() == LICENCE_MASTER_LOCAL_SERVER);
	}
	
	public static boolean IsMasterLocalServer(){
		return (GetCurrentLicence() == LICENCE_MASTER_LOCAL_SERVER);
	}
	
	public static boolean IsLocalServerButNoMasterLocalServer(){
		return (GetCurrentLicence() == LICENCE_LOCAL_SERVER);
	}
	
	public static boolean IsControlApp(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return isControlApp;
		} else {
			return false;
		}
	}
	
	public static int GetCompanyId(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return companyId;
		} else {
			return -1;
		}
	}
	
	public static String GetDBName(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return dbName;
		} else {
			return null;
		}
	}
	
	public static String GetOIB(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return userOib;
		} else {
			return null;
		}
	}
	
	public static String GetCompanyName(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return companyName;
		} else {
			return null;
		}
	}
	
	public static int GetOfficeNumber(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return officeNumber;
		} else {
			return -1;
		}
	}
	
	public static String GetOfficeTag(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return officeTag;
		} else {
			return null;
		}
	}
	
	public static String GetCompanyAddress(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return companyAddress;
		} else {
			return null;
		}
	}
	
	public static String GetOfficeAddress(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return officeAddress;
		} else {
			return null;
		}
	}
	
	public static Date GetExpirationDate(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return expirationDate;
		} else {
			return null;
		}
	}
	
	public static int GetCashRegisterNumber(){
		int result = GetCurrentLicence();
		if(result == LICENCE_CLIENT || result == LICENCE_LOCAL_SERVER || result == LICENCE_MASTER_LOCAL_SERVER){
			return cashRegisterNumber;
		} else {
			return 0;
		}
	}
	
	public static int GetCurrentLicence(){
		int licenceValid = LICENCE_NOT_EXIST;
		try {
			licenceValid = CheckLicense();
		} catch (Exception ex) {}
		
		return licenceValid;
	}
	
	private static int CheckLicense() throws Exception {
		// Decrypt license
		byte[] keyBytes = Files.readAllBytes(new File(PATH_PUBLIC_KEY).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey publicKey = kf.generatePublic(spec);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		
		File f = new File(PATH_LICENCE);
		FileInputStream fis = new FileInputStream(f);
		byte[] fbytes = new byte[(int) f.length()];
		fis.read(fbytes);
		fis.close();
		
		byte[] licenceBytes = cipher.doFinal(fbytes);
		
		// Check license
		String licenceString = new String(licenceBytes);
		String[] licenceLines = licenceString.split(LICENCE_SPLIT_STRING);
		
		if(!"licence".equals(licenceLines[0]))
			return LICENCE_NOT_EXIST;
		
		if(!licenceLines[1].equals(UniqueComputerID.GetUniqueID())){
			return LICENCE_NOT_MATCH;
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		expirationDate =  df.parse(licenceLines[2]);
		if(new Date().after(expirationDate)){
			return LICENCE_EXPIRED;
		}
		
		// Read data from licence
		companyId = Integer.parseInt(licenceLines[4]);
		dbName = licenceLines[5];
		userOib = licenceLines[6];
		officeNumber = Integer.parseInt(licenceLines[7]);
		officeTag = licenceLines[8];
		companyName = licenceLines[9];
		companyAddress = licenceLines[10];
		officeAddress = licenceLines[11];
		cashRegisterNumber = Integer.parseInt(licenceLines[12]);
		isControlApp = "1".equals(licenceLines[13].trim());
		
		if(licenceLines[3].equals(Integer.toString(LICENCE_TYPE_CLIENT))){
			return LICENCE_CLIENT;
		} else if(licenceLines[3].equals(Integer.toString(LICENCE_TYPE_LOCAL_SERVER))){
			return LICENCE_LOCAL_SERVER;
		} else if(licenceLines[3].equals(Integer.toString(LICENCE_TYPE_MASTER_LOCAL_SERVER))){
			return LICENCE_MASTER_LOCAL_SERVER;
		}
		
		return LICENCE_NOT_EXIST;
	}
	
	public static void SavePublicKey(byte[] publicKeyBytes) throws Exception {
		File file = new File(PATH_PUBLIC_KEY);
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(publicKeyBytes);
		fos.flush();
		fos.close();
	}
	
	public static void SaveLicence(byte[] licenceBytes) throws Exception {
		File file = new File(PATH_LICENCE);
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(licenceBytes);
		fos.flush();
		fos.close();
	}
	
	public static void SaveActivationKey(String key) throws Exception {
		File file = new File(PATH_ACTIVATION_KEY);
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(key.getBytes());
		fos.flush();
		fos.close();
	}
	
	public static String GetActivationKey() {
		String activationKey = "";
		try {
			byte[] keyBytes = Files.readAllBytes(new File(PATH_ACTIVATION_KEY).toPath());
			activationKey = new String(keyBytes).trim().toUpperCase();
		} catch (IOException ex) { }
		return activationKey;
	}
}
