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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppTransfersMaterialAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int transferId;
	private int currentYear;
	
	private int transferMaterialId;
	private int transferMaterialMaterialId;
	private float startAmount;
	private float lastPrice;
	private int startOfficeId, destinationOfficeId;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppTransfersMaterialAddEditDialog(java.awt.Frame parent, boolean modal, int transferId, int transferMaterialId, int transferMaterialMaterialId, int startOfficeId, int destinationOfficeId, int currentYear) {
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
						jButtonExitActionPerformed(null);
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonSaveActionPerformed(null);
					}
				}
				
				return false;
			}
		});
		
		this.transferMaterialId = transferMaterialId;
		this.transferMaterialMaterialId = transferMaterialMaterialId;
		this.transferId = transferId;
		this.startOfficeId = startOfficeId;
		this.destinationOfficeId = destinationOfficeId;
		this.currentYear = currentYear;
		
		SetupDialogItemName();
		
		if(transferMaterialId != -1){
			SetupDialogForEdit();
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogItemName(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		String query = "SELECT MATERIALS.NAME, MEASURING_UNITS.NAME, MATERIALS.LAST_PRICE "
				+ "FROM (MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "WHERE MATERIALS.ID = ? FETCH FIRST ROW ONLY";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, transferMaterialMaterialId);
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
						lastPrice = databaseQueryResult.getFloat(2);
						jLabelPrice.setText(ClientAppUtils.FloatToPriceString(lastPrice));
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
		jLabelTitle.setText("Uredi materijal");
		setTitle("Uredi materijal");
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT AMOUNT_START, PRICE FROM TRANSFER_MATERIALS WHERE ID = ?");
			databaseQuery.AddParam(1, transferMaterialId);
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
							jLabelPrice.setText(ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(1)));
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
        jLabel4 = new javax.swing.JLabel();
        jLabelPrice = new javax.swing.JLabel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj materijal");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj materijal");

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

        jLabel4.setText("Nabavna cijena:");
        jLabel4.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelPrice.setText("cijena");

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
                            .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelPrice)))
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
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPrice))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        if(jFormattedTextFieldAmount.getValue() == null){
            ClientAppLogger.GetInstance().ShowMessage("Količina nije ispravnog oblika");
            return;
        }
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(startOfficeId);
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(destinationOfficeId);
		
		if(transferMaterialId != -1 && amount == startAmount)
			return;
		
		{
			int multiDatabaseQueryLength;
			if (transferMaterialId == -1) {
				if(amount != startAmount){
					multiDatabaseQueryLength = 4;
				} else {
					multiDatabaseQueryLength = 1;
				}
			} else {
				if(amount != startAmount){
					multiDatabaseQueryLength = 3;
				} else {
					multiDatabaseQueryLength = 0;
				}
			}
			
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(multiDatabaseQueryLength);
			
			if(transferMaterialId == -1){
				String queryInsert = "INSERT INTO TRANSFER_MATERIALS (ID, TRANSFER_ID, MATERIAL_ID, AMOUNT_START, PRICE, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
				multiDatabaseQuery.SetQuery(0, queryInsert);
				multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "TRANSFER_MATERIALS");
				multiDatabaseQuery.AddParam(0, 2, transferId);
				multiDatabaseQuery.AddParam(0, 3, transferMaterialMaterialId);
				multiDatabaseQuery.AddParam(0, 4, 0);
				multiDatabaseQuery.AddParam(0, 5, lastPrice);
				multiDatabaseQuery.AddParam(0, 6, 0);
			}
			
			if(amount != startAmount){
				String queryUpdateStartingMaterialAmountMinus = "UPDATE MATERIAL_AMOUNTS "
						+ "SET AMOUNT = AMOUNT - (? - (SELECT AMOUNT_START FROM TRANSFER_MATERIALS WHERE ID = ?)) "
						+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
						+ "AND (SELECT IS_DELETED FROM TRANSFERS WHERE ID = ?) = 0 "
						+ "AND (SELECT IS_DELETED FROM TRANSFER_MATERIALS WHERE ID = ?) = 0 "
						+ "AND AMOUNT_YEAR = ?";
				if(transferMaterialId == -1){
					multiDatabaseQuery.SetQuery(1, queryUpdateStartingMaterialAmountMinus);
					multiDatabaseQuery.AddParam(1, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(1, 2, 0);
					multiDatabaseQuery.AddParam(1, 3, transferMaterialMaterialId);
					multiDatabaseQuery.AddParam(1, 4, startOfficeId);
					multiDatabaseQuery.AddParam(1, 5, transferId);
					multiDatabaseQuery.AddAutoGeneratedParam(1, 6, 0);
					multiDatabaseQuery.AddParam(1, 7, currentYear);
				} else {
					multiDatabaseQuery.SetQuery(0, queryUpdateStartingMaterialAmountMinus);
					multiDatabaseQuery.AddParam(0, 1, amount);
					multiDatabaseQuery.AddParam(0, 2, transferMaterialId);
					multiDatabaseQuery.AddParam(0, 3, transferMaterialMaterialId);
					multiDatabaseQuery.AddParam(0, 4, startOfficeId);
					multiDatabaseQuery.AddParam(0, 5, transferId);
					multiDatabaseQuery.AddParam(0, 6, transferMaterialId);
					multiDatabaseQuery.AddParam(0, 7, currentYear);
				}
				
				String queryUpdateDestinationMaterialAmountPlusIfDelivered = "UPDATE MATERIAL_AMOUNTS "
						+ "SET AMOUNT = AMOUNT + (? - (SELECT AMOUNT_START FROM TRANSFER_MATERIALS WHERE ID = ?)) "
						+ "WHERE MATERIAL_ID = ? AND OFFICE_NUMBER = ? "
						+ "AND (SELECT IS_DELIVERED FROM TRANSFERS WHERE ID = ?) = 1 "
						+ "AND (SELECT IS_DELETED FROM TRANSFERS WHERE ID = ?) = 0 "
						+ "AND (SELECT IS_DELETED FROM TRANSFER_MATERIALS WHERE ID = ?) = 0 "
						+ "AND AMOUNT_YEAR = ?";
				if(transferMaterialId == -1){
					multiDatabaseQuery.SetQuery(2, queryUpdateDestinationMaterialAmountPlusIfDelivered);
					multiDatabaseQuery.AddParam(2, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(2, 2, 0);
					multiDatabaseQuery.AddParam(2, 3, transferMaterialMaterialId);
					multiDatabaseQuery.AddParam(2, 4, destinationOfficeId);
					multiDatabaseQuery.AddParam(2, 5, transferId);
					multiDatabaseQuery.AddParam(2, 6, transferId);
					multiDatabaseQuery.AddAutoGeneratedParam(2, 7, 0);
					multiDatabaseQuery.AddParam(2, 8, currentYear);
				} else {
					multiDatabaseQuery.SetQuery(1, queryUpdateDestinationMaterialAmountPlusIfDelivered);
					multiDatabaseQuery.AddParam(1, 1, amount);
					multiDatabaseQuery.AddParam(1, 2, transferMaterialId);
					multiDatabaseQuery.AddParam(1, 3, transferMaterialMaterialId);
					multiDatabaseQuery.AddParam(1, 4, destinationOfficeId);
					multiDatabaseQuery.AddParam(1, 5, transferId);
					multiDatabaseQuery.AddParam(1, 6, transferId);
					multiDatabaseQuery.AddParam(1, 7, transferMaterialId);
					multiDatabaseQuery.AddParam(1, 8, currentYear);
				}
				
				String queryUpdate = "UPDATE TRANSFER_MATERIALS SET AMOUNT_START = ? WHERE ID = ?";
				if(transferMaterialId == -1){
					multiDatabaseQuery.SetQuery(3, queryUpdate);
					multiDatabaseQuery.AddParam(3, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(3, 2, 0);
				} else {
					multiDatabaseQuery.SetQuery(2, queryUpdate);
					multiDatabaseQuery.AddParam(2, 1, amount);
					multiDatabaseQuery.AddParam(2, 2, transferMaterialId);
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
    private javax.swing.JFormattedTextField jFormattedTextFieldAmount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelMaterialName;
    private javax.swing.JLabel jLabelMeasuringUnit;
    private javax.swing.JLabel jLabelPrice;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
