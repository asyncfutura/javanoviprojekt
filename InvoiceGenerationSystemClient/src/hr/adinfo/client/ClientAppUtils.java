/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.PackagingRefunds;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 * @author Matej
 */
public class ClientAppUtils {
	
	public static final String CHANGES_LOG_QUERY = "INSERT INTO CHANGES_LOG "
			+ "(ID, CHANGE_DATE, CHANGE_TIME, OFFICE_NUMBER, STAFF_ID, STAFF_NAME, CHANGE_TYPE, CHANGE_DESC) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String MAX_WAREHOUSE_ITEM_ID_QUERY = 
			"SELECT COALESCE(MAX(MaxId), 0) + 1 "
			+ "FROM ("
				+ "SELECT MAX(ID) AS MaxId FROM ARTICLES "
				+ "UNION ALL "
				+ "SELECT MAX(ID) AS MaxId FROM SERVICES "
				+ "UNION ALL "
				+ "SELECT MAX(ID) AS MaxId FROM TRADING_GOODS "
			+ ") AS WAREHOUSE_MAX_IDS";
	
	public static final String MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY = 
			"SELECT COALESCE(MAX(MaxCustomId), 0) + 1 "
			+ "FROM ("
				+ "SELECT MAX(CUSTOM_ID) AS MaxCustomId FROM ARTICLES "
				+ "UNION ALL "
				+ "SELECT MAX(CUSTOM_ID) AS MaxCustomId FROM SERVICES "
				+ "UNION ALL "
				+ "SELECT MAX(CUSTOM_ID) AS MaxCustomId FROM TRADING_GOODS "
			+ ") AS WAREHOUSE_MAX_CUSTOM_IDS";
        
        public static Integer totalInteger = 0;

	private static void CreateMaterialAmount(int materialId, int officeId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "INSERT INTO MATERIAL_AMOUNTS (ID, OFFICE_NUMBER, MATERIAL_ID, AMOUNT, AMOUNT_YEAR) VALUES (?, ?, ?, ?, ?)";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.SetAutoIncrementParam(1, "ID", "MATERIAL_AMOUNTS");
		databaseQuery.AddParam(2, officeId);
		databaseQuery.AddParam(3, materialId);
		databaseQuery.AddParam(4, 0);
		databaseQuery.AddParam(5, ClientAppSettings.currentYear);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private static void CreateTradingGoodsAmount(int tradingGoodsId, int officeId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		

		String query = "INSERT INTO TRADING_GOODS_AMOUNTS (ID, OFFICE_NUMBER, TRADING_GOODS_ID, AMOUNT, AMOUNT_YEAR) VALUES (?, ?, ?, ?, ?)";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.SetAutoIncrementParam(1, "ID", "TRADING_GOODS_AMOUNTS");
		databaseQuery.AddParam(2, officeId);
		databaseQuery.AddParam(3, tradingGoodsId);
		databaseQuery.AddParam(4, 0);
		databaseQuery.AddParam(5, ClientAppSettings.currentYear);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private static void CreateConsumptionTaxAmount(int materialId, int officeId){
		float value = 3f;
		if(Licence.GetOfficeAddress().toLowerCase().contains("zagreb")){
			value = 2f;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "INSERT INTO CONSUMPTION_TAX_VALUES (ID, OFFICE_NUMBER, CONSUMPTION_TAX_ID, VALUE) VALUES (?, ?, ?, ?)";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.SetAutoIncrementParam(1, "ID", "CONSUMPTION_TAX_VALUES");
		databaseQuery.AddParam(2, officeId);
		databaseQuery.AddParam(3, materialId);
		databaseQuery.AddParam(4, value);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	public static void CreateAllMaterialAmountsIfNoExist(int officeId){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT MATERIALS.ID "
					+ "FROM MATERIALS "
					+ "WHERE MATERIALS.ID NOT IN ("
						+ "SELECT MATERIALS.ID "
						+ "FROM MATERIALS "
						+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID "
						+ "WHERE MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND MATERIAL_AMOUNTS.AMOUNT_YEAR = ?"
					+ ")";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, officeId);
			databaseQuery.AddParam(2, ClientAppSettings.currentYear);
			
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						while (databaseQueryResult.next()) {
							CreateMaterialAmount(databaseQueryResult.getInt(0), officeId);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	public static void CreateAllTradingGoodsAmountsIfNoExist(int officeId){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT TRADING_GOODS.ID "
					+ "FROM TRADING_GOODS "
					+ "WHERE TRADING_GOODS.ID NOT IN ("
						+ "SELECT TRADING_GOODS.ID "
						+ "FROM TRADING_GOODS "
						+ "INNER JOIN TRADING_GOODS_AMOUNTS ON TRADING_GOODS.ID = TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID "
						+ "WHERE TRADING_GOODS_AMOUNTS.OFFICE_NUMBER = ? AND TRADING_GOODS_AMOUNTS.AMOUNT_YEAR = ?"
					+ ")";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, officeId);
			databaseQuery.AddParam(2, ClientAppSettings.currentYear);
			
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						while (databaseQueryResult.next()) {
							CreateTradingGoodsAmount(databaseQueryResult.getInt(0), officeId);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	public static void CreateConsumptionTaxAmountsIfNoExist(int officeId){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT CONSUMPTION_TAXES.ID "
					+ "FROM CONSUMPTION_TAXES "
					+ "WHERE CONSUMPTION_TAXES.ID NOT IN ("
						+ "SELECT CONSUMPTION_TAXES.ID "
						+ "FROM CONSUMPTION_TAXES "
						+ "INNER JOIN CONSUMPTION_TAX_VALUES ON CONSUMPTION_TAXES.ID = CONSUMPTION_TAX_VALUES.CONSUMPTION_TAX_ID "
						+ "WHERE CONSUMPTION_TAX_VALUES.OFFICE_NUMBER = ?"
					+ ")";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, officeId);
			
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						while (databaseQueryResult.next()) {
							CreateConsumptionTaxAmount(databaseQueryResult.getInt(0), officeId);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	public static void SetTouchLayoutGroupButtonText(JButton button, String text){
		String prefix = "<html> <div style=\"text-align: center\"> ";
		String sufix = " </div> </html>";
		button.setText(prefix + text + sufix);
	}
	
	public static void SetTouchLayoutTabTitleText(JTabbedPane tabbedPane, int index, String text){
		String prefix = "<html> <div width = \"75\"  align = \"center\"> <br> ";
		String sufix = " <br><br> </div> </html>";
		tabbedPane.setTitleAt(index, prefix + text + sufix);
	}
	
	public static void SetTouchLayoutItemButtonText(JButton button, String text){
		String prefix = "<html> <div style=\"text-align: center\"> ";
		String sufix = " </div> </html>";
		button.setText(prefix + text + sufix);
	}
	
	public static String DoubleToPriceString(double d){
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
		return bd.toString();
	}
	
	public static double DoubleToPriceDouble(double d){
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
		return bd.doubleValue();
	}
	
	public static String FloatToPriceString(float d){
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
		return bd.toString();
	}
	
	public static float FloatToPriceFloat(float d){
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
		return bd.floatValue();
	}
	
	public static String FloatToStringNoLimit(float d){
		int decPlaces = String.valueOf(d).split("\\.")[1].length();
		if(decPlaces > 4){
			decPlaces = 4;
		}
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decPlaces, BigDecimal.ROUND_HALF_UP);       
		return bd.toString();
	}
	
	public static void SetupFocusTraversal(Window dialog){
		List<Component> compList = Utils.GetAllComponents(dialog);
		for (Component comp : compList) {
			if(comp instanceof JComboBox){
				Set setForward = new HashSet(comp.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
				Set setBackward = new HashSet(comp.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
				setForward.remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
				setBackward.remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
				comp.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward);
				comp.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward);
				
				comp.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent e){
						((JComboBox)comp).showPopup();
					}
				});
			} else if(comp instanceof JTable){
				Set setForward = new HashSet(comp.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
				Set setBackward = new HashSet(comp.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
				setForward.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
				setBackward.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
				comp.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward);
				comp.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward);
			} else if (comp instanceof JCheckBox){
				InputMap im = ((JCheckBox)comp).getInputMap();
				KeyStroke existingKeyStroke = KeyStroke.getKeyStroke("SPACE");
				KeyStroke addedKeyStroke = KeyStroke.getKeyStroke("ENTER");
				im.put(addedKeyStroke, im.get(existingKeyStroke));
				existingKeyStroke = KeyStroke.getKeyStroke("released SPACE");
				addedKeyStroke = KeyStroke.getKeyStroke("released ENTER");
				im.put(addedKeyStroke, im.get(existingKeyStroke));
			}
		}
	}
	
	public static boolean ArrayContains(Values.AppSettingsEnum[] array, Values.AppSettingsEnum value){
		for(int i = 0; i < array.length; ++i){
			if(array[i] == value)
				return true;
		}
		
		return false;
	}
	
	public static boolean ArrayContains(int[] array, int value){
		for(int i = 0; i < array.length; ++i){
			if(array[i] == value)
				return true;
		}
		
		return false;
	}
	
	public static int ArrayIndexOf(ArrayList<Integer> array, int value){
		for(int i = 0; i < array.size(); ++i){
			if(array.get(i) == value)
				return i;
		}
		
		return -1;
	}
	
	public static int ArrayIndexOf(ArrayList<Float> array, float value){
		for(int i = 0; i < array.size(); ++i){
			if(array.get(i) == value)
				return i;
		}
		
		return -1;
	}
	
	public static int ArrayIndexOf(ArrayList<Double> array, double value){
		for(int i = 0; i < array.size(); ++i){
			if(array.get(i) == value)
				return i;
		}
		
		return -1;
	}
	
	public static int ArrayIndexOf(ArrayList<String> array, String value){
		for(int i = 0; i < array.size(); ++i){
			if(array.get(i).equals(value))
				return i;
		}
		
		return -1;
	}
	
	public static InvoiceTaxes CalculateTaxes(Invoice invoice){
		return CalculateTaxes(invoice, invoice.totalPrice);
	}
	
	public static InvoiceTaxes CalculateTaxes(Invoice invoice, double invoiceTotalPrice){
		InvoiceTaxes invoiceTaxes = new InvoiceTaxes();
		float totalPackagingRefund = 0f;
		for (int i = 0; i < invoice.items.size(); ++i){
			int taxRateIndex = ArrayIndexOf(invoiceTaxes.taxRates, invoice.items.get(i).taxRate);
			double itemPrice = invoice.items.get(i).itemAmount * invoice.items.get(i).itemPrice;
			
			if(invoice.items.get(i).discountPercentage != 0f){
				itemPrice = itemPrice * (100f - invoice.items.get(i).discountPercentage) / 100f;
			} else if(invoice.items.get(i).discountValue != 0f){
				itemPrice = itemPrice - invoice.items.get(i).itemAmount * invoice.items.get(i).discountValue;
			}
			
			if(invoice.discountPercentage != 0f){
				itemPrice = itemPrice * (100f - invoice.discountPercentage) / 100f;
			} else if(invoice.discountValue != 0f){
				itemPrice = itemPrice - itemPrice / invoiceTotalPrice * invoice.discountValue;
			}
			
			float packagindRefund = invoice.items.get(i).itemAmount * invoice.items.get(i).packagingRefund;
			totalPackagingRefund += packagindRefund;
			itemPrice -= packagindRefund;
			
			double taxRate = invoice.items.get(i).taxRate;
			double consumptionTaxRate = invoice.items.get(i).consumptionTaxRate;
			if(consumptionTaxRate != 0f){
				int consumptionTaxIndex = ArrayIndexOf(invoiceTaxes.taxRates, invoice.items.get(i).consumptionTaxRate);
				double taxBase = 100f * itemPrice / (taxRate + consumptionTaxRate + 100f);
				double taxAmount = taxRate * taxBase / 100f;
				double consumptionTaxAmount = consumptionTaxRate * taxBase / 100f;
				
				if(taxRateIndex == -1){
					invoiceTaxes.taxRates.add(taxRate);
					invoiceTaxes.taxAmounts.add(taxAmount);
					invoiceTaxes.taxBases.add(taxBase);
					invoiceTaxes.isConsumpionTax.add(false);
				} else {
					invoiceTaxes.taxAmounts.set(taxRateIndex, invoiceTaxes.taxAmounts.get(taxRateIndex) + taxAmount);
					invoiceTaxes.taxBases.set(taxRateIndex, invoiceTaxes.taxBases.get(taxRateIndex) + taxBase);
				}
				
				if(consumptionTaxIndex == -1){
					invoiceTaxes.taxRates.add(consumptionTaxRate);
					invoiceTaxes.taxAmounts.add(consumptionTaxAmount);
					invoiceTaxes.taxBases.add(taxBase);
					invoiceTaxes.isConsumpionTax.add(true);
				} else {
					invoiceTaxes.taxAmounts.set(consumptionTaxIndex, invoiceTaxes.taxAmounts.get(consumptionTaxIndex) + consumptionTaxAmount);
					invoiceTaxes.taxBases.set(consumptionTaxIndex, invoiceTaxes.taxBases.get(consumptionTaxIndex) + taxBase);
				}
			} else {
				double taxBase = 100f * itemPrice / (taxRate + 100f);
				double taxAmount = itemPrice - taxBase;
				
				if(taxRateIndex == -1){
					invoiceTaxes.taxRates.add(taxRate);
					invoiceTaxes.taxAmounts.add(taxAmount);
					invoiceTaxes.taxBases.add(taxBase);
					invoiceTaxes.isConsumpionTax.add(false);
				} else {
					invoiceTaxes.taxAmounts.set(taxRateIndex, invoiceTaxes.taxAmounts.get(taxRateIndex) + taxAmount);
					invoiceTaxes.taxBases.set(taxRateIndex, invoiceTaxes.taxBases.get(taxRateIndex) + taxBase);
				}
			}
		}
		
		double baseSum = 0f;
		double taxSum = 0f;
		double consumptionTaxSum = 0f;
		for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
			double taxBase = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxBases.get(i));
			double taxAmount = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxAmounts.get(i));
			invoiceTaxes.taxBases.set(i, taxBase);
			invoiceTaxes.taxAmounts.set(i, taxAmount);
			if(invoiceTaxes.isConsumpionTax.get(i)){
				consumptionTaxSum += taxAmount;
			} else {
				baseSum += taxBase;
				taxSum += taxAmount;
			}
		}
		
		// Round errors
		double totalSum = ClientAppUtils.DoubleToPriceDouble(baseSum + taxSum + consumptionTaxSum);
		double totalPrice = ClientAppUtils.DoubleToPriceDouble(invoiceTotalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
		totalPrice -= totalPackagingRefund;
		double totalDiff = totalPrice - totalSum;
		if(totalDiff != 0f){
			boolean roundSuccess = false;
			double oldBase = 0f;
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.isConsumpionTax.get(i))
					continue;
				
				double taxBase = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxBases.get(i));
				double taxAmount = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxAmounts.get(i));
				double taxRate = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxRates.get(i));
				if(ClientAppUtils.DoubleToPriceDouble((taxBase + totalDiff) * taxRate / 100f) == taxAmount){
					roundSuccess = true;
					oldBase = taxBase;
					invoiceTaxes.taxBases.set(i, taxBase + totalDiff);
					break;
				}
			}
			if(!roundSuccess){
				for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
					if(invoiceTaxes.isConsumpionTax.get(i))
						continue;

					double taxBase = ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxBases.get(i));
					roundSuccess = true;
					oldBase = taxBase;
					invoiceTaxes.taxBases.set(i, taxBase + totalDiff);
					break;
				}
			}
			
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.isConsumpionTax.get(i) && invoiceTaxes.taxBases.get(i) == oldBase){
					invoiceTaxes.taxBases.set(i, oldBase + totalDiff);
				}
			}
		}
		
		return invoiceTaxes;
	}
	
	public static PackagingRefunds CalculatePackagingRefunds(Invoice invoice){
		PackagingRefunds packagingRefunds = new PackagingRefunds();
		
		for (int i = 0; i < invoice.items.size(); ++i){
			if(invoice.items.get(i).packagingRefund == 0f)
				continue;
			
			int refundIndex = ArrayIndexOf(packagingRefunds.refundValues, invoice.items.get(i).packagingRefund);
			if(refundIndex == -1){
				packagingRefunds.refundValues.add(invoice.items.get(i).packagingRefund);
				packagingRefunds.refundAmounts.add(invoice.items.get(i).itemAmount);
			} else {
				packagingRefunds.refundAmounts.set(refundIndex, packagingRefunds.refundAmounts.get(refundIndex) + invoice.items.get(i).itemAmount);
			}
		}
		
		return packagingRefunds;
	}
	
	public static Invoice GetInvoice(int invoiceId, boolean isLocalInvoice){
		Invoice invoice = new Invoice();
		invoice.isCopy = true;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);

		String queryInvoice = "";
		String queryItems = "";
		if(isLocalInvoice){
			queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
					+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
					+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
					+ "STAFF.FIRST_NAME, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
					+ "FROM LOCAL_INVOICES "
					+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
					+ "LEFT OUTER JOIN CLIENTS ON LOCAL_INVOICES.C_ID = CLIENTS.ID "
					+ "WHERE LOCAL_INVOICES.ID = ?";
			queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "WHERE IN_ID = ?";
		} else {
			queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
					+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
					+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
					+ "STAFF.FIRST_NAME, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
					+ "FROM INVOICES "
					+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
					+ "LEFT OUTER JOIN CLIENTS ON INVOICES.C_ID = CLIENTS.ID "
					+ "WHERE INVOICES.ID = ?";
			queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
					+ "FROM INVOICE_ITEMS "
					+ "WHERE IN_ID = ?";
		}
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoice = queryInvoice.replace("INVOICES", "INVOICES_TEST");
			queryItems = queryItems.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
		multiDatabaseQuery.SetQuery(0, queryInvoice);
		multiDatabaseQuery.AddParam(0, 1, invoiceId);
		multiDatabaseQuery.SetQuery(1, queryItems);
		multiDatabaseQuery.AddParam(1, 1, invoiceId);
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult[0].next()) {
						invoice.officeNumber = databaseQueryResult[0].getInt(0);
						invoice.cashRegisterNumber = databaseQueryResult[0].getInt(1);
						invoice.invoiceNumber = databaseQueryResult[0].getInt(2);
						invoice.specialNumber = databaseQueryResult[0].getInt(3);
						invoice.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[0].getString(4) + " " + databaseQueryResult[0].getString(5));
						invoice.staffOib = databaseQueryResult[0].getString(6);
						invoice.staffId = databaseQueryResult[0].getInt(7);
						invoice.paymentMethodName = databaseQueryResult[0].getString(8);
						invoice.paymentMethodType = databaseQueryResult[0].getInt(9);
						invoice.clientId = databaseQueryResult[0].getInt(10);
						invoice.discountPercentage = databaseQueryResult[0].getFloat(11);
						invoice.discountValue = databaseQueryResult[0].getFloat(12);
						invoice.totalPrice = databaseQueryResult[0].getFloat(13);
						invoice.zki = databaseQueryResult[0].getString(14);
						invoice.jir = databaseQueryResult[0].getString(15);
						invoice.note = databaseQueryResult[0].getString(16);
						invoice.clientName = databaseQueryResult[0].getString(17);
						invoice.clientOIB = databaseQueryResult[0].getString(18);
						invoice.paymentDelay = databaseQueryResult[0].getInt(19);
						invoice.staffName = databaseQueryResult[0].getString(20);
						invoice.officeTag = databaseQueryResult[0].getString(21);
						invoice.isInVatSystem = databaseQueryResult[0].getInt(22) == 1;
						invoice.einvoiceId = databaseQueryResult[0].getString(23);
						invoice.einvoiceStatus = databaseQueryResult[0].getString(24);
						invoice.specialZki = databaseQueryResult[0].getString(25);
						invoice.specialJir = databaseQueryResult[0].getString(26);
						invoice.paymentMethodName2 = databaseQueryResult[0].getString(27);
						invoice.paymentMethodType2 = databaseQueryResult[0].getInt(28);
						invoice.paymentAmount2 = databaseQueryResult[0].getFloat(29);
						invoice.isTest = !isProduction;
					}
					
					while (databaseQueryResult[1].next()) {
						InvoiceItem invoiceItem = new InvoiceItem();
						invoiceItem.itemId = databaseQueryResult[1].getInt(0);
						invoiceItem.itemName = databaseQueryResult[1].getString(1);
						invoiceItem.itemAmount = databaseQueryResult[1].getFloat(2);
						invoiceItem.itemPrice = databaseQueryResult[1].getFloat(3);
						invoiceItem.discountPercentage = databaseQueryResult[1].getFloat(4);
						invoiceItem.discountValue = databaseQueryResult[1].getFloat(5);
						invoiceItem.taxRate = databaseQueryResult[1].getFloat(6);
						invoiceItem.consumptionTaxRate = databaseQueryResult[1].getFloat(7);
						invoiceItem.itemType = databaseQueryResult[1].getInt(8);
						invoiceItem.packagingRefund = databaseQueryResult[1].getFloat(9);
						invoice.items.add(invoiceItem);
					}
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return invoice;
	}
	
	public static Invoice GetInvoice(int invoiceNumber, String officeTag, int cashRegisterNumber, int specialNumber, int paymentType, boolean isLocalInvoice){
		Invoice invoice = null;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);

		String queryInvoice = "";
		String queryItems = "";
		if(isLocalInvoice){
			queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
					+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
					+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
					+ "STAFF.FIRST_NAME, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
					+ "FROM LOCAL_INVOICES "
					+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
					+ "LEFT OUTER JOIN CLIENTS ON LOCAL_INVOICES.C_ID = CLIENTS.ID "
					+ "WHERE LOCAL_INVOICES.O_TAG = ? AND LOCAL_INVOICES.CR_NUM = ? AND LOCAL_INVOICES.I_NUM = ? AND YEAR(LOCAL_INVOICES.I_DATE) = ? "
					+ "AND SPEC_NUM = ? AND (PAY_TYPE = ? OR ? = " + Values.PAYMENT_METHOD_ANY_METHOD + ")";
			queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "WHERE IN_ID = (SELECT ID FROM LOCAL_INVOICES "
						+ "WHERE O_TAG = ? AND CR_NUM = ? AND I_NUM = ? AND YEAR(I_DATE) = ? AND SPEC_NUM = ? AND (PAY_TYPE = ? OR ? = " + Values.PAYMENT_METHOD_ANY_METHOD + "))";
		} else {
			queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
					+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
					+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
					+ "STAFF.FIRST_NAME, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
					+ "FROM INVOICES "
					+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
					+ "LEFT OUTER JOIN CLIENTS ON INVOICES.C_ID = CLIENTS.ID "
					+ "WHERE INVOICES.O_TAG = ? AND INVOICES.CR_NUM = ? AND INVOICES.I_NUM = ? AND YEAR(INVOICES.I_DATE) = ? "
					+ "AND SPEC_NUM = ? AND (PAY_TYPE = ? OR ? = " + Values.PAYMENT_METHOD_ANY_METHOD + ")";
			queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
					+ "FROM INVOICE_ITEMS "
					+ "WHERE IN_ID = (SELECT ID FROM INVOICES "
						+ "WHERE O_TAG = ? AND CR_NUM = ? AND I_NUM = ? AND YEAR(I_DATE) = ? AND SPEC_NUM = ? AND (PAY_TYPE = ? OR ? = " + Values.PAYMENT_METHOD_ANY_METHOD + "))";
		}
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoice = queryInvoice.replace("INVOICES", "INVOICES_TEST");
			queryItems = queryItems.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICES", "INVOICES_TEST");
		}
		multiDatabaseQuery.SetQuery(0, queryInvoice);
		multiDatabaseQuery.AddParam(0, 1, officeTag);
		multiDatabaseQuery.AddParam(0, 2, cashRegisterNumber);
		multiDatabaseQuery.AddParam(0, 3, invoiceNumber);
		multiDatabaseQuery.AddParam(0, 4, ClientAppSettings.currentYear);
		multiDatabaseQuery.AddParam(0, 5, specialNumber);
		multiDatabaseQuery.AddParam(0, 6, paymentType);
		multiDatabaseQuery.AddParam(0, 7, paymentType);
		multiDatabaseQuery.SetQuery(1, queryItems);
		multiDatabaseQuery.AddParam(1, 1, officeTag);
		multiDatabaseQuery.AddParam(1, 2, cashRegisterNumber);
		multiDatabaseQuery.AddParam(1, 3, invoiceNumber);
		multiDatabaseQuery.AddParam(1, 4, ClientAppSettings.currentYear);
		multiDatabaseQuery.AddParam(1, 5, specialNumber);
		multiDatabaseQuery.AddParam(1, 6, paymentType);
		multiDatabaseQuery.AddParam(1, 7, paymentType);
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult[0].next()) {
						invoice = new Invoice();
						invoice.isCopy = true;
						invoice.officeNumber = databaseQueryResult[0].getInt(0);
						invoice.cashRegisterNumber = databaseQueryResult[0].getInt(1);
						invoice.invoiceNumber = databaseQueryResult[0].getInt(2);
						invoice.specialNumber = databaseQueryResult[0].getInt(3);
						invoice.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[0].getString(4) + " " + databaseQueryResult[0].getString(5));
						invoice.staffOib = databaseQueryResult[0].getString(6);
						invoice.staffId = databaseQueryResult[0].getInt(7);
						invoice.paymentMethodName = databaseQueryResult[0].getString(8);
						invoice.paymentMethodType = databaseQueryResult[0].getInt(9);
						invoice.clientId = databaseQueryResult[0].getInt(10);
						invoice.discountPercentage = databaseQueryResult[0].getFloat(11);
						invoice.discountValue = databaseQueryResult[0].getFloat(12);
						invoice.totalPrice = databaseQueryResult[0].getFloat(13);
						invoice.zki = databaseQueryResult[0].getString(14);
						invoice.jir = databaseQueryResult[0].getString(15);
						invoice.note = databaseQueryResult[0].getString(16);
						invoice.clientName = databaseQueryResult[0].getString(17);
						invoice.clientOIB = databaseQueryResult[0].getString(18);
						invoice.paymentDelay = databaseQueryResult[0].getInt(19);
						invoice.staffName = databaseQueryResult[0].getString(20);
						invoice.officeTag = databaseQueryResult[0].getString(21);
						invoice.isInVatSystem = databaseQueryResult[0].getInt(22) == 1;
						invoice.einvoiceId = databaseQueryResult[0].getString(23);
						invoice.einvoiceStatus = databaseQueryResult[0].getString(24);
						invoice.specialZki = databaseQueryResult[0].getString(25);
						invoice.specialJir = databaseQueryResult[0].getString(26);
						invoice.paymentMethodName2 = databaseQueryResult[0].getString(27);
						invoice.paymentMethodType2 = databaseQueryResult[0].getInt(28);
						invoice.paymentAmount2 = databaseQueryResult[0].getFloat(29);
						invoice.isTest = !isProduction;
					}
					
					while (databaseQueryResult[1].next()) {
						InvoiceItem invoiceItem = new InvoiceItem();
						invoiceItem.itemId = databaseQueryResult[1].getInt(0);
						invoiceItem.itemName = databaseQueryResult[1].getString(1);
						invoiceItem.itemAmount = databaseQueryResult[1].getFloat(2);
						invoiceItem.itemPrice = databaseQueryResult[1].getFloat(3);
						invoiceItem.discountPercentage = databaseQueryResult[1].getFloat(4);
						invoiceItem.discountValue = databaseQueryResult[1].getFloat(5);
						invoiceItem.taxRate = databaseQueryResult[1].getFloat(6);
						invoiceItem.consumptionTaxRate = databaseQueryResult[1].getFloat(7);
						invoiceItem.itemType = databaseQueryResult[1].getInt(8);
						invoiceItem.packagingRefund = databaseQueryResult[1].getFloat(9);
						invoice.items.add(invoiceItem);
					}
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
                
		return invoice;
	}
	
	public static Invoice GetUnfiscalizedInvoice(boolean isProduction, boolean isLocalInvoice){
		Invoice invoice = null;
		int invoiceId = -1;
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);

			String fiscTypesString = ClientAppUtils.GetFiscalizationTypesString();
			String queryInvoice = "";
			if(isLocalInvoice){
				queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
						+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
						+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
						+ "STAFF.FIRST_NAME, LOCAL_INVOICES.ID, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
						+ "FROM LOCAL_INVOICES "
						+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
						+ "LEFT OUTER JOIN CLIENTS ON LOCAL_INVOICES.C_ID = CLIENTS.ID "
						+ "WHERE JIR = ? AND PAY_TYPE IN (" + fiscTypesString +  ") AND LOCAL_INVOICES.IS_DELETED = 0 "
						+ "ORDER BY LOCAL_INVOICES.ID "
						+ "FETCH FIRST ROW ONLY";
			} else {
				queryInvoice = "SELECT O_NUM, CR_NUM, I_NUM, SPEC_NUM, "
						+ "I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, C_ID, "
						+ "DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, "
						+ "STAFF.FIRST_NAME, INVOICES.ID, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2 "
						+ "FROM INVOICES "
						+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
						+ "LEFT OUTER JOIN CLIENTS ON INVOICES.C_ID = CLIENTS.ID "
						+ "WHERE JIR = ? AND PAY_TYPE IN (" + fiscTypesString +  ") "
						+ "ORDER BY INVOICES.ID "
						+ "FETCH FIRST ROW ONLY";
			}
			if(!isProduction){
				queryInvoice = queryInvoice.replace("INVOICES", "INVOICES_TEST");
			}
			multiDatabaseQuery.SetQuery(0, queryInvoice);
			multiDatabaseQuery.AddParam(0, 1, Values.DEFAULT_JIR);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult[] databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if (databaseQueryResult[0].next()) {
							invoice = new Invoice();
							invoice.isCopy = true;
							invoice.officeNumber = databaseQueryResult[0].getInt(0);
							invoice.cashRegisterNumber = databaseQueryResult[0].getInt(1);
							invoice.invoiceNumber = databaseQueryResult[0].getInt(2);
							invoice.specialNumber = databaseQueryResult[0].getInt(3);
							invoice.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[0].getString(4) + " " + databaseQueryResult[0].getString(5));
							invoice.staffOib = databaseQueryResult[0].getString(6);
							invoice.staffId = databaseQueryResult[0].getInt(7);
							invoice.paymentMethodName = databaseQueryResult[0].getString(8);
							invoice.paymentMethodType = databaseQueryResult[0].getInt(9);
							invoice.clientId = databaseQueryResult[0].getInt(10);
							invoice.discountPercentage = databaseQueryResult[0].getFloat(11);
							invoice.discountValue = databaseQueryResult[0].getFloat(12);
							invoice.totalPrice = databaseQueryResult[0].getFloat(13);
							invoice.zki = databaseQueryResult[0].getString(14);
							invoice.jir = databaseQueryResult[0].getString(15);
							invoice.note = databaseQueryResult[0].getString(16);
							invoice.clientName = databaseQueryResult[0].getString(17);
							invoice.clientOIB = databaseQueryResult[0].getString(18);
							invoice.paymentDelay = databaseQueryResult[0].getInt(19);
							invoice.staffName = databaseQueryResult[0].getString(20);
							invoiceId = databaseQueryResult[0].getInt(21);
							invoice.officeTag = databaseQueryResult[0].getString(22);
							invoice.isInVatSystem = databaseQueryResult[0].getInt(23) == 1;
							invoice.einvoiceId = databaseQueryResult[0].getString(24);
							invoice.einvoiceStatus = databaseQueryResult[0].getString(25);
							invoice.specialZki = databaseQueryResult[0].getString(26);
							invoice.specialJir = databaseQueryResult[0].getString(27);
							invoice.paymentMethodName2 = databaseQueryResult[0].getString(28);
							invoice.paymentMethodType2 = databaseQueryResult[0].getInt(29);
							invoice.paymentAmount2 = databaseQueryResult[0].getFloat(30);
							invoice.isTest = !isProduction;
						}
					}
				} catch (InterruptedException | ExecutionException | ParseException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);

			String queryItems = "";
			if(isLocalInvoice){
				queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
						+ "FROM LOCAL_INVOICE_ITEMS "
						+ "WHERE IN_ID = ?";
			} else {
				queryItems = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, IT_TYPE, PACK_REF "
						+ "FROM INVOICE_ITEMS "
						+ "WHERE IN_ID = ?";
			}
			if(!isProduction){
				queryItems = queryItems.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			}
			multiDatabaseQuery.SetQuery(0, queryItems);
			multiDatabaseQuery.AddParam(0, 1, invoiceId);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult[] databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						while (databaseQueryResult[0].next()) {
							InvoiceItem invoiceItem = new InvoiceItem();
							invoiceItem.itemId = databaseQueryResult[0].getInt(0);
							invoiceItem.itemName = databaseQueryResult[0].getString(1);
							invoiceItem.itemAmount = databaseQueryResult[0].getFloat(2);
							invoiceItem.itemPrice = databaseQueryResult[0].getFloat(3);
							invoiceItem.discountPercentage = databaseQueryResult[0].getFloat(4);
							invoiceItem.discountValue = databaseQueryResult[0].getFloat(5);
							invoiceItem.taxRate = databaseQueryResult[0].getFloat(6);
							invoiceItem.consumptionTaxRate = databaseQueryResult[0].getFloat(7);
							invoiceItem.itemType = databaseQueryResult[0].getInt(8);
							invoiceItem.packagingRefund = databaseQueryResult[0].getFloat(9);
							invoice.items.add(invoiceItem);
						}
					} else {
						invoice = null;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		return invoice;
	}
        
        public static Integer GetTotalValue(){
		String toReturn = "";
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT FIN_PR FROM INVOICES");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult.next()) {
						 totalInteger += databaseQueryResult.getInt(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return totalInteger;
	}
	
	public static boolean HaveUnfiscalizedInvoices(boolean isProduction){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String fiscTypesString = ClientAppUtils.GetFiscalizationTypesString();
		
		String queryLocal = "SELECT LOCAL_INVOICES.ID "
				+ "FROM LOCAL_INVOICES "
				+ "WHERE JIR = ? AND PAY_TYPE IN (" + fiscTypesString +  ")";
		String query = "SELECT INVOICES.ID "
				+ "FROM INVOICES "
				+ "WHERE JIR = ? AND PAY_TYPE IN (" + fiscTypesString +  ")";
		if(!isProduction){
			queryLocal = queryLocal.replace("INVOICES", "INVOICES_TEST");
			query = query.replace("INVOICES", "INVOICES_TEST");
		}
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, queryLocal);
		multiDatabaseQuery.AddParam(0, 1, Values.DEFAULT_JIR);
		multiDatabaseQuery.SetQuery(1, query);
		multiDatabaseQuery.AddParam(1, 1, Values.DEFAULT_JIR);
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult[0].next()) {
						return true;
					}
					
					if (databaseQueryResult[1].next()) {
						return true;
					}
					
					return false;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return true;
	}
	
	public static boolean InsertLocalInvoice(Invoice invoice){
		if(Licence.IsControlApp()){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće izdavati račune kroz kontrolnu aplikaciju");
			return false;
		}
		
		boolean success = false;
		Invoice subtotalInvoice = null;
		if(invoice.isSubtotal){
			subtotalInvoice = new Invoice(invoice);
		}
		
		// Calculate article materials sum
		String insertMaterialsArticleIdsString = "";
		String caseString = "";
		ArrayList<Pair<Integer, Float>> articleAmountSum = new ArrayList<Pair<Integer, Float>>();
		
		//if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_OFFER && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			for (int i = 0; i < invoice.items.size(); ++i){
				if(invoice.items.get(i).itemType != Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE)
					continue;

				boolean insertNew = true;
				for(int j = 0; j < articleAmountSum.size(); ++j){
					Pair<Integer, Float> pair = articleAmountSum.get(j);
					if(pair.getKey() == invoice.items.get(i).itemId){
						insertNew = false;
						articleAmountSum.set(j, new Pair<>(pair.getKey(), pair.getValue() + invoice.items.get(i).itemAmount));
						break;
					}
				}
				if(insertNew){
					articleAmountSum.add(new Pair<>(invoice.items.get(i).itemId, invoice.items.get(i).itemAmount));
				}
			}

			for (int i = 0; i < articleAmountSum.size(); ++i){
				insertMaterialsArticleIdsString += (i == 0 ? "" : ", ") + articleAmountSum.get(i).getKey();
				caseString += "WHEN NORMATIVES.ARTICLE_ID = " + articleAmountSum.get(i).getKey() + " THEN " + articleAmountSum.get(i).getValue() + " ";
			}
		//}
		
		// Get items count
		int itemsCount = 0;
		for(int i = 0; i < invoice.items.size(); ++i){
			if("".equals(invoice.items.get(i).itemNote)){
				itemsCount++;
			}
		}
		
		// Insert invoice
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2 + itemsCount + (articleAmountSum.isEmpty() ? 0 : 1) + (invoice.isSubtotal ? 1 : 0));
			multiDatabaseQuery.executeLocally = true;

			String insertLocalInvoiceQuery;

			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP || invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				insertLocalInvoiceQuery = "INSERT INTO LOCAL_INVOICES (ID, "
						+ "O_NUM, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, "
						+ "C_ID, DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, IS_DELETED) "
						+ "VALUES (?, ?, ?, 0, "
							+ "CASE WHEN (SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) > "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) THEN "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) ELSE "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) END, "
						+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
			} else if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_OFFER){
				insertLocalInvoiceQuery = "INSERT INTO LOCAL_INVOICES (ID, "
						+ "O_NUM, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, "
						+ "C_ID, DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, IS_DELETED) "
						+ "VALUES (?, ?, ?, 0, "
							+ "CASE WHEN (SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) > "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) THEN "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) ELSE "
							+ "(SELECT COALESCE(MAX(SPEC_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND PAY_TYPE = ? AND YEAR(I_DATE) = ?) END, "
						+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
			} else {
				insertLocalInvoiceQuery = "INSERT INTO LOCAL_INVOICES (ID, "
						+ "O_NUM, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_OIB, S_ID, PAY_NAME, PAY_TYPE, "
						+ "C_ID, DIS_PCT, DIS_AMT, FIN_PR, ZKI, JIR, NOTE, O_TAG, VAT_SYS, E_IN_ID, E_IN_ST, S_ZKI, S_JIR, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, IS_DELETED) "
						+ "VALUES (?, ?, ?, "
							+ "CASE WHEN (SELECT COALESCE(MAX(I_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND YEAR(I_DATE) = ?) > "
							+ "(SELECT COALESCE(MAX(I_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND YEAR(I_DATE) = ?) THEN "
							+ "(SELECT COALESCE(MAX(I_NUM), 0) + 1 FROM LOCAL_INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND YEAR(I_DATE) = ?) ELSE "
							+ "(SELECT COALESCE(MAX(I_NUM), 0) + 1 FROM INVOICES WHERE O_NUM = ? AND CR_NUM = ? AND YEAR(I_DATE) = ?) END, "
						+ "0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
			}
			boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
			if(!isProduction){
				insertLocalInvoiceQuery = insertLocalInvoiceQuery.replace("INVOICES", "INVOICES_TEST");
			}
			multiDatabaseQuery.SetQuery(0, insertLocalInvoiceQuery);
			multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", isProduction ? "LOCAL_INVOICES" : "LOCAL_INVOICES_TEST");
			multiDatabaseQuery.AddParam(0, 2, invoice.officeNumber);
			int offset = 0;
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP || invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				multiDatabaseQuery.AddParam(0, 3, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 4, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 5, invoice.paymentMethodType);
				multiDatabaseQuery.AddParam(0, 6, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 7, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 8, invoice.paymentMethodType);
				multiDatabaseQuery.AddParam(0, 9, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 10, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 11, invoice.paymentMethodType);
				multiDatabaseQuery.AddParam(0, 12, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 13, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 14, invoice.paymentMethodType);
				multiDatabaseQuery.AddParam(0, 15, ClientAppSettings.currentYear);
			} else if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_OFFER){
				multiDatabaseQuery.AddParam(0, 3, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 4, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 5, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 6, Values.PAYMENT_METHOD_TYPE_OFFER);
				multiDatabaseQuery.AddParam(0, 7, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 8, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 9, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 10, Values.PAYMENT_METHOD_TYPE_OFFER);
				multiDatabaseQuery.AddParam(0, 11, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 12, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 13, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 14, Values.PAYMENT_METHOD_TYPE_OFFER);
				multiDatabaseQuery.AddParam(0, 15, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 16, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 17, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 18, Values.PAYMENT_METHOD_TYPE_OFFER);
				multiDatabaseQuery.AddParam(0, 19, ClientAppSettings.currentYear);
				offset = 4;
			} else {
				multiDatabaseQuery.AddParam(0, 3, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 4, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 5, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 6, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 7, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 8, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 9, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 10, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 11, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 12, ClientAppSettings.currentYear);
				multiDatabaseQuery.AddParam(0, 13, invoice.officeNumber);
				multiDatabaseQuery.AddParam(0, 14, invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(0, 15, ClientAppSettings.currentYear);
			}
			multiDatabaseQuery.AddParam(0, 16 + offset, new SimpleDateFormat("yyyy-MM-dd").format(invoice.date));
			multiDatabaseQuery.AddParam(0, 17 + offset, new SimpleDateFormat("HH:mm:ss").format(invoice.date));
			multiDatabaseQuery.AddParam(0, 18 + offset, invoice.staffOib);
			multiDatabaseQuery.AddParam(0, 19 + offset, invoice.staffId);
			multiDatabaseQuery.AddParam(0, 20 + offset, invoice.paymentMethodName);
			multiDatabaseQuery.AddParam(0, 21 + offset, invoice.paymentMethodType);
			multiDatabaseQuery.AddParam(0, 22 + offset, invoice.clientId);
			multiDatabaseQuery.AddParam(0, 23 + offset, invoice.discountPercentage);
			multiDatabaseQuery.AddParam(0, 24 + offset, invoice.discountValue);
			multiDatabaseQuery.AddParam(0, 25 + offset, invoice.totalPrice);
			multiDatabaseQuery.AddParam(0, 26 + offset, invoice.zki);
			multiDatabaseQuery.AddParam(0, 27 + offset, invoice.jir);
			multiDatabaseQuery.AddParam(0, 28 + offset, invoice.note);
			multiDatabaseQuery.AddParam(0, 29 + offset, invoice.officeTag);
			multiDatabaseQuery.AddParam(0, 30 + offset, invoice.isInVatSystem ? 1 : 0);
			multiDatabaseQuery.AddParam(0, 31 + offset, invoice.einvoiceId);
			multiDatabaseQuery.AddParam(0, 32 + offset, invoice.einvoiceStatus);
			multiDatabaseQuery.AddParam(0, 33 + offset, invoice.specialZki);
			multiDatabaseQuery.AddParam(0, 34 + offset, invoice.specialJir);
			multiDatabaseQuery.AddParam(0, 35 + offset, invoice.paymentMethodName2);
			multiDatabaseQuery.AddParam(0, 36 + offset, invoice.paymentMethodType2);
			multiDatabaseQuery.AddParam(0, 37 + offset, invoice.paymentAmount2);

			String insertLocalInvoiceItemQuery = "INSERT INTO LOCAL_INVOICE_ITEMS (ID, IN_ID, IT_TYPE, "
					+ "IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, TAX, C_TAX, PACK_REF) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			if(!isProduction){
				insertLocalInvoiceItemQuery = insertLocalInvoiceItemQuery.replace("ITEMS", "ITEMS_TEST");
			}
			int queryCount = 1;
			for (int i = 0; i < invoice.items.size(); ++i){
				if(!"".equals(invoice.items.get(i).itemNote))
					continue;

				multiDatabaseQuery.SetQuery(queryCount, insertLocalInvoiceItemQuery);
				multiDatabaseQuery.SetAutoIncrementParam(queryCount, 1, "ID", isProduction ? "LOCAL_INVOICE_ITEMS" : "LOCAL_INVOICE_ITEMS_TEST");
				multiDatabaseQuery.AddAutoGeneratedParam(queryCount, 2, 0);
				multiDatabaseQuery.AddParam(queryCount, 3, invoice.items.get(i).itemType);
				multiDatabaseQuery.AddParam(queryCount, 4, invoice.items.get(i).itemId);
				multiDatabaseQuery.AddParam(queryCount, 5, invoice.items.get(i).itemName);
				multiDatabaseQuery.AddParam(queryCount, 6, invoice.items.get(i).itemAmount);
				multiDatabaseQuery.AddParam(queryCount, 7, invoice.items.get(i).itemPrice);
				multiDatabaseQuery.AddParam(queryCount, 8, invoice.items.get(i).discountPercentage);
				multiDatabaseQuery.AddParam(queryCount, 9, invoice.items.get(i).discountValue);
				multiDatabaseQuery.AddParam(queryCount, 10, invoice.items.get(i).taxRate);
				multiDatabaseQuery.AddParam(queryCount, 11, invoice.items.get(i).consumptionTaxRate);
				multiDatabaseQuery.AddParam(queryCount, 12, invoice.items.get(i).packagingRefund);

				queryCount++;
			}

			if(!articleAmountSum.isEmpty()){
				String insertLocalInvoiceMaterialsQuery = "INSERT INTO LOCAL_INVOICE_MATERIALS (ID, IN_ID, "
						+ "ART_ID, MAT_ID, AMT, NORM, IS_DELETED) "
						+ "SELECT ROW_NUMBER() OVER() + ?, ?, NORMATIVES.ARTICLE_ID, NORMATIVES.MATERIAL_ID, "
						+ "(CASE " + caseString + " ELSE 0 END), "
						+ "NORMATIVES.AMOUNT, 0 "
						+ "FROM NORMATIVES "
						+ "WHERE NORMATIVES.ARTICLE_ID IN (" + insertMaterialsArticleIdsString + ") "
						+ "AND NORMATIVES.IS_DELETED = 0";
				if(!isProduction){
					insertLocalInvoiceMaterialsQuery = insertLocalInvoiceMaterialsQuery.replace("LOCAL_INVOICE_MATERIALS", "LOCAL_INVOICE_MATERIALS_TEST");
				}
				multiDatabaseQuery.SetQuery(queryCount, insertLocalInvoiceMaterialsQuery);
				multiDatabaseQuery.SetAutoIncrementParam(queryCount, 1, "ID", isProduction ? "LOCAL_INVOICE_MATERIALS" : "LOCAL_INVOICE_MATERIALS_TEST");
				multiDatabaseQuery.AddAutoGeneratedParam(queryCount, 2, 0);

				queryCount++;
			}

			if(invoice.isSubtotal && subtotalInvoice != null){
				String updateLocalInvoiceSubtotal = "UPDATE LOCAL_INVOICES SET PAY_NAME = (? || '' || "
						+ "(SELECT TRIM(CAST(CAST(I_NUM AS CHAR(32)) AS VARCHAR(32))) FROM LOCAL_INVOICES WHERE ID = ?)"
						+ " || '' || ?) "
						+ "WHERE I_NUM = ? AND O_NUM = ? AND SPEC_NUM = ?";
				if(!isProduction){
					updateLocalInvoiceSubtotal = updateLocalInvoiceSubtotal.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
				}
				multiDatabaseQuery.SetQuery(queryCount, updateLocalInvoiceSubtotal);
				multiDatabaseQuery.AddParam(queryCount, 1, Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME);
				multiDatabaseQuery.AddAutoGeneratedParam(queryCount, 2, 0);
				multiDatabaseQuery.AddParam(queryCount, 3, "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber);
				multiDatabaseQuery.AddParam(queryCount, 4, subtotalInvoice.invoiceNumber);
				multiDatabaseQuery.AddParam(queryCount, 5, subtotalInvoice.officeNumber);
				multiDatabaseQuery.AddParam(queryCount, 6, subtotalInvoice.specialNumber);
			
				queryCount++;
			}

			String querySelectIds = "SELECT I_NUM, SPEC_NUM FROM LOCAL_INVOICES WHERE ID = ?";
			if(!isProduction){
				querySelectIds = querySelectIds.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			}
			multiDatabaseQuery.SetQuery(queryCount, querySelectIds);
			multiDatabaseQuery.AddAutoGeneratedParam(queryCount, 1, 0);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult[] databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if(databaseQueryResult[queryCount].next()){
							success = true;
							invoice.invoiceNumber = databaseQueryResult[queryCount].getInt(0);
							invoice.specialNumber = databaseQueryResult[queryCount].getInt(1);
							invoice.isTest = !isProduction;
						}
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		if(success && invoice.isSubtotal && subtotalInvoice != null){
			String updateInvoiceSubtotal = "UPDATE INVOICES SET PAY_NAME = (? || '' || ?) "
					+ "WHERE I_NUM = ? AND O_NUM = ? AND SPEC_NUM = ?";
			if(subtotalInvoice.isTest){
				updateInvoiceSubtotal = updateInvoiceSubtotal.replace("INVOICES", "INVOICES_TEST");
			}
			
			final JDialog loadingDialog = new LoadingDialog(null, true);
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
			multiDatabaseQuery.SetQuery(0, updateInvoiceSubtotal);
			multiDatabaseQuery.AddParam(0, 1, Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME);
			multiDatabaseQuery.AddParam(0, 2, invoice.invoiceNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber);
			multiDatabaseQuery.AddParam(0, 3, subtotalInvoice.invoiceNumber);
			multiDatabaseQuery.AddParam(0, 4, subtotalInvoice.officeNumber);
			multiDatabaseQuery.AddParam(0, 5, subtotalInvoice.specialNumber);
			
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult[] databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		return success;
	}
	
	public static String GetLocalValue(String name){
		String toReturn = "";
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT VALUE FROM LOCAL_VALUES_TABLE WHERE NAME = ?");
		databaseQuery.AddParam(1, name);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult.next()) {
						toReturn = databaseQueryResult.getString(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	public static boolean SetLocalValue(String name, String value){
		boolean success = false;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		DatabaseQuery databaseQuery = new DatabaseQuery("UPDATE LOCAL_VALUES_TABLE SET VALUE = ? WHERE NAME = ?");
		databaseQuery.AddParam(1, value);
		databaseQuery.AddParam(2, name);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					success = true;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return success;
	}
	
	public static boolean IsFiscalizationType(int type){
		return ClientAppUtils.ArrayContains(GetFiscalizationTypes(), type);
	}
	
	public static int[] GetFiscalizationTypes(){
		return new int[]{
			Values.PAYMENT_METHOD_TYPE_CASH, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CHECK, 
			Values.PAYMENT_METHOD_TYPE_OTHER, Values.PAYMENT_METHOD_TYPE_OFFER
		};
	}
	
	public static String GetFiscalizationTypesString(){
		String toReturn = "";
		int[] fiscTypes = GetFiscalizationTypes();
		for (int i = 0; i < fiscTypes.length; ++i){
			toReturn += "".equals(toReturn) ? fiscTypes[i] : ", " + fiscTypes[i];
		}
		return toReturn;
	}
	
	public static PrintTableExtraData GetInvoiceExtraDataItems(Invoice invoice, String ItemNote){
		// Invoice discount
                int dtCheck = CheckDate(invoice.date);
                //Rezultat moze bit 0-ne prolazi ni jedan uvjet ili je dobio krivi datum
                //10 - datum je manji od 01.01.2023
                //20 - datum je veći od 01.01.2023 a manji od 01.01.2024
                //30 - datum je veći od 01.01.2024
                String discountLine1 = null;
                String discountLine2 = null;
                float totalPriceWithDiscount = 0;
                String totalPriceWithDiscountEur = null;
                if(dtCheck == 10){
                    String discountLine1Eur = PrintUtils.CalculateExchangeRate(invoice.totalPrice) + " Eur";
                    discountLine1 = "Cijena bez popusta: " + ClientAppUtils.FloatToPriceString(invoice.totalPrice) + " kn / " + discountLine1Eur;
                    discountLine2 = "";
                    String discountLine2Eur = "";
                    if(invoice.discountPercentage != 0f){
                        discountLine2Eur = PrintUtils.CalculateExchangeRate(invoice.totalPrice * invoice.discountPercentage / 100f) + " Eur";
                            discountLine2 = "Popust: " + invoice.discountPercentage + "% (" + ClientAppUtils.FloatToPriceString(invoice.totalPrice * invoice.discountPercentage / 100f) + " kn)  / " + discountLine2Eur;
                    } else if(invoice.discountValue != 0f){
                        discountLine2Eur = PrintUtils.CalculateExchangeRate(invoice.discountValue) + " Eur";
                            discountLine2 = "Popust: " + invoice.discountValue + " kn / " + discountLine2Eur;
                    }
                    totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
                    totalPriceWithDiscountEur = PrintUtils.CalculateExchangeRate(totalPriceWithDiscount) + " Eur";
                }else if(dtCheck == 20){
                    String discountLine1Eur = PrintUtils.EurToKn(invoice.totalPrice) + " kn";
                    discountLine1 = "Cijena bez popusta: " + ClientAppUtils.FloatToPriceString(invoice.totalPrice) + " eur / " + discountLine1Eur;
                    discountLine2 = "";
                    String discountLine2Eur = "";
                    if(invoice.discountPercentage != 0f){
                        discountLine2Eur = PrintUtils.EurToKn(invoice.totalPrice * invoice.discountPercentage / 100f) + " kn";
                            discountLine2 = "Popust: " + invoice.discountPercentage + "% (" + ClientAppUtils.FloatToPriceString(invoice.totalPrice * invoice.discountPercentage / 100f) + " eur)  / " + discountLine2Eur;
                    } else if(invoice.discountValue != 0f){
                        discountLine2Eur = PrintUtils.EurToKn(invoice.discountValue) + " kn";
                            discountLine2 = "Popust: " + invoice.discountValue + " eur / " + discountLine2Eur;
                    }
                    totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
                    totalPriceWithDiscountEur = PrintUtils.EurToKn(totalPriceWithDiscount) + " Kn";
                }else{
                    discountLine1 = "Cijena bez popusta: " + ClientAppUtils.FloatToPriceString(invoice.totalPrice) + " eur" ;
                    discountLine2 = "";
                    if(invoice.discountPercentage != 0f){
                            discountLine2 = "Popust: " + invoice.discountPercentage + "% (" + ClientAppUtils.FloatToPriceString(invoice.totalPrice * invoice.discountPercentage / 100f) + " eur) ";
                    } else if(invoice.discountValue != 0f){
                            discountLine2 = "Popust: " + invoice.discountValue + " eur";
                    }
                    totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
                    //totalPriceWithDiscountEur = PrintUtils.EurToKn(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue) + " Kn";
                }
                
		
		// Invoice extra data
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		if(invoice.isCopy && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_OFFER && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			printTableExtraData.headerList.add(new Pair<>("KOPIJA RAČUNA", ""));
			printTableExtraData.headerList.add(new Pair<>(" ", " "));
		}
		if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			printTableExtraData.headerList.add(new Pair<>("PREDRAČUN", ""));
			printTableExtraData.headerList.add(new Pair<>(" ", " "));
		}
		printTableExtraData.headerList.add(new Pair<>("Datum:                    ", new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(invoice.date)));
		printTableExtraData.headerList.add(new Pair<>("Oznaka djelatnika:  ", "" + invoice.staffId + "-" + invoice.staffName));
		if(invoice.clientId != -1){
			printTableExtraData.headerList.add(new Pair<>(" ", " "));
			printTableExtraData.headerList.add(new Pair<>("Kupac:           ", invoice.clientName));
                        printTableExtraData.headerList.add(new Pair<>("Adresa:       ", getClientAddress(invoice.clientId)));
			printTableExtraData.headerList.add(new Pair<>("OIB kupca:    ", invoice.clientOIB));
		}
		if(!"".equals(discountLine2)){
			printTableExtraData.footerList.add(new Pair<>(PrintUtils.ALIGN_RIGHT_SUBSTRING, discountLine1));
			printTableExtraData.footerList.add(new Pair<>(PrintUtils.ALIGN_RIGHT_SUBSTRING, discountLine2));
		}
                if(dtCheck == 10){
                    printTableExtraData.footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " kn ", PrintUtils.ALIGN_RIGHT_SUBSTRING));
                }else if(dtCheck == 20){
                    printTableExtraData.footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " eur ", PrintUtils.ALIGN_RIGHT_SUBSTRING));
                }else{
                    printTableExtraData.footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " eur ", PrintUtils.ALIGN_RIGHT_SUBSTRING));
                }
		//printTableExtraData.footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " kn / " + totalPriceWithDiscountEur, PrintUtils.ALIGN_RIGHT_SUBSTRING));
                //printTableExtraData.footerList.add(new Pair<>("Tečaj Kn/Eur: " + PrintUtils.strExchangeRate, PrintUtils.ALIGN_RIGHT_SUBSTRING));
                
                printTableExtraData.footerList.add(new Pair<>(" ", " "));
                
		if(invoice.paymentMethodType2 == -1){
			printTableExtraData.footerList.add(new Pair<>("", "Način plaćanja: " + invoice.paymentMethodName));
		} else {
			float totalPriceWithDiscount2 = invoice.paymentAmount2 * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
			float totalPriceWithDiscount1 = totalPriceWithDiscount - totalPriceWithDiscount2;
			printTableExtraData.footerList.add(new Pair<>("", "Načini plaćanja: "));
			printTableExtraData.footerList.add(new Pair<>("", invoice.paymentMethodName + " = " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount1)));
			printTableExtraData.footerList.add(new Pair<>("", invoice.paymentMethodName2 + " = " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount2)));
		}
		if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(invoice.date);
			calendar.add(Calendar.DATE, invoice.paymentDelay);
			printTableExtraData.footerList.add(new Pair<>("Rok plaćanja:    ", new SimpleDateFormat("dd.MM.yyyy.").format(calendar.getTime())));
		}
                
		if(!"".equals(invoice.note)){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>("Napomena: ", invoice.note));
		}
                
                if(!"".equals(ItemNote)){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>("Napomena: ", ItemNote));
		}
		
		return printTableExtraData;
	}
        
        private static int CheckDate(Date dtRacuna){
            int Rez = 0; //Rezultat moze bit 0-ne prolazi ni jedan uvjet ili je dobio krivi datum
                         //10 - datum je manji od 01.01.2023
                         //20 - datum je veći od 01.01.2023 a manji od 01.01.2024
                         //30 - datum je veći od 01.01.2024
            Date dtKonverzije = null;
            Date dtPrestanakDvojnogIskazivanja = null;
            try{
                String strDtKonverzije = "01.01.2023 0:0:0.000";
                String strDtKrajDvojnogIskazivanja = "01.01.2024 0:0:0.000";
                dtKonverzije = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).parse(strDtKonverzije);
                dtPrestanakDvojnogIskazivanja = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).parse(strDtKrajDvojnogIskazivanja);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            if(dtRacuna.compareTo(dtKonverzije) < 0){
                Rez = 10;
            }else if((dtRacuna.compareTo(dtKonverzije)) > 0 && (dtRacuna.compareTo(dtPrestanakDvojnogIskazivanja) < 0)){
                Rez = 20;
            }else{
                Rez = 30;
            }
            return Rez;
        }
        
        private static String getClientAddress(int id){
            String address = null;
            
            final JDialog loadingDialog = new LoadingDialog(null, true);
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT STREET, HOUSE_NUM, TOWN, POSTAL_CODE FROM CLIENTS WHERE ID = ?");
		databaseQuery.AddParam(1, id);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResult != null){
					if (databaseQueryResult.next()) {
						address = databaseQueryResult.getString(0) + " "  + databaseQueryResult.getString(1) + "\n                " + databaseQueryResult.getString(3) + " " + databaseQueryResult.getString(2);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
            
            return address;
        }
	
	public static PrintTableExtraData GetInvoiceExtraDataTaxes(Invoice invoice){
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Razrada poreza:", ""));
		
		PackagingRefunds packagingRefunds = ClientAppUtils.CalculatePackagingRefunds(invoice);
		boolean refundsTitlePrinted = false;
		for (int refundIndex = 0; refundIndex < packagingRefunds.refundValues.size(); ++refundIndex){
			if(packagingRefunds.refundAmounts.get(refundIndex) == 0f)
				continue;

			if(!refundsTitlePrinted){
				refundsTitlePrinted = true;
				printTableExtraData.footerList.add(new Pair<>("Povratne naknade:", ""));
			}

			String[] refundsLineStrings = new String[]{
				packagingRefunds.refundAmounts.get(refundIndex) + " kom", 
				ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex)), 
				ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex) * packagingRefunds.refundAmounts.get(refundIndex)), 
			};
			
			String refundsLineString = packagingRefunds.refundAmounts.get(refundIndex) + " kom" + "     " 
					+ ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex)) + "     "
					+ ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex) * packagingRefunds.refundAmounts.get(refundIndex));
			
			printTableExtraData.footerList.add(new Pair<>("", refundsLineString));
		}
		if(refundsTitlePrinted){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
		}
		
		// ZKI and JIR
		if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
			printTableExtraData.footerList.add(new Pair<>("ZKI:  ", invoice.zki));
			if(Values.DEFAULT_JIR.equals(invoice.jir)){
				printTableExtraData.footerList.add(new Pair<>("JIR:   ", "Nije dobiven u predviđenom vremenu"));
			} else {
				printTableExtraData.footerList.add(new Pair<>("JIR:   ", invoice.jir));
			}
                        SimpleDateFormat dFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
                        Date dtNow = new Date();
                        long raz = dtNow.getTime() - invoice.date.getTime();
                        long diffday = TimeUnit.DAYS.convert(raz, TimeUnit.MILLISECONDS);
                        if(!Values.DEFAULT_ZKI.equals(invoice.zki) && diffday < 30){
                            String uk = ClientAppUtils.FloatToPriceString(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
                            String amount = uk.replace(",", "").replace(".", "");
                            String QRurl = "https://porezna.gov.hr/rn?zki=" + invoice.zki + "&datv=" + dFormat.format(invoice.date) + "&izn=" + amount;
                            
                            printTableExtraData.footerList.add(new Pair<>("QR", QRurl));
                        }else{
                            printTableExtraData.footerList.add(new Pair<>("QR", "QR"));
                        }
                        
		} else {
			printTableExtraData.footerList.add(new Pair<>("Ovaj račun nije podložan fiskalizaciji", ""));
		}
		
		// E invoice
		if (!"".equals(invoice.einvoiceId)){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>("Broj e-računa: ", invoice.einvoiceId));
		}
		
		if (!invoice.isInVatSystem){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>(" ", "Obveznik nije u sustavu PDV-a, PDV nije obračunat temeljem čl. 90 st.2 Zakona o PDV-u."));
		}
		
		if(invoice.isTest){
			printTableExtraData.footerList.add(new Pair<>(" ", " "));
			printTableExtraData.footerList.add(new Pair<>("OVAJ RAČUN JE IZDAN U TESTNOM OKRUŽENJU", ""));
		}
		
		return printTableExtraData;
	}
}
