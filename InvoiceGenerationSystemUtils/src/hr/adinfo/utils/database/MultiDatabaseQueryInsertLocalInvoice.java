/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.database;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class MultiDatabaseQueryInsertLocalInvoice extends MultiDatabaseQuery implements Serializable {
	
	public int oNum, crNum, iNum, specNum, payType, iYear;
	public boolean isTest;
	
	public MultiDatabaseQueryInsertLocalInvoice(int querySize) {
		super(querySize);
	}
	
}
