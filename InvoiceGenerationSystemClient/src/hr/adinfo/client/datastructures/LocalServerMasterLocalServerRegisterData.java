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
public class LocalServerMasterLocalServerRegisterData implements Serializable {
	public int officeNumber;
	public String officeAddress;
	
	public LocalServerMasterLocalServerRegisterData(int officeNumber, String officeAddress){
		this.officeNumber = officeNumber;
		this.officeAddress = officeAddress;
	}
}