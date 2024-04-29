/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.datastructures;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.Licence;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class StaffUserInfo {
	public int userId = -1;
	public String fullName = "";
	public String firstName = "";
	public String userOIB = "";
	public int userRightsType;
        public static String totalString = "0";

			
	public boolean[] userRights = new boolean[Values.STAFF_RIGHTS_TOTAL_LENGTH];
	
	private static StaffUserInfo currentStaffUser;
	
	public StaffUserInfo(int rightsType){
		userRightsType = rightsType;
		
		if(rightsType == Values.STAFF_RIGHTS_OWNER || rightsType == Values.STAFF_RIGHTS_ADMIN){
			for(int i = 0; i < userRights.length; ++i){
				userRights[i] = true;
			}
		} else if(rightsType == Values.STAFF_RIGHTS_EMPLOYEE || rightsType == Values.STAFF_RIGHTS_STUDENT){
			for(int i = 0; i < userRights.length; ++i){
				userRights[i] = false;
			}
		}
                else if (rightsType == Values.STAFF_RIGHTS_MANAGER){
                    	for(int i = 0; i < userRights.length; ++i){
                            if (i == 26){
                                userRights[i] = false;
                            }
                            else {
                                userRights[i] = false;
                            }
			}
                }
	}
	
	public void SaveStaffRights(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "UPDATE STAFF_RIGHTS SET "
				+ "W1 = ?, W2 = ?, W3 = ?, W4 = ?, W5 = ?, W6 = ?, "
				+ "CR1 = ?, CR2 = ?, CR3 = ?, CR4 = ?, CR5 = ?, CR6 = ?, "
				+ "R1 = ?, R2 = ?, R3 = ?, R4 = ?, R5 = ?, R6 = ?, "
				+ "S1 = ?, "
				+ "RE1 = ?, RE2 = ?, "
				+ "CS1 = ?, CS2 = ?, CS3 = ?, "
				+ "O1 = ?, O2 = ?, O3 = ?, O4 = ?, O5 = ?, "
				+ "R29 = ?, R30 = ? "
				+ "WHERE STAFF_ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		for(int i = 0; i < userRights.length; ++i){
			databaseQuery.AddParam(i + 1, userRights[i] ? 1 : 0);
		}
		databaseQuery.AddParam(userRights.length + 1, userId);
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
						
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	public void LoadStaffRights(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT "
				+ "W1, W2, W3, W4, W5, W6, "
				+ "CR1, CR2, CR3, CR4, CR5, CR6, "
				+ "R1, R2, R3, R4, R5, R6, "
				+ "S1, "
				+ "RE1, RE2, "
				+ "CS1, CS2, CS3, "
				+ "O1, O2, O3, O4, O5, "
				+ "R29, R30 "
				+ "FROM STAFF_RIGHTS WHERE STAFF_ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, userId);
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
						for(int i = 0; i < userRights.length; ++i){
							userRights[i] = databaseQueryResult.getInt(i) == 1;
						}
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
        
        	
	public static void SetCurrentUserInfo(StaffUserInfo staffUserInfo){
		currentStaffUser = staffUserInfo;
	}
	
	public static StaffUserInfo GetCurrentUserInfo(){
		if(currentStaffUser == null){
			currentStaffUser = new StaffUserInfo(Values.STAFF_RIGHTS_EMPLOYEE);
		}
                
                Integer finalId = -1;
                String firstName = currentStaffUser.firstName;
                
                if (finalId == -1 && currentStaffUser.firstName.startsWith("adm")){
                    currentStaffUser.userId = 0;
                }
                
                String lastName = "";
                
                if (currentStaffUser.userId == -1){
                final JDialog loadingDialog = new LoadingDialog(null, true);
			
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT FIRST_NAME, LAST_NAME, ID FROM STAFF WHERE FIRST_NAME = ? AND IS_DELETED = 0 AND ID <> 0");
		databaseQuery.AddParam(1, firstName);
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
						firstName = databaseQueryResult.getString(0);
						lastName = databaseQueryResult.getString(1);
                                                finalId = databaseQueryResult.getInt(2);
                                                currentStaffUser.userId = finalId;
                                        }
                                }
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
                }
            }
        return currentStaffUser;
        }
    }
