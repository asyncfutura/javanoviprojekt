/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.datastructures;

import hr.adinfo.utils.Values;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class Invoice implements Serializable {
	public ArrayList<InvoiceItem> items = new ArrayList<>();
	public int clientId = -1;
	public String clientName;
	public String clientOIB;
	public float discountPercentage = 0f;
	public float discountValue = 0f;
	public float totalPrice = 0f;
	public int officeNumber;
	public String officeTag;
	public int cashRegisterNumber;
	public int invoiceNumber;
	public int specialNumber;
	public Date date;
	public String zki;
	public String jir;
	public String paymentMethodName = "";
	public int paymentMethodType;
	public int staffId;
	public String staffOib;
	public String staffName;
	public boolean isCopy;
	public String note = "";
	public boolean isTest;
	public int paymentDelay = 0;
	public boolean isSubtotal = false;
	public boolean isInVatSystem;
	public String einvoiceId = "";
	public String einvoiceStatus = "";
	public String specialZki = Values.DEFAULT_ZKI;
	public String specialJir = Values.DEFAULT_JIR;
	public String paymentMethodName2 = "";
	public int paymentMethodType2 = -1;
	public float paymentAmount2 = 0f;
        public String iznosNapojnice = "";
        public String tipNapojnice = "";
        public String ZKINapojnice = "";
        public String JIRNapojnice = "";
        
	
	public Invoice(){
		
	}
	
	public Invoice(Invoice invoice){
		this.clientId = invoice.clientId;
		this.clientName = invoice.clientName;
		this.clientOIB = invoice.clientOIB;
		this.discountPercentage = invoice.discountPercentage;
		this.discountValue = invoice.discountValue;
		this.totalPrice = invoice.totalPrice;
		this.officeNumber = invoice.officeNumber;
		this.officeTag = invoice.officeTag;
		this.cashRegisterNumber = invoice.cashRegisterNumber;
		this.invoiceNumber = invoice.invoiceNumber;
		this.specialNumber = invoice.specialNumber;
		this.date = invoice.date;
		this.zki = invoice.zki;
		this.jir = invoice.jir;
		this.paymentMethodName = invoice.paymentMethodName;
		this.paymentMethodType = invoice.paymentMethodType;
		this.staffId = invoice.staffId;
		this.staffOib = invoice.staffOib;
		this.staffName = invoice.staffName;
		this.isCopy = invoice.isCopy;
		this.note = invoice.note;
		this.isTest = invoice.isTest;
		this.paymentDelay = invoice.paymentDelay;
		this.isInVatSystem = invoice.isInVatSystem;
		this.einvoiceId = invoice.einvoiceId;
		this.einvoiceStatus = invoice.einvoiceStatus;
		this.specialZki = invoice.specialZki;
		this.specialJir = invoice.specialJir;
		this.paymentMethodName2 = invoice.paymentMethodName2;
		this.paymentMethodType2 = invoice.paymentMethodType2;
		this.paymentAmount2 = invoice.paymentAmount2;
                this.iznosNapojnice = invoice.iznosNapojnice;
                this.tipNapojnice = invoice.tipNapojnice;
                this.ZKINapojnice = invoice.ZKINapojnice;
                this.JIRNapojnice = invoice.JIRNapojnice;
		
		for(int i = 0; i < invoice.items.size(); ++i){
			InvoiceItem invoiceItem = new InvoiceItem(invoice.items.get(i));
			items.add(invoiceItem);
		}
	}
}
