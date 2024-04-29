/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import static hr.adinfo.utils.Values.TIMEOUT_SELECT_QUERY_SECONDS;
import hr.adinfo.utils.communication.ServerQuery;
import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class DatabaseDiffQuery extends ServerQuery implements Serializable {
	public int lastDiffId;
	
	public DatabaseDiffQuery(int lastDiffId){
		super();
		this.lastDiffId = lastDiffId;
		this.timeoutSeconds = TIMEOUT_SELECT_QUERY_SECONDS;
	}
}
