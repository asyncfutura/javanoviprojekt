/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.ui.reports;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import hr.adinfo.utils.Pair;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReportsStaffWorkHoursDialog extends javax.swing.JDialog {

	private boolean setupDone;
	private boolean overtimeWork;
	
	ArrayList<Date> holidaysList = new ArrayList<>();
	
	/**
	 * Creates new form ClientAppWarehouseMaterialsDialog
	 */
	public ClientAppReportsStaffWorkHoursDialog(java.awt.Frame parent, boolean modal) {
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
						BasicDatePickerUI pickerUI1 = (BasicDatePickerUI) jXDatePickerFrom.getUI();
						BasicDatePickerUI pickerUI2 = (BasicDatePickerUI) jXDatePickerTo.getUI();
						if (pickerUI1.isPopupVisible()) {
							pickerUI1.hidePopup();
							return false;
						}
						if (pickerUI2.isPopupVisible()) {
							pickerUI2.hidePopup();
							return false;
						}
						
						jButtonExit.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F4){
						jButtonPrintPosInvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F5){
						jButtonPrintA4InvoicesTable.doClick();
					} else if(ke.getKeyCode() == KeyEvent.VK_F1){
						jXDatePickerFrom.requestFocusInWindow();
					}
				}
				
				return false;
			}
		});
		
		jTable1.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getTableHeader().setReorderingAllowed(false);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		jXDatePickerFrom.setFormats("dd.MM.yyyy");
		jXDatePickerFrom.getEditor().setEditable(false);
		jXDatePickerFrom.setDate(new Date());
		jXDatePickerFrom.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerFrom.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerFrom.getMonthView() && e.getOppositeComponent() != null) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		jXDatePickerTo.setFormats("dd.MM.yyyy");
		jXDatePickerTo.getEditor().setEditable(false);
		jXDatePickerTo.setDate(new Date());
		jXDatePickerTo.getEditor().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				BasicDatePickerUI pickerUI = (BasicDatePickerUI) jXDatePickerTo.getUI();
				if (!pickerUI.isPopupVisible() && e.getOppositeComponent() != getRootPane() && e.getOppositeComponent() != jXDatePickerTo.getMonthView() && e.getOppositeComponent() != null) {
					pickerUI.toggleShowPopup();
				}
			}
			
			@Override
			public void focusLost(FocusEvent e) {}
		});
		
		ClientAppUtils.SetupFocusTraversal(this);
		
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
							String[] resultStrings = new String[28];
							for (int i = 0; i < 28; ++i){
								resultStrings[i] = databaseQueryResult.getString(7 + i);
								if(resultStrings[i].length() == 1){
									resultStrings[i] = "0".concat(resultStrings[i]);
								}
							}
							
							int index = 0;
							if(databaseQueryResult.getInt(0) == 0){
								jLabelMon.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelMon.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(1) == 0){
								jLabelTue.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelTue.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(2) == 0){
								jLabelWed.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelWed.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(3) == 0){
								jLabelThu.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelThu.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(4) == 0){
								jLabelFri.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelFri.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(5) == 0){
								jLabelSat.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelSat.setText("");
							}
							
							index += 4;
							if(databaseQueryResult.getInt(6) == 0){
								jLabelSun.setText(resultStrings[index] + ":" + resultStrings[index + 2] + " - " + resultStrings[index + 1] + ":" + resultStrings[index + 3]);
							} else {
								jLabelSun.setText("");
							}
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		overtimeWork = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_OVERTIME_WORK.ordinal());
		
		LoadHolidays();
		
		setupDone = true;
		RefreshTable();
	}
	
	private void RefreshTable(){
		if(!setupDone)
			return;
		
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		Date dateFrom;
		Date dateTo;
		try {
			dateFrom = new SimpleDateFormat("dd.MM.yyyy").parse(dateFromString);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Od");
			return;
		}
		try {
			dateTo = new SimpleDateFormat("dd.MM.yyyy").parse(dateToString);
		} catch (ParseException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Do");
			return;
		}
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String queryLocalInvoices = "SELECT DISTINCT STAFF.ID, STAFF.FIRST_NAME, STAFF.LAST_NAME, STAFF.OIB, LOCAL_INVOICES.I_DATE, COUNT(DISTINCT HOUR(LOCAL_INVOICES.I_TIME)) "
				+ "FROM LOCAL_INVOICES "
				+ "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
				+ "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
				+ "AND I_DATE >= ? AND I_DATE <= ? "
				+ "AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?, ?) "
				+ "GROUP BY STAFF.ID, STAFF.FIRST_NAME, STAFF.LAST_NAME, STAFF.OIB, LOCAL_INVOICES.I_DATE";
		String queryInvoices = queryLocalInvoices.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
		boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
		if(!isProduction){
			queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
			queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
		}
		
		String queryWorktime = "SELECT STAFF_ID, DAY, HF, MF, HT, MT FROM STAFF_WORKTIME WHERE IS_DELETED = 0";

		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
		multiDatabaseQuery.SetQuery(0, queryLocalInvoices);
		multiDatabaseQuery.SetQuery(1, queryInvoices);
		multiDatabaseQuery.SetQuery(2, queryWorktime);
		for (int i = 0; i < 2; ++i){
			multiDatabaseQuery.AddParam(i, 1, Licence.GetOfficeNumber());
			multiDatabaseQuery.AddParam(i, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
			multiDatabaseQuery.AddParam(i, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
			multiDatabaseQuery.AddParam(i, 4, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
			multiDatabaseQuery.AddParam(i, 5, Values.PAYMENT_METHOD_TYPE_OFFER);
			multiDatabaseQuery.AddParam(i, 6, Values.PAYMENT_METHOD_TYPE_SUBTOTAL);
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
					class StaffWorktimeInfo {
						int staffId;
						int day;
						int hf;
						int mf;
						int ht;
						int mt;
					}
					ArrayList<StaffWorktimeInfo> staffWorktimeList = new ArrayList<>();
					
					// Staff worktime
					while (databaseQueryResult[2].next()) {
						StaffWorktimeInfo staffWorktimeInfo = new StaffWorktimeInfo();
						staffWorktimeInfo.staffId = databaseQueryResult[2].getInt(0);
						staffWorktimeInfo.day = databaseQueryResult[2].getInt(1);
						staffWorktimeInfo.hf = databaseQueryResult[2].getInt(2);
						staffWorktimeInfo.mf = databaseQueryResult[2].getInt(3);
						staffWorktimeInfo.ht = databaseQueryResult[2].getInt(4);
						staffWorktimeInfo.mt = databaseQueryResult[2].getInt(5);
						staffWorktimeList.add(staffWorktimeInfo);
					}
					
					class StaffTableInfo {
						int staffId;
						String staffName;
						String staffOib;
						int minutesNormal;
						int minutesNight;
						int minutesSunday;
						int minutesHoliday;
					}
					ArrayList<StaffTableInfo> staffTableList = new ArrayList<>();
					
					// Local invoices
					while (databaseQueryResult[0].next()) {
						int staffId = databaseQueryResult[0].getInt(0);
						
						Date date;
						try {
							date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[0].getString(4));
						} catch (Exception ex) {
							continue;
						}
						
						Calendar c = Calendar.getInstance();
						c.setTime(date);
						int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
						int day;
						if(dayOfWeek == Calendar.MONDAY){
							day = 0;
						} else if(dayOfWeek == Calendar.TUESDAY){
							day = 1;
						} else if(dayOfWeek == Calendar.WEDNESDAY){
							day = 2;
						} else if(dayOfWeek == Calendar.THURSDAY){
							day = 3;
						} else if(dayOfWeek == Calendar.FRIDAY){
							day = 4;
						} else if(dayOfWeek == Calendar.SATURDAY){
							day = 5;
						} else {
							day = 6;
						}
						
						int staffWorktimeListIndex = -1;
						for (int i = 0; i < staffWorktimeList.size(); ++i){
							if(staffWorktimeList.get(i).staffId == staffId && staffWorktimeList.get(i).day == day){
								staffWorktimeListIndex = i;
							}
						}
						
						int minutesNormal = 0;
						int minutesNight = 0;
						int minutesSunday = 0;
						int minutesHoliday = 0;
						if(staffWorktimeListIndex == -1){
							int minutes = 8 * 60;
							if(overtimeWork){
								minutes = databaseQueryResult[0].getInt(5) * 60;
							}
							if(IsHoliday(date)){
								minutesHoliday = minutes;
							} else if (dayOfWeek == Calendar.SUNDAY){
								minutesSunday = minutes;
							} else {
								minutesNormal = minutes;
							}
						} else {
							int hf = staffWorktimeList.get(staffWorktimeListIndex).hf;
							int mf = staffWorktimeList.get(staffWorktimeListIndex).mf;
							int ht = staffWorktimeList.get(staffWorktimeListIndex).ht;
							int mt = staffWorktimeList.get(staffWorktimeListIndex).mt;
							
							int minutes;
							if(hf < ht || hf == ht && mf <= mt){
								minutes = 60 * (ht - hf) + (mt - mf);
							} else {
								minutes = 24 * 60 - 60 * (hf - ht) + (mf - mt);
							}
							
							if(IsHoliday(date)){
								minutesHoliday = minutes;
							} else if (IsNight(hf, ht)){
								minutesNight = GetNightMinutes(hf, mf, ht, mt);
								if (dayOfWeek == Calendar.SUNDAY){
									minutesSunday = minutes - minutesNight;
								} else {
									minutesNormal = minutes - minutesNight;
								}
							} else if (dayOfWeek == Calendar.SUNDAY){
								minutesSunday = minutes;
							} else {
								minutesNormal = minutes;
							}
						}
						
						int staffListIndex = -1;
						for (int i = 0; i < staffTableList.size(); ++i){
							if(staffTableList.get(i).staffId == staffId){
								staffListIndex = i;
							}
						}
						if(staffListIndex == -1){
							StaffTableInfo staffTableInfo = new StaffTableInfo();
							staffTableInfo.staffId = staffId;
							staffTableInfo.staffName = databaseQueryResult[0].getString(1) + " " + databaseQueryResult[0].getString(2);
							staffTableInfo.staffOib = databaseQueryResult[0].getString(3);
							staffTableInfo.minutesNormal = minutesNormal;
							staffTableInfo.minutesNight = minutesNight;
							staffTableInfo.minutesSunday = minutesSunday;
							staffTableInfo.minutesHoliday = minutesHoliday;
							staffTableList.add(staffTableInfo);
						} else {
							staffTableList.get(staffListIndex).minutesNormal += minutesNormal;
							staffTableList.get(staffListIndex).minutesNight += minutesNight;
							staffTableList.get(staffListIndex).minutesSunday += minutesSunday;
							staffTableList.get(staffListIndex).minutesHoliday += minutesHoliday;
						}
					}
					
					// Invoices
					while (databaseQueryResult[1].next()) {
						int staffId = databaseQueryResult[1].getInt(0);
						
						Date date;
						try {
							date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult[1].getString(4));
						} catch (Exception ex) {
							continue;
						}
						
						Calendar c = Calendar.getInstance();
						c.setTime(date);
						int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
						int day;
						if(dayOfWeek == Calendar.MONDAY){
							day = 0;
						} else if(dayOfWeek == Calendar.TUESDAY){
							day = 1;
						} else if(dayOfWeek == Calendar.WEDNESDAY){
							day = 2;
						} else if(dayOfWeek == Calendar.THURSDAY){
							day = 3;
						} else if(dayOfWeek == Calendar.FRIDAY){
							day = 4;
						} else if(dayOfWeek == Calendar.SATURDAY){
							day = 5;
						} else {
							day = 6;
						}
						
						int staffWorktimeListIndex = -1;
						for (int i = 0; i < staffWorktimeList.size(); ++i){
							if(staffWorktimeList.get(i).staffId == staffId && staffWorktimeList.get(i).day == day){
								staffWorktimeListIndex = i;
							}
						}
						
						int minutesNormal = 0;
						int minutesNight = 0;
						int minutesSunday = 0;
						int minutesHoliday = 0;
						if(staffWorktimeListIndex == -1){
							int minutes = 8 * 60;
							if(overtimeWork){
								minutes = databaseQueryResult[1].getInt(5) * 60;
							}
							if(IsHoliday(date)){
								minutesHoliday = minutes;
							} else if (dayOfWeek == Calendar.SUNDAY){
								minutesSunday = minutes;
							} else {
								minutesNormal = minutes;
							}
						} else {
							int hf = staffWorktimeList.get(staffWorktimeListIndex).hf;
							int mf = staffWorktimeList.get(staffWorktimeListIndex).mf;
							int ht = staffWorktimeList.get(staffWorktimeListIndex).ht;
							int mt = staffWorktimeList.get(staffWorktimeListIndex).mt;

							int minutes;
							if(hf < ht || hf == ht && mf <= mt){
								minutes = 60 * (ht - hf) + (mt - mf);
							} else {
								minutes = 24 * 60 - 60 * (hf - ht) + (mf - mt);
							}
							
							if(IsHoliday(date)){
								minutesHoliday = minutes;
							} else if (IsNight(hf, ht)){
								minutesNight = GetNightMinutes(hf, mf, ht, mt);
								if (dayOfWeek == Calendar.SUNDAY){
									minutesSunday = minutes - minutesNight;
								} else {
									minutesNormal = minutes - minutesNight;
								}
							} else if (dayOfWeek == Calendar.SUNDAY){
								minutesSunday = minutes;
							} else {
								minutesNormal = minutes;
							}
						}
						
						int staffListIndex = -1;
						for (int i = 0; i < staffTableList.size(); ++i){
							if(staffTableList.get(i).staffId == staffId){
								staffListIndex = i;
							}
						}
						if(staffListIndex == -1){
							StaffTableInfo staffTableInfo = new StaffTableInfo();
							staffTableInfo.staffId = staffId;
							staffTableInfo.staffName = databaseQueryResult[1].getString(1) + " " + databaseQueryResult[1].getString(2);
							staffTableInfo.staffOib = databaseQueryResult[1].getString(3);
							staffTableInfo.minutesNormal = minutesNormal;
							staffTableInfo.minutesNight = minutesNight;
							staffTableInfo.minutesSunday = minutesSunday;
							staffTableInfo.minutesHoliday = minutesHoliday;
							staffTableList.add(staffTableInfo);
						} else {
							staffTableList.get(staffListIndex).minutesNormal += minutesNormal;
							staffTableList.get(staffListIndex).minutesNight += minutesNight;
							staffTableList.get(staffListIndex).minutesSunday += minutesSunday;
							staffTableList.get(staffListIndex).minutesHoliday += minutesHoliday;
						}
					}
					
					CustomTableModel customTableModel = new CustomTableModel();
					customTableModel.setColumnIdentifiers(new String[] {"Ime", "OIB", "Redovni rad", "Noćni rad", "Rad nedjeljom", "Rad praznicima", "Ukupno"});
					for (int i = 0; i < staffTableList.size(); ++i) {
						String hoursNormal = "" + (staffTableList.get(i).minutesNormal / 60);
						String minutesNormal = "" + (staffTableList.get(i).minutesNormal % 60);
						if(hoursNormal.length() == 1){ hoursNormal = "0".concat(hoursNormal); }
						if(minutesNormal.length() == 1){ minutesNormal = "0".concat(minutesNormal); }
					
						String hoursNight = "" + (staffTableList.get(i).minutesNight / 60);
						String minutesNight = "" + (staffTableList.get(i).minutesNight % 60);
						if(hoursNight.length() == 1){ hoursNight = "0".concat(hoursNight); }
						if(minutesNight.length() == 1){ minutesNight = "0".concat(minutesNight); }
						
						String hoursSunday = "" + (staffTableList.get(i).minutesSunday / 60);
						String minutesSunday = "" + (staffTableList.get(i).minutesSunday % 60);
						if(hoursSunday.length() == 1){ hoursSunday = "0".concat(hoursSunday); }
						if(minutesSunday.length() == 1){ minutesSunday = "0".concat(minutesSunday); }
						
						String hoursHoliday = "" + (staffTableList.get(i).minutesHoliday / 60);
						String minutesHoliday = "" + (staffTableList.get(i).minutesHoliday % 60);
						if(hoursHoliday.length() == 1){ hoursHoliday = "0".concat(hoursHoliday); }
						if(minutesHoliday.length() == 1){ minutesHoliday = "0".concat(minutesHoliday); }
						
						int minutesSum = staffTableList.get(i).minutesNormal + staffTableList.get(i).minutesNight + staffTableList.get(i).minutesSunday + staffTableList.get(i).minutesHoliday;
						String hoursTotal = "" + (minutesSum / 60);
						String minutesTotal = "" + (minutesSum % 60);
						if(hoursTotal.length() == 1){ hoursTotal = "0".concat(hoursTotal); }
						if(minutesTotal.length() == 1){ minutesTotal = "0".concat(minutesTotal); }
						
						Object[] rowData = new Object[7];
						rowData[0] = staffTableList.get(i).staffName;
						rowData[1] = staffTableList.get(i).staffOib;
						rowData[2] = hoursNormal + ":" + minutesNormal;
						rowData[3] = hoursNight + ":" + minutesNight;
						rowData[4] = hoursSunday + ":" + minutesSunday;
						rowData[5] = hoursHoliday + ":" + minutesHoliday;
						rowData[6] = hoursTotal + ":" + minutesTotal;
						customTableModel.addRow(rowData);
					}
					jTable1.setModel(customTableModel);
					
					jTable1.getColumnModel().getColumn(0).setPreferredWidth(jScrollPane1.getWidth() * 20 / 100);
					jTable1.getColumnModel().getColumn(1).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(2).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(5).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
					jTable1.getColumnModel().getColumn(6).setPreferredWidth(jScrollPane1.getWidth() * 15 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}

	boolean IsHoliday(Date date){
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date);
		int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
		int month1 = calendar1.get(Calendar.MONTH);
		int year1 = calendar1.get(Calendar.YEAR);
		for (int i = 0; i < holidaysList.size(); ++i){
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(holidaysList.get(i));
			int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
			int month2 = calendar2.get(Calendar.MONTH);
			int year2 = calendar2.get(Calendar.YEAR);
			
			if (day1 == day2 && month1 == month2 && year1 == year2){
				return true;
			}
		}
		
		return false;
	}
	boolean IsNight(int hf, int ht){
		if (hf <= ht){
			return hf < 6 || ht >= 22;
		} else {
			return true;
		}
	}
	int GetNightMinutes(int hf, int mf, int ht, int mt){
		int nightMinutes = 0;
		
		if(hf < ht || hf == ht && mf <= mt){
			if (hf < 6){
				nightMinutes += 60 * (6 - hf) + (0 - mf);
			}
			if (ht > 22){
				nightMinutes += 60 * (ht - 22) + (mt - 0);
			}
		} else {
			if (hf < 22){
				nightMinutes += 60 * 2;
			} else {
				nightMinutes += 60 * (24 - hf) + (0 - mf);
			}
			if (ht > 6){
				nightMinutes += 60 * 6;
			} else {
				nightMinutes += 60 * (ht - 0) + (mt - 0);
			}
		}
		
		return nightMinutes;
	}
	
	void LoadHolidays(){
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		String query = "SELECT HOLIDAY_DATE FROM HOLIDAYS WHERE OFFICE_NUMBER = ? AND IS_DELETED = 0 AND IS_ACTIVE = 1";
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
					while (databaseQueryResult.next()) {
						Date date;
						try {
							date = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(0));
						} catch (Exception ex) {
							continue;
						}
						
						holidaysList.add(date);
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
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jXDatePickerFrom = new org.jdesktop.swingx.JXDatePicker();
        jXDatePickerTo = new org.jdesktop.swingx.JXDatePicker();
        jButtonPrintPosInvoicesTable = new javax.swing.JButton();
        jButtonPrintA4InvoicesTable = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabelMon = new javax.swing.JLabel();
        jLabelTue = new javax.swing.JLabel();
        jLabelWed = new javax.swing.JLabel();
        jLabelThu = new javax.swing.JLabel();
        jLabelFri = new javax.swing.JLabel();
        jLabelSat = new javax.swing.JLabel();
        jLabelSun = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Porez na potrošnju");
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
        jLabel9.setText("Radni sati djelatnika");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Radni sati djelatnika"));

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jXDatePickerFrom.setPreferredSize(new java.awt.Dimension(150, 25));
        jXDatePickerFrom.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerFromPropertyChange(evt);
            }
        });

        jXDatePickerTo.setPreferredSize(new java.awt.Dimension(150, 25));
        jXDatePickerTo.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXDatePickerToPropertyChange(evt);
            }
        });

        jButtonPrintPosInvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPosInvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPosInvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintPosInvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosInvoicesTableActionPerformed(evt);
            }
        });

        jButtonPrintA4InvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis A4  <br> [F5] </div> </html>");
        jButtonPrintA4InvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4InvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4InvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4InvoicesTableActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Odustani <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(70, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jLabel1.setText("Od [F1]:");
        jLabel1.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(45, 14));

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(249, 249, 249)
                .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 261, Short.MAX_VALUE)
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Radno vrijeme poslovnice"));

        jLabel8.setText("Ponedjeljak");

        jLabel10.setText("Utorak");

        jLabel12.setText("Srijeda");

        jLabel13.setText("Četvrtak");

        jLabel14.setText("Petak");

        jLabel15.setText("Subota");

        jLabel16.setText("Nedjelja i praznici");

        jLabelMon.setText("Ponedjeljak");

        jLabelTue.setText("Utorak");

        jLabelWed.setText("Srijeda");

        jLabelThu.setText("Četvrtak");

        jLabelFri.setText("Petak");

        jLabelSat.setText("Subota");

        jLabelSun.setText("Nedjelja i praznici");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelMon, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTue, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelWed, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelThu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelFri, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSat, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSun, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMon)
                    .addComponent(jLabelTue)
                    .addComponent(jLabelWed)
                    .addComponent(jLabelThu)
                    .addComponent(jLabelFri)
                    .addComponent(jLabelSat)
                    .addComponent(jLabelSun))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 502, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAdinfoLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel9)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonPrintPosInvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosInvoicesTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do: ", dateToString));
		
		PrintUtils.PrintPosTable("Radni sati djelatnika", jTable1, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5, 6}}, printTableExtraData);
    }//GEN-LAST:event_jButtonPrintPosInvoicesTableActionPerformed

    private void jButtonPrintA4InvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoicesTableActionPerformed
		String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
		String dateToString = jXDatePickerTo.getEditor().getText().trim();
		
		PrintTableExtraData printTableExtraData = new PrintTableExtraData();
		printTableExtraData.headerList.add(new Pair<>("Od: ", dateFromString));
		printTableExtraData.headerList.add(new Pair<>("Do: ", dateToString));
		
		PrintUtils.PrintA4Table("RadniSatiDjelatnika", "Radni sati djelatnika", jTable1, new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{}, printTableExtraData, "");
    }//GEN-LAST:event_jButtonPrintA4InvoicesTableActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
		RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4InvoicesTable;
    private javax.swing.JButton jButtonPrintPosInvoicesTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
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
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
