/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.datastructures;

import java.util.ArrayList;

/**
 *
 * @author Matej
 */
public class InvoiceTaxes {
	public ArrayList<Double> taxRates = new ArrayList<>();
	public ArrayList<Double> taxBases = new ArrayList<>();
	public ArrayList<Double> taxAmounts = new ArrayList<>();
	public ArrayList<Boolean> isConsumpionTax = new ArrayList<>();
}
