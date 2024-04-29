/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication;

import static hr.adinfo.utils.Values.RESPONSE_ERROR_CODE_SUCCESS;
import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class ServerResponse implements Serializable {
	public int queryId;
	public int clientId;
	public int localServerId;
	public int errorCode = RESPONSE_ERROR_CODE_SUCCESS;
	
	public ServerResponse(int localServerId, int clientId, int queryId){
		this.localServerId = localServerId;
		this.clientId = clientId;
		this.queryId = queryId;
	}
	
	public ServerResponse(ServerQuery serverQuery){
		this.localServerId = serverQuery.localServerId;
		this.clientId = serverQuery.clientId;
		this.queryId = serverQuery.queryId;
	}
}
