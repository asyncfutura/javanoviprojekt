/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.communication.misc;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class LocalServerServerAppPingData implements Serializable {
	public int companyId;
	public int officeNumber;
	public boolean haveUnfiscalizedInvoices;
	public boolean isControlApp;
	
	public LocalServerServerAppPingData(int companyId, int officeNumber, boolean haveUnfiscalizedInvoices, boolean isControlApp){
		this.companyId = companyId;
		this.officeNumber = officeNumber;
		this.haveUnfiscalizedInvoices = haveUnfiscalizedInvoices;
		this.isControlApp = isControlApp;
	}
}
