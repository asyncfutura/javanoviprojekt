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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReportsOffersDialog extends javax.swing.JDialog {
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
        private String receiptNoteOnA4 = "";

	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppReportsOffersDialog(java.awt.Frame parent, boolean modal) {
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
						jButtonPrintPosOffersTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4OffersTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPrintPosOffer.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonPrintA4Offer.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonPrintPosAllOffers.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F9){
						jButtonPrintA4AllOffers.doClick();
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
		
		jTableOffers.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableOffers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableOffers.getTableHeader().setReorderingAllowed(false);
		jTableOffers.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneItems.getWidth() * 25 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneItems.getWidth() * 15 / 100);
		
		jTableOffers.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableOffers.getSelectedRow() == -1)
					return;
				
				int rowId = jTableOffers.convertRowIndexToModel(jTableOffers.getSelectedRow());
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
				
				String discount = "Popust na ponudu:  -";
				if(discountPercentage != 0f){
					discount = "Popust na ponudu:  " + discountPercentage + "% = " + ClientAppUtils.FloatToPriceString((finalPrice * discountPercentage) / 100f) + " kn";
					finalPrice = finalPrice * (100f - discountPercentage) / 100f;
				} else if(discountAmount != 0f){
					discount = "Popust na ponudu:  " + ClientAppUtils.FloatToPriceString(discountAmount) + " kn";
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
		
		String queryLocal = "SELECT LOCAL_INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(LOCAL_INVOICE_ITEMS.AMT) "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				+ "INNER JOIN LOCAL_INVOICE_ITEMS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND PAY_TYPE = ? "
				+ "GROUP BY LOCAL_INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, LOCAL_INVOICES.DIS_PCT, "
				+ "LOCAL_INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR "
				+ "ORDER BY CR_NUM, SPEC_NUM";
		String query = "SELECT INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, INVOICES.DIS_PCT, "
				+ "INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR, MIN(INVOICE_ITEMS.AMT) "
				+ "FROM INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = INVOICES.S_ID "
				+ "INNER JOIN INVOICE_ITEMS ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
				+ "WHERE O_NUM = ? "
				+ "AND I_DATE >= ? AND I_DATE <= ? AND PAY_TYPE = ? "
				+ "GROUP BY INVOICES.ID, CR_NUM, SPEC_NUM, I_DATE, I_TIME, S_ID, PAY_NAME, PAY_TYPE, C_ID, INVOICES.DIS_PCT, "
				+ "INVOICES.DIS_AMT, FIN_PR, NOTE, STAFF.FIRST_NAME, STAFF.LAST_NAME, JIR "
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
		multiDatabaseQuery.AddParam(0, 4, Values.PAYMENT_METHOD_TYPE_OFFER);
		multiDatabaseQuery.SetQuery(1, query);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
		multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
		multiDatabaseQuery.AddParam(1, 4, Values.PAYMENT_METHOD_TYPE_OFFER);
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Datum", "Br. ponude", "Djelatnik", "Nač. plać.", "Tip ponude", "Ukupno"});
					
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
					
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[6];
						String dateString = databaseQueryResult[0].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[0].getString(4);
						rowData[1] = databaseQueryResult[0].getString(2) + "/" + Licence.GetOfficeTag() + "/" + databaseQueryResult[0].getString(1);
						rowData[2] = databaseQueryResult[0].getString(13) + " " + databaseQueryResult[0].getString(14);
						rowData[3] = databaseQueryResult[0].getString(6);
						rowData[4] = databaseQueryResult[0].getInt(8) == -1 ? "Običan" : "R1";
						float totalPrice = databaseQueryResult[0].getFloat(11) * (100f - databaseQueryResult[0].getFloat(9)) / 100f - databaseQueryResult[0].getFloat(10);
						rowData[5] = "** " + ClientAppUtils.FloatToPriceString(totalPrice) + " **";
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
					}
					
					while (databaseQueryResult[1].next()) {
						Object[] rowData = new Object[6];
						String dateString = databaseQueryResult[1].getString(3);
						Date dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(dateDate) + " " + databaseQueryResult[1].getString(4);
						rowData[1] = databaseQueryResult[1].getString(2) + "/" + Licence.GetOfficeTag() + "/" + databaseQueryResult[1].getString(1);
						rowData[2] = databaseQueryResult[1].getString(13) + " " + databaseQueryResult[1].getString(14);
						rowData[3] = databaseQueryResult[1].getString(6);
						rowData[4] = databaseQueryResult[1].getInt(8) == -1 ? "Običan" : "R1";
						float totalPrice = databaseQueryResult[1].getFloat(11) * (100f - databaseQueryResult[1].getFloat(9)) / 100f - databaseQueryResult[1].getFloat(10);
						rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice);
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
					}
					
					jTableOffers.setModel(customTableModel);
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
					
					jTableOffers.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneOffers.getWidth() * 24 / 100);
					jTableOffers.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneOffers.getWidth() * 14 / 100);
					jTableOffers.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneOffers.getWidth() * 17 / 100);
					jTableOffers.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneOffers.getWidth() * 13 / 100);
					jTableOffers.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneOffers.getWidth() * 14 / 100);
					jTableOffers.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneOffers.getWidth() * 12 / 100);
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
        jScrollPaneOffers = new javax.swing.JScrollPane();
        jTableOffers = new javax.swing.JTable();
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
        jButtonPrintPosOffersTable = new javax.swing.JButton();
        jButtonPrintA4OffersTable = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonPrintPosOffer = new javax.swing.JButton();
        jButtonPrintA4Offer = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jButtonPrintPosAllOffers = new javax.swing.JButton();
        jButtonPrintA4AllOffers = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ponude");
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
        jLabel9.setText("Ponude");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Ponude"));

        jTableOffers.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneOffers.setViewportView(jTableOffers);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOffers)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOffers, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke na ponudi"));

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

        jButtonPrintPosOffersTable.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> lista ponuda <br> [F4] </div> </html>");
        jButtonPrintPosOffersTable.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosOffersTable.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintPosOffersTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosOffersTableActionPerformed(evt);
            }
        });

        jButtonPrintA4OffersTable.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> lista ponuda <br> [F5] </div> </html>");
        jButtonPrintA4OffersTable.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4OffersTable.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintA4OffersTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4OffersTableActionPerformed(evt);
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

        jButtonPrintPosOffer.setText("<html> <div style=\"text-align: center\"> Ispis POS <br>ponuda <br>  [F6] </div> </html>");
        jButtonPrintPosOffer.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosOffer.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintPosOffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosOfferActionPerformed(evt);
            }
        });

        jButtonPrintA4Offer.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> ponuda <br> [F7] </div> </html>");
        jButtonPrintA4Offer.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4Offer.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintA4Offer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4OfferActionPerformed(evt);
            }
        });

        jLabel1.setText("Od [F1]:");
        jLabel1.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(45, 14));

        jButtonPrintPosAllOffers.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> sve ponude <br>  [F8] </div> </html>");
        jButtonPrintPosAllOffers.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintPosAllOffers.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintPosAllOffers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosAllOffersActionPerformed(evt);
            }
        });

        jButtonPrintA4AllOffers.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> sve ponude <br> [F9] </div> </html>");
        jButtonPrintA4AllOffers.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintA4AllOffers.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintA4AllOffers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4AllOffersActionPerformed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                .addComponent(jButtonPrintPosOffersTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4OffersTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosOffer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4Offer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosAllOffers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4AllOffers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonPrintA4Offer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonPrintPosOffersTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonPrintA4OffersTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButtonPrintPosOffer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButtonPrintA4AllOffers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintPosAllOffers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGap(197, 197, 197)
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
                        .addGap(15, 15, 15)
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

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosOffersTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosOffersTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintPosTable("Lista ponuda", jTableOffers, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5}}, printTableExtraData);
    }//GEN-LAST:event_jButtonPrintPosOffersTableActionPerformed

    private void jButtonPrintA4OffersTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4OffersTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
		
		PrintUtils.PrintA4Table("ListaPonuda", "Lista ponuda", jTableOffers, new int[]{0, 1, 2, 3, 4, 5}, new int[]{}, printTableExtraData, "");
    }//GEN-LAST:event_jButtonPrintA4OffersTableActionPerformed

    private void jButtonPrintPosOfferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosOfferActionPerformed
		if(jTableOffers.getSelectedRow() == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite ponudu u tablici koji želite ispisati.");
			return;
		}
		
		int rowId = jTableOffers.convertRowIndexToModel(jTableOffers.getSelectedRow());
		int tableId = tableInvoicesIdList.get(rowId);
		boolean isInvoiceLocal = false;
		if(tableId == -1){
			tableId = tableLocalInvoicesIdList.get(rowId);
			isInvoiceLocal = true;
		}
		
		Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
		
		PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
    }//GEN-LAST:event_jButtonPrintPosOfferActionPerformed

    private void jButtonPrintA4OfferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4OfferActionPerformed
		if(jTableOffers.getSelectedRow() == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite ponudu u tablici koji želite ispisati.");
			return;
		}
		
		int rowId = jTableOffers.convertRowIndexToModel(jTableOffers.getSelectedRow());
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
		
		if(!"".equals(invoice.note)){
			printTableExtraData1.footerList.add(new Pair<>(" ", " "));
			printTableExtraData1.footerList.add(new Pair<>("Napomena: ", invoice.note));
		}
                		
		PrintTableExtraData printTableExtraData2 = new PrintTableExtraData();
		printTableExtraData2.headerList.add(new Pair<>("Razrada poreza:", ""));
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
		
		PrintUtils.PrintA4Table("Ponuda-" + invoice.specialNumber + "-" + invoice.officeTag + "-" + invoice.cashRegisterNumber, 
				new String[]{"Broj ponude: " + invoice.specialNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber, ""}, 
				new JTable[]{jTableItems, tempJTable}, 
				new int[][]{new int[]{ 1, 2, 3, 4, 5 }, new int[]{ 0, 1, 2 }}, 
				new int[][]{new int[]{ }, new int[]{ }}, 
				new PrintTableExtraData[]{ printTableExtraData1, printTableExtraData2 }, 
				new boolean[]{false, false},
                                "");
    }//GEN-LAST:event_jButtonPrintA4OfferActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    private void jButtonPrintPosAllOffersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosAllOffersActionPerformed
		for(int invoiceIndex = 0; invoiceIndex < jTableOffers.getRowCount(); ++invoiceIndex){
			int tableId = tableInvoicesIdList.get(invoiceIndex);
			boolean isInvoiceLocal = false;
			if(tableId == -1){
				tableId = tableLocalInvoicesIdList.get(invoiceIndex);
				isInvoiceLocal = true;
			}
			Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
			PrintUtils.PrintPosInvoice(invoice, Values.POS_PRINTER_TYPE_INVOICE);
		}
    }//GEN-LAST:event_jButtonPrintPosAllOffersActionPerformed

    private void jButtonPrintA4AllOffersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4AllOffersActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		JTable[] tempJTables = new JTable[jTableOffers.getRowCount() * 2];
		PrintTableExtraData[] extraData = new PrintTableExtraData[jTableOffers.getRowCount() * 2];
		String[] tableTitle = new String[jTableOffers.getRowCount() * 2];
		
		for (int invoiceIndex = 0; invoiceIndex < jTableOffers.getRowCount(); ++invoiceIndex){
			int tableId = tableInvoicesIdList.get(invoiceIndex);
			boolean isInvoiceLocal = false;
			if(tableId == -1){
				tableId = tableLocalInvoicesIdList.get(invoiceIndex);
				isInvoiceLocal = true;
			}
			Invoice invoice = ClientAppUtils.GetInvoice(tableId, isInvoiceLocal);
			
			tableTitle[2 * invoiceIndex] = "Broj ponude: " + invoice.specialNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber;
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
			if(!"".equals(invoice.note)){
				extraData[2 * invoiceIndex].footerList.add(new Pair<>(" ", " "));
				extraData[2 * invoiceIndex].footerList.add(new Pair<>("Napomena: ", invoice.note));
			}

			extraData[2 * invoiceIndex + 1] = new PrintTableExtraData();
			extraData[2 * invoiceIndex + 1].headerList.add(new Pair<>("Razrada poreza:", ""));
			
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
		
		PrintUtils.PrintA4Table("PonudeOd" + dateFromString + "Do" + dateToString, 
				tableTitle,
				tempJTables, 
				columnIndex,
				mergeIndex, 
				extraData, 
				newPageBoolean,
                                "");
    }//GEN-LAST:event_jButtonPrintA4AllOffersActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4AllOffers;
    private javax.swing.JButton jButtonPrintA4Offer;
    private javax.swing.JButton jButtonPrintA4OffersTable;
    private javax.swing.JButton jButtonPrintPosAllOffers;
    private javax.swing.JButton jButtonPrintPosOffer;
    private javax.swing.JButton jButtonPrintPosOffersTable;
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
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDiscount;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneItems;
    private javax.swing.JScrollPane jScrollPaneOffers;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTable jTableOffers;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
