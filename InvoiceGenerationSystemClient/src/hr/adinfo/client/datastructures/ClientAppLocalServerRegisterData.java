/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.datastructures;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class ClientAppLocalServerRegisterData implements Serializable {
	public int officeNumber;
	public int cashRegisterNumber;
	
	public ClientAppLocalServerRegisterData(int officeNumber, int cashRegisterNumber){
		this.officeNumber = officeNumber;
		this.cashRegisterNumber = cashRegisterNumber;
	}
}