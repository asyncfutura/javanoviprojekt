/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import java.io.Serializable;
import java.util.ArrayList;
import hr.adinfo.utils.Pair;

/**
 *
 * @author Matej
 */
public class DatabaseDiff implements Serializable {
	public int diffId;
	public String query;
	public ArrayList<Pair<Integer, String>> params;
	public ArrayList<Pair<Integer, byte[]>> paramsBytes;
	
	public DatabaseDiff(DatabaseQuery databaseQuery){
		query = databaseQuery.query;
		params = new ArrayList<>(0);
		paramsBytes = new ArrayList<>(0);
		for (Pair<Integer, String> param : databaseQuery.params) {
			AddParam(param.getKey(), param.getValue());
		}
		for (Pair<Integer, byte[]> param : databaseQuery.paramsBytes) {
			AddParam(param.getKey(), param.getValue());
		}
	}
	
	public DatabaseDiff(MultiDatabaseQuery multiDatabaseQuery, int queryId){
		query = multiDatabaseQuery.query[queryId];
		params = new ArrayList<>(0);
		paramsBytes = new ArrayList<>(0);
		for (Pair<Integer, String> param : multiDatabaseQuery.params[queryId]) {
			AddParam(param.getKey(), param.getValue());
		}
		for (Pair<Integer, byte[]> param : multiDatabaseQuery.paramsBytes[queryId]) {
			AddParam(param.getKey(), param.getValue());
		}
	}
	
	public void AddParam(int key, String value){
		params.add(new Pair<Integer, String>(key, value));
	}
	
	public void AddParam(int key, int value){
		params.add(new Pair<Integer, String>(key, Integer.toString(value)));
	}
	
	public void AddParam(int key, byte[] value){
		paramsBytes.add(new Pair<Integer, byte[]>(key, value));
	}
}
