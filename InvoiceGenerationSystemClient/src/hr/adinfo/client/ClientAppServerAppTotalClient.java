/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.services.LocalServerServerAppPingService;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.communication.ExecuteQueryInterface;
import hr.adinfo.utils.communication.MasterServerIPResponse;
import hr.adinfo.utils.communication.misc.LocalServerServerAppPingData;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.licence.Licence;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class ClientAppServerAppTotalClient implements ExecuteQueryInterface {
	private static ClientAppServerAppTotalClient ClientAppServerAppTotalClient = null;
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private ServerResponseList serverResponseList = new ServerResponseList();
	private final Object oosLock = new Object();
	
	private final Object socketLock = new Object();
	private Date lastSocketConnectTime;
	
	private ClientAppServerAppTotalClient() {
		ConnectSocket();
		ReadResponses();
	}
	
	private void ConnectSocket() {
                        ClientAppLogger.GetInstance().LogMessage("I entered connect socket into clientappserverapptotalclient. I do not know why.");
		synchronized(socketLock){
			if(Utils.IsSocketValid(clientSocket) && oos != null && ois != null)
				return;

			Utils.CloseSocket(clientSocket, oos, ois);
			clientSocket = null;
			oos = null;
			ois = null;
			
			boolean connectionSuccess = false;
			try {
				String serverAppAddress = GetServerAppAddress();
				clientSocket = new Socket(serverAppAddress, Values.SERVER_APP_LOCALHOST_PORT);
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(clientSocket.getInputStream());

				lastSocketConnectTime = new Date();
				
				OnSocketConnected();
				connectionSuccess = true;
			} catch (IOException ex) {
				Utils.CloseSocket(clientSocket, oos, ois);
				clientSocket = null;
				oos = null;
				ois = null;
				if(ex.getMessage() != null && ex.getMessage().contains("Connection refused: connect")){
					ClientAppLogger.GetInstance().LogMessage("E804");
				} else {
					ClientAppLogger.GetInstance().LogError(ex);
				}
			}
			
			if(!connectionSuccess){
				try {
					clientSocket = new Socket();
					clientSocket.connect(new InetSocketAddress(Values.SERVER_APP_ADDRESS, Values.SERVER_APP_LOCALHOST_PORT), 10000);
					oos = new ObjectOutputStream(clientSocket.getOutputStream());
					oos.flush();
					ois = new ObjectInputStream(clientSocket.getInputStream());

					lastSocketConnectTime = new Date();

					OnSocketConnected();
				} catch (IOException ex) {
					Utils.CloseSocket(clientSocket, oos, ois);
					clientSocket = null;
					oos = null;
					ois = null;
					if(ex.getMessage() != null && ex.getMessage().contains("Connection refused: connect")){
						ClientAppLogger.GetInstance().LogMessage("E805");
					} else {
						ClientAppLogger.GetInstance().LogError(ex);
					}
				}
			}
		}
	}
	
	private void OnSocketConnected(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				ClientAppLogger.GetInstance().LogMessage("ClientAppServerAppTotalClient connected");
				
				if(ClientApp.GetInstance() != null){
					ClientApp.GetInstance().RefreshLicence(true);
				}

				if(Licence.IsLocalServer()){
					LocalServerServerAppPingService.PingServerApp();
				}

				if(Licence.IsMasterLocalServer() && MasterLocalServer.GetInstance() != null){
					MasterLocalServer.GetInstance().isMasterSynced = false;
				}
			}
		}).start();
	}
	
	@Override
	public ServerResponse ExecuteQuery(ServerQuery serverQuery) throws Exception {
		ConnectSocket();
		synchronized (oosLock){
			return Utils.ExecuteRemoteQuery(serverQuery, clientSocket, oos, serverResponseList);
		}
	}
	
	public boolean ServerAppPing(boolean haveUnfiscalizedInvoices){
		ClientAppLogger.GetInstance().LogMessage("ServerAppPing");
		ConnectSocket();
		try {
			LocalServerServerAppPingData data = new LocalServerServerAppPingData(Licence.GetCompanyId(), Licence.GetOfficeNumber(), haveUnfiscalizedInvoices, Licence.IsControlApp());
			synchronized (oosLock){
				oos.writeObject(data);
				oos.flush();
			}
			return true;
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().LogError(ex);
		}
		
		return false;
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
							ClientAppLogger.GetInstance().LogMessage("E401");
						} else if(inputObject instanceof LicenceQueryResponse){
							LicenceQueryResponse element = (LicenceQueryResponse) inputObject;
							serverResponseList.AddResponse(element);
						} else if(inputObject instanceof DatabaseDiffResponse){
							DatabaseDiffResponse element = (DatabaseDiffResponse) inputObject;
							serverResponseList.AddResponse(element);
						} else if(inputObject instanceof MasterServerIPResponse){
							MasterServerIPResponse element = (MasterServerIPResponse) inputObject;
							serverResponseList.AddResponse(element);
						} else if(inputObject instanceof DatabaseQuery){
							DatabaseQuery element = (DatabaseQuery) inputObject;
							int oldQueryId = element.queryId;
							element.queryId = new ServerQuery().queryId;
							ServerResponse serverResponse = null;
							try {
								serverResponse = ClientAppLocalServerClient.GetInstance().ExecuteQuery(element);
							} catch (Exception ex) {}
							if(serverResponse != null){
								serverResponse.queryId = oldQueryId;
								synchronized (oosLock){
									oos.writeObject(serverResponse);
									oos.flush();
								}
							}
						} else {
							ClientAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (Exception ex) {
						if(!ClientApp.appClosing){
							if(ex.getMessage() != null && ex.getMessage().contains("recv failed")){
								ClientAppLogger.GetInstance().LogMessage("ClientAppServerAppTotalClient disconnected (1)");
							} else if(ex.getMessage() != null && ex.getMessage().contains("Connection reset")){
								ClientAppLogger.GetInstance().LogMessage("ClientAppServerAppTotalClient disconnected (2)");
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
	private String GetServerAppAddress(){
		String localServerAddress = "";
		
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setBroadcast(true);
			InetAddress IPAddress = InetAddress.getByName("255.255.255.255");				
			String message = "client app to server app";

			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Values.SERVER_APP_CONTROL_UDP_PORT);
			clientSocket.send(sendPacket);

			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.setSoTimeout(1000);
			clientSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData()).trim();
			if("server app to client app".equals(receivedString)){
				localServerAddress = receivePacket.getAddress().getHostAddress();
			}
			clientSocket.close();
		} catch (Exception ex) {
			if(ex.getMessage() != null && ex.getMessage().contains("Receive timed out")){
				ClientAppLogger.GetInstance().LogMessage("E801");
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
		if(ClientAppServerAppTotalClient == null){
			ClientAppServerAppTotalClient = new ClientAppServerAppTotalClient();
		}
	}
	
	public static ClientAppServerAppTotalClient GetInstance(){
		return ClientAppServerAppTotalClient;
	}
	
	public void OnAppClose(){
		CloseSocket();
	}
}
