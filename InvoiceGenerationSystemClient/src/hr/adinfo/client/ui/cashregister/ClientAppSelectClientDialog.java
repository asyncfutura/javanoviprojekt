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
import hr.adinfo.client.ui.clientssuppliers.ClientAppClientsAddEditDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppSelectClientDialog extends javax.swing.JDialog {
	
	private ArrayList<Integer> tableIdList = new ArrayList<>();
	
	public int selectedId = -1;
	public String selectedClientName = "";
	public String selectedClientOIB = "";
        public String selectedAddress = "";
        public String selectedCity = "";
	public int selectedClientPaymentDelay = 0;
        public Invoice selectedInvoice = null;
	public int selectedClientDiscount = 0;
    public String strLoyalty = "";
    private boolean loyaltyCardEnabled = false;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppSelectClientDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						jButtonAdd.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_ENTER){
						jButtonSelect.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonSelect.doClick();
				}
			}
		});
		
		jTable1.setRowHeight(2 * Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
                DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                jTable1.getColumnModel().getColumn(0).setCellRenderer(dtcr);

                ClientAppSettings.LoadSettings();
                        loyaltyCardEnabled = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_TRUSTCARD.ordinal());
                jPanel2.setEnabled(loyaltyCardEnabled);
                jPanel2.setVisible(loyaltyCardEnabled);  
		
		RefreshTable();
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		String[] col = null;
		if(loyaltyCardEnabled) {
			col = new String[] {"Šifra", "Naziv", "OIB", "Odgoda plaćanja", "Popust", "Kartica povjerenja"};
		} else {
			col = new String[] {"Šifra", "Naziv", "OIB", "Odgoda plaćanja", "Popust"}; 
		}
		
		String query = "SELECT CLIENTS.ID, CLIENTS.NAME, CLIENTS.OIB, CLIENTS.PAYMENT_DELAY, CLIENTS.DISCOUNT, CLIENTS.LOYALTY_CARD "
				+ "FROM CLIENTS "
				+ "WHERE CLIENTS.IS_DELETED = 0";
        
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		
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
					customTableModel.setColumnIdentifiers(col); //new String[] {"Šifra", "Naziv", "OIB", "Odgoda plaćanja", "Popust"}
					ArrayList<Integer> idList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[loyaltyCardEnabled ? 6 : 5];
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = databaseQueryResult.getString(3);
						rowData[4] = databaseQueryResult.getString(4);
						if(loyaltyCardEnabled){
							rowData[5] = databaseQueryResult.getString(5);
						}
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
					}
					jTable1.setModel(customTableModel);
					tableIdList = idList;
					
					jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 50 / 100);
					jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 25 / 100);
					jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 25 / 100);
					jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 25 / 100);
					if (loyaltyCardEnabled){
						jTable1.getColumnModel().getColumn(5).setPreferredWidth(jScrollPane1.getWidth() * 30 / 100);
					}            
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		jTable1.getColumnModel().getColumn(0).setCellRenderer(dtcr);
		jTable1.changeSelection(0, 0, false, false);
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelButtons = new javax.swing.JPanel();
        jButtonSelect = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldBrojRacuna = new javax.swing.JTextField();
        jButtonProvjeri = new javax.swing.JButton();
        jButtonPromijeniR1Korisnika = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabelDate = new javax.swing.JLabel();
        jLabelInvoiceAmount = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Odabir klijenta");
        setResizable(false);

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSelect.setText("<html> <div style=\"text-align: center\"> Odaberi <br> [ENTER] </div> </html>");
        jButtonSelect.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj novog <br> klijenta <br> [INS] </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(100, 80));
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Filter");

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
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(53, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Kartica povjerenja");

        jTextField2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jLabel3.setText("Broj računa:");

        jButtonProvjeri.setText("PROVJERI");
        jButtonProvjeri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProvjeriActionPerformed(evt);
            }
        });

        jButtonPromijeniR1Korisnika.setText("Promijeni R1 korisnika");
        jButtonPromijeniR1Korisnika.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPromijeniR1KorisnikaActionPerformed(evt);
            }
        });

        jLabel4.setText("Datum i iznos računa:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelDate)
                                .addGap(60, 60, 60)
                                .addComponent(jLabelInvoiceAmount)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonProvjeri)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldBrojRacuna)
                                    .addComponent(jButtonPromijeniR1Korisnika, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jTextFieldBrojRacuna, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonProvjeri)
                            .addComponent(jButtonPromijeniR1Korisnika, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabelDate)
                            .addComponent(jLabelInvoiceAmount)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
        if(jTable1.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite odabrati.");
            return;
        }
		
		int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
		selectedId = tableIdList.get(rowId);
		selectedClientName = String.valueOf(jTable1.getModel().getValueAt(rowId, 1));
		selectedClientOIB = String.valueOf(jTable1.getModel().getValueAt(rowId, 2));
		if(loyaltyCardEnabled){
			strLoyalty = String.valueOf(jTable1.getModel().getValueAt(rowId, 5));
			if(strLoyalty != null && !strLoyalty.isEmpty()){
				try {
					selectedClientDiscount = Integer.parseInt(String.valueOf(jTable1.getModel().getValueAt(rowId, 4)));
				} catch (NumberFormatException ex) {}
			}
		}
		
		try {
		selectedClientPaymentDelay = Integer.parseInt(String.valueOf(jTable1.getModel().getValueAt(rowId, 3)));
		} catch (NumberFormatException ex) {}

		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
                selectedId = -1;
		selectedClientName = "";
		strLoyalty = "";
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        ClientAppClientsAddEditDialog addEditdialog = new ClientAppClientsAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTable1.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
		String searchString = jTextField2.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTable1.setRowSorter(sorter);
		jTable1.changeSelection(0, 0, false, false);
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		jTable1.getColumnModel().getColumn(0).setCellRenderer(dtcr);
    }//GEN-LAST:event_jTextField2KeyReleased

    private void jButtonProvjeriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProvjeriActionPerformed
         if (jTextFieldBrojRacuna.getText().length() > 0){
                    String[] invoiceNumberString = jTextFieldBrojRacuna.getText().split("/");

                    int invoiceNumber = 0;
                    String officeTag = "";
                    int cashRegisterNumber = 0;
                    try {
                            invoiceNumber = Integer.parseInt(invoiceNumberString[0]);
                            officeTag = invoiceNumberString[1];
                            if(invoiceNumberString.length == 3){
                                    cashRegisterNumber = Integer.parseInt(invoiceNumberString[2]);
                            }
                    } catch (NumberFormatException ex){
                            ClientAppLogger.GetInstance().ShowMessage("Broj računa nije ispravnog oblika.");
                            return;
                    }

                    if(!officeTag.equals(Licence.GetOfficeTag())){
                            ClientAppLogger.GetInstance().ShowMessage("Račun nije moguće stornirati jer nije izdan u ovoj poslovnici.");
                            return;
                    }

                    int paymentMethod = Values.PAYMENT_METHOD_ANY_METHOD;
                    int specialNumber = 0;

                    if(invoiceNumberString.length == 2){
                            paymentMethod = Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP;
                            specialNumber = invoiceNumber;
                            invoiceNumber = 0;
                    }

                    selectedInvoice = ClientAppUtils.GetInvoice(invoiceNumber, officeTag, cashRegisterNumber, specialNumber, paymentMethod, false);
                    if(selectedInvoice == null){
                            selectedInvoice = ClientAppUtils.GetInvoice(invoiceNumber, officeTag, cashRegisterNumber, specialNumber, paymentMethod, true);
                    }

                    if(selectedInvoice == null){
                            ClientAppLogger.GetInstance().ShowMessage("Ne postoji račun sa unesenim brojem.");
                            return;
                    }

                    jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(selectedInvoice.date));
                    jLabelInvoiceAmount.setText(ClientAppUtils.FloatToPriceString(selectedInvoice.totalPrice * (100f - selectedInvoice.discountPercentage) / 100f - selectedInvoice.discountValue));
                }
                else {
                    	ClientAppLogger.GetInstance().ShowMessage("Molimo unesite broj računa.");
			return;
                }
    }//GEN-LAST:event_jButtonProvjeriActionPerformed

    private void jButtonPromijeniR1KorisnikaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPromijeniR1KorisnikaActionPerformed
                    if (jTextFieldBrojRacuna.getText().length() > 0){
                         if(jTable1.getSelectedRow() == -1){
                            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite odabrati.");
                            return;
                        }
		
                        int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
                        selectedId = tableIdList.get(rowId);
                        selectedClientName = String.valueOf(jTable1.getModel().getValueAt(rowId, 1));
                        selectedClientOIB = String.valueOf(jTable1.getModel().getValueAt(rowId, 2));
                        if(loyaltyCardEnabled){
                                strLoyalty = String.valueOf(jTable1.getModel().getValueAt(rowId, 5));
                                if(strLoyalty != null && !strLoyalty.isEmpty()){
                                        try {
                                                selectedClientDiscount = Integer.parseInt(String.valueOf(jTable1.getModel().getValueAt(rowId, 4)));
                                        } catch (NumberFormatException ex) {}
                                }
                        }

                        try {
                        selectedClientPaymentDelay = Integer.parseInt(String.valueOf(jTable1.getModel().getValueAt(rowId, 3)));
                        } catch (NumberFormatException ex) {}

                        Utils.DisposeDialog(this);
                        }
    }//GEN-LAST:event_jButtonPromijeniR1KorisnikaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPromijeniR1Korisnika;
    private javax.swing.JButton jButtonProvjeri;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelInvoiceAmount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextFieldBrojRacuna;
    // End of variables declaration//GEN-END:variables
}
