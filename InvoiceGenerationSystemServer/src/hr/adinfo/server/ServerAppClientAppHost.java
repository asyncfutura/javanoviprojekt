/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import static hr.adinfo.server.ServerApp.totalString;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.MasterServerIPQuery;
import hr.adinfo.utils.communication.MasterServerIPResponse;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.communication.misc.LocalServerServerAppPingData;
import hr.adinfo.utils.database.DatabaseDiff;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.KeyGeneratorRSA;
import hr.adinfo.utils.licence.LicenceQuery;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Cipher;

/**
 *
 * @author Matej
 */
public class ServerAppClientAppHost {
	private static Connection databaseConnection;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Object oosLock = new Object();
	
	// Diff sync
	private Connection syncDatabaseConnection;
	private boolean isMasterLocalServer;
	private boolean isLocalServer;
	private int companyId;
	private String companyName;
	private ServerResponseList serverDiffsResponseList = new ServerResponseList();
	private boolean diffTableExist;
	private final ServerAppClientAppHost thisHost;
	private String debugDBName = "";
	private int debugCompanyId = -1;
	private int debugOfficeId = -1;
	
	private int licenceId = -1;
	
	private static final int DIFF_SYNC_LOOP_DELAY_SECONDS = 10;
	
	ServerAppClientAppHost(Socket clientSocket) {
		ServerAppLogger.GetInstance().ShowMessage("Accepting connection from Client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
		thisHost = this;
		ServerApp.GetInstance().AddConnectionCloseHostListener(thisHost);
		this.clientSocket = clientSocket;
		
		try {
			CreateStreams();
			ReadQueries();
			SyncDiffs();
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	private void CreateStreams() throws Exception {
		oos = new ObjectOutputStream(clientSocket.getOutputStream());
		oos.flush();
		ois = new ObjectInputStream(clientSocket.getInputStream());
	}
	
	public void OnConnectionClose(String targetLicenceId){
		if(targetLicenceId.equals(Integer.toString(licenceId))){
			try {
				clientSocket.close();
			} catch (Exception ex) {}
		}
	}
        	
	private void ReadQueries(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							ServerAppLogger.GetInstance().ShowMessage("Null message recieved " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						} else if (inputObject instanceof MasterServerIPQuery){
							MasterServerIPResponse masterServerIPResponse = new	MasterServerIPResponse((MasterServerIPQuery) inputObject);
							if(isLocalServer){
								masterServerIPResponse.masterIp = GetMasterIp();
								ServerAppLogger.GetInstance().ShowMessage("MasterServerIPQuery success! " + companyId + " " + companyName + " masterIp:" + masterServerIPResponse.masterIp + " " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
							} else {
								masterServerIPResponse.masterIp = "";
								ServerAppLogger.GetInstance().ShowMessage("MasterServerIPQuery failed! Server ping not recieved yet " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
							}
							synchronized (oosLock){
								oos.writeObject(masterServerIPResponse);
								oos.flush();
							}
							ServerAppLogger.GetInstance().ShowMessage("");
						} else if(inputObject instanceof LicenceQuery){
							LicenceQuery element = (LicenceQuery) inputObject;
							LicenceQueryResponse licenceQueryResponse = new LicenceQueryResponse(element);
							try {
								if(element.queryType == Values.LICENCE_QUERY_ACTIVATE){
									licenceQueryResponse = ActivateLicence(element);
								} else {
									licenceQueryResponse = RefreshLicence(element);
								}
							} catch (Exception ex) {
								ServerAppLogger.GetInstance().ShowErrorLog(ex);
								licenceQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								if(element.queryType == Values.LICENCE_QUERY_ACTIVATE){
									licenceQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_ACTIVATION_FAILED;
								} else {
									licenceQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_REFRESH_FAILED;
								}
							}
							synchronized (oosLock){
								oos.writeObject(licenceQueryResponse);
								oos.flush();
							}
							
							// Print log
							if(licenceQueryResponse.licenceErrorCode == Values.LICENCE_ERROR_CODE_WRONG_CODE){
								ServerAppLogger.GetInstance().ShowMessage("Licence activation failed (wrong code)");
							} else if(licenceQueryResponse.licenceErrorCode == Values.LICENCE_ERROR_CODE_ALREADY_ACTIVE){
								ServerAppLogger.GetInstance().ShowMessage("Licence activation failed (already active)");
							} else if(licenceQueryResponse.licenceErrorCode == Values.LICENCE_ERROR_CODE_ACTIVATION_SUCCESS){
								ServerAppLogger.GetInstance().ShowMessage("Licence activation success");
							} else if(licenceQueryResponse.licenceErrorCode == Values.LICENCE_ERROR_CODE_REFRESH_SUCCESS){
								ServerAppLogger.GetInstance().ShowMessage("Licence refresh sent success");
							} else if(licenceQueryResponse.licenceErrorCode == Values.LICENCE_ERROR_CODE_REFRESH_FAILED){
								ServerAppLogger.GetInstance().ShowMessage("Licence refresh failed");
							}
							ServerAppLogger.GetInstance().ShowMessage("");
						} else if(inputObject instanceof LocalServerServerAppPingData){
							OnLocalServerPing((LocalServerServerAppPingData)inputObject);
						} else if(inputObject instanceof DatabaseQueryResponse){
							DatabaseQueryResponse element = (DatabaseQueryResponse) inputObject;
							serverDiffsResponseList.AddResponse(element);
						} else if(inputObject instanceof DatabaseDiffQuery){
							DatabaseDiffQuery element = (DatabaseDiffQuery) inputObject;
							DatabaseDiffResponse databaseDiffResponse = new DatabaseDiffResponse(element);
							if(isMasterLocalServer){
								try {
									databaseDiffResponse = ExecuteDatabaseDiffQuery(GetDiffDatabaseConnection(), element);
								} catch (Exception ex) {
									ServerAppLogger.GetInstance().ShowErrorLog(ex);
									databaseDiffResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								}
							} else {
								databaseDiffResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
							}
							synchronized (oosLock){
								oos.writeObject(databaseDiffResponse);
								oos.flush();
							}

							// Print log
							String clientInfo = "c " + companyId + ", cn " + companyName + ", max diff id " + databaseDiffResponse.maxDiffId + ", requested diff id " + element.lastDiffId + ", synced diff id " + GetLastDiffId();
							if(databaseDiffResponse.diffList == null || databaseDiffResponse.errorCode == Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED){
								if(companyName != null){
									ServerAppLogger.GetInstance().ShowMessage("Database diff query failed: " + clientInfo);
								} else {
									ServerAppLogger.GetInstance().ShowMessage("Database diff query failed! Server ping not recieved yet: " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
								}
							} else {
								ServerAppLogger.GetInstance().ShowMessage("Database diff query executed: " + clientInfo);
							}
							ServerAppLogger.GetInstance().ShowMessage("");
						} else {
							ServerAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().LogError(ex);
						if (clientSocket != null){
							ServerAppLogger.GetInstance().ShowMessage("Disconnected Client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						}
						ServerApp.GetInstance().RemoveConnectionCloseHostListener(thisHost);
						
						Utils.CloseSocket(clientSocket, oos, ois);
						clientSocket = null;
						oos = null;
						ois = null;
						
						return;
					}
				}
			}
		}).start();
	}
	
	private void SyncDiffs(){
		new Thread(new Runnable() {
			@Override
			public void run() {				
				while(true){
					/*try {
						ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs 1: " + "c " + debugCompanyId + "o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
					} catch (Exception ex){
						ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E1" + System.lineSeparator());
					}*/
					
					if(isMasterLocalServer){
						/*try {
							ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs 2: " + "c " + debugCompanyId + "o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						} catch (Exception ex){
							ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E2" + System.lineSeparator());
						}*/
						
						int lastDiffId = GetLastDiffId();
						if(lastDiffId == 0){
							CreateDiffTableIfNoExist();
						}
						
						String query = "SELECT ID, DIFF FROM DIFF_TABLE WHERE ID BETWEEN ? AND ? ORDER BY ID";
						DatabaseQuery databaseDiffQuery = new DatabaseQuery(query);
						databaseDiffQuery.AddParam(1, lastDiffId + 1);
						databaseDiffQuery.AddParam(2, lastDiffId + 1 + Values.LOCAL_SERVER_DIFF_SYNC_MAX_ROWS);
						databaseDiffQuery.clientId = companyId;
						try {
							synchronized (oosLock){
								oos.writeObject(databaseDiffQuery);
								oos.flush();
							}
							
							ServerResponse serverResponseFound = null;
							int timeout = 1000 * 20;
							int delay = 50;
							int counter = 0;
							while(counter < timeout){
								counter += delay;
								Thread.sleep(delay);

								serverResponseFound = serverDiffsResponseList.GetResponseByQueryIdAndClientId(databaseDiffQuery.queryId, companyId);
								if(serverResponseFound != null){
									serverDiffsResponseList.RemoveResponse(serverResponseFound);
									break;
								}
							}
							
							/*try {
								ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs 3: " + "c " + debugCompanyId + "o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
							} catch (Exception ex){
								ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E3" + System.lineSeparator());
							}*/
							
							DatabaseQueryResult databaseQueryResult = null;
							if(serverResponseFound != null && serverResponseFound.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
								/*try {
									ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs 4: " + "c " + debugCompanyId + "o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
								} catch (Exception ex){
									ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E4" + System.lineSeparator());
								}*/
								
								databaseQueryResult = ((DatabaseQueryResponse) serverResponseFound).databaseQueryResult;
								if(databaseQueryResult != null){
									/*try {
										ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs 5: " + "c " + debugCompanyId + "o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
									} catch (Exception ex){
										ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E5" + System.lineSeparator());
									}*/
									
									if(databaseQueryResult.getSize() != 0){
										ServerAppLogger.GetInstance().ShowMessage("Syncing database " + companyName + " ... Total rows: " + databaseQueryResult.getSize() + System.lineSeparator());
                                                                                
									}
									while (databaseQueryResult.next()) {
										String insertQuery = "INSERT INTO DIFF_TABLE (ID, DIFF) VALUES (?, ?) ";
										PreparedStatement ps = GetDiffDatabaseConnection().prepareStatement(insertQuery);
										ps.setInt(1, databaseQueryResult.getInt(0));
										ps.setBytes(2, databaseQueryResult.getBytes(1));
										ps.executeUpdate();
										//ServerAppLogger.GetInstance().ShowMessage("Diff: " + new String(databaseQueryResult.getBytes(1)));
									}
								}
							} else {
								try {
									ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E6: " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
								} catch (Exception ex){
									ServerAppLogger.GetInstance().LogMessageDebug("SyncDiffs E6" + System.lineSeparator());
								}
							}
						} catch (Exception ex) {
							ServerAppLogger.GetInstance().LogError(ex);
							if (clientSocket != null){
								ServerAppLogger.GetInstance().ShowMessage("Syncing error: " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
							}
							Utils.CloseSocket(clientSocket, oos, ois);
							clientSocket = null;
							oos = null;
							ois = null;
							return;
						}
					}
					
					try {
						Thread.sleep(1000 * DIFF_SYNC_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
					
					if(!Utils.IsSocketValid(clientSocket)){
						return;
					}
				}
			}
		}).start();
	}
	
	private LicenceQueryResponse ActivateLicence(LicenceQuery licenceQuery) throws Exception {
		LicenceQueryResponse databaseQueryResponse = new LicenceQueryResponse(licenceQuery);
		
		String[] lines = licenceQuery.queryMessage.split(Values.LICENCE_SPLIT_STRING);
		String activationKey = lines[0];
		String uniqueId = lines[1];
		
		int companyId = -1;
		licenceId = -1;
		int licenceType = -1;
		int cashRegisterNumber = -1;
		String licenceUniqueId = "";
		String licenceDate = "";
		String licenceDBName = "";
		String oib = "";
		int officeNumber = -1;
		String officeTag = "";
		String name = "";
		String companyAddress = "";
		String officeAddress = "";
		
		synchronized(ServerApp.databaseTransactionLock){
			String query = "SELECT LICENCES.ID, LICENCES.TYPE, LICENCES.COMPUTER_ID, LICENCES.EXPIRATION_DATE, COMPANIES.DBNAME, "
					+ "COMPANIES.OIB, OFFICES.OFFICE_NUMBER, OFFICES.OFFICE_TAG, COMPANIES.NAME, COMPANIES.ADDRESS, OFFICES.ADDRESS, LICENCES.CASH_REGISTER_NUMBER, "
					+ "COMPANIES.ID, COMPANIES.AUTO_RENEW "
					+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
					+ "WHERE LICENCES.ACTIVATION_KEY = ? "
					+ "FETCH FIRST ROW ONLY";
			PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
			ps.setString(1, activationKey);
			ps.setMaxRows(1);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				licenceId = result.getInt(1);
				licenceType = result.getInt(2);
				licenceUniqueId = result.getString(3);
				licenceDate = result.getString(4);
				licenceDBName = result.getString(5);
				oib = result.getString(6);
				officeNumber = result.getInt(7);
				officeTag = result.getString(8);
				name = result.getString(9);
				companyAddress = result.getString(10);
				officeAddress = result.getString(11);
				cashRegisterNumber = result.getInt(12);
				companyId = result.getInt(13);
				if(result.getInt(14) == 1){
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, 30);
					licenceDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
				}
			} else {
				databaseQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_WRONG_CODE;
				return databaseQueryResponse;
			}

			if(!"".equals(licenceUniqueId) && !"null".equals(licenceUniqueId) && licenceUniqueId != null){
				databaseQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_ALREADY_ACTIVE;
				return databaseQueryResponse;
			}

			// Generate keys
			KeyGeneratorRSA keyGeneratorRSA = new KeyGeneratorRSA(2048);
			keyGeneratorRSA.createKeys();

			// Generate licence
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, keyGeneratorRSA.getPrivateKey());
			String licenceString = ServerApp.GenerateLicenceString(uniqueId, licenceDate, licenceType, companyId, cashRegisterNumber, licenceDBName, oib, officeNumber, officeTag, name, companyAddress, officeAddress, 0);
			byte[] licenceBytes = cipher.doFinal(licenceString.getBytes());

			// Save to database
			String insertQuery = "UPDATE LICENCES SET COMPUTER_ID = ?, PRIVATE_KEY = ?, PUBLIC_KEY = ?, LICENSE_ENCRYPTED = ? WHERE ID = ?";
			PreparedStatement insertPreparedStatement = getDatabaseConnection().prepareStatement(insertQuery);
			insertPreparedStatement.setString(1, uniqueId);
			insertPreparedStatement.setBytes(2, keyGeneratorRSA.getPrivateKey().getEncoded());
			insertPreparedStatement.setBytes(3, keyGeneratorRSA.getPublicKey().getEncoded());
			insertPreparedStatement.setBytes(4, licenceBytes);
			insertPreparedStatement.setInt(5, licenceId);
			insertPreparedStatement.executeUpdate();

			// Response data
			databaseQueryResponse.publicKeyBytes = keyGeneratorRSA.getPublicKey().getEncoded();
			databaseQueryResponse.licenceBytes = licenceBytes;
			
			// Certificates
			String queryCertificates = "SELECT ALIAS, CERT FROM CERTIFICATES";
			PreparedStatement psCertificates = getDatabaseConnection().prepareStatement(queryCertificates);
			ResultSet resultCertificates = psCertificates.executeQuery();
			while (resultCertificates.next()) {
				if(Values.CERT_DEMO_ROOT_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certDemoRootBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_DEMO_SUB_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certDemoSubBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_PROD_ROOT_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certProdRootBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_PROD_SUB_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certProdSubBytes = resultCertificates.getBytes(2);
				}
			}
		}
		
		databaseQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_ACTIVATION_SUCCESS;
		return databaseQueryResponse;
	}
	
	private LicenceQueryResponse RefreshLicence(LicenceQuery licenceQuery) throws Exception {
		LicenceQueryResponse databaseQueryResponse = new LicenceQueryResponse(licenceQuery);
		
		String[] lines = licenceQuery.queryMessage.split(Values.LICENCE_SPLIT_STRING);
		String activationKey = lines[0];
		String uniqueId = lines[1];
		
		int companyId = -1;
		licenceId = -1;
		int licenceType = -1;
		int cashRegisterNumber = -1;
		String licenceDate = "";
		byte[] privateKeyBytes = null;
		String licenceDBName = "";
		String oib = "";
		int officeNumber = -1;
		String officeTag = "";
		String name = "";
		String companyAddress = "";
		String officeAddress = "";
		
		synchronized(ServerApp.databaseTransactionLock){
			String query = "SELECT LICENCES.ID, LICENCES.TYPE, LICENCES.EXPIRATION_DATE, LICENCES.PRIVATE_KEY, COMPANIES.DBNAME, "
					+ "COMPANIES.OIB, OFFICES.OFFICE_NUMBER, OFFICES.OFFICE_TAG, COMPANIES.NAME, COMPANIES.ADDRESS, OFFICES.ADDRESS, LICENCES.CASH_REGISTER_NUMBER, "
					+ "COMPANIES.ID, COMPANIES.AUTO_RENEW "
					+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
					+ "WHERE LICENCES.ACTIVATION_KEY = ? AND LICENCES.COMPUTER_ID = ? "
					+ "FETCH FIRST ROW ONLY";
			PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
			ps.setString(1, activationKey);
			ps.setString(2, uniqueId);
			ps.setMaxRows(1);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				licenceId = result.getInt(1);
				licenceType = result.getInt(2);
				licenceDate = result.getString(3);
				privateKeyBytes = result.getBytes(4);
				licenceDBName = result.getString(5);
				oib = result.getString(6);
				officeNumber = result.getInt(7);
				officeTag = result.getString(8);
				name = result.getString(9);
				companyAddress = result.getString(10);
				officeAddress = result.getString(11);
				cashRegisterNumber = result.getInt(12);
				companyId = result.getInt(13);
				if(result.getInt(14) == 1){
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, 30);
					licenceDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
				}
				ServerAppLogger.GetInstance().ShowMessage("Licence refresh recieved from: company id " + companyId + ", dbName " + licenceDBName  + ", office number " + officeNumber + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
				debugCompanyId = companyId;
				debugOfficeId = officeNumber;
				debugDBName = licenceDBName;
			} else {
				databaseQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_REFRESH_FAILED;
				return databaseQueryResponse;
			}

			// Generate keys
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(spec);

			// Generate licence
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			String licenceString = ServerApp.GenerateLicenceString(uniqueId, licenceDate, licenceType, companyId, cashRegisterNumber, licenceDBName, oib, officeNumber, officeTag, name, companyAddress, officeAddress, 0);
			byte[] licenceBytes = cipher.doFinal(licenceString.getBytes());

			// Save to database
			String insertQuery = "UPDATE LICENCES SET LICENSE_ENCRYPTED = ? WHERE ID = ?";
			PreparedStatement insertPreparedStatement = getDatabaseConnection().prepareStatement(insertQuery);
			insertPreparedStatement.setBytes(1, licenceBytes);
			insertPreparedStatement.setInt(2, licenceId);
			insertPreparedStatement.executeUpdate();

			// Response data
			databaseQueryResponse.licenceBytes = licenceBytes;
			
			// Certificates
			String queryCertificates = "SELECT ALIAS, CERT FROM CERTIFICATES";
			PreparedStatement psCertificates = getDatabaseConnection().prepareStatement(queryCertificates);
			ResultSet resultCertificates = psCertificates.executeQuery();
			while (resultCertificates.next()) {
				if(Values.CERT_DEMO_ROOT_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certDemoRootBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_DEMO_SUB_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certDemoSubBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_PROD_ROOT_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certProdRootBytes = resultCertificates.getBytes(2);
				} else if(Values.CERT_PROD_SUB_ALIAS.equals(resultCertificates.getString(1))){
					databaseQueryResponse.certProdSubBytes = resultCertificates.getBytes(2);
				}
			}
		}
		
		databaseQueryResponse.licenceErrorCode = Values.LICENCE_ERROR_CODE_REFRESH_SUCCESS;
		return databaseQueryResponse;
	}
	
	private void OnLocalServerPing(LocalServerServerAppPingData clientAppServerAppPingData) {
		ServerAppLogger.GetInstance().ShowMessage("Ping recieved from: c " + clientAppServerAppPingData.companyId + ", o " + clientAppServerAppPingData.officeNumber + ", have unfiscalized invoices " + clientAppServerAppPingData.haveUnfiscalizedInvoices + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
		
		companyId = clientAppServerAppPingData.companyId;
		
		if (clientAppServerAppPingData.isControlApp){
			isLocalServer = true;
			return;
		}
		
		synchronized(ServerApp.databaseTransactionLock){
			if(!clientAppServerAppPingData.haveUnfiscalizedInvoices){
				try {
					String query = "UPDATE OFFICES SET LAST_PING_DATE = ?, LAST_PING_TIME = ? WHERE USER_ID = ? AND OFFICE_NUMBER = ?";
					PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
					Date date = new Date();
					ps.setString(1, new SimpleDateFormat("yyyy-MM-dd").format(date));
					ps.setString(2, new SimpleDateFormat("HH:mm:ss").format(date));
					ps.setInt(3, clientAppServerAppPingData.companyId);
					ps.setInt(4, clientAppServerAppPingData.officeNumber);
					ps.setMaxRows(1);
					ps.executeUpdate();
				} catch (Exception ex){
					ServerAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
			
			try {
				String query = "SELECT COMPANIES.NAME FROM LICENCES "
						+ "INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID "
						+ "INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID "
						+ "WHERE OFFICES.USER_ID = ? AND OFFICES.OFFICE_NUMBER = ? AND LICENCES.TYPE = ?";
				PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
				ps.setInt(1, clientAppServerAppPingData.companyId);
				ps.setInt(2, clientAppServerAppPingData.officeNumber);
				ps.setInt(3, Values.LICENCE_TYPE_MASTER_LOCAL_SERVER);
				ps.setMaxRows(1);
				ResultSet result = ps.executeQuery();
				if (result.next()) {
					isMasterLocalServer = true;
					companyName = result.getString(1);
					SaveMasterIp();
				} else {
					isLocalServer = true;
				}
			} catch (Exception ex){
				ServerAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void SaveMasterIp() {
		synchronized(ServerApp.databaseTransactionLock){
			String ipAddress = clientSocket.getInetAddress().getHostAddress();
			try {
				String query = "UPDATE COMPANIES SET MASTER_IP = ? WHERE ID = ?";
				PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
				ps.setString(1, ipAddress);
				ps.setInt(2, companyId);
				ps.setMaxRows(1);
				ps.executeUpdate();
			} catch (Exception ex){
				ServerAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	private String GetMasterIp() {
		synchronized(ServerApp.databaseTransactionLock){
			try {
				String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
				PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
				ps.setInt(1, companyId);
				ps.setMaxRows(1);
				ResultSet result = ps.executeQuery();
				if (result.next()) {
					return result.getString(1);
				}
			} catch (Exception ex){
				ServerAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return "";
	}
       
	
	private DatabaseDiffResponse ExecuteDatabaseDiffQuery(Connection connection, DatabaseDiffQuery databaseDiffQuery) throws Exception {
		CreateDiffTableIfNoExist();
		
		String query = "SELECT DIFF FROM DIFF_TABLE WHERE ID BETWEEN ? AND ? ORDER BY ID";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, databaseDiffQuery.lastDiffId + 1);
		preparedStatement.setInt(2, databaseDiffQuery.lastDiffId + 1 + Values.LOCAL_SERVER_DIFF_SYNC_MAX_ROWS);
		preparedStatement.setQueryTimeout(databaseDiffQuery.timeoutSeconds);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		DatabaseDiffResponse databaseDiffResponse = new	DatabaseDiffResponse(databaseDiffQuery);
		while (resultSet != null && resultSet.next()) {
			byte[] bytes = resultSet.getBytes(1);
			DatabaseDiff databaseDiff = (DatabaseDiff) Utils.DeserializeObject(bytes);
			databaseDiffResponse.diffList.add(databaseDiff);
		}
		
		int lastDiffId = databaseDiffQuery.lastDiffId;
		if(databaseDiffResponse.diffList.size() > Values.LOCAL_SERVER_DIFF_SYNC_MAX_DIALOG_ROWS){
			String queryLastDiffId = "SELECT MAX(ID) FROM DIFF_TABLE";
			PreparedStatement psLastDiffId = connection.prepareStatement(queryLastDiffId);
			psLastDiffId.setMaxRows(1);
			ResultSet resultLastDiffId = psLastDiffId.executeQuery();
			if (resultLastDiffId.next()) {
				lastDiffId = resultLastDiffId.getInt(1);
			}
		}
		
		databaseDiffResponse.maxDiffId = lastDiffId;
		
		return databaseDiffResponse;
	}
	
	private int GetLastDiffId() {
		try {
			String query = "SELECT COALESCE(MAX(ID), 0) FROM DIFF_TABLE";
			PreparedStatement ps = GetDiffDatabaseConnection().prepareStatement(query);
			ps.setMaxRows(1);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (Exception ex){
			//ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return 0;
	}
	
	private void CreateDiffTableIfNoExist() {
		if(diffTableExist)
			return;
		
		synchronized(ServerApp.databaseTransactionLock){
			String tableName = "DIFF_TABLE";
			String tableQuery = "CREATE TABLE DIFF_TABLE(ID INT, DIFF VARCHAR (32672) FOR BIT DATA, PRIMARY KEY (ID))";
			try {
				DatabaseMetaData dbm = GetDiffDatabaseConnection().getMetaData();
				ResultSet rs = dbm.getTables(null, null, tableName, null);
				if(!rs.next()){
					Statement statement = GetDiffDatabaseConnection().createStatement();
					statement.execute(tableQuery);
					statement.close();
				}
				UpdateDiffTableType();
				diffTableExist = true;
			} catch (Exception ex){
				ServerAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void UpdateDiffTableType() throws Exception {
		DatabaseMetaData dbm = GetDiffDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getColumns(null, null, "DIFF_TABLE", "DIFF");
		if(rs.next()){
			if(!"32672".equals(rs.getString(7))){
				Statement statement = GetDiffDatabaseConnection().createStatement();
				statement.execute("ALTER TABLE DIFF_TABLE ALTER COLUMN DIFF SET DATA TYPE VARCHAR (32672) FOR BIT DATA");
				statement.close();
				ServerAppLogger.GetInstance().ShowMessage("Diff table altered - old size " + rs.getString(7) + ", new size 32672, cn " + companyName + System.lineSeparator());
			}
		}
	}
	
	private Connection GetDiffDatabaseConnection() throws UnknownHostException, SQLException {
		if(syncDatabaseConnection != null && (syncDatabaseConnection.isClosed() || !syncDatabaseConnection.isValid(1))){
			syncDatabaseConnection = null;
		}
		
		if(syncDatabaseConnection == null){
			String dbName = companyName.replaceAll("[^a-zA-Z0-9]", "");
            syncDatabaseConnection = Utils.getDatabaseConnection(Values.SERVER_APP_LOCALHOST, Values.SERVER_APP_DATABASE_SERVER_PORT, "backup/" + companyId + "-" + dbName);
		}
		
		return syncDatabaseConnection;
	}
	
	private Connection getDatabaseConnection() throws UnknownHostException, SQLException {
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null){
            databaseConnection = Utils.getDatabaseConnection(Values.SERVER_APP_LOCALHOST, Values.SERVER_APP_DATABASE_SERVER_PORT, Values.SERVER_APP_DATABASE_NAME);
		}
		
		return databaseConnection;
	}
	
	public static void OnAppClose(){
		try {
			if(databaseConnection != null){
				databaseConnection.close();
				databaseConnection = null;
			}
		} catch (SQLException ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
