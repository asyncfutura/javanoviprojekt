/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.warehouse;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseCardDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableMaterialsIdList = new ArrayList<>();
	private ArrayList<Integer> tableTradingGoodsIdList = new ArrayList<>();

	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppWarehouseCardDialog(java.awt.Frame parent, boolean modal) {
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
						jButtonPrintPos.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPrintPosAll.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonPrintA4All.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F2){
						jTabbedPane1.setSelectedIndex(0);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerFrom.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTo.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTextField1.requestFocusInWindow();
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jTabbedPane1.setSelectedIndex(1);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerFrom.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTo.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTextField2.requestFocusInWindow();
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerFrom.requestFocusInWindow();
					}
				}
				
				return false;
			}
		});
		
		jTableDocuments.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableDocuments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableDocuments.getTableHeader().setReorderingAllowed(false);
		jTableDocuments.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableMaterials.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableMaterials.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMaterials.getTableHeader().setReorderingAllowed(false);
		jTableMaterials.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableTradingGoods.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTradingGoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTradingGoods.getTableHeader().setReorderingAllowed(false);
		jTableTradingGoods.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Datum", "Tip dokumenta", "Oznaka dokumenta", "Ulaz", "Izlaz"});
		jTableDocuments.setModel(customTableModel);
		jTableDocuments.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneDocuments.getWidth() * 20 / 100);
		jTableDocuments.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneDocuments.getWidth() * 25 / 100);
		jTableDocuments.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneDocuments.getWidth() * 30 / 100);
		jTableDocuments.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneDocuments.getWidth() * 15 / 100);
		jTableDocuments.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneDocuments.getWidth() * 15 / 100);

		RefreshTablesMaterialsTradingGoods();
		
		jTableMaterials.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableMaterials.getSelectedRow() == -1)
					return;
				
				RefreshTableDocuments();
			}
		});
		
		jTableTradingGoods.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableTradingGoods.getSelectedRow() == -1)
					return;
				
				RefreshTableDocuments();
			}
		});
		
		jXDatePickerFrom.setFormats("dd.MM.yyyy");
		jXDatePickerFrom.getEditor().setEditable(false);
		jXDatePickerFrom.setDate(new Date());
		jXDatePickerFrom.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerFrom.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerFrom.getMonthView() && e.getOppositeComponent() != jTabbedPane1 && e.getOppositeComponent() != null) {
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
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerTo.getMonthView() && e.getOppositeComponent() != jTabbedPane1 && e.getOppositeComponent() != null) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
		Set setForward1 = new HashSet(jTextField1.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackward1 = new HashSet(jTextField1.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForward1.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackward1.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTextField1.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward1);
		jTextField1.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward1);
		Set setForward2 = new HashSet(jTextField2.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackward2 = new HashSet(jTextField2.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForward2.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackward2.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTextField2.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward2);
		jTextField2.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward2);
	}
	
	private void RefreshTablesMaterialsTradingGoods(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query1 = "SELECT MATERIALS.ID, MATERIALS.NAME "
				+ "FROM MATERIALS "
				+ "WHERE MATERIALS.IS_DELETED = 0";
		String query2 = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME "
				+ "FROM TRADING_GOODS "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, query1);
		multiDatabaseQuery.SetQuery(1, query2);
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
					// Materials
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv"});
					ArrayList<Integer> idListMaterials = new ArrayList<>();
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[2];
						rowData[0] = databaseQueryResult[0].getString(0);
						rowData[1] = databaseQueryResult[0].getString(1);
						customTableModel.addRow(rowData);
						idListMaterials.add(databaseQueryResult[0].getInt(0));
					}
					jTableMaterials.setModel(customTableModel);
					tableMaterialsIdList = idListMaterials;
					jTableMaterials.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
					jTableMaterials.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneMaterials.getWidth() * 80 / 100);
					
					// Trading goods
					CustomTableModel customTableModelTradingGoods = new CustomTableModel();
					customTableModelTradingGoods.setColumnIdentifiers(new String[] {"Šifra", "Naziv"});
					ArrayList<Integer> idListTradingGoods = new ArrayList<>();
					while (databaseQueryResult[1].next()) {
						Object[] rowData = new Object[2];
						rowData[0] = databaseQueryResult[1].getString(0);
						rowData[1] = databaseQueryResult[1].getString(1);
						customTableModelTradingGoods.addRow(rowData);
						idListTradingGoods.add(databaseQueryResult[1].getInt(0));
					}
					jTableTradingGoods.setModel(customTableModelTradingGoods);
					tableTradingGoodsIdList = idListTradingGoods;
					jTableTradingGoods.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 20 / 100);
					jTableTradingGoods.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 80 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableDocuments(){
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		Date dateFrom;
		Date dateTo;
		try {
			dateFrom = new SimpleDateFormat("dd.MM.yyyy").parse(dateFromString);
		} catch (ParseException ex) {
			return;
		}
		try {
			dateTo = new SimpleDateFormat("dd.MM.yyyy").parse(dateToString);
		} catch (ParseException ex) {
			return;
		}
		
		int multiDatabaseQueryLength = 0;
		int tableId;
		String queryReceipts;
		String queryTransfersMaterials = "";
		String queryTransfersArticles = "";
		String queryInvoices = "";
		String queryLocalInvoices = "";
		if(jTabbedPane1.getSelectedIndex() == 0){
			if(jTableMaterials.getSelectedRow() == -1){
				return;
			}
		
			int rowId = jTableMaterials.convertRowIndexToModel(jTableMaterials.getSelectedRow());
			tableId = tableMaterialsIdList.get(rowId);
			
			queryReceipts = "SELECT RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_MATERIALS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_MATERIALS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? "
					+ "AND RECEIPTS.IS_DELETED = 0 AND RECEIPT_MATERIALS.IS_DELETED = 0  "
					+ "AND RECEIPT_MATERIALS.MATERIAL_ID = ? AND RECEIPTS.OFFICE_NUMBER = ?";
			
			queryInvoices = "SELECT INVOICES.I_DATE, SUM(INVOICE_MATERIALS.AMT * INVOICE_MATERIALS.NORM) "
					+ "FROM INVOICE_MATERIALS "
					+ "INNER JOIN INVOICES ON INVOICE_MATERIALS.IN_ID = INVOICES.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICE_MATERIALS.MAT_ID = ? "
					+ "AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM) "
					+ "FROM LOCAL_INVOICE_MATERIALS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? "
					+ "AND LOCAL_INVOICE_MATERIALS.MAT_ID = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY LOCAL_INVOICES.I_DATE";
			
			queryTransfersMaterials = "SELECT TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_MATERIALS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_MATERIALS.MATERIAL_ID = ?";
			
			queryTransfersArticles = "SELECT TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_ARTICLES.AMOUNT_START * TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "INNER JOIN TRANSFER_ARTICLE_MATERIALS ON TRANSFER_ARTICLES.ID = TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_ARTICLES.IS_DELETED = 0 AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = (CASE "
						+ "WHEN TRANSFERS.STARTING_OFFICE_ID = ? THEN 1 "
						+ "ELSE 0 END)";
			
			multiDatabaseQueryLength = 5;
		} else {
			if(jTableTradingGoods.getSelectedRow() == -1){
				return;
			}
		
			int rowId = jTableTradingGoods.convertRowIndexToModel(jTableTradingGoods.getSelectedRow());
			tableId = tableTradingGoodsIdList.get(rowId);
			
			queryReceipts = "SELECT RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_TRADING_GOODS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_TRADING_GOODS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? AND RECEIPTS.IS_DELETED = 0 "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 AND RECEIPT_TRADING_GOODS.TRADING_GOODS_ID = ? AND RECEIPTS.OFFICE_NUMBER = ?";

			queryInvoices = "SELECT INVOICES.I_DATE, SUM(INVOICE_ITEMS.AMT) "
					+ "FROM INVOICE_ITEMS "
					+ "INNER JOIN INVOICES ON INVOICE_ITEMS.IN_ID = INVOICES.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICE_ITEMS.IT_ID = ? AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_ITEMS.AMT) "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICE_ITEMS.IT_ID = ? AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND LOCAL_INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY LOCAL_INVOICES.I_DATE";
			
			multiDatabaseQueryLength = 3;
		}
		
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
		}
		
		if(multiDatabaseQueryLength == 0)
			return;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(multiDatabaseQueryLength);
		
		multiDatabaseQuery.SetQuery(0, queryReceipts);
		multiDatabaseQuery.AddParam(0, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(0, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(0, 3, tableId);
		multiDatabaseQuery.AddParam(0, 4, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(1, queryInvoices);
		multiDatabaseQuery.AddParam(1, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(1, 3, tableId);
		multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(2, queryLocalInvoices);
		multiDatabaseQuery.AddParam(2, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(2, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(2, 3, tableId);
		multiDatabaseQuery.AddParam(2, 4, Licence.GetOfficeNumber());
		
		if(jTabbedPane1.getSelectedIndex() == 0){
			multiDatabaseQuery.SetQuery(3, queryTransfersMaterials);
			multiDatabaseQuery.AddParam(3, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(3, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(3, 7, tableId);
			
			multiDatabaseQuery.SetQuery(4, queryTransfersArticles);
			multiDatabaseQuery.AddParam(4, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 7, tableId);
			multiDatabaseQuery.AddParam(4, 8, Licence.GetOfficeNumber());
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
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Datum", "Tip dokumenta", "Oznaka dokumenta", "Ulaz", "Izlaz"});
					float sumIn = 0f, sumOut = 0f;
					
					// Receipts
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[5];
						Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[0].getString(0));
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
						rowData[1] = "Primka";
						rowData[2] = databaseQueryResult[0].getString(1);
						rowData[3] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[0].getFloat(2));
						rowData[4] = "0.00";
						sumIn += databaseQueryResult[0].getFloat(2);
						customTableModel.addRow(rowData);
					}
					
					// Invoices
					while (databaseQueryResult[1].next()) {
						Object[] rowData = new Object[5];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[1].getString(0));
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[1] = "DnevniPromet";
						rowData[2] = "";
						rowData[3] = "0.00";
						rowData[4] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[1].getFloat(1));
						sumOut += databaseQueryResult[1].getFloat(1);
						customTableModel.addRow(rowData);
					}
					while (databaseQueryResult[2].next()) {
						Object[] rowData = new Object[5];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[2].getString(0));
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[1] = "DnevniPrometX";
						rowData[2] = "";
						rowData[3] = "0.00";
						rowData[4] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[2].getFloat(1));
						sumOut += databaseQueryResult[2].getFloat(1);
						customTableModel.addRow(rowData);
					}
					
					// Transfers
					if(jTabbedPane1.getSelectedIndex() == 0){
						while (databaseQueryResult[3].next()) {
							Object[] rowData = new Object[5];
							rowData[1] = "Međuskladišnica";
							rowData[2] = databaseQueryResult[3].getString(0);

							if(databaseQueryResult[3].getInt(3) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[3].getString(1));
								rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[3] = "0.00";
								rowData[4] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[3].getFloat(5));
								sumOut += databaseQueryResult[3].getFloat(5);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[3].getString(2));
								rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[3] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[3].getFloat(5));
								rowData[4] = "0.00";
								sumIn += databaseQueryResult[3].getFloat(5);
							}

							customTableModel.addRow(rowData);
						}
						while (databaseQueryResult[4].next()) {
							Object[] rowData = new Object[5];
							rowData[1] = "Međuskladišnica";
							rowData[2] = databaseQueryResult[4].getString(0);

							if(databaseQueryResult[4].getInt(3) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[4].getString(1));
								rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[3] = "0.00";
								rowData[4] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[4].getFloat(5));
								sumOut += databaseQueryResult[4].getFloat(5);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[4].getString(2));
								rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[3] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult[4].getFloat(5));
								rowData[4] = "0.00";
								sumIn += databaseQueryResult[4].getFloat(5);
							}

							customTableModel.addRow(rowData);
						}
					}
					
					jLabelIn.setText(ClientAppUtils.FloatToPriceString(sumIn));
					jLabelOut.setText(ClientAppUtils.FloatToPriceString(sumOut));
					jLabelDiff.setText(ClientAppUtils.FloatToPriceString(sumIn - sumOut));
					
					jTableDocuments.setModel(customTableModel);
					jTableDocuments.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneDocuments.getWidth() * 20 / 100);
					jTableDocuments.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneDocuments.getWidth() * 25 / 100);
					jTableDocuments.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneDocuments.getWidth() * 30 / 100);
					jTableDocuments.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneDocuments.getWidth() * 15 / 100);
					jTableDocuments.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneDocuments.getWidth() * 15 / 100);		
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
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
        jPanel2 = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jXDatePickerFrom = new org.jdesktop.swingx.JXDatePicker();
        jXDatePickerTo = new org.jdesktop.swingx.JXDatePicker();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonPrintPosAll = new javax.swing.JButton();
        jButtonPrintA4All = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPaneDocuments = new javax.swing.JScrollPane();
        jTableDocuments = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabelIn = new javax.swing.JLabel();
        jLabelOut = new javax.swing.JLabel();
        jLabelDiff = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPaneMaterials = new javax.swing.JScrollPane();
        jTableMaterials = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jScrollPaneTradingGoods = new javax.swing.JScrollPane();
        jTableTradingGoods = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Normativi");
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
        jLabel9.setText("Skladišna kartica");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Dokumenti [F1]"));

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

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonPrintPosAll.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> sve stavke <br>  [F6] </div> </html>");
        jButtonPrintPosAll.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPosAll.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonPrintPosAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosAllActionPerformed(evt);
            }
        });

        jButtonPrintA4All.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> sve stavke <br> [F7] </div> </html>");
        jButtonPrintA4All.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4All.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonPrintA4All.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4AllActionPerformed(evt);
            }
        });

        jLabel1.setText("Od:");
        jLabel1.setPreferredSize(new java.awt.Dimension(20, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(20, 14));

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
                .addGap(54, 54, 54)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4All, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonPrintA4All, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButtonPrintPosAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        jTableDocuments.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneDocuments.setViewportView(jTableDocuments);

        jLabel14.setText("Ukupan ulaz:");
        jLabel14.setPreferredSize(new java.awt.Dimension(100, 15));

        jLabel15.setText("Ukupan izlaz:");
        jLabel15.setPreferredSize(new java.awt.Dimension(100, 15));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Razlika:");
        jLabel16.setPreferredSize(new java.awt.Dimension(100, 15));

        jLabelIn.setText("ulaz");

        jLabelOut.setText("izlaz");

        jLabelDiff.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelDiff.setText("razlika");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPaneDocuments)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelIn))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelOut))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelDiff)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneDocuments, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelIn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOut))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDiff))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Filter");

        jTextField1.setPreferredSize(new java.awt.Dimension(130, 25));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(6, 6, 6))
        );

        jTableMaterials.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneMaterials.setViewportView(jTableMaterials);

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setText("Materijali");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(jLabel13)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneMaterials)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Materijali [F2]", jPanel3);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel10.setText("Filter");

        jTextField2.setPreferredSize(new java.awt.Dimension(130, 25));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(6, 6, 6))
        );

        jTableTradingGoods.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTradingGoods.setViewportView(jTableTradingGoods);

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setText("Trgovačka roba");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTradingGoods)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Trgovačka roba [F3]", jPanel4);

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(194, 194, 194)
                        .addComponent(jLabel9)
                        .addContainerGap(404, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addGap(38, 38, 38)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableMaterials.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableMaterials.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		String tableValue = "";
		if(jTabbedPane1.getSelectedIndex() == 0){
			if(jTableMaterials.getSelectedRow() == -1){
				ClientAppLogger.GetInstance().ShowMessage("Odaberite materijal u tablici čiju skladišnu karticu želite ispisati.");
				return;
			}

			int rowId = jTableMaterials.convertRowIndexToModel(jTableMaterials.getSelectedRow());
			tableValue = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 1));
		} else {
			if(jTableTradingGoods.getSelectedRow() == -1){
				ClientAppLogger.GetInstance().ShowMessage("Odaberite trgovačku robu u tablici čiju skladišnu karticu želite ispisati.");
				return;
			}

			int rowId = jTableTradingGoods.convertRowIndexToModel(jTableTradingGoods.getSelectedRow());
			tableValue = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 1));
		}
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData extraData = new PrintTableExtraData();
		extraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		extraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		extraData.footerList.add(new Pair<>("Ukupan ulaz:    ", jLabelIn.getText()));
		extraData.footerList.add(new Pair<>("Ukupan izlaz:   ", jLabelOut.getText()));
		extraData.footerList.add(new Pair<>("Razlika:        ", jLabelDiff.getText()));
		
		PrintUtils.PrintPosTable("Skladišna kartica: " + tableValue, jTableDocuments, new int[][]{ new int[]{0, 1}, new int[]{2, 3, 4} }, extraData);
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		String tableValue = "";
		if(jTabbedPane1.getSelectedIndex() == 0){
			if(jTableMaterials.getSelectedRow() == -1){
				ClientAppLogger.GetInstance().ShowMessage("Odaberite materijal u tablici čiju skladišnu karticu želite ispisati.");
				return;
			}

			int rowId = jTableMaterials.convertRowIndexToModel(jTableMaterials.getSelectedRow());
			tableValue = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 1));
		} else {
			if(jTableTradingGoods.getSelectedRow() == -1){
				ClientAppLogger.GetInstance().ShowMessage("Odaberite trgovačku robu u tablici čiju skladišnu karticu želite ispisati.");
				return;
			}

			int rowId = jTableTradingGoods.convertRowIndexToModel(jTableTradingGoods.getSelectedRow());
			tableValue = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 1));
		}
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData a4PrintExtraData = new PrintTableExtraData();
		a4PrintExtraData.headerList.add(new Pair<>("Od datuma:   ", dateFromString + "."));
		a4PrintExtraData.headerList.add(new Pair<>("Do datuma:   ", dateToString + "."));
		a4PrintExtraData.footerList.add(new Pair<>("Ukupan ulaz:    ", jLabelIn.getText()));
		a4PrintExtraData.footerList.add(new Pair<>("Ukupan izlaz:   ", jLabelOut.getText()));
		a4PrintExtraData.footerList.add(new Pair<>("Razlika:            ", jLabelDiff.getText()));
		
		PrintUtils.PrintA4Table("SkladisnaKartica-" + tableValue, "Skladišna kartica za: " + tableValue, 
				jTableDocuments, new int[]{0, 1, 2, 3, 4}, new int[]{}, a4PrintExtraData, "");
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonPrintPosAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosAllActionPerformed
		JTable tempJTable = new JTable();
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		Date dateFrom;
		Date dateTo;
		try {
			dateFrom = new SimpleDateFormat("dd.MM.yyyy").parse(dateFromString);
		} catch (ParseException ex) {
			return;
		}
		try {
			dateTo = new SimpleDateFormat("dd.MM.yyyy").parse(dateToString);
		} catch (ParseException ex) {
			return;
		}
		
		int multiDatabaseQueryLength = 0;
		String queryReceipts;
		String queryTransfersMaterials = "";
		String queryTransfersArticles = "";
		String queryInvoices = "";
		String queryLocalInvoices = "";
		
		if(jTabbedPane1.getSelectedIndex() == 0){
			queryReceipts = "SELECT MATERIALS.ID, MATERIALS.NAME, RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_MATERIALS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_MATERIALS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = RECEIPT_MATERIALS.MATERIAL_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? "
					+ "AND RECEIPTS.IS_DELETED = 0 AND RECEIPT_MATERIALS.IS_DELETED = 0  "
					+ "AND RECEIPTS.OFFICE_NUMBER = ?";
			
			queryInvoices = "SELECT MATERIALS.ID, MATERIALS.NAME, INVOICES.I_DATE, SUM(INVOICE_MATERIALS.AMT * INVOICE_MATERIALS.NORM) "
					+ "FROM INVOICE_MATERIALS "
					+ "INNER JOIN INVOICES ON INVOICE_MATERIALS.IN_ID = INVOICES.ID "
					+ "INNER JOIN MATERIALS ON INVOICE_MATERIALS.MAT_ID = MATERIALS.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY MATERIALS.ID, MATERIALS.NAME, INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT MATERIALS.ID, MATERIALS.NAME, LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM) "
					+ "FROM LOCAL_INVOICE_MATERIALS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
					+ "INNER JOIN MATERIALS ON LOCAL_INVOICE_MATERIALS.MAT_ID = MATERIALS.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? "
					+ "AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY MATERIALS.ID, MATERIALS.NAME, LOCAL_INVOICES.I_DATE";
			
			queryTransfersMaterials = "SELECT MATERIALS.ID, MATERIALS.NAME, TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_MATERIALS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = TRANSFER_MATERIALS.MATERIAL_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_MATERIALS.IS_DELETED = 0";
			
			queryTransfersArticles = "SELECT MATERIALS.ID, MATERIALS.NAME, TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_ARTICLES.AMOUNT_START * TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "INNER JOIN TRANSFER_ARTICLE_MATERIALS ON TRANSFER_ARTICLES.ID = TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_ARTICLES.IS_DELETED = 0 AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = (CASE "
						+ "WHEN TRANSFERS.STARTING_OFFICE_ID = ? THEN 1 "
						+ "ELSE 0 END)";
			
			multiDatabaseQueryLength = 5;
		} else {
			queryReceipts = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_TRADING_GOODS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_TRADING_GOODS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "INNER JOIN TRADING_GOODS ON TRADING_GOODS.ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? AND RECEIPTS.IS_DELETED = 0 "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ?";

			queryInvoices = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, INVOICES.I_DATE, SUM(INVOICE_ITEMS.AMT) "
					+ "FROM INVOICE_ITEMS "
					+ "INNER JOIN INVOICES ON INVOICE_ITEMS.IN_ID = INVOICES.ID "
					+ "INNER JOIN TRADING_GOODS ON INVOICE_ITEMS.IT_ID = TRADING_GOODS.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY TRADING_GOODS.ID, TRADING_GOODS.NAME, INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_ITEMS.AMT) "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID "
					+ "INNER JOIN TRADING_GOODS ON LOCAL_INVOICE_ITEMS.IT_ID = TRADING_GOODS.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND LOCAL_INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY TRADING_GOODS.ID, TRADING_GOODS.NAME, LOCAL_INVOICES.I_DATE";
			
			multiDatabaseQueryLength = 3;
		}
		
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
		}
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(multiDatabaseQueryLength);
		
		multiDatabaseQuery.SetQuery(0, queryReceipts);
		multiDatabaseQuery.AddParam(0, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(0, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(1, queryInvoices);
		multiDatabaseQuery.AddParam(1, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(2, queryLocalInvoices);
		multiDatabaseQuery.AddParam(2, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(2, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(2, 3, Licence.GetOfficeNumber());
		
		if(jTabbedPane1.getSelectedIndex() == 0){
			multiDatabaseQuery.SetQuery(3, queryTransfersMaterials);
			multiDatabaseQuery.AddParam(3, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(3, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			
			multiDatabaseQuery.SetQuery(4, queryTransfersArticles);
			multiDatabaseQuery.AddParam(4, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 7, Licence.GetOfficeNumber());
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResults = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResults != null){
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Stavka", "Datum", "Tip dok.", "Ozn. dok.", "Ulaz", "Izlaz"});
					// Receipts
					while (databaseQueryResults[0].next()) {
						Object[] rowData = new Object[7];
						Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[0].getString(2));
						rowData[0] = databaseQueryResults[0].getString(0);
						rowData[1] = databaseQueryResults[0].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
						rowData[3] = "Primka";
						rowData[4] = databaseQueryResults[0].getString(3);
						rowData[5] = databaseQueryResults[0].getString(4);
						rowData[6] = "0.00";
						//sumIn += databaseQueryResults[0].getFloat(4);
						customTableModel.addRow(rowData);
					}
					
					// Invoices
					while (databaseQueryResults[1].next()) {
						Object[] rowData = new Object[7];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[1].getString(2));
						rowData[0] = databaseQueryResults[1].getString(0);
						rowData[1] = databaseQueryResults[1].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[3] = "DnevniPromet";
						rowData[4] = "";
						rowData[5] = "0.00";
						rowData[6] = databaseQueryResults[1].getString(3);
						//sumOut += databaseQueryResults[1].getFloat(3);
						customTableModel.addRow(rowData);
					}
					while (databaseQueryResults[2].next()) {
						Object[] rowData = new Object[7];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[2].getString(3));
						rowData[0] = databaseQueryResults[2].getString(0);
						rowData[1] = databaseQueryResults[2].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[3] = "DnevniPrometX";
						rowData[4] = "";
						rowData[5] = "0.00";
						rowData[6] = databaseQueryResults[2].getString(3);
						//sumOut += databaseQueryResult[2].getFloat(3);
						customTableModel.addRow(rowData);
					}
					
					// Transfers
					if(jTabbedPane1.getSelectedIndex() == 0){
						while (databaseQueryResults[3].next()) {
							Object[] rowData = new Object[7];
							rowData[0] = databaseQueryResults[3].getString(0);
							rowData[1] = databaseQueryResults[3].getString(1);
							rowData[3] = "Međuskladišnica";
							rowData[4] = databaseQueryResults[3].getString(2);

							if(databaseQueryResults[3].getInt(5) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[3].getString(3));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = "0.00";
								rowData[6] = databaseQueryResults[3].getString(7);
								//sumOut += databaseQueryResults[3].getFloat(7);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[3].getString(4));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = databaseQueryResults[3].getString(7);
								rowData[6] = "0.00";
								//sumIn += databaseQueryResults[3].getFloat(7);
							}

							customTableModel.addRow(rowData);
						}
						while (databaseQueryResults[4].next()) {
							Object[] rowData = new Object[7];
							rowData[0] = databaseQueryResults[4].getString(0);
							rowData[1] = databaseQueryResults[5].getString(1);
							rowData[3] = "Međuskladišnica";
							rowData[4] = databaseQueryResults[4].getString(2);

							if(databaseQueryResults[4].getInt(5) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[4].getString(3));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = "0.00";
								rowData[6] = databaseQueryResults[4].getString(7);
								//sumOut += databaseQueryResults[4].getFloat(7);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[4].getString(4));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = databaseQueryResults[4].getString(7);
								rowData[6] = "0.00";
								//sumIn += databaseQueryResults[4].getFloat(7);
							}

							customTableModel.addRow(rowData);
						}
					}
					tempJTable.setModel(customTableModel);
					tempJTable.getColumnModel().getColumn(0).setPreferredWidth(20);
					tempJTable.getColumnModel().getColumn(1).setPreferredWidth(50);
					tempJTable.getColumnModel().getColumn(2).setPreferredWidth(30);
					tempJTable.getColumnModel().getColumn(3).setPreferredWidth(25);
					tempJTable.getColumnModel().getColumn(4).setPreferredWidth(25);
					tempJTable.getColumnModel().getColumn(5).setPreferredWidth(25);
					tempJTable.getColumnModel().getColumn(6).setPreferredWidth(25);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		String docTitle;
		if(jTabbedPane1.getSelectedIndex() == 0){
			docTitle = "Skladišna kartica - materijali";
		} else {
			docTitle = "Skladišna kartica - trgovačka roba";
		}
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintPosTable(docTitle, tempJTable, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5, 6}}, printTableExtraData);
		tempJTable = null;
    }//GEN-LAST:event_jButtonPrintPosAllActionPerformed

    private void jButtonPrintA4AllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4AllActionPerformed
		JTable tempJTable = new JTable();
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		Date dateFrom;
		Date dateTo;
		try {
			dateFrom = new SimpleDateFormat("dd.MM.yyyy").parse(dateFromString);
		} catch (ParseException ex) {
			return;
		}
		try {
			dateTo = new SimpleDateFormat("dd.MM.yyyy").parse(dateToString);
		} catch (ParseException ex) {
			return;
		}
		
		int multiDatabaseQueryLength = 0;
		String queryReceipts;
		String queryTransfersMaterials = "";
		String queryTransfersArticles = "";
		String queryInvoices = "";
		String queryLocalInvoices = "";
		
		if(jTabbedPane1.getSelectedIndex() == 0){
			queryReceipts = "SELECT MATERIALS.ID, MATERIALS.NAME, RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_MATERIALS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_MATERIALS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = RECEIPT_MATERIALS.MATERIAL_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? "
					+ "AND RECEIPTS.IS_DELETED = 0 AND RECEIPT_MATERIALS.IS_DELETED = 0  "
					+ "AND RECEIPTS.OFFICE_NUMBER = ?";
			
			queryInvoices = "SELECT MATERIALS.ID, MATERIALS.NAME, INVOICES.I_DATE, SUM(INVOICE_MATERIALS.AMT * INVOICE_MATERIALS.NORM) "
					+ "FROM INVOICE_MATERIALS "
					+ "INNER JOIN INVOICES ON INVOICE_MATERIALS.IN_ID = INVOICES.ID "
					+ "INNER JOIN MATERIALS ON INVOICE_MATERIALS.MAT_ID = MATERIALS.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY MATERIALS.ID, MATERIALS.NAME, INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT MATERIALS.ID, MATERIALS.NAME, LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM) "
					+ "FROM LOCAL_INVOICE_MATERIALS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
					+ "INNER JOIN MATERIALS ON LOCAL_INVOICE_MATERIALS.MAT_ID = MATERIALS.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? "
					+ "AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "GROUP BY MATERIALS.ID, MATERIALS.NAME, LOCAL_INVOICES.I_DATE";
			
			queryTransfersMaterials = "SELECT MATERIALS.ID, MATERIALS.NAME, TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_MATERIALS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = TRANSFER_MATERIALS.MATERIAL_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_MATERIALS.IS_DELETED = 0";
			
			queryTransfersArticles = "SELECT MATERIALS.ID, MATERIALS.NAME, TRANSFERS.ID, TRANSFERS.TRANSFER_START_DATE, TRANSFERS.TRANSFER_RECIEVED_DATE, "
					+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID, TRANSFER_ARTICLES.AMOUNT_START * TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFERS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "INNER JOIN TRANSFER_ARTICLE_MATERIALS ON TRANSFER_ARTICLES.ID = TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID "
					+ "INNER JOIN MATERIALS ON MATERIALS.ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID "
					+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
					+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
						+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
					+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_ARTICLES.IS_DELETED = 0 AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = (CASE "
						+ "WHEN TRANSFERS.STARTING_OFFICE_ID = ? THEN 1 "
						+ "ELSE 0 END)";
			
			multiDatabaseQueryLength = 5;
		} else {
			queryReceipts = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, RECEIPTS.RECEIPT_DATE, RECEIPTS.DOCUMENT_NUMBER, RECEIPT_TRADING_GOODS.AMOUNT "
					+ "FROM RECEIPTS "
					+ "INNER JOIN RECEIPT_TRADING_GOODS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "INNER JOIN TRADING_GOODS ON TRADING_GOODS.ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID "
					+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? AND RECEIPTS.IS_DELETED = 0 "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ?";

			queryInvoices = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, INVOICES.I_DATE, SUM(INVOICE_ITEMS.AMT) "
					+ "FROM INVOICE_ITEMS "
					+ "INNER JOIN INVOICES ON INVOICE_ITEMS.IN_ID = INVOICES.ID "
					+ "INNER JOIN TRADING_GOODS ON INVOICE_ITEMS.IT_ID = TRADING_GOODS.ID "
					+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
					+ "AND INVOICES.O_NUM = ? "
					+ "AND (INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY TRADING_GOODS.ID, TRADING_GOODS.NAME, INVOICES.I_DATE";
			
			queryLocalInvoices = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, LOCAL_INVOICES.I_DATE, SUM(LOCAL_INVOICE_ITEMS.AMT) "
					+ "FROM LOCAL_INVOICE_ITEMS "
					+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID "
					+ "INNER JOIN TRADING_GOODS ON LOCAL_INVOICE_ITEMS.IT_ID = TRADING_GOODS.ID "
					+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? AND LOCAL_INVOICES.IS_DELETED = 0 "
					+ "AND LOCAL_INVOICES.O_NUM = ? "
					+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
					+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
					+ "AND LOCAL_INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS + " "
					+ "GROUP BY TRADING_GOODS.ID, TRADING_GOODS.NAME, LOCAL_INVOICES.I_DATE";
			
			multiDatabaseQueryLength = 3;
		}
		
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
		}
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(multiDatabaseQueryLength);
		
		multiDatabaseQuery.SetQuery(0, queryReceipts);
		multiDatabaseQuery.AddParam(0, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(0, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(1, queryInvoices);
		multiDatabaseQuery.AddParam(1, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
		
		multiDatabaseQuery.SetQuery(2, queryLocalInvoices);
		multiDatabaseQuery.AddParam(2, 1, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(2, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(2, 3, Licence.GetOfficeNumber());
		
		if(jTabbedPane1.getSelectedIndex() == 0){
			multiDatabaseQuery.SetQuery(3, queryTransfersMaterials);
			multiDatabaseQuery.AddParam(3, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(3, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(3, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(3, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			
			multiDatabaseQuery.SetQuery(4, queryTransfersArticles);
			multiDatabaseQuery.AddParam(4, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(4, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(4, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(4, 7, Licence.GetOfficeNumber());
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResults = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if(databaseQueryResults != null){
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Stavka", "Datum", "Tip dok.", "Oznaka dok.", "Ulaz", "Izlaz"});
					// Receipts
					while (databaseQueryResults[0].next()) {
						Object[] rowData = new Object[7];
						Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[0].getString(2));
						rowData[0] = databaseQueryResults[0].getString(0);
						rowData[1] = databaseQueryResults[0].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
						rowData[3] = "Primka";
						rowData[4] = databaseQueryResults[0].getString(3);
						rowData[5] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[0].getFloat(4));
						rowData[6] = "0.00";
						//sumIn += databaseQueryResults[0].getFloat(4);
						customTableModel.addRow(rowData);
					}
					
					// Invoices
					while (databaseQueryResults[1].next()) {
						Object[] rowData = new Object[7];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[1].getString(2));
						rowData[0] = databaseQueryResults[1].getString(0);
						rowData[1] = databaseQueryResults[1].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[3] = "DnevniPromet";
						rowData[4] = "";
						rowData[5] = "0.00";
						rowData[6] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[1].getFloat(3));
						//sumOut += databaseQueryResults[1].getFloat(3);
						customTableModel.addRow(rowData);
					}
					while (databaseQueryResults[2].next()) {
						Object[] rowData = new Object[7];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[2].getString(3));
						rowData[0] = databaseQueryResults[2].getString(0);
						rowData[1] = databaseQueryResults[2].getString(1);
						rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[3] = "DnevniPrometX";
						rowData[4] = "";
						rowData[5] = "0.00";
						rowData[6] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[2].getFloat(3));
						//sumOut += databaseQueryResult[2].getFloat(3);
						customTableModel.addRow(rowData);
					}
					
					// Transfers
					if(jTabbedPane1.getSelectedIndex() == 0){
						while (databaseQueryResults[3].next()) {
							Object[] rowData = new Object[7];
							rowData[0] = databaseQueryResults[3].getString(0);
							rowData[1] = databaseQueryResults[3].getString(1);
							rowData[3] = "Međuskladišnica";
							rowData[4] = databaseQueryResults[3].getString(2);

							if(databaseQueryResults[3].getInt(5) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[3].getString(3));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = "0.00";
								rowData[6] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[3].getFloat(7));
								//sumOut += databaseQueryResults[3].getFloat(7);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[3].getString(4));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[3].getFloat(7));
								rowData[6] = "0.00";
								//sumIn += databaseQueryResults[3].getFloat(7);
							}

							customTableModel.addRow(rowData);
						}
						while (databaseQueryResults[4].next()) {
							Object[] rowData = new Object[7];
							rowData[0] = databaseQueryResults[4].getString(0);
							rowData[1] = databaseQueryResults[5].getString(1);
							rowData[3] = "Međuskladišnica";
							rowData[4] = databaseQueryResults[4].getString(2);

							if(databaseQueryResults[4].getInt(5) == Licence.GetOfficeNumber()){
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[4].getString(3));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = "0.00";
								rowData[6] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[4].getFloat(7));
								//sumOut += databaseQueryResults[4].getFloat(7);
							} else {
								Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResults[4].getString(4));
								rowData[2] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
								rowData[5] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResults[4].getFloat(7));
								rowData[6] = "0.00";
								//sumIn += databaseQueryResults[4].getFloat(7);
							}

							customTableModel.addRow(rowData);
						}
					}
					tempJTable.setModel(customTableModel);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		String docName, docTitle;
		if(jTabbedPane1.getSelectedIndex() == 0){
			docName = "SkladišnaKartica-Materijali";
			docTitle = "Skladišna kartica - materijali";
		} else {
			docName = "SkladišnaKartica-TrgovačkaRoba";
			docTitle = "Skladišna kartica - trgovačka roba";
		}
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintA4Table(docName, docTitle, tempJTable, new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{0, 1}, printTableExtraData, "");
		tempJTable = null;
    }//GEN-LAST:event_jButtonPrintA4AllActionPerformed

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
		String searchString = jTextField2.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTradingGoods.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableTradingGoods.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField2KeyReleased

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTableDocuments();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTableDocuments();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintA4All;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JButton jButtonPrintPosAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDiff;
    private javax.swing.JLabel jLabelIn;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelOut;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneDocuments;
    private javax.swing.JScrollPane jScrollPaneMaterials;
    private javax.swing.JScrollPane jScrollPaneTradingGoods;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableDocuments;
    private javax.swing.JTable jTableMaterials;
    private javax.swing.JTable jTableTradingGoods;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
