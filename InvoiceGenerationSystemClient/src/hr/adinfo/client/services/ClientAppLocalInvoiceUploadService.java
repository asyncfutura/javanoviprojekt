/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.LocalServer;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryInsertLocalInvoice;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppLocalInvoiceUploadService {
	
	private static final int SERVICE_LOOP_DELAY_SECONDS = 10;
	
	private static boolean repeatImmediate;	
	
	public static void Init(){
		repeatImmediate = false;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					if(LocalServer.GetInstance() != null && LocalServer.GetInstance().isSyncedWithMaster){
						CheckLocalInvoices(true);
						CheckLocalInvoices(false);
					} else {
						repeatImmediate = true;
					}
					//System.out.println("repeatImmediate " + repeatImmediate);
					try {
						if(repeatImmediate){
							repeatImmediate = false;
							Thread.sleep(100);
						} else {
							Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
						}
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckLocalInvoices(boolean isProduction){
		boolean repeatImmediateFlag = false;
		
		synchronized(Fiscalization.fiscalizationInvoiceUploadLock){
			if(Fiscalization.IsFiscalisationInProgress())
				return;
			
			MultiDatabaseQueryInsertLocalInvoice multiDatabaseQueryInsert = null;
			String queryInvoicesInsert = "INSERT INTO INVOICES (ID, "
					+ "O_NUM, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, "
					+ "C_ID, DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String queryItemsInsert = "INSERT INTO INVOICE_ITEMS (ID, IN_ID, IT_TYPE, "
				+ "IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, PACK_REF) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String queryMaterialsInsert = "INSERT INTO INVOICE_MATERIALS (ID, IN_ID, ART_ID, MAT_ID, AMT, NORM) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			String queryMaterialAmounts = "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ? "
					+ "WHERE AMOUNT_YEAR = ? AND MATERIAL_ID = ? AND OFFICE_NUMBER = ?";
			String queryTradingGoodsAmounts = "UPDATE TRADING_GOODS_AMOUNTS SET AMOUNT = AMOUNT - ? "
					+ "WHERE AMOUNT_YEAR = ? AND TRADING_GOODS_ID = ? AND OFFICE_NUMBER = ?";
			String queryUpdateLocalInvoice = "UPDATE LOCAL_INVOICES SET IS_DELETED = 1 "
					+ "WHERE ID = ? AND O_NUM = ?";
			String queryUpdateLocalInvoiceMaterials = "UPDATE LOCAL_INVOICE_MATERIALS SET IS_DELETED = 1 "
					+ "WHERE ID = ? AND IN_ID = ? AND (SELECT O_NUM FROM LOCAL_INVOICES WHERE LOCAL_INVOICES.ID = ?) = ? ";
			if(!isProduction){
				queryInvoicesInsert = queryInvoicesInsert.replace("INVOICES", "INVOICES_TEST");
				queryItemsInsert = queryItemsInsert.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
				queryMaterialsInsert = queryMaterialsInsert.replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
				queryUpdateLocalInvoice = queryUpdateLocalInvoice.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
				queryUpdateLocalInvoiceMaterials = queryUpdateLocalInvoiceMaterials.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST").replace("LOCAL_INVOICE_MATERIALS", "LOCAL_INVOICE_MATERIALS_TEST");
			}
			ArrayList<Integer> invoiceIdList = new ArrayList<>();
			ArrayList<Integer> invoiceQueryIdList = new ArrayList<>();
			ArrayList<Integer> invoiceOfficeIdList = new ArrayList<>();
			ArrayList<Integer> invoiceYearList = new ArrayList<>();
			ArrayList<Integer> invoicePayTypeList = new ArrayList<>();
			
			String queryInvoices = "SELECT ID, O_NUM, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
					+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
					+ "FROM LOCAL_INVOICES "
					+ "WHERE IS_DELETED = 0 "
					+ "ORDER BY ID "
					+ "FETCH FIRST ROW ONLY";
			String queryItems = "SELECT ID, IN_ID, IT_TYPE, IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, PACK_REF "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "WHERE IN_ID IN (SELECT LOCAL_INVOICES.ID FROM LOCAL_INVOICES WHERE IS_DELETED = 0 ORDER BY LOCAL_INVOICES.ID FETCH FIRST ROW ONLY) "
					+ "ORDER BY ID";
			String queryMaterials = "SELECT ID, IN_ID, ART_ID, MAT_ID, AMT, NORM "
					+ "FROM LOCAL_INVOICE_MATERIALS "
					+ "WHERE IS_DELETED = 0 "
					+ "AND IN_ID IN (SELECT LOCAL_INVOICES.ID FROM LOCAL_INVOICES WHERE IS_DELETED = 0 ORDER BY LOCAL_INVOICES.ID FETCH FIRST ROW ONLY) "
					+ "ORDER BY ID";
			String queryTradingGoodsItems = "SELECT IN_ID, IT_ID, AMT "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "WHERE IN_ID IN (SELECT LOCAL_INVOICES.ID FROM LOCAL_INVOICES WHERE IS_DELETED = 0 ORDER BY LOCAL_INVOICES.ID FETCH FIRST ROW ONLY) "
					+ "AND IT_TYPE = ? "
					+ "ORDER BY ID";
			if(!isProduction){
				queryInvoices = queryInvoices.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
				queryItems = queryItems.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST").replace("LOCAL_INVOICE_ITEMS", "LOCAL_INVOICE_ITEMS_TEST");
				queryMaterials = queryMaterials.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST").replace("LOCAL_INVOICE_MATERIALS", "LOCAL_INVOICE_MATERIALS_TEST");
				queryTradingGoodsItems = queryTradingGoodsItems.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST").replace("LOCAL_INVOICE_ITEMS", "LOCAL_INVOICE_ITEMS_TEST");
			}
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);

				MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);
				multiDatabaseQuery.SetQuery(0, queryInvoices);
				multiDatabaseQuery.SetQuery(1, queryItems);
				multiDatabaseQuery.SetQuery(2, queryMaterials);
				multiDatabaseQuery.SetQuery(3, queryTradingGoodsItems);
				multiDatabaseQuery.AddParam(3, 1, Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS);
				ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

				databaseQueryTask.execute();
				
				//loadingDialog.setVisible(true);
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
						DatabaseQueryResult[] databaseQueryResults = null;
						if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
							databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
						}
						if(databaseQueryResults != null && databaseQueryResults[0].getSize() > 0){
							//if(databaseQueryResults[0].getSize() > 8){
								repeatImmediateFlag = true;
							//}
							
							int totalQuerySize = 2 * databaseQueryResults[0].getSize() 
									+ databaseQueryResults[1].getSize() 
									+ 3 * databaseQueryResults[2].getSize() 
									+ databaseQueryResults[3].getSize();
							if(!isProduction){
								totalQuerySize = 2 * databaseQueryResults[0].getSize() 
									+ databaseQueryResults[1].getSize() 
									+ 2 * databaseQueryResults[2].getSize();
							}
							multiDatabaseQueryInsert = new MultiDatabaseQueryInsertLocalInvoice(totalQuerySize);
							int queryCount = 0;

							DatabaseQueryResult databaseQueryResult = databaseQueryResults[0];
							while (databaseQueryResult.next()) {
								Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(5));
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(date);
								
								multiDatabaseQueryInsert.oNum = databaseQueryResult.getInt(1);
								multiDatabaseQueryInsert.crNum = databaseQueryResult.getInt(2);
								multiDatabaseQueryInsert.iNum  = databaseQueryResult.getInt(3);
								multiDatabaseQueryInsert.specNum = databaseQueryResult.getInt(4);
								multiDatabaseQueryInsert.payType = databaseQueryResult.getInt(10);
								multiDatabaseQueryInsert.iYear  = calendar.get(Calendar.YEAR);
								multiDatabaseQueryInsert.isTest = !isProduction;
								
								/*System.out.println(multiDatabaseQueryInsert.oNum + " " + multiDatabaseQueryInsert.crNum + " " + multiDatabaseQueryInsert.iNum 
								+ " " + multiDatabaseQueryInsert.specNum + " " + multiDatabaseQueryInsert.payType + " " + multiDatabaseQueryInsert.isTest);*/
								
								invoiceIdList.add(databaseQueryResult.getInt(0));
								invoiceQueryIdList.add(queryCount);
								invoiceOfficeIdList.add(databaseQueryResult.getInt(1));
								invoiceYearList.add(calendar.get(Calendar.YEAR));
								invoicePayTypeList.add(databaseQueryResult.getInt(10));
								
								multiDatabaseQueryInsert.SetQuery(queryCount, queryInvoicesInsert);
								multiDatabaseQueryInsert.SetAutoIncrementParam(queryCount, 1, "ID", isProduction ? "INVOICES" : "INVOICES_TEST");
								multiDatabaseQueryInsert.AddParam(queryCount, 2, databaseQueryResult.getString(1));
								multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(2));
								multiDatabaseQueryInsert.AddParam(queryCount, 4, databaseQueryResult.getString(3));
								multiDatabaseQueryInsert.AddParam(queryCount, 5, databaseQueryResult.getString(4));
								multiDatabaseQueryInsert.AddParam(queryCount, 6, databaseQueryResult.getString(5));
								multiDatabaseQueryInsert.AddParam(queryCount, 7, databaseQueryResult.getString(6));
								multiDatabaseQueryInsert.AddParam(queryCount, 8, databaseQueryResult.getString(7));
								multiDatabaseQueryInsert.AddParam(queryCount, 9, databaseQueryResult.getString(8));
								multiDatabaseQueryInsert.AddParam(queryCount, 10, databaseQueryResult.getString(9));
								multiDatabaseQueryInsert.AddParam(queryCount, 11, databaseQueryResult.getString(10));
								multiDatabaseQueryInsert.AddParam(queryCount, 12, databaseQueryResult.getString(11));
								multiDatabaseQueryInsert.AddParam(queryCount, 13, databaseQueryResult.getString(12));
								multiDatabaseQueryInsert.AddParam(queryCount, 14, databaseQueryResult.getString(13));
								multiDatabaseQueryInsert.AddParam(queryCount, 15, databaseQueryResult.getString(14));
								multiDatabaseQueryInsert.AddParam(queryCount, 16, databaseQueryResult.getString(15));
								multiDatabaseQueryInsert.AddParam(queryCount, 17, databaseQueryResult.getString(16));
								multiDatabaseQueryInsert.AddParam(queryCount, 18, databaseQueryResult.getString(17));
								multiDatabaseQueryInsert.AddParam(queryCount, 19, databaseQueryResult.getString(18));
								multiDatabaseQueryInsert.AddParam(queryCount, 20, databaseQueryResult.getString(19));
								multiDatabaseQueryInsert.AddParam(queryCount, 21, databaseQueryResult.getString(20));
								multiDatabaseQueryInsert.AddParam(queryCount, 22, databaseQueryResult.getString(21));
								multiDatabaseQueryInsert.AddParam(queryCount, 23, databaseQueryResult.getString(22));
								multiDatabaseQueryInsert.AddParam(queryCount, 24, databaseQueryResult.getString(23));
								multiDatabaseQueryInsert.AddParam(queryCount, 25, databaseQueryResult.getString(24));
								multiDatabaseQueryInsert.AddParam(queryCount, 26, databaseQueryResult.getString(25));
								multiDatabaseQueryInsert.AddParam(queryCount, 27, databaseQueryResult.getString(26));
								++queryCount;

								multiDatabaseQueryInsert.SetQuery(queryCount, queryUpdateLocalInvoice);
								multiDatabaseQueryInsert.AddParam(queryCount, 1, databaseQueryResult.getString(0));
								multiDatabaseQueryInsert.AddParam(queryCount, 2, databaseQueryResult.getString(1));
								++queryCount;
							}

							databaseQueryResult = databaseQueryResults[1];
							while (databaseQueryResult.next()) {
								int invoiceListIndex = ClientAppUtils.ArrayIndexOf(invoiceIdList, databaseQueryResult.getInt(1));
								multiDatabaseQueryInsert.SetQuery(queryCount, queryItemsInsert);
								multiDatabaseQueryInsert.SetAutoIncrementParam(queryCount, 1, "ID", isProduction ? "INVOICE_ITEMS" : "INVOICE_ITEMS_TEST");
								multiDatabaseQueryInsert.AddAutoGeneratedParam(queryCount, 2, invoiceQueryIdList.get(invoiceListIndex));
								multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(2));
								multiDatabaseQueryInsert.AddParam(queryCount, 4, databaseQueryResult.getString(3));
								multiDatabaseQueryInsert.AddParam(queryCount, 5, databaseQueryResult.getString(4));
								multiDatabaseQueryInsert.AddParam(queryCount, 6, databaseQueryResult.getString(5));
								multiDatabaseQueryInsert.AddParam(queryCount, 7, databaseQueryResult.getString(6));
								multiDatabaseQueryInsert.AddParam(queryCount, 8, databaseQueryResult.getString(7));
								multiDatabaseQueryInsert.AddParam(queryCount, 9, databaseQueryResult.getString(8));
								multiDatabaseQueryInsert.AddParam(queryCount, 10, databaseQueryResult.getString(9));
								multiDatabaseQueryInsert.AddParam(queryCount, 11, databaseQueryResult.getString(10));
								multiDatabaseQueryInsert.AddParam(queryCount, 12, databaseQueryResult.getString(11));
								++queryCount;
							}

							databaseQueryResult = databaseQueryResults[2];
							while (databaseQueryResult.next()) {
								int invoiceListIndex = ClientAppUtils.ArrayIndexOf(invoiceIdList, databaseQueryResult.getInt(1));
								multiDatabaseQueryInsert.SetQuery(queryCount, queryMaterialsInsert);
								multiDatabaseQueryInsert.SetAutoIncrementParam(queryCount, 1, "ID", isProduction ? "INVOICE_MATERIALS" : "INVOICE_MATERIALS_TEST");
								multiDatabaseQueryInsert.AddAutoGeneratedParam(queryCount, 2, invoiceQueryIdList.get(invoiceListIndex));
								multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(2));
								multiDatabaseQueryInsert.AddParam(queryCount, 4, databaseQueryResult.getString(3));
								multiDatabaseQueryInsert.AddParam(queryCount, 5, databaseQueryResult.getString(4));
								multiDatabaseQueryInsert.AddParam(queryCount, 6, databaseQueryResult.getString(5));
								++queryCount;
								
								if(isProduction){
									float amountToAdd = 0f;
									if(invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP && invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_OFFER && invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
										amountToAdd = databaseQueryResult.getFloat(4) * databaseQueryResult.getFloat(5);
									}
									multiDatabaseQueryInsert.SetQuery(queryCount, queryMaterialAmounts);
									multiDatabaseQueryInsert.AddParam(queryCount, 1, amountToAdd);
									multiDatabaseQueryInsert.AddParam(queryCount, 2, invoiceYearList.get(invoiceListIndex));
									multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(3));
									multiDatabaseQueryInsert.AddParam(queryCount, 4, invoiceOfficeIdList.get(invoiceListIndex));
									++queryCount;
								}
								
								multiDatabaseQueryInsert.SetQuery(queryCount, queryUpdateLocalInvoiceMaterials);
								multiDatabaseQueryInsert.AddParam(queryCount, 1, databaseQueryResult.getString(0));
								multiDatabaseQueryInsert.AddParam(queryCount, 2, databaseQueryResult.getString(1));
								multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(1));
								multiDatabaseQueryInsert.AddParam(queryCount, 4, invoiceOfficeIdList.get(invoiceListIndex));
								++queryCount;
							}
							
							if(isProduction){
								databaseQueryResult = databaseQueryResults[3];
								while (databaseQueryResult.next()) {
									int invoiceListIndex = ClientAppUtils.ArrayIndexOf(invoiceIdList, databaseQueryResult.getInt(0));
									float amountToAdd = 0f;
									if(invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP && invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_OFFER && invoicePayTypeList.get(invoiceListIndex) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
										amountToAdd = databaseQueryResult.getFloat(2);
									}
									multiDatabaseQueryInsert.SetQuery(queryCount, queryTradingGoodsAmounts);
									multiDatabaseQueryInsert.AddParam(queryCount, 1, amountToAdd);
									multiDatabaseQueryInsert.AddParam(queryCount, 2, invoiceYearList.get(invoiceListIndex));
									multiDatabaseQueryInsert.AddParam(queryCount, 3, databaseQueryResult.getString(1));
									multiDatabaseQueryInsert.AddParam(queryCount, 4, invoiceOfficeIdList.get(invoiceListIndex));
									++queryCount;
								}
							}
						}
					} catch (InterruptedException | ExecutionException | ParseException ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
			
			if(Fiscalization.IsFiscalisationInProgress())
				return;
			
			if(multiDatabaseQueryInsert != null){
				final JDialog loadingDialog = new LoadingDialog(null, true);
				ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQueryInsert, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance(), false);
				
				databaseQueryTask.execute();
				
				//loadingDialog.setVisible(true);
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
						DatabaseQueryResult[] databaseQueryResult = null;
						if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
							databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
						}
						if(databaseQueryResult != null){
							if(repeatImmediateFlag){
								repeatImmediate = true;
							}
						}
					} catch (InterruptedException | ExecutionException ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}
	}
}
