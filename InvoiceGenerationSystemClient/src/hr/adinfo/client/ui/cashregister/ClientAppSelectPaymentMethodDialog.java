/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.cashregister;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Color;

/**
 *
 * @author Matej
 */
public class ClientAppSelectPaymentMethodDialog extends javax.swing.JDialog {
	
	private ArrayList<Integer> paymentMethodTypeList = new ArrayList<>();
	private ArrayList<String> paymentMethodNameList = new ArrayList<>();
	
	public int selectedPaymentMethodType = -1;
	public String selectedPaymentMethodName = "";
	public int selectedPaymentMethodType2 = -1;
	public String selectedPaymentMethodName2 = "";
	public float paymentAmount2 = 0f;
        public Invoice selectedInvoice = null;
        public Boolean textPastInvoiceNotEntered = false;

	
	private float totalPriceWithDiscount;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppSelectPaymentMethodDialog(java.awt.Frame parent, boolean modal, int[] paymentMethodTypeArray, boolean allowMultiple, float totalPriceWithDiscount) {
		super(parent, modal);
		initComponents();
		
		this.totalPriceWithDiscount = totalPriceWithDiscount;
		
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

					} /*else if(ke.getKeyCode() == KeyEvent.VK_ENTER){

						jButtonSelect.doClick();

					}*/ else if(ke.getKeyCode() == KeyEvent.VK_INSERT){

						jButtonMultiple.doClick();

					}
				}
				
				return false;
			}
		});
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				/*if (mouseEvent.getClickCount() == 2) {

					jButtonSelect.doClick();

				}*/
			}
		});
		
		jTable1.setRowHeight(3 * Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jButtonMultiple.setEnabled(allowMultiple);
		if(!allowMultiple){
			jButtonMultiple.setText("");
		}
                
		
		RefreshTable(paymentMethodTypeArray);
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(int[] paymentMethodTypeArray){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT NAME, PAYMENT_TYPE "
				+ "FROM PAYMENT_METHODS "
				+ "WHERE IS_DELETED = 0 AND IS_ACTIVE = 1";
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
					customTableModel.setColumnIdentifiers(new String[] {"Naziv", "Fiskalni tip"});
					ArrayList<Integer> typeList = new ArrayList<>();
					ArrayList<String> nameList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						if(paymentMethodTypeArray.length != 0 && !ClientAppUtils.ArrayContains(paymentMethodTypeArray, databaseQueryResult.getInt(1)))
							continue;
						
						Object[] rowData = new Object[2];
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = Values.PAYMENT_METHOD_TYPE_NAMES[databaseQueryResult.getInt(1)];
						customTableModel.addRow(rowData);
						nameList.add(databaseQueryResult.getString(0));
						typeList.add(databaseQueryResult.getInt(1));
					}
					jTable1.setModel(customTableModel);
					paymentMethodNameList = nameList;
					paymentMethodTypeList = typeList;
					
					jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 50 / 100);
					jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 50 / 100);
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

        jPanelButtons = new javax.swing.JPanel();
        jButtonSelect = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonMultiple = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jBrojRacunaTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelDate = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jButtonReturnReceiptCardInvoice = new javax.swing.JButton();
        jButtonProvjeri = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Način plaćanja");
        setResizable(false);

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSelect.setText("<html> <div style=\"text-align: center\"> Odaberi <br> [ENTER] </div> </html>");
        jButtonSelect.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonMultiple.setText("<html> <div style=\"text-align: center\"> Više načina <br> plaćanja <br> [INS] </div> </html>");
        jButtonMultiple.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonMultiple.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonMultiple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMultipleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap(294, Short.MAX_VALUE)
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonMultiple, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButtonMultiple, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jLabel2.setText("Broj računa za storno:");

        jBrojRacunaTextField.setPreferredSize(new java.awt.Dimension(200, 25));
        jBrojRacunaTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jBrojRacunaTextFieldKeyReleased(evt);
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
                .addGap(54, 54, 54)
                .addComponent(jLabel2)
                .addGap(32, 32, 32)
                .addComponent(jBrojRacunaTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBrojRacunaTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setText("Datum računa:");

        jLabel4.setText("Ukupan iznos:");

        jLabelDate.setText("datum");

        jLabelTotal.setText("iznos");

        jButtonReturnReceiptCardInvoice.setText("Promjena načina plaćanja za odabrani račun");
        jButtonReturnReceiptCardInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReturnReceiptCardInvoiceActionPerformed(evt);
            }
        });

        jButtonProvjeri.setText("Provjeri");
        jButtonProvjeri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProvjeriActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTotal))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelDate)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonProvjeri)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonReturnReceiptCardInvoice)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelTotal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonReturnReceiptCardInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonProvjeri, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
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

        if (jBrojRacunaTextField.getText().length() == 0 || jBrojRacunaTextField == null){
            textPastInvoiceNotEntered = true;
        }
        
        int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
        selectedPaymentMethodType = paymentMethodTypeList.get(rowId);
        selectedPaymentMethodName = paymentMethodNameList.get(rowId);
        Utils.DisposeDialog(this);
        
        
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		selectedPaymentMethodType = -1;
		selectedPaymentMethodName = "";
		selectedPaymentMethodType2 = -1;
		selectedPaymentMethodName2 = "";
		paymentAmount2 = 0f;
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTable1.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonMultipleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMultipleActionPerformed
		ClientAppSelectMultiplePaymentMethodDialog dialog = new ClientAppSelectMultiplePaymentMethodDialog(null, true, totalPriceWithDiscount);
		dialog.setVisible(true);
		if(dialog.selectedPaymentMethodType != -1 && dialog.selectedPaymentMethodType2 != -1 && dialog.paymentAmount2 != 0 && dialog.paymentAmount2 != totalPriceWithDiscount && !(dialog.selectedPaymentMethodName.equals(dialog.selectedPaymentMethodName2) && dialog.selectedPaymentMethodType == dialog.selectedPaymentMethodType2)){
			selectedPaymentMethodName = dialog.selectedPaymentMethodName;
			selectedPaymentMethodType = dialog.selectedPaymentMethodType;
			selectedPaymentMethodName2 = dialog.selectedPaymentMethodName2;
			selectedPaymentMethodType2 = dialog.selectedPaymentMethodType2;
			paymentAmount2 = dialog.paymentAmount2;
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonMultipleActionPerformed

    private void jBrojRacunaTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jBrojRacunaTextFieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jBrojRacunaTextFieldKeyReleased

    private void jButtonReturnReceiptCardInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReturnReceiptCardInvoiceActionPerformed
                String[] invoiceNumberString = jBrojRacunaTextField.getText().split("/");
		
		int invoiceNumber = 0;
		String officeTag = "";
		int cashRegisterNumber = 0;
		try {
			invoiceNumber = Integer.parseInt(invoiceNumberString[0]);
			officeTag = invoiceNumberString[1];
			if(invoiceNumberString.length == 3){
				cashRegisterNumber = Integer.parseInt(invoiceNumberString[2]);
			}
                        else if (invoiceNumberString.length == 2){
                            	cashRegisterNumber = Integer.parseInt(invoiceNumberString[1]);
                        }
                        else if (invoiceNumberString.length == 1){
                            	cashRegisterNumber = Integer.parseInt(invoiceNumberString[0]);
                        }
		} catch (NumberFormatException ex){
			ClientAppLogger.GetInstance().ShowMessage("Broj računa nije ispravnog oblika.");
			return;
		}
		
		if(!officeTag.equals(Licence.GetOfficeTag())){
			ClientAppLogger.GetInstance().ShowMessage("Račun nije moguće stornirati jer nije izdan u ovoj poslovnici.");
			return;
		}
                
                if (jBrojRacunaTextField.getText().length() == 0 || jBrojRacunaTextField == null){
                    textPastInvoiceNotEntered = true;
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
			selectedInvoice = ClientAppUtils.GetInvoice(invoiceNumber, officeTag, cashRegisterNumber, specialNumber, paymentMethod, false);
		}
		
		if(selectedInvoice == null){
			ClientAppLogger.GetInstance().ShowMessage("Ne postoji račun sa unesenim brojem.");
			return;
		}
                
                jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(selectedInvoice.date));
		jLabelTotal.setText(ClientAppUtils.FloatToPriceString(selectedInvoice.totalPrice * (100f - selectedInvoice.discountPercentage) / 100f - selectedInvoice.discountValue));
                
                jButtonSelectActionPerformed(evt);
    }//GEN-LAST:event_jButtonReturnReceiptCardInvoiceActionPerformed

    private void jButtonProvjeriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProvjeriActionPerformed
                if (jBrojRacunaTextField.getText().length() > 0){
                    String[] invoiceNumberString = jBrojRacunaTextField.getText().split("/");

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
                    
                    //TODO: PROMJENA NAČINA PLAĆANJA

                    if(selectedInvoice == null){
                            ClientAppLogger.GetInstance().ShowMessage("Ne postoji račun sa unesenim brojem.");
                            return;
                    }

                    jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(selectedInvoice.date));
                    jLabelTotal.setText(ClientAppUtils.FloatToPriceString(selectedInvoice.totalPrice * (100f - selectedInvoice.discountPercentage) / 100f - selectedInvoice.discountValue));
                }
                else {
                    	ClientAppLogger.GetInstance().ShowMessage("Molimo unesite broj računa.");
			return;
                }
                
                
            if (!jLabelTotal.getText().isEmpty()) {
                jButtonSelect.setEnabled(false);
                jButtonReturnReceiptCardInvoice.setBackground(Color.green);
            }
               
    }//GEN-LAST:event_jButtonProvjeriActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jBrojRacunaTextField;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonMultiple;
    private javax.swing.JButton jButtonProvjeri;
    private javax.swing.JButton jButtonReturnReceiptCardInvoice;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
