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
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseNormativesMaterialAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int articleId;	
	private int normativeId;
	private int materialId;
	private float startAmount;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppWarehouseNormativesMaterialAddEditDialog(java.awt.Frame parent, boolean modal, int articleId, int normativeId, int materialId) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonSave.doClick();
					}
				}
				
				return false;
			}
		});
		
		this.normativeId = normativeId;
		this.materialId = materialId;
		this.articleId = articleId;
		
		SetupDialogItemName();
		
		if(normativeId != -1){
			SetupDialogForEdit();
		}
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN){
			jCheckBox1.setVisible(false);
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogItemName(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT MATERIALS.NAME, MEASURING_UNITS.NAME "
				+ "FROM (MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "WHERE MATERIALS.ID = ? FETCH FIRST ROW ONLY";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, materialId);
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
						jLabelMaterialName.setText(databaseQueryResult.getString(0));
						jLabelMeasuringUnit.setText(databaseQueryResult.getString(1));
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
	
	private void SetupDialogForEdit(){
		jLabelTitle.setText("Uredi normativ");
		setTitle("Uredi normativ");
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT AMOUNT FROM NORMATIVES WHERE ID = ?");
			databaseQuery.AddParam(1, normativeId);
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
							startAmount = databaseQueryResult.getFloat(0);
							jFormattedTextFieldAmount.setValue(startAmount);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelMaterialName = new javax.swing.JLabel();
        jFormattedTextFieldAmount = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabelMeasuringUnit = new javax.swing.JLabel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj normativ");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj normativ");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Naziv:");
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel2.setText("Količina:");
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelMaterialName.setText("naziv");

        jFormattedTextFieldAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00000"))));
        jFormattedTextFieldAmount.setPreferredSize(new java.awt.Dimension(200, 25));

        jLabel3.setText("Mjerna jedinica:");
        jLabel3.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelMeasuringUnit.setText("mj. jed.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelMeasuringUnit))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelMaterialName)
                            .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMaterialName))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMeasuringUnit))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(84, 78));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(84, 78));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Promjena normativa unatrag do početka godine");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTitle)
                .addGap(138, 138, 138))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(97, 97, 97)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabelTitle)
                .addGap(34, 34, 34)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		try {
			jFormattedTextFieldAmount.commitEdit();
		} catch (ParseException ex){
			ClientAppLogger.GetInstance().ShowMessage("Količina nije ispravnog oblika");
			return;
		}
		
		if(jFormattedTextFieldAmount.getValue() == null){
            ClientAppLogger.GetInstance().ShowMessage("Količina nije ispravnog oblika");
            return;
        }
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		
		// Check if material already in normatives
		if(normativeId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
		
			String query = "SELECT ID FROM NORMATIVES WHERE ARTICLE_ID = ? AND MATERIAL_ID = ? AND IS_DELETED = 0";
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, articleId);
			databaseQuery.AddParam(2, materialId);
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
							ClientAppLogger.GetInstance().ShowMessage("Odabrani materijal već postoji u normativu ovog artikla");
							return;
						}
					} else {
						return;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					return;
				}
			}
		}
		
		boolean fromYearStart = StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN && jCheckBox1.isSelected();
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery;
			if(normativeId == -1){
				multiDatabaseQuery = new MultiDatabaseQuery(fromYearStart ? 11 : 2);
				
				String query = "INSERT INTO NORMATIVES (ID, ARTICLE_ID, MATERIAL_ID, AMOUNT, IS_DELETED) "
						+ "SELECT ?, ?, ?, ?, ? FROM NORMATIVES WHERE ARTICLE_ID = ? AND MATERIAL_ID = ? AND IS_DELETED = 0 HAVING COUNT(*) = 0 ";
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "NORMATIVES");
				multiDatabaseQuery.AddParam(0, 2, articleId);
				multiDatabaseQuery.AddParam(0, 3, materialId);
				multiDatabaseQuery.AddParam(0, 4, amount);
				multiDatabaseQuery.AddParam(0, 5, 0);
				multiDatabaseQuery.AddParam(0, 6, articleId);
				multiDatabaseQuery.AddParam(0, 7, materialId);
				
				Date date = new Date();
				multiDatabaseQuery.SetQuery(1, ClientAppUtils.CHANGES_LOG_QUERY);
				multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "CHANGES_LOG");
				multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
				multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("HH:mm:ss").format(date));
				multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 5, StaffUserInfo.GetCurrentUserInfo().userId);
				multiDatabaseQuery.AddParam(1, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
				multiDatabaseQuery.AddParam(1, 7, "Dodavanje normativa" + (fromYearStart ? " - od početka godine" : ""));
				multiDatabaseQuery.AddParam(1, 8, "Artikl id " + articleId + ", materijal " + materialId + " " + jLabelMaterialName.getText() + ", količina " + amount);
				
				if(fromYearStart){
					String updateInvoiceAmounts = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
							+ "SELECT SUM(INVOICE_ITEMS.AMT * ?) "
							+ "FROM INVOICE_ITEMS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
							+ "WHERE INVOICE_ITEMS.IT_ID = ? AND INVOICE_ITEMS.IT_TYPE = ? "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT INVOICE_ITEMS.AMT "
							+ "FROM INVOICE_ITEMS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
							+ "WHERE INVOICE_ITEMS.IT_ID = ? AND INVOICE_ITEMS.IT_TYPE = ? "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM"
						+ ") "
						+ "AND AMOUNT_YEAR = ? AND MATERIAL_ID = ?";
					multiDatabaseQuery.SetQuery(2, updateInvoiceAmounts);
					multiDatabaseQuery.AddParam(2, 1, amount);
					multiDatabaseQuery.AddParam(2, 2, articleId);
					multiDatabaseQuery.AddParam(2, 3, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(2, 4, articleId);
					multiDatabaseQuery.AddParam(2, 5, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(2, 6, ClientAppSettings.currentYear);
					multiDatabaseQuery.AddParam(2, 7, materialId);

					String updateInvoiceMaterials = "INSERT INTO INVOICE_MATERIALS (ID, IN_ID, ART_ID, MAT_ID, AMT, NORM) "
							+ "SELECT ROW_NUMBER() OVER() + ?, IN_ID, ?, ?, AMT, ? "
							+ "FROM INVOICE_ITEMS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_ITEMS.IN_ID "
							+ "WHERE INVOICE_ITEMS.IT_ID = ? AND INVOICE_ITEMS.IT_TYPE = ? "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND YEAR(INVOICES.I_DATE) = ? ";
					multiDatabaseQuery.SetQuery(3, updateInvoiceMaterials);
					multiDatabaseQuery.SetAutoIncrementParam(3, 1, "ID", "INVOICE_MATERIALS");
					multiDatabaseQuery.AddParam(3, 2, articleId);
					multiDatabaseQuery.AddParam(3, 3, materialId);
					multiDatabaseQuery.AddParam(3, 4, amount);
					multiDatabaseQuery.AddParam(3, 5, articleId);
					multiDatabaseQuery.AddParam(3, 6, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(3, 7, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterials = "INSERT INTO LOCAL_INVOICE_MATERIALS (ID, IN_ID, ART_ID, MAT_ID, AMT, NORM, IS_DELETED) "
							+ "SELECT ROW_NUMBER() OVER() + ?, IN_ID, ?, ?, AMT, ?, 0 "
							+ "FROM LOCAL_INVOICE_ITEMS "
							+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
							+ "WHERE LOCAL_INVOICE_ITEMS.IT_ID = ? AND LOCAL_INVOICE_ITEMS.IT_TYPE = ? "
							+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND YEAR(LOCAL_INVOICES.I_DATE) = ? ";
					multiDatabaseQuery.SetQuery(4, updateLocalInvoiceMaterials);
					multiDatabaseQuery.SetAutoIncrementParam(4, 1, "ID", "LOCAL_INVOICE_MATERIALS");
					multiDatabaseQuery.AddParam(4, 2, articleId);
					multiDatabaseQuery.AddParam(4, 3, materialId);
					multiDatabaseQuery.AddParam(4, 4, amount);
					multiDatabaseQuery.AddParam(4, 5, articleId);
					multiDatabaseQuery.AddParam(4, 6, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(4, 7, ClientAppSettings.currentYear);
										
					String updateInvoiceMaterialsTest = updateInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
					multiDatabaseQuery.SetQuery(5, updateInvoiceMaterialsTest);
					multiDatabaseQuery.SetAutoIncrementParam(5, 1, "ID", "INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.AddParam(5, 2, articleId);
					multiDatabaseQuery.AddParam(5, 3, materialId);
					multiDatabaseQuery.AddParam(5, 4, amount);
					multiDatabaseQuery.AddParam(5, 5, articleId);
					multiDatabaseQuery.AddParam(5, 6, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(5, 7, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterialsTest = updateLocalInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
					multiDatabaseQuery.SetQuery(6, updateLocalInvoiceMaterialsTest);
					multiDatabaseQuery.SetAutoIncrementParam(6, 1, "ID", "LOCAL_INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.AddParam(6, 2, articleId);
					multiDatabaseQuery.AddParam(6, 3, materialId);
					multiDatabaseQuery.AddParam(6, 4, amount);
					multiDatabaseQuery.AddParam(6, 5, articleId);
					multiDatabaseQuery.AddParam(6, 6, Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE);
					multiDatabaseQuery.AddParam(6, 7, ClientAppSettings.currentYear);
					
					String queryUpdateAllStartingArticleMaterialAmountsMinus = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
							+ "SELECT SUM(? * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLES.AMOUNT_START "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ? AND MATERIAL_ID = ?";
					multiDatabaseQuery.SetQuery(7, queryUpdateAllStartingArticleMaterialAmountsMinus);
					multiDatabaseQuery.AddParam(7, 1, amount);
					multiDatabaseQuery.AddParam(7, 2, articleId);
					multiDatabaseQuery.AddParam(7, 3, articleId);
					multiDatabaseQuery.AddParam(7, 4, ClientAppSettings.currentYear);
					multiDatabaseQuery.AddParam(7, 5, materialId);
					
					String queryUpdateAllDestinationArticleMaterialAmountsPlusIfDelivered = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
							+ "SELECT SUM(? * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLES.AMOUNT_START "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ? AND MATERIAL_ID = ?";
					multiDatabaseQuery.SetQuery(8, queryUpdateAllDestinationArticleMaterialAmountsPlusIfDelivered);
					multiDatabaseQuery.AddParam(8, 1, amount);
					multiDatabaseQuery.AddParam(8, 2, articleId);
					multiDatabaseQuery.AddParam(8, 3, articleId);
					multiDatabaseQuery.AddParam(8, 4, ClientAppSettings.currentYear);
					multiDatabaseQuery.AddParam(8, 5, materialId);
					
					String updateTransferNormativesStarting = "INSERT INTO TRANSFER_ARTICLE_MATERIALS (ID, TRANSFER_ARTICLE_ID, MATERIAL_ID, "
							+ "NORMATIVE, IS_STARTING, IS_DELETED) "
							+ "SELECT ROW_NUMBER() OVER() + ?, TRANSFER_ARTICLES.ID, ?, ?, 1, 0 "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND YEAR(TRANSFERS.TRANSFER_START_DATE) = ? ";
					multiDatabaseQuery.SetQuery(9, updateTransferNormativesStarting);
					multiDatabaseQuery.SetAutoIncrementParam(9, 1, "ID", "TRANSFER_ARTICLE_MATERIALS");
					multiDatabaseQuery.AddParam(9, 2, materialId);
					multiDatabaseQuery.AddParam(9, 3, amount);
					multiDatabaseQuery.AddParam(9, 4, articleId);
					multiDatabaseQuery.AddParam(9, 5, ClientAppSettings.currentYear);
					
					String updateTransferNormativesDest = "INSERT INTO TRANSFER_ARTICLE_MATERIALS (ID, TRANSFER_ARTICLE_ID, MATERIAL_ID, "
							+ "NORMATIVE, IS_STARTING, IS_DELETED) "
							+ "SELECT ROW_NUMBER() OVER() + ?, TRANSFER_ARTICLES.ID, ?, ?, 0, 0 "
							+ "FROM TRANSFER_ARTICLES "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND YEAR(TRANSFERS.TRANSFER_START_DATE) = ? ";
					multiDatabaseQuery.SetQuery(10, updateTransferNormativesDest);
					multiDatabaseQuery.SetAutoIncrementParam(10, 1, "ID", "TRANSFER_ARTICLE_MATERIALS");
					multiDatabaseQuery.AddParam(10, 2, materialId);
					multiDatabaseQuery.AddParam(10, 3, amount);
					multiDatabaseQuery.AddParam(10, 4, articleId);
					multiDatabaseQuery.AddParam(10, 5, ClientAppSettings.currentYear);
				}
			} else {
				multiDatabaseQuery = new MultiDatabaseQuery(fromYearStart ? 10 : 2);
				
				String query = "UPDATE NORMATIVES SET AMOUNT = ? WHERE ID = ?";
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.AddParam(0, 1, amount);
				multiDatabaseQuery.AddParam(0, 2, normativeId);
				
				Date date = new Date();
				multiDatabaseQuery.SetQuery(1, ClientAppUtils.CHANGES_LOG_QUERY);
				multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "CHANGES_LOG");
				multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
				multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("HH:mm:ss").format(date));
				multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 5, StaffUserInfo.GetCurrentUserInfo().userId);
				multiDatabaseQuery.AddParam(1, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
				multiDatabaseQuery.AddParam(1, 7, "Izmjena normativa" + (fromYearStart ? " - od početka godine" : ""));
				multiDatabaseQuery.AddParam(1, 8, "Artikl id " + articleId + ", materijal " + materialId + " " + jLabelMaterialName.getText() + ", stara količina " + startAmount + ", nova količina " + amount);
				
				if(fromYearStart){
					String updateInvoiceAmounts = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
							+ "SELECT SUM(INVOICE_MATERIALS.AMT * (INVOICE_MATERIALS.NORM - ?)) "
							+ "FROM INVOICE_MATERIALS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_MATERIALS.IN_ID "
							+ "WHERE INVOICE_MATERIALS.ART_ID = ? AND INVOICE_MATERIALS.MAT_ID = ? "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = INVOICE_MATERIALS.MAT_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT INVOICE_MATERIALS.AMT "
							+ "FROM INVOICE_MATERIALS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_MATERIALS.IN_ID "
							+ "WHERE INVOICE_MATERIALS.ART_ID = ? AND INVOICE_MATERIALS.MAT_ID = ? "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
							+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = INVOICE_MATERIALS.MAT_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(2, updateInvoiceAmounts);
					multiDatabaseQuery.AddParam(2, 1, amount);
					multiDatabaseQuery.AddParam(2, 2, articleId);
					multiDatabaseQuery.AddParam(2, 3, materialId);
					multiDatabaseQuery.AddParam(2, 4, articleId);
					multiDatabaseQuery.AddParam(2, 5, materialId);
					multiDatabaseQuery.AddParam(2, 6, ClientAppSettings.currentYear);
					
					String updateInvoiceMaterials = ""
							+ "UPDATE INVOICE_MATERIALS SET NORM = ? "
							+ "WHERE ART_ID = ? AND MAT_ID = ? "
							+ "AND YEAR((SELECT I_DATE FROM INVOICES WHERE INVOICE_MATERIALS.IN_ID = INVOICES.ID)) = ?";
					multiDatabaseQuery.SetQuery(3, updateInvoiceMaterials);
					multiDatabaseQuery.AddParam(3, 1, amount);
					multiDatabaseQuery.AddParam(3, 2, articleId);
					multiDatabaseQuery.AddParam(3, 3, materialId);
					multiDatabaseQuery.AddParam(3, 4, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterials = ""
							+ "UPDATE LOCAL_INVOICE_MATERIALS SET NORM = ? "
							+ "WHERE ART_ID = ? AND MAT_ID = ? AND IS_DELETED = 0 "
							+ "AND YEAR((SELECT I_DATE FROM LOCAL_INVOICES WHERE LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID)) = ?";
					multiDatabaseQuery.SetQuery(4, updateLocalInvoiceMaterials);
					multiDatabaseQuery.AddParam(4, 1, amount);
					multiDatabaseQuery.AddParam(4, 2, articleId);
					multiDatabaseQuery.AddParam(4, 3, materialId);
					multiDatabaseQuery.AddParam(4, 4, ClientAppSettings.currentYear);
					
					String updateInvoiceMaterialsTest = updateInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.SetQuery(5, updateInvoiceMaterialsTest);
					multiDatabaseQuery.AddParam(5, 1, amount);
					multiDatabaseQuery.AddParam(5, 2, articleId);
					multiDatabaseQuery.AddParam(5, 3, materialId);
					multiDatabaseQuery.AddParam(5, 4, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterialsTest = updateLocalInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.SetQuery(6, updateLocalInvoiceMaterialsTest);
					multiDatabaseQuery.AddParam(6, 1, amount);
					multiDatabaseQuery.AddParam(6, 2, articleId);
					multiDatabaseQuery.AddParam(6, 3, materialId);
					multiDatabaseQuery.AddParam(6, 4, ClientAppSettings.currentYear);
					
					String queryUpdateAllStartingArticleMaterialAmountsPlus = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
							+ "SELECT SUM((TRANSFER_ARTICLE_MATERIALS.NORMATIVE - ?) * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(7, queryUpdateAllStartingArticleMaterialAmountsPlus);
					multiDatabaseQuery.AddParam(7, 1, amount);
					multiDatabaseQuery.AddParam(7, 2, materialId);
					multiDatabaseQuery.AddParam(7, 3, articleId);
					multiDatabaseQuery.AddParam(7, 4, materialId);
					multiDatabaseQuery.AddParam(7, 5, articleId);
					multiDatabaseQuery.AddParam(7, 6, ClientAppSettings.currentYear);
					
					String queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
							+ "SELECT SUM((TRANSFER_ARTICLE_MATERIALS.NORMATIVE - ?) * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(8, queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered);
					multiDatabaseQuery.AddParam(8, 1, amount);
					multiDatabaseQuery.AddParam(8, 2, materialId);
					multiDatabaseQuery.AddParam(8, 3, articleId);
					multiDatabaseQuery.AddParam(8, 4, materialId);
					multiDatabaseQuery.AddParam(8, 5, articleId);
					multiDatabaseQuery.AddParam(8, 6, ClientAppSettings.currentYear);
					
					String updateTransferNormatives = ""
							+ "UPDATE TRANSFER_ARTICLE_MATERIALS SET NORMATIVE = ? "
							+ "WHERE MATERIAL_ID = ? AND IS_DELETED = 0 "
							+ "AND ((SELECT STARTING_ARTICLE_ID FROM TRANSFER_ARTICLES "
								+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID) = ? AND IS_STARTING = 1 "
								+ "OR "
								+ "(SELECT DESTINATION_ARTICLE_ID FROM TRANSFER_ARTICLES "
								+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID) = ? AND IS_STARTING = 0) "
							+ "AND YEAR((SELECT TRANSFER_START_DATE FROM TRANSFERS "
								+ "WHERE ID = (SELECT TRANSFER_ID FROM TRANSFER_ARTICLES "
									+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID))) = ?";
					multiDatabaseQuery.SetQuery(9, updateTransferNormatives);
					multiDatabaseQuery.AddParam(9, 1, amount);
					multiDatabaseQuery.AddParam(9, 2, materialId);
					multiDatabaseQuery.AddParam(9, 3, articleId);
					multiDatabaseQuery.AddParam(9, 4, articleId);
					multiDatabaseQuery.AddParam(9, 5, ClientAppSettings.currentYear);
				}
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
		
		if(changeSuccess){
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFormattedTextField jFormattedTextFieldAmount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelMaterialName;
    private javax.swing.JLabel jLabelMeasuringUnit;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
