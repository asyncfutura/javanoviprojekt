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
import hr.adinfo.client.ui.receipts.ClientAppSelectFromToIdDialog;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppTransfersDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableTransfersIdList = new ArrayList<>();
	private ArrayList<Integer> tableTransfersYearList = new ArrayList<>();
	private ArrayList<Integer> tableTransfersStartOfficeIdList = new ArrayList<>();
	private ArrayList<Integer> tableTransfersDestOfficeIdList = new ArrayList<>();
	private ArrayList<Float> tableTransferTotalPriceList = new ArrayList<>();
	private int lastSelectedRowId = -1;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppTransfersDialog(java.awt.Frame parent, boolean modal) {
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
						jButtonPrintTransfers.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTableTransfers.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEdit.doClick();
				}
				if (mouseEvent.getClickCount() == 1) {
					if(lastSelectedRowId == jTableTransfers.getSelectedRow()){
						jButtonEdit.doClick();
					}
					lastSelectedRowId = jTableTransfers.getSelectedRow();
				}
			}
		});
		
		jTableTransfers.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTransfers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTransfers.getTableHeader().setReorderingAllowed(false);
		jTableTransfers.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableItems.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableItems.getTableHeader().setReorderingAllowed(false);
		jTableItems.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RestoreDefaultItemsTable();
		RefreshTable();
		
		jTableTransfers.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting() && jTableTransfers.getSelectedRow() == -1){
					lastSelectedRowId = -1;
				}
				if (event.getValueIsAdjusting() || jTableTransfers.getSelectedRow() == -1)
					return;
				
				int rowId = jTableTransfers.convertRowIndexToModel(jTableTransfers.getSelectedRow());
				int tableId = tableTransfersIdList.get(rowId);
				float currentTotalReceiptPrice = tableTransferTotalPriceList.get(rowId);
				RefreshTableItems(tableId, currentTotalReceiptPrice);
			}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ID, TRANSFER_START_DATE, STARTING_OFFICE_ID, DESTINATION_OFFICE_ID, "
				+ "TOTAL_PRICE, IS_DELIVERED "
				+ "FROM TRANSFERS "
				+ "WHERE IS_DELETED = 0 AND YEAR(TRANSFER_START_DATE) = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, ClientAppSettings.currentYear);
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Broj", "Datum otpreme", "Poslovnice", "Vrijednost", "Preuzeto"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Integer> yearList = new ArrayList<>();
					ArrayList<Integer> startOfficeIdList = new ArrayList<>();
					ArrayList<Integer> destOfficeIdList = new ArrayList<>();
					ArrayList<Float> totalPriceList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = databaseQueryResult.getString(0);
						Date transferDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(1));
						rowData[1] = new SimpleDateFormat("dd.MM.yyyy.").format(transferDate);
						rowData[2] = databaseQueryResult.getString(2) + " -> " + databaseQueryResult.getString(3);
						rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
						rowData[4] = databaseQueryResult.getInt(5) == 0 ? "Ne" : "Da";
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
						totalPriceList.add(databaseQueryResult.getFloat(4));
						startOfficeIdList.add(databaseQueryResult.getInt(2));
						destOfficeIdList.add(databaseQueryResult.getInt(3));
						
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(transferDate);
						yearList.add(calendar.get(Calendar.YEAR));
					}
					jTableTransfers.setModel(customTableModel);
					tableTransfersIdList = idList;
					tableTransfersYearList = yearList;
					tableTransfersStartOfficeIdList = startOfficeIdList;
					tableTransfersDestOfficeIdList = destOfficeIdList;
					tableTransferTotalPriceList = totalPriceList;
					
					jTableTransfers.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
					jTableTransfers.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTransfers.getWidth() * 25 / 100);
					jTableTransfers.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTransfers.getWidth() * 20 / 100);
					jTableTransfers.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTransfers.getWidth() * 25 / 100);
					jTableTransfers.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableItems(int transferId, float currentSavedTotalPrice){
		float totalAmount = 0f;
		float totalPrice = 0f;
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "Cijena", "Ukupno"});
		
		// Get materials
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			String query = "SELECT TRANSFER_MATERIALS.MATERIAL_ID, MATERIALS.NAME, TRANSFER_MATERIALS.AMOUNT_START, MEASURING_UNITS.NAME, TRANSFER_MATERIALS.PRICE "
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
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[2] = databaseQueryResult.getString(3);
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4);
							rowData[4] = ClientAppUtils.FloatToPriceString(price);
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
		
		// Get articles
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT TRANSFER_ARTICLES.STARTING_ARTICLE_ID, ARTICLES.NAME, TRANSFER_ARTICLES.AMOUNT_START, MEASURING_UNITS.NAME, TRANSFER_ARTICLES.PRICE "
					+ "FROM ((TRANSFER_ARTICLES INNER JOIN ARTICLES ON TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ARTICLES.ID)"
					+ "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
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
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult.getString(1);
							rowData[1] = databaseQueryResult.getString(2);
							rowData[2] = databaseQueryResult.getString(3);
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
							float price = databaseQueryResult.getFloat(2) * databaseQueryResult.getFloat(4);
							rowData[4] = ClientAppUtils.FloatToPriceString(price);
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
		
		jLabelAmount.setText("" + totalAmount);
		jLabelValue.setText(ClientAppUtils.FloatToPriceString(totalPrice));
		
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTransfers.getWidth() * 35 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTransfers.getWidth() * 20 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTransfers.getWidth() * 20 / 100);
				
		// Update transfer total price
		if(currentSavedTotalPrice != totalPrice){
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
		customTableModel.setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "Cijena", "Ukupno"});
		jTableItems.setModel(customTableModel);
		jTableItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTransfers.getWidth() * 35 / 100);
		jTableItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTransfers.getWidth() * 15 / 100);
		jTableItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTransfers.getWidth() * 20 / 100);
		jTableItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTransfers.getWidth() * 20 / 100);
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
        jButtonPrintTransfers = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneTransfers = new javax.swing.JScrollPane();
        jTableTransfers = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneItems = new javax.swing.JScrollPane();
        jTableItems = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabelAmount = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Međuskladišnica");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Međuskladišnica");

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
        jButtonPrintFromTo.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonPrintFromTo.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintFromTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintFromToActionPerformed(evt);
            }
        });

        jButtonPrintTransfers.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> lista među- <br> skladišnica <br> [F6] </div> </html>");
        jButtonPrintTransfers.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPrintTransfers.setPreferredSize(new java.awt.Dimension(85, 60));
        jButtonPrintTransfers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintTransfersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintFromTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintTransfers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(jButtonPrintTransfers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPrintFromTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(724, Short.MAX_VALUE))
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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Međuskladišnice"));

        jTableTransfers.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTransfers.setViewportView(jTableTransfers);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTransfers)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTransfers, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Stavke pojedine međuskladišnice"));

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
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
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
		ClientAppSelectOfficeDialog selectDialog = new ClientAppSelectOfficeDialog(null, true);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppTransfersAddEditDialog addEditdialog = new ClientAppTransfersAddEditDialog(null, true, -1, selectDialog.selectedId, selectDialog.selectedAddress);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RestoreDefaultItemsTable();
				RefreshTable();
			}
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if(jTableTransfers.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableTransfers.convertRowIndexToModel(jTableTransfers.getSelectedRow());
        int tableId = tableTransfersIdList.get(rowId);

        ClientAppTransfersAddEditDialog addEditdialog = new ClientAppTransfersAddEditDialog(null, true, tableId, -1, "");
        addEditdialog.setVisible(true);
        RestoreDefaultItemsTable();
        RefreshTable();
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if(jTableTransfers.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableTransfers.convertRowIndexToModel(jTableTransfers.getSelectedRow());
        int transferId = tableTransfersIdList.get(rowId);
        int receiptYear = tableTransfersYearList.get(rowId);
        int startOfficeId = tableTransfersStartOfficeIdList.get(rowId);
        int destOfficeId = tableTransfersDestOfficeIdList.get(rowId);
        String tableValue = String.valueOf(jTableTransfers.getModel().getValueAt(rowId, 0));
		
        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati međuskladišnicu broj " + tableValue, "Obriši međuskladišnicu", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			String queryUpdateAllStartingArticleMaterialAmountsPlus = ""
				+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
					+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
					+ "FROM TRANSFER_ARTICLE_MATERIALS "
					+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
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
					+ "WHERE TRANSFER_ARTICLES.TRANSFER_ID = ? "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
					+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
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
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "WHERE EXISTS ("
					+ "SELECT TRANSFER_MATERIALS.AMOUNT_START "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
					+ "WHERE TRANSFER_MATERIALS.TRANSFER_ID = ? "
					+ "AND TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.IS_DELETED = 0 "
					+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_MATERIALS.MATERIAL_ID"
				+ ") "
				+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";

			String queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered = ""
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

			String queryUpdateAllMaterialsAmountsMinusIfDelivered = ""
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
		
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(5);
			multiDatabaseQuery.SetQuery(0, queryUpdateAllMaterialAmountsPlus);
			multiDatabaseQuery.AddParam(0, 1, transferId);
			multiDatabaseQuery.AddParam(0, 2, transferId);
			multiDatabaseQuery.AddParam(0, 3, startOfficeId);
			multiDatabaseQuery.AddParam(0, 4, receiptYear);

			multiDatabaseQuery.SetQuery(1, queryUpdateAllStartingArticleMaterialAmountsPlus);
			multiDatabaseQuery.AddParam(1, 1, transferId);
			multiDatabaseQuery.AddParam(1, 2, transferId);
			multiDatabaseQuery.AddParam(1, 3, startOfficeId);
			multiDatabaseQuery.AddParam(1, 4, receiptYear);
			
			multiDatabaseQuery.SetQuery(2, queryUpdateAllMaterialsAmountsMinusIfDelivered);
			multiDatabaseQuery.AddParam(2, 1, transferId);
			multiDatabaseQuery.AddParam(2, 2, transferId);
			multiDatabaseQuery.AddParam(2, 3, destOfficeId);
			multiDatabaseQuery.AddParam(2, 4, receiptYear);

			multiDatabaseQuery.SetQuery(3, queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered);
			multiDatabaseQuery.AddParam(3, 1, transferId);
			multiDatabaseQuery.AddParam(3, 2, transferId);
			multiDatabaseQuery.AddParam(3, 3, destOfficeId);
			multiDatabaseQuery.AddParam(3, 4, receiptYear);
			
			String deleteTransferQuery = "UPDATE TRANSFERS SET IS_DELETED = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(4, deleteTransferQuery);
			multiDatabaseQuery.AddParam(4, 1, 1);
			multiDatabaseQuery.AddParam(4, 2, transferId);
			
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
			
			RestoreDefaultItemsTable();
			RefreshTable();
		}
    }//GEN-LAST:event_jButtonDeleteActionPerformed
	
    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		if(jTableTransfers.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite međuskladišnicu u tablici čiji sažetak želite ispisati.");
            return;
        }
		
		int rowIdTransfers = jTableTransfers.convertRowIndexToModel(jTableTransfers.getSelectedRow());
        String tableValue = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 0));
		
        String dateString = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 1));
        String officesString = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 2));
        String isDelivered = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 4));
		
		PrintTableExtraData extraData = new PrintTableExtraData();
		extraData.headerList.add(new Pair<>("Datum otpreme:      ", dateString));
		extraData.headerList.add(new Pair<>("Šifra poslovnica:   ", officesString));
		extraData.headerList.add(new Pair<>("Preuzeto:           ", isDelivered));
		extraData.footerList.add(new Pair<>("Ukupna količina:    ", jLabelAmount.getText()));
		extraData.footerList.add(new Pair<>("Ukupna vrijednost:  ", jLabelValue.getText()));
		
		PrintUtils.PrintPosTable("Sažetak međuskladišnice broj: " + tableValue, jTableItems, new int[]{0, 1, 3, 4}, extraData);
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		if(jTableTransfers.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite međuskladišnicu u tablici čiji sažetak želite ispisati.");
            return;
        }
		
		int rowIdTransfers = jTableTransfers.convertRowIndexToModel(jTableTransfers.getSelectedRow());
        String tableValue = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 0));
		
        String dateString = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 1));
        String officesString = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 2));
        String isDelivered = String.valueOf(jTableTransfers.getModel().getValueAt(rowIdTransfers, 4));
		
		PrintTableExtraData extraData = new PrintTableExtraData();
		extraData.headerList.add(new Pair<>("Datum otpreme:       ", dateString));
		extraData.headerList.add(new Pair<>("Šifra poslovnica:      ", officesString));
		extraData.headerList.add(new Pair<>("Preuzeto:                  ", isDelivered));
		extraData.headerList.add(new Pair<>("Ukupna količina:     ", jLabelAmount.getText()));
		extraData.headerList.add(new Pair<>("Ukupna vrijednost:  ", jLabelValue.getText()));

		PrintUtils.PrintA4Table("SažetakMeđuskladišnice-" + tableValue, "Sažetak međuskladišnice broj: " + tableValue, jTableItems, new int[]{0, 1, 2, 3, 4}, new int[]{}, extraData, "");
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
			
			String queryMaterials = "SELECT TRANSFERS.ID, MATERIALS.NAME, TRANSFER_MATERIALS.AMOUNT_START, MEASURING_UNITS.NAME, "
					+ "TRANSFER_MATERIALS.PRICE "
					+ "FROM TRANSFER_MATERIALS "
					+ "INNER JOIN MATERIALS ON TRANSFER_MATERIALS.MATERIAL_ID = MATERIALS.ID "
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFER_MATERIALS.TRANSFER_ID = TRANSFERS.ID "
					+ "WHERE TRANSFER_MATERIALS.IS_DELETED = 0 "
					+ "AND TRANSFERS.ID >= ? AND TRANSFERS.ID <= ? AND TRANSFERS.IS_DELETED = 0 "
					+ "ORDER BY TRANSFERS.ID";
			multiDatabaseQuery.SetQuery(0, queryMaterials);
			multiDatabaseQuery.AddParam(0, 1, printFromId);
			multiDatabaseQuery.AddParam(0, 2, printToId);
			
			String queryTradingGoods = "SELECT TRANSFERS.ID, ARTICLES.NAME, TRANSFER_ARTICLES.AMOUNT_START, MEASURING_UNITS.NAME, "
					+ "TRANSFER_ARTICLES.PRICE "
					+ "FROM TRANSFER_ARTICLES "
					+ "INNER JOIN ARTICLES ON TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ARTICLES.ID "
					+ "INNER JOIN TRANSFERS ON TRANSFER_ARTICLES.TRANSFER_ID = TRANSFERS.ID "
					+ "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "WHERE TRANSFER_ARTICLES.IS_DELETED = 0 "
					+ "AND TRANSFERS.ID >= ? AND TRANSFERS.ID <= ? AND TRANSFERS.IS_DELETED = 0 "
					+ "ORDER BY TRANSFERS.ID";
			multiDatabaseQuery.SetQuery(1, queryTradingGoods);
			multiDatabaseQuery.AddParam(1, 1, printFromId);
			multiDatabaseQuery.AddParam(1, 2, printToId);
			
			String queryReceipts = "SELECT ID, TRANSFER_START_DATE, STARTING_OFFICE_ID, DESTINATION_OFFICE_ID, "
					+ "TOTAL_PRICE, IS_DELIVERED "
					+ "FROM TRANSFERS "
					+ "WHERE IS_DELETED = 0"
					+ "AND ID >= ? AND ID <= ? "
					+ "ORDER BY ID";
			multiDatabaseQuery.SetQuery(2, queryReceipts);
			multiDatabaseQuery.AddParam(2, 1, printFromId);
			multiDatabaseQuery.AddParam(2, 2, printToId);
			
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
							customTableModels[tableCount].setColumnIdentifiers(new String[] {"Stavka", "Količina", "Mj. jed.", "Cijena", "Ukupno"});
							tempJTables[tableCount] = new JTable();
							extraData[tableCount] = new PrintTableExtraData();
							
							tableTitle[tableCount] = "Sažetak međuskladišnice broj: " + databaseQueryResult[2].getString(0);
							
							Date receiptDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[2].getString(1));
							String receiptDateString = new SimpleDateFormat("dd.MM.yyyy.").format(receiptDate);
							extraData[tableCount].headerList.add(new Pair<>("Datum otpreme:       ", receiptDateString));
							extraData[tableCount].headerList.add(new Pair<>("Šifra poslovnica:      ", databaseQueryResult[2].getString(2) + " -> " + databaseQueryResult[2].getString(3)));
							extraData[tableCount].headerList.add(new Pair<>("Preuzeto:                  ", databaseQueryResult[2].getInt(5) == 1 ? "Da" : "Ne"));
							extraData[tableCount].headerList.add(new Pair<>("Ukupna vrijednost:  ", databaseQueryResult[2].getString(4)));
							
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
							
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult[0].getString(1);
							rowData[1] = databaseQueryResult[0].getString(2);
							rowData[2] = databaseQueryResult[0].getString(3);
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult[0].getFloat(4));
							rowData[4] = databaseQueryResult[0].getFloat(2) * databaseQueryResult[0].getFloat(4);
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
							
							Object[] rowData = new Object[5];
							rowData[0] = databaseQueryResult[1].getString(1);
							rowData[1] = databaseQueryResult[1].getString(2);
							rowData[2] = databaseQueryResult[1].getString(3);
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult[1].getFloat(4));
							rowData[4] = databaseQueryResult[1].getFloat(2) * databaseQueryResult[1].getFloat(4);
							customTableModels[tableId].addRow(rowData);
							amountSum[tableId] += databaseQueryResult[1].getFloat(2);
						}
						
						for (int i = 0; i < tableCount; ++i){
							extraData[i].headerList.add(new Pair<>("Ukupna količina:     ", "" + amountSum[i]));
							tempJTables[i].setModel(customTableModels[i]);
							tempJTables[i].getColumnModel().getColumn(0).setPreferredWidth(40);
							tempJTables[i].getColumnModel().getColumn(1).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(2).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(3).setPreferredWidth(15);
							tempJTables[i].getColumnModel().getColumn(4).setPreferredWidth(15);
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
			columnIndex[i] = new int[]{0, 1, 2, 3, 4};
			mergeIndex[i] = new int[]{};
			newPageBoolean[i] = true;
		}
		
		PrintUtils.PrintA4Table("ListaMeđuskladišnicaOdDo", tableTitle, tempJTables, columnIndex, mergeIndex, extraData, newPageBoolean, "");
		
		if(tempJTables != null){
			for (int i = 0; i < tempJTables.length; ++i){
				tempJTables[i] = null;
			}
		}
		tempJTables = null;
    }//GEN-LAST:event_jButtonPrintFromToActionPerformed

    private void jButtonPrintTransfersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintTransfersActionPerformed
		PrintUtils.PrintA4Table("ListaMeđuskladišnica", "Lista međuskladišnica", jTableTransfers, "");
    }//GEN-LAST:event_jButtonPrintTransfersActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTransfers.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableTransfers.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintFromTo;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JButton jButtonPrintTransfers;
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
    private javax.swing.JScrollPane jScrollPaneTransfers;
    private javax.swing.JTable jTableItems;
    private javax.swing.JTable jTableTransfers;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
