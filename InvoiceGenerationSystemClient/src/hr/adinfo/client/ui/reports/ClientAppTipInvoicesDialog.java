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
import static hr.adinfo.client.ClientAppUtils.GetInvoice;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.fiscalization.Fiscalization;
import static hr.adinfo.client.fiscalization.Fiscalization.FiscalizeInvoiceNapojnice;
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
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.Color;
import java.awt.Component;
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
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppTipInvoicesDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableLocalInvoicesIdList = new ArrayList<>();
	private ArrayList<Float> tableLocalInvoicesDiscountPercentageList = new ArrayList<>();
	private ArrayList<Float> tableLocalInvoicesDiscountAmountList = new ArrayList<>();
	private ArrayList<Float> tableLocalInvoicesFinalPriceList = new ArrayList<>();
	private ArrayList<String> tableLocalInvoicesNoteList = new ArrayList<>();
	private ArrayList<Integer> tableInvoicesIdList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesDiscountPercentageList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesDiscountAmountList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesFinalPriceList = new ArrayList<>();
	private ArrayList<String> tableInvoicesNoteList = new ArrayList<>();
	
	private ArrayList<Integer> clientsIdList = new ArrayList<>();
	private ArrayList<String> clientsNameList = new ArrayList<>();

	private boolean setupDone;
        private String napomenaNaIspisu = "";
        public String iznosNapojnice = "";
        public String tipPlacanja = "";
        public Integer invoiceId = 0;
        public boolean isLocal = false;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppTipInvoicesDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		ClientAppSettings.LoadSettings();
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerFrom.requestFocusInWindow();
					}
				} 
				
				return false;
			}
		});
		
		jTableItems.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableItems.getTableHeader().setReorderingAllowed(false);
		jTableItems.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableInvoices.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableInvoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableInvoices.getTableHeader().setReorderingAllowed(false);
		jTableInvoices.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneItems.getWidth() * 25 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		
		jTableInvoices.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableInvoices.getSelectedRow() == -1)
					return;
				
				int rowId = jTableInvoices.convertRowIndexToModel(jTableInvoices.getSelectedRow());
				int tableId = tableInvoicesIdList.get(rowId);
                                invoiceId = tableId;
				float finalPrice = tableInvoicesFinalPriceList.get(rowId);
				float discountPercentage = tableInvoicesDiscountPercentageList.get(rowId);
				float discountAmount = tableInvoicesDiscountAmountList.get(rowId);
				String note = tableInvoicesNoteList.get(rowId);
				boolean isInvoiceLocal = false;
				if(tableId == -1){
					tableId = tableLocalInvoicesIdList.get(rowId);
					finalPrice = tableLocalInvoicesFinalPriceList.get(rowId);
					discountPercentage = tableLocalInvoicesDiscountPercentageList.get(rowId);
					discountAmount = tableLocalInvoicesDiscountAmountList.get(rowId);
					note = tableLocalInvoicesNoteList.get(rowId);
					isInvoiceLocal = true;
				}
				
				String discount = "Popust na račun:  -";
				if(discountPercentage != 0f){
					discount = "Popust na račun:  " + discountPercentage + "% = " + ClientAppUtils.FloatToPriceString((finalPrice * discountPercentage) / 100f) + " kn";
					finalPrice = finalPrice * (100f - discountPercentage) / 100f;
				} else if(discountAmount != 0f){
					discount = "Popust na račun:  " + ClientAppUtils.FloatToPriceString(discountAmount) + " kn";
					finalPrice = finalPrice - discountAmount;
				}
				
				jLabelTotal.setText(ClientAppUtils.FloatToPriceString(finalPrice));
				jLabelDiscount.setText(discount);
				
				RefreshTableItems(tableId, isInvoiceLocal, note);
			}
		});
		
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
		LoadClientsList();
		RefreshTable();
	}
        
	
	private void LoadClientsList(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ID, NAME FROM CLIENTS";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
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
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<String> nameList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						idList.add(databaseQueryResult.getInt(0));
						nameList.add(databaseQueryResult.getString(1));
					}
					clientsIdList = idList;
					clientsNameList = nameList;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private String GetClientName(int clientId){
		for(int i = 0; i < clientsIdList.size(); ++i){
			if(clientsIdList.get(i) == clientId){
				return clientsNameList.get(i);
			}
		}
		
		return "";
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
		
		String payTypesString = "";
		int[] fiscTypes = new int[]{Values.PAYMENT_METHOD_TYPE_CASH, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CHECK, 
			Values.PAYMENT_METHOD_TYPE_OTHER, Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL, Values.PAYMENT_METHOD_TYPE_OTHER_NOT_FISCALIZED};
		for (int i = 0; i < fiscTypes.length; ++i){
			payTypesString += "".equals(payTypesString) ? fiscTypes[i] : ", " + fiscTypes[i];
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLocal = "SELECT LOCAL_INVOICES.ID, CR_NUM, I_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(LOCAL_INVOICE_ITEMS.AMT), SPEC_NUM, PAY_TYPE_2, IZNOS_NAPOJNICE "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				+ "INNER JOIN LOCAL_INVOICE_ITEMS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND PAY_TYPE IN (" + payTypesString + ") "
				+ "GROUP BY LOCAL_INVOICES.ID, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, PAY_TYPE_2, IZNOS_NAPOJNICE "
				+ "ORDER BY CR_NUM, I_NUM, PAY_TYPE";
		String query = "SELECT INVOICES.ID, CR_NUM, I_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, INVOICES.DIS_PCT, "
				+ "INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(INVOICE_ITEMS.AMT), SPEC_NUM, PAY_TYPE_2, IZNOS_NAPOJNICE "
				+ "FROM INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
				+ "INNER JOIN INVOICE_ITEMS ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
				+ "WHERE O_NUM = ? "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND PAY_TYPE IN (" + payTypesString + ") "
				+ "GROUP BY INVOICES.ID, CR_NUM, I_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, INVOICES.DIS_PCT, "
				+ "INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, PAY_TYPE_2, IZNOS_NAPOJNICE "
				+ "ORDER BY CR_NUM, I_NUM, PAY_TYPE";
                
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryLocal = queryLocal.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			query = query.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");;
		}
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, queryLocal);
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(0, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(0, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.SetQuery(1, query);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
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
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Datum", "Kasa", "Br. rač.", "Djelatnik", "Nač. plać.", "Tip rač.", "Ukupno", "Napojnica"});
					
					ArrayList<Integer> idListInvoices = new ArrayList<>();
					ArrayList<Integer> invoicesNumList = new ArrayList<>();
					ArrayList<Float> discountPercentageListInvoices = new ArrayList<>();
					ArrayList<Float> discountAmountListInvoices = new ArrayList<>();
					ArrayList<Float> finalPriceListInvoices = new ArrayList<>();
					ArrayList<String> noteListInvoices = new ArrayList<>();
					
					ArrayList<Integer> idListInvoicesLocal = new ArrayList<>();
					ArrayList<Integer> localInvoicesNumList = new ArrayList<>();
					ArrayList<Float> discountPercentageListInvoicesLocal = new ArrayList<>();
					ArrayList<Float> discountAmountListInvoicesLocal = new ArrayList<>();
					ArrayList<Float> finalPriceListInvoicesLocal = new ArrayList<>();
					ArrayList<String> noteListInvoicesLocal = new ArrayList<>();
					
					final ArrayList<Color> foregroundColor = new ArrayList<>();
					final ArrayList<Color> backgroundColor = new ArrayList<>();
					final ArrayList<Color> foregroundColorSelected = new ArrayList<>();
					final ArrayList<Color> backgroundColorSelected = new ArrayList<>();
					
					while (databaseQueryResult[0].next()) {
						if(localInvoicesNumList.contains(databaseQueryResult[0].getInt(2)))
							continue;
						
						localInvoicesNumList.add(databaseQueryResult[0].getInt(2));
						
						Object[] rowData = new Object[8];
						String dateString = databaseQueryResult[0].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[0].getString(4);
						rowData[1] = databaseQueryResult[0].getString(1);
						rowData[2] = databaseQueryResult[0].getInt(2) != 0 ? databaseQueryResult[0].getString(2) : databaseQueryResult[0].getString(17);
						rowData[3] = databaseQueryResult[0].getString(13) + " " + databaseQueryResult[0].getString(14);
						rowData[4] = databaseQueryResult[0].getInt(18) == -1 ? databaseQueryResult[0].getString(6) : "Više načina plaćanja";
						rowData[5] = databaseQueryResult[0].getInt(8) == -1 ? "Običan" : ("R1 - " + GetClientName(databaseQueryResult[0].getInt(8)));
						float totalPrice = databaseQueryResult[0].getFloat(11) * (100f - databaseQueryResult[0].getFloat(9)) / 100f - databaseQueryResult[0].getFloat(10);
						rowData[6] = "** " + ClientAppUtils.FloatToPriceString(totalPrice) + " **";
                                                rowData[7] = databaseQueryResult[0].getString(19);
						customTableModel.addRow(rowData);
						
						idListInvoices.add(-1);
						discountPercentageListInvoices.add(0f);
						discountAmountListInvoices.add(0f);
						finalPriceListInvoices.add(0f);
						noteListInvoices.add("");
						idListInvoicesLocal.add(databaseQueryResult[0].getInt(0));
						discountPercentageListInvoicesLocal.add(databaseQueryResult[0].getFloat(9));
						discountAmountListInvoicesLocal.add(databaseQueryResult[0].getFloat(10));
						finalPriceListInvoicesLocal.add(databaseQueryResult[0].getFloat(11));
						noteListInvoicesLocal.add(databaseQueryResult[0].getString(12));
						
						if(Values.DEFAULT_JIR.equals(databaseQueryResult[0].getString(15)) && ClientAppUtils.IsFiscalizationType(databaseQueryResult[0].getInt(7))){
							foregroundColor.add(Values.redForeground);
							backgroundColor.add(Values.redBackground);
							foregroundColorSelected.add(Values.redForegroundSelected);
							backgroundColorSelected.add(Values.redBackgroundSelected);
						} else if(databaseQueryResult[0].getFloat(16) < 0f) {
							foregroundColor.add(Values.orangeForeground);
							backgroundColor.add(Values.orangeBackground);
							foregroundColorSelected.add(Values.orangeForegroundSelected);
							backgroundColorSelected.add(Values.orangeBackgroundSelected);
						} else {
							foregroundColor.add(Values.normalForeground);
							backgroundColor.add(Values.normalBackground);
							foregroundColorSelected.add(Values.normalForegroundSelected);
							backgroundColorSelected.add(Values.normalBackgroundSelected);
						}
					}
					
					while (databaseQueryResult[1].next()) {
						invoicesNumList.add(databaseQueryResult[1].getInt(2));
						
						Object[] rowData = new Object[8];
						String dateString = databaseQueryResult[1].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[1].getString(4);
						rowData[1] = databaseQueryResult[1].getString(1);
						rowData[2] = databaseQueryResult[1].getInt(2) != 0 ? databaseQueryResult[1].getString(2) : databaseQueryResult[1].getString(17);
						rowData[3] = databaseQueryResult[1].getString(13) + " " + databaseQueryResult[1].getString(14);
						rowData[4] = databaseQueryResult[1].getInt(18) == -1 ? databaseQueryResult[1].getString(6) : "Više načina plaćanja";
						rowData[5] = databaseQueryResult[1].getInt(8) == -1 ? "Običan" : ("R1 - " + GetClientName(databaseQueryResult[1].getInt(8)));
						float totalPrice = databaseQueryResult[1].getFloat(11) * (100f - databaseQueryResult[1].getFloat(9)) / 100f - databaseQueryResult[1].getFloat(10);
						rowData[6] = ClientAppUtils.FloatToPriceString(totalPrice);
                                                rowData[7] = databaseQueryResult[1].getString(19);
						customTableModel.addRow(rowData);
						idListInvoices.add(databaseQueryResult[1].getInt(0));
						discountPercentageListInvoices.add(databaseQueryResult[1].getFloat(9));
						discountAmountListInvoices.add(databaseQueryResult[1].getFloat(10));
						finalPriceListInvoices.add(databaseQueryResult[1].getFloat(11));
						noteListInvoices.add(databaseQueryResult[1].getString(12));
						idListInvoicesLocal.add(-1);
						discountPercentageListInvoicesLocal.add(0f);
						discountAmountListInvoicesLocal.add(0f);
						finalPriceListInvoicesLocal.add(0f);
						noteListInvoicesLocal.add("");

						if(Values.DEFAULT_JIR.equals(databaseQueryResult[1].getString(15)) && ClientAppUtils.IsFiscalizationType(databaseQueryResult[1].getInt(7))){
							foregroundColor.add(Values.redForeground);
							backgroundColor.add(Values.redBackground);
							foregroundColorSelected.add(Values.redForegroundSelected);
							backgroundColorSelected.add(Values.redBackgroundSelected);
						} else if(databaseQueryResult[1].getFloat(16) < 0f) {
							foregroundColor.add(Values.orangeForeground);
							backgroundColor.add(Values.orangeBackground);
							foregroundColorSelected.add(Values.orangeForegroundSelected);
							backgroundColorSelected.add(Values.orangeBackgroundSelected);
						} else {
							foregroundColor.add(Values.normalForeground);
							backgroundColor.add(Values.normalBackground);
							foregroundColorSelected.add(Values.normalForegroundSelected);
							backgroundColorSelected.add(Values.normalBackgroundSelected);
						}
					}
					
					jTableInvoices.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
						@Override
						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
							final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
							if(isSelected){
								c.setForeground(foregroundColorSelected.get(row));
								c.setBackground(backgroundColorSelected.get(row));
							} else {
								c.setForeground(foregroundColor.get(row));
								c.setBackground(backgroundColor.get(row));
							}
							
							return c;
						}
					});
					
					jTableInvoices.setModel(customTableModel);
					tableInvoicesIdList = idListInvoices;
					tableInvoicesDiscountPercentageList = discountPercentageListInvoices;
					tableInvoicesDiscountAmountList = discountAmountListInvoices;
					tableInvoicesFinalPriceList = finalPriceListInvoices;
					tableInvoicesNoteList = noteListInvoices;
					tableLocalInvoicesIdList = idListInvoicesLocal;
					tableLocalInvoicesDiscountPercentageList = discountPercentageListInvoicesLocal;
					tableLocalInvoicesDiscountAmountList = discountAmountListInvoicesLocal;
					tableLocalInvoicesFinalPriceList = finalPriceListInvoicesLocal;
					tableLocalInvoicesNoteList = noteListInvoicesLocal;
					
					jTableInvoices.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneInvoices.getWidth() * 25 / 100);
					jTableInvoices.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneInvoices.getWidth() * 8 / 100);
					jTableInvoices.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneInvoices.getWidth() * 11 / 100);
					jTableInvoices.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneInvoices.getWidth() * 18 / 100);
					jTableInvoices.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneInvoices.getWidth() * 14 / 100);
					jTableInvoices.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneInvoices.getWidth() * 13 / 100);
					jTableInvoices.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneInvoices.getWidth() * 13 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		UpdateSorter();
	}
	
	private void RefreshTableItems(int itemId, boolean isInvoiceLocal, String note){
		final JDialog loadingDialog = new LoadingDialog(null, true);

		String query = "";
		if(isInvoiceLocal){
			query = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "WHERE IN_ID = ?";
		} else {
			query = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT "
					+ "FROM INVOICE_ITEMS "
					+ "WHERE IN_ID = ?";
		}
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			query = query.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, itemId);
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
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[6];
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
						rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
						String discount = "";
                                                String discountEur = "";
						float totalPrice = databaseQueryResult.getFloat(3) * databaseQueryResult.getFloat(2);
                                                float totalPriceEur = PrintUtils.floatCalculateExchangeRate(databaseQueryResult.getFloat(3) * databaseQueryResult.getFloat(2));
						if(databaseQueryResult.getFloat(4) != 0f){
							discount = databaseQueryResult.getFloat(4) + " %";                                                        
							totalPrice = totalPrice * (100f - databaseQueryResult.getFloat(4)) / 100f;
                                                        totalPriceEur = PrintUtils.floatCalculateExchangeRate(totalPrice * (100f - databaseQueryResult.getFloat(4)) / 100f);
						} else if(databaseQueryResult.getFloat(5) != 0f){
							discount = ClientAppUtils.FloatToPriceFloat(databaseQueryResult.getFloat(5)) + " kn/kom";
                                                        discountEur = PrintUtils.CalculateExchangeRate(databaseQueryResult.getFloat(5)) + " Eur/kom";
							totalPrice = totalPrice - databaseQueryResult.getFloat(5) * databaseQueryResult.getFloat(2);
                                                        totalPriceEur = PrintUtils.floatCalculateExchangeRate(totalPrice - databaseQueryResult.getFloat(5) * databaseQueryResult.getFloat(2));
						}
                                                if(discountEur != ""){
                                                    discount += " / " + discountEur;
                                                }
						rowData[4] = discount ;
						rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice); // + " / " + ClientAppUtils.FloatToPriceString(totalPriceEur);
						customTableModel.addRow(rowData);
					}
					
					jTableItems.setModel(customTableModel);
					
					jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
					jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneItems.getWidth() * 25 / 100);
					jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
					jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
					jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
					jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
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
        jScrollPaneInvoices = new javax.swing.JScrollPane();
        jTableInvoices = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneItems = new javax.swing.JScrollPane();
        jTableItems = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabelDiscount = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jXDatePickerFrom = new org.jdesktop.swingx.JXDatePicker();
        jXDatePickerTo = new org.jdesktop.swingx.JXDatePicker();
        jButtonExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jRadioButtonKartice = new javax.swing.JRadioButton();
        jRadioButtonGotovina = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldIznosNapojnice = new javax.swing.JTextField();
        jButtonSave = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldNapomenaNaIspisu = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Računi");
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
        jLabel9.setText("Dodaj napojnicu");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Računi"));

        jTableInvoices.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneInvoices.setViewportView(jTableInvoices);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneInvoices)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneInvoices, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke na računu"));

        jTableItems.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneItems.setViewportView(jTableItems);

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Ukupno:");
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabelTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTotal.setText("0.00");
        jLabelTotal.setToolTipText("");
        jLabelTotal.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("eur");

        jLabelDiscount.setText("Popust: -");
        jLabelDiscount.setPreferredSize(new java.awt.Dimension(70, 15));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneItems)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneItems, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabelDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel13.setText("Odaberi način plaćanja:");

        jRadioButtonKartice.setText("Kartica");

        jRadioButtonGotovina.setText("Gotovina");

        jLabel14.setText("Unesi iznos napojnice u EUR:");

        jButtonSave.setText("Spremi");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextFieldIznosNapojnice, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioButtonKartice, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelButtonsLayout.createSequentialGroup()
                    .addContainerGap(632, Short.MAX_VALUE)
                    .addComponent(jRadioButtonGotovina, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(248, 248, 248)))
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jTextFieldIznosNapojnice)
                            .addComponent(jLabel13)))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButtonKartice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jButtonSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 10, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelButtonsLayout.createSequentialGroup()
                    .addGap(11, 11, 11)
                    .addComponent(jRadioButtonGotovina, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel10.setText("Filter - način plaćanja:");

        jTextField1.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel12.setText("Filter - klijenti:");

        jTextField2.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        jLabel8.setText("Napomena na ispisu:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jTextFieldNapomenaNaIspisu, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)
                        .addComponent(jTextFieldNapomenaNaIspisu))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addGap(197, 197, 197)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(18, 20, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		UpdateSorter();
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
		UpdateSorter();
    }//GEN-LAST:event_jTextField2KeyReleased

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
                String iznosNapojnice = jTextFieldIznosNapojnice.getText();
                final JDialog loadingDialog = new LoadingDialog(null, true);
                String tipPlacanja = ""; Double doubleIznosNapojnice;
                boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
                int startOfficeNumber = Licence.GetOfficeNumber();
                String startOfficeTag = Licence.GetOfficeTag();
                int startCashRegisterNumber = Licence.GetCashRegisterNumber();
                String startCompanyOib = Licence.GetOIB();
                boolean isInVatSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

                try {
                    iznosNapojnice = iznosNapojnice.replace(',','.');
                    
                    doubleIznosNapojnice = Double.parseDouble(iznosNapojnice);
                    
                    if (jRadioButtonGotovina.isSelected()){
                        tipPlacanja = "G";
                    }
                    else if (jRadioButtonKartice.isSelected()){
                        tipPlacanja = "K";
                    }
                    else {
                         ClientAppLogger.GetInstance().ShowMessage("Molimo odaberite način plaćanja.");
                    }
                    
                    if (doubleIznosNapojnice <= 0 || doubleIznosNapojnice > 1000){
                       ClientAppLogger.GetInstance().ShowMessage("Molimo odaberite napojnicu u rasponu od 0 do 1000 eura.");
                       return;
                    }
                    
                    if (jTableInvoices.getSelectedRow() == -1){
                        ClientAppLogger.GetInstance().ShowMessage("Molimo odaberite račun kako biste mogli spremiti napojnicu.");
                        return;
                    }

                    if (doubleIznosNapojnice > 0 && tipPlacanja.length() > 0){
                        this.iznosNapojnice = doubleIznosNapojnice.toString();
                        this.tipPlacanja = tipPlacanja;
                    }

                    Invoice myInvoice = GetInvoice(invoiceId, isLocal);
                    myInvoice.iznosNapojnice = doubleIznosNapojnice.toString();
                    myInvoice.tipNapojnice = tipPlacanja;
                    myInvoice.isTest = !isProduction;
                    myInvoice.isInVatSystem = isInVatSystem;

                    FiscalizeInvoiceNapojnice(myInvoice, true);

                    String updateInvoiceNapojnice = "UPDATE USER1.INVOICES SET IZNOS_NAPOJNICE = ?, TIP_PLACANJA = ?, ZKI_NAPOJNICE = ?  WHERE ID = ?";

                    if(!isProduction){
                            updateInvoiceNapojnice = updateInvoiceNapojnice.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
                            isLocal = true;
                    }

                    String finalIznosNapojnice = doubleIznosNapojnice.toString();
                    DatabaseQuery databaseQuery = new DatabaseQuery(updateInvoiceNapojnice);
                    databaseQuery.AddParam(1, finalIznosNapojnice);
                    databaseQuery.AddParam(2, tipPlacanja);
                    databaseQuery.AddParam(3, myInvoice.zki);
                    databaseQuery.AddParam(4, invoiceId);
                    ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

                    databaseQueryTask.execute();
                            try {
                                    ServerResponse serverResponse = databaseQueryTask.get();
                                    DatabaseQueryResult databaseQueryResult = null;
                                    if (serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS) {
                                            databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
                                    }

                                    if (databaseQueryResult != null) {
                                               ClientAppLogger.GetInstance().ShowMessage("Uspješno spremljena napojnica.");
                                    }
                            } catch (Exception ex) {
                                    ClientAppLogger.GetInstance().ShowErrorLog(ex);
                            }   
                }
                catch (Exception ex){
                    ClientAppLogger.GetInstance().LogMessage(ex.getMessage());
                    ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške prilikom unosa napojnice. Pokušajte ponovno.");
                    Utils.DisposeDialog(this);
                }
                        
                Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSaveActionPerformed

	void UpdateSorter(){
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableInvoices.getModel());
		List<RowFilter<Object,Object>> filters = new ArrayList<>(2);
		filters.add(RowFilter.regexFilter("(?iu)" + jTextField1.getText(), 4));
		filters.add(RowFilter.regexFilter("(?iu)" + jTextField2.getText(), 5));
		sorter.setRowFilter(RowFilter.andFilter(filters));
        jTableInvoices.setRowSorter(sorter);
	}
        

        
        
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDiscount;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JRadioButton jRadioButtonGotovina;
    private javax.swing.JRadioButton jRadioButtonKartice;
    private javax.swing.JScrollPane jScrollPaneInvoices;
    private javax.swing.JScrollPane jScrollPaneItems;
    private javax.swing.JTable jTableInvoices;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextFieldIznosNapojnice;
    private javax.swing.JTextField jTextFieldNapomenaNaIspisu;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
