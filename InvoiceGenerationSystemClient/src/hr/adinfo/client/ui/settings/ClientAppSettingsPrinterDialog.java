/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.settings;

import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsPrinterDialog extends javax.swing.JDialog {
	
	/**
	 * Creates new form ClientAppStaffDialog
	 */
	public ClientAppSettingsPrinterDialog(java.awt.Frame parent, boolean modal) {
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
		
		// Disable non-admin settings
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN){
			jTextFieldPOSHeader1.setEnabled(false);
			jTextFieldPOSHeader2.setEnabled(false);
			jTextFieldPOSHeader3.setEnabled(false);
			jTextFieldPOSHeader4.setEnabled(false);
			jTextFieldPOSHeader5.setEnabled(false);
			jTextFieldA4Header1.setEnabled(false);
			jTextFieldA4Header2.setEnabled(false);
			jTextFieldA4Header3.setEnabled(false);
			jTextFieldA4Header4.setEnabled(false);
			jTextFieldA4Header5.setEnabled(false);
		}
		
		// Setup printers
		DefaultComboBoxModel defaultComboBoxModelInvoice = new DefaultComboBoxModel();
		DefaultComboBoxModel defaultComboBoxModelKitchen = new DefaultComboBoxModel();
		DefaultComboBoxModel defaultComboBoxModelBar = new DefaultComboBoxModel();
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printServices){
			defaultComboBoxModelInvoice.addElement(printer.getName());
			defaultComboBoxModelKitchen.addElement(printer.getName());
			defaultComboBoxModelBar.addElement(printer.getName());
		}
		jComboBoxPrinterInvoice.setModel(defaultComboBoxModelInvoice);
		jComboBoxPrinterKitchen.setModel(defaultComboBoxModelKitchen);
		jComboBoxPrinterBar.setModel(defaultComboBoxModelBar);
		
		// Load settings
		ClientAppSettings.LoadSettings();
		jCheckBox1.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN.ordinal()));
		jCheckBox2.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN_SUBTOTAL.ordinal()));
		jCheckBoxPrinterBarOn.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_BAR.ordinal()));
		jCheckBoxPrinterInvoiceOn.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_INVOICES.ordinal()));
		jCheckBoxPrinterKitchenOn.setSelected(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_KITCHEN.ordinal()));
		
		jTextFieldPOSHeader1.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_1.ordinal()));
		jTextFieldPOSHeader2.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_2.ordinal()));
		jTextFieldPOSHeader3.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_3.ordinal()));
		jTextFieldPOSHeader4.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_4.ordinal()));
		jTextFieldPOSHeader5.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_5.ordinal()));
		jTextFieldPOSHeader6.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_6.ordinal()));
		jTextFieldPOSHeader7.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_7.ordinal()));
		jTextFieldPOSFooter1.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_1.ordinal()));
		jTextFieldPOSFooter2.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_2.ordinal()));
		jTextFieldPOSFooter3.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_3.ordinal()));
		
		jTextFieldA4Header1.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_1.ordinal()));
		jTextFieldA4Header2.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_2.ordinal()));
		jTextFieldA4Header3.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_3.ordinal()));
		jTextFieldA4Header4.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_4.ordinal()));
		jTextFieldA4Header5.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_5.ordinal()));
		jTextFieldA4Header6.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_6.ordinal()));
		jTextFieldA4Header7.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_7.ordinal()));
		jTextFieldA4Footer1.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_1.ordinal()));
		jTextFieldA4Footer2.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_2.ordinal()));
		jTextFieldA4Footer3.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_3.ordinal()));
		
		jSpinnerAmountInvoice.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal()));
		jSpinnerAmountBar.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_BAR.ordinal()));
		jSpinnerAmountKitchen.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_KITCHEN.ordinal()));
		jSpinnerAmountSubtotal.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_SUBTOTAL.ordinal()));
		jSpinnerAmountEmptyLine.setValue(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()));
		
		jTextFieldPrinterInvoice.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_INVOICES.ordinal()));
		jTextFieldPrinterBar.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_BAR.ordinal()));
		jTextFieldPrinterKitchen.setText(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_KITCHEN.ordinal()));
		
		jComboBoxPrinterInvoice.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_INVOICES.ordinal()));
		jComboBoxPrinterBar.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_BAR.ordinal()));
		jComboBoxPrinterKitchen.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_KITCHEN.ordinal()));
		
		jComboBoxPrinterInvoiceType.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_INVOICES.ordinal()));
		jComboBoxPrinterBarType.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_BAR.ordinal()));
		jComboBoxPrinterKitchenType.setSelectedItem(ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_KITCHEN.ordinal()));
		
		ClientAppUtils.SetupFocusTraversal(this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldPOSHeader1 = new javax.swing.JTextField();
        jTextFieldPOSHeader2 = new javax.swing.JTextField();
        jTextFieldPOSHeader3 = new javax.swing.JTextField();
        jTextFieldPOSHeader4 = new javax.swing.JTextField();
        jTextFieldPOSHeader5 = new javax.swing.JTextField();
        jTextFieldPOSHeader6 = new javax.swing.JTextField();
        jTextFieldPOSHeader7 = new javax.swing.JTextField();
        jTextFieldPOSFooter1 = new javax.swing.JTextField();
        jTextFieldPOSFooter2 = new javax.swing.JTextField();
        jTextFieldPOSFooter3 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldA4Header1 = new javax.swing.JTextField();
        jTextFieldA4Header2 = new javax.swing.JTextField();
        jTextFieldA4Header3 = new javax.swing.JTextField();
        jTextFieldA4Header4 = new javax.swing.JTextField();
        jTextFieldA4Header5 = new javax.swing.JTextField();
        jTextFieldA4Header6 = new javax.swing.JTextField();
        jTextFieldA4Header7 = new javax.swing.JTextField();
        jTextFieldA4Footer1 = new javax.swing.JTextField();
        jTextFieldA4Footer2 = new javax.swing.JTextField();
        jTextFieldA4Footer3 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jComboBoxPrinterInvoice = new javax.swing.JComboBox<>();
        jTextFieldPrinterInvoice = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jCheckBoxPrinterInvoiceOn = new javax.swing.JCheckBox();
        jComboBoxPrinterInvoiceType = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jComboBoxPrinterKitchen = new javax.swing.JComboBox<>();
        jTextFieldPrinterKitchen = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jCheckBoxPrinterKitchenOn = new javax.swing.JCheckBox();
        jComboBoxPrinterKitchenType = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jComboBoxPrinterBar = new javax.swing.JComboBox<>();
        jTextFieldPrinterBar = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jCheckBoxPrinterBarOn = new javax.swing.JCheckBox();
        jComboBoxPrinterBarType = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jSpinnerAmountInvoice = new javax.swing.JSpinner();
        jSpinnerAmountKitchen = new javax.swing.JSpinner();
        jSpinnerAmountBar = new javax.swing.JSpinner();
        jSpinnerAmountSubtotal = new javax.swing.JSpinner();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel41 = new javax.swing.JLabel();
        jSpinnerAmountEmptyLine = new javax.swing.JSpinner();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Postavke printera");
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
        jLabel1.setText("Postavke printera");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ispis POS"));

        jLabel9.setText("Zaglavlje - linija 2:");
        jLabel9.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel10.setText("Zaglavlje - linija 3:");
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel11.setText("Zaglavlje - linija 4:");
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel12.setText("Zaglavlje - linija 5:");
        jLabel12.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel13.setText("Zaglavlje - linija 6:");
        jLabel13.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel14.setText("Zaglavlje - linija 7:");
        jLabel14.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel15.setText("Podnožje - linija 1:");
        jLabel15.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel16.setText("Podnožje - linija 2:");
        jLabel16.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel17.setText("Podnožje - linija 3:");
        jLabel17.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel8.setText("Zaglavlje - linija 1:");
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 14));

        jTextFieldPOSHeader1.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader2.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader3.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader4.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader5.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader6.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSHeader7.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSFooter1.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSFooter2.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldPOSFooter3.setPreferredSize(new java.awt.Dimension(200, 22));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSHeader7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSFooter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSFooter2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPOSFooter3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSHeader7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSFooter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSFooter2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPOSFooter3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Ispis A4"));

        jLabel18.setText("Zaglavlje - linija 2:");
        jLabel18.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel19.setText("Zaglavlje - linija 3:");
        jLabel19.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel20.setText("Zaglavlje - linija 4:");
        jLabel20.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel21.setText("Zaglavlje - linija 5:");
        jLabel21.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel22.setText("Zaglavlje - linija 6:");
        jLabel22.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel23.setText("Zaglavlje - linija 7:");
        jLabel23.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel24.setText("Podnožje - linija 1:");
        jLabel24.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel25.setText("Podnožje - linija 2:");
        jLabel25.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel26.setText("Podnožje - linija 3:");
        jLabel26.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel27.setText("Zaglavlje - linija 1:");
        jLabel27.setPreferredSize(new java.awt.Dimension(100, 14));

        jTextFieldA4Header1.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header2.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header3.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header4.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header5.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header6.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Header7.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Footer1.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Footer2.setPreferredSize(new java.awt.Dimension(200, 22));

        jTextFieldA4Footer3.setPreferredSize(new java.awt.Dimension(200, 22));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Header7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Footer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Footer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldA4Footer3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Header7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Footer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Footer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldA4Footer3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Printer za ispis - Računi"));

        jLabel28.setText("Lokalni:");
        jLabel28.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel29.setText("Mrežni:");
        jLabel29.setPreferredSize(new java.awt.Dimension(50, 14));

        jComboBoxPrinterInvoice.setPreferredSize(new java.awt.Dimension(210, 25));

        jTextFieldPrinterInvoice.setPreferredSize(new java.awt.Dimension(210, 25));

        jLabel30.setText("Tip POS printera:");

        jCheckBoxPrinterInvoiceOn.setText("Printer je uključen");

        jComboBoxPrinterInvoiceType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Epson 42", "Epson 56", "Ostalo 32", "Ostalo 42", "Ostalo 56", "Nixdorf" }));
        jComboBoxPrinterInvoiceType.setPreferredSize(new java.awt.Dimension(154, 25));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBoxPrinterInvoiceType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxPrinterInvoiceOn)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxPrinterInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldPrinterInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxPrinterInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPrinterInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jComboBoxPrinterInvoiceType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxPrinterInvoiceOn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Printer za ispis - Kuhinja"));

        jLabel31.setText("Lokalni:");
        jLabel31.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel32.setText("Mrežni:");
        jLabel32.setPreferredSize(new java.awt.Dimension(50, 14));

        jComboBoxPrinterKitchen.setPreferredSize(new java.awt.Dimension(210, 25));

        jTextFieldPrinterKitchen.setPreferredSize(new java.awt.Dimension(210, 25));

        jLabel33.setText("Tip POS printera:");

        jCheckBoxPrinterKitchenOn.setText("Printer je uključen");

        jComboBoxPrinterKitchenType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Epson 42", "Epson 56", "Ostalo 32", "Ostalo 42", "Ostalo 56", "Nixdorf" }));
        jComboBoxPrinterKitchenType.setPreferredSize(new java.awt.Dimension(154, 25));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBoxPrinterKitchenType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxPrinterKitchenOn)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxPrinterKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldPrinterKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxPrinterKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPrinterKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(jComboBoxPrinterKitchenType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxPrinterKitchenOn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Printer za ispis - Šank"));

        jLabel34.setText("Lokalni:");
        jLabel34.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel35.setText("Mrežni:");
        jLabel35.setPreferredSize(new java.awt.Dimension(50, 14));

        jComboBoxPrinterBar.setPreferredSize(new java.awt.Dimension(210, 25));

        jTextFieldPrinterBar.setPreferredSize(new java.awt.Dimension(210, 25));

        jLabel36.setText("Tip POS printera:");

        jCheckBoxPrinterBarOn.setText("Printer je uključen");

        jComboBoxPrinterBarType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Epson 42", "Epson 56", "Ostalo 32", "Ostalo 42", "Ostalo 56", "Nixdorf" }));
        jComboBoxPrinterBarType.setPreferredSize(new java.awt.Dimension(154, 25));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel36)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBoxPrinterBarType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxPrinterBarOn)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxPrinterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldPrinterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxPrinterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPrinterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jComboBoxPrinterBarType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxPrinterBarOn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Postavke"));

        jLabel37.setText("Broj kopija - ispis računa:");
        jLabel37.setPreferredSize(new java.awt.Dimension(135, 14));

        jLabel38.setText("Broj kopija - ispis kuhinja:");
        jLabel38.setPreferredSize(new java.awt.Dimension(135, 14));

        jLabel39.setText("Broj kopija - ispis šank:");
        jLabel39.setPreferredSize(new java.awt.Dimension(135, 14));

        jLabel40.setText("Broj kopija - predračun:");
        jLabel40.setPreferredSize(new java.awt.Dimension(135, 14));

        jSpinnerAmountInvoice.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));
        jSpinnerAmountInvoice.setPreferredSize(new java.awt.Dimension(50, 22));

        jSpinnerAmountKitchen.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));
        jSpinnerAmountKitchen.setPreferredSize(new java.awt.Dimension(50, 22));

        jSpinnerAmountBar.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));
        jSpinnerAmountBar.setPreferredSize(new java.awt.Dimension(50, 22));

        jSpinnerAmountSubtotal.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));
        jSpinnerAmountSubtotal.setPreferredSize(new java.awt.Dimension(50, 22));

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jCheckBox1.setText("<html> <div style=\"text-align: left\">  Kod ispisa računa automatski <br> pošalji narudžbu u kuhinju </div> </html>");

        jCheckBox2.setText("<html> <div style=\"text-align: left\">  Kod ispisa predračuna automatski <br> pošalji narudžbu u kuhinju </div> </html>");

        jLabel41.setText("Broj praznih linija (za logo):");
        jLabel41.setPreferredSize(new java.awt.Dimension(135, 14));

        jSpinnerAmountEmptyLine.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        jSpinnerAmountEmptyLine.setPreferredSize(new java.awt.Dimension(50, 22));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerAmountInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerAmountKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerAmountBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerAmountSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerAmountEmptyLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerAmountInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerAmountKitchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerAmountBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerAmountSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerAmountEmptyLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGap(120, 120, 120)
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
        ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN.ordinal(), jCheckBox1.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_AUTO_KITCHEN_SUBTOTAL.ordinal(), jCheckBox2.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_BAR.ordinal(), jCheckBoxPrinterBarOn.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_INVOICES.ordinal(), jCheckBoxPrinterInvoiceOn.isSelected());
		ClientAppSettings.SetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_KITCHEN.ordinal(), jCheckBoxPrinterKitchenOn.isSelected());
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_1.ordinal(), jTextFieldPOSHeader1.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_2.ordinal(), jTextFieldPOSHeader2.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_3.ordinal(), jTextFieldPOSHeader3.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_4.ordinal(), jTextFieldPOSHeader4.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_5.ordinal(), jTextFieldPOSHeader5.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_6.ordinal(), jTextFieldPOSHeader6.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_POS_7.ordinal(), jTextFieldPOSHeader7.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_1.ordinal(), jTextFieldPOSFooter1.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_2.ordinal(), jTextFieldPOSFooter2.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_POS_3.ordinal(), jTextFieldPOSFooter3.getText());
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_1.ordinal(), jTextFieldA4Header1.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_2.ordinal(), jTextFieldA4Header2.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_3.ordinal(), jTextFieldA4Header3.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_4.ordinal(), jTextFieldA4Header4.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_5.ordinal(), jTextFieldA4Header5.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_6.ordinal(), jTextFieldA4Header6.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_HEADER_A4_7.ordinal(), jTextFieldA4Header7.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_1.ordinal(), jTextFieldA4Footer1.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_2.ordinal(), jTextFieldA4Footer2.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_FOOTER_A4_3.ordinal(), jTextFieldA4Footer3.getText());

		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_INVOICE.ordinal(), ((Number)jSpinnerAmountInvoice.getValue()).intValue());
		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_BAR.ordinal(), ((Number)jSpinnerAmountBar.getValue()).intValue());
		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_KITCHEN.ordinal(), ((Number)jSpinnerAmountKitchen.getValue()).intValue());
		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_PRINTCOUNT_SUBTOTAL.ordinal(), ((Number)jSpinnerAmountSubtotal.getValue()).intValue());
		ClientAppSettings.SetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal(), ((Number)jSpinnerAmountEmptyLine.getValue()).intValue());
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_INVOICES.ordinal(), jTextFieldPrinterInvoice.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_BAR.ordinal(), jTextFieldPrinterBar.getText());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_NETWORK_KITCHEN.ordinal(), jTextFieldPrinterKitchen.getText());
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_INVOICES.ordinal(), (String)jComboBoxPrinterInvoice.getSelectedItem());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_BAR.ordinal(), (String)jComboBoxPrinterBar.getSelectedItem());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_KITCHEN.ordinal(), (String)jComboBoxPrinterKitchen.getSelectedItem());
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_INVOICES.ordinal(), (String)jComboBoxPrinterInvoiceType.getSelectedItem());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_BAR.ordinal(), (String)jComboBoxPrinterBarType.getSelectedItem());
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_KITCHEN.ordinal(), (String)jComboBoxPrinterKitchenType.getSelectedItem());
		
		ClientAppSettings.SaveSettings();
		
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBoxPrinterBarOn;
    private javax.swing.JCheckBox jCheckBoxPrinterInvoiceOn;
    private javax.swing.JCheckBox jCheckBoxPrinterKitchenOn;
    private javax.swing.JComboBox<String> jComboBoxPrinterBar;
    private javax.swing.JComboBox<String> jComboBoxPrinterBarType;
    private javax.swing.JComboBox<String> jComboBoxPrinterInvoice;
    private javax.swing.JComboBox<String> jComboBoxPrinterInvoiceType;
    private javax.swing.JComboBox<String> jComboBoxPrinterKitchen;
    private javax.swing.JComboBox<String> jComboBoxPrinterKitchenType;
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
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
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
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JSpinner jSpinnerAmountBar;
    private javax.swing.JSpinner jSpinnerAmountEmptyLine;
    private javax.swing.JSpinner jSpinnerAmountInvoice;
    private javax.swing.JSpinner jSpinnerAmountKitchen;
    private javax.swing.JSpinner jSpinnerAmountSubtotal;
    private javax.swing.JTextField jTextFieldA4Footer1;
    private javax.swing.JTextField jTextFieldA4Footer2;
    private javax.swing.JTextField jTextFieldA4Footer3;
    private javax.swing.JTextField jTextFieldA4Header1;
    private javax.swing.JTextField jTextFieldA4Header2;
    private javax.swing.JTextField jTextFieldA4Header3;
    private javax.swing.JTextField jTextFieldA4Header4;
    private javax.swing.JTextField jTextFieldA4Header5;
    private javax.swing.JTextField jTextFieldA4Header6;
    private javax.swing.JTextField jTextFieldA4Header7;
    private javax.swing.JTextField jTextFieldPOSFooter1;
    private javax.swing.JTextField jTextFieldPOSFooter2;
    private javax.swing.JTextField jTextFieldPOSFooter3;
    private javax.swing.JTextField jTextFieldPOSHeader1;
    private javax.swing.JTextField jTextFieldPOSHeader2;
    private javax.swing.JTextField jTextFieldPOSHeader3;
    private javax.swing.JTextField jTextFieldPOSHeader4;
    private javax.swing.JTextField jTextFieldPOSHeader5;
    private javax.swing.JTextField jTextFieldPOSHeader6;
    private javax.swing.JTextField jTextFieldPOSHeader7;
    private javax.swing.JTextField jTextFieldPrinterBar;
    private javax.swing.JTextField jTextFieldPrinterInvoice;
    private javax.swing.JTextField jTextFieldPrinterKitchen;
    // End of variables declaration//GEN-END:variables
}
