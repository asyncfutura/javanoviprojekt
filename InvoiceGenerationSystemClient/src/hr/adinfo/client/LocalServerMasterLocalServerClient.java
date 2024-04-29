/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.services.LocalServerMasterLocalServerSync;
import hr.adinfo.client.datastructures.LocalServerMasterLocalServerRegisterData;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.MasterServerIPQuery;
import hr.adinfo.utils.communication.MasterServerIPResponse;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class LocalServerMasterLocalServerClient {
	private static LocalServerMasterLocalServerClient localServerMasterLocalServerClient = null;
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Connection databaseConnection;
	
	private final Object socketLock = new Object();
	private Date lastSocketConnectTime;
		
	private LocalServerMasterLocalServerClient(){
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
			
			boolean connectionSuccess = false;
			try {
				String masterLocalServerAddress = GetMasterLocalServerAddress();
				if("master local server not synced".equals(masterLocalServerAddress)){
					ClientAppLogger.GetInstance().LogMessage("W701");
				} else {
					clientSocket = new Socket(masterLocalServerAddress, Values.CLIENT_APP_MASTER_LOCAL_SERVER_LOCALHOST_PORT);
					oos = new ObjectOutputStream(clientSocket.getOutputStream());
					oos.flush();
					ois = new ObjectInputStream(clientSocket.getInputStream());

					LocalServerMasterLocalServerRegisterData data = new LocalServerMasterLocalServerRegisterData(Licence.GetOfficeNumber(), Licence.GetOfficeAddress());
					oos.writeObject(data);
					oos.flush();

					lastSocketConnectTime = new Date();
					connectionSuccess = true;
					
					if(!Licence.IsMasterLocalServer() && Licence.IsLocalServer() && LocalServer.GetInstance() != null){
						LocalServer.GetInstance().isSyncedWithMaster = false;
					}

					ClientAppLogger.GetInstance().LogMessage("LocalServerMasterLocalServerClient connected");
				}
			} catch (Exception ex) {
				Utils.CloseSocket(clientSocket, oos, ois);
				clientSocket = null;
				oos = null;
				ois = null;
				if(ex.getMessage() != null && ex.getMessage().contains("Connection refused: connect")){
					ClientAppLogger.GetInstance().LogMessage("E806");
				} else {
					ClientAppLogger.GetInstance().LogError(ex);
				}
			}
			
			if(!connectionSuccess){
				String masterIp = "";
				MasterServerIPQuery masterServerIPQuery = new MasterServerIPQuery();
				ServerResponse serverResponse = null;
				try {
					serverResponse = ClientAppServerAppClient.GetInstance().ExecuteQuery(masterServerIPQuery);
				} catch (Exception ex) {}
				if(serverResponse != null && serverResponse instanceof MasterServerIPResponse){
					masterIp = ((MasterServerIPResponse)serverResponse).masterIp;
				}
				ClientAppLogger.GetInstance().LogMessageDebug("LocalServerMasterLocalServerClient ConnectSocket MasterServerIPResponse " + masterIp);
				if(!"".equals(masterIp)){
					try {
						clientSocket = new Socket();
						clientSocket.connect(new InetSocketAddress(masterIp, Values.CLIENT_APP_MASTER_LOCAL_SERVER_LOCALHOST_PORT), 10000);
						oos = new ObjectOutputStream(clientSocket.getOutputStream());
						oos.flush();
						ois = new ObjectInputStream(clientSocket.getInputStream());

						lastSocketConnectTime = new Date();
						
						if(!Licence.IsMasterLocalServer() && Licence.IsLocalServer()&& LocalServer.GetInstance() != null){
							LocalServer.GetInstance().isSyncedWithMaster = false;
						}

						ClientAppLogger.GetInstance().LogMessage("LocalServerMasterLocalServerClient connected");
					} catch (IOException ex) {
						Utils.CloseSocket(clientSocket, oos, ois);
						clientSocket = null;
						oos = null;
						ois = null;
						if(ex.getMessage() != null && ex.getMessage().contains("Connection refused: connect")){
							ClientAppLogger.GetInstance().LogMessage("E807");
						} else {
							ClientAppLogger.GetInstance().LogError(ex);
						}
					}
				}
			}
		}
	}
	
	public void ForwardExecuteQuery(ServerQuery serverQuery) throws IOException {
		ConnectSocket();
		
		/*if(!Utils.IsSocketValid(clientSocket))
			return;*/
		
		if(!Utils.IsSocketValid(clientSocket) || oos == null){
			throw new IOException("E201");
		}
		
		if(!LocalServer.GetInstance().isSyncedWithMaster && !(serverQuery instanceof DatabaseDiffQuery)){
			throw new IOException("E101");
		}
		
		oos.writeObject(serverQuery);
		oos.flush();
		
		if(serverQuery instanceof DatabaseQuery){
			LocalServerMasterLocalServerSync.OnLocalServerDatabaseQueryForwarded();
		} else if(serverQuery instanceof MultiDatabaseQuery){
			LocalServerMasterLocalServerSync.OnLocalServerDatabaseQueryForwarded();
		}
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
							Thread.sleep(5000);
						} catch (InterruptedException ex) {
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
						}
						continue;
					}
					
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							ClientAppLogger.GetInstance().LogMessage("E404");
						} else if(inputObject instanceof DatabaseQueryResponse){
							DatabaseQueryResponse element = (DatabaseQueryResponse) inputObject;
							LocalServer.GetServerResponseList().AddResponse(element);
							ClientAppLogger.GetInstance().LogMessageDebug("6 LocalServerMasterLocalServerClient ReadResponses DatabaseQueryResponse " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else if(inputObject instanceof MultiDatabaseQueryResponse){
							MultiDatabaseQueryResponse element = (MultiDatabaseQueryResponse) inputObject;
							LocalServer.GetServerResponseList().AddResponse(element);
							ClientAppLogger.GetInstance().LogMessageDebug("6 LocalServerMasterLocalServerClient ReadResponses MultiDatabaseQueryResponse " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else if(inputObject instanceof DatabaseDiffResponse){
							DatabaseDiffResponse element = (DatabaseDiffResponse) inputObject;
							LocalServer.GetServerResponseList().AddResponse(element);
							ClientAppLogger.GetInstance().LogMessageDebug("6 LocalServerMasterLocalServerClient ReadResponses DatabaseDiffResponse " + element.queryId + " (" + element.clientId + ") [" + element.localServerId + "]");
						} else {
							ClientAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						if(!ClientApp.appClosing){
							if(ex.getMessage() != null && ex.getMessage().contains("recv failed")){
								ClientAppLogger.GetInstance().LogMessage("LocalServerMasterLocalServerClient disconnected (1)");
							} else if(ex.getMessage() != null && ex.getMessage().contains("Connection reset")){
								ClientAppLogger.GetInstance().LogMessage("LocalServerMasterLocalServerClient disconnected (2)");
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

	// TODO remove - used for testing
	private String GetMasterLocalServerAddress(){
		String localServerAddress = "";
		
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setBroadcast(true);
			InetAddress IPAddress = InetAddress.getByName("255.255.255.255");				
			String message = "local server to master local server";

			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9999);
			clientSocket.send(sendPacket);

			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.setSoTimeout(1000);
			clientSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData()).trim();
			if(("master local server to local server" + Licence.GetDBName()).equals(receivedString)){
				localServerAddress = receivePacket.getAddress().getHostAddress();
			} else if ("master local server not synced".equals(receivedString)){
				localServerAddress = receivedString;
			}
			clientSocket.close();
		} catch (Exception ex) {
			if(ex.getMessage() != null && ex.getMessage().contains("Receive timed out")){
				ClientAppLogger.GetInstance().LogMessage("E803");
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
		if(localServerMasterLocalServerClient == null){
			localServerMasterLocalServerClient = new LocalServerMasterLocalServerClient();
		}
	}
	
	public static LocalServerMasterLocalServerClient GetInstance(){
		return localServerMasterLocalServerClient;
	}
	
	public void OnAppClose(){
		try {
			Utils.CloseSocket(clientSocket, oos, ois);
			clientSocket = null;
			oos = null;
			ois = null;
			if(databaseConnection != null){
				databaseConnection.close();
				databaseConnection = null;
			}
		} catch (SQLException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
