/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.LoggerInterface;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppSettings {
	public static int currentYear;
	
	private static ArrayList<Pair<Integer, String>> settingsList;
	private static ArrayList<Pair<Integer, String>> settingsListOriginal;
	public static final Object settingsLock = new Object();
	
	public static void InitCurrentYear(){
		currentYear = Calendar.getInstance().get(Calendar.YEAR);
	}
	
	public static void LoadSettings(){
		LoadSettings(true);
	}
	
	public static void LoadSettings(boolean showLoadingDialog){
		synchronized(settingsLock){
			settingsList = new ArrayList<>();
			settingsListOriginal = new ArrayList<>();

			boolean loadSuccess = LoadSettingsFromDatabase(showLoadingDialog);
			if(loadSuccess && settingsList.isEmpty()){
				InsertDefaultValues();
				LoadSettingsFromDatabase(showLoadingDialog);
			} else if(settingsList.size() != Values.AppSettingsEnum.values().length){
				InsertMissingValues();
				LoadSettingsFromDatabase(showLoadingDialog);
			}
		}
	}
	
	private static boolean LoadSettingsFromDatabase(boolean showLoadingDialog){
		boolean success = false;
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, VALUE FROM APP_SETTINGS WHERE OFFICE_NUMBER = ? AND CR_NUMBER = ? ORDER BY ID");
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
		databaseQuery.AddParam(2, Licence.GetCashRegisterNumber());
		
		LoggerInterface logger = showLoadingDialog ? ClientAppLogger.GetInstance() : null;
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), logger);
		
		databaseQueryTask.execute();
		
		/*if(showLoadingDialog){
			loadingDialog.setVisible(true);
		} else {
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
		}*/
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				loadingDialog.setVisible(showLoadingDialog);
			}
		});
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		Utils.DisposeDialog(loadingDialog);
		
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
					success = true;
					while (databaseQueryResult.next()) {
						settingsList.add(new Pair<>(databaseQueryResult.getInt(0), databaseQueryResult.getString(1)));
						settingsListOriginal.add(new Pair<>(databaseQueryResult.getInt(0), databaseQueryResult.getString(1)));
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return success;
	}
	
	private static void InsertDefaultValues(){
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(Values.AppSettingsEnum.values().length);
		String s = Values.SETTINGS_LAYOUT_SPLIT_STRING;
		int officeNumber = Licence.GetOfficeNumber();
		int crNumber = Licence.GetCashRegisterNumber();
		
		for(int i = 0; i < Values.AppSettingsEnum.values().length; ++i){
			multiDatabaseQuery.SetQuery(i, "INSERT INTO APP_SETTINGS (ID, VALUE, OFFICE_NUMBER, CR_NUMBER) VALUES (?, ?, ?, ?)");
			multiDatabaseQuery.AddParam(i, 1, i);
			multiDatabaseQuery.AddParam(i, 3, officeNumber);
			multiDatabaseQuery.AddParam(i, 4, crNumber);
			if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_NOTES_BAR_PRINT){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN_PRINT){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_BUTTON_ITEM_NOTE){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME){
				multiDatabaseQuery.AddParam(i, 2, "10");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT){
				multiDatabaseQuery.AddParam(i, 2, "60");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_BAR){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_KITCHEN){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_SUBTOTAL){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_TOUCH){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT){
				multiDatabaseQuery.AddParam(i, 2, "10");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS){
				multiDatabaseQuery.AddParam(i, 2, "14");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_HOURS){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT){
				multiDatabaseQuery.AddParam(i, 2, "0");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_SHOW_CASH_RETURN_DIALOG){
				multiDatabaseQuery.AddParam(i, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID){
				String[] valuesStrings = new String[]{
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1"
				};
				multiDatabaseQuery.AddParam(i, 2, String.join(s, valuesStrings));
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE){
				String[] valuesStrings = new String[]{
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1"
				};
				multiDatabaseQuery.AddParam(i, 2, String.join(s, valuesStrings));
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME){
				String[] valuesStrings = new String[]{
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", 
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", 
					" ", " ", " "
				};
				multiDatabaseQuery.AddParam(i, 2, String.join(s, valuesStrings));
			}  else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_LAYOUT_GROUP_NAMES){
				String[] groupNames = new String[]{"Grupa 1", "Grupa 2", "Grupa 3", "Grupa 4", "Grupa 5", "Grupa 6", "Grupa 7", "Grupa 8", "Grupa 9", "Grupa 10"};
				multiDatabaseQuery.AddParam(i, 2, String.join(s, groupNames));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_SUBGROUP_NAMES, Values.AppSettingsEnum.values()[i])){
				String[] subgroupNames = new String[]{"Podgrupa 1", "Podgrupa 2", "Podgrupa 3", "Podgrupa 4"};
				multiDatabaseQuery.AddParam(i, 2, String.join(s, subgroupNames));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_IDS, Values.AppSettingsEnum.values()[i])){
				String[] itemIds = new String[4 * 35];
				for(int j = 0; j < itemIds.length; ++j){
					itemIds[j] = "-1";
				}
				multiDatabaseQuery.AddParam(i, 2, String.join(s, itemIds));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_TYPES, Values.AppSettingsEnum.values()[i])){
				String[] itemTypes = new String[4 * 35];
				for(int j = 0; j < itemTypes.length; ++j){
					itemTypes[j] = "-1";
				}
				multiDatabaseQuery.AddParam(i, 2, String.join(s, itemTypes));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_COLORS, Values.AppSettingsEnum.values()[i])){
				String[] itemColors = new String[4 * 35];
				for(int j = 0; j < itemColors.length; ++j){
					itemColors[j] = "0";
				}
				multiDatabaseQuery.AddParam(i, 2, String.join(s, itemColors));
			} else {
				multiDatabaseQuery.AddParam(i, 2, "");
			}
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance(), false);
		databaseQueryTask.execute();
		
		/*loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}*/
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				loadingDialog.setVisible(true);
			}
		});
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		Utils.DisposeDialog(loadingDialog);
		
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
					if (databaseQueryResult[0].next()) {
						
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private static void InsertMissingValues(){
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(Values.AppSettingsEnum.values().length - settingsList.size());
		String s = Values.SETTINGS_LAYOUT_SPLIT_STRING;
		int officeNumber = Licence.GetOfficeNumber();
		int crNumber = Licence.GetCashRegisterNumber();
		int queryCount = 0;
		
		for (int i = 0; i < Values.AppSettingsEnum.values().length; ++i){
			boolean found = false;
			for (int j = 0; j < settingsList.size(); ++j){
				if (settingsList.get(j).getKey() == i){
					found = true;
					break;
				}
			}
			if (found){
				continue;
			}
			
			multiDatabaseQuery.SetQuery(queryCount, "INSERT INTO APP_SETTINGS (ID, VALUE, OFFICE_NUMBER, CR_NUMBER) VALUES (?, ?, ?, ?)");
			multiDatabaseQuery.AddParam(queryCount, 1, i);
			multiDatabaseQuery.AddParam(queryCount, 3, officeNumber);
			multiDatabaseQuery.AddParam(queryCount, 4, crNumber);
			if (Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_NOTES_BAR_PRINT){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN_PRINT){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_BUTTON_ITEM_NOTE){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME){
				multiDatabaseQuery.AddParam(queryCount, 2, "10");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT){
				multiDatabaseQuery.AddParam(queryCount, 2, "60");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_BAR){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_KITCHEN){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_SUBTOTAL){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_TOUCH){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT){
				multiDatabaseQuery.AddParam(queryCount, 2, "10");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS){
				multiDatabaseQuery.AddParam(queryCount, 2, "14");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_ADMIN_EXPIRY_NOTICE_HOURS){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT){
				multiDatabaseQuery.AddParam(queryCount, 2, "0");
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_SHOW_CASH_RETURN_DIALOG){
				multiDatabaseQuery.AddParam(queryCount, 2, "1");
			}else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE){
                            multiDatabaseQuery.AddParam(queryCount, 2, "1");
                        }
                         else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID){
				String[] valuesStrings = new String[]{
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1"
				};
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, valuesStrings));
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE){
				String[] valuesStrings = new String[]{
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", 
					"-1", "-1", "-1"
				};
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, valuesStrings));
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME){
				String[] valuesStrings = new String[]{
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", 
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", 
					" ", " ", " "
				};
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, valuesStrings));
			} else if(Values.AppSettingsEnum.values()[i] == Values.AppSettingsEnum.SETTINGS_LAYOUT_GROUP_NAMES){
				String[] groupNames = new String[]{"Grupa 1", "Grupa 2", "Grupa 3", "Grupa 4", "Grupa 5", "Grupa 6", "Grupa 7", "Grupa 8", "Grupa 9", "Grupa 10"};
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, groupNames));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_SUBGROUP_NAMES, Values.AppSettingsEnum.values()[i])){
				String[] subgroupNames = new String[]{"Podgrupa 1", "Podgrupa 2", "Podgrupa 3", "Podgrupa 4"};
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, subgroupNames));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_IDS, Values.AppSettingsEnum.values()[i])){
				String[] itemIds = new String[4 * 35];
				for(int j = 0; j < itemIds.length; ++j){
					itemIds[j] = "-1";
				}
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, itemIds));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_TYPES, Values.AppSettingsEnum.values()[i])){
				String[] itemTypes = new String[4 * 35];
				for(int j = 0; j < itemTypes.length; ++j){
					itemTypes[j] = "-1";
				}
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, itemTypes));
			} else if(ClientAppUtils.ArrayContains(Values.SETTINGS_LAYOUT_ITEM_COLORS, Values.AppSettingsEnum.values()[i])){
				String[] itemColors = new String[4 * 35];
				for(int j = 0; j < itemColors.length; ++j){
					itemColors[j] = "0";
				}
				multiDatabaseQuery.AddParam(queryCount, 2, String.join(s, itemColors));
			} else {
				multiDatabaseQuery.AddParam(queryCount, 2, "");
			}
			
			++queryCount;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance(), false);
		databaseQueryTask.execute();
		
		/*loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}*/
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				loadingDialog.setVisible(true);
			}
		});
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		Utils.DisposeDialog(loadingDialog);
		
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
					if (databaseQueryResult[0].next()) {
						
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	public static String GetString(int key){
		if(settingsList == null)
			return "";
		
		synchronized(settingsLock){
			for(int i = 0; i < settingsList.size(); ++i){
				if(settingsList.get(i).getKey() == key){
					return settingsList.get(i).getValue();
				}
			}

			return "";
		}
	}
	
	public static float GetFloat(int key){
		float toReturn = 0f;
		try {
			toReturn = Float.parseFloat(GetString(key));
		} catch (NumberFormatException ex) {}

		return toReturn;
	}
	
	public static int GetInt(int key){
		int toReturn = 0;
		try {
			toReturn = Integer.parseInt(GetString(key));
		} catch (NumberFormatException ex) {}

		return toReturn;
	}
	
	public static boolean GetBoolean(int key){
		return "1".equals(GetString(key));
	}
	
	public static void SetString(int key, String value){
		if(settingsList == null)
			return;
		
		synchronized(settingsLock){
			for(int i = 0; i < settingsList.size(); ++i){
				if(settingsList.get(i).getKey() == key){
					settingsList.set(i, new Pair(key, value));
				}
			}
		}
	}
	
	public static void SetFloat(int key, float value){
		SetString(key, Float.toString(value));
	}
	
	public static void SetInt(int key, int value){
		SetString(key, Integer.toString(value));
	}
	
	public static void SetBoolean(int key, boolean value){
		SetString(key, value ? "1" : "0");
	}
	
	public static void SaveSettings(){
		if(settingsList == null)
			return;
		
		synchronized(settingsLock){
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID FROM APP_SETTINGS WHERE ID = 0");

			String caseString = "";
			String idString = "";
			int caseCount = 0;
			for(int i = 0; i < settingsListOriginal.size(); ++i){
				if(!settingsListOriginal.get(i).getValue().equals(settingsList.get(i).getValue())){
					caseString += "WHEN ID = ? THEN ? ";
					idString += (caseCount == 0 ? "" : ", ") + settingsList.get(i).getKey() ;
					databaseQuery.AddParam(1 + 2 * caseCount, settingsList.get(i).getKey());
					databaseQuery.AddParam(1 + 2 * caseCount + 1, settingsList.get(i).getValue());
					++caseCount;
				}
			}

			if(caseCount == 0)
				return;

			final JDialog loadingDialog = new LoadingDialog(null, true);

			databaseQuery.query = "UPDATE APP_SETTINGS "
					+ "SET VALUE = (CASE "
					+ caseString
					+ "ELSE '' END) "
					+ "WHERE ID IN (" + idString + ") "
					+ "AND OFFICE_NUMBER = ? AND CR_NUMBER = ?";

			databaseQuery.AddParam(1 + 2 * caseCount, Licence.GetOfficeNumber());
			databaseQuery.AddParam(1 + 2 * caseCount + 1, Licence.GetCashRegisterNumber());
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
						while (databaseQueryResult.next()) {
							settingsList.add(new Pair<>(databaseQueryResult.getInt(0), databaseQueryResult.getString(1)));
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
}
