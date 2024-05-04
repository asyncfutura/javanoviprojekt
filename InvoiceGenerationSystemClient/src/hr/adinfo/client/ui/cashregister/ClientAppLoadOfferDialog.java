/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.cashregister;

import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import javax.swing.JButton;

/**
 *
 * @author Matej
 */
public class ClientAppLoadOfferDialog extends javax.swing.JDialog {
	
	public Invoice selectedInvoice = null;
		
	private final JButton[] buttonSpecialChars;
	private final JButton[] buttonLetters;
	private int paymentMethodType;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppLoadOfferDialog(java.awt.Frame parent, boolean modal, int paymentMethodType) {
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
						jButtonCheck.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonSelect.doClick();
					}
				}
				
				return false;
			}
		});
		
		buttonLetters = new JButton[]{
			jButton21, jButton22, jButton23, jButton24, jButton25, jButton26, jButton27, jButton28, jButton29, jButton30, 
			jButton31, jButton32, jButton33, jButton34, jButton35, jButton36, jButton37, jButton38, jButton39, jButton40, 
			jButton41, jButton42, jButton43, jButton44, jButton45, jButton46, jButton47, jButton48, jButton49, jButton50, 
			jButton51, 
		};
		
		buttonSpecialChars = new JButton[]{
			jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9, jButton10, 
			jButton11, jButton12, jButton13, jButton14, jButton15, jButton16, jButton17, jButton18, jButton19, jButton20, 
			jButton52, jButton53, jButton57, 
		};
		
		for (int i = 0; i < buttonLetters.length; ++i){
			final int index = i;
			buttonLetters[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				jButtonLetterActionPerformed(index);
			});
		}
		
		for (int i = 0; i < buttonSpecialChars.length; ++i){
			final int index = i;
			buttonSpecialChars[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				jButtonSpecialCharActionPerformed(index);
			});
		}
		
		jButtonSpace.addActionListener((java.awt.event.ActionEvent evt) -> {
			jButtonSpaceCharActionPerformed();
		});
		jButtonShift.addActionListener((java.awt.event.ActionEvent evt) -> {
			jButtonShiftCharActionPerformed();
		});
		jButtonDelete.addActionListener((java.awt.event.ActionEvent evt) -> {
			jButtonDeleteCharActionPerformed();
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
		
		jLabelDate.setText("");
		jLabelTotal.setText("");
		
		if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			setTitle("Učitaj predračun");
			jLabelTitle.setText("Učitaj predračun");
			jLabel2.setText("Broj predračuna:");
			jLabel1.setText("Datum predračuna:");
		}
		
		this.paymentMethodType = paymentMethodType;
	}
	
	private void jButtonLetterActionPerformed(int index){
		jTextField1.setText(jTextField1.getText() + buttonLetters[index].getText());
	}
	
	private void jButtonSpecialCharActionPerformed(int index){
		jTextField1.setText(jTextField1.getText() + buttonSpecialChars[index].getText());
	}
	
	private void jButtonShiftCharActionPerformed(){
		if(buttonLetters[0].getText().equals(buttonLetters[0].getText().toLowerCase())){
			for (int i = 0; i < buttonLetters.length; ++i){
				buttonLetters[i].setText(buttonLetters[i].getText().toUpperCase());
			}
		} else {
			for (int i = 0; i < buttonLetters.length; ++i){
				buttonLetters[i].setText(buttonLetters[i].getText().toLowerCase());
			}
		}
	}
	
	private void jButtonSpaceCharActionPerformed(){
		jTextField1.setText(jTextField1.getText() + " ");
	}
	
	private void jButtonDeleteCharActionPerformed(){
		if(jTextField1.getText().length() == 0)
			return;
		
		jTextField1.setText(jTextField1.getText().substring(0, jTextField1.getText().length() - 1));
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
        jButtonExit = new javax.swing.JButton();
        jButtonSelect = new javax.swing.JButton();
        jButtonCheck = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabelTitle = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelDate = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
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
        jButtonShift = new javax.swing.JButton();
        jButtonSpace = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Učitaj ponudu");
        setResizable(false);

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonSelect.setText("<html> <div style=\"text-align: center\"> Odaberi <br> [F8] </div> </html>");
        jButtonSelect.setEnabled(false);
        jButtonSelect.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonCheck.setText("<html> <div style=\"text-align: center\"> Provjeri <br> [ENTER] </div> </html>");
        jButtonCheck.setPreferredSize(new java.awt.Dimension(100, 80));
        jButtonCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jButtonCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Broj ponude:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitle.setText("Učitaj ponudu");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Datum ponude:");

        jLabel3.setText("Ukupan iznos:");

        jLabelDate.setText("datum");

        jLabelTotal.setText("iznos");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelDate))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTotal)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelTotal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton1.setText("!");
        jButton1.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton2.setText("?");
        jButton2.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton3.setText("@");
        jButton3.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton4.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton4.setText(":");
        jButton4.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton5.setText(";");
        jButton5.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton6.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton6.setText("\"");
        jButton6.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton7.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton7.setText("+");
        jButton7.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton8.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton8.setText("-");
        jButton8.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton9.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton9.setText("*");
        jButton9.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton10.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton10.setText("/");
        jButton10.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton11.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton11.setText("1");
        jButton11.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton12.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton12.setText("2");
        jButton12.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton13.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton13.setText("3");
        jButton13.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton14.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton14.setText("4");
        jButton14.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton15.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton15.setText("5");
        jButton15.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton16.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton16.setText("6");
        jButton16.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton17.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton17.setText("7");
        jButton17.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton18.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton18.setText("8");
        jButton18.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton19.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton19.setText("9");
        jButton19.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton20.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton20.setText("0");
        jButton20.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton21.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton21.setText("q");
        jButton21.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton22.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton22.setText("w");
        jButton22.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton23.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton23.setText("e");
        jButton23.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton24.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton24.setText("r");
        jButton24.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton25.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton25.setText("t");
        jButton25.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton26.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton26.setText("z");
        jButton26.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton27.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton27.setText("u");
        jButton27.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton28.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton28.setText("i");
        jButton28.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton29.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton29.setText("o");
        jButton29.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton30.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton30.setText("p");
        jButton30.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton31.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton31.setText("š");
        jButton31.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton32.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton32.setText("đ");
        jButton32.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton33.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton33.setText("a");
        jButton33.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton34.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton34.setText("s");
        jButton34.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton35.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton35.setText("d");
        jButton35.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton36.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton36.setText("f");
        jButton36.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton37.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton37.setText("g");
        jButton37.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton38.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton38.setText("h");
        jButton38.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton39.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton39.setText("j");
        jButton39.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton40.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton40.setText("k");
        jButton40.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton41.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton41.setText("l");
        jButton41.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton42.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton42.setText("č");
        jButton42.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton43.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton43.setText("ć");
        jButton43.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton44.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton44.setText("ž");
        jButton44.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton45.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton45.setText("y");
        jButton45.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton46.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton46.setText("x");
        jButton46.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton47.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton47.setText("c");
        jButton47.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton48.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton48.setText("v");
        jButton48.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton49.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton49.setText("b");
        jButton49.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton50.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton50.setText("n");
        jButton50.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton51.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton51.setText("m");
        jButton51.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton52.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton52.setText(",");
        jButton52.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton53.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton53.setText(".");
        jButton53.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButtonShift.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButtonShift.setText("shift");
        jButtonShift.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButtonSpace.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButtonSpace.setText("space");
        jButtonSpace.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButtonDelete.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButtonDelete.setText("delete");
        jButtonDelete.setMargin(new java.awt.Insets(2, 6, 2, 6));

        jButton57.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton57.setText("%");
        jButton57.setMargin(new java.awt.Insets(2, 6, 2, 6));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonShift, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(201, 201, 201)
                        .addComponent(jButtonSpace, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonShift, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSpace, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		selectedInvoice = null;
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCheckActionPerformed
		String[] invoiceNumberString = jTextField1.getText().split("/");
		if(invoiceNumberString.length != 3){
			if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				ClientAppLogger.GetInstance().ShowMessage("Broj predračuna nije ispravnog oblika.");
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Broj ponude nije ispravnog oblika.");
			}
			return;
		}
		
		int specialNumber = -1;
		String officeTag = "";
		int cashRegisterNumber = 0;
		try {
			specialNumber = Integer.parseInt(invoiceNumberString[0]);
			officeTag = invoiceNumberString[1];
			cashRegisterNumber = Integer.parseInt(invoiceNumberString[2]);
		} catch (NumberFormatException ex){
			if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				ClientAppLogger.GetInstance().ShowMessage("Broj predračuna nije ispravnog oblika.");
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Broj ponude nije ispravnog oblika.");
			}
			return;
		}
		
		if(!officeTag.equals(Licence.GetOfficeTag())){
			if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				ClientAppLogger.GetInstance().ShowMessage("Predračun nije moguće odabrati jer nije izdan u ovoj poslovnici.");
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Ponudu nije moguće odabrati jer nije izdana u ovoj poslovnici.");
			}
			return;
		}
		
		selectedInvoice = ClientAppUtils.GetInvoice(0, officeTag, cashRegisterNumber, specialNumber, paymentMethodType, false);
		if(selectedInvoice == null){
			selectedInvoice = ClientAppUtils.GetInvoice(0, officeTag, cashRegisterNumber, specialNumber, paymentMethodType, true);
		}
		
		if(selectedInvoice == null){
			if(paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				ClientAppLogger.GetInstance().ShowMessage("Ne postoji predračun sa unesenim brojem.");
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Ne postoji ponuda sa unesenim brojem.");
			}
			return;
		}
		
		jTextField1.setEditable(false);
		jTextField1.setEnabled(false);
		jButtonSelect.setEnabled(true);
		jButtonCheck.setEnabled(false);
		jLabelDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(selectedInvoice.date));
		jLabelTotal.setText(ClientAppUtils.FloatToPriceString(selectedInvoice.totalPrice * (100f - selectedInvoice.discountPercentage) / 100f - selectedInvoice.discountValue));
    }//GEN-LAST:event_jButtonCheckActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
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
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonCheck;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JButton jButtonShift;
    private javax.swing.JButton jButtonSpace;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
