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
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.cashregister.ClientAppSelectPaymentMethodDialog;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReportsIssueSlipsMergeDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableInvoicesIdList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesDiscountPercentageList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesDiscountAmountList = new ArrayList<>();
	private ArrayList<Float> tableInvoicesFinalPriceList = new ArrayList<>();
	private ArrayList<String> tableInvoicesNoteList = new ArrayList<>();
	private ArrayList<Integer> tableInvoicesClientIdList = new ArrayList<>();
	private ArrayList<Integer> tableInvoicesInvoiceNumberList = new ArrayList<>();
	private ArrayList<InvoiceItem> invoiceItemsList = new ArrayList<>();
	
	private boolean setupDone;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppReportsIssueSlipsMergeDialog(java.awt.Frame parent, boolean modal) {
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
		
		jTableInvoices.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				int rowId = jTableInvoices.convertRowIndexToModel(jTableInvoices.getSelectedRow());
				
				int clientId = tableInvoicesClientIdList.get(rowId);
				for (int i = 0; i < jTableInvoices.getModel().getRowCount(); ++i){
					boolean value = (Boolean) jTableInvoices.getModel().getValueAt(i, 0);
					if(value && clientId != tableInvoicesClientIdList.get(i)){
						ClientAppLogger.GetInstance().ShowMessage("Nije moguće spojiti izdatnice različitih klijenata.");
						return;
					}
				}
				
				if (rowId != -1) {
					boolean value = (Boolean) jTableInvoices.getModel().getValueAt(rowId, 0);
					jTableInvoices.getModel().setValueAt(!value, rowId, 0);
					RefreshTableItems();
				}
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
		
		SetDefaultTableItems();
		
		setupDone = true;
		RefreshTable();
	}
	
	private void SetDefaultTableItems(){
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneItems.getWidth() * 25 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
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
		
		String query = "SELECT INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, C_ID, DIS_PCT, DIS_AMT, FIN_PR, NOTE, "
				+ "STAFF.FIRST_NAME, STAFF.LAST_NAME, CLIENTS.NAME "
				+ "FROM INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
				+ "LEFT OUTER JOIN CLIENTS ON CLIENTS.ID = INVOICES.C_ID "
				+ "WHERE O_NUM = ? "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND INVOICES.PAY_TYPE = ? AND INVOICES.PAY_NAME <> ? "
				+ "ORDER BY CR_NUM, SPEC_NUM ";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			query = query.replace("INVOICES", "INVOICES_TEST");
		}
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
		multiDatabaseQuery.SetQuery(0, query);
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(0, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(0, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(0, 4, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
		multiDatabaseQuery.AddParam(0, 5, Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME);
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
					CustomTableModel customTableModel = new CustomTableModel(){
						@Override
						public Class getColumnClass(int column) {
							switch (column) {
								case 0:
									return Boolean.class;
								default:
									return String.class;
							}
						}
					};
					customTableModel.setColumnIdentifiers(new String[] {"Odabir", "Datum", "Br. izdatnice", "Klijent", "Djelatnik", "Ukupno"});
					
					ArrayList<Integer> idListInvoices = new ArrayList<>();
					ArrayList<Float> discountPercentageListInvoices = new ArrayList<>();
					ArrayList<Float> discountAmountListInvoices = new ArrayList<>();
					ArrayList<Float> finalPriceListInvoices = new ArrayList<>();
					ArrayList<Integer> clientListInvoices = new ArrayList<>();
					ArrayList<Integer> iNumListInvoices = new ArrayList<>();
					ArrayList<String> noteListInvoices = new ArrayList<>();
					
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[6];
						rowData[0] = false;
						String dateString = databaseQueryResult[0].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[1] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[0].getString(4);
						rowData[2] = databaseQueryResult[0].getString(2);
						rowData[3] = databaseQueryResult[0].getString(13);
						rowData[4] = databaseQueryResult[0].getString(11) + " " + databaseQueryResult[0].getString(12);
						float totalPrice = databaseQueryResult[0].getFloat(9) * (100f - databaseQueryResult[0].getFloat(7)) / 100f - databaseQueryResult[0].getFloat(8);
						rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice);
						customTableModel.addRow(rowData);
						
						idListInvoices.add(databaseQueryResult[0].getInt(0));
						discountPercentageListInvoices.add(databaseQueryResult[0].getFloat(7));
						discountAmountListInvoices.add(databaseQueryResult[0].getFloat(8));
						finalPriceListInvoices.add(databaseQueryResult[0].getFloat(9));
						noteListInvoices.add(databaseQueryResult[0].getString(10));
						clientListInvoices.add(databaseQueryResult[0].getInt(6));
						iNumListInvoices.add(databaseQueryResult[0].getInt(2));
					}
					
					jTableInvoices.setModel(customTableModel);
					tableInvoicesIdList = idListInvoices;
					tableInvoicesDiscountPercentageList = discountPercentageListInvoices;
					tableInvoicesDiscountAmountList = discountAmountListInvoices;
					tableInvoicesFinalPriceList = finalPriceListInvoices;
					tableInvoicesClientIdList = clientListInvoices;
					tableInvoicesInvoiceNumberList = iNumListInvoices;
					tableInvoicesNoteList = noteListInvoices;
					
					jTableInvoices.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneInvoices.getWidth() * 10 / 100);
					jTableInvoices.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneInvoices.getWidth() * 26 / 100);
					jTableInvoices.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneInvoices.getWidth() * 16 / 100);
					jTableInvoices.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneInvoices.getWidth() * 16 / 100);
					jTableInvoices.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneInvoices.getWidth() * 18 / 100);
					jTableInvoices.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneInvoices.getWidth() * 14 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableItems(){
		float totalPriceSum = 0f;
		float totalDiscountSum = 0f;
		String invoiceIds = "";
		for (int i = 0; i < jTableInvoices.getModel().getRowCount(); ++i){
			boolean value = (boolean) jTableInvoices.getModel().getValueAt(i, 0);
			if(value){
				totalPriceSum += tableInvoicesFinalPriceList.get(i)* (100f - tableInvoicesDiscountPercentageList.get(i)) / 100f - tableInvoicesDiscountAmountList.get(i);
				totalDiscountSum += tableInvoicesFinalPriceList.get(i) * tableInvoicesDiscountPercentageList.get(i) / 100f + tableInvoicesDiscountAmountList.get(i);
				if("".equals(invoiceIds)){
					invoiceIds += tableInvoicesIdList.get(i);
				} else {
					invoiceIds += ", " + tableInvoicesIdList.get(i);
				}
			}
		}
		
		jLabelTotal.setText(ClientAppUtils.FloatToPriceString(totalPriceSum));
		String discountSumString = "Popust na račun:  -";
		if(totalDiscountSum != 0f){
			discountSumString = "Popust na račun:  " + ClientAppUtils.FloatToPriceString(totalDiscountSum) + " kn";
		}
		jLabelDiscount.setText(discountSumString);
				
		if("".equals(invoiceIds)){
			SetDefaultTableItems();
			return;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT IT_ID, IT_NAME, AMT, PR, DIS_PCT, DIS_AMT, IT_TYPE, TAX, C_TAX, PACK_REF "
				+ "FROM INVOICE_ITEMS "
				+ "WHERE IN_ID IN (" + invoiceIds + ")";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			query = query.replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
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
					invoiceItemsList = new ArrayList<>();
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
					while (databaseQueryResult.next()) {
						InvoiceItem invoiceItem = new InvoiceItem();
						invoiceItem.itemId = databaseQueryResult.getInt(0);
						invoiceItem.itemName = databaseQueryResult.getString(1);
						invoiceItem.itemAmount = databaseQueryResult.getFloat(2);
						invoiceItem.itemPrice = databaseQueryResult.getFloat(3);
						invoiceItem.discountPercentage = databaseQueryResult.getFloat(4);
						invoiceItem.discountValue = databaseQueryResult.getFloat(5);
						invoiceItem.itemType = databaseQueryResult.getInt(6);
						invoiceItem.taxRate = databaseQueryResult.getFloat(7);
						invoiceItem.consumptionTaxRate = databaseQueryResult.getFloat(8);
						invoiceItem.packagingRefund = databaseQueryResult.getFloat(9);
						
						boolean itemFound = false;
						for	(int i = 0; i < invoiceItemsList.size(); ++i){
							if(invoiceItemsList.get(i).itemId == invoiceItem.itemId && invoiceItemsList.get(i).itemType == invoiceItem.itemType
									&& invoiceItemsList.get(i).itemPrice == invoiceItem.itemPrice
									&& invoiceItemsList.get(i).discountPercentage == invoiceItem.discountPercentage && invoiceItemsList.get(i).discountValue == invoiceItem.discountValue
									&& invoiceItemsList.get(i).taxRate == invoiceItem.taxRate && invoiceItemsList.get(i).consumptionTaxRate == invoiceItem.consumptionTaxRate
									&& invoiceItemsList.get(i).itemName.equals(invoiceItem.itemName)){
								itemFound = true;
								invoiceItemsList.get(i).itemAmount += invoiceItem.itemAmount;
								break;
							}
						}
						
						if(!itemFound){
							invoiceItemsList.add(invoiceItem);
						}
					}
					
					for	(int i = 0; i < invoiceItemsList.size(); ++i){
						Object[] rowData = new Object[6];
						rowData[0] = invoiceItemsList.get(i).itemId;
						rowData[1] = invoiceItemsList.get(i).itemName;
						rowData[2] = invoiceItemsList.get(i).itemAmount;
						rowData[3] = ClientAppUtils.FloatToPriceString(invoiceItemsList.get(i).itemPrice);
						String discount = "";
						float totalPrice = invoiceItemsList.get(i).itemPrice * invoiceItemsList.get(i).itemAmount;
						if(invoiceItemsList.get(i).discountPercentage != 0f){
							discount = invoiceItemsList.get(i).discountPercentage + " %";
							totalPrice = totalPrice * (100f - invoiceItemsList.get(i).discountPercentage) / 100f;
						} else if(invoiceItemsList.get(i).discountValue != 0f){
							discount = ClientAppUtils.FloatToPriceFloat(invoiceItemsList.get(i).discountValue) + " kn/kom";
							totalPrice = totalPrice - invoiceItemsList.get(i).discountValue * invoiceItemsList.get(i).itemAmount;
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
        jButtonExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jButtonMergeIssueSlips = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

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
        jLabel9.setText("Spajanje izdatnica u račun");

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
                .addComponent(jScrollPaneInvoices, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Spojene stavke na računu"));

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

        jButtonMergeIssueSlips.setText("<html> <div style=\"text-align: center\"> Spoji izdatnice <br> u račun <br> [F3] </div> </html>");
        jButtonMergeIssueSlips.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonMergeIssueSlips.setPreferredSize(new java.awt.Dimension(90, 60));
        jButtonMergeIssueSlips.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMergeIssueSlipsActionPerformed(evt);
            }
        });

        jLabel8.setText("Filter:");

        jTextField1.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
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
                .addGap(68, 68, 68)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 310, Short.MAX_VALUE)
                .addComponent(jButtonMergeIssueSlips, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonMergeIssueSlips, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 522, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(jLabel9)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonMergeIssueSlipsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMergeIssueSlipsActionPerformed
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
			ClientAppLogger.GetInstance().ShowMessage("Samo vlasnik poslovnice može spajati izdatnice u račune.");
			return;
		}
		
		if(Licence.IsControlApp()){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće spajati izdatnice kroz kontrolnu aplikaciju");
			return;
		}
		
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
			return;
		}		
		
		// Get last invoice for data
		int lastInvoiceId = -1;
		for (int i = 0; i < jTableInvoices.getModel().getRowCount(); ++i){
			boolean value = (Boolean) jTableInvoices.getModel().getValueAt(i, 0);
			if(value){
				lastInvoiceId = tableInvoicesIdList.get(i);
			}
		}
		if(lastInvoiceId == -1){
			return;
		}
		Invoice lastInvoice = ClientAppUtils.GetInvoice(lastInvoiceId, false);
		if(lastInvoice == null){
			ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške tijekom spajanja izdatnica. Molimo pokušajte ponovno otvoriti ovaj prozor.");
			return;
		}
		
		// Generate invoice
		Invoice invoice = new Invoice(lastInvoice);
		invoice.items = invoiceItemsList;
		invoice.discountPercentage = 0f;
		invoice.cashRegisterNumber = Licence.GetCashRegisterNumber();
		invoice.date = new Date();
		invoice.staffId = StaffUserInfo.GetCurrentUserInfo().userId;
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_STUDENT){
			invoice.staffOib = Licence.GetOIB();
		} else {
			invoice.staffOib = StaffUserInfo.GetCurrentUserInfo().userOIB;
		}
		invoice.staffName = StaffUserInfo.GetCurrentUserInfo().firstName;
		invoice.zki = Values.DEFAULT_ZKI;
		invoice.jir = Values.DEFAULT_JIR;
		invoice.isCopy = false;
		invoice.isInVatSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		// Get price data
		float totalPriceSum = 0f;
		float totalDiscountSum = 0f;
		String invoiceNumbers = "";
		String invoiceIds = "";
		String officeTag = Licence.GetOfficeTag();
		for (int i = 0; i < jTableInvoices.getModel().getRowCount(); ++i){
			boolean value = (boolean) jTableInvoices.getModel().getValueAt(i, 0);
			if(value){
				totalPriceSum += tableInvoicesFinalPriceList.get(i);
				totalDiscountSum += tableInvoicesFinalPriceList.get(i) * tableInvoicesDiscountPercentageList.get(i) / 100f + tableInvoicesDiscountAmountList.get(i);
				if("".equals(invoiceNumbers)){
					invoiceIds += tableInvoicesIdList.get(i);
					invoiceNumbers += tableInvoicesInvoiceNumberList.get(i) + "/" + officeTag;
				} else {
					invoiceIds += ", " + tableInvoicesIdList.get(i);
					invoiceNumbers += ", " + tableInvoicesInvoiceNumberList.get(i) + "/" + officeTag;
				}
			}
		}
		invoice.totalPrice = totalPriceSum;
		invoice.discountValue = totalDiscountSum;
		invoice.note = "Račun kreiran prema izdatnicama: " + invoiceNumbers;
		
		// Select payment method
		ClientAppSelectPaymentMethodDialog dialog = new ClientAppSelectPaymentMethodDialog(null, true, new int[]{}, false, 0f);
		dialog.setVisible(true);
		if(dialog.selectedPaymentMethodType == -1){
			return;
		}
		if(dialog.selectedPaymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP){
			ClientAppLogger.GetInstance().ShowMessage("Kao način plaćanja nije moguće odabrati izdatnicu.");
			return;
		}
		invoice.paymentMethodName = dialog.selectedPaymentMethodName;
		invoice.paymentMethodType = dialog.selectedPaymentMethodType;
		invoice.paymentMethodName2 = dialog.selectedPaymentMethodName2;
		invoice.paymentMethodType2 = dialog.selectedPaymentMethodType2;
		if (invoice.discountValue != 0f){
			invoice.paymentAmount2 = dialog.paymentAmount2 + invoice.discountValue;
		} else if (invoice.discountPercentage != 0f){
			invoice.paymentAmount2 = dialog.paymentAmount2 * 100f / (100f - invoice.discountPercentage);
		} else {
			invoice.paymentAmount2 = dialog.paymentAmount2;
		}
		
		// Exit if licence invalid
		if(invoice.officeNumber == 0 || invoice.cashRegisterNumber == 0 || invoice.staffOib == null || invoice.officeTag == null){
			ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške kod čitanja licence. Molimo ponovno pokrenite program.");
			Utils.DisposeDialog(this);
			return;
		}
		
		// Delete issue slips from this invoice
		boolean deleteIssueSlipsSuccess = false;
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryUpdate = "UPDATE INVOICES SET PAY_NAME = ? WHERE ID IN (" + invoiceIds + ")";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryUpdate = queryUpdate.replace("INVOICES", "INVOICES_TEST");
		}
		DatabaseQuery databaseQuery = new DatabaseQuery(queryUpdate);
		databaseQuery.AddParam(1, Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME);
		
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
					deleteIssueSlipsSuccess = true;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		if(!deleteIssueSlipsSuccess){
			ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
			return;
		}
		
		// Generate invoice
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
		} else {
			// Insert non-fiscalization invoice
			boolean invoiceInsertSuccess = ClientAppUtils.InsertLocalInvoice(invoice);
			if(!invoiceInsertSuccess){
				ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
				return;
			}
		}
		
		PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
		
		ClientAppLogger.GetInstance().ShowMessage("Izdatnice uspješno spojene u račun!" + System.lineSeparator() + "Račun u A4 formatu možete isprintati u prozoru Izvještaji/Računi.");
		invoiceItemsList = new ArrayList<>();
		RefreshTable();
    }//GEN-LAST:event_jButtonMergeIssueSlipsActionPerformed

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
        String searchString = jTextField1.getText();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableInvoices.getModel());
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
        jTableInvoices.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonMergeIssueSlips;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneInvoices;
    private javax.swing.JScrollPane jScrollPaneItems;
    private javax.swing.JTable jTableInvoices;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTextField jTextField1;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
