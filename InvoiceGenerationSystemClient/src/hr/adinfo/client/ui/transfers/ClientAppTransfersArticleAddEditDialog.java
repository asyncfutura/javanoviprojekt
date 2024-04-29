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
public class ClientAppTransfersArticleAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int transferId;
	private int currentYear;
	
	private int transferArticleId;
	private int transferArticleStartArticleId;
	private int transferArticleDestArticleId;
	private float startAmount;
	private float price;
	private int startOfficeId, destinationOfficeId;
			
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppTransfersArticleAddEditDialog(java.awt.Frame parent, boolean modal, int transferId, int transferArticleId, int transferArticleStartArticleId, int transferArticleDestArticleId, int startOfficeId, int destinationOfficeId, int currentYear) {
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
		
		this.transferArticleId = transferArticleId;
		this.transferArticleStartArticleId = transferArticleStartArticleId;
		this.transferArticleDestArticleId = transferArticleDestArticleId;
		this.transferId = transferId;
		this.startOfficeId = startOfficeId;
		this.destinationOfficeId = destinationOfficeId;
		this.currentYear = currentYear;
		
		SetupDialogItemName();
		
		if(transferArticleId != -1){
			SetupDialogForEdit();
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogItemName(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		String query = "SELECT ARTICLES_1.NAME, ARTICLES_2.NAME, ARTICLES_1.PRICE "
				+ "FROM ARTICLES ARTICLES_1, ARTICLES ARTICLES_2 "
				+ "WHERE ARTICLES_1.ID = ? AND ARTICLES_2.ID = ? FETCH FIRST ROW ONLY";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, transferArticleStartArticleId);
		databaseQuery.AddParam(2, transferArticleDestArticleId);
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
						jLabelArticleNameStart.setText(databaseQueryResult.getString(0));
						jLabelArticleNameDest.setText(databaseQueryResult.getString(1));
						jLabelPrice.setText(ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2)));
						price = databaseQueryResult.getFloat(2);
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
		jLabelTitle.setText("Uredi artikl");
		setTitle("Uredi artikl");
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT AMOUNT_START, PRICE FROM TRANSFER_ARTICLES WHERE ID = ?");
			databaseQuery.AddParam(1, transferArticleId);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabelTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelArticleNameStart = new javax.swing.JLabel();
        jFormattedTextFieldAmount = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabelPrice = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelArticleNameDest = new javax.swing.JLabel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj artikl");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj artikl");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Izlazni artikl:");
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel2.setText("Količina:");
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelArticleNameStart.setText("naziv");

        jFormattedTextFieldAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00000"))));
        jFormattedTextFieldAmount.setPreferredSize(new java.awt.Dimension(200, 25));

        jLabel4.setText("Cijena izlaznog artikla:");
        jLabel4.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelPrice.setText("cijena");

        jLabel5.setText("Ulazni artikl:");
        jLabel5.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelArticleNameDest.setText("naziv");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelArticleNameStart)
                            .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelArticleNameDest)))
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
                    .addComponent(jLabelArticleNameStart))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelArticleNameDest))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPrice))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTitle)
                .addGap(152, 152, 152))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabelTitle)
                .addGap(32, 32, 32)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
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
		
		if(transferArticleId != -1 && amount == startAmount)
			return;
		
		{
			int multiDatabaseQueryLength;
			if (transferArticleId == -1) {
				if(amount != startAmount){
					multiDatabaseQueryLength = 6;
				} else {
					multiDatabaseQueryLength = 3;
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
			if(transferArticleId == -1){
				String insertQuery = "INSERT INTO TRANSFER_ARTICLES (ID, TRANSFER_ID, STARTING_ARTICLE_ID, DESTINATION_ARTICLE_ID, AMOUNT_START, PRICE, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
				multiDatabaseQuery.SetQuery(0, insertQuery);
				multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "TRANSFER_ARTICLES");
				multiDatabaseQuery.AddParam(0, 2, transferId);
				multiDatabaseQuery.AddParam(0, 3, transferArticleStartArticleId);
				multiDatabaseQuery.AddParam(0, 4, transferArticleDestArticleId);
				multiDatabaseQuery.AddParam(0, 5, 0);
				multiDatabaseQuery.AddParam(0, 6, price);
				multiDatabaseQuery.AddParam(0, 7, 0);
				
				String queryInsertArticleMaterials = "INSERT INTO TRANSFER_ARTICLE_MATERIALS "
						+ "(ID, TRANSFER_ARTICLE_ID, MATERIAL_ID, NORMATIVE, IS_STARTING, IS_DELETED) "
						+ "SELECT ROW_NUMBER() OVER () + ?, ?, NORMATIVES.MATERIAL_ID, NORMATIVES.AMOUNT, ?, ? "
						+ "FROM NORMATIVES "
						+ "WHERE NORMATIVES.ARTICLE_ID = ? AND NORMATIVES.IS_DELETED = 0 "
						+ "ORDER BY NORMATIVES.ID";
				multiDatabaseQuery.SetQuery(1, queryInsertArticleMaterials);
				multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "TRANSFER_ARTICLE_MATERIALS");
				multiDatabaseQuery.AddAutoGeneratedParam(1, 2, 0);
				multiDatabaseQuery.AddParam(1, 3, 1);
				multiDatabaseQuery.AddParam(1, 4, 0);
				multiDatabaseQuery.AddParam(1, 5, transferArticleStartArticleId);
				
				multiDatabaseQuery.SetQuery(2, queryInsertArticleMaterials);
				multiDatabaseQuery.SetAutoIncrementParam(2, 1, "ID", "TRANSFER_ARTICLE_MATERIALS");
				multiDatabaseQuery.AddAutoGeneratedParam(2, 2, 0);
				multiDatabaseQuery.AddParam(2, 3, 0);
				multiDatabaseQuery.AddParam(2, 4, 0);
				multiDatabaseQuery.AddParam(2, 5, transferArticleDestArticleId);
			}

			if(amount != startAmount){
				String queryUpdateStartingArticleMaterialAmountsMinus = ""
					+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
						+ "SELECT SUM("
							+ "(? - (SELECT AMOUNT_START FROM TRANSFER_ARTICLES WHERE ID = ?)) * TRANSFER_ARTICLE_MATERIALS.NORMATIVE"
						+ ") "
						+ "FROM TRANSFER_ARTICLE_MATERIALS "
						+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
						+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
						+ "WHERE TRANSFER_ARTICLES.ID = ? "
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
						+ "WHERE TRANSFER_ARTICLES.ID = ? "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
						+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
						+ "AND TRANSFERS.IS_DELETED = 0 "
						+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
					+ ") "
					+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
				
				if(transferArticleId == -1){
					multiDatabaseQuery.SetQuery(3, queryUpdateStartingArticleMaterialAmountsMinus);
					multiDatabaseQuery.AddParam(3, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(3, 2, 0);
					multiDatabaseQuery.AddAutoGeneratedParam(3, 3, 0);
					multiDatabaseQuery.AddAutoGeneratedParam(3, 4, 0);
					multiDatabaseQuery.AddParam(3, 5, startOfficeId);
					multiDatabaseQuery.AddParam(3, 6, currentYear);
				} else {
					multiDatabaseQuery.SetQuery(0, queryUpdateStartingArticleMaterialAmountsMinus);
					multiDatabaseQuery.AddParam(0, 1, amount);
					multiDatabaseQuery.AddParam(0, 2, transferArticleId);
					multiDatabaseQuery.AddParam(0, 3, transferArticleId);
					multiDatabaseQuery.AddParam(0, 4, transferArticleId);
					multiDatabaseQuery.AddParam(0, 5, startOfficeId);
					multiDatabaseQuery.AddParam(0, 6, currentYear);
				}

				String queryUpdateDestinationArticleMaterialAmountsPlusIfDelivered = ""
					+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
						+ "SELECT SUM("
							+ "(? - (SELECT AMOUNT_START FROM TRANSFER_ARTICLES WHERE ID = ?)) * TRANSFER_ARTICLE_MATERIALS.NORMATIVE"
						+ ") "
						+ "FROM TRANSFER_ARTICLE_MATERIALS "
						+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
						+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
						+ "WHERE TRANSFER_ARTICLES.ID = ? "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
						+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
						+ "AND TRANSFERS.IS_DELETED = 0 "
						+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID "
						+ "AND TRANSFERS.IS_DELIVERED = 1"
					+ ") "
					+ "WHERE EXISTS ("
						+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
						+ "FROM TRANSFER_ARTICLE_MATERIALS "
						+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
						+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
						+ "WHERE TRANSFER_ARTICLES.ID = ? "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
						+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
						+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
						+ "AND TRANSFERS.IS_DELETED = 0 "
						+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID "
						+ "AND TRANSFERS.IS_DELIVERED = 1"
					+ ") "
					+ "AND OFFICE_NUMBER = ? AND AMOUNT_YEAR = ?";
				
				if(transferArticleId == -1){
					multiDatabaseQuery.SetQuery(4, queryUpdateDestinationArticleMaterialAmountsPlusIfDelivered);
					multiDatabaseQuery.AddParam(4, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(4, 2, 0);
					multiDatabaseQuery.AddAutoGeneratedParam(4, 3, 0);
					multiDatabaseQuery.AddAutoGeneratedParam(4, 4, 0);
					multiDatabaseQuery.AddParam(4, 5, destinationOfficeId);
					multiDatabaseQuery.AddParam(4, 6, currentYear);
				} else {
					multiDatabaseQuery.SetQuery(1, queryUpdateDestinationArticleMaterialAmountsPlusIfDelivered);
					multiDatabaseQuery.AddParam(1, 1, amount);
					multiDatabaseQuery.AddParam(1, 2, transferArticleId);
					multiDatabaseQuery.AddParam(1, 3, transferArticleId);
					multiDatabaseQuery.AddParam(1, 4, transferArticleId);
					multiDatabaseQuery.AddParam(1, 5, destinationOfficeId);
					multiDatabaseQuery.AddParam(1, 6, currentYear);
				}
				
				String updateQuery = "UPDATE TRANSFER_ARTICLES SET AMOUNT_START = ? WHERE ID = ?";
				if(transferArticleId == -1){
					multiDatabaseQuery.SetQuery(5, updateQuery);
					multiDatabaseQuery.AddParam(5, 1, amount);
					multiDatabaseQuery.AddAutoGeneratedParam(5, 2, 0);
				} else {
					multiDatabaseQuery.SetQuery(2, updateQuery);
					multiDatabaseQuery.AddParam(2, 1, amount);
					multiDatabaseQuery.AddParam(2, 2, transferArticleId);
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JFormattedTextField jFormattedTextFieldAmount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelArticleNameDest;
    private javax.swing.JLabel jLabelArticleNameStart;
    private javax.swing.JLabel jLabelPrice;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
