/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication;

import static hr.adinfo.utils.Utils.DisposeDialog;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import hr.adinfo.utils.LoggerInterface;
import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQuery;

/**
 *
 * @author Matej
 */
public class ServerQueryTask extends SwingWorker<ServerResponse, Void> {

	private JDialog loadingDialog = null;
	private final ServerQuery serverQuery;
	private final ExecuteQueryInterface executeQueryInterface;
	private final LoggerInterface printLogInterface;
	private final boolean showErrors;
	
	public ServerQueryTask(JDialog loadingDialog, ServerQuery serverQuery, ExecuteQueryInterface executeQueryInterface, LoggerInterface printLogInterface){
		this.loadingDialog = loadingDialog;
		this.serverQuery = serverQuery;
		this.executeQueryInterface = executeQueryInterface;
		this.printLogInterface = printLogInterface;
		this.showErrors = true;
	}
        
        	public ServerQueryTask(ServerQuery serverQuery, ExecuteQueryInterface executeQueryInterface, LoggerInterface printLogInterface){
		this.serverQuery = serverQuery;
		this.executeQueryInterface = executeQueryInterface;
		this.printLogInterface = printLogInterface;
		this.showErrors = true;
	}
	
	public ServerQueryTask(JDialog loadingDialog, ServerQuery serverQuery, ExecuteQueryInterface executeQueryInterface, LoggerInterface printLogInterface, boolean showErrors){
		this.loadingDialog = loadingDialog;
		this.serverQuery = serverQuery;
		this.executeQueryInterface = executeQueryInterface;
		this.printLogInterface = printLogInterface;
		this.showErrors = showErrors;
	}
	
	@Override
	protected ServerResponse doInBackground() throws Exception {
		ServerResponse serverResponse = null;
		try {
			serverResponse = executeQueryInterface.ExecuteQuery(serverQuery);
			if(serverResponse == null){
				if(printLogInterface != null){
					String debugInfo = "";
					if(serverQuery instanceof DatabaseQuery){
						debugInfo = ((DatabaseQuery)serverQuery).query;
					} else if(serverQuery instanceof MultiDatabaseQuery && ((MultiDatabaseQuery)serverQuery).query.length > 0){
						debugInfo = ((MultiDatabaseQuery)serverQuery).query[0];
					}
					if(showErrors){
						printLogInterface.ShowMessage("Pogreška u komunikaciji - istek vremena. Molimo pokušajte zatvoriti, pa ponovno otvoriti trenutni prozor. Kod pogreške: 301");
					}
					printLogInterface.LogMessage("E301 " + debugInfo);
				}
			} else if(serverResponse.errorCode != RESPONSE_ERROR_CODE_SUCCESS){
				if(serverResponse.errorCode == RESPONSE_ERROR_CODE_CONNECTION_FAILED){
					if(printLogInterface != null){
						if(showErrors){
							printLogInterface.ShowMessage("Pogreška u komunikaciji. Molimo pokušajte ponovno. Kod pogreške: 302");
						}
						printLogInterface.LogMessage("E302");
					}
				} else if(serverResponse.errorCode == RESPONSE_ERROR_CODE_SQL_QUERY_FAILED){
					if(printLogInterface != null){
						if(showErrors){
							printLogInterface.ShowMessage("Pogreška u komunikaciji. Molimo pokušajte ponovno. Kod pogreške: 303");
						}
						printLogInterface.LogMessage("E303");
					}
				} else if(serverResponse.errorCode == RESPONSE_ERROR_CODE_MASTER_NOT_SYNCED){
					if(printLogInterface != null){
						if(showErrors){
							printLogInterface.ShowMessage("Pogreška u komunikaciji. Molimo pokušajte ponovno. Kod pogreške: 304");
						}
						printLogInterface.LogMessage("E304");
					}
				} else if(serverResponse.errorCode == RESPONSE_ERROR_CODE_LOCAL_SERVER_NOT_SYNCED){
					if(printLogInterface != null){
						if(showErrors){
							printLogInterface.ShowMessage("Pogreška u komunikaciji. Molimo pokušajte ponovno. Kod pogreške: 305");
						}
						printLogInterface.LogMessage("E305");
					}
				}
			}
		} catch (Exception ex) {
			if(printLogInterface != null){
				if(showErrors){
					printLogInterface.ShowErrorLog(ex);
				} else {
					printLogInterface.LogError(ex);
				}
			}
		}
		return serverResponse;
	}
	
	@Override
	protected void done() {
		DisposeDialog(loadingDialog);
	}
}