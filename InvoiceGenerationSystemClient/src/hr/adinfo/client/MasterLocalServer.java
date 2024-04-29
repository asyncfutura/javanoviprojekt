/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryList;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Matej
 */
public class MasterLocalServer {
	private static MasterLocalServer masterLocalServer = null;
	private ServerSocket serverSocket = null;
        private ServerSocket serverWebAppSocket = null; 
        private ServerSocket serverSparePrvaKasaSocket = null;
        private ServerSocket serverSpareDrugaKasaSocket = null;
        private ServerSocket serverPingvinPrvaKasaSocket = null;
        private ServerSocket serverPingvinDrugaKasaSocket = null;
        private ServerSocket serverKaptolskaKletSocket = null;
        private ServerSocket serverBreceraSocket = null;
	private Connection databaseConnection;
	private ServerQueryList serverQueryList = new ServerQueryList();
	private ServerResponseList serverResponseList = new ServerResponseList();
	private DatagramSocket udpServerSocket;
        private boolean passOnce = false;
	
	public boolean isMasterSynced;
	
	private MasterLocalServer(){	
		try {
			serverSocket = new ServerSocket(Values.CLIENT_APP_MASTER_LOCAL_SERVER_LOCALHOST_PORT);
                        serverWebAppSocket = new ServerSocket(Values.CLIENT_APP_TOTAL_PORT);
                        //serverSparePrvaKasaSocket = new ServerSocket(Values.CLIENT_APP_SPARE_PRVA_KASA_PORT);
                        //serverSpareDrugaKasaSocket = new ServerSocket(Values.CLIENT_APP_SPARE_DRUGA_KASA_PORT);
                        /*serverPingvinPrvaKasaSocket = new ServerSocket(Values.CLIENT_APP_PINGVIN_PRVA_KASA_PORT);
                        serverPingvinDrugaKasaSocket = new ServerSocket(Values.CLIENT_APP_PINGVIN_DRUGA_KASA_PORT);
                        serverKaptolskaKletSocket = new ServerSocket(Values.CLIENT_APP_KAPTOLSKA_KLET_PRVA_KASA_PORT);
                        serverBreceraSocket = new ServerSocket(Values.CLIENT_APP_SPARE_DRUGA_KASA_PORT);*/
		} catch (IOException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
			return;
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverSocket.accept();
						if(!isMasterSynced){
							new ObjectOutputStream(clientSocket.getOutputStream()).flush();
							clientSocket.close();
							ClientAppLogger.GetInstance().LogMessage("E501");
							continue;
						}
						
						new MasterLocalServerHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverWebAppSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
               
      /*          new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverSparePrvaKasaSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverSpareDrugaKasaSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();*/
                 /*                                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverPingvinPrvaKasaSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                                                                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverPingvinDrugaKasaSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                                                                                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverKaptolskaKletSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                                                                                                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					try {
						Socket clientSocket = serverBreceraSocket.accept();
						new MasterLocalServerWebAppHost(clientSocket);
					} catch (IOException ex) {
						if(!ClientApp.appClosing)
							ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                                       */                                                                        
		
		// TODO remove - used for testing
		// Setup UDP server
		try {
			udpServerSocket = new DatagramSocket(9999);
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
							ClientAppLogger.GetInstance().LogMessage("Master local Server: UDP message recieved: " + receivedString + "; from " + receivePacket.getAddress() + ":" + receivePacket.getPort());
							if("local server to master local server".equals(receivedString)){
								InetAddress IPAddress = receivePacket.getAddress();
								int port = receivePacket.getPort();
								String response = "master local server to local server" + Licence.GetDBName();
								if(!isMasterSynced){
									response = "master local server not synced";
								}
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
		} catch (SocketException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}

		CheckQueries();
	}
	
	private void CheckQueries(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(ClientApp.appClosing)
						break;
					
					ServerQuery serverQuery;
					while((serverQuery = serverQueryList.GetNextQuery()) != null){
						ClientAppLogger.GetInstance().LogMessageDebug("4 MasterLocalServer CheckQueries " + serverQuery.queryId + " (" + serverQuery.clientId + ") [" + serverQuery.localServerId + "]");

						// Wait for sync
						if(!isMasterSynced){
							DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse(serverQuery);
							databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_MASTER_NOT_SYNCED;
							
							serverResponseList.AddResponse(databaseQueryResponse);
							serverQueryList.RemoveQuery(serverQuery);
							continue;
						}
						
						if(serverQuery instanceof DatabaseQuery){
							DatabaseQueryResponse databaseQueryResponse = new DatabaseQueryResponse(serverQuery);
							try {
								databaseQueryResponse.databaseQueryResult = Utils.MasterExecuteDatabaseQuery(getDatabaseConnection(), (DatabaseQuery) serverQuery, ClientApp.databaseTransactionLock);
							} catch (IOException | SQLException ex) {
								if(!ClientApp.appClosing){
									ClientAppLogger.GetInstance().ShowErrorLog(ex);
									databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								}
							}
							
							serverResponseList.AddResponse(databaseQueryResponse);
							serverQueryList.RemoveQuery(serverQuery);
						} else if(serverQuery instanceof MultiDatabaseQuery){
							MultiDatabaseQueryResponse databaseQueryResponse = new MultiDatabaseQueryResponse(serverQuery);
							try {
								databaseQueryResponse.databaseQueryResult = Utils.MasterExecuteMultiDatabaseQuery(getDatabaseConnection(), (MultiDatabaseQuery) serverQuery, ClientApp.databaseTransactionLock);
							} catch (IOException | SQLException ex) {
								if(!ClientApp.appClosing){
									ClientAppLogger.GetInstance().ShowErrorLog(ex);
									for (int i = 0; i < ((MultiDatabaseQuery) serverQuery).query.length; ++i){
										ClientAppLogger.GetInstance().LogMessageDebug(((MultiDatabaseQuery) serverQuery).query[i]);
									}
									databaseQueryResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								}
							}

							serverResponseList.AddResponse(databaseQueryResponse);
							serverQueryList.RemoveQuery(serverQuery);
						} else if(serverQuery instanceof DatabaseDiffQuery){
							DatabaseDiffResponse databaseDiffResponse = new DatabaseDiffResponse(serverQuery);
							try {
								databaseDiffResponse = Utils.ExecuteDatabaseDiffQuery(getDatabaseConnection(), (DatabaseDiffQuery) serverQuery);
							} catch (Exception ex) {
								if(!ClientApp.appClosing){
									ClientAppLogger.GetInstance().ShowErrorLog(ex);
									databaseDiffResponse.errorCode = Values.RESPONSE_ERROR_CODE_SQL_QUERY_FAILED;
								}
							}

							serverResponseList.AddResponse(databaseDiffResponse);
							serverQueryList.RemoveQuery(serverQuery);
						}
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
	
	public void RegisterOffice(int officeNumber, String officeAddress){
		final int fakeClientId = Values.FAKE_QUERY_ID_REGISTER_OFFICE;
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
							Thread.sleep(1000);
						} catch (InterruptedException ex) {}
					}
					doSleep = true;
					
					DatabaseQuery databaseQuery = new DatabaseQuery("SELECT OFFICE_NUMBER, ADDRESS FROM OFFICES WHERE OFFICE_NUMBER = ?");
					databaseQuery.clientId = fakeClientId;
					databaseQuery.localServerId = fakeClientId;
					databaseQuery.AddParam(1, officeNumber);
					serverQueryList.AddQuery(databaseQuery);

					boolean doInsert = false;
					boolean doUpdate = false;
					int counter = 0;
					final int timeoutDelayMiliseconds = 100;
					final int timeoutSeconds = 10;
					while(counter < 1000 * timeoutSeconds){
						if(ClientApp.appClosing)
							break;

						ServerResponse serverResponseFound;
						if((serverResponseFound = MasterLocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(databaseQuery.queryId, fakeClientId)) != null){
							MasterLocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
							DatabaseQueryResponse databaseQueryResponse = (DatabaseQueryResponse) serverResponseFound;
							if(databaseQueryResponse.databaseQueryResult.next()){
								if(officeAddress.equals(databaseQueryResponse.databaseQueryResult.getString(1))){
									success = true;
								} else {
									doUpdate = true;
								}
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
					MultiDatabaseQuery multiDatabaseQuery = null;
					if(doInsert){
						// Insert office
						String query = "INSERT INTO OFFICES (OFFICE_NUMBER, ADDRESS, IS_DELETED) VALUES (?, ?, ?)";
						multiDatabaseQuery = new MultiDatabaseQuery(1 + 1 + 14);
						multiDatabaseQuery.clientId = fakeClientId;
						multiDatabaseQuery.SetQuery(0, query);
						multiDatabaseQuery.AddParam(0, 1, officeNumber);
						multiDatabaseQuery.AddParam(0, 2, officeAddress);
						multiDatabaseQuery.AddParam(0, 3, 0);

						// Insert office worktime
						String officeWorktimeQueryInsert = "INSERT INTO OFFICE_WORKTIME (ID, OFFICE_NUMBER, "
								+ "W1, W2, W3, W4, W5, W6, W7, "
								+ "HF1, HT1, MF1, MT1, "
								+ "HF2, HT2, MF2, MT2, "
								+ "HF3, HT3, MF3, MT3, "
								+ "HF4, HT4, MF4, MT4, "
								+ "HF5, HT5, MF5, MT5, "
								+ "HF6, HT6, MF6, MT6, "
								+ "HF7, HT7, MF7, MT7) "
								+ "VALUES (?, ?, "
								+ "?, ?, ?, ?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?, "
								+ "?, ?, ?, ?)";
						multiDatabaseQuery.SetQuery(1, officeWorktimeQueryInsert);
						multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "OFFICE_WORKTIME");
						multiDatabaseQuery.AddParam(1, 2, officeNumber);
						for(int i = 0; i < 7; ++i){
							multiDatabaseQuery.AddParam(1, 3 + i, 1);
						}
						for(int i = 0; i < 4 * 7; ++i){
							multiDatabaseQuery.AddParam(1, 3 + 7 + i, 0);
						}

						// Insert holidays
						String holidaysQueryInsert = "INSERT INTO HOLIDAYS (ID, OFFICE_NUMBER, "
								+ "HOLIDAY_DATE, NAME, IS_ACTIVE, IS_DELETED) "
								+ "VALUES (?, ?, ?, ?, ?, ?)";
						String[] toInsertDate = new String[] {"2020-01-01", "2020-01-06", "2020-04-12", "2020-04-13", "2020-05-01", "2020-05-30", 
							"2020-06-11", "2020-06-22", "2020-08-05", "2020-08-15", 
							"2020-11-01", "2020-11-18", "2020-12-25", "2020-12-26"};
						String[] toInsertName = new String[] {"Nova godina", "Sveta tri kralja", "Uskrs", "Uskrsni ponedjeljak", "Praznik rada", "Dan državnosti", 
							"Tijelovo", "Dan antifašističke borbe", "Dan domovinske zahvalnosti", "Velika Gospa", 
							"Dan svih svetih", "Dan sjećanja na žrtve Domovinskog rata", "Božić", "Sveti Stjepan"};
						for (int i = 0; i < 14; ++i){
							multiDatabaseQuery.SetQuery(2 + i, holidaysQueryInsert);
							multiDatabaseQuery.SetAutoIncrementParam(2 + i, 1, "ID", "HOLIDAYS");
							multiDatabaseQuery.AddParam(2 + i, 2, officeNumber);
							multiDatabaseQuery.AddParam(2 + i, 3, toInsertDate[i]);
							multiDatabaseQuery.AddParam(2 + i, 4, toInsertName[i]);
							multiDatabaseQuery.AddParam(2 + i, 5, 1);
							multiDatabaseQuery.AddParam(2 + i, 6, 0);
						}

						serverQueryList.AddQuery(multiDatabaseQuery);
					} else if (doUpdate){
						String query = "UPDATE OFFICES SET ADDRESS = ? WHERE OFFICE_NUMBER = ?";
						multiDatabaseQuery = new MultiDatabaseQuery(1);
						multiDatabaseQuery.clientId = fakeClientId;
						multiDatabaseQuery.SetQuery(0, query);
						multiDatabaseQuery.AddParam(0, 1, officeAddress);
						multiDatabaseQuery.AddParam(0, 2, officeNumber);
						serverQueryList.AddQuery(multiDatabaseQuery);
					}

					if(multiDatabaseQuery != null){
						counter = 0;
						while(counter < 1000 * timeoutSeconds){
							if(ClientApp.appClosing)
								break;

							ServerResponse serverResponseFound;
							if((serverResponseFound = MasterLocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(multiDatabaseQuery.queryId, fakeClientId)) != null){
								MasterLocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
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
	
	// TODO remove
	public void TestDiffStressTest(DatabaseQuery databaseQuery, int count){
		try {
			for(int i = 0; i < count; ++i){
				serverQueryList.AddQuery(databaseQuery);
			}
		} catch (Exception ex) {}
		
		while(serverQueryList.GetNextQuery() != null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
	}
	// TestDiffStressTest usage example:
	/*if(MasterLocalServer.GetInstance() != null){
		String hex = String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue());  
		DatabaseQuery databaseQuery = new DatabaseQuery("UPDATE TEST_TABLE SET COLOR = ? WHERE ID = ?");
		databaseQuery.AddParam(1, hex);
		databaseQuery.AddParam(2, rowID);
		MasterLocalServer.GetInstance().TestDiffStressTest(databaseQuery, 10000);
	}*/
	
	public static ServerQueryList GetServerQueryList(){
		return masterLocalServer.serverQueryList;
	}
	
	public static ServerResponseList GetServerResponseList(){
		return masterLocalServer.serverResponseList;
	}
	
	private Connection getDatabaseConnection() throws UnknownHostException, SQLException {
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null && Licence.GetDBName() != null){
			databaseConnection = Utils.getDatabaseConnection(InetAddress.getLocalHost().getHostAddress(), Values.CLIENT_APP_DATABASE_SERVER_PORT, Licence.GetDBName());
		}
		
		return databaseConnection;
	}
	
	public static void Init(){
		if(masterLocalServer == null){
			masterLocalServer = new MasterLocalServer();
		}
	}
	
	public static MasterLocalServer GetInstance(){
		return masterLocalServer;
	}
	
	public void OnAppClose(){
		try {
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
