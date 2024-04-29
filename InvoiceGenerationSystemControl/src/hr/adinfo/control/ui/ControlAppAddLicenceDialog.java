/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control.ui;

import hr.adinfo.control.ControlAppLogger;
import hr.adinfo.control.ControlAppServerAppClient;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.communication.ServerResponse;
import static hr.adinfo.utils.Utils.DisposeDialog;
import hr.adinfo.utils.Values;
import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.communication.ServerQueryTask;
import static hr.adinfo.utils.localization.Localization.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ControlAppAddLicenceDialog extends javax.swing.JDialog {

	private ArrayList<Integer> officeBoxId = new ArrayList<>();
	
	private int companyId = -1;
	
	/**
	 * Creates new form ControlAppAddLicence
	 */
	public ControlAppAddLicenceDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		
		jXDatePicker1.setFormats("dd.MM.yyyy");
		jXDatePicker1.getEditor().setEditable(false);
		jXDatePicker1.setDate(new Date());
				
		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
		defaultComboBoxModel.addElement(LOCALIZATION_SELECT_OFFICE);
		jComboBoxOffice.setModel(defaultComboBoxModel);
		
		jButtonSelectCompany.doClick();
	}
	
	private void RefreshCompany(){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT OIB, NAME FROM COMPANIES WHERE ID = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, companyId);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

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
							jLabelCompanyName.setText(databaseQueryResult.getString(1));
							jLabelCompanyOIB.setText(databaseQueryResult.getString(0));
						} else {
							Utils.DisposeDialog(this);
						}
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, ADDRESS, OFFICE_NUMBER, OFFICE_TAG, OFFICE_NAME FROM OFFICES WHERE USER_ID = ? ORDER BY OFFICE_NUMBER ASC");
			databaseQuery.AddParam(1, companyId);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
				companyId = -1;
				jLabelCompanyName.setText("");
				jLabelCompanyOIB.setText("");
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
						defaultComboBoxModel.addElement(LOCALIZATION_SELECT_OFFICE);
						ArrayList<Integer> idList = new ArrayList<>();
						idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(2) + " - " + databaseQueryResult.getString(3) + " - " + databaseQueryResult.getString(4) + " - " + databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxOffice.setModel(defaultComboBoxModel);
						officeBoxId = idList;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	public static String GenerateActivationKey(){
		String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rng = new SecureRandom(); 
		int length = 16;
		int spacing = 4;
		char spacingChar = '-';
		StringBuilder sb = new StringBuilder();
		int spacer = 0;
		while(length > 0){
			if(spacer == spacing){
				sb.append(spacingChar);
				spacer = 0;
			}
			length--;
			spacer++;
			char randomChar = ALPHABET.charAt(rng.nextInt(ALPHABET.length()));
			sb.append(randomChar);
		}
		return sb.toString();
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
        jLabel1 = new javax.swing.JLabel();
        jButtonAdd = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxOffice = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jXDatePicker1 = new org.jdesktop.swingx.JXDatePicker();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelCompanyName = new javax.swing.JLabel();
        jLabelCompanyOIB = new javax.swing.JLabel();
        jButtonSelectCompany = new javax.swing.JButton();
        jRadioButtonCashRegister = new javax.swing.JRadioButton();
        jRadioButtonLocalServer = new javax.swing.JRadioButton();
        jRadioButtonMasterServer = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Dodavanje licence");

        jButtonAdd.setText("Dodaj licencu");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setText("Poslovnica:");

        jLabel4.setText("Tip licence:");

        jComboBoxOffice.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Datum isteka:");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Korisnik"));

        jLabel6.setText("Naziv:");
        jLabel6.setPreferredSize(new java.awt.Dimension(83, 14));

        jLabel7.setText("OIB:");
        jLabel7.setPreferredSize(new java.awt.Dimension(83, 14));

        jLabelCompanyName.setText("naziv");

        jLabelCompanyOIB.setText("oib");

        jButtonSelectCompany.setText("Odaberi");
        jButtonSelectCompany.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectCompanyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelCompanyName))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelCompanyOIB)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSelectCompany)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompanyName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompanyOIB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSelectCompany, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        buttonGroup1.add(jRadioButtonCashRegister);
        jRadioButtonCashRegister.setSelected(true);
        jRadioButtonCashRegister.setText("Blagajna");

        buttonGroup1.add(jRadioButtonLocalServer);
        jRadioButtonLocalServer.setText("Lokalni server");

        buttonGroup1.add(jRadioButtonMasterServer);
        jRadioButtonMasterServer.setText("Glavni lokalni server");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jXDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jRadioButtonCashRegister)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButtonLocalServer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButtonMasterServer))
                            .addComponent(jComboBoxOffice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jRadioButtonCashRegister)
                    .addComponent(jRadioButtonLocalServer)
                    .addComponent(jRadioButtonMasterServer))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jXDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(jButtonAdd)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		if(jComboBoxOffice.getSelectedIndex() == 0){
			ControlAppLogger.GetInstance().ShowMessage("Odaberite korisnika i poslovnicu.");
			return;
		}
		
		int officeId = officeBoxId.get(jComboBoxOffice.getSelectedIndex());
		final int licenceType;
		if(jRadioButtonCashRegister.isSelected()){
			licenceType = LICENCE_TYPE_CLIENT;
		} else if(jRadioButtonLocalServer.isSelected()){
			licenceType = LICENCE_TYPE_LOCAL_SERVER;
		} else {
			licenceType = LICENCE_TYPE_MASTER_LOCAL_SERVER;
		}
		String expirationDate = jXDatePicker1.getEditor().getText().trim();
		String activationKey = GenerateActivationKey();
		
		// Check if LocalServer exist in Office, or MasterLocalServer exist in all Offices
		if(licenceType == LICENCE_TYPE_LOCAL_SERVER || licenceType == LICENCE_TYPE_MASTER_LOCAL_SERVER){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID FROM LICENCES WHERE OFFICE_ID = ? AND (TYPE = ? OR TYPE = ?) FETCH FIRST ROW ONLY");
			databaseQuery.AddParam(1, officeId);
			databaseQuery.AddParam(2, LICENCE_TYPE_LOCAL_SERVER);
			databaseQuery.AddParam(3, LICENCE_TYPE_MASTER_LOCAL_SERVER);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
				return;
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if (databaseQueryResult.next()) {
							ControlAppLogger.GetInstance().ShowMessage("Lokalni server već postoji u ovoj poslovnici.");
							return;
						}
					} else {
						return;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
					return;
				}
			}
		}
		
		if(licenceType == LICENCE_TYPE_MASTER_LOCAL_SERVER){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT OFFICES.USER_ID "
					+ "FROM (LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) "
					+ "WHERE OFFICES.USER_ID = ? AND LICENCES.TYPE = ? "
					+ "FETCH FIRST ROW ONLY";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, companyId);
			databaseQuery.AddParam(2, LICENCE_TYPE_MASTER_LOCAL_SERVER);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
				return;
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if (databaseQueryResult.next()) {
							ControlAppLogger.GetInstance().ShowMessage("Glavni server već postoji kod ovog korisnika.");
							return;
						}
					} else {
						return;
					}
				} catch (InterruptedException | ExecutionException ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
					return;
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String insertQuery = "INSERT INTO LICENCES (ID, OFFICE_ID, CASH_REGISTER_NUMBER, TYPE, EXPIRATION_DATE, ACTIVATION_KEY, IS_DELETED) "
					+ "VALUES (?, ?, "
					+ "(SELECT COUNT(ID) FROM LICENCES WHERE OFFICE_ID = ?) + 1, "
					+ "?, ?, ?, ?)";
			DatabaseQuery databaseQuery = new DatabaseQuery(insertQuery);
			databaseQuery.SetAutoIncrementParam(1, "ID", "LICENCES");
			databaseQuery.AddParam(2, officeId);
			databaseQuery.AddParam(3, officeId);
			databaseQuery.AddParam(4, licenceType);
			databaseQuery.AddParam(5, expirationDate);
			databaseQuery.AddParam(6, activationKey);
			databaseQuery.AddParam(7, 0);
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
				companyId = -1;
				jLabelCompanyName.setText("");
				jLabelCompanyOIB.setText("");
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						DisposeDialog(this);
					}
				} catch (Exception ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonSelectCompanyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectCompanyActionPerformed
		ControlAppAddOfficeSelectCompanyDialog addEditdialog = new ControlAppAddOfficeSelectCompanyDialog(null, true);
        addEditdialog.setVisible(true);
        if(addEditdialog.selectedId != -1 && addEditdialog.selectedId != companyId){
			companyId = addEditdialog.selectedId;
            RefreshCompany();
        }
    }//GEN-LAST:event_jButtonSelectCompanyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonSelectCompany;
    private javax.swing.JComboBox<String> jComboBoxOffice;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelCompanyName;
    private javax.swing.JLabel jLabelCompanyOIB;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonCashRegister;
    private javax.swing.JRadioButton jRadioButtonLocalServer;
    private javax.swing.JRadioButton jRadioButtonMasterServer;
    private org.jdesktop.swingx.JXDatePicker jXDatePicker1;
    // End of variables declaration//GEN-END:variables
}
