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
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.extensions.ColorIcon;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsLayoutItemEditDialog extends javax.swing.JDialog {
	
	public boolean changeSuccess = false;
	public int finalItemId = -1;
	public int finalItemType = -1;
	public int finalItemColor = 0;
	public String finalItemName = "";
			
	/**
	 * Creates new form ClientAppWarehouseCategoriesAddDialog
	 */
	public ClientAppSettingsLayoutItemEditDialog(java.awt.Frame parent, boolean modal, int itemId, int itemType, int itemColor) {
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
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDelete.doClick();
					}
				}
				
				return false;
			}
		});
		
		finalItemId = itemId;
		finalItemType = itemType;
		finalItemColor = itemColor;
		
		jButtonColor0.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[0], jButtonColor0.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor0.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor1.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[1], jButtonColor1.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor1.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor2.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[2], jButtonColor2.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor2.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor3.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[3], jButtonColor3.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor3.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor4.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[4], jButtonColor4.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor4.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor5.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[5], jButtonColor5.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor5.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor6.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[6], jButtonColor6.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor6.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor7.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[7], jButtonColor7.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor7.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor8.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[8], jButtonColor8.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor8.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor9.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[9], jButtonColor9.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor9.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor10.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[10], jButtonColor10.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor10.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		jButtonColor11.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[11], jButtonColor11.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonColor11.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
		
		if(finalItemId != -1){
			final JDialog loadingDialog = new LoadingDialog(null, true);
			
		
			String query = "SELECT ARTICLES.NAME FROM ARTICLES WHERE ARTICLES.ID = ?";
			if(finalItemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
				query = "SELECT SERVICES.NAME FROM SERVICES WHERE SERVICES.ID = ?";
			} else if(finalItemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS){
				query = "SELECT TRADING_GOODS.NAME FROM TRADING_GOODS WHERE TRADING_GOODS.ID = ?";
			}
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, finalItemId);
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
							finalItemName = databaseQueryResult.getString(0);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		ClientAppUtils.SetTouchLayoutItemButtonText(jButtonExample, finalItemName);
		jLabelItem.setText(finalItemName);
		
		ClientAppUtils.SetupFocusTraversal(this);
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
        jLabelItem = new javax.swing.JLabel();
        jButtonExample = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonSelectArticle = new javax.swing.JButton();
        jButtonSelectTradingGoods = new javax.swing.JButton();
        jButtonSelectService = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButtonColor0 = new javax.swing.JButton();
        jButtonColor1 = new javax.swing.JButton();
        jButtonColor2 = new javax.swing.JButton();
        jButtonColor3 = new javax.swing.JButton();
        jButtonColor4 = new javax.swing.JButton();
        jButtonColor5 = new javax.swing.JButton();
        jButtonColor6 = new javax.swing.JButton();
        jButtonColor7 = new javax.swing.JButton();
        jButtonColor8 = new javax.swing.JButton();
        jButtonColor9 = new javax.swing.JButton();
        jButtonColor10 = new javax.swing.JButton();
        jButtonColor11 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Touch screen - uredi stavku");
        setResizable(false);

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelTitle.setText("Touch screen - uredi stavku");
        jLabelTitle.setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Izgled stavke"));

        jLabel1.setText("Odabrana stavka:");

        jLabelItem.setText("odabrana stavka");
        jLabelItem.setPreferredSize(new java.awt.Dimension(250, 14));

        jButtonExample.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExample.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExample.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabelItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabelItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Odabir stavke"));

        jButtonSelectArticle.setText("<html> <div style=\"text-align: center\"> Artikl </div> </html>");
        jButtonSelectArticle.setToolTipText("");
        jButtonSelectArticle.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonSelectArticle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectArticleActionPerformed(evt);
            }
        });

        jButtonSelectTradingGoods.setText("<html> <div style=\"text-align: center\"> Trgovačka roba </div> </html>");
        jButtonSelectTradingGoods.setToolTipText("");
        jButtonSelectTradingGoods.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonSelectTradingGoods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectTradingGoodsActionPerformed(evt);
            }
        });

        jButtonSelectService.setText("<html> <div style=\"text-align: center\"> Usluga </div> </html>");
        jButtonSelectService.setToolTipText("");
        jButtonSelectService.setPreferredSize(new java.awt.Dimension(80, 60));
        jButtonSelectService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectServiceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(jButtonSelectArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonSelectTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonSelectService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonSelectArticle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSelectService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonSelectTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jButtonDelete.setText("<html> <div style=\"text-align: center\"> Obriši stavku <br> [DEL] </div> </html>");
        jButtonDelete.setPreferredSize(new java.awt.Dimension(84, 78));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Odabir boje"));

        jButtonColor0.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor0ActionPerformed(evt);
            }
        });

        jButtonColor1.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor1ActionPerformed(evt);
            }
        });

        jButtonColor2.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor2ActionPerformed(evt);
            }
        });

        jButtonColor3.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor3ActionPerformed(evt);
            }
        });

        jButtonColor4.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor4ActionPerformed(evt);
            }
        });

        jButtonColor5.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor5ActionPerformed(evt);
            }
        });

        jButtonColor6.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor6ActionPerformed(evt);
            }
        });

        jButtonColor7.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor7ActionPerformed(evt);
            }
        });

        jButtonColor8.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor8ActionPerformed(evt);
            }
        });

        jButtonColor9.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor9ActionPerformed(evt);
            }
        });

        jButtonColor10.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor10ActionPerformed(evt);
            }
        });

        jButtonColor11.setPreferredSize(new java.awt.Dimension(70, 70));
        jButtonColor11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor11ActionPerformed(evt);
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
                        .addComponent(jButtonColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(jButtonColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jButtonColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(240, 240, 240)
                        .addComponent(jButtonColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(320, 320, 320)
                        .addComponent(jButtonColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(400, 400, 400)
                        .addComponent(jButtonColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButtonColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(jLabelTitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabelTitle)
                .addGap(30, 30, 30)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 57, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
		changeSuccess = true;
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
		changeSuccess = true;
		finalItemId = -1;
		finalItemType = 0;
		finalItemColor = 0;
		finalItemName = "";
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonSelectArticleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectArticleActionPerformed
		ClientAppSelectArticleDialog selectDialog = new ClientAppSelectArticleDialog(null, true);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			finalItemId = selectDialog.selectedId;
			finalItemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE;
			finalItemName = selectDialog.selectedName;
			ClientAppUtils.SetTouchLayoutItemButtonText(jButtonExample, finalItemName);
			jLabelItem.setText(finalItemName);
        }
    }//GEN-LAST:event_jButtonSelectArticleActionPerformed

    private void jButtonSelectTradingGoodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectTradingGoodsActionPerformed
		ClientAppSelectTradingGoodsDialog selectDialog = new ClientAppSelectTradingGoodsDialog(null, true, ClientAppSettings.currentYear);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			finalItemId = selectDialog.selectedId;
			finalItemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS;
			finalItemName = selectDialog.selectedName;
			ClientAppUtils.SetTouchLayoutItemButtonText(jButtonExample, finalItemName);
			jLabelItem.setText(finalItemName);
        }
    }//GEN-LAST:event_jButtonSelectTradingGoodsActionPerformed

    private void jButtonSelectServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectServiceActionPerformed
		ClientAppSelectServiceDialog selectDialog = new ClientAppSelectServiceDialog(null, true);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			finalItemId = selectDialog.selectedId;
			finalItemType = Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE;
			finalItemName = selectDialog.selectedName;
			ClientAppUtils.SetTouchLayoutItemButtonText(jButtonExample, finalItemName);
			jLabelItem.setText(finalItemName);
        }
    }//GEN-LAST:event_jButtonSelectServiceActionPerformed

    private void jButtonColor0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor0ActionPerformed
		finalItemColor = 0;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor0ActionPerformed

    private void jButtonColor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor1ActionPerformed
		finalItemColor = 1;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor1ActionPerformed

    private void jButtonColor2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor2ActionPerformed
		finalItemColor = 2;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor2ActionPerformed

    private void jButtonColor3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor3ActionPerformed
		finalItemColor = 3;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor3ActionPerformed

    private void jButtonColor4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor4ActionPerformed
		finalItemColor = 4;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor4ActionPerformed

    private void jButtonColor5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor5ActionPerformed
		finalItemColor = 5;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor5ActionPerformed

    private void jButtonColor6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor6ActionPerformed
		finalItemColor = 6;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor6ActionPerformed

    private void jButtonColor7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor7ActionPerformed
		finalItemColor = 7;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor7ActionPerformed

    private void jButtonColor8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor8ActionPerformed
		finalItemColor = 8;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor8ActionPerformed

    private void jButtonColor9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor9ActionPerformed
		finalItemColor = 9;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor9ActionPerformed

    private void jButtonColor10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor10ActionPerformed
        finalItemColor = 10;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor10ActionPerformed

    private void jButtonColor11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor11ActionPerformed
        finalItemColor = 11;
		jButtonExample.setIcon(new ColorIcon(Values.SETTINGS_LAYOUT_COLORS[finalItemColor], jButtonExample.getWidth() - Values.SETTINGS_LAYOUT_COLOR_OFFSET, jButtonExample.getHeight() - Values.SETTINGS_LAYOUT_COLOR_OFFSET));
    }//GEN-LAST:event_jButtonColor11ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonColor0;
    private javax.swing.JButton jButtonColor1;
    private javax.swing.JButton jButtonColor10;
    private javax.swing.JButton jButtonColor11;
    private javax.swing.JButton jButtonColor2;
    private javax.swing.JButton jButtonColor3;
    private javax.swing.JButton jButtonColor4;
    private javax.swing.JButton jButtonColor5;
    private javax.swing.JButton jButtonColor6;
    private javax.swing.JButton jButtonColor7;
    private javax.swing.JButton jButtonColor8;
    private javax.swing.JButton jButtonColor9;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonExample;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSelectArticle;
    private javax.swing.JButton jButtonSelectService;
    private javax.swing.JButton jButtonSelectTradingGoods;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelItem;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
