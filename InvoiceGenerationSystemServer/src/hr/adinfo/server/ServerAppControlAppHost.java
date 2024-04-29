/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.Utils;
import static hr.adinfo.utils.Values.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import hr.adinfo.utils.Pair;
import static hr.adinfo.utils.Utils.ExecuteDatabaseQuery;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.KeyGeneratorRSA;
import hr.adinfo.utils.licence.LicenceQuery;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.crypto.Cipher;

/**
 *
 * @author Matej
 */
public class ServerAppControlAppHost {
	private static Connection databaseConnection;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Object oosLock = new Object();
		
	ServerAppControlAppHost(Socket clientSocket) {
		ServerAppLogger.GetInstance().ShowMessage("Accepting connection from Control: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
		this.clientSocket = clientSocket;
		
		try {
			CreateStreams();
			ReadQueries();
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	private void CreateStreams() throws IOException{
		oos = new ObjectOutputStream(clientSocket.getOutputStream());
		oos.flush();
		ois = new ObjectInputStream(clientSocket.getInputStream());
	}
	
	private void ReadQueries(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							// TODO connection ended
						} else if(inputObject instanceof DatabaseQuery){
							DatabaseQuery element = (DatabaseQuery) inputObject;
							DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse(element);
							try {
								databaseQueryResponse.databaseQueryResult = ExecuteDatabaseQuery(getDatabaseConnection(), element, ServerApp.databaseTransactionLock);
							} catch (SQLException | UnknownHostException ex) {
								ServerAppLogger.GetInstance().ShowErrorLog(ex);
								databaseQueryResponse.errorCode = RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
							}
							
							synchronized (oosLock){
								oos.writeObject(databaseQueryResponse);
								oos.flush();
							}
							
							// Print log
							if(databaseQueryResponse.databaseQueryResult == null){
								ServerAppLogger.GetInstance().ShowMessage("Query failed: " + element.query);
							} else {
								ServerAppLogger.GetInstance().ShowMessage("Query executed: " + element.query);
							}
							ServerAppLogger.GetInstance().ShowMessage("Params: ");
							for (Pair<Integer, String> param : element.params) {
								ServerAppLogger.GetInstance().ShowMessage(param.getKey() + ": " + param.getValue());
							}
							for (Pair<Integer, byte[]> param : element.paramsBytes) {
								ServerAppLogger.GetInstance().ShowMessage(param.getKey() + ": " + String.valueOf(param.getValue()));
							}
							ServerAppLogger.GetInstance().ShowMessage("");
							
							if(element.query.contains("UPDATE LICENCES")){
								String licenceIdString = element.params.get(element.params.size() - 1).getValue();
								ServerAppLogger.GetInstance().ShowMessage("Licence with id " + licenceIdString + " changed. Restarting connection..." + System.lineSeparator());
								ServerApp.GetInstance().CloseConnection(licenceIdString);
							}
						} else if(inputObject instanceof MultiDatabaseQuery){
							MultiDatabaseQuery element = (MultiDatabaseQuery) inputObject;
							MultiDatabaseQueryResponse databaseQueryResponse = new MultiDatabaseQueryResponse(element);
							try {
								databaseQueryResponse.databaseQueryResult = Utils.ExecuteMultiDatabaseQuery(getDatabaseConnection(), element, ServerApp.databaseTransactionLock);
							} catch (SQLException | UnknownHostException ex) {
								ServerAppLogger.GetInstance().ShowErrorLog(ex);
								databaseQueryResponse.errorCode = RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
							}
							
							synchronized (oosLock){
								oos.writeObject(databaseQueryResponse);
								oos.flush();
							}
							
							// Print log
							if(databaseQueryResponse.databaseQueryResult == null){
								ServerAppLogger.GetInstance().ShowMessage("Query failed: ");
							} else {
								ServerAppLogger.GetInstance().ShowMessage("Query executed: ");
							}
							for (int i = 0; i < element.querySize; ++i) {
								ServerAppLogger.GetInstance().ShowMessage("Query: " + element.query[i]);
								ServerAppLogger.GetInstance().ShowMessage("Params: ");
								for (Pair<Integer, String> param : element.params[i]) {
									ServerAppLogger.GetInstance().ShowMessage(param.getKey() + ": " + param.getValue());
								}
								for (Pair<Integer, byte[]> param : element.paramsBytes[i]) {
									ServerAppLogger.GetInstance().ShowMessage(param.getKey() + ": " + String.valueOf(param.getValue()));
								}
								ServerAppLogger.GetInstance().ShowMessage("");
							}
						} else if(inputObject instanceof LicenceQuery){
							LicenceQuery element = (LicenceQuery) inputObject;
							LicenceQueryResponse licenceQueryResponse = new LicenceQueryResponse(element);
							try {
								if(element.queryType == LICENCE_QUERY_ACTIVATE){
									licenceQueryResponse = CreateControlAppAdminLicence(element);
								}
							} catch (Exception ex) {
								ServerAppLogger.GetInstance().ShowErrorLog(ex);
								licenceQueryResponse.errorCode = RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								if(element.queryType == LICENCE_QUERY_ACTIVATE){
									licenceQueryResponse.licenceErrorCode = LICENCE_ERROR_CODE_ACTIVATION_FAILED;
								} else {
									licenceQueryResponse.licenceErrorCode = LICENCE_ERROR_CODE_REFRESH_FAILED;
								}
							}
							
							synchronized (oosLock){
								oos.writeObject(licenceQueryResponse);
								oos.flush();
							}
							
							// Print log
							if(licenceQueryResponse.licenceErrorCode == LICENCE_ERROR_CODE_WRONG_CODE){
								ServerAppLogger.GetInstance().ShowMessage("Control app licence activation failed (wrong code)");
							} else if(licenceQueryResponse.licenceErrorCode == LICENCE_ERROR_CODE_ALREADY_ACTIVE){
								ServerAppLogger.GetInstance().ShowMessage("Control app licence activation failed (already active)");
							} else if(licenceQueryResponse.licenceErrorCode == LICENCE_ERROR_CODE_ACTIVATION_SUCCESS){
								ServerAppLogger.GetInstance().ShowMessage("Control app licence activation success");
							}
							ServerAppLogger.GetInstance().ShowMessage("");
						} else {
							ServerAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().LogError(ex);
						ServerAppLogger.GetInstance().ShowMessage("Disconnected Control: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						
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
	
	private LicenceQueryResponse CreateControlAppAdminLicence(LicenceQuery licenceQuery) throws Exception {
		LicenceQueryResponse databaseQueryResponse = new LicenceQueryResponse(licenceQuery);
		
		String[] lines = licenceQuery.queryMessage.split(LICENCE_SPLIT_STRING);
		String officeId = lines[0];
		String uniqueId = lines[1];
		
		int companyId = -1;
		String licenceDBName = "";
		int licenceType = LICENCE_TYPE_LOCAL_SERVER;
		int officeNumber = -1;
		String officeTag = "";
		String oib = "";
		String name = "";
		String companyAddress = "";
		String officeAddress = "";
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String licenceDate = dateFormat.format(calendar.getTime());
		
		String query = "SELECT COMPANIES.DBNAME, OFFICES.OFFICE_NUMBER, OFFICES.OFFICE_TAG, COMPANIES.OIB, COMPANIES.NAME, COMPANIES.ADDRESS, OFFICES.ADDRESS, "
				+ "COMPANIES.ID "
				+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "WHERE OFFICES.ID = ? "
				+ "FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setString(1, officeId);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (result.next()) {
			licenceDBName = result.getString(1);
			officeNumber = result.getInt(2);
			officeTag = result.getString(3);
			oib = result.getString(4);
			name = result.getString(5);
			companyAddress = result.getString(6);
			officeAddress = result.getString(7);
			companyId = result.getInt(8);
		} else {
			databaseQueryResponse.licenceErrorCode = LICENCE_ERROR_CODE_WRONG_CODE;
			return databaseQueryResponse;
		}

		// Generate keys
		KeyGeneratorRSA keyGeneratorRSA = new KeyGeneratorRSA(2048);
		keyGeneratorRSA.createKeys();

		// Generate licence
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, keyGeneratorRSA.getPrivateKey());
		String licenceString = ServerApp.GenerateLicenceString(uniqueId, licenceDate, licenceType, companyId, 0, licenceDBName, oib, officeNumber, officeTag, name, companyAddress, officeAddress, 1);
		byte[] licenceBytes = cipher.doFinal(licenceString.getBytes());

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
		
		databaseQueryResponse.licenceErrorCode = LICENCE_ERROR_CODE_ACTIVATION_SUCCESS;
		return databaseQueryResponse;
	}
	
	private Connection getDatabaseConnection() throws UnknownHostException, SQLException{
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null){
            databaseConnection = Utils.getDatabaseConnection(SERVER_APP_LOCALHOST, SERVER_APP_DATABASE_SERVER_PORT, SERVER_APP_DATABASE_NAME);
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
