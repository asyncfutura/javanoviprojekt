/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.datastructures.OldDatabaseData;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Matej
 */
public class ClientAppOldDatabaseImport {
	private static final String SEPARATOR = "%%%";
	
	public static void ImportDatabase(){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Učitaj datoteku");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".txt", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
		File selectedFile = null;
        if(returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
			selectedFile = chooser.getSelectedFile();
        }
		if(selectedFile == null)
			return;
		
		ArrayList<OldDatabaseData> databaseDataList = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split(SEPARATOR);
				if(cols.length == 0)
					continue;
				
				OldDatabaseData databaseDataElement = new OldDatabaseData();
				databaseDataElement.tableName = cols[0].trim();
				
				for (int i = 1; i < cols.length; ++i){
					databaseDataElement.params.add(cols[i].trim());
				}
				
				databaseDataList.add(databaseDataElement);
			}
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		if(!databaseDataList.isEmpty()){
			SaveDatabaseData(databaseDataList);
		}
	}
	
	private static void SaveDatabaseData(ArrayList<OldDatabaseData> databaseDataList){
		// First receipt - delete old one and create new one
		GenerateFirstReceipt();
		
		// Insert queries
		String insertArticles = "INSERT INTO ARTICLES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, MIN_AMOUNT, CONSUMPTION_TAX_ID, "
				+ "PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0 FROM STAFF " 
				+ "WHERE NOT EXISTS (SELECT ID FROM ARTICLES WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateArticles = "UPDATE ARTICLES SET NAME = ?, CATEGORY_ID = ?, MEASURING_UNIT_ID = ?, TAX_RATE_ID = ?, MIN_AMOUNT = ?, CONSUMPTION_TAX_ID = ?, "
				+ "PRICE = ?, EVENT_PRICE = ?, CUSTOM_ID = ?, IS_ACTIVE = 1, IS_DELETED = 0 "
				+ "WHERE ID = ?";
		
		String insertMaterials = "INSERT INTO MATERIALS (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, MIN_AMOUNT, LAST_PRICE, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM MATERIALS WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateMaterials = "UPDATE MATERIALS SET NAME = ?, CATEGORY_ID = ?, MEASURING_UNIT_ID = ?, MIN_AMOUNT = ?, LAST_PRICE = ?, IS_DELETED = 0 "
				+ "WHERE ID = ?";
		
		String insertNormatives = "INSERT INTO NORMATIVES (ID, ARTICLE_ID, MATERIAL_ID, AMOUNT, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM NORMATIVES WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateNormatives = "UPDATE NORMATIVES SET ARTICLE_ID = ?, MATERIAL_ID = ?, AMOUNT = ?, IS_DELETED = 0 "
				+ "WHERE ID = ?";

		String insertTradingGoods = "INSERT INTO TRADING_GOODS (ID, NAME, CATEGORY_ID, TAX_RATE_ID, MIN_AMOUNT, PACKAGING_REFUND_ID, "
				+ "PRICE, LAST_PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM TRADING_GOODS WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateTradingGoods = "UPDATE TRADING_GOODS SET NAME = ?, CATEGORY_ID = ?, TAX_RATE_ID = ?, MIN_AMOUNT = ?, PACKAGING_REFUND_ID = ?, "
				+ "PRICE = ?, LAST_PRICE = ?, EVENT_PRICE = ?, CUSTOM_ID = ?, IS_ACTIVE = 1, IS_DELETED = 0 "
				+ "WHERE ID = ?";
		
		String insertServices = "INSERT INTO SERVICES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, PRICE, EVENT_PRICE, "
				+ "CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, ?, ?, 1, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM SERVICES WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateServices = "UPDATE SERVICES SET NAME = ?, CATEGORY_ID = ?, MEASURING_UNIT_ID = ?, TAX_RATE_ID = ?, PRICE = ?, EVENT_PRICE = ?, "
				+ "CUSTOM_ID = ?, IS_ACTIVE = 1, IS_DELETED = 0 "
				+ "WHERE ID = ?";
		
		String insertClients = "INSERT INTO CLIENTS (ID, NAME, OIB, STREET, HOUSE_NUM, TOWN, "
				+ "POSTAL_CODE, COUNTRY, PAYMENT_DELAY, BIRTHDAY, MOBILE_NUM, TELEPHONE_NUM, WEBSITE, EMAIL, "
				+ "NOTES, TRAFFIC, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM CLIENTS WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateClients = "UPDATE CLIENTS SET NAME = ?, OIB = ?, STREET = ?, HOUSE_NUM = ?, TOWN = ?, "
				+ "POSTAL_CODE = ?, COUNTRY = ?, PAYMENT_DELAY = ?, BIRTHDAY = ?, MOBILE_NUM = ?, TELEPHONE_NUM = ?, WEBSITE = ?, "
				+ "EMAIL = ?, NOTES = ?, TRAFFIC = ?, IS_DELETED = 0 "
				+ "WHERE ID = ?";
		
		String insertSuppliers = "INSERT INTO SUPPLIERS (ID, NAME, OIB, STREET, HOUSE_NUM, TOWN, "
				+ "POSTAL_CODE, COUNTRY, CONTACT_PERSON, MOBILE_NUM, TELEPHONE_NUM, WEBSITE, EMAIL, "
				+ "NOTES, IS_DELETED) "
				+ "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0 FROM STAFF "
				+ "WHERE NOT EXISTS (SELECT ID FROM SUPPLIERS WHERE ID = ?) FETCH FIRST ROW ONLY";
		String updateSuppliers = "UPDATE SUPPLIERS SET NAME = ?, OIB = ?, STREET = ?, HOUSE_NUM = ?, TOWN = ?, "
				+ "POSTAL_CODE = ?, COUNTRY = ?, CONTACT_PERSON = ?, MOBILE_NUM = ?, TELEPHONE_NUM = ?, WEBSITE = ?, "
				+ "EMAIL = ?, NOTES = ?, IS_DELETED = 0 "
				+ "WHERE ID = ?";

		// Insert data
		InsertData(databaseDataList, "ARTICLES", insertArticles, updateArticles);
		InsertData(databaseDataList, "MATERIALS", insertMaterials, updateMaterials);
		InsertData(databaseDataList, "NORMATIVES", insertNormatives, updateNormatives);
		InsertData(databaseDataList, "TRADING_GOODS", insertTradingGoods, updateTradingGoods);
		InsertData(databaseDataList, "SERVICES", insertServices, updateServices);
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(Licence.GetOfficeNumber());
		ClientAppUtils.CreateAllTradingGoodsAmountsIfNoExist(Licence.GetOfficeNumber());
		InsertData(databaseDataList, "CLIENTS", insertClients, updateClients);
		InsertData(databaseDataList, "SUPPLIERS", insertSuppliers, updateSuppliers);
		InsertReceiptMaterials(databaseDataList);
	}
	
	private static void InsertReceiptMaterials(ArrayList<OldDatabaseData> databaseDataList){
		String subqueryReceiptId = "SELECT RECEIPTS.ID FROM RECEIPTS "
				+ "WHERE RECEIPTS.OFFICE_NUMBER = ? AND YEAR(RECEIPTS.RECEIPT_DATE) = ? AND RECEIPT_NUMBER = 1";
		String queryInsert = "INSERT INTO RECEIPT_MATERIALS (ID, RECEIPT_ID, MATERIAL_ID, AMOUNT, PRICE, RABATE, IS_DELETED) "
					+ "VALUES (?, (" + subqueryReceiptId + "), ?, ?, ?, ?, 0)";
		String queryUpdateMaterialPlus = "UPDATE MATERIAL_AMOUNTS "
					+ "SET AMOUNT = AMOUNT + ? "
					+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
					+ "AND AMOUNT_YEAR = ?";
		
		// Calculate query size
		int querySize = 0;
		for (int i = 0; i < databaseDataList.size(); ++i){
			if("RECEIPT_MATERIALS".equals(databaseDataList.get(i).tableName)){
				++querySize;
			}
		}
		
		if(querySize == 0)
			return;
		
		// Generate query
		final JDialog loadingDialog = new LoadingDialog(null, true);
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2 * querySize);
		
		querySize = 0;
		for (int i = 0; i < databaseDataList.size(); ++i){
			if(!"RECEIPT_MATERIALS".equals(databaseDataList.get(i).tableName))
				continue;
			
			multiDatabaseQuery.SetQuery(querySize, queryInsert);
			multiDatabaseQuery.SetAutoIncrementParam(querySize, 1, "ID", "RECEIPT_MATERIALS");
			multiDatabaseQuery.AddParam(querySize, 2, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(querySize, 3, ClientAppSettings.currentYear);
			multiDatabaseQuery.AddParam(querySize, 4, databaseDataList.get(i).params.get(0));
			multiDatabaseQuery.AddParam(querySize, 5, databaseDataList.get(i).params.get(1));
			multiDatabaseQuery.AddParam(querySize, 6, databaseDataList.get(i).params.get(2));
			multiDatabaseQuery.AddParam(querySize, 7, databaseDataList.get(i).params.get(3));
			++querySize;
			
			multiDatabaseQuery.SetQuery(querySize, queryUpdateMaterialPlus);
			multiDatabaseQuery.AddParam(querySize, 1, databaseDataList.get(i).params.get(1));
			multiDatabaseQuery.AddParam(querySize, 2, databaseDataList.get(i).params.get(0));
			multiDatabaseQuery.AddParam(querySize, 3, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(querySize, 4, ClientAppSettings.currentYear);
			++querySize;
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResults = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResults != null){
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private static void InsertData(ArrayList<OldDatabaseData> databaseDataList, String tableName, String insertQuery, String updateQuery){
		// Calculate query size
		int querySize = 0;
		for (int i = 0; i < databaseDataList.size(); ++i){
			if(!tableName.equals(databaseDataList.get(i).tableName)){
				continue;
			}
			
			++querySize;
		}
		
		if(querySize == 0)
			return;
		
		// Generate query
		final JDialog loadingDialog = new LoadingDialog(null, true);
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2 * querySize);
		
		querySize = 0;
		for (int i = 0; i < databaseDataList.size(); ++i){
			if(!tableName.equals(databaseDataList.get(i).tableName)){
				continue;
			}
			
			multiDatabaseQuery.SetQuery(querySize, insertQuery);
			for (int j = 0; j < databaseDataList.get(i).params.size(); ++j){
				multiDatabaseQuery.AddParam(querySize, j + 1, databaseDataList.get(i).params.get(j));
			}
			multiDatabaseQuery.AddParam(querySize, databaseDataList.get(i).params.size() + 1, databaseDataList.get(i).params.get(0));
			++querySize;
			
			multiDatabaseQuery.SetQuery(querySize, updateQuery);
			for (int j = 1; j < databaseDataList.get(i).params.size(); ++j){
				multiDatabaseQuery.AddParam(querySize, j, databaseDataList.get(i).params.get(j));
			}
			multiDatabaseQuery.AddParam(querySize, databaseDataList.get(i).params.size(), databaseDataList.get(i).params.get(0));
			++querySize;
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResults = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResults != null){
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private static void GenerateFirstReceipt(){
		int firstReceiptId = -1;
		int currentYear = ClientAppSettings.currentYear;
		
		// Get first receipt id if exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			String query = "SELECT RECEIPTS.ID FROM RECEIPTS "
					+ "WHERE RECEIPTS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? AND YEAR(RECEIPTS.RECEIPT_DATE) = ? AND RECEIPT_NUMBER = 1";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, Licence.GetOfficeNumber());
			databaseQuery.AddParam(2, currentYear);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
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
						if (databaseQueryResult.next()) {
							firstReceiptId = databaseQueryResult.getInt(0);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		// Delete first receipt items if exits
		if(firstReceiptId != -1){
			String queryUpdateAllMaterialAmountsMinus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(RECEIPT_MATERIALS.AMOUNT) "
					+ "FROM RECEIPT_MATERIALS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "WHERE RECEIPT_MATERIALS.RECEIPT_ID = ? "
					+ "AND RECEIPT_MATERIALS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = RECEIPT_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT RECEIPT_MATERIALS.AMOUNT "
					+ "FROM RECEIPT_MATERIALS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "WHERE RECEIPT_MATERIALS.RECEIPT_ID = ? "
					+ "AND RECEIPT_MATERIALS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = RECEIPT_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";

			String queryUpdateAllTradingGoodsAmountsMinus = ""
				+ "UPDATE TRADING_GOODS_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(RECEIPT_TRADING_GOODS.AMOUNT) "
					+ "FROM RECEIPT_TRADING_GOODS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "WHERE RECEIPT_TRADING_GOODS.RECEIPT_ID = ? "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT RECEIPT_TRADING_GOODS.AMOUNT "
					+ "FROM RECEIPT_TRADING_GOODS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "WHERE RECEIPT_TRADING_GOODS.RECEIPT_ID = ? "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";

			String queryDeleteMaterials = "UPDATE RECEIPT_MATERIALS SET IS_DELETED = 1 WHERE RECEIPT_ID = ?";
			String queryDeleteTradingGoods = "UPDATE RECEIPT_TRADING_GOODS SET IS_DELETED = 1 WHERE RECEIPT_ID = ?";
			
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);

				MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);

				multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsMinus);
				multiDatabaseQuery.AddParam(0, 1, firstReceiptId);
				multiDatabaseQuery.AddParam(0, 2, firstReceiptId);
				multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(0, 4, currentYear);

				multiDatabaseQuery.SetQuery(1, queryUpdateAllTradingGoodsAmountsMinus);
				multiDatabaseQuery.AddParam(1, 1, firstReceiptId);
				multiDatabaseQuery.AddParam(1, 2, firstReceiptId);
				multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 4, currentYear);

				multiDatabaseQuery.SetQuery(2, queryDeleteMaterials);
				multiDatabaseQuery.AddParam(2, 1, firstReceiptId);
				
				multiDatabaseQuery.SetQuery(3, queryDeleteTradingGoods);
				multiDatabaseQuery.AddParam(3, 1, firstReceiptId);

				ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
				databaseQueryTask.execute();
				loadingDialog.setVisible(true);
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

						}
					} catch (Exception ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
		}
		
		// Create first receipt id if no exist
		if(firstReceiptId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			String query = "INSERT INTO RECEIPTS (ID, RECEIPT_DATE, SUPPLIER_ID, DOCUMENT_NUMBER, TOTAL_PRICE, "
				+ "PAYMENT_DUE_DATE, IS_PAID, OFFICE_NUMBER, RECEIPT_NUMBER, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, 0)";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.SetAutoIncrementParam(1, "ID", "RECEIPTS");
			databaseQuery.AddParam(2, currentYear + "-01-01");
			databaseQuery.AddParam(3, 0);
			databaseQuery.AddParam(4, "inventura");
			databaseQuery.AddParam(5, 0);
			databaseQuery.AddParam(6, currentYear + "-01-01");
			databaseQuery.AddParam(7, 1);
			databaseQuery.AddParam(8, Licence.GetOfficeNumber());

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
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
						firstReceiptId = databaseQueryResult.autoGeneratedKey;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
}
