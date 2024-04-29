/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.staff;

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
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Matej
 */
public class ClientAppStaffAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	private int staffId;
	
	/**
	 * Creates new form ClientAppStaffAddDialog
	 */
	public ClientAppStaffAddEditDialog(java.awt.Frame parent, boolean modal, int staffId) {
		super(parent, modal);
		initComponents();
		
		this.staffId = staffId;
		if(staffId != -1){
			SetupStaffEdit();
			//jTextFieldFirstName.setEnabled(false);
			//jTextFieldLastName.setEnabled(false);
			jTextFieldOIB.setEnabled(false);
		}
				
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
		
		jTextFieldOIB.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				CheckOIB();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				CheckOIB();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				CheckOIB();
			}
			
			public void CheckOIB(){
				String oib = jTextFieldOIB.getText().trim();
				if(oib.length() < 11){
					jTextFieldOIB.setBorder(new LineBorder(Values.TEXT_FIELD_NORMAL, 1));
				} else {
					if(Utils.IsValidOIB(oib)){
						jTextFieldOIB.setBorder(new LineBorder(Values.TEXT_FIELD_GREEN, 1));
					} else {
						jTextFieldOIB.setBorder(new LineBorder(Values.TEXT_FIELD_RED, 1));
					}
				}
			}
		});
		
		jPasswordField1.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				CheckPassword();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				CheckPassword();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				CheckPassword();
			}
			
			public void CheckPassword(){
				String pass = String.valueOf(jPasswordField1.getPassword()).trim();
                                jPasswordField1.setEchoChar((char)0);
				if(pass.length() == 0){
					jPasswordField1.setBorder(new LineBorder(Values.TEXT_FIELD_NORMAL, 1));
				} else {
					if(pass.length() < 4){
						jPasswordField1.setBorder(new LineBorder(Values.TEXT_FIELD_RED, 1));
					} else {
						jPasswordField1.setBorder(new LineBorder(Values.TEXT_FIELD_GREEN, 1));
					}
				}
			}
		});
		
		jPasswordField2.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				CheckPasswordAgain();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				CheckPasswordAgain();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				CheckPasswordAgain();
			}
			
			public void CheckPasswordAgain(){
				String pass1 = String.valueOf(jPasswordField1.getPassword()).trim();
                                jPasswordField1.setEchoChar((char)0);
				String pass2 = String.valueOf(jPasswordField2.getPassword()).trim();
                                jPasswordField2.setEchoChar((char)0);
				if(pass2.length() == 0){
					jPasswordField2.setBorder(new LineBorder(Values.TEXT_FIELD_NORMAL, 1));
				} else {
					if(!pass1.equals(pass2)){
						jPasswordField2.setBorder(new LineBorder(Values.TEXT_FIELD_RED, 1));
					} else {
						jPasswordField2.setBorder(new LineBorder(Values.TEXT_FIELD_GREEN, 1));
					}
				}
			}
		});
		
		jTextFieldOIB.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (jTextFieldOIB.getText().length() >= 11){
					e.consume();
				}
			}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
	}

	private void SetupStaffEdit(){
		jLabelTitle.setText("Uredi djelatnika");
		setTitle("Uredi djelatnika");
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT FIRST_NAME, LAST_NAME, PASSWORD, OIB, RIGHTS, TELEPHONE_NUM, MOBILE_NUM FROM STAFF WHERE ID = ?");
		databaseQuery.AddParam(1, staffId);
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
						jTextFieldFirstName.setText(databaseQueryResult.getString(0));
						jTextFieldLastName.setText(databaseQueryResult.getString(1));
						jPasswordField1.setText(databaseQueryResult.getString(2));
						jPasswordField2.setText(databaseQueryResult.getString(2));
						jTextFieldOIB.setText(databaseQueryResult.getString(3));
						int staffRightsIndex = databaseQueryResult.getInt(4);
						if(staffRightsIndex == Values.STAFF_RIGHTS_OWNER){
							jComboBox1.setSelectedIndex(3);
						} else if(staffRightsIndex == Values.STAFF_RIGHTS_MANAGER){
							jComboBox1.setSelectedIndex(2);
						} else if(staffRightsIndex == Values.STAFF_RIGHTS_EMPLOYEE){
							jComboBox1.setSelectedIndex(1);
						} else if(staffRightsIndex == Values.STAFF_RIGHTS_STUDENT){
							jComboBox1.setSelectedIndex(4);
						}
						jTextFieldTelephone.setText(databaseQueryResult.getString(5));
						jTextFieldMobile.setText(databaseQueryResult.getString(6));
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
                        jPasswordField1.setEchoChar((char)0);
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldFirstName = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jTextFieldOIB = new javax.swing.JTextField();
        jTextFieldTelephone = new javax.swing.JTextField();
        jTextFieldMobile = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldLastName = new javax.swing.JTextField();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj djelatnika");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj djelatnika");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel2.setText("Ime:");

        jLabel3.setText("Lozinka:");

        jLabel4.setText("Lozinka ponovno:");

        jLabel5.setText("Razina ovlaštenja:");

        jLabel6.setText("OIB:");

        jLabel7.setText("Telefon:");

        jLabel8.setText("Mobitel:");

        jTextFieldFirstName.setPreferredSize(new java.awt.Dimension(260, 25));

        jPasswordField1.setPreferredSize(new java.awt.Dimension(260, 25));

        jPasswordField2.setPreferredSize(new java.awt.Dimension(260, 25));

        jTextFieldOIB.setPreferredSize(new java.awt.Dimension(260, 25));

        jTextFieldTelephone.setPreferredSize(new java.awt.Dimension(260, 25));

        jTextFieldMobile.setPreferredSize(new java.awt.Dimension(260, 25));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Izaberite razinu ovlaštenja", "Djelatnik", "Poslovođa", "Vlasnik", "Student" }));
        jComboBox1.setToolTipText("");
        jComboBox1.setPreferredSize(new java.awt.Dimension(260, 25));

        jLabel9.setText("Prezime:");

        jTextFieldLastName.setPreferredSize(new java.awt.Dimension(260, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFirstName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldOIB, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldTelephone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldMobile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldOIB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldTelephone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldMobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(156, 156, 156)
                        .addComponent(jLabelTitle)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(97, 97, 97)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabelTitle)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String firstName = jTextFieldFirstName.getText().trim();
		String lastName = jTextFieldLastName.getText().trim();
		String pass1 = String.valueOf(jPasswordField1.getPassword()).trim();
                jPasswordField1.setEchoChar((char)0);
		String pass2 = String.valueOf(jPasswordField2.getPassword()).trim();
                jPasswordField2.setEchoChar((char)0);
		int comboBoxIndex = jComboBox1.getSelectedIndex();
		String oib = jTextFieldOIB.getText().trim();
		String telephone = jTextFieldTelephone.getText().trim();
		String mobile = jTextFieldMobile.getText().trim();
		
		if("".equals(firstName)){
			ClientAppLogger.GetInstance().ShowMessage("Unesite ime djelatnika");
			return;
		}
		
		if("".equals(lastName)){
			ClientAppLogger.GetInstance().ShowMessage("Unesite prezime djelatnika");
			return;
		}
		
		if(pass1.length() < 4){
			ClientAppLogger.GetInstance().ShowMessage("Lozinka mora imati barem 4 znaka");
			return;
		}
		
		if(!pass1.equals(pass2)){
			ClientAppLogger.GetInstance().ShowMessage("Unesene lozinke se ne podudaraju");
			return;
		}
		
		if(comboBoxIndex == 0){
			ClientAppLogger.GetInstance().ShowMessage(jComboBox1.getItemAt(0));
			return;
		}
		
		if(!Utils.IsValidOIB(oib)){
			ClientAppLogger.GetInstance().ShowMessage("Unesite ispravan OIB");
			return;
		}
		
		int staffRights = Values.STAFF_RIGHTS_ADMIN;
		if(comboBoxIndex == 1){
			staffRights = Values.STAFF_RIGHTS_EMPLOYEE;
		} else if(comboBoxIndex == 2){
			staffRights = Values.STAFF_RIGHTS_MANAGER;
		} else if(comboBoxIndex == 3){
			staffRights = Values.STAFF_RIGHTS_OWNER;
		} else if(comboBoxIndex == 4){
			staffRights = Values.STAFF_RIGHTS_STUDENT;
		}
		
		if((staffRights == Values.STAFF_RIGHTS_ADMIN || staffRights == Values.STAFF_RIGHTS_OWNER) 
				&& StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN 
				&& StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
			ClientAppLogger.GetInstance().ShowMessage("Odabrana razina ovlaštenja ne može biti viša od razine ovlaštenja trenutnog korisnika");
			return;
		}	
		
		// Check if user already exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID FROM STAFF WHERE OIB = ? AND IS_DELETED = 0 FETCH FIRST ROW ONLY");
			databaseQuery.AddParam(1, oib);
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
							if(databaseQueryResult.getInt(0) != staffId){
								ClientAppLogger.GetInstance().ShowMessage("Djelatnik sa unesenim OIB-om već postoji.");
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
		
		// Check if password already exist
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID FROM STAFF WHERE PASSWORD = ? AND IS_DELETED = 0 FETCH FIRST ROW ONLY");
			databaseQuery.AddParam(1, pass1);
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
							if(databaseQueryResult.getInt(0) != staffId){
								ClientAppLogger.GetInstance().ShowMessage("Unesena lozinka već se koristi. Dva različita djelatnika ne smiju imati istu lozinku.");
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
		
		MultiDatabaseQuery multiDatabaseQuery;
		if(staffId == -1){
			multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			String query = "INSERT INTO STAFF (ID, FIRST_NAME, LAST_NAME, OIB, PASSWORD, OFFICE_NUMBER, RIGHTS, TELEPHONE_NUM, MOBILE_NUM, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			multiDatabaseQuery.SetQuery(0, query);
			multiDatabaseQuery.SetAutoIncrementParam(0, 1, "ID", "STAFF");
			multiDatabaseQuery.AddParam(0, 2, firstName);
			multiDatabaseQuery.AddParam(0, 3, lastName);
			multiDatabaseQuery.AddParam(0, 4, oib);
			multiDatabaseQuery.AddParam(0, 5, pass1);
			multiDatabaseQuery.AddParam(0, 6, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(0, 7, staffRights);
			multiDatabaseQuery.AddParam(0, 8, telephone);
			multiDatabaseQuery.AddParam(0, 9, mobile);
			multiDatabaseQuery.AddParam(0, 10, 0);
			
			int staffRightsStartValue = (staffRights == Values.STAFF_RIGHTS_OWNER || staffRights == Values.STAFF_RIGHTS_ADMIN) ? 1 : 0;
			String staffRightsQuery = "INSERT INTO STAFF_RIGHTS (ID, STAFF_ID, "
					+ "W1, W2, W3, W4, W5, W6, "
					+ "CR1, CR2, CR3, CR4, CR5, CR6, "
					+ "R1, R2, R3, R4, R5, R6, "
					+ "S1, "
					+ "RE1, RE2, "
					+ "CS1, CS2, CS3, "
					+ "O1, O2, O3, O4, O5, "
					+ "R29, R30) "
					+ "VALUES (?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, "
					+ "?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ?, ?, ?, "
					+ "?, ?)";
			multiDatabaseQuery.SetQuery(1, staffRightsQuery);
			multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "STAFF_RIGHTS");
			multiDatabaseQuery.AddAutoGeneratedParam(1, 2, 0);
			for(int i = 0; i < Values.STAFF_RIGHTS_TOTAL_LENGTH; ++i){
				multiDatabaseQuery.AddParam(1, 3 + i, staffRightsStartValue);
			}
			
			String currentRights = "Administrator";
			if(staffRights == Values.STAFF_RIGHTS_OWNER){
				currentRights = "Vlasnik";
			} else if(staffRights == Values.STAFF_RIGHTS_MANAGER){
				currentRights = "Poslovođa";
			} else if(staffRights == Values.STAFF_RIGHTS_EMPLOYEE){
				currentRights = "Djelatnik";
			} else if(staffRights == Values.STAFF_RIGHTS_STUDENT){
				currentRights = "Student";
			}
			Date date = new Date();
			multiDatabaseQuery.SetQuery(2, ClientAppUtils.CHANGES_LOG_QUERY);
			multiDatabaseQuery.SetAutoIncrementParam(2, 1, "ID", "CHANGES_LOG");
			multiDatabaseQuery.AddParam(2, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
			multiDatabaseQuery.AddParam(2, 3, new SimpleDateFormat("HH:mm:ss").format(date));
			multiDatabaseQuery.AddParam(2, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(2, 5, StaffUserInfo.GetCurrentUserInfo().userId);
			multiDatabaseQuery.AddParam(2, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
			multiDatabaseQuery.AddParam(2, 7, "Dodavanje djelatnika");
			multiDatabaseQuery.AddParam(2, 8, firstName + " " + lastName + " " + oib + " " + currentRights);
			
		} else {
			String query = "UPDATE STAFF SET FIRST_NAME = ?, LAST_NAME = ?, OIB = ?, PASSWORD = ?, RIGHTS = ?, TELEPHONE_NUM = ?, MOBILE_NUM = ? WHERE ID = ?";
			multiDatabaseQuery = new MultiDatabaseQuery(2);
			multiDatabaseQuery.SetQuery(0, query);
			multiDatabaseQuery.AddParam(0, 1, firstName);
			multiDatabaseQuery.AddParam(0, 2, lastName);
			multiDatabaseQuery.AddParam(0, 3, oib);
			multiDatabaseQuery.AddParam(0, 4, pass1);
			multiDatabaseQuery.AddParam(0, 5, staffRights);
			multiDatabaseQuery.AddParam(0, 6, telephone);
			multiDatabaseQuery.AddParam(0, 7, mobile);
			multiDatabaseQuery.AddParam(0, 8, staffId);
			
			String currentRights = "Administrator";
			if(staffRights == Values.STAFF_RIGHTS_OWNER){
				currentRights = "Vlasnik";
			} else if(staffRights == Values.STAFF_RIGHTS_MANAGER){
				currentRights = "Poslovođa";
			} else if(staffRights == Values.STAFF_RIGHTS_EMPLOYEE){
				currentRights = "Djelatnik";
			} else if(staffRights == Values.STAFF_RIGHTS_STUDENT){
				currentRights = "Student";
			}
			Date date = new Date();
			multiDatabaseQuery.SetQuery(1, ClientAppUtils.CHANGES_LOG_QUERY);
			multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "CHANGES_LOG");
			multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
			multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("HH:mm:ss").format(date));
			multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(1, 5, StaffUserInfo.GetCurrentUserInfo().userId);
			multiDatabaseQuery.AddParam(1, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
			multiDatabaseQuery.AddParam(1, 7, "Uređivanje djelatnika");
			multiDatabaseQuery.AddParam(1, 8, staffId + " " + firstName + " " + lastName + " " + oib + " " + currentRights);
		}

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult[0];
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JTextField jTextFieldFirstName;
    private javax.swing.JTextField jTextFieldLastName;
    private javax.swing.JTextField jTextFieldMobile;
    private javax.swing.JTextField jTextFieldOIB;
    private javax.swing.JTextField jTextFieldTelephone;
    // End of variables declaration//GEN-END:variables
}
