/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.cashregister;

import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.extensions.CustomTableModel;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Matej
 */
public class ClientAppCashRegisterTablesSplitDialog extends javax.swing.JDialog {
	private Invoice originalInvoice;
	private Invoice splittedInvoice;
	private boolean setupDone;
	
	public boolean splitSuccess;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppCashRegisterTablesSplitDialog(java.awt.Frame parent, boolean modal, Invoice splittedInvoice) {
		super(parent, modal);
		initComponents();
		
		this.splittedInvoice = splittedInvoice;
		originalInvoice = new Invoice(splittedInvoice);
		for (int i = 0; i < splittedInvoice.items.size(); ++i){
			splittedInvoice.items.get(i).itemAmount = 0f;
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
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonSplitTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						jButtonEdit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_ADD){
						jButtonPlus.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_SUBTRACT){
						jButtonMinus.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTableOriginalInvoice.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				int rowId = GetSelectedItemIndex();
				if (mouseEvent.getClickCount() == 2 && rowId != -1) {
					OnTableDoubleClick(rowId);
				}
			}
		});
		
		jTableSplittedInvoice.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableSplittedInvoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableSplittedInvoice.getTableHeader().setReorderingAllowed(false);
		jTableSplittedInvoice.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableOriginalInvoice.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableOriginalInvoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableOriginalInvoice.getTableHeader().setReorderingAllowed(false);
		jTableOriginalInvoice.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
		jTableSplittedInvoice.setModel(customTableModel);
		jTableSplittedInvoice.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		jTableSplittedInvoice.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 25 / 100);
		jTableSplittedInvoice.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		jTableSplittedInvoice.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		jTableSplittedInvoice.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		jTableSplittedInvoice.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		
		ClientAppUtils.SetupFocusTraversal(this);
		
		setupDone = true;
		RefreshTables();
	}
	
	private void RefreshTables(){
		// Original invoice
		{
			CustomTableModel customTableModel = new CustomTableModel();
			customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Cijena", "Popust", "Stara količina", "Nova količina"});
			for (int i = 0; i < originalInvoice.items.size(); ++i){
				Object[] rowData = new Object[6];
				if("".equals(originalInvoice.items.get(i).itemNote)){
					rowData[0] = originalInvoice.items.get(i).itemId;
					rowData[1] = originalInvoice.items.get(i).itemName;
					rowData[2] = originalInvoice.items.get(i).itemPrice;
					String discount = "";
					if(originalInvoice.items.get(i).discountPercentage != 0f){
						discount = originalInvoice.items.get(i).discountPercentage + " %";
					} else if(originalInvoice.items.get(i).discountValue != 0f){
						discount = ClientAppUtils.FloatToPriceFloat(originalInvoice.items.get(i).discountValue) + " kn/kom";
					}
					rowData[3] = discount;
					rowData[4] = originalInvoice.items.get(i).itemAmount;
					rowData[5] = splittedInvoice.items.get(i).itemAmount;
				} else {
					rowData[0] = "";
					rowData[1] = " - " + originalInvoice.items.get(i).itemNote;
					rowData[2] = "";
					rowData[3] = "";
					rowData[4] = "";
					rowData[5] = "";
				}

				customTableModel.addRow(rowData);
			}

			jTableOriginalInvoice.setModel(customTableModel);
			jTableOriginalInvoice.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 15 / 100);
			jTableOriginalInvoice.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 25 / 100);
			jTableOriginalInvoice.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 15 / 100);
			jTableOriginalInvoice.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 15 / 100);
			jTableOriginalInvoice.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 15 / 100);
			jTableOriginalInvoice.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneOriginalInvoice.getWidth() * 15 / 100);
		}
		
		// Splitted invoice
		{
			CustomTableModel customTableModel = new CustomTableModel();
			customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Cijena", "Popust", "Ukupno"});
			for (int i = 0; i < splittedInvoice.items.size(); ++i){
				if(splittedInvoice.items.get(i).itemAmount == 0f && "".equals(splittedInvoice.items.get(i).itemNote))
					continue;
				
				if(i > 0 && !"".equals(splittedInvoice.items.get(i).itemNote) && splittedInvoice.items.get(i - 1).itemAmount == 0f)
					continue;
				
				Object[] rowData = new Object[6];
				if("".equals(splittedInvoice.items.get(i).itemNote)){
					rowData[0] = splittedInvoice.items.get(i).itemId;
					rowData[1] = splittedInvoice.items.get(i).itemName;
					rowData[2] = splittedInvoice.items.get(i).itemAmount;
					rowData[3] = splittedInvoice.items.get(i).itemPrice;
					String discount = "";
					float totalPrice = splittedInvoice.items.get(i).itemPrice * splittedInvoice.items.get(i).itemAmount;
					if(splittedInvoice.items.get(i).discountPercentage != 0f){
						discount = splittedInvoice.items.get(i).discountPercentage + " %";
						totalPrice = totalPrice * (100f - splittedInvoice.items.get(i).discountPercentage) / 100f;
					} else if(splittedInvoice.items.get(i).discountValue != 0f){
						discount = ClientAppUtils.FloatToPriceFloat(splittedInvoice.items.get(i).discountValue) + " kn/kom";
						totalPrice = totalPrice - splittedInvoice.items.get(i).discountValue * splittedInvoice.items.get(i).itemAmount;
					}
					rowData[4] = discount;
					rowData[5] = ClientAppUtils.FloatToPriceString(totalPrice);
				} else {
					rowData[0] = "";
					rowData[1] = " - " + splittedInvoice.items.get(i).itemNote;
					rowData[2] = "";
					rowData[3] = "";
					rowData[4] = "";
					rowData[5] = "";
				}

				customTableModel.addRow(rowData);
			}

			jTableSplittedInvoice.setModel(customTableModel);
			jTableSplittedInvoice.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
			jTableSplittedInvoice.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 25 / 100);
			jTableSplittedInvoice.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
			jTableSplittedInvoice.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
			jTableSplittedInvoice.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
			jTableSplittedInvoice.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneSplittedInvoice.getWidth() * 15 / 100);
		}
		
		// Update total price
		float totalPrice = 0f;
		for(InvoiceItem invoiceItem : splittedInvoice.items){
			if(invoiceItem.itemAmount == 0f)
				continue;
			
			float totalItemPrice = invoiceItem.itemAmount * invoiceItem.itemPrice;
			if(invoiceItem.discountPercentage != 0f){
				totalItemPrice = totalItemPrice * (100f - invoiceItem.discountPercentage) / 100f;
			} else if(invoiceItem.discountValue != 0f){
				totalItemPrice = totalItemPrice - invoiceItem.discountValue * invoiceItem.itemAmount;
			}
			totalPrice += totalItemPrice;
		}
		
		splittedInvoice.totalPrice = totalPrice;
		
		String discount = "Popust: -";
		if(splittedInvoice.discountPercentage != 0f){
			discount = "Popust: " + splittedInvoice.discountPercentage + "% = " + (totalPrice * splittedInvoice.discountPercentage) / 100f + " kn";
			totalPrice = totalPrice * (100f - splittedInvoice.discountPercentage) / 100f;
		} else if(splittedInvoice.discountValue != 0f){
			discount = "Popust: " + ClientAppUtils.FloatToPriceString(splittedInvoice.discountValue) + " kn";
			totalPrice = totalPrice - splittedInvoice.discountValue;
		}
		
		jLabelDiscount.setText(discount);
		jLabelTotal.setText(ClientAppUtils.FloatToPriceString(totalPrice).replace(".", ","));
	}
	
	private int GetSelectedItemIndex(){
		int rowId = jTableOriginalInvoice.getSelectedRow();
		if(rowId != -1 && !"".equals(originalInvoice.items.get(rowId).itemNote)){
			rowId--;
		}
		
		return rowId;
	}
	
	private void OnTableDoubleClick(int rowId){
		ClientAppEnterAmountDialog enterAmountDialog = new ClientAppEnterAmountDialog(null, true);
        enterAmountDialog.setVisible(true);
		if(enterAmountDialog.changeSuccess){
			splittedInvoice.items.get(rowId).itemAmount = enterAmountDialog.enteredAmount;
			RefreshTables();
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
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneOriginalInvoice = new javax.swing.JScrollPane();
        jTableOriginalInvoice = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneSplittedInvoice = new javax.swing.JScrollPane();
        jTableSplittedInvoice = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabelDiscount = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jButtonSplitTable = new javax.swing.JButton();
        jButtonPlus = new javax.swing.JButton();
        jButtonMinus = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Razdvajanje stola");
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
        jLabel9.setText("Razdvajanje stola");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Početni stol"));

        jTableOriginalInvoice.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneOriginalInvoice.setViewportView(jTableOriginalInvoice);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOriginalInvoice)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOriginalInvoice, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Razdvojeni stol"));

        jTableSplittedInvoice.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneSplittedInvoice.setViewportView(jTableSplittedInvoice);

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Ukupno:");
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabelTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelTotal.setText("0.00");
        jLabelTotal.setToolTipText("");
        jLabelTotal.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("kn ");

        jLabelDiscount.setText("Popust: -");
        jLabelDiscount.setPreferredSize(new java.awt.Dimension(70, 15));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSplittedInvoice)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneSplittedInvoice, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabelDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonSplitTable.setText("<html> <div style=\"text-align: center\"> Razdvoji <br> stol <br> [F5] </div> </html>");
        jButtonSplitTable.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSplitTable.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonSplitTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSplitTableActionPerformed(evt);
            }
        });

        jButtonPlus.setFont(jButtonPlus.getFont().deriveFont(jButtonPlus.getFont().getSize()+25f));
        jButtonPlus.setText("+");
        jButtonPlus.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPlus.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlusActionPerformed(evt);
            }
        });

        jButtonMinus.setFont(jButtonMinus.getFont().deriveFont(jButtonMinus.getFont().getSize()+25f));
        jButtonMinus.setText("-");
        jButtonMinus.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonMinus.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMinusActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Unesi <br> količinu <br> [F10] </div> </html>");
        jButtonEdit.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonEdit.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonPlus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonMinus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSplitTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPlus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonMinus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonSplitTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 522, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel9)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlusActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		splittedInvoice.items.get(rowId).itemAmount += 1f;
		
		RefreshTables();
		
		jTableOriginalInvoice.setRowSelectionInterval(rowId, rowId);
    }//GEN-LAST:event_jButtonPlusActionPerformed

    private void jButtonMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMinusActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		splittedInvoice.items.get(rowId).itemAmount -= 1f;
		
		RefreshTables();
		
		jTableOriginalInvoice.setRowSelectionInterval(rowId, rowId);
    }//GEN-LAST:event_jButtonMinusActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
		int rowId = GetSelectedItemIndex();
		if(rowId == -1){
			return;
        }
		
		OnTableDoubleClick(rowId);
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonSplitTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSplitTableActionPerformed
		boolean haveItems = false;
		for(InvoiceItem invoiceItem : splittedInvoice.items){
			if(invoiceItem.itemAmount != 0f){
				haveItems = true;
				break;
			}
		}
		
		if(!haveItems){
			ClientAppLogger.GetInstance().ShowMessage("Na razdvojenom stolu nema stavaka!" + System.lineSeparator() + "Novi stol mora imati bar jednu stavku");
			return;
		}
		
		// Remove items with zero amount
		int i = 0;
		while (i < splittedInvoice.items.size()){
			if (splittedInvoice.items.get(i).itemAmount == 0f && "".equals(splittedInvoice.items.get(i).itemNote)){
				if(i + 1 < splittedInvoice.items.size()){
					if(!"".equals(splittedInvoice.items.get(i + 1).itemNote)){
						splittedInvoice.items.remove(i + 1);
					}
				}
				
				splittedInvoice.items.remove(i);
			} else {
				++i;
			}
		}
		
		splitSuccess = true;
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSplitTableActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonMinus;
    private javax.swing.JButton jButtonPlus;
    private javax.swing.JButton jButtonSplitTable;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDiscount;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneOriginalInvoice;
    private javax.swing.JScrollPane jScrollPaneSplittedInvoice;
    private javax.swing.JTable jTableOriginalInvoice;
    private javax.swing.JTable jTableSplittedInvoice;
    // End of variables declaration//GEN-END:variables
}
