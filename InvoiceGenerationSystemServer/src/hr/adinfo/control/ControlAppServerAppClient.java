/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control;

import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import static hr.adinfo.utils.Utils.*;
import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.communication.ServerResponseList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import hr.adinfo.utils.communication.ExecuteQueryInterface;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author Matej
 */
public class ControlAppServerAppClient implements ExecuteQueryInterface {
	private static ControlAppServerAppClient controlAppServerAppClient = null;
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private ServerResponseList serverResponseList;
	
	private ControlAppServerAppClient(){
		serverResponseList = new ServerResponseList();
		
		ConnectSocket();
		ReadResponses();
	}
	
	private void ConnectSocket() {
		if(IsSocketValid(clientSocket))
			return;
		
		try {
			String serverAppAddress = GetServerAppAddress();
			clientSocket = new Socket(serverAppAddress, SERVER_APP_CONTROL_LOCALHOST_PORT);
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException ex) {
			//LicencesUpdatesControlApp.ShowErrorLog(ex.toString());
		}
	}
	
	@Override
	public ServerResponse ExecuteQuery(ServerQuery serverQuery) throws Exception {
		ConnectSocket();
		return ExecuteRemoteQuery(serverQuery, clientSocket, oos, serverResponseList);
	}
	
	private void ReadResponses(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ControlApp.appClosing)
						break;
					
					ConnectSocket();
					if(!IsSocketValid(clientSocket)){
						try {
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							ControlAppLogger.GetInstance().ShowErrorLog(ex);
						}
						continue;
					}
					
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							// TODO connection ended
						} else if(inputObject instanceof DatabaseQueryResponse){
							DatabaseQueryResponse element = (DatabaseQueryResponse) inputObject;
							serverResponseList.AddResponse(element);
						} else if(inputObject instanceof ServerResponse){
							ServerResponse element = (ServerResponse) inputObject;
							serverResponseList.AddResponse(element);
						} else {
							ControlAppLogger.GetInstance().ShowMessage("Unknown message recieved: " + this.getClass().getName() + System.lineSeparator() + inputObject.toString());
						}
					} catch (IOException | ClassNotFoundException ex) {
						if(!ControlApp.appClosing){
							ControlAppLogger.GetInstance().ShowErrorLog(ex);
							try {
								clientSocket.close();
							} catch (Exception ex1) {}
							clientSocket = null;
						}
						
					}
				}
			}
		}).start();
	}
	
	private String GetServerAppAddress(){
		String localServerAddress = "";
		
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setBroadcast(true);
			InetAddress IPAddress = InetAddress.getByName("255.255.255.255");				
			String message = "control app to server app";

			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_APP_CONTROL_UDP_PORT);
			clientSocket.send(sendPacket);
			
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.setSoTimeout(1000);
			clientSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData()).trim();
			if("server app to control app".equals(receivedString)){
				localServerAddress = receivePacket.getAddress().getHostAddress();
			}
			clientSocket.close();
		} catch (Exception ex) {
			ControlAppLogger.GetInstance().ShowErrorLog(ex);
		}

		return localServerAddress;
	}
	
	public static void Init(){
		if(controlAppServerAppClient == null){
			controlAppServerAppClient = new ControlAppServerAppClient();
		}
	}
	
	public static ControlAppServerAppClient GetInstance(){
		return controlAppServerAppClient;
	}
	
	public void OnAppClose(){
		try {
			if(oos != null){
				oos.close();
				oos = null;
			}
			if(ois != null){
				ois.close();
				ois = null;
			}
			if(clientSocket != null){
				clientSocket.close();
				clientSocket = null;
			}
		} catch (IOException ex) {
			ControlAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
}
