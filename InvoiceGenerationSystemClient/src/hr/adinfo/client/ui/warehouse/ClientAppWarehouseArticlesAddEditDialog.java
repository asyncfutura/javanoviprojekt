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
import hr.adinfo.client.datastructures.StaffUserInfo;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
/**
 *
 * @author Matej
 */
public class ClientAppWarehouseArticlesAddEditDialog extends javax.swing.JDialog {
	public boolean changeSuccess = false;
	private int tableId;
	private ArrayList<Integer> categoriesIdList = new ArrayList<>();
	private ArrayList<Integer> measuringUnitsIdList = new ArrayList<>();
	private ArrayList<Integer> taxRatesIdList = new ArrayList<>();
	private ArrayList<Integer> consumptionTaxesIdList = new ArrayList<>();
	
	private float oldPrice;
	private float oldEventPrice;
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsAddDialog
	 */
	public ClientAppWarehouseArticlesAddEditDialog(java.awt.Frame parent, boolean modal, int tableId) {
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
		
		// Default values setup
		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
		defaultComboBoxModel.addElement("Izaberite");
		jComboBoxMaterialCategories.setModel(defaultComboBoxModel);
		jComboBoxMeasuringUnits.setModel(defaultComboBoxModel);
		jComboBoxTaxRates.setModel(defaultComboBoxModel);
		jComboBoxConsumptionTax.setModel(defaultComboBoxModel);
		jComboBoxMaterialMeasuringUnits.setModel(defaultComboBoxModel);
		
		jCheckBox1.setSelected(false);
		jCheckBox1ItemStateChanged(null);
		jCheckBoxAutoCreateMaterial.setSelected(false);
		jCheckBoxAutoCreateMaterialItemStateChanged(null);
		
		SetupDialogComboBoxes();
		
		this.tableId = tableId;
		if(tableId != -1){
			SetupDialogForEdit();
		}
		
		try {
			jFormattedTextFieldNormative.commitEdit();
		} catch (ParseException ex) {}
		
		if(StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN){
			jButtonAddTaxRate.setVisible(false);
			jButtonAddConsumptionTax.setVisible(false);
		}
		
		ClientAppSettings.LoadSettings();
		boolean nightPricesEnabled = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_EVENT_PRICES.ordinal());
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		jLabel9.setVisible(nightPricesEnabled);
		jLabel10.setVisible(nightPricesEnabled);
		jLabel11.setVisible(customIdEnabled);
		jFormattedTextFieldNightPrice.setVisible(nightPricesEnabled);
		jFormattedTextFieldCustomId.setVisible(customIdEnabled);
		jCheckBoxAutomaticCustomId.setVisible(customIdEnabled);
		if(tableId != -1){
			jCheckBoxAutomaticCustomId.setVisible(false);
			jFormattedTextFieldCustomId.setEnabled(false);
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupDialogComboBoxes(){
		// Material categories
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME FROM CATEGORIES WHERE IS_DELETED = 0");
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
						defaultComboBoxModel.addElement("Izaberite");
						ArrayList<Integer> idList = new ArrayList<>();
						idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxMaterialCategories.setModel(defaultComboBoxModel);
						categoriesIdList = idList;
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
		
		// Measuring units
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME FROM MEASURING_UNITS WHERE IS_DELETED = 0");
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
						DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
						//defaultComboBoxModel.addElement("Izaberite");
						ArrayList<Integer> idList = new ArrayList<>();
						//idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							defaultComboBoxModel2.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxMeasuringUnits.setModel(defaultComboBoxModel);
						jComboBoxMaterialMeasuringUnits.setModel(defaultComboBoxModel2);
						measuringUnitsIdList = idList;
					} else {
						Utils.DisposeDialog(this);
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
					Utils.DisposeDialog(this);
				}
			}
		}
		
		// Tax rates
		{
			boolean isInVATSystem = Utils.GetIsInVATSystem(ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
			
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME, VALUE FROM TAX_RATES WHERE IS_DELETED = 0");
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
						int selectedIndex = 0;
						DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
						ArrayList<Integer> idList = new ArrayList<>();
						while (databaseQueryResult.next()) {
							if(isInVATSystem && databaseQueryResult.getFloat(2) > 0f || !isInVATSystem && databaseQueryResult.getFloat(2) == 0f){
								String element = databaseQueryResult.getString(1);
								defaultComboBoxModel.addElement(element);
								idList.add(databaseQueryResult.getInt(0));
							}
							if(isInVATSystem && databaseQueryResult.getFloat(2) == 0f && StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN){
								String element = databaseQueryResult.getString(1);
								defaultComboBoxModel.addElement(element);
								idList.add(databaseQueryResult.getInt(0));
								selectedIndex = 1;
							}
						}
						if(idList.isEmpty()){
							defaultComboBoxModel.addElement("Izaberite");
							idList.add(-1);
						}
						jComboBoxTaxRates.setModel(defaultComboBoxModel);
						taxRatesIdList = idList;
						if(selectedIndex < jComboBoxTaxRates.getItemCount()){
							jComboBoxTaxRates.setSelectedIndex(selectedIndex);
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
		
		// Consumption taxes
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME FROM CONSUMPTION_TAXES WHERE IS_DELETED = 0");
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
						defaultComboBoxModel.addElement("Izaberite");
						ArrayList<Integer> idList = new ArrayList<>();
						idList.add(-1);
						while (databaseQueryResult.next()) {
							String element = databaseQueryResult.getString(1);
							defaultComboBoxModel.addElement(element);
							idList.add(databaseQueryResult.getInt(0));
						}
						jComboBoxConsumptionTax.setModel(defaultComboBoxModel);
						consumptionTaxesIdList = idList;
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
		jLabelTitle.setText("Uredi artikl");
		setTitle("Uredi artikl");
		
		jPanelArticle.setVisible(false);
		
		// Setup dialog for edit
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			String query = "SELECT NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, CONSUMPTION_TAX_ID, "
					+ "MIN_AMOUNT, PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE "
					+ "FROM ARTICLES "
					+ "WHERE ID = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, tableId);
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
							jTextFieldName.setText(databaseQueryResult.getString(0));
							int categoryIndex = categoriesIdList.indexOf(databaseQueryResult.getInt(1));
							int unitIndex = measuringUnitsIdList.indexOf(databaseQueryResult.getInt(2));
							int taxIndex = taxRatesIdList.indexOf(databaseQueryResult.getInt(3));
							int consumptionTaxIndex = consumptionTaxesIdList.indexOf(databaseQueryResult.getInt(4));
							jComboBoxMaterialCategories.setSelectedIndex(categoryIndex);
							jComboBoxMeasuringUnits.setSelectedIndex(unitIndex);
							jComboBoxTaxRates.setSelectedIndex(taxIndex);
							jComboBoxConsumptionTax.setSelectedIndex(consumptionTaxIndex);

							float minAmount = databaseQueryResult.getFloat(5);
							if(minAmount >= 0f){
								jCheckBox1.setSelected(true);
								jCheckBox1ItemStateChanged(null);
								jFormattedTextField1.setValue(databaseQueryResult.getFloat(5));
							}

							jFormattedTextFieldPrice.setValue(databaseQueryResult.getFloat(6));
							jFormattedTextFieldNightPrice.setValue(databaseQueryResult.getFloat(7));
							jFormattedTextFieldCustomId.setValue(databaseQueryResult.getInt(8));
							
							oldPrice = databaseQueryResult.getFloat(6);
							oldEventPrice = databaseQueryResult.getFloat(7);
							
							jCheckBoxIsActive.setSelected(databaseQueryResult.getInt(9) == 1);
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
        jLabel2 = new javax.swing.JLabel();
        jLabelMeasuringUnit = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jComboBoxMaterialCategories = new javax.swing.JComboBox<>();
        jComboBoxMeasuringUnits = new javax.swing.JComboBox<>();
        jComboBoxTaxRates = new javax.swing.JComboBox<>();
        jButtonAddMaterialCategory = new javax.swing.JButton();
        jButtonAddMeasuringUnit = new javax.swing.JButton();
        jButtonAddTaxRate = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxConsumptionTax = new javax.swing.JComboBox<>();
        jButtonAddConsumptionTax = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jFormattedTextFieldPrice = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jFormattedTextFieldNightPrice = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jFormattedTextFieldCustomId = new javax.swing.JFormattedTextField();
        jCheckBoxAutomaticCustomId = new javax.swing.JCheckBox();
        jCheckBoxIsActive = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanelArticle = new javax.swing.JPanel();
        jCheckBoxAutoCreateMaterial = new javax.swing.JCheckBox();
        jFormattedTextFieldNormative = new javax.swing.JFormattedTextField();
        jLabelMaterialMeasuringUnit = new javax.swing.JLabel();
        jComboBoxMaterialMeasuringUnits = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dodaj artikl");
        setPreferredSize(new java.awt.Dimension(511, 690));

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Dodaj artikl");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Naziv:");
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 14));

        jLabel2.setText("Kategorija:");
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 14));

        jLabelMeasuringUnit.setText("Mjerna jedinica:");
        jLabelMeasuringUnit.setPreferredSize(new java.awt.Dimension(120, 14));

        jLabel4.setText("Porezna stopa:");
        jLabel4.setPreferredSize(new java.awt.Dimension(120, 14));

        jTextFieldName.setPreferredSize(new java.awt.Dimension(300, 25));

        jComboBoxMaterialCategories.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMaterialCategories.setPreferredSize(new java.awt.Dimension(200, 25));

        jComboBoxMeasuringUnits.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMeasuringUnits.setMinimumSize(new java.awt.Dimension(56, 25));
        jComboBoxMeasuringUnits.setPreferredSize(new java.awt.Dimension(200, 25));

        jComboBoxTaxRates.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxTaxRates.setMinimumSize(new java.awt.Dimension(56, 25));
        jComboBoxTaxRates.setName(""); // NOI18N
        jComboBoxTaxRates.setPreferredSize(new java.awt.Dimension(200, 25));

        jButtonAddMaterialCategory.setText("+");
        jButtonAddMaterialCategory.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddMaterialCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMaterialCategoryActionPerformed(evt);
            }
        });

        jButtonAddMeasuringUnit.setText("+");
        jButtonAddMeasuringUnit.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddMeasuringUnit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMeasuringUnitActionPerformed(evt);
            }
        });

        jButtonAddTaxRate.setText("+");
        jButtonAddTaxRate.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddTaxRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddTaxRateActionPerformed(evt);
            }
        });

        jLabel6.setText("Porez na potrošnju:");
        jLabel6.setPreferredSize(new java.awt.Dimension(120, 14));

        jComboBoxConsumptionTax.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxConsumptionTax.setMinimumSize(new java.awt.Dimension(56, 25));
        jComboBoxConsumptionTax.setName(""); // NOI18N
        jComboBoxConsumptionTax.setPreferredSize(new java.awt.Dimension(200, 25));

        jButtonAddConsumptionTax.setText("+");
        jButtonAddConsumptionTax.setPreferredSize(new java.awt.Dimension(41, 25));
        jButtonAddConsumptionTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddConsumptionTaxActionPerformed(evt);
            }
        });

        jLabel7.setText("Cijena:");
        jLabel7.setPreferredSize(new java.awt.Dimension(120, 14));

        jFormattedTextFieldPrice.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        jFormattedTextFieldPrice.setPreferredSize(new java.awt.Dimension(100, 25));

        jLabel8.setText("eur");

        jLabel9.setText("Event  cijena:");
        jLabel9.setPreferredSize(new java.awt.Dimension(120, 14));

        jFormattedTextFieldNightPrice.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        jFormattedTextFieldNightPrice.setPreferredSize(new java.awt.Dimension(100, 25));

        jLabel10.setText("eur");

        jLabel11.setText("Šifra artikla:");
        jLabel11.setPreferredSize(new java.awt.Dimension(120, 14));

        jFormattedTextFieldCustomId.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldCustomId.setPreferredSize(new java.awt.Dimension(100, 25));

        jCheckBoxAutomaticCustomId.setText("Dodijeli šifru automatski");
        jCheckBoxAutomaticCustomId.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxAutomaticCustomIdItemStateChanged(evt);
            }
        });

        jCheckBoxIsActive.setSelected(true);
        jCheckBoxIsActive.setText("Aktivan");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTaxRates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBoxMaterialCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonAddMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonAddTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonAddMaterialCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonAddConsumptionTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxConsumptionTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldNightPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldCustomId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBoxAutomaticCustomId, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBoxIsActive))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxMaterialCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddMaterialCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTaxRates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddTaxRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxConsumptionTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddConsumptionTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextFieldNightPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextFieldCustomId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxAutomaticCustomId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxIsActive)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBox1.setText("Automatska obavijest ako količina u skladištu bude manja od:");
        jCheckBox1.setToolTipText("");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

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

        jPanelArticle.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jCheckBoxAutoCreateMaterial.setText("Automatski stvori novi materijal i pridruži ga ovom artiklu s normativom:");
        jCheckBoxAutoCreateMaterial.setToolTipText("");
        jCheckBoxAutoCreateMaterial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxAutoCreateMaterialItemStateChanged(evt);
            }
        });

        jFormattedTextFieldNormative.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
        jFormattedTextFieldNormative.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabelMaterialMeasuringUnit.setText("Mjerna jedinica materijala:");
        jLabelMaterialMeasuringUnit.setPreferredSize(new java.awt.Dimension(140, 14));

        jComboBoxMaterialMeasuringUnits.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMaterialMeasuringUnits.setMinimumSize(new java.awt.Dimension(56, 25));
        jComboBoxMaterialMeasuringUnits.setPreferredSize(new java.awt.Dimension(200, 25));

        javax.swing.GroupLayout jPanelArticleLayout = new javax.swing.GroupLayout(jPanelArticle);
        jPanelArticle.setLayout(jPanelArticleLayout);
        jPanelArticleLayout.setHorizontalGroup(
            jPanelArticleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelArticleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelArticleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelArticleLayout.createSequentialGroup()
                        .addComponent(jCheckBoxAutoCreateMaterial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextFieldNormative, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelArticleLayout.createSequentialGroup()
                        .addComponent(jLabelMaterialMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxMaterialMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelArticleLayout.setVerticalGroup(
            jPanelArticleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelArticleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelArticleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxAutoCreateMaterial)
                    .addComponent(jFormattedTextFieldNormative, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelArticleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMaterialMeasuringUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxMaterialMeasuringUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextField1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(97, 97, 97)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelArticle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(187, 187, 187)
                .addComponent(jLabelTitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabelTitle)
                .addGap(25, 25, 25)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String name = jTextFieldName.getText().trim();
        if("".equals(name)){
            ClientAppLogger.GetInstance().ShowMessage("Unesite ime artikla");
            return;
        }
		
		int comboBoxCategoryId = jComboBoxMaterialCategories.getSelectedIndex();
		int comboBoxUnitsId = jComboBoxMeasuringUnits.getSelectedIndex();
		int comboBoxMaterialUnitsId = jComboBoxMaterialMeasuringUnits.getSelectedIndex();
		int comboBoxTaxesId = jComboBoxTaxRates.getSelectedIndex();
		int comboBoxConsumptionTaxId = jComboBoxConsumptionTax.getSelectedIndex();
		if(comboBoxCategoryId == 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite kategoriju artikla");
            return;
		}
		if(comboBoxUnitsId == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite mjernu jedinicu artikla");
            return;
		}
		if(comboBoxTaxesId == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite poreznu stopu artikla");
            return;
		}
		if(comboBoxConsumptionTaxId == 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite porez na potrošnju artikla");
            return;
		}
		
		float minAmount = -1;
		if(jCheckBox1.isSelected()){
			if(jFormattedTextField1.getValue() != null){
				minAmount = ((Number)jFormattedTextField1.getValue()).floatValue();
			}
		}
		
        if(jFormattedTextFieldPrice.getValue() == null){
            ClientAppLogger.GetInstance().ShowMessage("Cijena nije ispravnog oblika");
            return;
        }
		float price = ((Number)jFormattedTextFieldPrice.getValue()).floatValue();
		price = ClientAppUtils.FloatToPriceFloat(price);
		
		float nightPrice;
		if(jFormattedTextFieldNightPrice.isVisible()){
			if(jFormattedTextFieldNightPrice.getValue() == null){
				ClientAppLogger.GetInstance().ShowMessage("Noćna cijena nije ispravnog oblika");
				return;
			}
			nightPrice = ((Number)jFormattedTextFieldNightPrice.getValue()).floatValue();
			nightPrice = ClientAppUtils.FloatToPriceFloat(nightPrice);
		} else {
			nightPrice = price;
		}
		
		int customId;
		if(jFormattedTextFieldCustomId.isVisible() && !jCheckBoxAutomaticCustomId.isSelected()){
			if(jFormattedTextFieldCustomId.getValue() == null){
				ClientAppLogger.GetInstance().ShowMessage("Šifra artikla nije ispravnog oblika");
				return;
			}
			customId = ((Number)jFormattedTextFieldCustomId.getValue()).intValue();
			if(customId < 0){
				ClientAppLogger.GetInstance().ShowMessage("Šifra artikla nije ispravnog oblika");
				return;
			}
		} else {
			customId = tableId;
		}
		
		int isActive = jCheckBoxIsActive.isSelected() ? 1 : 0;
		
		int materialCategoryId = categoriesIdList.get(comboBoxCategoryId);
		int measuringUnitId = measuringUnitsIdList.get(comboBoxUnitsId);
		int taxRateId = taxRatesIdList.get(comboBoxTaxesId);
		int consumptionTaxId = consumptionTaxesIdList.get(comboBoxConsumptionTaxId);
		
		// Article section
		boolean createNewMaterial = jCheckBoxAutoCreateMaterial.isSelected();
		float normative = 1f;
		int materialMeasuringUnitId = 0;
		if(createNewMaterial){
			if(jFormattedTextFieldNormative.getValue() == null){
				ClientAppLogger.GetInstance().ShowMessage("Normativ nije ispravnog oblika");
				return;
			}
			normative = ((Number)jFormattedTextFieldNormative.getValue()).floatValue();
			
			if(comboBoxMaterialUnitsId == -1){
				ClientAppLogger.GetInstance().ShowMessage("Odaberite mjernu jedinicu materijala");
				return;
			}
			materialMeasuringUnitId = measuringUnitsIdList.get(comboBoxMaterialUnitsId);
		}
		
		// Check if customId is already used
		if(tableId == -1){
			String queryCustomIdCheck = "SELECT NAME FROM ARTICLES WHERE CUSTOM_ID = ?";
		
			final JDialog loadingDialog = new LoadingDialog(null, true);

			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
			multiDatabaseQuery.SetQuery(0, queryCustomIdCheck);
			multiDatabaseQuery.AddParam(0, 1, customId);
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
							ClientAppLogger.GetInstance().ShowMessage("Unesena šifra artikla već se koristi u artiklu " + databaseQueryResult[0].getString(0) + ". Molimo odaberite drugu šifru.");
							return;
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		// Insert article
		if(!(tableId == -1 && createNewMaterial)){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(1);
			if(tableId == -1){
				if(customId == -1){
					String query = "INSERT INTO ARTICLES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, "
						+ "MIN_AMOUNT, CONSUMPTION_TAX_ID, PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
						+ "VALUES ((MAX_WAREHOUSE_ITEM_ID_QUERY), ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "(MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY), "
							+ "?, ?)";
					query = query.replace("MAX_WAREHOUSE_ITEM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_ID_QUERY);
					query = query.replace("MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY);
					multiDatabaseQuery.SetQuery(0, query);
					multiDatabaseQuery.AddParam(0, 1, name);
					multiDatabaseQuery.AddParam(0, 2, materialCategoryId);
					multiDatabaseQuery.AddParam(0, 3, measuringUnitId);
					multiDatabaseQuery.AddParam(0, 4, taxRateId);
					multiDatabaseQuery.AddParam(0, 5, minAmount);
					multiDatabaseQuery.AddParam(0, 6, consumptionTaxId);
					multiDatabaseQuery.AddParam(0, 7, price);
					multiDatabaseQuery.AddParam(0, 8, nightPrice);
					multiDatabaseQuery.AddParam(0, 9, isActive);
					multiDatabaseQuery.AddParam(0, 10, 0);
				} else {
					String query = "INSERT INTO ARTICLES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, "
						+ "MIN_AMOUNT, CONSUMPTION_TAX_ID, PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
						+ "VALUES ((MAX_WAREHOUSE_ITEM_ID_QUERY), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					query = query.replace("MAX_WAREHOUSE_ITEM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_ID_QUERY);
					multiDatabaseQuery.SetQuery(0, query);
					multiDatabaseQuery.AddParam(0, 1, name);
					multiDatabaseQuery.AddParam(0, 2, materialCategoryId);
					multiDatabaseQuery.AddParam(0, 3, measuringUnitId);
					multiDatabaseQuery.AddParam(0, 4, taxRateId);
					multiDatabaseQuery.AddParam(0, 5, minAmount);
					multiDatabaseQuery.AddParam(0, 6, consumptionTaxId);
					multiDatabaseQuery.AddParam(0, 7, price);
					multiDatabaseQuery.AddParam(0, 8, nightPrice);
					multiDatabaseQuery.AddParam(0, 9, customId);
					multiDatabaseQuery.AddParam(0, 10, isActive);
					multiDatabaseQuery.AddParam(0, 11, 0);
				}
			} else {
				multiDatabaseQuery = new MultiDatabaseQuery(2);
				
				String query = "UPDATE ARTICLES SET NAME = ?, CATEGORY_ID = ?, MEASURING_UNIT_ID = ?, TAX_RATE_ID = ?, "
						+ "MIN_AMOUNT = ?, CONSUMPTION_TAX_ID = ?, PRICE = ?, EVENT_PRICE = ?, IS_ACTIVE = ? "
						+ "WHERE ID = ?";
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.AddParam(0, 1, name);
				multiDatabaseQuery.AddParam(0, 2, materialCategoryId);
				multiDatabaseQuery.AddParam(0, 3, measuringUnitId);
				multiDatabaseQuery.AddParam(0, 4, taxRateId);
				multiDatabaseQuery.AddParam(0, 5, minAmount);
				multiDatabaseQuery.AddParam(0, 6, consumptionTaxId);
				multiDatabaseQuery.AddParam(0, 7, price);
				multiDatabaseQuery.AddParam(0, 8, nightPrice);
				multiDatabaseQuery.AddParam(0, 9, isActive);
				multiDatabaseQuery.AddParam(0, 10, tableId);
				
				Date date = new Date();
				multiDatabaseQuery.SetQuery(1, ClientAppUtils.CHANGES_LOG_QUERY);
				multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "CHANGES_LOG");
				multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
				multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("HH:mm:ss").format(date));
				multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 5, StaffUserInfo.GetCurrentUserInfo().userId);
				multiDatabaseQuery.AddParam(1, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
				multiDatabaseQuery.AddParam(1, 7, "Izmjena cijene - artikli");
				multiDatabaseQuery.AddParam(1, 8, tableId + " " + name + ": stara " + oldPrice + ", nova " + price + ", stara event " + oldEventPrice + ", nova event " + nightPrice);
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
		} else {
			// Auto generate material
			MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
			
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
			if(customId == -1){
				String query = "INSERT INTO ARTICLES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, "
					+ "MIN_AMOUNT, CONSUMPTION_TAX_ID, PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
					+ "VALUES ((MAX_WAREHOUSE_ITEM_ID_QUERY), ?, ?, ?, ?, ?, ?, ?, ?, "
						+ "(MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY), "
						+ "?, ?)";
				query = query.replace("MAX_WAREHOUSE_ITEM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_ID_QUERY);
				query = query.replace("MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_CUSTOM_ID_QUERY);
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.AddParam(0, 1, name);
				multiDatabaseQuery.AddParam(0, 2, materialCategoryId);
				multiDatabaseQuery.AddParam(0, 3, measuringUnitId);
				multiDatabaseQuery.AddParam(0, 4, taxRateId);
				multiDatabaseQuery.AddParam(0, 5, minAmount);
				multiDatabaseQuery.AddParam(0, 6, consumptionTaxId);
				multiDatabaseQuery.AddParam(0, 7, price);
				multiDatabaseQuery.AddParam(0, 8, nightPrice);
				multiDatabaseQuery.AddParam(0, 9, isActive);
				multiDatabaseQuery.AddParam(0, 10, 0);
			} else {
				String query = "INSERT INTO ARTICLES (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, TAX_RATE_ID, "
					+ "MIN_AMOUNT, CONSUMPTION_TAX_ID, PRICE, EVENT_PRICE, CUSTOM_ID, IS_ACTIVE, IS_DELETED) "
					+ "VALUES ((MAX_WAREHOUSE_ITEM_ID_QUERY), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				query = query.replace("MAX_WAREHOUSE_ITEM_ID_QUERY", ClientAppUtils.MAX_WAREHOUSE_ITEM_ID_QUERY);
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.AddParam(0, 1, name);
				multiDatabaseQuery.AddParam(0, 2, materialCategoryId);
				multiDatabaseQuery.AddParam(0, 3, measuringUnitId);
				multiDatabaseQuery.AddParam(0, 4, taxRateId);
				multiDatabaseQuery.AddParam(0, 5, minAmount);
				multiDatabaseQuery.AddParam(0, 6, consumptionTaxId);
				multiDatabaseQuery.AddParam(0, 7, price);
				multiDatabaseQuery.AddParam(0, 8, nightPrice);
				multiDatabaseQuery.AddParam(0, 9, customId);
				multiDatabaseQuery.AddParam(0, 10, isActive);
				multiDatabaseQuery.AddParam(0, 11, 0);
			}
			
			String query2 = "INSERT INTO MATERIALS (ID, NAME, CATEGORY_ID, MEASURING_UNIT_ID, "
				+ "MIN_AMOUNT, LAST_PRICE, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			multiDatabaseQuery.SetQuery(1, query2);
			multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "MATERIALS");
			multiDatabaseQuery.AddParam(1, 2, name);
			multiDatabaseQuery.AddParam(1, 3, materialCategoryId);
			multiDatabaseQuery.AddParam(1, 4, materialMeasuringUnitId);
			multiDatabaseQuery.AddParam(1, 5, minAmount);
			multiDatabaseQuery.AddParam(1, 6, 0);
			multiDatabaseQuery.AddParam(1, 7, 0);
				
			String query3 = "INSERT INTO NORMATIVES (ID, ARTICLE_ID, MATERIAL_ID, AMOUNT, IS_DELETED) "
				+ "VALUES (?, (SELECT MAX(ID) FROM ARTICLES), ?, ?, ?)";
			multiDatabaseQuery.SetQuery(2, query3);
			multiDatabaseQuery.SetAutoIncrementParam(2, 1, "ID", "NORMATIVES");
			multiDatabaseQuery.AddAutoGeneratedParam(2, 2, 1);
			multiDatabaseQuery.AddParam(2, 3, normative);
			multiDatabaseQuery.AddParam(2, 4, 0);
			
			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
			databaseQueryTask.execute();
			loadingDialog.setVisible(true);
			if(!databaseQueryTask.isDone()){
				databaseQueryTask.cancel(true);
			} else {
				try {
					ServerResponse serverResponse = databaseQueryTask.get();
					DatabaseQueryResult databaseQueryResult = null;
					if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
						databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult[0];
					}
					if(databaseQueryResult != null){
						changeSuccess = true;
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

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
		boolean isSelected = jCheckBox1.isSelected();
		jFormattedTextField1.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jButtonAddMaterialCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMaterialCategoryActionPerformed
		ClientAppWarehouseCategoriesAddEditDialog addEditdialog = new ClientAppWarehouseCategoriesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddMaterialCategoryActionPerformed

    private void jButtonAddMeasuringUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMeasuringUnitActionPerformed
		ClientAppWarehouseMeasuringUnitsAddEditDialog addEditdialog = new ClientAppWarehouseMeasuringUnitsAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddMeasuringUnitActionPerformed

    private void jButtonAddTaxRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddTaxRateActionPerformed
		ClientAppWarehouseTaxRatesAddEditDialog addEditdialog = new ClientAppWarehouseTaxRatesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddTaxRateActionPerformed

    private void jButtonAddConsumptionTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddConsumptionTaxActionPerformed
		ClientAppWarehouseConsumptionTaxesAddEditDialog addEditdialog = new ClientAppWarehouseConsumptionTaxesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            SetupDialogComboBoxes();
        }
    }//GEN-LAST:event_jButtonAddConsumptionTaxActionPerformed

    private void jCheckBoxAutoCreateMaterialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxAutoCreateMaterialItemStateChanged
		boolean isSelected = jCheckBoxAutoCreateMaterial.isSelected();
		jFormattedTextFieldNormative.setEnabled(isSelected);
		jLabelMaterialMeasuringUnit.setEnabled(isSelected);
		jComboBoxMaterialMeasuringUnits.setEnabled(isSelected);
    }//GEN-LAST:event_jCheckBoxAutoCreateMaterialItemStateChanged

    private void jCheckBoxAutomaticCustomIdItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxAutomaticCustomIdItemStateChanged
		boolean isSelected = jCheckBoxAutomaticCustomId.isSelected();
		jLabel11.setEnabled(!isSelected);
		jFormattedTextFieldCustomId.setEnabled(!isSelected);
    }//GEN-LAST:event_jCheckBoxAutomaticCustomIdItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddConsumptionTax;
    private javax.swing.JButton jButtonAddMaterialCategory;
    private javax.swing.JButton jButtonAddMeasuringUnit;
    private javax.swing.JButton jButtonAddTaxRate;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBoxAutoCreateMaterial;
    private javax.swing.JCheckBox jCheckBoxAutomaticCustomId;
    private javax.swing.JCheckBox jCheckBoxIsActive;
    private javax.swing.JComboBox<String> jComboBoxConsumptionTax;
    private javax.swing.JComboBox<String> jComboBoxMaterialCategories;
    private javax.swing.JComboBox<String> jComboBoxMaterialMeasuringUnits;
    private javax.swing.JComboBox<String> jComboBoxMeasuringUnits;
    private javax.swing.JComboBox<String> jComboBoxTaxRates;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JFormattedTextField jFormattedTextFieldCustomId;
    private javax.swing.JFormattedTextField jFormattedTextFieldNightPrice;
    private javax.swing.JFormattedTextField jFormattedTextFieldNormative;
    private javax.swing.JFormattedTextField jFormattedTextFieldPrice;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelMaterialMeasuringUnit;
    private javax.swing.JLabel jLabelMeasuringUnit;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelArticle;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
}
