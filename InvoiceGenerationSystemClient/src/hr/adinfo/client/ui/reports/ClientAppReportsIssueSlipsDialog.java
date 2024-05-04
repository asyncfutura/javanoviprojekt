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
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.StaffUserInfo;
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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
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
public class ClientAppReportsIssueSlipsDialog extends javax.swing.JDialog {
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

	private boolean setupDone;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppReportsIssueSlipsDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPosInvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4InvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPrintPosInvoice.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonPrintA4Invoice.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonPrintPosAllInvoices.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F9){
						jButtonPrintA4AllInvoices.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jButtonMergeIssueSlips.doClick();
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
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN){
			jButtonMergeIssueSlips.setEnabled(false);
		}
		
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
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLocal = "SELECT LOCAL_INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(LOCAL_INVOICE_ITEMS.AMT), CLIENTS.NAME "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				+ "INNER JOIN LOCAL_INVOICE_ITEMS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
				+ "INNER JOIN CLIENTS ON CLIENTS.ID = LOCAL_INVOICES.C_ID "
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND LOCAL_INVOICES.PAY_TYPE = ?"
				+ "GROUP BY LOCAL_INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, CLIENTS.NAME "
				+ "ORDER BY CR_NUM, SPEC_NUM";
		String query = "SELECT INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, C_ID, INVOICES.DIS_PCT, INVOICES.DIS_AMT, FIN_PR, NOTE, "
				+ "STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(INVOICE_ITEMS.AMT), CLIENTS.NAME "
				+ "FROM INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
				+ "INNER JOIN INVOICE_ITEMS ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
				+ "INNER JOIN CLIENTS ON CLIENTS.ID = INVOICES.C_ID "
				+ "WHERE O_NUM = ? "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND INVOICES.PAY_TYPE = ? "
				+ "GROUP BY INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, INVOICES.DIS_PCT, "
				+ "INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, CLIENTS.NAME "
				+ "ORDER BY CR_NUM, SPEC_NUM ";
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
		multiDatabaseQuery.AddParam(0, 4, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
		multiDatabaseQuery.SetQuery(1, query);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(1, 4, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
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
					customTableModel.setColumnIdentifiers(new String[] {"Datum", "Br.izd.", "Kupac", "Djelatnik", "Nač. plać.", "Tip rač.", "Ukupno"});
					
					ArrayList<Integer> idListInvoices = new ArrayList<>();
					ArrayList<Float> discountPercentageListInvoices = new ArrayList<>();
					ArrayList<Float> discountAmountListInvoices = new ArrayList<>();
					ArrayList<Float> finalPriceListInvoices = new ArrayList<>();
					ArrayList<String> noteListInvoices = new ArrayList<>();
					ArrayList<Integer> idListInvoicesLocal = new ArrayList<>();
					ArrayList<Float> discountPercentageListInvoicesLocal = new ArrayList<>();
					ArrayList<Float> discountAmountListInvoicesLocal = new ArrayList<>();
					ArrayList<Float> finalPriceListInvoicesLocal = new ArrayList<>();
					ArrayList<String> noteListInvoicesLocal = new ArrayList<>();
					
					final ArrayList<Color> foregroundColor = new ArrayList<>();
					final ArrayList<Color> backgroundColor = new ArrayList<>();
					final ArrayList<Color> foregroundColorSelected = new ArrayList<>();
					final ArrayList<Color> backgroundColorSelected = new ArrayList<>();
					
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[7];
						String dateString = databaseQueryResult[0].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[0].getString(4);
						rowData[1] = databaseQueryResult[0].getString(2);
						rowData[2] = databaseQueryResult[0].getString(16);
						rowData[3] = databaseQueryResult[0].getString(12) + " " + databaseQueryResult[0].getString(13);
						rowData[4] = databaseQueryResult[0].getString(6);
						rowData[5] = databaseQueryResult[0].getInt(7) == -1 ? "Običan" : "R1";
						float totalPrice = databaseQueryResult[0].getFloat(10) * (100f - databaseQueryResult[0].getFloat(8)) / 100f - databaseQueryResult[0].getFloat(9);
						rowData[6] = "** " + ClientAppUtils.FloatToPriceString(totalPrice) + " **";
						customTableModel.addRow(rowData);
						
						idListInvoices.add(-1);
						discountPercentageListInvoices.add(0f);
						discountAmountListInvoices.add(0f);
						finalPriceListInvoices.add(0f);
						noteListInvoices.add("");
						idListInvoicesLocal.add(databaseQueryResult[0].getInt(0));
						discountPercentageListInvoicesLocal.add(databaseQueryResult[0].getFloat(8));
						discountAmountListInvoicesLocal.add(databaseQueryResult[0].getFloat(9));
						finalPriceListInvoicesLocal.add(databaseQueryResult[0].getFloat(10));
						noteListInvoicesLocal.add(databaseQueryResult[0].getString(11));

						if(databaseQueryResult[0].getFloat(15) < 0f) {
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
						Object[] rowData = new Object[7];
						String dateString = databaseQueryResult[1].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[1].getString(4);
						rowData[1] = databaseQueryResult[1].getString(2);
						rowData[2] = databaseQueryResult[1].getString(16);
						rowData[3] = databaseQueryResult[1].getString(12) + " " + databaseQueryResult[1].getString(13);
						rowData[4] = databaseQueryResult[1].getString(6);
						rowData[5] = databaseQueryResult[1].getInt(7) == -1 ? "Običan" : "R1";
						float totalPrice = databaseQueryResult[1].getFloat(10) * (100f - databaseQueryResult[1].getFloat(8)) / 100f - databaseQueryResult[1].getFloat(9);
						rowData[6] = ClientAppUtils.FloatToPriceString(totalPrice);
						customTableModel.addRow(rowData);
						
						idListInvoices.add(databaseQueryResult[1].getInt(0));
						discountPercentageListInvoices.add(databaseQueryResult[1].getFloat(8));
						discountAmountListInvoices.add(databaseQueryResult[1].getFloat(9));
						finalPriceListInvoices.add(databaseQueryResult[1].getFloat(10));
						noteListInvoices.add(databaseQueryResult[1].getString(11));
						idListInvoicesLocal.add(-1);
						discountPercentageListInvoicesLocal.add(0f);
						discountAmountListInvoicesLocal.add(0f);
						finalPriceListInvoicesLocal.add(0f);
						noteListInvoicesLocal.add("");
						
						if(databaseQueryResult[1].getFloat(15) < 0f) {
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
					
					jTableInvoices.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneInvoices.getWidth() * 22 / 100);
					jTableInvoices.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneInvoices.getWidth() * 12 / 100);
					jTableInvoices.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneInvoices.getWidth() * 19 / 100);
					jTableInvoices.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneInvoices.getWidth() * 17 / 100);
					jTableInvoices.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneInvoices.getWidth() * 17 / 100);
					jTableInvoices.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneInvoices.getWidth() * 11 / 100);
					jTableInvoices.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneInvoices.getWidth() * 12 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
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
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
						String discount = "";
						float totalPrice = databaseQueryResult.getFloat(3) * databaseQueryResult.getFloat(2);
						if(databaseQueryResult.getFloat(4) != 0f){
							discount = databaseQueryResult.getFloat(4) + " %";
							totalPrice = totalPrice * (100f - databaseQueryResult.getFloat(4)) / 100f;
						} else if(databaseQueryResult.getFloat(5) != 0f){
							discount = ClientAppUtils.FloatToPriceFloat(databaseQueryResult.getFloat(5)) + " kn/kom";
							totalPrice = totalPrice - databaseQueryResult.getFloat(5) * databaseQueryResult.getFloat(2);
						}
						rowData[4] = discount;
						rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice);
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
        jButtonPrintPosInvoicesTable = new javax.swing.JButton();
        jButtonPrintA4InvoicesTable = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonPrintPosInvoice = new javax.swing.JButton();
        jButtonPrintA4Invoice = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jButtonPrintPosAllInvoices = new javax.swing.JButton();
        jButtonPrintA4AllInvoices = new javax.swing.JButton();
        jButtonMergeIssueSlips = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Izdatnice");
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
        jLabel9.setText("Izdatnice");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Izdatnice"));

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
                .addComponent(jScrollPaneInvoices, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke na izdatnici"));

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
        jLabel17.setText("kn ");

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
                .addComponent(jScrollPaneItems, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
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

        jButtonPrintPosInvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> lista izdatnica <br> [F4] </div> </html>");
        jButtonPrintPosInvoicesTable.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosInvoicesTable.setPreferredSize(new java.awt.Dimension(85, 60));
        jButtonPrintPosInvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosInvoicesTableActionPerformed(evt);
            }
        });

        jButtonPrintA4InvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> lista izdatnica <br> [F5] </div> </html>");
        jButtonPrintA4InvoicesTable.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4InvoicesTable.setPreferredSize(new java.awt.Dimension(85, 60));
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

        jButtonPrintPosInvoice.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> izdatnica <br>  [F6] </div> </html>");
        jButtonPrintPosInvoice.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosInvoice.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintPosInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosInvoiceActionPerformed(evt);
            }
        });

        jButtonPrintA4Invoice.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> izdatnica <br> [F7] </div> </html>");
        jButtonPrintA4Invoice.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4Invoice.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4Invoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4InvoiceActionPerformed(evt);
            }
        });

        jLabel1.setText("Od [F1]:");
        jLabel1.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(45, 14));

        jButtonPrintPosAllInvoices.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> sve izdatnice <br>  [F8] </div> </html>");
        jButtonPrintPosAllInvoices.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosAllInvoices.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintPosAllInvoices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosAllInvoicesActionPerformed(evt);
            }
        });

        jButtonPrintA4AllInvoices.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> sve izdatnice <br> [F9] </div> </html>");
        jButtonPrintA4AllInvoices.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4AllInvoices.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintA4AllInvoices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4AllInvoicesActionPerformed(evt);
            }
        });

        jButtonMergeIssueSlips.setText("<html> <div style=\"text-align: center\"> Spoji izdatnice <br> u račun <br> [F3] </div> </html>");
        jButtonMergeIssueSlips.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonMergeIssueSlips.setPreferredSize(new java.awt.Dimension(90, 60));
        jButtonMergeIssueSlips.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMergeIssueSlipsActionPerformed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonMergeIssueSlips, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4Invoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosAllInvoices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4AllInvoices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonPrintA4Invoice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonMergeIssueSlips, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButtonPrintPosInvoice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButtonPrintA4AllInvoices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintPosAllInvoices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel10.setText("Filter");

        jTextField2.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(723, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
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
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintPosTable("Lista izdatnica", jTableInvoices, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5, 6 }}, printTableExtraData);
    }//GEN-LAST:event_jButtonPrintPosInvoicesTableActionPerformed

    private void jButtonPrintA4InvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoicesTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintA4Table("ListaIzdatnica", "Lista izdatnica", jTableInvoices, new int[]{0, 1, 2, 3, 4, 5, 6 }, new int[]{}, printTableExtraData, "");
    }//GEN-LAST:event_jButtonPrintA4InvoicesTableActionPerformed

    private void jButtonPrintPosInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosInvoiceActionPerformed
		if(jTableInvoices.getSelectedRow() == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite izdatnicu u tablici koju želite ispisati.");
			return;
		}
		
		int rowId = jTableInvoices.convertRowIndexToModel(jTableInvoices.getSelectedRow());
		int tableId = tableInvoicesIdList.get(rowId);
		boolean isInvoiceLocal = false;
		if(tableId == -1){
			tableId = tableLocalInvoicesIdList.get(rowId);
			isInvoiceLocal = true;
		}
		
		Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
		
		PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
    }//GEN-LAST:event_jButtonPrintPosInvoiceActionPerformed

    private void jButtonPrintA4InvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoiceActionPerformed
		if(jTableInvoices.getSelectedRow() == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite izdatnicu u tablici koju želite ispisati.");
			return;
		}
		
		int rowId = jTableInvoices.convertRowIndexToModel(jTableInvoices.getSelectedRow());
		int tableId = tableInvoicesIdList.get(rowId);
		boolean isInvoiceLocal = false;
		if(tableId == -1){
			tableId = tableLocalInvoicesIdList.get(rowId);
			isInvoiceLocal = true;
		}
		
		Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
		
		// Invoice discount
		String discountPrefix = "";
		String discountSufix = "";
		if(invoice.discountPercentage != 0f){
			discountPrefix = "Popust (" + invoice.discountPercentage + " %) = ";
			discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.totalPrice * invoice.discountPercentage / 100f);
		} else if(invoice.discountValue != 0f){
			discountPrefix = "Popust (" + invoice.discountValue + " kn) = ";
			discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.discountValue);
		}
		float totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
		
		// Taxes table
		JTable tempJTable = new JTable();
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"PDV", "Osnovica", "Iznos"});
		InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
		for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
			if(invoiceTaxes.taxRates.get(i) == 0f)
				continue;
			
			Object[] rowData = new Object[3];
			rowData[0] = invoiceTaxes.taxRates.get(i) + "%";
			rowData[1] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i));
			rowData[2] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i));
			customTableModel.addRow(rowData);
		}
		tempJTable.setModel(customTableModel);
		tempJTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		tempJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
		tempJTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		
		// Extra data
		PrintTableExtraData printTableExtraData1 = new PrintTableExtraData();
		if(invoice.isCopy){
			//printTableExtraData1.headerList.add(new Pair<>("KOPIJA RAČUNA", ""));
			printTableExtraData1.headerList.add(new Pair<>(" ", " "));
		}
		printTableExtraData1.headerList.add(new Pair<>("Datum:                    ", new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(invoice.date)));
		printTableExtraData1.headerList.add(new Pair<>("Oznaka djelatnika:  ", "" + invoice.staffId + "-" + invoice.staffName));
		if(invoice.clientId != -1){
			printTableExtraData1.headerList.add(new Pair<>(" ", " "));
			printTableExtraData1.headerList.add(new Pair<>("Kupac:           ", invoice.clientName));
			printTableExtraData1.headerList.add(new Pair<>("OIB kupca:    ", invoice.clientOIB));
		}
		if(!"".equals(discountPrefix)){
			printTableExtraData1.footerList.add(new Pair<>("", discountPrefix + discountSufix));
		}
		printTableExtraData1.footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " kn", ""));
		printTableExtraData1.footerList.add(new Pair<>(" ", " "));
		printTableExtraData1.footerList.add(new Pair<>("", "Način plaćanja: " + invoice.paymentMethodName));
		if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(invoice.date);
			calendar.add(Calendar.DATE, invoice.paymentDelay);
			printTableExtraData1.footerList.add(new Pair<>("Rok plaćanja:    ", new SimpleDateFormat("dd.MM.yyyy.").format(calendar.getTime())));
		}
		if(!"".equals(invoice.note)){
			printTableExtraData1.footerList.add(new Pair<>(" ", " "));
			printTableExtraData1.footerList.add(new Pair<>("Napomena: ", invoice.note));
		}
		
		PrintTableExtraData printTableExtraData2 = new PrintTableExtraData();
		printTableExtraData2.headerList.add(new Pair<>("Razrada poreza:", ""));
		
		// ZKI and JIR
		if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
			printTableExtraData2.footerList.add(new Pair<>("ZKI:  ", invoice.zki));
			if(Values.DEFAULT_JIR.equals(invoice.jir)){
				printTableExtraData2.footerList.add(new Pair<>("JIR:   ", "Nije dobiven u predviđenom vremenu"));
			} else {
				printTableExtraData2.footerList.add(new Pair<>("JIR:   ", invoice.jir));
			}
		} else {
			printTableExtraData2.footerList.add(new Pair<>("Ovaj račun nije podložan fiskalizaciji", ""));
		}
		
		// E invoice
		if (!"".equals(invoice.einvoiceId)){
			printTableExtraData2.footerList.add(new Pair<>(" ", " "));
			printTableExtraData2.footerList.add(new Pair<>("Broj e-računa: ", invoice.einvoiceId));
		}
		
		if (!invoice.isInVatSystem){
			printTableExtraData2.footerList.add(new Pair<>(" ", " "));
			printTableExtraData2.footerList.add(new Pair<>(" ", "Obveznik nije u sustavu PDV-a, PDV nije obračunat temeljem čl. 90 st.2 Zakona o PDV-u."));
		}
		
		if(invoice.isTest){
			printTableExtraData2.footerList.add(new Pair<>(" ", " "));
			printTableExtraData2.footerList.add(new Pair<>("!!! OVAJ RAČUN JE IZDAN U TESTNOM OKRUŽENJU !!!", ""));
		}
		
		PrintUtils.PrintA4Table("Izdatnica-" + invoice.specialNumber + "-" + invoice.officeTag, 
				new String[]{"Broj izdatnice: " + invoice.specialNumber + "/" + invoice.officeTag, ""}, 
				new JTable[]{jTableItems, tempJTable}, 
				new int[][]{new int[]{ 1, 2, 3, 4, 5 }, new int[]{ 0, 1, 2 }}, 
				new int[][]{new int[]{ }, new int[]{ }}, 
				new PrintTableExtraData[]{ printTableExtraData1, printTableExtraData2 }, 
				new boolean[]{false, false}, "");
    }//GEN-LAST:event_jButtonPrintA4InvoiceActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    private void jButtonPrintPosAllInvoicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosAllInvoicesActionPerformed
		for(int invoiceIndex = 0; invoiceIndex < jTableInvoices.getRowCount(); ++invoiceIndex){
			int tableId = tableInvoicesIdList.get(invoiceIndex);
			boolean isInvoiceLocal = false;
			if(tableId == -1){
				tableId = tableLocalInvoicesIdList.get(invoiceIndex);
				isInvoiceLocal = true;
			}

			Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);

			PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
		}
    }//GEN-LAST:event_jButtonPrintPosAllInvoicesActionPerformed

    private void jButtonPrintA4AllInvoicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4AllInvoicesActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		JTable[] tempJTables = new JTable[jTableInvoices.getRowCount() * 2];
		PrintTableExtraData[] extraData = new PrintTableExtraData[jTableInvoices.getRowCount() * 2];
		String[] tableTitle = new String[jTableInvoices.getRowCount() * 2];
		
		for (int invoiceIndex = 0; invoiceIndex < jTableInvoices.getRowCount(); ++invoiceIndex){
			int tableId = tableInvoicesIdList.get(invoiceIndex);
			boolean isInvoiceLocal = false;
			if(tableId == -1){
				tableId = tableLocalInvoicesIdList.get(invoiceIndex);
				isInvoiceLocal = true;
			}

			Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
			tableTitle[2 * invoiceIndex] = "Broj izdatnice: " + invoice.specialNumber + "/" + invoice.officeTag;
			tableTitle[2 * invoiceIndex + 1] = "";
			
			// Invoice discount
			String discountPrefix = "";
			String discountSufix = "";
			if(invoice.discountPercentage != 0f){
				discountPrefix = "Popust (" + invoice.discountPercentage + " %) = ";
				discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.totalPrice * invoice.discountPercentage / 100f);
			} else if(invoice.discountValue != 0f){
				discountPrefix = "Popust (" + invoice.discountValue + " kn) = ";
				discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.discountValue);
			}
			float totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;

			// Items table
			tempJTables[2 * invoiceIndex] = new JTable();
			CustomTableModel customTableModelItems = new CustomTableModel();
			customTableModelItems.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
			for (int i = 0; i < invoice.items.size(); ++i){
				Object[] rowData = new Object[6];
				rowData[0] = invoice.items.get(i).itemId;
				rowData[1] = invoice.items.get(i).itemName;
				rowData[2] = invoice.items.get(i).itemAmount;
				rowData[3] = ClientAppUtils.FloatToPriceString(invoice.items.get(i).itemPrice);
				String discount = "";
				float totalPrice = invoice.items.get(i).itemPrice * invoice.items.get(i).itemAmount;
				if(invoice.items.get(i).discountPercentage != 0f){
					discount = invoice.items.get(i).discountPercentage + " %";
					totalPrice = totalPrice * (100f - invoice.items.get(i).discountPercentage) / 100f;
				} else if(invoice.items.get(i).discountValue != 0f){
					discount = ClientAppUtils.FloatToPriceFloat(invoice.items.get(i).discountValue) + " kn/kom";
					totalPrice = totalPrice - invoice.items.get(i).discountValue * invoice.items.get(i).itemAmount;
				}
				rowData[4] = discount;
				rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice);
				customTableModelItems.addRow(rowData);
			}
			tempJTables[2 * invoiceIndex].setModel(customTableModelItems);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(0).setPreferredWidth(15);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(1).setPreferredWidth(25);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(2).setPreferredWidth(15);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(3).setPreferredWidth(15);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(4).setPreferredWidth(15);
			tempJTables[2 * invoiceIndex].getColumnModel().getColumn(5).setPreferredWidth(15);
			
			// Taxes table
			tempJTables[2 * invoiceIndex + 1] = new JTable();
			CustomTableModel customTableModelTaxes = new CustomTableModel();
			customTableModelTaxes.setColumnIdentifiers(new String[] {"PDV", "Osnovica", "Iznos"});
			InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.taxRates.get(i) == 0f)
					continue;
			
				Object[] rowData = new Object[3];
				rowData[0] = invoiceTaxes.taxRates.get(i) + "%";
				rowData[1] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i));
				rowData[2] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i));
				customTableModelTaxes.addRow(rowData);
			}
			tempJTables[2 * invoiceIndex + 1].setModel(customTableModelTaxes);
			tempJTables[2 * invoiceIndex + 1].getColumnModel().getColumn(0).setPreferredWidth(30);
			tempJTables[2 * invoiceIndex + 1].getColumnModel().getColumn(1).setPreferredWidth(30);
			tempJTables[2 * invoiceIndex + 1].getColumnModel().getColumn(2).setPreferredWidth(30);
			
			// Extra data
			extraData[2 * invoiceIndex] = new PrintTableExtraData();
			if(invoice.isCopy){
				//extraData[2 * invoiceIndex].headerList.add(new Pair<>("KOPIJA RAČUNA", ""));
				extraData[2 * invoiceIndex].headerList.add(new Pair<>(" ", " "));
			}
			extraData[2 * invoiceIndex].headerList.add(new Pair<>("Datum:                    ", new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(invoice.date)));
			extraData[2 * invoiceIndex].headerList.add(new Pair<>("Oznaka djelatnika:  ", "" + invoice.staffId + "-" + invoice.staffName));
			if(invoice.clientId != -1){
				extraData[2 * invoiceIndex].headerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex].headerList.add(new Pair<>("Kupac:           ", invoice.clientName));
				extraData[2 * invoiceIndex].headerList.add(new Pair<>("OIB kupca:    ", invoice.clientOIB));
			}
			if(!"".equals(discountPrefix)){
				extraData[2 * invoiceIndex].footerList.add(new Pair<>("", discountPrefix + discountSufix));
			}
			extraData[2 * invoiceIndex].footerList.add(new Pair<>("Ukupno: " + ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " kn", ""));
			extraData[2 * invoiceIndex].footerList.add(new Pair<>(" ", " "));
			extraData[2 * invoiceIndex].footerList.add(new Pair<>("", "Način plaćanja: " + invoice.paymentMethodName));
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(invoice.date);
				calendar.add(Calendar.DATE, invoice.paymentDelay);
				extraData[2 * invoiceIndex].footerList.add(new Pair<>("Rok plaćanja:    ", new SimpleDateFormat("dd.MM.yyyy.").format(calendar.getTime())));
			}
			if(!"".equals(invoice.note)){
				extraData[2 * invoiceIndex].footerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex].footerList.add(new Pair<>("Napomena: ", invoice.note));
			}

			extraData[2 * invoiceIndex + 1] = new PrintTableExtraData();
			extraData[2 * invoiceIndex + 1].headerList.add(new Pair<>("Razrada poreza:", ""));
			
			// ZKI and JIR
			if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("ZKI:  ", invoice.zki));
				if(Values.DEFAULT_JIR.equals(invoice.jir)){
					extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("JIR:   ", "Nije dobiven u predviđenom vremenu"));
				} else {
					extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("JIR:   ", invoice.jir));
				}
			} else {
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("Ovaj račun nije podložan fiskalizaciji", ""));
			}
			
			// E invoice
			if (!"".equals(invoice.einvoiceId)){
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("Broj e-računa: ", invoice.einvoiceId));
			}
			
			if (!invoice.isInVatSystem){
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>(" ", "Obveznik nije u sustavu PDV-a, PDV nije obračunat temeljem čl. 90 st.2 Zakona o PDV-u."));
			}
			
			if(invoice.isTest){
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex + 1].footerList.add(new Pair<>("!!! OVAJ RAČUN JE IZDAN U TESTNOM OKRUŽENJU !!!", ""));
			}
		}
		
		int[][] mergeIndex = new int[tempJTables.length][];
		int[][] columnIndex = new int[tempJTables.length][];
		boolean[] newPageBoolean = new boolean[tempJTables.length];
		for (int i = 0; i < tempJTables.length / 2; ++i){
			columnIndex[2*i] = new int[]{1, 2, 3, 4, 5};
			mergeIndex[2*i] = new int[]{};
			newPageBoolean[2*i] = false;
			
			columnIndex[2*i + 1] = new int[]{0, 1, 2};
			mergeIndex[2*i + 1] = new int[]{};
			newPageBoolean[2*i + 1] = true;
		}
		
		PrintUtils.PrintA4Table("IzdatniceOd" + dateFromString + "Do" + dateToString, 
				tableTitle,
				tempJTables, 
				columnIndex,
				mergeIndex, 
				extraData, 
				newPageBoolean, "");	
    }//GEN-LAST:event_jButtonPrintA4AllInvoicesActionPerformed

    private void jButtonMergeIssueSlipsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMergeIssueSlipsActionPerformed
		new ClientAppReportsIssueSlipsMergeDialog(null, true).setVisible(true);
		RefreshTable();
    }//GEN-LAST:event_jButtonMergeIssueSlipsActionPerformed

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
        String searchString = jTextField2.getText();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableInvoices.getModel());
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
        jTableInvoices.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField2KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonMergeIssueSlips;
    private javax.swing.JButton jButtonPrintA4AllInvoices;
    private javax.swing.JButton jButtonPrintA4Invoice;
    private javax.swing.JButton jButtonPrintA4InvoicesTable;
    private javax.swing.JButton jButtonPrintPosAllInvoices;
    private javax.swing.JButton jButtonPrintPosInvoice;
    private javax.swing.JButton jButtonPrintPosInvoicesTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneInvoices;
    private javax.swing.JScrollPane jScrollPaneItems;
    private javax.swing.JTable jTableInvoices;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
