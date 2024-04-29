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
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Matej
 */
public class ClientAppCashRegisterSaldoDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesAddDialog
	 */
	public ClientAppCashRegisterSaldoDialog(java.awt.Frame parent, boolean modal) {
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
					}
				}
				
				return false;
			}
		});
		
		jTableTotalStaff.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTotalStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTotalStaff.getTableHeader().setReorderingAllowed(false);
		jTableTotalStaff.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		ClientAppUtils.SetupFocusTraversal(this);
		
		SetupDialog();
	}
	
	private void SetupDialog(){
		Date todayDate = new Date();
		
		jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy.").format(todayDate));
		jLabelCashRegister.setText("" + Licence.GetCashRegisterNumber());
		float deposit = ClientAppSettings.GetFloat(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_DEPOSIT.ordinal());
		jLabelDeposit.setText(ClientAppUtils.FloatToPriceString(deposit));
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLocalInvoices = "SELECT LOCAL_INVOICES.ID, LOCAL_INVOICES.FIN_PR, LOCAL_INVOICES.DIS_PCT, LOCAL_INVOICES.DIS_AMT, S_ID, "
				+ "STAFF.FIRST_NAME "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND I_DATE = ? "
				+ "AND LOCAL_INVOICES.CR_NUM = ? AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?, ?)";
		String queryInvoices = queryLocalInvoices.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, queryLocalInvoices);
		multiDatabaseQuery.SetQuery(1, queryInvoices);
		for (int i = 0; i < 2; ++i){
			multiDatabaseQuery.AddParam(i, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(i, 2, new SimpleDateFormat("yyyy-MM-dd").format(todayDate));
			multiDatabaseQuery.AddParam(i, 3, Licence.GetCashRegisterNumber());
			multiDatabaseQuery.AddParam(i, 4, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
			multiDatabaseQuery.AddParam(i, 5, Values.PAYMENT_METHOD_TYPE_OFFER);
			multiDatabaseQuery.AddParam(i, 6, Values.PAYMENT_METHOD_TYPE_SUBTOTAL);
		}
		
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
				if(databaseQueryResults != null){
					ArrayList<Integer> staffIdList = new ArrayList<>();
					ArrayList<String> staffNameList = new ArrayList<>();
					ArrayList<Float> staffAmountSumList = new ArrayList<>();
					float totalSum = 0f;
					
					DatabaseQueryResult databaseQueryResult = databaseQueryResults[0];
					while (databaseQueryResult.next()) {
						float invoicePrice = databaseQueryResult.getFloat(1) * (100f - databaseQueryResult.getFloat(2)) / 100f - databaseQueryResult.getFloat(3);
						totalSum += invoicePrice;
						
						int staffId = databaseQueryResult.getInt(4);
						int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
						if(staffListId == -1){
							staffIdList.add(staffId);
							staffNameList.add(databaseQueryResult.getString(5));
							staffAmountSumList.add(invoicePrice);
						} else {
							staffAmountSumList.set(staffListId, staffAmountSumList.get(staffListId) + invoicePrice);
						}
					}
					
					databaseQueryResult = databaseQueryResults[1];
					while (databaseQueryResult.next()) {
						float invoicePrice = databaseQueryResult.getFloat(1) * (100f - databaseQueryResult.getFloat(2)) / 100f - databaseQueryResult.getFloat(3);
						totalSum += invoicePrice;
						
						int staffId = databaseQueryResult.getInt(4);
						int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
						if(staffListId == -1){
							staffIdList.add(staffId);
							staffNameList.add(databaseQueryResult.getString(5));
							staffAmountSumList.add(invoicePrice);
						} else {
							staffAmountSumList.set(staffListId, staffAmountSumList.get(staffListId) + invoicePrice);
						}
					}
					
					// Labels
					jLabelTotal.setText(ClientAppUtils.FloatToPriceString(totalSum));
					jLabelTotalWithDeposit.setText(ClientAppUtils.FloatToPriceString(deposit + totalSum));
					
					// Table staff
					CustomTableModel customTableModelStaff = new CustomTableModel();
					customTableModelStaff.setColumnIdentifiers(new String[] {"Djelatnik", "Ukupno"});
					for (int i = 0; i < staffIdList.size(); ++i){
						Object[] rowData = new Object[2];
						rowData[0] = staffIdList.get(i) + "-" + staffNameList.get(i);
						rowData[1] = ClientAppUtils.FloatToPriceString(staffAmountSumList.get(i));
						customTableModelStaff.addRow(rowData);
					}
					
					jTableTotalStaff.setModel(customTableModelStaff);				
					jTableTotalStaff.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 60 / 100);
					jTableTotalStaff.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 40 / 100);
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
        jLabelDate = new javax.swing.JLabel();
        jLabelCashRegister = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelDeposit = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelTotalWithDeposit = new javax.swing.JLabel();
        jScrollPaneTotalStaff = new javax.swing.JScrollPane();
        jTableTotalStaff = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj kategoriju");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitle.setText("Saldo");
        jLabelTitle.setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Ukupni promet na dan:");

        jLabelDate.setText("jLabel2");

        jLabelCashRegister.setText("jLabel2");

        jLabel2.setText("Kasa:");

        jLabel3.setText("Ukupno:");

        jLabelTotal.setText("jLabel2");

        jLabel4.setText("Polog kase:");

        jLabelDeposit.setText("jLabel2");

        jLabel5.setText("Ukupno s pologom:");

        jLabelTotalWithDeposit.setText("jLabel2");

        jTableTotalStaff.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPaneTotalStaff.setViewportView(jTableTotalStaff);

        jLabel6.setText("Pregled totala po djelatnicima:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneTotalStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelDate))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelCashRegister))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelTotal))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelDeposit))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelTotalWithDeposit))
                            .addComponent(jLabel6))
                        .addGap(0, 184, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelCashRegister))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelTotal))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelDeposit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelTotalWithDeposit))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTotalStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addContainerGap())
        );

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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabelTitle)
                .addGap(30, 30, 30)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelCashRegister;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelDeposit;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JLabel jLabelTotalWithDeposit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPaneTotalStaff;
    private javax.swing.JTable jTableTotalStaff;
    // End of variables declaration//GEN-END:variables
}
