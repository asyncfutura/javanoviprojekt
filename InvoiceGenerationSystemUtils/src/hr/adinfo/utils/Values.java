/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils;

import java.awt.Color;

/**
 *
 * @author Matej
 */
public final class Values {
	// Server info
	public static final String SERVER_APP_ADDRESS = "adinfo.ddns.net";
	public static final String SERVER_APP_LOCALHOST = "localhost";
        public static final int CLIENT_APP_SPARE_PRVA_KASA_PORT = 9083;
        public static final int CLIENT_APP_SPARE_DRUGA_KASA_PORT = 9084;
        public static final int CLIENT_APP_PINGVIN_PRVA_KASA_PORT = 9085;
        public static final int CLIENT_APP_PINGVIN_DRUGA_KASA_PORT = 9086;
        public static final int CLIENT_APP_BRECERA_PORT = 9087;
        public static final int CLIENT_APP_TOTAL_KAPTOLSKA_KLET_PORT = 9088;
        public static final int CLIENT_APP_TOTAL_PORT = 9089;
	public static final int CLIENT_APP_LOCAL_SERVER_LOCALHOST_PORT = 9090;
	public static final int CLIENT_APP_MASTER_LOCAL_SERVER_LOCALHOST_PORT = 9091;
	public static final int CLIENT_APP_LOCAL_SERVER_UDP_PORT = 9098;
	public static final int SERVER_APP_LOCALHOST_PORT = 9093;
	public static final int SERVER_APP_CONTROL_LOCALHOST_PORT = 9094;
	public static final int SERVER_APP_CONTROL_UDP_PORT = 9099;
        public static final int SERVER_APP_TOTAL_PORT = 9700;
	
	// Lock ports
	public static final int CLIENT_APP_LOCK_PORT = 9095;
	public static final int CONTROL_APP_LOCK_PORT = 9096;
	public static final int SERVER_APP_LOCK_PORT = 9097;
	
	// Database info
	public static final String DATABASE_PROTOCOL = "jdbc:derby://";
	public static final String SERVER_APP_DATABASE_NAME = "ServerAppDB";
	public static final int CLIENT_APP_DATABASE_SERVER_PORT = 1527;
	public static final String CLIENT_APP_DATABASE_SERVER_HOST = "0.0.0.0";
	public static final int SERVER_APP_DATABASE_SERVER_PORT = 9092;
	
	// Database synchronization
	public static final int LOCAL_SERVER_DIFF_SYNC_DELAY_SECONDS = 5;
	public static final int LOCAL_SERVER_DIFF_SYNC_MAX_ROWS = 150;
	public static final int LOCAL_SERVER_DIFF_SYNC_MAX_DIALOG_ROWS = 100;
	
	// Staff values
	public static final int STAFF_RIGHTS_OWNER = 0;
	public static final int STAFF_RIGHTS_MANAGER = 1;
	public static final int STAFF_RIGHTS_EMPLOYEE = 2;
	public static final int STAFF_RIGHTS_STUDENT = 3;
	public static final int STAFF_RIGHTS_ADMIN = 4;
	public static final int STAFF_RIGHTS_WAREHOUSE_ARTICLES = 0;
	public static final int STAFF_RIGHTS_WAREHOUSE_TRADINGGOODS = 1;
	public static final int STAFF_RIGHTS_WAREHOUSE_SERVICES = 2;
	public static final int STAFF_RIGHTS_WAREHOUSE_PREDEFINEDVALUES = 3;
	public static final int STAFF_RIGHTS_WAREHOUSE_WAREHOUSECARD = 4;
	public static final int STAFF_RIGHTS_WAREHOUSE_STOCKTAKING = 5;
	public static final int STAFF_RIGHTS_CASHREGISTER_TOTAL = 6;
	public static final int STAFF_RIGHTS_CASHREGISTER_SALDO = 7;
	public static final int STAFF_RIGHTS_CASHREGISTER_DISCOUTS = 8;
	public static final int STAFF_RIGHTS_CASHREGISTER_TABLEDELETE = 9;
	public static final int STAFF_RIGHTS_REPORTS_INVOICE = 10;
	public static final int STAFF_RIGHTS_REPORTS_TOTAL = 11;
	public static final int STAFF_RIGHTS_REPORTS_TRUSTCARD = 12;
	public static final int STAFF_RIGHTS_REPORTS_OFFER = 13;
	public static final int STAFF_RIGHTS_REPORTS_CONSUMPTIONTAX = 14;
	public static final int STAFF_RIGHTS_REPORTS_WORKTIME = 15;
	public static final int STAFF_RIGHTS_STAFF = 16;
	public static final int STAFF_RIGHTS_RECEIPTS = 17;
	public static final int STAFF_RIGHTS_CLIENTS = 18;
	public static final int STAFF_RIGHTS_CLIENTSSUPLIERS_FILES = 19;
	public static final int STAFF_RIGHTS_SUPPLIERS = 20;
	public static final int STAFF_RIGHTS_SETTINGS_PRINTER = 21;
	public static final int STAFF_RIGHTS_SETTINGS_CASHREGISTER = 22;
	public static final int STAFF_RIGHTS_SETTINGS_PAYMENTMETHODS = 23;
	public static final int STAFF_RIGHTS_SETTINGS_DISCOUNTS = 24;
	public static final int STAFF_RIGHTS_SETTINGS_WORKTIMES = 25;
	public static final int STAFF_RIGHTS_TRANSFERS = 26;
	public static final int STAFF_RIGHTS_CASHREGISTER_INVOICE_CANCELATION = 27;
	public static final int STAFF_RIGHTS_CASHREGISTER_OFFER = 28;
	public static final int STAFF_RIGHTS_CASHREGISTER_DELETE_ITEM = 29;
	public static final int STAFF_RIGHTS_REPORTS_TOTAL_PLUS = 30;
	public static final int STAFF_RIGHTS_TOTAL_LENGTH = 31;
	
	// Licences
	public static final int LICENCE_TYPE_CLIENT = 0;
	public static final int LICENCE_TYPE_LOCAL_SERVER = 1;
	public static final int LICENCE_TYPE_MASTER_LOCAL_SERVER = 2;
	public static final int LICENCE_QUERY_ACTIVATE = 0;
	public static final int LICENCE_QUERY_REFRESH = 1;
	public static final String PATH_PUBLIC_KEY = "licence/pk.licence";
	public static final String PATH_LICENCE = "licence/licence.licence";
	public static final String PATH_ACTIVATION_KEY = "licence/key.licence";
	public static final String PATH_UNIQUE_COMPUTER_ID = "licence/id.licence";
	public static final int LICENCE_ERROR_CODE_ACTIVATION_SUCCESS = 0;
	public static final int LICENCE_ERROR_CODE_ALREADY_ACTIVE = 1;
	public static final int LICENCE_ERROR_CODE_WRONG_CODE = 2;
	public static final int LICENCE_ERROR_CODE_REFRESH_SUCCESS = 3;
	public static final int LICENCE_ERROR_CODE_ACTIVATION_FAILED = 4;
	public static final int LICENCE_ERROR_CODE_REFRESH_FAILED = 5;
	public static final String LICENCE_SPLIT_STRING = "%%%";
	
	// Timeout
	public static final int TIMEOUT_SELECT_QUERY_SECONDS = 10;
	public static final int TIMEOUT_UPDATE_QUERY_SECONDS = 5;

	// UI values
	public static final int TABLE_COLUMN_HEIGHT = 20;
	
	// Response error codes
	public static final int RESPONSE_ERROR_CODE_SUCCESS = 0;
	public static final int RESPONSE_ERROR_CODE_CONNECTION_FAILED = 1;
	public static final int RESPONSE_ERROR_CODE_SQL_QUERY_FAILED = 2;
	public static final int RESPONSE_ERROR_CODE_MASTER_NOT_SYNCED = 3;
	public static final int RESPONSE_ERROR_CODE_LOCAL_SERVER_NOT_SYNCED = 4;
	
	// Colors
	public static final Color LIGHT_RED = new Color(255, 220, 220);
	public static final Color LIGHT_GREEN = new Color(220, 255, 220);
	public static final Color TEXT_FIELD_NORMAL = new Color(170, 170, 170);
	public static final Color TEXT_FIELD_RED = new Color(255, 51, 51);
	public static final Color TEXT_FIELD_GREEN = new Color(100, 200, 100);
	
	// Client app - warehouse values
	public static final String TRADING_GOODS_MEASURING_UNIT = "Kom";
	public static final int MEASURING_UNITS_MAX_DEFAULT_ID = 10;
	public static final int CONSUMPTION_TAXES_MAX_DEFAULT_ID = 8;

	// Client app - payment method type values
	public static final int PAYMENT_METHOD_ANY_METHOD = -1;
	public static final int PAYMENT_METHOD_TYPE_OFFER = -2;
	public static final int PAYMENT_METHOD_TYPE_SUBTOTAL = -3;
	public static final String PAYMENT_METHOD_OFFER_NAME = "Ponuda";
	public static final String PAYMENT_METHOD_ISSUE_SLIP_PAID_NAME = "Izdatnica - plaćeno";
	public static final String PAYMENT_METHOD_SUBTOTAL_PAID_NAME = "Plaćeno - br. računa: ";
	public static final int PAYMENT_METHODS_MAX_DEFAULT_ID = 8;
	
	public static final int PAYMENT_METHOD_TYPE_CASH = 0;
	public static final int PAYMENT_METHOD_TYPE_CREDIT_CARD = 1;
	public static final int PAYMENT_METHOD_TYPE_CHECK = 2;
	public static final int PAYMENT_METHOD_TYPE_TRANSACTION_BILL = 3;
	public static final int PAYMENT_METHOD_TYPE_OTHER = 4;
	public static final int PAYMENT_METHOD_TYPE_OTHER_NOT_FISCALIZED = 5;
	public static final int PAYMENT_METHOD_TYPE_ISSUE_SLIP = 6;
	public static String[] PAYMENT_METHOD_TYPE_NAMES = new String[] {
		"Gotovina", "Kartice", "Ček", "Transakcijski račun", "Ostalo", "Ostalo - ne fiskalizira se", "Izdatnica"
	};
	
	// Settings
	public static enum AppSettingsEnum {
		SETTINGS_PASS_TOTALSALDO, SETTINGS_PASS_PRINTINVOICE, SETTINGS_PASS_SUBTOTAL, SETTINGS_PASS_SELECTTABLE_OWN, SETTINGS_PASS_SELECTTABLE_OTHER, 
		SETTINGS_BUTTON_SUBTOTAL, SETTINGS_BUTTON_SENDTOBAR, SETTINGS_BUTTON_SENDTOKITCHEN, SETTINGS_BUTTON_CHANGEPAYMENTMETHOD, SETTINGS_BUTTON_STAFFINVOICE, 
		SETTINGS_NOTES_INVOICE, SETTINGS_NOTES_INVOICE_AT_START, SETTINGS_NOTES_BAR, SETTINGS_NOTES_BAR_PRINT, SETTINGS_NOTES_KITCHEN, 
		SETTINGS_NOTES_KITCHEN_PRINT, SETTINGS_TOUCH, SETTINGS_QUICKCHOICE, SETTINGS_EVENT_PRICES, SETTINGS_AUTO_ARTICLEID, SETTINGS_AUTO_WORKTIME, 
		SETTINGS_TRUSTCARD, SETTINGS_AUTO_LOCK_CASHREGISTER, SETTINGS_AUTO_LOCK_TABLE, SETTINGS_AUTO_LOCK_CASHREGISTER_TIME, 
		SETTINGS_AUTO_LOCK_TABLE_TIME, SETTINGS_STAFF_DISCOUNT_AMOUNT, SETTINGS_BUTTON_SENDTOBAR_PRICE, SETTINGS_BUTTON_SENDTOKITCHEN_PRICE, 
		SETTINGS_BUTTON_ITEM_NOTE, SETTINGS_OVERTIME_WORK, SETTINGS_HEADER_POS_1, SETTINGS_HEADER_POS_2, 
		SETTINGS_HEADER_POS_3, SETTINGS_HEADER_POS_4, SETTINGS_HEADER_POS_5, SETTINGS_HEADER_POS_6, SETTINGS_HEADER_POS_7, SETTINGS_FOOTER_POS_1, 
		SETTINGS_FOOTER_POS_2, SETTINGS_FOOTER_POS_3, SETTINGS_HEADER_A4_1, SETTINGS_HEADER_A4_2, SETTINGS_HEADER_A4_3, SETTINGS_HEADER_A4_4, 
		SETTINGS_HEADER_A4_5, SETTINGS_HEADER_A4_6, SETTINGS_HEADER_A4_7, SETTINGS_FOOTER_A4_1, SETTINGS_FOOTER_A4_2, SETTINGS_FOOTER_A4_3, 
		SETTINGS_PRINTER_LOCAL_INVOICES, SETTINGS_PRINTER_LOCAL_BAR, SETTINGS_PRINTER_LOCAL_KITCHEN, SETTINGS_PRINTER_NETWORK_INVOICES, 
		SETTINGS_PRINTER_NETWORK_BAR, SETTINGS_PRINTER_NETWORK_KITCHEN, SETTINGS_PRINTER_TYPE_INVOICES, SETTINGS_PRINTER_TYPE_BAR, 
		SETTINGS_PRINTER_TYPE_KITCHEN, SETTINGS_PRINTER_ON_INVOICES, SETTINGS_PRINTER_ON_BAR, SETTINGS_PRINTER_ON_KITCHEN, SETTINGS_PRINTCOUNT_INVOICE, 
		SETTINGS_PRINTCOUNT_KITCHEN, SETTINGS_PRINTCOUNT_BAR, SETTINGS_PRINTCOUNT_SUBTOTAL, SETTINGS_PRINTER_AUTO_KITCHEN, 
		SETTINGS_PRINTER_AUTO_KITCHEN_SUBTOTAL, SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION, SETTINGS_FISCALISATION_WAITTIME, 
		SETTINGS_FISCALISATION_WAITTIME_REPEAT, SETTINGS_DISCOUNT_TYPE_PERCENTAGE, SETTINGS_DISCOUNT_TYPE_AMOUNT, SETTINGS_CASH_REGISTER_TABLES_COUNT, 
		
		SETTINGS_LAYOUT_GROUP_NAMES, 
		SETTINGS_LAYOUT_SUBGROUP_NAMES_0, SETTINGS_LAYOUT_SUBGROUP_NAMES_1, SETTINGS_LAYOUT_SUBGROUP_NAMES_2, SETTINGS_LAYOUT_SUBGROUP_NAMES_3, SETTINGS_LAYOUT_SUBGROUP_NAMES_4, 
		SETTINGS_LAYOUT_SUBGROUP_NAMES_5, SETTINGS_LAYOUT_SUBGROUP_NAMES_6, SETTINGS_LAYOUT_SUBGROUP_NAMES_7, SETTINGS_LAYOUT_SUBGROUP_NAMES_8, SETTINGS_LAYOUT_SUBGROUP_NAMES_9,
		SETTINGS_LAYOUT_ITEM_IDS_0, SETTINGS_LAYOUT_ITEM_IDS_1, SETTINGS_LAYOUT_ITEM_IDS_2, SETTINGS_LAYOUT_ITEM_IDS_3, SETTINGS_LAYOUT_ITEM_IDS_4, 
		SETTINGS_LAYOUT_ITEM_IDS_5, SETTINGS_LAYOUT_ITEM_IDS_6, SETTINGS_LAYOUT_ITEM_IDS_7, SETTINGS_LAYOUT_ITEM_IDS_8, SETTINGS_LAYOUT_ITEM_IDS_9,
		SETTINGS_LAYOUT_ITEM_TYPES_0, SETTINGS_LAYOUT_ITEM_TYPES_1, SETTINGS_LAYOUT_ITEM_TYPES_2, SETTINGS_LAYOUT_ITEM_TYPES_3, SETTINGS_LAYOUT_ITEM_TYPES_4, 
		SETTINGS_LAYOUT_ITEM_TYPES_5, SETTINGS_LAYOUT_ITEM_TYPES_6, SETTINGS_LAYOUT_ITEM_TYPES_7, SETTINGS_LAYOUT_ITEM_TYPES_8, SETTINGS_LAYOUT_ITEM_TYPES_9,
		SETTINGS_LAYOUT_ITEM_COLORS_0, SETTINGS_LAYOUT_ITEM_COLORS_1, SETTINGS_LAYOUT_ITEM_COLORS_2, SETTINGS_LAYOUT_ITEM_COLORS_3, SETTINGS_LAYOUT_ITEM_COLORS_4, 
		SETTINGS_LAYOUT_ITEM_COLORS_5, SETTINGS_LAYOUT_ITEM_COLORS_6, SETTINGS_LAYOUT_ITEM_COLORS_7, SETTINGS_LAYOUT_ITEM_COLORS_8, SETTINGS_LAYOUT_ITEM_COLORS_9,
		
		SETTINGS_CASH_REGISTER_DEPOSIT, SETTINGS_ADMIN_EXPIRY_NOTICE_DAYS, SETTINGS_ADMIN_EXPIRY_NOTICE_HOURS, SETTINGS_EMPTY_LINES_COUNT,
		
		SETTINGS_CASH_REGISTER_QUICK_PICK_ID, SETTINGS_CASH_REGISTER_QUICK_PICK_NAME, SETTINGS_CASH_REGISTER_QUICK_PICK_TYPE, SETTINGS_SHOW_CASH_RETURN_DIALOG,
		SETTINGS_ADMIN_DISABLE_INVOICE_CREATION, SETTINGS_CASH_REGISTER_SUBTOTAL_AUTODELETE, SETTING_ADMIN_SERVER_STATUS_NOTIFICATION,
                SETTINGS_PASS_SELECTTABLE, SETTINGS_PASS_ESCAPEBUTTON, SETTINGS_PASS_CANCELBUTTON
	}
	
	public static final AppSettingsEnum[] SETTINGS_LAYOUT_SUBGROUP_NAMES = new AppSettingsEnum[]{
		AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_0, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_1, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_2, 
		AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_3, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_4, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_5,
		AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_6, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_7, AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_8,
		AppSettingsEnum.SETTINGS_LAYOUT_SUBGROUP_NAMES_9
	};
	public static final AppSettingsEnum[] SETTINGS_LAYOUT_ITEM_IDS = new AppSettingsEnum[]{
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_0, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_1, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_2, 
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_3, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_4, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_5,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_6, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_7, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_8,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_IDS_9
	};
	public static final AppSettingsEnum[] SETTINGS_LAYOUT_ITEM_TYPES = new AppSettingsEnum[]{
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_0, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_1, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_2, 
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_3, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_4, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_5,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_6, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_7, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_8,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_TYPES_9
	};
	public static final AppSettingsEnum[] SETTINGS_LAYOUT_ITEM_COLORS = new AppSettingsEnum[]{
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_0, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_1, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_2, 
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_3, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_4, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_5,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_6, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_7, AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_8,
		AppSettingsEnum.SETTINGS_LAYOUT_ITEM_COLORS_9
	};
	
	public static final String SETTINGS_LAYOUT_SPLIT_STRING = "%";

	// Settings - Touch layout
	public static final int SETTINGS_LAYOUT_ITEM_TYPE_ARTICLE = 0;
	public static final int SETTINGS_LAYOUT_ITEM_TYPE_TRADINGGOODS = 1;
	public static final int SETTINGS_LAYOUT_ITEM_TYPE_SERVICE = 2;
	public static final int SETTINGS_LAYOUT_COLOR_OFFSET = 5;

	// Settings - Discounts
	public static final int SETTINGS_DISCOUNT_TYPE_PERCENTAGE = 0;
	public static final int SETTINGS_DISCOUNT_TYPE_AMOUNT = 1;
	
	// Settings - Layout
	public static final int SLCA = 200;
	public static final Color[] SETTINGS_LAYOUT_COLORS = new Color[]{
		new Color(187, 222, 251, SLCA), new Color(255, 205, 210, SLCA), new Color(251, 192, 45, SLCA), new Color(215, 204, 200, SLCA),
		new Color(200, 230, 201, SLCA), new Color(139, 175, 94, SLCA), new Color(128, 203, 196, SLCA), new Color(0, 188, 212, SLCA),
		new Color(176, 190, 197  , SLCA), new Color(206, 147, 216, SLCA), new Color(244, 143, 177, SLCA), new Color(244, 67, 54, SLCA),
	};
	
	// Settings - Printer
	public static final int POS_PRINTER_TYPE_INVOICE = 0;
	public static final int POS_PRINTER_TYPE_BAR = 1;
	public static final int POS_PRINTER_TYPE_KITCHEN = 2;
	
	// Print values
	public static final int PRINT_A4_TABLE_ROW_HEIGHT = 18;
	public static final Color PRINT_A4_TABLE_HEADER_BACKGROUND_COLOR = new Color(220, 240, 255);
	
	// Fiscalization
	public static final String DEFAULT_ZKI = "--------------------------------";
	public static final String DEFAULT_JIR = "Ovo nije fiskalizirani račun        ";
	
	// Certificates
	public static final String CERT_DEMO_ROOT_ALIAS = "DemoRootCA";
	public static final String CERT_DEMO_SUB_ALIAS = "DemoSubCA";
	public static final String CERT_PROD_ROOT_ALIAS = "ProdRootCA";
	public static final String CERT_PROD_SUB_ALIAS = "ProdSubCA";
	public static final String CERT_PRIVATE_ALIAS = "PrivateCert";
	public static final int CERT_TYPE_PROD = 0;
	public static final int CERT_TYPE_TEST = 1;
	public static final int CERT_TYPE_EINVOICE_PROD = 2;
	public static final int CERT_TYPE_EINVOICE_TEST = 3;
	
	// Fake query ids
	public static final int FAKE_QUERY_ID_SYNC = -1;
	public static final int FAKE_QUERY_ID_REGISTER_OFFICE = -2;
	public static final int FAKE_QUERY_ID_REGISTER_CASH_REGISTER = -3;
	
	// Local values keys
	public static final String LOCAL_VALUE_CONTACT_EMAIL = "contactEmail";
	public static final String LOCAL_VALUE_NOTIFICATION_TIME = "notificationTime";
	public static final String LOCAL_VALUE_NOTIFICATION_TIME_2 = "notificationTime2";
	
	// Reports - colors
	public static final Color normalForeground = Color.BLACK;
	public static final Color normalBackground = Color.WHITE;
	public static final Color normalForegroundSelected = Color.WHITE;
	public static final Color normalBackgroundSelected = new Color(0, 120, 215);
	public static final Color redForeground = Color.RED;
	public static final Color redBackground = Color.WHITE;
	public static final Color redForegroundSelected = Color.WHITE;
	public static final Color redBackgroundSelected = Color.RED;
	public static final Color orangeForeground =  new Color(244, 134, 66);
	public static final Color orangeBackground = Color.WHITE;
	public static final Color orangeForegroundSelected = Color.WHITE;
	public static final Color orangeBackgroundSelected = new Color(244, 134, 66);
	public static final Color grayForeground = Color.GRAY;
	public static final Color grayBackground = Color.WHITE;
	public static final Color grayForegroundSelected = Color.WHITE;
	public static final Color grayBackgroundSelected = Color.GRAY;
}
