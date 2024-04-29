/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppServerAppClient;
import hr.adinfo.client.LocalServer;
import hr.adinfo.client.MasterLocalServer;
import hr.adinfo.client.ui.LocalServerMasterLocalServerSyncDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseDiff;
import hr.adinfo.utils.database.DatabaseDiffQuery;
import hr.adinfo.utils.database.DatabaseDiffResponse;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class MasterLocalServerServerAppSync {
	private static MasterLocalServerServerAppSync masterLocalServerServerAppSync = null;
	
	private LocalServerMasterLocalServerSyncDialog syncDialog;
	private boolean syncInProgress;
	
	private static final int SERVICE_LOOP_DELAY_SECONDS = 2;
	
	private MasterLocalServerServerAppSync() {
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
					
					if(LocalServer.GetInstance() == null){
						try {
							Thread.sleep(100);
						} catch (InterruptedException ex) {}
						continue;
					}
					
					if(MasterLocalServer.GetInstance() == null){
						try {
							Thread.sleep(100);
						} catch (InterruptedException ex) {}
						continue;
					}
					
					if(!MasterLocalServer.GetInstance().isMasterSynced){
						MasterLocalServerTrySync();
					}
					
					try {
						Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private void MasterLocalServerTrySync(){
		ClientAppLogger.GetInstance().LogMessage("MasterLocalServerTrySync " + syncInProgress);
		
		if(syncInProgress)
			return;
		
		syncInProgress = true;
		
		try {
			int lastDiffId = LocalServer.GetInstance().GetLastDiffId();
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseDiffQuery databaseDiffQuery = new DatabaseDiffQuery(lastDiffId);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseDiffQuery, ClientAppServerAppClient.GetInstance(), ClientAppLogger.GetInstance(), false);

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
				ServerResponse serverResponse = databaseQueryTask.get();
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					DatabaseDiffResponse databaseDiffResponse = (DatabaseDiffResponse) serverResponse;
					if(databaseDiffResponse.maxDiffId > lastDiffId + Values.LOCAL_SERVER_DIFF_SYNC_MAX_DIALOG_ROWS){
						MasterLocalServerSyncBatch(lastDiffId, databaseDiffResponse);
					} else {
						for(DatabaseDiff databaseDiff : databaseDiffResponse.diffList){
							LocalServer.GetInstance().InsertDatabaseDiff(databaseDiff);
						}
					}
					
					if(databaseDiffResponse.maxDiffId == lastDiffId){
						if(!MasterLocalServer.GetInstance().isMasterSynced){
							ClientAppLogger.GetInstance().LogMessage("MasterLocalServerServerAppSync done");
						}
						MasterLocalServer.GetInstance().isMasterSynced = true;
					}
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
	
	private void MasterLocalServerSyncBatch(int lastDiffId, DatabaseDiffResponse databaseDiffResponse) throws Exception {
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
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			DatabaseDiffQuery databaseDiffQuery = new DatabaseDiffQuery(lastDiffId);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseDiffQuery, ClientAppServerAppClient.GetInstance(), ClientAppLogger.GetInstance(), false);

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
				ServerResponse serverResponse = databaseQueryTask.get();
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseDiffResponse = (DatabaseDiffResponse) serverResponse;
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
					syncDone = true;
				}
			}
		}
		
		syncDialog.SetSyncDialogVisible(false);
	}
	
	public static void Init(){
		if(masterLocalServerServerAppSync == null){
			masterLocalServerServerAppSync = new MasterLocalServerServerAppSync();
		}
	}
}
