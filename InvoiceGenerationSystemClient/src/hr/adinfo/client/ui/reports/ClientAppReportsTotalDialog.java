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
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.StaffUserInfo;
import hr.adinfo.client.print.PrintTableExtraData;
import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Pair;
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
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

/**
 *
 * @author Matej
 */
public class ClientAppReportsTotalDialog extends javax.swing.JDialog {

    private ArrayList<Integer> categoriesFilterIdList = new ArrayList<>();
    private ArrayList<Integer> staffFilterIdList = new ArrayList<>();
    private ArrayList<Integer> articlesFilterIdList = new ArrayList<>();
    private ArrayList<Integer> tradingGoodsFilterIdList = new ArrayList<>();
    private ArrayList<Integer> servicesFilterIdList = new ArrayList<>();
    private boolean setupDone;
    private int totalType = 0;
    private boolean dateChanged = false;
    private boolean isAnyShiftEnabled = false;
    private boolean previousSelected = false;

    private class DailyTrafficData {

        public Date date;
        public float amountWithoutDiscount, discount;
        public float cash, maestro, mastercard, amex, diners, visa, transactionBill, other;

        public void SetDate(Date newDate) {
            date = newDate;
        }

        public boolean equalsDate(Date dateToCheck) {
            if (date == null) {
                return false;
            }

            String dateD = new SimpleDateFormat("dd").format(date);
            String dateM = new SimpleDateFormat("MM").format(date);
            String dateY = new SimpleDateFormat("yyyy").format(date);

            String dateToCheckD = new SimpleDateFormat("dd").format(dateToCheck);
            String dateToCheckM = new SimpleDateFormat("MM").format(dateToCheck);
            String dateToCheckY = new SimpleDateFormat("yyyy").format(dateToCheck);

            return dateToCheckD.equals(dateD) && dateToCheckM.equals(dateM) && dateToCheckY.equals(dateY);
        }

        public void AddAmountPaymentType(float amountWithoutDiscountToAdd, float discountToAdd, String paymentMethod) {
            amountWithoutDiscount += amountWithoutDiscountToAdd;
            discount += discountToAdd;
            float amountToAdd = amountWithoutDiscountToAdd - discountToAdd;
            if ("Novčanice i/ili kovanice".equals(paymentMethod)) {
                cash += amountToAdd;
            } else if ("Maestro".equals(paymentMethod)) {
                maestro += amountToAdd;
            } else if ("Mastercard".equals(paymentMethod)) {
                mastercard += amountToAdd;
            } else if ("American Express".equals(paymentMethod)) {
                amex += amountToAdd;
            } else if ("Diners".equals(paymentMethod)) {
                diners += amountToAdd;
            } else if ("Visa".equals(paymentMethod)) {
                visa += amountToAdd;
            } else if ("Transakcijski račun".equals(paymentMethod)) {
                transactionBill += amountToAdd;
            } else {
                other += amountToAdd;
            }
        }
    }

    private class MaterialsConsumptionData {

        public int invoiceId;
        public int articleId;
        public int materialId;
        public float normative;
        public String materialName;
        public float lastPrice;
    }

    /**
     * Creates new form ClientAppWarehouseMaterialsDialog
     */
    public ClientAppReportsTotalDialog(java.awt.Frame parent, boolean modal, boolean allowDateChange) {
        super(parent, modal);
        initComponents();
        ClientAppSettings.LoadSettings();
        final Window thisWindow = this;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                if (!thisWindow.isDisplayable()) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                }

                if (!thisWindow.isFocused()) {
                    return false;
                }

                if (ke.getID() == KeyEvent.KEY_PRESSED) {
                    if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
                    } else if (ke.getKeyCode() == KeyEvent.VK_F4) {
                        jButtonPrintPosInvoicesTable.doClick();
                    } else if (ke.getKeyCode() == KeyEvent.VK_F5) {
                        jButtonPrintA4InvoicesTable.doClick();
                    } else if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_M) {
                        if (StaffUserInfo.GetCurrentUserInfo().userRights[Values.STAFF_RIGHTS_REPORTS_TOTAL_PLUS] || StaffUserInfo.GetCurrentUserInfo().userRightsType == Values.STAFF_RIGHTS_ADMIN) {
                            totalType = (totalType + 1) % 3;
                            String[] totalTypeNames = new String[]{"Total", "Predračuni", "Total + Predračuni"};
                            jLabel9.setText(totalTypeNames[totalType]);
                            RefreshTable();
                        }
                    }
                }

                return false;
            }
        });

        jTableTotalItemsDetailed.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalItemsDetailed.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalItemsDetailed.getTableHeader().setReorderingAllowed(false);
        jTableTotalItemsDetailed.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalStaff.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalStaff.getTableHeader().setReorderingAllowed(false);
        jTableTotalStaff.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalPayMethod.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalPayMethod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalPayMethod.getTableHeader().setReorderingAllowed(false);
        jTableTotalPayMethod.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalDays.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalDays.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalDays.getTableHeader().setReorderingAllowed(false);
        jTableTotalDays.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalMaterials.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalMaterials.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalMaterials.getTableHeader().setReorderingAllowed(false);
        jTableTotalMaterials.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalTradingGoods.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalTradingGoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalTradingGoods.getTableHeader().setReorderingAllowed(false);
        jTableTotalTradingGoods.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalItems.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalItems.getTableHeader().setReorderingAllowed(false);
        jTableTotalItems.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableTotalTaxes.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableTotalTaxes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableTotalTaxes.getTableHeader().setReorderingAllowed(false);
        jTableTotalTaxes.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterCategory.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableFilterCategory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFilterCategory.getTableHeader().setReorderingAllowed(false);
        jTableFilterCategory.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterStaff.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableFilterStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFilterStaff.getTableHeader().setReorderingAllowed(false);
        jTableFilterStaff.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterArticles.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableFilterArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFilterArticles.getTableHeader().setReorderingAllowed(false);
        jTableFilterArticles.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterTradingGoods.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableFilterTradingGoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFilterTradingGoods.getTableHeader().setReorderingAllowed(false);
        jTableFilterTradingGoods.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterServices.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
        jTableFilterServices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFilterServices.getTableHeader().setReorderingAllowed(false);
        jTableFilterServices.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

//        jTableFilterShifts.setRowHeight(Values.TABLE_COLUMN_HEIGHT);
//        jTableFilterShifts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        jTableFilterShifts.getTableHeader().setReorderingAllowed(false);
//        jTableFilterShifts.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        jTableFilterCategory.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int rowId = jTableFilterCategory.convertRowIndexToModel(jTableFilterCategory.getSelectedRow());
                if (rowId != -1) {
                    boolean value = (Boolean) jTableFilterCategory.getModel().getValueAt(rowId, 0);
                    jTableFilterCategory.getModel().setValueAt(!value, rowId, 0);
                    RefreshTable();
                }
            }
        });

        jTableFilterStaff.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int rowId = jTableFilterStaff.convertRowIndexToModel(jTableFilterStaff.getSelectedRow());
                if (rowId != -1) {
                    boolean value = (Boolean) jTableFilterStaff.getModel().getValueAt(rowId, 0);
                    jTableFilterStaff.getModel().setValueAt(!value, rowId, 0);
                    RefreshTable();
                }
            }
        });

        jTableFilterArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int rowId = jTableFilterArticles.convertRowIndexToModel(jTableFilterArticles.getSelectedRow());
                if (rowId != -1) {
                    boolean value = (Boolean) jTableFilterArticles.getModel().getValueAt(rowId, 0);
                    jTableFilterArticles.getModel().setValueAt(!value, rowId, 0);
                    RefreshTable();
                }
            }
        });

        jTableFilterTradingGoods.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int rowId = jTableFilterTradingGoods.convertRowIndexToModel(jTableFilterTradingGoods.getSelectedRow());
                if (rowId != -1) {
                    boolean value = (Boolean) jTableFilterTradingGoods.getModel().getValueAt(rowId, 0);
                    jTableFilterTradingGoods.getModel().setValueAt(!value, rowId, 0);
                    RefreshTable();
                }
            }
        });

        jTableFilterServices.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int rowId = jTableFilterServices.convertRowIndexToModel(jTableFilterServices.getSelectedRow());
                if (rowId != -1) {
                    boolean value = (Boolean) jTableFilterServices.getModel().getValueAt(rowId, 0);
                    jTableFilterServices.getModel().setValueAt(!value, rowId, 0);
                    RefreshTable();
                }
            }
        });

//        jTableFilterShifts.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent mouseEvent) {
//                int rowId = jTableFilterShifts.convertRowIndexToModel(jTableFilterShifts.getSelectedRow());
//                if (rowId != -1) {
//                    boolean value = (Boolean) jTableFilterShifts.getModel().getValueAt(rowId, 0);
//
//                    jTableFilterShifts.getModel().setValueAt(!value, rowId, 0);
//                    dateChanged = true;
//                    if (value) {
//                        String fromTime = (String) jTableFilterShifts.getModel().getValueAt(rowId, 2);
//                        String toTime = (String) jTableFilterShifts.getModel().getValueAt(rowId, 3);
//                        try {
//                            SimpleDateFormat sdfFrom = new SimpleDateFormat("HH:mm");
//                            Date tFrom = (Date)sdfFrom.parse(fromTime);
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTime(tFrom);
//                            OdSati.setEnabled(true);
//                            OdMinuta.setEnabled(true);
//                            OdSati.setSelectedItem(cal.get(Calendar.HOUR_OF_DAY));
//                            OdMinuta.setSelectedItem(cal.get(Calendar.MINUTE));
//                        } catch (Exception ex) {
//                        }
//                        try{
//                            SimpleDateFormat sdfFrom = new SimpleDateFormat("HH:mm");
//                            Date tFrom = (Date)sdfFrom.parse(toTime);
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTime(tFrom);
//                            DoSati.setEnabled(true);
//                            DoMinuta.setEnabled(true);
//                            DoSati.setSelectedItem(cal.get(Calendar.HOUR_OF_DAY));
//                            DoMinuta.setSelectedItem(cal.get(Calendar.MINUTE));                            
//                        }catch(Exception e){}
//                        for (int i = 0; i < jTableFilterShifts.getModel().getRowCount(); ++i) {
//                        boolean value2 = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
//                        if (value2) {
//                            isAnyShiftEnabled = true;
//                        }
//                    }
//                    } else {
//                        isAnyShiftEnabled = true;
//                        String fromTime = (String) jTableFilterShifts.getModel().getValueAt(rowId, 2);
//                        String toTime = (String) jTableFilterShifts.getModel().getValueAt(rowId, 3);
//                        try {
//                            SimpleDateFormat sdfFrom = new SimpleDateFormat("HH:mm");
//                            Date tFrom = (Date)sdfFrom.parse(fromTime);
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTime(tFrom);
//                            OdSati.setSelectedItem(cal.get(Calendar.HOUR_OF_DAY));
//                            OdMinuta.setSelectedItem(cal.get(Calendar.MINUTE));                         
//                        } catch (Exception ex) {
//                        }
//                        try {
//                            SimpleDateFormat sdfFrom = new SimpleDateFormat("HH:mm");
//                            Date tFrom = (Date)sdfFrom.parse(toTime);
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTime(tFrom);
//                            DoSati.setSelectedItem(cal.get(Calendar.HOUR_OF_DAY));
//                            DoMinuta.setSelectedItem(cal.get(Calendar.MINUTE));                              
//                        } catch (Exception ex) {
//                        }
//                        
//                        OdSati.setEnabled(false);
//                        OdMinuta.setEnabled(false);  
//                        DoSati.setEnabled(false);
//                        DoMinuta.setEnabled(false);  
//                        for (int i = 0; i < jTableFilterShifts.getModel().getRowCount(); ++i) {
//                        boolean value2 = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
//                        if (value2) {
//                            isAnyShiftEnabled = true;
//                        }
//                    }
//                    }
//                    dateChanged = false;
//                    setupDone = true;
//                    
//                    RefreshTable();
//                }
//                isAnyShiftEnabled = false;
//            }
//        });
        

        OdSati.setSelectedItem("00");
        OdMinuta.setSelectedItem("00");

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
            public void focusLost(FocusEvent e) {
            }
        });
        

        DoSati.setSelectedItem("23");
        DoMinuta.setSelectedItem("59");

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
                else {
                    pickerUI.hidePopup();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        if (!allowDateChange) {
            jXDatePickerFrom.setEnabled(true);
            jXDatePickerTo.setEnabled(true);
            DoSati.setEnabled(true);
            DoMinuta.setEnabled(true);
            OdSati.setEnabled(true);
            OdMinuta.setEnabled(true);
        }
        
        if (StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_OWNER && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_MANAGER && StaffUserInfo.GetCurrentUserInfo().userRightsType != Values.STAFF_RIGHTS_ADMIN) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -1);
            jXDatePickerFrom.getMonthView().setLowerBound(calendar.getTime());
            jXDatePickerTo.getMonthView().setLowerBound(calendar.getTime());
            OdSati.setSelectedItem(calendar.get(Calendar.HOUR_OF_DAY));
            OdMinuta.setSelectedItem(calendar.get(Calendar.MINUTE));
            DoSati.setSelectedItem(calendar.get(Calendar.HOUR_OF_DAY));
            DoMinuta.setSelectedItem(calendar.get(Calendar.MINUTE));
        }

        ClientAppUtils.SetupFocusTraversal(this);

//        try {
//            jFormattedTextFieldTimeFrom.commitEdit();
//            jFormattedTextFieldTimeTo.commitEdit();
//        } catch (ParseException ex) {
//        }

        SetupFilterTables();

        setupDone = true;
        RefreshTable();

    }

    private void SetupFilterTables() {
        final JDialog loadingDialog = new LoadingDialog(null, true);

        String queryCashRegisters = "SELECT CR_NUMBER FROM CASH_REGISTERS WHERE OFFICE_NUMBER = ?";
        String queryCategories = "SELECT ID, NAME FROM CATEGORIES";
        String queryStaff = "SELECT ID, FIRST_NAME, LAST_NAME FROM STAFF WHERE OFFICE_NUMBER = ?";
        String queryArticles = "SELECT ARTICLES.ID, ARTICLES.NAME, MEASURING_UNITS.NAME, ARTICLES.PRICE "
                + "FROM ARTICLES "
                + "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID";
        String queryServices = "SELECT SERVICES.ID, SERVICES.NAME, MEASURING_UNITS.NAME, SERVICES.PRICE "
                + "FROM SERVICES "
                + "INNER JOIN MEASURING_UNITS ON SERVICES.MEASURING_UNIT_ID = MEASURING_UNITS.ID";
        String queryTradingGoods = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, TRADING_GOODS.PRICE "
                + "FROM TRADING_GOODS";
        String queryShifts = "SELECT ID, NAME, HF, HT, MF, MT FROM SHIFTS WHERE IS_DELETED = 0 AND OFFICE_NUMBER = ?";

        MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(7);
        multiDatabaseQuery.SetQuery(0, queryCashRegisters);
        multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
        multiDatabaseQuery.SetQuery(1, queryCategories);
        multiDatabaseQuery.SetQuery(2, queryStaff);
        multiDatabaseQuery.AddParam(2, 1, Licence.GetOfficeNumber());
        multiDatabaseQuery.SetQuery(3, queryArticles);
        multiDatabaseQuery.SetQuery(4, queryServices);
        multiDatabaseQuery.SetQuery(5, queryTradingGoods);
        multiDatabaseQuery.SetQuery(6, queryShifts);
        multiDatabaseQuery.AddParam(6, 1, Licence.GetOfficeNumber());

        ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

        databaseQueryTask.execute();
        loadingDialog.setVisible(true);
        if (!databaseQueryTask.isDone()) {
            databaseQueryTask.cancel(true);
        } else {
            try {
                ServerResponse serverResponse = databaseQueryTask.get();
                DatabaseQueryResult[] databaseQueryResult = null;
                if (serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS) {
                    databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
                }
                if (databaseQueryResult != null) {
                    // Cash registers
                //    DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
                //    defaultComboBoxModel.addElement("Sve kase");
                //    while (databaseQueryResult[0].next()) {
                //        String element = databaseQueryResult[0].getString(0);
                //        defaultComboBoxModel.addElement(element);
                //    }
                //  jComboBoxCashRegisters.setModel(defaultComboBoxModel);

                //  Categories
                    CustomTableModel customTableModelCategories = new CustomTableModel() {
                        @Override
                        public Class getColumnClass(int column) {
                            switch (column) {
                                case 0:
                                    return Boolean.class;
                                default:
                                    return String.class;
                            }
                        }
                    };
                    customTableModelCategories.setColumnIdentifiers(new String[]{"Odabir", "Šifra", "Kategorija"});
                    ArrayList<Integer> categoriesIdListTemp = new ArrayList<>();
                    while (databaseQueryResult[1].next()) {
                        Object[] rowData = new Object[3];
                        rowData[0] = true;
                        rowData[1] = databaseQueryResult[1].getInt(0);
                        rowData[2] = databaseQueryResult[1].getString(1);
                        categoriesIdListTemp.add(databaseQueryResult[1].getInt(0));
                        customTableModelCategories.addRow(rowData);
                    }
                    categoriesFilterIdList = categoriesIdListTemp;
                    jTableFilterCategory.setModel(customTableModelCategories);
                    jTableFilterCategory.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterCategory.getWidth() * 10 / 100);
                    jTableFilterCategory.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterCategory.getWidth() * 15 / 100);
                    jTableFilterCategory.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterCategory.getWidth() * 70 / 100);

                    // Staff
                    CustomTableModel customTableModelStaff = new CustomTableModel() {
                        @Override
                        public Class getColumnClass(int column) {
                            switch (column) {
                                case 0:
                                    return Boolean.class;
                                default:
                                    return String.class;
                            }
                        }
                    };
                    customTableModelStaff.setColumnIdentifiers(new String[]{"Odabir", "Šifra", "Djelatnik"});
                    ArrayList<Integer> staffIdListTemp = new ArrayList<>();
                    while (databaseQueryResult[2].next()) {
                        Object[] rowData = new Object[3];
                        rowData[0] = true;
                        rowData[1] = databaseQueryResult[2].getInt(0);
                        rowData[2] = databaseQueryResult[2].getString(1) + " " + databaseQueryResult[2].getString(2);
                        staffIdListTemp.add(databaseQueryResult[2].getInt(0));
                        customTableModelStaff.addRow(rowData);
                    }
                    staffFilterIdList = staffIdListTemp;
                    jTableFilterStaff.setModel(customTableModelStaff);
                    jTableFilterStaff.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterStaff.getWidth() * 10 / 100);
                    jTableFilterStaff.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterStaff.getWidth() * 15 / 100);
                    jTableFilterStaff.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterStaff.getWidth() * 70 / 100);

                    // Articles
                    CustomTableModel customTableModelArticles = new CustomTableModel() {
                        @Override
                        public Class getColumnClass(int column) {
                            switch (column) {
                                case 0:
                                    return Boolean.class;
                                default:
                                    return String.class;
                            }
                        }
                    };
                    customTableModelArticles.setColumnIdentifiers(new String[]{"Odabir", "Šifra", "Naziv", "Mj. jed.", "Cijena"});
                    ArrayList<Integer> articlesIdListTemp = new ArrayList<>();
                    while (databaseQueryResult[3].next()) {
                        Object[] rowData = new Object[5];
                        rowData[0] = true;
                        rowData[1] = databaseQueryResult[3].getInt(0);
                        rowData[2] = databaseQueryResult[3].getString(1);
                        rowData[3] = databaseQueryResult[3].getString(2);
                        rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult[3].getFloat(3));
                        articlesIdListTemp.add(databaseQueryResult[3].getInt(0));
                        customTableModelArticles.addRow(rowData);
                    }
                    articlesFilterIdList = articlesIdListTemp;
                    jTableFilterArticles.setModel(customTableModelArticles);
                    jTableFilterArticles.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterArticles.getWidth() * 10 / 100);
                    jTableFilterArticles.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterArticles.getWidth() * 15 / 100);
                    jTableFilterArticles.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterArticles.getWidth() * 40 / 100);
                    jTableFilterArticles.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneFilterArticles.getWidth() * 15 / 100);
                    jTableFilterArticles.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneFilterArticles.getWidth() * 15 / 100);

                    // Services
                    CustomTableModel customTableModelServices = new CustomTableModel() {
                        @Override
                        public Class getColumnClass(int column) {
                            switch (column) {
                                case 0:
                                    return Boolean.class;
                                default:
                                    return String.class;
                            }
                        }
                    };
                    customTableModelServices.setColumnIdentifiers(new String[]{"Odabir", "Šifra", "Naziv", "Mj. jed.", "Cijena"});
                    ArrayList<Integer> servicesIdListTemp = new ArrayList<>();
                    while (databaseQueryResult[4].next()) {
                        Object[] rowData = new Object[5];
                        rowData[0] = true;
                        rowData[1] = databaseQueryResult[4].getInt(0);
                        rowData[2] = databaseQueryResult[4].getString(1);
                        rowData[3] = databaseQueryResult[4].getString(2);
                        rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult[4].getFloat(3));
                        servicesIdListTemp.add(databaseQueryResult[4].getInt(0));
                        customTableModelServices.addRow(rowData);
                    }
                    servicesFilterIdList = servicesIdListTemp;
                    jTableFilterServices.setModel(customTableModelServices);
                    jTableFilterServices.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterServices.getWidth() * 10 / 100);
                    jTableFilterServices.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterServices.getWidth() * 15 / 100);
                    jTableFilterServices.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterServices.getWidth() * 40 / 100);
                    jTableFilterServices.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneFilterServices.getWidth() * 15 / 100);
                    jTableFilterServices.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneFilterServices.getWidth() * 15 / 100);

                    // Trading goods
                    CustomTableModel customTableModelTradingGoods = new CustomTableModel() {
                        @Override
                        public Class getColumnClass(int column) {
                            switch (column) {
                                case 0:
                                    return Boolean.class;
                                default:
                                    return String.class;
                            }
                        }
                    };
                    customTableModelTradingGoods.setColumnIdentifiers(new String[]{"Odabir", "Šifra", "Naziv", "Mj. jed.", "Cijena"});
                    ArrayList<Integer> tradingGoodsIdListTemp = new ArrayList<>();
                    while (databaseQueryResult[5].next()) {
                        Object[] rowData = new Object[5];
                        rowData[0] = true;
                        rowData[1] = databaseQueryResult[5].getInt(0);
                        rowData[2] = databaseQueryResult[5].getString(1);
                        rowData[3] = Values.TRADING_GOODS_MEASURING_UNIT;
                        rowData[4] = ClientAppUtils.FloatToPriceString(databaseQueryResult[5].getFloat(2));
                        tradingGoodsIdListTemp.add(databaseQueryResult[5].getInt(0));
                        customTableModelTradingGoods.addRow(rowData);
                    }
                    tradingGoodsFilterIdList = tradingGoodsIdListTemp;
                    jTableFilterTradingGoods.setModel(customTableModelTradingGoods);
                    jTableFilterTradingGoods.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterTradingGoods.getWidth() * 10 / 100);
                    jTableFilterTradingGoods.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterTradingGoods.getWidth() * 15 / 100);
                    jTableFilterTradingGoods.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterTradingGoods.getWidth() * 40 / 100);
                    jTableFilterTradingGoods.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneFilterTradingGoods.getWidth() * 15 / 100);
                    jTableFilterTradingGoods.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneFilterTradingGoods.getWidth() * 15 / 100);

                    // Shifts
//                    CustomTableModel customTableModelShifts = new CustomTableModel() {
//                        @Override
//                        public Class getColumnClass(int column) {
//                            switch (column) {
//                                case 0:
//                                    return Boolean.class;
//                                default:
//                                    return String.class;
//                            }
//                        }
//                    };
//                    customTableModelShifts.setColumnIdentifiers(new String[]{"Odabir", "Smjena", "Početak", "Kraj"});
//                    {
//                        Object[] rowData = new Object[4];
//                        rowData[0] = true;
//                        rowData[1] = "Cijeli dan";
//                        rowData[2] = "00:00";
//                        rowData[3] = "23:59";
//                        customTableModelShifts.addRow(rowData);
//                    }
//                    while (databaseQueryResult[6].next()) {
//                        Object[] rowData = new Object[4];
//                        rowData[0] = false;
//                        rowData[1] = databaseQueryResult[6].getString(1);
//                        String hf, ht, mf, mt;
//                        if (databaseQueryResult[6].getInt(2) < 10) {
//                            hf = "0" + databaseQueryResult[6].getInt(2);
//                        } else {
//                            hf = "" + databaseQueryResult[6].getInt(2);
//                        }
//                        if (databaseQueryResult[6].getInt(3) < 10) {
//                            ht = "0" + databaseQueryResult[6].getInt(3);
//                        } else {
//                            ht = "" + databaseQueryResult[6].getInt(3);
//                        }
//                        if (databaseQueryResult[6].getInt(4) < 10) {
//                            mf = "0" + databaseQueryResult[6].getInt(4);
//                        } else {
//                            mf = "" + databaseQueryResult[6].getInt(4);
//                        }
//                        if (databaseQueryResult[6].getInt(5) < 10) {
//                            mt = "0" + databaseQueryResult[6].getInt(5);
//                        } else {
//                            mt = "" + databaseQueryResult[6].getInt(5);
//                        }
//                        rowData[2] = hf + ":" + mf;
//                        rowData[3] = ht + ":" + mt;
//                        customTableModelShifts.addRow(rowData);
//                    }
//                    jTableFilterShifts.setModel(customTableModelShifts);
//                    jTableFilterShifts.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneFilterShifts.getWidth() * 10 / 100);
//                    jTableFilterShifts.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneFilterShifts.getWidth() * 20 / 100);
//                    jTableFilterShifts.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneFilterShifts.getWidth() * 35 / 100);
//                    jTableFilterShifts.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneFilterShifts.getWidth() * 35 / 100);
                }
            } catch (InterruptedException | ExecutionException ex) {
                ClientAppLogger.GetInstance().ShowErrorLog(ex);
            }
        }
    }

    private void RefreshTable() {
        if (!setupDone) {
            return;
        }

        String timeFrom = "00:00:00";
        String timeTo = "23:59:59";
//        if (jFormattedTextFieldTimeFrom.getValue() != null) {
//            Date notifDate = (Date) jFormattedTextFieldTimeFrom.getValue();
//            timeFrom = new SimpleDateFormat("HH:mm:00").format(notifDate);
//        }
//        if (jFormattedTextFieldTimeTo.getValue() != null) {
//            Date notifDate = (Date) jFormattedTextFieldTimeTo.getValue();
//            timeTo = new SimpleDateFormat("HH:mm:59").format(notifDate);
//        }
        timeFrom = OdSati.getSelectedItem().toString() + ":" + OdMinuta.getSelectedItem().toString() + ":00";
        timeTo = DoSati.getSelectedItem().toString() + ":" + DoMinuta.getSelectedItem().toString() + ":59";


        String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
        String dateToString = jXDatePickerTo.getEditor().getText().trim();
        Date dateFrom;
        Date dateTo;
        Date dateToOriginal;
        try {
            dateFrom = new SimpleDateFormat("dd.MM.yyyy HH:mm:00").parse(dateFromString + " " + timeFrom);
        } catch (ParseException ex) {
            ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Od");
            return;
        }
        try {
            dateToOriginal = new SimpleDateFormat("dd.MM.yyyy HH:mm:59").parse(dateToString + " " + timeTo);
            // Add one day - for shifts after midnight (will be filtered at shifts filter)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateToOriginal);
            //calendar.add(Calendar.DATE, 1);
            dateTo = calendar.getTime();
        } catch (ParseException ex) {
            ClientAppLogger.GetInstance().ShowMessage("Pogrešan unos datuma Do");
            return;
        }

        String cashRegisterNumber = "-1";
        
        
        if (jCheckBoxFirstCashRegister.isSelected() && jCheckBoxSecondCashRegister.isSelected()){
            cashRegisterNumber = "-1";
            previousSelected = true;
        }
        else if (jCheckBoxFirstCashRegister.isSelected()){
            cashRegisterNumber = "1";
            previousSelected = true;
        }
        else if (jCheckBoxSecondCashRegister.isSelected()){
            cashRegisterNumber = "2";
            previousSelected = true;
        }
        else {
           cashRegisterNumber = "99";
        }

        final JDialog loadingDialog = new LoadingDialog(null, true);

        String queryLocalItems = "SELECT LOCAL_INVOICE_ITEMS.IT_ID, LOCAL_INVOICE_ITEMS.IT_NAME, LOCAL_INVOICE_ITEMS.IT_TYPE, "
                + "LOCAL_INVOICE_ITEMS.AMT, LOCAL_INVOICE_ITEMS.PR, LOCAL_INVOICE_ITEMS.DIS_PCT, LOCAL_INVOICE_ITEMS.DIS_AMT, "
                + "LOCAL_INVOICE_ITEMS.TAX, LOCAL_INVOICE_ITEMS.C_TAX, LOCAL_INVOICE_ITEMS.IN_ID, PAY_NAME, PAY_TYPE, LOCAL_INVOICE_ITEMS.PACK_REF "
                + "FROM LOCAL_INVOICES "
                + "INNER JOIN LOCAL_INVOICE_ITEMS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_ITEMS.IN_ID "
                + "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
                + "AND (I_DATE > ? OR (I_DATE >= ? AND I_TIME >= ?)) AND (I_DATE < ? OR (I_DATE <= ? AND I_TIME <= ?)) "
                + "AND (LOCAL_INVOICES.CR_NUM = ? OR -1 = ?) AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?) "
                + "ORDER BY LOCAL_INVOICE_ITEMS.IT_TYPE, LOCAL_INVOICE_ITEMS.IT_NAME";
        String queryLocalInvoices = "SELECT LOCAL_INVOICES.ID, LOCAL_INVOICES.FIN_PR, LOCAL_INVOICES.DIS_PCT, LOCAL_INVOICES.DIS_AMT, S_ID, "
                + "STAFF.FIRST_NAME, (SELECT COUNT(LOCAL_INVOICE_ITEMS.ID) FROM LOCAL_INVOICE_ITEMS WHERE LOCAL_INVOICE_ITEMS.IN_ID = LOCAL_INVOICES.ID), "
                + "PAY_NAME, PAY_TYPE, I_TIME, I_DATE, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, I_NUM, SPEC_NUM, CR_NUM "
                + "FROM LOCAL_INVOICES "
                + "INNER JOIN STAFF ON STAFF.ID = LOCAL_INVOICES.S_ID "
                + "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
                + "AND (I_DATE > ? OR (I_DATE >= ? AND I_TIME >= ?)) AND (I_DATE < ? OR (I_DATE <= ? AND I_TIME <= ?)) "
                + "AND (LOCAL_INVOICES.CR_NUM = ? OR -1 = ?) AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?)";
        String queryLocalMaterials = "SELECT LOCAL_INVOICE_MATERIALS.IN_ID, LOCAL_INVOICE_MATERIALS.ART_ID, LOCAL_INVOICE_MATERIALS.MAT_ID, "
                + "LOCAL_INVOICE_MATERIALS.NORM, MATERIALS.NAME, MATERIALS.LAST_PRICE "
                + "FROM LOCAL_INVOICES "
                + "INNER JOIN LOCAL_INVOICE_MATERIALS ON LOCAL_INVOICES.ID = LOCAL_INVOICE_MATERIALS.IN_ID "
                + "INNER JOIN MATERIALS ON MATERIALS.ID = LOCAL_INVOICE_MATERIALS.MAT_ID "
                + "WHERE O_NUM = ? AND LOCAL_INVOICES.IS_DELETED = 0 "
                + "AND (I_DATE > ? OR (I_DATE >= ? AND I_TIME >= ?)) AND (I_DATE < ? OR (I_DATE <= ? AND I_TIME <= ?)) "
                + "AND (LOCAL_INVOICES.CR_NUM = ? OR -1 = ?) AND LOCAL_INVOICES.PAY_TYPE NOT IN (?, ?) "
                + "ORDER BY LOCAL_INVOICE_MATERIALS.MAT_ID";
        String queryLocalArticlesList = "SELECT ARTICLES.ID, MEASURING_UNITS.NAME, ARTICLES.CATEGORY_ID "
                + "FROM ARTICLES "
                + "INNER JOIN MEASURING_UNITS ON ARTICLES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
                + "ORDER BY ARTICLES.ID";
        String queryLocalServicesList = "SELECT SERVICES.ID, MEASURING_UNITS.NAME, SERVICES.CATEGORY_ID "
                + "FROM SERVICES "
                + "INNER JOIN MEASURING_UNITS ON SERVICES.MEASURING_UNIT_ID = MEASURING_UNITS.ID "
                + "ORDER BY SERVICES.ID";
        String queryLocalTradingGoodsList = "SELECT TRADING_GOODS.ID, TRADING_GOODS.CATEGORY_ID, LAST_PRICE, TAX_RATES.VALUE "
                + "FROM TRADING_GOODS "
                + "INNER JOIN TAX_RATES ON TRADING_GOODS.TAX_RATE_ID = TAX_RATES.ID "
                + "ORDER BY TRADING_GOODS.ID";
        String queryItems = queryLocalItems.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
        String queryInvoices = queryLocalInvoices.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
        String queryMaterials = queryLocalMaterials.replace("LOCAL_", "").replace(" AND INVOICES.IS_DELETED = 0", "");
        boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
        if (!isProduction) {
            queryLocalItems = queryLocalItems.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            queryLocalInvoices = queryLocalInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            queryLocalMaterials = queryLocalMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
            queryItems = queryItems.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            queryInvoices = queryInvoices.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_ITEMS", "INVOICE_ITEMS_TEST");
            queryMaterials = queryMaterials.replace("INVOICES", "INVOICES_TEST").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST");
        }
        

        MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(9);
        multiDatabaseQuery.SetQuery(0, queryLocalItems);
        multiDatabaseQuery.SetQuery(1, queryItems);
        multiDatabaseQuery.SetQuery(2, queryLocalInvoices);
        multiDatabaseQuery.SetQuery(3, queryInvoices);
        multiDatabaseQuery.SetQuery(4, queryLocalMaterials);
        multiDatabaseQuery.SetQuery(5, queryMaterials);
        multiDatabaseQuery.SetQuery(6, queryLocalArticlesList);
        multiDatabaseQuery.SetQuery(7, queryLocalServicesList);
        multiDatabaseQuery.SetQuery(8, queryLocalTradingGoodsList);

        for (int i = 0; i < 6; ++i) {
            multiDatabaseQuery.AddParam(i, 1, Licence.GetOfficeNumber());
            multiDatabaseQuery.AddParam(i, 2, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
            multiDatabaseQuery.AddParam(i, 3, new SimpleDateFormat("yyyy-MM-dd").format(dateFrom));
            multiDatabaseQuery.AddParam(i, 4, timeFrom);
            multiDatabaseQuery.AddParam(i, 5, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
            multiDatabaseQuery.AddParam(i, 6, new SimpleDateFormat("yyyy-MM-dd").format(dateTo));
            multiDatabaseQuery.AddParam(i, 7, timeTo);
            multiDatabaseQuery.AddParam(i, 8, cashRegisterNumber);
            multiDatabaseQuery.AddParam(i, 9, cashRegisterNumber);
            multiDatabaseQuery.AddParam(i, 10, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP);
            multiDatabaseQuery.AddParam(i, 11, Values.PAYMENT_METHOD_TYPE_OFFER);
        }

        ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, multiDatabaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

        databaseQueryTask.execute();
        loadingDialog.setVisible(true);
        if (!databaseQueryTask.isDone()) {
            databaseQueryTask.cancel(true);
        } else {
            try {
                ServerResponse serverResponse = databaseQueryTask.get();
                DatabaseQueryResult[] databaseQueryResult = null;
                if (serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS) {
                    databaseQueryResult = ((MultiDatabaseQueryResponse) serverResponse).databaseQueryResult;
                }
                if (databaseQueryResult != null) {
                    ArrayList<InvoiceItem> items = new ArrayList<>();
                    ArrayList<Integer> staffIdList = new ArrayList<>();
                    ArrayList<String> staffNameList = new ArrayList<>();
                    ArrayList<Float> staffAmountSumList = new ArrayList<>();
                    ArrayList<Float> staffDiscountAmountSumList = new ArrayList<>();

                    ArrayList<Integer> articlesIdList = new ArrayList<>();
                    ArrayList<Integer> articlesCategoryIdList = new ArrayList<>();
                    ArrayList<String> articlesMeasuringUnitList = new ArrayList<>();
                    ArrayList<Integer> servicesIdList = new ArrayList<>();
                    ArrayList<Integer> servicesCategoryIdList = new ArrayList<>();
                    ArrayList<String> servicesMeasuringUnitList = new ArrayList<>();
                    ArrayList<Integer> tradingGoodsIdList = new ArrayList<>();
                    ArrayList<Integer> tradingGoodsCategoryIdList = new ArrayList<>();
                    ArrayList<Float> tradingGoodsPurchasePriceList = new ArrayList<>();

                    ArrayList<Integer> invoicesIdList = new ArrayList<>();
                    ArrayList<String> invoicesNumList = new ArrayList<>();
                    ArrayList<Integer> invoicesSpecNumList = new ArrayList<>();
                    ArrayList<Integer> invoicesItemCountList = new ArrayList<>();
                    ArrayList<Float> invoicesTotalPriceList = new ArrayList<>();
                    ArrayList<Float> invoicesTotalPriceList2 = new ArrayList<>();
                    ArrayList<Float> invoicesDiscountAmountList = new ArrayList<>();
                    ArrayList<Integer> invoicesStaffIdList = new ArrayList<>();
                    ArrayList<String> invoicesPaymentMethodsList = new ArrayList<>();
                    ArrayList<String> invoicesPaymentMethodsList2 = new ArrayList<>();
                    ArrayList<Integer> invoicesPaymentMethodsTypeList2 = new ArrayList<>();
                    ArrayList<Date> invoicesDateList = new ArrayList<>();

                    ArrayList<Integer> localInvoicesIdList = new ArrayList<>();
                    ArrayList<String> localInvoicesNumList = new ArrayList<>();
                    ArrayList<Integer> localInvoicesSpecNumList = new ArrayList<>();
                    ArrayList<Integer> localInvoicesItemCountList = new ArrayList<>();
                    ArrayList<Float> localInvoicesTotalPriceList = new ArrayList<>();
                    ArrayList<Float> localInvoicesTotalPriceList2 = new ArrayList<>();
                    ArrayList<Float> localInvoicesDiscountAmountList = new ArrayList<>();
                    ArrayList<Integer> localInvoicesStaffIdList = new ArrayList<>();
                    ArrayList<String> localInvoicesPaymentMethodsList = new ArrayList<>();
                    ArrayList<String> localInvoicesPaymentMethodsList2 = new ArrayList<>();
                    ArrayList<Integer> localInvoicesPaymentMethodsTypeList2 = new ArrayList<>();
                    ArrayList<Date> localInvoicesDateList = new ArrayList<>();

                    ArrayList<String> paymentMethodsList = new ArrayList<>();
                    ArrayList<Double> paymentMethodsAmountList = new ArrayList<>();
                    ArrayList<DailyTrafficData> dailyTrafficList = new ArrayList<>();

                    ArrayList<MaterialsConsumptionData> localMaterialsConsumptionDataList = new ArrayList<>();
                    ArrayList<MaterialsConsumptionData> materialsConsumptionDataList = new ArrayList<>();
                    ArrayList<Integer> materialsIdList = new ArrayList<>();
                    ArrayList<String> materialsNameList = new ArrayList<>();
                    ArrayList<Float> materialsAmountList = new ArrayList<>();
                    ArrayList<Float> materialsPurchasePriceList = new ArrayList<>();

                    double totalInvoicePriceSum = 0d;
                    double totalinvoiceDiscountSum = 0d;
                                        
                    // Check if any filter set - used for total: payment methods
                    boolean anyFilterSet, articlesFilterSet, tradingGoodsFilterSet, servicesFilterSet, staffFilterSet, categoryFilterSet;
                    articlesFilterSet = tradingGoodsFilterSet = servicesFilterSet = staffFilterSet = categoryFilterSet = false;
                    for (int i = 0; i < jTableFilterArticles.getModel().getRowCount(); ++i) {
                        boolean tableValue = (Boolean) jTableFilterArticles.getModel().getValueAt(i, 0);
                        if (!tableValue) {
                            articlesFilterSet = true;
                            break;
                        }
                    }
                    for (int i = 0; i < jTableFilterTradingGoods.getModel().getRowCount(); ++i) {
                        boolean tableValue = (Boolean) jTableFilterTradingGoods.getModel().getValueAt(i, 0);
                        if (!tableValue) {
                            tradingGoodsFilterSet = true;
                            break;
                        }
                    }
                    for (int i = 0; i < jTableFilterServices.getModel().getRowCount(); ++i) {
                        boolean tableValue = (Boolean) jTableFilterServices.getModel().getValueAt(i, 0);
                        if (!tableValue) {
                            servicesFilterSet = true;
                            break;
                        }
                    }
                    for (int i = 0; i < jTableFilterStaff.getModel().getRowCount(); ++i) {
                        boolean tableValue = (Boolean) jTableFilterStaff.getModel().getValueAt(i, 0);
                        if (!tableValue) {
                            staffFilterSet = true;
                            break;
                        }
                    }
                    for (int i = 0; i < jTableFilterCategory.getModel().getRowCount(); ++i) {
                        boolean tableValue = (Boolean) jTableFilterCategory.getModel().getValueAt(i, 0);
                        if (!tableValue) {
                            categoryFilterSet = true;
                            break;
                        }
                    }
                    //boolean shiftsFilterSet = !((Boolean) jTableFilterShifts.getModel().getValueAt(0, 0));
                    anyFilterSet = articlesFilterSet || tradingGoodsFilterSet || servicesFilterSet || staffFilterSet || categoryFilterSet; // || shiftsFilterSet;

                    // Items
                    while (databaseQueryResult[6].next()) {
                        articlesIdList.add(databaseQueryResult[6].getInt(0));
                        articlesMeasuringUnitList.add(databaseQueryResult[6].getString(1));
                        articlesCategoryIdList.add(databaseQueryResult[6].getInt(2));
                    }
                    while (databaseQueryResult[7].next()) {
                        servicesIdList.add(databaseQueryResult[7].getInt(0));
                        servicesMeasuringUnitList.add(databaseQueryResult[7].getString(1));
                        servicesCategoryIdList.add(databaseQueryResult[7].getInt(2));
                    }
                    while (databaseQueryResult[8].next()) {
                        tradingGoodsIdList.add(databaseQueryResult[8].getInt(0));
                        tradingGoodsCategoryIdList.add(databaseQueryResult[8].getInt(1));
                        tradingGoodsPurchasePriceList.add(databaseQueryResult[8].getFloat(2) * (100f + databaseQueryResult[8].getFloat(3)) / 100f);
                    }

                    // Local invoices
                    while (databaseQueryResult[2].next()) {
                        if (databaseQueryResult[2].getInt(14) != 0 && localInvoicesNumList.contains(databaseQueryResult[2].getInt(14) + "/" + databaseQueryResult[2].getInt(13))) {
                            continue;
                        }
                        if (databaseQueryResult[2].getInt(15) != 0 && localInvoicesSpecNumList.contains(databaseQueryResult[2].getInt(15))) {
                            continue;
                        }

                        if (totalType == 0) {
                            if (databaseQueryResult[2].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
                                continue;
                            }
                        } else if (totalType == 1) {
                            if (databaseQueryResult[2].getInt(8) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[2].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        } else if (totalType == 2) {
                            if (databaseQueryResult[2].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[2].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        }

                        localInvoicesNumList.add(databaseQueryResult[2].getInt(14) + "/" + databaseQueryResult[2].getInt(13));
                        localInvoicesSpecNumList.add(databaseQueryResult[2].getInt(15));

                        localInvoicesIdList.add(databaseQueryResult[2].getInt(0));
                        localInvoicesItemCountList.add(databaseQueryResult[2].getInt(6));
                        localInvoicesTotalPriceList.add(databaseQueryResult[2].getFloat(1));
                        localInvoicesTotalPriceList2.add(databaseQueryResult[2].getFloat(13));
                        float discountAmount = databaseQueryResult[2].getFloat(1) * databaseQueryResult[2].getFloat(2) / 100f + databaseQueryResult[2].getFloat(3);
                        localInvoicesDiscountAmountList.add(discountAmount);
                        

                        int staffId = databaseQueryResult[2].getInt(4);
                        localInvoicesStaffIdList.add(staffId);

                        int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
                        if (staffListId == -1) {
                            staffIdList.add(staffId);
                            staffNameList.add(databaseQueryResult[2].getString(5));
                            staffAmountSumList.add(0f);
                            staffDiscountAmountSumList.add(0f);
                        }

                        String payMethod = databaseQueryResult[2].getString(7);
                        localInvoicesPaymentMethodsList.add(payMethod);

                        String payMethod2 = databaseQueryResult[2].getString(11);
                        localInvoicesPaymentMethodsList2.add(payMethod2);
                        int payMethodType2 = databaseQueryResult[2].getInt(12);
                        localInvoicesPaymentMethodsTypeList2.add(payMethodType2);

                        int payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);
                        if (payMethodListId == -1) {
                            paymentMethodsList.add(payMethod);
                            paymentMethodsAmountList.add(0d);
                        }

                        int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                        if (payMethodListId2 == -1) {
                            paymentMethodsList.add(payMethod2);
                            paymentMethodsAmountList.add(0d);
                        }

                        Date invoiceDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[2].getString(10) + " " + databaseQueryResult[2].getString(9));
                        localInvoicesDateList.add(invoiceDate);

                        if (!anyFilterSet && invoiceDate.after(dateFrom) && invoiceDate.before(dateTo)) {
                            float invoiceAmountWithDiscount = (databaseQueryResult[2].getFloat(1) - databaseQueryResult[2].getFloat(13)) * (100f - databaseQueryResult[2].getFloat(2)) / 100f - databaseQueryResult[2].getFloat(3);
                            payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);
                            paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + ClientAppUtils.FloatToPriceFloat(invoiceAmountWithDiscount));

                            float invoiceAmountWithDiscount2 = databaseQueryResult[2].getFloat(13) * (100f - databaseQueryResult[2].getFloat(2)) / 100f - databaseQueryResult[2].getFloat(3);
                            payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                            paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + ClientAppUtils.FloatToPriceFloat(invoiceAmountWithDiscount2));
                        }
                    }

                    // Invoices
                    while (databaseQueryResult[3].next()) {
                    //    if (databaseQueryResult[3].getInt(14) != 0 && invoicesNumList.contains(databaseQueryResult[3].getInt(14) + "/" + databaseQueryResult[3].getInt(13))) {
                    //        continue;
                    //    }
                    //    if (databaseQueryResult[3].getInt(15) != 0 && invoicesSpecNumList.contains(databaseQueryResult[3].getInt(15))) {
                    //        continue;
                    //    }

                        if (totalType == 0) {
                            if (databaseQueryResult[3].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
                                continue;
                            }
                        } else if (totalType == 1) {
                            if (databaseQueryResult[3].getInt(8) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[3].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        } else if (totalType == 2) {
                            if (databaseQueryResult[3].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[3].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        }

                        invoicesNumList.add(databaseQueryResult[3].getInt(14) + "/" + databaseQueryResult[3].getInt(13));
                        invoicesSpecNumList.add(databaseQueryResult[3].getInt(15));

                        invoicesIdList.add(databaseQueryResult[3].getInt(0));
                        invoicesItemCountList.add(databaseQueryResult[3].getInt(6));
                        invoicesTotalPriceList.add(databaseQueryResult[3].getFloat(1));
                        invoicesTotalPriceList2.add(databaseQueryResult[3].getFloat(13));
                        float discountAmount = databaseQueryResult[3].getFloat(1) * databaseQueryResult[3].getFloat(2) / 100f + databaseQueryResult[3].getFloat(3);
                        invoicesDiscountAmountList.add(discountAmount);
                        
                        
                        int staffId = databaseQueryResult[3].getInt(4);
                        invoicesStaffIdList.add(staffId);

                        int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
                        if (staffListId == -1) {
                            staffIdList.add(staffId);
                            staffNameList.add(databaseQueryResult[3].getString(5));
                            staffAmountSumList.add(0f);
                            staffDiscountAmountSumList.add(0f);
                        }

                        String payMethod = databaseQueryResult[3].getString(7);
                        invoicesPaymentMethodsList.add(payMethod);

                        String payMethod2 = databaseQueryResult[3].getString(11);
                        invoicesPaymentMethodsList2.add(payMethod2);
                        int payMethodType2 = databaseQueryResult[3].getInt(12);
                        invoicesPaymentMethodsTypeList2.add(payMethodType2);

                        int payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);
                        if (payMethodListId == -1) {
                            paymentMethodsList.add(payMethod);
                            paymentMethodsAmountList.add(0d);
                        }

                        int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                        if (payMethodListId2 == -1) {
                            paymentMethodsList.add(payMethod2);
                            paymentMethodsAmountList.add(0d);
                        }

                        Date invoiceDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[3].getString(10) + " " + databaseQueryResult[3].getString(9));
                        invoicesDateList.add(invoiceDate);

                        if (!anyFilterSet && invoiceDate.after(dateFrom) && invoiceDate.before(dateTo)) {
                            float invoiceAmountWithDiscount = (databaseQueryResult[3].getFloat(1) - databaseQueryResult[3].getFloat(13)) * (100f - databaseQueryResult[3].getFloat(2)) / 100f - databaseQueryResult[3].getFloat(3);
                            payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);
                            paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + ClientAppUtils.FloatToPriceFloat(invoiceAmountWithDiscount));

                            float invoiceAmountWithDiscount2 = databaseQueryResult[3].getFloat(13) * (100f - databaseQueryResult[3].getFloat(2)) / 100f - databaseQueryResult[3].getFloat(3);
                            payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                            paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + ClientAppUtils.FloatToPriceFloat(invoiceAmountWithDiscount2));
                        }
                    }

                    // Local materials
                    while (databaseQueryResult[4].next()) {
                        if (!localInvoicesIdList.contains(databaseQueryResult[4].getInt(0))) {
                            continue;
                        }

                        MaterialsConsumptionData mcd = new MaterialsConsumptionData();
                        mcd.invoiceId = databaseQueryResult[4].getInt(0);
                        mcd.articleId = databaseQueryResult[4].getInt(1);
                        mcd.materialId = databaseQueryResult[4].getInt(2);
                        mcd.normative = databaseQueryResult[4].getFloat(3);
                        mcd.materialName = databaseQueryResult[4].getString(4);
                        mcd.lastPrice = databaseQueryResult[4].getFloat(5);
                        localMaterialsConsumptionDataList.add(mcd);
                    }

                    // Materials
                    while (databaseQueryResult[5].next()) {
                        if (!invoicesIdList.contains(databaseQueryResult[5].getInt(0))) {
                            continue;
                        }

                        MaterialsConsumptionData mcd = new MaterialsConsumptionData();
                        mcd.invoiceId = databaseQueryResult[5].getInt(0);
                        mcd.articleId = databaseQueryResult[5].getInt(1);
                        mcd.materialId = databaseQueryResult[5].getInt(2);
                        mcd.normative = databaseQueryResult[5].getFloat(3);
                        mcd.materialName = databaseQueryResult[5].getString(4);
                        mcd.lastPrice = databaseQueryResult[5].getFloat(5);
                        materialsConsumptionDataList.add(mcd);
                    }

                    // Local invoice items
                    while (databaseQueryResult[0].next()) {
                        if (!localInvoicesIdList.contains(databaseQueryResult[0].getInt(9))) {
                            continue;
                        }

                        if (totalType == 0) {
                            if (databaseQueryResult[0].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
                                continue;
                            }
                        } else if (totalType == 1) {
                            if (databaseQueryResult[0].getInt(11) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[0].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        } else if (totalType == 2) {
                            if (databaseQueryResult[0].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[0].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        }

                        int itemId = databaseQueryResult[0].getInt(0);
                        String itemName = databaseQueryResult[0].getString(1);
                        int itemType = databaseQueryResult[0].getInt(2);
                        float itemAmount = databaseQueryResult[0].getFloat(3);
                        float itemPrice = databaseQueryResult[0].getFloat(4);
                        float itemDisPct = databaseQueryResult[0].getFloat(5);
                        float itemDisAmt = databaseQueryResult[0].getFloat(6);
                        float itemTax = databaseQueryResult[0].getFloat(7);
                        float itemConsTax = databaseQueryResult[0].getFloat(8);
                        float packagingRefund = databaseQueryResult[0].getFloat(12);

                        // Categories filter
                        int categoryId = -1;
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemId);
                            categoryId = articlesCategoryIdList.get(itemListId);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(servicesIdList, itemId);
                            categoryId = servicesCategoryIdList.get(itemListId);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(tradingGoodsIdList, itemId);
                            categoryId = tradingGoodsCategoryIdList.get(itemListId);
                        }
                        int rowId = ClientAppUtils.ArrayIndexOf(categoriesFilterIdList, categoryId);
                        boolean value = (Boolean) jTableFilterCategory.getModel().getValueAt(rowId, 0);
                        int invoiceListId = ClientAppUtils.ArrayIndexOf(localInvoicesIdList, databaseQueryResult[0].getInt(9));
                        if (!value) {
                            localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Staff filter
                        int staffId = localInvoicesStaffIdList.get(invoiceListId);
                        int staffTableRowId = ClientAppUtils.ArrayIndexOf(staffFilterIdList, staffId);
                        boolean staffTableValue = (Boolean) jTableFilterStaff.getModel().getValueAt(staffTableRowId, 0);
                        if (!staffTableValue) {
                            localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Shifts filter (with dateTo plus one day shifts after midnight filter)
                        Date invoiceDate = localInvoicesDateList.get(invoiceListId);
                        boolean dateInRange = false;
                        int invoiceHour = Integer.parseInt(new SimpleDateFormat("HH").format(invoiceDate));
                        int invoiceMinute = Integer.parseInt(new SimpleDateFormat("mm").format(invoiceDate));
                        boolean isPreviousDayInvoice = false;

                        String invoiceD = new SimpleDateFormat("dd").format(invoiceDate);
                        String invoiceM = new SimpleDateFormat("MM").format(invoiceDate);
                        String invoiceY = new SimpleDateFormat("yyyy").format(invoiceDate);
                        String dateFromD = new SimpleDateFormat("dd").format(dateFrom);
                        String dateFromM = new SimpleDateFormat("MM").format(dateFrom);
                        String dateFromY = new SimpleDateFormat("yyyy").format(dateFrom);
                        String dateToD = new SimpleDateFormat("dd").format(dateTo);
                        String dateToM = new SimpleDateFormat("MM").format(dateTo);
                        String dateToY = new SimpleDateFormat("yyyy").format(dateTo);
//                        if(isAnyShiftEnabled){
//                            for (int i = 0; i < jTableFilterShifts.getRowCount(); ++i) {
//                                boolean shiftsTableValue = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
//                                if (!shiftsTableValue) {
//                                    continue;
//                                }
//
//                                String fromTime = (String) jTableFilterShifts.getModel().getValueAt(i, 2);
//                                String toTime = (String) jTableFilterShifts.getModel().getValueAt(i, 3);
//                                int fromHour = Integer.parseInt(fromTime.split(":")[0]);
//                                int fromMinute = Integer.parseInt(fromTime.split(":")[1]);
//                                int toHour = Integer.parseInt(toTime.split(":")[0]);
//                                int toMinute = Integer.parseInt(toTime.split(":")[1]);
//
//                                if (fromHour < toHour) {
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        continue;
//                                    }
//
//                                    if ((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute)
//                                            && (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)) {
//                                        dateInRange = true;
//                                        break;
//                                    }
//                                } else if (fromHour == toHour && fromMinute <= toMinute) {
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        continue;
//                                    }
//
//                                    if (invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute) {
//                                        dateInRange = true;
//                                        break;
//                                    }
//                                } else {
//                                    if (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute) {
//                                        isPreviousDayInvoice = true;
//                                    }
//
//                                    if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)) {
//                                        if (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute) {
//                                            continue;
//                                        }
//                                    }
//
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        if (invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute) {
//                                            continue;
//                                        }
//                                    }
//
//                                    if (!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute)
//                                            && (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))) {
//                                        dateInRange = true;
//                                        break;
//                                    }
//                                }
//                            }
//                        }else{
//                            int fromHour = 0, fromMinute = 0, toHour = 0, toMinute = 0;
//                            
//                            //timeFrom = OdSati.getSelectedItem().toString() + ":" + OdMinuta.getSelectedItem().toString() + ":00";
//                            //timeTo = DoSati.getSelectedItem().toString() + ":" + DoMinuta.getSelectedItem().toString() + ":59";
//
//                            fromHour = Integer.parseInt(new SimpleDateFormat("HH").format(dateFrom));
//                            fromMinute = Integer.parseInt(new SimpleDateFormat("mm").format(dateFrom));
//                            toHour = Integer.parseInt(new SimpleDateFormat("HH").format(dateTo));
//                            toMinute = Integer.parseInt(new SimpleDateFormat("mm").format(dateTo));
//
//                            if (fromHour < toHour) {
//                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                    continue;
//                                }
//
//                                if ((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute)
//                                        && (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            } else if (fromHour == toHour && fromMinute <= toMinute) {
//                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                    continue;
//                                }
//
//                                if (invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            } else {
//                                if (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute) {
//                                    isPreviousDayInvoice = true;
//                                }
//
//                                if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)) {
//                                    if (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute) {
//                                        continue;
//                                    }
//                                }
//
//                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                    if (invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute) {
//                                        continue;
//                                    }
//                                }
//
//                                if (!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute)
//                                        && (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            }
                        //}
                        //if (!dateInRange) {
                            localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
                            //continue;
                        //}

                        // Items filter
                        boolean itemTablesValue = true;
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(articlesFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterArticles.getModel().getValueAt(filterRowId, 0);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(servicesFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterServices.getModel().getValueAt(filterRowId, 0);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(tradingGoodsFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterTradingGoods.getModel().getValueAt(filterRowId, 0);
                        }
                        if (!itemTablesValue) {
                            localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Item data
                        float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
                        float itemPriceWithoutDiscount = itemPrice * itemAmount;
                        float itemToInvoicePriceRatio = (itemPriceWithoutDiscount - itemDiscount) / (localInvoicesTotalPriceList.get(invoiceListId) != 0f ? localInvoicesTotalPriceList.get(invoiceListId) : 1f);
                        float itemInvoiceDiscount = itemToInvoicePriceRatio * localInvoicesDiscountAmountList.get(invoiceListId);
                        totalinvoiceDiscountSum += itemDiscount + itemInvoiceDiscount;
                        totalInvoicePriceSum += itemPriceWithoutDiscount;

                        // Staff info
                        int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
                        staffAmountSumList.set(staffListId, staffAmountSumList.get(staffListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
                        staffDiscountAmountSumList.set(staffListId, staffDiscountAmountSumList.get(staffListId) + itemDiscount + itemInvoiceDiscount);

                        // Payment method info
                        String payMethod = localInvoicesPaymentMethodsList.get(invoiceListId);
                        int payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);

                        if (anyFilterSet) {
                            if (localInvoicesPaymentMethodsTypeList2.get(invoiceListId) == -1) {
                                paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
                            } else {
                                String payMethod2 = localInvoicesPaymentMethodsList2.get(invoiceListId);
                                int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                                float paymentMethodsRatio = localInvoicesTotalPriceList2.get(invoiceListId) / localInvoicesTotalPriceList.get(invoiceListId);
                                float itemPriceWithDiscount = itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount;
                                paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + (1f - paymentMethodsRatio) * itemPriceWithDiscount);
                                paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + paymentMethodsRatio * itemPriceWithDiscount);
                            }
                        }

                        // Daily traffic info
                        Date dailyTrafficDate = invoiceDate;
                        if (isPreviousDayInvoice) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dailyTrafficDate);
                            calendar.add(Calendar.DATE, -1);
                            dailyTrafficDate = calendar.getTime();
                        }
                        int dailyTrafficListIndex = -1;
                        for (int i = 0; i < dailyTrafficList.size(); ++i) {
                            if (dailyTrafficList.get(i).equalsDate(dailyTrafficDate)) {
                                dailyTrafficListIndex = i;
                                break;
                            }
                        }
                        if (dailyTrafficListIndex == -1) {
                            DailyTrafficData dailyTrafficData = new DailyTrafficData();
                            dailyTrafficData.SetDate(dailyTrafficDate);
                            dailyTrafficList.add(dailyTrafficData);
                            dailyTrafficListIndex = dailyTrafficList.size() - 1;
                        }

                        DailyTrafficData dailyTrafficData = dailyTrafficList.get(dailyTrafficListIndex);
                        if (localInvoicesPaymentMethodsTypeList2.get(invoiceListId) == -1) {
                            dailyTrafficData.AddAmountPaymentType(itemPriceWithoutDiscount, itemDiscount + itemInvoiceDiscount, payMethod);
                        } else {
                            String payMethod2 = localInvoicesPaymentMethodsList2.get(invoiceListId);
                            float paymentMethodsRatio = localInvoicesTotalPriceList2.get(invoiceListId) / localInvoicesTotalPriceList.get(invoiceListId);
                            dailyTrafficData.AddAmountPaymentType((1f - paymentMethodsRatio) * itemPriceWithoutDiscount, (1f - paymentMethodsRatio) * (itemDiscount + itemInvoiceDiscount), payMethod);
                            dailyTrafficData.AddAmountPaymentType(paymentMethodsRatio * itemPriceWithoutDiscount, paymentMethodsRatio * (itemDiscount + itemInvoiceDiscount), payMethod2);
                        }
                        dailyTrafficList.set(dailyTrafficListIndex, dailyTrafficData);

                        // Material consumption data
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            for (int i = 0; i < localMaterialsConsumptionDataList.size(); ++i) {
                                MaterialsConsumptionData mcd = localMaterialsConsumptionDataList.get(i);
                                if (mcd.invoiceId == localInvoicesIdList.get(invoiceListId) && mcd.articleId == itemId) {
                                    boolean materialFound = false;
                                    for (int j = 0; j < materialsIdList.size(); ++j) {
                                        if (mcd.materialId == materialsIdList.get(j)) {
                                            materialFound = true;
                                            materialsAmountList.set(j, materialsAmountList.get(j) + mcd.normative * itemAmount);
                                        }
                                    }
                                    if (!materialFound) {
                                        materialsIdList.add(mcd.materialId);
                                        materialsNameList.add(mcd.materialName);
                                        materialsAmountList.add(mcd.normative * itemAmount);
                                        materialsPurchasePriceList.add(mcd.lastPrice);
                                    }
                                }
                            }
                        }

                        // Add item
                        boolean itemFound = false;
                        for (int i = 0; i < items.size(); ++i) {
                            if (items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
                                    && items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
                                    && items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax
                                    && items.get(i).packagingRefund == packagingRefund
                                    && items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)) {
                                itemFound = true;
                                items.get(i).itemAmount += itemAmount;
                            }
                        }
                        if (!itemFound) {
                            InvoiceItem invoiceItem = new InvoiceItem();
                            invoiceItem.itemId = itemId;
                            invoiceItem.itemName = itemName;
                            invoiceItem.itemType = itemType;
                            invoiceItem.itemAmount = itemAmount;
                            invoiceItem.itemPrice = itemPrice;
                            invoiceItem.discountPercentage = itemDisPct;
                            invoiceItem.discountValue = itemDisAmt;
                            invoiceItem.taxRate = itemTax;
                            invoiceItem.consumptionTaxRate = itemConsTax;
                            invoiceItem.packagingRefund = packagingRefund;
                            invoiceItem.invoiceDiscountTotal = (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f);
                            items.add(invoiceItem);
                        }
                    }

                    // Invoice items
                    while (databaseQueryResult[1].next()) {
                        if (!invoicesIdList.contains(databaseQueryResult[1].getInt(9))) {
                            continue;
                        }

                        if (totalType == 0) {
                            if (databaseQueryResult[1].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
                                continue;
                            }
                        } else if (totalType == 1) {
                            if (databaseQueryResult[1].getInt(11) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[1].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        } else if (totalType == 2) {
                            if (databaseQueryResult[1].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[1].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)) {
                                continue;
                            }
                        }

                        int itemId = databaseQueryResult[1].getInt(0);
                        String itemName = databaseQueryResult[1].getString(1);
                        int itemType = databaseQueryResult[1].getInt(2);
                        float itemAmount = databaseQueryResult[1].getFloat(3);
                        float itemPrice = databaseQueryResult[1].getFloat(4);
                        float itemDisPct = databaseQueryResult[1].getFloat(5);
                        float itemDisAmt = databaseQueryResult[1].getFloat(6);
                        float itemTax = databaseQueryResult[1].getFloat(7);
                        float itemConsTax = databaseQueryResult[1].getFloat(8);
                        float packagingRefund = databaseQueryResult[1].getFloat(12);

                        // Categories filter
                        int categoryId = -1;
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemId);
                            categoryId = articlesCategoryIdList.get(itemListId);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(servicesIdList, itemId);
                            categoryId = servicesCategoryIdList.get(itemListId);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            int itemListId = ClientAppUtils.ArrayIndexOf(tradingGoodsIdList, itemId);
                            categoryId = tradingGoodsCategoryIdList.get(itemListId);
                        }
                        int rowId = ClientAppUtils.ArrayIndexOf(categoriesFilterIdList, categoryId);
                        boolean value = (Boolean) jTableFilterCategory.getModel().getValueAt(rowId, 0);
                        int invoiceListId = ClientAppUtils.ArrayIndexOf(invoicesIdList, databaseQueryResult[1].getInt(9));
                        if (!value) {
                            invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Staff filter
                        int staffId = invoicesStaffIdList.get(invoiceListId);
                        int staffTableRowId = ClientAppUtils.ArrayIndexOf(staffFilterIdList, staffId);
                        boolean staffTableValue = (Boolean) jTableFilterStaff.getModel().getValueAt(staffTableRowId, 0);
                        if (!staffTableValue) {
                            invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Shifts filter (with dateTo plus one day shifts after midnight filter)
                        Date invoiceDate = invoicesDateList.get(invoiceListId);
                        boolean dateInRange = false;
                        int invoiceHour = Integer.parseInt(new SimpleDateFormat("HH").format(invoiceDate));
                        int invoiceMinute = Integer.parseInt(new SimpleDateFormat("mm").format(invoiceDate));
                        boolean isPreviousDayInvoice = false;

                        String invoiceD = new SimpleDateFormat("dd").format(invoiceDate);
                        String invoiceM = new SimpleDateFormat("MM").format(invoiceDate);
                        String invoiceY = new SimpleDateFormat("yyyy").format(invoiceDate);
                        String dateFromD = new SimpleDateFormat("dd").format(dateFrom);
                        String dateFromM = new SimpleDateFormat("MM").format(dateFrom);
                        String dateFromY = new SimpleDateFormat("yyyy").format(dateFrom);
                        String dateToD = new SimpleDateFormat("dd").format(dateTo);
                        String dateToM = new SimpleDateFormat("MM").format(dateTo);
                        String dateToY = new SimpleDateFormat("yyyy").format(dateTo);
//                        if (isAnyShiftEnabled) {
//                            for (int i = 0; i < jTableFilterShifts.getRowCount(); ++i) {
//                                boolean shiftsTableValue = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
//                                if (!shiftsTableValue) {
//                                    continue;
//                                }
//                                int fromHour = 0, fromMinute = 0, toHour = 0, toMinute = 0;
//
//                                String fromTime = (String) jTableFilterShifts.getModel().getValueAt(i, 2);
//                                String toTime = (String) jTableFilterShifts.getModel().getValueAt(i, 3);
//                                fromHour = Integer.parseInt(fromTime.split(":")[0]);
//                                fromMinute = Integer.parseInt(fromTime.split(":")[1]);
//                                toHour = Integer.parseInt(toTime.split(":")[0]);
//                                toMinute = Integer.parseInt(toTime.split(":")[1]);
//
//                                if (fromHour < toHour) {
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        continue;
//                                    }
//
//                                    if ((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute)
//                                            && (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)) {
//                                        dateInRange = true;
//                                        //break;
//                                    }
//                                } else if (fromHour == toHour && fromMinute <= toMinute) {
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        continue;
//                                    }
//
//                                    if (invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute) {
//                                        dateInRange = true;
//                                        //break;
//                                    }
//                                } else {
//                                    if (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute) {
//                                        isPreviousDayInvoice = true;
//                                    }
//
//                                    if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)) {
//                                        if (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute) {
//                                            continue;
//                                        }
//                                    }
//
//                                    if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                        if (invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute) {
//                                            continue;
//                                        }
//                                    }
//
//                                    if (!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute)
//                                            && (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))) {
//                                        dateInRange = true;
//                                        //break;
//                                    }
//                                }
//                            }
//                        } else {
//                            int fromHour = 0, fromMinute = 0, toHour = 0, toMinute = 0;
//
//                            fromHour = Integer.parseInt(new SimpleDateFormat("HH").format(dateFrom));
//                            fromMinute = Integer.parseInt(new SimpleDateFormat("mm").format(dateFrom));
//                            toHour = Integer.parseInt(new SimpleDateFormat("HH").format(dateTo));
//                            toMinute = Integer.parseInt(new SimpleDateFormat("mm").format(dateTo));
//
//                            if (fromHour < toHour) {
////                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
////                                    continue;
////                                }
//
//                                if ((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute)
//                                        && (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            } else if (fromHour == toHour && fromMinute <= toMinute) {
//                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                    continue;
//                                }
//
//                                if (invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            } else {
//                                if (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute) {
//                                    isPreviousDayInvoice = true;
//                                }
//
//                                if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)) {
//                                    if (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute) {
//                                        continue;
//                                    }
//                                }
//
//                                if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)) {
//                                    if (invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute) {
//                                        continue;
//                                    }
//                                }
//
//                                if (!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute)
//                                        && (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))) {
//                                    dateInRange = true;
//                                    //break;
//                                }
//                            }
                        //}
                        //if (!dateInRange) {
                            invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
                            //continue;
                        //}

                        // Items filter
                        boolean itemTablesValue = true;
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(articlesFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterArticles.getModel().getValueAt(filterRowId, 0);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(servicesFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterServices.getModel().getValueAt(filterRowId, 0);
                        } else if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            int filterRowId = ClientAppUtils.ArrayIndexOf(tradingGoodsFilterIdList, itemId);
                            itemTablesValue = (Boolean) jTableFilterTradingGoods.getModel().getValueAt(filterRowId, 0);
                        }
                        if (!itemTablesValue) {
                            invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
                            continue;
                        }

                        // Item data
                        float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
                        float itemPriceWithoutDiscount = itemPrice * itemAmount;
                        float itemToInvoicePriceRatio = (itemPriceWithoutDiscount - itemDiscount) / (invoicesTotalPriceList.get(invoiceListId) != 0f ? invoicesTotalPriceList.get(invoiceListId) : 1f);
                        float itemInvoiceDiscount = itemToInvoicePriceRatio * invoicesDiscountAmountList.get(invoiceListId);
                        totalinvoiceDiscountSum += itemDiscount + itemInvoiceDiscount;
                        totalInvoicePriceSum += itemPriceWithoutDiscount;

                        // Staff info
                        int staffListId = ClientAppUtils.ArrayIndexOf(staffIdList, staffId);
                        staffAmountSumList.set(staffListId, staffAmountSumList.get(staffListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
                        staffDiscountAmountSumList.set(staffListId, staffDiscountAmountSumList.get(staffListId) + itemDiscount + itemInvoiceDiscount);

                        // Payment method info
                        String payMethod = invoicesPaymentMethodsList.get(invoiceListId);
                        int payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);

                        if (anyFilterSet) {
                            if (invoicesPaymentMethodsTypeList2.get(invoiceListId) == -1) {
                                paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
                            } else {
                                String payMethod2 = invoicesPaymentMethodsList2.get(invoiceListId);
                                int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
                                float paymentMethodsRatio = invoicesTotalPriceList2.get(invoiceListId) / invoicesTotalPriceList.get(invoiceListId);
                                float itemPriceWithDiscount = itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount;
                                paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + (1f - paymentMethodsRatio) * itemPriceWithDiscount);
                                paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + paymentMethodsRatio * itemPriceWithDiscount);
                            }
                        }
                        // Daily traffic info
                        Date dailyTrafficDate = invoiceDate;
                        if (isPreviousDayInvoice) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dailyTrafficDate);
                            calendar.add(Calendar.DATE, -1);
                            dailyTrafficDate = calendar.getTime();
                        }
                        int dailyTrafficListIndex = -1;
                        for (int i = 0; i < dailyTrafficList.size(); ++i) {
                            if (dailyTrafficList.get(i).equalsDate(dailyTrafficDate)) {
                                dailyTrafficListIndex = i;
                                break;
                            }
                        }
                        if (dailyTrafficListIndex == -1) {
                            DailyTrafficData dailyTrafficData = new DailyTrafficData();
                            dailyTrafficData.SetDate(dailyTrafficDate);
                            dailyTrafficList.add(dailyTrafficData);
                            dailyTrafficListIndex = dailyTrafficList.size() - 1;
                        }

                        DailyTrafficData dailyTrafficData = dailyTrafficList.get(dailyTrafficListIndex);
                        if (invoicesPaymentMethodsTypeList2.get(invoiceListId) == -1) {
                            dailyTrafficData.AddAmountPaymentType(itemPriceWithoutDiscount, itemDiscount + itemInvoiceDiscount, payMethod);
                        } else {
                            String payMethod2 = invoicesPaymentMethodsList2.get(invoiceListId);
                            float paymentMethodsRatio = invoicesTotalPriceList2.get(invoiceListId) / invoicesTotalPriceList.get(invoiceListId);
                            dailyTrafficData.AddAmountPaymentType((1f - paymentMethodsRatio) * itemPriceWithoutDiscount, (1f - paymentMethodsRatio) * (itemDiscount + itemInvoiceDiscount), payMethod);
                            dailyTrafficData.AddAmountPaymentType(paymentMethodsRatio * itemPriceWithoutDiscount, paymentMethodsRatio * (itemDiscount + itemInvoiceDiscount), payMethod2);
                        }
                        dailyTrafficList.set(dailyTrafficListIndex, dailyTrafficData);

                        // Material consumption data
                        if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            for (int i = 0; i < materialsConsumptionDataList.size(); ++i) {
                                MaterialsConsumptionData mcd = materialsConsumptionDataList.get(i);
                                if (mcd.invoiceId == invoicesIdList.get(invoiceListId) && mcd.articleId == itemId) {
                                    boolean materialFound = false;
                                    for (int j = 0; j < materialsIdList.size(); ++j) {
                                        if (mcd.materialId == materialsIdList.get(j)) {
                                            materialFound = true;
                                            materialsAmountList.set(j, materialsAmountList.get(j) + mcd.normative * itemAmount);
                                        }
                                    }
                                    if (!materialFound) {
                                        materialsIdList.add(mcd.materialId);
                                        materialsNameList.add(mcd.materialName);
                                        materialsAmountList.add(mcd.normative * itemAmount);
                                        materialsPurchasePriceList.add(mcd.lastPrice);
                                    }
                                }
                            }
                        }

                        // Add item
                        boolean itemFound = false;
                        for (int i = 0; i < items.size(); ++i) {
                            if (items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
                                    && items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
                                    && items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax
                                    && items.get(i).packagingRefund == packagingRefund
                                    && items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)) {
                                itemFound = true;
                                items.get(i).itemAmount += itemAmount;
                            }
                        }
                        if (!itemFound) {
                            InvoiceItem invoiceItem = new InvoiceItem();
                            invoiceItem.itemId = itemId;
                            invoiceItem.itemName = itemName;
                            invoiceItem.itemType = itemType;
                            invoiceItem.itemAmount = itemAmount;
                            invoiceItem.itemPrice = itemPrice;
                            invoiceItem.discountPercentage = itemDisPct;
                            invoiceItem.discountValue = itemDisAmt;
                            invoiceItem.taxRate = itemTax;
                            invoiceItem.consumptionTaxRate = itemConsTax;
                            invoiceItem.packagingRefund = packagingRefund;
                            invoiceItem.invoiceDiscountTotal = (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f);
                            items.add(invoiceItem);
                        }
                    }

                    // Update labels
                    int invoiceCount = 0;
                    for (int i = 0; i < localInvoicesNumList.size(); ++i) {
                        if (localInvoicesNumList.get(i) != null) {
                            ++invoiceCount;
                        }
                    }
                    for (int i = 0; i < invoicesNumList.size(); ++i) {
                        if (invoicesNumList.get(i) != null) {
                            ++invoiceCount;
                        }
                    }
                    jLabelInvoiceCount.setText(Integer.toString(invoiceCount));
                    jLabelTotalPrice.setText(ClientAppUtils.DoubleToPriceString(totalInvoicePriceSum - totalinvoiceDiscountSum));
                    jLabelDiscount.setText(ClientAppUtils.DoubleToPriceString(totalinvoiceDiscountSum));
                    jLabelWithoutDiscount.setText(ClientAppUtils.DoubleToPriceString(totalInvoicePriceSum));

                    // Table items
                    ArrayList<InvoiceItem> itemsMerged = new ArrayList<>();
                    for (int i = 0; i < items.size(); ++i) {
                        boolean itemFound = false;
                        float itemDisAmt = 0f;
                        if (items.get(i).discountPercentage != 0f) {
                            itemDisAmt += items.get(i).discountPercentage * items.get(i).itemAmount * items.get(i).itemPrice / 100f;
                        } else if (items.get(i).discountValue != 0f) {
                            itemDisAmt += items.get(i).discountValue * items.get(i).itemAmount;
                        }
                        if (items.get(i).invoiceDiscountTotal != 0f) {
                            itemDisAmt += items.get(i).invoiceDiscountTotal * items.get(i).itemAmount;
                        }

                        for (int j = 0; j < itemsMerged.size(); ++j) {
                            if (items.get(i).itemId == itemsMerged.get(j).itemId && items.get(i).itemType == itemsMerged.get(j).itemType && items.get(i).itemPrice == itemsMerged.get(j).itemPrice
                                    //&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
                                    && items.get(i).taxRate == itemsMerged.get(j).taxRate && items.get(i).consumptionTaxRate == itemsMerged.get(j).consumptionTaxRate
                                    && items.get(i).packagingRefund == itemsMerged.get(j).packagingRefund
                                    && items.get(i).itemName.equals(itemsMerged.get(j).itemName) //&& items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)
                                    ) {
                                itemFound = true;
                                itemsMerged.get(j).itemAmount += items.get(i).itemAmount;
                                itemsMerged.get(j).discountValue += ClientAppUtils.FloatToPriceFloat(itemDisAmt);
                                break;
                            }
                        }
                        if (!itemFound) {
                            InvoiceItem invoiceItem = new InvoiceItem();
                            invoiceItem.itemId = items.get(i).itemId;
                            invoiceItem.itemName = items.get(i).itemName;
                            invoiceItem.itemType = items.get(i).itemType;
                            invoiceItem.itemAmount = items.get(i).itemAmount;
                            invoiceItem.itemPrice = items.get(i).itemPrice;
                            invoiceItem.discountPercentage = 0f;
                            invoiceItem.discountValue = ClientAppUtils.FloatToPriceFloat(itemDisAmt);
                            invoiceItem.taxRate = items.get(i).taxRate;
                            invoiceItem.consumptionTaxRate = items.get(i).consumptionTaxRate;
                            invoiceItem.packagingRefund = items.get(i).packagingRefund;
                            invoiceItem.invoiceDiscountTotal = 0f;
                            itemsMerged.add(invoiceItem);
                        }
                    }

                    for (int i = 0; i < itemsMerged.size(); ++i) {
                        if (itemsMerged.get(i).itemAmount == 0f && itemsMerged.get(i).discountValue != 0f) {
                            itemsMerged.get(i).itemAmount = 1f;
                            itemsMerged.get(i).itemPrice = 0f;
                        }
                    }

                    CustomTableModel customTableModelItems = new CustomTableModel();
                    customTableModelItems.setColumnIdentifiers(new String[]{"Šifra", "Tip stavke", "Naziv", "Količina", "Mj. jed.", "Cijena", "Popust", "Ukupno"});
                    double sumItemsMerged = 0f;
                    for (int i = 0; i < itemsMerged.size(); ++i) {
                        if (itemsMerged.get(i).itemAmount == 0f) {
                            continue;
                        }

                        String itemType = "Artikl";
                        if (itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            itemType = "Usluga";
                        } else if (itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            itemType = "Trg. roba";
                        }

                        String discount = "";
                        float totalPrice = itemsMerged.get(i).itemAmount * itemsMerged.get(i).itemPrice;
                        if (itemsMerged.get(i).discountValue != 0f) {
                            discount = ClientAppUtils.FloatToPriceFloat(itemsMerged.get(i).discountValue) + "";
                            totalPrice = totalPrice - itemsMerged.get(i).discountValue;
                        }

                        String measuringUnit = Values.TRADING_GOODS_MEASURING_UNIT;
                        if (itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int listId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemsMerged.get(i).itemId);
                            measuringUnit = articlesMeasuringUnitList.get(listId);
                        } else if (itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int listId = ClientAppUtils.ArrayIndexOf(servicesIdList, itemsMerged.get(i).itemId);
                            measuringUnit = servicesMeasuringUnitList.get(listId);
                        }

                        Object[] rowData = new Object[8];
                        rowData[0] = itemsMerged.get(i).itemId;
                        rowData[1] = itemType;
                        rowData[2] = itemsMerged.get(i).itemName;
                        rowData[3] = itemsMerged.get(i).itemAmount;
                        rowData[4] = measuringUnit;
                        rowData[5] = ClientAppUtils.FloatToPriceString(itemsMerged.get(i).itemPrice);
                        rowData[6] = discount;
                        rowData[7] = ClientAppUtils.FloatToPriceString(totalPrice);
                        customTableModelItems.addRow(rowData);

                        sumItemsMerged += ClientAppUtils.FloatToPriceFloat(totalPrice);
                    }
                    //System.out.println("Stavke:          " + ClientAppUtils.DoubleToPriceString(sumItemsMerged));

                    jTableTotalItems.setModel(customTableModelItems);
                    jTableTotalItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 30 / 100);
                    jTableTotalItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItems.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
                    jTableTotalItems.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);

                    // Table staff
                    CustomTableModel customTableModelStaff = new CustomTableModel();
                    customTableModelStaff.setColumnIdentifiers(new String[]{"Djelatnik", "Ukupno bez popusta", "Ukupno popusta", "Ukupno"});
                    for (int i = 0; i < staffIdList.size(); ++i) {
                        Object[] rowData = new Object[4];
                        rowData[0] = staffIdList.get(i) + "-" + staffNameList.get(i);
                        rowData[1] = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i) + staffAmountSumList.get(i));
                        rowData[2] = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i));
                        rowData[3] = ClientAppUtils.FloatToPriceString(staffAmountSumList.get(i));
                        customTableModelStaff.addRow(rowData);
                    }

                    jTableTotalStaff.setModel(customTableModelStaff);
                    jTableTotalStaff.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 20 / 100);
                    jTableTotalStaff.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 20 / 100);
                    jTableTotalStaff.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 20 / 100);
                    jTableTotalStaff.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 20 / 100);
                    jTableTotalStaff.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalStaff.getWidth() * 20 / 100);


                    // Table payment methods
                    CustomTableModel customTableModelPayMethods = new CustomTableModel();
                    customTableModelPayMethods.setColumnIdentifiers(new String[]{"Način plaćanja", "Ukupno"});

                    double sumPaymentMethods = 0d;
                    for (int i = 0; i < paymentMethodsList.size(); ++i) {
                        if (paymentMethodsAmountList.get(i) == 0f) {
                            continue;
                        }

                        Object[] rowData = new Object[2];
                        rowData[0] = paymentMethodsList.get(i);
                        rowData[1] = ClientAppUtils.DoubleToPriceString(paymentMethodsAmountList.get(i));
                        customTableModelPayMethods.addRow(rowData);

                        sumPaymentMethods += ClientAppUtils.DoubleToPriceDouble(paymentMethodsAmountList.get(i));
                        System.out.println("Načini plaćanja: " + ClientAppUtils.DoubleToPriceString(paymentMethodsAmountList.get(i)) + " " + ClientAppUtils.DoubleToPriceDouble(paymentMethodsAmountList.get(i)) + " " + paymentMethodsAmountList.get(i));
                    }
                    System.out.println("Načini plaćanja: " + ClientAppUtils.DoubleToPriceString(sumPaymentMethods));

                    jTableTotalPayMethod.setModel(customTableModelPayMethods);
                    jTableTotalPayMethod.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalPayMethod.getWidth() * 60 / 100);
                    jTableTotalPayMethod.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalPayMethod.getWidth() * 40 / 100);

                    // Table daily traffic
                    CustomTableModel customTableModelDailyTraffic = new CustomTableModel();
                    customTableModelDailyTraffic.setColumnIdentifiers(new String[]{
                        "Datum", "Ukupno bez popusta", "Popust", "Ukupno", "Gotovina", "Maestro", "Mastercard", "Amex", "Diners", "Visa", "Virman", "Ostalo"
                    });
                    for (int i = 0; i < dailyTrafficList.size(); ++i) {
                        Object[] rowData = new Object[12];
                        rowData[0] = new SimpleDateFormat("dd.MM.yyyy").format(dailyTrafficList.get(i).date);
                        rowData[1] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).amountWithoutDiscount);
                        rowData[2] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).discount);
                        rowData[3] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).amountWithoutDiscount - dailyTrafficList.get(i).discount);
                        rowData[4] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).cash);
                        rowData[5] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).maestro);
                        rowData[6] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).mastercard);
                        rowData[7] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).amex);
                        rowData[8] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).diners);
                        rowData[9] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).visa);
                        rowData[10] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).transactionBill);
                        rowData[11] = ClientAppUtils.FloatToPriceString(dailyTrafficList.get(i).other);
                        customTableModelDailyTraffic.addRow(rowData);
                    }

                    jTableTotalDays.setModel(customTableModelDailyTraffic);
                    jTableTotalDays.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 17 / 100);
                    jTableTotalDays.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 20 / 100);
                    jTableTotalDays.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(9).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(10).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
                    jTableTotalDays.getColumnModel().getColumn(11).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);

                    // Table materials consumption
                    CustomTableModel customTableModelMaterials = new CustomTableModel();
                    customTableModelMaterials.setColumnIdentifiers(new String[]{"Šifra", "Naziv", "Količina", "Nabavna cijena", "Uk. nab. cijena"});
                    for (int i = 0; i < materialsIdList.size(); ++i) {
                        Object[] rowData = new Object[5];
                        rowData[0] = materialsIdList.get(i);
                        rowData[1] = materialsNameList.get(i);
                        rowData[2] = materialsAmountList.get(i);
                        rowData[3] = ClientAppUtils.FloatToPriceString(materialsPurchasePriceList.get(i));
                        rowData[4] = ClientAppUtils.FloatToPriceString(materialsPurchasePriceList.get(i) * materialsAmountList.get(i));
                        customTableModelMaterials.addRow(rowData);
                    }

                    jTableTotalMaterials.setModel(customTableModelMaterials);
                    jTableTotalMaterials.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 15 / 100);
                    jTableTotalMaterials.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 25 / 100);
                    jTableTotalMaterials.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 25 / 100);
                    jTableTotalMaterials.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 15 / 100);
                    jTableTotalMaterials.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 15 / 100);

                    // Table trading goods consumption
                    CustomTableModel customTableModelTradingGoods = new CustomTableModel();
                    customTableModelTradingGoods.setColumnIdentifiers(new String[]{"Šifra", "Naziv", "Kol.", "Nabavna cijena", "Prodajna cijena", "Uk. nab. cijena", "Uk. prod. cijena", "Pov. naknada", "Stopa", "Porez", "Razlika"});
                    for (int i = 0; i < items.size(); ++i) {
                        if (items.get(i).itemAmount == 0f || items.get(i).itemType != Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            continue;
                        }

                        float totalPrice = items.get(i).itemAmount * items.get(i).itemPrice;
                        if (items.get(i).discountPercentage != 0f) {
                            totalPrice = totalPrice * (100f - items.get(i).discountPercentage) / 100f;
                        } else if (items.get(i).discountValue != 0f) {
                            totalPrice = totalPrice - items.get(i).discountValue * items.get(i).itemAmount;
                        }
                        if (items.get(i).invoiceDiscountTotal != 0f) {
                            totalPrice -= items.get(i).invoiceDiscountTotal * items.get(i).itemAmount;
                        }

                        int itemListId = ClientAppUtils.ArrayIndexOf(tradingGoodsIdList, items.get(i).itemId);
                        float purchasePrice = tradingGoodsPurchasePriceList.get(itemListId);

                        Object[] rowData = new Object[11];
                        rowData[0] = items.get(i).itemId;
                        rowData[1] = items.get(i).itemName;
                        rowData[2] = items.get(i).itemAmount;
                        rowData[3] = ClientAppUtils.FloatToPriceString(purchasePrice);
                        rowData[4] = ClientAppUtils.FloatToPriceString(totalPrice / items.get(i).itemAmount);
                        rowData[5] = ClientAppUtils.FloatToPriceString(purchasePrice * items.get(i).itemAmount);
                        rowData[6] = ClientAppUtils.FloatToPriceString(totalPrice);
                        rowData[7] = items.get(i).packagingRefund * items.get(i).itemAmount;
                        rowData[8] = ClientAppUtils.FloatToPriceString(items.get(i).taxRate) + "%";
                        float totalPriceWithoutPackagingRefunds = totalPrice - items.get(i).packagingRefund * items.get(i).itemAmount;
                        rowData[9] = ClientAppUtils.FloatToPriceString(totalPriceWithoutPackagingRefunds - 100f * totalPriceWithoutPackagingRefunds / (items.get(i).taxRate + 100f));
                        rowData[10] = ClientAppUtils.FloatToPriceString(totalPrice - purchasePrice * items.get(i).itemAmount);
                        customTableModelTradingGoods.addRow(rowData);
                    }

                    jTableTotalTradingGoods.setModel(customTableModelTradingGoods);
                    jTableTotalTradingGoods.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(9).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);
                    jTableTotalTradingGoods.getColumnModel().getColumn(10).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);

                    // Table items - detailed
                    CustomTableModel customTableModelItemsDetailed = new CustomTableModel();
                    customTableModelItemsDetailed.setColumnIdentifiers(new String[]{"Šifra", "Tip stavke", "Naziv", "Količina", "Mj. jed.", "Cijena", "Popust", "Popust na račun", "Ukupno"});
                    double sumItemsDetail = 0d;
                    for (int i = 0; i < items.size(); ++i) {
                        if (items.get(i).itemAmount == 0f) {
                            continue;
                        }

                        String itemType = "Artikl";
                        if (items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            itemType = "Usluga";
                        } else if (items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS) {
                            itemType = "Trg. roba";
                        }

                        String discount = "";
                        float totalPrice = items.get(i).itemAmount * items.get(i).itemPrice;
                        if (items.get(i).discountPercentage != 0f) {
                            discount = items.get(i).discountPercentage + "%";
                            totalPrice = totalPrice * (100f - items.get(i).discountPercentage) / 100f;
                        } else if (items.get(i).discountValue != 0f) {
                            discount = ClientAppUtils.FloatToPriceFloat(items.get(i).discountValue) + " eur/kom";
                            totalPrice = totalPrice - items.get(i).discountValue * items.get(i).itemAmount;
                        }
                        String invoiceDiscount = "";
                        if (items.get(i).invoiceDiscountTotal != 0f) {
                            invoiceDiscount = ClientAppUtils.FloatToPriceString(items.get(i).invoiceDiscountTotal) + " eur/kom";
                            totalPrice -= items.get(i).invoiceDiscountTotal * items.get(i).itemAmount;
                        }

                        String measuringUnit = Values.TRADING_GOODS_MEASURING_UNIT;
                        if (items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE) {
                            int listId = ClientAppUtils.ArrayIndexOf(articlesIdList, items.get(i).itemId);
                            measuringUnit = articlesMeasuringUnitList.get(listId);
                        } else if (items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE) {
                            int listId = ClientAppUtils.ArrayIndexOf(servicesIdList, items.get(i).itemId);
                            measuringUnit = servicesMeasuringUnitList.get(listId);
                        }

                        Object[] rowData = new Object[9];
                        rowData[0] = items.get(i).itemId;
                        rowData[1] = itemType;
                        rowData[2] = items.get(i).itemName;
                        rowData[3] = items.get(i).itemAmount;
                        rowData[4] = measuringUnit;
                        rowData[5] = ClientAppUtils.FloatToPriceString(items.get(i).itemPrice);
                        rowData[6] = discount;
                        rowData[7] = invoiceDiscount;
                        rowData[8] = ClientAppUtils.FloatToPriceString(totalPrice);
                        customTableModelItemsDetailed.addRow(rowData);

                        sumItemsDetail += ClientAppUtils.FloatToPriceFloat(totalPrice);
                    }
                    //System.out.println("Stavke detalji:  " + ClientAppUtils.DoubleToPriceString(sumItemsDetail));

                    jTableTotalItemsDetailed.setModel(customTableModelItemsDetailed);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 30 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
                    jTableTotalItemsDetailed.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);

                    // Table taxes
                    CustomTableModel customTableModelTaxes = new CustomTableModel();
                    customTableModelTaxes.setColumnIdentifiers(new String[]{"Stopa", "Osnovica", "Iznos poreza"});

                    Invoice taxInvoice = new Invoice();
                    double totalInvoicePrice = 0d;
                                        
                    for (int i = 0; i < itemsMerged.size(); ++i) {
                        if (itemsMerged.get(i).itemAmount == 0f) {
                            continue;
                        }

                        InvoiceItem taxItem = new InvoiceItem(itemsMerged.get(i));

                        // itemsMerged hacks (discountValue already includes amount, invoiceDiscountTotal is 0, discount percentage is 0)
                        float totalPrice = taxItem.itemAmount * taxItem.itemPrice;
                        if (taxItem.discountValue != 0f) {
                            totalPrice = totalPrice - taxItem.discountValue;
                            taxItem.discountValue /= taxItem.itemAmount;
                        }

                        taxInvoice.items.add(taxItem);
                        taxInvoice.totalPrice += totalPrice;
                        totalInvoicePrice += totalPrice;
                    }
                    //System.out.println("Tax invoice tot: " + ClientAppUtils.DoubleToPriceString(taxInvoice.totalPrice));
                    //System.out.println("Tax invoice dbl: " + ClientAppUtils.DoubleToPriceString(totalInvoicePrice));

                    InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(taxInvoice, totalInvoicePrice);
                    double sumTaxes = 0d;
                    for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i) {
                        /*if(invoiceTaxes.taxRates.get(i) == 0d)
							continue;*/

                        Object[] rowData = new Object[3];
                        rowData[0] = invoiceTaxes.taxRates.get(i) + "%";
                        rowData[1] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i));
                        rowData[2] = ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i));
                        customTableModelTaxes.addRow(rowData);

                        if (!invoiceTaxes.isConsumpionTax.get(i)) {
                            sumTaxes += ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxBases.get(i));
                            //System.out.println("Razrada poreza: " + ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxBases.get(i)));
                        }
                        sumTaxes += ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxAmounts.get(i));
                        //System.out.println("Razrada poreza: " + ClientAppUtils.DoubleToPriceDouble(invoiceTaxes.taxAmounts.get(i)));
                    }
                    //System.out.println("Razrada poreza:  " + ClientAppUtils.DoubleToPriceString(sumTaxes) + System.lineSeparator());

                    jTableTotalTaxes.setModel(customTableModelTaxes);
                    jTableTotalTaxes.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalTaxes.getWidth() * 20 / 100);
                    jTableTotalTaxes.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalTaxes.getWidth() * 30 / 100);
                    jTableTotalTaxes.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalTaxes.getWidth() * 30 / 100);

                    // Sorters
                    {
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTotalItems.getModel());
                        jTableTotalItems.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
                        sorter.setSortKeys(sortKeys);
                        sorter.sort();
                    }
                    {
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTotalItemsDetailed.getModel());
                        jTableTotalItemsDetailed.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
                        sorter.setSortKeys(sortKeys);
                        sorter.sort();
                    }
                    {
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTotalMaterials.getModel());
                        jTableTotalMaterials.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
                        sorter.setSortKeys(sortKeys);
                        sorter.sort();
                    }
                    {
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTotalTradingGoods.getModel());
                        jTableTotalTradingGoods.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
                        sorter.setSortKeys(sortKeys);
                        sorter.sort();
                    }
                    {
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableTotalDays.getModel());
                        jTableTotalDays.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
                        sorter.setSortKeys(sortKeys);
                        sorter.sort();
                    }
                }
            } catch (InterruptedException | ExecutionException | ParseException ex) {
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
        jLabelInternetConnection = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jXDatePickerFrom = new org.jdesktop.swingx.JXDatePicker();
        jXDatePickerTo = new org.jdesktop.swingx.JXDatePicker();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        OdMinuta = new javax.swing.JComboBox<>();
        OdSati = new javax.swing.JComboBox<>();
        DoSati = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        DoMinuta = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jButtonPrintPosInvoicesTable = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonPrintA4InvoicesTable = new javax.swing.JButton();
        jButtonPrintA4InvoicesTable1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jCheckBoxFirstCashRegister = new javax.swing.JCheckBox();
        jCheckBoxSecondCashRegister = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPaneFilterCategory = new javax.swing.JScrollPane();
        jTableFilterCategory = new javax.swing.JTable();
        jButtonCategoriesAll = new javax.swing.JButton();
        jButtonCategoriesNone = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPaneFilterStaff = new javax.swing.JScrollPane();
        jTableFilterStaff = new javax.swing.JTable();
        jButtonStaffNone = new javax.swing.JButton();
        jButtonStaffAll = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPaneFilterArticles = new javax.swing.JScrollPane();
        jTableFilterArticles = new javax.swing.JTable();
        jPanel19 = new javax.swing.JPanel();
        jTextFieldFilterArticles = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jButtonArticlesNone = new javax.swing.JButton();
        jButtonArticlesAll = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jScrollPaneFilterTradingGoods = new javax.swing.JScrollPane();
        jTableFilterTradingGoods = new javax.swing.JTable();
        jPanel20 = new javax.swing.JPanel();
        jButtonTradingGoodsAll = new javax.swing.JButton();
        jButtonTradingGoodsNone = new javax.swing.JButton();
        jTextFieldFilterTradingGoods = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPaneFilterServices = new javax.swing.JScrollPane();
        jTableFilterServices = new javax.swing.JTable();
        jPanel21 = new javax.swing.JPanel();
        jButtonServicesAll = new javax.swing.JButton();
        jButtonServicesNone = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldFilterServices = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabelInvoiceCount = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabelTotalPrice = new javax.swing.JLabel();
        jLabelWithoutDiscount = new javax.swing.JLabel();
        jLabelDiscount = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel23 = new javax.swing.JPanel();
        jScrollPaneTotalItems = new javax.swing.JScrollPane();
        jTableTotalItems = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jScrollPaneTotalStaff = new javax.swing.JScrollPane();
        jTableTotalStaff = new javax.swing.JTable();
        jPanel16 = new javax.swing.JPanel();
        jScrollPaneTotalPayMethod = new javax.swing.JScrollPane();
        jTableTotalPayMethod = new javax.swing.JTable();
        jPanel17 = new javax.swing.JPanel();
        jScrollPaneTotalDays = new javax.swing.JScrollPane();
        jTableTotalDays = new javax.swing.JTable();
        jPanel18 = new javax.swing.JPanel();
        jScrollPaneTotalMaterials = new javax.swing.JScrollPane();
        jTableTotalMaterials = new javax.swing.JTable();
        jPanel22 = new javax.swing.JPanel();
        jScrollPaneTotalTradingGoods = new javax.swing.JScrollPane();
        jTableTotalTradingGoods = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneTotalItemsDetailed = new javax.swing.JScrollPane();
        jTableTotalItemsDetailed = new javax.swing.JTable();
        jPanel24 = new javax.swing.JPanel();
        jScrollPaneTotalTaxes = new javax.swing.JScrollPane();
        jTableTotalTaxes = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Total");
        setAlwaysOnTop(true);
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
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Total");

        jLabelInternetConnection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelInternetConnection.setForeground(new java.awt.Color(255, 0, 0));
        jLabelInternetConnection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelInternetConnection.setText("NEMA INTERNETSKE VEZE");
        jLabelInternetConnection.setName("jLabelInternetConnection"); // NOI18N
        jLabelInternetConnection.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder("Period"));

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

        jLabel1.setText("Od:");
        jLabel1.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel11.setText("Do:");
        jLabel11.setPreferredSize(new java.awt.Dimension(45, 14));

        jLabel18.setText(":");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.setPreferredSize(new java.awt.Dimension(45, 14));

        OdMinuta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", " " }));

        OdSati.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));

        DoSati.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        DoSati.setSelectedIndex(23);

        jLabel19.setText(":");
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.setPreferredSize(new java.awt.Dimension(45, 14));

        DoMinuta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", " " }));
        DoMinuta.setSelectedIndex(59);

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jXDatePickerTo, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(OdSati, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(DoSati, 0, 1, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(OdMinuta, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelButtonsLayout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DoMinuta, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jXDatePickerFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OdMinuta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OdSati, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXDatePickerTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DoSati, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DoMinuta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButtonPrintPosInvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis POS <br> [F4] </div> </html>");
        jButtonPrintPosInvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintPosInvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintPosInvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintPosInvoicesTableActionPerformed(evt);
            }
        });

        jButtonExit.setText("<html> <div style=\"text-align: center\"> Izlaz <br> [ESC] </div> </html>");
        jButtonExit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExit.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonPrintA4InvoicesTable.setText("<html> <div style=\"text-align: center\"> Ispis A4 <br> [F5] </div> </html>");
        jButtonPrintA4InvoicesTable.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4InvoicesTable.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4InvoicesTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4InvoicesTableActionPerformed(evt);
            }
        });

        jButtonPrintA4InvoicesTable1.setBackground(new java.awt.Color(0, 153, 0));
        jButtonPrintA4InvoicesTable1.setForeground(new java.awt.Color(50, 145, 50));
        jButtonPrintA4InvoicesTable1.setText("<html> <div style=\"text-align: center\"> Pokreni <br>izračun  </div> </html>");
        jButtonPrintA4InvoicesTable1.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrintA4InvoicesTable1.setPreferredSize(new java.awt.Dimension(75, 60));
        jButtonPrintA4InvoicesTable1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintA4InvoicesTable1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPrintA4InvoicesTable1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonPrintPosInvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrintA4InvoicesTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrintA4InvoicesTable1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Kasa"));

        jCheckBoxFirstCashRegister.setText("1");

        jCheckBoxSecondCashRegister.setText("2");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxFirstCashRegister)
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxSecondCashRegister)
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxFirstCashRegister)
                    .addComponent(jCheckBoxSecondCashRegister))
                .addGap(40, 40, 40))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Filteri"));

        jTableFilterCategory.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFilterCategory.setViewportView(jTableFilterCategory);

        jButtonCategoriesAll.setText("Sve");
        jButtonCategoriesAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCategoriesAllActionPerformed(evt);
            }
        });

        jButtonCategoriesNone.setText("Ništa");
        jButtonCategoriesNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCategoriesNoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFilterCategory, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCategoriesAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCategoriesNone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButtonCategoriesAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCategoriesNone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 87, Short.MAX_VALUE))
                    .addComponent(jScrollPaneFilterCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Kategorije", jPanel5);

        jTableFilterStaff.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFilterStaff.setViewportView(jTableFilterStaff);

        jButtonStaffNone.setText("Ništa");
        jButtonStaffNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffNoneActionPerformed(evt);
            }
        });

        jButtonStaffAll.setText("Sve");
        jButtonStaffAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStaffAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFilterStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonStaffAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStaffNone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButtonStaffAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonStaffNone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPaneFilterStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Djelatnici", jPanel6);

        jTableFilterArticles.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFilterArticles.setViewportView(jTableFilterArticles);

        jTextFieldFilterArticles.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextFieldFilterArticles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilterArticlesKeyReleased(evt);
            }
        });

        jLabel15.setText(" Filter:");

        jButtonArticlesNone.setText("Ništa");
        jButtonArticlesNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonArticlesNoneActionPerformed(evt);
            }
        });

        jButtonArticlesAll.setText("Sve");
        jButtonArticlesAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonArticlesAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonArticlesAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonArticlesNone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel15)
                    .addComponent(jTextFieldFilterArticles, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jButtonArticlesAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonArticlesNone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFilterArticles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFilterArticles, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneFilterArticles, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Artikli", jPanel7);

        jTableFilterTradingGoods.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFilterTradingGoods.setViewportView(jTableFilterTradingGoods);

        jButtonTradingGoodsAll.setText("Sve");
        jButtonTradingGoodsAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTradingGoodsAllActionPerformed(evt);
            }
        });

        jButtonTradingGoodsNone.setText("Ništa");
        jButtonTradingGoodsNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTradingGoodsNoneActionPerformed(evt);
            }
        });

        jTextFieldFilterTradingGoods.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextFieldFilterTradingGoods.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilterTradingGoodsKeyReleased(evt);
            }
        });

        jLabel16.setText(" Filter:");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonTradingGoodsAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonTradingGoodsNone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextFieldFilterTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(0, 0, 0))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jButtonTradingGoodsAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonTradingGoodsNone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFilterTradingGoods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFilterTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneFilterTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Trgovačka roba", jPanel12);

        jTableFilterServices.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneFilterServices.setViewportView(jTableFilterServices);

        jButtonServicesAll.setText("Sve");
        jButtonServicesAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonServicesAllActionPerformed(evt);
            }
        });

        jButtonServicesNone.setText("Ništa");
        jButtonServicesNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonServicesNoneActionPerformed(evt);
            }
        });

        jLabel17.setText(" Filter:");

        jTextFieldFilterServices.setPreferredSize(new java.awt.Dimension(200, 25));
        jTextFieldFilterServices.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilterServicesKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonServicesAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonServicesNone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextFieldFilterServices, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(0, 0, 0))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jButtonServicesAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonServicesNone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFilterServices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFilterServices, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneFilterServices, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Usluge", jPanel13);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel14.setText("Broj računa u totalu:");

        jLabelInvoiceCount.setText("0");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelInvoiceCount)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabelInvoiceCount))
                .addGap(6, 6, 6))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Ukupno:");

        jLabel12.setText("Bez popusta:");

        jLabel13.setText("Popust:");

        jLabelTotalPrice.setText("0.00 eur");

        jLabelWithoutDiscount.setText("0.00 eur");

        jLabelDiscount.setText("0.00 eur");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelWithoutDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabelTotalPrice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabelWithoutDiscount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabelDiscount))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Pregled"));

        jTableTotalItems.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalItems.setViewportView(jTableTotalItems);

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalItems)
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalItems, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pregled po stavkama", jPanel23);

        jTableTotalStaff.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalStaff.setViewportView(jTableTotalStaff);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalStaff)
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pregled po djelatnicima", jPanel15);

        jTableTotalPayMethod.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalPayMethod.setViewportView(jTableTotalPayMethod);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalPayMethod)
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalPayMethod, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pregled po načinu plaćanja", jPanel16);

        jTableTotalDays.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalDays.setViewportView(jTableTotalDays);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalDays)
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalDays, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pregled po danima", jPanel17);

        jTableTotalMaterials.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalMaterials.setViewportView(jTableTotalMaterials);

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalMaterials)
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalMaterials, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Utrošak materijala", jPanel18);

        jTableTotalTradingGoods.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalTradingGoods.setViewportView(jTableTotalTradingGoods);

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalTradingGoods)
                .addContainerGap())
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalTradingGoods, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Utrošak trg. robe", jPanel22);

        jTableTotalItemsDetailed.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalItemsDetailed.setViewportView(jTableTotalItemsDetailed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalItemsDetailed)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalItemsDetailed, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pregled po stavkama - detalji", jPanel3);

        jTableTotalTaxes.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPaneTotalTaxes.setViewportView(jTableTotalTaxes);

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalTaxes)
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTotalTaxes, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Razrada poreza", jPanel24);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelInternetConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(92, 92, 92))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelButtons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelInternetConnection.setText("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        Utils.DisposeDialog(this);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jXDatePickerFromPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerFromPropertyChange
        //RefreshTable();
    }//GEN-LAST:event_jXDatePickerFromPropertyChange

    private void jXDatePickerToPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jXDatePickerToPropertyChange
        //RefreshTable();
    }//GEN-LAST:event_jXDatePickerToPropertyChange

    private void jButtonPrintA4InvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoicesTableActionPerformed
        String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
        String dateToString = jXDatePickerTo.getEditor().getText().trim();
        String timeFrom = OdSati.getSelectedItem().toString() + ":" + OdMinuta.getSelectedItem().toString() + ":00";
        String timeTo = DoSati.getSelectedItem().toString() + ":" + DoMinuta.getSelectedItem().toString() + ":59";
        RefreshTable();

        PrintTableExtraData printTableExtraData = new PrintTableExtraData();
        printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
        printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));
        

        printTableExtraData.footerList.add(new Pair<>("Ukupno:        ", jLabelTotalPrice.getText()));
        printTableExtraData.footerList.add(new Pair<>("", "Bez popusta: " + jLabelWithoutDiscount.getText()));
        printTableExtraData.footerList.add(new Pair<>("", "Popust:          " + jLabelDiscount.getText()));

        String[] totalTypeNames = new String[]{"Total", "Total+", "Total++"};
        String totalTitle = totalTypeNames[totalType];

        if (jTabbedPane1.getSelectedIndex() == 0) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoStavkama", totalTitle + " - pregled po stavkama", jTableTotalItems, new int[]{0, 2, 3, 4, 5, 6, 7}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 1) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoDjelatnicima", totalTitle + " - pregled po djelatnicima", jTableTotalStaff, new int[]{0, 1, 2, 3}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 2) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoNačinuPlaćanja", totalTitle + " - pregled po načinu plaćanja", jTableTotalPayMethod, new int[]{0, 1}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 3) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoDanima", totalTitle + " - pregled po danima", jTableTotalDays, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 4) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoUtroškuMaterijala", totalTitle + " - pregled po utrošku materijala", jTableTotalMaterials, new int[]{0, 1, 2, 3, 4}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 5) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoUtroškuTrgRobe", totalTitle + " - pregled po utrošku trg. robe", jTableTotalTradingGoods, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 6) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoStavkama-Detalji", totalTitle + " - pregled po stavkama - detalji", jTableTotalItemsDetailed, new int[]{0, 2, 3, 4, 5, 6, 7, 8}, new int[]{}, printTableExtraData, "");
        } else if (jTabbedPane1.getSelectedIndex() == 7) {
            PrintUtils.PrintA4Table(totalTitle + "-PregledPoRazradiPoreza", totalTitle + " - pregled po razradi poreza", jTableTotalTaxes, new int[]{0, 1, 2}, new int[]{}, printTableExtraData, "");
        }
        this.dispose();
    }//GEN-LAST:event_jButtonPrintA4InvoicesTableActionPerformed

    private void jButtonPrintPosInvoicesTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintPosInvoicesTableActionPerformed
        String dateFromString = jXDatePickerFrom.getEditor().getText().trim();
        String dateToString = jXDatePickerTo.getEditor().getText().trim();
        RefreshTable();

        PrintTableExtraData printTableExtraData = new PrintTableExtraData();
        printTableExtraData.headerList.add(new Pair<>("Od datuma: ", dateFromString));
        printTableExtraData.headerList.add(new Pair<>("Do datuma: ", dateToString));

        printTableExtraData.footerList.add(new Pair<>("Ukupno:      ", jLabelTotalPrice.getText()));
        printTableExtraData.footerList.add(new Pair<>("", "Bez popusta: " + jLabelWithoutDiscount.getText()));
        printTableExtraData.footerList.add(new Pair<>("", "Popust:      " + jLabelDiscount.getText()));

        String[] totalTypeNames = new String[]{"Total", "Total+", "Total++"};
        String totalTitle = totalTypeNames[totalType];

        if (jTabbedPane1.getSelectedIndex() == 0) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po stavkama", jTableTotalItems, new int[][]{new int[]{0, 2}, new int[]{3, 4, 5}, new int[]{6, 7}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 1) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po djelatnicima", jTableTotalStaff, new int[][]{new int[]{0, 1, 2, 3}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 2) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po načinu plaćanja", jTableTotalPayMethod, new int[][]{new int[]{0, 1}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 3) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po danima", jTableTotalDays, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8}, new int[]{9, 10, 11}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 4) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po utrošku materijala", jTableTotalMaterials, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 5) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po utrošku trg. robe", jTableTotalTradingGoods, new int[][]{new int[]{0, 1, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8}, new int[]{9, 10}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 6) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po stavkama - detalji", jTableTotalItemsDetailed, new int[][]{new int[]{0, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8}}, printTableExtraData);
        } else if (jTabbedPane1.getSelectedIndex() == 7) {
            PrintUtils.PrintPosTable(totalTitle + " - pregled po razradi poreza", jTableTotalItemsDetailed, new int[][]{new int[]{0, 1, 2}}, printTableExtraData);
        }
    }//GEN-LAST:event_jButtonPrintPosInvoicesTableActionPerformed

    private void jButtonPrintA4InvoicesTable1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintA4InvoicesTable1ActionPerformed
        RefreshTable();        
    }//GEN-LAST:event_jButtonPrintA4InvoicesTable1ActionPerformed

    private void jTextFieldFilterServicesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilterServicesKeyReleased
        String searchString = jTextFieldFilterServices.getText();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableFilterServices.getModel());
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
        jTableFilterServices.setRowSorter(sorter);
    }//GEN-LAST:event_jTextFieldFilterServicesKeyReleased

    private void jButtonServicesNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonServicesNoneActionPerformed
        for (int i = 0; i < jTableFilterServices.getModel().getRowCount(); ++i) {
            jTableFilterServices.getModel().setValueAt(false, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonServicesNoneActionPerformed

    private void jButtonServicesAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonServicesAllActionPerformed
        for (int i = 0; i < jTableFilterServices.getModel().getRowCount(); ++i) {
            jTableFilterServices.getModel().setValueAt(true, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonServicesAllActionPerformed

    private void jTextFieldFilterTradingGoodsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilterTradingGoodsKeyReleased
        String searchString = jTextFieldFilterTradingGoods.getText();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableFilterTradingGoods.getModel());
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
        jTableFilterTradingGoods.setRowSorter(sorter);
    }//GEN-LAST:event_jTextFieldFilterTradingGoodsKeyReleased

    private void jButtonTradingGoodsNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTradingGoodsNoneActionPerformed
        for (int i = 0; i < jTableFilterTradingGoods.getModel().getRowCount(); ++i) {
            jTableFilterTradingGoods.getModel().setValueAt(false, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonTradingGoodsNoneActionPerformed

    private void jButtonTradingGoodsAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTradingGoodsAllActionPerformed
        for (int i = 0; i < jTableFilterTradingGoods.getModel().getRowCount(); ++i) {
            jTableFilterTradingGoods.getModel().setValueAt(true, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonTradingGoodsAllActionPerformed

    private void jButtonArticlesAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonArticlesAllActionPerformed
        for (int i = 0; i < jTableFilterArticles.getModel().getRowCount(); ++i) {
            jTableFilterArticles.getModel().setValueAt(true, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonArticlesAllActionPerformed

    private void jButtonArticlesNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonArticlesNoneActionPerformed
        for (int i = 0; i < jTableFilterArticles.getModel().getRowCount(); ++i) {
            jTableFilterArticles.getModel().setValueAt(false, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonArticlesNoneActionPerformed

    private void jTextFieldFilterArticlesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilterArticlesKeyReleased
        String searchString = jTextFieldFilterArticles.getText();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTableFilterArticles.getModel());
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + searchString));
        jTableFilterArticles.setRowSorter(sorter);
    }//GEN-LAST:event_jTextFieldFilterArticlesKeyReleased

    private void jButtonStaffAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStaffAllActionPerformed
        for (int i = 0; i < jTableFilterStaff.getModel().getRowCount(); ++i) {
            jTableFilterStaff.getModel().setValueAt(true, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonStaffAllActionPerformed

    private void jButtonStaffNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStaffNoneActionPerformed
        for (int i = 0; i < jTableFilterStaff.getModel().getRowCount(); ++i) {
            jTableFilterStaff.getModel().setValueAt(false, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonStaffNoneActionPerformed

    private void jButtonCategoriesNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCategoriesNoneActionPerformed
        for (int i = 0; i < jTableFilterCategory.getModel().getRowCount(); ++i) {
            jTableFilterCategory.getModel().setValueAt(false, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonCategoriesNoneActionPerformed

    private void jButtonCategoriesAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCategoriesAllActionPerformed
        for (int i = 0; i < jTableFilterCategory.getModel().getRowCount(); ++i) {
            jTableFilterCategory.getModel().setValueAt(true, i, 0);
        }
        RefreshTable();
    }//GEN-LAST:event_jButtonCategoriesAllActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> DoMinuta;
    private javax.swing.JComboBox<String> DoSati;
    private javax.swing.JComboBox<String> OdMinuta;
    private javax.swing.JComboBox<String> OdSati;
    private javax.swing.JButton jButtonArticlesAll;
    private javax.swing.JButton jButtonArticlesNone;
    private javax.swing.JButton jButtonCategoriesAll;
    private javax.swing.JButton jButtonCategoriesNone;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonPrintA4InvoicesTable;
    private javax.swing.JButton jButtonPrintA4InvoicesTable1;
    private javax.swing.JButton jButtonPrintPosInvoicesTable;
    private javax.swing.JButton jButtonServicesAll;
    private javax.swing.JButton jButtonServicesNone;
    private javax.swing.JButton jButtonStaffAll;
    private javax.swing.JButton jButtonStaffNone;
    private javax.swing.JButton jButtonTradingGoodsAll;
    private javax.swing.JButton jButtonTradingGoodsNone;
    private javax.swing.JCheckBox jCheckBoxFirstCashRegister;
    private javax.swing.JCheckBox jCheckBoxSecondCashRegister;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDiscount;
    private javax.swing.JLabel jLabelInternetConnection;
    private javax.swing.JLabel jLabelInvoiceCount;
    private javax.swing.JLabel jLabelTotalPrice;
    private javax.swing.JLabel jLabelWithoutDiscount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAdinfoLogo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneFilterArticles;
    private javax.swing.JScrollPane jScrollPaneFilterCategory;
    private javax.swing.JScrollPane jScrollPaneFilterServices;
    private javax.swing.JScrollPane jScrollPaneFilterStaff;
    private javax.swing.JScrollPane jScrollPaneFilterTradingGoods;
    private javax.swing.JScrollPane jScrollPaneTotalDays;
    private javax.swing.JScrollPane jScrollPaneTotalItems;
    private javax.swing.JScrollPane jScrollPaneTotalItemsDetailed;
    private javax.swing.JScrollPane jScrollPaneTotalMaterials;
    private javax.swing.JScrollPane jScrollPaneTotalPayMethod;
    private javax.swing.JScrollPane jScrollPaneTotalStaff;
    private javax.swing.JScrollPane jScrollPaneTotalTaxes;
    private javax.swing.JScrollPane jScrollPaneTotalTradingGoods;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTableFilterArticles;
    private javax.swing.JTable jTableFilterCategory;
    private javax.swing.JTable jTableFilterServices;
    private javax.swing.JTable jTableFilterStaff;
    private javax.swing.JTable jTableFilterTradingGoods;
    private javax.swing.JTable jTableTotalDays;
    private javax.swing.JTable jTableTotalItems;
    private javax.swing.JTable jTableTotalItemsDetailed;
    private javax.swing.JTable jTableTotalMaterials;
    private javax.swing.JTable jTableTotalPayMethod;
    private javax.swing.JTable jTableTotalStaff;
    private javax.swing.JTable jTableTotalTaxes;
    private javax.swing.JTable jTableTotalTradingGoods;
    private javax.swing.JTextField jTextFieldFilterArticles;
    private javax.swing.JTextField jTextFieldFilterServices;
    private javax.swing.JTextField jTextFieldFilterTradingGoods;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerFrom;
    private org.jdesktop.swingx.JXDatePicker jXDatePickerTo;
    // End of variables declaration//GEN-END:variables
}
