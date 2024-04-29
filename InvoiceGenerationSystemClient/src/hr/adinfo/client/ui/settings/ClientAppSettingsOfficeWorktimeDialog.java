/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.settings;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
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
import hr.adinfo.utils.extensions.CustomFocusListener;
import hr.adinfo.utils.extensions.CustomKeyAdapter;
import hr.adinfo.utils.extensions.CustomTableModel;
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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsOfficeWorktimeDialog extends javax.swing.JDialog {
	private ArrayList<Integer> tableHolidaysIdList = new ArrayList<>();
	private ArrayList<Integer> tableShiftsIdList = new ArrayList<>();
	
	/**
	 * Creates new form ClientAppStaffWorktimeDialog
	 */
	public ClientAppSettingsOfficeWorktimeDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		
		jCheckBoxMon.setSelected(true);
		jCheckBoxTue.setSelected(true);
		jCheckBoxWed.setSelected(true);
		jCheckBoxThu.setSelected(true);
		jCheckBoxFri.setSelected(true);
		jCheckBoxSat.setSelected(true);
		jCheckBoxSun.setSelected(true);
		
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
					} else if(ke.getKeyCode() == KeyEvent.VK_INSERT){
						jButtonAddHoliday.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F10){
						jButtonEditHoliday.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_DELETE){
						jButtonDeleteHoliday.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F7){
						jButtonAddShift.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F8){
						jButtonEditShift.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F9){
						jButtonDeleteShift.doClick();
					}
				}
				
				return false;
			}
		});
		
		JTextField[][] textFields = new JTextField[][]{
			new JTextField[] {jTextFieldMon1, jTextFieldMon2, jTextFieldMon3, jTextFieldMon4 },
			new JTextField[] {jTextFieldTue1, jTextFieldTue2, jTextFieldTue3, jTextFieldTue4 },
			new JTextField[] {jTextFieldWed1, jTextFieldWed2, jTextFieldWed3, jTextFieldWed4 },
			new JTextField[] {jTextFieldThu1, jTextFieldThu2, jTextFieldThu3, jTextFieldThu4 },
			new JTextField[] {jTextFieldFri1, jTextFieldFri2, jTextFieldFri3, jTextFieldFri4 },
			new JTextField[] {jTextFieldSat1, jTextFieldSat2, jTextFieldSat3, jTextFieldSat4 },
			new JTextField[] {jTextFieldSun1, jTextFieldSun2, jTextFieldSun3, jTextFieldSun4 },
		};
		
		for(int i = 0; i < textFields.length; i++){
			for(int j = 0; j < textFields[i].length; j++){
				textFields[i][j].addKeyListener(new CustomKeyAdapter(textFields[i][j], 0, (j % 2 == 0) ? 23 : 59));
				textFields[i][j].addFocusListener(new CustomFocusListener(textFields[i][j]));
			}
		}
		
		// Holidays
		jTableHolidays.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableHolidays.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableHolidays.getTableHeader().setReorderingAllowed(false);
		jTableHolidays.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RefreshTableHolidays();
		
		// Shifts
		jTableShifts.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableShifts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableShifts.getTableHeader().setReorderingAllowed(false);
		jTableShifts.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		RefreshTableShifts();
		
		// Load settings
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);
			

			String query = "SELECT "
					+ "W1, W2, W3, W4, W5, W6, W7, "
					+ "HF1, HT1, MF1, MT1, "
					+ "HF2, HT2, MF2, MT2, "
					+ "HF3, HT3, MF3, MT3, "
					+ "HF4, HT4, MF4, MT4, "
					+ "HF5, HT5, MF5, MT5, "
					+ "HF6, HT6, MF6, MT6, "
					+ "HF7, HT7, MF7, MT7 "
					+ "FROM OFFICE_WORKTIME WHERE OFFICE_NUMBER = ?";
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, Licence.GetOfficeNumber());
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
							jCheckBoxMon.setSelected(databaseQueryResult.getInt(0) == 1);
							jCheckBoxTue.setSelected(databaseQueryResult.getInt(1) == 1);
							jCheckBoxWed.setSelected(databaseQueryResult.getInt(2) == 1);
							jCheckBoxThu.setSelected(databaseQueryResult.getInt(3) == 1);
							jCheckBoxFri.setSelected(databaseQueryResult.getInt(4) == 1);
							jCheckBoxSat.setSelected(databaseQueryResult.getInt(5) == 1);
							jCheckBoxSun.setSelected(databaseQueryResult.getInt(6) == 1);
							jTextFieldMon1.setText(databaseQueryResult.getString(7));
							jTextFieldMon3.setText(databaseQueryResult.getString(8));
							jTextFieldMon2.setText(databaseQueryResult.getString(9));
							jTextFieldMon4.setText(databaseQueryResult.getString(10));
							jTextFieldTue1.setText(databaseQueryResult.getString(11));
							jTextFieldTue3.setText(databaseQueryResult.getString(12));
							jTextFieldTue2.setText(databaseQueryResult.getString(13));
							jTextFieldTue4.setText(databaseQueryResult.getString(14));
							jTextFieldWed1.setText(databaseQueryResult.getString(15));
							jTextFieldWed3.setText(databaseQueryResult.getString(16));
							jTextFieldWed2.setText(databaseQueryResult.getString(17));
							jTextFieldWed4.setText(databaseQueryResult.getString(18));
							jTextFieldThu1.setText(databaseQueryResult.getString(19));
							jTextFieldThu3.setText(databaseQueryResult.getString(20));
							jTextFieldThu2.setText(databaseQueryResult.getString(21));
							jTextFieldThu4.setText(databaseQueryResult.getString(22));
							jTextFieldFri1.setText(databaseQueryResult.getString(23));
							jTextFieldFri3.setText(databaseQueryResult.getString(24));
							jTextFieldFri2.setText(databaseQueryResult.getString(25));
							jTextFieldFri4.setText(databaseQueryResult.getString(26));
							jTextFieldSat1.setText(databaseQueryResult.getString(27));
							jTextFieldSat3.setText(databaseQueryResult.getString(28));
							jTextFieldSat2.setText(databaseQueryResult.getString(29));
							jTextFieldSat4.setText(databaseQueryResult.getString(30));
							jTextFieldSun1.setText(databaseQueryResult.getString(31));
							jTextFieldSun3.setText(databaseQueryResult.getString(32));
							jTextFieldSun2.setText(databaseQueryResult.getString(33));
							jTextFieldSun4.setText(databaseQueryResult.getString(34));
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void RefreshTableHolidays(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, HOLIDAY_DATE, NAME, IS_ACTIVE FROM HOLIDAYS WHERE OFFICE_NUMBER = ? AND IS_DELETED = 0");
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
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
					customTableModel.setColumnIdentifiers(new String[] {"Datum", "Naziv", "Aktivan"});
					ArrayList<Integer> idList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[3];
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(1));
						rowData[0] = new SimpleDateFormat("dd.MM.yyyy.").format(date);
						rowData[1] = databaseQueryResult.getString(2);
						rowData[2] = databaseQueryResult.getInt(3) == 0 ? "Ne" : "Da";
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
					}
					jTableHolidays.setModel(customTableModel);
					tableHolidaysIdList = idList;
					
					jTableHolidays.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneHolidays.getWidth() * 30 / 100);
					jTableHolidays.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneHolidays.getWidth() * 50 / 100);
					jTableHolidays.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneHolidays.getWidth() * 20 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTableShifts(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, NAME, HF, HT, MF, MT FROM SHIFTS WHERE OFFICE_NUMBER = ? AND IS_DELETED = 0");
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
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
					customTableModel.setColumnIdentifiers(new String[] {"Naziv smjene", "Početak smjene", "Kraj smjene"});
					ArrayList<Integer> idList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[3];
						rowData[0] = databaseQueryResult.getString(1);
						String hf, ht, mf, mt;
						if(databaseQueryResult.getInt(2) < 10){
							hf = "0" + databaseQueryResult.getInt(2);
						} else {
							hf = "" + databaseQueryResult.getInt(2);
						}
						if(databaseQueryResult.getInt(3) < 10){
							ht = "0" + databaseQueryResult.getInt(3);
						} else {
							ht = "" + databaseQueryResult.getInt(3);
						}
						if(databaseQueryResult.getInt(4) < 10){
							mf = "0" + databaseQueryResult.getInt(4);
						} else {
							mf = "" + databaseQueryResult.getInt(4);
						}
						if(databaseQueryResult.getInt(5) < 10){
							mt = "0" + databaseQueryResult.getInt(5);
						} else {
							mt = "" + databaseQueryResult.getInt(5);
						}
						rowData[1] = hf + ":" + mf;
						rowData[2] = ht + ":" + mt;
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
					}
					jTableShifts.setModel(customTableModel);
					tableShiftsIdList = idList;
					
					jTableShifts.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneShifts.getWidth() * 40 / 100);
					jTableShifts.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneShifts.getWidth() * 30 / 100);
					jTableShifts.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneShifts.getWidth() * 30 / 100);
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

        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldMon1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldMon2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldMon3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldMon4 = new javax.swing.JTextField();
        jCheckBoxMon = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldTue1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldTue2 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldTue3 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldTue4 = new javax.swing.JTextField();
        jCheckBoxTue = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldWed1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldWed2 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldWed3 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldWed4 = new javax.swing.JTextField();
        jCheckBoxWed = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldThu1 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldThu2 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldThu3 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldThu4 = new javax.swing.JTextField();
        jCheckBoxThu = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jTextFieldFri1 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldFri2 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextFieldFri3 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jTextFieldFri4 = new javax.swing.JTextField();
        jCheckBoxFri = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldSat1 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jTextFieldSat2 = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jTextFieldSat3 = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jTextFieldSat4 = new javax.swing.JTextField();
        jCheckBoxSat = new javax.swing.JCheckBox();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jTextFieldSun1 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jTextFieldSun2 = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jTextFieldSun3 = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jTextFieldSun4 = new javax.swing.JTextField();
        jCheckBoxSun = new javax.swing.JCheckBox();
        jPanelAdinfoLogo = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonAddHoliday = new javax.swing.JButton();
        jButtonEditHoliday = new javax.swing.JButton();
        jButtonDeleteHoliday = new javax.swing.JButton();
        jButtonPrintPosHoliday = new javax.swing.JButton();
        jButtonPrintA4Holiday = new javax.swing.JButton();
        jScrollPaneHolidays = new javax.swing.JScrollPane();
        jTableHolidays = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanelButtons1 = new javax.swing.JPanel();
        jButtonAddShift = new javax.swing.JButton();
        jButtonEditShift = new javax.swing.JButton();
        jButtonDeleteShift = new javax.swing.JButton();
        jButtonPrintPosShift = new javax.swing.JButton();
        jButtonPrintA4Shift = new javax.swing.JButton();
        jScrollPaneShifts = new javax.swing.JScrollPane();
        jTableShifts = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Radno vrijeme poslovnice");
        setResizable(false);

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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Radno vrijeme poslovnice"));

        jLabel1.setText("Ponedjeljak:");
        jLabel1.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel2.setText("Od:");

        jTextFieldMon1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldMon1.setText("00");
        jTextFieldMon1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel3.setText(":");

        jTextFieldMon2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldMon2.setText("00");
        jTextFieldMon2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel4.setText("Do:");

        jTextFieldMon3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldMon3.setText("00");
        jTextFieldMon3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel5.setText(":");

        jTextFieldMon4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldMon4.setText("00");
        jTextFieldMon4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxMon.setText("Neradni dan");
        jCheckBoxMon.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMonItemStateChanged(evt);
            }
        });

        jLabel6.setText("Utorak:");
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel7.setText("Od:");

        jTextFieldTue1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldTue1.setText("00");
        jTextFieldTue1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel8.setText(":");

        jTextFieldTue2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldTue2.setText("00");
        jTextFieldTue2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel9.setText("Do:");

        jTextFieldTue3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldTue3.setText("00");
        jTextFieldTue3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel10.setText(":");

        jTextFieldTue4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldTue4.setText("00");
        jTextFieldTue4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxTue.setText("Neradni dan");
        jCheckBoxTue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxTueItemStateChanged(evt);
            }
        });

        jLabel11.setText("Srijeda:");
        jLabel11.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel12.setText("Od:");

        jTextFieldWed1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldWed1.setText("00");
        jTextFieldWed1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel13.setText(":");

        jTextFieldWed2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldWed2.setText("00");
        jTextFieldWed2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel14.setText("Do:");

        jTextFieldWed3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldWed3.setText("00");
        jTextFieldWed3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel15.setText(":");

        jTextFieldWed4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldWed4.setText("00");
        jTextFieldWed4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxWed.setText("Neradni dan");
        jCheckBoxWed.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxWedItemStateChanged(evt);
            }
        });

        jLabel16.setText("Četvrtak:");
        jLabel16.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel17.setText("Od:");

        jTextFieldThu1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldThu1.setText("00");
        jTextFieldThu1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel18.setText(":");

        jTextFieldThu2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldThu2.setText("00");
        jTextFieldThu2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel19.setText("Do:");

        jTextFieldThu3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldThu3.setText("00");
        jTextFieldThu3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel20.setText(":");

        jTextFieldThu4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldThu4.setText("00");
        jTextFieldThu4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxThu.setText("Neradni dan");
        jCheckBoxThu.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxThuItemStateChanged(evt);
            }
        });

        jLabel21.setText("Petak:");
        jLabel21.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel22.setText("Od:");

        jTextFieldFri1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldFri1.setText("00");
        jTextFieldFri1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel23.setText(":");

        jTextFieldFri2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldFri2.setText("00");
        jTextFieldFri2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel24.setText("Do:");

        jTextFieldFri3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldFri3.setText("00");
        jTextFieldFri3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel25.setText(":");

        jTextFieldFri4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldFri4.setText("00");
        jTextFieldFri4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxFri.setText("Neradni dan");
        jCheckBoxFri.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxFriItemStateChanged(evt);
            }
        });

        jLabel26.setText("Subota:");
        jLabel26.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel27.setText("Od:");

        jTextFieldSat1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSat1.setText("00");
        jTextFieldSat1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel28.setText(":");

        jTextFieldSat2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSat2.setText("00");
        jTextFieldSat2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel29.setText("Do:");

        jTextFieldSat3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSat3.setText("00");
        jTextFieldSat3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel30.setText(":");

        jTextFieldSat4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSat4.setText("00");
        jTextFieldSat4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxSat.setText("Neradni dan");
        jCheckBoxSat.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxSatItemStateChanged(evt);
            }
        });

        jLabel31.setText("Nedjelja i blagdan:");
        jLabel31.setPreferredSize(new java.awt.Dimension(90, 14));

        jLabel32.setText("Od:");

        jTextFieldSun1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSun1.setText("00");
        jTextFieldSun1.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel33.setText(":");

        jTextFieldSun2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSun2.setText("00");
        jTextFieldSun2.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel34.setText("Do:");

        jTextFieldSun3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSun3.setText("00");
        jTextFieldSun3.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel35.setText(":");

        jTextFieldSun4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSun4.setText("00");
        jTextFieldSun4.setPreferredSize(new java.awt.Dimension(25, 25));

        jCheckBoxSun.setText("Neradni dan");
        jCheckBoxSun.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxSunItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldMon1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMon2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldMon3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMon4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addComponent(jCheckBoxMon))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldTue1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldTue2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldTue3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldTue4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxTue))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldWed1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldWed2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldWed3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldWed4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxWed))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldThu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldThu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldThu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldThu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxThu))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldFri1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFri2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldFri3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFri4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxFri))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldSat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSat2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldSat3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSat4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxSat))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldSun1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel33)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSun2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldSun3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSun4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxSun)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldMon3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(jTextFieldMon4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBoxMon))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(jTextFieldMon1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jTextFieldMon2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldTue1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldTue2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldTue3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jTextFieldTue4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxTue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldWed1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jTextFieldWed2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jTextFieldWed3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jTextFieldWed4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxWed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jTextFieldThu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jTextFieldThu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jTextFieldThu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(jTextFieldThu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxThu))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(jTextFieldFri1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(jTextFieldFri2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(jTextFieldFri3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(jTextFieldFri4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxFri))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(jTextFieldSat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(jTextFieldSat2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(jTextFieldSat3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(jTextFieldSat4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxSat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32)
                    .addComponent(jTextFieldSun1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33)
                    .addComponent(jTextFieldSun2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34)
                    .addComponent(jTextFieldSun3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(jTextFieldSun4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxSun))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hr/adinfo/client/ui/adinfologo.jpg"))); // NOI18N

        jLabel37.setFont(jLabel37.getFont().deriveFont(jLabel37.getFont().getSize()-2f));
        jLabel37.setText("mob: 095/6230-100");

        jLabel38.setFont(jLabel38.getFont().deriveFont(jLabel38.getFont().getSize()-2f));
        jLabel38.setText("mob: 091/6230-670");

        jLabel39.setFont(jLabel39.getFont().deriveFont(jLabel39.getFont().getSize()-2f));
        jLabel39.setText("fax: 01/6230-699");

        jLabel40.setFont(jLabel40.getFont().deriveFont(jLabel40.getFont().getSize()-2f));
        jLabel40.setText("tel: 01/6230-668");

        jLabel41.setFont(jLabel41.getFont().deriveFont(jLabel41.getFont().getSize()-2f));
        jLabel41.setText("office.accable@gmail.com");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37)
                    .addComponent(jLabel38)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40)
                    .addComponent(jLabel41))
                .addGap(0, 48, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jLabel37)
                .addGap(2, 2, 2)
                .addComponent(jLabel38)
                .addGap(2, 2, 2)
                .addComponent(jLabel39)
                .addGap(2, 2, 2)
                .addComponent(jLabel40)
                .addGap(2, 2, 2)
                .addComponent(jLabel41)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelAdinfoLogoLayout = new javax.swing.GroupLayout(jPanelAdinfoLogo);
        jPanelAdinfoLogo.setLayout(jPanelAdinfoLogoLayout);
        jPanelAdinfoLogoLayout.setHorizontalGroup(
            jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelAdinfoLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanelAdinfoLogoLayout.setVerticalGroup(
            jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAdinfoLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAdinfoLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel42.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel42.setText("Radno vrijeme poslovnice");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Državni praznici i blagdani"));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAddHoliday.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [INS] </div> </html>");
        jButtonAddHoliday.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonAddHoliday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddHolidayActionPerformed(evt);
            }
        });

        jButtonEditHoliday.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F10] </div> </html>");
        jButtonEditHoliday.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonEditHoliday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditHolidayActionPerformed(evt);
            }
        });

        jButtonDeleteHoliday.setText("<html> <div style=\"text-align: center\"> Obriši <br> [DEL] </div> </html>");
        jButtonDeleteHoliday.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonDeleteHoliday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteHolidayActionPerformed(evt);
            }
        });

        jButtonPrintPosHoliday.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPosHoliday.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonPrintPosHoliday.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonPrintPosHoliday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosHolidayActionPerformed(evt);
            }
        });

        jButtonPrintA4Holiday.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4Holiday.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonPrintA4Holiday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4HolidayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEditHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonDeleteHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jButtonPrintPosHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintA4Holiday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonAddHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEditHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrintA4Holiday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonDeleteHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrintPosHoliday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTableHolidays.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneHolidays.setViewportView(jTableHolidays);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneHolidays, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneHolidays, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Rad u smjenama"));

        jPanelButtons1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonAddShift.setText("<html> <div style=\"text-align: center\"> Dodaj <br> [F7] </div> </html>");
        jButtonAddShift.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonAddShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddShiftActionPerformed(evt);
            }
        });

        jButtonEditShift.setText("<html> <div style=\"text-align: center\"> Uredi <br> [F8] </div> </html>");
        jButtonEditShift.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonEditShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditShiftActionPerformed(evt);
            }
        });

        jButtonDeleteShift.setText("<html> <div style=\"text-align: center\"> Obriši <br> [F9] </div> </html>");
        jButtonDeleteShift.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonDeleteShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteShiftActionPerformed(evt);
            }
        });

        jButtonPrintPosShift.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F2] </div> </html>");
        jButtonPrintPosShift.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonPrintPosShift.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonPrintPosShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosShiftActionPerformed(evt);
            }
        });

        jButtonPrintA4Shift.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F3] </div> </html>");
        jButtonPrintA4Shift.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonPrintA4Shift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4ShiftActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtons1Layout = new javax.swing.GroupLayout(jPanelButtons1);
        jPanelButtons1.setLayout(jPanelButtons1Layout);
        jPanelButtons1Layout.setHorizontalGroup(
            jPanelButtons1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtons1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEditShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonDeleteShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonPrintPosShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPrintA4Shift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtons1Layout.setVerticalGroup(
            jPanelButtons1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtons1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtons1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonAddShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEditShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtons1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonDeleteShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintA4Shift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonPrintPosShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTableShifts.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneShifts.setViewportView(jTableShifts);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelButtons1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneShifts))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanelButtons1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneShifts, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(8, 8, 8))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(120, 120, 120)
                                .addComponent(jLabel42)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
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
                        .addComponent(jLabel42)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        final JDialog loadingDialog = new LoadingDialog(null, true);
		

		String query = "UPDATE OFFICE_WORKTIME SET "
				+ "W1 = ?, W2 = ?, W3 = ?, W4 = ?, W5 = ?, W6 = ?, W7 = ?, "
				+ "HF1 = ?, HT1 = ?, MF1 = ?, MT1 = ?, "
				+ "HF2 = ?, HT2 = ?, MF2 = ?, MT2 = ?, "
				+ "HF3 = ?, HT3 = ?, MF3 = ?, MT3 = ?, "
				+ "HF4 = ?, HT4 = ?, MF4 = ?, MT4 = ?, "
				+ "HF5 = ?, HT5 = ?, MF5 = ?, MT5 = ?, "
				+ "HF6 = ?, HT6 = ?, MF6 = ?, MT6 = ?, "
				+ "HF7 = ?, HT7 = ?, MF7 = ?, MT7 = ? "
				+ "WHERE OFFICE_NUMBER = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, jCheckBoxMon.isSelected() ? 1 : 0);
		databaseQuery.AddParam(2, jCheckBoxTue.isSelected() ? 1 : 0);
		databaseQuery.AddParam(3, jCheckBoxWed.isSelected() ? 1 : 0);
		databaseQuery.AddParam(4, jCheckBoxThu.isSelected() ? 1 : 0);
		databaseQuery.AddParam(5, jCheckBoxFri.isSelected() ? 1 : 0);
		databaseQuery.AddParam(6, jCheckBoxSat.isSelected() ? 1 : 0);
		databaseQuery.AddParam(7, jCheckBoxSun.isSelected() ? 1 : 0);
		databaseQuery.AddParam(8, jTextFieldMon1.getText());
		databaseQuery.AddParam(9, jTextFieldMon3.getText());
		databaseQuery.AddParam(10, jTextFieldMon2.getText());
		databaseQuery.AddParam(11, jTextFieldMon4.getText());
		databaseQuery.AddParam(12, jTextFieldTue1.getText());
		databaseQuery.AddParam(13, jTextFieldTue3.getText());
		databaseQuery.AddParam(14, jTextFieldTue2.getText());
		databaseQuery.AddParam(15, jTextFieldTue4.getText());
		databaseQuery.AddParam(16, jTextFieldWed1.getText());
		databaseQuery.AddParam(17, jTextFieldWed3.getText());
		databaseQuery.AddParam(18, jTextFieldWed2.getText());
		databaseQuery.AddParam(19, jTextFieldWed4.getText());
		databaseQuery.AddParam(20, jTextFieldThu1.getText());
		databaseQuery.AddParam(21, jTextFieldThu3.getText());
		databaseQuery.AddParam(22, jTextFieldThu2.getText());
		databaseQuery.AddParam(23, jTextFieldThu4.getText());
		databaseQuery.AddParam(24, jTextFieldFri1.getText());
		databaseQuery.AddParam(25, jTextFieldFri3.getText());
		databaseQuery.AddParam(26, jTextFieldFri2.getText());
		databaseQuery.AddParam(27, jTextFieldFri4.getText());
		databaseQuery.AddParam(28, jTextFieldSat1.getText());
		databaseQuery.AddParam(29, jTextFieldSat3.getText());
		databaseQuery.AddParam(30, jTextFieldSat2.getText());
		databaseQuery.AddParam(31, jTextFieldSat4.getText());
		databaseQuery.AddParam(32, jTextFieldSun1.getText());
		databaseQuery.AddParam(33, jTextFieldSun3.getText());
		databaseQuery.AddParam(34, jTextFieldSun2.getText());
		databaseQuery.AddParam(35, jTextFieldSun4.getText());
		databaseQuery.AddParam(36, Licence.GetOfficeNumber());
                System.out.println(databaseQuery.params.get(8).getValue());
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
						
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		jButtonExit.doClick();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jCheckBoxMonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxMonItemStateChanged
        boolean isWorkingDay = !jCheckBoxMon.isSelected();
		jTextFieldMon1.setEnabled(isWorkingDay);
		jTextFieldMon2.setEnabled(isWorkingDay);
		jTextFieldMon3.setEnabled(isWorkingDay);
		jTextFieldMon4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxMonItemStateChanged

    private void jCheckBoxTueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxTueItemStateChanged
        boolean isWorkingDay = !jCheckBoxTue.isSelected();
		jTextFieldTue1.setEnabled(isWorkingDay);
		jTextFieldTue2.setEnabled(isWorkingDay);
		jTextFieldTue3.setEnabled(isWorkingDay);
		jTextFieldTue4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxTueItemStateChanged

    private void jCheckBoxWedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxWedItemStateChanged
        boolean isWorkingDay = !jCheckBoxWed.isSelected();
		jTextFieldWed1.setEnabled(isWorkingDay);
		jTextFieldWed2.setEnabled(isWorkingDay);
		jTextFieldWed3.setEnabled(isWorkingDay);
		jTextFieldWed4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxWedItemStateChanged

    private void jCheckBoxThuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxThuItemStateChanged
        boolean isWorkingDay = !jCheckBoxThu.isSelected();
		jTextFieldThu1.setEnabled(isWorkingDay);
		jTextFieldThu2.setEnabled(isWorkingDay);
		jTextFieldThu3.setEnabled(isWorkingDay);
		jTextFieldThu4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxThuItemStateChanged

    private void jCheckBoxFriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxFriItemStateChanged
        boolean isWorkingDay = !jCheckBoxFri.isSelected();
		jTextFieldFri1.setEnabled(isWorkingDay);
		jTextFieldFri2.setEnabled(isWorkingDay);
		jTextFieldFri3.setEnabled(isWorkingDay);
		jTextFieldFri4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxFriItemStateChanged

    private void jCheckBoxSatItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxSatItemStateChanged
        boolean isWorkingDay = !jCheckBoxSat.isSelected();
		jTextFieldSat1.setEnabled(isWorkingDay);
		jTextFieldSat2.setEnabled(isWorkingDay);
		jTextFieldSat3.setEnabled(isWorkingDay);
		jTextFieldSat4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxSatItemStateChanged

    private void jCheckBoxSunItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxSunItemStateChanged
        boolean isWorkingDay = !jCheckBoxSun.isSelected();
		jTextFieldSun1.setEnabled(isWorkingDay);
		jTextFieldSun2.setEnabled(isWorkingDay);
		jTextFieldSun3.setEnabled(isWorkingDay);
		jTextFieldSun4.setEnabled(isWorkingDay);
    }//GEN-LAST:event_jCheckBoxSunItemStateChanged

    private void jButtonAddHolidayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddHolidayActionPerformed
        ClientAppSettingsHolidayAddEditDialog addEditdialog = new ClientAppSettingsHolidayAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTableHolidays();
        }
    }//GEN-LAST:event_jButtonAddHolidayActionPerformed

    private void jButtonEditHolidayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditHolidayActionPerformed
        if(jTableHolidays.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
        int rowId = jTableHolidays.convertRowIndexToModel(jTableHolidays.getSelectedRow());
        int tableId = tableHolidaysIdList.get(rowId);

        ClientAppSettingsHolidayAddEditDialog addEditdialog = new ClientAppSettingsHolidayAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTableHolidays();
        }
    }//GEN-LAST:event_jButtonEditHolidayActionPerformed

    private void jButtonDeleteHolidayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteHolidayActionPerformed
        if(jTableHolidays.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
        int rowId = jTableHolidays.convertRowIndexToModel(jTableHolidays.getSelectedRow());
        int tableId = tableHolidaysIdList.get(rowId);
        String tableValue = String.valueOf(jTableHolidays.getModel().getValueAt(rowId, 1)) + " (" + String.valueOf(jTableHolidays.getModel().getValueAt(rowId, 0)) + ")";

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati praznik " + tableValue, "Obriši praznik", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            final JDialog loadingDialog = new LoadingDialog(null, true);
            

            String query = "UPDATE HOLIDAYS SET IS_DELETED = 1 WHERE ID = ?";
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
                        RefreshTableHolidays();
                    }
                } catch (Exception ex) {
                    ClientAppLogger.GetInstance().ShowErrorLog(ex);
                }
            }
        }
    }//GEN-LAST:event_jButtonDeleteHolidayActionPerformed

    private void jButtonAddShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddShiftActionPerformed
		ClientAppSettingsShiftAddEditDialog addEditdialog = new ClientAppSettingsShiftAddEditDialog(null, true, -1);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTableShifts();
        }
    }//GEN-LAST:event_jButtonAddShiftActionPerformed

    private void jButtonEditShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditShiftActionPerformed
		 if(jTableShifts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
        int rowId = jTableShifts.convertRowIndexToModel(jTableShifts.getSelectedRow());
        int tableId = tableShiftsIdList.get(rowId);

        ClientAppSettingsShiftAddEditDialog addEditdialog = new ClientAppSettingsShiftAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        if(addEditdialog.changeSuccess){
            RefreshTableShifts();
        }
    }//GEN-LAST:event_jButtonEditShiftActionPerformed

    private void jButtonDeleteShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteShiftActionPerformed
		if(jTableShifts.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
        int rowId = jTableShifts.convertRowIndexToModel(jTableShifts.getSelectedRow());
        int tableId = tableShiftsIdList.get(rowId);
        String tableValue = String.valueOf(jTableShifts.getModel().getValueAt(rowId, 0));

        int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati smjenu " + tableValue, "Obriši smjenu", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            final JDialog loadingDialog = new LoadingDialog(null, true);
            

            String query = "UPDATE SHIFTS SET IS_DELETED = 1 WHERE ID = ?";
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
                        RefreshTableShifts();
                    }
                } catch (Exception ex) {
                    ClientAppLogger.GetInstance().ShowErrorLog(ex);
                }
            }
        }
    }//GEN-LAST:event_jButtonDeleteShiftActionPerformed

    private void jButtonPrintPosHolidayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosHolidayActionPerformed
        PrintUtils.PrintPosTable("Praznici", jTableHolidays, new int[]{0, 1, 2});
    }//GEN-LAST:event_jButtonPrintPosHolidayActionPerformed

    private void jButtonPrintA4HolidayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4HolidayActionPerformed
        PrintUtils.PrintA4Table("Praznici", "Praznici", jTableHolidays, "");
    }//GEN-LAST:event_jButtonPrintA4HolidayActionPerformed

    private void jButtonPrintA4ShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4ShiftActionPerformed
        PrintUtils.PrintA4Table("Smjene", "Smjene", jTableShifts, "");
    }//GEN-LAST:event_jButtonPrintA4ShiftActionPerformed

    private void jButtonPrintPosShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosShiftActionPerformed
        PrintUtils.PrintPosTable("Smjene", jTableShifts, new int[]{0, 1, 2});
    }//GEN-LAST:event_jButtonPrintPosShiftActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddHoliday;
    private javax.swing.JButton jButtonAddShift;
    private javax.swing.JButton jButtonDeleteHoliday;
    private javax.swing.JButton jButtonDeleteShift;
    private javax.swing.JButton jButtonEditHoliday;
    private javax.swing.JButton jButtonEditShift;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4Holiday;
    private javax.swing.JButton jButtonPrintA4Shift;
    private javax.swing.JButton jButtonPrintPosHoliday;
    private javax.swing.JButton jButtonPrintPosShift;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBoxFri;
    private javax.swing.JCheckBox jCheckBoxMon;
    private javax.swing.JCheckBox jCheckBoxSat;
    private javax.swing.JCheckBox jCheckBoxSun;
    private javax.swing.JCheckBox jCheckBoxThu;
    private javax.swing.JCheckBox jCheckBoxTue;
    private javax.swing.JCheckBox jCheckBoxWed;
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
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelButtons1;
    private javax.swing.JScrollPane jScrollPaneHolidays;
    private javax.swing.JScrollPane jScrollPaneShifts;
    private javax.swing.JTable jTableHolidays;
    private javax.swing.JTable jTableShifts;
    private javax.swing.JTextField jTextFieldFri1;
    private javax.swing.JTextField jTextFieldFri2;
    private javax.swing.JTextField jTextFieldFri3;
    private javax.swing.JTextField jTextFieldFri4;
    private javax.swing.JTextField jTextFieldMon1;
    private javax.swing.JTextField jTextFieldMon2;
    private javax.swing.JTextField jTextFieldMon3;
    private javax.swing.JTextField jTextFieldMon4;
    private javax.swing.JTextField jTextFieldSat1;
    private javax.swing.JTextField jTextFieldSat2;
    private javax.swing.JTextField jTextFieldSat3;
    private javax.swing.JTextField jTextFieldSat4;
    private javax.swing.JTextField jTextFieldSun1;
    private javax.swing.JTextField jTextFieldSun2;
    private javax.swing.JTextField jTextFieldSun3;
    private javax.swing.JTextField jTextFieldSun4;
    private javax.swing.JTextField jTextFieldThu1;
    private javax.swing.JTextField jTextFieldThu2;
    private javax.swing.JTextField jTextFieldThu3;
    private javax.swing.JTextField jTextFieldThu4;
    private javax.swing.JTextField jTextFieldTue1;
    private javax.swing.JTextField jTextFieldTue2;
    private javax.swing.JTextField jTextFieldTue3;
    private javax.swing.JTextField jTextFieldTue4;
    private javax.swing.JTextField jTextFieldWed1;
    private javax.swing.JTextField jTextFieldWed2;
    private javax.swing.JTextField jTextFieldWed3;
    private javax.swing.JTextField jTextFieldWed4;
    // End of variables declaration//GEN-END:variables
}
