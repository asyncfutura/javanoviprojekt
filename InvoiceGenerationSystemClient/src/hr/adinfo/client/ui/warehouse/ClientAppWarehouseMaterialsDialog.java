/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.warehouse;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
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
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseMaterialsDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableIdList = new ArrayList<>();

	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppWarehouseMaterialsDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						jButtonAddActionPerformed(null);
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						jButtonEditActionPerformed(null);
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDeleteActionPerformed(null);
					}else if(ke.getKeyCode() == KeyEvent.VK_F6){
						//jButtonRightsActionPerformed(null);
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPos.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEdit.doClick();
				}
			}
		});
		
		jTable1.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RefreshTable();
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		ClientAppUtils.CreateAllMaterialAmountsIfNoExist(Licence.GetOfficeNumber());
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String localInvoiceAmountsSubquery = "SELECT COALESCE(SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM), 0) "
				+ "FROM LOCAL_INVOICE_MATERIALS INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
				+ "WHERE LOCAL_INVOICE_MATERIALS.MAT_ID = MATERIALS.ID AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0 "
				+ "AND (LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP + " OR LOCAL_INVOICES.PAY_NAME <> '" + Values.PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME + "') "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
				+ "AND YEAR(LOCAL_INVOICES.I_DATE) = " + ClientAppSettings.currentYear;
		
		String query = "SELECT MATERIALS.ID, MATERIALS.NAME, MEASURING_UNITS.NAME, "
				+ "MATERIAL_AMOUNTS.AMOUNT - (" + localInvoiceAmountsSubquery + "), MATERIALS.LAST_PRICE "
				+ "FROM ((MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID) "
				+ "WHERE MATERIALS.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND MATERIAL_AMOUNTS.AMOUNT_YEAR = ?";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			String invoiceAmountsSubquery = localInvoiceAmountsSubquery.replace(" AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0", "").replace("LOCAL_INVOICE_MATERIALS", "INVOICE_MATERIALS").replace("LOCAL_INVOICES", "INVOICES");
			String invoiceTestAmountsSubquery = localInvoiceAmountsSubquery.replace(" AND LOCAL_INVOICE_MATERIALS.IS_DELETED = 0", "").replace("LOCAL_INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("LOCAL_INVOICES", "INVOICES_TEST");
			String localInvoiceTestAmountsSubquery = localInvoiceAmountsSubquery.replace("LOCAL_INVOICE_MATERIALS", "LOCAL_INVOICE_MATERIALS_TEST").replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			query = "SELECT MATERIALS.ID, MATERIALS.NAME, MEASURING_UNITS.NAME, "
				+ "MATERIAL_AMOUNTS.AMOUNT + (" + invoiceAmountsSubquery + ") - (" + invoiceTestAmountsSubquery + ") - (" + localInvoiceTestAmountsSubquery + "), "
				+ "MATERIALS.LAST_PRICE "
				+ "FROM ((MATERIALS INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID) "
				+ "WHERE MATERIALS.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND MATERIAL_AMOUNTS.AMOUNT_YEAR = ?";
		}
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
		databaseQuery.AddParam(2, ClientAppSettings.currentYear);
		
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
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Mjerna jedinica", "PNC", "Na skladištu"});
					ArrayList<Integer> idList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(4));
						rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3));
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
					}
					jTable1.setModel(customTableModel);
					tableIdList = idList;
					
					jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 40 / 100);
					jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
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

        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonPrintOnDate = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Materijali");
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

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Materijali");

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEdit.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonPrintOnDate.setText("<html> <div style=\"text-align: center\"> Ispis stanja <br> kroz period <br> [F6] </div> </html>");
        jButtonPrintOnDate.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintOnDate.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonPrintOnDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintOnDateActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintOnDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonPrintOnDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Filter");

        jTextField1.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(597, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

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
                        .addGap(173, 173, 173)
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addGap(8, 8, 8)
                        .addComponent(jLabel9)))
                .addGap(18, 18, 18)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		ClientAppWarehouseMaterialsAddEditDialog addEditdialog = new ClientAppWarehouseMaterialsAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if(jTable1.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
        int tableId = tableIdList.get(rowId);

        ClientAppWarehouseMaterialsAddEditDialog addEditdialog = new ClientAppWarehouseMaterialsAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if(jTable1.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
        int tableId = tableIdList.get(rowId);
        String tableValue = String.valueOf(jTable1.getModel().getValueAt(rowId, 1));
		
		{
			String queryArticles = "SELECT ARTICLES.ID, ARTICLES.NAME "
					+ "FROM ARTICLES "
					+ "INNER JOIN NORMATIVES ON NORMATIVES.ARTICLE_ID = ARTICLES.ID "
					+ "WHERE NORMATIVES.IS_DELETED = 0 AND ARTICLES.IS_DELETED = 0 AND NORMATIVES.MATERIAL_ID = ?";
		
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
			multiDatabaseQuery.SetQuery(0, queryArticles);
			multiDatabaseQuery.AddParam(0, 1, tableId);
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
						String articlesList = "";
						while (databaseQueryResult[0].next()) {
							articlesList += (databaseQueryResult[0].getInt(0) + " - " + databaseQueryResult[0].getString(1) + System.lineSeparator());
						}
						
						boolean isUsed = false;
						if (!"".equals(articlesList)){
							ClientAppLogger.GetInstance().ShowMessage("Materijal " + tableValue + " se koristi u normativima sljedećih artikala. Potrebno je promijeniti normative tih artikala kako bi se materijal mogao obrisati." + System.lineSeparator() + articlesList);
							isUsed = true;
						}
						
						if(isUsed){
							return;
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati materijal " + tableValue, "Obriši materijal", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            final JDialog loadingDialog = new LoadingDialog(null, true);
            

            String query = "UPDATE MATERIALS SET IS_DELETED = ? WHERE ID = ?";
            DatabaseQuery databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, 1);
            databaseQuery.AddParam(2, tableId);

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
                        RefreshTable();
                    }
                } catch (Exception ex) {
                    ClientAppLogger.GetInstance().ShowErrorLog(ex);
                }
            }
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTable1.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
        PrintUtils.PrintA4Table("Materijali", "Materijali", jTable1, "");
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		PrintUtils.PrintPosTable("Materijali", jTable1, new int[]{0, 1, 3, 4});
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintOnDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintOnDateActionPerformed
		ClientAppSelectFromToDateDialog dialog = new ClientAppSelectFromToDateDialog(null, true);
		dialog.setVisible(true);
		if(dialog.selectedDateFrom == null || dialog.selectedDateTo == null){
			return;
		}
		if(dialog.selectedDateFrom.after(dialog.selectedDateTo)){
			return;
		}
		
		JTable tempJTable = new JTable();
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(6);
		
		String queryMaterials = "SELECT MATERIALS.ID, MATERIALS.NAME, MEASURING_UNITS.NAME, MATERIALS.LAST_PRICE "
				+ "FROM MATERIALS "
				+ "INNER JOIN MEASURING_UNITS ON MEASURING_UNITS.ID = MATERIALS.MEASURING_UNIT_ID "
				+ "WHERE MATERIALS.IS_DELETED = 0 "
				+ "ORDER BY MATERIALS.ID";
		multiDatabaseQuery.SetQuery(0, queryMaterials);
		
		String queryReceipts = "SELECT RECEIPT_MATERIALS.MATERIAL_ID, SUM(RECEIPT_MATERIALS.AMOUNT) "
				+ "FROM RECEIPTS "
				+ "INNER JOIN RECEIPT_MATERIALS ON RECEIPTS.ID = RECEIPT_MATERIALS.RECEIPT_ID "
				+ "WHERE RECEIPTS.RECEIPT_DATE >= ? AND RECEIPTS.RECEIPT_DATE <= ? "
				+ "AND RECEIPTS.OFFICE_NUMBER = ? "
				+ "AND RECEIPTS.IS_DELETED = 0 AND RECEIPT_MATERIALS.IS_DELETED = 0 "
				+ "GROUP BY RECEIPT_MATERIALS.MATERIAL_ID";
		multiDatabaseQuery.SetQuery(1, queryReceipts);
		multiDatabaseQuery.AddParam(1, 1, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(1, 3, Licence.GetOfficeNumber());
		
		String queryInvoices = "SELECT INVOICE_MATERIALS.MAT_ID, SUM(INVOICE_MATERIALS.AMT * INVOICE_MATERIALS.NORM) "
				+ "FROM INVOICE_MATERIALS "
				+ "INNER JOIN INVOICES ON INVOICE_MATERIALS.IN_ID = INVOICES.ID "
				+ "WHERE INVOICES.I_DATE >= ? AND INVOICES.I_DATE <= ? "
				+ "AND INVOICES.O_NUM = ? AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
				+ "AND INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
				+ "GROUP BY INVOICE_MATERIALS.MAT_ID";
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
		}
		multiDatabaseQuery.SetQuery(2, queryInvoices);
		multiDatabaseQuery.AddParam(2, 1, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(2, 2, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(2, 3, Licence.GetOfficeNumber());
		
		String queryLocalInvoices = "SELECT LOCAL_INVOICE_MATERIALS.MAT_ID, SUM(LOCAL_INVOICE_MATERIALS.AMT * LOCAL_INVOICE_MATERIALS.NORM) "
				+ "FROM LOCAL_INVOICE_MATERIALS "
				+ "INNER JOIN LOCAL_INVOICES ON LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID "
				+ "WHERE LOCAL_INVOICES.I_DATE >= ? AND LOCAL_INVOICES.I_DATE <= ? "
				+ "AND LOCAL_INVOICES.O_NUM = ? AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_OFFER + " "
				+ "AND LOCAL_INVOICES.PAY_TYPE <> " + Values.PAYMENT_METHOD_TYPE_SUBTOTAL + " "
				+ "AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "GROUP BY LOCAL_INVOICE_MATERIALS.MAT_ID";
		if(!isProduction){
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
		}
		multiDatabaseQuery.SetQuery(3, queryLocalInvoices);
		multiDatabaseQuery.AddParam(3, 1, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(3, 2, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(3, 3, Licence.GetOfficeNumber());
		
		String queryTransfersMaterials = "SELECT TRANSFER_MATERIALS.MATERIAL_ID, TRANSFER_MATERIALS.AMOUNT_START, "
				+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID "
				+ "FROM TRANSFERS "
				+ "INNER JOIN TRANSFER_MATERIALS ON TRANSFERS.ID = TRANSFER_MATERIALS.TRANSFER_ID "
				+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
				+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
				+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_MATERIALS.IS_DELETED = 0 ";
		multiDatabaseQuery.SetQuery(4, queryTransfersMaterials);
		multiDatabaseQuery.AddParam(4, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(4, 2, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(4, 3, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(4, 4, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(4, 5, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(4, 6, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		
		String queryTransfersArticles = "SELECT TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID, TRANSFER_ARTICLES.AMOUNT_START * TRANSFER_ARTICLE_MATERIALS.NORMATIVE, "
				+ "TRANSFERS.STARTING_OFFICE_ID, TRANSFERS.DESTINATION_OFFICE_ID "
				+ "FROM TRANSFERS "
				+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
				+ "INNER JOIN TRANSFER_ARTICLE_MATERIALS ON TRANSFER_ARTICLES.ID = TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID "
				+ "WHERE ((TRANSFERS.STARTING_OFFICE_ID = ? AND TRANSFERS.TRANSFER_START_DATE >= ? AND TRANSFERS.TRANSFER_START_DATE <= ?) "
				+ "OR (TRANSFERS.DESTINATION_OFFICE_ID = ? AND TRANSFERS.IS_DELIVERED = 1 "
					+ "AND TRANSFERS.TRANSFER_RECIEVED_DATE >= ? AND TRANSFERS.TRANSFER_RECIEVED_DATE <= ?)) "
				+ "AND TRANSFERS.IS_DELETED = 0 AND TRANSFER_ARTICLES.IS_DELETED = 0 AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
				+ "AND TRANSFER_ARTICLE_MATERIALS.IS_STARTING = (CASE "
					+ "WHEN TRANSFERS.STARTING_OFFICE_ID = ? THEN 1 "
					+ "ELSE 0 END)";
		multiDatabaseQuery.SetQuery(5, queryTransfersArticles);
		multiDatabaseQuery.AddParam(5, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(5, 2, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(5, 3, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(5, 4, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(5, 5, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateFrom));
		multiDatabaseQuery.AddParam(5, 6, new SimpleDateFormat("yyyy-MM-dd").format(dialog.selectedDateTo));
		multiDatabaseQuery.AddParam(5, 7, Licence.GetOfficeNumber());
		
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
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Mj. jed.", "Stanje", "Posljednja nabavna cijena", "Vrijednost skladišta po posljednjoj nabavnoj cijeni"});
					
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<String> nameList = new ArrayList<>();
					ArrayList<String> measuringList = new ArrayList<>();
					ArrayList<Float> amountList = new ArrayList<>();
					ArrayList<Float> lastPriceList = new ArrayList<>();
					
					int index = 0;
					while (databaseQueryResult[index].next()) {
						idList.add(databaseQueryResult[index].getInt(0));
						nameList.add(databaseQueryResult[index].getString(1));
						measuringList.add(databaseQueryResult[index].getString(2));
						amountList.add(0f);
						lastPriceList.add(databaseQueryResult[index].getFloat(3));
					}
					
					index = 1;
					while (databaseQueryResult[index].next()) {
						int listIndex = ClientAppUtils.ArrayIndexOf(idList, databaseQueryResult[index].getInt(0));
						amountList.set(listIndex, amountList.get(listIndex) + databaseQueryResult[index].getFloat(1));
					}
					
					index = 2;
					while (databaseQueryResult[index].next()) {
						int listIndex = ClientAppUtils.ArrayIndexOf(idList, databaseQueryResult[index].getInt(0));
						amountList.set(listIndex, amountList.get(listIndex) - databaseQueryResult[index].getFloat(1));
					}
					
					index = 3;
					while (databaseQueryResult[index].next()) {
						int listIndex = ClientAppUtils.ArrayIndexOf(idList, databaseQueryResult[index].getInt(0));
						amountList.set(listIndex, amountList.get(listIndex) - databaseQueryResult[index].getFloat(1));
					}
					
					index = 4;
					while (databaseQueryResult[index].next()) {
						int listIndex = ClientAppUtils.ArrayIndexOf(idList, databaseQueryResult[index].getInt(0));
						if(databaseQueryResult[index].getInt(2) == Licence.GetOfficeNumber()){
							amountList.set(listIndex, amountList.get(listIndex) - databaseQueryResult[index].getFloat(1));
						} else {
							amountList.set(listIndex, amountList.get(listIndex) + databaseQueryResult[index].getFloat(1));
						}
					}
					
					index = 5;
					while (databaseQueryResult[index].next()) {
						int listIndex = ClientAppUtils.ArrayIndexOf(idList, databaseQueryResult[index].getInt(0));
						if(databaseQueryResult[index].getInt(2) == Licence.GetOfficeNumber()){
							amountList.set(listIndex, amountList.get(listIndex) - databaseQueryResult[index].getFloat(1));
						} else {
							amountList.set(listIndex, amountList.get(listIndex) + databaseQueryResult[index].getFloat(1));
						}
					}
					
					for(int i = 0; i < idList.size(); ++i){
						Object[] rowData = new Object[6];
						rowData[0] = idList.get(i);
						rowData[1] = nameList.get(i);
						rowData[2] = measuringList.get(i);
						rowData[3] = amountList.get(i);
						rowData[4] = lastPriceList.get(i);
						rowData[5] = amountList.get(i) * lastPriceList.get(i);
						customTableModel.addRow(rowData);
					}
					
					tempJTable.setModel(customTableModel);
					tempJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
					tempJTable.getColumnModel().getColumn(1).setPreferredWidth(35);
					tempJTable.getColumnModel().getColumn(2).setPreferredWidth(15);
					tempJTable.getColumnModel().getColumn(3).setPreferredWidth(15);
					tempJTable.getColumnModel().getColumn(4).setPreferredWidth(15);
					tempJTable.getColumnModel().getColumn(5).setPreferredWidth(20);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od datuma: ", new SimpleDateFormat("dd.MM.yyyy.").format(dialog.selectedDateFrom)));
		printTableExtraData.headerList.add(new Pair<>("Do datuma: ", new SimpleDateFormat("dd.MM.yyyy.").format(dialog.selectedDateTo)));
		
		PrintUtils.PrintA4Table("Materijali-StanjeKrozPeriod", "Stanje skladišta kroz period - Materijali", tempJTable, new int[]{0, 1, 2, 3, 4, 5}, new int[]{}, printTableExtraData, "");
		tempJTable = null;
    }//GEN-LAST:event_jButtonPrintOnDateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintOnDate;
    private javax.swing.JButton jButtonPrintPos;
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
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
