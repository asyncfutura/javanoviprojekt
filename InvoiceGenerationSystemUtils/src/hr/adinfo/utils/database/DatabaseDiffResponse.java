/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Matej
 */
public class DatabaseDiffResponse extends ServerResponse implements Serializable {
	public int maxDiffId;
	public ArrayList<DatabaseDiff> diffList;
	
	public DatabaseDiffResponse(int localServerId, int clientId, int queryId){
		super(localServerId, clientId, queryId);
		diffList = new ArrayList<DatabaseDiff>();
	}
	
	public DatabaseDiffResponse(ServerQuery serverQuery){
		super(serverQuery);
		diffList = new ArrayList<DatabaseDiff>();
	}
}
