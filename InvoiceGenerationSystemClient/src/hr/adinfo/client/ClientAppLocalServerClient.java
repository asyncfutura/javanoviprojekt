/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.datastructures.ClientAppLocalServerRegisterData;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ExecuteQueryInterface;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class ClientAppLocalServerClient implements ExecuteQueryInterface{
	private static ClientAppLocalServerClient localServerClient = null;
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Connection databaseConnection;
	private ServerResponseList serverResponseList = new ServerResponseList();
	
	private final Object socketLock = new Object();
	private Date lastSocketConnectTime;
	
	private ClientAppLocalServerClient(){
		ConnectSocket();
		ReadResponses();
	}
	
	private void ConnectSocket() {
		synchronized(socketLock){
			if(Utils.IsSocketValid(clientSocket) && oos != null && ois != null)
				return;

			Utils.CloseSocket(clientSocket, oos, ois);
			clientSocket = null;
			oos = null;
			ois = null;

			try {
				String host = GetLocalServerAddress();
				clientSocket = new Socket(host, Values.CLIENT_APP_LOCAL_SERVER_LOCALHOST_PORT);
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(clientSocket.getInputStream());

				ClientAppLocalServerRegisterData data = new ClientAppLocalServerRegisterData(Licence.GetOfficeNumber(), Licence.GetCashRegisterNumber());
				oos.writeObject(data);
				oos.flush();
				
				lastSocketConnectTime = new Date();
				
				ClientAppLogger.GetInstance().LogMessage("ClientAppLocalServerClient connected");
			} catch (IOException ex) {
				Utils.CloseSocket(clientSocket, oos, ois);
				clientSocket = null;
				oos = null;
				ois = null;
				if(ex.getMessage() != null && ex.getMessage().contains("Connection refused: connect")){
					ClientAppLogger.GetInstance().LogMessage("E808");
				} else {
					ClientAppLogger.GetInstance().LogError(ex);
				}
			}
		}
	}
	
	@Override
	public ServerResponse ExecuteQuery(ServerQuery serverQuery) throws Exception {
		ConnectSocket();
		
		// Select database query - run on LocalServer
		if(serverQuery instanceof DatabaseQuery && (IsSelectQuery((DatabaseQuery) serverQuery) || ((DatabaseQuery) serverQuery).executeLocally)){
			//ClientAppLogger.GetInstance().LogMessageDebug("ClientAppLocalServerClient ExecuteQuery DatabaseQuery " + serverQuery.queryId + " (" + serverQuery.clientId + ") [" + serverQuery.localServerId + "]");
			//ClientAppLogger.GetInstance().LogMessageDebug(((DatabaseQuery) serverQuery).query);
			DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse(serverQuery);
			try {
				databaseQueryResponse.databaseQueryResult = Utils.ExecuteDatabaseQuery(getDatabaseConnection(), (DatabaseQuery) serverQuery, ClientApp.databaseTransactionLock);
			} catch (SQLException | UnknownHostException ex) {
				//if(ClientApp.SHOW_CONNECTION_ERRORS){
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				//}
				
				databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
			}
			return databaseQueryResponse;
		}
		
		// Select multi database query - run on LocalServer
		if(serverQuery instanceof MultiDatabaseQuery && (IsSelectQuery((MultiDatabaseQuery) serverQuery) || ((MultiDatabaseQuery) serverQuery).executeLocally)){
			//ClientAppLogger.GetInstance().LogMessageDebug("ClientAppLocalServerClient ExecuteQuery MultiDatabaseQuery " + serverQuery.queryId + " (" + serverQuery.clientId + ") [" + serverQuery.localServerId + "]");
			//ClientAppLogger.GetInstance().LogMessageDebug(((MultiDatabaseQuery) serverQuery).query[0]);
			MultiDatabaseQueryResponse multiDatabaseQueryResponse = new MultiDatabaseQueryResponse(serverQuery);
			try {
				multiDatabaseQueryResponse.databaseQueryResult = Utils.ExecuteMultiDatabaseQuery(getDatabaseConnection(), (MultiDatabaseQuery) serverQuery, ClientApp.databaseTransactionLock);
			} catch (SQLException | UnknownHostException ex) {
				//if(ClientApp.SHOW_CONNECTION_ERRORS){
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					ClientAppLogger.GetInstance().LogMessageDebug("E901 " + ((MultiDatabaseQuery) serverQuery).query[0]);
				//}
				multiDatabaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
			}
			return multiDatabaseQueryResponse;
		}
		
		// Non-Select database query - run remotely on MasterLocalServer
		ClientAppLogger.GetInstance().LogMessageDebug("1 ClientAppLocalServerClient ExecuteQuery Remote " + serverQuery.queryId + " (" + serverQuery.clientId + ") [" + serverQuery.localServerId + "]");
		return Utils.ExecuteRemoteQuery(serverQuery, clientSocket, oos, serverResponseList);
	}
	
	private boolean IsSelectQuery(DatabaseQuery databaseQuery){
		if(databaseQuery.query.length() < 6)
			return false;
		
		if(!"SELECT".equals(databaseQuery.query.toUpperCase().substring(0, 6)))
			return false;
		
		if(databaseQuery.query.contains(";"))
			return false;
		
		return true;
	}
	
	private boolean IsSelectQuery(MultiDatabaseQuery multiDatabaseQuery){
		if(multiDatabaseQuery.query.length != multiDatabaseQuery.querySize)
			return false;
		
		for(int i = 0; i < multiDatabaseQuery.querySize; i++){
			if(multiDatabaseQuery.query[i].length() < 6)
				return false;

			if(!"SELECT".equals(multiDatabaseQuery.query[i].toUpperCase().substring(0, 6)))
				return false;

			if(multiDatabaseQuery.query[i].contains(";"))
				return false;
		}
		
		return true;
	}
	
	private void ReadResponses(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					ConnectSocket();
					if(!Utils.IsSocketValid(clientSocket) || ois == null){
						CloseSocket();
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
						}
						continue;
					}
					
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							ClientAppLogger.GetInstance().LogMessage("E402");
						} else if(inputObject instanceof DatabaseQueryResponse){
							DatabaseQueryResponse element = (DatabaseQueryResponse) inputObject;
							serverResponseList.AddResponse(element);
							ClientAppLogger.GetInstance().LogMessageDebug("8 ClientAppLocalServerClient ReadResponses DatabaseQueryResponse " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else if(inputObject instanceof MultiDatabaseQueryResponse){
							MultiDatabaseQueryResponse element = (MultiDatabaseQueryResponse) inputObject;
							serverResponseList.AddResponse(element);
							ClientAppLogger.GetInstance().LogMessageDebug("8 ClientAppLocalServerClient ReadResponses MultiDatabaseQueryResponse " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else {
							ClientAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						if(!ClientApp.appClosing){
							if(ex.getMessage() != null && ex.getMessage().contains("recv failed")){
								ClientAppLogger.GetInstance().LogMessage("ClientAppLocalServerClient disconnected (1)");
							} else if(ex.getMessage() != null && ex.getMessage().contains("Connection reset")){
								ClientAppLogger.GetInstance().LogMessage("ClientAppLocalServerClient disconnected (2)");
							} else {
								ClientAppLogger.GetInstance().LogError(ex);
							}
							
							CloseSocket();
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ex2) {}
						}
					}
				}
			}
		}).start();
	}
	
	private Connection getDatabaseConnection() throws UnknownHostException, SQLException {
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null && Licence.GetDBName() != null){
			databaseConnection = Utils.getDatabaseConnection(GetLocalServerAddress(), Values.CLIENT_APP_DATABASE_SERVER_PORT, Licence.GetDBName());
		}
		
		return databaseConnection;
	}
	
	private String GetLocalServerAddress(){
		String localServerAddress = "";
		
		try {
			if(Licence.IsLocalServer()){
				localServerAddress = InetAddress.getLocalHost().getHostAddress();
			} else {
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setBroadcast(true);
				InetAddress IPAddress = InetAddress.getByName("255.255.255.255");				
				String message = "client app to local server";
				
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Values.CLIENT_APP_LOCAL_SERVER_UDP_PORT);
				clientSocket.send(sendPacket);
				
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.setSoTimeout(1000);
				clientSocket.receive(receivePacket);
				String receivedString = new String(receivePacket.getData()).trim();
				if(("local server to client app" + Licence.GetOfficeNumber() + Licence.GetDBName()).equals(receivedString)){
					localServerAddress = receivePacket.getAddress().getHostAddress();
				}
				clientSocket.close();
			}
		} catch (Exception ex) {
			if(ex.getMessage() != null && ex.getMessage().contains("Receive timed out")){
				ClientAppLogger.GetInstance().LogMessage("E802");
			} else {
				ClientAppLogger.GetInstance().LogError(ex);
			}
		}
		
		return localServerAddress;
	}
	
	public void CloseSocket(){
		synchronized(socketLock){
			if(lastSocketConnectTime == null)
				return;
			
			if(new Date().getTime() - lastSocketConnectTime.getTime() < 5000)
				return;
			
			Utils.CloseSocket(clientSocket, oos, ois);
			clientSocket = null;
			oos = null;
			ois = null;
		}
	}
	
	public boolean IsValid(){
		return Utils.IsSocketValid(clientSocket);
	}
	
	public static void Init(){
		if(localServerClient == null){
			localServerClient = new ClientAppLocalServerClient();
		}
	}
	
	public static ClientAppLocalServerClient GetInstance(){
		return localServerClient;
	}
	
	public void OnAppClose(){
		try {
			CloseSocket();
			if(databaseConnection != null){
				databaseConnection.close();
				databaseConnection = null;
			}
		} catch (SQLException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
