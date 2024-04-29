/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui;

//import hr.adinfo.client.AppTecaj;
import hr.adinfo.client.ui.staff.ClientAppStaffDialog;
import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.ui.cashregister.ClientAppCashRegisterStandardDialog;
import hr.adinfo.client.ui.cashregister.ClientAppCashRegisterTouchDialog;
import hr.adinfo.client.ui.clientssuppliers.ClientAppClientsSuppliersDialog;
import hr.adinfo.client.ui.receipts.ClientAppReceiptsDialog;
import hr.adinfo.client.ui.reports.ClientAppReportsDialog;
import hr.adinfo.client.ui.settings.ClientAppSettingsDialog;
import hr.adinfo.client.ui.transfers.ClientAppTransfersDialog;
import hr.adinfo.client.ui.warehouse.ClientAppWarehouseDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppMainWindow extends javax.swing.JFrame {

	/**
	 * Creates new form ClientAppMainWindow
	 */
	public ClientAppMainWindow() {
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
					if(ke.getKeyCode() == KeyEvent.VK_X){
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_M){
						jButtonAbout.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_D){
						jButtonEmployees.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_S){
						jButtonWarehouse.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_A){
						jButtonChangeUser.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_N){
						jButtonClientsSuppliers.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_P){
						jButtonReceipts.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_R){
						jButtonSettings.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_E){
						jButtonTransfers.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_K){
						jButtonCashRegister.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_I){
						jButtonReports.doClick();
					}
					
					if(ke.getKeyCode() == KeyEvent.VK_8){
						if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN){
							//new ClientAppTestDialog(null, true).setVisible(true);
						}
					}
				}
				
				return false;
			}
		});
		
		SetupCurrentUserData();
		
		ClientAppUtils.SetupFocusTraversal(this);                
	}
        

	
	private void SetupCurrentUserData(){
		// Setup current user
		jLabelCurrentName.setText("Djelatnik: " + StaffUserInfo.GetCurrentUserInfo().userId + "-" + StaffUserInfo.GetCurrentUserInfo().firstName);
		String currentRights = "Administrator";
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_OWNER){
			currentRights = "Vlasnik";
		} else if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_MANAGER){
			currentRights = "Poslovođa";
		} else if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_EMPLOYEE){
			currentRights = "Djelatnik";
		} else if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_STUDENT){
			currentRights = "Student";
		}
                
		jLabelCurrentRights.setText("Ovlaštenje: " + currentRights);
		
		jLabelCompanyName.setText(Licence.GetCompanyName());
		jLabelCompanyOIB.setText("OIB: " + Licence.GetOIB());
		jLabelOffice.setText("PP: " + Licence.GetOfficeTag()+ ", " + Licence.GetOfficeAddress());
		
                if (currentRights.contains("oslo")){
                    jButtonEmployees.setVisible(true);
                    jButtonReceipts.setVisible(true);
                    jButtonTransfers.setVisible(true);
                }
                else {
                // Setup current user rights
		jButtonEmployees.setVisible(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_STAFF]);
		jButtonReceipts.setVisible(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_RECEIPTS]);
		jButtonTransfers.setVisible(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_TRANSFERS]);
                }
		
		// Licence type
		if(Licence.IsMasterLocalServer()){
			jLabelLicenceType.setText("Glavni server");
		} else if(Licence.IsLocalServer()){
			jLabelLicenceType.setText("Lokalni server");
		} else {
			jLabelLicenceType.setText("Blagajna");
		}
		
		// TODO remove - used for testing
		/*hr.adinfo.client.datastructures.Invoice invoice = new hr.adinfo.client.datastructures.Invoice();
		invoice.isTest = true;
		invoice.clientOIB = "22413472900";
		invoice.clientName = "MINISTARSTVO GOSPODARSTVA";
		invoice.date = new java.util.Date();
		invoice.invoiceNumber = 1;
		invoice.officeTag = "1";
		invoice.cashRegisterNumber = 1;
		hr.adinfo.client.datastructures.InvoiceItem invoiceItem = new hr.adinfo.client.datastructures.InvoiceItem();
		invoiceItem.itemName = "Test item 1";
		invoiceItem.itemPrice = 10f;
		invoiceItem.itemAmount = 2f;
		invoiceItem.taxRate = 25f;
		//invoiceItem.discountPercentage = 10f;
		invoice.items.add(invoiceItem);
		//invoice.totalPrice = 18f;
		invoice.totalPrice = 20f;
		//invoice.discountValue = 5f;*/
		
		//String s = hr.adinfo.client.fiscalization.ElectronicInvoice.UploadInvoice(invoice, "");
		//System.out.println(hr.adinfo.client.fiscalization.ElectronicInvoice.GetInvoiceEncoded(invoice));
		
		//System.out.println(hr.adinfo.client.fiscalization.ElectronicInvoice.UpdateInvoiceStatus(1, "1", 1, 1, 0, 2020, "", true));
		
		//System.out.println(hr.adinfo.client.fiscalization.ElectronicInvoice.GetReceiverList("22413472900"));
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButtonWarehouse = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButtonCashRegister = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButtonReports = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabelCompanyName = new javax.swing.JLabel();
        jLabelCompanyOIB = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelOffice = new javax.swing.JLabel();
        jLabelCurrentName = new javax.swing.JLabel();
        jLabelCurrentRights = new javax.swing.JLabel();
        jPanelEmployees = new javax.swing.JPanel();
        jButtonEmployees = new javax.swing.JButton();
        jPanelReceipts = new javax.swing.JPanel();
        jButtonReceipts = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jButtonChangeUser = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jButtonSettings = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jButtonClientsSuppliers = new javax.swing.JButton();
        jPanelTransfers = new javax.swing.JPanel();
        jButtonTransfers = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jButtonAbout = new javax.swing.JButton();
        jLabelVersion = new javax.swing.JLabel();
        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();
        jLabelLicenceType = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Skladište"));
        jPanel2.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonWarehouse.setFont(jButtonWarehouse.getFont().deriveFont(jButtonWarehouse.getFont().getStyle() | java.awt.Font.BOLD, jButtonWarehouse.getFont().getSize()+2));
        jButtonWarehouse.setText("<html> <div style=\"text-align: center\"> Skladište <br> [S] </div> </html>");
        jButtonWarehouse.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonWarehouse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWarehouseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonWarehouse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonWarehouse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Kasa"));
        jPanel3.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonCashRegister.setFont(jButtonCashRegister.getFont().deriveFont(jButtonCashRegister.getFont().getStyle() | java.awt.Font.BOLD, jButtonCashRegister.getFont().getSize()+2));
        jButtonCashRegister.setText("<html> <div style=\"text-align: center\"> Kasa <br> [K] </div> </html>");
        jButtonCashRegister.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonCashRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCashRegisterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonCashRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCashRegister, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Izvješća"));
        jPanel4.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonReports.setFont(jButtonReports.getFont().deriveFont(jButtonReports.getFont().getStyle() | java.awt.Font.BOLD, jButtonReports.getFont().getSize()+2));
        jButtonReports.setText("<html> <div style=\"text-align: center\"> Izvješća <br> [I] </div> </html>");
        jButtonReports.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReportsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonReports, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonReports, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Podaci o trenutnoj prijavi"));
        jPanel5.setPreferredSize(new java.awt.Dimension(185, 145));

        jLabelCompanyName.setText("Ime firme");

        jLabelCompanyOIB.setText("OIB:");

        jLabelOffice.setText("PP:");

        jLabelCurrentName.setText("Djelatnik:");

        jLabelCurrentRights.setText("Ovlaštenje:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelCompanyOIB, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelCompanyName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelCurrentName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelCurrentRights))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(jLabelCompanyName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelCompanyOIB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOffice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelCurrentName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelCurrentRights)
                .addContainerGap())
        );

        jPanelEmployees.setBorder(javax.swing.BorderFactory.createTitledBorder("Djelatnici"));
        jPanelEmployees.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonEmployees.setFont(jButtonEmployees.getFont().deriveFont(jButtonEmployees.getFont().getStyle() | java.awt.Font.BOLD, jButtonEmployees.getFont().getSize()+2));
        jButtonEmployees.setText("<html>\n<div style=\"text-align: center\">\nDjelatnici\n<br>\n[D]\n</div>\n</html>");
        jButtonEmployees.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonEmployees.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEmployeesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelEmployeesLayout = new javax.swing.GroupLayout(jPanelEmployees);
        jPanelEmployees.setLayout(jPanelEmployeesLayout);
        jPanelEmployeesLayout.setHorizontalGroup(
            jPanelEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEmployeesLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanelEmployeesLayout.setVerticalGroup(
            jPanelEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEmployeesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonEmployees, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelReceipts.setBorder(javax.swing.BorderFactory.createTitledBorder("Primke"));
        jPanelReceipts.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonReceipts.setFont(jButtonReceipts.getFont().deriveFont(jButtonReceipts.getFont().getStyle() | java.awt.Font.BOLD, jButtonReceipts.getFont().getSize()+2));
        jButtonReceipts.setText("<html>\n<div style=\"text-align: center\">\nPrimke\n<br>\n[P]\n</div>\n</html>");
        jButtonReceipts.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonReceipts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReceiptsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelReceiptsLayout = new javax.swing.GroupLayout(jPanelReceipts);
        jPanelReceipts.setLayout(jPanelReceiptsLayout);
        jPanelReceiptsLayout.setHorizontalGroup(
            jPanelReceiptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReceiptsLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanelReceiptsLayout.setVerticalGroup(
            jPanelReceiptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReceiptsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonReceipts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Promjena korisnika"));
        jPanel8.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonChangeUser.setFont(jButtonChangeUser.getFont().deriveFont(jButtonChangeUser.getFont().getStyle() | java.awt.Font.BOLD, jButtonChangeUser.getFont().getSize()+2));
        jButtonChangeUser.setText("<html> <div style=\"text-align: center\"> Promjena korisnika <br> [A] </div> </html>");
        jButtonChangeUser.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonChangeUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeUserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonChangeUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonChangeUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Izlaz"));
        jPanel9.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonExit.setFont(jButtonExit.getFont().deriveFont(jButtonExit.getFont().getStyle() | java.awt.Font.BOLD, jButtonExit.getFont().getSize()+2));
        jButtonExit.setText("<html>\n<div style=\"text-align: center\">\nIzlaz\n<br>\n[X]\n</div>\n</html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Postavke programa"));
        jPanel10.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonSettings.setFont(jButtonSettings.getFont().deriveFont(jButtonSettings.getFont().getStyle() | java.awt.Font.BOLD, jButtonSettings.getFont().getSize()+2));
        jButtonSettings.setText("<html>\n<div style=\"text-align: center\">\nPostavke\n<br>\n[R]\n</div>\n</html>");
        jButtonSettings.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSettingsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Klijenti i dobavljači"));
        jPanel12.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonClientsSuppliers.setFont(jButtonClientsSuppliers.getFont().deriveFont(jButtonClientsSuppliers.getFont().getStyle() | java.awt.Font.BOLD, jButtonClientsSuppliers.getFont().getSize()+2));
        jButtonClientsSuppliers.setText("<html>\n<div style=\"text-align: center\">\nKlijenti i dobavljači\n<br>\n[N]\n</div>\n</html>");
        jButtonClientsSuppliers.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonClientsSuppliers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClientsSuppliersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonClientsSuppliers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonClientsSuppliers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelTransfers.setBorder(javax.swing.BorderFactory.createTitledBorder("Međuskladišnica"));
        jPanelTransfers.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonTransfers.setFont(jButtonTransfers.getFont().deriveFont(jButtonTransfers.getFont().getStyle() | java.awt.Font.BOLD, jButtonTransfers.getFont().getSize()+2));
        jButtonTransfers.setText("<html>\n<div style=\"text-align: center\">\nMeđu- skladišnica\n<br>\n[E]\n</div>\n</html>");
        jButtonTransfers.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonTransfers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTransfersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTransfersLayout = new javax.swing.GroupLayout(jPanelTransfers);
        jPanelTransfers.setLayout(jPanelTransfersLayout);
        jPanelTransfersLayout.setHorizontalGroup(
            jPanelTransfersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTransfersLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonTransfers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanelTransfersLayout.setVerticalGroup(
            jPanelTransfersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTransfersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonTransfers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("O programu"));
        jPanel11.setPreferredSize(new java.awt.Dimension(185, 145));

        jButtonAbout.setFont(jButtonAbout.getFont().deriveFont(jButtonAbout.getFont().getStyle() | java.awt.Font.BOLD, jButtonAbout.getFont().getSize()+2));
        jButtonAbout.setText("<html>\n<div style=\"text-align: center\">\nO programu\n<br>\n[M]\n</div>\n</html>");
        jButtonAbout.setPreferredSize(new java.awt.Dimension(100, 100));
        jButtonAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAboutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButtonAbout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAbout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelTransfers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelTransfers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelVersion.setText("Verzija 1.0.81");

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

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jLabelLicenceType.setForeground(new java.awt.Color(153, 153, 153));
        jLabelLicenceType.setText("Tip licence");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelVersion)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelLicenceType))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 6, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelVersion)
                    .addComponent(jLabelLicenceType))
                .addGap(5, 5, 5))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
		if(ClientApp.GetInstance() != null){
			ClientApp.GetInstance().OnAppClose();
		}
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAboutActionPerformed
		String message = "Novosti u programu:" + System.lineSeparator()
				+ System.lineSeparator()
				
				//+ "Verzija 0.9.41: " + System.lineSeparator() + System.lineSeparator()
				//+ "- po defaultu user admin je dobio OIB iz licence (oib firme), pa zato nije dalo dodati djelatnika sa istim oibom (npr kod obrta). sada admin ima oib 00000000001" + System.lineSeparator()
				//+ "- ako kod izdavanja računa nije dobiven JIR sada piše: Nije dobiven u predviđenom vremenu" + System.lineSeparator()
				//+ "- ponekad se u kasi nije pojavila obavijest da postoje nefiskalizirani računi - sada radi" + System.lineSeparator()
				//+ "- ispravak greške sa krivim zbrajanjem stanja na skladištu (materijali)" + System.lineSeparator()
	
				+ System.lineSeparator();
		ClientAppLogger.GetInstance().ShowMessage(message);
    }//GEN-LAST:event_jButtonAboutActionPerformed

    private void jButtonEmployeesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEmployeesActionPerformed
	if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN || StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_OWNER || StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_MANAGER){
            new ClientAppStaffDialog(null, true).setVisible(true);
        } 
        else {
        new ClientAppStaffDialog(null, true).setVisible(false);
         }
    }//GEN-LAST:event_jButtonEmployeesActionPerformed

    private void jButtonWarehouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWarehouseActionPerformed
        new ClientAppWarehouseDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonWarehouseActionPerformed

    private void jButtonChangeUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeUserActionPerformed
		ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, true, -1);
		dialog.setVisible(true);
		if(dialog.loginSuccess){
			SetupCurrentUserData();
		}
    }//GEN-LAST:event_jButtonChangeUserActionPerformed

    private void jButtonClientsSuppliersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClientsSuppliersActionPerformed
		new ClientAppClientsSuppliersDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonClientsSuppliersActionPerformed

    private void jButtonReceiptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReceiptsActionPerformed
		new ClientAppReceiptsDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonReceiptsActionPerformed

    private void jButtonSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSettingsActionPerformed
		new ClientAppSettingsDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonSettingsActionPerformed

    private void jButtonTransfersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTransfersActionPerformed
		new ClientAppTransfersDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonTransfersActionPerformed

    private void jButtonCashRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCashRegisterActionPerformed
		if(Licence.GetOfficeNumber() == -1){
			ClientAppLogger.GetInstance().ShowMessage("Licenca nije ispravna! Molimo ponovno pokrenite aplikaciju.");
			return;
		}
                
                
		
		if(ClientAppSettings.currentYear != Calendar.getInstance().get(Calendar.YEAR)){
			ClientAppLogger.GetInstance().ShowMessage("Trenutno odabrana godina različita je od tekuće godine. Molimo promijenite trenutnu godinu u postavkama kase.");
			return;
		}
                
		final JDialog cashRegisterEntryLoadingDialog = new LoadingDialog(null, true);
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				cashRegisterEntryLoadingDialog.setVisible(true);
			}
		});
		
		ClientAppSettings.LoadSettings(false);
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		boolean isTouch = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_TOUCH.ordinal());
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN && isProduction){
			ClientAppLogger.GetInstance().ShowMessage("U produkcijskom okruženju nije moguće ući u kasu kao Admin");
			Utils.DisposeDialog(cashRegisterEntryLoadingDialog);
			return;
		}
		
		if(Licence.IsControlApp()){
			ClientAppLogger.GetInstance().ShowMessage("Nije moguće ući u kasu kroz kontrolnu aplikaciju");
			Utils.DisposeDialog(cashRegisterEntryLoadingDialog);
			return;
		}
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				if(isTouch){
                                    try {
                                        ClientAppCashRegisterTouchDialog clientAppCashRegisterTouchDialog = new ClientAppCashRegisterTouchDialog(null, true);
                                        Utils.DisposeDialog(cashRegisterEntryLoadingDialog);
                                        clientAppCashRegisterTouchDialog.setVisible(true);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ClientAppMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                                    }
				} else {
					ClientAppCashRegisterStandardDialog clientAppCashRegisterStandardDialog = new ClientAppCashRegisterStandardDialog(null, true);
					Utils.DisposeDialog(cashRegisterEntryLoadingDialog);
					clientAppCashRegisterStandardDialog.setVisible(true);
				}
			}
		});
		
    }//GEN-LAST:event_jButtonCashRegisterActionPerformed
    
    private void jButtonReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReportsActionPerformed
       new ClientAppReportsDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonReportsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAbout;
    private javax.swing.JButton jButtonCashRegister;
    private javax.swing.JButton jButtonChangeUser;
    private javax.swing.JButton jButtonClientsSuppliers;
    private javax.swing.JButton jButtonEmployees;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonReceipts;
    private javax.swing.JButton jButtonReports;
    private javax.swing.JButton jButtonSettings;
    private javax.swing.JButton jButtonTransfers;
    private javax.swing.JButton jButtonWarehouse;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelCompanyName;
    private javax.swing.JLabel jLabelCompanyOIB;
    private javax.swing.JLabel jLabelCurrentName;
    private javax.swing.JLabel jLabelCurrentRights;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelLicenceType;
    private javax.swing.JLabel jLabelOffice;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelEmployees;
    private javax.swing.JPanel jPanelReceipts;
    private javax.swing.JPanel jPanelTransfers;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}
