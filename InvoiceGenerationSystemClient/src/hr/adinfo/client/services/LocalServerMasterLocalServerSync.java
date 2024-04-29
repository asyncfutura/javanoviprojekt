/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.LocalServer;
import hr.adinfo.client.LocalServerMasterLocalServerClient;
import hr.adinfo.client.ui.LocalServerMasterLocalServerSyncDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseDiff;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;

/**
 *
 * @author Matej
 */
public class LocalServerMasterLocalServerSync {
	private static LocalServerMasterLocalServerSync localServerMasterLocalServerSync = null;
	
	private LocalServerMasterLocalServerSyncDialog syncDialog;
	private boolean syncInProgress;
	
	private LocalServerMasterLocalServerSync() {
		syncDialog = new LocalServerMasterLocalServerSyncDialog(null, true);
		syncInProgress = false;
		
		LocalServerSync();
	}
	
	private void LocalServerSync(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					LocalServerTrySync();
					
					try {
						Thread.sleep(1000 * Values.LOCAL_SERVER_DIFF_SYNC_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private void LocalServerTrySync(){
		if(syncInProgress)
			return;
		
		syncInProgress = true;
		
		try {
			int lastDiffId = LocalServer.GetInstance().GetLastDiffId();
			DatabaseDiffQuery databaseDiffQuery = new DatabaseDiffQuery(lastDiffId);
			databaseDiffQuery.clientId = Values.FAKE_QUERY_ID_SYNC;
			LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(databaseDiffQuery);

			ServerResponse serverResponseFound = null;

			int timeout = 1000 * databaseDiffQuery.timeoutSeconds;
			int delay = 50;
			int counter = 0;
			while(counter < timeout){
				counter += delay;
				Thread.sleep(delay);

				serverResponseFound = LocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(databaseDiffQuery.queryId, Values.FAKE_QUERY_ID_SYNC);
				if(serverResponseFound != null){
					LocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
					break;
				}
			}

			if(serverResponseFound != null){
				DatabaseDiffResponse databaseDiffResponse = (DatabaseDiffResponse)serverResponseFound;
				if(databaseDiffResponse.maxDiffId > lastDiffId + Values.LOCAL_SERVER_DIFF_SYNC_MAX_DIALOG_ROWS){
					LocalServerSyncBatch(lastDiffId, databaseDiffResponse);
				} else {
					for(DatabaseDiff databaseDiff : databaseDiffResponse.diffList){
						LocalServer.GetInstance().InsertDatabaseDiff(databaseDiff);
					}
				}
				
				if(databaseDiffResponse.maxDiffId == lastDiffId){
					if(!LocalServer.GetInstance().isSyncedWithMaster){
						ClientAppLogger.GetInstance().LogMessage("LocalServerMasterLocalServerSync done");
					}
					LocalServer.GetInstance().isSyncedWithMaster = true;
				}
			}
		} catch (Exception ex) {
			syncDialog.SetSyncDialogVisible(false);
			if(!ClientApp.appClosing){
				ClientAppLogger.GetInstance().LogError(ex);
			}
		} finally {
			syncInProgress = false;
		}
	}
	
	private void LocalServerSyncBatch(int lastDiffId, DatabaseDiffResponse databaseDiffResponse) throws Exception {
		syncDialog.SetProgressBarBounds(lastDiffId, databaseDiffResponse.maxDiffId);
		syncDialog.SetProgressBarValue(lastDiffId);
		syncDialog.SetSyncDialogVisible(true);
		int diffInsertedCount = 0;
		
		for(DatabaseDiff databaseDiff : databaseDiffResponse.diffList){
			LocalServer.GetInstance().InsertDatabaseDiff(databaseDiff);
			++diffInsertedCount;
			if(diffInsertedCount == 100){
				diffInsertedCount = 0;
				syncDialog.SetProgressBarValue(databaseDiff.diffId);
			}
		}
		
		boolean syncDone = false;
		while(!syncDone){
			if(ClientApp.appClosing)
				break;

			lastDiffId = LocalServer.GetInstance().GetLastDiffId();
			DatabaseDiffQuery databaseDiffQuery = new DatabaseDiffQuery(lastDiffId);
			databaseDiffQuery.clientId = Values.FAKE_QUERY_ID_SYNC;
			LocalServerMasterLocalServerClient.GetInstance().ForwardExecuteQuery(databaseDiffQuery);

			ServerResponse serverResponseFound = null;

			int timeout = 1000 * databaseDiffQuery.timeoutSeconds;
			int delay = 50;
			int counter = 0;
			while(counter < timeout){
				counter += delay;
				Thread.sleep(delay);

				serverResponseFound = LocalServer.GetServerResponseList().GetResponseByQueryIdAndClientId(databaseDiffQuery.queryId, Values.FAKE_QUERY_ID_SYNC);
				if(serverResponseFound != null){
					LocalServer.GetServerResponseList().RemoveResponse(serverResponseFound);
					break;
				}
			}
			
			if(serverResponseFound != null){
				databaseDiffResponse = (DatabaseDiffResponse)serverResponseFound;
				for(DatabaseDiff databaseDiff : databaseDiffResponse.diffList){
					LocalServer.GetInstance().InsertDatabaseDiff(databaseDiff);
					++diffInsertedCount;
					if(diffInsertedCount == 100){
						diffInsertedCount = 0;
						syncDialog.SetProgressBarValue(databaseDiff.diffId);
					}
				}
				
				if(databaseDiffResponse.diffList.isEmpty()){
					syncDone = true;
				}
			} else {
				syncDone =  true;
			}
		}
		
		syncDialog.SetSyncDialogVisible(false);
	}
	
	public static void OnLocalServerDatabaseQueryForwarded(){
		if(localServerMasterLocalServerSync != null){
			localServerMasterLocalServerSync.LocalServerTrySync();
		}
	}
	
	public static void Init(){
		if(localServerMasterLocalServerSync == null){
			localServerMasterLocalServerSync = new LocalServerMasterLocalServerSync();
		}
	}
}
