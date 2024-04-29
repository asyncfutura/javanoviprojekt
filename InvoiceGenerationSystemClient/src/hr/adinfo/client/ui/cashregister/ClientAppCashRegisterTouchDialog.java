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
import hr.adinfo.client.ui.reports.ClientAppTipInvoicesDialog;
import hr.adinfo.client.ui.settings.ClientAppSettingsCashRegisterDialog;
import hr.adinfo.client.ui.staff.ClientAppStaffRightsDialog;
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
import hr.adinfo.utils.extensions.ColorIcon;
import hr.adinfo.utils.licence.Licence;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Matej
 */
public class ClientAppCashRegisterTouchDialog extends javax.swing.JDialog {
	private static final int COLUMN_INDEX_NAME = 0;
	private static final int COLUMN_INDEX_AMOUNT = 1;
	private static final int COLUMN_INDEX_PRICE = 2;
	private static final int COLUMN_INDEX_DISCOUNT = 3;
	private static final int COLUMN_INDEX_TOTAL = 4;
	
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
	private String[] groupButtonNames = new String[10];
	private String[][] subgroupButtonNames = new String[10][4];
	private int[][][] itemButtonId = new int[10][4][35];
	private int[][][] itemButtonType = new int[10][4][35];
	private int[][][] itemButtonColor = new int[10][4][35];
	
	private ArrayList<WarehouseItem> warehouseItems = new ArrayList<>();
	private DefaultTableModel defaultTableModel;
	private ArrayList<Pair<Integer, Integer>> articlesNormativeCount = new ArrayList<>();
			
	private int selectedGroupIndex = 0;
	private final JButton[] jButtonListGroup;
	private final JButton[][] jButtonListItems;
	
	private boolean cashRegisterOpen = true;
	
	private boolean invoiceNoteEnabled;
	private String defaultNote = "";
	private Invoice lastInvoice = null;
	private int currentTableId = -1;
	private boolean eventPrices;
	private boolean lockCashRegister;
	private long lockCashRegisterTime;
        private boolean lockTable;
        private long lockTableTime;
	private long lastInputTime;        
        private boolean employeeClickTable;
        private ClientAppStaffRightsDialog clientAppStaffRightsDialog;
	
	// Invoice data
	private Invoice invoice = new Invoice();
	private int startOfficeNumber;
	private String startOfficeTag;
	private int startCashRegisterNumber;
	private String startCompanyOib;
	private boolean isProduction;
	private boolean isInVatSystem;
        private String street;
        private String houseNum;
        private String town;
	// Unfiscalized invoices check
	private Date oldestUnfiscalizedInvoiceDate;
        private boolean isDialogOpen = false;
        private Lock dialogLock = new ReentrantLock();
        private ClientAppCashRegisterTablesDialog dialog;
        private AtomicBoolean isDialogOpenAtomic = new AtomicBoolean(false);

	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppCashRegisterTouchDialog(java.awt.Frame parent, boolean modal) throws InterruptedException {
		super(parent, modal);
		initComponents();
		
		setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		
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
				
				if(ke.getID() == KeyEvent.KEY_PRESSED){
					if(ke.getKeyCode() == KeyEvent.VK_ESCAPE){
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDelete.doClick();
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
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				int rowId = GetSelectedItemIndex();
				if (mouseEvent.getClickCount() == 2 && rowId != -1) {
					OnTableDoubleClick(rowId);
				}
			}
		});
                

		// Init
		jButtonListGroup = new JButton[]{
			jButton1, jButton2, jButton3, jButton4, jButton5, 
			jButton6, jButton7, jButton8, jButton9, jButton10,
		};
		jButtonListItems = new JButton[][]{
			new JButton[]{
				jButton11, jButton12, jButton13, jButton14, jButton15, jButton16, jButton17, jButton18, jButton19, jButton20, 
				jButton21, jButton22, jButton23, jButton24, jButton25, jButton26, jButton27, jButton28, jButton29, jButton30, 
				jButton31, jButton32, jButton33, jButton34, jButton35, jButton36, jButton37, jButton38, jButton39, jButton40, 
				jButton41, jButton42, jButton43, jButton44, jButton45 },
			new JButton[]{
				jButton46, jButton47, jButton48, jButton49, jButton50, 
				jButton51, jButton52, jButton53, jButton54, jButton55, jButton56, jButton57, jButton58, jButton59, jButton60, 
				jButton61, jButton62, jButton63, jButton64, jButton65, jButton66, jButton67, jButton68, jButton69, jButton70, 
				jButton71, jButton72, jButton73, jButton74, jButton75, jButton76, jButton77, jButton78, jButton79, jButton80 },
			new JButton[]{ 
				jButton81, jButton82, jButton83, jButton84, jButton85, jButton86, jButton87, jButton88, jButton89, jButton90, 
				jButton91, jButton92, jButton93, jButton94, jButton95, jButton96, jButton97, jButton98, jButton99, jButton100, 
				jButton101, jButton102, jButton103, jButton104, jButton105, jButton106, jButton107, jButton108, jButton109, jButton110, 
				jButton111, jButton112, jButton113, jButton114, jButton115 },
			new JButton[]{
				jButton116, jButton117, jButton118, jButton119, jButton120, 
				jButton121, jButton122, jButton123, jButton124, jButton125, jButton126, jButton127, jButton128, jButton129, jButton130, 
				jButton131, jButton132, jButton133, jButton134, jButton135, jButton136, jButton137, jButton138, jButton139, jButton140, 
				jButton141, jButton142, jButton143, jButton144, jButton145, jButton146, jButton147, jButton148, jButton149, jButton150, 
			}
		};
		
		for (int i = 0; i < jButtonListGroup.length; ++i){
			final int index = i;
			jButtonListGroup[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				jButtonGroupActionPerformed(index);
			});
		}
		
		for (int i = 0; i < jButtonListItems.length; ++i){
				for (int j = 0; j < jButtonListItems[i].length; ++j){
				final int index = j;
				jButtonListItems[i][j].addActionListener((java.awt.event.ActionEvent evt) -> {
					jButtonItemActionPerformed(index);
				});
			}
		}
		
		// Create if no exist
		ClientAppUtils.CreateConsumptionTaxAmountsIfNoExist(startOfficeNumber);
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(startOfficeNumber);
		ClientAppUtils.CreateAllTradingGoodsAmountsIfNoExist(startOfficeNumber);
		
		// Load settings
		ClientAppSettings.LoadSettings(false);
		invoiceNoteEnabled = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE.ordinal());
		isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		groupButtonNames = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_LAYOUT_GROUP_NAMES.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		for (int i = 0; i < jButtonListGroup.length; ++i){
			subgroupButtonNames[i] = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_SUBGROUP_NAMES[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemIdsString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_IDS[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemTypesString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_TYPES[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemColorsString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_COLORS[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			for (int j = 0; j < jButtonListItems.length; ++j){
				for (int k = 0; k < jButtonListItems[j].length; ++k){
					int itemId = -1;
					int itemType = -1;
					int itemColor = 0;
					try {
						itemId = Integer.parseInt(itemIdsString[j * 35 + k]);
						itemType = Integer.parseInt(itemTypesString[j * 35 + k]);
						itemColor = Integer.parseInt(itemColorsString[j * 35 + k]);
					} catch (NumberFormatException ex) {}
					
					itemButtonId[i][j][k] = itemId;
					itemButtonType[i][j][k] = itemType;
					itemButtonColor[i][j][k] = itemColor;
				}
			}
		}
                
                
		
		lockCashRegister = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER.ordinal());
		lockCashRegisterTime = (long) ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER_TIME.ordinal());
		isInVatSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
                
                lockTable = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE.ordinal());
                lockTableTime = (long)ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE_TIME.ordinal());
		
		// Apply settings
		for (int i = 0; i < jButtonListGroup.length; ++i){
			ClientAppUtils.SetTouchLayoutGroupButtonText(jButtonListGroup[i], groupButtonNames[i]);
		}

		for (int i = 0; i < jTabbedPane1.getTabCount(); ++i){
			ClientAppUtils.SetTouchLayoutTabTitleText(jTabbedPane1, i, subgroupButtonNames[selectedGroupIndex][i]);
		}
		
		UpdateCurrentUser();
		
		// Setup table
		//jTable1.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 40 / 100);
		jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
		jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
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
		
		// Refresh items
		jTabbedPane1StateChanged(null);
		
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
		
		updateDateTimeThread.start();
		lockCashRegisterThread.start();
                //lockTableThread2.start();
                
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ev) {
				// Revert buttons
				for (int i = 0; i < jButtonListItems.length; ++i){
					for (int j = 0; j < jButtonListItems[i].length; ++j){
						//jButtonListItems[i][j].setText("");
						jButtonListItems[i][j].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[0], jButtonListItems[i][j].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[i][j].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
					}
				}
		
				// Load new values
				int tabIndex = jTabbedPane1.getSelectedIndex();
				for (int i = 0; i < itemButtonId[selectedGroupIndex][tabIndex].length; ++i){
					int itemId = itemButtonId[selectedGroupIndex][tabIndex][i];
					if(itemId != -1){
						int itemType = itemButtonType[selectedGroupIndex][tabIndex][i];
						int colorIndex = itemButtonColor[selectedGroupIndex][tabIndex][i];
						String name = "";
						for(WarehouseItem warehouseItem : warehouseItems){
							if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
								name = warehouseItem.itemName;
								break;
							}
						}

						if("".equals(name))
							continue;

						//ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][i], name);
						jButtonListItems[tabIndex][i].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[colorIndex], jButtonListItems[tabIndex][i].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][i].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
					}
				}
			}
		});
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Refresh items
				jTabbedPane1StateChanged(null);
			}
		});
                
                try {
                    Thread.sleep(500);
                    ClientAppCashRegisterTablesDialog dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
                    dialog.setVisible(true);
                    int oldCurrentTableId = currentTableId;
                    currentTableId = dialog.selectedTableId;
                    if(oldCurrentTableId == -1 && !invoice.items.isEmpty()){
                            SaveTable();
                    } else {
                            LoadTable();
                    }
                    UpdateCurrentUser();

                    jLabelCashRegisterName.setText("Kasa " + startCashRegisterNumber + (currentTableId != -1 ? " - Stol " + (currentTableId + 1) : ""));
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
        
        private static StaffUserInfo currentStaffUser;
	
	private void UpdateCurrentUser(){
            
                clientAppStaffRightsDialog = new 
                ClientAppStaffRightsDialog(null, true, StaffUserInfo.GetCurrentUserInfo().userId, StaffUserInfo.GetCurrentUserInfo().firstName, 
                        StaffUserInfo.GetCurrentUserInfo().userOIB, "");

            
		jLabelStaffUserName.setText(StaffUserInfo.GetCurrentUserInfo().fullName);
		jButtonTotal.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_TOTAL]);
		jButtonSaldo.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_SALDO]);
		jButtonOpenCashRegister.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_DISCOUTS]);
		jButtonItemDiscount.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_DISCOUTS]);
		jButtonInvoiceCancelation.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_INVOICE_CANCELATION]);
		jButtonOffer.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_OFFER]);
		jButtonLoadOffer.setEnabled(clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_OFFER]);
                
                if (StaffUserInfo.GetCurrentUserInfo().userRightsType == 1){
                    jButtonTotal.setEnabled(true);
                }
                
                if (StaffUserInfo.GetCurrentUserInfo().userRightsType == 1){
                    jButtonSaldo.setEnabled(true);
                }
		                
		jButtonTotal.setText(jButtonTotal.isEnabled() ? "<html> <div style=\"text-align: center\"> Total </div> </html>" : "");
		jButtonSaldo.setText(jButtonSaldo.isEnabled() ? "<html> <div style=\"text-align: center\"> Saldo </div> </html>" : "");
		jButtonOpenCashRegister.setText(jButtonOpenCashRegister.isEnabled() ? "<html> <div style=\"text-align: center\"> Otvori <br> ladicu </div> </html>" : "");
		jButtonItemDiscount.setText(jButtonItemDiscount.isEnabled() ? "<html> <div style=\"text-align: center\"> Popust <br> na stavku </div> </html>" : "");
		jButtonInvoiceCancelation.setText(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER ? "<html> <div style=\"text-align: center\"> Storno <br> računa </div> </html>" : "");
		jButtonOffer.setText(jButtonOffer.isEnabled() ? "<html> <div style=\"text-align: center\"> Ponuda </div> </html>" : "");
		jButtonLoadOffer.setText(jButtonLoadOffer.isEnabled() ? "<html> <div style=\"text-align: center\"> Učitaj <br> ponudu </div> </html>" : "");
		
                
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
	
	private void jButtonGroupActionPerformed(int index) {                                         
		selectedGroupIndex = index;
		for (int i = 0; i < jTabbedPane1.getTabCount(); ++i){
			ClientAppUtils.SetTouchLayoutTabTitleText(jTabbedPane1, i, subgroupButtonNames[selectedGroupIndex][i]);
		}
		if(jTabbedPane1.getSelectedIndex() != 0){
			jTabbedPane1.setSelectedIndex(0);
		} else {
			jTabbedPane1StateChanged(null);
		}
    }
	
	private void jButtonItemActionPerformed(int index) {
		int tabIndex = jTabbedPane1.getSelectedIndex();
		int itemId = itemButtonId[selectedGroupIndex][tabIndex][index];
		if(itemId == -1)
			return;
		
		if(invoice.isSubtotal){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
			return;
		}
		
		int itemType = itemButtonType[selectedGroupIndex][tabIndex][index];		
		String itemName = "";
		float itemPrice = 0f;
		float itemTaxRate = 0f;
		float itemConsumptionTaxRate = 0f;
		boolean isFood = false;
		float packagingRefund = 0f;
		for(WarehouseItem warehouseItem : warehouseItems){
			if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
				if(warehouseItem.articleWithoutNormatives){
					ClientAppLogger.GetInstance().ShowMessage("Nije moguće odabrati ovaj artikl jer nema normativa!");
					return;
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
		
		if("".equals(itemName))
			return;
		
		boolean insertItem = true;
		for (int i = 0; i < invoice.items.size(); ++i){
			if(invoice.items.get(i).itemId == itemId && invoice.items.get(i).itemType == itemType && invoice.items.get(i).itemPrice == itemPrice 
					&& invoice.items.get(i).discountPercentage == 0f && invoice.items.get(i).discountValue == 0f){
				insertItem = false;
				invoice.items.get(i).itemAmount += 1f;
				RefreshItemAmount(i);
			}
		}
		
		if(insertItem){
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

			Object[] rowData = new Object[5];
			rowData[COLUMN_INDEX_NAME] = itemName;
			rowData[COLUMN_INDEX_AMOUNT] = "1";
			rowData[COLUMN_INDEX_PRICE] = itemPrice;
			rowData[COLUMN_INDEX_DISCOUNT] = "";
			rowData[COLUMN_INDEX_TOTAL] = itemPrice;
			defaultTableModel.addRow(rowData);
			
			UpdateTotalPrice(true);
		}
		
		jTable1.setRowSelectionInterval(jTable1.getRowCount() - 1, jTable1.getRowCount() - 1);
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
        jPanelButtons = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel16 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton95 = new javax.swing.JButton();
        jButton96 = new javax.swing.JButton();
        jButton97 = new javax.swing.JButton();
        jButton98 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton99 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jButton101 = new javax.swing.JButton();
        jButton102 = new javax.swing.JButton();
        jButton103 = new javax.swing.JButton();
        jButton104 = new javax.swing.JButton();
        jButton105 = new javax.swing.JButton();
        jButton106 = new javax.swing.JButton();
        jButton107 = new javax.swing.JButton();
        jButton108 = new javax.swing.JButton();
        jButton109 = new javax.swing.JButton();
        jButton110 = new javax.swing.JButton();
        jButton111 = new javax.swing.JButton();
        jButton112 = new javax.swing.JButton();
        jButton113 = new javax.swing.JButton();
        jButton114 = new javax.swing.JButton();
        jButton115 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton116 = new javax.swing.JButton();
        jButton117 = new javax.swing.JButton();
        jButton118 = new javax.swing.JButton();
        jButton119 = new javax.swing.JButton();
        jButton120 = new javax.swing.JButton();
        jButton121 = new javax.swing.JButton();
        jButton122 = new javax.swing.JButton();
        jButton123 = new javax.swing.JButton();
        jButton124 = new javax.swing.JButton();
        jButton125 = new javax.swing.JButton();
        jButton126 = new javax.swing.JButton();
        jButton127 = new javax.swing.JButton();
        jButton128 = new javax.swing.JButton();
        jButton129 = new javax.swing.JButton();
        jButton130 = new javax.swing.JButton();
        jButton131 = new javax.swing.JButton();
        jButton132 = new javax.swing.JButton();
        jButton133 = new javax.swing.JButton();
        jButton134 = new javax.swing.JButton();
        jButton135 = new javax.swing.JButton();
        jButton136 = new javax.swing.JButton();
        jButton137 = new javax.swing.JButton();
        jButton138 = new javax.swing.JButton();
        jButton139 = new javax.swing.JButton();
        jButton140 = new javax.swing.JButton();
        jButton141 = new javax.swing.JButton();
        jButton142 = new javax.swing.JButton();
        jButton143 = new javax.swing.JButton();
        jButton144 = new javax.swing.JButton();
        jButton145 = new javax.swing.JButton();
        jButton146 = new javax.swing.JButton();
        jButton147 = new javax.swing.JButton();
        jButton148 = new javax.swing.JButton();
        jButton149 = new javax.swing.JButton();
        jButton150 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jButtonPrintKitchen = new javax.swing.JButton();
        jButtonPrintBar = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jButtonDelete = new javax.swing.JButton();
        jButtonMinus = new javax.swing.JButton();
        jButtonPlus = new javax.swing.JButton();
        jButtonItemNote = new javax.swing.JButton();
        jButtonItemDiscount = new javax.swing.JButton();
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
        jButtonNapojnica = new javax.swing.JButton();

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 61, Short.MAX_VALUE)
                        .addComponent(jLabelCompanyData2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 51, Short.MAX_VALUE))
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
                        .addComponent(jLabelDate, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTime, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelNefisk1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelCashRegisterName, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelStaffUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                        .addComponent(jLabelNefisk2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)))
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

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButton1.setBackground(new java.awt.Color(102, 102, 102));
        jButton1.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton1.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton1.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton1.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton1.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton2.setBackground(new java.awt.Color(102, 102, 102));
        jButton2.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton2.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton2.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton2.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton2.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton3.setBackground(new java.awt.Color(102, 102, 102));
        jButton3.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton3.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton3.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton3.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton3.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton4.setBackground(new java.awt.Color(102, 102, 102));
        jButton4.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton4.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton4.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton4.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton5.setBackground(new java.awt.Color(102, 102, 102));
        jButton5.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton5.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton5.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton5.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton5.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton6.setBackground(new java.awt.Color(102, 102, 102));
        jButton6.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton6.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton6.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton6.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton6.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton7.setBackground(new java.awt.Color(102, 102, 102));
        jButton7.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton7.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton7.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton7.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton7.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton8.setBackground(new java.awt.Color(102, 102, 102));
        jButton8.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton8.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton8.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton8.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton8.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton9.setBackground(new java.awt.Color(102, 102, 102));
        jButton9.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton9.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton9.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton9.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton9.setPreferredSize(new java.awt.Dimension(70, 60));

        jButton10.setBackground(new java.awt.Color(102, 102, 102));
        jButton10.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton10.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton10.setMaximumSize(new java.awt.Dimension(700, 600));
        jButton10.setMinimumSize(new java.awt.Dimension(70, 60));
        jButton10.setPreferredSize(new java.awt.Dimension(70, 60));

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Izlaz <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );

        jSplitPane2.setDividerSize(0);
        jSplitPane2.setResizeWeight(0.35);
        jSplitPane2.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPane2.setPreferredSize(new java.awt.Dimension(996, 580));

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel16.setMinimumSize(new java.awt.Dimension(379, 559));
        jPanel16.setLayout(new java.awt.GridLayout(1, 0));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton11);

        jButton12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton12);

        jButton13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton13);

        jButton14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton14);

        jButton15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton15);

        jButton16.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton16);

        jButton17.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton17);

        jButton18.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton18);

        jButton19.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton19);

        jButton20.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton20);

        jButton21.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton21);

        jButton22.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton22.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton22);

        jButton23.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton23.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton23);

        jButton24.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton24.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton24);

        jButton25.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton25.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton25);

        jButton26.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton26.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton26);

        jButton27.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton27.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton27.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton27);

        jButton28.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton28.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton28.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton28);

        jButton29.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton29.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton29.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton29);

        jButton30.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton30.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton30.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton30);

        jButton31.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton31.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton31.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton31);

        jButton32.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton32.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton32.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton32);

        jButton33.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton33.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton33.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton33);

        jButton34.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton34.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton34.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton34);

        jButton35.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton35.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton35.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton35);

        jButton36.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton36.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton36.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton36);

        jButton37.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton37.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton37.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton37);

        jButton38.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton38.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton38.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton38);

        jButton39.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton39.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton39.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton39);

        jButton40.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton40.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton40.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton40);

        jButton41.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton41.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton41.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton41);

        jButton42.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton42.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton42.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton42);

        jButton43.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton43.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton43.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton43);

        jButton44.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton44.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton44.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton44);

        jButton45.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton45.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton45.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton45);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel1);

        jPanel3.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton46.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton46.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton46.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton46);

        jButton47.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton47.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton47.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton47);

        jButton48.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton48.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton48.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton48);

        jButton49.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton49.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton49.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton49);

        jButton50.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton50.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton50.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton50);

        jButton51.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton51.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton51.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton51);

        jButton52.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton52.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton52.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton52);

        jButton53.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton53.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton53.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton53);

        jButton54.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton54.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton54.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton54);

        jButton55.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton55.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton55.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton55);

        jButton56.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton56.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton56.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton56);

        jButton57.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton57.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton57.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton57);

        jButton58.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton58.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton58.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton58);

        jButton59.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton59.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton59.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton59);

        jButton60.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton60.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton60.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton60);

        jButton61.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton61.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton61.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton61);

        jButton62.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton62.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton62);

        jButton63.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton63.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton63.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton63);

        jButton64.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton64.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton64.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton64);

        jButton65.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton65.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton65.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton65);

        jButton66.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton66.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton66.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton66);

        jButton67.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton67.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton67.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton67);

        jButton68.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton68.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton68.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton68);

        jButton69.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton69.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton69.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton69);

        jButton70.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton70.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton70.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton70);

        jButton71.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton71.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton71.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton71);

        jButton72.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton72.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton72.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton72);

        jButton73.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton73.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton73.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton73);

        jButton74.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton74.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton74.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton74);

        jButton75.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton75.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton75.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton75);

        jButton76.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton76.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton76.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton76);

        jButton77.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton77.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton77.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton77);

        jButton78.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton78.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton78.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton78);

        jButton79.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton79.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton79.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton79);

        jButton80.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton80.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton80.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton80);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel3);

        jPanel4.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton81.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton81.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton81.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton81);

        jButton82.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton82.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton82.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton82);

        jButton83.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton83.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton83.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton83);

        jButton84.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton84.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton84.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton84);

        jButton85.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton85.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton85.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton85);

        jButton86.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton86.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton86.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton86);

        jButton87.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton87.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton87.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton87);

        jButton88.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton88.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton88.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton88);

        jButton89.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton89.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton89.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton89);

        jButton90.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton90.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton90.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton90);

        jButton91.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton91.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton91.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton91);

        jButton92.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton92.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton92.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton92);

        jButton94.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton94.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton94.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton94);

        jButton95.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton95.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton95.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton95);

        jButton96.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton96.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton96.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton96);

        jButton97.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton97.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton97.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton97);

        jButton98.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton98.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton98.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton98);

        jButton93.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton93.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton93.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton93);

        jButton99.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton99.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton99.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton99);

        jButton100.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton100.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton100.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton100);

        jButton101.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton101.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton101.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton101);

        jButton102.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton102.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton102.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton102);

        jButton103.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton103.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton103.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton103);

        jButton104.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton104.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton104.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton104);

        jButton105.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton105.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton105.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton105);

        jButton106.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton106.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton106.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton106);

        jButton107.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton107.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton107.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton107);

        jButton108.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton108.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton108.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton108);

        jButton109.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton109.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton109.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton109);

        jButton110.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton110.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton110.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton110);

        jButton111.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton111.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton111.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton111);

        jButton112.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton112.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton112.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton112);

        jButton113.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton113.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton113.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton113);

        jButton114.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton114.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton114.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton114);

        jButton115.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton115.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton115.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton115);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel4);

        jPanel5.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton116.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton116.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton116.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton116);

        jButton117.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton117.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton117.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton117);

        jButton118.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton118.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton118.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton118);

        jButton119.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton119.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton119.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton119);

        jButton120.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton120.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton120.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton120);

        jButton121.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton121.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton121.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton121);

        jButton122.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton122.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton122.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton122);

        jButton123.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton123.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton123.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton123);

        jButton124.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton124.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton124.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton124);

        jButton125.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton125.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton125.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton125);

        jButton126.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton126.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton126.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton126);

        jButton127.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton127.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton127.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton127);

        jButton128.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton128.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton128.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton128);

        jButton129.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton129.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton129.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton129);

        jButton130.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton130.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton130.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton130);

        jButton131.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton131.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton131.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton131);

        jButton132.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton132.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton132.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton132);

        jButton133.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton133.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton133.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton133);

        jButton134.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton134.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton134.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton134);

        jButton135.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton135.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton135.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton135);

        jButton136.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton136.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton136.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton136);

        jButton137.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton137.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton137.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton137);

        jButton138.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton138.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton138.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton138);

        jButton139.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton139.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton139.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton139);

        jButton140.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton140.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton140.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton140);

        jButton141.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton141.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton141.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton141);

        jButton142.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton142.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton142.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton142);

        jButton143.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton143.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton143.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton143);

        jButton144.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton144.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton144.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton144);

        jButton145.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton145.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton145.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton145);

        jButton146.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton146.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton146.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton146);

        jButton147.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton147.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton147.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton147);

        jButton148.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton148.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton148.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton148);

        jButton149.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton149.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton149.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton149);

        jButton150.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton150.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton150.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton150);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel5);

        jPanel16.add(jTabbedPane1);

        jSplitPane2.setLeftComponent(jPanel16);

        jPanel17.setMinimumSize(new java.awt.Dimension(612, 579));

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonPrintKitchen.setText("<html> <div style=\"text-align: center\"> Ispis <br> kuhinja </div> </html>");
        jButtonPrintKitchen.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintKitchen.setMaximumSize(new java.awt.Dimension(100, 80));
        jButtonPrintKitchen.setPreferredSize(new java.awt.Dimension(60, 55));
        jButtonPrintKitchen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintKitchenActionPerformed(evt);
            }
        });

        jButtonPrintBar.setText("<html> <div style=\"text-align: center\"> Ispis <br> šank </div> </html>");
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
                .addComponent(jButtonPrintKitchen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonDelete.setFont(jButtonDelete.getFont().deriveFont(jButtonDelete.getFont().getSize()+5f));
        jButtonDelete.setText("Briši");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonMinus.setFont(jButtonMinus.getFont().deriveFont(jButtonMinus.getFont().getSize()+25f));
        jButtonMinus.setText("-");
        jButtonMinus.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMinusActionPerformed(evt);
            }
        });

        jButtonPlus.setFont(jButtonPlus.getFont().deriveFont(jButtonPlus.getFont().getSize()+25f));
        jButtonPlus.setText("+");
        jButtonPlus.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlusActionPerformed(evt);
            }
        });

        jButtonItemNote.setText("<html> <div style=\"text-align: center\"> Napomena <br> na stavku </div> </html>");
        jButtonItemNote.setToolTipText("");
        jButtonItemNote.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonItemNote.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonItemNote.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonItemNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonItemNoteActionPerformed(evt);
            }
        });

        jButtonItemDiscount.setText("<html> <div style=\"text-align: center\"> Popust <br> na stavku </div> </html>");
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
                .addGap(31, 31, 31)
                .addComponent(jButtonItemNote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonItemDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPlus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonMinus, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonItemNote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonItemDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPlus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonMinus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(4, 4, 4))
        );

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Naziv stavke", "Količina", "Cijena", "Popust", "Ukupno"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setRowHeight(25);
        jScrollPane1.setViewportView(jTable1);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Zadnji račun"));

        jButtonLastInvoiceChangePaymentMethod.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonLastInvoiceChangePaymentMethod.setText("<html> <div style=\"text-align: center\"> Promjena <br> načina  <br> plaćanja </div> </html>");
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
                .addGap(18, 47, Short.MAX_VALUE)
                .addComponent(jButtonLastInvoiceChangePaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
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
                .addComponent(jButtonLastInvoiceChangePaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Računi"));

        jButtonInvoiceCancelation.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonInvoiceCancelation.setText("<html> <div style=\"text-align: center\"> Storno <br> računa </div> </html>");
        jButtonInvoiceCancelation.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonInvoiceCancelation.setPreferredSize(new java.awt.Dimension(60, 53));
        jButtonInvoiceCancelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceCancelationActionPerformed(evt);
            }
        });

        jButtonInvoiceCopyPrint.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonInvoiceCopyPrint.setText("<html> <div style=\"text-align: center\"> Ispis <br> kopije <br> računa </div> </html>");
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
                .addComponent(jButtonInvoiceCancelation, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonInvoiceCopyPrint, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonInvoiceCancelation, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                    .addComponent(jButtonInvoiceCopyPrint, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
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
                .addContainerGap(76, Short.MAX_VALUE)
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
        jButtonCash.setText("GOTOVINA");
        jButtonCash.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonCash.setPreferredSize(new java.awt.Dimension(100, 110));
        jButtonCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCashActionPerformed(evt);
            }
        });

        jButtonCard.setBackground(new java.awt.Color(102, 255, 102));
        jButtonCard.setText("KARTICA");
        jButtonCard.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonCard.setPreferredSize(new java.awt.Dimension(80, 53));
        jButtonCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCardActionPerformed(evt);
            }
        });

        jButtonOtherPaymentMethods.setBackground(new java.awt.Color(102, 255, 102));
        jButtonOtherPaymentMethods.setText("<html> <div style=\"text-align: center\"> Drugi načini <br> plaćanja </div> </html>");
        jButtonOtherPaymentMethods.setToolTipText("");
        jButtonOtherPaymentMethods.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonOtherPaymentMethods.setPreferredSize(new java.awt.Dimension(80, 53));
        jButtonOtherPaymentMethods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOtherPaymentMethodsActionPerformed(evt);
            }
        });

        jPanel15.setLayout(new java.awt.GridLayout(2, 0, 5, 5));

        jButtonStaffInvoice.setText("<html> <div style=\"text-align: center\"> Račun <br> djelatnika </div> </html>");
        jButtonStaffInvoice.setToolTipText("");
        jButtonStaffInvoice.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonStaffInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffInvoiceActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonStaffInvoice);

        jButtonOffer.setText("Ponuda");
        jButtonOffer.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonOffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOfferActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonOffer);

        jButtonTotal.setText("<html> <div style=\"text-align: center\"> Total </div> </html>");
        jButtonTotal.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTotalActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonTotal);

        jButtonTables.setBackground(new java.awt.Color(153, 153, 0));
        jButtonTables.setText("Stolovi");
        jButtonTables.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTablesActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonTables);

        jButtonSubtotal.setText("<html> <div style=\"text-align: center\"> Stanje <br> stola </div> </html>");
        jButtonSubtotal.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSubtotalActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonSubtotal);

        jButtonInvoiceR1.setBackground(new java.awt.Color(0, 102, 204));
        jButtonInvoiceR1.setText("R1");
        jButtonInvoiceR1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonInvoiceR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvoiceR1ActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonInvoiceR1);

        jButtonStaffUserChange.setText("<html> <div style=\"text-align: center\"> Promjena <br> djelatnika </div> </html>");
        jButtonStaffUserChange.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonStaffUserChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffUserChangeActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonStaffUserChange);

        jButtonLoadOffer.setText("<html> <div style=\"text-align: center\"> Učitaj <br> ponudu </div> </html>");
        jButtonLoadOffer.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonLoadOffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadOfferActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonLoadOffer);

        jButtonSaldo.setText("<html> <div style=\"text-align: center\"> Saldo </div> </html>");
        jButtonSaldo.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonSaldo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaldoActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonSaldo);

        jButtonEventPrices.setText("<html> <div style=\"text-align: center\"> Event <br> cijene </div> </html>");
        jButtonEventPrices.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEventPrices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEventPricesActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonEventPrices);

        jButtonOpenCashRegister.setText("<html> <div style=\"text-align: center\"> Otvori <br> ladicu </div> </html>");
        jButtonOpenCashRegister.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonOpenCashRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenCashRegisterActionPerformed(evt);
            }
        });
        jPanel15.add(jButtonOpenCashRegister);

        jButtonInvoiceDiscount.setBackground(new java.awt.Color(0, 102, 204));
        jButtonInvoiceDiscount.setText("<html> <div style=\"text-align: center\"> Popust <br> na račun </div> </html>");
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
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(jButtonCash, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
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
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabelR1, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23)
                        .addComponent(jLabelSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
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
                .addGap(0, 0, 0)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
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

        jSplitPane2.setRightComponent(jPanel17);

        jButtonNapojnica.setText("Napojnica");
        jButtonNapojnica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNapojnicaActionPerformed(evt);
            }
        });

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
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonNapojnica, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonNapojnica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		if(jButtonListItems == null){
			return;
		}

		// Revert buttons
		for (int i = 0; i < jButtonListItems.length; ++i){
			for (int j = 0; j < jButtonListItems[i].length; ++j){
				jButtonListItems[i][j].setText("");
				jButtonListItems[i][j].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[0], jButtonListItems[i][j].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[i][j].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
			}
		}
		
		// Load new values
		int tabIndex = jTabbedPane1.getSelectedIndex();
		for (int i = 0; i < itemButtonId[selectedGroupIndex][tabIndex].length; ++i){
			int itemId = itemButtonId[selectedGroupIndex][tabIndex][i];
			if(itemId != -1){
				int itemType = itemButtonType[selectedGroupIndex][tabIndex][i];
				int colorIndex = itemButtonColor[selectedGroupIndex][tabIndex][i];
				String name = "";
				for(WarehouseItem warehouseItem : warehouseItems){
					if(warehouseItem.itemId == itemId && warehouseItem.itemType == itemType){
						name = warehouseItem.itemName;
						break;
					}
				}
				
				if("".equals(name))
					continue;
				
				ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][i], name);
				jButtonListItems[tabIndex][i].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[colorIndex], jButtonListItems[tabIndex][i].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][i].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
			}
		}
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButtonNapojnicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNapojnicaActionPerformed
        //ClientAppLogger.GetInstance().ShowMessage("Ova funkcionalnost je u izradi.");
        //return;

        ClientAppTipInvoicesDialog dialog = new ClientAppTipInvoicesDialog(null, true);
        final JDialog loadingDialog = new LoadingDialog(null, true);

        //String zki_if_not = "ALTER TABLE USER1.INVOICES ADD COLUMN NAP_ZKI VARCHAR(50)";
        // String jir_if_not = "ALTER TABLE USER1.INVOICES ADD COLUMN NAP_JIR VARCHAR(50)";

        //boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
        //if(!isProduction){
            //        zki_if_not = zki_if_not.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            //        jir_if_not = jir_if_not.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            // }

        //String iznos_napojnice_if_not = "ALTER TABLE USER1.LOCAL_INVOICES ADD COLUMN IZNOS_NAPOJNICE VARCHAR(50)";
        //String tip_placanja_if_not = "ALTER TABLE USER1.LOCAL_INVOICES ADD COLUMN TIP_PLACANJA VARCHAR(50)";

        //boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
        //if(!isProduction){
            //        iznos_napojnice_if_not = iznos_napojnice_if_not.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            //        tip_placanja_if_not = tip_placanja_if_not.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            // }

        // DatabaseQuery databaseQuery2 = new DatabaseQuery(iznos_napojnice_if_not);
        // ServerQueryTask databaseQueryTask2 = new ServerQueryTask(loadingDialog, databaseQuery2, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        // databaseQueryTask2.execute();

        // DatabaseQuery databaseQuery3 = new DatabaseQuery(tip_placanja_if_not);
        // ServerQueryTask databaseQueryTask3 = new ServerQueryTask(loadingDialog, databaseQuery3, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        // databaseQueryTask3.execute();

        //  DatabaseQuery databaseQuery4 = new DatabaseQuery(zki_if_not);
        //  ServerQueryTask databaseQueryTask4 = new ServerQueryTask(loadingDialog, databaseQuery4, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        //  databaseQueryTask4.execute();

        //  DatabaseQuery databaseQuery5 = new DatabaseQuery(jir_if_not);
        //  ServerQueryTask databaseQueryTask5 = new ServerQueryTask(loadingDialog, databaseQuery5, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        //  databaseQueryTask5.execute();

        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonNapojnicaActionPerformed

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

    private void jButtonOpenCashRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenCashRegisterActionPerformed

    }//GEN-LAST:event_jButtonOpenCashRegisterActionPerformed

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

    private void jButtonSaldoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaldoActionPerformed
        new ClientAppCashRegisterSaldoDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonSaldoActionPerformed

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

    private void jButtonStaffUserChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStaffUserChangeActionPerformed
        ClientAppLoginPasswordOnlyDialog dialog = new ClientAppLoginPasswordOnlyDialog(null, true, true, -1);
        dialog.setVisible(true);
        if(dialog.loginSuccess){
            UpdateCurrentUser();
            SaveTable(true);
        }
    }//GEN-LAST:event_jButtonStaffUserChangeActionPerformed

    private void jButtonInvoiceR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceR1ActionPerformed
        ClientAppSelectClientDialog selectDialog = new ClientAppSelectClientDialog(null, true);
        selectDialog.setVisible(true);

        final JDialog loadingDialog = new LoadingDialog(null, true);

        String query = "SELECT CLIENTS.HOUSE_NUM, CLIENTS.STREET, CLIENTS.TOWN, CLIENTS.PAYMENT_DELAY, CLIENTS.DISCOUNT, CLIENTS.LOYALTY_CARD "
        + "FROM CLIENTS "
        + "WHERE CLIENTS.OIB = ?";

        ClientAppLogger.GetInstance().LogMessage("Query is: " + query);
        ClientAppLogger.GetInstance().LogMessage("Param is: " + selectDialog.selectedClientOIB);

        Invoice selectedInvoiceToChange = selectDialog.selectedInvoice;

        DatabaseQuery databaseQuery = new DatabaseQuery(query);
        databaseQuery.AddParam(1, selectDialog.selectedClientOIB);
        ClientAppLogger.GetInstance().LogMessage("Unešen OIB je: " + selectDialog.selectedClientOIB);

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

        ClientAppLogger.GetInstance().LogMessage("Client ID: " + selectDialog.selectedId);
        invoice.clientId = selectDialog.selectedId;
        invoice.clientName = selectDialog.selectedClientName;
        invoice.clientOIB = selectDialog.selectedClientOIB;
        ClientAppLogger.GetInstance().LogMessage("Spremljen OIB je: " + selectDialog.selectedClientOIB);
        invoice.paymentDelay = selectDialog.selectedClientPaymentDelay;

        if (rowData[1].toString().length() < 5){
            invoice.note =  ", ";
        }
        else {
            invoice.note = rowData[1] + " " + rowData[0] + ", " + rowData[2];
        }

        if(invoice.clientId == -1){
            jLabelR1.setText("");
        } else {
            jLabelR1.setText("R1: " + selectDialog.selectedClientName + ", OIB: " + selectDialog.selectedClientOIB + ""
                + ", Adresa " + street + " " + houseNum + ", Grad: " + town);
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

        selectedInvoiceToChange.clientId = invoice.clientId;
        selectedInvoiceToChange.clientName = invoice.clientName;
        selectedInvoiceToChange.clientOIB = invoice.clientOIB;
        selectedInvoiceToChange.paymentDelay = invoice.paymentDelay;
        selectedInvoiceToChange.note = invoice.note;

        if (selectedInvoiceToChange != null){
            jLabelR1.setText("");
            ClientAppLogger.GetInstance().LogMessage("Idem printati sa OIBom: " + selectDialog.selectedClientOIB);
            PrintUtils.PrintPosInvoice(selectedInvoiceToChange, Values.POS_PRINTER_TYPE_INVOICE);
            ClientAppLogger.GetInstance().LogMessage("Invoice name before update: " + invoice.clientName);
            ClientAppLogger.GetInstance().LogMessage("Invoice oib before update: " + invoice.clientOIB);
            ClientAppLogger.GetInstance().LogMessage("Invoice id before update: " + invoice.clientId);

            UpdateCurrentInvoiceData(invoice);

            ClientAppLogger.GetInstance().LogMessage("Invoice name after update: " + invoice.clientName);
            ClientAppLogger.GetInstance().LogMessage("Invoice oib after update: " + invoice.clientOIB);
            ClientAppLogger.GetInstance().LogMessage("Invoice id after update: " + invoice.clientId);
        }

        UpdateTotalPrice(true);
        SaveTable();
        //  LoadTable();
    }//GEN-LAST:event_jButtonInvoiceR1ActionPerformed

    private void jButtonSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSubtotalActionPerformed
        if(invoice.isSubtotal){
            ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
            return;
        }

        OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_SUBTOTAL);

        invoice.specialZki = invoice.zki;
        invoice.specialJir = invoice.jir;
    }//GEN-LAST:event_jButtonSubtotalActionPerformed

    private void jButtonTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTablesActionPerformed
        ClientAppCashRegisterTablesDialog dialog = new ClientAppCashRegisterTablesDialog(null, true, currentTableId, startCashRegisterNumber, invoice.items.isEmpty());
        dialog.setVisible(true);
        int oldCurrentTableId = currentTableId;
        currentTableId = dialog.selectedTableId;
        if(oldCurrentTableId == -1 && !invoice.items.isEmpty()){
            SaveTable();
        } else {
            LoadTable();
        }
        UpdateCurrentUser();

        jLabelCashRegisterName.setText("Kasa " + startCashRegisterNumber + (currentTableId != -1 ? " - Stol " + (currentTableId + 1) : ""));
    }//GEN-LAST:event_jButtonTablesActionPerformed

    private void jButtonTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTotalActionPerformed
        new ClientAppReportsTotalDialog(null, true, false).setVisible(true);
    }//GEN-LAST:event_jButtonTotalActionPerformed

    private void jButtonOfferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOfferActionPerformed
        if(invoice.isSubtotal){
            ClientAppLogger.GetInstance().ShowMessage("Nije moguće mijenjati stavke nakon prikaza stanja stola." + System.lineSeparator() + "Potrebno je izdati račun ili pobrisati stavke.");
            return;
        }

        OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_OFFER);
    }//GEN-LAST:event_jButtonOfferActionPerformed

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

    private void jButtonOtherPaymentMethodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOtherPaymentMethodsActionPerformed
        OnInvoiceDone(Values.PAYMENT_METHOD_ANY_METHOD);
    }//GEN-LAST:event_jButtonOtherPaymentMethodsActionPerformed

    private void jButtonCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCardActionPerformed
        OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_CREDIT_CARD);
    }//GEN-LAST:event_jButtonCardActionPerformed

    private void jButtonCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCashActionPerformed
        OnInvoiceDone(Values.PAYMENT_METHOD_TYPE_CASH);
        try {
            Thread.sleep(2000);

        } catch (InterruptedException ex) {
            Logger.getLogger(ClientAppCashRegisterTouchDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonCashActionPerformed

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

    private void jButtonInvoiceCancelationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvoiceCancelationActionPerformed
        if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
            ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
            return;
        }

        if (StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
            ClientAppLogger.GetInstance().ShowMessage("Nemate dovoljna prava za ovu akciju.");
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
        minusInvoice.paymentAmount2 *= -1f;
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

    private void jButtonLastInvoiceChangePaymentMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLastInvoiceChangePaymentMethodActionPerformed
        if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
            ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
            return;
        }

        if(HaveOldUnfiscalizedInvoices()){
            ClientAppLogger.GetInstance().ShowMessage("Upozorenje: u poslovnici postoje nefiskalizirani računi stariji od 48 sati. Izdavanje novih računa je onemogućeno!");
            return;
        }

        //if(lastInvoice == null){
            //	ClientAppLogger.GetInstance().ShowMessage("Od ulaska u kasu nema izdanih računa.");
            //	return;
            //}

        ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{}, false, 0f);
        dialog.setVisible(true);
        if(dialog.selectedPaymentMethodType == -1){
            return;
        }

        Invoice minusInvoice; Invoice newInvoice;

        if (lastInvoice != null && dialog.textPastInvoiceNotEntered){
            minusInvoice = new Invoice(lastInvoice);
            minusInvoice.note = "Storno računa " + lastInvoice.invoiceNumber + "/" + lastInvoice.officeTag + "/" + lastInvoice.cashRegisterNumber;
        }
        else {
            minusInvoice = new Invoice(dialog.selectedInvoice);
            minusInvoice.note = "Storno računa " + dialog.selectedInvoice.invoiceNumber + "/" + dialog.selectedInvoice.officeTag + "/" + dialog.selectedInvoice.cashRegisterNumber;
        }

        UpdateCurrentInvoiceData(minusInvoice);
        minusInvoice.isCopy = false;
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

        if (lastInvoice != null){
            newInvoice = new Invoice(lastInvoice);
        }
        else {
            newInvoice = new Invoice(dialog.selectedInvoice);
        }

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
    }//GEN-LAST:event_jButtonLastInvoiceChangePaymentMethodActionPerformed

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
                Logger.getLogger(ClientAppCashRegisterTouchDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SaveTable();
    }//GEN-LAST:event_jButtonPrintBarActionPerformed

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
                Logger.getLogger(ClientAppCashRegisterTouchDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SaveTable();
    }//GEN-LAST:event_jButtonPrintKitchenActionPerformed

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
            discount = ClientAppUtils.FloatToPriceString(invoice.items.get(rowId).discountValue) + " eur/kom";
        }

        defaultTableModel.setValueAt(totalItemPrice, rowId, COLUMN_INDEX_TOTAL);
        defaultTableModel.setValueAt(discount, rowId, COLUMN_INDEX_DISCOUNT);

        UpdateTotalPrice(true);
    }//GEN-LAST:event_jButtonItemDiscountActionPerformed

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

                Object[] rowData = new Object[5];
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

    private void jButtonPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlusActionPerformed
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
    }//GEN-LAST:event_jButtonPlusActionPerformed

    private void jButtonMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMinusActionPerformed
        int rowId = GetSelectedItemIndex();
        if(rowId == -1){
            return;
        }

        if (StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
            ClientAppLogger.GetInstance().ShowMessage("Nemate dovoljna prava za ovu akciju.");
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
    }//GEN-LAST:event_jButtonMinusActionPerformed

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

        clientAppStaffRightsDialog = new
        ClientAppStaffRightsDialog(null, true, StaffUserInfo.GetCurrentUserInfo().userId, StaffUserInfo.GetCurrentUserInfo().firstName,
            StaffUserInfo.GetCurrentUserInfo().userOIB, "");

        if(!clientAppStaffRightsDialog.staffUserInfo.userRights[Values.STAFF_RIGHTS_CASHREGISTER_DELETE_ITEM]){
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

	private void OnInvoiceDone(int paymentMethodType){
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
                        return;
		}
		
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_DISABLE_INVOICE_CREATION.ordinal()) && paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			ClientAppLogger.GetInstance().ShowMessage("Na ovoj blagajni onemogućeno je izdavanje računa.");
                        return;
		}
		
		//if(HaveOldUnfiscalizedInvoices()){
		//	ClientAppLogger.GetInstance().ShowMessage("Upozorenje: u poslovnici postoje nefiskalizirani računi stariji od 48 sati. Izdavanje novih računa je onemogućeno!");
		//	return;
		//}
		
		if(invoice.items.size() == 0){
			ClientAppLogger.GetInstance().ShowMessage("Račun je prazan.");
			return;
		}
		
		for(int i = 0; i < invoice.items.size(); ++i){
			if(invoice.items.get(i).itemAmount == 0f && "".equals(invoice.items.get(i).itemNote)){
				ClientAppLogger.GetInstance().ShowMessage("Stavka ne može imati količinu 0.");
				return;
			}
		}
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN && isProduction){
			ClientAppLogger.GetInstance().ShowMessage("U produkcijskom okruženju nije moguće izdavati račune kao Admin.");
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
		
                //bilo komentirano prije 01.11.2023.
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
			Object[] rowData = new Object[5];
			
			if("".equals(invoice.items.get(i).itemNote)){
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
		
                ClientAppLogger.GetInstance().LogMessage("Current table id in SaveTable" + currentTableId);
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
                ClientAppLogger.GetInstance().LogMessage("Current table id is: " + currentTableId);
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
                                                ClientAppLogger.GetInstance().LogMessage("I have set new invoice");
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
                ClientAppLogger.GetInstance().LogMessage("Current table id in clear table if empty: " + currentTableId);
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton101;
    private javax.swing.JButton jButton102;
    private javax.swing.JButton jButton103;
    private javax.swing.JButton jButton104;
    private javax.swing.JButton jButton105;
    private javax.swing.JButton jButton106;
    private javax.swing.JButton jButton107;
    private javax.swing.JButton jButton108;
    private javax.swing.JButton jButton109;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton110;
    private javax.swing.JButton jButton111;
    private javax.swing.JButton jButton112;
    private javax.swing.JButton jButton113;
    private javax.swing.JButton jButton114;
    private javax.swing.JButton jButton115;
    private javax.swing.JButton jButton116;
    private javax.swing.JButton jButton117;
    private javax.swing.JButton jButton118;
    private javax.swing.JButton jButton119;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton120;
    private javax.swing.JButton jButton121;
    private javax.swing.JButton jButton122;
    private javax.swing.JButton jButton123;
    private javax.swing.JButton jButton124;
    private javax.swing.JButton jButton125;
    private javax.swing.JButton jButton126;
    private javax.swing.JButton jButton127;
    private javax.swing.JButton jButton128;
    private javax.swing.JButton jButton129;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton130;
    private javax.swing.JButton jButton131;
    private javax.swing.JButton jButton132;
    private javax.swing.JButton jButton133;
    private javax.swing.JButton jButton134;
    private javax.swing.JButton jButton135;
    private javax.swing.JButton jButton136;
    private javax.swing.JButton jButton137;
    private javax.swing.JButton jButton138;
    private javax.swing.JButton jButton139;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton140;
    private javax.swing.JButton jButton141;
    private javax.swing.JButton jButton142;
    private javax.swing.JButton jButton143;
    private javax.swing.JButton jButton144;
    private javax.swing.JButton jButton145;
    private javax.swing.JButton jButton146;
    private javax.swing.JButton jButton147;
    private javax.swing.JButton jButton148;
    private javax.swing.JButton jButton149;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton150;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButton95;
    private javax.swing.JButton jButton96;
    private javax.swing.JButton jButton97;
    private javax.swing.JButton jButton98;
    private javax.swing.JButton jButton99;
    private javax.swing.JButton jButtonCard;
    private javax.swing.JButton jButtonCash;
    private javax.swing.JButton jButtonDelete;
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
    private javax.swing.JButton jButtonMinus;
    private javax.swing.JButton jButtonNapojnica;
    private javax.swing.JButton jButtonOffer;
    private javax.swing.JButton jButtonOpenCashRegister;
    private javax.swing.JButton jButtonOtherPaymentMethods;
    private javax.swing.JButton jButtonPlus;
    private javax.swing.JButton jButtonPrintBar;
    private javax.swing.JButton jButtonPrintKitchen;
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
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
