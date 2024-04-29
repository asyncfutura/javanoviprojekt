/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.fiscalization;

import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.ui.settings.ClientAppSettingsEinvoiceDialog;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.certificates.CertificateManager;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.Licence;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JDialog;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Matej
 */
public class ElectronicInvoice {
	
	public static final String KEYSTORE_NAME = "keystore.jks";
	public static final String KEYSTORE_PASS = "password";
	
	//public static final String E_INVOICE_URL = "https://digitalneusluge.fina.hr/eRacunB2B";	
	//public static final String E_INVOICE_URL = "https://eposlovanje.fina.hr/e-racunB2B";
	//public static final String E_INVOICE_URL = "https://webservisi.fina.hr/e-racunB2B";
	//public static final String E_INVOICE_URL = "https://webservisi.fina.hr/SendB2BOutgoingInvoicePKIWebService/services/SendB2BOutgoingInvoicePKIWebService";
	//public static final String E_INVOICE_URL = "https://prez.fina.hr/B2BFinaInvoiceWebService/services/B2BFinaInvoiceWebService";
	
	public static final String E_INVOICE_URL_TEST = "https://prez.fina.hr/SendB2BOutgoingInvoicePKIWebService/services/SendB2BOutgoingInvoicePKIWebService";
	public static final String E_INVOICE_URL_PROD = "https://eposlovanje.fina.hr/SendB2BOutgoingInvoicePKIWebService/services/SendB2BOutgoingInvoicePKIWebService";
	
	public static final String E_INVOICE_PREFIX = "http://fina.hr/eracun/b2b";
	
	public static String UploadInvoice(Invoice invoice, String invoiceAttachment){
		LoadCertificates(!invoice.isTest);
		boolean success = false;
		try {
			String nameSpace_iwsc = "iwsc";
			String nameSpace_xsd = "xsd";
			String nameSpace_soi = "soi";
			String nameSpace_wsee = "wsee";
			String nameSpace_wsu = "wsu";
			String nameSpace_iwsc_URI = E_INVOICE_PREFIX + "/invoicewebservicecomponents/v0.1";
			String nameSpace_xsd_URI = "http://www.w3.org/2001/XMLSchema";
			String nameSpace_soi_URI = E_INVOICE_PREFIX + "/pki/SendB2BOutgoingInvoice/v0.1";
			String nameSpace_wsee_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
			String nameSpace_wsu_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(nameSpace_iwsc, nameSpace_iwsc_URI);
			envelope.addNamespaceDeclaration(nameSpace_xsd, nameSpace_xsd_URI);
			envelope.addNamespaceDeclaration(nameSpace_soi, nameSpace_soi_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsee, nameSpace_wsee_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsu, nameSpace_wsu_URI);
			soapMessage.getMimeHeaders().addHeader("SOAPAction", E_INVOICE_PREFIX + "/SendB2BOutgoingInvoice");

			// Header
			SOAPHeader soapHeader = envelope.getHeader();
			SOAPElement securityElem = soapHeader.addChildElement("Security", nameSpace_wsee);
			securityElem.addAttribute(envelope.createName("SOAP-ENV:mustUnderstand"), "1");
			
			SOAPElement timestampElem = securityElem.addChildElement("Timestamp", nameSpace_wsu);
			timestampElem.addAttribute(envelope.createName("Id"), "id_timestampElem").setIdAttribute("Id", true);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			timestampElem.addChildElement("Created", nameSpace_wsu).addTextNode(sdf.format(new Date()));
			timestampElem.addChildElement("Expires", nameSpace_wsu).addTextNode(sdf.format(new Date(new Date().getTime() + 1000 * 60 * 5)));
			
			byte[] certByte = GetPrivateCertificate(false).getEncoded();
			SOAPElement binarySecurityToken = securityElem.addChildElement("BinarySecurityToken", nameSpace_wsee);
			binarySecurityToken.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			binarySecurityToken.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
			binarySecurityToken.setAttribute("wsu:Id", "X509Token");
			binarySecurityToken.addTextNode(Base64.getEncoder().encodeToString(certByte));
			
			// Body
			SOAPBody soapBody = envelope.getBody();
			soapBody.addAttribute(envelope.createName("Id"), "id_soapBody").setIdAttribute("Id", true);
			SOAPElement sendB2BOutgoingInvoiceMsgElem = soapBody.addChildElement("SendB2BOutgoingInvoiceMsg", nameSpace_soi);
			
			// Body header supplier
			SOAPElement headerSupplierElem = sendB2BOutgoingInvoiceMsgElem.addChildElement("HeaderSupplier", nameSpace_iwsc);
			headerSupplierElem.addChildElement("MessageID", nameSpace_iwsc).addTextNode(UUID.randomUUID().toString());
			headerSupplierElem.addChildElement("SupplierID", nameSpace_iwsc).addTextNode("9934:" + GetFiscOib(!invoice.isTest));
			//headerSupplierElem.addChildElement("AdditionalSupplierID", nameSpace_iwsc).addTextNode("");
			headerSupplierElem.addChildElement("MessageType", nameSpace_iwsc).addTextNode("9001");
			//headerSupplierElem.addChildElement("MessageAttributes", nameSpace_iwsc).addTextNode("");
			
			// Outgoing invoice data elem
			SOAPElement dataElem = sendB2BOutgoingInvoiceMsgElem.addChildElement("Data", nameSpace_soi);
			SOAPElement B2BOutgoingInvoiceEnvelopeElem = dataElem.addChildElement("B2BOutgoingInvoiceEnvelope", nameSpace_soi);
			B2BOutgoingInvoiceEnvelopeElem.addChildElement("XMLStandard", nameSpace_soi).addTextNode("UBL");
			B2BOutgoingInvoiceEnvelopeElem.addChildElement("SpecificationIdentifier", nameSpace_soi).addTextNode("urn:cen.eu:en16931:2017");
			B2BOutgoingInvoiceEnvelopeElem.addChildElement("SupplierInvoiceID", nameSpace_soi).addTextNode(invoice.invoiceNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber);
			B2BOutgoingInvoiceEnvelopeElem.addChildElement("BuyerID", nameSpace_soi).addTextNode("9934:" + invoice.clientOIB);
			//B2BOutgoingInvoiceEnvelopeElem.addChildElement("AdditionalBuyerID", nameSpace_soi).addTextNode("");
			B2BOutgoingInvoiceEnvelopeElem.addChildElement("InvoiceEnvelope", nameSpace_soi).addTextNode(GetInvoiceEncoded(invoice, invoiceAttachment));
			
			// Save message
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			// Sign the message
			SignMessage(securityElem, new String[]{"#id_timestampElem", "#id_soapBody"}, !invoice.isTest);
			soapMessage.saveChanges();
			
			// Key info
			Iterator i = securityElem.getChildElements();
			i.next();
			i.next();
			SOAPElement signatureElem = (SOAPElement) i.next();
			SOAPElement keyInfoElem = signatureElem.addChildElement("KeyInfo", "ds");
			SOAPElement securityTokenReference = keyInfoElem.addChildElement("SecurityTokenReference", nameSpace_wsee);
			SOAPElement securityTokenReferenceUri = securityTokenReference.addChildElement("Reference", nameSpace_wsee);
			securityTokenReferenceUri.setAttribute("URI", "#X509Token");
			securityTokenReferenceUri.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			soapMessage.saveChanges();
			
			//System.out.println("Request SOAP Message:");
			//soapMessage.writeTo(System.out);
			//System.out.println("\n\n");
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLSent(soapMessage, "ER-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
			
			/*final int timeout;
			if (isNow){
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			} else {
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT.ordinal());
			}*/
			final int timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			URL endpoint = new URL(new URL(invoice.isTest ? E_INVOICE_URL_TEST : E_INVOICE_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			
			//System.out.println("Response SOAP Message:");
			//soapResponse.writeTo(System.out);
			//System.out.println();
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLReceived(soapResponse, "ER-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
			boolean invoiceExists = false;
			NodeList nodeList = soapResponse.getSOAPBody().getElementsByTagName("iwsc:AckStatusText");
			if (nodeList.getLength() == 1){
				if (nodeList.item(0).getTextContent().contains("Broj računa postoji za pretinac pošiljatelja u tekućoj godini")){
					ClientAppLogger.GetInstance().ShowMessage(nodeList.item(0).getTextContent());
					invoiceExists = true;
				}
			}
			
			if(!invoiceExists){
				nodeList = soapResponse.getSOAPBody().getElementsByTagNameNS("http://fina.hr/eracun/b2b/pki/SendB2BOutgoingInvoice/v0.1", "CorrectB2BOutgoingInvoice");
				if (nodeList.getLength() == 1){
					nodeList = soapResponse.getSOAPBody().getElementsByTagNameNS("http://fina.hr/eracun/b2b/pki/SendB2BOutgoingInvoice/v0.1", "InvoiceID");
						if (nodeList.getLength() == 1){
							success = true;
							invoice.einvoiceId = nodeList.item(0).getTextContent();
						}
				}
			}
			
            soapConnection.close();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		if (success){
			SaveEInvoiceId(invoice);
		}
		
		return invoice.einvoiceId;
	}

	public static String GetInvoiceEncoded(Invoice invoice, String invoiceAttachment){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			// Invoice element
			Element invoiceElement = doc.createElement("Invoice");
			doc.appendChild(invoiceElement);
			invoiceElement.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
			invoiceElement.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
			invoiceElement.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
			invoiceElement.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
			invoiceElement.setAttribute("xmlns:sac", "urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
			invoiceElement.setAttribute("xmlns:sbc", "urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");
			invoiceElement.setAttribute("xmlns:sig", "urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
			
			// Invoice elements
			AddChildNodeWithText(doc, invoiceElement, "cbc:CustomizationID", "urn:cen.eu:en16931:2017");
			AddChildNodeWithText(doc, invoiceElement, "cbc:ID", invoice.invoiceNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber);
			AddChildNodeWithText(doc, invoiceElement, "cbc:IssueDate", new SimpleDateFormat("yyyy-MM-dd").format(invoice.date));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(invoice.date);
			calendar.add(Calendar.DATE, invoice.paymentDelay);
			AddChildNodeWithText(doc, invoiceElement, "cbc:DueDate", new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
			AddChildNodeWithText(doc, invoiceElement, "cbc:InvoiceTypeCode", "380");
			AddChildNodeWithText(doc, invoiceElement, "cbc:DocumentCurrencyCode", "HRK");
			
			// Attachments
			if(!"".equals(invoiceAttachment)){
				Element additionalDocumentReferenceElement = AddChildNode(doc, invoiceElement, "cac:AdditionalDocumentReference");
				AddChildNodeWithText(doc, additionalDocumentReferenceElement, "cbc:ID", "prilog1");
				Element attachmentElement = AddChildNode(doc, additionalDocumentReferenceElement, "cac:Attachment");
				Element embeddedDocumentBinaryObjectElement = AddChildNodeWithTextAndAttribute(doc, attachmentElement, "cbc:EmbeddedDocumentBinaryObject", invoiceAttachment, "mimeCode", "image/jpeg");
				embeddedDocumentBinaryObjectElement.setAttribute("filename", "prilog1.jpg");
			}
			
			// Signature
			Element signatureElement = AddChildNode(doc, invoiceElement, "cac:Signature");
			AddChildNodeWithText(doc, signatureElement, "cbc:ID", "urn:oasis:names:specification:ubl:signatures");
			AddChildNodeWithText(doc, signatureElement, "cbc:SignatureMethod", "urn:oasis:names:specification:ubl:profile:dsig:signature");
			Element signaturePartyElement = AddChildNode(doc, signatureElement, "cac:SignatoryParty");
			Element signaturePartyIDElement = AddChildNode(doc, signaturePartyElement, "cac:PartyIdentification");
			AddChildNodeWithText(doc, signaturePartyIDElement, "cbc:ID", "MyParty");
			
			// Accounting supplier party
			Element supplierElement = AddChildNode(doc, invoiceElement, "cac:AccountingSupplierParty");
			{
				String supplierOib = GetFiscOib(!invoice.isTest);
				Element partyElement = AddChildNode(doc, supplierElement, "cac:Party");
				AddChildNodeWithTextAndAttribute(doc, partyElement, "cbc:EndpointID", supplierOib, "schemeID", "9934");
				Element partyIdentificationElement = AddChildNode(doc, partyElement, "cac:PartyIdentification");
				AddChildNodeWithText(doc, partyIdentificationElement, "cbc:ID", "9934:" + supplierOib);
				Element postalAddressElement = AddChildNode(doc, partyElement, "cac:PostalAddress");
				AddChildNodeWithText(doc, postalAddressElement, "cbc:StreetName", Licence.GetCompanyAddress());
				Element countryElement = AddChildNode(doc, postalAddressElement, "cac:Country");
				AddChildNodeWithText(doc, countryElement, "cbc:IdentificationCode", "HR");
				
				Element partyTaxScheme = AddChildNode(doc, partyElement, "cac:PartyTaxScheme");
				AddChildNodeWithText(doc, partyTaxScheme, "cbc:CompanyID", "HR" + supplierOib);
				Element taxScheme = AddChildNode(doc, partyTaxScheme, "cac:TaxScheme");
				AddChildNodeWithText(doc, taxScheme, "cbc:ID", "VAT");
				
				Element PartyLegalEntity = AddChildNode(doc, partyElement, "cac:PartyLegalEntity");
				AddChildNodeWithText(doc, PartyLegalEntity, "cbc:RegistrationName", Licence.GetCompanyName());
				
				//  TODO Contact
			}
			
			// Accounting customer party
			Element customerElement = AddChildNode(doc, invoiceElement, "cac:AccountingCustomerParty");
			{
				String customerOib = invoice.clientOIB;
				Element partyElement = AddChildNode(doc, customerElement, "cac:Party");
				AddChildNodeWithTextAndAttribute(doc, partyElement, "cbc:EndpointID", customerOib, "schemeID", "9934");
				Element partyIdentificationElement = AddChildNode(doc, partyElement, "cac:PartyIdentification");
				AddChildNodeWithText(doc, partyIdentificationElement, "cbc:ID", "9934:" + customerOib);
				Element postalAddressElement = AddChildNode(doc, partyElement, "cac:PostalAddress");
				// TODO uncomment next line with correct address
				//AddChildNodeWithText(doc, postalAddressElement, "cbc:StreetName", Licence.GetCompanyAddress());
				Element countryElement = AddChildNode(doc, postalAddressElement, "cac:Country");
				AddChildNodeWithText(doc, countryElement, "cbc:IdentificationCode", "HR");
				
				Element partyTaxScheme = AddChildNode(doc, partyElement, "cac:PartyTaxScheme");
				AddChildNodeWithText(doc, partyTaxScheme, "cbc:CompanyID", "HR" + customerOib);
				Element taxScheme = AddChildNode(doc, partyTaxScheme, "cac:TaxScheme");
				AddChildNodeWithText(doc, taxScheme, "cbc:ID", "VAT");
				
				Element PartyLegalEntity = AddChildNode(doc, partyElement, "cac:PartyLegalEntity");
				AddChildNodeWithText(doc, PartyLegalEntity, "cbc:RegistrationName", invoice.clientName);
				
				//  TODO Contact
			}
			
			// Delivery
			Element deliveryElement = AddChildNode(doc, invoiceElement, "cac:Delivery");
			Element deliveryLocationElement = AddChildNode(doc, deliveryElement, "cac:DeliveryLocation");
			Element addressElement = AddChildNode(doc, deliveryLocationElement, "cac:Address");
			Element countryElement = AddChildNode(doc, addressElement, "cac:Country");
			AddChildNodeWithText(doc, countryElement, "cbc:IdentificationCode", "HR");
			
			// Payment means
			Element paymentMeansElement = AddChildNode(doc, invoiceElement, "cac:PaymentMeans");
			AddChildNodeWithText(doc, paymentMeansElement, "cbc:PaymentMeansCode", GetPaymentMethodCode(invoice.paymentMethodType));
			AddChildNodeWithText(doc, paymentMeansElement, "cbc:InstructionNote", invoice.paymentMethodName);
			if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_CASH){
				Element payeeAccountElement = AddChildNode(doc, paymentMeansElement, "cac:PayeeFinancialAccount");
				AddChildNodeWithText(doc, payeeAccountElement, "cbc:ID", ClientAppSettingsEinvoiceDialog.GetIBAN());
			}
			
			// Taxes calculation
			InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			float taxSum = 0f;
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				taxSum += invoiceTaxes.taxAmounts.get(i);
			}
			
			// Invoice discount
			float totalPrice = ClientAppUtils.FloatToPriceFloat(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			float discountAmountNet = invoice.totalPrice - totalPrice - (invoice.totalPrice - totalPrice) * (taxSum / totalPrice);
			if(discountAmountNet != 0f){
				for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
					double discountAmountNetThisRate = discountAmountNet * (invoiceTaxes.taxAmounts.get(i) / taxSum);
					
					Element allowanceChargeElement = AddChildNode(doc, invoiceElement, "cac:AllowanceCharge");
					AddChildNodeWithText(doc, allowanceChargeElement, "cbc:ChargeIndicator", "false");
					AddChildNodeWithText(doc, allowanceChargeElement, "cbc:AllowanceChargeReason", "Popust");
					AddChildNodeWithTextAndAttribute(doc, allowanceChargeElement, "cbc:Amount", ClientAppUtils.DoubleToPriceString(discountAmountNetThisRate), "currencyID", "HRK");

					Element taxCategoryElement = AddChildNode(doc, allowanceChargeElement, "cac:TaxCategory");
					if(taxSum == 0f){
						AddChildNodeWithText(doc, taxCategoryElement, "cbc:ID", "E"); // TODO it is possible that "O" goes here
					} else {
						AddChildNodeWithText(doc, taxCategoryElement, "cbc:ID", "S");
					}
					AddChildNodeWithText(doc, taxCategoryElement, "cbc:Percent", ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
					Element taxSchemeElement = AddChildNode(doc, taxCategoryElement, "cac:TaxScheme");
					AddChildNodeWithText(doc, taxSchemeElement, "cbc:ID", "VAT");
				}
			}
			
			// Taxes
			Element taxTotalElement = AddChildNode(doc, invoiceElement, "cac:TaxTotal");
			AddChildNodeWithTextAndAttribute(doc, taxTotalElement, "cbc:TaxAmount", ClientAppUtils.FloatToPriceString(taxSum), "currencyID", "HRK");
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				Element taxSubtotalElement = AddChildNode(doc, taxTotalElement, "cac:TaxSubtotal");
				AddChildNodeWithTextAndAttribute(doc, taxSubtotalElement, "cbc:TaxableAmount", ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)), "currencyID", "HRK");
				AddChildNodeWithTextAndAttribute(doc, taxSubtotalElement, "cbc:TaxAmount", ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)), "currencyID", "HRK");
				Element taxCategoryElement = AddChildNode(doc, taxSubtotalElement, "cac:TaxCategory");
				if(invoiceTaxes.taxRates.get(i) == 0f){
					AddChildNodeWithText(doc, taxCategoryElement, "cbc:ID", "E"); // TODO it is possible that "O" goes here
				} else {
					AddChildNodeWithText(doc, taxCategoryElement, "cbc:ID", "S");
				}
				AddChildNodeWithText(doc, taxCategoryElement, "cbc:Percent", ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				Element taxSchemeElement = AddChildNode(doc, taxCategoryElement, "cac:TaxScheme");
				AddChildNodeWithText(doc, taxSchemeElement, "cbc:ID", "VAT");
			}
			
			// Legal monetary total
			Element legalMonetaryTotalElement = AddChildNode(doc, invoiceElement, "cac:LegalMonetaryTotal");
			AddChildNodeWithTextAndAttribute(doc, legalMonetaryTotalElement, "cbc:LineExtensionAmount", ClientAppUtils.FloatToPriceString(totalPrice - taxSum + discountAmountNet), "currencyID", "HRK");
			AddChildNodeWithTextAndAttribute(doc, legalMonetaryTotalElement, "cbc:TaxExclusiveAmount", ClientAppUtils.FloatToPriceString(totalPrice - taxSum), "currencyID", "HRK");
			AddChildNodeWithTextAndAttribute(doc, legalMonetaryTotalElement, "cbc:TaxInclusiveAmount", ClientAppUtils.FloatToPriceString(totalPrice), "currencyID", "HRK");
			if(discountAmountNet != 0f){
				AddChildNodeWithTextAndAttribute(doc, legalMonetaryTotalElement, "cbc:AllowanceTotalAmount", ClientAppUtils.FloatToPriceString(discountAmountNet), "currencyID", "HRK");
			}
			AddChildNodeWithTextAndAttribute(doc, legalMonetaryTotalElement, "cbc:PayableAmount", ClientAppUtils.FloatToPriceString(totalPrice), "currencyID", "HRK");
			
			// Invoice lines
			for (int i = 0; i < invoice.items.size(); ++i){
				float costOriginalWithVat = invoice.items.get(i).itemPrice;
				float costOriginal = 100f * costOriginalWithVat / (invoice.items.get(i).taxRate + invoice.items.get(i).consumptionTaxRate + 100f);
				
				// Cost original rounding
				float costOriginalDiff = ClientAppUtils.FloatToPriceFloat(costOriginalWithVat) - (ClientAppUtils.FloatToPriceFloat(costOriginal) + ClientAppUtils.FloatToPriceFloat(costOriginal * invoice.items.get(i).taxRate / 100f) + ClientAppUtils.FloatToPriceFloat(costOriginal * invoice.items.get(i).consumptionTaxRate / 100f));
				if(costOriginalDiff != 0f){
					costOriginal += costOriginalDiff;
				}
				
				float costWithDiscount = costOriginal;
				float discountParam = 0f;
				if(invoice.items.get(i).discountPercentage != 0f){
					costWithDiscount = costOriginal - costOriginal * invoice.items.get(i).discountPercentage / 100f;
					discountParam = invoice.items.get(i).discountPercentage / 100f;
				} else if(invoice.items.get(i).discountValue != 0f){
					costWithDiscount = costOriginal - invoice.items.get(i).discountValue;
					discountParam = 1f - costWithDiscount / costOriginal;
				}
				
				Element invoiceLineElement = AddChildNode(doc, invoiceElement, "cac:InvoiceLine");
				AddChildNodeWithText(doc, invoiceLineElement, "cbc:ID", "" + (i+1));
				AddChildNodeWithTextAndAttribute(doc, invoiceLineElement, "cbc:InvoicedQuantity", "" + invoice.items.get(i).itemAmount, "unitCode", "H87");
				AddChildNodeWithTextAndAttribute(doc, invoiceLineElement, "cbc:LineExtensionAmount", ClientAppUtils.FloatToPriceString(invoice.items.get(i).itemAmount * costWithDiscount), "currencyID", "HRK");
				
				Element itemElement = AddChildNode(doc, invoiceLineElement, "cac:Item");
				AddChildNodeWithText(doc, itemElement, "cbc:Name", invoice.items.get(i).itemName);
				
				// Tax category - VAT
				{
					Element classifiedTaxCategoryElement = AddChildNode(doc, itemElement, "cac:ClassifiedTaxCategory");
					if(invoice.items.get(i).taxRate == 0f){
						AddChildNodeWithText(doc, classifiedTaxCategoryElement, "cbc:ID", "E"); // TODO it is possible that "O" goes here
					} else {
						AddChildNodeWithText(doc, classifiedTaxCategoryElement, "cbc:ID", "S");
					}
					AddChildNodeWithText(doc, classifiedTaxCategoryElement, "cbc:Percent", ClientAppUtils.FloatToPriceString(invoice.items.get(i).taxRate));
					Element taxSchemeElement = AddChildNode(doc, classifiedTaxCategoryElement, "cac:TaxScheme");
					AddChildNodeWithText(doc, taxSchemeElement, "cbc:ID", "VAT");
				}
				
				// Tax category - PnP
				if(invoice.items.get(i).consumptionTaxRate != 0f){
					Element classifiedTaxCategoryElement = AddChildNode(doc, itemElement, "cac:ClassifiedTaxCategory");
					AddChildNodeWithText(doc, classifiedTaxCategoryElement, "cbc:ID", "S");
					AddChildNodeWithText(doc, classifiedTaxCategoryElement, "cbc:Percent", ClientAppUtils.FloatToPriceString(invoice.items.get(i).consumptionTaxRate));
					Element taxSchemeElement = AddChildNode(doc, classifiedTaxCategoryElement, "cac:TaxScheme");
					AddChildNodeWithText(doc, taxSchemeElement, "cbc:ID", "VAT");
				}
				
				Element priceElement = AddChildNode(doc, invoiceLineElement, "cac:Price");
				AddChildNodeWithTextAndAttribute(doc, priceElement, "cbc:PriceAmount", ClientAppUtils.FloatToPriceString(costWithDiscount), "currencyID", "HRK");
				AddChildNodeWithTextAndAttribute(doc, priceElement, "cbc:BaseQuantity", "1", "unitCode", "H87");
				if(discountParam != 0f){
					Element allowanceChargeElement = AddChildNode(doc, priceElement, "cac:AllowanceCharge");
					AddChildNodeWithText(doc, allowanceChargeElement, "cbc:ChargeIndicator", "false");
					AddChildNodeWithText(doc, allowanceChargeElement, "cbc:MultiplierFactorNumeric", ClientAppUtils.FloatToPriceString(discountParam));
					AddChildNodeWithTextAndAttribute(doc, allowanceChargeElement, "cbc:Amount", ClientAppUtils.FloatToPriceString(costOriginal - costWithDiscount), "currencyID", "HRK");
					AddChildNodeWithTextAndAttribute(doc, allowanceChargeElement, "cbc:BaseAmount", ClientAppUtils.FloatToPriceString(costOriginal), "currencyID", "HRK");
				}
			}
			
			// Sign message
			SignInvoice(invoiceElement, !invoice.isTest);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "no");
					
			// Output to file
			//transformer.transform(new DOMSource(doc), new StreamResult(new File("C:\\file.xml")));

			// Output to console for testing
			//transformer.transform(new DOMSource(doc), new StreamResult(System.out));
			//System.out.println(); System.out.println();
			
			// Output to string
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return Base64.getEncoder().encodeToString(writer.toString().getBytes());
			//return writer.toString();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
			
		return "";
	}
	
	private static void SaveEInvoiceId(Invoice invoice){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE LOCAL_INVOICES SET E_IN_ID = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
			if(invoice.isTest){
				query = query.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.executeLocally = true;
			databaseQuery.AddParam(1, invoice.einvoiceId);
			databaseQuery.AddParam(2, invoice.officeNumber);
			databaseQuery.AddParam(3, invoice.cashRegisterNumber);
			databaseQuery.AddParam(4, invoice.invoiceNumber);
			databaseQuery.AddParam(5, invoice.specialNumber);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
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
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE INVOICES SET E_IN_ID = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
			if(invoice.isTest){
				query = query.replace("INVOICES", "INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, invoice.einvoiceId);
			databaseQuery.AddParam(2, invoice.officeNumber);
			databaseQuery.AddParam(3, invoice.cashRegisterNumber);
			databaseQuery.AddParam(4, invoice.invoiceNumber);
			databaseQuery.AddParam(5, invoice.specialNumber);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
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
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	private static void SaveEInvoiceStatus(int officeNumber, int cashRegisterNumber, int invoiceNumber, int specialNumber, String year, String status, boolean isTest){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE LOCAL_INVOICES SET E_IN_ST = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ? AND YEAR(I_DATE) = ?";
			if(isTest){
				query = query.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.executeLocally = true;
			databaseQuery.AddParam(1, status);
			databaseQuery.AddParam(2, officeNumber);
			databaseQuery.AddParam(3, cashRegisterNumber);
			databaseQuery.AddParam(4, invoiceNumber);
			databaseQuery.AddParam(5, specialNumber);
			databaseQuery.AddParam(6, year);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
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
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
		
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE INVOICES SET E_IN_ST = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ? AND YEAR(I_DATE) = ?";
			if(isTest){
				query = query.replace("INVOICES", "INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, status);
			databaseQuery.AddParam(2, officeNumber);
			databaseQuery.AddParam(3, cashRegisterNumber);
			databaseQuery.AddParam(4, invoiceNumber);
			databaseQuery.AddParam(5, specialNumber);
			databaseQuery.AddParam(6, year);

			ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

			databaseQueryTask.execute();
			
			//loadingDialog.setVisible(true);
			while(!databaseQueryTask.isDone()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {}
			}
			
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
				} catch (InterruptedException | ExecutionException ex) {
					ClientAppLogger.GetInstance().ShowErrorLog(ex);
				}
			}
		}
	}
	
	private static void SignInvoice(Element element, boolean isProduction){
		try {
			Element extensionsElement = element.getOwnerDocument().createElement("ext:UBLExtensions");
			element.insertBefore(extensionsElement, element.getFirstChild());
			Element extensionElement = AddChildNode(element.getOwnerDocument(), extensionsElement, "ext:UBLExtension");
			AddChildNodeWithText(element.getOwnerDocument(), extensionElement, "ext:ExtensionURI", "urn:oasis:names:specification:ubl:profile:dsig:signature");
			Element extensionContentElement = AddChildNode(element.getOwnerDocument(), extensionElement, "ext:ExtensionContent");
			Element docSignaturesElement = AddChildNode(element.getOwnerDocument(), extensionContentElement, "sig:UBLDocumentSignatures");
			Element sigInfoElement = AddChildNode(element.getOwnerDocument(), docSignaturesElement, "sac:SignatureInformation");
			AddChildNodeWithText(element.getOwnerDocument(), sigInfoElement, "cbc:ID", "urn:oasis:names:specification:ubl:signatures:1");
			AddChildNodeWithText(element.getOwnerDocument(), sigInfoElement, "sbc:ReferencedSignatureID", "MyParty");
			
			XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance();
			List transformsList = new ArrayList();
			transformsList.add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null));
			transformsList.add(sigFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null));
			Transform transform = sigFactory.newTransform(CanonicalizationMethod.XPATH, new XPathFilterParameterSpec("/Invoice"));
			transformsList.add(transform);
			Reference ref = sigFactory.newReference("", sigFactory.newDigestMethod(DigestMethod.SHA256, null), transformsList, null, null);
			SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
				CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null), sigFactory
				.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
			
			X509Certificate cert = GetPrivateCertificate(isProduction);
			KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
			List x509Content = new ArrayList();
			x509Content.add(cert.getSubjectX500Principal().getName());
			x509Content.add(cert);
			//x509Content.add(kif.newX509IssuerSerial(cert.getIssuerX500Principal().getName(), cert.getSerialNumber()));
			X509Data xData = kif.newX509Data(x509Content);
			KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(xData));
			
			DOMSignContext sigContext = new DOMSignContext(GetPrivateKey(isProduction), sigInfoElement);
			sigContext.putNamespacePrefix(XMLSignature.XMLNS, "ds");
			XMLSignature sig = sigFactory.newXMLSignature(signedInfo, keyInfo);
			//XMLSignature sig = sigFactory.newXMLSignature(signedInfo, null);
			sig.sign(sigContext);
			
			//element.insertBefore(extensionsElement, element.getFirstChild());
		} catch (Exception ex){
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	private static Element AddChildNode(Document doc, Element parentElement, String key){
		Element newElement = doc.createElement(key);
		parentElement.appendChild(newElement);
		return newElement;
	}
	
	private static Element AddChildNodeWithText(Document doc, Element parentElement, String key, String value){
		Element newElement = doc.createElement(key);
		parentElement.appendChild(newElement);
		newElement.appendChild(doc.createTextNode(value));
		return newElement;
	}
	
	private static Element AddChildNodeWithTextAndAttribute(Document doc, Element parentElement, String key, String value, String attKey, String attValue){
		Element newElement = doc.createElement(key);
		parentElement.appendChild(newElement);
		newElement.appendChild(doc.createTextNode(value));
		newElement.setAttribute(attKey, attValue);
		return newElement;
	}
	
	public static String ElectronicInvoiceEcho(String message){
		LoadCertificates(false);
		
		try {
			String nameSpace_iwsc = "iwsc";
			String nameSpace_xsd = "xsd";
			String nameSpace_eb = "eb";
			String nameSpace_wsee = "wsee";
			String nameSpace_wsu = "wsu";
			String nameSpace_iwsc_URI = E_INVOICE_PREFIX + "/invoicewebservicecomponents/v0.1";
			String nameSpace_xsd_URI = "http://www.w3.org/2001/XMLSchema";
			String nameSpace_eb_URI = E_INVOICE_PREFIX + "/pki/Echo/v0.1";
			String nameSpace_wsee_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
			String nameSpace_wsu_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(nameSpace_iwsc, nameSpace_iwsc_URI);
			envelope.addNamespaceDeclaration(nameSpace_xsd, nameSpace_xsd_URI);
			envelope.addNamespaceDeclaration(nameSpace_eb, nameSpace_eb_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsee, nameSpace_wsee_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsu, nameSpace_wsu_URI);
			soapMessage.getMimeHeaders().addHeader("SOAPAction", E_INVOICE_PREFIX + "/Echo");
			
			// Header
			SOAPHeader soapHeader = envelope.getHeader();
			SOAPElement securityElem = soapHeader.addChildElement("Security", nameSpace_wsee);
			securityElem.addAttribute(envelope.createName("SOAP-ENV:mustUnderstand"), "1");
			
			SOAPElement timestampElem = securityElem.addChildElement("Timestamp", nameSpace_wsu);
			timestampElem.addAttribute(envelope.createName("Id"), "id_timestampElem").setIdAttribute("Id", true);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			timestampElem.addChildElement("Created", nameSpace_wsu).addTextNode(sdf.format(new Date()));
			timestampElem.addChildElement("Expires", nameSpace_wsu).addTextNode(sdf.format(new Date(new Date().getTime() + 1000 * 60 * 5)));
			
			byte[] certByte = GetPrivateCertificate(false).getEncoded();
			SOAPElement binarySecurityToken = securityElem.addChildElement("BinarySecurityToken", nameSpace_wsee);
			binarySecurityToken.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			binarySecurityToken.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
			binarySecurityToken.setAttribute("wsu:Id", "X509Token");
			binarySecurityToken.addTextNode(Base64.getEncoder().encodeToString(certByte));
			
			// Body
			SOAPBody soapBody = envelope.getBody();
			soapBody.addAttribute(envelope.createName("Id"), "id_soapBody").setIdAttribute("Id", true);
			
			SOAPElement echoMsgElem = soapBody.addChildElement("EchoMsg", nameSpace_eb);
			
			SOAPElement headerSupplierElem = echoMsgElem.addChildElement("HeaderSupplier", nameSpace_iwsc);
			headerSupplierElem.addChildElement("MessageID", nameSpace_iwsc).addTextNode(UUID.randomUUID().toString());
			headerSupplierElem.addChildElement("SupplierID", nameSpace_iwsc).addTextNode("9934:" + GetFiscOib(false));
			headerSupplierElem.addChildElement("AdditionalSupplierID", nameSpace_iwsc).addTextNode("");
			headerSupplierElem.addChildElement("MessageType", nameSpace_iwsc).addTextNode("9999");
			headerSupplierElem.addChildElement("MessageAttributes", nameSpace_iwsc).addTextNode("");
					
			SOAPElement dataElem = echoMsgElem.addChildElement("Data", nameSpace_eb);
			SOAPElement echoDataElem = dataElem.addChildElement("EchoData", nameSpace_eb);
			echoDataElem.addChildElement("Echo", nameSpace_eb).addTextNode(message);
			
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			SignMessage(securityElem, new String[]{"#id_timestampElem", "#id_soapBody"}, false);
			soapMessage.saveChanges();
			
			// Key info
			Iterator i = securityElem.getChildElements();
			i.next();
			i.next();
			SOAPElement signatureElem = (SOAPElement) i.next();
			SOAPElement keyInfoElem = signatureElem.addChildElement("KeyInfo", "ds");
			SOAPElement securityTokenReference = keyInfoElem.addChildElement("SecurityTokenReference", nameSpace_wsee);
			SOAPElement securityTokenReferenceUri = securityTokenReference.addChildElement("Reference", nameSpace_wsee);
			securityTokenReferenceUri.setAttribute("URI", "#X509Token");
			securityTokenReferenceUri.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			soapMessage.saveChanges();

			/*System.out.println("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println("\n");*/
			
			boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage soapResponse = soapConnection.call(soapMessage, isProduction ? E_INVOICE_URL_PROD : E_INVOICE_URL_TEST);
			
			/*System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();*/
			
			Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node node = (Node) reponseIterator.next();
				if(node.getNodeName().contains(":EchoAckMsg")){
					String toReturn = "";
					NodeList nodeList = node.getChildNodes();
					for (int j = 0; j < nodeList.getLength(); ++j){
						if("iwsc:MessageAck".equals(nodeList.item(j).getNodeName())){
							toReturn += nodeList.item(j).getLastChild().getTextContent();
						}
					}
					for (int j = 0; j < nodeList.getLength(); ++j){
						if(nodeList.item(j).getNodeName().contains(":EchoData")){
							return toReturn + System.lineSeparator() + nodeList.item(j).getFirstChild().getTextContent();
						}
					}
				}
			}
			
            soapConnection.close();
		} catch (SOAPException | UnsupportedOperationException /*| IOException*/ | CertificateEncodingException | NullPointerException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return "Pogreška u komunikaciji.";
	}
	
	public static String UpdateInvoiceStatus(int officeNumber, String officeTag, int cashRegisterNumber, int invoiceNumber, int specialNumber, String year, String oldStatus, boolean isTest){
		LoadCertificates(!isTest);
		
		try {
			String nameSpace_iwsc = "iwsc";
			String nameSpace_xsd = "xsd";
			String nameSpace_gois = "gois";
			String nameSpace_wsee = "wsee";
			String nameSpace_wsu = "wsu";
			String nameSpace_iwsc_URI = E_INVOICE_PREFIX + "/invoicewebservicecomponents/v0.1";
			String nameSpace_xsd_URI = "http://www.w3.org/2001/XMLSchema";
			String nameSpace_gois_URI = E_INVOICE_PREFIX + "/pki/GetB2BOutgoingInvoiceStatus/v0.1";
			String nameSpace_wsee_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
			String nameSpace_wsu_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(nameSpace_iwsc, nameSpace_iwsc_URI);
			envelope.addNamespaceDeclaration(nameSpace_xsd, nameSpace_xsd_URI);
			envelope.addNamespaceDeclaration(nameSpace_gois, nameSpace_gois_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsee, nameSpace_wsee_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsu, nameSpace_wsu_URI);
			soapMessage.getMimeHeaders().addHeader("SOAPAction", E_INVOICE_PREFIX + "/GetB2BOutgoingInvoiceStatus");
			
			// Header
			SOAPHeader soapHeader = envelope.getHeader();
			SOAPElement securityElem = soapHeader.addChildElement("Security", nameSpace_wsee);
			securityElem.addAttribute(envelope.createName("SOAP-ENV:mustUnderstand"), "1");
			
			SOAPElement timestampElem = securityElem.addChildElement("Timestamp", nameSpace_wsu);
			timestampElem.addAttribute(envelope.createName("Id"), "id_timestampElem").setIdAttribute("Id", true);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			timestampElem.addChildElement("Created", nameSpace_wsu).addTextNode(sdf.format(new Date()));
			timestampElem.addChildElement("Expires", nameSpace_wsu).addTextNode(sdf.format(new Date(new Date().getTime() + 1000 * 60 * 5)));
			
			byte[] certByte = GetPrivateCertificate(!isTest).getEncoded();
			SOAPElement binarySecurityToken = securityElem.addChildElement("BinarySecurityToken", nameSpace_wsee);
			binarySecurityToken.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			binarySecurityToken.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
			binarySecurityToken.setAttribute("wsu:Id", "X509Token");
			binarySecurityToken.addTextNode(Base64.getEncoder().encodeToString(certByte));
			
			// Body
			SOAPBody soapBody = envelope.getBody();
			soapBody.addAttribute(envelope.createName("Id"), "id_soapBody").setIdAttribute("Id", true);
			
			SOAPElement getInvoiceStatusMsgElem = soapBody.addChildElement("GetB2BOutgoingInvoiceStatusMsg", nameSpace_gois);
			
			SOAPElement headerSupplierElem = getInvoiceStatusMsgElem.addChildElement("HeaderSupplier", nameSpace_iwsc);
			headerSupplierElem.addChildElement("MessageID", nameSpace_iwsc).addTextNode(UUID.randomUUID().toString());
			headerSupplierElem.addChildElement("SupplierID", nameSpace_iwsc).addTextNode("9934:" + GetFiscOib(!isTest));
			//headerSupplierElem.addChildElement("AdditionalSupplierID", nameSpace_iwsc).addTextNode("");
			headerSupplierElem.addChildElement("MessageType", nameSpace_iwsc).addTextNode("9011");
			//headerSupplierElem.addChildElement("MessageAttributes", nameSpace_iwsc).addTextNode("");
					
			SOAPElement dataElem = getInvoiceStatusMsgElem.addChildElement("Data", nameSpace_gois);
			SOAPElement echoDataElem = dataElem.addChildElement("B2BOutgoingInvoiceStatus", nameSpace_gois);
			echoDataElem.addChildElement("SupplierInvoiceID", nameSpace_gois).addTextNode(invoiceNumber + "/" + officeTag + "/" + cashRegisterNumber);
			echoDataElem.addChildElement("InvoiceYear", nameSpace_gois).addTextNode(year);
			
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			SignMessage(securityElem, new String[]{"#id_timestampElem", "#id_soapBody"}, !isTest);
			soapMessage.saveChanges();
			
			// Key info
			Iterator i = securityElem.getChildElements();
			i.next();
			i.next();
			SOAPElement signatureElem = (SOAPElement) i.next();
			SOAPElement keyInfoElem = signatureElem.addChildElement("KeyInfo", "ds");
			SOAPElement securityTokenReference = keyInfoElem.addChildElement("SecurityTokenReference", nameSpace_wsee);
			SOAPElement securityTokenReferenceUri = securityTokenReference.addChildElement("Reference", nameSpace_wsee);
			securityTokenReferenceUri.setAttribute("URI", "#X509Token");
			securityTokenReferenceUri.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			soapMessage.saveChanges();

			//System.out.println("Request SOAP Message:");
			//soapMessage.writeTo(System.out);
			//System.out.println("\n");
			
			//SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //SOAPMessage soapResponse = soapConnection.call(soapMessage, E_INVOICE_URL);
			
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
			final int timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			URL endpoint = new URL(new URL(isTest ? E_INVOICE_URL_TEST : E_INVOICE_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			
			//System.out.println("Response SOAP Message:");
			//soapResponse.writeTo(System.out);
            //System.out.println();
			
			String toReturn = "";
			
			NodeList nodeList = soapResponse.getSOAPBody().getElementsByTagName("iwsc:StatusCode");
			for (int j = 0; j < nodeList.getLength(); ++j){
				toReturn += nodeList.item(j).getTextContent();
			}
			
			nodeList = soapResponse.getSOAPBody().getElementsByTagName("iwsc:StatusText");
			for (int j = 0; j < nodeList.getLength(); ++j){
				toReturn += " - " + nodeList.item(j).getTextContent();
			}
			
			nodeList = soapResponse.getSOAPBody().getElementsByTagName("iwsc:StatusTimestamp");
			for (int j = 0; j < nodeList.getLength(); ++j){
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss z");
				toReturn += " - " + dateFormat2.format(dateFormat.parse(nodeList.item(j).getTextContent()));
			}

            soapConnection.close();
			
			if("".equals(toReturn)){
				return oldStatus;
			} else {
				SaveEInvoiceStatus(officeNumber, cashRegisterNumber, invoiceNumber, specialNumber, year, toReturn, isTest);
				return toReturn;
			}
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return "Pogreška u komunikaciji.";
	}
	
	public static String GetReceiverList(String searchOib){
		LoadCertificates(false);
		
		try {
			String nameSpace_iwsc = "iwsc";
			String nameSpace_xsd = "xsd";
			String nameSpace_grl = "grl";
			String nameSpace_wsee = "wsee";
			String nameSpace_wsu = "wsu";
			String nameSpace_iwsc_URI = E_INVOICE_PREFIX + "/invoicewebservicecomponents/v0.1";
			String nameSpace_xsd_URI = "http://www.w3.org/2001/XMLSchema";
			String nameSpace_grl_URI = E_INVOICE_PREFIX + "/pki/GetReceiverList/v0.1";
			String nameSpace_wsee_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
			String nameSpace_wsu_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(nameSpace_iwsc, nameSpace_iwsc_URI);
			envelope.addNamespaceDeclaration(nameSpace_xsd, nameSpace_xsd_URI);
			envelope.addNamespaceDeclaration(nameSpace_grl, nameSpace_grl_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsee, nameSpace_wsee_URI);
			envelope.addNamespaceDeclaration(nameSpace_wsu, nameSpace_wsu_URI);
			soapMessage.getMimeHeaders().addHeader("SOAPAction", E_INVOICE_PREFIX + "/GetReceiverList");
			
			// Header
			SOAPHeader soapHeader = envelope.getHeader();
			SOAPElement securityElem = soapHeader.addChildElement("Security", nameSpace_wsee);
			securityElem.addAttribute(envelope.createName("SOAP-ENV:mustUnderstand"), "1");
			
			SOAPElement timestampElem = securityElem.addChildElement("Timestamp", nameSpace_wsu);
			timestampElem.addAttribute(envelope.createName("Id"), "id_timestampElem").setIdAttribute("Id", true);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			timestampElem.addChildElement("Created", nameSpace_wsu).addTextNode(sdf.format(new Date()));
			timestampElem.addChildElement("Expires", nameSpace_wsu).addTextNode(sdf.format(new Date(new Date().getTime() + 1000 * 60 * 5)));
			
			byte[] certByte = GetPrivateCertificate(false).getEncoded();
			SOAPElement binarySecurityToken = securityElem.addChildElement("BinarySecurityToken", nameSpace_wsee);
			binarySecurityToken.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			binarySecurityToken.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
			binarySecurityToken.setAttribute("wsu:Id", "X509Token");
			binarySecurityToken.addTextNode(Base64.getEncoder().encodeToString(certByte));
			
			// Body
			SOAPBody soapBody = envelope.getBody();
			soapBody.addAttribute(envelope.createName("Id"), "id_soapBody").setIdAttribute("Id", true);
			
			SOAPElement getInvoiceStatusMsgElem = soapBody.addChildElement("GetReceiverListMsg", nameSpace_grl);
			
			SOAPElement headerSupplierElem = getInvoiceStatusMsgElem.addChildElement("HeaderSupplier", nameSpace_iwsc);
			headerSupplierElem.addChildElement("MessageID", nameSpace_iwsc).addTextNode(UUID.randomUUID().toString());
			headerSupplierElem.addChildElement("SupplierID", nameSpace_iwsc).addTextNode("9934:" + GetFiscOib(false));
			//headerSupplierElem.addChildElement("AdditionalSupplierID", nameSpace_iwsc).addTextNode("");
			headerSupplierElem.addChildElement("MessageType", nameSpace_iwsc).addTextNode("9011");
			//headerSupplierElem.addChildElement("MessageAttributes", nameSpace_iwsc).addTextNode("");
					
			SOAPElement dataElem = getInvoiceStatusMsgElem.addChildElement("Data", nameSpace_grl);
			SOAPElement receiverListElem = dataElem.addChildElement("ReceiverList", nameSpace_grl);
			SOAPElement filterElem = receiverListElem.addChildElement("Filter", nameSpace_grl);
			SOAPElement textSearchElem = filterElem.addChildElement("TextSearch", nameSpace_grl);
			textSearchElem.addChildElement("SearchField", nameSpace_grl).addTextNode("OIB");
			textSearchElem.addChildElement("SearchValue", nameSpace_grl).addTextNode(searchOib);
			
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			SignMessage(securityElem, new String[]{"#id_timestampElem", "#id_soapBody"}, false);
			soapMessage.saveChanges();
			
			// Key info
			Iterator i = securityElem.getChildElements();
			i.next();
			i.next();
			SOAPElement signatureElem = (SOAPElement) i.next();
			SOAPElement keyInfoElem = signatureElem.addChildElement("KeyInfo", "ds");
			SOAPElement securityTokenReference = keyInfoElem.addChildElement("SecurityTokenReference", nameSpace_wsee);
			SOAPElement securityTokenReferenceUri = securityTokenReference.addChildElement("Reference", nameSpace_wsee);
			securityTokenReferenceUri.setAttribute("URI", "#X509Token");
			securityTokenReferenceUri.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
			soapMessage.saveChanges();

			System.out.println("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println("\n");
				
			//SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //SOAPMessage soapResponse = soapConnection.call(soapMessage, E_INVOICE_URL);
			
			boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
			final int timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			URL endpoint = new URL(new URL(isProduction ? E_INVOICE_URL_PROD : E_INVOICE_URL_TEST), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			
			System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();
			
			/*Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node node = (Node) reponseIterator.next();
				if("ns5:EchoAckMsg".equals(node.getNodeName())){
					String toReturn = "";
					NodeList nodeList = node.getChildNodes();
					for (int j = 0; j < nodeList.getLength(); ++j){
						if("iwsc:MessageAck".equals(nodeList.item(j).getNodeName())){
							toReturn += nodeList.item(j).getLastChild().getTextContent();
						}
					}
					for (int j = 0; j < nodeList.getLength(); ++j){
						if("ns5:EchoData".equals(nodeList.item(j).getNodeName())){
							return toReturn + System.lineSeparator() + nodeList.item(j).getFirstChild().getTextContent();
						}
					}
				}
			}*/
			
            soapConnection.close();
		} catch (SOAPException | UnsupportedOperationException | IOException | CertificateEncodingException | NullPointerException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return "Pogreška u komunikaciji.";
	}
	
	private static void LoadCertificates(boolean isProduction){
		int certType = isProduction ? Values.CERT_TYPE_EINVOICE_PROD : Values.CERT_TYPE_EINVOICE_TEST;
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = KEYSTORE_PASS.toCharArray();
			trustStore.load(null, password);
			
			X509Certificate certificateRoot = CertificateManager.GetCertificate(Values.CERT_DEMO_ROOT_ALIAS);
			trustStore.setCertificateEntry(Values.CERT_DEMO_ROOT_ALIAS, certificateRoot);
			
			X509Certificate certificateSub = CertificateManager.GetCertificate(Values.CERT_DEMO_SUB_ALIAS);
			trustStore.setCertificateEntry(Values.CERT_DEMO_SUB_ALIAS, certificateSub);
			
			X509Certificate certificateProdRoot = CertificateManager.GetCertificate(Values.CERT_PROD_ROOT_ALIAS);
			trustStore.setCertificateEntry(Values.CERT_PROD_ROOT_ALIAS, certificateProdRoot);
			
			X509Certificate certificateProdSub = CertificateManager.GetCertificate(Values.CERT_PROD_SUB_ALIAS);
			trustStore.setCertificateEntry(Values.CERT_PROD_SUB_ALIAS, certificateProdSub);
			
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			//KeyStore keyStore = KeyStore.getInstance("PKCS12");
			char[] passwordClient = GetPrivateCertificatePass(certType).toCharArray();
			keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(certType)), passwordClient);
			
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, GetPrivateCertificatePass(certType).toCharArray());
			//keyManagerFactory.init(keyStore, KEYSTORE_PASS.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			SSLSocketFactory socketFactory = context.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	public static String GetFiscOib(boolean isProduction){
		String toReturn = Licence.GetOIB();
		
		if(!isProduction){
			try {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				char[] password = GetPrivateCertificatePass(Values.CERT_TYPE_EINVOICE_TEST).toCharArray();
				keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(Values.CERT_TYPE_EINVOICE_TEST)), password);
				if(keyStore.aliases().hasMoreElements()){
					X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement());
					String subjectString = cert.getSubjectDN().toString();
					Pattern pattern = Pattern.compile("\\d{11}");
					Matcher matcher = pattern.matcher(subjectString);
					if (matcher.find()){
						toReturn = matcher.group();
					}
				}
			} catch(Exception ex){
				ClientAppLogger.GetInstance().LogError(ex);
			}
		}
		
		return toReturn;
	}
	
	public static byte[] GetPrivateCertificateBytes(int certType){
		byte[] toReturn = null;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT CERT FROM PRIVATE_CERTIFICATE WHERE ID = ?");
		databaseQuery.AddParam(1, certType);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
			
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
						toReturn = databaseQueryResult.getBytes(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	public static String GetPrivateCertificatePass(int certType){
		String toReturn = "";
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT PASS FROM PRIVATE_CERTIFICATE WHERE ID = ?");
		databaseQuery.AddParam(1, certType);
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
		
		databaseQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!databaseQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
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
						toReturn = databaseQueryResult.getString(0);
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}
		
		return toReturn;
	}
	
	private static String GetPaymentMethodCode(int paymentType){
		if (paymentType == Values.PAYMENT_METHOD_TYPE_CASH){
			return "10";
		} else if (paymentType == Values.PAYMENT_METHOD_TYPE_CREDIT_CARD){
			return "42";
		} else if (paymentType == Values.PAYMENT_METHOD_TYPE_CHECK){
			return "42";
		} else if (paymentType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
			return "42";
		}
		
		return "42";
	}
	
	private static void SignMessage(Element element, String[] referenceString, boolean isProduction){
		try {
			XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance();
			List transformsList = new ArrayList();
			transformsList.add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null));
			transformsList.add(sigFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null));
			List<Reference> refList = new ArrayList<>();
			for (int i = 0; i < referenceString.length; ++i){
				Reference ref = sigFactory.newReference(referenceString[i], sigFactory.newDigestMethod(DigestMethod.SHA256, null), transformsList, null, null);
				refList.add(ref);
			}
			SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
				CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null), sigFactory
				.newSignatureMethod(SignatureMethod.RSA_SHA1, null), refList);
			
			//X509Certificate cert = GetPrivateCertificate(isProduction);
			//KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
			//List x509Content = new ArrayList();
			//x509Content.add(cert.getSubjectX500Principal().getName());
			//x509Content.add(cert);
			//x509Content.add(kif.newX509IssuerSerial(cert.getIssuerX500Principal().getName(), cert.getSerialNumber()));
			//X509Data xData = kif.newX509Data(x509Content);
			//KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(xData));
			
			/*List keyInfoList = new ArrayList();
			keyInfoList.add();
			KeyInfo keyInfo = sigFactory.getKeyInfoFactory().newKeyInfo(keyInfoList);*/

			DOMSignContext sigContext = new DOMSignContext(GetPrivateKey(isProduction), element);
			sigContext.putNamespacePrefix(XMLSignature.XMLNS, "ds");
			//XMLSignature sig = sigFactory.newXMLSignature(signedInfo, keyInfo);
			XMLSignature sig = sigFactory.newXMLSignature(signedInfo, null);
			sig.sign(sigContext);
		} catch (Exception ex){
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}

	public static Key GetPrivateKey(boolean isProduction){
		int certType = isProduction ? Values.CERT_TYPE_EINVOICE_PROD : Values.CERT_TYPE_EINVOICE_TEST;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			//KeyStore keyStore = KeyStore.getInstance("PKCS12");
			char[] password = GetPrivateCertificatePass(certType).toCharArray();
			keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(certType)), password);
			if(keyStore.aliases().hasMoreElements()){
				String keyAlias = keyStore.aliases().nextElement();
				
				// Environment type and key type check
				X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
				boolean isDemoCert = cert.getIssuerDN().toString().toUpperCase().contains("DEMO");
				
				if(isDemoCert && isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U produkcijskoj okolini ne smije se koristiti demo certifikat!");
					return null;
				}
				
				if(!isDemoCert && !isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U demo okolini ne smije se koristiti produkcijski certifikat!");
					return null;
				}
				
				return keyStore.getKey(keyAlias, password);	
			}
		} catch (Exception ex) {
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return null;
	}
	
	public static X509Certificate GetPrivateCertificate(boolean isProduction){
		int certType = isProduction ? Values.CERT_TYPE_EINVOICE_PROD : Values.CERT_TYPE_EINVOICE_TEST;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			//KeyStore keyStore = KeyStore.getInstance("PKCS12");
			char[] password = GetPrivateCertificatePass(certType).toCharArray();
			keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(certType)), password);
			if(keyStore.aliases().hasMoreElements()){
				String keyAlias = keyStore.aliases().nextElement();
				
				// Environment type and key type check
				X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
				boolean isDemoCert = cert.getIssuerDN().toString().toUpperCase().contains("DEMO");
				
				if(isDemoCert && isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U produkcijskoj okolini ne smije se koristiti demo certifikat!");
					return null;
				}
				
				if(!isDemoCert && !isProduction){
					ClientAppLogger.GetInstance().ShowMessage("U demo okolini ne smije se koristiti produkcijski certifikat!");
					return null;
				}
				
				return cert;
			}
		} catch (Exception ex) {
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return null;
	}
}
