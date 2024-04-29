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
public class InvoiceItem implements Serializable {
	public int itemId;
	public int itemType;
	public String itemName;
	public float itemPrice;
	public float itemAmount;
	public float discountPercentage;
	public float discountValue;
	public String itemNote = "";
	public float taxRate;
	public float consumptionTaxRate;
	public float packagingRefund;
	public float invoiceDiscountTotal = 0f;
	public boolean isFood;
	public float printedAmount;
	
	public InvoiceItem(){
		
	}
	
	public InvoiceItem(InvoiceItem invoiceItem){
		this.itemId = invoiceItem.itemId;
		this.itemType = invoiceItem.itemType;
		this.itemName = invoiceItem.itemName;
		this.itemPrice = invoiceItem.itemPrice;
		this.itemAmount = invoiceItem.itemAmount;
		this.discountPercentage = invoiceItem.discountPercentage;
		this.discountValue = invoiceItem.discountValue;
		this.itemNote = invoiceItem.itemNote;
		this.taxRate = invoiceItem.taxRate;
		this.consumptionTaxRate = invoiceItem.consumptionTaxRate;
		this.packagingRefund = invoiceItem.packagingRefund;
		this.invoiceDiscountTotal = invoiceItem.invoiceDiscountTotal;
		this.isFood = invoiceItem.isFood;
		this.printedAmount = invoiceItem.printedAmount;
	}
}
