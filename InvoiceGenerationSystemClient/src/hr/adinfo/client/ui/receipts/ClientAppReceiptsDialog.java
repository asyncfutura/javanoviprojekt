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
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppReceiptsDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableReceiptsIdList = new ArrayList<>();
	private ArrayList<Integer> tableReceiptsYearList = new ArrayList<>();
	private ArrayList<Float> tableReceiptTotalPriceList = new ArrayList<>();
	private int lastSelectedRowId = -1;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppReceiptsDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						jButtonAdd.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						jButtonEdit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDelete.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPos.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F3){
						jButtonPrintFromTo.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPrintReceipts.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonCreateReceipt.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTableReceipts.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEdit.doClick();
				}
				if (mouseEvent.getClickCount() == 1) {
					if(lastSelectedRowId == jTableReceipts.getSelectedRow()){
						jButtonEdit.doClick();
					}
					lastSelectedRowId = jTableReceipts.getSelectedRow();
				}
			}
		});
		
		jTableReceipts.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableReceipts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableReceipts.getTableHeader().setReorderingAllowed(false);
		jTableReceipts.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableItems.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableItems.getTableHeader().setReorderingAllowed(false);
		jTableItems.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RestoreDefaultItemsTable();
		RefreshTable();
		
		jTableReceipts.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting() && jTableReceipts.getSelectedRow() == -1){
					lastSelectedRowId = -1;
				}
				if (event.getValueIsAdjusting() || jTableReceipts.getSelectedRow() == -1)
					return;
				
				int rowId = jTableReceipts.convertRowIndexToModel(jTableReceipts.getSelectedRow());
				int tableId = tableReceiptsIdList.get(rowId);
				float currentTotalReceiptPrice = tableReceiptTotalPriceList.get(rowId);
				RefreshTableItems(tableId, currentTotalReceiptPrice);
			}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT RECEIPTS.ID, RECEIPTS.RECEIPT_DATE, SUPPLIERS.NAME, RECEIPTS.DOCUMENT_NUMBER, RECEIPTS.TOTAL_PRICE, RECEIPTS.RECEIPT_NUMBER "
				+ "FROM RECEIPTS INNER JOIN SUPPLIERS ON RECEIPTS.SUPPLIER_ID = SUPPLIERS.ID "
				+ "WHERE RECEIPTS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? AND YEAR(RECEIPTS.RECEIPT_DATE) = ? "
				+ "ORDER BY RECEIPTS.RECEIPT_NUMBER";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
		databaseQuery.AddParam(2, ClientAppSettings.currentYear);
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Broj", "Datum", "Dobavljač", "Br. dokumenta", "Vrijednost"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Integer> yearList = new ArrayList<>();
					ArrayList<Float> totalPriceList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = databaseQueryResult.getString(5);
						Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(1));
						rowData[1] = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = databaseQueryResult.getString(3);
						rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
						totalPriceList.add(databaseQueryResult.getFloat(4));
						
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(receiptDate);
						yearList.add(calendar.get(Calendar.YEAR));
					}
					jTableReceipts.setModel(customTableModel);
					tableReceiptsIdList = idList;
					tableReceiptsYearList = yearList;
					tableReceiptTotalPriceList = totalPriceList;
					
					jTableReceipts.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneReceipts.getWidth() * 10 / 100);
					jTableReceipts.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneReceipts.getWidth() * 20 / 100);
					jTableReceipts.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneReceipts.getWidth() * 30 / 100);
					jTableReceipts.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneReceipts.getWidth() * 20 / 100);
					jTableReceipts.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneReceipts.getWidth() * 20 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableItems(int receiptId, float currentSavedTotalPrice){
		float totalAmount = 0f;
		float totalPrice = 0f;
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "PNC", "Cijena", "Ukupno"});
		
		// Get materials
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT RECEIPT_MATERIALS.MATERIAL_ID, MATERIALS.NAME, RECEIPT_MATERIALS.AMOUNT, MEASURING_UNITS.NAME, "
					+ "RECEIPT_MATERIALS.PRICE, RECEIPT_MATERIALS.RABATE, MATERIALS.LAST_PRICE "
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
						if(databaseQueryResult.getSize() != 0){
							Object[] rowDataHeader = new Object[6];
							rowDataHeader[0] = "Materijali:";
							rowDataHeader[1] = "";
							rowDataHeader[2] = "";
							rowDataHeader[3] = "";
							rowDataHeader[4] = "";
							rowDataHeader[5] = "";
							customTableModel.addRow(rowDataHeader);
						}
						
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[6];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = databaseQueryResult.getString(3);
							rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(6));
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4) * (100f - databaseQueryResult.getFloat(5)) / 100f;
							rowData[5] = ClientAppUtils.FloatToPriceString(price);
							customTableModel.addRow(rowData);
							totalAmount += databaseQueryResult.getFloat(2);
							totalPrice += price;
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		final int materialsSize = customTableModel.getRowCount();
		jTableItems.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
				final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				c.setForeground(row == materialsSize || row == 0 ? Values.normalForegroundSelected : Values.normalForeground);
				c.setBackground(row == materialsSize || row == 0 ? Values.normalBackgroundSelected : Values.normalBackground);
				return c;
			}
		});
		
		// Get trading goods
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT RECEIPT_TRADING_GOODS.TRADING_GOODS_ID, TRADING_GOODS.NAME, RECEIPT_TRADING_GOODS.AMOUNT, "
					+ "RECEIPT_TRADING_GOODS.PRICE, RECEIPT_TRADING_GOODS.RABATE, TRADING_GOODS.LAST_PRICE "
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
						if(databaseQueryResult.getSize() != 0){
							Object[] rowDataHeader = new Object[6];
							rowDataHeader[0] = "Trgovačka roba:";
							rowDataHeader[1] = "";
							rowDataHeader[2] = "";
							rowDataHeader[3] = "";
							rowDataHeader[4] = "";
							rowDataHeader[5] = "";
							customTableModel.addRow(rowDataHeader);
						}
						
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[6];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = Values.TRADING_GOODS_MEASURING_UNIT;
							rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(5));
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(3) * (100f - databaseQueryResult.getFloat(4)) / 100f;
							rowData[5] = ClientAppUtils.FloatToPriceString(price);
							customTableModel.addRow(rowData);
							totalAmount += databaseQueryResult.getFloat(2);
							totalPrice += price;
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		jLabelAmount.setText(ClientAppUtils.FloatToPriceString(totalAmount));
		jLabelValue.setText(ClientAppUtils.FloatToPriceString(totalPrice));
		
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneReceipts.getWidth() * 30 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneReceipts.getWidth() * 10 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
				
		// Update receipt total price
		if(currentSavedTotalPrice != totalPrice){
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
						
					} else {
						
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	private void RestoreDefaultItemsTable(){
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "PNC", "Cijena", "Ukupno"});
		jTableItems.setModel(customTableModel);

		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneReceipts.getWidth() * 30 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneReceipts.getWidth() * 10 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneReceipts.getWidth() * 15 / 100);
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonPrintFromTo = new javax.swing.JButton();
        jButtonPrintReceipts = new javax.swing.JButton();
        jButtonCreateReceipt = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneReceipts = new javax.swing.JScrollPane();
        jTableReceipts = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneItems = new javax.swing.JScrollPane();
        jTableItems = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabelAmount = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Primke");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Primke");

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

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEdit.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDelete.setToolTipText("");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonPrintFromTo.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> od-do <br> [F3] </div> </html>");
        jButtonPrintFromTo.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintFromTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintFromToActionPerformed(evt);
            }
        });

        jButtonPrintReceipts.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> lista primki <br> [F6] </div> </html>");
        jButtonPrintReceipts.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintReceipts.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPrintReceipts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintReceiptsActionPerformed(evt);
            }
        });

        jButtonCreateReceipt.setText("<html> <div style=\"text-align: center\"> Generiraj  <br> primku broj 1 <br> [F7] </div> </html>");
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
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintFromTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jButtonCreateReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonPrintReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonPrintFromTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jButtonCreateReceipt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Filter");

        jTextField1.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Primke"));

        jTableReceipts.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneReceipts.setViewportView(jTableReceipts);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneReceipts)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneReceipts, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke pojedine primke"));

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

        jLabel9.setText("Količina: ");

        jLabelAmount.setText("0");

        jLabel11.setText("Vrijednost:");

        jLabelValue.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneItems)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jLabelAmount)
                .addGap(114, 114, 114)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(jLabelValue)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneItems, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabelAmount)
                    .addComponent(jLabel11)
                    .addComponent(jLabelValue)))
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
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(202, 202, 202)
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(30, 30, 30)))
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        ClientAppReceiptsAddEditDialog addEditdialog = new ClientAppReceiptsAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
		RestoreDefaultItemsTable();
        RefreshTable();
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if(jTableReceipts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableReceipts.convertRowIndexToModel(jTableReceipts.getSelectedRow());
        int tableId = tableReceiptsIdList.get(rowId);

        ClientAppReceiptsAddEditDialog addEditdialog = new ClientAppReceiptsAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        RestoreDefaultItemsTable();
        RefreshTable();
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if(jTableReceipts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati.");
            return;
        }
		int rowId = jTableReceipts.convertRowIndexToModel(jTableReceipts.getSelectedRow());
        int tableId = tableReceiptsIdList.get(rowId);
        int receiptYear = tableReceiptsYearList.get(rowId);
        String tableValue = String.valueOf(jTableReceipts.getModel().getValueAt(rowId, 0));
		
		if("1".equals(tableValue)){
			ClientAppLogger.GetInstance().ShowMessage("Primka broj 1 (godišnja inventura) ne može se obrisati");
            return;
		}
		
        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati primku broj " + tableValue, "Obriši primku", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
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
						
			String queryDeleteReceipt = "UPDATE RECEIPTS SET IS_DELETED = ? WHERE ID = ?";
			
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);
				
				MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
				
				multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsMinus);
				multiDatabaseQuery.AddParam(0, 1, tableId);
				multiDatabaseQuery.AddParam(0, 2, tableId);
				multiDatabaseQuery.AddParam(0, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(0, 4, receiptYear);
				
				multiDatabaseQuery.SetQuery(1, queryUpdateAllTradingGoodsAmountsMinus);
				multiDatabaseQuery.AddParam(1, 1, tableId);
				multiDatabaseQuery.AddParam(1, 2, tableId);
				multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 4, receiptYear);
				
				multiDatabaseQuery.SetQuery(2, queryDeleteReceipt);
				multiDatabaseQuery.AddParam(2, 1, 1);
				multiDatabaseQuery.AddParam(2, 2, tableId);

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
			
			RestoreDefaultItemsTable();
			RefreshTable();
		}
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		if(jTableReceipts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite primku u tablici čiji sažetak želite ispisati.");
            return;
        }
		
		int rowIdArticles = jTableReceipts.convertRowIndexToModel(jTableReceipts.getSelectedRow());
        String tableValue = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 0));
		
        String dateString = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 1));
        String supplierString = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 2));
        String documentNumber = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 3));
		
		PrintTableExtraData extraData = new PrintTableExtraData();
		extraData.headerList.add(new Pair<>("Datum primke:    ", dateString));
		extraData.headerList.add(new Pair<>("Broj dokumenta:  ", documentNumber));
		extraData.headerList.add(new Pair<>("Dobavljač:       ", supplierString));
		extraData.footerList.add(new Pair<>("Ukupna količina:    ", jLabelAmount.getText()));
		extraData.footerList.add(new Pair<>("Ukupna vrijednost:  ", jLabelValue.getText() + " kn"));
		
		PrintUtils.PrintPosTable("Sažetak primke broj: " + tableValue, jTableItems, new int[]{0, 1, 4, 5}, extraData);
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		if(jTableReceipts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite primku u tablici čiji sažetak želite ispisati.");
            return;
        }
		
		int rowIdArticles = jTableReceipts.convertRowIndexToModel(jTableReceipts.getSelectedRow());
        String tableValue = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 0));
		
        String dateString = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 1));
        String supplierString = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 2));
        String documentNumber = String.valueOf(jTableReceipts.getModel().getValueAt(rowIdArticles, 3));
		
		PrintTableExtraData extraData = new PrintTableExtraData();
		extraData.headerList.add(new Pair<>("Datum primke:          ", dateString));
		extraData.headerList.add(new Pair<>("Dobavljač:                 ", supplierString));
		extraData.headerList.add(new Pair<>("Broj dokumenta:       ", documentNumber));
		extraData.footerList.add(new Pair<>("Ukupna količina:      ", jLabelAmount.getText()));
		extraData.footerList.add(new Pair<>("Ukupna vrijednost:   ", jLabelValue.getText() + " kn"));

		PrintUtils.PrintA4Table("SažetakPrimke-" + tableValue, "Sažetak primke broj: " + tableValue, jTableItems, new int[]{0, 1, 2, 3, 4, 5}, new int[]{}, extraData, "");
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonPrintFromToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintFromToActionPerformed
        ClientAppSelectFromToIdDialog fromToDialog = new ClientAppSelectFromToIdDialog(null, true);
		fromToDialog.setVisible(true);
		if(fromToDialog.selectedIdFrom == -1 || fromToDialog.selectedIdTo == -1){
			return;
		}
		if(fromToDialog.selectedIdFrom > fromToDialog.selectedIdTo){
			return;
		}
		
		JTable[] tempJTables = null;
		PrintTableExtraData[] extraData = null;
		String[] tableTitle = null;
		
		int printFromId = fromToDialog.selectedIdFrom;
		int printToId = fromToDialog.selectedIdTo;

		// Get data
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			String queryMaterials = "SELECT RECEIPTS.ID, MATERIALS.NAME, RECEIPT_MATERIALS.AMOUNT, MEASURING_UNITS.NAME, "
					+ "RECEIPT_MATERIALS.PRICE, RECEIPT_MATERIALS.RABATE, MATERIALS.LAST_PRICE "
					+ "FROM RECEIPT_MATERIALS "
					+ "INNER JOIN MATERIALS ON RECEIPT_MATERIALS.MATERIAL_ID = MATERIALS.ID "
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "INNER JOIN RECEIPTS ON RECEIPT_MATERIALS.RECEIPT_ID = RECEIPTS.ID "
					+ "WHERE RECEIPT_MATERIALS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? "
					+ "AND RECEIPTS.ID >= ? AND RECEIPTS.ID <= ? AND RECEIPTS.IS_DELETED = 0 "
					+ "ORDER BY RECEIPTS.ID";
			multiDatabaseQuery.SetQuery(0, queryMaterials);
			multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 2, printFromId);
			multiDatabaseQuery.AddParam(0, 3, printToId);
			
			String queryTradingGoods = "SELECT RECEIPTS.ID, TRADING_GOODS.NAME, RECEIPT_TRADING_GOODS.AMOUNT, "
					+ "RECEIPT_TRADING_GOODS.PRICE, RECEIPT_TRADING_GOODS.RABATE, TRADING_GOODS.LAST_PRICE "
					+ "FROM RECEIPT_TRADING_GOODS "
					+ "INNER JOIN TRADING_GOODS ON RECEIPT_TRADING_GOODS.TRADING_GOODS_ID = TRADING_GOODS.ID "
					+ "INNER JOIN RECEIPTS ON RECEIPT_TRADING_GOODS.RECEIPT_ID = RECEIPTS.ID "
					+ "WHERE RECEIPT_TRADING_GOODS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? "
					+ "AND RECEIPTS.ID >= ? AND RECEIPTS.ID <= ? AND RECEIPTS.IS_DELETED = 0 "
					+ "ORDER BY RECEIPTS.ID";
			multiDatabaseQuery.SetQuery(1, queryTradingGoods);
			multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(1, 2, printFromId);
			multiDatabaseQuery.AddParam(1, 3, printToId);
			
			String queryReceipts = "SELECT RECEIPTS.ID, RECEIPTS.RECEIPT_DATE, SUPPLIERS.NAME, RECEIPTS.DOCUMENT_NUMBER, RECEIPTS.TOTAL_PRICE "
					+ "FROM RECEIPTS INNER JOIN SUPPLIERS ON RECEIPTS.SUPPLIER_ID = SUPPLIERS.ID "
					+ "WHERE RECEIPTS.IS_DELETED = 0 AND RECEIPTS.OFFICE_NUMBER = ? "
					+ "AND RECEIPTS.ID >= ? AND RECEIPTS.ID <= ? "
					+ "ORDER BY RECEIPTS.ID";
			multiDatabaseQuery.SetQuery(2, queryReceipts);
			multiDatabaseQuery.AddParam(2, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(2, 2, printFromId);
			multiDatabaseQuery.AddParam(2, 3, printToId);
			
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
						if(databaseQueryResult[2].getSize() == 0){
							return;
						}
						
						tempJTables = new JTable[databaseQueryResult[2].getSize()];
						extraData = new PrintTableExtraData[databaseQueryResult[2].getSize()];
						CustomTableModel[] customTableModels = new CustomTableModel[databaseQueryResult[2].getSize()];
						tableTitle = new String[databaseQueryResult[2].getSize()];
						int[] amountSum = new int[databaseQueryResult[2].getSize()];
						int[] receiptIdMapping = new int[databaseQueryResult[2].getSize()];
						int tableCount = 0;
						
						while (databaseQueryResult[2].next()) {
							customTableModels[tableCount] = new CustomTableModel();
							customTableModels[tableCount].setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "PNC", "Cijena", "Ukupno"});
							tempJTables[tableCount] = new JTable();
							extraData[tableCount] = new PrintTableExtraData();
							
							tableTitle[tableCount] = "Sažetak primke broj: " + databaseQueryResult[2].getString(0);
							
							Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[2].getString(1));
							String receiptDateString = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
							extraData[tableCount].headerList.add(new Pair<>("Datum primke:          ", receiptDateString));
							extraData[tableCount].headerList.add(new Pair<>("Dobavljač:                 ", databaseQueryResult[2].getString(2)));
							extraData[tableCount].headerList.add(new Pair<>("Broj dokumenta:       ", databaseQueryResult[2].getString(3)));
							extraData[tableCount].footerList.add(new Pair<>("Ukupna vrijednost:   ", databaseQueryResult[2].getString(4)));

							receiptIdMapping[tableCount] = databaseQueryResult[2].getInt(0);
							
							++tableCount;
						}
						
						while (databaseQueryResult[0].next()) {
							int tableId = -1;
							for(int i = 0; i < tableCount; ++i){
								if(receiptIdMapping[i] == databaseQueryResult[0].getInt(0)){
									tableId = i;
								}
							}
							
							if(tableId == -1)
								continue;
							
							Object[] rowData = new Object[6];
							rowData[0] = databaseQueryResult[0].getString(1);
							rowData[1] = databaseQueryResult[0].getString(2);
							rowData[2] = databaseQueryResult[0].getString(3);
							rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult[0].getFloat(4));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult[0].getFloat(6));
							float price = databaseQueryResult[0].getFloat(2) * databaseQueryResult[0].getFloat(4) * (100f - databaseQueryResult[0].getFloat(5)) / 100f;
							rowData[5] = ClientAppUtils.FloatToPriceString(price);
							customTableModels[tableId].addRow(rowData);
							amountSum[tableId] += databaseQueryResult[0].getFloat(2);
						}
						
						while (databaseQueryResult[1].next()) {
							int tableId = -1;
							for(int i = 0; i < tableCount; ++i){
								if(receiptIdMapping[i] == databaseQueryResult[1].getInt(0)){
									tableId = i;
								}
							}
							
							if(tableId == -1)
								continue;
							
							Object[] rowData = new Object[6];
							rowData[0] = databaseQueryResult[1].getString(1);
							rowData[1] = databaseQueryResult[1].getString(2);
							rowData[2] = Values.TRADING_GOODS_MEASURING_UNIT;
							rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult[1].getFloat(3));
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult[1].getFloat(5));
							float price = databaseQueryResult[1].getFloat(2) * databaseQueryResult[1].getFloat(3) * (100f - databaseQueryResult[1].getFloat(4)) / 100f;
							rowData[5] = ClientAppUtils.FloatToPriceString(price);
							customTableModels[tableId].addRow(rowData);
							amountSum[tableId] += databaseQueryResult[1].getFloat(2);
						}
						
						for (int i = 0; i < tableCount; ++i){
							extraData[i].footerList.add(new Pair<>("Ukupna količina:      ", "" + amountSum[i]));
							tempJTables[i].setModel(customTableModels[i]);
							tempJTables[i].getColumnModel().getColumn(0).setPreferredWidth(40);
							tempJTables[i].getColumnModel().getColumn(1).setPreferredWidth(10);
							tempJTables[i].getColumnModel().getColumn(2).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(3).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(4).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(5).setPreferredWidth(15);
						}
					}
				} catch (InterruptedException | ExecutionException | ParseException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		int[][] mergeIndex = new int[tempJTables.length][];
		int[][] columnIndex = new int[tempJTables.length][];
		boolean[] newPageBoolean = new boolean[tempJTables.length];
		for (int i = 0; i < tempJTables.length; ++i){
			columnIndex[i] = new int[]{0, 1, 2, 3, 4, 5};
			mergeIndex[i] = new int[]{};
			newPageBoolean[i] = true;
		}
		
		PrintUtils.PrintA4Table("ListaPrimkiOdDo", tableTitle, tempJTables, columnIndex, mergeIndex, extraData, newPageBoolean, "");
		
		if(tempJTables != null){
			for (int i = 0; i < tempJTables.length; ++i){
				tempJTables[i] = null;
			}
		}
		tempJTables = null;
    }//GEN-LAST:event_jButtonPrintFromToActionPerformed

    private void jButtonPrintReceiptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintReceiptsActionPerformed
        PrintUtils.PrintA4Table("ListaPrimki", "Lista primki", jTableReceipts, "");
    }//GEN-LAST:event_jButtonPrintReceiptsActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableReceipts.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableReceipts.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

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

        RestoreDefaultItemsTable();
		RefreshTable();
    }//GEN-LAST:event_jButtonCreateReceiptActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonCreateReceipt;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintFromTo;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JButton jButtonPrintReceipts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelAmount;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelValue;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneItems;
    private javax.swing.JScrollPane jScrollPaneReceipts;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTable jTableReceipts;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
