/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.settings;

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
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Matej
 */
public class ClientAppSettingsStaffWorktimeDialog extends javax.swing.JDialog {
	private ArrayList<Integer> idList1 = new ArrayList<>();
	private ArrayList<Integer> idList2 = new ArrayList<>();
	private ArrayList<Integer> idList3 = new ArrayList<>();
	private ArrayList<Integer> idList4 = new ArrayList<>();
	private ArrayList<Integer> idList5 = new ArrayList<>();
	private ArrayList<Integer> idList6 = new ArrayList<>();
	private ArrayList<Integer> idList7 = new ArrayList<>();
	
	/**
	 * Creates new form ClientAppWarehouseCategoriesDialog
	 */
	public ClientAppSettingsStaffWorktimeDialog(java.awt.Frame parent, boolean modal) {
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
		
		jTableMon.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditMon.doClick();
				}
			}
		});
		jTableTue.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditTue.doClick();
				}
			}
		});
		jTableWed.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditWed.doClick();
				}
			}
		});
		jTableThu.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditThu.doClick();
				}
			}
		});
		jTableFri.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditFri.doClick();
				}
			}
		});
		jTableSat.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditSat.doClick();
				}
			}
		});
		jTableSun.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					jButtonEditSun.doClick();
				}
			}
		});

		jTableMon.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableMon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMon.getTableHeader().setReorderingAllowed(false);
		jTableMon.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableTue.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableTue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTue.getTableHeader().setReorderingAllowed(false);
		jTableTue.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableWed.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableWed.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableWed.getTableHeader().setReorderingAllowed(false);
		jTableWed.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableThu.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableThu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableThu.getTableHeader().setReorderingAllowed(false);
		jTableThu.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableFri.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableFri.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableFri.getTableHeader().setReorderingAllowed(false);
		jTableFri.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableSat.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableSat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableSat.getTableHeader().setReorderingAllowed(false);
		jTableSat.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableSun.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableSun.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableSun.getTableHeader().setReorderingAllowed(false);
		jTableSun.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jTableStaff.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableStaff.getTableHeader().setReorderingAllowed(false);
		jTableStaff.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		SetupCashRegistersAndWorktimes();
		
		RefreshTable();
		
		ClientAppUtils.SetupFocusTraversal(this);
	}
	
	private void SetupCashRegistersAndWorktimes(){
		final JDialog loadingDialog = new LoadingDialog(null, true);

		String queryCashRegisters = "SELECT CR_NUMBER FROM CASH_REGISTERS WHERE OFFICE_NUMBER = ?";
		String queryWorktime = "SELECT "
					+ "W1, W2, W3, W4, W5, W6, W7, "
					+ "HF1, HT1, MF1, MT1, "
					+ "HF2, HT2, MF2, MT2, "
					+ "HF3, HT3, MF3, MT3, "
					+ "HF4, HT4, MF4, MT4, "
					+ "HF5, HT5, MF5, MT5, "
					+ "HF6, HT6, MF6, MT6, "
					+ "HF7, HT7, MF7, MT7 "
					+ "FROM OFFICE_WORKTIME WHERE OFFICE_NUMBER = ?";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(2);
		multiDatabaseQuery.SetQuery(0, queryCashRegisters);
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.SetQuery(1, queryWorktime);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		
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
					// Cash registers
					DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
					defaultComboBoxModel.addElement("Izaberite");
					while (databaseQueryResult[0].next()) {
						String element = databaseQueryResult[0].getString(0);
						defaultComboBoxModel.addElement(element);
					}
					jComboBoxCashRegisters.setModel(defaultComboBoxModel);
					
					// Worktime
					if (databaseQueryResult[1].next()) {
						String[] resultStrings = new String[28];
						for (int i = 0; i < 28; ++i){
							resultStrings[i] = databaseQueryResult[1].getString(7 + i);
							if(resultStrings[i].length() == 1){
								resultStrings[i] = "0".concat(resultStrings[i]);
							}
						}

						int index = 0;
						if(databaseQueryResult[1].getInt(0) == 0){
							jLabelMon.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelMon.setText("");
							jPanelMon.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelMon);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(1) == 0){
							jLabelTue.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelTue.setText("");
							jPanelTue.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelTue);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(2) == 0){
							jLabelWed.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelWed.setText("");
							jPanelWed.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelWed);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(3) == 0){
							jLabelThu.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelThu.setText("");
							jPanelThu.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelThu);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(4) == 0){
							jLabelFri.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelFri.setText("");
							jPanelFri.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelFri);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(5) == 0){
							jLabelSat.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelSat.setText("");
							jPanelSat.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelSat);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}

						index += 4;
						if(databaseQueryResult[1].getInt(6) == 0){
							jLabelSun.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
						} else {
							jLabelSun.setText("");
							jPanelSun.setEnabled(false);
							List<Component> compList = Utils.GetAllComponents(jPanelSun);
							for (Component comp : compList) {
								comp.setEnabled(false);
							}
						}
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshTable(){
		String crNum;
		
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			crNum = "-1";
		} else {
			crNum = jComboBoxCashRegisters.getItemAt(jComboBoxCashRegisters.getSelectedIndex());
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT STAFF_WORKTIME.ID, DAY, STAFF.FIRST_NAME, STAFF.LAST_NAME, STAFF.OIB, HF, MF, HT, MT "
				+ "FROM STAFF_WORKTIME INNER JOIN STAFF ON STAFF_WORKTIME.STAFF_ID = STAFF.ID "
				+ "WHERE STAFF_WORKTIME.IS_DELETED = 0 AND O_NUM = ? AND CR_NUM = ?");
		databaseQuery.AddParam(1, Licence.GetOfficeNumber());
		databaseQuery.AddParam(2, crNum);
		
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
					CustomTableModel customTableModel1 = new CustomTableModel();
					CustomTableModel customTableModel2 = new CustomTableModel();
					CustomTableModel customTableModel3 = new CustomTableModel();
					CustomTableModel customTableModel4 = new CustomTableModel();
					CustomTableModel customTableModel5 = new CustomTableModel();
					CustomTableModel customTableModel6 = new CustomTableModel();
					CustomTableModel customTableModel7 = new CustomTableModel();
					customTableModel1.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel2.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel3.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel4.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel5.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel6.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					customTableModel7.setColumnIdentifiers(new String[] {"Od", "Do", "Djelatnik", "OIB"});
					ArrayList<Integer> idListTemp1 = new ArrayList<>();
					ArrayList<Integer> idListTemp2 = new ArrayList<>();
					ArrayList<Integer> idListTemp3 = new ArrayList<>();
					ArrayList<Integer> idListTemp4 = new ArrayList<>();
					ArrayList<Integer> idListTemp5 = new ArrayList<>();
					ArrayList<Integer> idListTemp6 = new ArrayList<>();
					ArrayList<Integer> idListTemp7 = new ArrayList<>();
					while (databaseQueryResult.next()) {
						String hf = databaseQueryResult.getString(5);
						String mf = databaseQueryResult.getString(6);
						String ht = databaseQueryResult.getString(7);
						String mt = databaseQueryResult.getString(8);
						if(hf.length() == 1){ hf = "0".concat(hf); }
						if(mf.length() == 1){ mf = "0".concat(mf); }
						if(ht.length() == 1){ ht = "0".concat(ht); }
						if(mt.length() == 1){ mt = "0".concat(mt); }
						
						Object[] rowData = new Object[4];
						rowData[0] = hf + ":" + mf;
						rowData[1] = ht + ":" + mt;
						rowData[2] = databaseQueryResult.getString(2) + " " + databaseQueryResult.getString(3);
						rowData[3] = databaseQueryResult.getString(4);
						
						if(databaseQueryResult.getInt(1) == 0){
							customTableModel1.addRow(rowData);
							idListTemp1.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 1){
							customTableModel2.addRow(rowData);
							idListTemp2.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 2){
							customTableModel3.addRow(rowData);
							idListTemp3.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 3){
							customTableModel4.addRow(rowData);
							idListTemp4.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 4){
							customTableModel5.addRow(rowData);
							idListTemp5.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 5){
							customTableModel6.addRow(rowData);
							idListTemp6.add(databaseQueryResult.getInt(0));
						} else if(databaseQueryResult.getInt(1) == 6){
							customTableModel7.addRow(rowData);
							idListTemp7.add(databaseQueryResult.getInt(0));
						}
					}
					jTableMon.setModel(customTableModel1);
					jTableTue.setModel(customTableModel2);
					jTableWed.setModel(customTableModel3);
					jTableThu.setModel(customTableModel4);
					jTableFri.setModel(customTableModel5);
					jTableSat.setModel(customTableModel6);
					jTableSun.setModel(customTableModel7);
					idList1 = idListTemp1;
					idList2 = idListTemp2;
					idList3 = idListTemp3;
					idList4 = idListTemp4;
					idList5 = idListTemp5;
					idList6 = idListTemp6;
					idList7 = idListTemp7;
					
					jTableMon.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneMon.getWidth() * 15 / 100);
					jTableMon.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneMon.getWidth() * 15 / 100);
					jTableMon.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneMon.getWidth() * 40 / 100);
					jTableMon.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneMon.getWidth() * 30 / 100);
					
					jTableTue.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTue.getWidth() * 15 / 100);
					jTableTue.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTue.getWidth() * 15 / 100);
					jTableTue.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTue.getWidth() * 40 / 100);
					jTableTue.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTue.getWidth() * 30 / 100);
					
					jTableWed.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneWed.getWidth() * 15 / 100);
					jTableWed.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneWed.getWidth() * 15 / 100);
					jTableWed.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneWed.getWidth() * 40 / 100);
					jTableWed.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneWed.getWidth() * 30 / 100);
					
					jTableThu.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneThu.getWidth() * 15 / 100);
					jTableThu.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneThu.getWidth() * 15 / 100);
					jTableThu.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneThu.getWidth() * 40 / 100);
					jTableThu.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneThu.getWidth() * 30 / 100);
					
					jTableFri.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFri.getWidth() * 15 / 100);
					jTableFri.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFri.getWidth() * 15 / 100);
					jTableFri.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFri.getWidth() * 40 / 100);
					jTableFri.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneFri.getWidth() * 30 / 100);
					
					jTableSat.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneSat.getWidth() * 15 / 100);
					jTableSat.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneSat.getWidth() * 15 / 100);
					jTableSat.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneSat.getWidth() * 40 / 100);
					jTableSat.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneSat.getWidth() * 30 / 100);
					
					jTableSun.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneSun.getWidth() * 15 / 100);
					jTableSun.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneSun.getWidth() * 15 / 100);
					jTableSun.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneSun.getWidth() * 40 / 100);
					jTableSun.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneSun.getWidth() * 30 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		RefreshStaffTable();
	}
	
	private void RefreshStaffTable(){
		final JDialog loadingDialog = new LoadingDialog(null, true);

		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT STAFF.ID, STAFF.FIRST_NAME, STAFF.LAST_NAME, STAFF.OIB, HF, MF, HT, MT "
				+ "FROM STAFF_WORKTIME INNER JOIN STAFF ON STAFF_WORKTIME.STAFF_ID = STAFF.ID "
				+ "WHERE STAFF_WORKTIME.IS_DELETED = 0 AND O_NUM = ?");
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
					class StaffTableInfo {
						int staffId;
						String staffName;
						String staffOib;
						int totalMinutes;
					}
					ArrayList<StaffTableInfo> staffListTemp = new ArrayList<>();
					while (databaseQueryResult.next()) {
						int hf = databaseQueryResult.getInt(4);
						int mf = databaseQueryResult.getInt(5);
						int ht = databaseQueryResult.getInt(6);
						int mt = databaseQueryResult.getInt(7);
						
						int minutes;
						if(hf < ht || hf == ht && mf <= mt){
							minutes = 60 * (ht - hf) + (mt - mf);
						} else {
							minutes = 24 * 60 - 60 * (hf - ht) + (mf - mt);
						}
						
						int staffId = databaseQueryResult.getInt(0);
						
						int staffListIndex = -1;
						for (int i = 0; i < staffListTemp.size(); ++i){
							if(staffListTemp.get(i).staffId == staffId){
								staffListIndex = i;
							}
						}
						if(staffListIndex == -1){
							StaffTableInfo staffTableInfo = new StaffTableInfo();
							staffTableInfo.staffId = staffId;
							staffTableInfo.totalMinutes = minutes;
							staffTableInfo.staffName = databaseQueryResult.getString(1) + " " + databaseQueryResult.getString(2);
							staffTableInfo.staffOib = databaseQueryResult.getString(3);
							staffListTemp.add(staffTableInfo);
						} else {
							staffListTemp.get(staffListIndex).totalMinutes += minutes;
						}
					}
					
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Djelatnik", "OIB", "Ukupno sati"});
					
					for (int i = 0; i < staffListTemp.size(); ++i) {
						String hours = "" + (staffListTemp.get(i).totalMinutes / 60);
						String minutes = "" + (staffListTemp.get(i).totalMinutes % 60);
						if(hours.length() == 1){ hours = "0".concat(hours); }
						if(minutes.length() == 1){ minutes = "0".concat(minutes); }
						
						Object[] rowData = new Object[3];
						rowData[0] = staffListTemp.get(i).staffName;
						rowData[1] = staffListTemp.get(i).staffOib;
						rowData[2] = hours + ":" + minutes;
						customTableModel.addRow(rowData);
					}
					jTableStaff.setModel(customTableModel);
					
					jTableStaff.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneStaff.getWidth() * 35 / 100);
					jTableStaff.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneStaff.getWidth() * 35 / 100);
					jTableStaff.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneStaff.getWidth() * 30 / 100);
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void AddWorktime(int day, String workTime, String dayString){
		String crNum = jComboBoxCashRegisters.getItemAt(jComboBoxCashRegisters.getSelectedIndex());
		
		ClientAppSelectStaffDialog selectDialog = new ClientAppSelectStaffDialog(null, true);
        selectDialog.setVisible(true);
        if(selectDialog.selectedId != -1){
			ClientAppSettingsStaffWorktimeAddEditDialog addEditdialog = new ClientAppSettingsStaffWorktimeAddEditDialog(null, true, -1, selectDialog.selectedId, crNum, day, workTime, selectDialog.selectedName, selectDialog.selectedOib, dayString);
			addEditdialog.setVisible(true);
			if(addEditdialog.changeSuccess){
				RefreshTable();
			}
        }
	}
	
	private void EditWorktime(int day, String workTime, String dayString, int worktimeId, String staffName, String staffOib){
		String crNum = jComboBoxCashRegisters.getItemAt(jComboBoxCashRegisters.getSelectedIndex());

		ClientAppSettingsStaffWorktimeAddEditDialog addEditdialog = new ClientAppSettingsStaffWorktimeAddEditDialog(null, true, worktimeId, -1, crNum, day, workTime, staffName, staffOib, dayString);
		addEditdialog.setVisible(true);
		if(addEditdialog.changeSuccess){
			RefreshTable();
		}
	}
	
	private void DeleteWorktime(int worktimeId, String staffName){
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati djelatnika " + staffName + " iz radnog vremena?", "Obriši djelatnika", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            final JDialog loadingDialog = new LoadingDialog(null, true);
            
            String query = "UPDATE STAFF_WORKTIME SET IS_DELETED = 1 WHERE ID = ?";
            DatabaseQuery databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, worktimeId);

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
	
	private void DeleteAllWorktime(int day, String dayName){
		String crNum = jComboBoxCashRegisters.getItemAt(jComboBoxCashRegisters.getSelectedIndex());

		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati cijeli raspored radnog vremena za " + dayName + " ?", "Obriši raspored radnog vremena", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            final JDialog loadingDialog = new LoadingDialog(null, true);
            
            String query = "UPDATE STAFF_WORKTIME SET IS_DELETED = 1 WHERE DAY = ? AND CR_NUM = ? AND O_NUM = ?";
            DatabaseQuery databaseQuery = new DatabaseQuery(query);
            databaseQuery.AddParam(1, day);
            databaseQuery.AddParam(2, crNum);
            databaseQuery.AddParam(3, Licence.GetOfficeNumber());

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
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jComboBoxCashRegisters = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelMon = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabelMon = new javax.swing.JLabel();
        jScrollPaneMon = new javax.swing.JScrollPane();
        jTableMon = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jButtonAddMon = new javax.swing.JButton();
        jButtonEditMon = new javax.swing.JButton();
        jButtonDeleteMon = new javax.swing.JButton();
        jButtonDeleteAllMon = new javax.swing.JButton();
        jPanelTue = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabelTue = new javax.swing.JLabel();
        jScrollPaneTue = new javax.swing.JScrollPane();
        jTableTue = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jButtonAddTue = new javax.swing.JButton();
        jButtonEditTue = new javax.swing.JButton();
        jButtonDeleteTue = new javax.swing.JButton();
        jButtonDeleteAllTue = new javax.swing.JButton();
        jPanelWed = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabelWed = new javax.swing.JLabel();
        jScrollPaneWed = new javax.swing.JScrollPane();
        jTableWed = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jButtonAddWed = new javax.swing.JButton();
        jButtonEditWed = new javax.swing.JButton();
        jButtonDeleteWed = new javax.swing.JButton();
        jButtonDeleteAllWed = new javax.swing.JButton();
        jPanelThu = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabelThu = new javax.swing.JLabel();
        jScrollPaneThu = new javax.swing.JScrollPane();
        jTableThu = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jButtonAddThu = new javax.swing.JButton();
        jButtonEditThu = new javax.swing.JButton();
        jButtonDeleteThu = new javax.swing.JButton();
        jButtonDeleteAllThu = new javax.swing.JButton();
        jPanelFri = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabelFri = new javax.swing.JLabel();
        jScrollPaneFri = new javax.swing.JScrollPane();
        jTableFri = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jButtonAddFri = new javax.swing.JButton();
        jButtonEditFri = new javax.swing.JButton();
        jButtonDeleteFri = new javax.swing.JButton();
        jButtonDeleteAllFri = new javax.swing.JButton();
        jPanelSat = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabelSat = new javax.swing.JLabel();
        jScrollPaneSat = new javax.swing.JScrollPane();
        jTableSat = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jButtonAddSat = new javax.swing.JButton();
        jButtonEditSat = new javax.swing.JButton();
        jButtonDeleteSat = new javax.swing.JButton();
        jButtonDeleteAllSat = new javax.swing.JButton();
        jPanelSun = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabelSun = new javax.swing.JLabel();
        jScrollPaneSun = new javax.swing.JScrollPane();
        jTableSun = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jButtonAddSun = new javax.swing.JButton();
        jButtonEditSun = new javax.swing.JButton();
        jButtonDeleteSun = new javax.swing.JButton();
        jButtonDeleteAllSun = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPaneStaff = new javax.swing.JScrollPane();
        jTableStaff = new javax.swing.JTable();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Raspored radnog vremena");
        setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Odabir blagajne"));

        jComboBoxCashRegisters.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxCashRegisters.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxCashRegistersItemStateChanged(evt);
            }
        });

        jLabel9.setText("Kasa broj:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Raspored radnog vremena");

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jComboBoxCashRegisters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(239, 239, 239)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jComboBoxCashRegisters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        jPanelMon.setBorder(javax.swing.BorderFactory.createTitledBorder("Ponedjeljak"));

        jLabel8.setText("Radno vrijeme:");

        jLabelMon.setText("radno vrijeme");

        jTableMon.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneMon.setViewportView(jTableMon);

        jButtonAddMon.setText("Dodaj");
        jButtonAddMon.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddMon.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddMonActionPerformed(evt);
            }
        });

        jButtonEditMon.setText("Uredi");
        jButtonEditMon.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditMon.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditMonActionPerformed(evt);
            }
        });

        jButtonDeleteMon.setText("Obriši");
        jButtonDeleteMon.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteMon.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteMonActionPerformed(evt);
            }
        });

        jButtonDeleteAllMon.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllMon.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllMon.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllMonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddMon, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditMon, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteMon, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllMon, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddMon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditMon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteMon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllMon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelMonLayout = new javax.swing.GroupLayout(jPanelMon);
        jPanelMon.setLayout(jPanelMonLayout);
        jPanelMonLayout.setHorizontalGroup(
            jPanelMonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneMon, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelMonLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelMon)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelMonLayout.setVerticalGroup(
            jPanelMonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMonLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelMonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabelMon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneMon, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelTue.setBorder(javax.swing.BorderFactory.createTitledBorder("Utorak"));

        jLabel10.setText("Radno vrijeme:");

        jLabelTue.setText("radno vrijeme");

        jTableTue.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTue.setViewportView(jTableTue);

        jButtonAddTue.setText("Dodaj");
        jButtonAddTue.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddTue.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddTue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddTueActionPerformed(evt);
            }
        });

        jButtonEditTue.setText("Uredi");
        jButtonEditTue.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditTue.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditTue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditTueActionPerformed(evt);
            }
        });

        jButtonDeleteTue.setText("Obriši");
        jButtonDeleteTue.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteTue.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteTue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteTueActionPerformed(evt);
            }
        });

        jButtonDeleteAllTue.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllTue.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllTue.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllTue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllTueActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddTue, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditTue, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteTue, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllTue, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddTue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditTue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteTue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllTue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelTueLayout = new javax.swing.GroupLayout(jPanelTue);
        jPanelTue.setLayout(jPanelTueLayout);
        jPanelTueLayout.setHorizontalGroup(
            jPanelTueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTueLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneTue, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelTueLayout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTue)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelTueLayout.setVerticalGroup(
            jPanelTueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTueLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelTueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabelTue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneTue, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelWed.setBorder(javax.swing.BorderFactory.createTitledBorder("Srijeda"));

        jLabel11.setText("Radno vrijeme:");

        jLabelWed.setText("radno vrijeme");

        jTableWed.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneWed.setViewportView(jTableWed);

        jButtonAddWed.setText("Dodaj");
        jButtonAddWed.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddWed.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddWed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddWedActionPerformed(evt);
            }
        });

        jButtonEditWed.setText("Uredi");
        jButtonEditWed.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditWed.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditWed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditWedActionPerformed(evt);
            }
        });

        jButtonDeleteWed.setText("Obriši");
        jButtonDeleteWed.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteWed.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteWed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteWedActionPerformed(evt);
            }
        });

        jButtonDeleteAllWed.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllWed.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllWed.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllWed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllWedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddWed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditWed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteWed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllWed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddWed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditWed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteWed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllWed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelWedLayout = new javax.swing.GroupLayout(jPanelWed);
        jPanelWed.setLayout(jPanelWedLayout);
        jPanelWedLayout.setHorizontalGroup(
            jPanelWedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelWedLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelWedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneWed, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelWedLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelWed)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelWedLayout.setVerticalGroup(
            jPanelWedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelWedLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelWedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabelWed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneWed, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelThu.setBorder(javax.swing.BorderFactory.createTitledBorder("Četvrtak"));

        jLabel12.setText("Radno vrijeme:");

        jLabelThu.setText("radno vrijeme");

        jTableThu.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneThu.setViewportView(jTableThu);

        jButtonAddThu.setText("Dodaj");
        jButtonAddThu.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddThu.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddThuActionPerformed(evt);
            }
        });

        jButtonEditThu.setText("Uredi");
        jButtonEditThu.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditThu.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditThuActionPerformed(evt);
            }
        });

        jButtonDeleteThu.setText("Obriši");
        jButtonDeleteThu.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteThu.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteThuActionPerformed(evt);
            }
        });

        jButtonDeleteAllThu.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllThu.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllThu.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllThuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddThu, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditThu, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteThu, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllThu, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddThu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditThu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteThu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllThu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelThuLayout = new javax.swing.GroupLayout(jPanelThu);
        jPanelThu.setLayout(jPanelThuLayout);
        jPanelThuLayout.setHorizontalGroup(
            jPanelThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelThuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneThu, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelThuLayout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelThu)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelThuLayout.setVerticalGroup(
            jPanelThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelThuLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabelThu))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneThu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelFri.setBorder(javax.swing.BorderFactory.createTitledBorder("Petak"));

        jLabel13.setText("Radno vrijeme:");

        jLabelFri.setText("radno vrijeme");

        jTableFri.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFri.setViewportView(jTableFri);

        jButtonAddFri.setText("Dodaj");
        jButtonAddFri.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddFri.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddFri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFriActionPerformed(evt);
            }
        });

        jButtonEditFri.setText("Uredi");
        jButtonEditFri.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditFri.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditFri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditFriActionPerformed(evt);
            }
        });

        jButtonDeleteFri.setText("Obriši");
        jButtonDeleteFri.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteFri.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteFri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteFriActionPerformed(evt);
            }
        });

        jButtonDeleteAllFri.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllFri.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllFri.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllFri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllFriActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddFri, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditFri, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteFri, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllFri, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddFri, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditFri, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteFri, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllFri, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelFriLayout = new javax.swing.GroupLayout(jPanelFri);
        jPanelFri.setLayout(jPanelFriLayout);
        jPanelFriLayout.setHorizontalGroup(
            jPanelFriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFriLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneFri, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelFriLayout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelFri)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelFriLayout.setVerticalGroup(
            jPanelFriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFriLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelFriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabelFri))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneFri, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelSat.setBorder(javax.swing.BorderFactory.createTitledBorder("Subota"));

        jLabel14.setText("Radno vrijeme:");

        jLabelSat.setText("radno vrijeme");

        jTableSat.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneSat.setViewportView(jTableSat);

        jButtonAddSat.setText("Dodaj");
        jButtonAddSat.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddSat.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddSat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSatActionPerformed(evt);
            }
        });

        jButtonEditSat.setText("Uredi");
        jButtonEditSat.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditSat.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditSat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditSatActionPerformed(evt);
            }
        });

        jButtonDeleteSat.setText("Obriši");
        jButtonDeleteSat.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteSat.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteSat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteSatActionPerformed(evt);
            }
        });

        jButtonDeleteAllSat.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllSat.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllSat.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllSat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllSatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddSat, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditSat, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteSat, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllSat, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddSat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditSat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteSat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllSat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelSatLayout = new javax.swing.GroupLayout(jPanelSat);
        jPanelSat.setLayout(jPanelSatLayout);
        jPanelSatLayout.setHorizontalGroup(
            jPanelSatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSat, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelSatLayout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSat)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelSatLayout.setVerticalGroup(
            jPanelSatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSatLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelSatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabelSat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneSat, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelSun.setBorder(javax.swing.BorderFactory.createTitledBorder("Nedjelja"));

        jLabel15.setText("Radno vrijeme:");

        jLabelSun.setText("radno vrijeme");

        jTableSun.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneSun.setViewportView(jTableSun);

        jButtonAddSun.setText("Dodaj");
        jButtonAddSun.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonAddSun.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonAddSun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSunActionPerformed(evt);
            }
        });

        jButtonEditSun.setText("Uredi");
        jButtonEditSun.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonEditSun.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonEditSun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditSunActionPerformed(evt);
            }
        });

        jButtonDeleteSun.setText("Obriši");
        jButtonDeleteSun.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteSun.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteSun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteSunActionPerformed(evt);
            }
        });

        jButtonDeleteAllSun.setText("<html> <div style=\"text-align: center\"> Obriši <br> sve </div> </html>");
        jButtonDeleteAllSun.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonDeleteAllSun.setPreferredSize(new java.awt.Dimension(80, 70));
        jButtonDeleteAllSun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllSunActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddSun, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEditSun, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteSun, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jButtonDeleteAllSun, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddSun, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonEditSun, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDeleteSun, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonDeleteAllSun, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        javax.swing.GroupLayout jPanelSunLayout = new javax.swing.GroupLayout(jPanelSun);
        jPanelSun.setLayout(jPanelSunLayout);
        jPanelSunLayout.setHorizontalGroup(
            jPanelSunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSunLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSun, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelSunLayout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSun)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelSunLayout.setVerticalGroup(
            jPanelSunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSunLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelSunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabelSun))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneSun, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tjedni raspored djelatnika"));

        jTableStaff.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneStaff.setViewportView(jTableStaff);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 6, 2, 6));
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
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanelMon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelTue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelWed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jPanelSun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jPanelThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanelFri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanelSat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonExit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelMon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelTue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelWed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelFri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelSat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanelSun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddMonActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(0, jLabelMon.getText(), "Ponedjeljak");
    }//GEN-LAST:event_jButtonAddMonActionPerformed

    private void jButtonEditMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditMonActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableMon.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableMon.convertRowIndexToModel(jTableMon.getSelectedRow());
        int tableId = idList1.get(rowId);
		
		String staffName = String.valueOf(jTableMon.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableMon.getModel().getValueAt(rowId, 3));

		EditWorktime(0, jLabelMon.getText(), "Ponedjeljak", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditMonActionPerformed

    private void jButtonDeleteMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteMonActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableMon.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableMon.convertRowIndexToModel(jTableMon.getSelectedRow());
        int tableId = idList1.get(rowId);

        String staffName = String.valueOf(jTableMon.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteMonActionPerformed

    private void jButtonDeleteAllMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllMonActionPerformed
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(0, "Ponedjeljak");
    }//GEN-LAST:event_jButtonDeleteAllMonActionPerformed

    private void jComboBoxCashRegistersItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxCashRegistersItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			RefreshTable();
		}
    }//GEN-LAST:event_jComboBoxCashRegistersItemStateChanged

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
		Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonAddTueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddTueActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(1, jLabelTue.getText(), "Utorak");
    }//GEN-LAST:event_jButtonAddTueActionPerformed

    private void jButtonEditTueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditTueActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableTue.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableTue.convertRowIndexToModel(jTableTue.getSelectedRow());
        int tableId = idList2.get(rowId);
		
		String staffName = String.valueOf(jTableTue.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableTue.getModel().getValueAt(rowId, 3));

		EditWorktime(1, jLabelTue.getText(), "Utorak", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditTueActionPerformed

    private void jButtonDeleteTueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteTueActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableTue.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableTue.convertRowIndexToModel(jTableTue.getSelectedRow());
        int tableId = idList2.get(rowId);

        String staffName = String.valueOf(jTableTue.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteTueActionPerformed

    private void jButtonDeleteAllTueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllTueActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(1, "Utorak");
    }//GEN-LAST:event_jButtonDeleteAllTueActionPerformed

    private void jButtonAddWedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddWedActionPerformed
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(2, jLabelWed.getText(), "Srijeda");
    }//GEN-LAST:event_jButtonAddWedActionPerformed

    private void jButtonEditWedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditWedActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableWed.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableWed.convertRowIndexToModel(jTableWed.getSelectedRow());
        int tableId = idList3.get(rowId);
		
		String staffName = String.valueOf(jTableWed.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableWed.getModel().getValueAt(rowId, 3));

		EditWorktime(2, jLabelWed.getText(), "Srijeda", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditWedActionPerformed

    private void jButtonDeleteWedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteWedActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableWed.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableWed.convertRowIndexToModel(jTableWed.getSelectedRow());
        int tableId = idList3.get(rowId);

        String staffName = String.valueOf(jTableWed.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteWedActionPerformed

    private void jButtonDeleteAllWedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllWedActionPerformed
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(2, "Srijeda");
    }//GEN-LAST:event_jButtonDeleteAllWedActionPerformed

    private void jButtonAddThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddThuActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(3, jLabelThu.getText(), "Četvrtak");
    }//GEN-LAST:event_jButtonAddThuActionPerformed

    private void jButtonEditThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditThuActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableThu.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableThu.convertRowIndexToModel(jTableThu.getSelectedRow());
        int tableId = idList4.get(rowId);
		
		String staffName = String.valueOf(jTableThu.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableThu.getModel().getValueAt(rowId, 3));

		EditWorktime(3, jLabelThu.getText(), "Četvrtak", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditThuActionPerformed

    private void jButtonDeleteThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteThuActionPerformed
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableThu.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableThu.convertRowIndexToModel(jTableThu.getSelectedRow());
        int tableId = idList4.get(rowId);

        String staffName = String.valueOf(jTableThu.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteThuActionPerformed

    private void jButtonDeleteAllThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllThuActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(3, "Četvrtak");
    }//GEN-LAST:event_jButtonDeleteAllThuActionPerformed

    private void jButtonAddFriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFriActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(4, jLabelFri.getText(), "Petak");
    }//GEN-LAST:event_jButtonAddFriActionPerformed

    private void jButtonEditFriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditFriActionPerformed
       if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableFri.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableFri.convertRowIndexToModel(jTableFri.getSelectedRow());
        int tableId = idList5.get(rowId);
		
		String staffName = String.valueOf(jTableFri.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableFri.getModel().getValueAt(rowId, 3));

		EditWorktime(4, jLabelFri.getText(), "Petak", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditFriActionPerformed

    private void jButtonDeleteFriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteFriActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableFri.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableFri.convertRowIndexToModel(jTableFri.getSelectedRow());
        int tableId = idList5.get(rowId);

        String staffName = String.valueOf(jTableFri.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteFriActionPerformed

    private void jButtonDeleteAllFriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllFriActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(4, "Petak");
    }//GEN-LAST:event_jButtonDeleteAllFriActionPerformed

    private void jButtonAddSatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSatActionPerformed
		if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(5, jLabelSat.getText(), "Subota");
    }//GEN-LAST:event_jButtonAddSatActionPerformed

    private void jButtonEditSatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditSatActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableSat.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableSat.convertRowIndexToModel(jTableSat.getSelectedRow());
        int tableId = idList6.get(rowId);
		
		String staffName = String.valueOf(jTableSat.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableSat.getModel().getValueAt(rowId, 3));

		EditWorktime(5, jLabelSat.getText(), "Subota", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditSatActionPerformed

    private void jButtonDeleteSatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteSatActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableSat.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableSat.convertRowIndexToModel(jTableSat.getSelectedRow());
        int tableId = idList6.get(rowId);

        String staffName = String.valueOf(jTableSat.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteSatActionPerformed

    private void jButtonDeleteAllSatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllSatActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(5, "Subota");
    }//GEN-LAST:event_jButtonDeleteAllSatActionPerformed

    private void jButtonAddSunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSunActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		AddWorktime(6, jLabelSun.getText(), "Nedjelja");
    }//GEN-LAST:event_jButtonAddSunActionPerformed

    private void jButtonEditSunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditSunActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableSun.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableSun.convertRowIndexToModel(jTableSun.getSelectedRow());
        int tableId = idList7.get(rowId);
		
		String staffName = String.valueOf(jTableSun.getModel().getValueAt(rowId, 2));
		String staffOib = String.valueOf(jTableSun.getModel().getValueAt(rowId, 3));

		EditWorktime(6, jLabelSun.getText(), "Nedjelja", tableId, staffName, staffOib);
    }//GEN-LAST:event_jButtonEditSunActionPerformed

    private void jButtonDeleteSunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteSunActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		if(jTableSun.getSelectedRow() == -1){
            ClientAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableSun.convertRowIndexToModel(jTableSun.getSelectedRow());
        int tableId = idList7.get(rowId);

        String staffName = String.valueOf(jTableSun.getModel().getValueAt(rowId, 2));
		
        DeleteWorktime(tableId, staffName);
    }//GEN-LAST:event_jButtonDeleteSunActionPerformed

    private void jButtonDeleteAllSunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllSunActionPerformed
        if (jComboBoxCashRegisters.getSelectedIndex() <= 0){
			ClientAppLogger.GetInstance().ShowMessage("Odaberite broj kase");
			return;
		}
		
		DeleteAllWorktime(6, "Nedjelja");
    }//GEN-LAST:event_jButtonDeleteAllSunActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddFri;
    private javax.swing.JButton jButtonAddMon;
    private javax.swing.JButton jButtonAddSat;
    private javax.swing.JButton jButtonAddSun;
    private javax.swing.JButton jButtonAddThu;
    private javax.swing.JButton jButtonAddTue;
    private javax.swing.JButton jButtonAddWed;
    private javax.swing.JButton jButtonDeleteAllFri;
    private javax.swing.JButton jButtonDeleteAllMon;
    private javax.swing.JButton jButtonDeleteAllSat;
    private javax.swing.JButton jButtonDeleteAllSun;
    private javax.swing.JButton jButtonDeleteAllThu;
    private javax.swing.JButton jButtonDeleteAllTue;
    private javax.swing.JButton jButtonDeleteAllWed;
    private javax.swing.JButton jButtonDeleteFri;
    private javax.swing.JButton jButtonDeleteMon;
    private javax.swing.JButton jButtonDeleteSat;
    private javax.swing.JButton jButtonDeleteSun;
    private javax.swing.JButton jButtonDeleteThu;
    private javax.swing.JButton jButtonDeleteTue;
    private javax.swing.JButton jButtonDeleteWed;
    private javax.swing.JButton jButtonEditFri;
    private javax.swing.JButton jButtonEditMon;
    private javax.swing.JButton jButtonEditSat;
    private javax.swing.JButton jButtonEditSun;
    private javax.swing.JButton jButtonEditThu;
    private javax.swing.JButton jButtonEditTue;
    private javax.swing.JButton jButtonEditWed;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JComboBox<String> jComboBoxCashRegisters;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFri;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelMon;
    private javax.swing.JLabel jLabelSat;
    private javax.swing.JLabel jLabelSun;
    private javax.swing.JLabel jLabelThu;
    private javax.swing.JLabel jLabelTue;
    private javax.swing.JLabel jLabelWed;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelFri;
    private javax.swing.JPanel jPanelMon;
    private javax.swing.JPanel jPanelSat;
    private javax.swing.JPanel jPanelSun;
    private javax.swing.JPanel jPanelThu;
    private javax.swing.JPanel jPanelTue;
    private javax.swing.JPanel jPanelWed;
    private javax.swing.JScrollPane jScrollPaneFri;
    private javax.swing.JScrollPane jScrollPaneMon;
    private javax.swing.JScrollPane jScrollPaneSat;
    private javax.swing.JScrollPane jScrollPaneStaff;
    private javax.swing.JScrollPane jScrollPaneSun;
    private javax.swing.JScrollPane jScrollPaneThu;
    private javax.swing.JScrollPane jScrollPaneTue;
    private javax.swing.JScrollPane jScrollPaneWed;
    private javax.swing.JTable jTableFri;
    private javax.swing.JTable jTableMon;
    private javax.swing.JTable jTableSat;
    private javax.swing.JTable jTableStaff;
    private javax.swing.JTable jTableSun;
    private javax.swing.JTable jTableThu;
    private javax.swing.JTable jTableTue;
    private javax.swing.JTable jTableWed;
    // End of variables declaration//GEN-END:variables
}
