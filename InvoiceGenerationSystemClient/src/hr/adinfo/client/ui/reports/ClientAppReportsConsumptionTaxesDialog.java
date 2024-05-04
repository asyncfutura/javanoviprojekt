/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.reports;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReportsConsumptionTaxesDialog extends javax.swing.JDialog {

	private boolean setupDone;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppReportsConsumptionTaxesDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		
		final Window thisWindow = this;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				if(!thisWindow.isDisplayable()){
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
				}
				
				if(!thisWindow.isFocused())
					return false;
				
				if(ke.getID() == KeyEvent.KEY_PRESSED){
					if(ke.getKeyCode() == KeyEvent.VK_ESCAPE){
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerFrom.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTo.getUI();
						if (pickerUI1.isPopupVisible()) {
							pickerUI1.hidePopup();
							return false;
						}
						if (pickerUI2.isPopupVisible()) {
							pickerUI2.hidePopup();
							return false;
						}
						
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPosInvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4InvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerFrom.requestFocusInWindow();
					}
				}
				
				return false;
			}
		});
		
		jTableTaxes.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTaxes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTaxes.getTableHeader().setReorderingAllowed(false);
		jTableTaxes.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jXDatePickerFrom.setFormats("dd.MM.yyyy");
		jXDatePickerFrom.getEditor().setEditable(false);
		jXDatePickerFrom.setDate(new Date());
		jXDatePickerFrom.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerFrom.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerFrom.getMonthView() && e.getOppositeComponent() != null) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		jXDatePickerTo.setFormats("dd.MM.yyyy");
		jXDatePickerTo.getEditor().setEditable(false);
		jXDatePickerTo.setDate(new Date());
		jXDatePickerTo.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerTo.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerTo.getMonthView() && e.getOppositeComponent() != null) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
		
		setupDone = true;
		RefreshTable();
	}
	
	private void RefreshTable(){
		if(!setupDone)
			return;
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		Date dateFrom;
		Date dateTo;
		try {
			dateFrom = new SimpleDateFormat("dd.MM.yyyy").parse(dateFromString);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Od");
			return;
		}
		try {
			dateTo = new SimpleDateFormat("dd.MM.yyyy").parse(dateToString);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Do");
			return;
		}
		
		String timeFrom = "00:00:00";
		String timeTo = "23:59:59";
		if(jFormattedTextFieldTimeFrom.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeFrom.getValue();
			timeFrom = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		if(jFormattedTextFieldTimeTo.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeTo.getValue();
			timeTo = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLocalItems = "SELECT LOCAL_INVOICE_ITEMS.IT_ID, LOCAL_INVOICE_ITEMS.IT_NAME, LOCAL_INVOICE_ITEMS.IT_TYPE, "
				+ "LOCAL_INVOICE_ITEMS.AMT, LOCAL_INVOICE_ITEMS.PR, LOCAL_INVOICE_ITEMS.DIS_PCT, LOCAL_INVOICE_ITEMS.DIS_AMT, "
				+ "LOCAL_INVOICE_ITEMS.TAX, LOCAL_INVOICE_ITEMS.C_TAX, LOCAL_INVOICE_ITEMS.IN_ID, PAY_NAME, PAY_TYPE "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN LOCAL_INVOICE_ITEMS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
				
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND (I_DATE > ? OR (I_DATE >= ? AND I_TIME >= ?)) AND (I_DATE < ? OR (I_DATE <= ? AND I_TIME <= ?)) "
				+ "AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?, ?)"
				
				+ "ORDER BY LOCAL_INVOICE_ITEMS.IT_TYPE, LOCAL_INVOICE_ITEMS.IT_NAME";
		String queryLocalInvoices = "SELECT LOCAL_INVOICES.ID, LOCAL_INVOICES.FIN_PR, LOCAL_INVOICES.DIS_PCT, LOCAL_INVOICES.DIS_AMT, S_ID, "
				+ "STAFF.FIRST_NAME, (SELECT COUNT(LOCAL_INVOICE_ITEMS.ID) FROM LOCAL_INVOICE_ITEMS WHERE LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID), "
				+ "PAY_NAME, PAY_TYPE, I_TIME, I_DATE, I_NUM "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND (I_DATE > ? OR (I_DATE >= ? AND I_TIME >= ?)) AND (I_DATE < ? OR (I_DATE <= ? AND I_TIME <= ?)) "
				+ "AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?, ?)";
		String queryLocalArticlesList = "SELECT ARTICLES.ID, CONSUMPTION_TAXES.NAME, ARTICLES.CONSUMPTION_TAX_ID "
				+ "FROM ARTICLES "
				+ "INNER JOIN CONSUMPTION_TAXES ON ARTICLES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "ORDER BY ARTICLES.ID";
		String queryItems = queryLocalItems.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
		String queryInvoices = queryLocalInvoices.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryLocalItems = queryLocalItems.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			queryItems = queryItems.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(5);
		multiDatabaseQuery.SetQuery(0, queryLocalItems);
		multiDatabaseQuery.SetQuery(1, queryItems);
		multiDatabaseQuery.SetQuery(2, queryLocalInvoices);
		multiDatabaseQuery.SetQuery(3, queryInvoices);
		multiDatabaseQuery.SetQuery(4, queryLocalArticlesList);
		
		for (int i = 0; i < 4; ++i){
			multiDatabaseQuery.AddParam(i, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(i, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(i, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(i, 4, timeFrom);
			multiDatabaseQuery.AddParam(i, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(i, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(i, 7, timeTo);
			multiDatabaseQuery.AddParam(i, 8, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
			multiDatabaseQuery.AddParam(i, 9, Values.PAYMENT_METHOD_TYPE_OFFER);
			multiDatabaseQuery.AddParam(i, 10, Values.PAYMENT_METHOD_TYPE_SUBTOTAL);
		}
		
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
					ArrayList<InvoiceItem> items = new ArrayList<>();

					ArrayList<Integer> invoicesIdList = new ArrayList<>();
					ArrayList<Integer> invoicesNumList = new ArrayList<>();
					ArrayList<Integer> invoicesItemCountList = new ArrayList<>();
					ArrayList<Float> invoicesTotalPriceList = new ArrayList<>();
					ArrayList<Float> invoicesDiscountAmountList = new ArrayList<>();
					
					ArrayList<Integer> localInvoicesIdList = new ArrayList<>();
					ArrayList<String> localInvoicesNumList = new ArrayList<>();
					ArrayList<Integer> localInvoicesItemCountList = new ArrayList<>();
					ArrayList<Float> localInvoicesTotalPriceList = new ArrayList<>();
					ArrayList<Float> localInvoicesDiscountAmountList = new ArrayList<>();
					
					ArrayList<Integer> articlesIdList = new ArrayList<>();
					ArrayList<Integer> articlesConsTaxIdList = new ArrayList<>();
					ArrayList<String> articlesConsTaxNameList = new ArrayList<>();
					
					// Items
					while (databaseQueryResult[4].next()) {
						articlesIdList.add(databaseQueryResult[4].getInt(0));
						articlesConsTaxNameList.add(databaseQueryResult[4].getString(1));
						articlesConsTaxIdList.add(databaseQueryResult[4].getInt(2));
					}
					
					// Local invoices
					while (databaseQueryResult[2].next()) {
						if(localInvoicesNumList.contains(databaseQueryResult[2].getInt(11) + "/" + databaseQueryResult[2].getInt(10)))
							continue;
						
						localInvoicesIdList.add(databaseQueryResult[2].getInt(0));
						localInvoicesNumList.add(databaseQueryResult[2].getInt(11) + "/" + databaseQueryResult[2].getInt(10));
						localInvoicesItemCountList.add(databaseQueryResult[2].getInt(6));
						localInvoicesTotalPriceList.add(databaseQueryResult[2].getFloat(1));
						float discountAmount = databaseQueryResult[2].getFloat(1) * databaseQueryResult[2].getFloat(2) / 100f + databaseQueryResult[2].getFloat(3);
						localInvoicesDiscountAmountList.add(discountAmount);
					}
					
					// Invoices
					while (databaseQueryResult[3].next()) {
						if(invoicesNumList.contains(databaseQueryResult[3].getInt(11)))
							continue;
						
						invoicesIdList.add(databaseQueryResult[3].getInt(0));
						invoicesNumList.add(databaseQueryResult[3].getInt(11));
						invoicesItemCountList.add(databaseQueryResult[3].getInt(6));
						invoicesTotalPriceList.add(databaseQueryResult[3].getFloat(1));
						float discountAmount = databaseQueryResult[3].getFloat(1) * databaseQueryResult[3].getFloat(2) / 100f + databaseQueryResult[3].getFloat(3);
						invoicesDiscountAmountList.add(discountAmount);
					}
					
					// Local invoice items
					while (databaseQueryResult[0].next()) {
						if(!localInvoicesIdList.contains(databaseQueryResult[0].getInt(9)))
							continue;
						
						int itemId = databaseQueryResult[0].getInt(0);
						String itemName = databaseQueryResult[0].getString(1);
						int itemType = databaseQueryResult[0].getInt(2);
						float itemAmount = databaseQueryResult[0].getFloat(3);
						float itemPrice = databaseQueryResult[0].getFloat(4);
						float itemDisPct = databaseQueryResult[0].getFloat(5);
						float itemDisAmt = databaseQueryResult[0].getFloat(6);
						float itemTax = databaseQueryResult[0].getFloat(7);
						float itemConsTax = databaseQueryResult[0].getFloat(8);
						
						// Item type filter
						int invoiceListId = ClientAppUtils.ArrayIndexOf(localInvoicesIdList, databaseQueryResult[0].getInt(9));
						if(itemType != Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}
						
						// Consumption tax filter
						/*int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemId);
						int consTaxId = articlesConsTaxIdList.get(itemListId);
						if(consTaxId > 3){
							localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}*/
								
						// Item data
						float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
						float itemPriceWithoutDiscount = itemPrice * itemAmount;
						float itemToInvoicePriceRatio = (itemPriceWithoutDiscount - itemDiscount) / (localInvoicesTotalPriceList.get(invoiceListId) != 0f ? localInvoicesTotalPriceList.get(invoiceListId) : 1f);
						float itemInvoiceDiscount = itemToInvoicePriceRatio * localInvoicesDiscountAmountList.get(invoiceListId);

						itemInvoiceDiscount = (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f);
						float itemDiscountPerAmount = (itemAmount != 0f ? itemDiscount / itemAmount : 0f);
						itemDisPct = 0f;
						itemDisAmt = itemDiscountPerAmount + itemInvoiceDiscount;
						
						// Add item
						boolean itemFound = false;
						for (int i = 0; i < items.size(); ++i){
							if(items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
									&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
									&& items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax 
									&& items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == itemInvoiceDiscount) {
								itemFound = true;
								items.get(i).itemAmount += itemAmount;
							}
						}
						if(!itemFound){
							InvoiceItem invoiceItem = new InvoiceItem();
							invoiceItem.itemId = itemId;
							invoiceItem.itemName = itemName;
							invoiceItem.itemType = itemType;
							invoiceItem.itemAmount = itemAmount;
							invoiceItem.itemPrice = itemPrice;
							invoiceItem.discountPercentage = itemDisPct;
							invoiceItem.discountValue = itemDisAmt;
							invoiceItem.taxRate = itemTax;
							invoiceItem.consumptionTaxRate = itemConsTax;
							invoiceItem.invoiceDiscountTotal = itemInvoiceDiscount;
							items.add(invoiceItem);
						}
					}
					
					// Invoice items
					while (databaseQueryResult[1].next()) {
						if(!invoicesIdList.contains(databaseQueryResult[1].getInt(9)))
							continue;
						
						int itemId = databaseQueryResult[1].getInt(0);
						String itemName = databaseQueryResult[1].getString(1);
						int itemType = databaseQueryResult[1].getInt(2);
						float itemAmount = databaseQueryResult[1].getFloat(3);
						float itemPrice = databaseQueryResult[1].getFloat(4);
						float itemDisPct = databaseQueryResult[1].getFloat(5);
						float itemDisAmt = databaseQueryResult[1].getFloat(6);
						float itemTax = databaseQueryResult[1].getFloat(7);
						float itemConsTax = databaseQueryResult[1].getFloat(8);
						
						// Item type filter
						int invoiceListId = ClientAppUtils.ArrayIndexOf(invoicesIdList, databaseQueryResult[1].getInt(9));
						if(itemType != Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}
						
						// Consumption tax filter
						/*int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemId);
						int consTaxId = articlesConsTaxIdList.get(itemListId);
						if(consTaxId > 3){
							invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}*/
						
						// Item data
						float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
						float itemPriceWithoutDiscount = itemPrice * itemAmount;
						float itemToInvoicePriceRatio = (itemPriceWithoutDiscount - itemDiscount) / (invoicesTotalPriceList.get(invoiceListId) != 0f ? invoicesTotalPriceList.get(invoiceListId) : 1f);
						float itemInvoiceDiscount = itemToInvoicePriceRatio * invoicesDiscountAmountList.get(invoiceListId);
						
						itemInvoiceDiscount = (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f);
						float itemDiscountPerAmount = (itemAmount != 0f ? itemDiscount / itemAmount : 0f);
						itemDisPct = 0f;
						itemDisAmt = itemDiscountPerAmount + itemInvoiceDiscount;
						
						// Add item
						boolean itemFound = false;
						for (int i = 0; i < items.size(); ++i){
							if(items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
									&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
									&& items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax 
									&& items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == itemInvoiceDiscount) {
								itemFound = true;
								items.get(i).itemAmount += itemAmount;
							}
						}
						if(!itemFound){
							InvoiceItem invoiceItem = new InvoiceItem();
							invoiceItem.itemId = itemId;
							invoiceItem.itemName = itemName;
							invoiceItem.itemType = itemType;
							invoiceItem.itemAmount = itemAmount;
							invoiceItem.itemPrice = itemPrice;
							invoiceItem.discountPercentage = itemDisPct;
							invoiceItem.discountValue = itemDisAmt;
							invoiceItem.taxRate = itemTax;
							invoiceItem.consumptionTaxRate = itemConsTax;
							invoiceItem.invoiceDiscountTotal = itemInvoiceDiscount;
							
							items.add(invoiceItem);
						}
					}
					
					// Consumption taxes
					int maxConsTaxId = 4;
					for (int i = 0; i < items.size(); ++i){
						int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, items.get(i).itemId);
						int consTaxId = articlesConsTaxIdList.get(itemListId);
						if (consTaxId + 1 > maxConsTaxId){
							maxConsTaxId = consTaxId + 1;
						}
					}
					
					Invoice[] consTaxInvoices = new Invoice[maxConsTaxId];
					for (int i = 0; i < consTaxInvoices.length; ++i){
						consTaxInvoices[i] = new Invoice();
					}
					for (int i = 0; i < items.size(); ++i){
						int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, items.get(i).itemId);
						int consTaxId = articlesConsTaxIdList.get(itemListId);
						consTaxInvoices[consTaxId].items.add(items.get(i));
						
						float itemPrice = items.get(i).itemAmount * items.get(i).itemPrice;
						if(items.get(i).discountPercentage != 0f){
							itemPrice = itemPrice * (100f - items.get(i).discountPercentage) / 100f;
						} else if(items.get(i).discountValue != 0f){
							itemPrice = itemPrice - items.get(i).itemAmount * items.get(i).discountValue;
						}
						consTaxInvoices[consTaxId].totalPrice += itemPrice;
						//consTaxInvoices[consTaxId].totalPrice += items.get(i).itemAmount * items.get(i).itemPrice - items.get(i).invoiceDiscountTotal;
					}
					
					// Table taxes
					CustomTableModel customTableModelItems = new CustomTableModel();
					customTableModelItems.setColumnIdentifiers(new String[] {"R. B.", "Porez na potrošnju", "Oznaka za AOP", "Osnovica za obračun poreza", "Stopa %", "Obračunati iznos poreza", "Uplaćeni iznos poreza"});

					for (int i = 0; i < consTaxInvoices.length; ++i){
						InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(consTaxInvoices[i]);
						for (int j = 0; j < invoiceTaxes.taxRates.size(); ++j){
							//System.err.println(invoiceTaxes.taxBases.get(j)+ " " + invoiceTaxes.taxRates.get(j)+ " " + invoiceTaxes.taxAmounts.get(j) );
							if(invoiceTaxes.isConsumpionTax.get(j)){
								int consTaxListId = ClientAppUtils.ArrayIndexOf(articlesConsTaxIdList, i);
								String consTaxName = articlesConsTaxNameList.get(consTaxListId);
								
								Object[] rowData = new Object[7];
								rowData[0] = i + 1;
								rowData[1] = consTaxName;
								rowData[2] = i + 1;
								rowData[3] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(j));
								rowData[4] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(j)) + "%";
								rowData[5] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(j));
								rowData[6] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(j));
								customTableModelItems.addRow(rowData);
								//break;
							}
						}
					}
					
					jTableTaxes.setModel(customTableModelItems);				
					jTableTaxes.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTaxes.getWidth() * 10 / 100);
					jTableTaxes.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTaxes.getWidth() * 20 / 100);
					jTableTaxes.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTaxes.getWidth() * 20 / 100);
					jTableTaxes.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTaxes.getWidth() * 25 / 100);
					jTableTaxes.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTaxes.getWidth() * 15 / 100);
					jTableTaxes.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTaxes.getWidth() * 20 / 100);
					jTableTaxes.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTaxes.getWidth() * 20 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneTaxes = new javax.swing.JScrollPane();
        jTableTaxes = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jXDatePickerFrom = new org.jdesktop.swingx.JXDatePicker();
        jXDatePickerTo = new org.jdesktop.swingx.JXDatePicker();
        jButtonPrintPosInvoicesTable = new javax.swing.JButton();
        jButtonPrintA4InvoicesTable = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jFormattedTextFieldTimeFrom = new javax.swing.JFormattedTextField();
        jFormattedTextFieldTimeTo = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Porez na potrošnju");
        setResizable(false);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hr/adinfo/client/ui/adinfologo.jpg"))); // NOI18N

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()-2f));
        jLabel2.setText("mob: 095/6230-100");

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getSize()-2f));
        jLabel3.setText("mob: 091/6230-670");

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getSize()-2f));
        jLabel4.setText("fax: 01/6230-699");

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getSize()-2f));
        jLabel5.setText("tel: 01/6230-668");

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getSize()-2f));
        jLabel6.setText("office.accable@gmail.com");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(0, 48, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(2, 2, 2)
                .addComponent(jLabel3)
                .addGap(2, 2, 2)
                .addComponent(jLabel4)
                .addGap(2, 2, 2)
                .addComponent(jLabel5)
                .addGap(2, 2, 2)
                .addComponent(jLabel6)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelAdinfoLogoLayout = new javax.swing.GroupLayout(jPanelAdinfoLogo);
        jPanelAdinfoLogo.setLayout(jPanelAdinfoLogoLayout);
        jPanelAdinfoLogoLayout.setHorizontalGroup(
            jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelAdinfoLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanelAdinfoLogoLayout.setVerticalGroup(
            jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAdinfoLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Porez na potrošnju");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Porez na potrošnju"));

        jTableTaxes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPaneTaxes.setViewportView(jTableTaxes);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTaxes)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTaxes, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jXDatePickerFrom.setPreferredSize(new java.awt.Dimension(150, 25));
        jXDatePickerFrom.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerFromPropertyChange(evt);
            }
        });

        jXDatePickerTo.setPreferredSize(new java.awt.Dimension(150, 25));
        jXDatePickerTo.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerToPropertyChange(evt);
            }
        });

        jButtonPrintPosInvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPosInvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPosInvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintPosInvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosInvoicesTableActionPerformed(evt);
            }
        });

        jButtonPrintA4InvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis A4  <br> [F5] </div> </html>");
        jButtonPrintA4InvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4InvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4InvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4InvoicesTableActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jLabel1.setText("Od [F1]:");
        jLabel1.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(45, 14));

        jFormattedTextFieldTimeFrom.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextFieldTimeFrom.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextFieldTimeFrom.setText("00:00");
        jFormattedTextFieldTimeFrom.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldTimeFromPropertyChange(evt);
            }
        });

        jFormattedTextFieldTimeTo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextFieldTimeTo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jFormattedTextFieldTimeTo.setText("23:59");
        jFormattedTextFieldTimeTo.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldTimeToPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFormattedTextFieldTimeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldTimeTo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jFormattedTextFieldTimeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextFieldTimeTo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 522, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel9)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosInvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosInvoicesTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		String timeFrom = "00:00:00";
		String timeTo = "23:59:59";
		if(jFormattedTextFieldTimeFrom.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeFrom.getValue();
			timeFrom = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		if(jFormattedTextFieldTimeTo.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeTo.getValue();
			timeTo = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od: ", dateFromString + " " + timeFrom));
		printTableExtraData.headerList.add(new Pair<>("Do: ", dateToString + " " + timeTo));
		
		PrintUtils.PrintPosTable("Porez na potrošnju", jTableTaxes, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5, 6}}, printTableExtraData);
    }//GEN-LAST:event_jButtonPrintPosInvoicesTableActionPerformed

    private void jButtonPrintA4InvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoicesTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		String timeFrom = "00:00:00";
		String timeTo = "23:59:59";
		if(jFormattedTextFieldTimeFrom.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeFrom.getValue();
			timeFrom = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		if(jFormattedTextFieldTimeTo.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldTimeTo.getValue();
			timeTo = new SimpleDateFormat("HH:mm:00").format(notifDate);
		}
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od: ", dateFromString + " " + timeFrom));
		printTableExtraData.headerList.add(new Pair<>("Do: ", dateToString + " " + timeTo));
		
		PrintUtils.PrintA4Table("PorezNaPotrošnju", "Porez na potrošnju", jTableTaxes, new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{}, printTableExtraData, "");
    }//GEN-LAST:event_jButtonPrintA4InvoicesTableActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    private void jFormattedTextFieldTimeFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldTimeFromPropertyChange
        if(evt.getPropertyName().equals("editValid"))
			return;
		
		RefreshTable();
    }//GEN-LAST:event_jFormattedTextFieldTimeFromPropertyChange

    private void jFormattedTextFieldTimeToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldTimeToPropertyChange
        if(evt.getPropertyName().equals("editValid"))
			return;
		
		RefreshTable();
    }//GEN-LAST:event_jFormattedTextFieldTimeToPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4InvoicesTable;
    private javax.swing.JButton jButtonPrintPosInvoicesTable;
    private javax.swing.JFormattedTextField jFormattedTextFieldTimeFrom;
    private javax.swing.JFormattedTextField jFormattedTextFieldTimeTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneTaxes;
    private javax.swing.JTable jTableTaxes;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
