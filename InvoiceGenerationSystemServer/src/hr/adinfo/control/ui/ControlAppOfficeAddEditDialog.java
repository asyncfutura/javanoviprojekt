/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control.ui;

import hr.adinfo.control.ControlAppLogger;
import hr.adinfo.control.ControlAppServerAppClient;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ControlAppOfficeAddEditDialog extends javax.swing.JDialog {

	private int tableId;
	private int companyId = -1;
	private String startOfficeTag = "";
	private String userOib = "";
	
	/**
	 * Creates new form ControlAppAddOfficeDialog
	 */
	public ControlAppOfficeAddEditDialog(java.awt.Frame parent, boolean modal, int tableId, int companyId) {
		super(parent, modal);
		initComponents();
		
		this.tableId = tableId;
		this.companyId = companyId;
		if(tableId != -1){
			jButtonSelectCompany.setEnabled(false);
			SetupDialogForEdit();
			RefreshCompany();
		} else {
			jButtonSelectCompany.doClick();
		}
	}
	
	private void SetupDialogForEdit(){
		jLabelTitle.setText("Uredi korisnika");
		setTitle("Uredi korisnika");
		jButtonAdd.setText("Spremi");
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ADDRESS, OFFICE_TAG, OFFICE_NAME, OFFICE_NUMBER FROM OFFICES WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, tableId);
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
						jTextFieldAddress1.setText(databaseQueryResult.getString(0));
						jTextFieldOfficeTag.setText(databaseQueryResult.getString(1));
						jTextFieldOfficeName.setText(databaseQueryResult.getString(2));
						startOfficeTag = databaseQueryResult.getString(1);
						int officeCount = Integer.parseInt(databaseQueryResult.getString(3));
						jLabelOfficeNumber.setText("" + officeCount);
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
							userOib = databaseQueryResult.getString(0);
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
		
		if(tableId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT COUNT (*) FROM OFFICES INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID WHERE COMPANIES.OIB = ?");
			databaseQuery.AddParam(1, userOib);
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
							int officeCount = Integer.parseInt(databaseQueryResult.getString(0));
							jLabelOfficeNumber.setText("" + (officeCount + 1));
						}
					}
				} catch (Exception ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
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
        jButtonAdd = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabelOfficeNumber = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldAddress2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldAddress1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelCompanyName = new javax.swing.JLabel();
        jLabelCompanyOIB = new javax.swing.JLabel();
        jButtonSelectCompany = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldOfficeName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldOfficeTag = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitle.setText("Dodavanje poslovnice");

        jButtonAdd.setText("Dodaj poslovnicu");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabelOfficeNumber.setText("redniBrojPoslovnice");

        jLabel3.setText("Broj poslovnice:");

        jTextFieldAddress2.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel5.setText("Adresa:");

        jTextFieldAddress1.setPreferredSize(new java.awt.Dimension(6, 25));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Korisnik"));

        jLabel4.setText("Naziv:");
        jLabel4.setPreferredSize(new java.awt.Dimension(83, 14));

        jLabel6.setText("OIB:");
        jLabel6.setPreferredSize(new java.awt.Dimension(83, 14));

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
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelCompanyName)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelCompanyOIB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                        .addComponent(jButtonSelectCompany)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompanyName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompanyOIB))
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSelectCompany)
                .addContainerGap())
        );

        jLabel2.setText("<html> <div style=\"text-align: center\"> Naziv poslovnice: <br> <font size=\"2\"> (nije obavezno) </font> </div> </html>");

        jTextFieldOfficeName.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel7.setText("Oznaka PP:");

        jTextFieldOfficeTag.setPreferredSize(new java.awt.Dimension(6, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabelOfficeNumber)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jTextFieldAddress1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextFieldAddress2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(jTextFieldOfficeName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(53, 53, 53)
                        .addComponent(jTextFieldOfficeTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addComponent(jLabelOfficeNumber))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldOfficeTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldOfficeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(jButtonAdd)
                .addContainerGap(140, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabelTitle)
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		if(companyId == -1){
			ControlAppLogger.GetInstance().ShowMessage("Odaberite poslovnicu");
			return;
		}
		String officeNumber = jLabelOfficeNumber.getText().trim();
		String address1 = jTextFieldAddress1.getText().trim();
		String address2 = jTextFieldAddress2.getText().trim();
		String officeName = jTextFieldOfficeName.getText().trim();
		String officeTag = jTextFieldOfficeTag.getText().trim();
		
		if("".equals(address1) && "".equals(address2)){
			ControlAppLogger.GetInstance().ShowMessage("Unesite adresu");
			return;
		}
		
		if("".equals(officeTag)){
			ControlAppLogger.GetInstance().ShowMessage("Unesite oznaku poslovnog prostora");
			return;
		}
		
		// Check if office tag already exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT OFFICE_TAG FROM OFFICES INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID WHERE OIB = ? AND OFFICE_TAG = ? FETCH FIRST ROW ONLY");
			databaseQuery.AddParam(1, userOib);
			databaseQuery.AddParam(2, officeTag);
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
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
					}
					if(databaseQueryResult != null){
						if (databaseQueryResult.next()) {
							if(!startOfficeTag.equals(databaseQueryResult.getString(0))){
								ControlAppLogger.GetInstance().ShowMessage("Unesena oznaka poslovnog prostora već postoji kod ovog korisnika.");
								return;
							}
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
		
		String address;
		if("".equals(address2)){
			address = address1;
		} else {
			address = address1 + ", " + address2;
		}
		
		if(tableId == -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);

			DatabaseQuery databaseQuery = new DatabaseQuery("INSERT INTO OFFICES (ID, USER_ID, ADDRESS, OFFICE_NUMBER, OFFICE_TAG, OFFICE_NAME, LAST_PING_DATE, LAST_PING_TIME) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			databaseQuery.SetAutoIncrementParam(1, "ID", "OFFICES");
			databaseQuery.AddParam(2, companyId);
			databaseQuery.AddParam(3, address);
			databaseQuery.AddParam(4, officeNumber);
			databaseQuery.AddParam(5, officeTag);
			databaseQuery.AddParam(6, officeName);
			Date date = new Date();
			databaseQuery.AddParam(7, new SimpleDateFormat("yyyy-MM-dd").format(date));
			databaseQuery.AddParam(8, new SimpleDateFormat("HH:mm:ss").format(date));

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
						Utils.DisposeDialog(this);
					}
				} catch (Exception ex) {
					ControlAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		} else {
			final JDialog loadingDialog = new LoadingDialog(null, true);
           
			String query = "UPDATE OFFICES SET ADDRESS = ?, OFFICE_TAG = ?, OFFICE_NAME = ? WHERE ID = ?";
            DatabaseQuery databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, address);
            databaseQuery.AddParam(2, officeTag);
            databaseQuery.AddParam(3, officeName);
            databaseQuery.AddParam(4, tableId);

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
                        Utils.DisposeDialog(this);
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
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonSelectCompany;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelCompanyName;
    private javax.swing.JLabel jLabelCompanyOIB;
    private javax.swing.JLabel jLabelOfficeNumber;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextFieldAddress1;
    private javax.swing.JTextField jTextFieldAddress2;
    private javax.swing.JTextField jTextFieldOfficeName;
    private javax.swing.JTextField jTextFieldOfficeTag;
    // End of variables declaration//GEN-END:variables
}
