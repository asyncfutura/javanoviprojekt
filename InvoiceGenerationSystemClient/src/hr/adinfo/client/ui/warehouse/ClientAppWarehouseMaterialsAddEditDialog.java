/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.warehouse;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseMaterialsAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	private int tableId;
	private ArrayList<Integer> materialCategoriesIdList = new ArrayList<>();
	private ArrayList<Integer> measuringUnitsIdList = new ArrayList<>();
	private ArrayList<Integer> taxRatesIdList = new ArrayList<>();
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppWarehouseMaterialsAddEditDialog(java.awt.Frame parent, boolean modal, int tableId) {
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
		
		// Default values setup
		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
		defaultComboBoxModel.addElement("Izaberite");
		jComboBoxMaterialCategories.setModel(defaultComboBoxModel);
		jComboBoxMeasuringUnits.setModel(defaultComboBoxModel);
		
		jCheckBox1.setSelected(false);
		jCheckBox1ItemStateChanged(null);
		
		SetupDialogComboBoxes();
		
		this.tableId = tableId;
		if(tableId != -1){
			SetupDialogForEdit();
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogComboBoxes(){
		// Material categories
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME FROM CATEGORIES WHERE IS_DELETED = 0");
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
						DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
						defaultComboBoxModel.addElement("Izaberite");
						ArrayList<Integer> idList = new ArrayList<>();
						idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxMaterialCategories.setModel(defaultComboBoxModel);
						materialCategoriesIdList = idList;
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
		
		// Measuring units
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME FROM MEASURING_UNITS WHERE IS_DELETED = 0");
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
						DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
						defaultComboBoxModel.addElement("Izaberite");
						ArrayList<Integer> idList = new ArrayList<>();
						idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxMeasuringUnits.setModel(defaultComboBoxModel);
						measuringUnitsIdList = idList;
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
	
	private void SetupDialogForEdit(){
		jLabelTitle.setText("Uredi materijal");
		setTitle("Uredi materijal");
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT NAME, CATEGORY_ID, MEASURING_UNIT_ID, MIN_AMOUNT FROM MATERIALS WHERE ID = ?");
		databaseQuery.AddParam(1, tableId);
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
						jTextFieldName.setText(databaseQueryResult.getString(0));
						int categoryIndex = materialCategoriesIdList.indexOf(databaseQueryResult.getInt(1));
						int unitIndex = measuringUnitsIdList.indexOf(databaseQueryResult.getInt(2));
						jComboBoxMaterialCategories.setSelectedIndex(categoryIndex);
						jComboBoxMeasuringUnits.setSelectedIndex(unitIndex);
						float minAmount = databaseQueryResult.getFloat(3);
						if(minAmount >= 0f){
							jCheckBox1.setSelected(true);
							jCheckBox1ItemStateChanged(null);
							jFormattedTextField1.setValue(databaseQueryResult.getFloat(3));
						}
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
        jLabel3 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jComboBoxMaterialCategories = new javax.swing.JComboBox<>();
        jComboBoxMeasuringUnits = new javax.swing.JComboBox<>();
        jButtonAddMaterialCategory = new javax.swing.JButton();
        jButtonAddMeasuringUnit = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj materijal");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj materijal");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Naziv:");
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel2.setText("Kategorija:");
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel3.setText("Mjerna jedinica:");
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 14));

        jTextFieldName.setPreferredSize(new java.awt.Dimension(300, 25));

        jComboBoxMaterialCategories.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMaterialCategories.setPreferredSize(new java.awt.Dimension(200, 25));

        jComboBoxMeasuringUnits.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMeasuringUnits.setMinimumSize(new java.awt.Dimension(56, 25));
        jComboBoxMeasuringUnits.setPreferredSize(new java.awt.Dimension(200, 25));

        jButtonAddMaterialCategory.setText("+");
        jButtonAddMaterialCategory.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddMaterialCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMaterialCategoryActionPerformed(evt);
            }
        });

        jButtonAddMeasuringUnit.setText("+");
        jButtonAddMeasuringUnit.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddMeasuringUnit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMeasuringUnitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonAddMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBoxMaterialCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonAddMaterialCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxMaterialCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddMaterialCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBox1.setText("Automatska obavijest ako količina u skladištu bude manja od:");
        jCheckBox1.setToolTipText("");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addGap(18, 18, 18)
                                .addComponent(jFormattedTextField1)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(97, 97, 97)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabelTitle)
                                .addGap(162, 162, 162))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabelTitle)
                .addGap(42, 42, 42)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String name = jTextFieldName.getText().trim();
        if("".equals(name)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite ime materijala");
            return;
        }
		float minAmount = -1;
		if(jCheckBox1.isSelected()){
			if(jFormattedTextField1.getValue() != null){
				minAmount = ((Number)jFormattedTextField1.getValue()).floatValue();
			}
		}
		
		int comboBoxCategoryId = jComboBoxMaterialCategories.getSelectedIndex();
		int comboBoxUnitsId = jComboBoxMeasuringUnits.getSelectedIndex();
		if(comboBoxCategoryId == 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite kategoriju materijala");
            return;
		}
		if(comboBoxUnitsId == 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite mjernu jedinicu materijala");
            return;
		}
		
		int materialCategoryId = materialCategoriesIdList.get(comboBoxCategoryId);
		int measuringUnitId = measuringUnitsIdList.get(comboBoxUnitsId);

		// Check if material name already exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID FROM MATERIALS WHERE NAME = ? AND IS_DELETED = 0 FETCH FIRST ROW ONLY");
			databaseQuery.AddParam(1, name);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
				return;
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if (databaseQueryResult.next()) {
							if(databaseQueryResult.getInt(0) != tableId){
								ClientAppLogger.GetInstance().ShowMessage("Materijal sa tim imenom već postoji.");
								return;
							}
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
		
        final JDialog loadingDialog = new LoadingDialog(null, true);
        
        String query;
        DatabaseQuery databaseQuery;
        if(tableId == -1){
            query = "INSERT INTO MATERIALS (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, "
				+ "MIN_AMOUNT, LAST_PRICE, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
            databaseQuery = new DatabaseQuery(query);
            databaseQuery.SetAutoIncrementParam(1, "ID", "MATERIALS");
            databaseQuery.AddParam(2, name);
            databaseQuery.AddParam(3, materialCategoryId);
            databaseQuery.AddParam(4, measuringUnitId);
            databaseQuery.AddParam(5, minAmount);
            databaseQuery.AddParam(6, 0);
            databaseQuery.AddParam(7, 0);
        } else {
            query = "UPDATE MATERIALS SET NAME = ?, CATEGORY_ID = ?, MEASURING_UNIT_ID = ?, MIN_AMOUNT = ? WHERE ID = ?";
            databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, name);
            databaseQuery.AddParam(2, materialCategoryId);
            databaseQuery.AddParam(3, measuringUnitId);
            databaseQuery.AddParam(4, minAmount);
            databaseQuery.AddParam(5, tableId);
        }

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
                    changeSuccess = true;
                    Utils.DisposeDialog(this);
                }
            } catch (Exception ex) {
                ClientAppLogger.GetInstance().ShowErrorLog(ex);
            }
        }
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
		boolean isSelected = jCheckBox1.isSelected();
		jFormattedTextField1.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jButtonAddMaterialCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMaterialCategoryActionPerformed
		ClientAppWarehouseCategoriesAddEditDialog addEditdialog = new ClientAppWarehouseCategoriesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddMaterialCategoryActionPerformed

    private void jButtonAddMeasuringUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMeasuringUnitActionPerformed
		ClientAppWarehouseMeasuringUnitsAddEditDialog addEditdialog = new ClientAppWarehouseMeasuringUnitsAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddMeasuringUnitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddMaterialCategory;
    private javax.swing.JButton jButtonAddMeasuringUnit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox<String> jComboBoxMaterialCategories;
    private javax.swing.JComboBox<String> jComboBoxMeasuringUnits;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
}
