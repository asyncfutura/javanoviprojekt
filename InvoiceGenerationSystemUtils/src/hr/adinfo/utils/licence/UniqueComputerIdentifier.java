/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.licence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;

/**
 *
 * @author Matej
 */
public class UniqueComputerIdentifier {
	private static String uniqueIndentifier = null;
	private static String OS = null;
	
	public static String GetUniqueIdentifier(){
		if (uniqueIndentifier != null) {
			return uniqueIndentifier;
		}
		
		try {
			if(isWindows()){
				getUniqueIdentifierWin();
			} else if(isUnix()){
				getUniqueIdentifierUnix();
			} else if(isMac()){
				getUniqueIdentifierMac();
			}
		} catch (Exception e) {
			// TODO
		}
		
		if (uniqueIndentifier != null) {
			return uniqueIndentifier;
		}
		
		return "0";
	}
	
	private static void getUniqueIdentifierWin() throws Exception {
		OutputStream os = null;
		InputStream is = null;

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		os = process.getOutputStream();
		is = process.getInputStream();

		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Scanner sc = new Scanner(is);
		try {
			while (sc.hasNext()) {
				String next = sc.next();
				if ("ProcessorId".equals(next)) {
					uniqueIndentifier = sc.next().trim();
					break;
				}
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static void getUniqueIdentifierUnix() throws Exception{
		String line = null;
		String marker = "Serial Number:";
		BufferedReader br = null;

		try {
			br = unixRead("dmidecode -t system");
			while ((line = br.readLine()) != null) {
				if (line.indexOf(marker) != -1) {
					uniqueIndentifier = line.split(marker)[1].trim();
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if(uniqueIndentifier != null)
			return;
		
		line = null;
		marker = "system.hardware.serial =";
		br = null;

		try {
			br = unixRead("lshal");
			while ((line = br.readLine()) != null) {
				if (line.indexOf(marker) != -1) {
					uniqueIndentifier = line.split(marker)[1].replaceAll("\\(string\\)|(\\')", "").trim();
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	private static BufferedReader unixRead(String command) throws Exception {

		OutputStream os = null;
		InputStream is = null;

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(command.split(" "));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		os = process.getOutputStream();
		is = process.getInputStream();

		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new BufferedReader(new InputStreamReader(is));
	}
	
	private static void getUniqueIdentifierMac() throws Exception {
		OutputStream os = null;
		InputStream is = null;

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] { "/usr/sbin/system_profiler", "SPHardwareDataType" });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		os = process.getOutputStream();
		is = process.getInputStream();

		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		String marker = "Serial Number";
		try {
			while ((line = br.readLine()) != null) {
				if (line.contains(marker)) {
					uniqueIndentifier = line.split(":")[1].trim();
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static String getOsName() {
		if(OS == null) {
			OS = System.getProperty("os.name").toLowerCase();
		}
		return OS;
	}
	private static boolean isWindows() {
		return getOsName().contains("win");
	}
	private static boolean isUnix() {
		return getOsName().contains("nix") || getOsName().contains("nux") || getOsName().contains("aix");
	}
	private static boolean isMac() {
		return getOsName().contains("mac");
	}
}
