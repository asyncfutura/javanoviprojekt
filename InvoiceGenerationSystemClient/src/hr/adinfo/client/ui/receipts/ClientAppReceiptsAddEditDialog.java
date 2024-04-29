/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.receipts;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.settings.ClientAppSelectTradingGoodsDialog;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReceiptsAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int receiptId;
	private int supplierId = -1;
	private ArrayList<Integer> receiptMaterialsIdList = new ArrayList<>();
	private ArrayList<Integer> receiptTradingGoodsIdList = new ArrayList<>();
	private ArrayList<Integer> receiptMaterialsMaterialsIdList = new ArrayList<>();
	private ArrayList<Integer> receiptTradingGoodsTradingGoodsIdList = new ArrayList<>();
	private ArrayList<Float> receiptMaterialsTaxList = new ArrayList<>();
	private ArrayList<Float> receiptTradingGoodsTaxList = new ArrayList<>();
	private ArrayList<Float> receiptMaterialsPriceList = new ArrayList<>();
	private ArrayList<Float> receiptTradingGoodsPriceList = new ArrayList<>();
	private float currentReceiptTotalPrice;
	private boolean tabSwitchFlag = true;
	private int receiptYear;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppReceiptsAddEditDialog(java.awt.Frame parent, boolean modal, int tableId) {
		super(parent, modal);
		initComponents();
		
		this.receiptId = tableId;
		
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
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerPaymentDue.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerReceiptDate.getUI();
						if (pickerUI1.isPopupVisible()) {
							pickerUI1.hidePopup();
							return false;
						}
						if (pickerUI2.isPopupVisible()) {
							pickerUI2.hidePopup();
							return false;
						}
						
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonSave.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPickSupplier.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerReceiptDate.grabFocus();
					} else if(ke.getKeyCode() == KeyEvent.VK_F2){
						jTabbedPane1.setSelectedIndex(0);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerPaymentDue.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerReceiptDate.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTableMaterials.requestFocusInWindow();
						if(jTableMaterials.getRowCount() > 0){
							jTableMaterials.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jTabbedPane1.setSelectedIndex(1);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerPaymentDue.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerReceiptDate.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTableTradingGoods.requestFocusInWindow();
						if(jTableTradingGoods.getRowCount() > 0){
							jTableTradingGoods.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonAddMaterial.doClick();
						} else {
							jButtonAddTradingGoods.doClick();
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonEditMaterial.doClick();
						} else {
							jButtonEditTradingGoods.doClick();
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonDeleteMaterial.doClick();
						} else {
							jButtonDeleteTradingGoods.doClick();
						}
					}
				}
				
				return false;
			}
		});
		
		jTableMaterials.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditMaterial.doClick();
				}
			}
		});
		
		jTableTradingGoods.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditTradingGoods.doClick();
				}
			}
		});
		
		jTableMaterials.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableMaterials.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMaterials.getTableHeader().setReorderingAllowed(false);
		jTableMaterials.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableTradingGoods.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTradingGoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTradingGoods.getTableHeader().setReorderingAllowed(false);
		jTableTradingGoods.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jXDatePickerReceiptDate.setFormats("dd.MM.yyyy");
		jXDatePickerReceiptDate.getEditor().setEditable(false);
		jXDatePickerReceiptDate.setDate(new Date());
		jXDatePickerReceiptDate.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if(tabSwitchFlag){
					tabSwitchFlag = false;
					if(jTabbedPane1.getSelectedIndex() == 0){
						jTableMaterials.requestFocusInWindow();
					} else {
						jTableTradingGoods.requestFocusInWindow();
					}
					return;
				}
				
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerReceiptDate.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerReceiptDate.getMonthView()) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		jXDatePickerPaymentDue.setFormats("dd.MM.yyyy");
		jXDatePickerPaymentDue.getEditor().setEditable(false);
		jXDatePickerPaymentDue.setDate(new Date());
		jXDatePickerPaymentDue.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerPaymentDue.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerPaymentDue.getMonthView()) {
					pickerUI.toggleShowPopup();
				}
			}
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		if(tableId != -1){
			SetupDialogForEdit();
		} else {
			jLabelReceiptNumber.setText("---");
			jPanelItems.setEnabled(false);
			jTabbedPane1.setEnabled(false);
			jLabelSupplierName.setText("");
			jLabelSupplierVATID.setText("");
			jLabelSupplierAddress1.setText("");
			jLabelSupplierAddress2.setText("");
			jLabelReceiptTotalValue.setText("0.00");
			jLabelTotalAmount.setText("0.00");
			jLabelTotalPrice.setText("0.00");
			jButtonAddMaterial.setEnabled(false);
			jButtonEditMaterial.setEnabled(false);
			jButtonDeleteMaterial.setEnabled(false);
			jButtonAddTradingGoods.setEnabled(false);
			jButtonEditTradingGoods.setEnabled(false);
			jButtonDeleteTradingGoods.setEnabled(false);
		}
		
		RefreshTableItems();
		
		ClientAppUtils.SetupFocusTraversal(this);
		Set setForwardMaterials = new HashSet(jTableMaterials.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackwardMaterials = new HashSet(jTableMaterials.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForwardMaterials.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackwardMaterials.remove(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTableMaterials.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForwardMaterials);
		jTableMaterials.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackwardMaterials);
		Set setForwardTradingGoods = new HashSet(jTableTradingGoods.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackwardTradingGoods = new HashSet(jTableTradingGoods.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForwardTradingGoods.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackwardTradingGoods.remove(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTableTradingGoods.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForwardTradingGoods);
		jTableTradingGoods.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackwardTradingGoods);
	}
	
	private void SetupDialogForEdit(){
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT RECEIPT_DATE, SUPPLIER_ID, DOCUMENT_NUMBER, PAYMENT_DUE_DATE, "
					+ "IS_PAID, TOTAL_PRICE, RECEIPT_NUMBER FROM RECEIPTS WHERE ID = ?");
			databaseQuery.AddParam(1, receiptId);
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
						if (databaseQueryResult.next()) {
							Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(0));
							jXDatePickerReceiptDate.setDate(receiptDate);
							Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(3));
							jXDatePickerPaymentDue.setDate(dueDate);
							
							supplierId = databaseQueryResult.getInt(1);
							currentReceiptTotalPrice = databaseQueryResult.getFloat(5);
							jTextFieldDocumentNumber.setText(databaseQueryResult.getString(2));

							int isPaid = databaseQueryResult.getInt(4);
							if(isPaid == 1){
								jCheckBox1.setSelected(true);
							}
							
							jLabelReceiptNumber.setText("" + databaseQueryResult.getInt(6));
							
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(receiptDate);
							receiptYear = calendar.get(Calendar.YEAR);
						} else {
							Utils.DisposeDialog(this);
						}
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException | ParseException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
		
		jButtonSave.setEnabled(false);
		RefreshSupplier();
	}
	
	private void RefreshSupplier(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		

		String query = "SELECT NAME, OIB, STREET, HOUSE_NUM, TOWN, POSTAL_CODE "
				+ "FROM SUPPLIERS WHERE IS_DELETED = 0 AND ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, supplierId);
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
					if (databaseQueryResult.next()) {
						jLabelSupplierName.setText(databaseQueryResult.getString(0));
						jLabelSupplierVATID.setText(databaseQueryResult.getString(1));
						jLabelSupplierAddress1.setText(databaseQueryResult.getString(2) + " " + databaseQueryResult.getString(3));
						jLabelSupplierAddress2.setText(databaseQueryResult.getString(4) + ", " + databaseQueryResult.getString(5));
					} else {
						Utils.DisposeDialog(this);
					}
				} else {
					Utils.DisposeDialog(this);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
				Utils.DisposeDialog(this);
			}
		}
	}
	
	private void RefreshTableItems(){
		float totalAmount = 0f;
		float totalPrice = 0f;
		
		// Get materials
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT RECEIPT_MATERIALS.ID, MATERIALS.NAME, RECEIPT_MATERIALS.AMOUNT, MEASURING_UNITS.NAME, RECEIPT_MATERIALS.PRICE, "
					+ "RECEIPT_MATERIALS.RABATE, MATERIALS.ID, MATERIALS.LAST_PRICE, RECEIPT_MATERIALS.TAX_IN_VALUE "
					+ "FROM ((RECEIPT_MATERIALS INNER JOIN MATERIALS ON RECEIPT_MATERIALS.MATERIAL_ID = MATERIALS.ID)"
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
					+ "WHERE RECEIPT_MATERIALS.RECEIPT_ID = ? AND RECEIPT_MATERIALS.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, receiptId);
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
						customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "PNC", "Cijena", "Rabat", "Ukupno"});
						ArrayList<Integer> idListMaterials = new ArrayList<>();
						ArrayList<Integer> idListMaterialsMaterials = new ArrayList<>();
						ArrayList<Float> taxListMaterials = new ArrayList<>();
						ArrayList<Float> priceListMaterials = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[7];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = databaseQueryResult.getString(3);
							rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(7));
							float rabateValue = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4) * (databaseQueryResult.getFloat(5) / 100f);
							rowData[5] = databaseQueryResult.getString(5) + "% = " + ClientAppUtils.FloatToPriceString(rabateValue) + "kn";
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4) * (100f - databaseQueryResult.getFloat(5)) / 100f;
							rowData[6] = ClientAppUtils.FloatToPriceString(price);
							
							customTableModel.addRow(rowData);
							idListMaterials.add(databaseQueryResult.getInt(0));
							idListMaterialsMaterials.add(databaseQueryResult.getInt(6));
							taxListMaterials.add(databaseQueryResult.getFloat(8));
							priceListMaterials.add(price);
							totalAmount += databaseQueryResult.getFloat(2);
							totalPrice += price;
						}
						jTableMaterials.setModel(customTableModel);
						receiptMaterialsIdList = idListMaterials;
						receiptMaterialsMaterialsIdList = idListMaterialsMaterials;
						receiptMaterialsTaxList = taxListMaterials;
						receiptMaterialsPriceList = priceListMaterials;

						jTableMaterials.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneMaterials.getWidth() * 30 / 100);
						jTableMaterials.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneMaterials.getWidth() * 10 / 100);
						jTableMaterials.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneMaterials.getWidth() * 10 / 100);
						jTableMaterials.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneMaterials.getWidth() * 10 / 100);
						jTableMaterials.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneMaterials.getWidth() * 10 / 100);
						jTableMaterials.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
						jTableMaterials.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneMaterials.getWidth() * 10 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		// Get trading goods
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT RECEIPT_TRADING_GOODS.ID, TRADING_GOODS.NAME, RECEIPT_TRADING_GOODS.AMOUNT, RECEIPT_TRADING_GOODS.PRICE, RECEIPT_TRADING_GOODS.RABATE,"
					+ "RECEIPT_TRADING_GOODS.MARGIN, RECEIPT_TRADING_GOODS.TAX_RATE, TRADING_GOODS.ID , TRADING_GOODS.LAST_PRICE, RECEIPT_TRADING_GOODS.TAX_IN_VALUE "
					+ "FROM (RECEIPT_TRADING_GOODS INNER JOIN TRADING_GOODS ON RECEIPT_TRADING_GOODS.TRADING_GOODS_ID = TRADING_GOODS.ID) "
					+ "WHERE RECEIPT_TRADING_GOODS.RECEIPT_ID = ? AND RECEIPT_TRADING_GOODS.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, receiptId);
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
						customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Kol.", "PNC", "Cijena", "Rabat po kom", "Ukupno", "Marža po kom.", "Bez PDV", "Sa PDV", "Ukupno (s maržom)"});
						ArrayList<Integer> idListTradingGoods = new ArrayList<>();
						ArrayList<Integer> idListTradingGoodsTradingGoods = new ArrayList<>();
						ArrayList<Float> taxListTradingGoods = new ArrayList<>();
						ArrayList<Float> priceListTradingGoods = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[10];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(8));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
							float rabateValue = databaseQueryResult.getFloat(3) * (databaseQueryResult.getFloat(4) / 100f);
							rowData[4] = databaseQueryResult.getString(4) + "% = " + ClientAppUtils.FloatToPriceString(rabateValue) + "kn";
							float pricePerUnit = databaseQueryResult.getFloat(3) * (100f - databaseQueryResult.getFloat(4)) / 100f;
							float price = databaseQueryResult.getFloat(2) * pricePerUnit;
							rowData[5] = ClientAppUtils.FloatToPriceString(price);
							float marginValue = pricePerUnit * (databaseQueryResult.getFloat(5) / 100f);
							rowData[6] = databaseQueryResult.getString(5) + "% = " + ClientAppUtils.FloatToPriceString(marginValue) + "kn";
							float marginNoTax = pricePerUnit * (100f + databaseQueryResult.getFloat(5)) / 100f;
							rowData[7] = ClientAppUtils.FloatToPriceString(marginNoTax);
							float marginWithTax = marginNoTax * (100f + databaseQueryResult.getFloat(6)) / 100f;
							rowData[8] = ClientAppUtils.FloatToPriceString(marginWithTax);
							rowData[9] = ClientAppUtils.FloatToPriceString(marginWithTax * databaseQueryResult.getFloat(2));
							
							customTableModel.addRow(rowData);
							idListTradingGoods.add(databaseQueryResult.getInt(0));
							idListTradingGoodsTradingGoods.add(databaseQueryResult.getInt(7));
							taxListTradingGoods.add(databaseQueryResult.getFloat(9));
							priceListTradingGoods.add(price);
							totalAmount += databaseQueryResult.getFloat(2);
							totalPrice += price;
						}
						jTableTradingGoods.setModel(customTableModel);
						receiptTradingGoodsIdList = idListTradingGoods;
						receiptTradingGoodsTradingGoodsIdList = idListTradingGoodsTradingGoods;
						receiptTradingGoodsTaxList = taxListTradingGoods;
						receiptTradingGoodsPriceList = priceListTradingGoods;

						jTableTradingGoods.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 15 / 100);
						jTableTradingGoods.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 7 / 100);
						jTableTradingGoods.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 7 / 100);
						jTableTradingGoods.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 7 / 100);
						jTableTradingGoods.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 15 / 100);
						jTableTradingGoods.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 10 / 100);
						jTableTradingGoods.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 15 / 100);
						jTableTradingGoods.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 10 / 100);
						jTableTradingGoods.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 10 / 100);
						jTableTradingGoods.getColumnModel().getColumn(9).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 10 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		jLabelTotalAmount.setText(ClientAppUtils.FloatToPriceString(totalAmount));
		jLabelTotalPrice.setText(ClientAppUtils.FloatToPriceString(totalPrice));
		jLabelReceiptTotalValue.setText(ClientAppUtils.FloatToPriceString(totalPrice));

		// Update receipt total price
		if(currentReceiptTotalPrice != totalPrice){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "UPDATE RECEIPTS SET TOTAL_PRICE = ? WHERE ID = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, totalPrice);
			databaseQuery.AddParam(2, receiptId);

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
						changeSuccess = true;
					} else {
						return;
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
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

        jLabelTitle = new javax.swing.JLabel();
        jLabelReceiptNumber = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButtonPickSupplier = new javax.swing.JButton();
        jXDatePickerReceiptDate = new org.jdesktop.swingx.JXDatePicker();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelSupplierName = new javax.swing.JLabel();
        jLabelSupplierVATID = new javax.swing.JLabel();
        jLabelSupplierAddress1 = new javax.swing.JLabel();
        jLabelSupplierAddress2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldDocumentNumber = new javax.swing.JTextField();
        jLabelReceiptTotalValue = new javax.swing.JLabel();
        jXDatePickerPaymentDue = new org.jdesktop.swingx.JXDatePicker();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonPrint = new javax.swing.JButton();
        jPanelItems = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelMaterials = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPaneMaterials = new javax.swing.JScrollPane();
        jTableMaterials = new javax.swing.JTable();
        jButtonAddMaterial = new javax.swing.JButton();
        jButtonEditMaterial = new javax.swing.JButton();
        jButtonDeleteMaterial = new javax.swing.JButton();
        jPanelTradingGoods = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPaneTradingGoods = new javax.swing.JScrollPane();
        jTableTradingGoods = new javax.swing.JTable();
        jButtonAddTradingGoods = new javax.swing.JButton();
        jButtonEditTradingGoods = new javax.swing.JButton();
        jButtonDeleteTradingGoods = new javax.swing.JButton();
        jPanelTotal = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabelTotalAmount = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabelTotalPrice = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Primka");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Primka broj: ");

        jLabelReceiptNumber.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelReceiptNumber.setText("0000");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Primka"));

        jLabel1.setText("Datum (F1):");
        jLabel1.setPreferredSize(new java.awt.Dimension(105, 14));

        jButtonPickSupplier.setText("Odaberi [F6]");
        jButtonPickSupplier.setPreferredSize(new java.awt.Dimension(73, 35));
        jButtonPickSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPickSupplierActionPerformed(evt);
            }
        });

        jXDatePickerReceiptDate.setPreferredSize(new java.awt.Dimension(104, 25));
        jXDatePickerReceiptDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerReceiptDatePropertyChange(evt);
            }
        });

        jLabel3.setText("Dobavljač:");
        jLabel3.setPreferredSize(new java.awt.Dimension(105, 14));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Dobavljač"));

        jLabel2.setText("Ime:");
        jLabel2.setPreferredSize(new java.awt.Dimension(55, 14));

        jLabel4.setText("OIB:");
        jLabel4.setPreferredSize(new java.awt.Dimension(55, 14));

        jLabel5.setText("Adresa:");
        jLabel5.setPreferredSize(new java.awt.Dimension(55, 14));

        jLabelSupplierName.setText("ime");

        jLabelSupplierVATID.setText("oib");

        jLabelSupplierAddress1.setText("adresa1");

        jLabelSupplierAddress2.setText("adresa2");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelSupplierName))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelSupplierVATID))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelSupplierAddress2)
                            .addComponent(jLabelSupplierAddress1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSupplierName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSupplierVATID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSupplierAddress1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelSupplierAddress2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("Broj dokumenta:");
        jLabel6.setPreferredSize(new java.awt.Dimension(105, 14));

        jLabel7.setText("Nabavna vrijednost:");
        jLabel7.setPreferredSize(new java.awt.Dimension(105, 14));

        jLabel8.setText("Dospijeće računa:");
        jLabel8.setPreferredSize(new java.awt.Dimension(105, 14));

        jTextFieldDocumentNumber.setPreferredSize(new java.awt.Dimension(6, 25));
        jTextFieldDocumentNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDocumentNumberKeyTyped(evt);
            }
        });

        jLabelReceiptTotalValue.setText("nabavna_vrijednost");

        jXDatePickerPaymentDue.setPreferredSize(new java.awt.Dimension(104, 25));
        jXDatePickerPaymentDue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerPaymentDuePropertyChange(evt);
            }
        });

        jCheckBox1.setText("Plaćeno");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Izlaz <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(75, 65));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(75, 65));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonPrint.setText("<html> <div style=\"text-align: center\"> Ispis <br> [F5] </div> </html>");
        jButtonPrint.setPreferredSize(new java.awt.Dimension(75, 65));
        jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerReceiptDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonPickSupplier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldDocumentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerPaymentDue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelReceiptTotalValue))
                            .addComponent(jCheckBox1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerReceiptDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPickSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldDocumentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelReceiptTotalValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerPaymentDue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelItems.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke"));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel13.setText("Materijali");

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

        jButtonAddMaterial.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAddMaterial.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonAddMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMaterialActionPerformed(evt);
            }
        });

        jButtonEditMaterial.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEditMaterial.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonEditMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditMaterialActionPerformed(evt);
            }
        });

        jButtonDeleteMaterial.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDeleteMaterial.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonDeleteMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteMaterialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMaterialsLayout = new javax.swing.GroupLayout(jPanelMaterials);
        jPanelMaterials.setLayout(jPanelMaterialsLayout);
        jPanelMaterialsLayout.setHorizontalGroup(
            jPanelMaterialsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMaterialsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMaterialsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addGroup(jPanelMaterialsLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonAddMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonEditMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonDeleteMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelMaterialsLayout.setVerticalGroup(
            jPanelMaterialsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMaterialsLayout.createSequentialGroup()
                .addGroup(jPanelMaterialsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMaterialsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelMaterialsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonAddMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonEditMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonDeleteMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelMaterialsLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel13)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Materijali [F2]", jPanelMaterials);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel11.setText("Trgovačka roba");

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

        jButtonAddTradingGoods.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAddTradingGoods.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonAddTradingGoods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddTradingGoodsActionPerformed(evt);
            }
        });

        jButtonEditTradingGoods.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEditTradingGoods.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonEditTradingGoods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditTradingGoodsActionPerformed(evt);
            }
        });

        jButtonDeleteTradingGoods.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDeleteTradingGoods.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonDeleteTradingGoods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteTradingGoodsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTradingGoodsLayout = new javax.swing.GroupLayout(jPanelTradingGoods);
        jPanelTradingGoods.setLayout(jPanelTradingGoodsLayout);
        jPanelTradingGoodsLayout.setHorizontalGroup(
            jPanelTradingGoodsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTradingGoodsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTradingGoodsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addGroup(jPanelTradingGoodsLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonAddTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonEditTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonDeleteTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelTradingGoodsLayout.setVerticalGroup(
            jPanelTradingGoodsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTradingGoodsLayout.createSequentialGroup()
                .addGroup(jPanelTradingGoodsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelTradingGoodsLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel11))
                    .addGroup(jPanelTradingGoodsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelTradingGoodsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonAddTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonEditTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonDeleteTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Trgovačka roba [F3]", jPanelTradingGoods);

        javax.swing.GroupLayout jPanelItemsLayout = new javax.swing.GroupLayout(jPanelItems);
        jPanelItems.setLayout(jPanelItemsLayout);
        jPanelItemsLayout.setHorizontalGroup(
            jPanelItemsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelItemsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        jPanelItemsLayout.setVerticalGroup(
            jPanelItemsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelItemsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelTotal.setBorder(javax.swing.BorderFactory.createTitledBorder("Ukupno"));

        jLabel9.setText("Količina:");

        jLabelTotalAmount.setText("količina");

        jLabel10.setText("Ukupno:");

        jLabelTotalPrice.setText("ukupno");

        jLabel12.setText("kn");

        javax.swing.GroupLayout jPanelTotalLayout = new javax.swing.GroupLayout(jPanelTotal);
        jPanelTotal.setLayout(jPanelTotalLayout);
        jPanelTotalLayout.setHorizontalGroup(
            jPanelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalLayout.createSequentialGroup()
                .addGap(118, 118, 118)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelTotalAmount)
                .addGap(158, 158, 158)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelTotalPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTotalLayout.setVerticalGroup(
            jPanelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabelTotalAmount)
                    .addComponent(jLabel10)
                    .addComponent(jLabelTotalPrice)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                        .addGap(24, 24, 24)
                        .addComponent(jLabelTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelReceiptNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelTitle)
                            .addComponent(jLabelReceiptNumber)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanelItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String receiptDate = jXDatePickerReceiptDate.getEditor().getText().trim();
		String paymentDueDate = jXDatePickerPaymentDue.getEditor().getText().trim();
		
		Date receiptDateDate;
		try {
			receiptDateDate = new SimpleDateFormat("dd.MM.yyyy").parse(receiptDate);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Uneseni datum primke nije isparavan");
            return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(receiptDateDate);
		if(receiptId == -1 && calendar.get(Calendar.YEAR) != ClientAppSettings.currentYear){
			ClientAppLogger.GetInstance().ShowMessage("Datum primke mora biti u tekućoj godini");
            return;
		} else if(receiptId != -1 && calendar.get(Calendar.YEAR) != receiptYear){
			ClientAppLogger.GetInstance().ShowMessage("Datum primke mora biti u tekućoj godini");
            return;
		}
		
		String documentNumber = jTextFieldDocumentNumber.getText().trim();
        if("".equals(documentNumber)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite broj dokumenta.");
            return;
        }
		
		if(supplierId == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite dobavljača.");
            return;
		}
		
		int isPaid = 0;
		if(jCheckBox1.isSelected()){
			isPaid = 1;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(receiptId == -1 ? 2 : 1);
		if(receiptId == -1){
			String query = "INSERT INTO RECEIPTS (ID, RECEIPT_DATE, SUPPLIER_ID, DOCUMENT_NUMBER, TOTAL_PRICE, "
				+ "PAYMENT_DUE_DATE, IS_PAID, OFFICE_NUMBER, RECEIPT_NUMBER, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, "
					+ "(SELECT CASE WHEN 1 = (COALESCE(MAX(RECEIPT_NUMBER), 0) + 1) THEN 2 ELSE (COALESCE(MAX(RECEIPT_NUMBER), 0) + 1) END FROM RECEIPTS WHERE OFFICE_NUMBER = ? AND YEAR(RECEIPT_DATE) = ?), "
					+ "?)";
			multiDatabaseQuery.SetQuery(0, query);
			multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "RECEIPTS");
			multiDatabaseQuery.AddParam(0, 2, receiptDate);
			multiDatabaseQuery.AddParam(0, 3, supplierId);
			multiDatabaseQuery.AddParam(0, 4, documentNumber);
			multiDatabaseQuery.AddParam(0, 5, 0);
			multiDatabaseQuery.AddParam(0, 6, paymentDueDate);
			multiDatabaseQuery.AddParam(0, 7, isPaid);
			multiDatabaseQuery.AddParam(0, 8, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 9, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 10, ClientAppSettings.currentYear);
			multiDatabaseQuery.AddParam(0, 11, 0);
			
			multiDatabaseQuery.SetQuery(1, "SELECT RECEIPT_NUMBER FROM RECEIPTS WHERE ID = ?");
			multiDatabaseQuery.AddAutoGeneratedParam(1, 1, 0);
		} else {
			String query = "UPDATE RECEIPTS SET RECEIPT_DATE = ?, SUPPLIER_ID = ?, DOCUMENT_NUMBER = ?, PAYMENT_DUE_DATE = ?, IS_PAID = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(0, query);
			multiDatabaseQuery.AddParam(0, 1, receiptDate);
			multiDatabaseQuery.AddParam(0, 2, supplierId);
			multiDatabaseQuery.AddParam(0, 3, documentNumber);
			multiDatabaseQuery.AddParam(0, 4, paymentDueDate);
			multiDatabaseQuery.AddParam(0, 5, isPaid);
			multiDatabaseQuery.AddParam(0, 6, receiptId);
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
				
				if(databaseQueryResult[0] != null){
					changeSuccess = true;
					if(receiptId == -1){
						receiptId = databaseQueryResult[0].autoGeneratedKey;
						receiptYear = calendar.get(Calendar.YEAR);
					}
				} else {
					return;
				}
				
				if(multiDatabaseQuery.querySize == 2 && databaseQueryResult[1] != null && databaseQueryResult[1].next()){
					jLabelReceiptNumber.setText("" + databaseQueryResult[1].getInt(0));
				}
			} catch (Exception ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		if(changeSuccess){
			jPanelItems.setEnabled(true);
			jTabbedPane1.setEnabled(true);
			jButtonSave.setEnabled(false);
			jButtonAddMaterial.setEnabled(true);
			jButtonEditMaterial.setEnabled(true);
			jButtonDeleteMaterial.setEnabled(true);
			jButtonAddTradingGoods.setEnabled(true);
			jButtonEditTradingGoods.setEnabled(true);
			jButtonDeleteTradingGoods.setEnabled(true);
		}
		
		jTabbedPane1.setSelectedIndex(0);
		BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerPaymentDue.getUI();
		BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerReceiptDate.getUI();
		if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
		if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
		jTableMaterials.requestFocusInWindow();
		if(jTableMaterials.getRowCount() > 0){
			jTableMaterials.setRowSelectionInterval(0, 0);
		}
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintActionPerformed
		if(jTableMaterials.getModel().getRowCount() <= 0 && jTableTradingGoods.getModel().getRowCount() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Primka je prazna. Potrebno je dodati stavke prije ispisa.");
            return;
		}
		
		PrintTableExtraData extraDataMaterials = new PrintTableExtraData();
		extraDataMaterials.headerList.add(new Pair<>("Datum primke:          ", jXDatePickerReceiptDate.getEditor().getText().trim()));
		extraDataMaterials.headerList.add(new Pair<>("Dobavljač:                 ", jLabelSupplierName.getText() + ", OIB: " + jLabelSupplierVATID.getText()));
		extraDataMaterials.headerList.add(new Pair<>("Broj dokumenta:       ", jTextFieldDocumentNumber.getText()));
		extraDataMaterials.headerList.add(new Pair<>("Dospijeće računa:      ", jXDatePickerPaymentDue.getEditor().getText().trim()));
		extraDataMaterials.headerList.add(new Pair<>("Plaćeno:                     ", jCheckBox1.isSelected() ? "Da" : "Ne"));
		extraDataMaterials.headerList.add(new Pair<>("Nabavna vrijednost:  ", jLabelReceiptTotalValue.getText() + " kn"));
		extraDataMaterials.headerList.add(new Pair<>(" ", " "));
		
		// Taxes table
		Invoice invoice = new Invoice();
		float totalPrice = 0f;
		for (int i = 0; i < receiptMaterialsIdList.size(); ++i){
			InvoiceItem invoiceItem = new InvoiceItem();
			invoiceItem.itemId = receiptMaterialsMaterialsIdList.get(i);
			invoiceItem.itemAmount = 1f;
			invoiceItem.taxRate = receiptMaterialsTaxList.get(i);
			invoiceItem.itemPrice = receiptMaterialsPriceList.get(i) * (100f + invoiceItem.taxRate) / 100f;
			invoice.items.add(invoiceItem);
			totalPrice += invoiceItem.itemPrice;
		}
		for (int i = 0; i < receiptTradingGoodsIdList.size(); ++i){
			InvoiceItem invoiceItem = new InvoiceItem();
			invoiceItem.itemId = receiptTradingGoodsTradingGoodsIdList.get(i);
			invoiceItem.itemAmount = 1f;
			invoiceItem.taxRate = receiptTradingGoodsTaxList.get(i);
			invoiceItem.itemPrice = receiptTradingGoodsPriceList.get(i) * (100f + invoiceItem.taxRate) / 100f;
			invoice.items.add(invoiceItem);
			totalPrice += invoiceItem.itemPrice;
		}
		invoice.totalPrice = totalPrice;
		
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
		PrintTableExtraData extraDataTaxes = new PrintTableExtraData();
		extraDataTaxes.headerList.add(new Pair<>("Razrada poreza:", ""));
		
		if (jTableMaterials.getModel().getRowCount() > 0 && jTableTradingGoods.getModel().getRowCount() > 0){
			extraDataMaterials.headerList.add(new Pair<>("Materijali: ", ""));
			
			PrintTableExtraData extraDataTradingGoods = new PrintTableExtraData();
			extraDataTradingGoods.headerList.add(new Pair<>("Trgovačka roba: ", ""));

			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText(), "", ""}, 
					new JTable[]{jTableMaterials, jTableTradingGoods, tempJTable}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, new int[]{ 0, 1, 2 }},
					new int[][]{new int[]{}, new int[]{}, new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials, extraDataTradingGoods, extraDataTaxes},
					new boolean[]{false, false, false},
                                        "");
		} else if (jTableMaterials.getModel().getRowCount() > 0){
			extraDataMaterials.headerList.add(new Pair<>("Materijali: ", ""));
			
			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText(), ""}, 
					new JTable[]{jTableMaterials, tempJTable}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{ 0, 1, 2 }},
					new int[][]{new int[]{}, new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials, extraDataTaxes},
					new boolean[]{false, false}, 
                                        "");
		} else {
			extraDataMaterials.headerList.add(new Pair<>("Trgovačka roba: ", ""));
			
			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText(), ""}, 
					new JTable[]{jTableTradingGoods, tempJTable}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, new int[]{ 0, 1, 2 }},
					new int[][]{new int[]{}, new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials, extraDataTaxes},
					new boolean[]{false, false},
                                        "");
		}
		
		/*if (jTableMaterials.getModel().getRowCount() > 0 && jTableTradingGoods.getModel().getRowCount() > 0){
			extraDataMaterials.headerList.add(new Pair<>("Materijali: ", ""));
			
			PrintTableExtraData extraDataTradingGoods = new PrintTableExtraData();
			extraDataTradingGoods.headerList.add(new Pair<>("Trgovačka roba: ", ""));

			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText(), ""}, 
					new JTable[]{jTableMaterials, jTableTradingGoods}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}},
					new int[][]{new int[]{}, new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials, extraDataTradingGoods},
					new boolean[]{false, false});
		} else if (jTableMaterials.getModel().getRowCount() > 0){
			extraDataMaterials.headerList.add(new Pair<>("Materijali: ", ""));
			
			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText()}, 
					new JTable[]{jTableMaterials}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6}},
					new int[][]{new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials},
					new boolean[]{false});
		} else {
			extraDataMaterials.headerList.add(new Pair<>("Trgovačka roba: ", ""));
			
			PrintUtils.PrintA4Table("Primka-" + jLabelReceiptNumber.getText(), 
					new String[]{"Primka broj: " + jLabelReceiptNumber.getText()}, 
					new JTable[]{jTableTradingGoods}, 
					new int[][]{new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}},
					new int[][]{new int[]{}}, 
					new PrintTableExtraData[]{extraDataMaterials},
					new boolean[]{false});
		}*/
    }//GEN-LAST:event_jButtonPrintActionPerformed

    private void jButtonPickSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPickSupplierActionPerformed
		ClientAppSelectSupplierDialog addEditdialog = new ClientAppSelectSupplierDialog(null, true);
        addEditdialog.setVisible(true);
        if(addEditdialog.selectedId != -1 && addEditdialog.selectedId != supplierId){
			supplierId = addEditdialog.selectedId;
			jButtonSave.setEnabled(true);
            RefreshSupplier();
        }
    }//GEN-LAST:event_jButtonPickSupplierActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
		jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jTextFieldDocumentNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDocumentNumberKeyTyped
        jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jTextFieldDocumentNumberKeyTyped

    private void jXDatePickerReceiptDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerReceiptDatePropertyChange
        jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jXDatePickerReceiptDatePropertyChange

    private void jXDatePickerPaymentDuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerPaymentDuePropertyChange
        jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jXDatePickerPaymentDuePropertyChange

    private void jButtonAddMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMaterialActionPerformed
        if(receiptId == -1)
			return;
		
		ClientAppSelectMaterialDialog selectDialog = new ClientAppSelectMaterialDialog(null, true, receiptYear);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppReceiptsMaterialAddEditDialog addEditdialog = new ClientAppReceiptsMaterialAddEditDialog(null, true, -1, receiptId, selectDialog.selectedId, receiptYear);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTableItems();
			}
        }
    }//GEN-LAST:event_jButtonAddMaterialActionPerformed

    private void jButtonEditMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditMaterialActionPerformed
		if(receiptId == -1)
			return;
		
		int rowId = jTableMaterials.getSelectedRow();
        if(jTableMaterials.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
        int receiptMaterialId = receiptMaterialsIdList.get(rowId);
        int receiptMaterialMaterialId = receiptMaterialsMaterialsIdList.get(rowId);
		
		ClientAppReceiptsMaterialAddEditDialog addEditdialog = new ClientAppReceiptsMaterialAddEditDialog(null, true, receiptMaterialId, receiptId, receiptMaterialMaterialId, receiptYear);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTableItems();
		}
    }//GEN-LAST:event_jButtonEditMaterialActionPerformed

    private void jButtonDeleteMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteMaterialActionPerformed
		if(receiptId == -1)
			return;
		
		int rowId = jTableMaterials.getSelectedRow();
        if(jTableMaterials.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
        int receiptMaterialId = receiptMaterialsIdList.get(rowId);
        String tableValue = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 0));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati materijal " + tableValue, "Obriši materijal", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){			
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
			
			int materialId = receiptMaterialsMaterialsIdList.get(rowId);
			String query1 = "UPDATE MATERIAL_AMOUNTS "
					+ "SET AMOUNT = AMOUNT - (SELECT AMOUNT FROM RECEIPT_MATERIALS WHERE ID = ?) "
					+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
					+ "AND (SELECT IS_DELETED FROM RECEIPTS WHERE ID = ?) = 0 "
					+ "AND (SELECT IS_DELETED FROM RECEIPT_MATERIALS WHERE ID = ?) = 0 "
					+ "AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(0, query1);
			multiDatabaseQuery.AddParam(0, 1, receiptMaterialId);
			multiDatabaseQuery.AddParam(0, 2, materialId);
			multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 4, receiptId);
			multiDatabaseQuery.AddParam(0, 5, receiptMaterialId);
			multiDatabaseQuery.AddParam(0, 6, receiptYear);
			
			String query0 = "UPDATE RECEIPT_MATERIALS SET IS_DELETED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(1, query0);
			multiDatabaseQuery.AddParam(1, 1, 1);
			multiDatabaseQuery.AddParam(1, 2, receiptMaterialId);
			
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
			
			RefreshTableItems();
        }
    }//GEN-LAST:event_jButtonDeleteMaterialActionPerformed

    private void jButtonAddTradingGoodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddTradingGoodsActionPerformed
		if(receiptId == -1)
			return;
		
		ClientAppSelectTradingGoodsDialog selectDialog = new ClientAppSelectTradingGoodsDialog(null, true, receiptYear);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppReceiptsTradingGoodsAddEditDialog addEditdialog = new ClientAppReceiptsTradingGoodsAddEditDialog(null, true, -1, receiptId, selectDialog.selectedId, receiptYear);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTableItems();
			}
        }
    }//GEN-LAST:event_jButtonAddTradingGoodsActionPerformed

    private void jButtonEditTradingGoodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditTradingGoodsActionPerformed
		if(receiptId == -1)
			return;
		
		int rowId = jTableTradingGoods.getSelectedRow();
        if(jTableTradingGoods.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
        int receiptTradingGoodsId = receiptTradingGoodsIdList.get(rowId);
        int receiptTradingGoodsTradingGoodsId = receiptTradingGoodsTradingGoodsIdList.get(rowId);
		
		ClientAppReceiptsTradingGoodsAddEditDialog addEditdialog = new ClientAppReceiptsTradingGoodsAddEditDialog(null, true, receiptTradingGoodsId, receiptId, receiptTradingGoodsTradingGoodsId, receiptYear);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTableItems();
		}
    }//GEN-LAST:event_jButtonEditTradingGoodsActionPerformed

    private void jButtonDeleteTradingGoodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteTradingGoodsActionPerformed
		if(receiptId == -1)
			return;
		
		int rowId = jTableTradingGoods.getSelectedRow();
        if(jTableTradingGoods.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
        int receiptTradingGoodsId = receiptTradingGoodsIdList.get(rowId);
        String tableValue = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 0));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati trgovačku robu " + tableValue, "Obriši trgovačku robu", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);

			int tradingGoodsId = receiptTradingGoodsTradingGoodsIdList.get(rowId);
			String query1 = "UPDATE TRADING_GOODS_AMOUNTS "
					+ "SET AMOUNT = AMOUNT - (SELECT AMOUNT FROM RECEIPT_TRADING_GOODS WHERE ID = ?) "
					+ "WHERE TRADING_GOODS_ID = ? AND OFFICE_NUMBER = ?"
					+ "AND (SELECT IS_DELETED FROM RECEIPTS WHERE ID = ?) = 0 "
					+ "AND (SELECT IS_DELETED FROM RECEIPT_TRADING_GOODS WHERE ID = ?) = 0 "
					+ "AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(0, query1);
			multiDatabaseQuery.AddParam(0, 1, receiptTradingGoodsId);
			multiDatabaseQuery.AddParam(0, 2, tradingGoodsId);
			multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 4, receiptId);
			multiDatabaseQuery.AddParam(0, 5, receiptTradingGoodsId);
			multiDatabaseQuery.AddParam(0, 6, receiptYear);
			
			String query0 = "UPDATE RECEIPT_TRADING_GOODS SET IS_DELETED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(1, query0);
			multiDatabaseQuery.AddParam(1, 1, 1);
			multiDatabaseQuery.AddParam(1, 2, receiptTradingGoodsId);
			
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
			
			RefreshTableItems();
        }
    }//GEN-LAST:event_jButtonDeleteTradingGoodsActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		tabSwitchFlag = true;
    }//GEN-LAST:event_jTabbedPane1StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddMaterial;
    private javax.swing.JButton jButtonAddTradingGoods;
    private javax.swing.JButton jButtonDeleteMaterial;
    private javax.swing.JButton jButtonDeleteTradingGoods;
    private javax.swing.JButton jButtonEditMaterial;
    private javax.swing.JButton jButtonEditTradingGoods;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPickSupplier;
    private javax.swing.JButton jButtonPrint;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelReceiptNumber;
    private javax.swing.JLabel jLabelReceiptTotalValue;
    private javax.swing.JLabel jLabelSupplierAddress1;
    private javax.swing.JLabel jLabelSupplierAddress2;
    private javax.swing.JLabel jLabelSupplierName;
    private javax.swing.JLabel jLabelSupplierVATID;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTotalAmount;
    private javax.swing.JLabel jLabelTotalPrice;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelItems;
    private javax.swing.JPanel jPanelMaterials;
    private javax.swing.JPanel jPanelTotal;
    private javax.swing.JPanel jPanelTradingGoods;
    private javax.swing.JScrollPane jScrollPaneMaterials;
    private javax.swing.JScrollPane jScrollPaneTradingGoods;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableMaterials;
    private javax.swing.JTable jTableTradingGoods;
    private javax.swing.JTextField jTextFieldDocumentNumber;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerPaymentDue;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerReceiptDate;
    // End of variables declaration//GEN-END:variables
}
