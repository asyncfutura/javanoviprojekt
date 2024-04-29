/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.receipts;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppUtils;
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
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Matej
 */
public class ClientAppReceiptsTradingGoodsAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	
	private int receiptTradingGoodsId;
	private ArrayList<Float> receiptTaxRatesValuesList = new ArrayList<>();
	
	private int receiptId;
	private int tradingGoodsId;
	private int currentYear;
	private boolean isInVATSystem;
	private float tradingGoodsTaxRate;
	private float startPricePerUnitWithTax;
	private float startLastPrice;
	private float currentPrice;
	
	private boolean formatLock;
	
	public ClientAppReceiptsTradingGoodsAddEditDialog(java.awt.Frame parent, boolean modal, int tableId, int receiptId, int tradingGoodsId, int currentYear) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonSaveActionPerformed(null);
					}
				}
				
				return false;
			}
		});
		
		this.receiptId = receiptId;
		this.tradingGoodsId = tradingGoodsId;
		this.receiptTradingGoodsId = tableId;
		this.currentYear = currentYear;
		
		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
		defaultComboBoxModel.addElement("Izaberite");
		jComboBoxReceiptTaxRates.setModel(defaultComboBoxModel);
		
		isInVATSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		SetupDialogComboBoxes();
		SetupDialogItemName();
		
		jLabelMeasuringUnit1.setText(Values.TRADING_GOODS_MEASURING_UNIT);
		jLabelMeasuringUnit2.setText(Values.TRADING_GOODS_MEASURING_UNIT);
		jLabelMeasuringUnit3.setText(Values.TRADING_GOODS_MEASURING_UNIT);
		
		if(tableId != -1){
			SetupDialogForEdit();
		}

		formatLock = true;
		UpdateFields();
		formatLock = false;
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
		
	private void SetupDialogItemName(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		String query = "SELECT TRADING_GOODS.NAME, TAX_RATES.VALUE, TRADING_GOODS.PRICE, TRADING_GOODS.LAST_PRICE "
				+ "FROM (TRADING_GOODS INNER JOIN TAX_RATES ON TRADING_GOODS.TAX_RATE_ID = TAX_RATES.ID) "
				+ "WHERE TRADING_GOODS.ID = ? FETCH FIRST ROW ONLY";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, tradingGoodsId);
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
					if (databaseQueryResult.next()) {
						formatLock = true;
						jLabelTradingGoodsName.setText(databaseQueryResult.getString(0));
						tradingGoodsTaxRate = databaseQueryResult.getFloat(1);
						jLabelTaxRate.setText("" + tradingGoodsTaxRate);
						startPricePerUnitWithTax = databaseQueryResult.getFloat(2);
						jFormattedTextFieldPricePerUnitWithTax.setValue(startPricePerUnitWithTax);
						jLabelLastPurchasePrice.setText(ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(3)));
						startLastPrice = databaseQueryResult.getFloat(3);
						formatLock = false;
					}
				} else {
					Utils.DisposeDialog(this);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
				Utils.DisposeDialog(this);
			}
		}
	}
	
	private void SetupDialogComboBoxes(){
		// Receipt tax rates
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT NAME, VALUE FROM RECEIPT_TAX_RATES WHERE IS_DELETED = 0");
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
						DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
						ArrayList<Float> valuesList = new ArrayList<>();
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(0);
							defaultComboBoxModel.addElement(element);
							valuesList.add(databaseQueryResult.getFloat(1));
						}
						jComboBoxReceiptTaxRates.setModel(defaultComboBoxModel);
						receiptTaxRatesValuesList = valuesList;
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
	}
	
	private void SetupDialogForEdit(){
		jLabelTitle.setText("Uredi trgovačku robu u primci");
		setTitle("Uredi trgovačku robu u primci");
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT AMOUNT, PRICE, RABATE, MARGIN, TAX_RATE FROM RECEIPT_TRADING_GOODS WHERE ID = ?");
			databaseQuery.AddParam(1, receiptTradingGoodsId);
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
						if (databaseQueryResult.next()) {
							formatLock = true;
							jFormattedTextFieldAmount.setValue(databaseQueryResult.getFloat(0));
							jLabelPricePerUnit.setText(ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(1)));
							jFormattedTextFieldRabate.setValue(databaseQueryResult.getFloat(2));
							
							float price = databaseQueryResult.getFloat(1);
							int comboBoxTaxRateId = jComboBoxReceiptTaxRates.getSelectedIndex();
							float taxRateValue;
							if(comboBoxTaxRateId != -1){
								taxRateValue = receiptTaxRatesValuesList.get(comboBoxTaxRateId);
							} else {
								taxRateValue = 0f;
							}
							if(isInVATSystem){
								price = price * (100f + taxRateValue) / 100f;
							}
						
							jFormattedTextFieldPrice.setValue(price);
							
							jFormattedTextFieldMargin.setValue(databaseQueryResult.getFloat(3));
							tradingGoodsTaxRate = databaseQueryResult.getFloat(4);
							jLabelTaxRate.setText("" + tradingGoodsTaxRate);
							
							FormattedTextFieldsCheckOK();
							MarginUpdate();
							
							formatLock = false;
						} else {
							Utils.DisposeDialog(this);
						}
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
	}

	private boolean FormattedTextFieldsCheckOK(){
		try {
			jFormattedTextFieldAmount.commitEdit();
		} catch (ParseException ex) {}
		try {
			jFormattedTextFieldPrice.commitEdit();
		} catch (ParseException ex) {}
		try {
			jFormattedTextFieldRabate.commitEdit();
		} catch (ParseException ex) {}
		try {
			jFormattedTextFieldTotal.commitEdit();
		} catch (ParseException ex) {}
		try {
			jFormattedTextFieldMargin.commitEdit();
		} catch (ParseException ex) {}
		try {
			jFormattedTextFieldPricePerUnitWithTax.commitEdit();
		} catch (ParseException ex) {}
		
		if(jFormattedTextFieldAmount.getValue() == null)
			return false;
		if(jFormattedTextFieldPrice.getValue() == null)
			return false;
		if(jFormattedTextFieldRabate.getValue() == null)
			return false;
		if(jFormattedTextFieldTotal.getValue() == null)
			return false;
		if(jFormattedTextFieldMargin.getValue() == null)
			return false;
		if(jFormattedTextFieldPricePerUnitWithTax.getValue() == null)
			return false;
		
		return true;
	}
	
	private void UpdateFields(){
		if(!FormattedTextFieldsCheckOK())
			return;
		
		int comboBoxTaxRateId = jComboBoxReceiptTaxRates.getSelectedIndex();
		float taxRateValue;
		if(comboBoxTaxRateId != -1){
			taxRateValue = receiptTaxRatesValuesList.get(comboBoxTaxRateId);
		} else {
			taxRateValue = 0f;
		}
		if(taxRateValue < 0f){
			ClientAppLogger.GetInstance().ShowMessage("Vrijednost odabrane porezne stope je manja od 0");
			return;
		}
		
		float uneditedPrice = ((Number)jFormattedTextFieldPrice.getValue()).floatValue();
		float price = uneditedPrice;
		if(isInVATSystem && jRadioButtonIncluding.isSelected()){
			price = uneditedPrice * 100f / (100f + taxRateValue);
		} else if(!isInVATSystem && jRadioButtonNotIncluding.isSelected()) {
			price = uneditedPrice * (100f + taxRateValue) / 100f;
		}
		jLabelPricePerUnit.setText(ClientAppUtils.FloatToPriceString(price));
		
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		float rabate = ((Number)jFormattedTextFieldRabate.getValue()).floatValue();
		float totalPrice = amount * price * (1f - rabate / 100f);
		
		float currentTotalPrice = ((Number)jFormattedTextFieldTotal.getValue()).floatValue();
		if(currentTotalPrice != totalPrice){
			jFormattedTextFieldTotal.setValue(totalPrice);
		}
		
		float purchasePrice = price * (1f - rabate / 100f);
		jLabelPurchasePrice.setText(ClientAppUtils.FloatToPriceString(purchasePrice));
		currentPrice = purchasePrice;
		
		float marginPricePerUnitWithTax = ((Number)jFormattedTextFieldPricePerUnitWithTax.getValue()).floatValue();
		float marginPricePerUnitNoTax = marginPricePerUnitWithTax * 100f / (100f + tradingGoodsTaxRate);
		jLabelPricePerUnitNoTax.setText(ClientAppUtils.FloatToPriceString(marginPricePerUnitNoTax));
		jLabelPriceTotalNoTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitNoTax));
		jLabelPriceTotalWithTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitWithTax));
		
		float margin = 0f;
		if(purchasePrice != 0f){
			margin = (marginPricePerUnitNoTax - purchasePrice) * 100f / purchasePrice;
		}
		jFormattedTextFieldMargin.setValue(margin);
		
		float marginPerUnit = margin * purchasePrice / 100f;
		float marginTotal = marginPerUnit * amount;
		jLabelMarginPerUnit.setText(ClientAppUtils.FloatToPriceString(marginPerUnit));
		jLabelMarginTotal.setText(ClientAppUtils.FloatToPriceString(marginTotal));
	}
	
	private void TotalValueUpdate(){
		if(!FormattedTextFieldsCheckOK())
			return;
		
		int comboBoxTaxRateId = jComboBoxReceiptTaxRates.getSelectedIndex();
		float taxRateValue;
		if(comboBoxTaxRateId != -1){
			taxRateValue = receiptTaxRatesValuesList.get(comboBoxTaxRateId);
		} else {
			taxRateValue = 0f;
		}
		if(taxRateValue < 0f){
			ClientAppLogger.GetInstance().ShowMessage("Vrijednost odabrane porezne stope je manja od 0");
			return;
		}
		
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		if(amount == 0f){
			ClientAppLogger.GetInstance().ShowMessage("Prvo unesite količinu");
			return;
		}
		
		float rabate = ((Number)jFormattedTextFieldRabate.getValue()).floatValue();
		float totalPrice = ((Number)jFormattedTextFieldTotal.getValue()).floatValue();
		float price = totalPrice / (amount * (1f - rabate / 100f));
		jLabelPricePerUnit.setText(ClientAppUtils.FloatToPriceString(price));
		
		float uneditedPrice = price;
		if(isInVATSystem && jRadioButtonIncluding.isSelected()){
			uneditedPrice = price * (100f + taxRateValue) / 100f;
		} else if(!isInVATSystem && jRadioButtonNotIncluding.isSelected()) {
			uneditedPrice = price * (100f + taxRateValue) / 100f;
		}
		jFormattedTextFieldPrice.setValue(uneditedPrice);
		
		float purchasePrice = totalPrice / amount;
		jLabelPurchasePrice.setText(ClientAppUtils.FloatToPriceString(purchasePrice));
		currentPrice = purchasePrice;
		
		float marginPricePerUnitWithTax = ((Number)jFormattedTextFieldPricePerUnitWithTax.getValue()).floatValue();
		float marginPricePerUnitNoTax = marginPricePerUnitWithTax * 100f / (100f + tradingGoodsTaxRate);
		jLabelPricePerUnitNoTax.setText(ClientAppUtils.FloatToPriceString(marginPricePerUnitNoTax));
		jLabelPriceTotalNoTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitNoTax));
		jLabelPriceTotalWithTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitWithTax));
		
		float margin = 0f;
		if(purchasePrice != 0f){
			margin = (marginPricePerUnitNoTax - purchasePrice) * 100f / purchasePrice;
		}
		jFormattedTextFieldMargin.setValue(margin);
		
		float marginPerUnit = margin * purchasePrice / 100f;
		float marginTotal = marginPerUnit * amount;
		jLabelMarginPerUnit.setText(ClientAppUtils.FloatToPriceString(marginPerUnit));
		jLabelMarginTotal.setText(ClientAppUtils.FloatToPriceString(marginTotal));
	}
	
	private void MarginUpdate(){
		if(!FormattedTextFieldsCheckOK())
			return;
		
		int comboBoxTaxRateId = jComboBoxReceiptTaxRates.getSelectedIndex();
		float taxRateValue;
		if(comboBoxTaxRateId != -1){
			taxRateValue = receiptTaxRatesValuesList.get(comboBoxTaxRateId);
		} else {
			taxRateValue = 0f;
		}
		if(taxRateValue < 0f){
			ClientAppLogger.GetInstance().ShowMessage("Vrijednost odabrane porezne stope je manja od 0");
			return;
		}
		
		float uneditedPrice = ((Number)jFormattedTextFieldPrice.getValue()).floatValue();
		float price = uneditedPrice;
		if(isInVATSystem && jRadioButtonIncluding.isSelected()){
			price = uneditedPrice * 100f / (100f + taxRateValue);
		} else if(!isInVATSystem && jRadioButtonNotIncluding.isSelected()) {
			price = uneditedPrice * (100f + taxRateValue) / 100f;
		}
		
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		float rabate = ((Number)jFormattedTextFieldRabate.getValue()).floatValue();
		float purchasePrice = price * (1f - rabate / 100f);
		
		float margin = ((Number)jFormattedTextFieldMargin.getValue()).floatValue();
		
		float marginPerUnit = margin * purchasePrice / 100f;
		float marginTotal = marginPerUnit * amount;
		jLabelMarginPerUnit.setText(ClientAppUtils.FloatToPriceString(marginPerUnit));
		jLabelMarginTotal.setText(ClientAppUtils.FloatToPriceString(marginTotal));
		
		float marginPricePerUnitNoTax = purchasePrice + marginPerUnit;
		float marginPricePerUnitWithTax = marginPricePerUnitNoTax * (100f + tradingGoodsTaxRate) / 100f;
		jLabelPricePerUnitNoTax.setText(ClientAppUtils.FloatToPriceString(marginPricePerUnitNoTax));
		jFormattedTextFieldPricePerUnitWithTax.setValue(marginPricePerUnitWithTax);
		jLabelPriceTotalNoTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitNoTax));
		jLabelPriceTotalWithTax.setText(ClientAppUtils.FloatToPriceString(amount * marginPricePerUnitWithTax));
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
        jLabelTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelTradingGoodsName = new javax.swing.JLabel();
        jFormattedTextFieldAmount = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        jFormattedTextFieldRabate = new javax.swing.JFormattedTextField();
        jFormattedTextFieldTotal = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabelMeasuringUnit1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelPurchasePrice = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabelMeasuringUnit2 = new javax.swing.JLabel();
        jLabelLastPurchasePrice = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabelMeasuringUnit3 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabelPricePerUnit = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButtonAddReceiptTaxRate = new javax.swing.JButton();
        jComboBoxReceiptTaxRates = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jFormattedTextFieldPrice = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        jRadioButtonIncluding = new javax.swing.JRadioButton();
        jRadioButtonNotIncluding = new javax.swing.JRadioButton();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jFormattedTextFieldMargin = new javax.swing.JFormattedTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabelMarginPerUnit = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabelMarginTotal = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabelTaxRate = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabelPricePerUnitNoTax = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabelPriceTotalNoTax = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jFormattedTextFieldPricePerUnitWithTax = new javax.swing.JFormattedTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabelPriceTotalWithTax = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj trgovačku robu u primku");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj trgovačku robu u primku");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Naziv:");
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel2.setText("Količina:");
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel4.setText("Rabat:");
        jLabel4.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel6.setText("Ukupno");
        jLabel6.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabel7.setText("Nabavna cijena:");
        jLabel7.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelTradingGoodsName.setText("naziv");

        jFormattedTextFieldAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00000"))));
        jFormattedTextFieldAmount.setPreferredSize(new java.awt.Dimension(200, 25));
        jFormattedTextFieldAmount.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldAmountPropertyChange(evt);
            }
        });

        jLabel9.setText("Posljednja nabavna cijena:");
        jLabel9.setPreferredSize(new java.awt.Dimension(140, 14));

        jFormattedTextFieldRabate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldRabate.setText("0");
        jFormattedTextFieldRabate.setPreferredSize(new java.awt.Dimension(200, 25));
        jFormattedTextFieldRabate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldRabatePropertyChange(evt);
            }
        });

        jFormattedTextFieldTotal.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldTotal.setText("0");
        jFormattedTextFieldTotal.setPreferredSize(new java.awt.Dimension(200, 25));
        jFormattedTextFieldTotal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldTotalPropertyChange(evt);
            }
        });

        jLabel5.setText("=");

        jLabelMeasuringUnit1.setText("mj. jed.");
        jLabelMeasuringUnit1.setPreferredSize(new java.awt.Dimension(70, 14));

        jLabel11.setText("%");

        jLabel12.setText("kn");

        jLabelPurchasePrice.setText("0");

        jLabel14.setText("kn /");

        jLabelMeasuringUnit2.setText("mj. jed.");

        jLabelLastPurchasePrice.setText("0");

        jLabel16.setText("kn /");

        jLabelMeasuringUnit3.setText("mj. jed.");

        jLabel13.setText("Ulazna cijena:");
        jLabel13.setPreferredSize(new java.awt.Dimension(110, 14));

        jLabelPricePerUnit.setText("0");

        jLabel15.setText("kn");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAddReceiptTaxRate.setText("+");
        jButtonAddReceiptTaxRate.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddReceiptTaxRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddReceiptTaxRateActionPerformed(evt);
            }
        });

        jComboBoxReceiptTaxRates.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxReceiptTaxRates.setPreferredSize(new java.awt.Dimension(56, 25));
        jComboBoxReceiptTaxRates.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxReceiptTaxRatesItemStateChanged(evt);
            }
        });

        jLabel10.setText("Cijena sa primke:");
        jLabel10.setPreferredSize(new java.awt.Dimension(120, 14));

        jFormattedTextFieldPrice.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldPrice.setText("0");
        jFormattedTextFieldPrice.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        jFormattedTextFieldPrice.setPreferredSize(new java.awt.Dimension(200, 25));
        jFormattedTextFieldPrice.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPricePropertyChange(evt);
            }
        });

        jLabel8.setText("kn");

        buttonGroup1.add(jRadioButtonIncluding);
        jRadioButtonIncluding.setSelected(true);
        jRadioButtonIncluding.setText("Uključuje PDV");
        jRadioButtonIncluding.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonIncludingItemStateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButtonNotIncluding);
        jRadioButtonNotIncluding.setText("Ne uključuje PDV");
        jRadioButtonNotIncluding.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonNotIncludingItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jRadioButtonIncluding)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonNotIncluding)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jFormattedTextFieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jComboBoxReceiptTaxRates, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddReceiptTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonIncluding)
                    .addComponent(jRadioButtonNotIncluding)
                    .addComponent(jComboBoxReceiptTaxRates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddReceiptTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabelPurchasePrice)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelMeasuringUnit2))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabelLastPurchasePrice)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelMeasuringUnit3))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelTradingGoodsName)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabelMeasuringUnit1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jFormattedTextFieldRabate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel11))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jFormattedTextFieldTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel12))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabelPricePerUnit)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel15))))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(268, 268, 268)
                                .addComponent(jLabel5))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTradingGoodsName))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMeasuringUnit1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPricePerUnit)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldRabate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPurchasePrice)
                    .addComponent(jLabel14)
                    .addComponent(jLabelMeasuringUnit2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLastPurchasePrice)
                    .addComponent(jLabel16)
                    .addComponent(jLabelMeasuringUnit3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Marža"));

        jLabel3.setText("Marža:");
        jLabel3.setPreferredSize(new java.awt.Dimension(140, 14));

        jFormattedTextFieldMargin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldMargin.setText("0");
        jFormattedTextFieldMargin.setPreferredSize(new java.awt.Dimension(60, 25));
        jFormattedTextFieldMargin.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldMarginPropertyChange(evt);
            }
        });

        jLabel18.setText("Marža po komadu:");
        jLabel18.setPreferredSize(new java.awt.Dimension(150, 14));

        jLabelMarginPerUnit.setText("jLabel19");
        jLabelMarginPerUnit.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel20.setText("Marža ukupno:");
        jLabel20.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelMarginTotal.setText("jLabel21");
        jLabelMarginTotal.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel22.setText("Iznos porezne stope:");
        jLabel22.setPreferredSize(new java.awt.Dimension(150, 14));

        jLabelTaxRate.setText("jLabel23");
        jLabelTaxRate.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel24.setText("%");

        jLabel19.setText("Cijena po komadu bez poreza:");
        jLabel19.setPreferredSize(new java.awt.Dimension(150, 14));

        jLabelPricePerUnitNoTax.setText("jLabel21");
        jLabelPricePerUnitNoTax.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel23.setText("Cijena ukupno bez poreza:");
        jLabel23.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelPriceTotalNoTax.setText("jLabel25");
        jLabelPriceTotalNoTax.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel26.setText("Cijena po komadu sa porezom:");
        jLabel26.setPreferredSize(new java.awt.Dimension(150, 14));

        jFormattedTextFieldPricePerUnitWithTax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldPricePerUnitWithTax.setPreferredSize(new java.awt.Dimension(60, 25));
        jFormattedTextFieldPricePerUnitWithTax.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPricePerUnitWithTaxPropertyChange(evt);
            }
        });

        jLabel17.setText("%");

        jLabel25.setText("Cijena ukupno sa porezom:");
        jLabel25.setPreferredSize(new java.awt.Dimension(140, 14));

        jLabelPriceTotalWithTax.setText("jLabel25");
        jLabelPriceTotalWithTax.setPreferredSize(new java.awt.Dimension(60, 14));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextFieldPricePerUnitWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelPriceTotalWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelPricePerUnitNoTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel24))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelMarginPerUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabelMarginTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabelPriceTotalNoTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextFieldMargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldMargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMarginPerUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMarginTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPricePerUnitNoTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPriceTotalNoTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelPriceTotalWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jFormattedTextFieldPricePerUnitWithTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(97, 97, 97)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(117, 117, 117)
                        .addComponent(jLabelTitle)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabelTitle)
                .addGap(35, 35, 35)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		if(jFormattedTextFieldAmount.getValue() == null){
            ClientAppLogger.GetInstance().ShowMessage("Količina nije ispravnog oblika");
            return;
        }
		float amount = ((Number)jFormattedTextFieldAmount.getValue()).floatValue();
		
		try {
			jFormattedTextFieldPrice.commitEdit();
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Cijena sa primke nije ispravnog oblika");
            return;
		}
        if(jFormattedTextFieldPrice.getValue() == null){
            ClientAppLogger.GetInstance().ShowMessage("Cijena sa primke nije ispravnog oblika");
            return;
        }
		float uneditedPrice = ((Number)jFormattedTextFieldPrice.getValue()).floatValue();
		
		if(jFormattedTextFieldRabate.getValue() == null){
			ClientAppLogger.GetInstance().ShowMessage("Rabat nije ispravnog oblika");
			return;
		}
		float rabate = ((Number)jFormattedTextFieldRabate.getValue()).floatValue();
		rabate = ClientAppUtils.FloatToPriceFloat(rabate);
		
		if(jFormattedTextFieldMargin.getValue() == null){
			ClientAppLogger.GetInstance().ShowMessage("Marža nije ispravnog oblika");
			return;
		}
		float margin = ((Number)jFormattedTextFieldMargin.getValue()).floatValue();
		margin = ClientAppUtils.FloatToPriceFloat(margin);
		
		int comboBoxTaxRateId = jComboBoxReceiptTaxRates.getSelectedIndex();
		float taxRateValue;
		if(comboBoxTaxRateId != -1){
			taxRateValue = receiptTaxRatesValuesList.get(comboBoxTaxRateId);
		} else {
			taxRateValue = 0f;
		}
		if(taxRateValue < 0f)
			return;
		
		float price = uneditedPrice;
		if(isInVATSystem && jRadioButtonIncluding.isSelected()){
			price = uneditedPrice * 100f / (100f + taxRateValue);
		} else if(!isInVATSystem && jRadioButtonNotIncluding.isSelected()) {
			price = uneditedPrice * (100f + taxRateValue) / 100f;
		}
		price = ClientAppUtils.FloatToPriceFloat(price);
		
		float marginPricePerUnitWithTax = ((Number)jFormattedTextFieldPricePerUnitWithTax.getValue()).floatValue();
		
		// Change price dialog
		boolean changePrice = false;
		if(marginPricePerUnitWithTax != startPricePerUnitWithTax){
			String message = "Izračunata cijena po komadu trgovačke robe (" + jLabelTradingGoodsName.getText() + ") razlikuje se od stare cijene." + System.lineSeparator() + System.lineSeparator()
					+ "Stara cijena: " + startPricePerUnitWithTax + System.lineSeparator()
					+ "Nova cijena: " + marginPricePerUnitWithTax + System.lineSeparator() + System.lineSeparator()
					+ "Želite li ažurirati cijenu ove trgovačke robe?";
			int dialogResult = JOptionPane.showConfirmDialog (null, message, "Ažuriraj cijenu", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				changePrice = true;
			}
		}
		
		{
			int multiDatabaseQueryLength;
			if (receiptTradingGoodsId == -1) {
				multiDatabaseQueryLength = 3;
			} else {
				multiDatabaseQueryLength = 2;
			}
			if(currentPrice != startLastPrice){
				multiDatabaseQueryLength += 1;
			}
			if(changePrice){
				multiDatabaseQueryLength += 1;
			}
			int queryCounter = 0;
			
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(multiDatabaseQueryLength);
			
			if(receiptTradingGoodsId == -1){
				String queryInsert = "INSERT INTO RECEIPT_TRADING_GOODS (ID, RECEIPT_ID, TRADING_GOODS_ID, AMOUNT, PRICE, RABATE, MARGIN, TAX_RATE, TAX_IN_VALUE, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
				multiDatabaseQuery.SetQuery(queryCounter, queryInsert);
				multiDatabaseQuery.SetAutoIncrementParam(queryCounter, 1, "ID", "RECEIPT_TRADING_GOODS");
				multiDatabaseQuery.AddParam(queryCounter, 2, receiptId);
				multiDatabaseQuery.AddParam(queryCounter, 3, tradingGoodsId);
				multiDatabaseQuery.AddParam(queryCounter, 4, 0);
				multiDatabaseQuery.AddParam(queryCounter, 5, price);
				multiDatabaseQuery.AddParam(queryCounter, 6, rabate);
				multiDatabaseQuery.AddParam(queryCounter, 7, margin);
				multiDatabaseQuery.AddParam(queryCounter, 8, tradingGoodsTaxRate);
				multiDatabaseQuery.AddParam(queryCounter, 9, taxRateValue);
				queryCounter++;
			}
			
			String queryUpdateTradingGoodsPlus = "UPDATE TRADING_GOODS_AMOUNTS "
					+ "SET AMOUNT = AMOUNT + (? - (SELECT AMOUNT FROM RECEIPT_TRADING_GOODS WHERE ID = ?)) "
					+ "WHERE TRADING_GOODS_ID = ? AND OFFICE_NUMBER = ?"
					+ "AND (SELECT IS_DELETED FROM RECEIPTS WHERE ID = ?) = 0 "
					+ "AND (SELECT IS_DELETED FROM RECEIPT_TRADING_GOODS WHERE ID = ?) = 0 "
					+ "AND AMOUNT_YEAR = ?";
			multiDatabaseQuery.SetQuery(queryCounter, queryUpdateTradingGoodsPlus);
			multiDatabaseQuery.AddParam(queryCounter, 1, amount);
			multiDatabaseQuery.AddParam(queryCounter, 3, tradingGoodsId);
			multiDatabaseQuery.AddParam(queryCounter, 4, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(queryCounter, 5, receiptId);
			multiDatabaseQuery.AddParam(queryCounter, 7, currentYear);
					
			if(receiptTradingGoodsId == -1){
				multiDatabaseQuery.AddAutoGeneratedParam(queryCounter, 2, 0);
				multiDatabaseQuery.AddAutoGeneratedParam(queryCounter, 6, 0);
			} else {
				multiDatabaseQuery.AddParam(queryCounter, 2, receiptTradingGoodsId);
				multiDatabaseQuery.AddParam(queryCounter, 6, receiptTradingGoodsId);
			}
			queryCounter++;
			
			String queryUpdate = "UPDATE RECEIPT_TRADING_GOODS SET AMOUNT = ?, PRICE = ?, RABATE = ?, MARGIN = ?, TAX_IN_VALUE = ? WHERE ID = ?";
			multiDatabaseQuery.SetQuery(queryCounter, queryUpdate);
			multiDatabaseQuery.AddParam(queryCounter, 1, amount);
			multiDatabaseQuery.AddParam(queryCounter, 2, price);
			multiDatabaseQuery.AddParam(queryCounter, 3, rabate);
			multiDatabaseQuery.AddParam(queryCounter, 4, margin);
			multiDatabaseQuery.AddParam(queryCounter, 5, taxRateValue);
			if(receiptTradingGoodsId == -1){
				multiDatabaseQuery.AddAutoGeneratedParam(queryCounter, 6, 0);
			} else {
				multiDatabaseQuery.AddParam(queryCounter, 6, receiptTradingGoodsId);
			}
			queryCounter++;
			
			if(currentPrice != startLastPrice){
				String queryUpdateTradingGoodsLastPrice = "UPDATE TRADING_GOODS SET TRADING_GOODS.LAST_PRICE = ? "
						+ "WHERE TRADING_GOODS.ID = ? "
						+ "AND (SELECT MAX(ID) FROM RECEIPTS WHERE RECEIPTS.OFFICE_NUMBER = ?) = ?";
				multiDatabaseQuery.SetQuery(queryCounter, queryUpdateTradingGoodsLastPrice);
				multiDatabaseQuery.AddParam(queryCounter, 1, currentPrice);
				multiDatabaseQuery.AddParam(queryCounter, 2, tradingGoodsId);
				multiDatabaseQuery.AddParam(queryCounter, 3, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(queryCounter, 4, receiptId);
				queryCounter++;
			}
			
			if(changePrice){
				String queryUpdateTradingGoodsPrice = "UPDATE TRADING_GOODS SET PRICE = ? WHERE ID = ? ";
				multiDatabaseQuery.SetQuery(queryCounter, queryUpdateTradingGoodsPrice);
				multiDatabaseQuery.AddParam(queryCounter, 1, marginPricePerUnitWithTax);
				multiDatabaseQuery.AddParam(queryCounter, 2, tradingGoodsId);
				queryCounter++;
			}
			
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
						changeSuccess = true;
					} else {
						return;
					}
				} catch (Exception ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		if(changeSuccess){
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonAddReceiptTaxRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddReceiptTaxRateActionPerformed
        ClientAppReceiptsTaxRatesDialog dialog = new ClientAppReceiptsTaxRatesDialog(null, true);
        dialog.setVisible(true);
        SetupDialogComboBoxes();
    }//GEN-LAST:event_jButtonAddReceiptTaxRateActionPerformed

    private void jFormattedTextFieldAmountPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldAmountPropertyChange
		if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldAmountPropertyChange

    private void jFormattedTextFieldPricePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldPricePropertyChange
		if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldPricePropertyChange

    private void jFormattedTextFieldRabatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRabatePropertyChange
		if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldRabatePropertyChange

    private void jFormattedTextFieldTotalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldTotalPropertyChange
		if(formatLock)
			return;
		
		formatLock = true;
		TotalValueUpdate();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldTotalPropertyChange

    private void jRadioButtonIncludingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonIncludingItemStateChanged
        if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jRadioButtonIncludingItemStateChanged

    private void jRadioButtonNotIncludingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonNotIncludingItemStateChanged
        if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jRadioButtonNotIncludingItemStateChanged

    private void jFormattedTextFieldMarginPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldMarginPropertyChange
       if(formatLock)
			return;
		
		formatLock = true;
		MarginUpdate();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldMarginPropertyChange

    private void jFormattedTextFieldPricePerUnitWithTaxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldPricePerUnitWithTaxPropertyChange
        if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jFormattedTextFieldPricePerUnitWithTaxPropertyChange

    private void jComboBoxReceiptTaxRatesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxReceiptTaxRatesItemStateChanged
        if(formatLock)
			return;
		
		formatLock = true;
		UpdateFields();
		formatLock = false;
    }//GEN-LAST:event_jComboBoxReceiptTaxRatesItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonAddReceiptTaxRate;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JComboBox<String> jComboBoxReceiptTaxRates;
    private javax.swing.JFormattedTextField jFormattedTextFieldAmount;
    private javax.swing.JFormattedTextField jFormattedTextFieldMargin;
    private javax.swing.JFormattedTextField jFormattedTextFieldPrice;
    private javax.swing.JFormattedTextField jFormattedTextFieldPricePerUnitWithTax;
    private javax.swing.JFormattedTextField jFormattedTextFieldRabate;
    private javax.swing.JFormattedTextField jFormattedTextFieldTotal;
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
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelLastPurchasePrice;
    private javax.swing.JLabel jLabelMarginPerUnit;
    private javax.swing.JLabel jLabelMarginTotal;
    private javax.swing.JLabel jLabelMeasuringUnit1;
    private javax.swing.JLabel jLabelMeasuringUnit2;
    private javax.swing.JLabel jLabelMeasuringUnit3;
    private javax.swing.JLabel jLabelPricePerUnit;
    private javax.swing.JLabel jLabelPricePerUnitNoTax;
    private javax.swing.JLabel jLabelPriceTotalNoTax;
    private javax.swing.JLabel jLabelPriceTotalWithTax;
    private javax.swing.JLabel jLabelPurchasePrice;
    private javax.swing.JLabel jLabelTaxRate;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTradingGoodsName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButtonIncluding;
    private javax.swing.JRadioButton jRadioButtonNotIncluding;
    // End of variables declaration//GEN-END:variables
}
