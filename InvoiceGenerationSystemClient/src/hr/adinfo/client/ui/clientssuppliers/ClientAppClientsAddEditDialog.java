/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.clientssuppliers;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppClientsAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	private int tableId;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppClientsAddEditDialog(java.awt.Frame parent, boolean modal, int tableId) {
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
		
		jTextFieldOIB.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (jTextFieldOIB.getText().length() <= 7 && jTextFieldOIB.getText().length() >= 14){
					e.consume();
				}
			}
		});
		
		this.tableId = tableId;
		if(tableId != -1){
			SetupDialogForEdit();
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogForEdit(){
		jLabelTitle.setText("Uredi klijenta");
		setTitle("Uredi klijenta");
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT NAME, OIB, STREET, HOUSE_NUM, TOWN, POSTAL_CODE, COUNTRY, BIRTHDAY, MOBILE_NUM, TELEPHONE_NUM, "
					+ "WEBSITE, EMAIL, NOTES, PAYMENT_DELAY, TYPE, DISCOUNT, LOYALTY_CARD "
					+ "FROM CLIENTS WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
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
						jTextFieldOIB.setText(databaseQueryResult.getString(1));
						jTextFieldStreet.setText(databaseQueryResult.getString(2));
						jTextFieldHouseNum.setText(databaseQueryResult.getString(3));
						jTextFieldTown.setText(databaseQueryResult.getString(4));
						jTextFieldPostalCode.setText(databaseQueryResult.getString(5));
						jTextFieldCountry.setText(databaseQueryResult.getString(6));
						jTextFieldBirthday.setText(databaseQueryResult.getString(7));
						jTextFieldMobileNum.setText(databaseQueryResult.getString(8));
						jTextFieldPhoneNum.setText(databaseQueryResult.getString(9));
						jTextFieldWebsite.setText(databaseQueryResult.getString(10));
						jTextFieldEmail.setText(databaseQueryResult.getString(11));
						jTextAreaNotes.setText(databaseQueryResult.getString(12));
						jFormattedTextFieldPaymentDelay.setValue(databaseQueryResult.getInt(13));
						jComboBox1.setSelectedIndex(databaseQueryResult.getInt(14));
						jFormattedTextFieldDiscount.setValue(databaseQueryResult.getInt(15));
						jTextFieldLoyaltyCard.setText(databaseQueryResult.getString(16));
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
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jTextFieldOIB = new javax.swing.JTextField();
        jTextFieldStreet = new javax.swing.JTextField();
        jTextFieldHouseNum = new javax.swing.JTextField();
        jTextFieldTown = new javax.swing.JTextField();
        jTextFieldPostalCode = new javax.swing.JTextField();
        jTextFieldCountry = new javax.swing.JTextField();
        jTextFieldBirthday = new javax.swing.JTextField();
        jTextFieldMobileNum = new javax.swing.JTextField();
        jTextFieldPhoneNum = new javax.swing.JTextField();
        jTextFieldWebsite = new javax.swing.JTextField();
        jTextFieldEmail = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaNotes = new javax.swing.JTextArea();
        jLabel19 = new javax.swing.JLabel();
        jFormattedTextFieldPaymentDelay = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldLoyaltyCard = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jFormattedTextFieldDiscount = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        stranacCheckbox = new javax.swing.JCheckBox();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj klijenta");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj klijenta");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Ime klijenta:");
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel6.setText("OIB:");
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel7.setText("Ulica:");
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel8.setText("Kućni broj:");
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel9.setText("Mjesto:");
        jLabel9.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel10.setText("Poštanski broj:");
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel11.setText("Država:");
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel12.setText("Datum rođenja:");
        jLabel12.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel13.setText("Mobitel:");
        jLabel13.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Telefon: ");
        jLabel14.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel15.setText("Web:");
        jLabel15.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel16.setText("Email:");
        jLabel16.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel17.setText("Zabilješke:");
        jLabel17.setPreferredSize(new java.awt.Dimension(100, 14));

        jTextFieldName.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldOIB.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldStreet.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldHouseNum.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldTown.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldPostalCode.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldCountry.setText("Hrvatska");
        jTextFieldCountry.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldBirthday.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldMobileNum.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldPhoneNum.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldWebsite.setPreferredSize(new java.awt.Dimension(300, 25));

        jTextFieldEmail.setPreferredSize(new java.awt.Dimension(300, 25));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextAreaNotes.setColumns(20);
        jTextAreaNotes.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextAreaNotes.setLineWrap(true);
        jTextAreaNotes.setRows(5);
        jScrollPane1.setViewportView(jTextAreaNotes);

        jLabel19.setText("Odgoda plaćanja:");
        jLabel19.setPreferredSize(new java.awt.Dimension(100, 14));

        jFormattedTextFieldPaymentDelay.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldPaymentDelay.setPreferredSize(new java.awt.Dimension(100, 25));

        jLabel2.setText("(broj dana)");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Firma / Pravna osoba", "Fizička osoba" }));
        jComboBox1.setPreferredSize(new java.awt.Dimension(127, 25));

        jLabel18.setText("Kartica povjerenja:");
        jLabel18.setPreferredSize(new java.awt.Dimension(100, 14));

        jTextFieldLoyaltyCard.setPreferredSize(new java.awt.Dimension(300, 25));

        jLabel20.setText("Popust:");
        jLabel20.setPreferredSize(new java.awt.Dimension(100, 14));

        jFormattedTextFieldDiscount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldDiscount.setPreferredSize(new java.awt.Dimension(100, 25));

        jLabel3.setText("%");

        stranacCheckbox.setText("Stranac");
        stranacCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stranacCheckboxActionPerformed(evt);
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
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldOIB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(stranacCheckbox))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldHouseNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldTown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldPostalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldWebsite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldStreet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jScrollPane1))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldMobileNum, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldPhoneNum, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldBirthday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jFormattedTextFieldPaymentDelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel2))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jFormattedTextFieldDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel3)))
                                    .addGap(113, 113, 113))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldLoyaltyCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldOIB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stranacCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldStreet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldHouseNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPostalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldLoyaltyCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextFieldDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jFormattedTextFieldPaymentDelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldBirthday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldMobileNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPhoneNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldWebsite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addGroup(layout.createSequentialGroup()
                .addGap(170, 170, 170)
                .addComponent(jLabelTitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabelTitle)
                .addGap(26, 26, 26)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
		String oib = jTextFieldOIB.getText().trim();
		String street = jTextFieldStreet.getText().trim();
		String houseNum = jTextFieldHouseNum.getText().trim();
		String town = jTextFieldTown.getText().trim();
		String postalCodeString = jTextFieldPostalCode.getText().trim();
		String country = jTextFieldCountry.getText().trim();
		String birthday = jTextFieldBirthday.getText().trim();
		String mobileNum = jTextFieldMobileNum.getText().trim();
		String phoneNum = jTextFieldPhoneNum.getText().trim();
		String website = jTextFieldWebsite.getText().trim();
		String email = jTextFieldEmail.getText().trim();
		String notes = jTextAreaNotes.getText().trim();
		String loyaltyCard = jTextFieldLoyaltyCard.getText().trim();
		int clientType = jComboBox1.getSelectedIndex();
		
        if("".equals(name)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite ime klijenta.");
            return;
        }
		if(!Utils.IsValidOIB(oib) && !stranacCheckbox.isSelected()){
			ClientAppLogger.GetInstance().ShowMessage("Unesite ispravan OIB.");
			return;
		}
                
		int postalCode = 0;
                
		try {
			postalCode = Integer.parseInt(postalCodeString);
		} catch (NumberFormatException ex){
			ClientAppLogger.GetInstance().ShowMessage("Unesite ispravan poštanski broj.");
			return;
		}
		if("".equals(street)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite ulicu.");
            return;
        }
		if("".equals(houseNum)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite kućni broj.");
            return;
        }
		if("".equals(town)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite mjesto.");
            return;
        }
		
		if(jFormattedTextFieldPaymentDelay.getValue() == null){
			ClientAppLogger.GetInstance().ShowMessage("Odgoda plaćanja nije ispravnog oblika.");
			return;
		}
		int paymentDelay = ((Number)jFormattedTextFieldPaymentDelay.getValue()).intValue();
		
		if(jFormattedTextFieldDiscount.getValue() == null){
			ClientAppLogger.GetInstance().ShowMessage("Popust nije ispravnog oblika.");
			return;
		}
		int discount = ((Number)jFormattedTextFieldDiscount.getValue()).intValue();
		
		// Check if loyaltyCard is already used
		if(!"".equals(loyaltyCard)){
			String queryCustomIdCheck = "SELECT NAME FROM CLIENTS WHERE LOYALTY_CARD = ? AND ID != ?";

			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
			multiDatabaseQuery.SetQuery(0, queryCustomIdCheck);
			multiDatabaseQuery.AddParam(0, 1, loyaltyCard);
			multiDatabaseQuery.AddParam(0, 2, tableId);
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
						if (databaseQueryResult[0].next()) {
							ClientAppLogger.GetInstance().ShowMessage("Unesena šifra kartice povjerenja već se koristi kod klijenta " + databaseQueryResult[0].getString(0) + ". Molimo odaberite drugu šifru.");
							return;
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
        final JDialog loadingDialog = new LoadingDialog(null, true);

        String query;
        DatabaseQuery databaseQuery;
        if(tableId == -1){
			query = "INSERT INTO CLIENTS(ID, NAME, OIB, STREET, HOUSE_NUM, TOWN, POSTAL_CODE, COUNTRY, BIRTHDAY, MOBILE_NUM, TELEPHONE_NUM, "
					+ "WEBSITE, EMAIL, NOTES, TRAFFIC, PAYMENT_DELAY, TYPE, DISCOUNT, LOYALTY_CARD, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            databaseQuery = new DatabaseQuery(query);
            databaseQuery.SetAutoIncrementParam(1, "ID", "CLIENTS");
            databaseQuery.AddParam(2, name);
            databaseQuery.AddParam(3, oib);
            databaseQuery.AddParam(4, street);
            databaseQuery.AddParam(5, houseNum);
            databaseQuery.AddParam(6, town);
            databaseQuery.AddParam(7, postalCode);
            databaseQuery.AddParam(8, country);
            databaseQuery.AddParam(9, birthday);
            databaseQuery.AddParam(10, mobileNum);
            databaseQuery.AddParam(11, phoneNum);
            databaseQuery.AddParam(12, website);
            databaseQuery.AddParam(13, email);
            databaseQuery.AddParam(14, notes);
            databaseQuery.AddParam(15, 0f);
            databaseQuery.AddParam(16, paymentDelay);
            databaseQuery.AddParam(17, clientType);
            databaseQuery.AddParam(18, discount);
            databaseQuery.AddParam(19, loyaltyCard);
            databaseQuery.AddParam(20, 0);
        } else {
            query = "UPDATE CLIENTS SET NAME = ?, OIB = ?, STREET = ?, HOUSE_NUM = ?, TOWN = ?, POSTAL_CODE = ?, COUNTRY = ?, BIRTHDAY = ?, "
					+ "MOBILE_NUM = ?, TELEPHONE_NUM = ?, WEBSITE = ?, EMAIL = ?, NOTES = ?, PAYMENT_DELAY = ?, TYPE = ?, DISCOUNT = ?, LOYALTY_CARD = ? "
					+ "WHERE ID = ?";
            databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, name);
            databaseQuery.AddParam(2, oib);
            databaseQuery.AddParam(3, street);
            databaseQuery.AddParam(4, houseNum);
            databaseQuery.AddParam(5, town);
            databaseQuery.AddParam(6, postalCode);
            databaseQuery.AddParam(7, country);
            databaseQuery.AddParam(8, birthday);
            databaseQuery.AddParam(9, mobileNum);
            databaseQuery.AddParam(10, phoneNum);
            databaseQuery.AddParam(11, website);
            databaseQuery.AddParam(12, email);
            databaseQuery.AddParam(13, notes);
            databaseQuery.AddParam(14, paymentDelay);
            databaseQuery.AddParam(15, clientType);
            databaseQuery.AddParam(16, discount);
            databaseQuery.AddParam(17, loyaltyCard);
            databaseQuery.AddParam(18, tableId);
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

    private void stranacCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stranacCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stranacCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JFormattedTextField jFormattedTextFieldDiscount;
    private javax.swing.JFormattedTextField jFormattedTextFieldPaymentDelay;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaNotes;
    private javax.swing.JTextField jTextFieldBirthday;
    private javax.swing.JTextField jTextFieldCountry;
    private javax.swing.JTextField jTextFieldEmail;
    private javax.swing.JTextField jTextFieldHouseNum;
    private javax.swing.JTextField jTextFieldLoyaltyCard;
    private javax.swing.JTextField jTextFieldMobileNum;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldOIB;
    private javax.swing.JTextField jTextFieldPhoneNum;
    private javax.swing.JTextField jTextFieldPostalCode;
    private javax.swing.JTextField jTextFieldStreet;
    private javax.swing.JTextField jTextFieldTown;
    private javax.swing.JTextField jTextFieldWebsite;
    private javax.swing.JCheckBox stranacCheckbox;
    // End of variables declaration//GEN-END:variables
}
