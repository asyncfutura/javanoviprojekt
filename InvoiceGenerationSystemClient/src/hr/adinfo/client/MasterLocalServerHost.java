/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.datastructures.LocalServerMasterLocalServerRegisterData;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Matej
 */
public class MasterLocalServerHost {
	public static int masterLocalServerClientIdCount = 0;
	private final int masterLocalServerClientId;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	MasterLocalServerHost(Socket clientSocket) {
		this.clientSocket = clientSocket;
		masterLocalServerClientId = ++masterLocalServerClientIdCount;
		// TODO break all connections
		if(masterLocalServerClientIdCount > 1000000)
			masterLocalServerClientIdCount = 0;
		
		try {
			CreateStreams();
			ReadQueries();
			CheckResponses();
			ClientAppLogger.GetInstance().LogMessage("MasterLocalServerHost connected");
		} catch (IOException ex) {
			ClientAppLogger.GetInstance().LogError(ex);
		}
	}
	
	private void CreateStreams() throws IOException {
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
						if(!MasterLocalServer.GetInstance().isMasterSynced){
							CloseSocket();
							break;
						}
						
						if(inputObject == null){
							ClientAppLogger.GetInstance().LogMessage("E405");
						} else if(inputObject instanceof ServerQuery){
							ServerQuery element = (ServerQuery) inputObject;
							element.localServerId = masterLocalServerClientId;
							MasterLocalServer.GetServerQueryList().AddQuery(element);
							ClientAppLogger.GetInstance().LogMessageDebug("3 MasterLocalServerHost ReadQueries ServerQuery " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else if(inputObject instanceof LocalServerMasterLocalServerRegisterData){
							LocalServerMasterLocalServerRegisterData element = (LocalServerMasterLocalServerRegisterData) inputObject;
							if(element.officeNumber != -1){
								// TODO Licence not valid
								// TODO Save this office connectionId
								// TODO Check if Local server with this number already connected
								MasterLocalServer.GetInstance().RegisterOffice(element.officeNumber, element.officeAddress);
							}
						} else {
							ClientAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						if(!ClientApp.appClosing){
							if(ex.getMessage() != null && ex.getMessage().contains("recv failed")){
								ClientAppLogger.GetInstance().LogMessage("MasterLocalServerHost disconnected (1)");
							} else if(ex.getMessage() != null && ex.getMessage().contains("Connection reset")){
								ClientAppLogger.GetInstance().LogMessage("MasterLocalServerHost disconnected (2)");
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
					while((serverResponseFound = MasterLocalServer.GetServerResponseList().GetResponseByLocalServerId(masterLocalServerClientId)) != null){
						ClientAppLogger.GetInstance().LogMessageDebug("5 MasterLocalServerHost CheckResponses " + serverResponseFound.queryId + " (" + serverResponseFound.clientId + ") [" + serverResponseFound.localServerId + "]");
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

						MasterLocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
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
