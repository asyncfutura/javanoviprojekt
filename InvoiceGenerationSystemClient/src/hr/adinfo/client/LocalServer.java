/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.services.LocalServerMasterLocalServerSync;
import hr.adinfo.utils.Pair;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.database.DatabaseDiff;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.derby.drda.NetworkServerControl;

/**
 *
 * @author Matej
 */
public class LocalServer {
	private static LocalServer localServer = null;
	private ServerSocket serverSocket = null;
	private DatagramSocket udpServerSocket = null;
	private NetworkServerControl derbyServer;
	private Connection databaseConnection;
	private ServerResponseList serverResponseList = new ServerResponseList();
	
	public boolean isSyncedWithMaster;
	
	private LocalServer() throws Exception {
		// Start derby server
		InetAddress hostInetAddress = InetAddress.getByName(Values.CLIENT_APP_DATABASE_SERVER_HOST);
		derbyServer = new NetworkServerControl(hostInetAddress, Values.CLIENT_APP_DATABASE_SERVER_PORT);
		derbyServer.start(null);
		int pingCounter = 0;
		while (true) {
			Thread.sleep(500);
			try {
				derbyServer.ping();
				break;
			} catch (Exception e) {
				if(++pingCounter > 10){
					throw e;
				}
			}
		}
		
		// Setup database
		LocalServerDatabaseSetup.SetupDatabase(getDatabaseConnection());
		
		// Setup server
		serverSocket = new ServerSocket(Values.CLIENT_APP_LOCAL_SERVER_LOCALHOST_PORT);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverSocket.accept();
						new LocalServerHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing){
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
						}
					}
				}
			}
		}).start();
		
		// Setup UDP server
		udpServerSocket = new DatagramSocket(Values.CLIENT_APP_LOCAL_SERVER_UDP_PORT);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						byte[] receiveData = new byte[1024];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						udpServerSocket.receive(receivePacket);
						String receivedString = new String(receivePacket.getData()).trim();
						ClientAppLogger.GetInstance().LogMessage("Local Server: UDP message recieved: " + receivedString + "; from " + receivePacket.getAddress() + ":" + receivePacket.getPort());
						if("client app to local server".equals(receivedString)){
							InetAddress IPAddress = receivePacket.getAddress();
							int port = receivePacket.getPort();
							String response = "local server to client app" + Licence.GetOfficeNumber() + Licence.GetDBName();
							byte[] sendData = response.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
							udpServerSocket.send(sendPacket);
						}
					} catch (IOException ex) {
						if(!ClientApp.appClosing){
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
						}
					}
				}
			}
		}).start();
		
		LocalServerMasterLocalServerClient.Init();
		
		if(!Licence.IsMasterLocalServer()){
			LocalServerMasterLocalServerSync.Init();
		} else {
			isSyncedWithMaster = true;
		}
	}
	
	public void RegisterCashRegister(int officeNumber, int cashRegisterNumber){
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = false;
				boolean doSleep = false;
				while(!success){
					if(ClientApp.appClosing)
						break;
					
					if(doSleep){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException ex) {}
					}
					doSleep = true;
					
					DatabaseQuery databaseQuery = new DatabaseQuery("SELECT OFFICE_NUMBER, CR_NUMBER FROM CASH_REGISTERS WHERE OFFICE_NUMBER = ? AND CR_NUMBER = ?");
					databaseQuery.clientId = Values.FAKE_QUERY_ID_REGISTER_CASH_REGISTER;
					databaseQuery.AddParam(1, officeNumber);
					databaseQuery.AddParam(2, cashRegisterNumber);
					try {
						LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(databaseQuery);
					} catch (IOException ex) {
						if("E201".equals(ex.getMessage())){
							ClientAppLogger.GetInstance().LogMessage("E201");
						} else if("E101".equals(ex.getMessage())){
							ClientAppLogger.GetInstance().LogMessage("E101");
						} else {
							ClientAppLogger.GetInstance().LogError(ex);
						}
						continue;
					}

					boolean doInsert = false;
					int counter = 0;
					final int timeoutDelayMiliseconds = 100;
					final int timeoutSeconds = 10;
					while(counter < 1000 * timeoutSeconds){
						if(ClientApp.appClosing)
							break;

						ServerResponse serverResponseFound;
						if((serverResponseFound = LocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(databaseQuery.queryId, Values.FAKE_QUERY_ID_REGISTER_CASH_REGISTER)) != null){
							LocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
							DatabaseQueryResponse databaseQueryResponse = (DatabaseQueryResponse) serverResponseFound;
							if(databaseQueryResponse.databaseQueryResult.next()){
								success = true;
							} else {
								doInsert = true;
							}
							break;
						} else {
							try {
								Thread.sleep(timeoutDelayMiliseconds);
								counter += timeoutDelayMiliseconds;
							} catch (InterruptedException ex) {
								ClientAppLogger.GetInstance().ShowErrorLog(ex);
							}
						}
					}

					if(doInsert){
						DatabaseQuery databaseQueryInsert = new DatabaseQuery("INSERT INTO CASH_REGISTERS (OFFICE_NUMBER, CR_NUMBER, IS_DELETED) VALUES (?, ?, ?)");
						databaseQueryInsert.clientId = Values.FAKE_QUERY_ID_REGISTER_CASH_REGISTER;
						databaseQueryInsert.AddParam(1, officeNumber);
						databaseQueryInsert.AddParam(2, cashRegisterNumber);
						databaseQueryInsert.AddParam(3, 0);

						try {
							LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(databaseQueryInsert);
						} catch (IOException ex) {
							ClientAppLogger.GetInstance().LogError(ex);
							continue;
						}

						counter = 0;
						while(counter < 1000 * timeoutSeconds){
							if(ClientApp.appClosing)
								break;

							ServerResponse serverResponseFound;
							if((serverResponseFound = LocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(databaseQueryInsert.queryId, Values.FAKE_QUERY_ID_REGISTER_CASH_REGISTER)) != null){
								LocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
								success = true;
								break;
							} else {
								try {
									Thread.sleep(timeoutDelayMiliseconds);
									counter += timeoutDelayMiliseconds;
								} catch (InterruptedException ex) {
									ClientAppLogger.GetInstance().ShowErrorLog(ex);
								}
							}
						}
					}
				}
			}
		}).start();
	}
	
	public static ServerResponseList GetServerResponseList(){
		return localServer.serverResponseList;
	}
	
	public int GetLastDiffId() throws SQLException, UnknownHostException {
		int lastDiffId = -1;
		String query = "SELECT VALUE FROM LOCAL_VALUES_TABLE WHERE NAME = 'lastDiffId' FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (result.next()) {
			lastDiffId = result.getInt(1);
		}
		
		return lastDiffId;
	}
	
	public void InsertDatabaseDiff(DatabaseDiff databaseDiff) throws Exception {
		try {
			Utils.LocalServerInsertDatabaseDiff(getDatabaseConnection(), databaseDiff, ClientApp.databaseTransactionLock);
		} catch (Exception ex){
			String paramsString = "";
			for (Pair<Integer, String> param : databaseDiff.params) {
				paramsString += param.getKey().toString() + " " + param.getValue() + ", ";
			}
			ClientAppLogger.GetInstance().LogMessage("LocalServer InsertDatabaseDiff failed! DiffId: " + databaseDiff.diffId + ", Query: " + databaseDiff.query + ", Params: " + paramsString);
			throw ex;
		}
	}
	
	private Connection getDatabaseConnection() throws UnknownHostException, SQLException{
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null && Licence.GetDBName() != null){
			databaseConnection = Utils.getDatabaseConnection(InetAddress.getLocalHost().getHostAddress(), Values.CLIENT_APP_DATABASE_SERVER_PORT, Licence.GetDBName());
		}
		
		return databaseConnection;
	}
	
	public static void Init() throws Exception{
		if(localServer == null){
			localServer = new LocalServer();
		}
	}
	
	public static LocalServer GetInstance(){
		return localServer;
	}
	
	public void OnAppClose(){
		try {
			if(derbyServer != null){
				derbyServer.shutdown();
				derbyServer = null;
			}
			if(databaseConnection != null){
				databaseConnection.close();
				databaseConnection = null;
			}
			if(serverSocket != null){
				serverSocket.close();
				serverSocket = null;
			}
			if(udpServerSocket != null){
				udpServerSocket.close();
				udpServerSocket = null;
			}
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
