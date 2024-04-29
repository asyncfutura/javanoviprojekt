/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.settings;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.ui.ClientAppKeyboardDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.ColorIcon;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsLayoutDialog extends javax.swing.JDialog {
	private String[] groupButtonNames = new String[10];
	private String[][] subgroupButtonNames = new String[10][4];
	private int[][][] itemButtonId = new int[10][4][35];
	private int[][][] itemButtonType = new int[10][4][35];
	private int[][][] itemButtonColor = new int[10][4][35];
	
	private int selectedGroupIndex = 0;
	private int selectedColorPickIndex = 0;
	
	private final JButton[] jButtonListGroup;
	private final JButton[][] jButtonListItems;
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppSettingsLayoutDialog(java.awt.Frame parent, boolean modal) {
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

		// Init
		jButtonListGroup = new JButton[]{
			jButton1, jButton2, jButton3, jButton4, jButton5, 
			jButton6, jButton7, jButton8, jButton9, jButton10,
		};
		jButtonListItems = new JButton[][]{
			new JButton[]{
				jButton11, jButton12, jButton13, jButton14, jButton15, jButton16, jButton17, jButton18, jButton19, jButton20, 
				jButton21, jButton22, jButton23, jButton24, jButton25, jButton26, jButton27, jButton28, jButton29, jButton30, 
				jButton31, jButton32, jButton33, jButton34, jButton35, jButton36, jButton37, jButton38, jButton39, jButton40, 
				jButton41, jButton42, jButton43, jButton44, jButton45 },
			new JButton[]{
				jButton46, jButton47, jButton48, jButton49, jButton50, 
				jButton51, jButton52, jButton53, jButton54, jButton55, jButton56, jButton57, jButton58, jButton59, jButton60, 
				jButton61, jButton62, jButton63, jButton64, jButton65, jButton66, jButton67, jButton68, jButton69, jButton70, 
				jButton71, jButton72, jButton73, jButton74, jButton75, jButton76, jButton77, jButton78, jButton79, jButton80 },
			new JButton[]{ 
				jButton81, jButton82, jButton83, jButton84, jButton85, jButton86, jButton87, jButton88, jButton89, jButton90, 
				jButton91, jButton92, jButton93, jButton94, jButton95, jButton96, jButton97, jButton98, jButton99, jButton100, 
				jButton101, jButton102, jButton103, jButton104, jButton105, jButton106, jButton107, jButton108, jButton109, jButton110, 
				jButton111, jButton112, jButton113, jButton114, jButton115 },
			new JButton[]{
				jButton116, jButton117, jButton118, jButton119, jButton120, 
				jButton121, jButton122, jButton123, jButton124, jButton125, jButton126, jButton127, jButton128, jButton129, jButton130, 
				jButton131, jButton132, jButton133, jButton134, jButton135, jButton136, jButton137, jButton138, jButton139, jButton140, 
				jButton141, jButton142, jButton143, jButton144, jButton145, jButton146, jButton147, jButton148, jButton149, jButton150, 
			}
		};
		
		for (int i = 0; i < jButtonListGroup.length; ++i){
			final int index = i;
			jButtonListGroup[i].addActionListener((java.awt.event.ActionEvent evt) -> {
				jButtonGroupActionPerformed(index);
			});
		}
		
		for (int i = 0; i < jButtonListItems.length; ++i){
				for (int j = 0; j < jButtonListItems[i].length; ++j){
				final int index = j;
				jButtonListItems[i][j].addActionListener((java.awt.event.ActionEvent evt) -> {
					jButtonItemActionPerformed(index);
				});
			}
		}
		
		// Load settings
		ClientAppSettings.LoadSettings();
		groupButtonNames = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_LAYOUT_GROUP_NAMES.ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
		for (int i = 0; i < jButtonListGroup.length; ++i){
			subgroupButtonNames[i] = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_SUBGROUP_NAMES[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemIdsString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_IDS[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemTypesString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_TYPES[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			String[] itemColorsString = ClientAppSettings.GetString(Values.SETTINGS_LAYOUT_ITEM_COLORS[i].ordinal()).split(Values.SETTINGS_LAYOUT_SPLIT_STRING);
			for (int j = 0; j < jButtonListItems.length; ++j){
				for (int k = 0; k < jButtonListItems[j].length; ++k){
					int itemId = -1;
					int itemType = -1;
					int itemColor = 0;
					try {
						itemId = Integer.parseInt(itemIdsString[j * 35 + k]);
						itemType = Integer.parseInt(itemTypesString[j * 35 + k]);
						itemColor = Integer.parseInt(itemColorsString[j * 35 + k]);
					} catch (NumberFormatException ex) {}
					
					itemButtonId[i][j][k] = itemId;
					itemButtonType[i][j][k] = itemType;
					itemButtonColor[i][j][k] = itemColor;
				}
			}
		}
		
		// Apply settings
		for (int i = 0; i < jButtonListGroup.length; ++i){
			ClientAppUtils.SetTouchLayoutGroupButtonText(jButtonListGroup[i], groupButtonNames[i]);
		}

		for (int i = 0; i < jTabbedPane1.getTabCount(); ++i){
			ClientAppUtils.SetTouchLayoutTabTitleText(jTabbedPane1, i, subgroupButtonNames[selectedGroupIndex][i]);
		}
		
		// Refresh items
		jTabbedPane1StateChanged(null);
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void jButtonGroupActionPerformed(int index) {                                         
		selectedGroupIndex = index;
		jLabelSelectedGroupName.setText(groupButtonNames[selectedGroupIndex]);
		for (int i = 0; i < jTabbedPane1.getTabCount(); ++i){
			ClientAppUtils.SetTouchLayoutTabTitleText(jTabbedPane1, i, subgroupButtonNames[selectedGroupIndex][i]);
		}
		if(jTabbedPane1.getSelectedIndex() != 0){
			jTabbedPane1.setSelectedIndex(0);
		} else {
			jTabbedPane1StateChanged(null);
		}
    }
	
	private void jButtonItemActionPerformed(int index) {
		int tabIndex = jTabbedPane1.getSelectedIndex();
		int itemId = itemButtonId[selectedGroupIndex][tabIndex][index];
		int itemTypeId = itemButtonType[selectedGroupIndex][tabIndex][index];
		int itemColorId = itemButtonColor[selectedGroupIndex][tabIndex][index];
		
		if(jRadioButtonEdit.isSelected()){
			ClientAppSettingsLayoutItemEditDialog addEditdialog = new ClientAppSettingsLayoutItemEditDialog(null, true, itemId, itemTypeId, itemColorId);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				itemButtonId[selectedGroupIndex][tabIndex][index] = addEditdialog.finalItemId;
				itemButtonType[selectedGroupIndex][tabIndex][index] = addEditdialog.finalItemType;
				itemButtonColor[selectedGroupIndex][tabIndex][index] = addEditdialog.finalItemColor;
				ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][index], addEditdialog.finalItemName);
				jButtonListItems[tabIndex][index].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[addEditdialog.finalItemColor], jButtonListItems[tabIndex][index].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][index].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
			}
		} else if (jRadioButtonDelete.isSelected()){
			itemButtonId[selectedGroupIndex][tabIndex][index] = -1;
			itemButtonType[selectedGroupIndex][tabIndex][index] = 0;
			itemButtonColor[selectedGroupIndex][tabIndex][index] = 0;
			ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][index], "");
			jButtonListItems[tabIndex][index].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[0], jButtonListItems[tabIndex][index].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][index].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		} else if (jRadioButtonColor.isSelected()){
			itemButtonColor[selectedGroupIndex][tabIndex][index] = selectedColorPickIndex;
			jButtonListItems[tabIndex][index].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[selectedColorPickIndex], jButtonListItems[tabIndex][index].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][index].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		} else if (jRadioButtonPickArticle.isSelected()){
			ClientAppSelectArticleDialog selectDialog = new ClientAppSelectArticleDialog(null, true);
			selectDialog.setVisible(true);
			if(selectDialog.selectedId != -1){
				itemButtonId[selectedGroupIndex][tabIndex][index] = selectDialog.selectedId;
				itemButtonType[selectedGroupIndex][tabIndex][index] = Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE;
				ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][index], selectDialog.selectedName);
			}
		} else if (jRadioButtonPickService.isSelected()){
			ClientAppSelectServiceDialog selectDialog = new ClientAppSelectServiceDialog(null, true);
			selectDialog.setVisible(true);
			if(selectDialog.selectedId != -1){
				itemButtonId[selectedGroupIndex][tabIndex][index] = selectDialog.selectedId;
				itemButtonType[selectedGroupIndex][tabIndex][index] = Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE;
				ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][index], selectDialog.selectedName);
			}
		} else if (jRadioButtonPickTradingGoods.isSelected()){
			ClientAppSelectTradingGoodsDialog selectDialog = new ClientAppSelectTradingGoodsDialog(null, true, ClientAppSettings.currentYear);
			selectDialog.setVisible(true);
			if(selectDialog.selectedId != -1){
				itemButtonId[selectedGroupIndex][tabIndex][index] = selectDialog.selectedId;
				itemButtonType[selectedGroupIndex][tabIndex][index] = Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
				ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][index], selectDialog.selectedName);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
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
        jPanel3 = new javax.swing.JPanel();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton95 = new javax.swing.JButton();
        jButton96 = new javax.swing.JButton();
        jButton97 = new javax.swing.JButton();
        jButton98 = new javax.swing.JButton();
        jButton99 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jButton101 = new javax.swing.JButton();
        jButton102 = new javax.swing.JButton();
        jButton103 = new javax.swing.JButton();
        jButton104 = new javax.swing.JButton();
        jButton105 = new javax.swing.JButton();
        jButton106 = new javax.swing.JButton();
        jButton107 = new javax.swing.JButton();
        jButton108 = new javax.swing.JButton();
        jButton109 = new javax.swing.JButton();
        jButton110 = new javax.swing.JButton();
        jButton111 = new javax.swing.JButton();
        jButton112 = new javax.swing.JButton();
        jButton113 = new javax.swing.JButton();
        jButton114 = new javax.swing.JButton();
        jButton115 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton116 = new javax.swing.JButton();
        jButton117 = new javax.swing.JButton();
        jButton118 = new javax.swing.JButton();
        jButton119 = new javax.swing.JButton();
        jButton120 = new javax.swing.JButton();
        jButton121 = new javax.swing.JButton();
        jButton122 = new javax.swing.JButton();
        jButton123 = new javax.swing.JButton();
        jButton124 = new javax.swing.JButton();
        jButton125 = new javax.swing.JButton();
        jButton126 = new javax.swing.JButton();
        jButton127 = new javax.swing.JButton();
        jButton128 = new javax.swing.JButton();
        jButton129 = new javax.swing.JButton();
        jButton130 = new javax.swing.JButton();
        jButton131 = new javax.swing.JButton();
        jButton132 = new javax.swing.JButton();
        jButton133 = new javax.swing.JButton();
        jButton134 = new javax.swing.JButton();
        jButton135 = new javax.swing.JButton();
        jButton136 = new javax.swing.JButton();
        jButton137 = new javax.swing.JButton();
        jButton138 = new javax.swing.JButton();
        jButton139 = new javax.swing.JButton();
        jButton140 = new javax.swing.JButton();
        jButton141 = new javax.swing.JButton();
        jButton142 = new javax.swing.JButton();
        jButton143 = new javax.swing.JButton();
        jButton144 = new javax.swing.JButton();
        jButton145 = new javax.swing.JButton();
        jButton146 = new javax.swing.JButton();
        jButton147 = new javax.swing.JButton();
        jButton148 = new javax.swing.JButton();
        jButton149 = new javax.swing.JButton();
        jButton150 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonChangeGroupName = new javax.swing.JButton();
        jButtonChangeSubgroupName = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabelSelectedGroupName = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jRadioButtonEdit = new javax.swing.JRadioButton();
        jRadioButtonColor = new javax.swing.JRadioButton();
        jRadioButtonPickArticle = new javax.swing.JRadioButton();
        jRadioButtonDelete = new javax.swing.JRadioButton();
        jRadioButtonPickService = new javax.swing.JRadioButton();
        jRadioButtonPickTradingGoods = new javax.swing.JRadioButton();
        jButtonColor = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Touch screen raspored");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Touch screen raspored");

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

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder("Grupe"));

        jButton1.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton1.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton1.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton2.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton2.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton2.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton3.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton3.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton3.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton4.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton4.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton5.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton5.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton5.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton6.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton6.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton6.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton7.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton7.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton7.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton8.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton8.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton8.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton9.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton9.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton9.setPreferredSize(new java.awt.Dimension(70, 70));

        jButton10.setText("<html> <div style=\"text-align: center\"> Grupa 1 </div> </html>");
        jButton10.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButton10.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton11);

        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton12);

        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton13);

        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton14);

        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton15);

        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton16);

        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton17);

        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton18);

        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton19);

        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton20);

        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton21);

        jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton22.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton22);

        jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton23.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton23);

        jButton24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton24.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton24);

        jButton25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton25.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton25);

        jButton26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton26.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton26);

        jButton27.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton27.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton27);

        jButton28.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton28.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton28);

        jButton29.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton29.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton29);

        jButton30.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton30.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton30);

        jButton31.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton31.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton31);

        jButton32.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton32.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton32);

        jButton33.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton33.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton33);

        jButton34.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton34.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton34);

        jButton35.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton35.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton35);

        jButton36.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton36.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton36);

        jButton37.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton37.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton37);

        jButton38.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton38.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton38);

        jButton39.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton39.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton39);

        jButton40.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton40.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton40);

        jButton41.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton41.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton41);

        jButton42.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton42.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton42);

        jButton43.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton43.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton43);

        jButton44.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton44.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton44);

        jButton45.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton45.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel1.add(jButton45);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel1);

        jPanel3.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton46.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton46.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton46);

        jButton47.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton47.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton47);

        jButton48.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton48.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton48);

        jButton49.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton49.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton49);

        jButton50.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton50.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton50);

        jButton51.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton51.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton51);

        jButton52.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton52.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton52);

        jButton53.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton53.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton53);

        jButton54.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton54.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton54);

        jButton55.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton55.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton55);

        jButton56.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton56.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton56);

        jButton57.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton57.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton57);

        jButton58.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton58.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton58);

        jButton59.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton59.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton59);

        jButton60.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton60.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton60);

        jButton61.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton61.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton61);

        jButton62.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton62.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton62);

        jButton63.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton63.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton63);

        jButton64.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton64.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton64);

        jButton65.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton65.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton65);

        jButton66.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton66.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton66);

        jButton67.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton67.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton67);

        jButton68.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton68.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton68);

        jButton69.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton69.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton69);

        jButton70.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton70.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton70);

        jButton71.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton71.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton71);

        jButton72.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton72.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton72);

        jButton73.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton73.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton73);

        jButton74.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton74.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton74);

        jButton75.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton75.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton75);

        jButton76.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton76.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton76);

        jButton77.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton77.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton77);

        jButton78.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton78.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton78);

        jButton79.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton79.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton79);

        jButton80.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton80.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel3.add(jButton80);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel3);

        jPanel4.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton81.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton81.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton81);

        jButton82.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton82.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton82);

        jButton83.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton83.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton83);

        jButton84.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton84.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton84);

        jButton85.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton85.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton85);

        jButton86.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton86.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton86);

        jButton87.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton87.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton87);

        jButton88.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton88.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton88);

        jButton89.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton89.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton89);

        jButton90.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton90.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton90);

        jButton91.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton91.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton91);

        jButton92.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton92.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton92);

        jButton93.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton93.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton93);

        jButton94.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton94.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton94);

        jButton95.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton95.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton95);

        jButton96.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton96.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton96);

        jButton97.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton97.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton97);

        jButton98.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton98.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton98);

        jButton99.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton99.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton99);

        jButton100.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton100.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton100);

        jButton101.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton101.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton101);

        jButton102.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton102.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton102);

        jButton103.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton103.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton103);

        jButton104.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton104.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton104);

        jButton105.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton105.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton105);

        jButton106.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton106.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton106);

        jButton107.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton107.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton107);

        jButton108.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton108.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton108);

        jButton109.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton109.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton109);

        jButton110.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton110.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton110);

        jButton111.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton111.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton111);

        jButton112.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton112.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton112);

        jButton113.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton113.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton113);

        jButton114.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton114.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton114);

        jButton115.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton115.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel4.add(jButton115);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel4);

        jPanel5.setLayout(new java.awt.GridLayout(7, 5, 7, 7));

        jButton116.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton116.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton116);

        jButton117.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton117.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton117);

        jButton118.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton118.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton118);

        jButton119.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton119.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton119);

        jButton120.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton120.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton120);

        jButton121.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton121.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton121);

        jButton122.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton122.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton122);

        jButton123.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton123.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton123);

        jButton124.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton124.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton124);

        jButton125.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton125.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton125);

        jButton126.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton126.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton126);

        jButton127.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton127.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton127);

        jButton128.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton128.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton128);

        jButton129.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton129.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton129);

        jButton130.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton130.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton130);

        jButton131.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton131.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton131);

        jButton132.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton132.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton132);

        jButton133.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton133.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton133);

        jButton134.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton134.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton134);

        jButton135.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton135.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton135);

        jButton136.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton136.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton136);

        jButton137.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton137.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton137);

        jButton138.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton138.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton138);

        jButton139.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton139.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton139);

        jButton140.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton140.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton140);

        jButton141.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton141.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton141);

        jButton142.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton142.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton142);

        jButton143.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton143.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton143);

        jButton144.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton144.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton144);

        jButton145.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton145.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton145);

        jButton146.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton146.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton146);

        jButton147.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton147.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton147);

        jButton148.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton148.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton148);

        jButton149.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton149.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton149);

        jButton150.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton150.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel5.add(jButton150);

        jTabbedPane1.addTab("<html> <div width = \"75\"  align = \"center\"> <br> Podgrupa  1 <br><br> </div> </html>", jPanel5);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Promjena imena"));

        jButtonChangeGroupName.setText("<html> <div style=\"text-align: center\"> Promijeni  ime grupe <br> </div> </html>");
        jButtonChangeGroupName.setPreferredSize(new java.awt.Dimension(110, 65));
        jButtonChangeGroupName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeGroupNameActionPerformed(evt);
            }
        });

        jButtonChangeSubgroupName.setText("<html> <div style=\"text-align: center\"> Promijeni ime podgrupe  </div> </html>");
        jButtonChangeSubgroupName.setPreferredSize(new java.awt.Dimension(110, 65));
        jButtonChangeSubgroupName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeSubgroupNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonChangeGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonChangeSubgroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonChangeGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonChangeSubgroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Trenutno odabrana grupa"));

        jLabelSelectedGroupName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelSelectedGroupName.setText("Grupa 1");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelSelectedGroupName)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelSelectedGroupName)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonSave.setText("<html> <div style=\"text-align: center\"> Spremi <br> [F8] </div> </html>");
        jButtonSave.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(80, 70));
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
                .addContainerGap()
                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Opcije odabira polja"));
        jPanel8.setToolTipText("Odaberite opciju, a zatim kliknite na željeno polje");

        buttonGroup1.add(jRadioButtonEdit);
        jRadioButtonEdit.setSelected(true);
        jRadioButtonEdit.setText("Uredi");

        buttonGroup1.add(jRadioButtonColor);
        jRadioButtonColor.setText("Oboji");
        jRadioButtonColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonColorActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonPickArticle);
        jRadioButtonPickArticle.setText("Odaberi artikl");

        buttonGroup1.add(jRadioButtonDelete);
        jRadioButtonDelete.setText("Obriši");

        buttonGroup1.add(jRadioButtonPickService);
        jRadioButtonPickService.setText("Odaberi uslugu");

        buttonGroup1.add(jRadioButtonPickTradingGoods);
        jRadioButtonPickTradingGoods.setText("Odaberi trg. robu");

        jButtonColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jRadioButtonDelete)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jRadioButtonColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonColor, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jRadioButtonEdit)
                            .addComponent(jRadioButtonPickTradingGoods)
                            .addComponent(jRadioButtonPickService)
                            .addComponent(jRadioButtonPickArticle))
                        .addContainerGap(18, Short.MAX_VALUE))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonColor)
                    .addComponent(jButtonColor, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonPickArticle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonPickService)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonPickTradingGoods)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonDelete)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(69, 69, 69)
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(42, 42, 42)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(34, 34, 34))
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite izaći? Unesene promjene neće biti spremljene!", "Izađi bez spremanja promjena", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			Utils.DisposeDialog(this);
		}
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		ClientAppSettings.SetString(Values.AppSettingsEnum.SETTINGS_LAYOUT_GROUP_NAMES.ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, groupButtonNames));
		for (int i = 0; i < jButtonListGroup.length; ++i){
			ClientAppSettings.SetString(Values.SETTINGS_LAYOUT_SUBGROUP_NAMES[i].ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, subgroupButtonNames[i]));
			String[] itemIdsString = new String[4 * 35];
			String[] itemTypesString = new String[4 * 35];
			String[] itemColorsString = new String[4 * 35];
			for (int j = 0; j < jButtonListItems.length; ++j){
				for (int k = 0; k < jButtonListItems[j].length; ++k){
					itemIdsString[j * 35 + k] = Integer.toString(itemButtonId[i][j][k]);
					itemTypesString[j * 35 + k] = Integer.toString(itemButtonType[i][j][k]);
					itemColorsString[j * 35 + k] = Integer.toString(itemButtonColor[i][j][k]);
				}
			}
			ClientAppSettings.SetString(Values.SETTINGS_LAYOUT_ITEM_IDS[i].ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, itemIdsString));
			ClientAppSettings.SetString(Values.SETTINGS_LAYOUT_ITEM_TYPES[i].ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, itemTypesString));
			ClientAppSettings.SetString(Values.SETTINGS_LAYOUT_ITEM_COLORS[i].ordinal(), String.join(Values.SETTINGS_LAYOUT_SPLIT_STRING, itemColorsString));
		}
		
		ClientAppSettings.SaveSettings();
		
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonChangeGroupNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeGroupNameActionPerformed
		ClientAppKeyboardDialog keyboardDialog = new ClientAppKeyboardDialog(null, true, "Uredi naziv grupe", groupButtonNames[selectedGroupIndex], 15);
        keyboardDialog.setVisible(true);
		String newName = keyboardDialog.enteredText;
		if(newName.length() > 15){
			newName = newName.substring(0, 15);
		}
        if(!"".equals(newName)){
			groupButtonNames[selectedGroupIndex] = newName;
			ClientAppUtils.SetTouchLayoutGroupButtonText(jButtonListGroup[selectedGroupIndex], newName);
			jLabelSelectedGroupName.setText(newName);
        }
    }//GEN-LAST:event_jButtonChangeGroupNameActionPerformed

    private void jButtonChangeSubgroupNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeSubgroupNameActionPerformed
		int selectedTabIndex = jTabbedPane1.getSelectedIndex();
		if(selectedTabIndex == -1){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite podgrupu koju želite urediti.");
			return;
		}
		
		ClientAppKeyboardDialog keyboardDialog = new ClientAppKeyboardDialog(null, true, "Uredi naziv podgrupe", subgroupButtonNames[selectedGroupIndex][selectedTabIndex], 15);
        keyboardDialog.setVisible(true);
		String newName = keyboardDialog.enteredText;
		if(newName.length() > 15){
			newName = newName.substring(0, 15);
		}
        if(!"".equals(newName)){
			subgroupButtonNames[selectedGroupIndex][selectedTabIndex] = newName;
			ClientAppUtils.SetTouchLayoutTabTitleText(jTabbedPane1, selectedTabIndex, newName);
        }
    }//GEN-LAST:event_jButtonChangeSubgroupNameActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		if(jButtonListItems == null){
			return;
		}

		// Revert buttons
		for (int i = 0; i < jButtonListItems.length; ++i){
			for (int j = 0; j < jButtonListItems[i].length; ++j){
				jButtonListItems[i][j].setText("");
				jButtonListItems[i][j].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[0], jButtonListItems[i][j].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[i][j].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
			}
		}
		
		// Load new values
		ArrayList<Integer> itemIdList = new ArrayList<>();
		ArrayList<Integer> itemTypeList = new ArrayList<>();
		ArrayList<Integer> buttonIdList = new ArrayList<>();
		int tabIndex = jTabbedPane1.getSelectedIndex();
		for (int i = 0; i < itemButtonId[selectedGroupIndex][tabIndex].length; ++i){
			int itemIndex = itemButtonId[selectedGroupIndex][tabIndex][i];
			int itemType = itemButtonType[selectedGroupIndex][tabIndex][i];
			if(itemIndex != -1){
				itemIdList.add(itemIndex);
				itemTypeList.add(itemType);
				buttonIdList.add(i);
			}
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(itemIdList.size());
		for (int i = 0; i < itemIdList.size(); ++i){
			if(itemTypeList.get(i) == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
				multiDatabaseQuery.SetQuery(i, "SELECT NAME FROM ARTICLES WHERE ARTICLES.ID = ? AND ARTICLES.IS_DELETED = 0");
			} else if(itemTypeList.get(i) == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS){
				multiDatabaseQuery.SetQuery(i, "SELECT NAME FROM TRADING_GOODS WHERE TRADING_GOODS.ID = ? AND TRADING_GOODS.IS_DELETED = 0");
			} else if(itemTypeList.get(i) == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
				multiDatabaseQuery.SetQuery(i, "SELECT NAME FROM SERVICES WHERE SERVICES.ID = ? AND SERVICES.IS_DELETED = 0");
			}
			multiDatabaseQuery.AddParam(i, 1, itemIdList.get(i));
		}
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
					for(int i = 0; i < multiDatabaseQueryResponse.databaseQueryResult.length; ++i){
						if (multiDatabaseQueryResponse.databaseQueryResult[i].next()) {
							String text = multiDatabaseQueryResponse.databaseQueryResult[i].getString(0);
							ClientAppUtils.SetTouchLayoutItemButtonText(jButtonListItems[tabIndex][buttonIdList.get(i)], text);
							int colorIndex = itemButtonColor[selectedGroupIndex][tabIndex][buttonIdList.get(i)];
							jButtonListItems[tabIndex][buttonIdList.get(i)].setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[colorIndex], jButtonListItems[tabIndex][buttonIdList.get(i)].getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonListItems[tabIndex][buttonIdList.get(i)].getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
						} else {
							itemButtonId[selectedGroupIndex][tabIndex][buttonIdList.get(i)] = -1;
						}
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jRadioButtonColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonColorActionPerformed
		jButtonColor.doClick();
    }//GEN-LAST:event_jRadioButtonColorActionPerformed

    private void jButtonColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColorActionPerformed
		ClientAppSettingsLayoutItemColorPickDialog addEditdialog = new ClientAppSettingsLayoutItemColorPickDialog(null, true);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			selectedColorPickIndex = addEditdialog.finalItemColor;
			jButtonColor.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[selectedColorPickIndex], jButtonColor.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		}
    }//GEN-LAST:event_jButtonColorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton101;
    private javax.swing.JButton jButton102;
    private javax.swing.JButton jButton103;
    private javax.swing.JButton jButton104;
    private javax.swing.JButton jButton105;
    private javax.swing.JButton jButton106;
    private javax.swing.JButton jButton107;
    private javax.swing.JButton jButton108;
    private javax.swing.JButton jButton109;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton110;
    private javax.swing.JButton jButton111;
    private javax.swing.JButton jButton112;
    private javax.swing.JButton jButton113;
    private javax.swing.JButton jButton114;
    private javax.swing.JButton jButton115;
    private javax.swing.JButton jButton116;
    private javax.swing.JButton jButton117;
    private javax.swing.JButton jButton118;
    private javax.swing.JButton jButton119;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton120;
    private javax.swing.JButton jButton121;
    private javax.swing.JButton jButton122;
    private javax.swing.JButton jButton123;
    private javax.swing.JButton jButton124;
    private javax.swing.JButton jButton125;
    private javax.swing.JButton jButton126;
    private javax.swing.JButton jButton127;
    private javax.swing.JButton jButton128;
    private javax.swing.JButton jButton129;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton130;
    private javax.swing.JButton jButton131;
    private javax.swing.JButton jButton132;
    private javax.swing.JButton jButton133;
    private javax.swing.JButton jButton134;
    private javax.swing.JButton jButton135;
    private javax.swing.JButton jButton136;
    private javax.swing.JButton jButton137;
    private javax.swing.JButton jButton138;
    private javax.swing.JButton jButton139;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton140;
    private javax.swing.JButton jButton141;
    private javax.swing.JButton jButton142;
    private javax.swing.JButton jButton143;
    private javax.swing.JButton jButton144;
    private javax.swing.JButton jButton145;
    private javax.swing.JButton jButton146;
    private javax.swing.JButton jButton147;
    private javax.swing.JButton jButton148;
    private javax.swing.JButton jButton149;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton150;
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
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButton95;
    private javax.swing.JButton jButton96;
    private javax.swing.JButton jButton97;
    private javax.swing.JButton jButton98;
    private javax.swing.JButton jButton99;
    private javax.swing.JButton jButtonChangeGroupName;
    private javax.swing.JButton jButtonChangeSubgroupName;
    private javax.swing.JButton jButtonColor;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelSelectedGroupName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JRadioButton jRadioButtonColor;
    private javax.swing.JRadioButton jRadioButtonDelete;
    private javax.swing.JRadioButton jRadioButtonEdit;
    private javax.swing.JRadioButton jRadioButtonPickArticle;
    private javax.swing.JRadioButton jRadioButtonPickService;
    private javax.swing.JRadioButton jRadioButtonPickTradingGoods;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
