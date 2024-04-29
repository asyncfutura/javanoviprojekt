/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control.ui;

import hr.adinfo.control.ControlApp;
import hr.adinfo.control.ControlAppLogger;
import hr.adinfo.control.ControlAppServerAppClient;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.certificates.CertificateManager;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import hr.adinfo.utils.licence.LicenceQuery;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import hr.adinfo.utils.licence.UniqueComputerID;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Matej
 */
public class ControlAppMainWindow extends javax.swing.JFrame {

	private ArrayList<Integer> usersTableId = new ArrayList<>();
	private ArrayList<Integer> officesTableId = new ArrayList<>();
	private ArrayList<Integer> officesCompanyId = new ArrayList<>();
	private ArrayList<Integer> licencesTableId = new ArrayList<>();
	private ArrayList<Integer> licencesTableOfficeId = new ArrayList<>();
	private ArrayList<Integer> licencesTableUserId = new ArrayList<>();
	
	private boolean demoRootExist;
	private boolean demoSubExist;
	private boolean prodRootExist;
	private boolean prodSubExist;
	
	/**
	 * Creates new form LicencesUpdatesControlAppWindow
	 */
	public ControlAppMainWindow() {
		initComponents();
		
		jTableUsers.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableUsers.getTableHeader().setReorderingAllowed(false);
		jTableUsers.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		jTableOffices.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableOffices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableOffices.getTableHeader().setReorderingAllowed(false);
		jTableOffices.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		jTableLicences.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
		jTableLicences.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableLicences.getTableHeader().setReorderingAllowed(false);
		jTableLicences.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
	}
	
	public void RefreshUserPermissions(){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				if(ControlApp.controlAppUserType == 0){
					jTabbedPane1.remove(4);
					jTabbedPane1.remove(3);
					jTabbedPane1.remove(2);
					jTabbedPane1.remove(0);
					jButtonOfficesAdd.setEnabled(false);
					jButtonOfficesEdit.setEnabled(false);
				}
			}
		});
	}
	
	private void RefreshUsersList(){
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ID, OIB, NAME, ADDRESS, AUTO_RENEW, IS_DELETED FROM COMPANIES ORDER BY NAME ASC");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Korisnik", "OIB", "Adresa sjedišta", "Redoviti platiša"});
					ArrayList<Integer> idList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						if(jRadioButtonUsersActive.isSelected() && databaseQueryResult.getInt(5) == 1)
							continue;
						
						if(jRadioButtonUsersDeleted.isSelected() && databaseQueryResult.getInt(5) == 0)
							continue;
						
						Object[] rowData = new Object[4];
						rowData[0] = databaseQueryResult.getString(2);
						rowData[1] = databaseQueryResult.getString(1);
						rowData[2] = databaseQueryResult.getString(3);
						rowData[3] = databaseQueryResult.getInt(4) == 1 ? "Da" : "";
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
					}
					jTableUsers.setModel(customTableModel);
					usersTableId = idList;
					
					jTableUsers.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneUsers.getWidth() * 25 / 100);
					jTableUsers.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneUsers.getWidth() * 25/ 100);
					jTableUsers.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneUsers.getWidth() * 30 / 100);
					jTableUsers.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneUsers.getWidth() * 20 / 100);
				}
			} catch (InterruptedException | ExecutionException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshOfficesList(){
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		String query = "SELECT OFFICES.ID, COMPANIES.NAME, OFFICES.ADDRESS, OFFICE_NUMBER, OFFICES.OFFICE_NAME, "
				+ "OFFICES.LAST_PING_DATE, OFFICES.LAST_PING_TIME, OFFICE_TAG, COMPANIES.ID, COMPANIES.IS_DELETED, OFFICES.IS_DELETED "
				+ "FROM (OFFICES INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "ORDER BY COMPANIES.NAME, OFFICE_NUMBER ASC";
		
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Korisnik", "Naziv poslovnice", "Broj poslovnice", "Oznaka PP", "Adresa poslovnice", "Zadnja aktivnost"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Integer> companyIdList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Date lastPingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult.getString(5) + " " + databaseQueryResult.getString(6));
						long secs = (new Date().getTime() - lastPingDate.getTime()) / 1000;
						long hours = secs / 3600;
						if(jRadioButtonOfficesInactive.isSelected() && hours < 12)
							continue;
						
						if(jRadioButtonOfficesDeleted.isSelected() && (databaseQueryResult.getInt(9) == 0 && databaseQueryResult.getInt(10) == 0)){
							continue;
						} else if(!jRadioButtonOfficesDeleted.isSelected() && (databaseQueryResult.getInt(9) == 1 || databaseQueryResult.getInt(10) == 1)){
							continue;
						}
						
						Object[] rowData = new Object[6];
						rowData[0] = databaseQueryResult.getString(1) + (databaseQueryResult.getInt(9) == 1 ? " (OBRISAN)" : "");
						rowData[1] = databaseQueryResult.getString(4);
						rowData[2] = databaseQueryResult.getString(3);
						rowData[3] = databaseQueryResult.getString(7);
						rowData[4] = databaseQueryResult.getString(2);
						rowData[5] = hours < 1 ? "Aktivan" : hours + " sati";
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(0));
						companyIdList.add(databaseQueryResult.getInt(8));
					}
					jTableOffices.setModel(customTableModel);
					officesTableId = idList;
					officesCompanyId = companyIdList;
					
					jTableOffices.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneOffices.getWidth() * 25 / 100);
					jTableOffices.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneOffices.getWidth() * 20 / 100);
					jTableOffices.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneOffices.getWidth() * 15 / 100);
					jTableOffices.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneOffices.getWidth() * 15 / 100);
					jTableOffices.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneOffices.getWidth() * 30 / 100);
					jTableOffices.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneOffices.getWidth() * 20 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshLicencesList(){
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		String query = "SELECT COMPANIES.NAME, OFFICES.OFFICE_NUMBER, OFFICES.ADDRESS, LICENCES.TYPE, LICENCES.EXPIRATION_DATE, "
				+ "LICENCES.ACTIVATION_KEY, LICENCES.ID, LICENCES.COMPUTER_ID, OFFICES.USER_ID, LICENCES.OFFICE_ID, "
				+ "LICENCES.CASH_REGISTER_NUMBER, OFFICES.OFFICE_NAME, OFFICES.OFFICE_TAG "
				+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "WHERE LICENCES.IS_DELETED = 0 "
				+ "ORDER BY COMPANIES.NAME, OFFICES.OFFICE_NUMBER, LICENCES.CASH_REGISTER_NUMBER ASC";
		
		if(jRadioButtonLicencesNearExpiration.isSelected()){
			query = "SELECT COMPANIES.NAME, OFFICES.OFFICE_NUMBER, OFFICES.ADDRESS, LICENCES.TYPE, LICENCES.EXPIRATION_DATE, "
				+ "LICENCES.ACTIVATION_KEY, LICENCES.ID, LICENCES.COMPUTER_ID, OFFICES.USER_ID, LICENCES.OFFICE_ID, "
				+ "LICENCES.CASH_REGISTER_NUMBER, OFFICES.OFFICE_NAME, OFFICES.OFFICE_TAG "
				+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "WHERE LICENCES.IS_DELETED = 0 AND LICENCES.EXPIRATION_DATE > ? AND LICENCES.EXPIRATION_DATE < ? "
				+ "ORDER BY LICENCES.EXPIRATION_DATE";
		} else if(jRadioButtonLicencesExpired.isSelected()){
			query = "SELECT COMPANIES.NAME, OFFICES.OFFICE_NUMBER, OFFICES.ADDRESS, LICENCES.TYPE, LICENCES.EXPIRATION_DATE, "
				+ "LICENCES.ACTIVATION_KEY, LICENCES.ID, LICENCES.COMPUTER_ID, OFFICES.USER_ID, LICENCES.OFFICE_ID, "
				+ "LICENCES.CASH_REGISTER_NUMBER, OFFICES.OFFICE_NAME, OFFICES.OFFICE_TAG "
				+ "FROM ((LICENCES INNER JOIN OFFICES ON LICENCES.OFFICE_ID = OFFICES.ID) INNER JOIN COMPANIES ON OFFICES.USER_ID = COMPANIES.ID) "
				+ "WHERE LICENCES.IS_DELETED = 0 AND LICENCES.EXPIRATION_DATE <= ? "
				+ "ORDER BY LICENCES.EXPIRATION_DATE DESC";
		}
		
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		
		if(jRadioButtonLicencesNearExpiration.isSelected()){
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, 30);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String licenceDate = dateFormat.format(calendar.getTime());
			databaseQuery.AddParam(1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			databaseQuery.AddParam(2, licenceDate);
		} else if(jRadioButtonLicencesExpired.isSelected()){
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String licenceDate = dateFormat.format(new Date());
			databaseQuery.AddParam(1, licenceDate);
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
					customTableModel.setColumnIdentifiers(new String[] {"Korisnik", "Poslovnica", "Kasa", "Tip licence", "Datum isteka", "Aktivacijski ključ", "Identifikacijski ključ"});
					ArrayList<Integer> idList = new ArrayList<>();
					ArrayList<Integer> officeIdList = new ArrayList<>();
					ArrayList<Integer> userIdList = new ArrayList<>();
					while (databaseQueryResult.next()) {
						Object[] rowData = new Object[7];
						rowData[0] = databaseQueryResult.getString(0);
						rowData[1] = databaseQueryResult.getString(1) + " - " + databaseQueryResult.getString(12) + " - " + databaseQueryResult.getString(11) + " - " + databaseQueryResult.getString(2);
						rowData[2] = databaseQueryResult.getString(10);
						if(databaseQueryResult.getInt(3) == Values.LICENCE_TYPE_CLIENT){
							rowData[3] = "Blagajna";
						} else if(databaseQueryResult.getInt(3) == Values.LICENCE_TYPE_LOCAL_SERVER){
							rowData[3] = "Lokalni server";
						} else {
							rowData[3] = "Glavni lokalni server";
						}
						Date expirationDate = new SimpleDateFormat("yyyy-MM-dd").parse(databaseQueryResult.getString(4));
						rowData[4] = new SimpleDateFormat("dd.MM.yyyy.").format(expirationDate);
						rowData[5] = databaseQueryResult.getString(5);
						rowData[6] = databaseQueryResult.getString(7);
						
						customTableModel.addRow(rowData);
						idList.add(databaseQueryResult.getInt(6));
						userIdList.add(databaseQueryResult.getInt(8));
						officeIdList.add(databaseQueryResult.getInt(9));
					}

					jTableLicences.setModel(customTableModel);
					licencesTableId = idList;
					licencesTableOfficeId = officeIdList;
					licencesTableUserId = userIdList;
					
					jTableLicences.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneLicences.getWidth() * 15 / 100);
					jTableLicences.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneLicences.getWidth() * 20 / 100);
					jTableLicences.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneLicences.getWidth() * 5 / 100);
					jTableLicences.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneLicences.getWidth() * 15 / 100);
					jTableLicences.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneLicences.getWidth() * 10 / 100);
					jTableLicences.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneLicences.getWidth() * 20 / 100);
					jTableLicences.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneLicences.getWidth() * 15 / 100);
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}

	private void RefreshCertificates(){
		demoRootExist = false;
		demoSubExist = false;
		prodRootExist = false;
		prodSubExist = false;
		
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT ALIAS, UPLOAD_DATE, UPLOAD_TIME FROM CERTIFICATES");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult.getString(1) + " " + databaseQueryResult.getString(2));
						if(Values.CERT_DEMO_ROOT_ALIAS.equals(databaseQueryResult.getString(0))){
							jLabelTestRootDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(date));
							demoRootExist = true;
						} else if(Values.CERT_DEMO_SUB_ALIAS.equals(databaseQueryResult.getString(0))){
							jLabelTestSubDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(date));
							demoSubExist = true;
						} else if(Values.CERT_PROD_ROOT_ALIAS.equals(databaseQueryResult.getString(0))){
							jLabelReleaseRootDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(date));
							prodRootExist = true;
						} else if(Values.CERT_PROD_SUB_ALIAS.equals(databaseQueryResult.getString(0))){
							jLabelReleaseSubDate.setText(new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(date));
							prodSubExist = true;
						}
					}
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
	}
	
	private void RefreshNotifications(){
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT NAME, VALUE FROM NOTIFICATIONS");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
						if("email1".equals(databaseQueryResult.getString(0))){
							jTextFieldEmail1.setText(databaseQueryResult.getString(1));
						} else if("email2".equals(databaseQueryResult.getString(0))){
							jTextFieldEmail2.setText(databaseQueryResult.getString(1));
						} else if("email3".equals(databaseQueryResult.getString(0))){
							jTextFieldEmail3.setText(databaseQueryResult.getString(1));
						} else if("email4".equals(databaseQueryResult.getString(0))){
							jTextFieldEmail4.setText(databaseQueryResult.getString(1));
						} else if("time1".equals(databaseQueryResult.getString(0))){
							Date date = new Date();
							try {
								date = new SimpleDateFormat("HH:mm").parse(databaseQueryResult.getString(1));
							} catch(Exception ex){}
							jFormattedTextFieldNotif1.setValue(date);
						} else if("time2".equals(databaseQueryResult.getString(0))){
							Date date = new Date();
							try {
								date = new SimpleDateFormat("HH:mm").parse(databaseQueryResult.getString(1));
							} catch(Exception ex){}
							jFormattedTextFieldNotif2.setValue(date);
						}
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel8 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelUsers = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jRadioButtonUsersActive = new javax.swing.JRadioButton();
        jRadioButtonUsersDeleted = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPaneUsers = new javax.swing.JScrollPane();
        jTableUsers = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jButtonUsersAdd = new javax.swing.JButton();
        jButtonUsersEdit = new javax.swing.JButton();
        jButtonUsersDelete = new javax.swing.JButton();
        jButtonActivateTotals = new javax.swing.JButton();
        jPanelOffices = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jRadioButtonOfficesAll = new javax.swing.JRadioButton();
        jRadioButtonOfficesInactive = new javax.swing.JRadioButton();
        jRadioButtonOfficesDeleted = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPaneOffices = new javax.swing.JScrollPane();
        jTableOffices = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jButtonConnectToOffice = new javax.swing.JButton();
        jButtonOfficesAdd = new javax.swing.JButton();
        jButtonOfficesEdit = new javax.swing.JButton();
        jButtonOfficesDelete = new javax.swing.JButton();
        jPanelLicences = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jRadioButtonLicencesAll = new javax.swing.JRadioButton();
        jRadioButtonLicencesNearExpiration = new javax.swing.JRadioButton();
        jRadioButtonLicencesExpired = new javax.swing.JRadioButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneLicences = new javax.swing.JScrollPane();
        jTableLicences = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jButtonLicencesExtend = new javax.swing.JButton();
        jButtonLicencesAdd = new javax.swing.JButton();
        jButtonLicencesDelete = new javax.swing.JButton();
        jButtonLicencesReset = new javax.swing.JButton();
        jPanelCertificates = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelTestRootDate = new javax.swing.JLabel();
        jButtonTestRoot = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabelTestSubDate = new javax.swing.JLabel();
        jButtonTestSub = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabelReleaseRootDate = new javax.swing.JLabel();
        jButtonReleaseRoot = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabelReleaseSubDate = new javax.swing.JLabel();
        jButtonReleaseSub = new javax.swing.JButton();
        jPanelNotifications = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldEmail1 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldEmail2 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldEmail3 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldEmail4 = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jFormattedTextFieldNotif1 = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jFormattedTextFieldNotif2 = new javax.swing.JFormattedTextField();
        jButtonSaveNotifications = new javax.swing.JButton();
        jLabelVersion = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        buttonGroup3.add(jRadioButtonUsersActive);
        jRadioButtonUsersActive.setSelected(true);
        jRadioButtonUsersActive.setText("Aktivni korisnici");
        jRadioButtonUsersActive.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonUsersActiveItemStateChanged(evt);
            }
        });

        buttonGroup3.add(jRadioButtonUsersDeleted);
        jRadioButtonUsersDeleted.setText("Obrisani korisnici");
        jRadioButtonUsersDeleted.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonUsersDeletedItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonUsersActive)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonUsersDeleted)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonUsersActive)
                    .addComponent(jRadioButtonUsersDeleted))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jTableUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPaneUsers.setViewportView(jTableUsers);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonUsersAdd.setText("Dodaj korisnika");
        jButtonUsersAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUsersAddActionPerformed(evt);
            }
        });

        jButtonUsersEdit.setText("Uredi korisnika");
        jButtonUsersEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUsersEditActionPerformed(evt);
            }
        });

        jButtonUsersDelete.setText("Obriši / vrati korisnika");
        jButtonUsersDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUsersDeleteActionPerformed(evt);
            }
        });

        jButtonActivateTotals.setText("Aktiviraj totale za određenu tvrtku");
        jButtonActivateTotals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActivateTotalsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonUsersAdd)
                .addGap(18, 18, 18)
                .addComponent(jButtonUsersEdit)
                .addGap(18, 18, 18)
                .addComponent(jButtonUsersDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonActivateTotals)
                .addGap(25, 25, 25))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonUsersAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonUsersEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonUsersDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonActivateTotals))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelUsersLayout = new javax.swing.GroupLayout(jPanelUsers);
        jPanelUsers.setLayout(jPanelUsersLayout);
        jPanelUsersLayout.setHorizontalGroup(
            jPanelUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUsersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelUsersLayout.setVerticalGroup(
            jPanelUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUsersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Korisnici", jPanelUsers);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        buttonGroup2.add(jRadioButtonOfficesAll);
        jRadioButtonOfficesAll.setSelected(true);
        jRadioButtonOfficesAll.setText("Sve poslovnice");
        jRadioButtonOfficesAll.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonOfficesAllItemStateChanged(evt);
            }
        });

        buttonGroup2.add(jRadioButtonOfficesInactive);
        jRadioButtonOfficesInactive.setText("Neaktivne poslovnice");
        jRadioButtonOfficesInactive.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonOfficesInactiveItemStateChanged(evt);
            }
        });

        buttonGroup2.add(jRadioButtonOfficesDeleted);
        jRadioButtonOfficesDeleted.setText("Obrisane poslovnice");
        jRadioButtonOfficesDeleted.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonOfficesDeletedItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonOfficesAll)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonOfficesInactive)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonOfficesDeleted)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonOfficesAll)
                    .addComponent(jRadioButtonOfficesInactive)
                    .addComponent(jRadioButtonOfficesDeleted))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jTableOffices.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneOffices.setViewportView(jTableOffices);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOffices, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneOffices, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonConnectToOffice.setText("Spoji se na poslovnicu");
        jButtonConnectToOffice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectToOfficeActionPerformed(evt);
            }
        });

        jButtonOfficesAdd.setText("Dodaj poslovnicu");
        jButtonOfficesAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOfficesAddActionPerformed(evt);
            }
        });

        jButtonOfficesEdit.setText("Uredi poslovnicu");
        jButtonOfficesEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOfficesEditActionPerformed(evt);
            }
        });

        jButtonOfficesDelete.setText("Obriši / vrati poslovnicu");
        jButtonOfficesDelete.setToolTipText("");
        jButtonOfficesDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOfficesDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonOfficesAdd)
                .addGap(18, 18, 18)
                .addComponent(jButtonOfficesEdit)
                .addGap(18, 18, 18)
                .addComponent(jButtonOfficesDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonConnectToOffice)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOfficesAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonConnectToOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOfficesEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOfficesDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelOfficesLayout = new javax.swing.GroupLayout(jPanelOffices);
        jPanelOffices.setLayout(jPanelOfficesLayout);
        jPanelOfficesLayout.setHorizontalGroup(
            jPanelOfficesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOfficesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOfficesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelOfficesLayout.setVerticalGroup(
            jPanelOfficesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOfficesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Poslovnice", jPanelOffices);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        buttonGroup1.add(jRadioButtonLicencesAll);
        jRadioButtonLicencesAll.setSelected(true);
        jRadioButtonLicencesAll.setText("Sve licence");
        jRadioButtonLicencesAll.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonLicencesAllItemStateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButtonLicencesNearExpiration);
        jRadioButtonLicencesNearExpiration.setText("Licence pred istekom");
        jRadioButtonLicencesNearExpiration.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonLicencesNearExpirationItemStateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButtonLicencesExpired);
        jRadioButtonLicencesExpired.setText("Istekle licence");
        jRadioButtonLicencesExpired.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonLicencesExpiredItemStateChanged(evt);
            }
        });

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel6.setText("Filter:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonLicencesAll)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonLicencesNearExpiration)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonLicencesExpired)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonLicencesAll)
                    .addComponent(jRadioButtonLicencesNearExpiration)
                    .addComponent(jRadioButtonLicencesExpired)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jTableLicences.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Korisnik", "Poslovnica", "Tip licence", "Datum isteka", "Aktivacijski ključ"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneLicences.setViewportView(jTableLicences);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneLicences, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneLicences, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonLicencesExtend.setText("Izmjeni licencu");
        jButtonLicencesExtend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLicencesExtendActionPerformed(evt);
            }
        });

        jButtonLicencesAdd.setText("Dodaj licencu");
        jButtonLicencesAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLicencesAddActionPerformed(evt);
            }
        });

        jButtonLicencesDelete.setText("Obriši licencu");
        jButtonLicencesDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLicencesDeleteActionPerformed(evt);
            }
        });

        jButtonLicencesReset.setText("Resetiraj licencu");
        jButtonLicencesReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLicencesResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonLicencesAdd)
                .addGap(18, 18, 18)
                .addComponent(jButtonLicencesExtend)
                .addGap(18, 18, 18)
                .addComponent(jButtonLicencesDelete)
                .addGap(18, 18, 18)
                .addComponent(jButtonLicencesReset)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLicencesAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLicencesExtend, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLicencesDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLicencesReset, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelLicencesLayout = new javax.swing.GroupLayout(jPanelLicences);
        jPanelLicences.setLayout(jPanelLicencesLayout);
        jPanelLicencesLayout.setHorizontalGroup(
            jPanelLicencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLicencesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLicencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelLicencesLayout.setVerticalGroup(
            jPanelLicencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLicencesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Licence", jPanelLicences);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Test root"));

        jLabel1.setText("Zadnji put učitano:");

        jLabelTestRootDate.setText("Nikad");

        jButtonTestRoot.setText("Učitaj");
        jButtonTestRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestRootActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabelTestRootDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonTestRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(449, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelTestRootDate)
                    .addComponent(jButtonTestRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Test sub"));

        jLabel3.setText("Zadnji put učitano:");

        jLabelTestSubDate.setText("Nikad");

        jButtonTestSub.setText("Učitaj");
        jButtonTestSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestSubActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jLabelTestSubDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonTestSub, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelTestSubDate)
                    .addComponent(jButtonTestSub, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Release root"));

        jLabel5.setText("Zadnji put učitano:");

        jLabelReleaseRootDate.setText("Nikad");

        jButtonReleaseRoot.setText("Učitaj");
        jButtonReleaseRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReleaseRootActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jLabelReleaseRootDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonReleaseRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelReleaseRootDate)
                    .addComponent(jButtonReleaseRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Release sub"));

        jLabel7.setText("Zadnji put učitano:");

        jLabelReleaseSubDate.setText("Nikad");

        jButtonReleaseSub.setText("Učitaj");
        jButtonReleaseSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReleaseSubActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jLabelReleaseSubDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonReleaseSub, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabelReleaseSubDate)
                    .addComponent(jButtonReleaseSub, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(284, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelCertificatesLayout = new javax.swing.GroupLayout(jPanelCertificates);
        jPanelCertificates.setLayout(jPanelCertificatesLayout);
        jPanelCertificatesLayout.setHorizontalGroup(
            jPanelCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCertificatesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelCertificatesLayout.setVerticalGroup(
            jPanelCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCertificatesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Certifikati", jPanelCertificates);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Popis emailova"));

        jLabel15.setText("Email 1:");

        jLabel16.setText("Email 2:");

        jLabel17.setText("Email 3:");

        jLabel18.setText("Email 4:");

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldEmail3, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldEmail4, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(518, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEmail3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEmail4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Vrijeme slanja"));

        jFormattedTextFieldNotif1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextFieldNotif1.setToolTipText("");

        jLabel2.setText("Prvo slanje:");

        jLabel4.setText("Drugo slanje:");

        jFormattedTextFieldNotif2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm"))));
        jFormattedTextFieldNotif2.setToolTipText("");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextFieldNotif1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFormattedTextFieldNotif2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jFormattedTextFieldNotif1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jFormattedTextFieldNotif2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonSaveNotifications.setText("Spremi");
        jButtonSaveNotifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveNotificationsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSaveNotifications, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSaveNotifications, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(256, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelNotificationsLayout = new javax.swing.GroupLayout(jPanelNotifications);
        jPanelNotifications.setLayout(jPanelNotificationsLayout);
        jPanelNotificationsLayout.setHorizontalGroup(
            jPanelNotificationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNotificationsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelNotificationsLayout.setVerticalGroup(
            jPanelNotificationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNotificationsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Obavijesti", jPanelNotifications);

        jLabelVersion.setText("Verzija 1.0.61");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabelVersion)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 648, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelVersion)
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(jTabbedPane1.getSelectedIndex() == jTabbedPane1.indexOfTab("Korisnici")){
					RefreshUsersList();
				} else if(jTabbedPane1.getSelectedIndex() == jTabbedPane1.indexOfTab("Poslovnice")){
					RefreshOfficesList();
				} else if(jTabbedPane1.getSelectedIndex() == jTabbedPane1.indexOfTab("Licence")){
					RefreshLicencesList();
				} else if(jTabbedPane1.getSelectedIndex() == jTabbedPane1.indexOfTab("Certifikati")){
					RefreshCertificates();
				} else if(jTabbedPane1.getSelectedIndex() == jTabbedPane1.indexOfTab("Obavijesti")){
					RefreshNotifications();
				}
			}
		}).start();
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jButtonUsersAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsersAddActionPerformed
		ControlAppCompanyAddEditDialog dialog = new ControlAppCompanyAddEditDialog(this, true, -1);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				RefreshUsersList();
			}
		});
		dialog.setVisible(true);
    }//GEN-LAST:event_jButtonUsersAddActionPerformed

    private void jButtonOfficesAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOfficesAddActionPerformed
		ControlAppOfficeAddEditDialog dialog = new ControlAppOfficeAddEditDialog(this, true, -1, -1);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				RefreshOfficesList();
			}
		});
		dialog.setVisible(true);
    }//GEN-LAST:event_jButtonOfficesAddActionPerformed

    private void jButtonLicencesAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLicencesAddActionPerformed
        ControlAppAddLicenceDialog dialog = new ControlAppAddLicenceDialog(this, true);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				RefreshLicencesList();
			}
		});
		dialog.setVisible(true);
    }//GEN-LAST:event_jButtonLicencesAddActionPerformed

    private void jButtonLicencesExtendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLicencesExtendActionPerformed
		int rowId = jTableLicences.getSelectedRow();
		if(rowId == -1){
			ControlAppLogger.GetInstance().ShowMessage("Molimo odaberite licencu koju želite izmijeniti");
			return;
		}
		int licenceId = licencesTableId.get(rowId);
		String userName = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 0));
		String officeName = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 1));
		String licenceType = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 3));
		String expirationDate = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 4));
		String cashRegisterNumber = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 2));
		int officeId = licencesTableOfficeId.get(rowId);
		int userId = licencesTableUserId.get(rowId);
		
		ControlAppEditLicenceDialog dialog = new ControlAppEditLicenceDialog(this, true);
		dialog.SetData(licenceId, userName, officeName, licenceType, expirationDate, officeId, userId, cashRegisterNumber);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				RefreshLicencesList();
			}
		});
		dialog.setVisible(true);
    }//GEN-LAST:event_jButtonLicencesExtendActionPerformed

    private void jButtonConnectToOfficeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectToOfficeActionPerformed
		int rowId = jTableOffices.getSelectedRow();
		if(rowId == -1){
			ControlAppLogger.GetInstance().ShowMessage("Molimo odaberite poslovnicu na koju se želite spojiti");
			return;
		}
		int officeId = officesTableId.get(rowId);
		
		String uniqueId = UniqueComputerID.GetUniqueID();
		String message = officeId + Values.LICENCE_SPLIT_STRING + uniqueId;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		LicenceQuery licenceQuery = new LicenceQuery(Values.LICENCE_QUERY_ACTIVATE, message);
		ServerQueryTask serverQueryTask = new ServerQueryTask(loadingDialog, licenceQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());

		serverQueryTask.execute();
		loadingDialog.setVisible(true);
		if(!serverQueryTask.isDone()){
			serverQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = serverQueryTask.get();
				byte[] publicKey = null;
				byte[] licence = null;
				int licenceErrorCode = -1;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					publicKey = ((LicenceQueryResponse) serverResponse).publicKeyBytes;
					licence = ((LicenceQueryResponse) serverResponse).licenceBytes;
					licenceErrorCode = ((LicenceQueryResponse) serverResponse).licenceErrorCode;
				}
				if(licenceErrorCode == Values.LICENCE_ERROR_CODE_WRONG_CODE){
					ControlAppLogger.GetInstance().ShowMessage("Uneseni aktivacijski ključ je neispravan");
				} else if(licenceErrorCode == Values.LICENCE_ERROR_CODE_ALREADY_ACTIVE){
					ControlAppLogger.GetInstance().ShowMessage("Uneseni aktivacijski ključ je već aktiviran na drugom računalu");
				} else if(licenceErrorCode == Values.LICENCE_ERROR_CODE_ACTIVATION_SUCCESS && publicKey != null && licence != null){
					try {
						Licence.SavePublicKey(publicKey);
						Licence.SaveLicence(licence);
						Licence.SaveActivationKey("activationKey");
						CertificateManager.SaveCertificate(Values.CERT_DEMO_ROOT_ALIAS, ((LicenceQueryResponse) serverResponse).certDemoRootBytes);
						CertificateManager.SaveCertificate(Values.CERT_DEMO_SUB_ALIAS, ((LicenceQueryResponse) serverResponse).certDemoSubBytes);
						CertificateManager.SaveCertificate(Values.CERT_PROD_ROOT_ALIAS, ((LicenceQueryResponse) serverResponse).certProdRootBytes);
						CertificateManager.SaveCertificate(Values.CERT_PROD_SUB_ALIAS, ((LicenceQueryResponse) serverResponse).certProdSubBytes);
					} catch (Exception ex) {
						ControlAppLogger.GetInstance().ShowErrorLog(ex);
					}
				} else {
					ControlAppLogger.GetInstance().ShowMessage("Došlo je do pogreške kod dohvaćanja licence");
				}
			} catch (InterruptedException | ExecutionException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		String command = "java -jar InvoiceGenerationSystemClient.jar";
		String targetWorkingDirectory = Paths.get("").toAbsolutePath().toString();
		try {
			Process proc = Runtime.getRuntime().exec(command, null, new File(targetWorkingDirectory));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Pogreška tijekom spajanja na poslovnicu" + System.lineSeparator() + ex);
		}
    }//GEN-LAST:event_jButtonConnectToOfficeActionPerformed

    private void jButtonLicencesDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLicencesDeleteActionPerformed
        if(jTableLicences.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableLicences.convertRowIndexToModel(jTableLicences.getSelectedRow());
        int tableId = licencesTableId.get(rowId);
        String tableValue = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 0));
		
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite obrisati licencu korisnika " + tableValue + " ?", "Obriši licencu", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);
				
				String query = "UPDATE LICENCES SET IS_DELETED = ?, TYPE = ?, EXPIRATION_DATE = ? WHERE ID = ?";
				DatabaseQuery databaseQuery = new DatabaseQuery(query);
				databaseQuery.AddParam(1, 1);
				databaseQuery.AddParam(2, Values.LICENCE_TYPE_CLIENT);
				databaseQuery.AddParam(3, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
				databaseQuery.AddParam(4, tableId);

				ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
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
							RefreshLicencesList();
						}
					} catch (Exception ex) {
						ControlAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
        }
    }//GEN-LAST:event_jButtonLicencesDeleteActionPerformed

    private void jRadioButtonLicencesAllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonLicencesAllItemStateChanged
		if(jRadioButtonLicencesAll.isSelected()){
			RefreshLicencesList();
		}
    }//GEN-LAST:event_jRadioButtonLicencesAllItemStateChanged

    private void jRadioButtonLicencesNearExpirationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonLicencesNearExpirationItemStateChanged
		if(jRadioButtonLicencesNearExpiration.isSelected()){
			RefreshLicencesList();
		}
    }//GEN-LAST:event_jRadioButtonLicencesNearExpirationItemStateChanged

    private void jRadioButtonLicencesExpiredItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonLicencesExpiredItemStateChanged
		if(jRadioButtonLicencesExpired.isSelected()){
			RefreshLicencesList();
		}
    }//GEN-LAST:event_jRadioButtonLicencesExpiredItemStateChanged

    private void jButtonTestRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestRootActionPerformed
		InsertCertificate(Values.CERT_DEMO_ROOT_ALIAS, demoRootExist);
    }//GEN-LAST:event_jButtonTestRootActionPerformed

    private void jButtonTestSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestSubActionPerformed
        InsertCertificate(Values.CERT_DEMO_SUB_ALIAS, demoSubExist);
    }//GEN-LAST:event_jButtonTestSubActionPerformed

    private void jButtonReleaseRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReleaseRootActionPerformed
		InsertCertificate(Values.CERT_PROD_ROOT_ALIAS, prodRootExist);
    }//GEN-LAST:event_jButtonReleaseRootActionPerformed

    private void jButtonReleaseSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReleaseSubActionPerformed
        InsertCertificate(Values.CERT_PROD_SUB_ALIAS, prodSubExist);
    }//GEN-LAST:event_jButtonReleaseSubActionPerformed

    private void jRadioButtonOfficesAllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonOfficesAllItemStateChanged
        if(jRadioButtonOfficesAll.isSelected()){
			RefreshOfficesList();
		}
    }//GEN-LAST:event_jRadioButtonOfficesAllItemStateChanged

    private void jRadioButtonOfficesInactiveItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonOfficesInactiveItemStateChanged
        if(jRadioButtonOfficesInactive.isSelected()){
			RefreshOfficesList();
		}
    }//GEN-LAST:event_jRadioButtonOfficesInactiveItemStateChanged

    private void jButtonSaveNotificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveNotificationsActionPerformed
		String notifTime1 = "09:00";
		String notifTime2 = "15:00";
		if(jFormattedTextFieldNotif1.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldNotif1.getValue();
			notifTime1 = new SimpleDateFormat("HH:mm").format(notifDate);
		}
		if(jFormattedTextFieldNotif2.getValue() != null){
			Date notifDate = (Date) jFormattedTextFieldNotif2.getValue();
			notifTime2 = new SimpleDateFormat("HH:mm").format(notifDate);
		}
		
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		String query = "UPDATE NOTIFICATIONS SET VALUE = ? WHERE NAME = ?";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(6);
		multiDatabaseQuery.SetQuery(0, query);
		multiDatabaseQuery.SetQuery(1, query);
		multiDatabaseQuery.SetQuery(2, query);
		multiDatabaseQuery.SetQuery(3, query);
		multiDatabaseQuery.SetQuery(4, query);
		multiDatabaseQuery.SetQuery(5, query);
		
		multiDatabaseQuery.AddParam(0, 1, jTextFieldEmail1.getText());
		multiDatabaseQuery.AddParam(0, 2, "email1");
		multiDatabaseQuery.AddParam(1, 1, jTextFieldEmail2.getText());
		multiDatabaseQuery.AddParam(1, 2, "email2");
		multiDatabaseQuery.AddParam(2, 1, jTextFieldEmail3.getText());
		multiDatabaseQuery.AddParam(2, 2, "email3");
		multiDatabaseQuery.AddParam(3, 1, jTextFieldEmail4.getText());
		multiDatabaseQuery.AddParam(3, 2, "email4");
		multiDatabaseQuery.AddParam(4, 1, notifTime1);
		multiDatabaseQuery.AddParam(4, 2, "time1");
		multiDatabaseQuery.AddParam(5, 1, notifTime2);
		multiDatabaseQuery.AddParam(5, 2, "time2");
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
					
				}
			} catch (InterruptedException | ExecutionException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
    }//GEN-LAST:event_jButtonSaveNotificationsActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
		String searchString = jTextField1.getText();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableLicences.getModel());
		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchString));
		jTableLicences.setRowSorter(sorter);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButtonUsersEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsersEditActionPerformed
		if(jTableUsers.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableUsers.convertRowIndexToModel(jTableUsers.getSelectedRow());
        int tableId = usersTableId.get(rowId);

        ControlAppCompanyAddEditDialog addEditdialog = new ControlAppCompanyAddEditDialog(null, true, tableId);
        addEditdialog.setVisible(true);
        RefreshUsersList();
    }//GEN-LAST:event_jButtonUsersEditActionPerformed

    private void jButtonOfficesEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOfficesEditActionPerformed
		if(jTableOffices.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite urediti.");
            return;
        }
		int rowId = jTableOffices.convertRowIndexToModel(jTableOffices.getSelectedRow());
        int tableId = officesTableId.get(rowId);
        int companyID = officesCompanyId.get(rowId);

        ControlAppOfficeAddEditDialog addEditdialog = new ControlAppOfficeAddEditDialog(null, true, tableId, companyID);
        addEditdialog.setVisible(true);
        RefreshOfficesList();
    }//GEN-LAST:event_jButtonOfficesEditActionPerformed

    private void jButtonLicencesResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLicencesResetActionPerformed
		if(jTableLicences.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati");
            return;
        }
		int rowId = jTableLicences.convertRowIndexToModel(jTableLicences.getSelectedRow());
        int tableId = licencesTableId.get(rowId);
        String tableValue = String.valueOf(jTableLicences.getModel().getValueAt(rowId, 0));
		
		int dialogResult = JOptionPane.showConfirmDialog (null, "Jeste li sigurni da želite resetirati licencu korisnika " + tableValue + " ?", "Resetiraj licencu", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
			{
				final JDialog loadingDialog = new LoadingDialog(null, true);
				
				String query = "UPDATE LICENCES SET ACTIVATION_KEY = ?, COMPUTER_ID = ? WHERE ID = ?";
				DatabaseQuery databaseQuery = new DatabaseQuery(query);
				databaseQuery.AddParam(1, ControlAppAddLicenceDialog.GenerateActivationKey());
				databaseQuery.AddParam(2, "null");
				databaseQuery.AddParam(3, tableId);

				ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
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
							RefreshLicencesList();
						}
					} catch (Exception ex) {
						ControlAppLogger.GetInstance().ShowErrorLog(ex);
					}
				}
			}
        }
    }//GEN-LAST:event_jButtonLicencesResetActionPerformed

    private void jRadioButtonOfficesDeletedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonOfficesDeletedItemStateChanged
        if(jRadioButtonOfficesDeleted.isSelected()){
			RefreshOfficesList();
		}
    }//GEN-LAST:event_jRadioButtonOfficesDeletedItemStateChanged

    private void jButtonOfficesDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOfficesDeleteActionPerformed
        if(jTableOffices.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati/vratiti.");
            return;
        }
		int rowId = jTableOffices.convertRowIndexToModel(jTableOffices.getSelectedRow());
        int tableId = officesTableId.get(rowId);

		final JDialog loadingDialog = new LoadingDialog(null, true);
           
		String query = "UPDATE OFFICES SET IS_DELETED = ? WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, jRadioButtonOfficesDeleted.isSelected() ? 0 : 1);
		databaseQuery.AddParam(2, tableId);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
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
					
				}
			} catch (Exception ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
        RefreshOfficesList();
    }//GEN-LAST:event_jButtonOfficesDeleteActionPerformed

    private void jButtonUsersDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsersDeleteActionPerformed
		if(jTableUsers.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati/vratiti.");
            return;
        }
		int rowId = jTableUsers.convertRowIndexToModel(jTableUsers.getSelectedRow());
        int tableId = usersTableId.get(rowId);

        final JDialog loadingDialog = new LoadingDialog(null, true);
            
		String query = "UPDATE COMPANIES SET IS_DELETED = ? WHERE ID = ?";
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		databaseQuery.AddParam(1, jRadioButtonUsersActive.isSelected() ? 1 : 0);
		databaseQuery.AddParam(2, tableId);

		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
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
					
				}
			} catch (Exception ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
        RefreshUsersList();
    }//GEN-LAST:event_jButtonUsersDeleteActionPerformed

    private void jRadioButtonUsersActiveItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonUsersActiveItemStateChanged
		if(jRadioButtonUsersActive.isSelected()){
			RefreshUsersList();
		}
    }//GEN-LAST:event_jRadioButtonUsersActiveItemStateChanged

    private void jRadioButtonUsersDeletedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonUsersDeletedItemStateChanged
		if(jRadioButtonUsersDeleted.isSelected()){
			RefreshUsersList();
		}
    }//GEN-LAST:event_jRadioButtonUsersDeletedItemStateChanged

    private void jButtonActivateTotalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActivateTotalsActionPerformed
         if(jTableUsers.getSelectedRow() == -1){
            ControlAppLogger.GetInstance().ShowMessage("Odaberite red u tablici koji želite obrisati/vratiti.");
            return;
           }
		int rowId = jTableUsers.convertRowIndexToModel(jTableUsers.getSelectedRow());
                int tableId = usersTableId.get(rowId);
                
                String timeFrom = "00:00:00";
                String timeTo = "23:59:59";
                
                final JDialog loadingDialog = new LoadingDialog(null, true);
                   try (Connection connection = establishDerbyConnection()) {
                                        String loToFile = "Entered connection.!";
                                        try {
                                            logToFile(loToFile);
                                        } catch (IOException ex) {
                                        }
                                        
                                        if (connection != null) {
                                            System.out.println(connection.getSchema());
                                            System.out.println(connection.getMetaData());
                                            executeDatabaseOperations(connection);
                                        }
                                    } catch (SQLException e) {
                                      System.out.println("Not entered into connection.");
                                      System.out.println(e.fillInStackTrace());
                                    }
    }//GEN-LAST:event_jButtonActivateTotalsActionPerformed
	
	private void InsertCertificate(String alias, boolean certExist){
		byte[] certFileBytes = GetCertificateBytes(alias);
		if(certFileBytes == null)
			return;
		
		String query;
		if(certExist){
			query = "UPDATE CERTIFICATES SET CERT = ?, UPLOAD_DATE = ?, UPLOAD_TIME = ? WHERE ALIAS = ?";
		} else {
			query = "INSERT INTO CERTIFICATES(ALIAS, CERT, UPLOAD_DATE, UPLOAD_TIME) VALUES (?, ?, ?, ?)";
		}
		
		final JDialog loadingDialog = new LoadingDialog(this, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery(query);
		Date date = new Date();
		if(certExist){
			databaseQuery.AddParam(1, certFileBytes);
			databaseQuery.AddParam(2, new SimpleDateFormat("yyyy-MM-dd").format(date));
			databaseQuery.AddParam(3, new SimpleDateFormat("HH:mm:ss").format(date));
			databaseQuery.AddParam(4, alias);
		} else {
			databaseQuery.AddParam(1, alias);
			databaseQuery.AddParam(2, certFileBytes);
			databaseQuery.AddParam(3, new SimpleDateFormat("yyyy-MM-dd").format(date));
			databaseQuery.AddParam(4, new SimpleDateFormat("HH:mm:ss").format(date));
		}
		
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ControlAppServerAppClient.GetInstance(), ControlAppLogger.GetInstance());
		
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
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		RefreshCertificates();
	}
	
	private byte[] GetCertificateBytes(String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".cer, .der", "der", "cer");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
		byte[] certFileBytes = null;
        if(returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
			File certFile = chooser.getSelectedFile();
			try {
				certFileBytes = Files.readAllBytes(certFile.toPath());
			} catch (IOException ex) {
				ControlAppLogger.GetInstance().ShowErrorLog(ex);
			}
        }
		
		return certFileBytes;
	}
        



    private static void executeDatabaseOperations(Connection connection) throws SQLException {
        String setSchema = "SET CURRENT SCHEMA USER1";
        System.out.println(setSchema);
        String selectQuery = "SELECT * FROM USER1.INVOICES_TEST";
        System.out.println(connection.getSchema());
        System.out.println(selectQuery);
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                // Process the results
                String date = resultSet.getString("I_DATE");
                System.out.println(date);
                String time = resultSet.getString("I_TIME");
                System.out.println(time);
                // ... (retrieve other columns)
                System.out.println("Date: " + date + ", Time: " + time);
                String loToFile =  ("Date: " + date + ", Time: " + time);
                System.out.println("Log to file is: " + loToFile);
                try {
                    logToFile(loToFile);
                } catch (IOException ex) {
                    //Logger.getLogger(Total.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } 
    }
    
    private static void logToFile(String logMessage) throws IOException {
        Logger logger = Logger.getLogger("MyLog");
        System.out.println("Entered log to file ");
        FileHandler fh;

        try {
            // This block configure the logger with handler and formatter
            File folder = new File("C:\\LogsAPI");
            if (!folder.exists()) {
                folder.mkdirs();  // creates the LogsAPI folder and any necessary parent folders
            }
            
            fh = new FileHandler("C:\\LogsAPI\\logFile.log", true);  // true for append mode
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            System.out.println("I will log: " + logMessage);
            // the following statement is used to log any messages
            logger.info(logMessage);

        } catch (SecurityException | IOException e) {
        }
    }
    
       private static Connection establishDerbyConnection() throws SQLException {
            String jdbcUrl = "jdbc:derby:C:\\Users\\Admin\\Desktop\\backup\\20-LovrekTest;create=false";
            String user = "";
            String password = "";
            String total = "";
            
           try {
            logToFile(jdbcUrl);
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (IOException ex) {
        }
        
         return DriverManager.getConnection(jdbcUrl, user, password);
      }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jButtonActivateTotals;
    private javax.swing.JButton jButtonConnectToOffice;
    private javax.swing.JButton jButtonLicencesAdd;
    private javax.swing.JButton jButtonLicencesDelete;
    private javax.swing.JButton jButtonLicencesExtend;
    private javax.swing.JButton jButtonLicencesReset;
    private javax.swing.JButton jButtonOfficesAdd;
    private javax.swing.JButton jButtonOfficesDelete;
    private javax.swing.JButton jButtonOfficesEdit;
    private javax.swing.JButton jButtonReleaseRoot;
    private javax.swing.JButton jButtonReleaseSub;
    private javax.swing.JButton jButtonSaveNotifications;
    private javax.swing.JButton jButtonTestRoot;
    private javax.swing.JButton jButtonTestSub;
    private javax.swing.JButton jButtonUsersAdd;
    private javax.swing.JButton jButtonUsersDelete;
    private javax.swing.JButton jButtonUsersEdit;
    private javax.swing.JFormattedTextField jFormattedTextFieldNotif1;
    private javax.swing.JFormattedTextField jFormattedTextFieldNotif2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelReleaseRootDate;
    private javax.swing.JLabel jLabelReleaseSubDate;
    private javax.swing.JLabel jLabelTestRootDate;
    private javax.swing.JLabel jLabelTestSubDate;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCertificates;
    private javax.swing.JPanel jPanelLicences;
    private javax.swing.JPanel jPanelNotifications;
    private javax.swing.JPanel jPanelOffices;
    private javax.swing.JPanel jPanelUsers;
    private javax.swing.JRadioButton jRadioButtonLicencesAll;
    private javax.swing.JRadioButton jRadioButtonLicencesExpired;
    private javax.swing.JRadioButton jRadioButtonLicencesNearExpiration;
    private javax.swing.JRadioButton jRadioButtonOfficesAll;
    private javax.swing.JRadioButton jRadioButtonOfficesDeleted;
    private javax.swing.JRadioButton jRadioButtonOfficesInactive;
    private javax.swing.JRadioButton jRadioButtonUsersActive;
    private javax.swing.JRadioButton jRadioButtonUsersDeleted;
    private javax.swing.JScrollPane jScrollPaneLicences;
    private javax.swing.JScrollPane jScrollPaneOffices;
    private javax.swing.JScrollPane jScrollPaneUsers;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableLicences;
    private javax.swing.JTable jTableOffices;
    private javax.swing.JTable jTableUsers;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldEmail1;
    private javax.swing.JTextField jTextFieldEmail2;
    private javax.swing.JTextField jTextFieldEmail3;
    private javax.swing.JTextField jTextFieldEmail4;
    // End of variables declaration//GEN-END:variables
}
