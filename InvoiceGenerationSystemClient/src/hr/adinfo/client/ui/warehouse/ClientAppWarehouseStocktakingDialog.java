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
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.receipts.ClientAppReceiptsDialog;
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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseStocktakingDialog extends javax.swing.JDialog {
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppWarehouseStocktakingDialog(java.awt.Frame parent, boolean modal) {
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
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPos.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F2){
						jTabbedPane1.setSelectedIndex(0);
						jTableMaterials.requestFocusInWindow();
						if(jTableMaterials.getRowCount() > 0){
							jTableMaterials.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jTabbedPane1.setSelectedIndex(1);
						jTableTradingGoods.requestFocusInWindow();
						if(jTableTradingGoods.getRowCount() > 0){
							jTableTradingGoods.setRowSelectionInterval(0, 0);
						}
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonCreateReceipt.doClick();
					}
				}
				
				return false;
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

		RefreshTablesMaterialsTradingGoods();
		
		jTableMaterials.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableMaterials.getSelectedRow() == -1)
					return;
				
				jTableMaterials.setColumnSelectionInterval(4, 4);
			}
		});
		
		jTableMaterials.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int rowId = e.getFirstRow();
				if(rowId < 0 || rowId > jTableMaterials.getRowCount())
					return;
				
				// New amount
				String currentNewAmountString = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 4));
				float newNewAmountFloat = 0f;
				String newNewAmountString = "";
				try {
					newNewAmountFloat = Float.parseFloat(currentNewAmountString.replace(",", "."));
					newNewAmountString = Float.toString(newNewAmountFloat);
				} catch (NumberFormatException ex){}
				
				if(!newNewAmountString.equals(currentNewAmountString)){
					jTableMaterials.getModel().setValueAt(newNewAmountString, rowId, 4);
				}
				
				// Diff
				String currentAmountString = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 3));
				String newDiffValueString = "";
				if(!"".equals(newNewAmountString)){
					float currentAmountFloat = 0f;
					try {
						currentAmountFloat = Float.parseFloat(currentAmountString);
					} catch (NumberFormatException ex){}
					newDiffValueString = Float.toString(newNewAmountFloat - currentAmountFloat);
				}
				
				String currentDiffValueString = String.valueOf(jTableMaterials.getModel().getValueAt(rowId, 5));
				if(!newDiffValueString.equals(currentDiffValueString)){
					jTableMaterials.getModel().setValueAt(newDiffValueString, rowId, 5);
				}
			}
		});
		
		jTableTradingGoods.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableTradingGoods.getSelectedRow() == -1)
					return;
				
				jTableTradingGoods.setColumnSelectionInterval(3, 3);
			}
		});
		
		jTableTradingGoods.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int rowId = e.getFirstRow();
				if(rowId < 0 || rowId > jTableTradingGoods.getRowCount())
					return;
				
				// New amount
				String currentNewAmountString = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 3));
				float newNewAmountFloat = 0f;
				String newNewAmountString = "";
				try {
					newNewAmountFloat = Float.parseFloat(currentNewAmountString.replace(",", "."));
					newNewAmountString = Float.toString(newNewAmountFloat);
				} catch (NumberFormatException ex){}
				
				if(!newNewAmountString.equals(currentNewAmountString)){
					jTableTradingGoods.getModel().setValueAt(newNewAmountString, rowId, 3);
				}
				
				// Diff
				String currentAmountString = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 2));
				String newDiffValueString = "";
				if(!"".equals(newNewAmountString)){
					float currentAmountFloat = 0f;
					try {
						currentAmountFloat = Float.parseFloat(currentAmountString);
					} catch (NumberFormatException ex){}
					newDiffValueString = Float.toString(newNewAmountFloat - currentAmountFloat);
				}
				
				String currentDiffValueString = String.valueOf(jTableTradingGoods.getModel().getValueAt(rowId, 4));
				if(!newDiffValueString.equals(currentDiffValueString)){
					jTableTradingGoods.getModel().setValueAt(newDiffValueString, rowId, 4);
				}
			}
		});
		
		jButtonCreateReceipt.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_OWNER || StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN);
		
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
	
	private void RefreshTablesMaterialsTradingGoods(){
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(Licence.GetOfficeNumber());
		ClientAppUtils.CreateAllTradingGoodsAmountsIfNoExist(Licence.GetOfficeNumber());
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String localInvoiceAmountsSubquery = "SELECT COALESCE(SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM), 0) "
				+ "FROM LOCAL_INVOICE_MATERIALS INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
				+ "WHERE LOCAL_INVOICE_MATERIALS.MAT_ID = MATERIALS.ID AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0 "
				+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
				+ "AND YEAR(LOCAL_INVOICES.I_DATE) = " + ClientAppSettings.currentYear;
		String localInvoiceAmountsSubquery2 = "SELECT COALESCE(SUM(LOCAL_INVOICE_ITEMS.AMT), 0) "
				+ "FROM LOCAL_INVOICE_ITEMS "
				+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID "
				+ "WHERE LOCAL_INVOICE_ITEMS.IT_ID = TRADING_GOODS.ID AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND YEAR(LOCAL_INVOICES.I_DATE) = " + ClientAppSettings.currentYear + " "			
				+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
				+ "AND LOCAL_INVOICE_ITEMS.IT_TYPE = " + Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
		
		String query1 = "SELECT MATERIALS.ID, MATERIALS.NAME, MEASURING_UNITS.NAME, "
				+ "MATERIAL_AMOUNTS.AMOUNT - (" + localInvoiceAmountsSubquery + ") "
				+ "FROM ((MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID) "
				+ "WHERE MATERIALS.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND MATERIAL_AMOUNTS.AMOUNT_YEAR = ? "
				+ "ORDER BY MATERIALS.NAME";
		String query2 = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, "
				+ "TRADING_GOODS_AMOUNTS.AMOUNT - (" + localInvoiceAmountsSubquery2 + "), TRADING_GOODS.CUSTOM_ID "
				+ "FROM TRADING_GOODS "
				+ "INNER JOIN TRADING_GOODS_AMOUNTS ON TRADING_GOODS.ID = TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0 AND TRADING_GOODS_AMOUNTS.OFFICE_NUMBER = ? "
				+ "AND AMOUNT_YEAR = ? "
				+ "ORDER BY TRADING_GOODS.NAME";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			String invoiceAmountsSubquery = localInvoiceAmountsSubquery.replace(" AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0", "").replace("LOCAL_INVOICE_MATERIALS", "INVOICE_MATERIALS").replace("LOCAL_INVOICES", "INVOICES");
			String invoiceTestAmountsSubquery = localInvoiceAmountsSubquery.replace(" AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0", "").replace("LOCAL_INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("LOCAL_INVOICES", "INVOICES_TEST");
			String localInvoiceTestAmountsSubquery = localInvoiceAmountsSubquery.replace("LOCAL_INVOICE_MATERIALS", "LOCAL_INVOICE_MATERIALS_TEST").replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			query1 = "SELECT MATERIALS.ID, MATERIALS.NAME, MEASURING_UNITS.NAME, "
				+ "MATERIAL_AMOUNTS.AMOUNT + (" + invoiceAmountsSubquery + ") - (" + invoiceTestAmountsSubquery + ") - (" + localInvoiceTestAmountsSubquery + ") "
				+ "FROM ((MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID) "
				+ "WHERE MATERIALS.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND MATERIAL_AMOUNTS.AMOUNT_YEAR = ? "
				+ "ORDER BY MATERIALS.NAME";
			
			String invoiceAmountsSubquery2 = localInvoiceAmountsSubquery2.replace(" AND LOCAL_INVOICES.IS_DELETED = 0", "").replace("LOCAL_INVOICE_ITEMS", "INVOICE_ITEMS").replace("LOCAL_INVOICES", "INVOICES");
			String invoiceTestAmountsSubquery2 = localInvoiceAmountsSubquery2.replace(" AND LOCAL_INVOICES.IS_DELETED = 0", "").replace("LOCAL_INVOICE_ITEMS", "INVOICE_ITEMS_TEST").replace("LOCAL_INVOICES", "INVOICES_TEST");
			String localInvoiceTestAmountsSubquery2 = localInvoiceAmountsSubquery2.replace("LOCAL_INVOICE_ITEMS", "LOCAL_INVOICE_ITEMS_TEST").replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			query2 = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, "
				+ "TRADING_GOODS_AMOUNTS.AMOUNT + (" + invoiceAmountsSubquery2 + ") - (" + invoiceTestAmountsSubquery2 + ") - (" + localInvoiceTestAmountsSubquery2 + "), "
				+ "TRADING_GOODS.CUSTOM_ID "
				+ "FROM TRADING_GOODS "
				+ "INNER JOIN TRADING_GOODS_AMOUNTS ON TRADING_GOODS.ID = TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0 AND TRADING_GOODS_AMOUNTS.OFFICE_NUMBER = ? "
				+ "AND AMOUNT_YEAR = ? "
				+ "ORDER BY TRADING_GOODS.NAME";
		}
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, query1);
		multiDatabaseQuery.SetQuery(1, query2);
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(0, 2, ClientAppSettings.currentYear);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 2, ClientAppSettings.currentYear);
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
					CustomTableModel customTableModel = new CustomTableModel(4);
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Mj. jed.", "Stanje", "Stvarno stanje", "Razlika"});
					while (databaseQueryResult[0].next()) {
						Object[] rowData = new Object[6];
						rowData[0] = databaseQueryResult[0].getString(0);
						rowData[1] = databaseQueryResult[0].getString(1);
						rowData[2] = databaseQueryResult[0].getString(2);
						rowData[3] = databaseQueryResult[0].getString(3);
						rowData[4] = "";
						rowData[5] = "";
						customTableModel.addRow(rowData);
					}
					jTableMaterials.setModel(customTableModel);
					jTableMaterials.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneMaterials.getWidth() * 15 / 100);
					jTableMaterials.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneMaterials.getWidth() * 30 / 100);
					jTableMaterials.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneMaterials.getWidth() * 15 / 100);
					jTableMaterials.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
					jTableMaterials.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
					jTableMaterials.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneMaterials.getWidth() * 20 / 100);
					
					DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
					headerRenderer.setBackground(new Color(220, 230, 250));
					jTableMaterials.getColumnModel().getColumn(4).setHeaderRenderer(headerRenderer);
							
					// Trading goods
					CustomTableModel customTableModelTradingGoods = new CustomTableModel(3);
					customTableModelTradingGoods.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Stanje", "Stvarno stanje", "Razlika"});
					while (databaseQueryResult[1].next()) {
						Object[] rowData = new Object[5];
						rowData[0] = customIdEnabled ? databaseQueryResult[1].getString(3) : databaseQueryResult[1].getString(0);
						rowData[1] = databaseQueryResult[1].getString(1);
						rowData[2] = databaseQueryResult[1].getString(2);
						rowData[3] = "";
						rowData[4] = "";
						customTableModelTradingGoods.addRow(rowData);
					}
					jTableTradingGoods.setModel(customTableModelTradingGoods);
					jTableTradingGoods.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 15 / 100);
					jTableTradingGoods.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 30 / 100);
					jTableTradingGoods.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 20 / 100);
					jTableTradingGoods.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 20 / 100);
					jTableTradingGoods.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTradingGoods.getWidth() * 20 / 100);
					
					jTableTradingGoods.getColumnModel().getColumn(3).setHeaderRenderer(headerRenderer);
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
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonCreateReceipt = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneMaterials = new javax.swing.JScrollPane();
        jTableMaterials = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPaneTradingGoods = new javax.swing.JScrollPane();
        jTableTradingGoods = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Inventura");
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
        jLabel9.setText("Inventura");

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(65, 55));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(65, 55));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(65, 55));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonCreateReceipt.setText("<html> <div style=\"text-align: center\"> Generiraj godišnju inventuru <br> (primku broj 1) <br> [F6] </div> </html>");
        jButtonCreateReceipt.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonCreateReceipt.setPreferredSize(new java.awt.Dimension(65, 55));
        jButtonCreateReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateReceiptActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCreateReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonCreateReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Materijali [F2]", jPanel3);

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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Trgovačka roba [F3]", jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(159, 159, 159)
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
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
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite izaći? Unesene promjene neće biti spremljene.", "", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		if(jTabbedPane1.getSelectedIndex() == 0){
			PrintUtils.PrintPosTable("Inventura - materijali", jTableMaterials, new int[][]{ new int[]{0, 1, 2}, new int[]{3, 4, 5} }, null);
		} else {
			PrintUtils.PrintPosTable("Inventura - trgovačka roba", jTableTradingGoods, new int[][]{ new int[]{0, 1}, new int[]{2, 3, 4} }, null);
		}
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		if(jTabbedPane1.getSelectedIndex() == 0){
			PrintUtils.PrintA4Table("Inventura-materijali", "Inventura - materijali", jTableMaterials, "");
		} else {
			PrintUtils.PrintA4Table("Inventura-trgovačkaRoba", "Inventura - trgovačka roba", jTableTradingGoods, "");
		}
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonCreateReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateReceiptActionPerformed
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite generirati godišnju inventuru? Ova opcija obrisati će staru primku broj 1, ako ona postoji!", "Generiraj godišnju inventuru", JOptionPane.YES_NO_OPTION);
        if(dialogResult != JOptionPane.YES_OPTION){
			return;
		}
		
		int firstReceiptId = -1;
		int currentYear = ClientAppSettings.currentYear;
		
		// Get first receipt id if exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			String query = "SELECT RECEIPTS.ID FROM RECEIPTS "
					+ "WHERE RECEIPTS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? AND YEAR(RECEIPTS.RECEIPT_DATE) = ? AND RECEIPT_NUMBER = 1";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, Licence.GetOfficeNumber());
			databaseQuery.AddParam(2, currentYear);

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
							firstReceiptId = databaseQueryResult.getInt(0);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		// Delete first receipt items if exits
		if(firstReceiptId != -1){
			String queryUpdateAllMaterialAmountsMinus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(RECEIPT_MATERIALS.AMOUNT) "
					+ "FROM RECEIPT_MATERIALS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "WHERE RECEIPT_MATERIALS.RECEIPT_ID = ? "
					+ "AND RECEIPT_MATERIALS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = RECEIPT_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT RECEIPT_MATERIALS.AMOUNT "
					+ "FROM RECEIPT_MATERIALS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
					+ "WHERE RECEIPT_MATERIALS.RECEIPT_ID = ? "
					+ "AND RECEIPT_MATERIALS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = RECEIPT_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";

			String queryUpdateAllTradingGoodsAmountsMinus = ""
				+ "UPDATE TRADING_GOODS_AMOUNTS SET AMOUNT = AMOUNT - ("
					+ "SELECT SUM(RECEIPT_TRADING_GOODS.AMOUNT) "
					+ "FROM RECEIPT_TRADING_GOODS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "WHERE RECEIPT_TRADING_GOODS.RECEIPT_ID = ? "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT RECEIPT_TRADING_GOODS.AMOUNT "
					+ "FROM RECEIPT_TRADING_GOODS "
					+ "INNER JOIN RECEIPTS ON RECEIPTS.ID = RECEIPT_TRADING_GOODS.RECEIPT_ID "
					+ "WHERE RECEIPT_TRADING_GOODS.RECEIPT_ID = ? "
					+ "AND RECEIPT_TRADING_GOODS.IS_DELETED = 0 "
					+ "AND RECEIPTS.IS_DELETED = 0 "
					+ "AND TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = RECEIPT_TRADING_GOODS.TRADING_GOODS_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";

			String queryDeleteMaterials = "UPDATE RECEIPT_MATERIALS SET IS_DELETED = 1 WHERE RECEIPT_ID = ?";
			String queryDeleteTradingGoods = "UPDATE RECEIPT_TRADING_GOODS SET IS_DELETED = 1 WHERE RECEIPT_ID = ?";
			
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);

				MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);

				multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsMinus);
				multiDatabaseQuery.AddParam(0, 1, firstReceiptId);
				multiDatabaseQuery.AddParam(0, 2, firstReceiptId);
				multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(0, 4, currentYear);

				multiDatabaseQuery.SetQuery(1, queryUpdateAllTradingGoodsAmountsMinus);
				multiDatabaseQuery.AddParam(1, 1, firstReceiptId);
				multiDatabaseQuery.AddParam(1, 2, firstReceiptId);
				multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 4, currentYear);

				multiDatabaseQuery.SetQuery(2, queryDeleteMaterials);
				multiDatabaseQuery.AddParam(2, 1, firstReceiptId);
				
				multiDatabaseQuery.SetQuery(3, queryDeleteTradingGoods);
				multiDatabaseQuery.AddParam(3, 1, firstReceiptId);

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
			}
		}
		
		// Create first receipt id if no exist
		if(firstReceiptId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			String query = "INSERT INTO RECEIPTS (ID, RECEIPT_DATE, SUPPLIER_ID, DOCUMENT_NUMBER, TOTAL_PRICE, "
				+ "PAYMENT_DUE_DATE, IS_PAID, OFFICE_NUMBER, RECEIPT_NUMBER, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, 0)";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.SetAutoIncrementParam(1, "ID", "RECEIPTS");
			databaseQuery.AddParam(2, currentYear + "-01-01");
			databaseQuery.AddParam(3, 0);
			databaseQuery.AddParam(4, "inventura");
			databaseQuery.AddParam(5, 0);
			databaseQuery.AddParam(6, currentYear + "-01-01");
			databaseQuery.AddParam(7, 1);
			databaseQuery.AddParam(8, Licence.GetOfficeNumber());

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
						firstReceiptId = databaseQueryResult.autoGeneratedKey;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		if(firstReceiptId == -1)
			return;
		
		// Insert materials and trading goods
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);
			
			String queryInsert = "INSERT INTO RECEIPT_MATERIALS (ID, RECEIPT_ID, MATERIAL_ID, AMOUNT, PRICE, RABATE, IS_DELETED) "
					+ "SELECT ROW_NUMBER() OVER() + ?, ?, MATERIAL_AMOUNTS.MATERIAL_ID, MATERIAL_AMOUNTS.AMOUNT, 0, 0, 0 "
					+ "FROM MATERIAL_AMOUNTS "
					+ "WHERE MATERIAL_AMOUNTS.AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(0, queryInsert);
			multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "RECEIPT_MATERIALS");
			multiDatabaseQuery.AddParam(0, 2, firstReceiptId);
			multiDatabaseQuery.AddParam(0, 3, currentYear - 1);
			
			String queryUpdateMaterialPlus = "UPDATE MATERIAL_AMOUNTS "
					+ "SET MATERIAL_AMOUNTS.AMOUNT = MATERIAL_AMOUNTS.AMOUNT + "
						+ "(SELECT MATERIAL_AMOUNTS_2.AMOUNT FROM MATERIAL_AMOUNTS MATERIAL_AMOUNTS_2 "
						+ "WHERE MATERIAL_AMOUNTS.MATERIAL_ID = MATERIAL_AMOUNTS_2.MATERIAL_ID AND MATERIAL_AMOUNTS_2.AMOUNT_YEAR = ?) "
					+ "WHERE EXISTS (SELECT MATERIAL_AMOUNTS_2.AMOUNT FROM MATERIAL_AMOUNTS MATERIAL_AMOUNTS_2 "
						+ "WHERE MATERIAL_AMOUNTS.MATERIAL_ID = MATERIAL_AMOUNTS_2.MATERIAL_ID AND MATERIAL_AMOUNTS_2.AMOUNT_YEAR = ?) "
					+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(1, queryUpdateMaterialPlus);
			multiDatabaseQuery.AddParam(1, 1, currentYear - 1);
			multiDatabaseQuery.AddParam(1, 2, currentYear - 1);
			multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());	
			multiDatabaseQuery.AddParam(1, 4, currentYear);
			
			String queryInsertTradingGoods = "INSERT INTO RECEIPT_TRADING_GOODS (ID, RECEIPT_ID, TRADING_GOODS_ID, AMOUNT, PRICE, RABATE, IS_DELETED) "
					+ "SELECT ROW_NUMBER() OVER() + ?, ?, TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID, TRADING_GOODS_AMOUNTS.AMOUNT, 0, 0, 0 "
					+ "FROM TRADING_GOODS_AMOUNTS "
					+ "WHERE TRADING_GOODS_AMOUNTS.AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(2, queryInsertTradingGoods);
			multiDatabaseQuery.SetAutoIncrementParam(2, 1, "ID", "RECEIPT_TRADING_GOODS");
			multiDatabaseQuery.AddParam(2, 2, firstReceiptId);
			multiDatabaseQuery.AddParam(2, 3, currentYear - 1);
			
			String queryUpdateTradingGoodsPlus = "UPDATE TRADING_GOODS_AMOUNTS "
					+ "SET TRADING_GOODS_AMOUNTS.AMOUNT = TRADING_GOODS_AMOUNTS.AMOUNT + "
						+ "(SELECT TRADING_GOODS_AMOUNTS_2.AMOUNT FROM TRADING_GOODS_AMOUNTS TRADING_GOODS_AMOUNTS_2 "
						+ "WHERE TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = TRADING_GOODS_AMOUNTS_2.TRADING_GOODS_ID AND TRADING_GOODS_AMOUNTS_2.AMOUNT_YEAR = ?) "
					+ "WHERE EXISTS (SELECT TRADING_GOODS_AMOUNTS_2.AMOUNT FROM TRADING_GOODS_AMOUNTS TRADING_GOODS_AMOUNTS_2 "
						+ "WHERE TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID = TRADING_GOODS_AMOUNTS_2.TRADING_GOODS_ID AND TRADING_GOODS_AMOUNTS_2.AMOUNT_YEAR = ?) "
					+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(3, queryUpdateTradingGoodsPlus);
			multiDatabaseQuery.AddParam(3, 1, currentYear - 1);
			multiDatabaseQuery.AddParam(3, 2, currentYear - 1);
			multiDatabaseQuery.AddParam(3, 3, Licence.GetOfficeNumber());	
			multiDatabaseQuery.AddParam(3, 4, currentYear);
			
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
		}
		
		RefreshTablesMaterialsTradingGoods();
		new ClientAppReceiptsDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonCreateReceiptActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCreateReceipt;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneMaterials;
    private javax.swing.JScrollPane jScrollPaneTradingGoods;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableMaterials;
    private javax.swing.JTable jTableTradingGoods;
    // End of variables declaration//GEN-END:variables
}
