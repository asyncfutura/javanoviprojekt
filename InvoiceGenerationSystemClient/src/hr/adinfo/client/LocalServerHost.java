/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.datastructures.ClientAppLocalServerRegisterData;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
 
/**
 *
 * @author Matej
 */
public class LocalServerHost {
	private static int localServerClientIdCount = 0;
	private final int localServerClientId;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	LocalServerHost(Socket clientSocket) {
		this.clientSocket = clientSocket;
		localServerClientId = ++localServerClientIdCount;
		if(localServerClientIdCount > 1000000)
			localServerClientIdCount = 0;
		
		try {
			CreateStreams();
			ReadQueries();
			CheckResponses();
			ClientAppLogger.GetInstance().LogMessage("LocalServerHost connected");
		} catch (IOException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
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
					if(ClientApp.appClosing)
						break;
					
					try {
						Object inputObject = ois.readObject();
						
						// Wait for sync
						if(inputObject instanceof ServerQuery && !LocalServer.GetInstance().isSyncedWithMaster){
							DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse((ServerQuery)inputObject);
							databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_LOCAL_SERVER_NOT_SYNCED;
							databaseQueryResponse.clientId = localServerClientId;
							LocalServer.GetServerResponseList().AddResponse(databaseQueryResponse); 
							continue;
						}
						
						if(inputObject == null){
							ClientAppLogger.GetInstance().LogMessage("E403");
						} else if(inputObject instanceof DatabaseQuery){
							DatabaseQuery element = (DatabaseQuery) inputObject;
							element.clientId = localServerClientId;
							ClientAppLogger.GetInstance().LogMessageDebug("2 LocalServerHost ReadQueries DatabaseQuery " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
							try {
								LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(element);
							} catch (IOException ex) {
								DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse(element);
								databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_CONNECTION_FAILED;
								LocalServer.GetServerResponseList().AddResponse(databaseQueryResponse);
								ClientAppLogger.GetInstance().LogError(ex);
							}
						} else if(inputObject instanceof MultiDatabaseQuery){
							MultiDatabaseQuery element = (MultiDatabaseQuery) inputObject;
							element.clientId = localServerClientId;
							ClientAppLogger.GetInstance().LogMessageDebug("2 LocalServerHost ReadQueries MultiDatabaseQuery " + element.queryId  + " (" + element.clientId + ") [" + element.localServerId + "]");
							try {
								LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(element);
							} catch (IOException ex) {
								MultiDatabaseQueryResponse multiDatabaseQueryResponse = new MultiDatabaseQueryResponse(element);
								multiDatabaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_CONNECTION_FAILED;
								LocalServer.GetServerResponseList().AddResponse(multiDatabaseQueryResponse);
								ClientAppLogger.GetInstance().LogError(ex);
							}
						} else if(inputObject instanceof ClientAppLocalServerRegisterData){
							ClientAppLocalServerRegisterData element = (ClientAppLocalServerRegisterData) inputObject;
							ClientAppLogger.GetInstance().LogMessageDebug("2 LocalServerHost ReadQueries ClientAppLocalServerRegisterData " + element.officeNumber + " " + element.cashRegisterNumber);
							if(element.officeNumber != -1 && element.cashRegisterNumber != -1){
								// TODO Licence not valid
								// TODO Save this cash register connectionId
								// TODO Check if cash register with this number already connected
								if(Licence.GetOfficeNumber() != element.officeNumber){
									CloseSocket();
									return;
								}
								LocalServer.GetInstance().RegisterCashRegister(element.officeNumber, element.cashRegisterNumber);
							}
						} else {
							ClientAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						if(!ClientApp.appClosing){
							if(ex.getMessage() != null && ex.getMessage().contains("recv failed")){
								ClientAppLogger.GetInstance().LogMessage("LocalServerHost disconnected (1)");
							} else if(ex.getMessage() != null && ex.getMessage().contains("Connection reset")){
								ClientAppLogger.GetInstance().LogMessage("LocalServerHost disconnected (2)");
							} else {
								ClientAppLogger.GetInstance().LogError(ex);
							}
						}
						
						CloseSocket();
						
						return;
					}
				}
			}
		}).start();
	}
	
	private void CheckResponses(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					ServerResponse serverResponseFound;
					while((serverResponseFound = LocalServer.GetServerResponseList().GetResponseByClientId(localServerClientId)) != null){
						ClientAppLogger.GetInstance().LogMessageDebug("7 LocalServerHost CheckResponses " + serverResponseFound.queryId + " (" + serverResponseFound.clientId + ") [" + serverResponseFound.localServerId + "]");
						try {
							oos.writeObject(serverResponseFound);
							oos.flush();
						} catch (Exception ex) {
							if(oos != null){
								ClientAppLogger.GetInstance().ShowErrorLog(ex);
							}
							CloseSocket();
							return;
						}
						
						LocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
	}
	
	public void CloseSocket(){
		Utils.CloseSocket(clientSocket, oos, ois);
		clientSocket = null;
		oos = null;
		ois = null;
	}
	
	public static void OnAppClose(){
		
	}
}
