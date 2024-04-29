/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.settings;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsCashRegisterDialog extends javax.swing.JDialog {
	
	/**
	 * Creates new form ClientAppStaffDialog
	 */
	public ClientAppSettingsCashRegisterDialog(java.awt.Frame parent, boolean modal) {
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
		
		// Load settings
		ClientAppSettings.LoadSettings();
		jCheckBox1.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_TOTALSALDO.ordinal()));
		jCheckBox2.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_PRINTINVOICE.ordinal()));
		jCheckBox3.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SUBTOTAL.ordinal()));
		jCheckBox4.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OTHER.ordinal()));
		jCheckBox24.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OWN.ordinal()));
		jCheckBox5.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SUBTOTAL.ordinal()));
		jCheckBox6.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR.ordinal()));
		jCheckBox7.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN.ordinal()));
		jCheckBox13.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_CHANGEPAYMENTMETHOD.ordinal()));
		jCheckBox17.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_STAFFINVOICE.ordinal()));
		jCheckBox8.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE.ordinal()));
		jCheckBox9.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE_AT_START.ordinal()));
		jCheckBox10.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR.ordinal()));
		jCheckBox11.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN.ordinal()));
		jCheckBox22.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR_PRINT.ordinal()));
		jCheckBox23.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN_PRINT.ordinal()));
		jCheckBox21.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_TOUCH.ordinal()));
		jCheckBox16.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_QUICKCHOICE.ordinal()));
		jCheckBox12.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_EVENT_PRICES.ordinal()));
		jCheckBox14.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal()));
		jCheckBox15.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_WORKTIME.ordinal()));
		jCheckBox20.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_TRUSTCARD.ordinal()));
		jCheckBox18.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER.ordinal()));
		jCheckBox19.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE.ordinal()));
		jCheckBox25.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_ITEM_NOTE.ordinal()));
		jCheckBox26.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_OVERTIME_WORK.ordinal()));
		jCheckBox27.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_SHOW_CASH_RETURN_DIALOG.ordinal()));
		jCheckBox28.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_SUBTOTAL_AUTODELETE.ordinal()));
		jCheckBox29.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE.ordinal()));
                jCheckBox30.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_CANCELBUTTON.ordinal()));
		jCheckBox31.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_ESCAPEBUTTON.ordinal()));
                
		jFormattedTextField1.setValue(ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_STAFF_DISCOUNT_AMOUNT.ordinal()));
		jFormattedTextField2.setValue(ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER_TIME.ordinal()));
		jFormattedTextField3.setValue(ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE_TIME.ordinal()));
		jFormattedTextFieldDeposit.setValue(ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_DEPOSIT.ordinal()));
		
		jRadioButton1.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR_PRICE.ordinal()));
		jRadioButton2.setSelected(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR_PRICE.ordinal()));
		jRadioButton3.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN_PRICE.ordinal()));
		jRadioButton4.setSelected(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN_PRICE.ordinal()));
		
		jSpinner2.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT.ordinal()));
		
		SetupYearComboBox();
		
		jTextFieldEmail.setText(ClientAppUtils.GetLocalValue(Values.LOCAL_VALUE_CONTACT_EMAIL));
		
		Date date = new Date();
		try {
			date = new SimpleDateFormat("HH:mm").parse(ClientAppUtils.GetLocalValue(Values.LOCAL_VALUE_NOTIFICATION_TIME));
		} catch(Exception ex){}
		jFormattedTextField4.setValue(date);
		
		try {
			date = new SimpleDateFormat("HH:mm").parse(ClientAppUtils.GetLocalValue(Values.LOCAL_VALUE_NOTIFICATION_TIME_2));
		} catch(Exception ex){}
		jFormattedTextField5.setValue(date);
		
		ClientAppUtils.SetupFocusTraversal(this);
	}

	private void SetupYearComboBox(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, "SELECT DISTINCT AMOUNT_YEAR FROM MATERIAL_AMOUNTS WHERE OFFICE_NUMBER = ?");
		multiDatabaseQuery.SetQuery(1, "SELECT DISTINCT AMOUNT_YEAR FROM TRADING_GOODS_AMOUNTS WHERE OFFICE_NUMBER = ?");
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult[] databaseQueryResults = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					databaseQueryResults = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
				ArrayList<Integer> idList = new ArrayList<>();
				
				DatabaseQueryResult databaseQueryResult = databaseQueryResults[0];
				if(databaseQueryResult != null){
					while (databaseQueryResult.next()) {
						if(!idList.contains(databaseQueryResult.getInt(0))){
							idList.add(databaseQueryResult.getInt(0));
						}
					}
				}
				
				databaseQueryResult = databaseQueryResults[1];
				if(databaseQueryResult != null){
					while (databaseQueryResult.next()) {
						if(!idList.contains(databaseQueryResult.getInt(0))){
							idList.add(databaseQueryResult.getInt(0));
						}
					}
				}
				
				if(idList.isEmpty()){
					idList.add(ClientAppSettings.currentYear);
				}
				
				for (int i = 0; i < idList.size(); ++i){
					defaultComboBoxModel.addElement(idList.get(i));
				}
				
				jComboBoxCurrentYear.setModel(defaultComboBoxModel);
				jComboBoxCurrentYear.setSelectedItem(ClientAppSettings.currentYear);
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

        buttonGroupBar = new javax.swing.ButtonGroup();
        buttonGroupKitchen = new javax.swing.ButtonGroup();
        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox24 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox13 = new javax.swing.JCheckBox();
        jCheckBox17 = new javax.swing.JCheckBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jCheckBox25 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox22 = new javax.swing.JCheckBox();
        jCheckBox23 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jCheckBox12 = new javax.swing.JCheckBox();
        jCheckBox14 = new javax.swing.JCheckBox();
        jCheckBox15 = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        jCheckBox18 = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        jFormattedTextField2 = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        jCheckBox19 = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jFormattedTextField3 = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox20 = new javax.swing.JCheckBox();
        jLabel17 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jFormattedTextFieldDeposit = new javax.swing.JFormattedTextField();
        jCheckBox26 = new javax.swing.JCheckBox();
        jCheckBox27 = new javax.swing.JCheckBox();
        jCheckBox28 = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jCheckBox21 = new javax.swing.JCheckBox();
        jCheckBox16 = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jComboBoxCurrentYear = new javax.swing.JComboBox<>();
        jPanel8 = new javax.swing.JPanel();
        jTextFieldEmail = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jFormattedTextField4 = new javax.swing.JFormattedTextField();
        jFormattedTextField5 = new javax.swing.JFormattedTextField();
        jPanel9 = new javax.swing.JPanel();
        jCheckBox29 = new javax.swing.JCheckBox();
        jCheckBox31 = new javax.swing.JCheckBox();
        jCheckBox30 = new javax.swing.JCheckBox();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Postavke kase");
        setResizable(false);

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

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Postavke kase");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(80, 65));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(80, 65));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zaštita kase"));

        jCheckBox1.setText("Lozinka za total i saldo");

        jCheckBox2.setText("Lozinka za ispis računa");

        jCheckBox3.setText("Lozinka za ispis predračuna");

        jCheckBox4.setText("Lozinka za odabir tuđeg stola");

        jCheckBox24.setText("Lozinka za odabir vlastitog stola");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox24))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox24)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Gumbi u kasi"));

        jCheckBox5.setText("Ispis stanja stola");
        jCheckBox5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox5ItemStateChanged(evt);
            }
        });

        jCheckBox6.setText("Šalji na šank");
        jCheckBox6.setPreferredSize(new java.awt.Dimension(100, 23));
        jCheckBox6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox6ItemStateChanged(evt);
            }
        });

        jCheckBox7.setText("Šalji u kuhinju");
        jCheckBox7.setPreferredSize(new java.awt.Dimension(100, 23));
        jCheckBox7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox7ItemStateChanged(evt);
            }
        });

        jCheckBox13.setText("Promjena načina plaćanja zadnjeg računa");

        jCheckBox17.setText("Račun djelatnika");
        jCheckBox17.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox17ItemStateChanged(evt);
            }
        });

        buttonGroupBar.add(jRadioButton1);
        jRadioButton1.setText("sa cijenom");
        jRadioButton1.setEnabled(false);

        buttonGroupBar.add(jRadioButton2);
        jRadioButton2.setText("bez cijene");
        jRadioButton2.setEnabled(false);

        buttonGroupKitchen.add(jRadioButton3);
        jRadioButton3.setText("sa cijenom");
        jRadioButton3.setEnabled(false);

        buttonGroupKitchen.add(jRadioButton4);
        jRadioButton4.setText("bez cijene");
        jRadioButton4.setEnabled(false);

        jCheckBox25.setText("Napomena na stavku");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton4))
                    .addComponent(jCheckBox13)
                    .addComponent(jCheckBox17)
                    .addComponent(jCheckBox25))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox25)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Napomene"));

        jCheckBox8.setText("Unos napomene kod ispisa računa");
        jCheckBox8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox8ItemStateChanged(evt);
            }
        });

        jCheckBox9.setText("Napomena kod ispisa računa - unos kod otvaranja kase");
        jCheckBox9.setEnabled(false);

        jCheckBox10.setText("Unos napomene kod ispisa na šank");

        jCheckBox11.setText("Unos napomene kod ispisa u kuhinju");

        jCheckBox22.setText("Prikaži napomene na stavkama kod ispisa na šank");

        jCheckBox23.setText("Prikaži napomene na stavkama kod ispisa u kuhinju");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox11)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox22)
                    .addComponent(jCheckBox23))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox23)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Razno"));

        jCheckBox12.setText("<html> <div> Omogući   \"event\"   cijene </div> </html>");

        jCheckBox14.setText("Automatska šifra novog artikla / trg. robe / usluge");

        jCheckBox15.setText("Automatska raspodjela radnog vremena");

        jLabel9.setText("Popust na račun djelatnika:");
        jLabel9.setEnabled(false);

        jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextField1.setText("0");
        jFormattedTextField1.setEnabled(false);
        jFormattedTextField1.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel8.setText("%");
        jLabel8.setEnabled(false);

        jCheckBox18.setText("Automatsko zaključavanje kase");
        jCheckBox18.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox18ItemStateChanged(evt);
            }
        });

        jLabel10.setText("Zaključaj kasu nakon");
        jLabel10.setEnabled(false);

        jFormattedTextField2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField2.setText("0");
        jFormattedTextField2.setEnabled(false);
        jFormattedTextField2.setPreferredSize(new java.awt.Dimension(60, 25));

        jLabel11.setText("sekundi neaktivnosti");
        jLabel11.setEnabled(false);

        jCheckBox19.setText("Automatsko zaključavanje stola");
        jCheckBox19.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox19ItemStateChanged(evt);
            }
        });

        jLabel12.setText("Zaključaj stol nakon");
        jLabel12.setEnabled(false);

        jFormattedTextField3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextField3.setText("0");
        jFormattedTextField3.setEnabled(false);
        jFormattedTextField3.setPreferredSize(new java.awt.Dimension(60, 25));

        jLabel13.setText("sekundi neaktivnosti");
        jLabel13.setEnabled(false);

        jCheckBox20.setText("Kartica povjerenja");

        jLabel17.setText("Broj stolova:");

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(10, 0, 100, 1));
        jSpinner2.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner2, "#"));
        jSpinner2.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel18.setText("Polog kase:");

        jFormattedTextFieldDeposit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldDeposit.setText("0.00");

        jCheckBox26.setText("Omogući prekovremeni rad (izvještaj radni sati)");

        jCheckBox27.setText("Prikaži prozor \"povrat gotovine\"");

        jCheckBox28.setText("Stanje stola - automatski obriši stavke");
        jCheckBox28.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox28)
                    .addComponent(jCheckBox27)
                    .addComponent(jCheckBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox14)
                    .addComponent(jCheckBox15)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8))
                    .addComponent(jCheckBox18)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11))
                    .addComponent(jCheckBox19)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13))
                    .addComponent(jCheckBox20)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextFieldDeposit, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox26))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox28)
                .addGap(10, 10, 10)
                .addComponent(jCheckBox18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jFormattedTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addComponent(jCheckBox19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jFormattedTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jFormattedTextFieldDeposit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Izgled kase"));

        jCheckBox21.setText("Touch screen");
        jCheckBox21.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox21ItemStateChanged(evt);
            }
        });

        jCheckBox16.setText("Kasa sa brzim odabirom");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox21)
                    .addComponent(jCheckBox16))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Trenutna godina"));

        jLabel14.setText("Godina:");
        jLabel14.setPreferredSize(new java.awt.Dimension(50, 14));

        jComboBoxCurrentYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jComboBoxCurrentYear, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxCurrentYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Obavijesti"));

        jLabel15.setText("Email:");

        jLabel16.setText("Vrijeme slanja:");

        jFormattedTextField4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextField4.setToolTipText("");

        jFormattedTextField5.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextField5.setToolTipText("");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jFormattedTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jFormattedTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Zaštita stola"));

        jCheckBox29.setText("Lozinka kod odabira stola");

        jCheckBox31.setText("Makni ESC kod stolova");

        jCheckBox30.setText("Makni ESC na glavnom meniju");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox30)
                    .addComponent(jCheckBox31)
                    .addComponent(jCheckBox29))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox30)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(154, 154, 154)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(32, 32, 32)))
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		float staffDiscount = 0f;
		if(jCheckBox17.isSelected()){
			if(jFormattedTextField1.getValue() != null){
				staffDiscount = ((Number)jFormattedTextField1.getValue()).floatValue();
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Popust na račun djelatnika nije ispravnog oblika");
				return;
			}
		}
		
		float cashRegisterLockTime = 0f;
		if(jCheckBox18.isSelected()){
			if(jFormattedTextField2.getValue() != null){
				cashRegisterLockTime = ((Number)jFormattedTextField2.getValue()).floatValue();
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Vrijeme zaključavanja kase nije ispravnog oblika");
				return;
			}
		}
		
		float tablesLockTime = 0f;
		if(jCheckBox19.isSelected()){
			if(jFormattedTextField3.getValue() != null){
				tablesLockTime = ((Number)jFormattedTextField3.getValue()).floatValue();
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Vrijeme zaključavanja stolova nije ispravnog oblika");
				return;
			}
		}
		
		float deposit = 0f;
		if(jFormattedTextFieldDeposit.getValue() != null){
			deposit = ((Number)jFormattedTextFieldDeposit.getValue()).floatValue();
		} else {
			ClientAppLogger.GetInstance().ShowMessage("Depozit kase nije ispravnog oblika");
			return;
		}
	
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_TOTALSALDO.ordinal(), jCheckBox1.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_PRINTINVOICE.ordinal(), jCheckBox2.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SUBTOTAL.ordinal(), jCheckBox3.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OTHER.ordinal(), jCheckBox4.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OWN.ordinal(), jCheckBox24.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SUBTOTAL.ordinal(), jCheckBox5.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR.ordinal(), jCheckBox6.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN.ordinal(), jCheckBox7.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_CHANGEPAYMENTMETHOD.ordinal(), jCheckBox13.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_STAFFINVOICE.ordinal(), jCheckBox17.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE.ordinal(), jCheckBox8.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_INVOICE_AT_START.ordinal(), jCheckBox9.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR.ordinal(), jCheckBox10.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN.ordinal(), jCheckBox11.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_BAR_PRINT.ordinal(), jCheckBox22.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_NOTES_KITCHEN_PRINT.ordinal(), jCheckBox23.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_TOUCH.ordinal(), jCheckBox21.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_QUICKCHOICE.ordinal(), jCheckBox16.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_EVENT_PRICES.ordinal(), jCheckBox12.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal(), jCheckBox14.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_WORKTIME.ordinal(), jCheckBox15.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_TRUSTCARD.ordinal(), jCheckBox20.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER.ordinal(), jCheckBox18.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE.ordinal(), jCheckBox19.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_ITEM_NOTE.ordinal(), jCheckBox25.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_OVERTIME_WORK.ordinal(), jCheckBox26.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_SHOW_CASH_RETURN_DIALOG.ordinal(), jCheckBox27.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_SUBTOTAL_AUTODELETE.ordinal(), jCheckBox28.isSelected());
		
		ClientAppSettings.SetFloat(Values.AppSettingsEnum.SETTINGS_STAFF_DISCOUNT_AMOUNT.ordinal(), staffDiscount);
		ClientAppSettings.SetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_CASHREGISTER_TIME.ordinal(), cashRegisterLockTime);
		ClientAppSettings.SetFloat(Values.AppSettingsEnum.SETTINGS_AUTO_LOCK_TABLE_TIME.ordinal(), tablesLockTime);
		ClientAppSettings.SetFloat(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_DEPOSIT.ordinal(), deposit);
		
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR_PRICE.ordinal(), jRadioButton1.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN_PRICE.ordinal(), jRadioButton3.isSelected());
		
		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT.ordinal(), ((Number)jSpinner2.getValue()).intValue());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE.ordinal(), jCheckBox29.isSelected());
                ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_ESCAPEBUTTON.ordinal(), jCheckBox31.isSelected());
                ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_CANCELBUTTON.ordinal(), jCheckBox30.isSelected());
		ClientAppSettings.SaveSettings();
		
		int newCurrentYear = Integer.valueOf(jComboBoxCurrentYear.getSelectedItem().toString());
		if(newCurrentYear != Calendar.getInstance().get(Calendar.YEAR)){
			String message = "Jeste li sigurni da želite promijeniti trenutnu godinu u: " + newCurrentYear + System.lineSeparator() + System.lineSeparator()
					+ "Trenutna godina biti će promijenjena samo na ovoj blagajni." + System.lineSeparator() 
					+ "Na blagajni nije moguće izdavati račune sve dok je trenutna godina različita od tekuće godine.";
			int dialogResult = JOptionPane.showConfirmDialog (null, message, "Promijeni trenutnu godinu", JOptionPane.YES_NO_OPTION);
			if(dialogResult != JOptionPane.YES_OPTION){
				return;
			}
		}
		
		ClientAppSettings.currentYear = newCurrentYear;
		
		ClientAppUtils.SetLocalValue(Values.LOCAL_VALUE_CONTACT_EMAIL, jTextFieldEmail.getText());
		String notifTime = "12:00";
		String notifTime2 = "20:00";
		if(jFormattedTextField4.getValue() != null){
			Date notifDate = (Date) jFormattedTextField4.getValue();
			notifTime = new SimpleDateFormat("HH:mm").format(notifDate);
		}
		if(jFormattedTextField5.getValue() != null){
			Date notifDate2 = (Date) jFormattedTextField5.getValue();
			notifTime2 = new SimpleDateFormat("HH:mm").format(notifDate2);
		}
		ClientAppUtils.SetLocalValue(Values.LOCAL_VALUE_NOTIFICATION_TIME, notifTime);
		ClientAppUtils.SetLocalValue(Values.LOCAL_VALUE_NOTIFICATION_TIME_2, notifTime2);
		
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jCheckBox6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox6ItemStateChanged
        boolean isSelected = jCheckBox6.isSelected();
		jRadioButton1.setEnabled(isSelected);
		jRadioButton2.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox6ItemStateChanged

    private void jCheckBox7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox7ItemStateChanged
        boolean isSelected = jCheckBox7.isSelected();
		jRadioButton3.setEnabled(isSelected);
		jRadioButton4.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox7ItemStateChanged

    private void jCheckBox8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox8ItemStateChanged
        boolean isSelected = jCheckBox8.isSelected();
		jCheckBox9.setEnabled(isSelected);
		if(!isSelected){
			jCheckBox9.setSelected(false);
		}
    }//GEN-LAST:event_jCheckBox8ItemStateChanged

    private void jCheckBox17ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox17ItemStateChanged
		boolean isSelected = jCheckBox17.isSelected();
		jLabel8.setEnabled(isSelected);
		jLabel9.setEnabled(isSelected);
		jFormattedTextField1.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox17ItemStateChanged

    private void jCheckBox18ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox18ItemStateChanged
		boolean isSelected = jCheckBox18.isSelected();
		jLabel10.setEnabled(isSelected);
		jLabel11.setEnabled(isSelected);
		jFormattedTextField2.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox18ItemStateChanged

    private void jCheckBox19ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox19ItemStateChanged
		boolean isSelected = jCheckBox19.isSelected();
		jLabel12.setEnabled(isSelected);
		jLabel13.setEnabled(isSelected);
		jFormattedTextField3.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox19ItemStateChanged

    private void jCheckBox21ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox21ItemStateChanged
		boolean isSelected = jCheckBox21.isSelected();
		jCheckBox16.setEnabled(!isSelected);
    }//GEN-LAST:event_jCheckBox21ItemStateChanged

    private void jCheckBox5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox5ItemStateChanged
        boolean isSelected = jCheckBox5.isSelected();
		jCheckBox28.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox5ItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupBar;
    private javax.swing.ButtonGroup buttonGroupKitchen;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox13;
    private javax.swing.JCheckBox jCheckBox14;
    private javax.swing.JCheckBox jCheckBox15;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox17;
    private javax.swing.JCheckBox jCheckBox18;
    private javax.swing.JCheckBox jCheckBox19;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox20;
    private javax.swing.JCheckBox jCheckBox21;
    private javax.swing.JCheckBox jCheckBox22;
    private javax.swing.JCheckBox jCheckBox23;
    private javax.swing.JCheckBox jCheckBox24;
    private javax.swing.JCheckBox jCheckBox25;
    private javax.swing.JCheckBox jCheckBox26;
    private javax.swing.JCheckBox jCheckBox27;
    private javax.swing.JCheckBox jCheckBox28;
    private javax.swing.JCheckBox jCheckBox29;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox30;
    private javax.swing.JCheckBox jCheckBox31;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JComboBox<String> jComboBoxCurrentYear;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JFormattedTextField jFormattedTextField2;
    private javax.swing.JFormattedTextField jFormattedTextField3;
    private javax.swing.JFormattedTextField jFormattedTextField4;
    private javax.swing.JFormattedTextField jFormattedTextField5;
    private javax.swing.JFormattedTextField jFormattedTextFieldDeposit;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JTextField jTextFieldEmail;
    // End of variables declaration//GEN-END:variables
}
