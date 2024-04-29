
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
import hr.adinfo.client.datastructures.InvoiceItem;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.database.MultiDatabaseQuery;
import hr.adinfo.utils.database.MultiDatabaseQueryResponse;
import hr.adinfo.utils.extensions.CustomTableModel;
import hr.adinfo.utils.licence.Licence;
import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JDialog;

/**
 *
 * @author Matej
 */
public class LocalServerNotificationsService {
	
	private static final int SERVICE_LOOP_DELAY_SECONDS = 60;
	
	private static Date lastNotifDate;
		
	public static void Init(){
		lastNotifDate = new Date();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					CheckForNotification(Values.LOCAL_VALUE_NOTIFICATION_TIME);
					CheckForNotification(Values.LOCAL_VALUE_NOTIFICATION_TIME_2);
					
					try {
						Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckForNotification(String localValueString){
		String emailTarget = ClientAppUtils.GetLocalValue(Values.LOCAL_VALUE_CONTACT_EMAIL);
		if("".equals(emailTarget))
			return;
		
		Date notifDate = null;
		Date nowDate = new Date();
		String prefix = ClientAppUtils.GetLocalValue(localValueString);
		String sufix = new SimpleDateFormat(":00 dd.MM.yyyy.").format(nowDate);
		
		try {
			notifDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy.").parse(prefix + sufix);
		} catch(Exception ex){
			return;
		}
		
		if(nowDate.after(notifDate) && lastNotifDate.before(notifDate)){
			lastNotifDate = notifDate;
			String messageString = GetMessageString();
			if(!"".equals(messageString)){
				SendNotification(messageString);
			}
		}
	}
	
	private static void SendNotification(String messageString){
		String emailTarget = ClientAppUtils.GetLocalValue(Values.LOCAL_VALUE_CONTACT_EMAIL);
		
		final String username = "noreply.accable@gmail.com";
        final String password = "ugkoierlkuyajbmc"; //noreply123
        final String subject = "Dnevni izvještaj - " + Licence.GetOfficeAddress();
		
        Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.port", "465");
                
                

        Session session = Session.getInstance(props,
			new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
        });
        
         File pdfFolder = new File("PDF"); // Assuming "PDF" folder is located in the same directory as the Java application
         File latestPDF = getLatestPDF(pdfFolder);
         
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTarget));
            message.setSubject(subject);
            DataSource source = new FileDataSource(latestPDF);
            message.setDataHandler(new DataHandler(source));
            message.setFileName(latestPDF.getName());
            message.setText(messageString);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
	}
	
	private static String GetMessageString(){
		String toReturn = "Dnevni izvještaj - " + Licence.GetCompanyName() + " - " + Licence.GetOfficeTag()+ " - " + Licence.GetOfficeAddress() + System.lineSeparator();
		//toReturn += GetMessageStringMinAmounts() + System.lineSeparator();
		toReturn += GetMessageStringTotal(0) + System.lineSeparator();
		toReturn += GetMessageStringTotal(1) + System.lineSeparator();
		return toReturn;
	}
	
	private static String GetMessageStringMinAmounts(){
		boolean customIdEnabled = !ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_AUTO_ARTICLEID.ordinal());
		ClientAppUtils.CreateAllTradingGoodsAmountsIfNoExist(Licence.GetOfficeNumber());
		String toReturn = "";
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		String queryTradingGoods = "SELECT TRADING_GOODS.ID, TRADING_GOODS.NAME, TRADING_GOODS.MIN_AMOUNT, TRADING_GOODS_AMOUNTS.AMOUNT, "
				+ "TRADING_GOODS.CUSTOM_ID "
				+ "FROM TRADING_GOODS "
				+ "INNER JOIN TRADING_GOODS_AMOUNTS ON TRADING_GOODS.ID = TRADING_GOODS_AMOUNTS.TRADING_GOODS_ID "
				+ "WHERE TRADING_GOODS.IS_DELETED = 0 AND TRADING_GOODS_AMOUNTS.OFFICE_NUMBER = ? "
				+ "AND AMOUNT_YEAR = ? AND TRADING_GOODS_AMOUNTS.AMOUNT < TRADING_GOODS.MIN_AMOUNT";
		String queryMaterials = "SELECT MATERIALS.ID, MATERIALS.NAME, MATERIALS.MIN_AMOUNT, MATERIAL_AMOUNTS.AMOUNT "
				+ "FROM MATERIALS "
				+ "INNER JOIN MATERIAL_AMOUNTS ON MATERIALS.ID = MATERIAL_AMOUNTS.MATERIAL_ID "
				+ "WHERE MATERIALS.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? "
				+ "AND AMOUNT_YEAR = ? AND MATERIAL_AMOUNTS.AMOUNT < MATERIALS.MIN_AMOUNT";
		String queryArticles = "SELECT ARTICLES.ID, ARTICLES.NAME, ARTICLES.MIN_AMOUNT, MIN(MATERIAL_AMOUNTS.AMOUNT / NORMATIVES.AMOUNT), "
				+ "ARTICLES.CUSTOM_ID "
				+ "FROM ARTICLES "
				+ "INNER JOIN NORMATIVES ON ARTICLES.ID = NORMATIVES.ARTICLE_ID "
				+ "INNER JOIN MATERIAL_AMOUNTS ON NORMATIVES.MATERIAL_ID = MATERIAL_AMOUNTS.MATERIAL_ID "
				+ "WHERE ARTICLES.IS_DELETED = 0 AND MATERIAL_AMOUNTS.OFFICE_NUMBER = ? AND NORMATIVES.IS_DELETED = 0 "
				+ "AND AMOUNT_YEAR = ? AND MATERIAL_AMOUNTS.AMOUNT < ARTICLES.MIN_AMOUNT "
				+ "GROUP BY ARTICLES.ID, ARTICLES.NAME, ARTICLES.MIN_AMOUNT, ARTICLES.CUSTOM_ID";
		MultiDatabaseQuery multiDatabaseQuery = new MultiDatabaseQuery(3);
		multiDatabaseQuery.SetQuery(0, queryTradingGoods);
		multiDatabaseQuery.AddParam(0, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(0, 2, ClientAppSettings.currentYear);
		multiDatabaseQuery.SetQuery(1, queryMaterials);
		multiDatabaseQuery.AddParam(1, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(1, 2, ClientAppSettings.currentYear);
		multiDatabaseQuery.SetQuery(2, queryArticles);
		multiDatabaseQuery.AddParam(2, 1, Licence.GetOfficeNumber());
		multiDatabaseQuery.AddParam(2, 2, ClientAppSettings.currentYear);
		
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
					if(databaseQueryResult[0].getSize() != 0 || databaseQueryResult[1].getSize() != 0 || databaseQueryResult[2].getSize() != 0){
						toReturn += "Stavke čije količine u skladištu su ispod minimalne: " + System.lineSeparator();
					}
					
					if(databaseQueryResult[0].getSize() != 0){
						toReturn += "Trgovačka roba (Šifra, naziv, minimalna količina, trenutna količina) " + System.lineSeparator();
					}
					while (databaseQueryResult[0].next()) {
						String idString = customIdEnabled ? databaseQueryResult[0].getString(4) : databaseQueryResult[0].getString(0);
						String nameString = databaseQueryResult[0].getString(1);
						String minAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[0].getFloat(2));
						String currAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[0].getFloat(3));
						toReturn += idString + ", " + nameString + ", " + minAmountString + ", " + currAmountString + System.lineSeparator();
					}
					
					if(databaseQueryResult[1].getSize() != 0){
						toReturn += "Materijali (Šifra, naziv, minimalna količina, trenutna količina) " + System.lineSeparator();
					}
					while (databaseQueryResult[1].next()) {
						String idString = databaseQueryResult[1].getString(0);
						String nameString = databaseQueryResult[1].getString(1);
						String minAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[1].getFloat(2));
						String currAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[1].getFloat(3));
						toReturn += idString + ", " + nameString + ", " + minAmountString + ", " + currAmountString + System.lineSeparator();
					}
					
					if(databaseQueryResult[2].getSize() != 0){
						toReturn += "Artikli (Šifra, naziv, minimalna količina, trenutna količina) " + System.lineSeparator();
					}
					while (databaseQueryResult[2].next()) {
						String idString = customIdEnabled ? databaseQueryResult[2].getString(4) : databaseQueryResult[2].getString(0);
						String nameString = databaseQueryResult[2].getString(1);
						String minAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[2].getFloat(2));
						String currAmountString = ClientAppUtils.FloatToPriceString(databaseQueryResult[2].getFloat(3));
						toReturn += idString + ", " + nameString + ", " + minAmountString + ", " + currAmountString + System.lineSeparator();
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	private static class DailyTrafficData {
		public Date date;
		public float amountWithoutDiscount, discount;
		public float cash, maestro, mastercard, amex, diners, visa, transactionBill, other;
		
		public void SetDate(Date newDate){
			date = newDate;
		}
		
		public boolean equalsDate(Date dateToCheck){
			if(date == null)
				return false;
			
			String dateD = new SimpleDateFormat("dd").format(date);
			String dateM = new SimpleDateFormat("MM").format(date);
			String dateY = new SimpleDateFormat("yyyy").format(date);
			
			String dateToCheckD = new SimpleDateFormat("dd").format(dateToCheck);
			String dateToCheckM = new SimpleDateFormat("MM").format(dateToCheck);
			String dateToCheckY = new SimpleDateFormat("yyyy").format(dateToCheck);
			
			return dateToCheckD.equals(dateD) && dateToCheckM.equals(dateM) && dateToCheckY.equals(dateY);
		}
		
		public void AddAmountPaymentType(float amountWithoutDiscountToAdd, float discountToAdd, String paymentMethod){
			amountWithoutDiscount += amountWithoutDiscountToAdd;
			discount += discountToAdd;
			float amountToAdd = amountWithoutDiscountToAdd - discountToAdd;
			if ("Novčanice i/ili kovanice".equals(paymentMethod)){
				cash += amountToAdd;
			} else if ("Maestro".equals(paymentMethod)){
				maestro += amountToAdd;
			} else if ("Mastercard".equals(paymentMethod)){
				mastercard += amountToAdd;
			} else if ("American Express".equals(paymentMethod)){
				amex += amountToAdd;
			} else if ("Diners".equals(paymentMethod)){
				diners += amountToAdd;
			} else if ("Visa".equals(paymentMethod)){
				visa += amountToAdd;
			} else if ("Transakcijski račun".equals(paymentMethod)){
				transactionBill += amountToAdd;
			} else {
				other += amountToAdd;
			}
		}
	}
        
         private static File getLatestPDF(File folder) {
        File[] pdfFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf");
            }
        });

        if (pdfFiles != null && pdfFiles.length > 0) {
            Arrays.sort(pdfFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            return pdfFiles[0]; // The first element is the latest PDF file
        } else {
            return null; // No PDF files found in the folder
        }
    }
	
	private static class MaterialsConsumptionData {
		public int invoiceId;
		public int articleId;
		public int materialId;
		public float normative;
		public String materialName;
		public float lastPrice;
	}
	
	private static String GetMessageStringTotal(int totalType){
		String toReturn = "";
		if(totalType == 0){
			toReturn = "Total: " + System.lineSeparator();
		} else if(totalType == 1){
			toReturn = "Total+: " + System.lineSeparator();
		} else if(totalType == 2){
			toReturn = "Total + Total+: " + System.lineSeparator();
		}

		Date dateFrom = new Date();
		Date dateTo = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(dateFrom);
                c.add(Calendar.DATE, -1);
                dateFrom = c.getTime();
		
		String timeFrom =  "04:00:00"; // Za pingvin stavit => "04:00:00";  //00:00:00
		String timeTo = "03:59:59";// za pingvin stavit => "03:59:59";    //23:59:59

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
                + "PAY_NAME, PAY_TYPE, I_TIME, I_DATE, PAY_NAME_2, PAY_TYPE_2, PAY_AMT_2, I_NUM, SPEC_NUM "
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
		if(!isProduction){
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
		
		for (int i = 0; i < 6; ++i){
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
					ArrayList<Integer> invoicesNumList = new ArrayList<>();
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
                    ArrayList<Integer> localInvoicesNumList = new ArrayList<>();
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
					ArrayList<Float> paymentMethodsAmountList = new ArrayList<>();
					ArrayList<DailyTrafficData> dailyTrafficList = new ArrayList<>();
					
					ArrayList<MaterialsConsumptionData> localMaterialsConsumptionDataList = new ArrayList<>();
					ArrayList<MaterialsConsumptionData> materialsConsumptionDataList = new ArrayList<>();
					ArrayList<Integer> materialsIdList = new ArrayList<>();
					ArrayList<String> materialsNameList = new ArrayList<>();
					ArrayList<Float> materialsAmountList = new ArrayList<>();
					ArrayList<Float> materialsPurchasePriceList = new ArrayList<>();
					
					float totalInvoicePriceSum = 0f;
					float totalinvoiceDiscountSum = 0f;
					
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
						if(databaseQueryResult[2].getInt(14) != 0 && localInvoicesNumList.contains(databaseQueryResult[2].getInt(14)))
                            continue;
                        if(databaseQueryResult[2].getInt(15) != 0 && localInvoicesSpecNumList.contains(databaseQueryResult[2].getInt(15)))
                            continue;
						
						if(totalType == 0){
							if(databaseQueryResult[2].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
								continue;
							}
						} else if (totalType == 1){
							if(databaseQueryResult[2].getInt(8) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[2].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						} else if (totalType == 2){
							if(databaseQueryResult[2].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[2].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						}
						
						localInvoicesNumList.add(databaseQueryResult[2].getInt(14));
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
						if(staffListId == -1){
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
						if(payMethodListId == -1){
							paymentMethodsList.add(payMethod);
							paymentMethodsAmountList.add(0f);
						}
						
						int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
						if(payMethodListId2 == -1){
							paymentMethodsList.add(payMethod2);
							paymentMethodsAmountList.add(0f);
						}
						
						Date invoiceDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[2].getString(10) + " " + databaseQueryResult[2].getString(9));
						localInvoicesDateList.add(invoiceDate);
					}
					
					// Invoices
					while (databaseQueryResult[3].next()) {
	//					if(databaseQueryResult[3].getInt(14) != 0 && invoicesNumList.contains(databaseQueryResult[3].getInt(14)))
        //                    continue;
        //                if(databaseQueryResult[3].getInt(15) != 0 && invoicesSpecNumList.contains(databaseQueryResult[3].getInt(15)))
        //                    continue;
						
						if(totalType == 0){
							if(databaseQueryResult[3].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
								continue;
							}
						} else if (totalType == 1){
							if(databaseQueryResult[3].getInt(8) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[3].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						} else if (totalType == 2){
							if(databaseQueryResult[3].getInt(8) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[3].getString(7).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						}
						
						invoicesNumList.add(databaseQueryResult[3].getInt(14));
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
						if(staffListId == -1){
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
						if(payMethodListId == -1){
							paymentMethodsList.add(payMethod);
							paymentMethodsAmountList.add(0f);
						}
						
						int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
						if(payMethodListId2 == -1){
							paymentMethodsList.add(payMethod2);
							paymentMethodsAmountList.add(0f);
						}
						
						Date invoiceDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(databaseQueryResult[3].getString(10) + " " + databaseQueryResult[3].getString(9));
						invoicesDateList.add(invoiceDate);
					}
					
					// Local materials
					while (databaseQueryResult[4].next()) {
						if(!localInvoicesIdList.contains(databaseQueryResult[4].getInt(0)))
                            continue;
						
						MaterialsConsumptionData mcd = new MaterialsConsumptionData();
						mcd.invoiceId = databaseQueryResult[4].getInt(0);
						mcd.articleId = databaseQueryResult[4].getInt(1);
						mcd.materialId = databaseQueryResult[4].getInt(2);
						mcd.normative = databaseQueryResult[4].getFloat(3);
						mcd.materialName  = databaseQueryResult[4].getString(4);
						mcd.lastPrice  = databaseQueryResult[4].getFloat(5);
						localMaterialsConsumptionDataList.add(mcd);
					}
					
					// Materials
					while (databaseQueryResult[5].next()) {
						if(!invoicesIdList.contains(databaseQueryResult[5].getInt(0)))
                            continue;
						
						MaterialsConsumptionData mcd = new MaterialsConsumptionData();
						mcd.invoiceId = databaseQueryResult[5].getInt(0);
						mcd.articleId = databaseQueryResult[5].getInt(1);
						mcd.materialId = databaseQueryResult[5].getInt(2);
						mcd.normative = databaseQueryResult[5].getFloat(3);
						mcd.materialName  = databaseQueryResult[5].getString(4);
						mcd.lastPrice  = databaseQueryResult[5].getFloat(5);
						materialsConsumptionDataList.add(mcd);
					}
					
					// Local invoice items
					while (databaseQueryResult[0].next()) {
						if(!localInvoicesIdList.contains(databaseQueryResult[0].getInt(9)))
                            continue;
						
						if(totalType == 0){
							if(databaseQueryResult[0].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
								continue;
							}
						} else if (totalType == 1){
							if(databaseQueryResult[0].getInt(11) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[0].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						} else if (totalType == 2){
							if(databaseQueryResult[0].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[0].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
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
						
						int invoiceListId = ClientAppUtils.ArrayIndexOf(localInvoicesIdList, databaseQueryResult[0].getInt(9));
						int staffId = localInvoicesStaffIdList.get(invoiceListId);
						
						// Shifts filter (with dateTo plus one day shifts after midnight filter)
						Date invoiceDate = localInvoicesDateList.get(invoiceListId);
						/*boolean dateInRange = false;
						int invoiceHour = Integer.parseInt(new SimpleDateFormat("HH").format(invoiceDate));
						int invoiceMinute = Integer.parseInt(new SimpleDateFormat("mm").format(invoiceDate));*/
						boolean isPreviousDayInvoice = false;
						
						/*String invoiceD = new SimpleDateFormat("dd").format(invoiceDate);
						String invoiceM = new SimpleDateFormat("MM").format(invoiceDate);
						String invoiceY = new SimpleDateFormat("yyyy").format(invoiceDate);
						String dateFromD = new SimpleDateFormat("dd").format(dateFrom);
						String dateFromM = new SimpleDateFormat("MM").format(dateFrom);
						String dateFromY = new SimpleDateFormat("yyyy").format(dateFrom);
						String dateToD = new SimpleDateFormat("dd").format(dateTo);
						String dateToM = new SimpleDateFormat("MM").format(dateTo);
						String dateToY = new SimpleDateFormat("yyyy").format(dateTo);
						
						for (int i = 0; i < jTableFilterShifts.getRowCount(); ++i){
							boolean shiftsTableValue = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
							if (!shiftsTableValue)
								continue;
							
							String fromTime = (String) jTableFilterShifts.getModel().getValueAt(i, 2);
							String toTime = (String) jTableFilterShifts.getModel().getValueAt(i, 3);
							int fromHour = Integer.parseInt(fromTime.split(":")[0]);
							int fromMinute = Integer.parseInt(fromTime.split(":")[1]);
							int toHour = Integer.parseInt(toTime.split(":")[0]);
							int toMinute = Integer.parseInt(toTime.split(":")[1]);
							
							if (fromHour < toHour){
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									continue;
								}
								
								if((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute) 
										&& (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)){
									dateInRange = true;
									break;
								}
							} else if(fromHour == toHour && fromMinute <= toMinute){
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									continue;
								}
								
								if(invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute){
									dateInRange = true;
									break;
								}
							} else {
								if(invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute){
									isPreviousDayInvoice = true;
								}
								
								if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)){
									if(invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute){
										continue;
									}
								}
								
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									if(invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute){
										continue;
									}
								}
								
								if(!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute) 
										&& (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))){
									dateInRange = true;
									break;
								}
							}
						}
						if(!dateInRange){
							localInvoicesItemCountList.set(invoiceListId, localInvoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}*/
						
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
						
						if(localInvoicesPaymentMethodsTypeList2.get(invoiceListId) == -1){
							paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
						} else {
							String payMethod2 = localInvoicesPaymentMethodsList2.get(invoiceListId);
							int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
							float paymentMethodsRatio = localInvoicesTotalPriceList2.get(invoiceListId) / localInvoicesTotalPriceList.get(invoiceListId);
							float itemPriceWithDiscount = itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount;
							paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + (1f - paymentMethodsRatio) * itemPriceWithDiscount);
							paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + paymentMethodsRatio * itemPriceWithDiscount);
						}
						
						// Daily traffic info
						Date dailyTrafficDate = invoiceDate;
						if(isPreviousDayInvoice){
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(dailyTrafficDate);
							calendar.add(Calendar.DATE, -1);
							dailyTrafficDate = calendar.getTime();
						}
						int dailyTrafficListIndex = -1;
						for(int i = 0; i < dailyTrafficList.size(); ++i){
							if(dailyTrafficList.get(i).equalsDate(dailyTrafficDate)){
								dailyTrafficListIndex = i;
								break;
							}
						}
						if(dailyTrafficListIndex == -1){
							DailyTrafficData dailyTrafficData = new DailyTrafficData();
							dailyTrafficData.SetDate(dailyTrafficDate);
							dailyTrafficList.add(dailyTrafficData);
							dailyTrafficListIndex = dailyTrafficList.size() - 1;
						}
						
						DailyTrafficData dailyTrafficData = dailyTrafficList.get(dailyTrafficListIndex);
						if(localInvoicesPaymentMethodsTypeList2.get(invoiceListId) == -1){
							dailyTrafficData.AddAmountPaymentType(itemPriceWithoutDiscount, itemDiscount + itemInvoiceDiscount, payMethod);
						} else {
							String payMethod2 = localInvoicesPaymentMethodsList2.get(invoiceListId);
							float paymentMethodsRatio = localInvoicesTotalPriceList2.get(invoiceListId) / localInvoicesTotalPriceList.get(invoiceListId);
							dailyTrafficData.AddAmountPaymentType((1f - paymentMethodsRatio) * itemPriceWithoutDiscount, (1f - paymentMethodsRatio) * (itemDiscount + itemInvoiceDiscount), payMethod);
							dailyTrafficData.AddAmountPaymentType(paymentMethodsRatio * itemPriceWithoutDiscount, paymentMethodsRatio * (itemDiscount + itemInvoiceDiscount), payMethod2);
						}
						dailyTrafficList.set(dailyTrafficListIndex, dailyTrafficData);
						
						// Material consumption data
						if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							for (int i = 0; i < localMaterialsConsumptionDataList.size(); ++i){
								MaterialsConsumptionData mcd = localMaterialsConsumptionDataList.get(i);
								if (mcd.invoiceId == localInvoicesIdList.get(invoiceListId) && mcd.articleId == itemId){
									boolean materialFound = false;
									for (int j = 0; j < materialsIdList.size(); ++j){
										if(mcd.materialId == materialsIdList.get(j)){
											materialFound = true;
											materialsAmountList.set(j, materialsAmountList.get(j) + mcd.normative * itemAmount);
										}
									}
									if(!materialFound){
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
						for (int i = 0; i < items.size(); ++i){
							if(items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
									&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
									&& items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax
									&& items.get(i).packagingRefund == packagingRefund
									&& items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)) {
								itemFound = true;
								items.get(i).itemAmount += itemAmount;
							}
						}
						if(!itemFound){
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
						if(!invoicesIdList.contains(databaseQueryResult[1].getInt(9)))
                            continue;
						
						if(totalType == 0){
							if(databaseQueryResult[1].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
								continue;
							}
						} else if (totalType == 1){
							if(databaseQueryResult[1].getInt(11) != Values.PAYMENT_METHOD_TYPE_SUBTOTAL || databaseQueryResult[1].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
								continue;
							}
						} else if (totalType == 2){
							if(databaseQueryResult[1].getInt(11) == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && databaseQueryResult[1].getString(10).contains(Values.PAYMENT_METHOD_SUBTOTAL_PAID_NAME)){
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

						int invoiceListId = ClientAppUtils.ArrayIndexOf(invoicesIdList, databaseQueryResult[1].getInt(9));
						int staffId = invoicesStaffIdList.get(invoiceListId);
						
						// Shifts filter (with dateTo plus one day shifts after midnight filter)
						Date invoiceDate = invoicesDateList.get(invoiceListId);
						/*boolean dateInRange = false;
						int invoiceHour = Integer.parseInt(new SimpleDateFormat("HH").format(invoiceDate));
						int invoiceMinute = Integer.parseInt(new SimpleDateFormat("mm").format(invoiceDate));*/
						boolean isPreviousDayInvoice = false;
						
						/*String invoiceD = new SimpleDateFormat("dd").format(invoiceDate);
						String invoiceM = new SimpleDateFormat("MM").format(invoiceDate);
						String invoiceY = new SimpleDateFormat("yyyy").format(invoiceDate);
						String dateFromD = new SimpleDateFormat("dd").format(dateFrom);
						String dateFromM = new SimpleDateFormat("MM").format(dateFrom);
						String dateFromY = new SimpleDateFormat("yyyy").format(dateFrom);
						String dateToD = new SimpleDateFormat("dd").format(dateTo);
						String dateToM = new SimpleDateFormat("MM").format(dateTo);
						String dateToY = new SimpleDateFormat("yyyy").format(dateTo);
						
						for (int i = 0; i < jTableFilterShifts.getRowCount(); ++i){
							boolean shiftsTableValue = (Boolean) jTableFilterShifts.getModel().getValueAt(i, 0);
							if (!shiftsTableValue)
								continue;
							
							String fromTime = (String) jTableFilterShifts.getModel().getValueAt(i, 2);
							String toTime = (String) jTableFilterShifts.getModel().getValueAt(i, 3);
							int fromHour = Integer.parseInt(fromTime.split(":")[0]);
							int fromMinute = Integer.parseInt(fromTime.split(":")[1]);
							int toHour = Integer.parseInt(toTime.split(":")[0]);
							int toMinute = Integer.parseInt(toTime.split(":")[1]);
							
							if (fromHour < toHour){
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									continue;
								}
								
								if((invoiceHour > fromHour || invoiceHour == fromHour && invoiceMinute >= fromMinute) 
										&& (invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute)){
									dateInRange = true;
									break;
								}
							} else if(fromHour == toHour && fromMinute <= toMinute){
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									continue;
								}
								
								if(invoiceHour == fromHour && invoiceMinute >= fromMinute && invoiceMinute <= toMinute){
									dateInRange = true;
									break;
								}
							} else {
								if(invoiceHour < toHour || invoiceHour == toHour && invoiceMinute <= toMinute){
									isPreviousDayInvoice = true;
								}
								
								if (invoiceD.equals(dateFromD) && invoiceM.equals(dateFromM) && invoiceY.equals(dateFromY)){
									if(invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute < fromMinute){
										continue;
									}
								}
								
								if (invoiceD.equals(dateToD) && invoiceM.equals(dateToM) && invoiceY.equals(dateToY)){
									if(invoiceHour > toHour || invoiceHour == toHour && invoiceMinute > toMinute){
										continue;
									}
								}
								
								if(!((invoiceHour > toHour || invoiceHour == toHour && invoiceMinute >= toMinute) 
										&& (invoiceHour < fromHour || invoiceHour == fromHour && invoiceMinute <= fromMinute))){
									dateInRange = true;
									break;
								}
							}
						}
						if(!dateInRange){
							invoicesItemCountList.set(invoiceListId, invoicesItemCountList.get(invoiceListId) - 1);
							continue;
						}*/
						
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
						
						if(invoicesPaymentMethodsTypeList2.get(invoiceListId) == -1){
							paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount);
						} else {
							String payMethod2 = invoicesPaymentMethodsList2.get(invoiceListId);
							int payMethodListId2 = ClientAppUtils.ArrayIndexOf(paymentMethodsList, payMethod2);
							float paymentMethodsRatio = invoicesTotalPriceList2.get(invoiceListId) / invoicesTotalPriceList.get(invoiceListId);
							float itemPriceWithDiscount = itemPriceWithoutDiscount - itemDiscount - itemInvoiceDiscount;
							paymentMethodsAmountList.set(payMethodListId, paymentMethodsAmountList.get(payMethodListId) + (1f - paymentMethodsRatio) * itemPriceWithDiscount);
							paymentMethodsAmountList.set(payMethodListId2, paymentMethodsAmountList.get(payMethodListId2) + paymentMethodsRatio * itemPriceWithDiscount);
						}
						
						// Daily traffic info
						Date dailyTrafficDate = invoiceDate;
						if(isPreviousDayInvoice){
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(dailyTrafficDate);
							calendar.add(Calendar.DATE, -1);
							dailyTrafficDate = calendar.getTime();
						}
						int dailyTrafficListIndex = -1;
						for(int i = 0; i < dailyTrafficList.size(); ++i){
							if(dailyTrafficList.get(i).equalsDate(dailyTrafficDate)){
								dailyTrafficListIndex = i;
								break;
							}
						}
						if(dailyTrafficListIndex == -1){
							DailyTrafficData dailyTrafficData = new DailyTrafficData();
							dailyTrafficData.SetDate(dailyTrafficDate);
							dailyTrafficList.add(dailyTrafficData);
							dailyTrafficListIndex = dailyTrafficList.size() - 1;
						}
						
						DailyTrafficData dailyTrafficData = dailyTrafficList.get(dailyTrafficListIndex);
						if(invoicesPaymentMethodsTypeList2.get(invoiceListId) == -1){
							dailyTrafficData.AddAmountPaymentType(itemPriceWithoutDiscount, itemDiscount + itemInvoiceDiscount, payMethod);
						} else {
							String payMethod2 = invoicesPaymentMethodsList2.get(invoiceListId);
							float paymentMethodsRatio = invoicesTotalPriceList2.get(invoiceListId) / invoicesTotalPriceList.get(invoiceListId);
							dailyTrafficData.AddAmountPaymentType((1f - paymentMethodsRatio) * itemPriceWithoutDiscount, (1f - paymentMethodsRatio) * (itemDiscount + itemInvoiceDiscount), payMethod);
							dailyTrafficData.AddAmountPaymentType(paymentMethodsRatio * itemPriceWithoutDiscount, paymentMethodsRatio * (itemDiscount + itemInvoiceDiscount), payMethod2);
						}
						dailyTrafficList.set(dailyTrafficListIndex, dailyTrafficData);
						
						// Material consumption data
						if (itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							for (int i = 0; i < materialsConsumptionDataList.size(); ++i){
								MaterialsConsumptionData mcd = materialsConsumptionDataList.get(i);
								if (mcd.invoiceId == invoicesIdList.get(invoiceListId) && mcd.articleId == itemId){
									boolean materialFound = false;
									for (int j = 0; j < materialsIdList.size(); ++j){
										if(mcd.materialId == materialsIdList.get(j)){
											materialFound = true;
											materialsAmountList.set(j, materialsAmountList.get(j) + mcd.normative * itemAmount);
										}
									}
									if(!materialFound){
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
						for (int i = 0; i < items.size(); ++i){
							if(items.get(i).itemId == itemId && items.get(i).itemType == itemType && items.get(i).itemPrice == itemPrice
									&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
									&& items.get(i).taxRate == itemTax && items.get(i).consumptionTaxRate == itemConsTax 
									&& items.get(i).packagingRefund == packagingRefund
									&& items.get(i).itemName.equals(itemName) && items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)) {
								itemFound = true;
								items.get(i).itemAmount += itemAmount;
							}
						}
						if(!itemFound){
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
					for (int i = 0; i < localInvoicesItemCountList.size(); ++i){
						if(localInvoicesItemCountList.get(i) > 0){
							++invoiceCount;
						}
					}
					for (int i = 0; i < invoicesItemCountList.size(); ++i){
						if(invoicesItemCountList.get(i) > 0){
							++invoiceCount;
						}
					}
					
					if(invoiceCount == 0){
						return "";
					}
					
					toReturn += "Broj izdanih računa: " + Integer.toString(invoiceCount) + System.lineSeparator();
					toReturn += "Ukupno: " + ClientAppUtils.FloatToPriceString(totalInvoicePriceSum - totalinvoiceDiscountSum) + System.lineSeparator();
					toReturn += "Popust: " + ClientAppUtils.FloatToPriceString(totalinvoiceDiscountSum) + System.lineSeparator();
					toReturn += "Bez popusta: " + ClientAppUtils.FloatToPriceString(totalInvoicePriceSum) + System.lineSeparator();
					
					// Table items
					/*ArrayList<InvoiceItem> itemsMerged = new ArrayList<>();
					for (int i = 0; i < items.size(); ++i){
						boolean itemFound = false;
						float itemDisAmt = 0f;
						if(items.get(i).discountPercentage != 0f){
							itemDisAmt += items.get(i).discountPercentage * items.get(i).itemAmount * items.get(i).itemPrice / 100f;
						} else if(items.get(i).discountValue != 0f){
							itemDisAmt += items.get(i).discountValue * items.get(i).itemAmount;
						}
						if(items.get(i).invoiceDiscountTotal != 0f){
 							itemDisAmt += items.get(i).invoiceDiscountTotal * items.get(i).itemAmount;
						}
						
						for (int j = 0; j < itemsMerged.size(); ++j){
							if(items.get(i).itemId == itemsMerged.get(j).itemId && items.get(i).itemType == itemsMerged.get(j).itemType && items.get(i).itemPrice == itemsMerged.get(j).itemPrice
									//&& items.get(i).discountPercentage == itemDisPct && items.get(i).discountValue == itemDisAmt
									&& items.get(i).taxRate == itemsMerged.get(j).taxRate && items.get(i).consumptionTaxRate == itemsMerged.get(j).consumptionTaxRate 
									&& items.get(i).packagingRefund == itemsMerged.get(j).packagingRefund 
									&& items.get(i).itemName.equals(itemsMerged.get(j).itemName)
									//&& items.get(i).invoiceDiscountTotal == (itemAmount != 0f ? itemInvoiceDiscount / itemAmount : 0f)
									) {
								itemFound = true;
								itemsMerged.get(j).itemAmount += items.get(i).itemAmount;
								itemsMerged.get(j).discountValue += ClientAppUtils.FloatToPriceFloat(itemDisAmt);
								break;
							}
						}
						if(!itemFound){
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
					
					CustomTableModel customTableModelItems = new CustomTableModel();
					customTableModelItems.setColumnIdentifiers(new String[] {"Šifra", "Tip stavke", "Naziv", "Količina", "Mj. jed.", "Cijena", "Popust", "Ukupno"});
					for (int i = 0; i < itemsMerged.size(); ++i){
						if(itemsMerged.get(i).itemAmount == 0f)
							continue;
						
						String itemType = "Artikl";
						if(itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
							itemType = "Usluga";
						} else if(itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS){
							itemType = "Trg. roba";
						}
						
						String discount = "";
						float totalPrice = itemsMerged.get(i).itemAmount * itemsMerged.get(i).itemPrice;
						if(itemsMerged.get(i).discountValue != 0f){
							discount = ClientAppUtils.FloatToPriceFloat(itemsMerged.get(i).discountValue) + " kn";
							totalPrice = totalPrice - itemsMerged.get(i).discountValue;
						}
						
						String measuringUnit = Values.TRADING_GOODS_MEASURING_UNIT;
						if(itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							int listId = ClientAppUtils.ArrayIndexOf(articlesIdList, itemsMerged.get(i).itemId);
							measuringUnit = articlesMeasuringUnitList.get(listId);
						} else if(itemsMerged.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
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
					}
					
					jTableTotalItems.setModel(customTableModelItems);				
					jTableTotalItems.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItems.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItems.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 30 / 100);
					jTableTotalItems.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItems.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItems.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItems.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
					jTableTotalItems.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);*/
					
					// Table staff
					toReturn += "Pregled po djelatnicima:" + System.lineSeparator();
					toReturn += "Djelatnik - Ukupno bez popusta - Ukupno popusta -> Ukupno" + System.lineSeparator();
					for (int i = 0; i < staffIdList.size(); ++i){
						if(staffAmountSumList.get(i) == 0f)
							continue;
						
						String s1 = staffIdList.get(i) + "-" + staffNameList.get(i);
						String s2 = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i) + staffAmountSumList.get(i));
						String s3 = ClientAppUtils.FloatToPriceString(staffDiscountAmountSumList.get(i));
						String s4 = ClientAppUtils.FloatToPriceString(staffAmountSumList.get(i));
						toReturn += s1 + " - " + s2 + " - " + s3 + " -> " + s4 + System.lineSeparator();
					}
					
					// Table payment methods
					if(totalType != 1){
						toReturn += "Pregled po načinu plaćanja:" + System.lineSeparator();
						toReturn += "Način plaćanja - Ukupno" + System.lineSeparator();
						CustomTableModel customTableModelPayMethods = new CustomTableModel();
						customTableModelPayMethods.setColumnIdentifiers(new String[] {"Način plaćanja", "Ukupno"});
						for (int i = 0; i < paymentMethodsList.size(); ++i){
							if(paymentMethodsAmountList.get(i) == 0f)
								continue;

							String s1 = paymentMethodsList.get(i);
							String s2 = ClientAppUtils.FloatToPriceString(paymentMethodsAmountList.get(i));
							toReturn += s1 + " - " + s2 + System.lineSeparator();
						}
					}
					
					// Table daily traffic
					/*CustomTableModel customTableModelDailyTraffic = new CustomTableModel();
					customTableModelDailyTraffic.setColumnIdentifiers(new String[] {
						"Datum", "Ukupno bez popusta", "Popust", "Ukupno", "Gotovina", "Maestro", "Mastercard", "Amex", "Diners", "Visa", "Virman", "Ostalo"
					});
					for (int i = 0; i < dailyTrafficList.size(); ++i){
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
					jTableTotalDays.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 20/ 100);
					jTableTotalDays.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10/ 100);
					jTableTotalDays.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10/ 100);
					jTableTotalDays.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10/ 100);
					jTableTotalDays.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
					jTableTotalDays.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
					jTableTotalDays.getColumnModel().getColumn(9).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);
					jTableTotalDays.getColumnModel().getColumn(10).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10/ 100);
					jTableTotalDays.getColumnModel().getColumn(11).setPreferredWidth(jScrollPaneTotalDays.getWidth() * 10 / 100);*/
					
					// Table materials consumption
					/*CustomTableModel customTableModelMaterials = new CustomTableModel();
					customTableModelMaterials.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Količina", "Nabavna cijena", "Uk. nab. cijena"});
					for (int i = 0; i < materialsIdList.size(); ++i){
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
					jTableTotalMaterials.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalMaterials.getWidth() * 15 / 100);*/
					
					// Table trading goods consumption
					/*CustomTableModel customTableModelTradingGoods = new CustomTableModel();
					customTableModelTradingGoods.setColumnIdentifiers(new String[] {"Šifra", "Naziv", "Kol.", "Nabavna cijena", "Prodajna cijena", "Uk. nab. cijena", "Uk. prod. cijena", "Pov. naknada", "Stopa", "Porez", "Razlika"});
					for (int i = 0; i < items.size(); ++i){
						if(items.get(i).itemAmount == 0f || items.get(i).itemType != Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS)
							continue;
						
						float totalPrice = items.get(i).itemAmount * items.get(i).itemPrice;
						if(items.get(i).discountPercentage != 0f){
							totalPrice = totalPrice * (100f - items.get(i).discountPercentage) / 100f;
						} else if(items.get(i).discountValue != 0f){
							totalPrice = totalPrice - items.get(i).discountValue * items.get(i).itemAmount;
						}
						if(items.get(i).invoiceDiscountTotal != 0f){
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
					jTableTotalTradingGoods.getColumnModel().getColumn(10).setPreferredWidth(jScrollPaneTotalTradingGoods.getWidth() * 10 / 100);*/
					
					// Table items - detailed
					/*CustomTableModel customTableModelItemsDetailed = new CustomTableModel();
					customTableModelItemsDetailed.setColumnIdentifiers(new String[] {"Šifra", "Tip stavke", "Naziv", "Količina", "Mj. jed.", "Cijena", "Popust", "Popust na račun", "Ukupno"});
					for (int i = 0; i < items.size(); ++i){
						if(items.get(i).itemAmount == 0f)
							continue;
						
						String itemType = "Artikl";
						if(items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
							itemType = "Usluga";
						} else if(items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS){
							itemType = "Trg. roba";
						}
						
						String discount = "";
						float totalPrice = items.get(i).itemAmount * items.get(i).itemPrice;
						if(items.get(i).discountPercentage != 0f){
							discount = items.get(i).discountPercentage + "%";
							totalPrice = totalPrice * (100f - items.get(i).discountPercentage) / 100f;
						} else if(items.get(i).discountValue != 0f){
							discount = ClientAppUtils.FloatToPriceFloat(items.get(i).discountValue) + " kn/kom";
							totalPrice = totalPrice - items.get(i).discountValue * items.get(i).itemAmount;
						}
						String invoiceDiscount = "";
						if(items.get(i).invoiceDiscountTotal != 0f){
							invoiceDiscount = ClientAppUtils.FloatToPriceString(items.get(i).invoiceDiscountTotal) + " kn/kom";
 							totalPrice -= items.get(i).invoiceDiscountTotal * items.get(i).itemAmount;
						}
						
						String measuringUnit = Values.TRADING_GOODS_MEASURING_UNIT;
						if(items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE){
							int listId = ClientAppUtils.ArrayIndexOf(articlesIdList, items.get(i).itemId);
							measuringUnit = articlesMeasuringUnitList.get(listId);
						} else if(items.get(i).itemType == Values.SETTINGS_LAYOUT_ITEM_TYPE_SERVICE){
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
					}
					
					jTableTotalItemsDetailed.setModel(customTableModelItemsDetailed);				
					jTableTotalItemsDetailed.getColumnModel().getColumn(0).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(1).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(2).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 30 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(4).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(5).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(6).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(7).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 15 / 100);
					jTableTotalItemsDetailed.getColumnModel().getColumn(8).setPreferredWidth(jScrollPaneTotalItemsDetailed.getWidth() * 10 / 100);*/
				}
			} catch (InterruptedException | ExecutionException | ParseException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
}
