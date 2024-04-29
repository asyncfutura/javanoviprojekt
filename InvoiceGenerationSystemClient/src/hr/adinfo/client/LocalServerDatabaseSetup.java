/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.utils.Values;
import hr.adinfo.utils.licence.Licence;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Matej
 */
public class LocalServerDatabaseSetup {
	private static Connection connection;
	
	public static void SetupDatabase(Connection connection) throws IOException, SQLException {
		LocalServerDatabaseSetup.connection = connection;
		
		// TODO remove - used for testing
		//CreateTableIfNoExist("123123", "Drop Table TRANSFER_ARTICLES");
		//CreateTableIfNoExist("123123", "alter Table LOCAL_INVOICES ADD COLUMN S_ID INT DEFAULT 0");
		//CreateTableIfNoExist("123123", "alter Table LOCAL_INVOICES ALTER COLUMN S_ID DROP DEFAULT");
		//CreateTableIfNoExist("123123", "alter Table LOCAL_INVOICES DROP COLUMN S_NAME");
		
		// Misc tables
		CreateTableIfNoExist("DIFF_TABLE", "CREATE TABLE DIFF_TABLE(ID INT, DIFF VARCHAR (32672) FOR BIT DATA, PRIMARY KEY (ID))");
		UpdateDiffTableType();
		CreateTableIfNoExist("LOCAL_VALUES_TABLE", "CREATE TABLE LOCAL_VALUES_TABLE(NAME VARCHAR(64), VALUE VARCHAR(64), PRIMARY KEY (NAME))");
		CreateTableIfNoExist("GLOBAL_VALUES_TABLE", "CREATE TABLE GLOBAL_VALUES_TABLE(NAME VARCHAR(64), VALUE VARCHAR(64), PRIMARY KEY (NAME))");
		CreateTableIfNoExist("PRIVATE_CERTIFICATE", "CREATE TABLE PRIVATE_CERTIFICATE(ID INT, CERT VARCHAR (8192) FOR BIT DATA, PASS VARCHAR(64))");
		CreateTableIfNoExist("CHANGES_LOG", "CREATE TABLE CHANGES_LOG(ID INT, CHANGE_DATE DATE, CHANGE_TIME TIME, OFFICE_NUMBER INT, STAFF_ID INT, STAFF_NAME VARCHAR(128), CHANGE_TYPE VARCHAR(64), CHANGE_DESC VARCHAR(2048), PRIMARY KEY (ID))");
		CreateTableIfNoExist("OFFICES", "CREATE TABLE OFFICES(OFFICE_NUMBER INT, ADDRESS VARCHAR(64), IS_DELETED INT, PRIMARY KEY (OFFICE_NUMBER))");
		CreateTableIfNoExist("CASH_REGISTERS", "CREATE TABLE CASH_REGISTERS(OFFICE_NUMBER INT, CR_NUMBER INT, IS_DELETED INT, PRIMARY KEY (OFFICE_NUMBER, CR_NUMBER))");
		CreateLocalValueIfNoExist("lastDiffId", "0");
		CreateLocalValueIfNoExist(Values.LOCAL_VALUE_CONTACT_EMAIL, "");
		CreateLocalValueIfNoExist(Values.LOCAL_VALUE_NOTIFICATION_TIME, "12:00");
		CreateLocalValueIfNoExist(Values.LOCAL_VALUE_NOTIFICATION_TIME_2, "20:00");
		CreateGlobalValueIfNoExist("VATSystem", "1");
		CreateGlobalValueIfNoExist("IBAN", "");
		
		// Staff
		CreateTableIfNoExist("STAFF", "CREATE TABLE STAFF(ID INT, FIRST_NAME VARCHAR(64), LAST_NAME VARCHAR(64), OIB VARCHAR(32), PASSWORD VARCHAR (64), OFFICE_NUMBER INT, RIGHTS INT, TELEPHONE_NUM VARCHAR(32), MOBILE_NUM VARCHAR(32), IS_DELETED INT, PRIMARY KEY (ID))");
		CreateTableIfNoExist("STAFF_WORKTIME", "CREATE TABLE STAFF_WORKTIME(ID INT, O_NUM INT, CR_NUM INT, DAY INT, STAFF_ID INT, HF INT, MF INT, HT INT, MT INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), CONSTRAINT FK_STAFF_WORKTIME_STAFF FOREIGN KEY (STAFF_ID) REFERENCES STAFF(ID))");
		
		String staffRightsQuery = "CREATE TABLE STAFF_RIGHTS (ID INT, STAFF_ID INT, "
				+ "W1 INT, W2 INT, W3 INT, W4 INT, W5 INT, W6 INT, "
				+ "CR1 INT, CR2 INT, CR3 INT, CR4 INT, CR5 INT, CR6 INT, "
				+ "R1 INT, R2 INT, R3 INT, R4 INT, R5 INT, R6 INT, "
				+ "S1 INT, "
				+ "RE1 INT, RE2 INT, "
				+ "CS1 INT, CS2 INT, CS3 INT, "
				+ "O1 INT, O2 INT, O3 INT, O4 INT, O5 INT, "
				+ "R29 INT, R30 INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_STAFF_RIGHTS_STAFF FOREIGN KEY (STAFF_ID) REFERENCES STAFF(ID)"
				+ ")";
		CreateTableIfNoExist("STAFF_RIGHTS", staffRightsQuery);
		CreateColumnIfNoExist("STAFF_RIGHTS", "R29", "ALTER TABLE STAFF_RIGHTS ADD COLUMN R29 INT DEFAULT 0");
		CreateColumnIfNoExist("STAFF_RIGHTS", "R30", "ALTER TABLE STAFF_RIGHTS ADD COLUMN R30 INT DEFAULT 0");
		CreateStaffAdminIfNoExist();
		
		// Warehouse
		CreateCategoriesIfNoExist();
		CreateMeasuringUnitsIfNoExist();
		CreateTaxRatesIfNoExist();
		ExpandTaxRatesIfNoExist();
		CreatePackagingRefundsIfNoExist();
		CreateConsumptionTaxesIfNoExist();
		CreateReceiptTaxRatesIfNoExist();
		
		String consumptionTaxAmountsQuery = "CREATE TABLE CONSUMPTION_TAX_VALUES("
				+ "ID INT, OFFICE_NUMBER INT, CONSUMPTION_TAX_ID INT, VALUE REAL, "
				+ "PRIMARY KEY (ID), UNIQUE (OFFICE_NUMBER, CONSUMPTION_TAX_ID), "
				+ "CONSTRAINT FK_CONSUMPTIONTAXVALUES_CONSUMPTIONTAXES FOREIGN KEY (CONSUMPTION_TAX_ID) REFERENCES CONSUMPTION_TAXES(ID))";
		CreateTableIfNoExist("CONSUMPTION_TAX_VALUES", consumptionTaxAmountsQuery);
		
		String materialsQuery = "CREATE TABLE MATERIALS(ID INT, NAME VARCHAR(64), CATEGORY_ID INT, MEASURING_UNIT_ID INT, "
				+ "MIN_AMOUNT REAL, LAST_PRICE REAL, IS_DELETED INT, PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_CATEGORIES_MATERIALS FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID), "
				+ "CONSTRAINT FK_MEASURINGUNITS_MATERIALS FOREIGN KEY (MEASURING_UNIT_ID) REFERENCES MEASURING_UNITS(ID))";
		CreateTableIfNoExist("MATERIALS", materialsQuery);
		
		String materialAmountsQuery = "CREATE TABLE MATERIAL_AMOUNTS("
				+ "ID INT, OFFICE_NUMBER INT, MATERIAL_ID INT, AMOUNT REAL, AMOUNT_YEAR INT, "
				+ "PRIMARY KEY (ID), UNIQUE (OFFICE_NUMBER, MATERIAL_ID, AMOUNT_YEAR), "
				+ "CONSTRAINT FK_MATERIALAMOUNTS_MATERIALS FOREIGN KEY (MATERIAL_ID) REFERENCES MATERIALS(ID))";
		CreateTableIfNoExist("MATERIAL_AMOUNTS", materialAmountsQuery);
		
		String articlesQuery = "CREATE TABLE ARTICLES(ID INT, NAME VARCHAR(64), CATEGORY_ID INT, MEASURING_UNIT_ID INT, TAX_RATE_ID INT, "
				+ "MIN_AMOUNT REAL, CONSUMPTION_TAX_ID INT, PRICE REAL, EVENT_PRICE REAL, CUSTOM_ID INT, IS_ACTIVE INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), UNIQUE (CUSTOM_ID), "
				+ "CONSTRAINT FK_CATEGORIES_ARTICLES FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID), "
				+ "CONSTRAINT FK_MEASURINGUNITS_ARTICLES FOREIGN KEY (MEASURING_UNIT_ID) REFERENCES MEASURING_UNITS(ID), "
				+ "CONSTRAINT FK_TAXRATES_ARTICLES FOREIGN KEY (TAX_RATE_ID) REFERENCES TAX_RATES(ID), "
				+ "CONSTRAINT FK_CONSUMPTIONTAXES_ARTICLES FOREIGN KEY (CONSUMPTION_TAX_ID) REFERENCES CONSUMPTION_TAXES(ID))";
		CreateTableIfNoExist("ARTICLES", articlesQuery);
                CreateColumnIfNoExist("ARTICLES", "PRICE_EUR", "ALTER TABLE ARTICLES ADD COLUMN PRICE_EUR REAL DEFAULT 0");
                CreateColumnIfNoExist("ARTICLES", "EVENT_PRICE_EUR", "ALTER TABLE ARTICLES ADD COLUMN EVENT_PRICE_EUR REAL DEFAULT 0");
		
		String normativesQuery = "CREATE TABLE NORMATIVES(ID INT, ARTICLE_ID INT, MATERIAL_ID INT, AMOUNT REAL, IS_DELETED INT, PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_ARTICLES_NORMATIVES FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLES(ID), "
				+ "CONSTRAINT FK_MATERIALS_NORMATIVES FOREIGN KEY (MATERIAL_ID) REFERENCES MATERIALS(ID))";
		CreateTableIfNoExist("NORMATIVES", normativesQuery);
		
		String servicesQuery = "CREATE TABLE SERVICES(ID INT, NAME VARCHAR(64), CATEGORY_ID INT, MEASURING_UNIT_ID INT, TAX_RATE_ID INT, "
				+ "PRICE REAL, EVENT_PRICE REAL, CUSTOM_ID INT, IS_ACTIVE INT, IS_DELETED INT, PRIMARY KEY (ID), UNIQUE (CUSTOM_ID), "
				+ "CONSTRAINT FK_SERVICES_CATEGORIES FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID), "
				+ "CONSTRAINT FK_SERVICES_MEASURINGUNITS FOREIGN KEY (MEASURING_UNIT_ID) REFERENCES MEASURING_UNITS(ID), "
				+ "CONSTRAINT FK_SERVICES_TAXRATES FOREIGN KEY (TAX_RATE_ID) REFERENCES TAX_RATES(ID))";
		CreateTableIfNoExist("SERVICES", servicesQuery);
		CreateColumnIfNoExist("SERVICES", "PRICE_EUR", "ALTER TABLE SERVICES ADD COLUMN PRICE_EUR REAL DEFAULT 0");
                CreateColumnIfNoExist("SERVICES", "EVENT_PRICE_EUR", "ALTER TABLE SERVICES ADD COLUMN EVENT_PRICE_EUR REAL DEFAULT 0");
                
		String tradingGoodsQuery = "CREATE TABLE TRADING_GOODS(ID INT, NAME VARCHAR(64), CATEGORY_ID INT, TAX_RATE_ID INT, "
				+ "MIN_AMOUNT REAL, PACKAGING_REFUND_ID INT, PRICE REAL, LAST_PRICE REAL, EVENT_PRICE REAL, CUSTOM_ID INT, "
				+ "IS_ACTIVE INT, IS_DELETED INT, PRIMARY KEY (ID), UNIQUE (CUSTOM_ID), "
				+ "CONSTRAINT FK_TRADINGGOODS_CATEGORIES FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID), "
				+ "CONSTRAINT FK_TRADINGGOODS_TAXRATES FOREIGN KEY (TAX_RATE_ID) REFERENCES TAX_RATES(ID), "
				+ "CONSTRAINT FK_TRADINGGOODS_PACKAGINGREFUND FOREIGN KEY (PACKAGING_REFUND_ID) REFERENCES PACKAGING_REFUNDS(ID))";
		CreateTableIfNoExist("TRADING_GOODS", tradingGoodsQuery);
                
                CreateColumnIfNoExist("TRADING_GOODS", "PRICE_EUR", "ALTER TABLE TRADING_GOODS ADD COLUMN PRICE_EUR REAL DEFAULT 0");
                CreateColumnIfNoExist("TRADING_GOODS", "EVENT_PRICE_EUR", "ALTER TABLE TRADING_GOODS ADD COLUMN EVENT_PRICE_EUR REAL DEFAULT 0");
		
		String tradingGoodsAmountsQuery = "CREATE TABLE TRADING_GOODS_AMOUNTS("
				+ "ID INT, OFFICE_NUMBER INT, TRADING_GOODS_ID INT, AMOUNT REAL, AMOUNT_YEAR INT, "
				+ "PRIMARY KEY (ID), UNIQUE (OFFICE_NUMBER, TRADING_GOODS_ID, AMOUNT_YEAR), "
				+ "CONSTRAINT FK_TRADINGGOODSAMOUNTS_TRADINGGOODS FOREIGN KEY (TRADING_GOODS_ID) REFERENCES TRADING_GOODS(ID))";
		CreateTableIfNoExist("TRADING_GOODS_AMOUNTS", tradingGoodsAmountsQuery);
		
		// Clients and suppliers
		String clientsQuery = "CREATE TABLE CLIENTS(ID INT, NAME VARCHAR(64), OIB VARCHAR(32), STREET VARCHAR(64), HOUSE_NUM VARCHAR(16), "
				+ "TOWN VARCHAR(64), POSTAL_CODE INT, COUNTRY VARCHAR(64), PAYMENT_DELAY INT, BIRTHDAY VARCHAR(64), MOBILE_NUM VARCHAR(32), TELEPHONE_NUM VARCHAR(32), "
				+ "WEBSITE VARCHAR(64), EMAIL VARCHAR(64), NOTES VARCHAR(256), TRAFFIC REAL, TYPE INT DEFAULT 0, DISCOUNT INT DEFAULT 0, LOYALTY_CARD VARCHAR(64) DEFAULT '', "
				+ "IS_DELETED INT, PRIMARY KEY (ID))";
		CreateTableIfNoExist("CLIENTS", clientsQuery);
		CreateColumnIfNoExist("CLIENTS", "TYPE", "ALTER TABLE CLIENTS ADD COLUMN TYPE INT DEFAULT 0");
		CreateColumnIfNoExist("CLIENTS", "DISCOUNT", "ALTER TABLE CLIENTS ADD COLUMN DISCOUNT INT DEFAULT 0");
		CreateColumnIfNoExist("CLIENTS", "LOYALTY_CARD", "ALTER TABLE CLIENTS ADD COLUMN LOYALTY_CARD VARCHAR(64) DEFAULT ''");

		CreateSuppliersIfNoExist();
		
		// Receipts
		String receiptsQuery = "CREATE TABLE RECEIPTS("
				+ "ID INT, RECEIPT_DATE DATE, SUPPLIER_ID INT, DOCUMENT_NUMBER VARCHAR(64), TOTAL_PRICE REAL, "
				+ "PAYMENT_DUE_DATE DATE, IS_PAID INT, OFFICE_NUMBER INT, RECEIPT_NUMBER INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_RECEIPTS_SUPPLIERS FOREIGN KEY (SUPPLIER_ID) REFERENCES SUPPLIERS(ID))";
		CreateTableIfNoExist("RECEIPTS", receiptsQuery);
		
		String receiptMaterialsQuery = "CREATE TABLE RECEIPT_MATERIALS("
				+ "ID INT, RECEIPT_ID INT, MATERIAL_ID INT, AMOUNT REAL, PRICE REAL, RABATE REAL, TAX_IN_VALUE REAL, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_RECEIPTMATERIALS_RECEIPTS FOREIGN KEY (RECEIPT_ID) REFERENCES RECEIPTS(ID), "
				+ "CONSTRAINT FK_RECEIPTMATERIALS_MATERIALS FOREIGN KEY (MATERIAL_ID) REFERENCES MATERIALS(ID))";
		CreateTableIfNoExist("RECEIPT_MATERIALS", receiptMaterialsQuery);
		
		String receiptTradingGoodsQuery = "CREATE TABLE RECEIPT_TRADING_GOODS("
				+ "ID INT, RECEIPT_ID INT, TRADING_GOODS_ID INT, AMOUNT REAL, PRICE REAL, RABATE REAL, MARGIN REAL, TAX_RATE REAL, TAX_IN_VALUE REAL, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_RECEIPTTRADINGGOODS_RECEIPTS FOREIGN KEY (RECEIPT_ID) REFERENCES RECEIPTS(ID), "
				+ "CONSTRAINT FK_RECEIPTTRADINGGOODS_TRADINGGOODS FOREIGN KEY (TRADING_GOODS_ID) REFERENCES TRADING_GOODS(ID))";
		CreateTableIfNoExist("RECEIPT_TRADING_GOODS", receiptTradingGoodsQuery);
		
		// Transfers
		String transfersQuery = "CREATE TABLE TRANSFERS("
				+ "ID INT, TRANSFER_START_DATE DATE, STARTING_OFFICE_ID INT, DESTINATION_OFFICE_ID INT, TOTAL_PRICE REAL, TRANSFER_RECIEVED_DATE DATE, IS_DELIVERED INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_TRANSFERS_OFFICES_1 FOREIGN KEY (STARTING_OFFICE_ID) REFERENCES OFFICES(OFFICE_NUMBER), "
				+ "CONSTRAINT FK_TRANSFERS_OFFICES_2 FOREIGN KEY (DESTINATION_OFFICE_ID) REFERENCES OFFICES(OFFICE_NUMBER))";
		CreateTableIfNoExist("TRANSFERS", transfersQuery);
		
		String transferMaterialsQuery = "CREATE TABLE TRANSFER_MATERIALS("
				+ "ID INT, TRANSFER_ID INT, MATERIAL_ID INT, AMOUNT_START REAL, AMOUNT_DEST REAL, PRICE REAL, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_TRANSFERMATERIALS_TRANSFERS FOREIGN KEY (TRANSFER_ID) REFERENCES TRANSFERS(ID), "
				+ "CONSTRAINT FK_TRANSFERMATERIALS_MATERIALS FOREIGN KEY (MATERIAL_ID) REFERENCES MATERIALS(ID))";
		CreateTableIfNoExist("TRANSFER_MATERIALS", transferMaterialsQuery);
		
		String transferArticlesQuery = "CREATE TABLE TRANSFER_ARTICLES("
				+ "ID INT, TRANSFER_ID INT, STARTING_ARTICLE_ID INT, DESTINATION_ARTICLE_ID INT, AMOUNT_START REAL, AMOUNT_DEST REAL, PRICE REAL, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_TRANSFERARTICLES_TRANSFERS FOREIGN KEY (TRANSFER_ID) REFERENCES TRANSFERS(ID), "
				+ "CONSTRAINT FK_TRANSFERARTICLES_ARTICLES_1 FOREIGN KEY (STARTING_ARTICLE_ID) REFERENCES ARTICLES(ID), "
				+ "CONSTRAINT FK_TRANSFERARTICLES_ARTICLES_2 FOREIGN KEY (DESTINATION_ARTICLE_ID) REFERENCES ARTICLES(ID))";
		CreateTableIfNoExist("TRANSFER_ARTICLES", transferArticlesQuery);
		
		String transferArticleMaterialsQuery = "CREATE TABLE TRANSFER_ARTICLE_MATERIALS("
				+ "ID INT, TRANSFER_ARTICLE_ID INT, MATERIAL_ID INT, NORMATIVE REAL, IS_STARTING INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_TRANSFERARTICLEMATERIALS_TRANSFERARTICLES FOREIGN KEY (TRANSFER_ARTICLE_ID) REFERENCES TRANSFER_ARTICLES(ID), "
				+ "CONSTRAINT FK_TRANSFERARTICLEMATERIALS_MATERIALS FOREIGN KEY (MATERIAL_ID) REFERENCES MATERIALS(ID))";
		CreateTableIfNoExist("TRANSFER_ARTICLE_MATERIALS", transferArticleMaterialsQuery);
		
		// Payment methods
		CreatePaymentMethodsIfNoExist();
		
		// Office worktime
		String officeWorktimeQuery = "CREATE TABLE OFFICE_WORKTIME (ID INT, OFFICE_NUMBER INT, "
				+ "W1 INT, W2 INT, W3 INT, W4 INT, W5 INT, W6 INT, W7 INT, "
				+ "HF1 INT, HT1 INT, MF1 INT, MT1 INT, "
				+ "HF2 INT, HT2 INT, MF2 INT, MT2 INT, "
				+ "HF3 INT, HT3 INT, MF3 INT, MT3 INT, "
				+ "HF4 INT, HT4 INT, MF4 INT, MT4 INT, "
				+ "HF5 INT, HT5 INT, MF5 INT, MT5 INT, "
				+ "HF6 INT, HT6 INT, MF6 INT, MT6 INT, "
				+ "HF7 INT, HT7 INT, MF7 INT, MT7 INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_OFFICEWORKTIME_OFFICES FOREIGN KEY (OFFICE_NUMBER) REFERENCES OFFICES(OFFICE_NUMBER))";
		CreateTableIfNoExist("OFFICE_WORKTIME", officeWorktimeQuery);
		
		// Holidays
		String holidaysQuery = "CREATE TABLE HOLIDAYS (ID INT, OFFICE_NUMBER INT, "
				+ "HOLIDAY_DATE DATE, NAME VARCHAR(64), IS_ACTIVE INT, IS_DELETED INT, PRIMARY KEY (ID))";
		CreateTableIfNoExist("HOLIDAYS", holidaysQuery);
		
		// Shifts
		String shiftsQuery = "CREATE TABLE SHIFTS (ID INT, OFFICE_NUMBER INT, "
				+ "NAME VARCHAR(64), HF INT, HT INT, MF INT, MT INT, IS_DELETED INT, PRIMARY KEY (ID))";
		CreateTableIfNoExist("SHIFTS", shiftsQuery);
		
		// Discounts
		String discountsQuery = "CREATE TABLE DISCOUNTS (ID INT, VALUE REAL, TYPE INT, IS_DELETED INT, PRIMARY KEY (ID))";
		CreateTableIfNoExist("DISCOUNTS", discountsQuery);
		
		// App settings
		String appSettingsQuery = "CREATE TABLE APP_SETTINGS(ID INT, VALUE VARCHAR(2048), OFFICE_NUMBER INT, CR_NUMBER INT, "
				+ "PRIMARY KEY (ID, OFFICE_NUMBER, CR_NUMBER))";
		CreateTableIfNoExist("APP_SETTINGS", appSettingsQuery);
		
		// Invoices
		String invoicesQuery = "CREATE TABLE INVOICES(ID INT, O_NUM INT, O_TAG VARCHAR(16), CR_NUM INT, I_NUM INT, SPEC_NUM INT, "
				+ "I_DATE DATE, I_TIME TIME, S_OIB VARCHAR(32), S_ID INT, PAY_NAME VARCHAR(64), PAY_TYPE INT, C_ID INT, "
				+ "DIS_PCT REAL, DIS_AMT REAL, FIN_PR REAL, ZKI CHAR(32), JIR CHAR(36), NOTE VARCHAR(512), VAT_SYS INT, "
				+ "E_IN_ID VARCHAR(32), E_IN_ST VARCHAR(64), S_ZKI VARCHAR(32), S_JIR VARCHAR(36), "
				+ "PAY_NAME_2 VARCHAR(64) DEFAULT '', PAY_TYPE_2 INT DEFAULT -1, PAY_AMT_2 REAL DEFAULT 0"
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_INVOICES_STAFF FOREIGN KEY (S_ID) REFERENCES STAFF(ID)"
				+ ")";
		CreateTableIfNoExist("INVOICES", invoicesQuery);
		CreateTableIfNoExist("INVOICES_TEST", invoicesQuery.replace("INVOICES", "INVOICES_TEST"));
		// TODO remove
		CreateColumnIfNoExist("INVOICES", "E_IN_ID", "ALTER TABLE INVOICES ADD COLUMN E_IN_ID VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES_TEST", "E_IN_ID", "ALTER TABLE INVOICES_TEST ADD COLUMN E_IN_ID VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES", "E_IN_ST", "ALTER TABLE INVOICES ADD COLUMN E_IN_ST VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES_TEST", "E_IN_ST", "ALTER TABLE INVOICES_TEST ADD COLUMN E_IN_ST VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES", "S_ZKI", "ALTER TABLE INVOICES ADD COLUMN S_ZKI VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES_TEST", "S_ZKI", "ALTER TABLE INVOICES_TEST ADD COLUMN S_ZKI VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES", "S_JIR", "ALTER TABLE INVOICES ADD COLUMN S_JIR VARCHAR(36) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES_TEST", "S_JIR", "ALTER TABLE INVOICES_TEST ADD COLUMN S_JIR VARCHAR(36) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES", "PAY_NAME_2", "ALTER TABLE INVOICES ADD COLUMN PAY_NAME_2 VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES_TEST", "PAY_NAME_2", "ALTER TABLE INVOICES_TEST ADD COLUMN PAY_NAME_2 VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("INVOICES", "PAY_TYPE_2", "ALTER TABLE INVOICES ADD COLUMN PAY_TYPE_2 INT DEFAULT -1");
		CreateColumnIfNoExist("INVOICES_TEST", "PAY_TYPE_2", "ALTER TABLE INVOICES_TEST ADD COLUMN PAY_TYPE_2 INT DEFAULT -1");
		CreateColumnIfNoExist("INVOICES", "PAY_AMT_2", "ALTER TABLE INVOICES ADD COLUMN PAY_AMT_2 REAL DEFAULT 0");
		CreateColumnIfNoExist("INVOICES_TEST", "PAY_AMT_2", "ALTER TABLE INVOICES_TEST ADD COLUMN PAY_AMT_2 REAL DEFAULT 0");
                //CreateColumnIfNoExist("INVOICES", "IZNOS_NAPOJNICE", "ALTER TABLE INVOICES ADD COLUMN IZNOS_NAPOJNICE VARCHAR(36) DEFAULT ''");
		//CreateColumnIfNoExist("INVOICES_TEST", "IZNOS_NAPOJNICE", "ALTER TABLE INVOICES ADD COLUMN IZNOS_NAPOJNICE VARCHAR(36) DEFAULT ''");
                //CreateColumnIfNoExist("INVOICES", "TIP_PLACANJA", "ALTER TABLE INVOICES ADD COLUMN TIP_PLACANJA VARCHAR(36) DEFAULT ''");
		//CreateColumnIfNoExist("INVOICES_TEST", "TIP_PLACANJA", "ALTER TABLE INVOICES_TEST ADD TIP_PLACANJA S_JIR VARCHAR(36) DEFAULT ''");
		
		String localInvoicesQuery = "CREATE TABLE LOCAL_INVOICES(ID INT, O_NUM INT, O_TAG VARCHAR(16), CR_NUM INT, I_NUM INT, SPEC_NUM INT, "
				+ "I_DATE DATE, I_TIME TIME, S_OIB VARCHAR(32), S_ID INT, PAY_NAME VARCHAR(64), PAY_TYPE INT, C_ID INT, "
				+ "DIS_PCT REAL, DIS_AMT REAL, FIN_PR REAL, ZKI CHAR(32), JIR CHAR(36), NOTE VARCHAR(512), VAT_SYS INT, "
				+ "E_IN_ID VARCHAR(32), E_IN_ST VARCHAR(64), S_ZKI VARCHAR(32), S_JIR VARCHAR(36), IS_DELETED INT, "
				+ "PAY_NAME_2 VARCHAR(64) DEFAULT '', PAY_TYPE_2 INT DEFAULT -1, PAY_AMT_2 REAL DEFAULT 0, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_LOCALINVOICES_STAFF FOREIGN KEY (S_ID) REFERENCES STAFF(ID)"
				+ ")";
		CreateTableIfNoExist("LOCAL_INVOICES", localInvoicesQuery);
		CreateTableIfNoExist("LOCAL_INVOICES_TEST", localInvoicesQuery.replace("INVOICES", "INVOICES_TEST"));
		// TODO remove
		CreateColumnIfNoExist("LOCAL_INVOICES", "E_IN_ID", "ALTER TABLE LOCAL_INVOICES ADD COLUMN E_IN_ID VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "E_IN_ID", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN E_IN_ID VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES", "E_IN_ST", "ALTER TABLE LOCAL_INVOICES ADD COLUMN E_IN_ST VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "E_IN_ST", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN E_IN_ST VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES", "S_ZKI", "ALTER TABLE LOCAL_INVOICES ADD COLUMN S_ZKI VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "S_ZKI", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN S_ZKI VARCHAR(32) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES", "S_JIR", "ALTER TABLE LOCAL_INVOICES ADD COLUMN S_JIR VARCHAR(36) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "S_JIR", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN S_JIR VARCHAR(36) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES", "PAY_NAME_2", "ALTER TABLE LOCAL_INVOICES ADD COLUMN PAY_NAME_2 VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "PAY_NAME_2", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN PAY_NAME_2 VARCHAR(64) DEFAULT ''");
		CreateColumnIfNoExist("LOCAL_INVOICES", "PAY_TYPE_2", "ALTER TABLE LOCAL_INVOICES ADD COLUMN PAY_TYPE_2 INT DEFAULT -1");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "PAY_TYPE_2", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN PAY_TYPE_2 INT DEFAULT -1");
		CreateColumnIfNoExist("LOCAL_INVOICES", "PAY_AMT_2", "ALTER TABLE LOCAL_INVOICES ADD COLUMN PAY_AMT_2 REAL DEFAULT 0");
		CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "PAY_AMT_2", "ALTER TABLE LOCAL_INVOICES_TEST ADD COLUMN PAY_AMT_2 REAL DEFAULT 0");
                //CreateColumnIfNoExist("LOCAL_INVOICES", "IZNOS_NAPOJNICE", "ALTER TABLE INVOICES ADD COLUMN IZNOS_NAPOJNICE VARCHAR(36) DEFAULT ''");
		//CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "IZNOS_NAPOJNICE", "ALTER TABLE INVOICES ADD COLUMN IZNOS_NAPOJNICE VARCHAR(36) DEFAULT ''");
                //CreateColumnIfNoExist("LOCAL_INVOICES", "TIP_PLACANJA", "ALTER TABLE INVOICES ADD COLUMN TIP_PLACANJA VARCHAR(36) DEFAULT ''");
		//CreateColumnIfNoExist("LOCAL_INVOICES_TEST", "TIP_PLACANJA", "ALTER TABLE INVOICES_TEST ADD TIP_PLACANJA S_JIR VARCHAR(36) DEFAULT ''");
		
		String invoiceItemsQuery = "CREATE TABLE INVOICE_ITEMS(ID INT, IN_ID INT, IT_TYPE INT, "
				+ "IT_ID INT, IT_NAME VARCHAR(64), AMT REAL, PR REAL, "
				+ "DIS_PCT REAL, DIS_AMT REAL, TAX REAL, C_TAX REAL, PACK_REF REAL, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_INVOICEITEMS_INVOICES FOREIGN KEY (IN_ID) REFERENCES INVOICES(ID))";
		CreateTableIfNoExist("INVOICE_ITEMS", invoiceItemsQuery);
		CreateTableIfNoExist("INVOICE_ITEMS_TEST", invoiceItemsQuery.replace("ITEMS", "ITEMS_TEST").replace("INVOICES", "INVOICES_TEST"));
		//CreateColumnIfNoExist("INVOICE_ITEMS", "PACK_REF", "ALTER TABLE INVOICE_ITEMS ADD COLUMN PACK_REF REAL DEFAULT 0");
		//CreateColumnIfNoExist("INVOICE_ITEMS_TEST", "PACK_REF", "ALTER TABLE INVOICE_ITEMS_TEST ADD COLUMN PACK_REF REAL DEFAULT 0");

		String localInvoiceItemsQuery = "CREATE TABLE LOCAL_INVOICE_ITEMS(ID INT, IN_ID INT, IT_TYPE INT, "
				+ "IT_ID INT, IT_NAME VARCHAR(64), AMT REAL, PR REAL, "
				+ "DIS_PCT REAL, DIS_AMT REAL, TAX REAL, C_TAX REAL, PACK_REF REAL, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_LOCALINVOICEITEMS_LOCALINVOICES FOREIGN KEY (IN_ID) REFERENCES LOCAL_INVOICES(ID))";
		CreateTableIfNoExist("LOCAL_INVOICE_ITEMS", localInvoiceItemsQuery);
		CreateTableIfNoExist("LOCAL_INVOICE_ITEMS_TEST", localInvoiceItemsQuery.replace("ITEMS", "ITEMS_TEST").replace("INVOICES", "INVOICES_TEST"));
		//CreateColumnIfNoExist("LOCAL_INVOICE_ITEMS", "PACK_REF", "ALTER TABLE LOCAL_INVOICE_ITEMS ADD COLUMN PACK_REF REAL DEFAULT 0");
		//CreateColumnIfNoExist("LOCAL_INVOICE_ITEMS_TEST", "PACK_REF", "ALTER TABLE LOCAL_INVOICE_ITEMS_TEST ADD COLUMN PACK_REF REAL DEFAULT 0");
		
		String invoiceMaterials = "CREATE TABLE INVOICE_MATERIALS(ID INT, "
				+ "IN_ID INT, ART_ID INT, MAT_ID INT, AMT REAL, NORM REAL, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_INVOICEMATERIALS_INVOICES FOREIGN KEY (IN_ID) REFERENCES INVOICES(ID), "
				+ "CONSTRAINT FK_INVOICEMATERIALS_MATERIALS FOREIGN KEY (MAT_ID) REFERENCES MATERIALS(ID), "
				+ "CONSTRAINT FK_INVOICEMATERIALS_ARTICLES FOREIGN KEY (ART_ID) REFERENCES ARTICLES(ID))";
		CreateTableIfNoExist("INVOICE_MATERIALS", invoiceMaterials);
		CreateTableIfNoExist("INVOICE_MATERIALS_TEST", invoiceMaterials.replace("MATERIALS_", "MATERIALSTEST_").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("INVOICES", "INVOICES_TEST"));
		
		String localInvoiceMaterials = "CREATE TABLE LOCAL_INVOICE_MATERIALS(ID INT, "
				+ "IN_ID INT, ART_ID INT, MAT_ID INT, AMT REAL, NORM REAL, IS_DELETED INT, "
				+ "PRIMARY KEY (ID), "
				+ "CONSTRAINT FK_LOCALINVOICEMATERIALS_LOCALINVOICES FOREIGN KEY (IN_ID) REFERENCES LOCAL_INVOICES(ID), "
				+ "CONSTRAINT FK_LOCALINVOICEMATERIALS_MATERIALS FOREIGN KEY (MAT_ID) REFERENCES MATERIALS(ID), "
				+ "CONSTRAINT FK_LOCALINVOICEMATERIALS_ARTICLES FOREIGN KEY (ART_ID) REFERENCES ARTICLES(ID))";
		CreateTableIfNoExist("LOCAL_INVOICE_MATERIALS", localInvoiceMaterials);
		CreateTableIfNoExist("LOCAL_INVOICE_MATERIALS_TEST", localInvoiceMaterials.replace("MATERIALS_", "MATERIALSTEST_").replace("INVOICE_MATERIALS", "INVOICE_MATERIALS_TEST").replace("INVOICES", "INVOICES_TEST"));
		
                CreateTableIfNoExist("TECAJ", "CREATE TABLE TECAJ(ID INT, EX_RATE REAL DEFAULT 0, PRICE_CONVERTED INT DEFAULT -1, APP_CONVERTED INT DEFAULT -1, PRIMARY KEY(ID))");
                CreateTecajIfNoExist();
		// Cash register tables
		CreateTablesIfNoExist();
		ExpandTablesIfNoExist();
		
		// TODO remove - used for testing
		//CreateTableIfNoExist("123123", "Drop Table APP_SETTINGS");
	}

	private static void CreateTableIfNoExist(String tableName, String tableQuery) throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, tableName, null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(tableQuery);
			statement.close();
		}
	}
	
	private static void CreateColumnIfNoExist(String tableName, String columnName, String tableQuery) throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getColumns(null, null, tableName, columnName);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(tableQuery);
			statement.close();
		}
	}
	
	private static void CreateLocalValueIfNoExist(String name, String value) throws SQLException {
		String query = "SELECT * FROM LOCAL_VALUES_TABLE WHERE NAME = ? FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setString(1, name);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (!result.next()) {
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO LOCAL_VALUES_TABLE (NAME, VALUE) VALUES (?, ?)");
			psInsert.setString(1, name);
			psInsert.setString(2, value);
            psInsert.executeUpdate();
			psInsert.close();
		}
		ps.close();
	}
	
	private static void CreateGlobalValueIfNoExist(String name, String value) throws SQLException {
		String query = "SELECT * FROM GLOBAL_VALUES_TABLE WHERE NAME = ? FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setString(1, name);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (!result.next()) {
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO GLOBAL_VALUES_TABLE (NAME, VALUE) VALUES (?, ?)");
			psInsert.setString(1, name);
			psInsert.setString(2, value);
            psInsert.executeUpdate();
			psInsert.close();
		}
		ps.close();
	}
	
	private static void UpdateDiffTableType() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getColumns(null, null, "DIFF_TABLE", "DIFF");
		if(rs.next()){
			if(!"32672".equals(rs.getString(7))){
				Statement statement = getDatabaseConnection().createStatement();
				statement.execute("ALTER TABLE DIFF_TABLE ALTER COLUMN DIFF SET DATA TYPE VARCHAR (32672) FOR BIT DATA");
				statement.close();
			}
		}
	}
        
        private static void CreateTecajIfNoExist() throws IOException, SQLException{
            String query = "SELECT * FROM TECAJ WHERE 1 = 1";
            PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
                if(!result.next()){
                    PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO TECAJ (ID, EX_RATE, PRICE_CONVERTED, APP_CONVERTED) VALUES (?, ?, ?, ?)");
                    psInsert.setInt(1, 0);
                    psInsert.setDouble(2, 7.53450);
                    psInsert.setInt(3, -1);
                    psInsert.setInt(4, -1);
                    psInsert.executeUpdate();
                    psInsert.close();
                }
            ps.close();
        }
	
	private static void CreateStaffAdminIfNoExist() throws IOException, SQLException {
		String query = "SELECT ID FROM STAFF WHERE ID = 0 AND LAST_NAME = 'admin' FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (!result.next()) {
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO STAFF (ID, FIRST_NAME, LAST_NAME, OIB, PASSWORD, OFFICE_NUMBER, RIGHTS, TELEPHONE_NUM, MOBILE_NUM, IS_DELETED) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			psInsert.setInt(1, 0);
			psInsert.setString(2, "admin");
			psInsert.setString(3, "admin");
			psInsert.setString(4, "00000000001");
			psInsert.setString(5, "98imunzb7");
			psInsert.setInt(6, Licence.GetOfficeNumber());
			psInsert.setInt(7, Values.STAFF_RIGHTS_ADMIN);
			psInsert.setString(8, "---");
			psInsert.setString(9, "---");
			psInsert.setInt(10, 0);
            psInsert.executeUpdate();
			psInsert.close();

			String staffRightsQuery = "INSERT INTO STAFF_RIGHTS (ID, STAFF_ID, "
					+ "W1, W2, W3, W4, W5, W6, "
					+ "CR1, CR2, CR3, CR4, CR5, CR6, "
					+ "R1, R2, R3, R4, R5, R6, "
					+ "S1, "
					+ "RE1, RE2, "
					+ "CS1, CS2, CS3, "
					+ "O1, O2, O3, O4, O5, "
					+ "R29, R30"
					+ ") VALUES (?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, "
					+ "?, "
					+ "?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ?, ?, ?, "
					+ "?, ?"
					+ ")";
			PreparedStatement psInsertStaffRights = getDatabaseConnection().prepareStatement(staffRightsQuery);
			psInsertStaffRights.setInt(1, 0);
			psInsertStaffRights.setInt(2, 0);
			for(int i = 0; i < Values.STAFF_RIGHTS_TOTAL_LENGTH; ++i){
				psInsertStaffRights.setInt(3 + i, 1);
			}
            psInsertStaffRights.executeUpdate();
			psInsertStaffRights.close();
		}
		ps.close();
	}
	
	private static void CreateCategoriesIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "CATEGORIES", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE CATEGORIES(ID INT, NAME VARCHAR(32), IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO CATEGORIES (ID, NAME, IS_DELETED) VALUES (?, ?, ?)");
			String[] toInsert = new String[] {"Hrana", "Piće", "Napici", "Cigarete", "Ostalo"};
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setInt(3, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreateMeasuringUnitsIfNoExist() throws SQLException {		
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "MEASURING_UNITS", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE MEASURING_UNITS(ID INT, NAME VARCHAR(32), IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO MEASURING_UNITS (ID, NAME, IS_DELETED) VALUES (?, ?, ?)");
			String[] toInsert = new String[] {"Litra", "Kilogram", "Pakiranje", "Komad", "Mililitar", "Decilitar", "Gram", "Porcija", "Par", "Sat"};
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setInt(3, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreateTaxRatesIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "TAX_RATES", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE TAX_RATES(ID INT, NAME VARCHAR(32), VALUE REAL, IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO TAX_RATES (ID, NAME, VALUE, IS_DELETED) VALUES (?, ?, ?, ?)");
			String[] toInsert = new String[] { "PDV - 0%", "PDV - 25%", "Ne podliježe PDV-u", "PDV - 13%" };
			float[] toInsertValues = new float[] { 0f, 25f, 0f, 13f };
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setFloat(3, toInsertValues[i]);
				psInsert.setInt(4, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void ExpandTaxRatesIfNoExist() throws SQLException {
		String query = "SELECT ID FROM TAX_RATES WHERE VALUE = 13 OR ID = 3 FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (!result.next()) {
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO TAX_RATES "
					+ "(ID, NAME, VALUE, IS_DELETED) VALUES (?, ?, ?, ?)");
			psInsert.setInt(1, 3);
			psInsert.setString(2, "PDV - 13%");
			psInsert.setFloat(3, 13f);
			psInsert.setInt(4, 0);
			psInsert.executeUpdate();
		}
		ps.close();
	}
	
	private static void CreateConsumptionTaxesIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "CONSUMPTION_TAXES", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE CONSUMPTION_TAXES(ID INT, NAME VARCHAR(32), IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO CONSUMPTION_TAXES (ID, NAME, IS_DELETED) VALUES (?, ?, ?)");
			String[] toInsert = new String[] { "Vino", "Žestoka alkoholna pića", "Pivo", "Bezalkoholna pića", "Hrana", "Kava", "Cigarete", "Ostalo" };
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setInt(3, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreatePackagingRefundsIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "PACKAGING_REFUNDS", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE PACKAGING_REFUNDS(ID INT, NAME VARCHAR(32), VALUE REAL, IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO PACKAGING_REFUNDS (ID, NAME, VALUE, IS_DELETED) VALUES (?, ?, ?, ?)");
			String[] toInsert = new String[] { "Nema povratne naknade", "Povratna ambalaža" };
			float[] toInsertValues = new float[] { 0.0f, 0.5f };
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setFloat(3, toInsertValues[i]);
				psInsert.setInt(4, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreateReceiptTaxRatesIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "RECEIPT_TAX_RATES", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute("CREATE TABLE RECEIPT_TAX_RATES(ID INT, NAME VARCHAR(32), VALUE REAL, IS_DELETED INT, PRIMARY KEY (ID))");
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO RECEIPT_TAX_RATES (ID, NAME, VALUE, IS_DELETED) VALUES (?, ?, ?, ?)");
			String[] toInsert = new String[] { "PDV - 25%", "PDV - 13%", "PDV - 0%" };
			float[] toInsertValues = new float[] { 25f, 13f, 0f };
			for(int i = 0; i < toInsert.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsert[i]);
				psInsert.setFloat(3, toInsertValues[i]);
				psInsert.setInt(4, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreatePaymentMethodsIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "PAYMENT_METHODS", null);
		if(!rs.next()){
			String paymentMethodsQuery = "CREATE TABLE PAYMENT_METHODS("
				+ "ID INT, NAME VARCHAR(64), PAYMENT_TYPE INT, IS_ACTIVE INT, IS_DELETED INT, "
				+ "PRIMARY KEY (ID))";
			
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(paymentMethodsQuery);
			statement.close();
			
			String  insertQuery = "INSERT INTO PAYMENT_METHODS "
					+ "(ID, NAME, PAYMENT_TYPE, IS_ACTIVE, IS_DELETED) "
					+ "VALUES (?, ?, ?, ?, ?)";
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement(insertQuery);
			String[] toInsertName = new String[] { "Novčanice i/ili kovanice", "Maestro", "Mastercard", "American Express", "Diners", "Visa", "Transakcijski račun", "Izdatnica" };
			int[] toInsertType = new int[] {
				Values.PAYMENT_METHOD_TYPE_CASH, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD,
				Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD, Values.PAYMENT_METHOD_TYPE_CREDIT_CARD,
				Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL, Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP
			};
			int[] toInsertIsActive = new int[] { 1, 0, 0, 0, 0, 0, 0, 1 };
			for(int i = 0; i < toInsertName.length; ++i){
				psInsert.setInt(1, i);
				psInsert.setString(2, toInsertName[i]);
				psInsert.setFloat(3, toInsertType[i]);
				psInsert.setInt(4, toInsertIsActive[i]);
				psInsert.setInt(5, 0);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void CreateTablesIfNoExist() throws SQLException {
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "TABLES", null);
		String tablesQuery = "CREATE TABLE TABLES(ID INT, STAFF_ID INT, STAFF_NAME VARCHAR (64), PRICE REAL, CR_NUM INT, "
				+ "INVOICE_DATA VARCHAR (32672) FOR BIT DATA, "
				+ "PRIMARY KEY (ID))";
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(tablesQuery);
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO TABLES "
					+ "(ID, STAFF_ID, STAFF_NAME, PRICE, CR_NUM, INVOICE_DATA) VALUES (?, ?, ?, ?, ?, ?)");
			for (int i = 0; i < 100; ++i){
				psInsert.setInt(1, i);
				psInsert.setInt(2, -1);
				psInsert.setString(3, "");
				psInsert.setFloat(4, 0f);
				psInsert.setInt(5, -1);
				psInsert.setBytes(6, null);
				psInsert.executeUpdate();
			}
		}
	}
	
	private static void ExpandTablesIfNoExist() throws SQLException {
		String query = "SELECT ID FROM TABLES WHERE ID = 50 FETCH FIRST ROW ONLY";
		PreparedStatement ps = getDatabaseConnection().prepareStatement(query);
		ps.setMaxRows(1);
		ResultSet result = ps.executeQuery();
		if (!result.next()) {
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement("INSERT INTO TABLES "
					+ "(ID, STAFF_ID, STAFF_NAME, PRICE, CR_NUM, INVOICE_DATA) VALUES (?, ?, ?, ?, ?, ?)");
			for (int i = 50; i < 100; ++i){
				psInsert.setInt(1, i);
				psInsert.setInt(2, -1);
				psInsert.setString(3, "");
				psInsert.setFloat(4, 0f);
				psInsert.setInt(5, -1);
				psInsert.setBytes(6, null);
				psInsert.executeUpdate();
			}
		}
		ps.close();
	}
	
	private static void CreateSuppliersIfNoExist() throws SQLException {
		String suppliersQuery = "CREATE TABLE SUPPLIERS(ID INT, NAME VARCHAR(64), OIB VARCHAR(32), STREET VARCHAR(64), HOUSE_NUM VARCHAR(16), "
				+ "TOWN VARCHAR(64), POSTAL_CODE INT, COUNTRY VARCHAR(64), CONTACT_PERSON VARCHAR(64), "
				+ "MOBILE_NUM VARCHAR(32), TELEPHONE_NUM VARCHAR(32), WEBSITE VARCHAR(64), EMAIL VARCHAR(64), NOTES VARCHAR(256), IS_DELETED INT, PRIMARY KEY (ID))";
		String insertSupplierQuery = "INSERT INTO SUPPLIERS (ID, NAME, OIB, STREET, HOUSE_NUM, "
				+ "TOWN, POSTAL_CODE, COUNTRY, CONTACT_PERSON, "
				+ "MOBILE_NUM, TELEPHONE_NUM, WEBSITE, EMAIL, NOTES, IS_DELETED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
		
		DatabaseMetaData dbm = getDatabaseConnection().getMetaData();
		ResultSet rs = dbm.getTables(null, null, "SUPPLIERS", null);
		if(!rs.next()){
			Statement statement = getDatabaseConnection().createStatement();
            statement.execute(suppliersQuery);
			statement.close();
			
			PreparedStatement psInsert = getDatabaseConnection().prepareStatement(insertSupplierQuery);
			psInsert.setInt(1, 0);
			psInsert.setString(2, "inventura");
			psInsert.setString(3, "00000000001");
			psInsert.setString(4, "");
			psInsert.setString(5, "");
			psInsert.setString(6, "");
			psInsert.setInt(7, 0);
			psInsert.setString(8, "");
			psInsert.setString(9, "");
			psInsert.setString(10, "");
			psInsert.setString(11, "");
			psInsert.setString(12, "");
			psInsert.setString(13, "");
			psInsert.setString(14, "");
			psInsert.executeUpdate();
		}
	}
	
	private static Connection getDatabaseConnection(){
		return LocalServerDatabaseSetup.connection;
	}
}
