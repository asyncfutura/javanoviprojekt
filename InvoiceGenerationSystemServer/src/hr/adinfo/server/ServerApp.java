/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import hr.adinfo.server.ui.ServerAppMainWindow;
import hr.adinfo.utils.Utils;
import static hr.adinfo.utils.Utils.CheckMultipleInstances;
import static hr.adinfo.utils.Utils.DisposeDialog;
import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.database.DatabaseQuery;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.derby.drda.NetworkServerControl;
import sun.misc.IOUtils;
import hr.adinfo.control.ControlAppServerAppClient;
import hr.adinfo.control.ControlAppLogger;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matej
 */
public class ServerApp { 
	private static ServerApp serverApp;
	private static ServerSocket serverAppLock;
	private ServerAppMainWindow serverAppWindow;
	private NetworkServerControl derbyServer;
	private Connection databaseConnection;
	private ServerSocket serverSocket = null;
        private ServerSocket serverSocketTotal = null;
        private ServerSocket serverSparePrvaKasaSocket = null;
        private ServerSocket serverSpareDrugaKasaSocket = null;
        private ServerSocket serverPingvinPrvaKasaSocket = null;
        private ServerSocket serverPingvinDrugaKasaSocket = null;
        private ServerSocket serverKaptolskaKletSocket = null;
        private ServerSocket serverBreceraSocket = null;
        private Socket client;
        private InputStream input;
	private ServerSocket serverControlSocket = null;
	private DatagramSocket udpServerSocket = null;
	private ArrayList<ServerAppClientAppHost> clientAppHosts = new ArrayList<>();
        private ArrayList<ServerAppWebAppHost> clientAppHostsTotal = new ArrayList<>();
	
	public static final Object databaseTransactionLock = new Object();
	public static final Object hostsListLock = new Object();
        public static final Object hostsListLockTotal = new Object();
	public static boolean appClosing;
        public static String OIB = null;
        public static Socket clientSocket = null;
        public static Socket controlSocket = null;
        public static String Total = "";
        public static boolean bothDates = false;
        public static String nameOfPerson = ""; 
        public static String databaseName = ""; 
        public static String dateFrom = ""; 
        public static String dateTo = "";
        public static String dateFromToQuery = "";
        public static String time = "00:00:00";
        public static String totalString = "0";
        public static int total = 0;
        public static String IPString = "";
		
	private ServerApp(){
		appClosing = false;
               // if (clientSocket.getInetAddress().getHostAddress() != null){
                 //   		ServerAppLogger.GetInstance().ShowMessage("Accepting connection from Client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
               // }
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				serverAppWindow = new ServerAppMainWindow();
				serverAppWindow.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(java.awt.event.WindowEvent windowEvent) {
						OnAppClose();
					}
					
					@Override
					public void windowClosing(java.awt.event.WindowEvent windowEvent) {
						OnAppClose();
					}
				});
				serverAppWindow.setVisible(true);
			}
		});
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				TryStartServerApp();
			}
		}).start();
	}
	
	private void TryStartServerApp(){
		try {
			StartServerApp();
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
			
			if(serverAppWindow != null)
				DisposeDialog(serverAppWindow);
		}
	}
	
	private void StartServerApp() throws Exception {
		// Wait for window setup
		int windowCounter = 0;
		while(serverAppWindow == null){
			Thread.sleep(100);
			if(++windowCounter > 50){
				throw new Exception("Window creation failed");
			}
		}
		
		// Start Derby server
		InetAddress hostInetAddress = InetAddress.getByName(SERVER_APP_LOCALHOST);
		derbyServer = new NetworkServerControl(hostInetAddress, SERVER_APP_DATABASE_SERVER_PORT);
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
		ServerAppLogger.GetInstance().ShowMessage("Derby server started on " + SERVER_APP_LOCALHOST + ":" + SERVER_APP_DATABASE_SERVER_PORT + System.lineSeparator());
		
		// Setup database
		SetupDatabase();
		
		// Setup server sockets
		serverSocket = new ServerSocket(SERVER_APP_LOCALHOST_PORT);
		ServerAppLogger.GetInstance().ShowMessage("Server listener started on port " + SERVER_APP_LOCALHOST_PORT + System.lineSeparator());
                
                serverSocketTotal = new ServerSocket(SERVER_APP_TOTAL_PORT);
		ServerAppLogger.GetInstance().ShowMessage("Server total port listener started on port " + SERVER_APP_TOTAL_PORT + System.lineSeparator());
                
	/*	serverControlSocket = new ServerSocket(SERVER_APP_CONTROL_LOCALHOST_PORT);
		ServerAppLogger.GetInstance().ShowMessage("Server control listener started on port " + SERVER_APP_CONTROL_LOCALHOST_PORT + System.lineSeparator());
                
		udpServerSocket = new DatagramSocket(SERVER_APP_CONTROL_UDP_PORT);
		ServerAppLogger.GetInstance().ShowMessage("Server UDP control listener started on port " + SERVER_APP_CONTROL_UDP_PORT + System.lineSeparator());
                
                serverSparePrvaKasaSocket = new ServerSocket(CLIENT_APP_SPARE_PRVA_KASA_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Spare ribs prva kasa socket listener started on port " + CLIENT_APP_SPARE_PRVA_KASA_PORT + System.lineSeparator());
                
                serverSpareDrugaKasaSocket = new ServerSocket(CLIENT_APP_SPARE_DRUGA_KASA_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Spare ribs druga kasa socket listener started on port " + CLIENT_APP_SPARE_DRUGA_KASA_PORT + System.lineSeparator());
                
                serverPingvinPrvaKasaSocket = new ServerSocket(CLIENT_APP_PINGVIN_PRVA_KASA_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Pingvin prva kasa socket listener started on port " + CLIENT_APP_PINGVIN_PRVA_KASA_PORT + System.lineSeparator());
                
                serverPingvinDrugaKasaSocket = new ServerSocket(CLIENT_APP_PINGVIN_DRUGA_KASA_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Pingvin druga kasa socket listener started on port " + CLIENT_APP_PINGVIN_DRUGA_KASA_PORT + System.lineSeparator());
                
                serverKaptolskaKletSocket = new ServerSocket(CLIENT_APP_TOTAL_KAPTOLSKA_KLET_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Kaptolska klet kasa socket listener started on port " + CLIENT_APP_TOTAL_KAPTOLSKA_KLET_PORT + System.lineSeparator());
                
                serverBreceraSocket = new ServerSocket(CLIENT_APP_BRECERA_PORT);
                ServerAppLogger.GetInstance().ShowMessage("Brecera kasa socket listener started on port " + CLIENT_APP_BRECERA_PORT + System.lineSeparator());*/
                
		
		// Start Client app server
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					
					try {
						Socket clientSocket = serverSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client socket local address is: "
                                                        + clientSocket.getLocalAddress() + "Client server local port is: " + clientSocket.getLocalPort());
						new ServerAppClientAppHost(clientSocket);
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                //FIRST APPROACH
                
                // Start Total Client app server
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotal = serverSocketTotal.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket local address is: "
                                                + clientSocketTotal.getLocalAddress() + "Client TOTAL  server local port is: " + clientSocketTotal.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotal.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotal.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                                
						//new ServerAppWebAppHost(clientSocketTotal);
                                                
                                                //InputStream in = clientSocketTotal.getInputStream();
                                                //String toSee = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
                                                //ServerAppLogger.GetInstance().ShowMessage(toSee));
                                                // Define a regular expression pattern to match the parameter
                                                /*Pattern pattern = Pattern.compile("Parameter: (.+)$"); 
                                                // Create a matcher object
                                                Matcher matcher = pattern.matcher(toSee);

                                                // Check if the pattern is found in the string
                                                if (matcher.find()) {
                                                    // Split the input string based on comma
                                               String[] parts = toSee.split(", ");

                                               // Because it will be send in string format with length 5 or 4, we are making this statement
                                                nameOfPerson = parts[0];
                                                databaseName = parts[parts.length - 1];

                                                //split the parameter according to the data we sent from front-end
                                                switch (parts.length) {
                                                    case 5:
                                                        dateFrom = parts[parts.length - 2];
                                                        dateTo = parts[parts.length - 3];
                                                        bothDates = true;
                                                        break;
                                                    case 4:
                                                        dateFrom = parts[parts.length - 2];
                                                        break;
                                                    default:
                                                        break;
                                                }
                                               
                                               // we will catch nameOfPerson by hardcoded names and point databases to that guy.
                                               if (nameOfPerson == "antonikrolo"){
                                                   
                                               }
                                               else if(nameOfPerson == "mladen" || nameOfPerson == "lovrek"){
                                                    databaseName = "20";
                                               }
                                               
                                               String lovrekTestBaza = "jdbc:derby:C:\\Users\\dell_\\OneDrive\\Desktop\\20-LovrekTest;create=false";
                                               String nevenTestBaza = "jdbc:derby:C:\\ACCOUNTABLE\\Server\\backup\\48-IZLETITEETNOPARKSAVAvlNevenVrani;create=false";
                                               Boolean insideQuery = false;

                                               //starts with 20 - to je Lovrek baza
                                                if (databaseName.startsWith("20")){
                                                    try{
                                                     DriverManager.getConnection(nevenTestBaza);
                                                    }   
                                                        catch(Exception ex){
                                                            ServerAppLogger.GetInstance().ShowMessage(ex.getMessage());
                                                        }
                                                         try (Connection connection = establishDerbyConnection()) {
                                                            if (connection != null) {
                                                                ServerAppLogger.GetInstance().ShowMessage(connection.getSchema());
                                                                    String setSchema = "USER1";
                                                                    connection.setSchema(setSchema);
                                                                    System.out.println("Get schema is now: " + connection.getSchema());
                                                                    
                                                                    String selectQuery = "SELECT * FROM USER1.INVOICES WHERE I_DATE IN (' + dateFrom + ') AND I_NUM = 0";
                                                                    if (bothDates){
                                                                        selectQuery = "SELECT * FROM USER1.INVOICES WHERE (I_DATE >= ('2024-01-02') AND I_TIME >= (" + time + ")) AND (I_DATE <= ('2024-01-04') AND I_NUM = 0";
                                                                    }
                                                                    else if (parts.length == 4) {
                                                                        selectQuery = "SELECT * FROM USER1.INVOICES WHERE I_DATE IN ('2024-01-02') AND I_NUM = 0";
                                                                    }
                                                                    else {
                                                                        selectQuery = "SELECT * FROM USER1.INVOICES";
                                                                    }
                                                                    ServerAppLogger.GetInstance().ShowMessage(connection.getSchema());
                                                                    ServerAppLogger.GetInstance().ShowMessage(selectQuery);
                                                                        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                                                                             ResultSet resultSet = preparedStatement.executeQuery()) {
                                                                            while (resultSet.next()) {
                                                                                ServerAppLogger.GetInstance().ShowMessage("Some row exist.");
                                                                                insideQuery = true;
                                                                                
                                                                                // Process the results
                                                                                totalString += resultSet.getString("FIN_PR");
                                                                                ServerAppLogger.GetInstance().ShowMessage(totalString);
                                                                            }
                                                                        } catch (SQLException ex) { 
                                                                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                                                                        } 
                                                                     }
                                                            }
                                                    }
                                                } else {
                                                    System.out.println("Parameter not found in the string.");
                                                }
                                                
                                                String[] parts = totalString.split("\\.");

                                                // Concatenate all numeric parts
                                                StringBuilder resultBuilder = new StringBuilder();
                                                for (int i = 0; i < parts.length; i++) {
                                                    // Check if the part is numeric (you can add additional validation if needed)
                                                    if (parts[i].matches("\\d+")) {
                                                        // Remove leading zeros and append to the result
                                                        resultBuilder.append(Integer.parseInt(parts[i]));
                                                    }
                                                }
                                                
                                                // Split the result into groups of two digits
                                                String concatenatedResult = resultBuilder.toString();
                                                String[] groups = concatenatedResult.split("(?<=\\G\\d{2})");

                                                // Concatenate the groups
                                                StringBuilder finalResultBuilder = new StringBuilder();
                                                for (String group : groups) {
                                                    finalResultBuilder.append(group).append(" ");
                                                }

                                                // Remove trailing space
                                                String finalResult = finalResultBuilder.toString().trim();
                                                ServerAppLogger.GetInstance().ShowMessage("Final result is: " + finalResult);
                                                
                                                // Split the string into individual tokens
                                                String[] tokens = finalResult.split("\\s+"); */
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotal.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotalSparePrvaKasa = serverSparePrvaKasaSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket spare prva kasa local address is: "
                                                + clientSocketTotalSparePrvaKasa.getLocalAddress() + "Client TOTAL  server local port is: " + clientSocketTotalSparePrvaKasa.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotalSparePrvaKasa.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotalSparePrvaKasa.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                                
					
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotalSparePrvaKasa.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotalSpareRibs = serverSpareDrugaKasaSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket local address is: "
                                                + clientSocketTotalSpareRibs.getLocalAddress() + "Client TOTAL  server local port is: " + clientSocketTotalSpareRibs.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotalSpareRibs.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotalSpareRibs.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee); 
                                                int databaseId = Integer.parseInt(toSee);
                                               
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotalSpareRibs.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotal = serverSocketTotal.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket local address is: " 
                                                + clientSocketTotal.getLocalAddress() + "Client TOTAL  server local port is: " + clientSocketTotal.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotal.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotal.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                                
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotal.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
  /*              new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotalPingvinPrvaKasa = serverPingvinPrvaKasaSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket pingvin prva kasa local address is: "
                                                + clientSocketTotalPingvinPrvaKasa.getLocalAddress() + "Client TOTAL  server local port foor pingvin prva kasa is: " + clientSocketTotalPingvinPrvaKasa.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotalPingvinPrvaKasa.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotalPingvinPrvaKasa.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                                
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotalPingvinPrvaKasa.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotalPingvinDrugaKasa = serverPingvinDrugaKasaSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket pingvin druga kasa local address is: "
                                                + clientSocketTotalPingvinDrugaKasa.getLocalAddress() + "Client TOTAL  server local port for pingvin druga kasa is: " + clientSocketTotalPingvinDrugaKasa.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotalPingvinDrugaKasa.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotalPingvinDrugaKasa.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                               
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotalPingvinDrugaKasa.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketTotalKaptolskaKlet = serverKaptolskaKletSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL kaptolska klet socket local address is: "
                                                + clientSocketTotalKaptolskaKlet.getLocalAddress() + "Client TOTAL kaptolska klet server local port is: " + clientSocketTotalKaptolskaKlet.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketTotalKaptolskaKlet.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketTotalKaptolskaKlet.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                                
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketTotalKaptolskaKlet.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
                
                new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					try {
                                                DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
						Socket clientSocketServerBreceraSocket = serverBreceraSocket.accept();
                                                ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL brecera socket local address is: "
                                                + clientSocketServerBreceraSocket.getLocalAddress() + "Client TOTAL brecera server local port is: " + clientSocketServerBreceraSocket.getLocalPort());
                                                
                                                PrintStream printStream = new PrintStream(clientSocketServerBreceraSocket.getOutputStream());
                                                //clientSocketTotal.getOutputStream().flush();
                                                
                                                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocketServerBreceraSocket.getInputStream()));
                                                String toSee = in.readLine();
                                                ServerAppLogger.GetInstance().ShowMessage(toSee);
                                                int databaseId = Integer.parseInt(toSee);
                                             
                                                
                                                synchronized(ServerApp.databaseTransactionLock){
                                                    try {
                                                            String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
                                                            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
                                                            ps.setInt(1, databaseId);
                                                            ps.setMaxRows(1);
                                                            ResultSet result = ps.executeQuery();
                                                            if (result.next()) {
                                                                    IPString = result.getString(1);
                                                            }
                                                    } catch (Exception ex){
                                                            ServerAppLogger.GetInstance().ShowErrorLog(ex);
                                                    }
                                            }

                                                // Initialize the sum
                                                int sum = 0;
                                                
                                                ServerAppLogger.GetInstance().ShowMessage(IPString);
                                                printStream.println(IPString);
                                                printStream.flush();
                                                ServerAppLogger.GetInstance().ShowMessage("Closing connection");
                                                clientSocketServerBreceraSocket.close();
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start(); */
		
		// Start Control app server
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					
					try {
						Socket controlSocket = serverControlSocket.accept();
						new ServerAppControlAppHost(controlSocket);
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}).start();
		
		// Setup Control app UDP server
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(appClosing)
						break;
					
					try {
						byte[] receiveData = new byte[1024];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						udpServerSocket.receive(receivePacket);
						String receivedString = new String(receivePacket.getData()).trim();
						ServerAppLogger.GetInstance().LogMessage("UDP message recieved: " + receivedString + "; from " + receivePacket.getAddress() + ":" + receivePacket.getPort() + System.lineSeparator());
						if("control app to server app".equals(receivedString)){
							InetAddress IPAddress = receivePacket.getAddress();
							int port = receivePacket.getPort();
							String response = "server app to control app";
							byte[] sendData = response.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
							udpServerSocket.send(sendPacket);
						} else if("client app to server app".equals(receivedString)){
							InetAddress IPAddress = receivePacket.getAddress();
							int port = receivePacket.getPort();
							String response = "server app to client app";
							byte[] sendData = response.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
							udpServerSocket.send(sendPacket);
						}
					} catch (IOException ex) {
						if(!appClosing){
							ServerAppLogger.GetInstance().ShowErrorLog(ex);
						}
					}
				}
			}
		}).start();
                
               // Socket clientSocketTotal = new Socket("127.0.0.1", SERVER_APP_TOTAL_PORT);
               // ServerAppLogger.GetInstance().ShowMessage("Entered client app server. Client TOTAL socket local address is: "
               // + clientSocketTotal.getLocalAddress() + "Client TOTAL  server local port is: " + clientSocketTotal.getLocalPort());
               // new ServerAppClientAppHostTotal(clientSocketTotal);
               // InputStream in = clientSocketTotal.getInputStream();
               // OutputStream out = clientSocketTotal.getOutputStream();
               // client = serverSocket.accept();
               // input = client.getInputStream();
		
		ServerAppUpdater.Init();
		ServerAppNotificationsService.Init();
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

               
        private static Connection establishDerbyConnection() throws SQLException {
            String jdbcUrl = "jdbc:derby:C:\\ACCOUNTABLE\\Server\\backup\\48-IZLETITEETNOPARKSAVAvlNevenVrani;create=false";
            String user = "";
            String password = "";
            String total = "";
            
            return DriverManager.getConnection(jdbcUrl, user, password);
              }
        
        
	
	private void SetupDatabase() throws UnknownHostException, SQLException {
		// TODO remove - used for testing
		//CreateTableIfNoExist("123123", "Drop Table INVOICES");
		//CreateTableIfNoExist("123123", "alter Table OFFICES ADD COLUMN LAST_PING_DATE DATE DEFAULT CURRENT_DATE");
		//CreateTableIfNoExist("123123", "alter Table OFFICES ALTER COLUMN LAST_PING_DATE DROP DEFAULT");
		//CreateTableIfNoExist("123123", "alter Table LOCAL_INVOICES DROP COLUMN S_NAME");
		
		CreateTableIfNoExist("COMPANIES", "CREATE TABLE COMPANIES(ID int, OIB varchar(16), NAME varchar(64), "
				+ "ADDRESS VARCHAR(64), DBNAME varchar(128), AUTO_RENEW INT, MASTER_IP VARCHAR(64), IS_DELETED INT, PRIMARY KEY (ID))");
		CreateColumnIfNoExist("COMPANIES", "MASTER_IP", "ALTER TABLE COMPANIES ADD COLUMN MASTER_IP VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("COMPANIES", "IS_DELETED", "ALTER TABLE COMPANIES ADD COLUMN IS_DELETED INT DEFAULT 0");
		
		CreateTableIfNoExist("OFFICES", "CREATE TABLE OFFICES(ID int, USER_ID int, ADDRESS varchar(64), "
				+ "OFFICE_NUMBER int, OFFICE_TAG VARCHAR(16), OFFICE_NAME varchar(64), LAST_PING_DATE DATE, LAST_PING_TIME TIME, IS_DELETED INT, PRIMARY KEY (ID), "
				+ "UNIQUE(USER_ID, OFFICE_TAG), "
				+ "CONSTRAINT FK_COMPANIES_OFFICES FOREIGN KEY (USER_ID) REFERENCES COMPANIES(ID))");
		CreateColumnIfNoExist("OFFICES", "IS_DELETED", "ALTER TABLE OFFICES ADD COLUMN IS_DELETED INT DEFAULT 0");
		
		CreateTableIfNoExist("CERTIFICATES", "CREATE TABLE CERTIFICATES(ALIAS VARCHAR(32), CERT VARCHAR(4096) FOR BIT DATA, UPLOAD_DATE DATE, UPLOAD_TIME TIME, "
				+ "PRIMARY KEY (ALIAS))");
		
		CreateEmailsListIfNoExist();
		
		String licencesQuery = "CREATE TABLE LICENCES(ID int, OFFICE_ID int, CASH_REGISTER_NUMBER int, TYPE varchar(16), EXPIRATION_DATE date, "
				+ "ACTIVATION_KEY varchar(32), COMPUTER_ID varchar(128), PRIVATE_KEY VARCHAR (2048) FOR BIT DATA, PUBLIC_KEY VARCHAR (2048) FOR BIT DATA, "
				+ "LICENSE_ENCRYPTED VARCHAR (1024) FOR BIT DATA, IS_DELETED int, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_OFFICES_LICENCES FOREIGN KEY (OFFICE_ID) REFERENCES OFFICES(ID))";
		CreateTableIfNoExist("LICENCES", licencesQuery);
	}
	
	private void CreateEmailsListIfNoExist() throws SQLException, UnknownHostException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "NOTIFICATIONS", null);
		if(!rs.next()){
			ServerAppLogger.GetInstance().ShowMessage("Table NOTIFICATIONS does not exist. Creating table NOTIFICATIONS ..." + System.lineSeparator());
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE NOTIFICATIONS(NAME VARCHAR(64), VALUE VARCHAR(64), PRIMARY KEY (NAME))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO NOTIFICATIONS (NAME, VALUE) VALUES (?, ?)");
			String[] toInsertName = new String[] { "email1", "email2", "email3", "email4", "time1", "time2" };
			String[] toInsertValue = new String[] { "", "", "", "", "09:00", "15:00" };
			for(int i = 0; i < toInsertName.length; ++i){
				psInsert.setString(1, toInsertName[i]);
				psInsert.setString(2, toInsertValue[i]);
				psInsert.executeUpdate();
			}
		}
	}
	
	private void CreateTableIfNoExist(String tableName, String tableQuery) throws UnknownHostException, SQLException{
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, tableName, null);
		if(rs.next()){
			//ServerAppLogger.GetInstance().ShowMessage("Table " + tableName + " exists");
		} else {
			ServerAppLogger.GetInstance().ShowMessage("Table " + tableName + " does not exist. Creating table " + tableName + " ..." + System.lineSeparator());
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(tableQuery);
			statement.close();
		}
	}
	private void CreateColumnIfNoExist(String tableName, String columnName, String tableQuery) throws SQLException, UnknownHostException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getColumns(null, null, tableName, columnName);
		if(!rs.next()){
			ServerAppLogger.GetInstance().ShowMessage("Column " + tableName + "." + columnName + " does not exist. Creating column " + tableName + "." + columnName + " ..." + System.lineSeparator());
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(tableQuery);
			statement.close();
		}
	}
	
	public void PrintLog(String message){
		if("".equals(message)){
			serverAppWindow.PrintLog(message);
		} else {
			LocalDateTime localDateTime = LocalDateTime.now();
			String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss"));
			serverAppWindow.PrintLog(timestamp + "  -  " + message);
		}
	}
	
	public void CloseConnection (String licenceId){
		synchronized(hostsListLock){
			for (int i = 0; i < clientAppHosts.size(); ++i){
				clientAppHosts.get(i).OnConnectionClose(licenceId);
			}
		}
	}
        
        public synchronized void AddConnectionCloseHostListener (ServerAppWebAppHost host){
		synchronized(hostsListLockTotal){
			clientAppHostsTotal.add(host);
		}
	}
	
	public synchronized void AddConnectionCloseHostListener (ServerAppClientAppHost host){
		synchronized(hostsListLock){
			clientAppHosts.add(host);
		}
	}
	
	public synchronized void RemoveConnectionCloseHostListener (ServerAppClientAppHost host){
		synchronized(hostsListLock){
			clientAppHosts.remove(host);
		}
	}
        
        	public synchronized void RemoveConnectionCloseHostListener (ServerAppWebAppHost host){
		synchronized(hostsListLockTotal){
			clientAppHosts.remove(host);
		}
	}
	
	public static String GenerateLicenceString(String uniqueId, String licenceDate, int licenceType, int companyId, int cashRegisternumber, String licenceDBName, String oib, int officeNumber, String officeTag, String name, String companyAddress, String officeAddress, int isControlApp){
		StringBuilder licenceString = new StringBuilder();
		licenceString.append("licence");
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(uniqueId);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(licenceDate);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(licenceType);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(companyId);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(licenceDBName);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(oib);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(officeNumber);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(officeTag);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(name);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(companyAddress);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(officeAddress);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(cashRegisternumber);
		licenceString.append(LICENCE_SPLIT_STRING);
		licenceString.append(isControlApp);
		return licenceString.toString();
	}
	
	private static void Init(){
		if(serverApp == null){
			// Check multiple instances
			if(serverAppLock != null){
				ServerAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
			serverAppLock = CheckMultipleInstances(SERVER_APP_LOCK_PORT);
			if(serverAppLock == null || (serverAppLock != null && !serverAppLock.isBound())){
				ServerAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
		
			serverApp = new ServerApp();
		}
	}
	
	public static ServerApp GetInstance(){
		return serverApp;
	}
	
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		/*try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(ServerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(ServerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(ServerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ServerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}*/
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		//</editor-fold>
		
		ServerApp.Init();
	}
	
	public void OnAppClose(){
		appClosing = true;
		
		try {
			if(derbyServer != null){
				derbyServer.shutdown();
				derbyServer = null;
			}
			if(databaseConnection != null){
				databaseConnection.close();
				databaseConnection = null;
			}
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		ServerAppControlAppHost.OnAppClose();
		
		try {
			if(serverAppLock != null){
				serverAppLock.close();
				serverAppLock = null;
			}
		} catch (Exception ex) {
			ServerAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		System.exit(0);
	}
}
