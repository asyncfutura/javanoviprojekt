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
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.client.ui.receipts.ClientAppSelectMaterialDialog;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseNormativesDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableArticlesIdList = new ArrayList<>();
	private ArrayList<Float> tableArticlesTaxList = new ArrayList<>();
	private ArrayList<Integer> tableNormativesIdList = new ArrayList<>();
	private ArrayList<Integer> tableNormativeMaterialIdList = new ArrayList<>();

	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppWarehouseNormativesDialog(java.awt.Frame parent, boolean modal) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						jButtonEdit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDelete.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPos.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F6){
						jButtonPrintPosAll.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonPrintA4All.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonNormativesPerMaterial.doClick();
					}
				}
				
				return false;
			}
		});
		
		jTableNormatives.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEdit.doClick();
				}
			}
		});
		
		jTableNormatives.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableNormatives.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableNormatives.getTableHeader().setReorderingAllowed(false);
		jTableNormatives.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableArticles.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableArticles.getTableHeader().setReorderingAllowed(false);
		jTableArticles.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		CustomTableModel customTableModel = new CustomTableModel();
		customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Mj. jed", "Cijena"});
		jTableNormatives.setModel(customTableModel);
		jTableNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
		jTableNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneNormatives.getWidth() * 50 / 100);
		jTableNormatives.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
		jTableNormatives.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneNormatives.getWidth() * 15 / 100);
		jTableNormatives.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneNormatives.getWidth() * 15 / 100);

		RefreshTableArticles();
		
		jTableArticles.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || jTableArticles.getSelectedRow() == -1)
					return;
				
				int rowId = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
				int tableId = tableArticlesIdList.get(rowId);
				float taxRate = tableArticlesTaxList.get(rowId);
				RefreshTableNormatives(tableId, taxRate);
			}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTableArticles(){
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ARTICLES.ID, ARTICLES.NAME, ARTICLES.CUSTOM_ID, TAX_RATES.VALUE "
				+ "FROM ARTICLES "
				+ "INNER JOIN TAX_RATES ON ARTICLES.TAX_RATE_ID = TAX_RATES.ID "
				+ "WHERE ARTICLES.IS_DELETED = 0";
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
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Float> taxList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[2];
						rowData[0] = customIdEnabled ? databaseQueryResult.getString(2) : databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
						taxList.add(databaseQueryResult.getFloat(3));
					}
					jTableArticles.setModel(customTableModel);
					tableArticlesIdList = idList;
					tableArticlesTaxList = taxList;
					
					jTableArticles.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneNormatives.getWidth() * 20 / 100);
					jTableArticles.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneNormatives.getWidth() * 80 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableNormatives(int articleId, float articleTaxRate){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT NORMATIVES.ID, MATERIALS.ID, MATERIALS.NAME, NORMATIVES.AMOUNT, MEASURING_UNITS.NAME, MATERIALS.LAST_PRICE * NORMATIVES.AMOUNT "
				+ "FROM ((NORMATIVES INNER JOIN MATERIALS ON NORMATIVES.MATERIAL_ID = MATERIALS.ID) "
				+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID) "
				+ "WHERE NORMATIVES.IS_DELETED = 0 AND MATERIALS.IS_DELETED = 0 AND NORMATIVES.ARTICLE_ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, articleId);
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
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Mj. jed", "Cijena"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Integer> idListMaterial = new ArrayList<>();
					float totalPrice = 0f;
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = databaseQueryResult.getString(1);
						rowData[1] = databaseQueryResult.getString(2);
						rowData[2] = ClientAppUtils.FloatToStringNoLimit(databaseQueryResult.getFloat(3));
						rowData[3] = databaseQueryResult.getString(4);
						float price = databaseQueryResult.getFloat(5) + databaseQueryResult.getFloat(5) * articleTaxRate / 100f;
						rowData[4] = ClientAppUtils.FloatToPriceString(price);
						totalPrice += price;
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
						idListMaterial.add(databaseQueryResult.getInt(1));
					}
					jTableNormatives.setModel(customTableModel);
					tableNormativesIdList = idList;
					tableNormativeMaterialIdList = idListMaterial;
					jLabelArticlePrice.setText(ClientAppUtils.FloatToPriceString(totalPrice));
					
					jTableNormatives.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
					jTableNormatives.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneNormatives.getWidth() * 50 / 100);
					jTableNormatives.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneNormatives.getWidth() * 10 / 100);
					jTableNormatives.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneNormatives.getWidth() * 15 / 100);
					jTableNormatives.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneNormatives.getWidth() * 15 / 100);
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
        jPanel2 = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonNormativesPerMaterial = new javax.swing.JButton();
        jButtonPrintPosAll = new javax.swing.JButton();
        jButtonPrintA4All = new javax.swing.JButton();
        jScrollPaneNormatives = new javax.swing.JScrollPane();
        jTableNormatives = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabelArticlePrice = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPaneArticles = new javax.swing.JScrollPane();
        jTableArticles = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Normativi");
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
        jLabel9.setText("Normativi");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Materijali"));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAdd.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAdd.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEdit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonEdit.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDelete.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonDelete.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonNormativesPerMaterial.setText("<html> <div style=\"text-align: center\"> Prikaz po <br> materijalu <br> [F8] </div> </html>");
        jButtonNormativesPerMaterial.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonNormativesPerMaterial.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonNormativesPerMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNormativesPerMaterialActionPerformed(evt);
            }
        });

        jButtonPrintPosAll.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> svi norm. <br>  [F6] </div> </html>");
        jButtonPrintPosAll.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPosAll.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintPosAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosAllActionPerformed(evt);
            }
        });

        jButtonPrintA4All.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> svi norm. <br> [F7] </div> </html>");
        jButtonPrintA4All.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4All.setPreferredSize(new java.awt.Dimension(70, 55));
        jButtonPrintA4All.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4AllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPrintPosAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4All, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jButtonNormativesPerMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonPrintA4All, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonNormativesPerMaterial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(jButtonPrintPosAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTableNormatives.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneNormatives.setViewportView(jTableNormatives);

        jLabel1.setText("Normativna cijena:");

        jLabelArticlePrice.setText("jLabel10");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPaneNormatives)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelArticlePrice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneNormatives, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelArticlePrice))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Artikli"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Filter");

        jTextField1.setPreferredSize(new java.awt.Dimension(130, 25));
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap())
        );

        jTableArticles.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneArticles.setViewportView(jTableArticles);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPaneArticles, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneArticles, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
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
                .addGap(15, 15, 15)
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(194, 194, 194)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 523, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addGap(38, 38, 38)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite artikl u tablici čiji normativ želite dodati.");
            return;
        }
		
		int rowId = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
		int tableId = tableArticlesIdList.get(rowId);
		float taxRate = tableArticlesTaxList.get(rowId);
		
		ClientAppSelectMaterialDialog selectDialog = new ClientAppSelectMaterialDialog(null, true, ClientAppSettings.currentYear);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppWarehouseNormativesMaterialAddEditDialog addEditdialog = new ClientAppWarehouseNormativesMaterialAddEditDialog(null, true, tableId, -1, selectDialog.selectedId);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTableNormatives(tableId, taxRate);
			}
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite artikl u tablici čiji normativ želite urediti.");
            return;
        }
		if(jTableNormatives.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		
		int rowIdArticles = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
		int dbIdArticles = tableArticlesIdList.get(rowIdArticles);
		int rowIdNormative = jTableNormatives.convertRowIndexToModel(jTableNormatives.getSelectedRow());
		int dbIdNormative = tableNormativesIdList.get(rowIdNormative);
		int dbIdMaterial = tableNormativeMaterialIdList.get(rowIdNormative);
		float taxRate = tableArticlesTaxList.get(rowIdArticles);
		
		ClientAppWarehouseNormativesMaterialAddEditDialog addEditdialog = new ClientAppWarehouseNormativesMaterialAddEditDialog(null, true, dbIdArticles, dbIdNormative, dbIdMaterial);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTableNormatives(dbIdArticles, taxRate);
		}
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if(jTableNormatives.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		
		int rowIdArticles = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
		int dbIdArticles = tableArticlesIdList.get(rowIdArticles);
		int rowIdNormative = jTableNormatives.convertRowIndexToModel(jTableNormatives.getSelectedRow());
		int dbIdNormative = tableNormativesIdList.get(rowIdNormative);
		int dbIdMaterial = tableNormativeMaterialIdList.get(rowIdNormative);
		float taxRate = tableArticlesTaxList.get(rowIdArticles);
				
        String tableValue = String.valueOf(jTableNormatives.getModel().getValueAt(jTableNormatives.getSelectedRow(), 1));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati normativ za materijal " + tableValue, "Obriši normativ", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			boolean fromYearStart = false;
			if(StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN){
				int dialogResult2 = JOptionPane.showConfirmDialog (null, "Obriši normativ unatrag do početka godine?", "Obriši normativ", JOptionPane.YES_NO_OPTION);
				if(dialogResult2 == JOptionPane.YES_OPTION) {
					fromYearStart = true;
				}
			}
			
			// Delete article
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);
				
				String query = "UPDATE NORMATIVES SET AMOUNT = 0, IS_DELETED = 1 WHERE ID = ?";
				MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(fromYearStart ? 10 : 2);
				multiDatabaseQuery.SetQuery(0, query);
				multiDatabaseQuery.AddParam(0, 1, dbIdNormative);
				
				String articleId = String.valueOf(jTableArticles.getModel().getValueAt(jTableArticles.getSelectedRow(), 0));
				String articleName = String.valueOf(jTableArticles.getModel().getValueAt(jTableArticles.getSelectedRow(), 1));
				String materialId = String.valueOf(jTableNormatives.getModel().getValueAt(jTableNormatives.getSelectedRow(), 0));
				String normativeAmount = String.valueOf(jTableNormatives.getModel().getValueAt(jTableNormatives.getSelectedRow(), 2));
				Date date = new Date();
				multiDatabaseQuery.SetQuery(1, ClientAppUtils.CHANGES_LOG_QUERY);
				multiDatabaseQuery.SetAutoIncrementParam(1, 1, "ID", "CHANGES_LOG");
				multiDatabaseQuery.AddParam(1, 2, new SimpleDateFormat("yyyy-MM-dd").format(date));
				multiDatabaseQuery.AddParam(1, 3, new SimpleDateFormat("HH:mm:ss").format(date));
				multiDatabaseQuery.AddParam(1, 4, Licence.GetOfficeNumber());
				multiDatabaseQuery.AddParam(1, 5, StaffUserInfo.GetCurrentUserInfo().userId);
				multiDatabaseQuery.AddParam(1, 6, StaffUserInfo.GetCurrentUserInfo().fullName);
				multiDatabaseQuery.AddParam(1, 7, "Brisanje normativa" + (fromYearStart ? " - od početka godine" : ""));
				multiDatabaseQuery.AddParam(1, 8, "Artikl " + articleId + " " + articleName + ", materijal " + materialId + " " + tableValue + ", stara količina " + normativeAmount);
				
				if(fromYearStart){
					String updateInvoiceAmounts = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
							+ "SELECT SUM(INVOICE_MATERIALS.AMT * INVOICE_MATERIALS.NORM) "
							+ "FROM INVOICE_MATERIALS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_MATERIALS.IN_ID "
							+ "WHERE INVOICE_MATERIALS.ART_ID = ? AND INVOICE_MATERIALS.MAT_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = INVOICE_MATERIALS.MAT_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT INVOICE_MATERIALS.AMT "
							+ "FROM INVOICE_MATERIALS "
							+ "INNER JOIN INVOICES ON INVOICES.ID = INVOICE_MATERIALS.IN_ID "
							+ "WHERE INVOICE_MATERIALS.ART_ID = ? AND INVOICE_MATERIALS.MAT_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(INVOICES.I_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = INVOICES.O_NUM "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = INVOICE_MATERIALS.MAT_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(2, updateInvoiceAmounts);
					multiDatabaseQuery.AddParam(2, 1, dbIdArticles);
					multiDatabaseQuery.AddParam(2, 2, dbIdMaterial);
					multiDatabaseQuery.AddParam(2, 3, dbIdArticles);
					multiDatabaseQuery.AddParam(2, 4, dbIdMaterial);
					multiDatabaseQuery.AddParam(2, 5, ClientAppSettings.currentYear);
					
					String updateInvoiceMaterials = ""
							+ "UPDATE INVOICE_MATERIALS SET NORM = 0 "
							+ "WHERE ART_ID = ? AND MAT_ID = ? "
							+ "AND YEAR((SELECT I_DATE FROM INVOICES WHERE INVOICE_MATERIALS.IN_ID = INVOICES.ID)) = ?";
					multiDatabaseQuery.SetQuery(3, updateInvoiceMaterials);
					multiDatabaseQuery.AddParam(3, 1, dbIdArticles);
					multiDatabaseQuery.AddParam(3, 2, dbIdMaterial);
					multiDatabaseQuery.AddParam(3, 3, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterials = ""
							+ "UPDATE LOCAL_INVOICE_MATERIALS SET NORM = 0, IS_DELETED = 1 "
							+ "WHERE ART_ID = ? AND MAT_ID = ? "
							+ "AND YEAR((SELECT I_DATE FROM LOCAL_INVOICES WHERE LOCAL_INVOICE_MATERIALS.IN_ID = LOCAL_INVOICES.ID)) = ?";
					multiDatabaseQuery.SetQuery(4, updateLocalInvoiceMaterials);
					multiDatabaseQuery.AddParam(4, 1, dbIdArticles);
					multiDatabaseQuery.AddParam(4, 2, dbIdMaterial);
					multiDatabaseQuery.AddParam(4, 3, ClientAppSettings.currentYear);
					
					String updateInvoiceMaterialsTest = updateInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.SetQuery(5, updateInvoiceMaterialsTest);
					multiDatabaseQuery.AddParam(5, 1, dbIdArticles);
					multiDatabaseQuery.AddParam(5, 2, dbIdMaterial);
					multiDatabaseQuery.AddParam(5, 3, ClientAppSettings.currentYear);
					
					String updateLocalInvoiceMaterialsTest = updateLocalInvoiceMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
					multiDatabaseQuery.SetQuery(6, updateLocalInvoiceMaterialsTest);
					multiDatabaseQuery.AddParam(6, 1, dbIdArticles);
					multiDatabaseQuery.AddParam(6, 2, dbIdMaterial);
					multiDatabaseQuery.AddParam(6, 3, ClientAppSettings.currentYear);
					
					String queryUpdateAllStartingArticleMaterialAmountsPlus = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT + ("
							+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.STARTING_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.STARTING_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(7, queryUpdateAllStartingArticleMaterialAmountsPlus);
					multiDatabaseQuery.AddParam(7, 1, dbIdMaterial);
					multiDatabaseQuery.AddParam(7, 2, dbIdArticles);
					multiDatabaseQuery.AddParam(7, 3, dbIdMaterial);
					multiDatabaseQuery.AddParam(7, 4, dbIdArticles);
					multiDatabaseQuery.AddParam(7, 5, ClientAppSettings.currentYear);
					
					String queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered = ""
						+ "UPDATE MATERIAL_AMOUNTS SET AMOUNT = AMOUNT - ("
							+ "SELECT SUM(TRANSFER_ARTICLE_MATERIALS.NORMATIVE * TRANSFER_ARTICLES.AMOUNT_START) "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "WHERE EXISTS ("
							+ "SELECT TRANSFER_ARTICLE_MATERIALS.NORMATIVE "
							+ "FROM TRANSFER_ARTICLE_MATERIALS "
							+ "INNER JOIN TRANSFER_ARTICLES ON TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID "
							+ "INNER JOIN TRANSFERS ON TRANSFERS.ID = TRANSFER_ARTICLES.TRANSFER_ID "
							+ "WHERE TRANSFER_ARTICLE_MATERIALS.IS_STARTING = 0 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.IS_DELETED = 0 "
							+ "AND TRANSFER_ARTICLES.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELETED = 0 "
							+ "AND TRANSFERS.IS_DELIVERED = 1 "
							+ "AND TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID = ? "
							+ "AND TRANSFER_ARTICLES.DESTINATION_ARTICLE_ID = ? "
							+ "AND MATERIAL_AMOUNTS.AMOUNT_YEAR = YEAR(TRANSFERS.TRANSFER_START_DATE) "
							+ "AND MATERIAL_AMOUNTS.OFFICE_NUMBER = TRANSFERS.DESTINATION_OFFICE_ID "
							+ "AND MATERIAL_AMOUNTS.MATERIAL_ID = TRANSFER_ARTICLE_MATERIALS.MATERIAL_ID"
						+ ") "
						+ "AND AMOUNT_YEAR = ?";
					multiDatabaseQuery.SetQuery(8, queryUpdateAllDestinationArticleMaterialAmountsMinusIfDelivered);
					multiDatabaseQuery.AddParam(8, 1, dbIdMaterial);
					multiDatabaseQuery.AddParam(8, 2, dbIdArticles);
					multiDatabaseQuery.AddParam(8, 3, dbIdMaterial);
					multiDatabaseQuery.AddParam(8, 4, dbIdArticles);
					multiDatabaseQuery.AddParam(8, 5, ClientAppSettings.currentYear);
					
					String updateTransferNormatives = ""
							+ "UPDATE TRANSFER_ARTICLE_MATERIALS SET NORMATIVE = 0, IS_DELETED = 1 "
							+ "WHERE MATERIAL_ID = ? "
							+ "AND ((SELECT STARTING_ARTICLE_ID FROM TRANSFER_ARTICLES "
								+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID) = ? AND IS_STARTING = 1 "
								+ "OR "
								+ "(SELECT DESTINATION_ARTICLE_ID FROM TRANSFER_ARTICLES "
								+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID) = ? AND IS_STARTING = 0) "
							+ "AND YEAR((SELECT TRANSFER_START_DATE FROM TRANSFERS "
								+ "WHERE ID = (SELECT TRANSFER_ID FROM TRANSFER_ARTICLES "
									+ "WHERE TRANSFER_ARTICLE_MATERIALS.TRANSFER_ARTICLE_ID = TRANSFER_ARTICLES.ID))) = ?";
					multiDatabaseQuery.SetQuery(9, updateTransferNormatives);
					multiDatabaseQuery.AddParam(9, 1, dbIdMaterial);
					multiDatabaseQuery.AddParam(9, 2, dbIdArticles);
					multiDatabaseQuery.AddParam(9, 3, dbIdArticles);
					multiDatabaseQuery.AddParam(9, 4, ClientAppSettings.currentYear);
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
							RefreshTableNormatives(dbIdArticles, taxRate);
						}
					} catch (Exception ex) {
						ClientAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableArticles.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTableArticles.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite artikl u tablici čiji normativ želite ispisati.");
            return;
        }
		
		int rowIdArticles = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
        String tableValue = String.valueOf(jTableArticles.getModel().getValueAt(rowIdArticles, 1));
		
		PrintUtils.PrintPosTable("Norm. - " + tableValue, jTableNormatives, new int[]{0, 1, 2, 3});
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		if(jTableArticles.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite artikl u tablici čiji normativ želite ispisati.");
            return;
        }
		
		int rowIdArticles = jTableArticles.convertRowIndexToModel(jTableArticles.getSelectedRow());
        String tableValue = String.valueOf(jTableArticles.getModel().getValueAt(rowIdArticles, 1));
		
		PrintUtils.PrintA4Table("Normativi-" + tableValue, "Normativi za artikl - " + tableValue, jTableNormatives, "");
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonNormativesPerMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNormativesPerMaterialActionPerformed
		new ClientAppWarehouseNormativesPerMaterialDialog(null, true).setVisible(true);
    }//GEN-LAST:event_jButtonNormativesPerMaterialActionPerformed

    private void jButtonPrintPosAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosAllActionPerformed
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		JTable tempJTable = new JTable();
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ARTICLES.ID, ARTICLES.NAME, MATERIALS.NAME, NORMATIVES.AMOUNT, MEASURING_UNITS.NAME, ARTICLES.CUSTOM_ID "
				+ "FROM NORMATIVES "
				+ "INNER JOIN ARTICLES ON NORMATIVES.ARTICLE_ID = ARTICLES.ID "
				+ "INNER JOIN MATERIALS ON NORMATIVES.MATERIAL_ID = MATERIALS.ID "
				+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
				+ "WHERE NORMATIVES.IS_DELETED = 0 AND MATERIALS.IS_DELETED = 0 AND ARTICLES.IS_DELETED = 0"
				+ "ORDER BY ARTICLES.ID, MATERIALS.NAME";
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
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Artikl", "Materijal", "Kol.", "Mj. jed."});
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = customIdEnabled ? databaseQueryResult.getString(5) : databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = databaseQueryResult.getString(3);
						rowData[4] = databaseQueryResult.getString(4);
						customTableModel.addRow(rowData);
					}
					tempJTable.setModel(customTableModel);
					tempJTable.getColumnModel().getColumn(0).setPreferredWidth(30);
					tempJTable.getColumnModel().getColumn(1).setPreferredWidth(70);
					tempJTable.getColumnModel().getColumn(2).setPreferredWidth(50);
					tempJTable.getColumnModel().getColumn(3).setPreferredWidth(20);
					tempJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	
		PrintUtils.PrintPosTableAllNormatives("Normativi", tempJTable);
		//PrintUtils.PrintPosTable("Normativi", tempJTable, new int[][]{new int[]{0, 1}, new int[]{2, 3, 4}}, null);
		tempJTable = null;
    }//GEN-LAST:event_jButtonPrintPosAllActionPerformed

    private void jButtonPrintA4AllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4AllActionPerformed
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		JTable tempJTable = new JTable();
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ARTICLES.ID, ARTICLES.NAME, MATERIALS.NAME, NORMATIVES.AMOUNT, MEASURING_UNITS.NAME, ARTICLES.CUSTOM_ID "
				+ "FROM NORMATIVES "
				+ "INNER JOIN ARTICLES ON NORMATIVES.ARTICLE_ID = ARTICLES.ID "
				+ "INNER JOIN MATERIALS ON NORMATIVES.MATERIAL_ID = MATERIALS.ID "
				+ "INNER JOIN MEASURING_UNITS ON MATERIALS.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
				+ "WHERE NORMATIVES.IS_DELETED = 0 AND MATERIALS.IS_DELETED = 0 AND ARTICLES.IS_DELETED = 0"
				+ "ORDER BY ARTICLES.ID, MATERIALS.NAME";
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
					customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv artikla", "Naziv materijala", "Količina", "Mjerna jedinica"});
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[5];
						rowData[0] = customIdEnabled ? databaseQueryResult.getString(5) : databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(2);
						rowData[3] = databaseQueryResult.getString(3);
						rowData[4] = databaseQueryResult.getString(4);
						customTableModel.addRow(rowData);
					}
					tempJTable.setModel(customTableModel);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	
		PrintUtils.PrintA4Table("Normativi", "Normativi", tempJTable, new int[]{0, 1, 2, 3, 4}, new int[]{0, 1}, null, "");
		tempJTable = null;
    }//GEN-LAST:event_jButtonPrintA4AllActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonNormativesPerMaterial;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintA4All;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JButton jButtonPrintPosAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelArticlePrice;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneArticles;
    private javax.swing.JScrollPane jScrollPaneNormatives;
    private javax.swing.JTable jTableArticles;
    private javax.swing.JTable jTableNormatives;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
