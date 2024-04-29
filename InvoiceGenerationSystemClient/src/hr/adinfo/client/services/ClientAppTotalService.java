/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.LocalServer;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.client.ui.reports.ClientAppReportsTotalDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryInsertLocalInvoice;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author dell_
 */
public class ClientAppTotalService {
    
    	private static final int SERVICE_LOOP_DELAY_SECONDS = 10;
        private String Total = "";
        private String Message = "";
		
	public void Init(){		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					if(LocalServer.GetInstance() != null && LocalServer.GetInstance().isSyncedWithMaster){
						RefreshTable();
                                                LogToFile(Message, "logDebug");
					}
                                        
					try {
						
                                            Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
        
        private synchronized void LogToFile(String message, String folderName){
		String timestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
		Message = System.lineSeparator() + System.lineSeparator() + timestamp + System.lineSeparator() + message + Total;
		
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		String filePathString = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + ".txt";
		Path path = Paths.get(filePathString);
		
		try {
			Files.createDirectories(path.getParent());
			if(Files.notExists(path)){
				Files.createFile(path);
			}
			Files.write(path, (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {}
		
		File file = new File(filePathString);
		if(file.length() > /*1048576*/ 248576){
			int sufixCounter = 0;
			String filePathStringNew = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + "-0" + ".txt";
			while (new File(filePathStringNew).exists()){
				++sufixCounter;
				filePathStringNew = Paths.get("").toAbsolutePath() + File.separator + folderName + File.separator + year + File.separator + month + File.separator + day + "-" + sufixCounter + ".txt";
			}
			
			try {
				Files.copy(path, Paths.get(filePathStringNew));
				file.delete();
			} catch (Exception ex){
				ex.printStackTrace();
			}

		}
	}
	
	public static int LinesCount(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}
	
    private void RefreshTable() {

        String timeFrom = "00:00:00";
        String timeTo = "23:59:59";
        Date dateFrom;
        Date dateTo;
        Date dateToOriginal;
        String dateFromString = "04.01.2024";
        String dateToString = "04.01.2024";

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
                + "PAY_NAME, PAY_TYPE, I_TIME, I_DATE, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, I_NUM, SPEC_NUM, CR_NUM, IZNOS_NAPOJNICE "
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
                    ArrayList<Float> staffTipsSumList = new ArrayList<>();

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
                    ArrayList<Integer> materialsIdList = new ArrayList<>();
                    ArrayList<String> materialsNameList = new ArrayList<>();
                    ArrayList<Float> materialsAmountList = new ArrayList<>();
                    ArrayList<Float> materialsPurchasePriceList = new ArrayList<>();

                    double totalInvoicePriceSum = 0d;
                    double totalinvoiceDiscountSum = 0d;
                                        
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

                        if (invoiceDate.after(dateFrom) && invoiceDate.before(dateTo)) {
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

                        if (invoiceDate.after(dateFrom) && invoiceDate.before(dateTo)) {
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

                    }

                    // Materials
                    while (databaseQueryResult[5].next()) {
                        if (!invoicesIdList.contains(databaseQueryResult[5].getInt(0))) {
                            continue;
                        }
                    }

                    // Local invoice items
                    while (databaseQueryResult[0].next()) {
                        if (!localInvoicesIdList.contains(databaseQueryResult[0].getInt(9))) {
                            continue;
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

                        // Shifts filter (with dateTo plus one day shifts after midnight filter)
                        boolean dateInRange = false;
                        String dateFromD = new SimpleDateFormat("dd").format(dateFrom);
                        String dateFromM = new SimpleDateFormat("MM").format(dateFrom);
                        String dateFromY = new SimpleDateFormat("yyyy").format(dateFrom);
                        String dateToD = new SimpleDateFormat("dd").format(dateTo);
                        String dateToM = new SimpleDateFormat("MM").format(dateTo);
                        String dateToY = new SimpleDateFormat("yyyy").format(dateTo);
                        // Item data
                        float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
                        float itemPriceWithoutDiscount = itemPrice * itemAmount;
                        totalinvoiceDiscountSum += itemDiscount;
                        totalInvoicePriceSum += itemPriceWithoutDiscount;
    

                        // Add item
                        boolean itemFound = false;
                        for (int i = 0; i < items.size(); ++i) {
                            if (items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
                                    && items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
                                    && items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax
                                    && items.get(i).packagingRefund == packagingRefund
                                    && items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemAmount / itemAmount : 0f)) {
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
                            items.add(invoiceItem);
                        }
                    }

                    // Invoice items
                    while (databaseQueryResult[1].next()) {
                        if (!invoicesIdList.contains(databaseQueryResult[1].getInt(9))) {
                            continue;
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
                        int invoiceListId = ClientAppUtils.ArrayIndexOf(invoicesIdList, databaseQueryResult[1].getInt(9));

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
                        //if (!dateInRange) {
                            invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
                            //continue;
                        //}


                        // Item data
                        float itemDiscount = itemDisAmt * itemAmount + itemDisPct * itemPrice * itemAmount / 100f;
                        float itemPriceWithoutDiscount = itemPrice * itemAmount;
                        float itemToInvoicePriceRatio = (itemPriceWithoutDiscount - itemDiscount) / (invoicesTotalPriceList.get(invoiceListId) != 0f ? invoicesTotalPriceList.get(invoiceListId) : 1f);
                        float itemInvoiceDiscount = itemToInvoicePriceRatio * invoicesDiscountAmountList.get(invoiceListId);
                        totalinvoiceDiscountSum += itemDiscount + itemInvoiceDiscount;
                        totalInvoicePriceSum += itemPriceWithoutDiscount;

                        // Payment method info
                        String payMethod = invoicesPaymentMethodsList.get(invoiceListId);
                        int payMethodListId = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod);

 
 
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
                    Total = (ClientAppUtils.DoubleToPriceString(totalInvoicePriceSum - totalinvoiceDiscountSum));

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
                            discount = ClientAppUtils.FloatToPriceFloat(itemsMerged.get(i).discountValue) + " kn";
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

                    // Table staff
                    CustomTableModel customTableModelStaff = new CustomTableModel();
                    customTableModelStaff.setColumnIdentifiers(new String[]{"Djelatnik", "Ukupno bez popusta", "Ukupno popusta", "Ukupno napojnica", "Ukupno"});
                    for (int i = 0; i < staffIdList.size(); ++i) {
                        Object[] rowData = new Object[4];
                        rowData[0] = staffIdList.get(i) + "-" + staffNameList.get(i);
                        rowData[1] = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i) + staffAmountSumList.get(i));
                        rowData[2] = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i));
                        rowData[3] = ClientAppUtils.FloatToPriceString(staffAmountSumList.get(i));
                        customTableModelStaff.addRow(rowData);
                    }

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

                    // Table daily traffic
                    CustomTableModel customTableModelDailyTraffic = new CustomTableModel();
                    customTableModelDailyTraffic.setColumnIdentifiers(new String[]{
                        "Datum", "Ukupno bez popusta", "Popust", "Ukupno", "Gotovina", "Maestro", "Mastercard", "Amex", "Diners", "Visa", "Virman", "Ostalo"
                    });


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
                }
            } catch (InterruptedException | ExecutionException | ParseException ex) {
                ClientAppLogger.GetInstance().ShowErrorLog(ex);
            }
        }
    }}
