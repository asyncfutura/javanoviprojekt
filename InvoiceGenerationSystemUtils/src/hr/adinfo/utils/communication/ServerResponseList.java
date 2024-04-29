/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Matej
 */
public class ServerResponseList {
	private List<ServerResponse> serverResponseList;
	
	public ServerResponseList(){
		serverResponseList = Collections.synchronizedList(new ArrayList<ServerResponse>());
	}
	
	public void AddResponse(ServerResponse serverResponse){
		ServerResponse serverResponseToRemove = null;
		synchronized(serverResponseList){
			for (ServerResponse serverResponse1 : serverResponseList) {
				if(serverResponse1.queryId == serverResponse.queryId && serverResponse1.clientId == serverResponse.clientId && serverResponse1.localServerId == serverResponse.localServerId){
					serverResponseToRemove = serverResponse1;
				}
			}
			serverResponseList.add(serverResponse);
		}
		if(serverResponseToRemove != null){
			RemoveResponse(serverResponseToRemove);
		}
	}
	
	public void RemoveResponse(ServerResponse serverResponse){
		synchronized(serverResponseList){
			serverResponseList.remove(serverResponse);
		}
	}
	
	public ServerResponse GetResponseByQueryId(int queryId){
		synchronized(serverResponseList){
			for (ServerResponse serverResponse : serverResponseList) {
				if(serverResponse.queryId == queryId){
					return serverResponse;
				}
			}
		}
		
		return null;
	}
	
	public ServerResponse GetResponseByClientId(int clientId){
		synchronized(serverResponseList){
			for (ServerResponse serverResponse : serverResponseList) {
				if(serverResponse.clientId == clientId){
					return serverResponse;
				}
			}
		}
		
		return null;
	}
	
	public ServerResponse GetResponseByLocalServerId(int localServerId){
		synchronized(serverResponseList){
			for (ServerResponse serverResponse : serverResponseList) {
				if(serverResponse.localServerId == localServerId){
					return serverResponse;
				}
			}
		}
		
		return null;
	}
	
	public ServerResponse GetResponseByQueryIdAndClientId(int queryId, int clientId){
		synchronized(serverResponseList){
			for (ServerResponse serverResponse : serverResponseList) {
				if(serverResponse.queryId == queryId && serverResponse.clientId == clientId){
					return serverResponse;
				}
			}
		}
		
		return null;
	}
}
