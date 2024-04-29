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
import hr.adinfo.client.ui.warehouse.ClientAppWarehouseArticlesAddEditDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
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
public class ClientAppTransfersSelectArticlesDialog extends javax.swing.JDialog {
	
	private ArrayList<Integer> articlesStartIdList = new ArrayList<>();
	private ArrayList<Integer> articlesDestIdList = new ArrayList<>();
	
	public int selectedIdStart = -1;
	public int selectedIdDest = -1;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppTransfersSelectArticlesDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_ENTER){
						jButtonSelect.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTableArticlesStart.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableArticlesStart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableArticlesStart.getTableHeader().setReorderingAllowed(false);
		jTableArticlesStart.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableArticlesDest.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableArticlesDest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableArticlesDest.getTableHeader().setReorderingAllowed(false);
		jTableArticlesDest.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableNormatives.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableNormatives.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableNormatives.getTableHeader().setReorderingAllowed(false);
		jTableNormatives.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Izlazni materijali",  "", "Ulazni materijali"});
		jTableNormatives.setModel(customTableModel);
		jTableNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneNormatives.getWidth() * 45 / 100);
		jTableNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
		jTableNormatives.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneNormatives.getWidth() * 45 / 100);
		
		RefreshTable();
		
		jTableArticlesStart.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableArticlesStart.getSelectedRow() == -1)
					return;
				
				int rowStartId = jTableArticlesStart.convertRowIndexToModel(jTableArticlesStart.getSelectedRow());
				int articleStartId = articlesStartIdList.get(rowStartId);
				
				int articleDestId;
				if (jTableArticlesDest.getSelectedRow() == -1){
					articleDestId = -1;
				} else {
					int rowDestId = jTableArticlesDest.convertRowIndexToModel(jTableArticlesDest.getSelectedRow());
					articleDestId = articlesDestIdList.get(rowDestId);
				}
				
				RefreshArticleNormatives(articleStartId, articleDestId);
			}
		});
		
		jTableArticlesDest.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableArticlesDest.getSelectedRow() == -1)
					return;
				
				int rowDestId = jTableArticlesDest.convertRowIndexToModel(jTableArticlesDest.getSelectedRow());
				int articleDestId = articlesDestIdList.get(rowDestId);
				
				int articleStartId;
				if (jTableArticlesStart.getSelectedRow() == -1){
					articleStartId = -1;
				} else {
					int rowStartId = jTableArticlesStart.convertRowIndexToModel(jTableArticlesStart.getSelectedRow());
					articleStartId = articlesStartIdList.get(rowStartId);
				}
				
				RefreshArticleNormatives(articleStartId, articleDestId);
			}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(Licence.GetOfficeNumber());
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "SELECT ARTICLES.ID, ARTICLES.NAME, MEASURING_UNITS.NAME, ARTICLES.CUSTOM_ID "
					+ "FROM ARTICLES "
					+ "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "WHERE ARTICLES.IS_DELETED = 0";
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
						CustomTableModel customTableModel = new CustomTableModel();
						customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Mjerna jedinica"});
						ArrayList<Integer> idList = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[3];
							rowData[0] = customIdEnabled ? databaseQueryResult.getString(3) : databaseQueryResult.getString(0);
							rowData[1] = databaseQueryResult.getString(1);
							rowData[2] = databaseQueryResult.getString(2);
							customTableModel.addRow(rowData);
							idList.add(databaseQueryResult.getInt(0));
						}
						jTableArticlesStart.setModel(customTableModel);
						articlesStartIdList = idList;

						jTableArticlesStart.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticlesStart.getWidth() * 20 / 100);
						jTableArticlesStart.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticlesStart.getWidth() * 50 / 100);
						jTableArticlesStart.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneArticlesStart.getWidth() * 30 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT ARTICLES.ID, ARTICLES.NAME, MEASURING_UNITS.NAME, ARTICLES.CUSTOM_ID "
					+ "FROM ARTICLES "
					+ "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "WHERE ARTICLES.IS_DELETED = 0";
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
						CustomTableModel customTableModel = new CustomTableModel();
						customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Mjerna jedinica"});
						ArrayList<Integer> idList = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[3];
							rowData[0] = customIdEnabled ? databaseQueryResult.getString(3) : databaseQueryResult.getString(0);
							rowData[1] = databaseQueryResult.getString(1);
							rowData[2] = databaseQueryResult.getString(2);
							customTableModel.addRow(rowData);
							idList.add(databaseQueryResult.getInt(0));
						}
						jTableArticlesDest.setModel(customTableModel);
						articlesDestIdList = idList;

						jTableArticlesDest.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneArticlesDest.getWidth() * 20 / 100);
						jTableArticlesDest.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneArticlesDest.getWidth() * 50 / 100);
						jTableArticlesDest.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneArticlesDest.getWidth() * 30 / 100);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	private void RefreshArticleNormatives(int startArticleId, int destArticleId){
		ArrayList<String> startMaterialsList = new ArrayList<>();
		ArrayList<String> destMaterialsList = new ArrayList<>();
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT MATERIALS.NAME, NORMATIVES.AMOUNT, MEASURING_UNITS.NAME "
					+ "FROM NORMATIVES "
					+ "INNER JOIN MATERIALS ON NORMATIVES.MATERIAL_ID = MATERIALS.ID "
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "WHERE NORMATIVES.ARTICLE_ID = ? AND NORMATIVES.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, startArticleId);
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
							startMaterialsList.add(databaseQueryResult.getString(0) + " (" + databaseQueryResult.getString(1) + ", " + databaseQueryResult.getString(2) + ")");
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT MATERIALS.NAME, NORMATIVES.AMOUNT, MEASURING_UNITS.NAME "
					+ "FROM NORMATIVES "
					+ "INNER JOIN MATERIALS ON NORMATIVES.MATERIAL_ID = MATERIALS.ID "
					+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
					+ "WHERE NORMATIVES.ARTICLE_ID = ? AND NORMATIVES.IS_DELETED = 0";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, destArticleId);
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
							destMaterialsList.add(databaseQueryResult.getString(0) + " (" + databaseQueryResult.getString(1) + ", " + databaseQueryResult.getString(2) + ")");
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			CustomTableModel customTableModel = new CustomTableModel();
			customTableModel.setColumnIdentifiers(new String[] {"Izlazni materijali (normativ, mj. jedinica)", "", "Ulazni materijali (normativ, mj. jedinica)"});
			int minIndex = startMaterialsList.size();
			if(destMaterialsList.size() < minIndex){
				minIndex = destMaterialsList.size();
			}

			for (int i = 0; i < minIndex; ++i){
				Object[] rowData = new Object[3];
				rowData[0] = startMaterialsList.get(i);
				rowData[1] = "";
				rowData[2] = destMaterialsList.get(i);
				customTableModel.addRow(rowData);
			}

			if(startMaterialsList.size() < destMaterialsList.size()){
				for (int i = minIndex; i < destMaterialsList.size(); ++i){
					Object[] rowData = new Object[3];
					rowData[0] = "";
					rowData[1] = "";
					rowData[2] = destMaterialsList.get(i);
					customTableModel.addRow(rowData);
				}
			} else {
				for (int i = minIndex; i < startMaterialsList.size(); ++i){
					Object[] rowData = new Object[3];
					rowData[0] = startMaterialsList.get(i);
					rowData[1] = "";
					rowData[2] = "";
					customTableModel.addRow(rowData);
				}
			}

			jTableNormatives.setModel(customTableModel);
			jTableNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneNormatives.getWidth() * 45 / 100);
			jTableNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
			jTableNormatives.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneNormatives.getWidth() * 45 / 100);
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

        jPanelButtons = new javax.swing.JPanel();
        jButtonSelect = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneArticlesStart = new javax.swing.JScrollPane();
        jTableArticlesStart = new javax.swing.JTable();
        jScrollPaneArticlesDest = new javax.swing.JScrollPane();
        jTableArticlesDest = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneNormatives = new javax.swing.JScrollPane();
        jTableNormatives = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Odabir materijala");
        setResizable(false);

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSelect.setText("<html> <div style=\"text-align: center\"> Odaberi <br> [ENTER] </div> </html>");
        jButtonSelect.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj novi <br> artikl <br> [INS] </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Filter izlazni artikl:");

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
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Odabir artikala"));

        jTableArticlesStart.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneArticlesStart.setViewportView(jTableArticlesStart);

        jTableArticlesDest.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneArticlesDest.setViewportView(jTableArticlesDest);

        jLabel2.setText("--- >");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneArticlesStart, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jScrollPaneArticlesDest, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(126, 126, 126))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPaneArticlesStart, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                            .addComponent(jScrollPaneArticlesDest, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Prikaz materijala"));

        jTableNormatives.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableNormatives.setEnabled(false);
        jTableNormatives.setFocusable(false);
        jTableNormatives.setRowSelectionAllowed(false);
        jScrollPaneNormatives.setViewportView(jTableNormatives);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneNormatives)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneNormatives, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setText("Filter odredišni artikl:");

        jTextField2.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
		if(jTableArticlesStart.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite izlazni artikl iz tablice");
            return;
        }
		if(jTableArticlesDest.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite odredišni artikl iz tablice");
            return;
        }
		
		int rowIdStart = jTableArticlesStart.convertRowIndexToModel(jTableArticlesStart.getSelectedRow());
		int rowIdDest = jTableArticlesDest.convertRowIndexToModel(jTableArticlesDest.getSelectedRow());
        selectedIdStart = articlesStartIdList.get(rowIdStart);
		selectedIdDest = articlesDestIdList.get(rowIdDest);
		
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		selectedIdStart = -1;
		selectedIdDest = -1;
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        ClientAppWarehouseArticlesAddEditDialog addEditdialog = new ClientAppWarehouseArticlesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableArticlesStart.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableArticlesStart.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
		String searchString = jTextField2.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableArticlesDest.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableArticlesDest.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField2KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneArticlesDest;
    private javax.swing.JScrollPane jScrollPaneArticlesStart;
    private javax.swing.JScrollPane jScrollPaneNormatives;
    private javax.swing.JTable jTableArticlesDest;
    private javax.swing.JTable jTableArticlesStart;
    private javax.swing.JTable jTableNormatives;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
