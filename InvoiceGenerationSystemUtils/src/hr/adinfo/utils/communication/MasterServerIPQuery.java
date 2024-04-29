/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication;

import static hr.adinfo.utils.Values.TIMEOUT_SELECT_QUERY_SECONDS;
import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class MasterServerIPQuery extends ServerQuery implements Serializable  {
	
	public MasterServerIPQuery(){
		super();
		timeoutSeconds = 2 * TIMEOUT_SELECT_QUERY_SECONDS;
	}
}
