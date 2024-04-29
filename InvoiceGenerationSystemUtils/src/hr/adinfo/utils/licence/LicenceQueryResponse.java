/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.licence;

import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.communication.ServerQuery;
import hr.adinfo.utils.communication.ServerResponse;
import java.io.Serializable;

/**
 *
 * @author Matej
 */

public class LicenceQueryResponse extends ServerResponse implements Serializable {
	public byte[] publicKeyBytes;
	public byte[] licenceBytes;
	public byte[] certDemoRootBytes;
	public byte[] certDemoSubBytes;
	public byte[] certProdRootBytes;
	public byte[] certProdSubBytes;
	public int licenceErrorCode = LICENCE_ERROR_CODE_ACTIVATION_SUCCESS;
	
	public LicenceQueryResponse(int localServerId, int clientId, int queryId){
		super(localServerId, clientId, queryId);
	}
	
	public LicenceQueryResponse(ServerQuery serverQuery){
		super(serverQuery);
	}
}
