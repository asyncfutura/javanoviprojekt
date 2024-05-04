/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.cashregister;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.ui.ClientAppLoginDialog;
import hr.adinfo.client.ui.ClientAppLoginChangingUserDialog;
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
import hr.adinfo.utils.extensions.ColorIcon;
import hr.adinfo.utils.licence.Licence;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Matej
 */
public class ClientAppCashRegisterTablesClickedDialog extends javax.swing.JDialog {
	
	private int tablesCount;
	private JButton[] jButtonList;
	
	private int cashRegisterNumber;
	private boolean isEmpty;
	private int[] tableStaffId;
	private int[] tableCashRegisterNumber;
	
	public int selectedTableId;
        public boolean isTableAuth;
        public boolean tbSelected;
        public boolean employeeClickTable;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppCashRegisterTablesClickedDialog(java.awt.Frame parent, boolean modal, int selectedTableId, int cashRegisterNumber, boolean isEmpty, boolean tableAuth, boolean employeeClickTable) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_ENTER){
						jButtonSelect.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDeleteAll.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jTextFieldTableNumber.grabFocus();
					} else if(ke.getKeyCode() == KeyEvent.VK_F2){
						jRadioButtonSelectTable.grabFocus();
					}
				}
				
				return false;
			}
		});
		
		this.selectedTableId = selectedTableId;
		this.cashRegisterNumber = cashRegisterNumber;
		this.isEmpty = isEmpty;
		this.isTableAuth = tableAuth;
                
                thisWindow.setFocusableWindowState(true);
                thisWindow.requestFocusInWindow();
                thisWindow.pack();
                
                if (employeeClickTable){
                    thisWindow.dispose();
                }
               
                                
		jButtonList = new JButton[]{
			jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9, jButton10,
			jButton11, jButton12, jButton13, jButton14, jButton15, jButton16, jButton17, jButton18, jButton19, jButton20,
			jButton21, jButton22, jButton23, jButton24, jButton25, jButton26, jButton27, jButton28, jButton29, jButton30,
			jButton31, jButton32, jButton33, jButton34, jButton35, jButton36, jButton37, jButton38, jButton39, jButton40,
			jButton41, jButton42, jButton43, jButton44, jButton45, jButton46, jButton47, jButton48, jButton49, jButton50,
			jButton51, jButton52, jButton53, jButton54, jButton55, jButton56, jButton57, jButton58, jButton59, jButton60,
			jButton61, jButton62, jButton63, jButton64, jButton65, jButton66, jButton67, jButton68, jButton69, jButton70,
			jButton71, jButton72, jButton73, jButton74, jButton75, jButton76, jButton77, jButton78, jButton79, jButton80,
			jButton81, jButton82, jButton83, jButton84, jButton85, jButton86, jButton87, jButton88, jButton89, jButton90,
			jButton91, jButton92, jButton93, jButton94, jButton95, jButton96, jButton97, jButton98, jButton99, jButton100,
		};
		
		tablesCount = ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_TABLES_COUNT.ordinal());
		
		tableStaffId = new int[jButtonList.length];
		tableCashRegisterNumber = new int[jButtonList.length];
		for (int i = 0; i < jButtonList.length; ++i){
			SetButtonText(jButtonList[i], i+1, "Slobodan", "", "", "");
			if(i >= tablesCount){
				jButtonList[i].setVisible(false);
			}
			
			final int index = i;
			jButtonList[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				jButtonActionPerformed(index);
			});
			
			tableStaffId[i] = -1;
			tableCashRegisterNumber[i] = -1;
		}
		
		jButtonDeleteAll.setEnabled(StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_CASHREGISTER_TABLEDELETE]);
		
		jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(40, 0));
		
		RefreshTables();
		
		if(selectedTableId == -1 && !isEmpty){
			jRadioButtonMoveTable.setSelected(true);
			jRadioButtonSelectTable.setEnabled(false);
			jRadioButtonMergeTables.setEnabled(false);
			jRadioButtonSplitTables.setEnabled(false);
		} else if(selectedTableId == -1 && isEmpty){
			jRadioButtonSelectTable.setSelected(true);
			jRadioButtonMoveTable.setEnabled(false);
			jRadioButtonMergeTables.setEnabled(false);
			jRadioButtonSplitTables.setEnabled(false);
		}
		
		if (isEmpty){
			jRadioButtonSplitTables.setEnabled(false);
		}
		
		if(selectedTableId != -1){
			jButtonList[selectedTableId].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[2], jButtonList[selectedTableId].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonList[selectedTableId].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		}
                
                 if (StaffUserInfo.GetCurrentUserInfo().userOIB.length() > 0){
                               final JDialog loadingDialog = new LoadingDialog(null, true);
                                int currentStaffId = StaffUserInfo.GetCurrentUserInfo().userId;
                                String currentStaffName = StaffUserInfo.GetCurrentUserInfo().firstName;

                                String query = "SELECT STAFF_NAME FROM TABLES";
                                DatabaseQuery databaseQuery = new DatabaseQuery(query);
                                databaseQuery.executeLocally = true;
                		
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
                                                 ClientAppLogger.GetInstance().LogMessage("After database query, current staff name is: " + currentStaffName);
                                                                if (currentStaffId == -1 && currentStaffName.length() > 0){
                                                                    StaffUserInfo.GetCurrentUserInfo().userId = SetUser(currentStaffId, currentStaffName, serverResponse, databaseQueryResult);
                                                                    StaffUserInfo.GetCurrentUserInfo().firstName = currentStaffName;
                                                                }
                                                }
                                                
                                        } catch (InterruptedException | ExecutionException ex) {
                                                ClientAppLogger.GetInstance().ShowErrorLog(ex);
                                        }

                                }
		
		jLabelStaffName.setText(StaffUserInfo.GetCurrentUserInfo().userId + "-" + StaffUserInfo.GetCurrentUserInfo().firstName);
                ClientAppLogger.GetInstance().LogMessage("After method execute, current staff Id is: " +  StaffUserInfo.GetCurrentUserInfo().userId);
                //dio koda koji isključuje gumb za izlaz iz forme i onemogućuje zatvaranje forme na X
//		if(this.isTableAuth == true){
//                    jButtonExit.setEnabled(false);
//                    this.setDefaultCloseOperation(0);
//                }
		ClientAppUtils.SetupFocusTraversal(this);
                 }
	}
        
        private int SetUser(int currentStaffId, String staffNameString, ServerResponse serverResponse, DatabaseQueryResult databaseQueryResult){
                                    final JDialog loadingQueryDialog = new LoadingDialog(null, true);
                                    MultiDatabaseQuery multiDatabaseStaffQuery = new MultiDatabaseQuery(1);
                                    ServerQueryTask databaseQueryStaffTask = new ServerQueryTask(loadingQueryDialog, multiDatabaseStaffQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

                                    String staffIdQuery;
                                    int staffId = currentStaffId;
                                    staffIdQuery = "SELECT ID FROM STAFF WHERE FIRST_NAME = ?";
                                    multiDatabaseStaffQuery.SetQuery(0, staffIdQuery);
                                    multiDatabaseStaffQuery.AddParam(0, 1, staffNameString);

                                    databaseQueryStaffTask.execute();
                                    loadingQueryDialog.setVisible(true);

                                    if (!databaseQueryStaffTask.isDone()) {
                                        databaseQueryStaffTask.cancel(true);
                                    } else {
                                         try {
                                            ServerResponse serverIDResponse = databaseQueryStaffTask.get();
                                            DatabaseQueryResult[] databaseQueryIDResult = null;
                                            if (serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS) {
                                                databaseQueryIDResult = ((MultiDatabaseQueryResponse) serverIDResponse).databaseQueryResult;
                                            }
                                            if (databaseQueryResult != null) {
                                                // Cash registers
                                                while (databaseQueryIDResult[0].next()) {
                                                    staffId = databaseQueryIDResult[0].getInt(0);
                                                }
                                            }
                                        } catch (InterruptedException | ExecutionException ex) {
                                            ClientAppLogger.GetInstance().ShowErrorLog(ex);
                                        }
                                    }
                                    
                                    return staffId;
        }
	
	private void RefreshTables(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
                String isFreeString = "";
                String cashRegisterString = "";
                String staffNameString = "";
                String priceString = "";
                int currentStaffId = -1;
		
		String query = "SELECT ID, STAFF_ID, STAFF_NAME, PRICE, CR_NUM FROM TABLES";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.executeLocally = true;
//		
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
					while (databaseQueryResult.next()) {
						int tableId = databaseQueryResult.getInt(0);
						currentStaffId = databaseQueryResult.getInt(1);
						String currentStaffName = databaseQueryResult.getString(2);
						String currentPrice = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
						int currentCashRegisterNumber = databaseQueryResult.getInt(4);
						tableCashRegisterNumber[tableId] = currentCashRegisterNumber;
                                                if (currentStaffId == -1 && currentStaffName.length() > 0){
                                                    currentStaffId = SetUser(currentStaffId, currentStaffName, serverResponse, databaseQueryResult);
                                                }
                                                
                                                tableStaffId[tableId] = currentStaffId;
                                                
						isFreeString = currentStaffId == -1 ? "Slobodan" : "Zauzet";
						cashRegisterString = currentStaffId != -1 ? "Kasa: " + currentCashRegisterNumber : "";
						staffNameString = currentStaffId != -1 ? currentStaffId + "-" + currentStaffName : "";
						priceString = currentStaffId != -1 ? currentPrice + " eur" : "";
						
						SetButtonText(jButtonList[tableId], tableId + 1, isFreeString, cashRegisterString, staffNameString, priceString);
					}
				}
                                
                                
                                
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
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
        jPanelButtons = new javax.swing.JPanel();
        jButtonSelect = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonDeleteAll = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldTableNumber = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabelStaffName = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton95 = new javax.swing.JButton();
        jButton96 = new javax.swing.JButton();
        jButton97 = new javax.swing.JButton();
        jButton98 = new javax.swing.JButton();
        jButton99 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jRadioButtonSelectTable = new javax.swing.JRadioButton();
        jRadioButtonMergeTables = new javax.swing.JRadioButton();
        jRadioButtonSplitTables = new javax.swing.JRadioButton();
        jRadioButtonMoveTable = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Stolovi");
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSelect.setText("<html> <div style=\"text-align: center\"> Odaberi <br> [ENTER] </div> </html>");
        jButtonSelect.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonDeleteAll.setText("<html> <div style=\"text-align: center\"> Obriši sve stolove <br> [DEL] </div> </html>");
        jButtonDeleteAll.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAll.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonDeleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButtonDeleteAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Odaberi stol broj [F1]:");

        jTextFieldTableNumber.setPreferredSize(new java.awt.Dimension(200, 25));

        jLabel2.setText("Trenutni djelatnik:");

        jLabelStaffName.setText("djelatnik");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jTextFieldTableNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(156, 156, 156)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jLabelStaffName, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldTableNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelStaffName))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Stolovi");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Stolovi"));

        jButton1.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton3.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton4.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton5.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton6.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton7.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton8.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton9.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton10.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton11.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton12.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton13.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton14.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton15.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton16.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton17.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton18.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton19.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton20.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton21.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton22.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton22.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton23.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton23.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton24.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton24.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton25.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton25.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton26.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton26.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton27.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton27.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton27.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton28.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton28.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton28.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton29.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton29.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton29.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton30.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton30.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton30.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton31.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton31.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton31.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton32.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton32.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton32.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton33.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton33.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton33.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton34.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton34.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton34.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton35.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton35.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton35.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton36.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton36.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton36.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton37.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton37.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton37.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton38.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton38.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton38.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton39.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton39.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton39.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton40.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton40.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton40.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton41.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton41.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton41.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton42.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton42.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton42.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton43.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton43.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton43.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton44.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton44.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton44.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton45.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton45.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton45.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton46.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton46.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton46.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton47.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton47.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton47.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton48.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton48.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton48.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton49.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton49.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton49.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton50.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton50.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton50.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton51.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton51.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton51.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton52.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton52.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton52.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton53.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton53.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton53.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton54.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton54.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton54.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton55.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton55.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton55.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton56.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton56.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton56.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton57.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton57.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton57.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton58.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton58.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton58.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton59.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton59.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton59.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton60.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton60.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton60.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton61.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton61.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton61.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton62.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton62.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton62.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton63.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton63.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton63.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton64.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton64.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton64.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton65.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton65.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton65.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton66.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton66.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton66.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton67.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton67.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton67.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton68.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton68.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton68.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton69.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton69.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton69.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton70.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton70.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton70.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton71.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton71.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton71.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton72.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton72.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton72.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton73.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton73.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton73.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton74.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton74.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton74.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton75.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton75.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton75.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton76.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton76.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton76.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton77.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton77.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton77.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton78.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton78.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton78.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton79.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton79.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton79.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton80.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton80.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton80.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton81.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton81.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton81.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton82.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton82.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton82.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton83.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton83.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton83.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton84.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton84.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton84.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton85.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton85.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton85.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton86.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton86.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton86.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton87.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton87.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton87.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton88.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton88.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton88.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton89.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton89.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton89.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton90.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton90.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton90.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton91.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton91.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton91.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton92.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton92.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton92.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton93.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton93.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton93.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton94.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton94.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton94.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton95.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton95.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton95.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton96.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton96.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton96.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton97.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton97.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton97.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton98.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton98.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton98.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton99.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton99.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton99.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton100.setText("<html> <div style=\"text-align: center\">  1 <br> Slobodan </div> </html>");
        jButton100.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton100.setMargin(new java.awt.Insets(2, 6, 2, 6));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton54, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton55, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton56, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton58, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton59, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton60, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton61, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton62, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton63, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton64, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton65, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton66, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton67, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton68, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton69, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton71, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton72, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton73, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton74, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton75, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton76, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton77, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton78, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton79, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton80, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton81, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton82, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton83, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton84, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton85, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton86, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton87, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton88, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton89, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton90, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton91, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton92, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton93, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton94, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton95, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton96, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton97, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton98, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton99, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton100, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton60, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton59, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton58, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton56, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton55, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton54, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton69, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton68, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton67, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton66, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton65, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton62, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton61, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton63, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton64, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton80, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton79, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton78, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton77, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton76, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton75, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton72, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton71, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton73, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton74, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton90, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton89, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton88, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton87, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton86, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton85, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton82, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton81, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton83, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton84, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton100, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton99, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton98, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton97, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton96, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton95, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton92, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton91, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton93, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton94, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Opcija [F2]"));

        buttonGroup1.add(jRadioButtonSelectTable);
        jRadioButtonSelectTable.setSelected(true);
        jRadioButtonSelectTable.setText("Prijeđi na odabrani stol");

        buttonGroup1.add(jRadioButtonMergeTables);
        jRadioButtonMergeTables.setText("Spoji trenutni stol sa odabranim stolom");

        buttonGroup1.add(jRadioButtonSplitTables);
        jRadioButtonSplitTables.setText("Razdvoji trenutni stol na odabrani stol");

        buttonGroup1.add(jRadioButtonMoveTable);
        jRadioButtonMoveTable.setText("Prebaci trenutni stol na odabrani stol");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonSelectTable)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonMergeTables)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonSplitTables)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonMoveTable)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonSelectTable)
                    .addComponent(jRadioButtonMergeTables)
                    .addComponent(jRadioButtonSplitTables)
                    .addComponent(jRadioButtonMoveTable))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(434, 434, 434)
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel9)
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
		try {
			int index = Integer.parseInt(jTextFieldTableNumber.getText().trim());
			jButtonActionPerformed(index);
		} catch (Exception ex){
			ClientAppLogger.GetInstance().ShowMessage("Uneseni broj stola nije ispravan.");
		}
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
	tbSelected = false;	
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllActionPerformed
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati sve stolove?", "Obriši stolove", JOptionPane.YES_NO_OPTION);
        if(dialogResult != JOptionPane.YES_OPTION){
			return;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "UPDATE TABLES SET STAFF_ID = -1, CR_NUM = -1, PRICE = 0, STAFF_NAME = '', INVOICE_DATA = NULL";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.executeLocally = true;
		
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
					jButtonExit.doClick();
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
    }//GEN-LAST:event_jButtonDeleteAllActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        
    }//GEN-LAST:event_formWindowGainedFocus

	public static void SetButtonText(JButton button, int tableNumber, String isFree, String cashRegister, String staffName, String price){
		String prefix = "<html> <div style=\"text-align: center\"> ";
		String sufix = " </div> </html>";
		button.setText(prefix + tableNumber + "<br>" + isFree + "<br>" + cashRegister + "<br>" + staffName + "<br>" + price + sufix);
	}
	
	private void jButtonActionPerformed(int index) {                                         
		if(index >= tablesCount){
			ClientAppLogger.GetInstance().ShowMessage("Uneseni broj stola nije ispravan.");
			return;
		}
		
		RefreshTables();
		
		boolean isTableFree = tableStaffId[index] == -1;
		boolean needPassOwn = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OWN.ordinal());
		boolean needPassOther = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE_OTHER.ordinal());
                boolean needPassSelect = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PASS_SELECTTABLE.ordinal());
		
		if(jRadioButtonSelectTable.isSelected()){
			// Different cash register
			if(!isTableFree && cashRegisterNumber != tableCashRegisterNumber[index]){
				int dialogResult = JOptionPane.showConfirmDialog (null, "Odabrali ste stol sa druge kase. " + System.lineSeparator()
						+ "Ova opcija prebaciti će taj stol na vašu kasu. " + System.lineSeparator() + System.lineSeparator()
						+ "Svejedno odaberi stol sa druge kase?", "Upozorenje!", JOptionPane.YES_NO_OPTION);
				if(dialogResult != JOptionPane.YES_OPTION){
					return;
				}
			}

			// Authorization
                        //12.10.2022 Izbačeno !isTableFree iz uvjeta
                        //if(!isTableFree && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER)
                        
                        if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN || StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
                            if(needPassSelect && StaffUserInfo.GetCurrentUserInfo().userId == tableStaffId[index]){ 
                                    ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, tableStaffId[index]);
					dialog.setVisible(true);
					if(!dialog.loginSuccess){
						return;
					}
                                }else if(needPassOther || needPassSelect && StaffUserInfo.GetCurrentUserInfo().userId != tableStaffId[index]){
					ClientAppLogger.GetInstance().ShowMessage("Odabrali ste stol drugog djelatnika. Unesite šifru tog djelatnika.");
					ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, tableStaffId[index]);
					dialog.setVisible(true);
					if(!dialog.loginSuccess){
						return;
					}
				} else if(needPassOwn && StaffUserInfo.GetCurrentUserInfo().userId == tableStaffId[index]){
					ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, tableStaffId[index]);
					dialog.setVisible(true);
					if(!dialog.loginSuccess){
						return;
					}
				}
                            
                             if(!isTableFree && needPassSelect && StaffUserInfo.GetCurrentUserInfo().userId != tableStaffId[index] && 
                                 StaffUserInfo.GetCurrentUserInfo().userId > -1){
                                 ClientAppLoginChangingUserDialog myDialog = new ClientAppLoginChangingUserDialog(null, true, true,StaffUserInfo.GetCurrentUserInfo().userId, tableStaffId[index]);
                                 myDialog.setVisible(true);
					if(!myDialog.loginSuccess){
						return;
					}
			}
                        			
                        
			// Clear if old table empty
			if(isEmpty && selectedTableId != -1){
				ClearTable(selectedTableId);
			}
			this.tbSelected = true;
			selectedTableId = index;
			jButtonExit.doClick();
		} else if(jRadioButtonMergeTables.isSelected()){
			// Different cash register
			if(!isTableFree && cashRegisterNumber != tableCashRegisterNumber[index]){
				int dialogResult = JOptionPane.showConfirmDialog (null, "Odabrali ste stol sa druge kase. " + System.lineSeparator()
						+ "Ova opcija prebaciti će taj stol na vašu kasu. " + System.lineSeparator() + System.lineSeparator()
						+ "Svejedno odaberi stol sa druge kase?", "Upozorenje!", JOptionPane.YES_NO_OPTION);
				if(dialogResult != JOptionPane.YES_OPTION){
					return;
				}
			}

			// Authorization
			if(!isTableFree && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER){
				if(needPassOther && StaffUserInfo.GetCurrentUserInfo().userId != tableStaffId[index]){
					ClientAppLogger.GetInstance().ShowMessage("Odabrali ste stol drugog djelatnika. Unesite šifru tog djelatnika.");
					ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, tableStaffId[index]);
					dialog.setVisible(true);
					if(!dialog.loginSuccess){
						return;
					}
				} else if(needPassOwn && StaffUserInfo.GetCurrentUserInfo().userId == tableStaffId[index]){
					ClientAppLoginDialog dialog = new ClientAppLoginDialog(null, true, false, tableStaffId[index]);
					dialog.setVisible(true);
					if(!dialog.loginSuccess){
						return;
					}
				}
			}
			
			// Merge tables
			MergeTables(selectedTableId, index);
			
			jButtonExit.doClick();
		} else if(jRadioButtonMoveTable.isSelected()){
			if(!isTableFree){
				ClientAppLogger.GetInstance().ShowMessage("Odabrani stol je zauzet.");
				return;
			}
			
			// Move table
			if(selectedTableId != -1){
				CopyTable(selectedTableId, index);
				ClearTable(selectedTableId);
			}
			
			selectedTableId = index;
			jButtonExit.doClick();
		} else if(jRadioButtonSplitTables.isSelected()){
			if(!isTableFree){
				ClientAppLogger.GetInstance().ShowMessage("Odabrani stol je zauzet.");
				return;
			}
			
			// Copy table
			if(selectedTableId != -1){
				CopyTable(selectedTableId, index);
				Invoice originalInvoice = LoadTable(selectedTableId);
				if(originalInvoice == null){
					ClearTable(selectedTableId);
					ClearTable(index);
					jButtonExit.doClick();
					return;
				}
				Invoice splittedInvoice = new Invoice(originalInvoice);
				ClientAppCashRegisterTablesSplitDialog dialog = new ClientAppCashRegisterTablesSplitDialog(null, true, splittedInvoice);
				dialog.setVisible(true);
				if (dialog.splitSuccess){
					Invoice originalSplitedInvoice = SplitTable(originalInvoice, splittedInvoice);
					if (originalSplitedInvoice != null){
						splittedInvoice.staffId = StaffUserInfo.GetCurrentUserInfo().userId;
						splittedInvoice.cashRegisterNumber = Licence.GetCashRegisterNumber();
						
						SaveTable(originalSplitedInvoice, selectedTableId);
						SaveTable(splittedInvoice, index);
					}
				} else {
					ClearTable(index);
				}
			}
			
			jButtonExit.doClick();
		}
            }
    }
	
	private Invoice SplitTable(Invoice originalInvoice, Invoice splittedInvoice){
		if(originalInvoice == null || splittedInvoice == null)
			return null;
		
		Invoice originalSplitedInvoice = new Invoice(originalInvoice);
		
		// Calculate item amounts
		for (int i = 0; i < splittedInvoice.items.size(); ++i){
			for (int j = 0; j < originalSplitedInvoice.items.size(); ++j){
				InvoiceItem itemSplitted = splittedInvoice.items.get(i);
				InvoiceItem itemOriginal = originalSplitedInvoice.items.get(j);
				if (itemSplitted.itemId == itemOriginal.itemId && itemSplitted.itemPrice == itemOriginal.itemPrice
						&& itemSplitted.discountPercentage == itemOriginal.discountPercentage && itemSplitted.discountValue == itemOriginal.discountValue 
						&& itemSplitted.itemType == itemOriginal.itemType){
					itemOriginal.itemAmount -= itemSplitted.itemAmount;
					break;
				}
			}
		}
		
		// Remove items with zero amount
		int i = 0;
		while (i < originalSplitedInvoice.items.size()){
			if (originalSplitedInvoice.items.get(i).itemAmount == 0f && "".equals(originalSplitedInvoice.items.get(i).itemNote)){
				if(i + 1 < originalSplitedInvoice.items.size()){
					if(!"".equals(originalSplitedInvoice.items.get(i + 1).itemNote)){
						originalSplitedInvoice.items.remove(i + 1);
					}
				}
				
				originalSplitedInvoice.items.remove(i);
			} else {
				++i;
			}
		}
		
		// Calculate total price
		float totalPrice = 0f;
		for(InvoiceItem invoiceItem : originalSplitedInvoice.items){
			float totalItemPrice = invoiceItem.itemAmount * invoiceItem.itemPrice;
			if(invoiceItem.discountPercentage != 0f){
				totalItemPrice = totalItemPrice * (100f - invoiceItem.discountPercentage) / 100f;
			} else if(invoiceItem.discountValue != 0f){
				totalItemPrice = totalItemPrice - invoiceItem.discountValue * invoiceItem.itemAmount;
			}
			totalPrice += totalItemPrice;
		}
		originalSplitedInvoice.totalPrice = totalPrice;
		
		return originalSplitedInvoice;
	}
	
	private void CopyTable(int fromId, int toId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "UPDATE TABLES "
				+ "SET STAFF_ID = (SELECT T2.STAFF_ID FROM TABLES T2 WHERE T2.ID = ?), "
				+ "STAFF_NAME = (SELECT T2.STAFF_NAME FROM TABLES T2 WHERE T2.ID = ?), "
				+ "PRICE = (SELECT T2.PRICE FROM TABLES T2 WHERE T2.ID = ?), "
				+ "CR_NUM = (SELECT T2.CR_NUM FROM TABLES T2 WHERE T2.ID = ?), "
				+ "INVOICE_DATA = (SELECT T2.INVOICE_DATA FROM TABLES T2 WHERE T2.ID = ?) "
				+ "WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, fromId);
		databaseQuery.AddParam(2, fromId);
		databaseQuery.AddParam(3, fromId);
		databaseQuery.AddParam(4, fromId);
		databaseQuery.AddParam(5, fromId);
		databaseQuery.AddParam(6, toId);
		databaseQuery.executeLocally = true;
		
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
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void ClearTable(int currentTableId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "UPDATE TABLES SET STAFF_ID = -1, CR_NUM = -1, PRICE = 0, STAFF_NAME = '', INVOICE_DATA = NULL WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, currentTableId);
		databaseQuery.executeLocally = true;
		
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
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void MergeTables(int originalId, int otherId){
		Invoice originalInvoice = LoadTable(originalId);
		Invoice otherInvoice = LoadTable(otherId);
		if(otherInvoice == null)
			return;
		
		if(originalInvoice == null){
			CopyTable(otherId, originalId);
			ClearTable(otherId);
			return;
		}
		
		// Add items
		for (int i = 0; i < otherInvoice.items.size(); ++i){
			originalInvoice.items.add(otherInvoice.items.get(i));
		}
		
		// Calculate total price
		float totalPrice = 0f;
		for(InvoiceItem invoiceItem : originalInvoice.items){
			float totalItemPrice = invoiceItem.itemAmount * invoiceItem.itemPrice;
			if(invoiceItem.discountPercentage != 0f){
				totalItemPrice = totalItemPrice * (100f - invoiceItem.discountPercentage) / 100f;
			} else if(invoiceItem.discountValue != 0f){
				totalItemPrice = totalItemPrice - invoiceItem.discountValue * invoiceItem.itemAmount;
			}
			totalPrice += totalItemPrice;
		}
		originalInvoice.totalPrice = totalPrice;
		
		boolean success = SaveTable(originalInvoice, originalId);

		if(success){
			ClearTable(otherId);
		}
	}
	
	private boolean SaveTable(Invoice invoice, int tableId){
		byte[] invoiceBytes = null;
		try {
			invoiceBytes = Utils.SerializeObject(invoice);
		} catch (Exception ex){
			ClientAppLogger.GetInstance().LogError(ex);
			return false;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);

		String query = "UPDATE TABLES SET PRICE = ?, INVOICE_DATA = ?, STAFF_ID = ?, CR_NUM = ?, STAFF_NAME = (SELECT FIRST_NAME FROM STAFF WHERE STAFF.ID = ?) "
				+ "WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
		databaseQuery.AddParam(2, invoiceBytes);
		databaseQuery.AddParam(3, invoice.staffId);
		databaseQuery.AddParam(4, invoice.cashRegisterNumber);
		databaseQuery.AddParam(5, invoice.staffId);
		databaseQuery.AddParam(6, tableId);
		databaseQuery.executeLocally = true;
		
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
					return true;
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return false;
	}
	
	private Invoice LoadTable(int tableId){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLoad = "SELECT INVOICE_DATA FROM TABLES WHERE ID = ?";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
		multiDatabaseQuery.SetQuery(0, queryLoad);
		multiDatabaseQuery.AddParam(0, 1, tableId);
		multiDatabaseQuery.executeLocally = true;
		
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
						byte[] invoiceBytes = databaseQueryResult[0].getBytes(0);
						Invoice newInvoice = (Invoice) Utils.DeserializeObject(invoiceBytes);
						return newInvoice;
					}
				}
			} catch (IOException | ClassNotFoundException ex) {
				ClientAppLogger.GetInstance().LogError(ex);
				return null;
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return null;
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButton95;
    private javax.swing.JButton jButton96;
    private javax.swing.JButton jButton97;
    private javax.swing.JButton jButton98;
    private javax.swing.JButton jButton99;
    private javax.swing.JButton jButtonDeleteAll;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelStaffName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JRadioButton jRadioButtonMergeTables;
    private javax.swing.JRadioButton jRadioButtonMoveTable;
    private javax.swing.JRadioButton jRadioButtonSelectTable;
    private javax.swing.JRadioButton jRadioButtonSplitTables;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldTableNumber;
    // End of variables declaration//GEN-END:variables
}
