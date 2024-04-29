/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.transfers;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.receipts.ClientAppSelectMaterialDialog;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppTransfersAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int transferId;
	private int destinationOfficeId;
	private int startOfficeId = -1;
	private ArrayList<Integer> transferMaterialsIdList = new ArrayList<>();
	private ArrayList<Integer> transferArticleIdList = new ArrayList<>();
	private ArrayList<Integer> transferMaterialsMaterialsIdList = new ArrayList<>();
	private ArrayList<Integer> transferArticleStartArticleIdList = new ArrayList<>();
	private ArrayList<Integer> transferArticleDestArticleIdList = new ArrayList<>();
	private float currentTransferTotalPrice;
	private boolean tabSwitchFlag;
	private int transferYear;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppTransfersAddEditDialog(java.awt.Frame parent, boolean modal, int transferId, int destinationOfficeId, String destinationOfficeAddress) {
		super(parent, modal);
		initComponents();
		
		this.transferId = transferId;
		this.destinationOfficeId = destinationOfficeId;
		
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
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerTransferEndDate.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTransferStartDate.getUI();
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerTransferStartDate.grabFocus();
					} else if(ke.getKeyCode() == KeyEvent.VK_F2){
						jTabbedPane1.setSelectedIndex(0);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerTransferEndDate.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTransferStartDate.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTableMaterials.requestFocusInWindow();
						if(jTableMaterials.getRowCount() > 0){
							jTableMaterials.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jTabbedPane1.setSelectedIndex(1);
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerTransferEndDate.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTransferStartDate.getUI();
						if (pickerUI1.isPopupVisible()) pickerUI1.hidePopup();
						if (pickerUI2.isPopupVisible()) pickerUI2.hidePopup();
						jTableArticles.requestFocusInWindow();
						if(jTableArticles.getRowCount() > 0){
							jTableArticles.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonAddMaterial.doClick();
						} else {
							jButtonAddArticle.doClick();
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonEditMaterial.doClick();
						} else {
							jButtonEditArticle.doClick();
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						if(jTabbedPane1.getSelectedIndex() == 0){
							jButtonDeleteMaterial.doClick();
						} else {
							jButtonDeleteArticle.doClick();
						}
					}
				}
				
				return false;
			}
		});
		
		jTableMaterials.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableMaterials.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMaterials.getTableHeader().setReorderingAllowed(false);
		jTableMaterials.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableArticles.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableArticles.getTableHeader().setReorderingAllowed(false);
		jTableArticles.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableArticleNormatives.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableArticleNormatives.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableArticleNormatives.getTableHeader().setReorderingAllowed(false);
		jTableArticleNormatives.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jXDatePickerTransferStartDate.setFormats("dd.MM.yyyy");
		jXDatePickerTransferStartDate.getEditor().setEditable(false);
		jXDatePickerTransferStartDate.setDate(new Date());
		jXDatePickerTransferStartDate.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if(tabSwitchFlag){
					tabSwitchFlag = false;
					if(jTabbedPane1.getSelectedIndex() == 0){
						jTableMaterials.requestFocusInWindow();
					} else {
						jTableArticles.requestFocusInWindow();
					}
					return;
				}
				
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerTransferStartDate.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerTransferStartDate.getMonthView()) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		jXDatePickerTransferEndDate.setFormats("dd.MM.yyyy");
		jXDatePickerTransferEndDate.getEditor().setEditable(false);
		jXDatePickerTransferEndDate.setDate(new Date());
		jXDatePickerTransferEndDate.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerTransferEndDate.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerTransferEndDate.getMonthView()) {
					pickerUI.toggleShowPopup();
				}
				
			}
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		if(transferId != -1){
			SetupDialogForEdit();
		} else {
			jLabelTransferNumber.setText("---");
			jPanelItems.setEnabled(false);
			jTabbedPane1.setEnabled(false);
			
			String startAddress = Licence.GetOfficeAddress();
			startOfficeId = Licence.GetOfficeNumber();
			jLabelOfficeNumberStart.setText("" + startOfficeId);
			jLabelOfficeAddress1Start.setText(startAddress.length() > 25 ? startAddress.substring(0, 25) + "-" : startAddress);
			jLabelOfficeAddress2Start.setText(startAddress.length() > 25 ? startAddress.substring(25) : "");
			
			jLabelOfficeNumberEnd.setText("" + destinationOfficeId);
			jLabelOfficeAddress1End.setText(destinationOfficeAddress.length() > 25 ? destinationOfficeAddress.substring(0, 25) + "-" : destinationOfficeAddress);
			jLabelOfficeAddress2End.setText(destinationOfficeAddress.length() > 25 ? destinationOfficeAddress.substring(25) : "");
			
			jLabelTransferTotalValue.setText("0.00");
			jLabelTotalAmount.setText("0.00");
			jLabelTotalPrice.setText("0.00");
			
			jButtonAddMaterial.setEnabled(false);
			jButtonEditMaterial.setEnabled(false);
			jButtonDeleteMaterial.setEnabled(false);
			jButtonAddArticle.setEnabled(false);
			jButtonEditArticle.setEnabled(false);
			jButtonDeleteArticle.setEnabled(false);

		}
		
		RefreshTableItems();
		
		jTableArticles.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableArticles.getSelectedRow() == -1)
					return;
				
				int rowId = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
				int transferArticleId = transferArticleIdList.get(rowId);
				RefreshArticleNormatives(transferArticleId);
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
		
		jTableArticles.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditArticle.doClick();
				}
			}
		});
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Izlazni materijali", "Ulazni materijali"});
		jTableArticleNormatives.setModel(customTableModel);
		jTableArticleNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
		jTableArticleNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
		
		ClientAppUtils.SetupFocusTraversal(this);
		Set setForwardMaterials = new HashSet(jTableMaterials.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackwardMaterials = new HashSet(jTableMaterials.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForwardMaterials.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackwardMaterials.remove(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTableMaterials.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForwardMaterials);
		jTableMaterials.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackwardMaterials);
		Set setForwardTradingGoods = new HashSet(jTableArticles.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set setBackwardTradingGoods = new HashSet(jTableArticles.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForwardTradingGoods.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackwardTradingGoods.remove(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		jTableArticles.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForwardTradingGoods);
		jTableArticles.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackwardTradingGoods);
	}
	
	private void SetupDialogForEdit(){
		jLabelTransferNumber.setText("" + transferId);
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			String query = "SELECT TRANSFER_START_DATE, TRANSFER_RECIEVED_DATE, STARTING_OFFICE_ID, DESTINATION_OFFICE_ID, "
					+ "IS_DELIVERED, TOTAL_PRICE, OFFICES_1.ADDRESS, OFFICES_2.ADDRESS "
					+ "FROM TRANSFERS "
					+ "INNER JOIN OFFICES OFFICES_1 ON OFFICES_1.OFFICE_NUMBER = STARTING_OFFICE_ID "
					+ "INNER JOIN OFFICES OFFICES_2 ON OFFICES_2.OFFICE_NUMBER = DESTINATION_OFFICE_ID "
					+ "WHERE ID = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, transferId);
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
							Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(0));
							jXDatePickerTransferStartDate.setDate(transferDate);
							Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(1));
							jXDatePickerTransferEndDate.setDate(dueDate);
							
							startOfficeId = databaseQueryResult.getInt(2);
							destinationOfficeId = databaseQueryResult.getInt(3);
							currentTransferTotalPrice = databaseQueryResult.getFloat(5);
							jCheckBox1.setSelected(databaseQueryResult.getInt(4) == 1);
							
							String startAddress = databaseQueryResult.getString(6);
							String destAddress = databaseQueryResult.getString(7);
							jLabelOfficeNumberStart.setText("" + startOfficeId);
							jLabelOfficeNumberEnd.setText("" + destinationOfficeId);
							jLabelOfficeAddress1Start.setText(startAddress.length() > 25 ? startAddress.substring(0, 25) + "-" : startAddress);
							jLabelOfficeAddress2Start.setText(startAddress.length() > 25 ? startAddress.substring(25) : "");
							jLabelOfficeAddress1End.setText(destAddress.length() > 25 ? destAddress.substring(0, 25) + "-" : destAddress);
							jLabelOfficeAddress2End.setText(destAddress.length() > 25 ? destAddress.substring(25) : "");
							
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(transferDate);
							transferYear = calendar.get(Calendar.YEAR);
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
	}
	
	private void RefreshTableItems(){
		float totalAmount = 0f;
		float totalPrice = 0f;
		
		// Get materials
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT TRANSFER_MATERIALS.ID, MATERIALS.NAME, TRANSFER_MATERIALS.AMOUNT_START, MEASURING_UNITS.NAME, TRANSFER_MATERIALS.PRICE, MATERIALS.ID "
					+ "FROM ((TRANSFER_MATERIALS INNER JOIN MATERIALS ON TRANSFER_MATERIALS.MATERIAL_ID = MATERIALS.ID)"
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? AND TRANSFER_MATERIALS.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, transferId);
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
						customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "Nabavna cijena", "Ukupno"});
						ArrayList<Integer> idListMaterials = new ArrayList<>();
						ArrayList<Integer> idListMaterialsMaterials = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = databaseQueryResult.getString(3);
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4);
							rowData[4] = ClientAppUtils.FloatToPriceString(price);
							
							customTableModel.addRow(rowData);
							idListMaterials.add(databaseQueryResult.getInt(0));
							idListMaterialsMaterials.add(databaseQueryResult.getInt(5));
							totalAmount += databaseQueryResult.getFloat(2);
							totalPrice += price;
						}
						jTableMaterials.setModel(customTableModel);
						transferMaterialsIdList = idListMaterials;
						transferMaterialsMaterialsIdList = idListMaterialsMaterials;;

						jTableMaterials.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneMaterials.getWidth() * 30 / 100);
						jTableMaterials.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneMaterials.getWidth() * 15 / 100);
						jTableMaterials.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
						jTableMaterials.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneMaterials.getWidth() * 15 / 100);
						jTableMaterials.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		// Get articles
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT TRANSFER_ARTICLES.ID, ARTICLES_1.NAME, ARTICLES_2.NAME, "
					+ "TRANSFER_ARTICLES.AMOUNT_START, MEASURING_UNITS_1.NAME, MEASURING_UNITS_2.NAME, "
					+ "TRANSFER_ARTICLES.PRICE, ARTICLES_1.ID, ARTICLES_2.ID "
					+ "FROM TRANSFER_ARTICLES "
					+ "INNER JOIN ARTICLES ARTICLES_1 ON TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ARTICLES_1.ID "
					+ "INNER JOIN ARTICLES ARTICLES_2 ON TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ARTICLES_2.ID "
					+ "INNER JOIN MEASURING_UNITS MEASURING_UNITS_1 ON ARTICLES_1.MEASURING_UNIT_ID = MEASURING_UNITS_1.ID "
					+ "INNER JOIN MEASURING_UNITS MEASURING_UNITS_2 ON ARTICLES_2.MEASURING_UNIT_ID = MEASURING_UNITS_2.ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? AND TRANSFER_ARTICLES.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, transferId);
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
						customTableModel.setColumnIdentifiers(new String[] {"Izlazni artikl (Mj. jed.)", "Odredišni artikl (Mj. jed.)", "Količina", "Cijena izlaznog artikla", "Ukupno",});
						ArrayList<Integer> idListArticles = new ArrayList<>();
						ArrayList<Integer> idListArticlesStartArticles = new ArrayList<>();
						ArrayList<Integer> idListArticlesDestArticles = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult.getString(1) + " (" + databaseQueryResult.getString(4) + ")";
							rowData[1] = databaseQueryResult.getString(2) + " (" + databaseQueryResult.getString(5) + ")";
							rowData[2] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(6));
							float price = databaseQueryResult.getFloat(3) * databaseQueryResult.getFloat(6);
							rowData[4] = ClientAppUtils.FloatToPriceString(price);
							
							customTableModel.addRow(rowData);
							idListArticles.add(databaseQueryResult.getInt(0));
							idListArticlesStartArticles.add(databaseQueryResult.getInt(7));
							idListArticlesDestArticles.add(databaseQueryResult.getInt(8));
							totalAmount += databaseQueryResult.getFloat(3);
							totalPrice += price;
						}
						jTableArticles.setModel(customTableModel);
						transferArticleIdList = idListArticles;
						transferArticleStartArticleIdList = idListArticlesStartArticles;
						transferArticleDestArticleIdList = idListArticlesDestArticles;

						jTableArticles.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticles.getWidth() * 30 / 100);
						jTableArticles.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticles.getWidth() * 30 / 100);
						jTableArticles.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneArticles.getWidth() * 13 / 100);
						jTableArticles.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneArticles.getWidth() * 13 / 100);
						jTableArticles.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneArticles.getWidth() * 14 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		jLabelTotalAmount.setText("" + totalAmount);
		jLabelTotalPrice.setText(ClientAppUtils.FloatToPriceString(totalPrice));
		jLabelTransferTotalValue.setText(ClientAppUtils.FloatToPriceString(totalPrice));

		// Update transfer total price
		if(currentTransferTotalPrice != totalPrice){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "UPDATE TRANSFERS SET TOTAL_PRICE = ? WHERE ID = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, totalPrice);
			databaseQuery.AddParam(2, transferId);

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

	private void RefreshArticleNormatives(int transferArticleId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		

		String query = "SELECT MATERIALS.NAME, TRANSFER_ARTICLE_MATERIALS.NORMATIVE, MEASURING_UNITS.NAME, TRANSFER_ARTICLE_MATERIALS.IS_STARTING "
				+ "FROM TRANSFER_ARTICLE_MATERIALS "
				+ "INNER JOIN MATERIALS ON TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = MATERIALS.ID "
				+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
				+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = ? AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, transferArticleId);
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
					customTableModel.setColumnIdentifiers(new String[] {"Izlazni materijali (normativ, mj. jedinica)", "Ulazni materijali (normativ, mj. jedinica)"});
					ArrayList<String> startMaterialsList = new ArrayList<>();
					ArrayList<String> destMaterialsList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						if(databaseQueryResult.getInt(3) == 1){
							startMaterialsList.add(databaseQueryResult.getString(0) + " (" + databaseQueryResult.getString(1) + ", " + databaseQueryResult.getString(2) + ")");
						} else {
							destMaterialsList.add(databaseQueryResult.getString(0) + " (" + databaseQueryResult.getString(1) + ", " + databaseQueryResult.getString(2) + ")");
						}
					}
					
					int minIndex = startMaterialsList.size();
					if(destMaterialsList.size() < minIndex){
						minIndex = destMaterialsList.size();
					}
					
					for (int i = 0; i < minIndex; ++i){
						Object[] rowData = new Object[2];
						rowData[0] = startMaterialsList.get(i);
						rowData[1] = destMaterialsList.get(i);
						customTableModel.addRow(rowData);
					}
					
					if(startMaterialsList.size() < destMaterialsList.size()){
						for (int i = minIndex; i < destMaterialsList.size(); ++i){
							Object[] rowData = new Object[2];
							rowData[0] = "";
							rowData[1] = destMaterialsList.get(i);
							customTableModel.addRow(rowData);
						}
					} else {
						for (int i = minIndex; i < startMaterialsList.size(); ++i){
							Object[] rowData = new Object[2];
							rowData[0] = startMaterialsList.get(i);
							rowData[1] = "";
							customTableModel.addRow(rowData);
						}
					}
					
					jTableArticleNormatives.setModel(customTableModel);
					jTableArticleNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
					jTableArticleNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
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

        jLabelTitle = new javax.swing.JLabel();
        jLabelTransferNumber = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jXDatePickerTransferStartDate = new org.jdesktop.swingx.JXDatePicker();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelOfficeNumberEnd = new javax.swing.JLabel();
        jLabelOfficeAddress1End = new javax.swing.JLabel();
        jLabelOfficeAddress2End = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabelTransferTotalValue = new javax.swing.JLabel();
        jXDatePickerTransferEndDate = new org.jdesktop.swingx.JXDatePicker();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonPrint = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelOfficeNumberStart = new javax.swing.JLabel();
        jLabelOfficeAddress1Start = new javax.swing.JLabel();
        jLabelOfficeAddress2Start = new javax.swing.JLabel();
        jPanelItems = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelMaterials = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPaneMaterials = new javax.swing.JScrollPane();
        jTableMaterials = new javax.swing.JTable();
        jButtonAddMaterial = new javax.swing.JButton();
        jButtonEditMaterial = new javax.swing.JButton();
        jButtonDeleteMaterial = new javax.swing.JButton();
        jPanelArticles = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPaneArticles = new javax.swing.JScrollPane();
        jTableArticles = new javax.swing.JTable();
        jScrollPaneArticleNormatives = new javax.swing.JScrollPane();
        jTableArticleNormatives = new javax.swing.JTable();
        jButtonAddArticle = new javax.swing.JButton();
        jButtonEditArticle = new javax.swing.JButton();
        jButtonDeleteArticle = new javax.swing.JButton();
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
        jLabelTitle.setText("Međuskladišnica broj: ");

        jLabelTransferNumber.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTransferNumber.setText("0000");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Međuskladišnica"));

        jLabel1.setText("Datum otpreme (F1):");
        jLabel1.setPreferredSize(new java.awt.Dimension(105, 14));

        jXDatePickerTransferStartDate.setPreferredSize(new java.awt.Dimension(104, 25));
        jXDatePickerTransferStartDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerTransferStartDatePropertyChange(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Odredišna poslovnica"));

        jLabel2.setText("Br. poslovnice:");
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabel5.setText("Adresa:");
        jLabel5.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabelOfficeNumberEnd.setText("ime");

        jLabelOfficeAddress1End.setText("adresa1");

        jLabelOfficeAddress2End.setText("adresa2");

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
                        .addComponent(jLabelOfficeNumberEnd))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelOfficeAddress2End)
                            .addComponent(jLabelOfficeAddress1End))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOfficeNumberEnd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOfficeAddress1End))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOfficeAddress2End)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel7.setText("Nabavna vrijednost:");
        jLabel7.setPreferredSize(new java.awt.Dimension(105, 14));

        jLabel8.setText("Datum preuzimanja");
        jLabel8.setPreferredSize(new java.awt.Dimension(105, 14));

        jLabelTransferTotalValue.setText("nabavna_vrijednost");

        jXDatePickerTransferEndDate.setPreferredSize(new java.awt.Dimension(104, 25));
        jXDatePickerTransferEndDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerTransferEndDatePropertyChange(evt);
            }
        });

        jCheckBox1.setText("Preuzeto");
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

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Izlazna poslovnica"));

        jLabel4.setText("Br. poslovnice:");
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabel6.setText("Adresa:");
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabelOfficeNumberStart.setText("ime");

        jLabelOfficeAddress1Start.setText("adresa1");

        jLabelOfficeAddress2Start.setText("adresa2");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelOfficeNumberStart))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelOfficeAddress2Start)
                            .addComponent(jLabelOfficeAddress1Start))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOfficeNumberStart))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOfficeAddress1Start))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOfficeAddress2Start)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerTransferStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerTransferEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelTransferTotalValue))
                            .addComponent(jCheckBox1))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerTransferStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTransferTotalValue))
                .addGap(57, 57, 57)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerTransferEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
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
                .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Materijali [F2]", jPanelMaterials);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel11.setText("Artikli");

        jTableArticles.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneArticles.setViewportView(jTableArticles);

        jTableArticleNormatives.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneArticleNormatives.setViewportView(jTableArticleNormatives);

        jButtonAddArticle.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAddArticle.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonAddArticle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddArticleActionPerformed(evt);
            }
        });

        jButtonEditArticle.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEditArticle.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonEditArticle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditArticleActionPerformed(evt);
            }
        });

        jButtonDeleteArticle.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDeleteArticle.setPreferredSize(new java.awt.Dimension(70, 50));
        jButtonDeleteArticle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteArticleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelArticlesLayout = new javax.swing.GroupLayout(jPanelArticles);
        jPanelArticles.setLayout(jPanelArticlesLayout);
        jPanelArticlesLayout.setHorizontalGroup(
            jPanelArticlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelArticlesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelArticlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneArticles, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                    .addGroup(jPanelArticlesLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonAddArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonEditArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonDeleteArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPaneArticleNormatives))
                .addContainerGap())
        );
        jPanelArticlesLayout.setVerticalGroup(
            jPanelArticlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelArticlesLayout.createSequentialGroup()
                .addGroup(jPanelArticlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelArticlesLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel11))
                    .addGroup(jPanelArticlesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelArticlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonAddArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonEditArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonDeleteArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneArticles, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneArticleNormatives, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Artikli [F3]", jPanelArticles);

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
                .addComponent(jTabbedPane1)
                .addContainerGap())
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
                        .addComponent(jLabelTransferNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
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
                            .addComponent(jLabelTransferNumber)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanelItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String transferStartDate = jXDatePickerTransferStartDate.getEditor().getText().trim();
		String transferEndDate = jXDatePickerTransferEndDate.getEditor().getText().trim();
		int isDelivered = jCheckBox1.isSelected() ? 1 : 0;
		
		Date transferDateDate;
		try {
			transferDateDate = new SimpleDateFormat("dd.MM.yyyy").parse(transferStartDate);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Uneseni datum međuskladišnice nije isparavan");
            return;
		}
		
		Date transferDateEnd;
		try {
			transferDateEnd = new SimpleDateFormat("dd.MM.yyyy").parse(transferEndDate);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Uneseni datum preuzimanja nije isparavan");
            return;
		}
		
		if(transferDateEnd.before(transferDateDate)){
			ClientAppLogger.GetInstance().ShowMessage("Datum preuzimanja međusklašnice ne može biti prije datuma otpreme.");
            return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(transferDateDate);
		if(transferId == -1 && calendar.get(Calendar.YEAR) != ClientAppSettings.currentYear){
			ClientAppLogger.GetInstance().ShowMessage("Datum međuskladišnice mora biti u tekućoj godini");
            return;
		} else if(transferId != -1 && calendar.get(Calendar.YEAR) != transferYear){
			ClientAppLogger.GetInstance().ShowMessage("Datum međuskladišnice mora biti u tekućoj godini");
            return;
		}
		
		String queryUpdateAllArticleMaterialAmountsPlus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
					+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
		
		String queryUpdateAllMaterialAmountsPlus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
					+ "SELECT SUM(TRANSFER_MATERIALS.AMOUNT_START) "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? "
					+ "AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? "
					+ "AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
		
		String queryUpdateAllArticleMaterialAmountsMinus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
		
		String queryUpdateAllMaterialAmountsMinus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(TRANSFER_MATERIALS.AMOUNT_START) "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? "
					+ "AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? "
					+ "AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
		
		if(transferId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "INSERT INTO TRANSFERS (ID, TRANSFER_START_DATE, TRANSFER_RECIEVED_DATE, STARTING_OFFICE_ID, DESTINATION_OFFICE_ID, "
					+ "TOTAL_PRICE, IS_DELIVERED, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.SetAutoIncrementParam(1, "ID", "TRANSFERS");
			databaseQuery.AddParam(2, transferStartDate);
			databaseQuery.AddParam(3, transferEndDate);
			databaseQuery.AddParam(4, startOfficeId);
			databaseQuery.AddParam(5, destinationOfficeId);
			databaseQuery.AddParam(6, 0);
			databaseQuery.AddParam(7, isDelivered);
			databaseQuery.AddParam(8, 0);

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
						if(transferId == -1){
							transferId = databaseQueryResult.autoGeneratedKey;
							transferYear = calendar.get(Calendar.YEAR);
						}
					} else {
						return;
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		} else {
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			if (isDelivered == 1) {
				multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsPlus);
				multiDatabaseQuery.SetQuery(1, queryUpdateAllArticleMaterialAmountsPlus);
			} else if (isDelivered == 0) {
				multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsMinus);
				multiDatabaseQuery.SetQuery(1, queryUpdateAllArticleMaterialAmountsMinus);
			}
			
			multiDatabaseQuery.AddParam(0, 1, transferId);
			multiDatabaseQuery.AddParam(0, 2, transferId);
			multiDatabaseQuery.AddParam(0, 3, destinationOfficeId);
			multiDatabaseQuery.AddParam(0, 4, transferYear);
			
			multiDatabaseQuery.AddParam(1, 1, transferId);
			multiDatabaseQuery.AddParam(1, 2, transferId);
			multiDatabaseQuery.AddParam(1, 3, destinationOfficeId);
			multiDatabaseQuery.AddParam(1, 4, transferYear);
			
			String updateQuery = "UPDATE TRANSFERS SET TRANSFER_START_DATE = ?, TRANSFER_RECIEVED_DATE = ?, IS_DELIVERED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(2, updateQuery);
			multiDatabaseQuery.AddParam(2, 1, transferStartDate);
			multiDatabaseQuery.AddParam(2, 2, transferEndDate);
			multiDatabaseQuery.AddParam(2, 3, isDelivered);
			multiDatabaseQuery.AddParam(2, 4, transferId);
			
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
							changeSuccess = true;
						}
					} catch (Exception ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
		}
		
		if(changeSuccess){
			jLabelTransferNumber.setText("" + transferId);
			jPanelItems.setEnabled(true);
			jTabbedPane1.setEnabled(true);
			jButtonSave.setEnabled(false);
			
			jButtonAddMaterial.setEnabled(true);
			jButtonEditMaterial.setEnabled(true);
			jButtonDeleteMaterial.setEnabled(true);
			jButtonAddArticle.setEnabled(true);
			jButtonEditArticle.setEnabled(true);
			jButtonDeleteArticle.setEnabled(true);
		}
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintActionPerformed
		PrintTableExtraData extraDataMaterials = new PrintTableExtraData();
		extraDataMaterials.headerList.add(new Pair<>("Izlazna poslovnica:         ", jLabelOfficeNumberStart.getText() + " - " + jLabelOfficeAddress1Start.getText()));
		extraDataMaterials.headerList.add(new Pair<>("Odredišna poslovnica:    ", jLabelOfficeNumberEnd.getText() + " - " + jLabelOfficeAddress1End.getText()));
		extraDataMaterials.headerList.add(new Pair<>("Datum otpreme:              ", jXDatePickerTransferStartDate.getEditor().getText().trim()));
		extraDataMaterials.headerList.add(new Pair<>("Datum preuzimanja:       ", jXDatePickerTransferEndDate.getEditor().getText().trim()));
		extraDataMaterials.headerList.add(new Pair<>("Preuzeto:                         ", jCheckBox1.isSelected() ? "Da" : "Ne"));
		extraDataMaterials.headerList.add(new Pair<>("Nabavna vrijednost:       ", jLabelTransferTotalValue.getText()));
		extraDataMaterials.headerList.add(new Pair<>(" ", " "));
		extraDataMaterials.headerList.add(new Pair<>("Materijali: ", ""));

		PrintTableExtraData extraDataArticles = new PrintTableExtraData();
		extraDataArticles.headerList.add(new Pair<>("Artikli: ", ""));
		
		PrintUtils.PrintA4Table("Međuskladišnica-" + jLabelTransferNumber.getText(), 
				new String[]{"Međuskladišnica broj: " + jLabelTransferNumber.getText(), ""}, 
				new JTable[]{jTableMaterials, jTableArticles}, 
				new int[][]{new int[]{0, 1, 2, 3, 4}, new int[]{0, 1, 2, 3, 4}},
				new int[][]{new int[]{}, new int[]{}}, 
				new PrintTableExtraData[]{extraDataMaterials, extraDataArticles},
				new boolean[]{false, false},
                                "");
    }//GEN-LAST:event_jButtonPrintActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
		jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jXDatePickerTransferStartDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerTransferStartDatePropertyChange
        jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jXDatePickerTransferStartDatePropertyChange

    private void jXDatePickerTransferEndDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerTransferEndDatePropertyChange
        jButtonSave.setEnabled(true);
    }//GEN-LAST:event_jXDatePickerTransferEndDatePropertyChange

    private void jButtonAddMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMaterialActionPerformed
        if(transferId == -1)
			return;
		
		ClientAppSelectMaterialDialog selectDialog = new ClientAppSelectMaterialDialog(null, true, transferYear);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppTransfersMaterialAddEditDialog addEditdialog = new ClientAppTransfersMaterialAddEditDialog(null, true, transferId, -1, selectDialog.selectedId, startOfficeId, destinationOfficeId, transferYear);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTableItems();
			}
        }
    }//GEN-LAST:event_jButtonAddMaterialActionPerformed

    private void jButtonEditMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditMaterialActionPerformed
		if(transferId == -1)
			return;
		
		int rowId = jTableMaterials.getSelectedRow();
        if(jTableMaterials.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
        int transferMaterialId = transferMaterialsIdList.get(rowId);
        int transferMaterialMaterialId = transferMaterialsMaterialsIdList.get(rowId);
		
		ClientAppTransfersMaterialAddEditDialog addEditdialog = new ClientAppTransfersMaterialAddEditDialog(null, true, transferId, transferMaterialId, transferMaterialMaterialId, startOfficeId, destinationOfficeId, transferYear);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTableItems();
		}
    }//GEN-LAST:event_jButtonEditMaterialActionPerformed

    private void jButtonDeleteMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteMaterialActionPerformed
		if(transferId == -1)
			return;
		
		int rowId = jTableMaterials.getSelectedRow();
        if(jTableMaterials.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
        int transferMaterialId = transferMaterialsIdList.get(rowId);
        String tableValue = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 0));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati materijal " + tableValue, "Obriši materijal", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			int materialId = transferMaterialsMaterialsIdList.get(rowId);
			String query1 = "UPDATE MATERIAL_AMOUNTS "
					+ "SET AMOUNT = AMOUNT + (SELECT AMOUNT_START FROM TRANSFER_MATERIALS WHERE ID = ?) "
					+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
					+ "AND (SELECT IS_DELETED FROM TRANSFERS WHERE ID = ?) = 0 "
					+ "AND (SELECT IS_DELETED FROM TRANSFER_MATERIALS WHERE ID = ?) = 0 "
					+ "AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(0, query1);
			multiDatabaseQuery.AddParam(0, 1, transferMaterialId);
			multiDatabaseQuery.AddParam(0, 2, materialId);
			multiDatabaseQuery.AddParam(0, 3, startOfficeId);
			multiDatabaseQuery.AddParam(0, 4, transferId);
			multiDatabaseQuery.AddParam(0, 5, transferMaterialId);
			multiDatabaseQuery.AddParam(0, 6, transferYear);
			
			String query2 = "UPDATE MATERIAL_AMOUNTS "
					+ "SET AMOUNT = AMOUNT - (SELECT AMOUNT_START FROM TRANSFER_MATERIALS WHERE ID = ?) "
					+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
					+ "AND (SELECT IS_DELIVERED FROM TRANSFERS WHERE ID = ?) = 1 "
					+ "AND (SELECT IS_DELETED FROM TRANSFERS WHERE ID = ?) = 0 "
					+ "AND (SELECT IS_DELETED FROM TRANSFER_MATERIALS WHERE ID = ?) = 0 "
					+ "AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(1, query2);
			multiDatabaseQuery.AddParam(1, 1, transferMaterialId);
			multiDatabaseQuery.AddParam(1, 2, materialId);
			multiDatabaseQuery.AddParam(1, 3, destinationOfficeId);
			multiDatabaseQuery.AddParam(1, 4, transferId);
			multiDatabaseQuery.AddParam(1, 5, transferId);
			multiDatabaseQuery.AddParam(1, 6, transferMaterialId);
			multiDatabaseQuery.AddParam(1, 7, transferYear);
			
			String query0 = "UPDATE TRANSFER_MATERIALS SET IS_DELETED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(2, query0);
			multiDatabaseQuery.AddParam(2, 1, 1);
			multiDatabaseQuery.AddParam(2, 2, transferMaterialId);
			
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

    private void jButtonAddArticleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddArticleActionPerformed
		if(transferId == -1)
			return;
		
		ClientAppTransfersSelectArticlesDialog selectDialog = new ClientAppTransfersSelectArticlesDialog(null, true);
        selectDialog.setVisible(true);
		if(selectDialog.selectedIdStart != -1 && selectDialog.selectedIdDest != -1){
			ClientAppTransfersArticleAddEditDialog addEditdialog = new ClientAppTransfersArticleAddEditDialog(null, true, transferId, -1, selectDialog.selectedIdStart, selectDialog.selectedIdDest, startOfficeId, destinationOfficeId, transferYear);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTableItems();
			}
        }
    }//GEN-LAST:event_jButtonAddArticleActionPerformed

    private void jButtonEditArticleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditArticleActionPerformed
        if(transferId == -1)
			return;
		
		if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableArticles.getSelectedRow();
        int transferArticleId = transferArticleIdList.get(rowId);
        int transferArticleStartArticleId = transferArticleStartArticleIdList.get(rowId);
        int transferArticleDestArticleId = transferArticleDestArticleIdList.get(rowId);
		
		ClientAppTransfersArticleAddEditDialog addEditdialog = new ClientAppTransfersArticleAddEditDialog(null, true, transferId, transferArticleId, transferArticleStartArticleId, transferArticleDestArticleId, startOfficeId, destinationOfficeId, transferYear);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTableItems();
		}
    }//GEN-LAST:event_jButtonEditArticleActionPerformed

    private void jButtonDeleteArticleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteArticleActionPerformed
        if(transferId == -1)
			return;
		
		if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableArticles.getSelectedRow();
        int transferArticleId = transferArticleIdList.get(rowId);
        String tableValue1 = String.valueOf(jTableArticles.getModel().getValueAt(rowId, 0));
        String tableValue2 = String.valueOf(jTableArticles.getModel().getValueAt(rowId, 1));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati transfer artikala " + tableValue1 + " --> " + tableValue2, "Obriši transfer artikala", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			String query1 = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
					+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(0, query1);
			multiDatabaseQuery.AddParam(0, 1, transferArticleId);
			multiDatabaseQuery.AddParam(0, 2, transferArticleId);
			multiDatabaseQuery.AddParam(0, 3, startOfficeId);
			multiDatabaseQuery.AddParam(0, 4, transferYear);
			
			String query2 = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(1, query2);
			multiDatabaseQuery.AddParam(1, 1, transferArticleId);
			multiDatabaseQuery.AddParam(1, 2, transferArticleId);
			multiDatabaseQuery.AddParam(1, 3, destinationOfficeId);
			multiDatabaseQuery.AddParam(1, 4, transferYear);
			
			String query0 = "UPDATE TRANSFER_ARTICLES SET IS_DELETED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(2, query0);
			multiDatabaseQuery.AddParam(2, 1, 1);
			multiDatabaseQuery.AddParam(2, 2, transferArticleId);
			
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
			
			CustomTableModel customTableModel = new CustomTableModel();
			customTableModel.setColumnIdentifiers(new String[] {"Izlazni materijali", "Ulazni materijali"});
			jTableArticleNormatives.setModel(customTableModel);
			jTableArticleNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
			jTableArticleNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticleNormatives.getWidth() * 50 / 100);
        }
    }//GEN-LAST:event_jButtonDeleteArticleActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		tabSwitchFlag = true;
    }//GEN-LAST:event_jTabbedPane1StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddArticle;
    private javax.swing.JButton jButtonAddMaterial;
    private javax.swing.JButton jButtonDeleteArticle;
    private javax.swing.JButton jButtonDeleteMaterial;
    private javax.swing.JButton jButtonEditArticle;
    private javax.swing.JButton jButtonEditMaterial;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrint;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelOfficeAddress1End;
    private javax.swing.JLabel jLabelOfficeAddress1Start;
    private javax.swing.JLabel jLabelOfficeAddress2End;
    private javax.swing.JLabel jLabelOfficeAddress2Start;
    private javax.swing.JLabel jLabelOfficeNumberEnd;
    private javax.swing.JLabel jLabelOfficeNumberStart;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTotalAmount;
    private javax.swing.JLabel jLabelTotalPrice;
    private javax.swing.JLabel jLabelTransferNumber;
    private javax.swing.JLabel jLabelTransferTotalValue;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelArticles;
    private javax.swing.JPanel jPanelItems;
    private javax.swing.JPanel jPanelMaterials;
    private javax.swing.JPanel jPanelTotal;
    private javax.swing.JScrollPane jScrollPaneArticleNormatives;
    private javax.swing.JScrollPane jScrollPaneArticles;
    private javax.swing.JScrollPane jScrollPaneMaterials;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableArticleNormatives;
    private javax.swing.JTable jTableArticles;
    private javax.swing.JTable jTableMaterials;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTransferEndDate;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTransferStartDate;
    // End of variables declaration//GEN-END:variables
}
