/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.communication.ServerQuery;
import java.io.Serializable;
import java.util.ArrayList;
import hr.adinfo.utils.Pair;

/**
 *
 * @author Matej
 */
public class DatabaseQuery extends ServerQuery implements Serializable {
	public String query;
	public ArrayList<Pair<Integer, String>> params;
	public ArrayList<Pair<Integer, byte[]>> paramsBytes;
	public Pair<Integer, String> autoIncrementParam;
	public String autoIncrementTable;
	public boolean executeLocally;
	
	public DatabaseQuery(String query){
		super();
		this.query = query;
		params = new ArrayList<>(0);
		paramsBytes = new ArrayList<>(0);
		if("SELECT".equals(query.toUpperCase().substring(0, 6))){
			timeoutSeconds = TIMEOUT_SELECT_QUERY_SECONDS;
		} else {
			timeoutSeconds = TIMEOUT_UPDATE_QUERY_SECONDS;
		}
	}
	
	/** int key: the first parameter is 1, the second is 2, ... */
	public void AddParam(int key, String value){
		params.add(new Pair<Integer, String>(key, value));
	}
	
	/** int key: the first parameter is 1, the second is 2, ... */
	public void AddParam(int key, int value){
		params.add(new Pair<Integer, String>(key, Integer.toString(value)));
	}
	
	/** int key: the first parameter is 1, the second is 2, ... */
	public void AddParam(int key, float value){
		params.add(new Pair<Integer, String>(key, Float.toString(value)));
	}
	
	/** int key: the first parameter is 1, the second is 2, ... */
	public void AddParam(int key, byte[] value){
		paramsBytes.add(new Pair<Integer, byte[]>(key, value));
	}
	
	/** int key: the first parameter is 1, the second is 2, ... */
	public void SetAutoIncrementParam(int key, String value, String table){
		autoIncrementParam = new Pair<Integer, String>(key, value);
		autoIncrementTable = table;
	}
}
