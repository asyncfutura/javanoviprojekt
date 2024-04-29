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

public class MasterServerIPResponse extends ServerResponse implements Serializable {
	public String masterIp;
	
	public MasterServerIPResponse(int localServerId, int clientId, int queryId){
		super(localServerId, clientId, queryId);
	}
	
	public MasterServerIPResponse(ServerQuery serverQuery){
		super(serverQuery);
	}
}