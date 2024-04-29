/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server;

import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.MasterServerIPQuery;
import hr.adinfo.utils.communication.MasterServerIPResponse;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.communication.ServerResponseList;
import hr.adinfo.utils.communication.misc.LocalServerServerAppPingData;
import hr.adinfo.utils.database.DatabaseDiff;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.KeyGeneratorRSA;
import hr.adinfo.utils.licence.LicenceQuery;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Cipher;

/**
 *
 * @author Matej
 */
public class ServerAppWebAppHost {
	private static Connection databaseConnection;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Object oosLock = new Object();
	
	// Diff sync
	private Connection syncDatabaseConnection;
	private boolean isMasterLocalServer;
	private boolean isLocalServer;
	private int companyId;
	private String companyName;
	private ServerResponseList serverDiffsResponseList = new ServerResponseList();
	private boolean diffTableExist;
	private final ServerAppWebAppHost thisHost;
	private String debugDBName = "";
	private int debugCompanyId = -1;
	private int debugOfficeId = -1;
        private Socket socket;
        private DataInputStream input = null;
        private DataOutputStream out = null;
        private ServerSocket serverSocketTotal = null;
        private Socket client;
	
	private int licenceId = -1;
	
	private static final int DIFF_SYNC_LOOP_DELAY_SECONDS = 10;
	
	ServerAppWebAppHost(Socket clientSocket) {
            ServerAppLogger.GetInstance().ShowMessage("Accepting connection from Client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
            thisHost = this;
            ServerApp.GetInstance().AddConnectionCloseHostListener(thisHost);
            this.socket = clientSocket;

            try {
                    CreateStreams();
                    //ReadQueries();
            } catch (Exception ex) {
                    ServerAppLogger.GetInstance().ShowErrorLog(ex);
            }
        }
        
        private void CreateStreams() throws Exception {
		oos = new ObjectOutputStream(socket.getOutputStream());
                //oos.writeUTF(clientSocket.getInetAddress().getHostAddress());
                //oos.flush();
		//ois = new ObjectInputStream(socket.getInputStream());
	}
        
        private void ReadQueries(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						Object inputObject = ois.readObject();
						if(inputObject == null){
							ServerAppLogger.GetInstance().ShowMessage("Null message recieved " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						} else if (inputObject instanceof MasterServerIPQuery){
							MasterServerIPResponse masterServerIPResponse = new	MasterServerIPResponse((MasterServerIPQuery) inputObject);
							if(isLocalServer){
								masterServerIPResponse.masterIp = GetMasterIp();
								ServerAppLogger.GetInstance().ShowMessage("MasterServerIPQuery in WEB APP TOTAL success! " + companyId + " " + companyName + " masterIp:" + masterServerIPResponse.masterIp + " " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
							} else {
								masterServerIPResponse.masterIp = "";
								ServerAppLogger.GetInstance().ShowMessage("MasterServerIPQuery failed! WEB APP TOTAL Server ping not recieved yet " + "c " + debugCompanyId + ", o " + debugOfficeId + ", db " + debugDBName + ", ip " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
							}
							synchronized (oosLock){
								oos.writeObject(masterServerIPResponse);
								oos.flush();
							}
						}
					} catch (Exception ex) {
						ServerAppLogger.GetInstance().LogError(ex);
						if (clientSocket != null){
							ServerAppLogger.GetInstance().ShowMessage("Disconnected Client: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + System.lineSeparator());
						} 
						ServerApp.GetInstance().RemoveConnectionCloseHostListener(thisHost);
						
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
                
        private String GetMasterIp() {
		synchronized(ServerApp.databaseTransactionLock){
			try {
				String query = "SELECT MASTER_IP FROM COMPANIES WHERE ID = ?";
				PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
				ps.setInt(1, companyId);
				ps.setMaxRows(1);
				ResultSet result = ps.executeQuery();
				if (result.next()) {
					return result.getString(1);
				}
			} catch (Exception ex){
				ServerAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return "";
	}
        
        private Connection getDatabaseConnection() throws UnknownHostException, SQLException {
		if(databaseConnection != null && (databaseConnection.isClosed() || !databaseConnection.isValid(1))){
			databaseConnection = null;
		}
		
		if(databaseConnection == null){
            databaseConnection = Utils.getDatabaseConnection(Values.SERVER_APP_LOCALHOST, Values.SERVER_APP_DATABASE_SERVER_PORT, Values.SERVER_APP_DATABASE_NAME);
		}
		
		return databaseConnection;
	}
}
