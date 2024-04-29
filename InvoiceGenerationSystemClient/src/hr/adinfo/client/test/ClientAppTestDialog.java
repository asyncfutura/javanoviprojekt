/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.test;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.MasterLocalServer;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Pair;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppTestDialog extends javax.swing.JDialog {
	
	private ArrayList<WarehouseItem> warehouseItems = new ArrayList<>();
	
	private class WarehouseItem {
		int itemId;
		int itemType;
		String itemName;
		float itemPrice;
		float eventPrice;
		float taxRate;
		float consumptionTaxRate;
		float packagingRefund;
		boolean isFood;
		boolean articleWithoutNormatives;
	}
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppTestDialog(java.awt.Frame parent, boolean modal) {
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
		
		// Get items
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(4);
		multiDatabaseQuery.SetQuery(0, "SELECT ARTICLES.ID, ARTICLES.NAME, ARTICLES.PRICE, TAX_RATES.VALUE, CONSUMPTION_TAX_VALUES.VALUE, EVENT_PRICE, CONSUMPTION_TAXES.NAME "
				+ "FROM ARTICLES "
				+ "INNER JOIN TAX_RATES ON ARTICLES.TAX_RATE_ID = TAX_RATES.ID "
				+ "INNER JOIN CONSUMPTION_TAXES ON ARTICLES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "INNER JOIN CONSUMPTION_TAX_VALUES ON CONSUMPTION_TAX_VALUES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND ARTICLES.IS_ACTIVE = 1 AND CONSUMPTION_TAX_VALUES.OFFICE_NUMBER = ?");
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.SetQuery(1, "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, TRADING_GOODS.PRICE, TAX_RATES.VALUE, EVENT_PRICE, PACKAGING_REFUNDS.VALUE "
				+ "FROM TRADING_GOODS "
				+ "INNER JOIN TAX_RATES ON TRADING_GOODS.TAX_RATE_ID = TAX_RATES.ID "
				+ "INNER JOIN PACKAGING_REFUNDS ON TRADING_GOODS.PACKAGING_REFUND_ID = PACKAGING_REFUNDS.ID "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0 AND TRADING_GOODS.IS_ACTIVE = 1");
		multiDatabaseQuery.SetQuery(2, "SELECT SERVICES.ID, SERVICES.NAME, SERVICES.PRICE, TAX_RATES.VALUE, EVENT_PRICE "
				+ "FROM SERVICES "
				+ "INNER JOIN TAX_RATES ON SERVICES.TAX_RATE_ID = TAX_RATES.ID "
				+ "WHERE SERVICES.IS_DELETED = 0 AND SERVICES.IS_ACTIVE = 1");
		multiDatabaseQuery.SetQuery(3, "SELECT ARTICLES.ID, "
					+ "(SELECT COUNT(NORMATIVES.ID) FROM NORMATIVES WHERE NORMATIVES.ARTICLE_ID = ARTICLES.ID AND NORMATIVES.IS_DELETED = 0) "
				+ "FROM ARTICLES "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND ARTICLES.IS_ACTIVE = 1");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				MultiDatabaseQueryResponse multiDatabaseQueryResponse = null;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					multiDatabaseQueryResponse = (MultiDatabaseQueryResponse) serverResponse;
				}
				if(multiDatabaseQueryResponse != null){
					ArrayList<Pair<Integer, Integer>> articlesNormativeCount = new ArrayList<>();
					while (multiDatabaseQueryResponse.databaseQueryResult[3].next()) {
						int articleId = multiDatabaseQueryResponse.databaseQueryResult[3].getInt(0);
						int articleNormativesCount = multiDatabaseQueryResponse.databaseQueryResult[3].getInt(1);
						articlesNormativeCount.add(new Pair<>(articleId, articleNormativesCount));
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[0].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[0].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[0].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(3);
						warehouseItem.consumptionTaxRate = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(4);
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(5);
						warehouseItem.packagingRefund = 0f;
						warehouseItem.isFood = multiDatabaseQueryResponse.databaseQueryResult[0].getString(6).toLowerCase().contains("hrana");
						
						for (int i = 0; i < articlesNormativeCount.size(); ++i){
							if(articlesNormativeCount.get(i).getKey() == id && articlesNormativeCount.get(i).getValue() == 0){
								warehouseItem.articleWithoutNormatives = true;
								break;
							}
						}
						
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[1].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[1].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[1].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(3);
						warehouseItem.consumptionTaxRate = 0f;
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(4);
						warehouseItem.packagingRefund = multiDatabaseQueryResponse.databaseQueryResult[1].getFloat(5);
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[2].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[2].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[2].getString(1);
						float price = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(2);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE;
						warehouseItem.itemName = name;
						warehouseItem.itemPrice = price;
						warehouseItem.taxRate = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(3);
						warehouseItem.consumptionTaxRate = 0f;
						warehouseItem.eventPrice = multiDatabaseQueryResponse.databaseQueryResult[2].getFloat(4);
						warehouseItem.packagingRefund = 0f;
						warehouseItems.add(warehouseItem);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void GenerateInvoices (int invoicesCount, int itemsCount){
		if(ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal()))
			return;
		
		if(warehouseItems.isEmpty())
			return;
		
		_privateKey = GetPrivateKey(false);
		_fiscOIB = GetFiscOib(false);
		
		for (int i = 0; i < invoicesCount; ++i){
			Invoice invoice = new Invoice();
			
			invoice.date = new Date();
			invoice.cashRegisterNumber = Licence.GetCashRegisterNumber();
			invoice.officeNumber = Licence.GetOfficeNumber();
			invoice.officeTag = Licence.GetOfficeTag();
			invoice.staffId = StaffUserInfo.GetCurrentUserInfo().userId;
			if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_STUDENT){
				invoice.staffOib = Licence.GetOIB();
			} else {
				invoice.staffOib = StaffUserInfo.GetCurrentUserInfo().userOIB;
			}
			invoice.staffName = StaffUserInfo.GetCurrentUserInfo().firstName;
			invoice.zki = Values.DEFAULT_ZKI;
			invoice.jir = Values.DEFAULT_JIR;
			invoice.isTest = true;
			invoice.isInVatSystem = true;
			
			invoice.paymentMethodName = "Novčanice i/ili kovanice";
			invoice.paymentMethodType = Values.PAYMENT_METHOD_TYPE_CASH;
			
			for (int j = 0; j < itemsCount; ++j){
				int randomItemIndex = (int)(Math.random() * (warehouseItems.size() + 1));
				if (randomItemIndex >= warehouseItems.size()) {
					randomItemIndex = warehouseItems.size() - 1;
				}
				
				WarehouseItem randomItem = warehouseItems.get(randomItemIndex);
				InvoiceItem invoiceItem = new InvoiceItem();
				invoiceItem.itemId = randomItem.itemId;
				invoiceItem.itemType = randomItem.itemType;
				invoiceItem.itemName = randomItem.itemName;
				invoiceItem.itemPrice = randomItem.itemPrice;
				invoiceItem.itemAmount = 1f;
				invoiceItem.taxRate = randomItem.taxRate;
				invoiceItem.consumptionTaxRate = randomItem.consumptionTaxRate;
				invoiceItem.packagingRefund = randomItem.packagingRefund;
				invoiceItem.isFood = randomItem.isFood;
				invoice.items.add(invoiceItem);
			}
			
			float totalPrice = 0f;
			for(InvoiceItem invoiceItem : invoice.items){
				float totalItemPrice = invoiceItem.itemAmount * invoiceItem.itemPrice;
				if(invoiceItem.discountPercentage != 0f){
					totalItemPrice = totalItemPrice * (100f - invoiceItem.discountPercentage) / 100f;
				} else if(invoiceItem.discountValue != 0f){
					totalItemPrice = totalItemPrice - invoiceItem.discountValue * invoiceItem.itemAmount;
				}
				totalPrice += totalItemPrice;
			}
			invoice.totalPrice = totalPrice;
			
			// Fiscalization
			if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
				// Get ZKI
				invoice.zki = CalculateZKI(invoice);
				if(Values.DEFAULT_ZKI.equals(invoice.zki)){
					ClientAppLogger.GetInstance().ShowMessage("Pogreška u izračunu ZKI. Račun nije izdan!" + System.lineSeparator() + "Molimo provjerite ispravnost učitanog certifikata."
							+ System.lineSeparator()+ System.lineSeparator() + "U slučaju ponavljanja pogreške, molimo pokušajte ponovno pokrenuti aplikaciju.");
					return;
				}

				// Insert local invoice
				boolean invoiceInsertSuccess = ClientAppUtils.InsertLocalInvoice(invoice);
				if(!invoiceInsertSuccess){
					ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
					return;
				}

				//Fiscalization.FiscalizeInvoiceSynchronized(invoice, true);
			} else {
				// Insert non-fiscalization invoice
				boolean invoiceInsertSuccess = ClientAppUtils.InsertLocalInvoice(invoice);
				if(!invoiceInsertSuccess){
					ClientAppLogger.GetInstance().ShowMessage("Pogreška u komunikaciji. Račun nije izdan!" + System.lineSeparator() + "Molimo pokušajte ponovno.");
					return;
				}
			}
		}
	}
	
	private static Key _privateKey;
	private static String _fiscOIB;
	
	private Key GetPrivateKey(boolean isProduction){
		int certType = isProduction ? Values.CERT_TYPE_PROD : Values.CERT_TYPE_TEST;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = GetPrivateCertificatePass(certType).toCharArray();
			keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(certType)), password);
			if(keyStore.aliases().hasMoreElements()){
				String keyAlias = keyStore.aliases().nextElement();
				
				// Environment type and key type check
				X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
				boolean isDemoCert = cert.getIssuerDN().toString().toUpperCase().contains("DEMO");
				
				if(isDemoCert && isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U produkcijskoj okolini ne smije se koristiti demo certifikat!");
					return null;
				}
				
				if(!isDemoCert && !isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U demo okolini ne smije se koristiti produkcijski certifikat!");
					return null;
				}
				
				return keyStore.getKey(keyAlias, password);	
			}
		} catch (Exception ex) {
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return null;
	}
	
	private byte[] GetPrivateCertificateBytes(int certType){
		byte[] toReturn = null;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT CERT FROM PRIVATE_CERTIFICATE WHERE ID = ?");
		databaseQuery.AddParam(1, certType);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
			
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
					if (databaseQueryResult.next()) {
						toReturn = databaseQueryResult.getBytes(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	private String GetPrivateCertificatePass(int certType){
		String toReturn = "";
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT PASS FROM PRIVATE_CERTIFICATE WHERE ID = ?");
		databaseQuery.AddParam(1, certType);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
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
					if (databaseQueryResult.next()) {
						toReturn = databaseQueryResult.getString(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	private String GetFiscOib(boolean isProduction){
		String toReturn = Licence.GetOIB();
		
		if(!isProduction){
			try {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				char[] password = GetPrivateCertificatePass(Values.CERT_TYPE_TEST).toCharArray();
				keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(Values.CERT_TYPE_TEST)), password);
				if(keyStore.aliases().hasMoreElements()){
					X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement());
					String subjectString = cert.getSubjectDN().toString();
					Pattern pattern = Pattern.compile("\\d{11}");
					Matcher matcher = pattern.matcher(subjectString);
					if (matcher.find()){
						toReturn = matcher.group();
					}
				}
			} catch(Exception ex){
				ClientAppLogger.GetInstance().LogError(ex);
			}
		}
		
		return toReturn;
	}
	
	private String CalculateZKI(Invoice invoice){
		String returnString = Values.DEFAULT_ZKI;
		String temp = "";
		
		temp += _fiscOIB;
		temp += new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(invoice.date);
		temp += invoice.invoiceNumber;
		temp += invoice.officeTag;
		temp += invoice.cashRegisterNumber;
		temp += ClientAppUtils.FloatToPriceString(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
		
		try {
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign((PrivateKey)_privateKey);
			signature.update(temp.getBytes());
			byte[] signedBytes = signature.sign();
			byte[] digestedBytes = MessageDigest.getInstance("MD5").digest(signedBytes);
			
			char[] hexArray = "0123456789ABCDEF".toCharArray();
			char[] hexChars = new char[digestedBytes.length * 2];
			for (int i = 0; i < digestedBytes.length; i++) {
				int v = digestedBytes[i] & 0xFF;
				hexChars[i * 2] = hexArray[v >>> 4];
				hexChars[i * 2 + 1] = hexArray[v & 0x0F];
			}
			returnString = new String(hexChars).toLowerCase();
		} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException ex) {
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return returnString;
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
        jLabel2 = new javax.swing.JLabel();
        jComboBoxTests = new javax.swing.JComboBox<>();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj uslugu");

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Testni prozor");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel2.setText("Test:");
        jLabel2.setPreferredSize(new java.awt.Dimension(70, 14));

        jComboBoxTests.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Stres test - 1000 malih izmjena", "Generiranje random računa - 10 računa sa 5 stavaka", "Generiranje random računa - 1000 računa sa 5 stavaka", "Generiranje random računa - 50000 računa sa 5 stavaka", "Generiranje random računa - 50000 računa sa 50 stavaka" }));
        jComboBoxTests.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jComboBoxTests.setPreferredSize(new java.awt.Dimension(200, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxTests, 0, 338, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTests, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(84, 78));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(97, 97, 97)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTitle)
                .addGap(168, 168, 168))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabelTitle)
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 63, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		int selectedTest = jComboBoxTests.getSelectedIndex();
		if(selectedTest == 0){
			if(MasterLocalServer.GetInstance() != null){
				DatabaseQuery databaseQuery = new DatabaseQuery("UPDATE TAX_RATES SET VALUE = ? WHERE ID = ?");
				databaseQuery.AddParam(1, 0);
				databaseQuery.AddParam(2, 0);
				MasterLocalServer.GetInstance().TestDiffStressTest(databaseQuery, 1000);
			} else {
				ClientAppLogger.GetInstance().ShowMessage("Dostupno samo na glavnom serveru.");
				return;
			}
		} else if (selectedTest == 1){
			GenerateInvoices(10, 5);
		} else if (selectedTest == 2){
			GenerateInvoices(1000, 5);
		} else if (selectedTest == 3){
			GenerateInvoices(50000, 5);
		} else if (selectedTest == 4){
			GenerateInvoices(50000, 50);
		}
		
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JComboBox<String> jComboBoxTests;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
