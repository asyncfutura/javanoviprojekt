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
public class ServerQueryList {
	private List<ServerQuery> serverQueryList;
	
	public ServerQueryList(){
		serverQueryList = Collections.synchronizedList(new ArrayList<ServerQuery>());
	}
	
	public void AddQuery(ServerQuery serverQuery){
		synchronized(serverQueryList){
			serverQueryList.add(serverQuery);
		}
	}
	
	public void RemoveQuery(ServerQuery serverQuery){
		synchronized(serverQueryList){
			serverQueryList.remove(serverQuery);
		}
	}
	
	public ServerQuery GetNextQuery(){
		synchronized(serverQueryList){
			if(!serverQueryList.isEmpty()){
				return serverQueryList.get(0);
			}
		}
		
		return null;
	}
}
