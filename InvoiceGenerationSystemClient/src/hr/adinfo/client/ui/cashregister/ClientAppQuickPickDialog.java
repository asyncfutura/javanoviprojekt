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
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Matej
 */
public class ClientAppQuickPickDialog extends javax.swing.JDialog {
	
	private final String[] jLabelQuickPickLetters = new String[]{
		"A", "B", "C", "Č", "Ć", "D", "Đ", "E", "F", "G", "H", "I", "J", "K", 
		"L", "M", "N", "O", "P", "R", "S", "Š", "T", "U", "V", "Z", "Ž", "Y",
		"X", "Q", "W"
	};
	
	private final JLabel[] jLabelQuickPickGroup;
	private final JButton[] jButtonSelectGroup;
	private final JTextField[] jTextFieldFullNameGroup;
	private final JTextField[] jTextFieldShortNameGroup;
	
	private int[] quickPickId = new int[31];
	private int[] quickPickType = new int[31];
	private String[] quickPickName = new String[31];
	
	private class WarehouseItem {
		int itemId;
		String itemName;
	}

	private ArrayList<WarehouseItem> warehouseItems = new ArrayList<>();
	
	/**
	 * Creates new form ClientAppStaffDialog
	 */
	public ClientAppQuickPickDialog(java.awt.Frame parent, boolean modal) {
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
		
		// Init
		jLabelQuickPickGroup = new JLabel[]{
			jLabelKey1, jLabelKey2, jLabelKey3, jLabelKey4,	jLabelKey5, jLabelKey6, jLabelKey7, jLabelKey8, jLabelKey9, jLabelKey10, 
			jLabelKey11, jLabelKey12, jLabelKey13, jLabelKey14,	jLabelKey15, jLabelKey16, jLabelKey17, jLabelKey18, jLabelKey19, jLabelKey20,
			jLabelKey21, jLabelKey22, jLabelKey23, jLabelKey24, jLabelKey25, jLabelKey26, jLabelKey27, jLabelKey28, jLabelKey29, jLabelKey30,
			jLabelKey31, 
		};
		jButtonSelectGroup = new JButton[]{
			jButtonSelect1, jButtonSelect2, jButtonSelect3, jButtonSelect4,	jButtonSelect5, jButtonSelect6, jButtonSelect7, jButtonSelect8, jButtonSelect9, jButtonSelect10, 
			jButtonSelect11, jButtonSelect12, jButtonSelect13, jButtonSelect14,	jButtonSelect15, jButtonSelect16, jButtonSelect17, jButtonSelect18, jButtonSelect19, jButtonSelect20,
			jButtonSelect21, jButtonSelect22, jButtonSelect23, jButtonSelect24, jButtonSelect25, jButtonSelect26, jButtonSelect27, jButtonSelect28, jButtonSelect29, jButtonSelect30,
			jButtonSelect31, 
		};
		jTextFieldFullNameGroup = new JTextField[]{
			jTextFieldFullName1, jTextFieldFullName2, jTextFieldFullName3, jTextFieldFullName4,	jTextFieldFullName5, jTextFieldFullName6, jTextFieldFullName7, jTextFieldFullName8, jTextFieldFullName9, jTextFieldFullName10, 
			jTextFieldFullName11, jTextFieldFullName12, jTextFieldFullName13, jTextFieldFullName14,	jTextFieldFullName15, jTextFieldFullName16, jTextFieldFullName17, jTextFieldFullName18, jTextFieldFullName19, jTextFieldFullName20,
			jTextFieldFullName21, jTextFieldFullName22, jTextFieldFullName23, jTextFieldFullName24, jTextFieldFullName25, jTextFieldFullName26, jTextFieldFullName27, jTextFieldFullName28, jTextFieldFullName29, jTextFieldFullName30,
			jTextFieldFullName31, 
		};
		jTextFieldShortNameGroup = new JTextField[]{
			jTextFieldShortName1, jTextFieldShortName2, jTextFieldShortName3, jTextFieldShortName4,	jTextFieldShortName5, jTextFieldShortName6, jTextFieldShortName7, jTextFieldShortName8, jTextFieldShortName9, jTextFieldShortName10, 
			jTextFieldShortName11, jTextFieldShortName12, jTextFieldShortName13, jTextFieldShortName14,	jTextFieldShortName15, jTextFieldShortName16, jTextFieldShortName17, jTextFieldShortName18, jTextFieldShortName19, jTextFieldShortName20,
			jTextFieldShortName21, jTextFieldShortName22, jTextFieldShortName23, jTextFieldShortName24, jTextFieldShortName25, jTextFieldShortName26, jTextFieldShortName27, jTextFieldShortName28, jTextFieldShortName29, jTextFieldShortName30,
			jTextFieldShortName31, 
		};
		
		for (int i = 0; i < jButtonSelectGroup.length; ++i){
			final int index = i;
			jButtonSelectGroup[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				OnChangeQuickPickButtonClick(index);
			});
		}
		for (int i = 0; i < jTextFieldShortNameGroup.length; ++i){
			final int index = i;
			jTextFieldShortNameGroup[i].addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					if (jTextFieldShortNameGroup[index].getText().length() >= 8){
						e.consume();
					}
				}
			});
		}
		
		// Get item names
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
		multiDatabaseQuery.SetQuery(0, "SELECT ARTICLES.ID, ARTICLES.NAME FROM ARTICLES");
		multiDatabaseQuery.SetQuery(1, "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME FROM TRADING_GOODS");
		multiDatabaseQuery.SetQuery(2, "SELECT SERVICES.ID, SERVICES.NAME FROM SERVICES");
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
					while (multiDatabaseQueryResponse.databaseQueryResult[0].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[0].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[0].getString(1);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemName = name;
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[1].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[1].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[1].getString(1);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemName = name;
						warehouseItems.add(warehouseItem);
					}
					while (multiDatabaseQueryResponse.databaseQueryResult[2].next()) {
						int id = multiDatabaseQueryResponse.databaseQueryResult[2].getInt(0);
						String name = multiDatabaseQueryResponse.databaseQueryResult[2].getString(1);
						WarehouseItem warehouseItem = new WarehouseItem();
						warehouseItem.itemId = id;
						warehouseItem.itemName = name;
						warehouseItems.add(warehouseItem);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		ClientAppSettings.LoadSettings();
		String[] quickPickIdStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickNameStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		String[] quickPickTypeStrings = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		for (int i = 0; i < jLabelQuickPickGroup.length; ++i){
			if (i < quickPickIdStrings.length){
				quickPickId[i] = Integer.parseInt(quickPickIdStrings[i]);
			} else {
				quickPickId[i] = -1;
			}
			
			if (i < quickPickTypeStrings.length){
				quickPickType[i] = Integer.parseInt(quickPickTypeStrings[i]);
			} else {
				quickPickType[i] = -1;
			}
			
			if (i < quickPickNameStrings.length){
				quickPickName[i] = quickPickNameStrings[i];
			} else {
				quickPickName[i] = "";
			}
			
			jLabelQuickPickGroup[i].setText("Tipka " + jLabelQuickPickLetters[i] + ":");
			jButtonSelectGroup[i].setText("Zamijeni " + jLabelQuickPickLetters[i]);
			if (quickPickId[i] != -1){
				jTextFieldFullNameGroup[i].setText(GetItemName(quickPickId[i]));
				jTextFieldShortNameGroup[i].setText(quickPickName[i]);
			} else {
				jTextFieldFullNameGroup[i].setText("");
				jTextFieldShortNameGroup[i].setText("");
			}
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private String GetItemName(int itemId){
		for (WarehouseItem warehouseItem : warehouseItems){
			if(warehouseItem.itemId == itemId ){
				return warehouseItem.itemName;
			}
		}
		
		return "Naziv";
	}
	
	private void OnChangeQuickPickButtonClick(int index){
		ClientAppSelectItemDialog dialog = new ClientAppSelectItemDialog(null, true, "");
		dialog.setVisible(true);
		if(dialog.selectedId != -1){
			quickPickId[index] = dialog.selectedId;
			String name = GetItemName(dialog.selectedId);
			jTextFieldFullNameGroup[index].setText(name);
			quickPickName[index] = name.length() > 8 ? name.substring(0, 8) : name;
			jTextFieldShortNameGroup[index].setText(quickPickName[index]);
		} else {
			quickPickId[index] = -1;
			quickPickName[index] = "";
			jTextFieldFullNameGroup[index].setText("");
			jTextFieldShortNameGroup[index].setText("");
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
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelKey1 = new javax.swing.JPanel();
        jLabelKey1 = new javax.swing.JLabel();
        jButtonSelect1 = new javax.swing.JButton();
        jTextFieldFullName1 = new javax.swing.JTextField();
        jTextFieldShortName1 = new javax.swing.JTextField();
        jPanelKey2 = new javax.swing.JPanel();
        jLabelKey2 = new javax.swing.JLabel();
        jButtonSelect2 = new javax.swing.JButton();
        jTextFieldFullName2 = new javax.swing.JTextField();
        jTextFieldShortName2 = new javax.swing.JTextField();
        jPanelKey3 = new javax.swing.JPanel();
        jLabelKey3 = new javax.swing.JLabel();
        jButtonSelect3 = new javax.swing.JButton();
        jTextFieldFullName3 = new javax.swing.JTextField();
        jTextFieldShortName3 = new javax.swing.JTextField();
        jPanelKey4 = new javax.swing.JPanel();
        jLabelKey4 = new javax.swing.JLabel();
        jButtonSelect4 = new javax.swing.JButton();
        jTextFieldFullName4 = new javax.swing.JTextField();
        jTextFieldShortName4 = new javax.swing.JTextField();
        jPanelKey5 = new javax.swing.JPanel();
        jLabelKey5 = new javax.swing.JLabel();
        jButtonSelect5 = new javax.swing.JButton();
        jTextFieldFullName5 = new javax.swing.JTextField();
        jTextFieldShortName5 = new javax.swing.JTextField();
        jPanelKey6 = new javax.swing.JPanel();
        jLabelKey6 = new javax.swing.JLabel();
        jButtonSelect6 = new javax.swing.JButton();
        jTextFieldFullName6 = new javax.swing.JTextField();
        jTextFieldShortName6 = new javax.swing.JTextField();
        jPanelKey7 = new javax.swing.JPanel();
        jLabelKey7 = new javax.swing.JLabel();
        jButtonSelect7 = new javax.swing.JButton();
        jTextFieldFullName7 = new javax.swing.JTextField();
        jTextFieldShortName7 = new javax.swing.JTextField();
        jPanelKey8 = new javax.swing.JPanel();
        jLabelKey8 = new javax.swing.JLabel();
        jButtonSelect8 = new javax.swing.JButton();
        jTextFieldFullName8 = new javax.swing.JTextField();
        jTextFieldShortName8 = new javax.swing.JTextField();
        jPanelKey9 = new javax.swing.JPanel();
        jLabelKey9 = new javax.swing.JLabel();
        jButtonSelect9 = new javax.swing.JButton();
        jTextFieldFullName9 = new javax.swing.JTextField();
        jTextFieldShortName9 = new javax.swing.JTextField();
        jPanelKey10 = new javax.swing.JPanel();
        jLabelKey10 = new javax.swing.JLabel();
        jButtonSelect10 = new javax.swing.JButton();
        jTextFieldFullName10 = new javax.swing.JTextField();
        jTextFieldShortName10 = new javax.swing.JTextField();
        jPanelKey11 = new javax.swing.JPanel();
        jLabelKey11 = new javax.swing.JLabel();
        jButtonSelect11 = new javax.swing.JButton();
        jTextFieldFullName11 = new javax.swing.JTextField();
        jTextFieldShortName11 = new javax.swing.JTextField();
        jPanelKey12 = new javax.swing.JPanel();
        jLabelKey12 = new javax.swing.JLabel();
        jButtonSelect12 = new javax.swing.JButton();
        jTextFieldFullName12 = new javax.swing.JTextField();
        jTextFieldShortName12 = new javax.swing.JTextField();
        jPanelKey13 = new javax.swing.JPanel();
        jLabelKey13 = new javax.swing.JLabel();
        jButtonSelect13 = new javax.swing.JButton();
        jTextFieldFullName13 = new javax.swing.JTextField();
        jTextFieldShortName13 = new javax.swing.JTextField();
        jPanelKey14 = new javax.swing.JPanel();
        jLabelKey14 = new javax.swing.JLabel();
        jButtonSelect14 = new javax.swing.JButton();
        jTextFieldFullName14 = new javax.swing.JTextField();
        jTextFieldShortName14 = new javax.swing.JTextField();
        jPanelKey15 = new javax.swing.JPanel();
        jLabelKey15 = new javax.swing.JLabel();
        jButtonSelect15 = new javax.swing.JButton();
        jTextFieldFullName15 = new javax.swing.JTextField();
        jTextFieldShortName15 = new javax.swing.JTextField();
        jPanelKey16 = new javax.swing.JPanel();
        jLabelKey16 = new javax.swing.JLabel();
        jButtonSelect16 = new javax.swing.JButton();
        jTextFieldFullName16 = new javax.swing.JTextField();
        jTextFieldShortName16 = new javax.swing.JTextField();
        jPanelKey17 = new javax.swing.JPanel();
        jLabelKey17 = new javax.swing.JLabel();
        jButtonSelect17 = new javax.swing.JButton();
        jTextFieldFullName17 = new javax.swing.JTextField();
        jTextFieldShortName17 = new javax.swing.JTextField();
        jPanelKey18 = new javax.swing.JPanel();
        jLabelKey18 = new javax.swing.JLabel();
        jButtonSelect18 = new javax.swing.JButton();
        jTextFieldFullName18 = new javax.swing.JTextField();
        jTextFieldShortName18 = new javax.swing.JTextField();
        jPanelKey19 = new javax.swing.JPanel();
        jLabelKey19 = new javax.swing.JLabel();
        jButtonSelect19 = new javax.swing.JButton();
        jTextFieldFullName19 = new javax.swing.JTextField();
        jTextFieldShortName19 = new javax.swing.JTextField();
        jPanelKey20 = new javax.swing.JPanel();
        jLabelKey20 = new javax.swing.JLabel();
        jButtonSelect20 = new javax.swing.JButton();
        jTextFieldFullName20 = new javax.swing.JTextField();
        jTextFieldShortName20 = new javax.swing.JTextField();
        jPanelKey21 = new javax.swing.JPanel();
        jLabelKey21 = new javax.swing.JLabel();
        jButtonSelect21 = new javax.swing.JButton();
        jTextFieldFullName21 = new javax.swing.JTextField();
        jTextFieldShortName21 = new javax.swing.JTextField();
        jPanelKey22 = new javax.swing.JPanel();
        jLabelKey22 = new javax.swing.JLabel();
        jButtonSelect22 = new javax.swing.JButton();
        jTextFieldFullName22 = new javax.swing.JTextField();
        jTextFieldShortName22 = new javax.swing.JTextField();
        jPanelKey23 = new javax.swing.JPanel();
        jLabelKey23 = new javax.swing.JLabel();
        jButtonSelect23 = new javax.swing.JButton();
        jTextFieldFullName23 = new javax.swing.JTextField();
        jTextFieldShortName23 = new javax.swing.JTextField();
        jPanelKey24 = new javax.swing.JPanel();
        jLabelKey24 = new javax.swing.JLabel();
        jButtonSelect24 = new javax.swing.JButton();
        jTextFieldFullName24 = new javax.swing.JTextField();
        jTextFieldShortName24 = new javax.swing.JTextField();
        jPanelKey25 = new javax.swing.JPanel();
        jLabelKey25 = new javax.swing.JLabel();
        jButtonSelect25 = new javax.swing.JButton();
        jTextFieldFullName25 = new javax.swing.JTextField();
        jTextFieldShortName25 = new javax.swing.JTextField();
        jPanelKey26 = new javax.swing.JPanel();
        jLabelKey26 = new javax.swing.JLabel();
        jButtonSelect26 = new javax.swing.JButton();
        jTextFieldFullName26 = new javax.swing.JTextField();
        jTextFieldShortName26 = new javax.swing.JTextField();
        jPanelKey27 = new javax.swing.JPanel();
        jLabelKey27 = new javax.swing.JLabel();
        jButtonSelect27 = new javax.swing.JButton();
        jTextFieldFullName27 = new javax.swing.JTextField();
        jTextFieldShortName27 = new javax.swing.JTextField();
        jPanelKey28 = new javax.swing.JPanel();
        jLabelKey28 = new javax.swing.JLabel();
        jButtonSelect28 = new javax.swing.JButton();
        jTextFieldFullName28 = new javax.swing.JTextField();
        jTextFieldShortName28 = new javax.swing.JTextField();
        jPanelKey29 = new javax.swing.JPanel();
        jLabelKey29 = new javax.swing.JLabel();
        jButtonSelect29 = new javax.swing.JButton();
        jTextFieldFullName29 = new javax.swing.JTextField();
        jTextFieldShortName29 = new javax.swing.JTextField();
        jPanelKey30 = new javax.swing.JPanel();
        jLabelKey30 = new javax.swing.JLabel();
        jButtonSelect30 = new javax.swing.JButton();
        jTextFieldFullName30 = new javax.swing.JTextField();
        jTextFieldShortName30 = new javax.swing.JTextField();
        jPanelKey31 = new javax.swing.JPanel();
        jLabelKey31 = new javax.swing.JLabel();
        jButtonSelect31 = new javax.swing.JButton();
        jTextFieldFullName31 = new javax.swing.JTextField();
        jTextFieldShortName31 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Brzo biranje");
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

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Brzo biranje");
        jLabel1.setToolTipText("");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Puni naziv");

        jLabel9.setText("Skračeni naziv");

        jLabel10.setText("Puni naziv");

        jLabel11.setText("Skračeni naziv");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(163, 163, 163)
                .addComponent(jLabel8)
                .addGap(130, 130, 130)
                .addComponent(jLabel9)
                .addGap(186, 186, 186)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(46, 46, 46))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel8)
                .addComponent(jLabel9)
                .addComponent(jLabel10)
                .addComponent(jLabel11))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setLayout(new java.awt.GridLayout(16, 2));

        jPanelKey1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey1.setText("Tipka A:");

        jButtonSelect1.setText("Zamijeni A");
        jButtonSelect1.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect1.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName1.setText("Puni naziv");
        jTextFieldFullName1.setEnabled(false);
        jTextFieldFullName1.setFocusable(false);

        jTextFieldShortName1.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey1Layout = new javax.swing.GroupLayout(jPanelKey1);
        jPanelKey1.setLayout(jPanelKey1Layout);
        jPanelKey1Layout.setHorizontalGroup(
            jPanelKey1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey1Layout.setVerticalGroup(
            jPanelKey1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey1)
                .addComponent(jButtonSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey1);

        jPanelKey2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey2.setText("Tipka A:");

        jButtonSelect2.setText("Zamijeni A");
        jButtonSelect2.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect2.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName2.setText("Puni naziv");
        jTextFieldFullName2.setEnabled(false);
        jTextFieldFullName2.setFocusable(false);

        jTextFieldShortName2.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey2Layout = new javax.swing.GroupLayout(jPanelKey2);
        jPanelKey2.setLayout(jPanelKey2Layout);
        jPanelKey2Layout.setHorizontalGroup(
            jPanelKey2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName2, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey2Layout.setVerticalGroup(
            jPanelKey2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey2)
                .addComponent(jButtonSelect2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey2);

        jPanelKey3.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey3.setText("Tipka A:");

        jButtonSelect3.setText("Zamijeni A");
        jButtonSelect3.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect3.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName3.setText("Puni naziv");
        jTextFieldFullName3.setEnabled(false);
        jTextFieldFullName3.setFocusable(false);

        jTextFieldShortName3.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey3Layout = new javax.swing.GroupLayout(jPanelKey3);
        jPanelKey3.setLayout(jPanelKey3Layout);
        jPanelKey3Layout.setHorizontalGroup(
            jPanelKey3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName3, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName3, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey3Layout.setVerticalGroup(
            jPanelKey3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey3)
                .addComponent(jButtonSelect3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey3);

        jPanelKey4.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey4.setText("Tipka A:");

        jButtonSelect4.setText("Zamijeni A");
        jButtonSelect4.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect4.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName4.setText("Puni naziv");
        jTextFieldFullName4.setEnabled(false);
        jTextFieldFullName4.setFocusable(false);

        jTextFieldShortName4.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey4Layout = new javax.swing.GroupLayout(jPanelKey4);
        jPanelKey4.setLayout(jPanelKey4Layout);
        jPanelKey4Layout.setHorizontalGroup(
            jPanelKey4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName4, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName4, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey4Layout.setVerticalGroup(
            jPanelKey4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey4)
                .addComponent(jButtonSelect4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey4);

        jPanelKey5.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey5.setText("Tipka A:");

        jButtonSelect5.setText("Zamijeni A");
        jButtonSelect5.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect5.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName5.setText("Puni naziv");
        jTextFieldFullName5.setEnabled(false);
        jTextFieldFullName5.setFocusable(false);

        jTextFieldShortName5.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey5Layout = new javax.swing.GroupLayout(jPanelKey5);
        jPanelKey5.setLayout(jPanelKey5Layout);
        jPanelKey5Layout.setHorizontalGroup(
            jPanelKey5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName5, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName5, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey5Layout.setVerticalGroup(
            jPanelKey5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey5)
                .addComponent(jButtonSelect5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey5);

        jPanelKey6.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey6.setText("Tipka A:");

        jButtonSelect6.setText("Zamijeni A");
        jButtonSelect6.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect6.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName6.setText("Puni naziv");
        jTextFieldFullName6.setEnabled(false);
        jTextFieldFullName6.setFocusable(false);

        jTextFieldShortName6.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey6Layout = new javax.swing.GroupLayout(jPanelKey6);
        jPanelKey6.setLayout(jPanelKey6Layout);
        jPanelKey6Layout.setHorizontalGroup(
            jPanelKey6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName6, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName6, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey6Layout.setVerticalGroup(
            jPanelKey6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey6)
                .addComponent(jButtonSelect6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey6);

        jPanelKey7.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey7.setText("Tipka A:");

        jButtonSelect7.setText("Zamijeni A");
        jButtonSelect7.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect7.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName7.setText("Puni naziv");
        jTextFieldFullName7.setEnabled(false);
        jTextFieldFullName7.setFocusable(false);

        jTextFieldShortName7.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey7Layout = new javax.swing.GroupLayout(jPanelKey7);
        jPanelKey7.setLayout(jPanelKey7Layout);
        jPanelKey7Layout.setHorizontalGroup(
            jPanelKey7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName7, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName7, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey7Layout.setVerticalGroup(
            jPanelKey7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey7)
                .addComponent(jButtonSelect7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey7);

        jPanelKey8.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey8.setText("Tipka A:");

        jButtonSelect8.setText("Zamijeni A");
        jButtonSelect8.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect8.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName8.setText("Puni naziv");
        jTextFieldFullName8.setEnabled(false);
        jTextFieldFullName8.setFocusable(false);

        jTextFieldShortName8.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey8Layout = new javax.swing.GroupLayout(jPanelKey8);
        jPanelKey8.setLayout(jPanelKey8Layout);
        jPanelKey8Layout.setHorizontalGroup(
            jPanelKey8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName8, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName8, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey8Layout.setVerticalGroup(
            jPanelKey8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey8)
                .addComponent(jButtonSelect8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey8);

        jPanelKey9.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey9.setText("Tipka A:");

        jButtonSelect9.setText("Zamijeni A");
        jButtonSelect9.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect9.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName9.setText("Puni naziv");
        jTextFieldFullName9.setEnabled(false);
        jTextFieldFullName9.setFocusable(false);

        jTextFieldShortName9.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey9Layout = new javax.swing.GroupLayout(jPanelKey9);
        jPanelKey9.setLayout(jPanelKey9Layout);
        jPanelKey9Layout.setHorizontalGroup(
            jPanelKey9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName9, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName9, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey9Layout.setVerticalGroup(
            jPanelKey9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey9)
                .addComponent(jButtonSelect9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey9);

        jPanelKey10.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey10.setText("Tipka A:");

        jButtonSelect10.setText("Zamijeni A");
        jButtonSelect10.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect10.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName10.setText("Puni naziv");
        jTextFieldFullName10.setEnabled(false);
        jTextFieldFullName10.setFocusable(false);

        jTextFieldShortName10.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey10Layout = new javax.swing.GroupLayout(jPanelKey10);
        jPanelKey10.setLayout(jPanelKey10Layout);
        jPanelKey10Layout.setHorizontalGroup(
            jPanelKey10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName10, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName10, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey10Layout.setVerticalGroup(
            jPanelKey10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey10)
                .addComponent(jButtonSelect10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey10);

        jPanelKey11.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey11.setText("Tipka A:");

        jButtonSelect11.setText("Zamijeni A");
        jButtonSelect11.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect11.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName11.setText("Puni naziv");
        jTextFieldFullName11.setEnabled(false);
        jTextFieldFullName11.setFocusable(false);

        jTextFieldShortName11.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey11Layout = new javax.swing.GroupLayout(jPanelKey11);
        jPanelKey11.setLayout(jPanelKey11Layout);
        jPanelKey11Layout.setHorizontalGroup(
            jPanelKey11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName11, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName11, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey11Layout.setVerticalGroup(
            jPanelKey11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey11)
                .addComponent(jButtonSelect11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey11);

        jPanelKey12.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey12.setText("Tipka A:");

        jButtonSelect12.setText("Zamijeni A");
        jButtonSelect12.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect12.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName12.setText("Puni naziv");
        jTextFieldFullName12.setEnabled(false);
        jTextFieldFullName12.setFocusable(false);

        jTextFieldShortName12.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey12Layout = new javax.swing.GroupLayout(jPanelKey12);
        jPanelKey12.setLayout(jPanelKey12Layout);
        jPanelKey12Layout.setHorizontalGroup(
            jPanelKey12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName12, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName12, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey12Layout.setVerticalGroup(
            jPanelKey12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey12)
                .addComponent(jButtonSelect12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey12);

        jPanelKey13.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey13.setText("Tipka A:");

        jButtonSelect13.setText("Zamijeni A");
        jButtonSelect13.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect13.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName13.setText("Puni naziv");
        jTextFieldFullName13.setEnabled(false);
        jTextFieldFullName13.setFocusable(false);

        jTextFieldShortName13.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey13Layout = new javax.swing.GroupLayout(jPanelKey13);
        jPanelKey13.setLayout(jPanelKey13Layout);
        jPanelKey13Layout.setHorizontalGroup(
            jPanelKey13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName13, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName13, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey13Layout.setVerticalGroup(
            jPanelKey13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey13)
                .addComponent(jButtonSelect13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey13);

        jPanelKey14.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey14.setText("Tipka A:");

        jButtonSelect14.setText("Zamijeni A");
        jButtonSelect14.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect14.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName14.setText("Puni naziv");
        jTextFieldFullName14.setEnabled(false);
        jTextFieldFullName14.setFocusable(false);

        jTextFieldShortName14.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey14Layout = new javax.swing.GroupLayout(jPanelKey14);
        jPanelKey14.setLayout(jPanelKey14Layout);
        jPanelKey14Layout.setHorizontalGroup(
            jPanelKey14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName14, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName14, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey14Layout.setVerticalGroup(
            jPanelKey14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey14)
                .addComponent(jButtonSelect14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey14);

        jPanelKey15.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey15.setText("Tipka A:");

        jButtonSelect15.setText("Zamijeni A");
        jButtonSelect15.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect15.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName15.setText("Puni naziv");
        jTextFieldFullName15.setEnabled(false);
        jTextFieldFullName15.setFocusable(false);

        jTextFieldShortName15.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey15Layout = new javax.swing.GroupLayout(jPanelKey15);
        jPanelKey15.setLayout(jPanelKey15Layout);
        jPanelKey15Layout.setHorizontalGroup(
            jPanelKey15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName15, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName15, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey15Layout.setVerticalGroup(
            jPanelKey15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey15)
                .addComponent(jButtonSelect15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey15);

        jPanelKey16.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey16.setText("Tipka A:");

        jButtonSelect16.setText("Zamijeni A");
        jButtonSelect16.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect16.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName16.setText("Puni naziv");
        jTextFieldFullName16.setEnabled(false);
        jTextFieldFullName16.setFocusable(false);

        jTextFieldShortName16.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey16Layout = new javax.swing.GroupLayout(jPanelKey16);
        jPanelKey16.setLayout(jPanelKey16Layout);
        jPanelKey16Layout.setHorizontalGroup(
            jPanelKey16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName16, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName16, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey16Layout.setVerticalGroup(
            jPanelKey16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey16)
                .addComponent(jButtonSelect16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey16);

        jPanelKey17.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey17.setText("Tipka A:");

        jButtonSelect17.setText("Zamijeni A");
        jButtonSelect17.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect17.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName17.setText("Puni naziv");
        jTextFieldFullName17.setEnabled(false);
        jTextFieldFullName17.setFocusable(false);

        jTextFieldShortName17.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey17Layout = new javax.swing.GroupLayout(jPanelKey17);
        jPanelKey17.setLayout(jPanelKey17Layout);
        jPanelKey17Layout.setHorizontalGroup(
            jPanelKey17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName17, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName17, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey17Layout.setVerticalGroup(
            jPanelKey17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey17)
                .addComponent(jButtonSelect17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey17);

        jPanelKey18.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey18.setText("Tipka A:");

        jButtonSelect18.setText("Zamijeni A");
        jButtonSelect18.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect18.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName18.setText("Puni naziv");
        jTextFieldFullName18.setEnabled(false);
        jTextFieldFullName18.setFocusable(false);

        jTextFieldShortName18.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey18Layout = new javax.swing.GroupLayout(jPanelKey18);
        jPanelKey18.setLayout(jPanelKey18Layout);
        jPanelKey18Layout.setHorizontalGroup(
            jPanelKey18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName18, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName18, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey18Layout.setVerticalGroup(
            jPanelKey18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey18)
                .addComponent(jButtonSelect18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey18);

        jPanelKey19.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey19.setText("Tipka A:");

        jButtonSelect19.setText("Zamijeni A");
        jButtonSelect19.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect19.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName19.setText("Puni naziv");
        jTextFieldFullName19.setEnabled(false);
        jTextFieldFullName19.setFocusable(false);

        jTextFieldShortName19.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey19Layout = new javax.swing.GroupLayout(jPanelKey19);
        jPanelKey19.setLayout(jPanelKey19Layout);
        jPanelKey19Layout.setHorizontalGroup(
            jPanelKey19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName19, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName19, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey19Layout.setVerticalGroup(
            jPanelKey19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey19)
                .addComponent(jButtonSelect19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey19);

        jPanelKey20.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey20.setText("Tipka A:");

        jButtonSelect20.setText("Zamijeni A");
        jButtonSelect20.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect20.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName20.setText("Puni naziv");
        jTextFieldFullName20.setEnabled(false);
        jTextFieldFullName20.setFocusable(false);

        jTextFieldShortName20.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey20Layout = new javax.swing.GroupLayout(jPanelKey20);
        jPanelKey20.setLayout(jPanelKey20Layout);
        jPanelKey20Layout.setHorizontalGroup(
            jPanelKey20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName20, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName20, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey20Layout.setVerticalGroup(
            jPanelKey20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey20)
                .addComponent(jButtonSelect20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey20);

        jPanelKey21.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey21.setText("Tipka A:");

        jButtonSelect21.setText("Zamijeni A");
        jButtonSelect21.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect21.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName21.setText("Puni naziv");
        jTextFieldFullName21.setEnabled(false);
        jTextFieldFullName21.setFocusable(false);

        jTextFieldShortName21.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey21Layout = new javax.swing.GroupLayout(jPanelKey21);
        jPanelKey21.setLayout(jPanelKey21Layout);
        jPanelKey21Layout.setHorizontalGroup(
            jPanelKey21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName21, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName21, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey21Layout.setVerticalGroup(
            jPanelKey21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey21)
                .addComponent(jButtonSelect21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey21);

        jPanelKey22.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey22.setText("Tipka A:");

        jButtonSelect22.setText("Zamijeni A");
        jButtonSelect22.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect22.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName22.setText("Puni naziv");
        jTextFieldFullName22.setEnabled(false);
        jTextFieldFullName22.setFocusable(false);

        jTextFieldShortName22.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey22Layout = new javax.swing.GroupLayout(jPanelKey22);
        jPanelKey22.setLayout(jPanelKey22Layout);
        jPanelKey22Layout.setHorizontalGroup(
            jPanelKey22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName22, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName22, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey22Layout.setVerticalGroup(
            jPanelKey22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey22)
                .addComponent(jButtonSelect22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey22);

        jPanelKey23.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey23.setText("Tipka A:");

        jButtonSelect23.setText("Zamijeni A");
        jButtonSelect23.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect23.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName23.setText("Puni naziv");
        jTextFieldFullName23.setEnabled(false);
        jTextFieldFullName23.setFocusable(false);

        jTextFieldShortName23.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey23Layout = new javax.swing.GroupLayout(jPanelKey23);
        jPanelKey23.setLayout(jPanelKey23Layout);
        jPanelKey23Layout.setHorizontalGroup(
            jPanelKey23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName23, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName23, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey23Layout.setVerticalGroup(
            jPanelKey23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey23)
                .addComponent(jButtonSelect23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey23);

        jPanelKey24.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey24.setText("Tipka A:");

        jButtonSelect24.setText("Zamijeni A");
        jButtonSelect24.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect24.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName24.setText("Puni naziv");
        jTextFieldFullName24.setEnabled(false);
        jTextFieldFullName24.setFocusable(false);

        jTextFieldShortName24.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey24Layout = new javax.swing.GroupLayout(jPanelKey24);
        jPanelKey24.setLayout(jPanelKey24Layout);
        jPanelKey24Layout.setHorizontalGroup(
            jPanelKey24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName24, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName24, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey24Layout.setVerticalGroup(
            jPanelKey24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey24)
                .addComponent(jButtonSelect24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey24);

        jPanelKey25.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey25.setText("Tipka A:");

        jButtonSelect25.setText("Zamijeni A");
        jButtonSelect25.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect25.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName25.setText("Puni naziv");
        jTextFieldFullName25.setEnabled(false);
        jTextFieldFullName25.setFocusable(false);

        jTextFieldShortName25.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey25Layout = new javax.swing.GroupLayout(jPanelKey25);
        jPanelKey25.setLayout(jPanelKey25Layout);
        jPanelKey25Layout.setHorizontalGroup(
            jPanelKey25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName25, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName25, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey25Layout.setVerticalGroup(
            jPanelKey25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey25)
                .addComponent(jButtonSelect25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey25);

        jPanelKey26.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey26.setText("Tipka A:");

        jButtonSelect26.setText("Zamijeni A");
        jButtonSelect26.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect26.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName26.setText("Puni naziv");
        jTextFieldFullName26.setEnabled(false);
        jTextFieldFullName26.setFocusable(false);

        jTextFieldShortName26.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey26Layout = new javax.swing.GroupLayout(jPanelKey26);
        jPanelKey26.setLayout(jPanelKey26Layout);
        jPanelKey26Layout.setHorizontalGroup(
            jPanelKey26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName26, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName26, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey26Layout.setVerticalGroup(
            jPanelKey26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey26)
                .addComponent(jButtonSelect26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey26);

        jPanelKey27.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey27.setText("Tipka A:");

        jButtonSelect27.setText("Zamijeni A");
        jButtonSelect27.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect27.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName27.setText("Puni naziv");
        jTextFieldFullName27.setEnabled(false);
        jTextFieldFullName27.setFocusable(false);

        jTextFieldShortName27.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey27Layout = new javax.swing.GroupLayout(jPanelKey27);
        jPanelKey27.setLayout(jPanelKey27Layout);
        jPanelKey27Layout.setHorizontalGroup(
            jPanelKey27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName27, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName27, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey27Layout.setVerticalGroup(
            jPanelKey27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey27)
                .addComponent(jButtonSelect27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey27);

        jPanelKey28.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey28.setText("Tipka A:");

        jButtonSelect28.setText("Zamijeni A");
        jButtonSelect28.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect28.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName28.setText("Puni naziv");
        jTextFieldFullName28.setEnabled(false);
        jTextFieldFullName28.setFocusable(false);

        jTextFieldShortName28.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey28Layout = new javax.swing.GroupLayout(jPanelKey28);
        jPanelKey28.setLayout(jPanelKey28Layout);
        jPanelKey28Layout.setHorizontalGroup(
            jPanelKey28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey28Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName28, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName28, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey28Layout.setVerticalGroup(
            jPanelKey28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey28)
                .addComponent(jButtonSelect28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey28);

        jPanelKey29.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey29.setText("Tipka A:");

        jButtonSelect29.setText("Zamijeni A");
        jButtonSelect29.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect29.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName29.setText("Puni naziv");
        jTextFieldFullName29.setEnabled(false);
        jTextFieldFullName29.setFocusable(false);

        jTextFieldShortName29.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey29Layout = new javax.swing.GroupLayout(jPanelKey29);
        jPanelKey29.setLayout(jPanelKey29Layout);
        jPanelKey29Layout.setHorizontalGroup(
            jPanelKey29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey29Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName29, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName29, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey29Layout.setVerticalGroup(
            jPanelKey29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey29)
                .addComponent(jButtonSelect29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey29);

        jPanelKey30.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey30.setText("Tipka A:");

        jButtonSelect30.setText("Zamijeni A");
        jButtonSelect30.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect30.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName30.setText("Puni naziv");
        jTextFieldFullName30.setEnabled(false);
        jTextFieldFullName30.setFocusable(false);

        jTextFieldShortName30.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey30Layout = new javax.swing.GroupLayout(jPanelKey30);
        jPanelKey30.setLayout(jPanelKey30Layout);
        jPanelKey30Layout.setHorizontalGroup(
            jPanelKey30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey30Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName30, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName30, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey30Layout.setVerticalGroup(
            jPanelKey30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey30)
                .addComponent(jButtonSelect30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey30);

        jPanelKey31.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipka A:"));

        jLabelKey31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelKey31.setText("Tipka A:");

        jButtonSelect31.setText("Zamijeni A");
        jButtonSelect31.setMargin(new java.awt.Insets(2, 8, 2, 8));
        jButtonSelect31.setPreferredSize(new java.awt.Dimension(81, 25));

        jTextFieldFullName31.setText("Puni naziv");
        jTextFieldFullName31.setEnabled(false);
        jTextFieldFullName31.setFocusable(false);

        jTextFieldShortName31.setText("SKRAČENI");

        javax.swing.GroupLayout jPanelKey31Layout = new javax.swing.GroupLayout(jPanelKey31);
        jPanelKey31.setLayout(jPanelKey31Layout);
        jPanelKey31Layout.setHorizontalGroup(
            jPanelKey31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey31Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelKey31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSelect31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFullName31, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldShortName31, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelKey31Layout.setVerticalGroup(
            jPanelKey31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelKey31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelKey31)
                .addComponent(jButtonSelect31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldFullName31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldShortName31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanelKey31);

        jScrollPane1.setViewportView(jPanel1);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(75, 55));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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
                        .addGap(120, 120, 120)
                        .addComponent(jLabel1)
                        .addGap(0, 434, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		String[] itemIdsString = new String[quickPickId.length];
		String[] itemTypesString = new String[quickPickType.length];
		for (int i = 0; i < quickPickId.length; ++i){
			itemIdsString[i] = Integer.toString(quickPickId[i]);
			itemTypesString[i] = Integer.toString(quickPickType[i]);
			quickPickName[i] = jTextFieldShortNameGroup[i].getText().length() > 8 ? jTextFieldShortNameGroup[i].getText().substring(0, 8) : jTextFieldShortNameGroup[i].getText();
			if(quickPickId[i] == -1){
				quickPickName[i] = ""; 
			}
		}
		
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_ID.ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, itemIdsString));
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_NAME.ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, quickPickName));
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE.ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, itemTypesString));

		ClientAppSettings.SaveSettings();
		
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSelect1;
    private javax.swing.JButton jButtonSelect10;
    private javax.swing.JButton jButtonSelect11;
    private javax.swing.JButton jButtonSelect12;
    private javax.swing.JButton jButtonSelect13;
    private javax.swing.JButton jButtonSelect14;
    private javax.swing.JButton jButtonSelect15;
    private javax.swing.JButton jButtonSelect16;
    private javax.swing.JButton jButtonSelect17;
    private javax.swing.JButton jButtonSelect18;
    private javax.swing.JButton jButtonSelect19;
    private javax.swing.JButton jButtonSelect2;
    private javax.swing.JButton jButtonSelect20;
    private javax.swing.JButton jButtonSelect21;
    private javax.swing.JButton jButtonSelect22;
    private javax.swing.JButton jButtonSelect23;
    private javax.swing.JButton jButtonSelect24;
    private javax.swing.JButton jButtonSelect25;
    private javax.swing.JButton jButtonSelect26;
    private javax.swing.JButton jButtonSelect27;
    private javax.swing.JButton jButtonSelect28;
    private javax.swing.JButton jButtonSelect29;
    private javax.swing.JButton jButtonSelect3;
    private javax.swing.JButton jButtonSelect30;
    private javax.swing.JButton jButtonSelect31;
    private javax.swing.JButton jButtonSelect4;
    private javax.swing.JButton jButtonSelect5;
    private javax.swing.JButton jButtonSelect6;
    private javax.swing.JButton jButtonSelect7;
    private javax.swing.JButton jButtonSelect8;
    private javax.swing.JButton jButtonSelect9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelKey1;
    private javax.swing.JLabel jLabelKey10;
    private javax.swing.JLabel jLabelKey11;
    private javax.swing.JLabel jLabelKey12;
    private javax.swing.JLabel jLabelKey13;
    private javax.swing.JLabel jLabelKey14;
    private javax.swing.JLabel jLabelKey15;
    private javax.swing.JLabel jLabelKey16;
    private javax.swing.JLabel jLabelKey17;
    private javax.swing.JLabel jLabelKey18;
    private javax.swing.JLabel jLabelKey19;
    private javax.swing.JLabel jLabelKey2;
    private javax.swing.JLabel jLabelKey20;
    private javax.swing.JLabel jLabelKey21;
    private javax.swing.JLabel jLabelKey22;
    private javax.swing.JLabel jLabelKey23;
    private javax.swing.JLabel jLabelKey24;
    private javax.swing.JLabel jLabelKey25;
    private javax.swing.JLabel jLabelKey26;
    private javax.swing.JLabel jLabelKey27;
    private javax.swing.JLabel jLabelKey28;
    private javax.swing.JLabel jLabelKey29;
    private javax.swing.JLabel jLabelKey3;
    private javax.swing.JLabel jLabelKey30;
    private javax.swing.JLabel jLabelKey31;
    private javax.swing.JLabel jLabelKey4;
    private javax.swing.JLabel jLabelKey5;
    private javax.swing.JLabel jLabelKey6;
    private javax.swing.JLabel jLabelKey7;
    private javax.swing.JLabel jLabelKey8;
    private javax.swing.JLabel jLabelKey9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelKey1;
    private javax.swing.JPanel jPanelKey10;
    private javax.swing.JPanel jPanelKey11;
    private javax.swing.JPanel jPanelKey12;
    private javax.swing.JPanel jPanelKey13;
    private javax.swing.JPanel jPanelKey14;
    private javax.swing.JPanel jPanelKey15;
    private javax.swing.JPanel jPanelKey16;
    private javax.swing.JPanel jPanelKey17;
    private javax.swing.JPanel jPanelKey18;
    private javax.swing.JPanel jPanelKey19;
    private javax.swing.JPanel jPanelKey2;
    private javax.swing.JPanel jPanelKey20;
    private javax.swing.JPanel jPanelKey21;
    private javax.swing.JPanel jPanelKey22;
    private javax.swing.JPanel jPanelKey23;
    private javax.swing.JPanel jPanelKey24;
    private javax.swing.JPanel jPanelKey25;
    private javax.swing.JPanel jPanelKey26;
    private javax.swing.JPanel jPanelKey27;
    private javax.swing.JPanel jPanelKey28;
    private javax.swing.JPanel jPanelKey29;
    private javax.swing.JPanel jPanelKey3;
    private javax.swing.JPanel jPanelKey30;
    private javax.swing.JPanel jPanelKey31;
    private javax.swing.JPanel jPanelKey4;
    private javax.swing.JPanel jPanelKey5;
    private javax.swing.JPanel jPanelKey6;
    private javax.swing.JPanel jPanelKey7;
    private javax.swing.JPanel jPanelKey8;
    private javax.swing.JPanel jPanelKey9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldFullName1;
    private javax.swing.JTextField jTextFieldFullName10;
    private javax.swing.JTextField jTextFieldFullName11;
    private javax.swing.JTextField jTextFieldFullName12;
    private javax.swing.JTextField jTextFieldFullName13;
    private javax.swing.JTextField jTextFieldFullName14;
    private javax.swing.JTextField jTextFieldFullName15;
    private javax.swing.JTextField jTextFieldFullName16;
    private javax.swing.JTextField jTextFieldFullName17;
    private javax.swing.JTextField jTextFieldFullName18;
    private javax.swing.JTextField jTextFieldFullName19;
    private javax.swing.JTextField jTextFieldFullName2;
    private javax.swing.JTextField jTextFieldFullName20;
    private javax.swing.JTextField jTextFieldFullName21;
    private javax.swing.JTextField jTextFieldFullName22;
    private javax.swing.JTextField jTextFieldFullName23;
    private javax.swing.JTextField jTextFieldFullName24;
    private javax.swing.JTextField jTextFieldFullName25;
    private javax.swing.JTextField jTextFieldFullName26;
    private javax.swing.JTextField jTextFieldFullName27;
    private javax.swing.JTextField jTextFieldFullName28;
    private javax.swing.JTextField jTextFieldFullName29;
    private javax.swing.JTextField jTextFieldFullName3;
    private javax.swing.JTextField jTextFieldFullName30;
    private javax.swing.JTextField jTextFieldFullName31;
    private javax.swing.JTextField jTextFieldFullName4;
    private javax.swing.JTextField jTextFieldFullName5;
    private javax.swing.JTextField jTextFieldFullName6;
    private javax.swing.JTextField jTextFieldFullName7;
    private javax.swing.JTextField jTextFieldFullName8;
    private javax.swing.JTextField jTextFieldFullName9;
    private javax.swing.JTextField jTextFieldShortName1;
    private javax.swing.JTextField jTextFieldShortName10;
    private javax.swing.JTextField jTextFieldShortName11;
    private javax.swing.JTextField jTextFieldShortName12;
    private javax.swing.JTextField jTextFieldShortName13;
    private javax.swing.JTextField jTextFieldShortName14;
    private javax.swing.JTextField jTextFieldShortName15;
    private javax.swing.JTextField jTextFieldShortName16;
    private javax.swing.JTextField jTextFieldShortName17;
    private javax.swing.JTextField jTextFieldShortName18;
    private javax.swing.JTextField jTextFieldShortName19;
    private javax.swing.JTextField jTextFieldShortName2;
    private javax.swing.JTextField jTextFieldShortName20;
    private javax.swing.JTextField jTextFieldShortName21;
    private javax.swing.JTextField jTextFieldShortName22;
    private javax.swing.JTextField jTextFieldShortName23;
    private javax.swing.JTextField jTextFieldShortName24;
    private javax.swing.JTextField jTextFieldShortName25;
    private javax.swing.JTextField jTextFieldShortName26;
    private javax.swing.JTextField jTextFieldShortName27;
    private javax.swing.JTextField jTextFieldShortName28;
    private javax.swing.JTextField jTextFieldShortName29;
    private javax.swing.JTextField jTextFieldShortName3;
    private javax.swing.JTextField jTextFieldShortName30;
    private javax.swing.JTextField jTextFieldShortName31;
    private javax.swing.JTextField jTextFieldShortName4;
    private javax.swing.JTextField jTextFieldShortName5;
    private javax.swing.JTextField jTextFieldShortName6;
    private javax.swing.JTextField jTextFieldShortName7;
    private javax.swing.JTextField jTextFieldShortName8;
    private javax.swing.JTextField jTextFieldShortName9;
    // End of variables declaration//GEN-END:variables
}
