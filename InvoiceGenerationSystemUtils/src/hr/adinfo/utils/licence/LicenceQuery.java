/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.licence;

import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.communication.ServerQuery;
import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class LicenceQuery extends ServerQuery implements Serializable  {
	
	public int queryType;
	public String queryMessage;
	
	public LicenceQuery(int queryType, String queryMessage){
		super();
		this.queryType = queryType;
		this.queryMessage = queryMessage;
		timeoutSeconds = 2 * TIMEOUT_SELECT_QUERY_SECONDS;
	}
}
