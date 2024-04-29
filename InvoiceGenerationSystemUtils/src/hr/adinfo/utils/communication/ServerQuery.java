/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class ServerQuery implements Serializable {
	protected static int queryIdCount = 0;
	
	public int queryId;
	public int clientId;
	public int localServerId;
	public int timeoutSeconds;
	
	public ServerQuery(){
		queryId = ++queryIdCount;
		if(queryIdCount > 1000000)
			queryIdCount = 0;
	}
}
