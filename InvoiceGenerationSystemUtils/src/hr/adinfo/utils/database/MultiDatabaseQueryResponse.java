/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class MultiDatabaseQueryResponse extends ServerResponse implements Serializable {
	public DatabaseQueryResult[] databaseQueryResult;
	
	public MultiDatabaseQueryResponse(int localServerId, int clientId, int queryId){
		super(localServerId, clientId, queryId);
	}
	
	public MultiDatabaseQueryResponse(ServerQuery serverQuery){
		super(serverQuery);
	}
}
