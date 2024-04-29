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
import hr.adinfo.client.print.PrintUtils;
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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ClientAppWarehouseArticlesDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableIdList = new ArrayList<>();

	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppWarehouseArticlesDialog(java.awt.Frame parent, boolean modal) {
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
					}
				}
				
				return false;
			}
		});
		
		jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEdit.doClick();
				}
			}
		});
		
		jTable1.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RefreshTable();
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTable(){
		ClientAppSettings.LoadSettings();
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT ARTICLES.ID, SUM(MATERIALS.LAST_PRICE * NORMATIVES.AMOUNT) "
				+ "FROM ARTICLES "
				+ "INNER JOIN NORMATIVES ON ARTICLES.ID = NORMATIVES.ARTICLE_ID "
				+ "INNER JOIN MATERIALS ON MATERIALS.ID = NORMATIVES.MATERIAL_ID "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND NORMATIVES.IS_DELETED = 0 "
				+ "GROUP BY ARTICLES.ID";
		String query2 = "SELECT ARTICLES.ID, ARTICLES.NAME, ARTICLES.PRICE, TAX_RATES.VALUE, CONSUMPTION_TAXES.NAME, MEASURING_UNITS.NAME, "
				+ "ARTICLES.CUSTOM_ID, ARTICLES.IS_ACTIVE "
				+ "FROM ARTICLES "
				+ "INNER JOIN CONSUMPTION_TAXES ON ARTICLES.CONSUMPTION_TAX_ID = CONSUMPTION_TAXES.ID "
				+ "INNER JOIN TAX_RATES ON ARTICLES.TAX_RATE_ID = TAX_RATES.ID "
				+ "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
				+ "WHERE ARTICLES.IS_DELETED = 0 "
				+ "ORDER BY ARTICLES.ID";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, query);
		multiDatabaseQuery.SetQuery(1, query2);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!databaseQueryTask.isDone()){
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					final ArrayList<Color> foregroundColor = new ArrayList<>();
					final ArrayList<Color> backgroundColor = new ArrayList<>();
					final ArrayList<Color> foregroundColorSelected = new ArrayList<>();
					final ArrayList<Color> backgroundColorSelected = new ArrayList<>();
					
					MultiDatabaseQueryResponse multiDatabaseQueryResponse = (MultiDatabaseQueryResponse) serverResponse;
					ArrayList<Pair<Integer, Float>> lastPriceList = new ArrayList<>();
					if(multiDatabaseQueryResponse.databaseQueryResult != null && multiDatabaseQueryResponse.databaseQueryResult[0] != null){
						while (multiDatabaseQueryResponse.databaseQueryResult[0].next()) {
							lastPriceList.add(new Pair<>(multiDatabaseQueryResponse.databaseQueryResult[0].getInt(0), multiDatabaseQueryResponse.databaseQueryResult[0].getFloat(1)));
						}
					}
					if(multiDatabaseQueryResponse.databaseQueryResult != null && multiDatabaseQueryResponse.databaseQueryResult[1] != null){
						DatabaseQueryResult databaseQueryResult = multiDatabaseQueryResponse.databaseQueryResult[1];
						CustomTableModel customTableModel = new CustomTableModel();
						customTableModel.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Porezna stopa", "Cijena", "PNC", "Mjerna jedinica", "PNP Tip"});
						ArrayList<Integer> idList = new ArrayList<>();
						while (databaseQueryResult.next()) {
							Object[] rowData = new Object[7];
							rowData[0] = customIdEnabled ? databaseQueryResult.getString(6) : databaseQueryResult.getString(0);
							rowData[1] = databaseQueryResult.getString(1);
							rowData[2] = databaseQueryResult.getString(3) + " %";
							rowData[3] = ClientAppUtils.FloatToPriceString(databaseQueryResult.getFloat(2));
							rowData[4] = "0.00";
							rowData[5] = databaseQueryResult.getString(5);
							rowData[6] = databaseQueryResult.getString(4);
							for (Pair<Integer, Float> pair : lastPriceList){
								if(pair.getKey() == databaseQueryResult.getInt(0)){
									rowData[4] = ClientAppUtils.FloatToPriceString(pair.getValue() + pair.getValue() * databaseQueryResult.getFloat(3) / 100f);
								}
							}
							customTableModel.addRow(rowData);
							idList.add(databaseQueryResult.getInt(0));
							
							if(databaseQueryResult.getInt(7) == 1){
								foregroundColor.add(Values.normalForeground);
								backgroundColor.add(Values.normalBackground);
								foregroundColorSelected.add(Values.normalForegroundSelected);
								backgroundColorSelected.add(Values.normalBackgroundSelected);
							} else {
								foregroundColor.add(Values.grayForeground);
								backgroundColor.add(Values.grayBackground);
								foregroundColorSelected.add(Values.grayForegroundSelected);
								backgroundColorSelected.add(Values.grayBackgroundSelected);
							}
						}
						jTable1.setModel(customTableModel);
						tableIdList = idList;

						jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 10 / 100);
						jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 35 / 100);
						jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
						jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 10 / 100);
						jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 10 / 100);
						jTable1.getColumnModel().getColumn(5).setPreferredWidth(jScrollPane1.getWidth() * 10 / 100);
						jTable1.getColumnModel().getColumn(6).setPreferredWidth(jScrollPane1.getWidth() * 10 / 100);
						
						jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
							@Override
							public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
								final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
								if(isSelected){
									c.setForeground(foregroundColorSelected.get(row));
									c.setBackground(backgroundColorSelected.get(row));
								} else {
									c.setForeground(foregroundColor.get(row));
									c.setBackground(backgroundColor.get(row));
								}

								return c;
							}
						});
					}
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
        jPanelButtons = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrintPos = new javax.swing.JButton();
        jButtonPrintA4 = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Artikli");
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
        jLabel9.setText("Artikli");

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAdd.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAdd.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonEdit.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEdit.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonPrintPos.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPos.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonPrintPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosActionPerformed(evt);
            }
        });

        jButtonPrintA4.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonPrintA4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setPreferredSize(new java.awt.Dimension(80, 70));
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
                .addContainerGap()
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(90, 90, 90)
                .addComponent(jButtonPrintPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintA4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 206, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintA4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setText("Filter");

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
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(574, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGap(173, 173, 173)
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addGap(8, 8, 8)
                        .addComponent(jLabel9)))
                .addGap(18, 18, 18)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
		ClientAppWarehouseArticlesAddEditDialog addEditdialog = new ClientAppWarehouseArticlesAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if(jTable1.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
        int tableId = tableIdList.get(rowId);

        ClientAppWarehouseArticlesAddEditDialog addEditdialog = new ClientAppWarehouseArticlesAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTable();
        }
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if(jTable1.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
        int tableId = tableIdList.get(rowId);
        String tableValue = String.valueOf(jTable1.getModel().getValueAt(rowId, 1));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati artikl " + tableValue, "Obriši artikl", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			// Delete article
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);
				

				String query = "UPDATE ARTICLES SET IS_DELETED = ? WHERE ID = ?";
				DatabaseQuery databaseQuery = new DatabaseQuery(query);
				databaseQuery.AddParam(1, 1);
				databaseQuery.AddParam(2, tableId);

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
							RefreshTable();
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
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
		jTable1.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonPrintA4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ActionPerformed
		PrintUtils.PrintA4Table("Artikli", "Artikli", jTable1, "");
    }//GEN-LAST:event_jButtonPrintA4ActionPerformed

    private void jButtonPrintPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosActionPerformed
		PrintUtils.PrintPosTable("Artikli", jTable1, new int[]{0, 1, 3});
    }//GEN-LAST:event_jButtonPrintPosActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4;
    private javax.swing.JButton jButtonPrintPos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
