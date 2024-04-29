/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.cashregister;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ui.ClientAppKeyboardDialog;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.ClientAppKeyboardMultilineDialog;
import hr.adinfo.client.ui.ClientAppLoginDialog;
import hr.adinfo.client.ui.ClientAppLoginPasswordOnlyDialog;
import hr.adinfo.client.ui.reports.ClientAppReportsTotalDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Pair;
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
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Matej
 */
public class ClientAppCashRegisterStandardDialog extends javax.swing.JDialog {
	private static final int COLUMN_INDEX_ID = 0;
	private static final int COLUMN_INDEX_NAME = 1;
	private static final int COLUMN_INDEX_AMOUNT = 2;
	private static final int COLUMN_INDEX_PRICE = 3;
	private static final int COLUMN_INDEX_DISCOUNT = 4;
	private static final int COLUMN_INDEX_TOTAL = 5;
	
	// Working time data
	private boolean[] isWorkDay = new boolean[7];
	private int[] workDayHoursFrom = new int[7];
	private int[] workDayHoursTo = new int[7];
	private int[] workDayMinutesFrom = new int[7];
	private int[] workDayMinutesTo = new int[7];
	private ArrayList<Integer> currentUserWorkDayList = new ArrayList<>();
	private ArrayList<Integer> currentUserWorkHourFromList = new ArrayList<>();
	private ArrayList<Integer> currentUserWorkHourToList = new ArrayList<>();
	private ArrayList<Integer> currentUserWorkMinuteFromList = new ArrayList<>();
	private ArrayList<Integer> currentUserWorkMinuteToList = new ArrayList<>();
	
	private class WarehouseItem {
		int itemId;
		int itemType;
		String itemName;
		float itemPrice;
		float eventPrice;
		float taxRate;
		float consumptionTaxRate;
		float packagingRefund;
		boolean isFood;
		boolean articleWithoutNormatives;
	}
	
	private int[] quickPickId = new int[31];
	private int[] quickPickType = new int[31];
	private String[] quickPickName = new String[31];
	private boolean isQuickPick;
	
	private ArrayList<WarehouseItem> warehouseItems = new ArrayList<>();
	private DefaultTableModel defaultTableModel;
	private ArrayList<Pair<Integer, Integer>> articlesNormativeCount = new ArrayList<>();
			
	private final JLabel[] jLabelQuickPickGroup;
	private final String[] jLabelQuickPickLetters = new String[]{
		"A", "B", "C", "Č", "Ć", "D", "Đ", "E", "F", "G", "H", "I", "J", "K", 
		"L", "M", "N", "O", "P", "R", "S", "Š", "T", "U", "V", "Z", "Ž", "Y",
		"X", "Q", "W"
	};
	
	private boolean cashRegisterOpen = true;
        private boolean tablesOpened = false;
	
	private boolean invoiceNoteEnabled;
	private String defaultNote = "";
	private Invoice lastInvoice = null;
	private int currentTableId = -1;
	private boolean eventPrices;
	private boolean lockCashRegister;
	private long lockCashRegisterTime;
	private long lastInputTime;
        private boolean lockTable;
        private long lockTableTime;
        private boolean employeeClickTable;
        
        private String street;
        private String houseNum;
        private String town;
	
	// Invoice data
	private Invoice invoice = new Invoice();
	private int startOfficeNumber;
	private String startOfficeTag;
	private int startCashRegisterNumber;
	private String startCompanyOib;
	private boolean isProduction;
	private boolean isInVatSystem;
	// Unfiscalized invoices check
	private Date oldestUnfiscalizedInvoiceDate;
        private boolean isDialogOpen = false;
        private Lock dialogLock = new ReentrantLock();
        private ClientAppCashRegisterTablesDialog dialog;
        private AtomicBoolean isDialogOpenAtomic = new AtomicBoolean(false);

	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppCashRegisterStandardDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		
		//setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		
		final Window thisWindow = this;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				if(!thisWindow.isDisplayable()){
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
				}
				
				lastInputTime = System.currentTimeMillis() / 1000;
				
				if(!thisWindow.isFocused())
					return false;
				
				if(!jTextField1.isFocusOwner()){
					jTextField1.requestFocusInWindow();
				}
				
				if(ke.getID() == KeyEvent.KEY_PRESSED){
					if(ke.isControlDown()){
						if(ke.getKeyCode() == KeyEvent.VK_S){
							jButtonItemNote.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_P){
							jButtonItemDiscount.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F11){
							jButtonPrintKitchen.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F10){
							jButtonPrintBar.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F6){
							jButtonLastInvoiceChangePaymentMethod.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F9){
							jButtonInvoiceCancelation.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F7){
							jButtonStaffInvoice.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_I){
							jButtonOffer.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_U){
							jButtonLoadOffer.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F12){
							jButtonTotal.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_N){
							jButtonEventPrices.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F5){
							jButtonSubtotal.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_R){
							jButtonInvoiceR1.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_O){
							jButtonInvoiceDiscount.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_K){
							jButtonCard.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_D){
							jButtonOtherPaymentMethods.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F8){
							
						}
					} else {
						if (ke.getKeyCode() == KeyEvent.VK_ESCAPE){
							jButtonExit.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_ENTER){
							OnEnterPressed();
						} else if(ke.getKeyCode() == KeyEvent.VK_ADD){
							OnPlusClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_SUBTRACT){
							OnMinusClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F10){
							int rowId = GetSelectedItemIndex();
							if (rowId != -1) {
								OnTableDoubleClick(rowId);
							}
						} else if(ke.getKeyCode() == KeyEvent.VK_F11){
							jTextField1.requestFocusInWindow();
						} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
							jButtonAdd.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
							jButtonDelete.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F6){
							jButtonInvoiceCopyPrint.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F8){
							jButtonStaffUserChange.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F12){
							jButtonSaldo.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F9){
							jButtonTables.doClick();
						} else if(ke.getKeyCode() == KeyEvent.VK_F7){
							jButtonOpenCashRegister.doClick();
						}  else if(ke.getKeyCode() == KeyEvent.VK_F5){
							jButtonCash.doClick();
						} else {
							char c = ke.getKeyChar();
							if(isQuickPick && Character.isAlphabetic(c)){
								OnQuickPickCharacterPressed(c);
							}
						}
					}
				}
				
				return false;
			}
		});
		lastInputTime = System.currentTimeMillis() / 1000;
		
		startOfficeNumber = Licence.GetOfficeNumber();
		startOfficeTag = Licence.GetOfficeTag();
		startCashRegisterNumber = Licence.GetCashRegisterNumber();
		startCompanyOib = Licence.GetOIB();
               
		jLabelCompanyData1.setText(Licence.GetCompanyName());
		jLabelCompanyData2.setText("Oznaka PP: " + startOfficeTag + ", Adresa: " + Licence.GetOfficeAddress());
		jLabelCompanyData3.setText("OIB: " + startCompanyOib);
		jLabelR1.setText("");
		jLabelInvoiceDiscount.setText("");
		jLabelCashRegisterName.setText("Kasa " + startCashRegisterNumber);
		jLabelSubtotal.setText("");
		jLabelEventPrices.setText("");
		
		jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(40, 0));
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				OnCashRegisterExit();
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				OnCashRegisterExit();
			}
		});
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				int rowId = GetSelectedItemIndex();
				if (mouseEvent.getClickCount() == 2 && rowId != -1) {
					OnTableDoubleClick(rowId);
				}
			}
		});
                
		
		jTextField1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == '*'){
					String text = ((JTextField) (e.getComponent())).getText();
					if (text.length() != 0){
						e.consume();
					}
				} else if(c == '-'){
					String text = ((JTextField) (e.getComponent())).getText();
					if (!(text.length() == 1 && text.charAt(0) == '*')){
						e.consume();
					}
				} else if (!Character.isDigit(c) && isQuickPick){
					e.consume();
				}
			}
		});

		// Init
		jLabelQuickPickGroup = new JLabel[]{
			jLabelQuickPick0, jLabelQuickPick1, jLabelQuickPick2, jLabelQuickPick3, jLabelQuickPick4,
			jLabelQuickPick5, jLabelQuickPick6, jLabelQuickPick7, jLabelQuickPick8, jLabelQuickPick9,
			jLabelQuickPick10, jLabelQuickPick11, jLabelQuickPick12, jLabelQuickPick13, jLabelQuickPick14,
			jLabelQuickPick15, jLabelQuickPick16, jLabelQuickPick17, jLabelQuickPick18, jLabelQuickPick19,
			jLabelQuickPick20, jLabelQuickPick21, jLabelQuickPick22, jLabelQuickPick23, jLabelQuickPick24,
			jLabelQuickPick25, jLabelQuickPick26, jLabelQuickPick27, jLabelQuickPick28, jLabelQuickPick29,
			jLabelQuickPick30, 
		};
		
		// Create if no exist
		ClientAppUtils.CreateConsumptionTaxAmountsIfNoExist(startOfficeNumber);
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(startOfficeNumber);
		ClientAppUtils.CreateAllTradingGoodsAmountsIfNoExist(startOfficeNumber);
		
		// Load settings
		ClientAppSettings.LoadSettings(false);
		invoiceNoteEnabled = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE.ordinal());
		isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		isQuickPick = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_QUICKCHOICE.ordinal());
		String[] quickPickIdStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickNameStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickTypeStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		for (int i = 0; i < jLabelQuickPickGroup.length; ++i){
			if (i < quickPickIdStrings.length){
				quickPickId[i] = Integer.parseInt(quickPickIdStrings[i]);
			} else {
				quickPickId[i] = -1;
			}
			
			if (i < quickPickTypeStrings.length){
				quickPickType[i] = Integer.parseInt(quickPickTypeStrings[i]);
			} else {
				quickPickType[i] = -1;
			}
			
			if (i < quickPickNameStrings.length){
				quickPickName[i] = quickPickNameStrings[i];
			} else {
				quickPickName[i] = "";
			}
			
			jLabelQuickPickGroup[i].setText(jLabelQuickPickLetters[i] + " = " + quickPickName[i]);
		}
		
		lockCashRegister = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER.ordinal());
		lockCashRegisterTime = (long) ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER_TIME.ordinal());
		isInVatSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		// Apply settings
		UpdateCurrentUser();
		
		// Setup table
		//jTable1.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 40 / 100);
		jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(5).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		defaultTableModel = (DefaultTableModel) jTable1.getModel();
		
		// Get item names
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);
		multiDatabaseQuery.SetQuery(0, "SELECT ARTICLES.ID, ARTICLES.NAME, ARTICLES.PRICE, TAX_RATES.VALUE, CONSUMPTION_TAX_VALUES.VALUE, EVENT_PRICE, CONSUMPTION_TAXES.NAME "
				+ "FROM ARTICLES "
				+ "INNER JOIN TAX_RATES ON ARTICLES.TAX_RATE_ID = TAX_RATES.ID "
				+ "INNER JOIN CONSUMPTION_TAXES ON ARTICLES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "INNER JOIN CONSUMPTION_TAX_VALUES ON CONSUMPTION_TAX_VALUES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND ARTICLES.IS_ACTIVE = 1 AND CONSUMPTION_TAX_VALUES.OFFICE_NUMBER = ?");
		multiDatabaseQuery.AddParam(0, 1, startOfficeNumber);
		multiDatabaseQuery.SetQuery(1, "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, TRADING_GOODS.PRICE, TAX_RATES.VALUE, EVENT_PRICE, PACKAGING_REFUNDS.VALUE "
				+ "FROM TRADING_GOODS "
				+ "INNER JOIN TAX_RATES ON TRADING_GOODS.TAX_RATE_ID = TAX_RATES.ID "
				+ "INNER JOIN PACKAGING_REFUNDS ON TRADING_GOODS.PACKAGING_REFUND_ID = PACKAGING_REFUNDS.ID "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0 AND TRADING_GOODS.IS_ACTIVE = 1");
		multiDatabaseQuery.SetQuery(2, "SELECT SERVICES.ID, SERVICES.NAME, SERVICES.PRICE, TAX_RATES.VALUE, EVENT_PRICE "
				+ "FROM SERVICES "
				+ "INNER JOIN TAX_RATES ON SERVICES.TAX_RATE_ID = TAX_RATES.ID "
				+ "WHERE SERVICES.IS_DELETED = 0 AND SERVICES.IS_ACTIVE = 1");
		multiDatabaseQuery.SetQuery(3, "SELECT ARTICLES.ID, "
					+ "(SELECT COUNT(NORMATIVES.ID) FROM NORMATIVES WHERE NORMATIVES.ARTICLE_ID = ARTICLES.ID AND NORMATIVES.IS_DELETED = 0) "
				+ "FROM ARTICLES "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND ARTICLES.IS_ACTIVE = 1");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				MultiDatabaseQueryResponse multiDatabaseQueryResponse = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					multiDatabaseQueryResponse = (MultiDatabaseQueryResponse) serverResponse;
				}
				if(multiDatabaseQueryResponse != null){
					while (multiDatabaseQueryResponse.databaseQueryResult[3].next()) {
						int articleId = multiDatabaseQueryResponse.databaseQueryResult[3].getInt(0);
						int articleNormativesCount = multiDatabaseQueryResponse.databaseQueryResult[3].getInt(1);
						articlesNormativeCount.add(new Pair<>(articleId, articleNormativesCount));
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[0].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[0].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[0].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(3);
						warehouseItem.consumptionTaxRate = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(4);
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(5);
						warehouseItem.packagingRefund = 0f;
						warehouseItem.isFood = multiDatabaseQueryResponse.databaseQueryResult[0].getString(6).toLowerCase().contains("hrana");
						
						for (int i = 0; i < articlesNormativeCount.size(); ++i){
							if(articlesNormativeCount.get(i).getKey() == id && articlesNormativeCount.get(i).getValue() == 0){
								warehouseItem.articleWithoutNormatives = true;
								break;
							}
						}
						
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[1].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[1].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[1].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(3);
						warehouseItem.consumptionTaxRate = 0f;
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(4);
						warehouseItem.packagingRefund = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(5);
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[2].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[2].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[2].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(3);
						warehouseItem.consumptionTaxRate = 0f;
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(4);
						warehouseItem.packagingRefund = 0f;
						warehouseItems.add(warehouseItem);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		// Exit if licence invalid
		if(startOfficeNumber == 0 || startCashRegisterNumber == 0 || startCompanyOib == null || startOfficeTag == null){
			ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške kod čitanja licence. Molimo ponovno pokrenite program.");
			Utils.DisposeDialog(this);
			return;
		}
		
		// Load button settings
		jButtonSubtotal.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SUBTOTAL.ordinal()));
		jButtonPrintBar.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR.ordinal()));
		jButtonPrintKitchen.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN.ordinal()));
		jButtonLastInvoiceChangePaymentMethod.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_CHANGEPAYMENTMETHOD.ordinal()));
		jButtonStaffInvoice.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_STAFFINVOICE.ordinal()));
		jButtonItemNote.setEnabled(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_ITEM_NOTE.ordinal()));
		jButtonTables.setEnabled(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT.ordinal()) != 0);
		jButtonCash.setEnabled(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()));
		jButtonCard.setEnabled(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()));
		jButtonOtherPaymentMethods.setEnabled(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()));
		jButtonOffer.setEnabled(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()));
		jButtonLoadOffer.setEnabled(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()));

		if(!jButtonSubtotal.isEnabled()) jButtonSubtotal.setText("");
		if(!jButtonPrintBar.isEnabled()) jButtonPrintBar.setText("");
		if(!jButtonPrintKitchen.isEnabled()) jButtonPrintKitchen.setText("");
		if(!jButtonLastInvoiceChangePaymentMethod.isEnabled()) jButtonLastInvoiceChangePaymentMethod.setText("");
		if(!jButtonStaffInvoice.isEnabled()) jButtonStaffInvoice.setText("");
		if(!jButtonItemNote.isEnabled()) jButtonItemNote.setText("");
		if(!jButtonTables.isEnabled()) jButtonTables.setText("");
		if(!jButtonCash.isEnabled()) jButtonCash.setText("");
		if(!jButtonCard.isEnabled()) jButtonCard.setText("");
		if(!jButtonOtherPaymentMethods.isEnabled()) jButtonOtherPaymentMethods.setText("");
		if(!jButtonOffer.isEnabled()) jButtonOffer.setText("");
		if(!jButtonLoadOffer.isEnabled()) jButtonLoadOffer.setText("");
		
		ClientAppUtils.SetupFocusTraversal(this);
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE_AT_START.ordinal())){
			ClientAppKeyboardMultilineDialog keyboardDialog = new ClientAppKeyboardMultilineDialog(null, true, "Unos predefinirane napomene na račun", defaultNote, 512);
			keyboardDialog.setVisible(true);
			defaultNote = keyboardDialog.enteredText;
		}
		
		LoadWorktimeSettings();
		
		// Unfiscalized invoice check
		UnfiscalizedInvoiceCheck();
		RefreshUnfisc();
		//getDataFromDB();
		updateDateTimeThread.start();
		lockCashRegisterThread.start();
                //lockTableThread2.start();
		
		if(!isQuickPick){
			jPanel1.setVisible(false);
		}
					
		jTextField1.requestFocusInWindow();
                
                try {
                    Thread.sleep(3000);
                    jButtonTables.doClick();
                }
                catch(Exception ex) {
                    return;
                }
	}
        
	private void UnfiscalizedInvoiceCheck(){
		Invoice invoiceLocal = ClientAppUtils.GetUnfiscalizedInvoice(isProduction, true);
		Invoice invoiceGlobal = ClientAppUtils.GetUnfiscalizedInvoice(isProduction, false);
		if(invoiceLocal != null && invoiceGlobal != null){
			if(invoiceLocal.date.before(invoiceGlobal.date)){
				oldestUnfiscalizedInvoiceDate = invoiceLocal.date;
			} else {
				oldestUnfiscalizedInvoiceDate = invoiceGlobal.date;
			}
		} else if(invoiceLocal != null) {
			oldestUnfiscalizedInvoiceDate = invoiceLocal.date;
		} else if(invoiceGlobal != null) {
			oldestUnfiscalizedInvoiceDate = invoiceGlobal.date;
		} else {
			oldestUnfiscalizedInvoiceDate = new Date();
		}
	}
	
	private boolean HaveOldUnfiscalizedInvoices(){
		RefreshUnfisc();
		
		long diffInMS = new Date().getTime() - oldestUnfiscalizedInvoiceDate.getTime();
		if(diffInMS > 36 * 1000 * 60 * 60){
			UnfiscalizedInvoiceCheck();
			long diffInMSNew = new Date().getTime() - oldestUnfiscalizedInvoiceDate.getTime();
			return diffInMSNew > 36 * 1000 * 60 * 60;
		}
		return false;
	}
	
	private void UpdateCurrentUser(){
		jLabelStaffUserName.setText(StaffUserInfo.GetCurrentUserInfo().fullName); 
                
                jButtonTotal.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_TOTAL]);
                jButtonSaldo.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_SALDO]);
		jButtonOpenCashRegister.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_DISCOUTS]);
		jButtonItemDiscount.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_DISCOUTS]);
		jButtonInvoiceCancelation.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_INVOICE_CANCELATION]);
		jButtonOffer.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_OFFER]);
		jButtonLoadOffer.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_OFFER]);
                
                if (StaffUserInfo.GetCurrentUserInfo().userRightsType == 1){
                    jButtonTotal.setEnabled(true);
                }
                
                if (StaffUserInfo.GetCurrentUserInfo().userRightsType == 1){
                    jButtonSaldo.setEnabled(true);
                }
		
		jButtonTotal.setText(jButtonTotal.isEnabled() ? "<html> <div style=\"text-align: center\"> Total <br>   <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [CTRL + F12]  </div>  </div> </html>" : "");
		jButtonSaldo.setText(jButtonSaldo.isEnabled() ? "<html> <div style=\"text-align: center\"> Saldo <br>  <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [F12]  </div>  </div> </html>" : "");
		jButtonOpenCashRegister.setText(jButtonOpenCashRegister.isEnabled() ? "<html> <div style=\"text-align: center\"> Otvori ladicu <br>   <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [F7]  </div>  </div> </html>" : "");
		jButtonItemDiscount.setText(jButtonItemDiscount.isEnabled() ? "<html> <div style=\"text-align: center\"> Popust <br> na stavku  <br>    <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [CTRL + P]  </div>   </div> </html>" : "");
		jButtonInvoiceCancelation.setText(jButtonInvoiceCancelation.isEnabled() ? "<html> <div style=\"text-align: center\"> Storno <br> računa <br>  <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [CTRL + F9]  </div>  </div> </html>" : "");
		jButtonOffer.setText(jButtonOffer.isEnabled() ? "<html> <div style=\"text-align: center\"> Ponuda <br>   <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [CTRL + I]  </div>   </div> </html>" : "");
		jButtonLoadOffer.setText(jButtonLoadOffer.isEnabled() ? "<html> <div style=\"text-align: center\"> Učitaj  ponudu <br>  <div style=\"color:#777777; margin-top: 2px; font-size: 95%;\"> [CTRL + U]  </div>  </div> </html>" : "");
		
		// Staff work hours
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT DAY, HF, MF, HT, MT "
				+ "FROM STAFF_WORKTIME "
				+ "WHERE STAFF_WORKTIME.IS_DELETED = 0 AND O_NUM = ? AND CR_NUM = ? AND STAFF_ID = ?");
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
		databaseQuery.AddParam(2, Licence.GetCashRegisterNumber());
		databaseQuery.AddParam(3, StaffUserInfo.GetCurrentUserInfo().userId);
		
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
					ArrayList<Integer> dayTemp = new ArrayList<>();
					ArrayList<Integer> hfTemp = new ArrayList<>();
					ArrayList<Integer> mfTemp = new ArrayList<>();
					ArrayList<Integer> htTemp = new ArrayList<>();
					ArrayList<Integer> mtTemp = new ArrayList<>();
					while (databaseQueryResult.next()) {
						dayTemp.add(databaseQueryResult.getInt(0));
						hfTemp.add(databaseQueryResult.getInt(1));
						mfTemp.add(databaseQueryResult.getInt(2));
						htTemp.add(databaseQueryResult.getInt(3));
						mtTemp.add(databaseQueryResult.getInt(4));
					}
					currentUserWorkDayList = dayTemp;
					currentUserWorkHourFromList = hfTemp;
					currentUserWorkMinuteFromList = mfTemp;
					currentUserWorkHourToList = htTemp;
					currentUserWorkMinuteToList = mtTemp;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void OnQuickPickCharacterPressed(char letter){
		letter = Character.toUpperCase(letter);
		for (int i = 0; i < jLabelQuickPickLetters.length; ++i){
			if (jLabelQuickPickLetters[i].equals(Character.toString(letter))){
				if(quickPickId[i] != -1){
					TryAddItemWithID(quickPickId[i]);
					return;
				}
				break;
			}
		}
		
		jTextField1.setText("");
	}
	
	private void OnEnterPressed(){
		String enteredText = jTextField1.getText();
		jTextField1.setText("");
		if (enteredText.length() == 0){
			ShowSelectItemDialog("");
			return;
		}
		
		if ('*' == enteredText.charAt(0)){
			int rowId = GetSelectedItemIndex();
			if(rowId == -1){
				return;
			}

			if(invoice.isSubtotal){
				ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
				return;
			}
			
			try {
				invoice.items.get(rowId).itemAmount = Integer.parseInt(enteredText.substring(1));
				RefreshItemAmount(rowId);
			} catch (Exception e){}
		} else {
			int itemId = -1;
			try {
				itemId = Integer.parseInt(enteredText);
			} catch (Exception e){}
			
			int result = 0;
			if (itemId != -1){
				result = TryAddItemWithID(itemId);
			}
			
			if (result == 0){
				ShowSelectItemDialog(enteredText);
				return;
			}
		}
	}
	
	private void ShowSelectItemDialog(String startFilter){
		ClientAppSelectItemDialog dialog = new ClientAppSelectItemDialog(null, true, startFilter);
		dialog.setVisible(true);
		if(dialog.selectedId != -1){
			TryAddItemWithID(dialog.selectedId);
		}
	}
	
	private int TryAddItemWithID(int itemId) {
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return -1;
		}
		
		int itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE;
		String itemName = "";
		float itemPrice = 0f;
		float itemTaxRate = 0f;
		float itemConsumptionTaxRate = 0f;
		boolean isFood = false;
		float packagingRefund = 0f;
		for (WarehouseItem warehouseItem : warehouseItems){
			if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
				if (warehouseItem.articleWithoutNormatives){
					ClientAppLogger.GetInstance().ShowMessage("Nije moguće odabrati ovaj artikl jer nema normativa!");
					return -1;
				}
				
				itemName = warehouseItem.itemName;
				if(eventPrices){
					itemPrice = warehouseItem.eventPrice;
				} else {
					itemPrice = warehouseItem.itemPrice;
				}
				itemTaxRate = warehouseItem.taxRate;
				itemConsumptionTaxRate = warehouseItem.consumptionTaxRate;
				isFood = warehouseItem.isFood;
				packagingRefund = warehouseItem.packagingRefund;
				break;
			}
		}
		
		if("".equals(itemName)){
			itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE;
			for (WarehouseItem warehouseItem : warehouseItems){
				if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
					/*if (warehouseItem.articleWithoutNormatives){
						ClientAppLogger.GetInstance().ShowMessage("Nije moguće odabrati ovaj artikl jer nema normativa!");
						return;
					}*/

					itemName = warehouseItem.itemName;
					if(eventPrices){
						itemPrice = warehouseItem.eventPrice;
					} else {
						itemPrice = warehouseItem.itemPrice;
					}
					itemTaxRate = warehouseItem.taxRate;
					itemConsumptionTaxRate = warehouseItem.consumptionTaxRate;
					isFood = warehouseItem.isFood;
					packagingRefund = warehouseItem.packagingRefund;
					break;
				}
			}
		}
		
		if("".equals(itemName)){
			itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
			for (WarehouseItem warehouseItem : warehouseItems){
				if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
					/*if (warehouseItem.articleWithoutNormatives){
						ClientAppLogger.GetInstance().ShowMessage("Nije moguće odabrati ovaj artikl jer nema normativa!");
						return;
					}*/

					itemName = warehouseItem.itemName;
					if(eventPrices){
						itemPrice = warehouseItem.eventPrice;
					} else {
						itemPrice = warehouseItem.itemPrice;
					}
					itemTaxRate = warehouseItem.taxRate;
					itemConsumptionTaxRate = warehouseItem.consumptionTaxRate;
					isFood = warehouseItem.isFood;
					packagingRefund = warehouseItem.packagingRefund;
					break;
				}
			}
		}
		
		if("".equals(itemName)){
			return 0;
		}
			
		boolean insertItem = true;
		for (int i = 0; i < invoice.items.size(); ++i){
			if(invoice.items.get(i).itemId == itemId && invoice.items.get(i).itemType == itemType && invoice.items.get(i).itemPrice == itemPrice 
					&& invoice.items.get(i).discountPercentage == 0f && invoice.items.get(i).discountValue == 0f){
				insertItem = false;
				invoice.items.get(i).itemAmount += 1f;
				RefreshItemAmount(i);
			}
		}
		
		if (insertItem){
			InvoiceItem invoiceItem = new InvoiceItem();
			invoiceItem.itemId = itemId;
			invoiceItem.itemType = itemType;
			invoiceItem.itemName = itemName;
			invoiceItem.itemPrice = itemPrice;
			invoiceItem.itemAmount = 1f;
			invoiceItem.taxRate = itemTaxRate;
			invoiceItem.consumptionTaxRate = itemConsumptionTaxRate;
			invoiceItem.packagingRefund = packagingRefund;
			invoiceItem.isFood = isFood;
			invoice.items.add(invoiceItem);

			Object[] rowData = new Object[6];
			rowData[COLUMN_INDEX_ID] = itemId;
			rowData[COLUMN_INDEX_NAME] = itemName;
			rowData[COLUMN_INDEX_AMOUNT] = "1";
			rowData[COLUMN_INDEX_PRICE] = itemPrice;
			rowData[COLUMN_INDEX_DISCOUNT] = "";
			rowData[COLUMN_INDEX_TOTAL] = itemPrice;
			defaultTableModel.addRow(rowData);
			
			UpdateTotalPrice(true);
		}
		
		jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
		
		return 1;
    }
	
	private void UpdateTotalPrice(boolean doSaveTable){
		lastInputTime = System.currentTimeMillis() / 1000;
		
		float totalPrice = 0f;
		for(InvoiceItem invoiceItem : invoice.items){
			float totalItemPrice = invoiceItem.itemAmount * invoiceItem.itemPrice;
			if(invoiceItem.discountPercentage != 0f){
				totalItemPrice = totalItemPrice * (100f - invoiceItem.discountPercentage) / 100f;
			} else if(invoiceItem.discountValue != 0f){
				totalItemPrice = totalItemPrice - invoiceItem.discountValue * invoiceItem.itemAmount;
			}
			totalPrice += totalItemPrice;
		}
		
		invoice.totalPrice = totalPrice;
		
		String discount = "";
		if(invoice.discountPercentage != 0f){
			discount = "Popust na račun: " + invoice.discountPercentage + "% = " + (totalPrice * invoice.discountPercentage) / 100f + " kn";
			totalPrice = totalPrice * (100f - invoice.discountPercentage) / 100f;
		} else if(invoice.discountValue != 0f){
			discount = "Popust na račun: " + ClientAppUtils.FloatToPriceString(invoice.discountValue) + " kn";
			totalPrice = totalPrice - invoice.discountValue;
		}
		
		jLabelInvoiceDiscount.setText(discount);
		jLabelTotalPrice.setText(ClientAppUtils.FloatToPriceString(totalPrice).replace(".", ","));
		
		if(doSaveTable){
			SaveTable();
		}
	}
	
	private void OnTableDoubleClick(int rowId){
		ClientAppEnterAmountDialog enterAmountDialog = new ClientAppEnterAmountDialog(null, true);
        enterAmountDialog.setVisible(true);
		if(enterAmountDialog.changeSuccess){
			boolean skipCheck = invoice.items.get(rowId).itemAmount <= 1f && enterAmountDialog.enteredAmount >= 0.1f;
			if(invoice.items.get(rowId).itemAmount > enterAmountDialog.enteredAmount && !skipCheck){
				if(!StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_DELETE_ITEM]){
					ClientAppOwnerAuthDialog dialog = new ClientAppOwnerAuthDialog(null, true);
					dialog.setVisible(true);
					if (!dialog.authSuccess){
						ClientAppLogger.GetInstance().ShowMessage("Potvrda Vlasnika ili Poslovođe nije uspjela.");
						return;
					}
				}
			}
			
			invoice.items.get(rowId).itemAmount = enterAmountDialog.enteredAmount;
			RefreshItemAmount(rowId);
		}
	}
	
	private void OnPlusClick(){
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		invoice.items.get(rowId).itemAmount += 1f;
		
		RefreshItemAmount(rowId);
	}
	
	private void OnMinusClick(){
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		if(!StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_DELETE_ITEM]){
			ClientAppOwnerAuthDialog dialog = new ClientAppOwnerAuthDialog(null, true);
			dialog.setVisible(true);
			if (!dialog.authSuccess){
				ClientAppLogger.GetInstance().ShowMessage("Potvrda Vlasnika ili Poslovođe nije uspjela.");
				return;
			}
		}
		
		invoice.items.get(rowId).itemAmount -= 1f;
		
		RefreshItemAmount(rowId);
	}
	
	private void RefreshItemAmount(int rowId){
		defaultTableModel.setValueAt(invoice.items.get(rowId).itemAmount, rowId, COLUMN_INDEX_AMOUNT);
		float totalItemPrice = invoice.items.get(rowId).itemAmount * invoice.items.get(rowId).itemPrice;
		if(invoice.items.get(rowId).discountPercentage != 0f){
			totalItemPrice = totalItemPrice * (100f - invoice.items.get(rowId).discountPercentage) / 100f;
		} else if(invoice.items.get(rowId).discountValue != 0f){
			totalItemPrice = totalItemPrice - invoice.items.get(rowId).discountValue * invoice.items.get(rowId).itemAmount;
		}
		defaultTableModel.setValueAt(totalItemPrice, rowId, COLUMN_INDEX_TOTAL);
		
		UpdateTotalPrice(true);
	}
	
	/*private void CloseCashRegister(){
		Utils.DisposeDialog(this);
	}*/
	
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
        jPanel9 = new javax.swing.JPanel();
        jLabelInternetConnection = new javax.swing.JLabel();
        jLabelCompanyData1 = new javax.swing.JLabel();
        jLabelCompanyData2 = new javax.swing.JLabel();
        jLabelCompanyData3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabelDate = new javax.swing.JLabel();
        jLabelTime = new javax.swing.JLabel();
        jLabelCashRegisterName = new javax.swing.JLabel();
        jLabelStaffUserName = new javax.swing.JLabel();
        jLabelNefisk1 = new javax.swing.JLabel();
        jLabelNefisk2 = new javax.swing.JLabel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel17 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButtonDelete = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonItemNote = new javax.swing.JButton();
        jButtonItemDiscount = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jButtonPrintKitchen = new javax.swing.JButton();
        jButtonPrintBar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jButtonLastInvoiceChangePaymentMethod = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabelLastInvoicePrice = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jButtonInvoiceCancelation = new javax.swing.JButton();
        jButtonInvoiceCopyPrint = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabelTotalPrice = new javax.swing.JLabel();
        jLabelR1 = new javax.swing.JLabel();
        jLabelInvoiceDiscount = new javax.swing.JLabel();
        jLabelSubtotal = new javax.swing.JLabel();
        jLabelEventPrices = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jButtonCash = new javax.swing.JButton();
        jButtonCard = new javax.swing.JButton();
        jButtonOtherPaymentMethods = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jButtonStaffInvoice = new javax.swing.JButton();
        jButtonOffer = new javax.swing.JButton();
        jButtonTotal = new javax.swing.JButton();
        jButtonTables = new javax.swing.JButton();
        jButtonSubtotal = new javax.swing.JButton();
        jButtonInvoiceR1 = new javax.swing.JButton();
        jButtonStaffUserChange = new javax.swing.JButton();
        jButtonLoadOffer = new javax.swing.JButton();
        jButtonSaldo = new javax.swing.JButton();
        jButtonEventPrices = new javax.swing.JButton();
        jButtonOpenCashRegister = new javax.swing.JButton();
        jButtonInvoiceDiscount = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButtonQuickPick = new javax.swing.JButton();
        jLabelQuickPick0 = new javax.swing.JLabel();
        jLabelQuickPick1 = new javax.swing.JLabel();
        jLabelQuickPick2 = new javax.swing.JLabel();
        jLabelQuickPick3 = new javax.swing.JLabel();
        jLabelQuickPick4 = new javax.swing.JLabel();
        jLabelQuickPick5 = new javax.swing.JLabel();
        jLabelQuickPick6 = new javax.swing.JLabel();
        jLabelQuickPick7 = new javax.swing.JLabel();
        jLabelQuickPick8 = new javax.swing.JLabel();
        jLabelQuickPick9 = new javax.swing.JLabel();
        jLabelQuickPick10 = new javax.swing.JLabel();
        jLabelQuickPick11 = new javax.swing.JLabel();
        jLabelQuickPick12 = new javax.swing.JLabel();
        jLabelQuickPick13 = new javax.swing.JLabel();
        jLabelQuickPick14 = new javax.swing.JLabel();
        jLabelQuickPick15 = new javax.swing.JLabel();
        jLabelQuickPick16 = new javax.swing.JLabel();
        jLabelQuickPick17 = new javax.swing.JLabel();
        jLabelQuickPick18 = new javax.swing.JLabel();
        jLabelQuickPick19 = new javax.swing.JLabel();
        jLabelQuickPick20 = new javax.swing.JLabel();
        jLabelQuickPick21 = new javax.swing.JLabel();
        jLabelQuickPick22 = new javax.swing.JLabel();
        jLabelQuickPick23 = new javax.swing.JLabel();
        jLabelQuickPick24 = new javax.swing.JLabel();
        jLabelQuickPick25 = new javax.swing.JLabel();
        jLabelQuickPick26 = new javax.swing.JLabel();
        jLabelQuickPick27 = new javax.swing.JLabel();
        jLabelQuickPick28 = new javax.swing.JLabel();
        jLabelQuickPick29 = new javax.swing.JLabel();
        jLabelQuickPick30 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Kasa");
        setMinimumSize(getPreferredSize());

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
            .addGroup(jPanelAdinfoLogoLayout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelAdinfoLogoLayout.setVerticalGroup(
            jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jLabelCompanyData1.setText("Ime firme d.o.o.");

        jLabelCompanyData2.setText("Poslovnica broj n: Ime ulice 123");
        jLabelCompanyData2.setPreferredSize(new java.awt.Dimension(250, 14));

        jLabelCompanyData3.setText("OIB: 00000000001");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabelCompanyData1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelCompanyData2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabelCompanyData3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCompanyData1)
                    .addComponent(jLabelCompanyData2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompanyData3))
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabelDate.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabelDate.setText("01.01.2019.");
        jLabelDate.setPreferredSize(new java.awt.Dimension(85, 14));

        jLabelTime.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabelTime.setText("08:00:00");
        jLabelTime.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabelCashRegisterName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabelCashRegisterName.setText("Kasa 1 - Stol 23");
        jLabelCashRegisterName.setPreferredSize(new java.awt.Dimension(85, 14));

        jLabelStaffUserName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabelStaffUserName.setText("Ime P.");
        jLabelStaffUserName.setPreferredSize(new java.awt.Dimension(150, 14));

        jLabelNefisk1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelNefisk1.setForeground(new java.awt.Color(255, 0, 0));
        jLabelNefisk1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelNefisk1.setText("POSTOJE NEFISK.");
        jLabelNefisk1.setPreferredSize(new java.awt.Dimension(80, 14));

        jLabelNefisk2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelNefisk2.setForeground(new java.awt.Color(255, 0, 0));
        jLabelNefisk2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelNefisk2.setText("RAČUNI ");
        jLabelNefisk2.setPreferredSize(new java.awt.Dimension(80, 14));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelTime, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelNefisk1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelCashRegisterName, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelStaffUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelNefisk2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNefisk1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCashRegisterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelStaffUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNefisk2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerSize(0);
        jSplitPane2.setResizeWeight(0.9);
        jSplitPane2.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPane2.setPreferredSize(new java.awt.Dimension(996, 580));

        jPanel17.setMinimumSize(new java.awt.Dimension(612, 579));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke"));

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[DEL] \n</div>\n\n</div> </html>");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Uredi <br>\n\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F10] \n</div>\n\n\n </div> </html>");
        jButtonEdit.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[INS] \n</div>\n\n </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonItemNote.setText("<html> <div style=\"text-align: center\"> Napomena <br> na stavku  <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + S] \n</div> \n\n</div> </html>");
        jButtonItemNote.setToolTipText("");
        jButtonItemNote.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonItemNote.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonItemNote.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonItemNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonItemNoteActionPerformed(evt);
            }
        });

        jButtonItemDiscount.setText("<html> <div style=\"text-align: center\"> Popust <br> na stavku  <br> \n\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + P] \n</div>\n\n\n</div> </html>");
        jButtonItemDiscount.setToolTipText("");
        jButtonItemDiscount.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonItemDiscount.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonItemDiscount.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonItemDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonItemDiscountActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonItemNote, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonItemDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonItemNote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonItemDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Unos"));

        jLabel8.setText(" Šifra [F11]:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 27, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Ispis"));

        jButtonPrintKitchen.setText("<html> <div style=\"text-align: center\"> Ispis kuhinja <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F11] \n</div>\n\n </div> </html>");
        jButtonPrintKitchen.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintKitchen.setMaximumSize(new java.awt.Dimension(100, 80));
        jButtonPrintKitchen.setPreferredSize(new java.awt.Dimension(60, 55));
        jButtonPrintKitchen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintKitchenActionPerformed(evt);
            }
        });

        jButtonPrintBar.setText("<html> <div style=\"text-align: center\"> Ispis šank <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F10] \n</div>\n\n </div> </html>");
        jButtonPrintBar.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintBar.setMaximumSize(new java.awt.Dimension(100, 80));
        jButtonPrintBar.setPreferredSize(new java.awt.Dimension(60, 55));
        jButtonPrintBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintBarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonPrintKitchen, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintBar, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonPrintBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPrintKitchen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Izlaz"));

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Izlaz <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[ESC] \n</div>\n\n</div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Šifra", "Naziv stavke", "Količina", "Cijena", "Popust", "Ukupno"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setRowHeight(25);
        jScrollPane1.setViewportView(jTable1);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Zadnji račun"));

        jButtonLastInvoiceChangePaymentMethod.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonLastInvoiceChangePaymentMethod.setText("<html> <div style=\"text-align: center\"> Promjena načina  <br> plaćanja <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F6] \n</div>\n\n </div> </html>");
        jButtonLastInvoiceChangePaymentMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonLastInvoiceChangePaymentMethod.setPreferredSize(new java.awt.Dimension(60, 53));
        jButtonLastInvoiceChangePaymentMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLastInvoiceChangePaymentMethodActionPerformed(evt);
            }
        });

        jLabel1.setText("Iznos:");

        jLabelLastInvoicePrice.setText("0,00");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabelLastInvoicePrice, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jButtonLastInvoiceChangePaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(jLabelLastInvoicePrice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(jButtonLastInvoiceChangePaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Računi"));

        jButtonInvoiceCancelation.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonInvoiceCancelation.setText("<html> <div style=\"text-align: center\"> Storno <br> računa <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F9] \n</div>\n\n</div> </html>");
        jButtonInvoiceCancelation.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonInvoiceCancelation.setPreferredSize(new java.awt.Dimension(60, 53));
        jButtonInvoiceCancelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceCancelationActionPerformed(evt);
            }
        });

        jButtonInvoiceCopyPrint.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonInvoiceCopyPrint.setText("<html> <div style=\"text-align: center\"> Ispis kopije <br> računa <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F6] \n</div> \n\n</div> </html>");
        jButtonInvoiceCopyPrint.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonInvoiceCopyPrint.setPreferredSize(new java.awt.Dimension(60, 53));
        jButtonInvoiceCopyPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceCopyPrintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonInvoiceCancelation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonInvoiceCopyPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonInvoiceCancelation, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                    .addComponent(jButtonInvoiceCopyPrint, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );

        jPanel10.setBackground(new java.awt.Color(0, 0, 0));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelTotalPrice.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        jLabelTotalPrice.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTotalPrice.setText("0,00");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTotalPrice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelR1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelR1.setForeground(new java.awt.Color(0, 51, 204));
        jLabelR1.setText("R1: Ime firme d.o.o.");

        jLabelInvoiceDiscount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelInvoiceDiscount.setForeground(new java.awt.Color(0, 51, 204));
        jLabelInvoiceDiscount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInvoiceDiscount.setText("Popust na račun: 20 %");

        jLabelSubtotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSubtotal.setText("STANJE STOLA");

        jLabelEventPrices.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelEventPrices.setText("EVENT CIJENE");

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonCash.setBackground(new java.awt.Color(102, 255, 102));
        jButtonCash.setText("<html> <div style=\"text-align: center\"> GOTOVINA <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F5]\n </div>\n\n </div> </html>");
        jButtonCash.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonCash.setPreferredSize(new java.awt.Dimension(100, 110));
        jButtonCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCashActionPerformed(evt);
            }
        });

        jButtonCard.setBackground(new java.awt.Color(102, 255, 102));
        jButtonCard.setText("<html> <div style=\"text-align: center\"> KARTICA <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + K] \n</div>\n\n </div> </html>");
        jButtonCard.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonCard.setPreferredSize(new java.awt.Dimension(120, 53));
        jButtonCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCardActionPerformed(evt);
            }
        });

        jButtonOtherPaymentMethods.setBackground(new java.awt.Color(102, 255, 102));
        jButtonOtherPaymentMethods.setText("<html> <div style=\"text-align: center\"> Drugi načini <br> plaćanja <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + D] \n</div>\n\n</div> </html>");
        jButtonOtherPaymentMethods.setToolTipText("");
        jButtonOtherPaymentMethods.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonOtherPaymentMethods.setPreferredSize(new java.awt.Dimension(120, 53));
        jButtonOtherPaymentMethods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOtherPaymentMethodsActionPerformed(evt);
            }
        });

        jPanel15.setLayout(new java.awt.GridLayout(2, 0, 5, 5));

        jButtonStaffInvoice.setText("<html> <div style=\"text-align: center\"> Račun djelatnika <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F7] \n</div>\n\n</div> </html>");
        jButtonStaffInvoice.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonStaffInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffInvoiceActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonStaffInvoice);

        jButtonOffer.setText("<html> <div style=\"text-align: center\"> Ponuda <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + I] \n</div>\n\n </div> </html>");
        jButtonOffer.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonOffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOfferActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonOffer);

        jButtonTotal.setText("<html> <div style=\"text-align: center\"> Total <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F12] \n</div>\n\n</div> </html>");
        jButtonTotal.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTotalActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonTotal);

        jButtonTables.setBackground(new java.awt.Color(153, 153, 0));
        jButtonTables.setText("<html> <div style=\"text-align: center\"> Stolovi <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F9] \n</div>\n\n</div> </html>");
        jButtonTables.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTablesActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonTables);

        jButtonSubtotal.setText("<html> <div style=\"text-align: center\"> Stanje stola <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F5] \n</div>\n\n </div> </html>");
        jButtonSubtotal.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSubtotalActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonSubtotal);

        jButtonInvoiceR1.setBackground(new java.awt.Color(0, 102, 204));
        jButtonInvoiceR1.setText("<html> <div style=\"text-align: center\"> R1 <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + R] \n</div>\n\n </div> </html>");
        jButtonInvoiceR1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonInvoiceR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceR1ActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonInvoiceR1);

        jButtonStaffUserChange.setText("<html> <div style=\"text-align: center\"> Promjena djelatnika <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F8] \n</div>\n\n </div> </html>");
        jButtonStaffUserChange.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonStaffUserChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffUserChangeActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonStaffUserChange);

        jButtonLoadOffer.setText("<html> <div style=\"text-align: center\"> Učitaj  ponudu <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + U] \n</div>\n </div> </html>");
        jButtonLoadOffer.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonLoadOffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadOfferActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonLoadOffer);

        jButtonSaldo.setText("<html> <div style=\"text-align: center\"> Saldo <br>\n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F12] \n</div>\n\n</div> </html>");
        jButtonSaldo.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonSaldo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaldoActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonSaldo);

        jButtonEventPrices.setText("<html> <div style=\"text-align: center\"> Event <br> cijene <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + N] \n</div>\n\n </div> </html>");
        jButtonEventPrices.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEventPrices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEventPricesActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonEventPrices);

        jButtonOpenCashRegister.setText("<html> <div style=\"text-align: center\"> Otvori ladicu <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[F7] \n</div>\n\n</div> </html>");
        jButtonOpenCashRegister.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonOpenCashRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenCashRegisterActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonOpenCashRegister);

        jButtonInvoiceDiscount.setBackground(new java.awt.Color(0, 102, 204));
        jButtonInvoiceDiscount.setText("<html> <div style=\"text-align: center\"> Popust <br> na račun <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + O] \n</div> \n\n</div> </html>");
        jButtonInvoiceDiscount.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonInvoiceDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceDiscountActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonInvoiceDiscount);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonOtherPaymentMethods, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCash, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCash, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButtonCard, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOtherPaymentMethods, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabelR1, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23)
                        .addComponent(jLabelSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelEventPrices, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInvoiceDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelR1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelInvoiceDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSubtotal)
                    .addComponent(jLabelEventPrices))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane2.setLeftComponent(jPanel17);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Brzo biranje"));

        jButtonQuickPick.setText("<html> <div style=\"text-align: center\"> Podesi <br> \n\n<div style=\"color:#777777; margin-top: 2px; font-size: 95%;\">\n[CTRL + F8] \n</div>\n\n</div> </html>");
        jButtonQuickPick.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonQuickPick.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonQuickPick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQuickPickActionPerformed(evt);
            }
        });

        jLabelQuickPick0.setText("A = ");

        jLabelQuickPick1.setText("A = ");

        jLabelQuickPick2.setText("A = ");

        jLabelQuickPick3.setText("A = ");

        jLabelQuickPick4.setText("A = ");

        jLabelQuickPick5.setText("A = ");

        jLabelQuickPick6.setText("A = ");

        jLabelQuickPick7.setText("A = ");

        jLabelQuickPick8.setText("A = ");

        jLabelQuickPick9.setText("A = ");

        jLabelQuickPick10.setText("A = ");

        jLabelQuickPick11.setText("A = ");

        jLabelQuickPick12.setText("A = ");

        jLabelQuickPick13.setText("A = ");

        jLabelQuickPick14.setText("A = ");

        jLabelQuickPick15.setText("A = ");

        jLabelQuickPick16.setText("A = ");

        jLabelQuickPick17.setText("A = ");

        jLabelQuickPick18.setText("A = ");

        jLabelQuickPick19.setText("A = ");

        jLabelQuickPick20.setText("A = ");

        jLabelQuickPick21.setText("A = ");

        jLabelQuickPick22.setText("A = ");

        jLabelQuickPick23.setText("A = ");

        jLabelQuickPick24.setText("A = ");

        jLabelQuickPick25.setText("A = ");

        jLabelQuickPick26.setText("A = ");

        jLabelQuickPick27.setText("A = ");

        jLabelQuickPick28.setText("A = ");

        jLabelQuickPick29.setText("A = ");

        jLabelQuickPick30.setText("A = ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelQuickPick0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonQuickPick, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQuickPick30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButtonQuickPick, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelQuickPick0)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick1)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick2)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick3)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick4)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick5)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick6)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick7)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick8)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick9)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick10)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick11)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick12)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick13)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick14)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick15)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick16)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick17)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick18)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick19)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick20)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick21)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick22)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick23)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick24)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick25)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick26)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick27)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick28)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick29)
                .addGap(2, 2, 2)
                .addComponent(jLabelQuickPick30)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		if(CheckIfAnyTableExist()){
			int dialogResult = JOptionPane.showConfirmDialog (null, "Postoje neprazni stolovi ove kase!" + System.lineSeparator()
					+ "Jeste li sigurni da želite izaći?", "Izađi iz kase", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				ClearTableIfEmpty();
				Utils.DisposeDialog(this);
			}
		} else if(!invoice.items.isEmpty()){
			int dialogResult = JOptionPane.showConfirmDialog (null, "Izlazite iz kase sa stavkama na računu!" + System.lineSeparator()
					+ "Jeste li sigurni da želite izaći?", "Izađi iz kase", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				ClearTableIfEmpty();
				Utils.DisposeDialog(this);
			}
		} else {
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		if(invoice.isSubtotal){
			int dialogResult = JOptionPane.showConfirmDialog (null, "Obriši sve stavke?", "Obriši sve stavke", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				ResetAllFields(true);
			}
			return;
		}
		
		if(!StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_DELETE_ITEM]){
			ClientAppOwnerAuthDialog dialog = new ClientAppOwnerAuthDialog(null, true);
			dialog.setVisible(true);
			if (!dialog.authSuccess){
				ClientAppLogger.GetInstance().ShowMessage("Potvrda Vlasnika ili Poslovođe nije uspjela.");
				return;
			}
		}
		
		boolean noteExist = false;
		if(rowId + 1 < invoice.items.size()){
			if(!"".equals(invoice.items.get(rowId + 1).itemNote)){
				noteExist = true;
			}
		}
		
		boolean noteSelected = jTable1.getSelectedRow() != rowId;
		
		if(noteExist){
			if(noteSelected){
				invoice.items.remove(rowId + 1);
				defaultTableModel.removeRow(rowId + 1);
			} else {
				invoice.items.remove(rowId);
				defaultTableModel.removeRow(rowId);
				invoice.items.remove(rowId);
				defaultTableModel.removeRow(rowId);
			}
		} else {
			invoice.items.remove(rowId);
			defaultTableModel.removeRow(rowId);
		}
		
		if(jTable1.getRowCount() > 0){
			jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
		}
		
		UpdateTotalPrice(true);
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		ShowSelectItemDialog("");
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
		int rowId = GetSelectedItemIndex();
		if (rowId != -1) {
			OnTableDoubleClick(rowId);
		}
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonInvoiceCopyPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceCopyPrintActionPerformed
        ClientAppCashRegisterPrintInvoiceCopyDialog dialog = new ClientAppCashRegisterPrintInvoiceCopyDialog(null, true);
		dialog.setVisible(true);
		if(dialog.selectedOption == -1){
			return;
		}
		
		if(dialog.selectedOption == 0){
			if(lastInvoice != null){
				PrintUtils.PrintPosInvoice(lastInvoice, Values.POS_PRINTER_TYPE_INVOICE);
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Od ulaska u kasu nema izdanih računa.");
			}
		} else if (dialog.selectedOption == 1){
			ClientAppSelectInvoiceDialog dialogInvoice = new ClientAppSelectInvoiceDialog(null, true, "Ispis kopije računa");
			dialogInvoice.setVisible(true);
			if(dialogInvoice.selectedInvoice != null){
				PrintUtils.PrintPosInvoice(dialogInvoice.selectedInvoice, Values.POS_PRINTER_TYPE_INVOICE);
			}
		} else if (dialog.selectedOption == 2){
			if(invoice.isSubtotal){
				ClientAppLogger.GetInstance().ShowMessage("Nije moguće učitati novi račun nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
				return;
			}

			ClientAppLoadOfferDialog dialogSubtotal = new ClientAppLoadOfferDialog(null, true, Values.PAYMENT_METHOD_TYPE_SUBTOTAL);
			dialogSubtotal.setVisible(true);
			if(dialogSubtotal.selectedInvoice == null){
				return;
			}

			//dialogSubtotal.selectedInvoice.note += System.lineSeparator() + "Račun kreiran prema predračunu broj " + dialogSubtotal.selectedInvoice.specialNumber + "/" + dialogSubtotal.selectedInvoice.officeTag + "/" + dialogSubtotal.selectedInvoice.cashRegisterNumber;
			SetCurrentInvoice(dialogSubtotal.selectedInvoice);
		}
    }//GEN-LAST:event_jButtonInvoiceCopyPrintActionPerformed

    private void jButtonLastInvoiceChangePaymentMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLastInvoiceChangePaymentMethodActionPerformed
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
			return;
		}
		
		if(HaveOldUnfiscalizedInvoices()){
			ClientAppLogger.GetInstance().ShowMessage("Upozorenje: u poslovnici postoje nefiskalizirani računi stariji od 48 sati. Izdavanje novih računa je onemogućeno!");
			return;
		}
		
		if(lastInvoice == null){
			ClientAppLogger.GetInstance().ShowMessage("Od ulaska u kasu nema izdanih računa");
			return;
		}
		
		ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{}, false, 0f);
		dialog.setVisible(true);
		if(dialog.selectedPaymentMethodType == -1){
			return;
		}
		
		  if (lastInvoice != null && dialog.selectedInvoice == null){
                    Invoice minusInvoice = new Invoice(lastInvoice);
                    UpdateCurrentInvoiceData(minusInvoice);
                    minusInvoice.isCopy = false;
                    minusInvoice.note = "Storno računa " + lastInvoice.invoiceNumber + "/" + lastInvoice.officeTag + "/" + lastInvoice.cashRegisterNumber;
                    for(int i = 0; i < minusInvoice.items.size(); ++i){
                            minusInvoice.items.get(i).itemAmount *= -1f;
                    }
                    minusInvoice.totalPrice *= -1f;
                    minusInvoice.paymentAmount2 *= -1f;
                    minusInvoice.zki = Fiscalization.CalculateZKI(minusInvoice);
                    if(Values.DEFAULT_ZKI.equals(minusInvoice.zki)){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
                                            + System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
                            return;
                    }

                    Invoice newInvoice = new Invoice(lastInvoice);
                    UpdateCurrentInvoiceData(newInvoice);
                    newInvoice.paymentMethodName = dialog.selectedPaymentMethodName;
                    newInvoice.paymentMethodType = dialog.selectedPaymentMethodType;
                    newInvoice.paymentMethodName2 = dialog.selectedPaymentMethodName2;
                    newInvoice.paymentMethodType2 = dialog.selectedPaymentMethodType2;
                    if (newInvoice.discountValue != 0f){
                            newInvoice.paymentAmount2 = dialog.paymentAmount2 + newInvoice.discountValue;
                    } else if (newInvoice.discountPercentage != 0f){
                            newInvoice.paymentAmount2 = dialog.paymentAmount2 * 100f / (100f - newInvoice.discountPercentage);
                    } else {
                            newInvoice.paymentAmount2 = dialog.paymentAmount2;
                    }
                    newInvoice.isCopy = false;
                    newInvoice.zki = Fiscalization.CalculateZKI(newInvoice);
                    if(Values.DEFAULT_ZKI.equals(newInvoice.zki)){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
                                            + System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
                            return;
                    }

                    boolean invoiceInsertSuccessMinus = ClientAppUtils.InsertLocalInvoice(minusInvoice);
                    if(!invoiceInsertSuccessMinus){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Storno računa nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
                            return;
                    }

                    Fiscalization.FiscalizeInvoiceSynchronized(minusInvoice, true);

                    PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    minusInvoice.isCopy = true;
                    int invoiceCopiesMinus = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
                    for(int i = 1; i < invoiceCopiesMinus; ++i){
                            PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    }

                    boolean invoiceInsertSuccessNew = ClientAppUtils.InsertLocalInvoice(newInvoice);
                    if(!invoiceInsertSuccessNew){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Novi račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
                            return;
                    }

                    Fiscalization.FiscalizeInvoiceSynchronized(newInvoice, true);

                    PrintUtils.PrintPosInvoice(newInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    newInvoice.isCopy = true;
                    int invoiceCopiesNew = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
                    for(int i = 1; i < invoiceCopiesNew; ++i){
                            PrintUtils.PrintPosInvoice(newInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    }

                    // Update last invoice
                    lastInvoice = newInvoice;
                    jLabelLastInvoicePrice.setText(ClientAppUtils.FloatToPriceString(lastInvoice.totalPrice * (100f - lastInvoice.discountPercentage) / 100f - lastInvoice.discountValue));
                }
                else {
                    Invoice minusInvoice = new Invoice(dialog.selectedInvoice);
                    UpdateCurrentInvoiceData(minusInvoice);
                    minusInvoice.isCopy = false;
                    minusInvoice.note = "Storno računa " + dialog.selectedInvoice.invoiceNumber + "/" + dialog.selectedInvoice.officeTag + "/" + dialog.selectedInvoice.cashRegisterNumber;
                    for(int i = 0; i < minusInvoice.items.size(); ++i){
                            minusInvoice.items.get(i).itemAmount *= -1f;
                    }
                    minusInvoice.totalPrice *= -1f;
                    minusInvoice.paymentAmount2 *= -1f;
                    minusInvoice.zki = Fiscalization.CalculateZKI(minusInvoice);
                    if(Values.DEFAULT_ZKI.equals(minusInvoice.zki)){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
                                            + System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
                            return;
                    }

                    Invoice newInvoice = new Invoice(dialog.selectedInvoice);
                    UpdateCurrentInvoiceData(newInvoice);
                    newInvoice.paymentMethodName = dialog.selectedPaymentMethodName;
                    newInvoice.paymentMethodType = dialog.selectedPaymentMethodType;
                    newInvoice.paymentMethodName2 = dialog.selectedPaymentMethodName2;
                    newInvoice.paymentMethodType2 = dialog.selectedPaymentMethodType2;
                    if (newInvoice.discountValue != 0f){
                            newInvoice.paymentAmount2 = dialog.paymentAmount2 + newInvoice.discountValue;
                    } else if (newInvoice.discountPercentage != 0f){
                            newInvoice.paymentAmount2 = dialog.paymentAmount2 * 100f / (100f - newInvoice.discountPercentage);
                    } else {
                            newInvoice.paymentAmount2 = dialog.paymentAmount2;
                    }
                    newInvoice.isCopy = false;
                    newInvoice.zki = Fiscalization.CalculateZKI(newInvoice);
                    if(Values.DEFAULT_ZKI.equals(newInvoice.zki)){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
                                            + System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
                            return;
                    }

                    boolean invoiceInsertSuccessMinus = ClientAppUtils.InsertLocalInvoice(minusInvoice);
                    if(!invoiceInsertSuccessMinus){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Storno računa nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
                            return;
                    }

                    Fiscalization.FiscalizeInvoiceSynchronized(minusInvoice, true);

                    PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    minusInvoice.isCopy = true;
                    int invoiceCopiesMinus = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
                    for(int i = 1; i < invoiceCopiesMinus; ++i){
                            PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    }

                    boolean invoiceInsertSuccessNew = ClientAppUtils.InsertLocalInvoice(newInvoice);
                    if(!invoiceInsertSuccessNew){
                            ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Novi račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
                            return;
                    }

                    Fiscalization.FiscalizeInvoiceSynchronized(newInvoice, true);

                    PrintUtils.PrintPosInvoice(newInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    newInvoice.isCopy = true;
                    int invoiceCopiesNew = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
                    for(int i = 1; i < invoiceCopiesNew; ++i){
                            PrintUtils.PrintPosInvoice(newInvoice, Values.POS_PRINTER_TYPE_INVOICE);
                    }

                    // Update last invoice
                    lastInvoice = newInvoice;
                    jLabelLastInvoicePrice.setText(ClientAppUtils.FloatToPriceString(lastInvoice.totalPrice * (100f - lastInvoice.discountPercentage) / 100f - lastInvoice.discountValue));
                }
    }//GEN-LAST:event_jButtonLastInvoiceChangePaymentMethodActionPerformed

    private void jButtonInvoiceCancelationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceCancelationActionPerformed
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
			return;
		}
		
		if(HaveOldUnfiscalizedInvoices()){
			ClientAppLogger.GetInstance().ShowMessage("Upozorenje: u poslovnici postoje nefiskalizirani računi stariji od 48 sati. Izdavanje novih računa je onemogućeno!");
			return;
		}
		
		ClientAppSelectInvoiceDialog dialog = new ClientAppSelectInvoiceDialog(null, true, "Storno računa");
		dialog.setVisible(true);
		if(dialog.selectedInvoice == null){
			return;
		}
		
		Invoice minusInvoice = new Invoice(dialog.selectedInvoice);
		UpdateCurrentInvoiceData(minusInvoice);
		minusInvoice.isCopy = false;
		minusInvoice.note = "Storno računa " + dialog.selectedInvoice.invoiceNumber + "/" + dialog.selectedInvoice.officeTag + "/" + dialog.selectedInvoice.cashRegisterNumber;
		for(int i = 0; i < minusInvoice.items.size(); ++i){
			minusInvoice.items.get(i).itemAmount *= -1f;
		}
		minusInvoice.totalPrice *= -1f;
		minusInvoice.paymentAmount2 *= 1f;
		minusInvoice.zki = Fiscalization.CalculateZKI(minusInvoice);
		if(Values.DEFAULT_ZKI.equals(minusInvoice.zki)){
			ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
					+ System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
			return;
		}
		
		boolean invoiceInsertSuccessMinus = ClientAppUtils.InsertLocalInvoice(minusInvoice);
		if(!invoiceInsertSuccessMinus){
			ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Storno računa nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
			return;
		}
		
		Fiscalization.FiscalizeInvoiceSynchronized(minusInvoice, true);
		
		PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
		minusInvoice.isCopy = true;
		int invoiceCopiesMinus = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
		for(int i = 1; i < invoiceCopiesMinus; ++i){
			PrintUtils.PrintPosInvoice(minusInvoice, Values.POS_PRINTER_TYPE_INVOICE);
		}
    }//GEN-LAST:event_jButtonInvoiceCancelationActionPerformed

    private void jButtonInvoiceR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceR1ActionPerformed
		ClientAppSelectClientDialog selectDialog = new ClientAppSelectClientDialog(null, true);
                selectDialog.setVisible(true);
                final JDialog loadingDialog = new LoadingDialog(null, true);
                
                String query = "SELECT CLIENTS.HOUSE_NUM, CLIENTS.STREET, CLIENTS.TOWN, CLIENTS.PAYMENT_DELAY, CLIENTS.DISCOUNT, CLIENTS.LOYALTY_CARD "
				+ "FROM CLIENTS "
				+ "WHERE CLIENTS.OIB = ?";                
       
                ClientAppLogger.GetInstance().LogMessage("Query is: " + query);
                ClientAppLogger.GetInstance().LogMessage("Param is: " + selectDialog.selectedClientOIB);
                                
               DatabaseQuery databaseQuery = new DatabaseQuery(query);
               databaseQuery.AddParam(1, selectDialog.selectedClientOIB);
		
		 Object[] rowData = new Object[3];

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
					while (databaseQueryResult.next()) {
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(2);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
               
                
                ClientAppLogger.GetInstance().LogMessage("Street from standard is: " + rowData[0]);
                ClientAppLogger.GetInstance().LogMessage("House Num from standard  is: " + rowData[1]);
                ClientAppLogger.GetInstance().LogMessage("Town  from standard  is: " + rowData[2]);
                                
		invoice.clientId = selectDialog.selectedId;
		invoice.clientName = selectDialog.selectedClientName;
		invoice.clientOIB = selectDialog.selectedClientOIB;
		invoice.paymentDelay = selectDialog.selectedClientPaymentDelay;
                invoice.note = rowData[1] + " " + rowData[0] + ", " + rowData[2];
                                
		if(invoice.clientId == -1){
			jLabelR1.setText("");
		} else {
			jLabelR1.setText("R1: " + selectDialog.selectedClientName + ", OIB: " + selectDialog.selectedClientOIB + "Adresa: " + street + " " + houseNum);
		}
		
		boolean loyalty_card = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_TRUSTCARD.ordinal());
                String loyalty_no = selectDialog.strLoyalty;
		if(loyalty_card && !"".equals(loyalty_no)){
			invoice.discountPercentage = selectDialog.selectedClientDiscount;
            invoice.discountValue = 0;                    
        } else {
			invoice.discountPercentage = 0;
			invoice.discountValue = 0;
		}
		
                UpdateCurrentInvoiceData(invoice);
		UpdateTotalPrice(true);
		SaveTable();
    }//GEN-LAST:event_jButtonInvoiceR1ActionPerformed

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
    
    private void jButtonItemDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonItemDiscountActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
                }
		
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		ClientAppSelectDiscountDialog selectDialog = new ClientAppSelectDiscountDialog(null, true);
                selectDialog.setVisible(true);
		
		invoice.items.get(rowId).discountPercentage = selectDialog.selectedDiscountPercentage;
		invoice.items.get(rowId).discountValue = selectDialog.selectedDiscountValue;
		
		String discount = "";
		float totalItemPrice = invoice.items.get(rowId).itemAmount * invoice.items.get(rowId).itemPrice;
		if(invoice.items.get(rowId).discountPercentage != 0f){
			totalItemPrice = totalItemPrice * (100f - invoice.items.get(rowId).discountPercentage) / 100f;
			discount = invoice.items.get(rowId).discountPercentage + " %";
		} else if(invoice.items.get(rowId).discountValue != 0f){
			totalItemPrice = totalItemPrice - invoice.items.get(rowId).discountValue * invoice.items.get(rowId).itemAmount;
			discount = ClientAppUtils.FloatToPriceString(invoice.items.get(rowId).discountValue) + " kn/kom";
		}
		
		defaultTableModel.setValueAt(totalItemPrice, rowId, COLUMN_INDEX_TOTAL);
		defaultTableModel.setValueAt(discount, rowId, COLUMN_INDEX_DISCOUNT);
		
		UpdateTotalPrice(true);
    }//GEN-LAST:event_jButtonItemDiscountActionPerformed

    private void jButtonOpenCashRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenCashRegisterActionPerformed
		
    }//GEN-LAST:event_jButtonOpenCashRegisterActionPerformed

    private void jButtonItemNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonItemNoteActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		boolean noteExist = false;
		String defaultText = "";
		if(rowId + 1 < invoice.items.size()){
			if(!"".equals(invoice.items.get(rowId + 1).itemNote)){
				noteExist = true;
				defaultText = invoice.items.get(rowId + 1).itemNote;
			}
		}
		
		ClientAppKeyboardDialog keyboardDialog = new ClientAppKeyboardDialog(null, true, "Napomena na stavku", defaultText, 64);
        keyboardDialog.setVisible(true);
		String newNote = keyboardDialog.enteredText;
		
		if(noteExist){
			if("".equals(newNote)){
				invoice.items.remove(rowId + 1);
				defaultTableModel.removeRow(rowId + 1);
				if(jTable1.getRowCount() > 0){
					jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
				}
			} else {
				invoice.items.get(rowId + 1).itemNote = newNote;
				defaultTableModel.setValueAt(" - " + newNote, rowId + 1, COLUMN_INDEX_NAME);
			}
		} else {
			if(!"".equals(newNote)){
				InvoiceItem invoiceItem = new InvoiceItem();
				invoiceItem.itemNote = newNote;
				invoice.items.add(rowId + 1, invoiceItem);

				Object[] rowData = new Object[6];
				rowData[COLUMN_INDEX_ID] = "";
				rowData[COLUMN_INDEX_NAME] = " - " + newNote;
				rowData[COLUMN_INDEX_AMOUNT] = "";
				rowData[COLUMN_INDEX_PRICE] = "";
				rowData[COLUMN_INDEX_DISCOUNT] = "";
				rowData[COLUMN_INDEX_TOTAL] = "";
				defaultTableModel.insertRow(rowId + 1, rowData);

				jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
			}
		}
		
		SaveTable();
    }//GEN-LAST:event_jButtonItemNoteActionPerformed

    private void jButtonInvoiceDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceDiscountActionPerformed
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		ClientAppSelectDiscountDialog selectDialog = new ClientAppSelectDiscountDialog(null, true);
        selectDialog.setVisible(true);
		if(invoice.totalPrice < 0){
                    invoice.discountValue = selectDialog.selectedDiscountValue * (-1);
                }else{
                    invoice.discountValue = selectDialog.selectedDiscountValue;
                }
		invoice.discountPercentage = selectDialog.selectedDiscountPercentage;

		
		UpdateTotalPrice(true);
    }//GEN-LAST:event_jButtonInvoiceDiscountActionPerformed

    private void jButtonCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCashActionPerformed
		OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_CASH);
    }//GEN-LAST:event_jButtonCashActionPerformed

    private void jButtonPrintKitchenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintKitchenActionPerformed
                UpdateCurrentInvoiceData(invoice);
		Invoice invoiceKitchen = new Invoice(invoice);
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN.ordinal())){
			ClientAppKeyboardMultilineDialog keyboardDialog = new ClientAppKeyboardMultilineDialog(null, true, "Napomena na narudžbu u kuhinji", defaultNote, 512);
			keyboardDialog.setVisible(true);
			invoiceKitchen.note = keyboardDialog.enteredText;
		}
		
		if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN_PRINT.ordinal())){
			invoiceKitchen.items = new ArrayList<>();
			for(int i = 0; i < invoice.items.size(); ++i){
				if("".equals(invoice.items.get(i).itemNote)){
					InvoiceItem invoiceItem = new InvoiceItem(invoice.items.get(i));
					invoiceKitchen.items.add(invoiceItem);
				}
			}
		}
		
		// Only new articles (food)
		ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
		for (int i = 0; i < invoiceKitchen.items.size(); ++i){
			int itemType = invoiceKitchen.items.get(i).itemType;
			if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE && invoiceKitchen.items.get(i).isFood && invoiceKitchen.items.get(i).printedAmount != invoiceKitchen.items.get(i).itemAmount){
				InvoiceItem invoiceItem = new InvoiceItem(invoiceKitchen.items.get(i));
				invoiceItem.itemAmount -= invoiceItem.printedAmount;
				invoiceItems.add(invoiceItem);
				
				if(i+1 < invoiceKitchen.items.size() && !"".equals(invoiceKitchen.items.get(i+1).itemNote)){
					InvoiceItem invoiceItemNote = new InvoiceItem(invoiceKitchen.items.get(i+1));
					invoiceItems.add(invoiceItemNote);
					++i;
				}
			} else {
				if(i+1 < invoiceKitchen.items.size() && !"".equals(invoiceKitchen.items.get(i+1).itemNote)){
					++i;
				}
			}
		}
		invoiceKitchen.items = invoiceItems;
		
		if(invoiceKitchen.items.size() == 0){
			ClientAppLogger.GetInstance().ShowMessage("Nema novih stavaka za ispis u kuhinju!");
			return;
		}
		
		// Tag printed items
		for (int i = 0; i < invoice.items.size(); ++i){
			int itemType = invoice.items.get(i).itemType;
			if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE && invoice.items.get(i).isFood){
				invoice.items.get(i).printedAmount = invoice.items.get(i).itemAmount;
			}
		}
		
		int invoiceCopies = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_KITCHEN.ordinal());
		for(int i = 0; i < invoiceCopies; ++i){
			try {
				PrintUtils.PrintPosInvoice(invoiceKitchen, Values.POS_PRINTER_TYPE_KITCHEN, currentTableId);
			} catch (IOException ex) {
				Logger.getLogger(ClientAppCashRegisterStandardDialog.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		SaveTable();
    }//GEN-LAST:event_jButtonPrintKitchenActionPerformed

    private void jButtonPrintBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintBarActionPerformed
        UpdateCurrentInvoiceData(invoice);
		Invoice invoiceBar = new Invoice(invoice);
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR.ordinal())){
			ClientAppKeyboardMultilineDialog keyboardDialog = new ClientAppKeyboardMultilineDialog(null, true, "Napomena na narudžbu na šanku", defaultNote, 512);
			keyboardDialog.setVisible(true);
			invoiceBar.note = keyboardDialog.enteredText;
		}
		
		if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR_PRINT.ordinal())){
			invoiceBar.items = new ArrayList<>();
			for(int i = 0; i < invoice.items.size(); ++i){
				if("".equals(invoice.items.get(i).itemNote)){
					InvoiceItem invoiceItem = new InvoiceItem(invoice.items.get(i));
					invoiceBar.items.add(invoiceItem);
				}
			}
		}
		
		// Only new articles (no food) and trading goods
		ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
		for (int i = 0; i < invoiceBar.items.size(); ++i){
			int itemType = invoiceBar.items.get(i).itemType;
			if ((itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE && !invoiceBar.items.get(i).isFood || itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) 
					&& invoiceBar.items.get(i).printedAmount != invoiceBar.items.get(i).itemAmount){
				InvoiceItem invoiceItem = new InvoiceItem(invoiceBar.items.get(i));
				invoiceItem.itemAmount -= invoiceItem.printedAmount;
				invoiceItems.add(invoiceItem);
												
				if(i+1 < invoiceBar.items.size() && !"".equals(invoiceBar.items.get(i+1).itemNote)){
					InvoiceItem invoiceItemNote = new InvoiceItem(invoiceBar.items.get(i+1));
					invoiceItems.add(invoiceItemNote);
					++i;
				}
			} else {
				if(i+1 < invoiceBar.items.size() && !"".equals(invoiceBar.items.get(i+1).itemNote)){
					++i;
				}
			}
		}
		invoiceBar.items = invoiceItems;
		
		if(invoiceBar.items.size() == 0){
			ClientAppLogger.GetInstance().ShowMessage("Nema novih stavaka za ispis na šank!");
			return;
		}
		
		// Tag printed items
		for (int i = 0; i < invoice.items.size(); ++i){
			int itemType = invoice.items.get(i).itemType;
			if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE && !invoice.items.get(i).isFood || itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS){
				invoice.items.get(i).printedAmount = invoice.items.get(i).itemAmount;
			}
		}
		
		int invoiceCopies = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_BAR.ordinal());
		for(int i = 0; i < invoiceCopies; ++i){
			try {
				PrintUtils.PrintPosInvoice(invoiceBar, Values.POS_PRINTER_TYPE_BAR, currentTableId);
			} catch (IOException ex) {
				Logger.getLogger(ClientAppCashRegisterStandardDialog.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		SaveTable();
    }//GEN-LAST:event_jButtonPrintBarActionPerformed

    private void jButtonCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCardActionPerformed
		OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_CREDIT_CARD);
    }//GEN-LAST:event_jButtonCardActionPerformed

    private void jButtonOtherPaymentMethodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOtherPaymentMethodsActionPerformed
		OnInvoiceDone(Values.PAYMENT_METHOD_ANY_METHOD);
    }//GEN-LAST:event_jButtonOtherPaymentMethodsActionPerformed

    private void jButtonTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTablesActionPerformed
		ClientAppCashRegisterTablesDialog dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
		dialog.setVisible(true);
		int oldCurrentTableId = currentTableId;
		currentTableId = dialog.selectedTableId;
                boolean emptyInvoiceItems = !invoice.items.isEmpty();
		if(oldCurrentTableId == -1){ //maybe use here emptyInvoiceItems
			SaveTable();
		} else {
			LoadTable();
		}
		UpdateCurrentUser();
		
		jLabelCashRegisterName.setText("Kasa " + startCashRegisterNumber + (currentTableId != -1 ? " - Stol " + (currentTableId + 1) : ""));
    }//GEN-LAST:event_jButtonTablesActionPerformed

    private void jButtonStaffUserChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStaffUserChangeActionPerformed
		ClientAppLoginPasswordOnlyDialog dialog = new ClientAppLoginPasswordOnlyDialog(null, true, true, -1);
		dialog.setVisible(true);
		if(dialog.loginSuccess){
			UpdateCurrentUser();
			SaveTable(true);
		}
    }//GEN-LAST:event_jButtonStaffUserChangeActionPerformed

    private void jButtonTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTotalActionPerformed
		new ClientAppReportsTotalDialog(null, true, false).setVisible(true);
    }//GEN-LAST:event_jButtonTotalActionPerformed

    private void jButtonSaldoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaldoActionPerformed
		new ClientAppCashRegisterSaldoDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonSaldoActionPerformed

    private void jButtonOfferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOfferActionPerformed
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_OFFER);
    }//GEN-LAST:event_jButtonOfferActionPerformed

    private void jButtonLoadOfferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadOfferActionPerformed
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		ClientAppLoadOfferDialog dialog = new ClientAppLoadOfferDialog(null, true, Values.PAYMENT_METHOD_TYPE_OFFER);
		dialog.setVisible(true);
		if(dialog.selectedInvoice == null){
			return;
		}
		
		dialog.selectedInvoice.note += System.lineSeparator() + "Račun kreiran prema ponudi broj " + dialog.selectedInvoice.specialNumber + "/" + dialog.selectedInvoice.officeTag + "/" + dialog.selectedInvoice.cashRegisterNumber;
		SetCurrentInvoice(dialog.selectedInvoice);
		
		invoice.specialZki = dialog.selectedInvoice.zki;
		invoice.specialJir = dialog.selectedInvoice.jir;
    }//GEN-LAST:event_jButtonLoadOfferActionPerformed

    private void jButtonStaffInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStaffInvoiceActionPerformed
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		float discountAmount = ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_STAFF_DISCOUNT_AMOUNT.ordinal());
		invoice.discountPercentage = discountAmount;
		invoice.discountValue = 0f;
		UpdateTotalPrice(true);
		
		invoice.note += System.lineSeparator() + "Račun djelatnika " + StaffUserInfo.GetCurrentUserInfo().userId + "-" + StaffUserInfo.GetCurrentUserInfo().firstName;
    }//GEN-LAST:event_jButtonStaffInvoiceActionPerformed

    private void jButtonSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSubtotalActionPerformed
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_SUBTOTAL);
		
		invoice.specialZki = invoice.zki;
		invoice.specialJir = invoice.jir;
    }//GEN-LAST:event_jButtonSubtotalActionPerformed

    private void jButtonEventPricesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEventPricesActionPerformed
		if(invoice.items.size() != 0){
			ClientAppLogger.GetInstance().ShowMessage("Račun mora biti prazan za uključivanje / isključivanje event cijena");
			return;
		}
		
		if(CheckIfAnyTableExist()){
			ClientAppLogger.GetInstance().ShowMessage("Svi stolovi moraju biti prazni za uključivanje / isključivanje event cijena");
			return;
		}
				
		ClientAppOwnerAuthDialog dialog = new ClientAppOwnerAuthDialog(null, true);
		dialog.setVisible(true);
		if (!dialog.authSuccess){
			ClientAppLogger.GetInstance().ShowMessage("Potvrda Vlasnika ili Poslovođe nije uspjela");
			return;
		}
		
		eventPrices = !eventPrices;
		jLabelEventPrices.setText(eventPrices ? "EVENT CIJENE" : "");
    }//GEN-LAST:event_jButtonEventPricesActionPerformed

    private void jButtonQuickPickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQuickPickActionPerformed
		ClientAppQuickPickDialog dialog = new ClientAppQuickPickDialog(null, true);
		dialog.setVisible(true);
		
		String[] quickPickIdStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickNameStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickTypeStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		for (int i = 0; i < jLabelQuickPickGroup.length; ++i){
			if (i < quickPickIdStrings.length){
				quickPickId[i] = Integer.parseInt(quickPickIdStrings[i]);
			} else {
				quickPickId[i] = -1;
			}
			
			if (i < quickPickTypeStrings.length){
				quickPickType[i] = Integer.parseInt(quickPickTypeStrings[i]);
			} else {
				quickPickType[i] = -1;
			}
			
			if (i < quickPickNameStrings.length){
				quickPickName[i] = quickPickNameStrings[i];
			} else {
				quickPickName[i] = "";
			}
			
			jLabelQuickPickGroup[i].setText(jLabelQuickPickLetters[i] + " = " + quickPickName[i]);
		}
    }//GEN-LAST:event_jButtonQuickPickActionPerformed

	private void OnInvoiceDone(int paymentMethodType){
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
			return;
		}
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()) && paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			ClientAppLogger.GetInstance().ShowMessage("Na ovoj blagajni onemogućeno je izdavanje računa.");
			return;
		}
		
		if(HaveOldUnfiscalizedInvoices()){
			ClientAppLogger.GetInstance().ShowMessage("Upozorenje: u poslovnici postoje nefiskalizirani računi stariji od 48 sati. Izdavanje novih računa je onemogućeno!");
			return;
		}
		
		if(invoice.items.size() == 0){
			ClientAppLogger.GetInstance().ShowMessage("Račun je prazan");
			return;
		}
		
		for(int i = 0; i < invoice.items.size(); ++i){
			if(invoice.items.get(i).itemAmount == 0f && "".equals(invoice.items.get(i).itemNote)){
				ClientAppLogger.GetInstance().ShowMessage("Stavka ne može imati količinu 0 ");
				return;
			}
		}
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN && isProduction){
			ClientAppLogger.GetInstance().ShowMessage("U produkcijskom okruženju nije moguće izdavati račune kao Admin");
			return;
		}
		
		// Check working time
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		int minuteOfHour = c.get(Calendar.MINUTE);
		if(dayOfWeek == -1){
			dayOfWeek = 6;
		}
		
		boolean timeIsBeforeWorktime = hourOfDay < workDayHoursFrom[dayOfWeek] || (hourOfDay == workDayHoursFrom[dayOfWeek] && minuteOfHour < workDayMinutesFrom[dayOfWeek]);
		boolean timeIsAfterWorktime = hourOfDay > workDayHoursTo[dayOfWeek] || (hourOfDay == workDayHoursTo[dayOfWeek] && minuteOfHour > workDayMinutesTo[dayOfWeek]);
		boolean isTimeInverted = workDayHoursFrom[dayOfWeek] > workDayHoursTo[dayOfWeek] || workDayHoursFrom[dayOfWeek] == workDayHoursTo[dayOfWeek] && workDayMinutesFrom[dayOfWeek] > workDayMinutesTo[dayOfWeek];
		if(!isWorkDay[dayOfWeek] || (!isTimeInverted && (timeIsBeforeWorktime || timeIsAfterWorktime)) || (isTimeInverted && (timeIsBeforeWorktime && timeIsAfterWorktime))){
			ClientAppLogger.GetInstance().ShowMessageBlock("Upozorenje: izdajete račun izvan radnog vremena poslovnice!" + System.lineSeparator() + "Samo vlasnik može izdavati račune izvan radnog vremena!");
			if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
				return;
			}
		}
                		
		/*boolean userShiftFound = false;
		for (int i = 0; i < currentUserWorkDayList.size(); ++i){
			if(currentUserWorkDayList.get(i) != dayOfWeek)
				continue;
			
			boolean timeIsBeforeShift = hourOfDay < currentUserWorkHourFromList.get(i) || (hourOfDay == currentUserWorkHourFromList.get(i) && minuteOfHour < currentUserWorkMinuteFromList.get(i));
			boolean timeIsAfterShift = hourOfDay > currentUserWorkHourToList.get(i) || (hourOfDay == currentUserWorkHourToList.get(i) && minuteOfHour > currentUserWorkMinuteToList.get(i));
			boolean isShiftInverted = currentUserWorkHourFromList.get(i) > currentUserWorkHourToList.get(i) || currentUserWorkHourFromList.get(i) == currentUserWorkHourToList.get(i) && currentUserWorkMinuteFromList.get(i) > currentUserWorkMinuteToList.get(i);
			if((!isShiftInverted && !(timeIsBeforeShift || timeIsAfterShift)) || (isShiftInverted && !(timeIsBeforeShift && timeIsAfterShift))){
				userShiftFound = true;
			}
		}
		if(!userShiftFound){
			ClientAppLogger.GetInstance().ShowMessageBlock("Upozorenje: izdajete račun izvan vašeg radnog vremena!");
		}*/
		
		// Enter password if needed
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_PRINTINVOICE.ordinal())){
			ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, StaffUserInfo.GetCurrentUserInfo().userId);
			dialog.setVisible(true);
			if(!dialog.loginSuccess){
				return;
			}
		}
		
		// Enter note if enabled
		if(invoiceNoteEnabled){
			String defaultNoteString = !"".equals(invoice.note) ? invoice.note : defaultNote;
			ClientAppKeyboardMultilineDialog keyboardDialog = new ClientAppKeyboardMultilineDialog(null, true, "Napomena na račun", defaultNoteString, 512);
			keyboardDialog.setVisible(true);
			invoice.note = keyboardDialog.enteredText;
		}
		
		// Select payment method
		String selectedPaymentMethodName = "";
		int selectedPaymentMethodType;
		if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_CASH){
			selectedPaymentMethodName = "Novčanice i/ili kovanice";
			selectedPaymentMethodType = Values.PAYMENT_METHOD_TYPE_CASH;
		} else if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_CREDIT_CARD){
			ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{Values.PAYMENT_METHOD_TYPE_CREDIT_CARD}, false, 0f);
			dialog.setVisible(true);
			if(dialog.selectedPaymentMethodType == -1){
				return;
			}
			selectedPaymentMethodName = dialog.selectedPaymentMethodName;
			selectedPaymentMethodType = Values.PAYMENT_METHOD_TYPE_CREDIT_CARD;
		} else if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_OFFER) {
			selectedPaymentMethodName = Values.PAYMENT_METHOD_OFFER_NAME;
			selectedPaymentMethodType = Values.PAYMENT_METHOD_TYPE_OFFER;
		} else if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
			/*ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{});
			dialog.setVisible(true);
			if(dialog.selectedPaymentMethodType == -1){
				return;
			}
			selectedPaymentMethodName = dialog.selectedPaymentMethodName;
			selectedPaymentMethodType = Values.PAYMENT_METHOD_TYPE_SUBTOTAL;*/
			
			selectedPaymentMethodName = "Novčanice i/ili kovanice";
			selectedPaymentMethodType = Values.PAYMENT_METHOD_TYPE_SUBTOTAL;
		} else {
			ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{}, true, invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			dialog.setVisible(true);
			if(dialog.selectedPaymentMethodType == -1){
				return;
			}
			selectedPaymentMethodName = dialog.selectedPaymentMethodName;
			selectedPaymentMethodType = dialog.selectedPaymentMethodType;
			invoice.paymentMethodName2 = dialog.selectedPaymentMethodName2;
			invoice.paymentMethodType2 = dialog.selectedPaymentMethodType2;
			if (invoice.discountValue != 0f){
				invoice.paymentAmount2 = dialog.paymentAmount2 + invoice.discountValue;
			} else if (invoice.discountPercentage != 0f){
				invoice.paymentAmount2 = dialog.paymentAmount2 * 100f / (100f - invoice.discountPercentage);
			} else {
				invoice.paymentAmount2 = dialog.paymentAmount2;
			}
		}
		
		if(invoice.clientId == -1 && (selectedPaymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL || selectedPaymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP)){
			ClientAppLogger.GetInstance().ShowMessage("Za ovaj način plaćanja račun mora biti R1." + System.lineSeparator() + "Račun nije izdan!");
			return;
		}
		
		// Invoice data
		UpdateCurrentInvoiceData(invoice);
		invoice.paymentMethodName = selectedPaymentMethodName;
		invoice.paymentMethodType = selectedPaymentMethodType;
		
		// Show cash return dialog
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_SHOW_CASH_RETURN_DIALOG.ordinal())){
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_CASH){
				float cashReturnDialogPrice = 0f;
				if (invoice.paymentMethodType2 == -1){
					cashReturnDialogPrice = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
				} else {
					cashReturnDialogPrice = (invoice.totalPrice - invoice.paymentAmount2) * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
				}

				ClientAppCashReturnDialog cashReturnDialog = new ClientAppCashReturnDialog(null, true, cashReturnDialogPrice);
				cashReturnDialog.setVisible(true);
				if(cashReturnDialog.cancelPayment)
					return;
			} else if(invoice.paymentMethodType2 == Values.PAYMENT_METHOD_TYPE_CASH){
				ClientAppCashReturnDialog cashReturnDialog = new ClientAppCashReturnDialog(null, true, invoice.paymentAmount2 * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
				cashReturnDialog.setVisible(true);
				if(cashReturnDialog.cancelPayment)
					return;
			}
		}

		// Setup subtotal
		if(invoice.isSubtotal){
			//invoice.note += "Račun izdan prema predračunu broj " + invoice.specialNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber;
		}
		
		if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
			// Get ZKI
			invoice.zki = Fiscalization.CalculateZKI(invoice);
			if(Values.DEFAULT_ZKI.equals(invoice.zki)){
				ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
						+ System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
				return;
			}
			
			// Insert local invoice
			boolean invoiceInsertSuccess = ClientAppUtils.InsertLocalInvoice(invoice);
			if(!invoiceInsertSuccess){
				ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
				return;
			}
			
			Fiscalization.FiscalizeInvoiceSynchronized(invoice, true);
			
			if(Values.DEFAULT_JIR.equals(invoice.jir)){
				ClientAppLogger.GetInstance().ShowMessage("Pogreška prilikom fiskalizacije računa. Račun nije fiskaliziran!" + System.lineSeparator() + "Račun će se fiskalizirati naknadno.");
				RefreshUnfisc();
			}
		} else {
			// Insert non-fiscalization invoice
			boolean invoiceInsertSuccess = ClientAppUtils.InsertLocalInvoice(invoice);
			if(!invoiceInsertSuccess){
				ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
				return;
			}
		}
		
		if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			invoice.isSubtotal = true;
			jLabelSubtotal.setText("STANJE STOLA");
		}
		
		PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
		
		// Extra copies
		invoice.isCopy = true;
		int invoiceCopies = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal());
		for(int i = 1; i < invoiceCopies; ++i){
			PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
		}
		
		if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_OFFER && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			// Send to kitchen
			if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN.ordinal())){
				jButtonPrintKitchen.doClick();
			}

			// Update last invoice
			lastInvoice = invoice;
			jLabelLastInvoicePrice.setText(ClientAppUtils.FloatToPriceString(lastInvoice.totalPrice * (100f - lastInvoice.discountPercentage) / 100f - lastInvoice.discountValue));
		}
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN_SUBTOTAL.ordinal()) && invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			jButtonPrintKitchen.doClick();
		}
		
		if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			ResetAllFields(true);
		} else {
			if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_SUBTOTAL_AUTODELETE.ordinal())){
				ResetAllFields(true);
			}
		}
	}
	
	private void UpdateCurrentInvoiceData(Invoice invoiceToUpdate){
		invoiceToUpdate.date = new Date();
		invoiceToUpdate.cashRegisterNumber = startCashRegisterNumber;
		invoiceToUpdate.officeNumber = startOfficeNumber;
		invoiceToUpdate.officeTag = startOfficeTag;
		invoiceToUpdate.staffId = StaffUserInfo.GetCurrentUserInfo().userId;
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_STUDENT){
			invoiceToUpdate.staffOib = startCompanyOib;
		} else {
			invoiceToUpdate.staffOib = StaffUserInfo.GetCurrentUserInfo().userOIB;
		}
		invoiceToUpdate.staffName = StaffUserInfo.GetCurrentUserInfo().firstName;
		invoiceToUpdate.zki = Values.DEFAULT_ZKI;
		invoiceToUpdate.jir = Values.DEFAULT_JIR;
		invoiceToUpdate.isTest = !isProduction;
		invoiceToUpdate.isInVatSystem = isInVatSystem;
	}
	
	private void ResetAllFields(boolean doSaveTable){
		invoice = new Invoice();
		
		jLabelInvoiceDiscount.setText("");
		jLabelTotalPrice.setText("0,00");
		jLabelR1.setText("");
		jLabelSubtotal.setText("");
		
		defaultTableModel.setRowCount(0);
		
		if(doSaveTable){
			SaveTable();
		}
	}
	
	private int GetSelectedItemIndex(){
		int rowId = jTable1.getSelectedRow();
		if(rowId != -1 && !"".equals(invoice.items.get(rowId).itemNote)){
			rowId--;
		}
		
		return rowId;
	}
	
	private void OnCashRegisterExit(){
		cashRegisterOpen = false;
	}
	
	private Thread updateDateTimeThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(cashRegisterOpen){                            
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						Date date = new Date();
						jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy.").format(date));
						jLabelTime.setText(new SimpleDateFormat("HH:mm:ss").format(date));                                                
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {}
			}
		}
	});
	
	private Thread lockCashRegisterThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(cashRegisterOpen){
				long currentTime = System.currentTimeMillis() / 1000;
				if(lockCashRegister && currentTime > lastInputTime + lockCashRegisterTime && lastInputTime != -1){
					ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, true, -1);
					dialog.setVisible(true);
					if(dialog.loginSuccess){
						lastInputTime = System.currentTimeMillis() / 1000;
						UpdateCurrentUser();
					} else {
						int dialogResult = JOptionPane.showConfirmDialog (null, "Izlazite iz aplikacije!" + System.lineSeparator()
								+ "Jeste li sigurni da želite izaći?", "Izađi iz aplikacije", JOptionPane.YES_NO_OPTION);
						if(dialogResult == JOptionPane.YES_OPTION){
							ClearTableIfEmpty();
							if(ClientApp.GetInstance() != null){
								ClientApp.GetInstance().OnAppClose();
							}
						} else {
							continue;
						}
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {}
			}
		}
	});
        
        
  private Thread lockTableThread = new Thread(new Runnable() {
                ClientAppCashRegisterTablesDialog dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
		@Override
		public void run() {
			while(cashRegisterOpen){
                                long currentTime = System.currentTimeMillis() / 1000;
                                    if(lockTable && currentTime > lastInputTime + lockTableTime && lastInputTime != -1){
                                        ClientAppLogger.GetInstance().LogMessage("Step 1: Time: " + currentTime + "dialog.tbselected is:  " + dialog.tbSelected);
                                            if (!dialog.tbSelected){
                                            ClientAppLogger.GetInstance().LogMessage("Step 2: Time: " + currentTime + "set visible is:  " + true);
                                                dialog.setVisible(true);
                                            }
                                            
                                            ClientAppLogger.GetInstance().LogMessage("Step 3: Employee click table is: " + dialog.employeeClickTable + "set visible is:  " + true);
                                            if(dialog.employeeClickTable){
                                                    lastInputTime = System.currentTimeMillis() / 1000;
                                                    UpdateCurrentUser();
                                            } 
                                    }

                                    try {
                                            Thread.sleep(1000);
                                    } catch (InterruptedException ex) {}
                                }
			}
	});
  
     private Thread lockTableThread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            while (cashRegisterOpen) {
                ClientAppLogger.GetInstance().LogMessage("STEP 1: Entered cash register. Cash register is: " + cashRegisterOpen);
                long currentTime = System.currentTimeMillis() / 1000;
                ClientAppLogger.GetInstance().LogMessage("STEP 2: Current time is: " + currentTime);
                                    ClientAppLogger.GetInstance().LogMessage("STEP 2.5: Lock table " + lockTable);
                    ClientAppLogger.GetInstance().LogMessage("STEP 2.5: Current time is: " + currentTime);
                    ClientAppLogger.GetInstance().LogMessage("STEP 2.5: lastInputTime is: " + lastInputTime);
                    ClientAppLogger.GetInstance().LogMessage("STEP 2.5: lockTableTime is: " + lockTableTime);
                    ClientAppLogger.GetInstance().LogMessage("STEP 2.5: employeeClickTabl is: " + employeeClickTable);
                    if (((currentTime - lastInputTime) / 2) > lockTableTime)
                    {
                        dialog.dispose();
                    }
                    
                    if (lockTable && currentTime > lastInputTime + lockTableTime && lastInputTime != -1 && !employeeClickTable ){
                        if (tryOpenCashRegisterDialog()) {
                            if (isDialogOpenAtomic.get()) {
                                lastInputTime = System.currentTimeMillis() / 1000;
                                UpdateCurrentUser();
                                ClientAppLogger.GetInstance().LogMessage("STEP 8: I have update the current user and last input time is: " + lastInputTime);
                            }
                        }
                    }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private boolean tryOpenCashRegisterDialog() {
            ClientAppLogger.GetInstance().LogMessage("STEP 3: Going to open cash register. isDialogOpenAtomic is: " + isDialogOpenAtomic);
            if (!isDialogOpenAtomic.getAndSet(true)) {
                try {
                    ClientAppLogger.GetInstance().LogMessage("STEP 4: Going to open cash register. isDialogOpenAtomic is: " + isDialogOpenAtomic);
                    ClientAppLogger.GetInstance().LogMessage("STEP 5: Going to open cash register. employeeClickTable is: " + employeeClickTable);
                    openCashRegisterDialog();
                    employeeClickTable = true;
                    ClientAppLogger.GetInstance().LogMessage("STEP 6: AFTER to open cash register. isDialogOpenAtomic is: " + isDialogOpenAtomic);
                    ClientAppLogger.GetInstance().LogMessage("STEP 7: AFTER to open cash register. employeeClickTable is: " + employeeClickTable);
                    return true;
                } finally {
                    isDialogOpenAtomic.set(false);
                }
            }
            return false;
        }

        private void openCashRegisterDialog() {
            dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                ClientAppLogger.GetInstance().LogMessage("STEP 11: Dialog is closed: reseting the flags, is dialog open atomic, cash register to true and lock table to true.");
                    // Reset the flag when the dialog is closed
                    isDialogOpenAtomic.set(false);
                    cashRegisterOpen = true;
                    lockTable = true;
                    employeeClickTable = false;
               ClientAppLogger.GetInstance().LogMessage("STEP 12: Employee click table after window closed is: " + employeeClickTable);
                }
            });
              dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    ClientAppLogger.GetInstance().LogMessage("STEP 9: Under window activated, employeeClickTable is: " + employeeClickTable);
                    if (employeeClickTable){
                        dialog.setVisible(false);
                        employeeClickTable = false;
                    ClientAppLogger.GetInstance().LogMessage("STEP 10: After window activated, employeeClickTable is: " + employeeClickTable);
                    }
                }
            });
            dialog.setVisible(true);
        }
    });
    
    private Thread lockTableThread3 = new Thread(new Runnable() {
        @Override
        public void run() {
            while (cashRegisterOpen) {
                long currentTime = System.currentTimeMillis() / 1000;
                if (lockTable && currentTime > lastInputTime + lockTableTime && lastInputTime != -1) {
                    if (tryAcquireDialogLock()) {
                        try {
                            openCashRegisterDialog();
                        } finally {
                            releaseDialogLock();
                        }
                    }

                    if (isDialogOpen) {
                        lastInputTime = System.currentTimeMillis() / 1000;
                        UpdateCurrentUser();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private boolean tryAcquireDialogLock() {
            return dialogLock.tryLock();
        }

        private void releaseDialogLock() {
            dialogLock.unlock();
        }

        private void openCashRegisterDialog() {
            if (!isDialogOpen) {
                dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        isDialogOpen = false;
                    }
                });
                dialog.setVisible(true);
                isDialogOpen = true;
            }
        }
    });
    
    private Thread lockTableThread4 = new Thread(new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis() / 1000;
            while (cashRegisterOpen) {
                if (lockTable && currentTime > lastInputTime + lockTableTime && lastInputTime != -1)
                {
                    if (tryOpenCashRegisterDialog(currentTime)) {
                        if (isDialogOpen && !dialog.employeeClickTable) {
                            lastInputTime = System.currentTimeMillis() / 1000;
                            UpdateCurrentUser();
                        }
                        else {
                            dialog.dispose();
                        }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
             }
           }
        }

        private synchronized boolean tryOpenCashRegisterDialog(Long currentTime) {
            if (!isDialogOpen) {
                openCashRegisterDialog();
                return true;
            }
            return false;
        }

        private void openCashRegisterDialog() {
            isDialogOpen = true;
            dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    // Reset the flag when the dialog is closed
                    isDialogOpen = false;
                }
            });
            dialog.setVisible(true);
        }
    });
     
      private Thread lockTableThread5 = new Thread(new Runnable() {
        @Override
        public void run() {
            while (cashRegisterOpen) {
                long currentTime = System.currentTimeMillis() / 1000;
                if (lockTable && currentTime > lastInputTime + lockTableTime && lastInputTime != -1) {
                    if (!isDialogOpen) {
                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                openCashRegisterDialog();
                                return null;
                            }
                        };
                        worker.execute();
                    }

                    if (isDialogOpen) {
                        lastInputTime = System.currentTimeMillis() / 1000;
                        UpdateCurrentUser();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void openCashRegisterDialog() {
            isDialogOpen = true;
            dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Reset the flag when the dialog is closed
                    isDialogOpen = false;
                }
            });
            dialog.setVisible(true);
        }
    });
        
        
        
        
        
	
	private void SetCurrentInvoice(Invoice newInvoice){
		ResetAllFields(false);
		
		if(newInvoice == null)
			return;
		
		invoice = newInvoice;
		
		if(invoice.clientId == -1){
			jLabelR1.setText("");
		} else {
			jLabelR1.setText("R1: " + invoice.clientName + ", OIB: " + invoice.clientOIB);
		}
		
		jLabelSubtotal.setText(invoice.isSubtotal ? "STANJE STOLA" : "");
		
		for (int i = 0; i < invoice.items.size(); ++i){
			Object[] rowData = new Object[6];
			
			if("".equals(invoice.items.get(i).itemNote)){
				rowData[COLUMN_INDEX_ID] = invoice.items.get(i).itemId;
				rowData[COLUMN_INDEX_NAME] = invoice.items.get(i).itemName;
				rowData[COLUMN_INDEX_AMOUNT] = invoice.items.get(i).itemAmount;
				rowData[COLUMN_INDEX_PRICE] = invoice.items.get(i).itemPrice;

				String discount = "";
				float totalItemPrice = invoice.items.get(i).itemAmount * invoice.items.get(i).itemPrice;
				if(invoice.items.get(i).discountPercentage != 0f){
					totalItemPrice = totalItemPrice * (100f - invoice.items.get(i).discountPercentage) / 100f;
					discount = invoice.items.get(i).discountPercentage + " %";
				} else if(invoice.items.get(i).discountValue != 0f){
					totalItemPrice = totalItemPrice - invoice.items.get(i).discountValue * invoice.items.get(i).itemAmount;
					discount = ClientAppUtils.FloatToPriceString(invoice.items.get(i).discountValue) + " kn/kom";
				}

				rowData[COLUMN_INDEX_DISCOUNT] = discount;
				rowData[COLUMN_INDEX_TOTAL] = totalItemPrice;
			} else {
				rowData[COLUMN_INDEX_NAME] = " - " + invoice.items.get(i).itemNote;
			}
			
			defaultTableModel.addRow(rowData);
		}
		
		if(jTable1.getRowCount() > 0){
			jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
		}
		
		UpdateTotalPrice(false);
	}
	
	private void SaveTable(){
		SaveTable(false);
    }
    
    private void SaveTable(boolean forceOverwrite){
		lastInputTime = System.currentTimeMillis() / 1000;
		
		if(currentTableId == -1)
			return;
		
		byte[] invoiceBytes = null;
		try { 
			invoiceBytes = Utils.SerializeObject(invoice);
		} catch (Exception ex){
			ClientAppLogger.GetInstance().LogError(ex);
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);

		String query = "UPDATE TABLES SET PRICE = ?, INVOICE_DATA = ?, STAFF_ID = ?, CR_NUM = ?, STAFF_NAME = ? "
				+ "WHERE ID = ? AND (STAFF_ID = ? OR STAFF_ID = -1) AND (CR_NUM = ? OR CR_NUM = -1)";
		if (forceOverwrite){
			query = "UPDATE TABLES SET PRICE = ?, INVOICE_DATA = ?, STAFF_ID = ?, CR_NUM = ?, STAFF_NAME = ? "
				+ "WHERE ID = ? AND (STAFF_ID = ? OR 1 = 1) AND (CR_NUM = ? OR CR_NUM = -1)";
		}
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
		databaseQuery.AddParam(2, invoiceBytes);
		databaseQuery.AddParam(3, StaffUserInfo.GetCurrentUserInfo().userId);
		databaseQuery.AddParam(4, startCashRegisterNumber);
		databaseQuery.AddParam(5, StaffUserInfo.GetCurrentUserInfo().firstName);
		databaseQuery.AddParam(6, currentTableId);
		databaseQuery.AddParam(7, StaffUserInfo.GetCurrentUserInfo().userId);
		databaseQuery.AddParam(8, startCashRegisterNumber);
		databaseQuery.executeLocally = true;
		
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
	
	private void LoadTable(){
		if(currentTableId == -1){
			ResetAllFields(false);
			return;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryUpdate = "UPDATE TABLES SET STAFF_ID = ?, CR_NUM = ?, STAFF_NAME = ? WHERE ID = ?";
		String queryLoad = "SELECT INVOICE_DATA FROM TABLES WHERE ID = ?";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, queryUpdate);
		multiDatabaseQuery.AddParam(0, 1, StaffUserInfo.GetCurrentUserInfo().userId);
		multiDatabaseQuery.AddParam(0, 2, startCashRegisterNumber);
		multiDatabaseQuery.AddParam(0, 3, StaffUserInfo.GetCurrentUserInfo().firstName);
		multiDatabaseQuery.AddParam(0, 4, currentTableId);
		multiDatabaseQuery.SetQuery(1, queryLoad);
		multiDatabaseQuery.AddParam(1, 1, currentTableId);
		multiDatabaseQuery.executeLocally = true;
		
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
					if (databaseQueryResult[1].next()) {
						byte[] invoiceBytes = databaseQueryResult[1].getBytes(0);
						Invoice newInvoice = (Invoice) Utils.DeserializeObject(invoiceBytes);
						SetCurrentInvoice(newInvoice);
					}
				}
			} catch (IOException | ClassNotFoundException ex) {
				ClientAppLogger.GetInstance().LogError(ex);
				ResetAllFields(true);
				SaveTable(true);
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private boolean CheckIfAnyTableExist(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT INVOICE_DATA FROM TABLES WHERE CR_NUM = ? AND PRICE <> 0";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
		multiDatabaseQuery.SetQuery(0, query);
		multiDatabaseQuery.AddParam(0, 1, startCashRegisterNumber);
		multiDatabaseQuery.executeLocally = true;
		
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
						return true;
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return false;
	}
	
	private void ClearTableIfEmpty(){
		if(currentTableId == -1)
			return;
		
		if(invoice.items.size() != 0)
			return;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "UPDATE TABLES SET STAFF_ID = -1 WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, currentTableId);
		databaseQuery.executeLocally = true;
		
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
		
		currentTableId = -1;
	}
	
	private void LoadWorktimeSettings() {
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT "
					+ "W1, W2, W3, W4, W5, W6, W7, "
					+ "HF1, HT1, MF1, MT1, "
					+ "HF2, HT2, MF2, MT2, "
					+ "HF3, HT3, MF3, MT3, "
					+ "HF4, HT4, MF4, MT4, "
					+ "HF5, HT5, MF5, MT5, "
					+ "HF6, HT6, MF6, MT6, "
					+ "HF7, HT7, MF7, MT7 "
					+ "FROM OFFICE_WORKTIME WHERE OFFICE_NUMBER = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, Licence.GetOfficeNumber());
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
							for (int i = 0; i < 7; ++i){
								isWorkDay[i] = databaseQueryResult.getInt(i) == 0;
								workDayHoursFrom[i] = databaseQueryResult.getInt(7 + 4 * i);
								workDayHoursTo[i] = databaseQueryResult.getInt(8 + 4 * i);
								workDayMinutesFrom[i] = databaseQueryResult.getInt(9 + 4 * i);
								workDayMinutesTo[i] = databaseQueryResult.getInt(10 + 4 * i);
							}
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	
	private void RefreshUnfisc(){
		if(ClientAppUtils.HaveUnfiscalizedInvoices(isProduction)){
			jLabelNefisk1.setText("POSTOJE NEFISK.");
			jLabelNefisk2.setText("RAČUNI");
		} else {
			jLabelNefisk1.setText("");
			jLabelNefisk2.setText("");
		}
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonCard;
    private javax.swing.JButton jButtonCash;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonEventPrices;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonInvoiceCancelation;
    private javax.swing.JButton jButtonInvoiceCopyPrint;
    private javax.swing.JButton jButtonInvoiceDiscount;
    private javax.swing.JButton jButtonInvoiceR1;
    private javax.swing.JButton jButtonItemDiscount;
    private javax.swing.JButton jButtonItemNote;
    private javax.swing.JButton jButtonLastInvoiceChangePaymentMethod;
    private javax.swing.JButton jButtonLoadOffer;
    private javax.swing.JButton jButtonOffer;
    private javax.swing.JButton jButtonOpenCashRegister;
    private javax.swing.JButton jButtonOtherPaymentMethods;
    private javax.swing.JButton jButtonPrintBar;
    private javax.swing.JButton jButtonPrintKitchen;
    private javax.swing.JButton jButtonQuickPick;
    private javax.swing.JButton jButtonSaldo;
    private javax.swing.JButton jButtonStaffInvoice;
    private javax.swing.JButton jButtonStaffUserChange;
    private javax.swing.JButton jButtonSubtotal;
    private javax.swing.JButton jButtonTables;
    private javax.swing.JButton jButtonTotal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelCashRegisterName;
    private javax.swing.JLabel jLabelCompanyData1;
    private javax.swing.JLabel jLabelCompanyData2;
    private javax.swing.JLabel jLabelCompanyData3;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelEventPrices;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelInvoiceDiscount;
    private javax.swing.JLabel jLabelLastInvoicePrice;
    private javax.swing.JLabel jLabelNefisk1;
    private javax.swing.JLabel jLabelNefisk2;
    private javax.swing.JLabel jLabelQuickPick0;
    private javax.swing.JLabel jLabelQuickPick1;
    private javax.swing.JLabel jLabelQuickPick10;
    private javax.swing.JLabel jLabelQuickPick11;
    private javax.swing.JLabel jLabelQuickPick12;
    private javax.swing.JLabel jLabelQuickPick13;
    private javax.swing.JLabel jLabelQuickPick14;
    private javax.swing.JLabel jLabelQuickPick15;
    private javax.swing.JLabel jLabelQuickPick16;
    private javax.swing.JLabel jLabelQuickPick17;
    private javax.swing.JLabel jLabelQuickPick18;
    private javax.swing.JLabel jLabelQuickPick19;
    private javax.swing.JLabel jLabelQuickPick2;
    private javax.swing.JLabel jLabelQuickPick20;
    private javax.swing.JLabel jLabelQuickPick21;
    private javax.swing.JLabel jLabelQuickPick22;
    private javax.swing.JLabel jLabelQuickPick23;
    private javax.swing.JLabel jLabelQuickPick24;
    private javax.swing.JLabel jLabelQuickPick25;
    private javax.swing.JLabel jLabelQuickPick26;
    private javax.swing.JLabel jLabelQuickPick27;
    private javax.swing.JLabel jLabelQuickPick28;
    private javax.swing.JLabel jLabelQuickPick29;
    private javax.swing.JLabel jLabelQuickPick3;
    private javax.swing.JLabel jLabelQuickPick30;
    private javax.swing.JLabel jLabelQuickPick4;
    private javax.swing.JLabel jLabelQuickPick5;
    private javax.swing.JLabel jLabelQuickPick6;
    private javax.swing.JLabel jLabelQuickPick7;
    private javax.swing.JLabel jLabelQuickPick8;
    private javax.swing.JLabel jLabelQuickPick9;
    private javax.swing.JLabel jLabelR1;
    private javax.swing.JLabel jLabelStaffUserName;
    private javax.swing.JLabel jLabelSubtotal;
    private javax.swing.JLabel jLabelTime;
    private javax.swing.JLabel jLabelTotalPrice;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
