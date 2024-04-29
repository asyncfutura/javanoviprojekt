/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import static hr.adinfo.client.ClientAppUtils.totalInteger;
import hr.adinfo.client.datastructures.LocalServerMasterLocalServerRegisterData;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class MasterLocalServerWebAppHost {
	private final int masterLocalServerClientId;
	
	private Socket clientSocket;
	private ObjectOutputStream oos;
	private BufferedReader ois;
        private Integer passOnce = 0;
        private String finalFloat = "";
        private PrintStream printStream;
	
	MasterLocalServerWebAppHost(Socket clientSocket) {
		this.clientSocket = clientSocket;
		masterLocalServerClientId = ++MasterLocalServerHost.masterLocalServerClientIdCount;
		// TODO break all connections
		if(MasterLocalServerHost.masterLocalServerClientIdCount > 1000000)
			MasterLocalServerHost.masterLocalServerClientIdCount = 0;
		
		try {
                        PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                        //oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        //InputStream in = clientSocket.getInputStream();
                        //ois = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        Float getOutputYesterday = 0f;
                        Float getOutputToday = 0f;
                        boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
                        LocalDate yesterday = LocalDate.now().minusDays(1);
                        LocalDate today = LocalDate.now();

                        // Define a format for the date string
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        // Format yesterday's date as a string
                        String yesterdayString = yesterday.format(formatter);
                        String todayString = today.format(formatter);
                         // Define a format for the date string

                         // Format the current date as a string
                         String queryItems = "";
                         String queryInvoicesYesterday = "";
                         String queryInvoicesToday = "";
                         
                         String payTypesString = "";
                            int[] fiscTypes = new int[]{Values.PAYMENT_METHOD_TYPE_CASH, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CHECK, 
                                    Values.PAYMENT_METHOD_TYPE_OTHER, Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL, Values.PAYMENT_METHOD_TYPE_OTHER_NOT_FISCALIZED};
                            for (int i = 0; i < fiscTypes.length; ++i){
                                    payTypesString += "".equals(payTypesString) ? fiscTypes[i] : ", " + fiscTypes[i];
                            }

                        final JDialog loadingDialog = new LoadingDialog(null, true);
                        
                        //FINPRdatabaseQueryResult[2].getFloat(1) * DISPCTdatabaseQueryResult[2].getFloat(2) / 100f + DISAMTdatabaseQueryResult[2].getFloat(3)
                                
                        if (isProduction){
                            queryInvoicesYesterday = "SELECT FIN_PR, DIS_PCT, DIS_AMT FROM INVOICES WHERE I_DATE = '" + yesterdayString + "' AND PAY_TYPE IN (" + payTypesString + ") AND LENGTH(NOTE) < 1";
                        }
                        else {
                            queryInvoicesYesterday = "SELECT FIN_PR, DIS_PCT, DIS_AMT FROM INVOICES_TEST";
                        }

                        DatabaseQuery databaseQuery = new DatabaseQuery(queryInvoicesYesterday);

                        ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

                        databaseQueryTask.execute();

                        while(!databaseQueryTask.isDone()){
                                try {
                                        Thread.sleep(100);
                                } catch (InterruptedException ex) {}
                        }

                        if(!databaseQueryTask.isDone()){
                                databaseQueryTask.cancel(true);
                        } else {
                                try {
                                        ServerResponse serverResponse = databaseQueryTask.get();
                                        DatabaseQueryResult databaseQueryResult = null;
                                        if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
                                                databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
                                        }
                                        if(databaseQueryResult != null){
                                                while(databaseQueryResult.next()) {
                                                    Float addToIt = databaseQueryResult.getFloat(0);
                                                    Float discount = databaseQueryResult.getFloat(1);
                                                    Float disAmt = databaseQueryResult.getFloat(2); //mora li biti discount > 0
                                                        if (discount > 0){
                                                            ClientAppLogger.GetInstance().LogMessage("Adding number before discount: " + addToIt);
                                                            Float number = (100 - discount) / 100;
                                                            addToIt = addToIt * number;
                                                        }
                                                    ClientAppLogger.GetInstance().LogMessage("Adding number: " + addToIt);
                                                    ClientAppLogger.GetInstance().LogMessage("Adding discount: " + discount);
                                                    getOutputYesterday = getOutputYesterday + addToIt;
                                                  }
                                        }
                                } catch (InterruptedException | ExecutionException ex) {
                                        ClientAppLogger.GetInstance().ShowErrorLog(ex);
                                }
                        }
                        
                        if (isProduction){
                            queryInvoicesToday = "SELECT FIN_PR, DIS_PCT, DIS_AMT FROM INVOICES WHERE I_DATE = '" + todayString + "' AND PAY_TYPE IN (" + payTypesString + ") AND LENGTH(NOTE) < 1";
                        }
                        else {
                            queryInvoicesToday = "SELECT FIN_PR, DIS_PCT FROM INVOICES_TEST";
                        }

                        DatabaseQuery databaseQuery2 = new DatabaseQuery(queryInvoicesToday);

                        ServerQueryTask databaseQueryTask2 = new ServerQueryTask(loadingDialog, databaseQuery2, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

                        databaseQueryTask2.execute();

                        while(!databaseQueryTask2.isDone()){
                                try {
                                        Thread.sleep(100);
                                } catch (InterruptedException ex) {}
                        }

                        if(!databaseQueryTask2.isDone()){
                                databaseQueryTask2.cancel(true);
                        } else {
                                try {
                                        ServerResponse serverResponse = databaseQueryTask2.get();
                                        DatabaseQueryResult databaseQueryResult2 = null;
                                        if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
                                                databaseQueryResult2 = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
                                        }
                                        if(databaseQueryResult2 != null){
                                               while(databaseQueryResult2.next()) {
                                                    Float addToIt2 = databaseQueryResult2.getFloat(0);
                                                    Float discount = databaseQueryResult2.getFloat(1);
                                                        if (discount > 0){
                                                            ClientAppLogger.GetInstance().LogMessage("Adding number before discount: " + addToIt2);
                                                            Float number = (100 - discount) / 100;
                                                            addToIt2 = addToIt2 * number;
                                                        }
                                                    ClientAppLogger.GetInstance().LogMessage("Adding number: " + addToIt2);
                                                    ClientAppLogger.GetInstance().LogMessage("Adding discount: " + discount);
                                                    getOutputToday = getOutputToday + addToIt2;
                                                  }
                                        }
                                } catch (InterruptedException | ExecutionException ex) {
                                        ClientAppLogger.GetInstance().ShowErrorLog(ex);
                                }
                        }
                        
                        
                        finalFloat = getOutputYesterday.toString()+","+getOutputToday.toString();
                        ClientAppLogger.GetInstance().LogMessage("Final float is: " + finalFloat);
                        printStream.println(getOutputYesterday.toString()+","+getOutputToday.toString());
                        printStream.flush();
                        //oos.writeObject(getOutput);
                        //oos.flush();      
                        clientSocket.close();
                        
			ClientAppLogger.GetInstance().LogMessage("MasterLocalServerHost connected");
		} catch (IOException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
			ClientAppLogger.GetInstance().LogError(ex);
		}
	}
	
	public void CloseSocket(){
		Utils.CloseSocket(clientSocket, oos, null);
		clientSocket = null;
		oos = null;
		try { ois.close(); } catch (Exception ex1) {}
                ois = null;
	}

                
	public static void OnAppClose(){
		
	}
}
