/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.fiscalization;

import hr.adinfo.utils.certificates.CertificateManager;
import hr.adinfo.client.ClientAppLocalServerClient;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.PackagingRefunds;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.licence.Licence;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JDialog;
import javax.xml.soap.*;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.NodeList;


/**
 *
 * @author Matej
 */
public class Fiscalization {
	
	public static final Object fiscalizationInvoiceUploadLock = new Object();
	public static final String KEYSTORE_NAME = "keystore.jks";
	public static final String KEYSTORE_PASS = "password";
	public static final String FISK_URL_TEST = "https://cistest.apis-it.hr:8449/FiskalizacijaServiceTest";
	public static final String FISK_URL_PROD = "https://cis.porezna-uprava.hr:8449/FiskalizacijaService";
	
	private static boolean fiscalisationInProgress;
	
	public static void FiscalizeInvoiceSynchronized(Invoice invoice, boolean isNow){
		fiscalisationInProgress = true;
		synchronized(fiscalizationInvoiceUploadLock){
			FiscalizeInvoiceUnsynchronized(invoice, isNow);
		}
		fiscalisationInProgress = false;
	}
        
        public static void FiscalizeInvoiceNapojnice(Invoice invoice, boolean isNow){		
		LoadCertificates();
		try {
			String myNamespace = "tns";
			String myNamespaceURI = "http://www.apis-it.hr/fin/2012/types/f73";
                        String tipPlacanja = "";
                        Integer iznosNapojnice = 0;
                        
                        if (invoice.iznosNapojnice == null){
                            invoice.iznosNapojnice = "";
                        } else {
                            iznosNapojnice = Integer.parseInt(invoice.iznosNapojnice);
                        } 
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

			SOAPBody soapBody = envelope.getBody();
			SOAPElement rootElem = soapBody.addChildElement("NapojnicaZahtjev", myNamespace);
			Name idName = envelope.createName("Id");
			rootElem.addAttribute(idName, "NapojnicaZahtjev");
			rootElem.setIdAttribute("Id", true);
			
			// Header
			Date currentDate = new Date();
			SOAPElement headerElem = rootElem.addChildElement("Zaglavlje", myNamespace);
			headerElem.addChildElement("IdPoruke", myNamespace).addTextNode(UUID.randomUUID().toString());
			headerElem.addChildElement("DatumVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(currentDate) + "T" + new SimpleDateFormat("HH:mm:ss").format(currentDate));
			
			// Invoice data 1
			SOAPElement invoiceElem = rootElem.addChildElement("Racun", myNamespace);
			invoiceElem.addChildElement("Oib", myNamespace).addTextNode(GetFiscOib(invoice.isTest));
			invoiceElem.addChildElement("USustPdv", myNamespace).addTextNode(invoice.isInVatSystem ? "true" : "false");
			invoiceElem.addChildElement("DatVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(invoice.date) + "T" + new SimpleDateFormat("HH:mm:ss").format(invoice.date));
			invoiceElem.addChildElement("OznSlijed", myNamespace).addTextNode("P");
			SOAPElement invoiceNumberElem = invoiceElem.addChildElement("BrRac", myNamespace);
			invoiceNumberElem.addChildElement("BrOznRac", myNamespace).addTextNode("" + invoice.invoiceNumber);
			invoiceNumberElem.addChildElement("OznPosPr", myNamespace).addTextNode("" + invoice.officeTag);
			invoiceNumberElem.addChildElement("OznNapUr", myNamespace).addTextNode("" + invoice.cashRegisterNumber);
                        InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			SOAPElement taxesElem = invoiceElem.addChildElement("Pdv", myNamespace);
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.isConsumpionTax.get(i))  
					continue;
				
				SOAPElement taxElem = taxesElem.addChildElement("Porez", myNamespace);
				taxElem.addChildElement("Stopa", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				taxElem.addChildElement("Osnovica", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)));
				taxElem.addChildElement("Iznos", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)));
			}
                        float totalPrice = ClientAppUtils.FloatToPriceFloat(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			invoiceElem.addChildElement("IznosUkupno", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(totalPrice));
			invoiceElem.addChildElement("NacinPlac", myNamespace).addTextNode(GetPaymentMethodCode(invoice));
			invoiceElem.addChildElement("OibOper", myNamespace).addTextNode(invoice.staffOib);
			invoiceElem.addChildElement("ZastKod", myNamespace).addTextNode(invoice.zki);
                        invoiceElem.addChildElement("NakDost", myNamespace).addTextNode(isNow ? "false" : "true");
                        SOAPElement napojnicaElement = invoiceElem.addChildElement("Napojnica", myNamespace);
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("hr-HR"));
                        symbols.setDecimalSeparator('.');
                        DecimalFormat df = new DecimalFormat("0.00", symbols);
                        String formattedValue = df.format((double) iznosNapojnice);
                        System.out.println(formattedValue);
                        napojnicaElement.addChildElement("iznosNapojnice", myNamespace).addTextNode(formattedValue);
			napojnicaElement.addChildElement("nacinPlacanjaNapojnice", myNamespace).addTextNode(invoice.tipNapojnice);

			// Save message
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			// Sign the message
			SignMessage(rootElem, "#NapojnicaZahtjev", invoice.isTest);
			soapMessage.saveChanges();
			
			/*System.out.println("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println("\n\n");*/
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLSent(soapMessage, "R-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
                        SOAPMessage soapResponse = soapConnection.call(soapMessage, FISK_URL_TEST);
			
			final int timeout;
			if (isNow){
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			} else {
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT.ordinal());
			}
			URL endpoint = new URL(new URL(invoice.isTest ? FISK_URL_TEST : FISK_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
			
			//System.out.println("Response SOAP Message:");
			//soapResponse.writeTo(System.out);
			//System.out.println();
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLReceived(soapResponse, "R-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
                        ClientAppLogger.GetInstance().LogMessage("Stigao do whilea.");
			Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node responseNode = (Node) reponseIterator.next();
                                 ClientAppLogger.GetInstance().LogMessage("Response node: " + responseNode.getNodeName());
				 if("tns:NapojnicaOdgovor".equals(responseNode.getNodeName())){
                                     ClientAppLogger.GetInstance().LogMessage(responseNode.getTextContent());
					NodeList racunOdgovorNodes = responseNode.getChildNodes();
					for(int i = 0; i < racunOdgovorNodes.getLength(); ++i){
                                                ClientAppLogger.GetInstance().LogMessage("racun odgovor node: " + responseNode.getNodeName());
						Node racunOdgovorNode = (Node) racunOdgovorNodes.item(i);
                                                if("tns:PorukaOdgovora".equals(racunOdgovorNode.getNodeName())){
							NodeList porukaOdgovoraNodes = responseNode.getChildNodes();
                                                        for(int j = 0; j < porukaOdgovoraNodes.getLength(); ++j){
                                                            Node porukaOdgovoraNode = (Node) porukaOdgovoraNodes.item(j);              
                                                            ClientAppLogger.GetInstance().LogMessage("poruka odgovora node: " + porukaOdgovoraNodes.item(j));
                                                            if("tns:SifraPoruke".equals(porukaOdgovoraNode.getNodeName())){
									ClientAppLogger.GetInstance().LogMessage(porukaOdgovoraNode.getTextContent());
                                                            };
                                                        }
						}
                                                else if ("tns:Greske".equals(racunOdgovorNode.getNodeName())){
                                                    ClientAppLogger.GetInstance().LogMessage(racunOdgovorNode.getTextContent());
                                                    NodeList porukaGresakaNodes = racunOdgovorNode.getChildNodes();
                                                    for(int k = 0; k < porukaGresakaNodes.getLength(); ++k){
                                                        Node porukaGresakaNode = (Node) porukaGresakaNodes.item(k);
                                                        ClientAppLogger.GetInstance().LogMessage("poruka gresaka node: " + porukaGresakaNodes.item(k));
                                                        if ("tns:Greska".equals(porukaGresakaNode.getNodeName())){
                                                            NodeList porukaNaGreskiNodes = porukaGresakaNode.getChildNodes();
                                                            for(int m = 0; m < porukaNaGreskiNodes.getLength(); ++m){
                                                               Node porukaGreskeNode = (Node) porukaNaGreskiNodes.item(m);
                                                               ClientAppLogger.GetInstance().LogMessage("poruka na greski node: " + porukaNaGreskiNodes.item(m));
                                                               if("tns:PorukaGreske".equals(porukaGreskeNode.getNodeName())){
									ClientAppLogger.GetInstance().LogMessage(porukaGreskeNode.getTextContent());
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                        }
                                 }
			}
			
            soapConnection.close();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().LogError(ex);
		}
		
		if(!Values.DEFAULT_JIR.equals(invoice.jir)){
			SaveInvoiceJir(invoice);
		}
	}
	
	public static void FiscalizeInvoiceUnsynchronized(Invoice invoice, boolean isNow){
		if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_OFFER){
			FiscalizeInvoiceSpecial(invoice, isNow);
		} else if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP || invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				
		} else {
			FiscalizeInvoice(invoice, isNow);
			//CheckInvoiceFiscalization(invoice, isNow);
		}
	} 
	
	public static void FiscalizeInvoice(Invoice invoice, boolean isNow){
		boolean specialZki = invoice.specialZki != null && !Values.DEFAULT_ZKI.equals(invoice.specialZki) && !"null".equals(invoice.specialZki); 
		boolean specialJir = invoice.specialJir != null && !Values.DEFAULT_JIR.equals(invoice.specialJir) && !"null".equals(invoice.specialJir);
		String methodName = specialZki || specialJir ? "RacunPDZahtjev" : "RacunZahtjev";
		
		LoadCertificates();
		try {
			String myNamespace = "tns";
			String myNamespaceURI = "http://www.apis-it.hr/fin/2012/types/f73";
                        String tipPlacanja = "";
                        String iznosNapojnice = "";
                        
                        if (invoice.iznosNapojnice == null){
                            invoice.iznosNapojnice = "";
                        }
			
                        if (invoice.tipNapojnice == null){
                            invoice.tipNapojnice = "";
                        }
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

			SOAPBody soapBody = envelope.getBody();
			SOAPElement rootElem = soapBody.addChildElement(methodName, myNamespace);
			Name idName = envelope.createName("Id");
			rootElem.addAttribute(idName, methodName);
			rootElem.setIdAttribute("Id", true);
			
			// Header
			Date currentDate = new Date();
			SOAPElement headerElem = rootElem.addChildElement("Zaglavlje", myNamespace);
			headerElem.addChildElement("IdPoruke", myNamespace).addTextNode(UUID.randomUUID().toString());
			headerElem.addChildElement("DatumVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(currentDate) + "T" + new SimpleDateFormat("HH:mm:ss").format(currentDate));
			
			// Invoice data 1
			SOAPElement invoiceElem = rootElem.addChildElement("Racun", myNamespace);
			invoiceElem.addChildElement("Oib", myNamespace).addTextNode(GetFiscOib(!invoice.isTest));
			invoiceElem.addChildElement("USustPdv", myNamespace).addTextNode(invoice.isInVatSystem ? "true" : "false");
			invoiceElem.addChildElement("DatVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(invoice.date) + "T" + new SimpleDateFormat("HH:mm:ss").format(invoice.date));
			invoiceElem.addChildElement("OznSlijed", myNamespace).addTextNode("N");
			SOAPElement invoiceNumberElem = invoiceElem.addChildElement("BrRac", myNamespace);
			invoiceNumberElem.addChildElement("BrOznRac", myNamespace).addTextNode("" + invoice.invoiceNumber);
			invoiceNumberElem.addChildElement("OznPosPr", myNamespace).addTextNode("" + invoice.officeTag);
			invoiceNumberElem.addChildElement("OznNapUr", myNamespace).addTextNode("" + invoice.cashRegisterNumber);
                        
			
			// Taxes
			InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			SOAPElement taxesElem = invoiceElem.addChildElement("Pdv", myNamespace);
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.isConsumpionTax.get(i))
					continue;
				
				SOAPElement taxElem = taxesElem.addChildElement("Porez", myNamespace);
				taxElem.addChildElement("Stopa", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				taxElem.addChildElement("Osnovica", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)));
				taxElem.addChildElement("Iznos", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)));
			}
			
			SOAPElement consTaxesElem = null;
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(!invoiceTaxes.isConsumpionTax.get(i))
					continue;
				
				if(consTaxesElem == null){
					consTaxesElem = invoiceElem.addChildElement("Pnp", myNamespace);
				}
				
				SOAPElement taxElem = consTaxesElem.addChildElement("Porez", myNamespace);
				taxElem.addChildElement("Stopa", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				taxElem.addChildElement("Osnovica", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)));
				taxElem.addChildElement("Iznos", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)));
			}
			
			// Packaging refunds
			PackagingRefunds packagingRefunds = ClientAppUtils.CalculatePackagingRefunds(invoice);
			SOAPElement refundsElem = null;
			for (int i = 0; i < packagingRefunds.refundValues.size(); ++i){
				if(packagingRefunds.refundAmounts.get(i) == 0f)
					continue;
				
				if(refundsElem == null){
					refundsElem = invoiceElem.addChildElement("Naknade", myNamespace);
				}
				
				SOAPElement refundElem = refundsElem.addChildElement("Naknada", myNamespace);
				refundElem.addChildElement("NazivN", myNamespace).addTextNode("Povratna naknada " + ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(i)));
				refundElem.addChildElement("IznosN", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(i) * packagingRefunds.refundAmounts.get(i)));
			}
			
			// Invoice data 2
			float totalPrice = ClientAppUtils.FloatToPriceFloat(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			invoiceElem.addChildElement("IznosUkupno", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(totalPrice));
			invoiceElem.addChildElement("NacinPlac", myNamespace).addTextNode(GetPaymentMethodCode(invoice));
			invoiceElem.addChildElement("OibOper", myNamespace).addTextNode(invoice.staffOib);
			invoiceElem.addChildElement("ZastKod", myNamespace).addTextNode(invoice.zki);
			invoiceElem.addChildElement("NakDost", myNamespace).addTextNode(isNow ? "false" : "true");


			// Special invoice
			if(specialJir || specialZki){
				SOAPElement specialElem = invoiceElem.addChildElement("PrateciDokument", myNamespace);
				if(specialJir){
					specialElem.addChildElement("JirPD", myNamespace).addTextNode(invoice.specialJir);
				} else if(specialZki){
					specialElem.addChildElement("ZastKodPD", myNamespace).addTextNode(invoice.specialZki);
				}
			}
			
			// Save message
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			// Sign the message
			SignMessage(rootElem, "#" + methodName, !invoice.isTest);
			soapMessage.saveChanges();
			
			/*System.out.println("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println("\n\n");*/
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLSent(soapMessage, "R-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //SOAPMessage soapResponse = soapConnection.call(soapMessage, FISK_URL_TEST);
			
			final int timeout;
			if (isNow){
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			} else {
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT.ordinal());
			}
			URL endpoint = new URL(new URL(invoice.isTest ? FISK_URL_TEST : FISK_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			
			//System.out.println("Response SOAP Message:");
			//soapResponse.writeTo(System.out);
			//System.out.println();
			
			ClientAppLogger.GetInstance().LogFiscalizationXMLReceived(soapResponse, "R-" + invoice.invoiceNumber + "-"+ invoice.officeTag + "-" + invoice.cashRegisterNumber);
			
			Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node responseNode = (Node) reponseIterator.next();
				if("tns:RacunOdgovor".equals(responseNode.getNodeName()) || "tns:RacunPDOdgovor".equals(responseNode.getNodeName())){
					NodeList racunOdgovorNodes = responseNode.getChildNodes();
					for(int i = 0; i < racunOdgovorNodes.getLength(); ++i){
						Node racunOdgovorNode = (Node) racunOdgovorNodes.item(i);
						if("tns:Jir".equals(racunOdgovorNode.getNodeName())){
							invoice.jir = racunOdgovorNode.getTextContent();
						}
					}
				}
			}
			
            soapConnection.close();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().LogError(ex);
		}
		
		if(!Values.DEFAULT_JIR.equals(invoice.jir)){
			SaveInvoiceJir(invoice);
		}
	}
	
	public static void FiscalizeInvoiceSpecial(Invoice invoice, boolean isNow){
		LoadCertificates();
		try {
			String myNamespace = "tns";
			String myNamespaceURI = "http://www.apis-it.hr/fin/2012/types/f73";
                        
                        if (invoice.iznosNapojnice == null){
                            invoice.iznosNapojnice = "";
                        }
			
                        if (invoice.tipNapojnice == null){
                            invoice.tipNapojnice = "";
                        }
                        
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

			SOAPBody soapBody = envelope.getBody();
			SOAPElement rootElem = soapBody.addChildElement("PrateciDokumentiZahtjev", myNamespace);
			Name idName = envelope.createName("Id");
			rootElem.addAttribute(idName, "PrateciDokumentiZahtjev");
			rootElem.setIdAttribute("Id", true);
			
			// Header
			Date currentDate = new Date();
			SOAPElement headerElem = rootElem.addChildElement("Zaglavlje", myNamespace);
			headerElem.addChildElement("IdPoruke", myNamespace).addTextNode(UUID.randomUUID().toString());
			headerElem.addChildElement("DatumVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(currentDate) + "T" + new SimpleDateFormat("HH:mm:ss").format(currentDate));
			
			// Invoice data 1
			SOAPElement invoiceElem = rootElem.addChildElement("PrateciDokument", myNamespace);
			invoiceElem.addChildElement("Oib", myNamespace).addTextNode(GetFiscOib(!invoice.isTest));
			invoiceElem.addChildElement("DatVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(invoice.date) + "T" + new SimpleDateFormat("HH:mm:ss").format(invoice.date));
			SOAPElement invoiceNumberElem = invoiceElem.addChildElement("BrPratecegDokumenta", myNamespace);
			invoiceNumberElem.addChildElement("BrOznPD", myNamespace).addTextNode("" + invoice.specialNumber);
			invoiceNumberElem.addChildElement("OznPosPr", myNamespace).addTextNode("" + invoice.officeTag);
			invoiceNumberElem.addChildElement("OznNapUr", myNamespace).addTextNode("" + invoice.cashRegisterNumber);
			
			// Invoice data 2
			float totalPrice = ClientAppUtils.FloatToPriceFloat(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			invoiceElem.addChildElement("IznosUkupno", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(totalPrice));
			invoiceElem.addChildElement("ZastKodPD", myNamespace).addTextNode(invoice.zki);
			invoiceElem.addChildElement("NakDost", myNamespace).addTextNode(isNow ? "false" : "true");
			
			// Save message
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			// Sign the message
			SignMessage(rootElem, "#PrateciDokumentiZahtjev", !invoice.isTest);
			soapMessage.saveChanges();
			
			//System.out.println("Request SOAP Message:");
			//soapMessage.writeTo(System.out);
			//System.out.println("\n\n");
			
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //SOAPMessage soapResponse = soapConnection.call(soapMessage, FISK_URL_TEST);
			
			final int timeout;
			if (isNow){
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			} else {
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT.ordinal());
			}
			URL endpoint = new URL(new URL(invoice.isTest ? FISK_URL_TEST : FISK_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return connection;
				}
			});
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			
			//System.out.println("Response SOAP Message:");
			//soapResponse.writeTo(System.out);
			//System.out.println();
			
			Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node responseNode = (Node) reponseIterator.next();
				if("tns:PrateciDokumentiOdgovor".equals(responseNode.getNodeName())){
					NodeList racunOdgovorNodes = responseNode.getChildNodes();
					for(int i = 0; i < racunOdgovorNodes.getLength(); ++i){
						Node racunOdgovorNode = (Node) racunOdgovorNodes.item(i);
						if("tns:Jir".equals(racunOdgovorNode.getNodeName())){
							invoice.jir = racunOdgovorNode.getTextContent();
						}
					}
				}
			}
			
            soapConnection.close();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().LogError(ex);
		}
		
		if(!Values.DEFAULT_JIR.equals(invoice.jir)){
			SaveInvoiceJir(invoice);
		}
	}
        
        	private static void SaveInvoiceJirNap(Invoice invoice){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE LOCAL_INVOICES SET NAP_JIR = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
                        
			if(invoice.isTest){
				query = query.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.executeLocally = true;
			databaseQuery.AddParam(1, invoice.JIRNapojnice);
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

			String query = "UPDATE INVOICES SET NAP_JIR = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
			if(invoice.isTest){
				query = query.replace("INVOICES", "INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, invoice.jir);
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

	
	private static void SaveInvoiceJir(Invoice invoice){
		{
			final JDialog loadingDialog = new LoadingDialog(null, true);

			String query = "UPDATE LOCAL_INVOICES SET JIR = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
			if(invoice.isTest){
				query = query.replace("LOCAL_INVOICES", "LOCAL_INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.executeLocally = true;
			databaseQuery.AddParam(1, invoice.jir);
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

			String query = "UPDATE INVOICES SET JIR = ? WHERE O_NUM = ? AND CR_NUM = ? AND I_NUM = ? AND SPEC_NUM = ?";
			if(invoice.isTest){
				query = query.replace("INVOICES", "INVOICES_TEST");
			}
			
			DatabaseQuery databaseQuery = new DatabaseQuery(query);
			databaseQuery.AddParam(1, invoice.jir);
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
	
	public static void CheckInvoiceFiscalization(Invoice invoice, boolean isNow){
		boolean specialZki = invoice.specialZki != null && !Values.DEFAULT_ZKI.equals(invoice.specialZki) && !"null".equals(invoice.specialZki);
		boolean specialJir = invoice.specialJir != null && !Values.DEFAULT_JIR.equals(invoice.specialJir) && !"null".equals(invoice.specialJir);
		String methodName = specialZki || specialJir ? "RacunPD" : "Racun";
		
		LoadCertificates();
		try {
			String myNamespace = "tns";
			String myNamespaceURI = "http://www.apis-it.hr/fin/2012/types/f73";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
                        
                                                if (invoice.iznosNapojnice == null){
                            invoice.iznosNapojnice = "";
                        }
			
                        if (invoice.tipNapojnice == null){
                            invoice.tipNapojnice = "";
                        }

			SOAPBody soapBody = envelope.getBody();
			SOAPElement rootElem = soapBody.addChildElement("ProvjeraZahtjev", myNamespace);
			Name idName = envelope.createName("Id");
			rootElem.addAttribute(idName, "ProvjeraZahtjev");
			rootElem.setIdAttribute("Id", true);
			
			// Header
			Date currentDate = new Date();
			SOAPElement headerElem = rootElem.addChildElement("Zaglavlje", myNamespace);
			headerElem.addChildElement("IdPoruke", myNamespace).addTextNode(UUID.randomUUID().toString());
			headerElem.addChildElement("DatumVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(currentDate) + "T" + new SimpleDateFormat("HH:mm:ss").format(currentDate));
			
			// Invoice data 1
			SOAPElement invoiceElem = rootElem.addChildElement(methodName, myNamespace);
			invoiceElem.addChildElement("Oib", myNamespace).addTextNode(GetFiscOib(!invoice.isTest));
			invoiceElem.addChildElement("USustPdv", myNamespace).addTextNode(invoice.isInVatSystem ? "true" : "false");
			invoiceElem.addChildElement("DatVrijeme", myNamespace).addTextNode(new SimpleDateFormat("dd.MM.yyyy").format(invoice.date) + "T" + new SimpleDateFormat("HH:mm:ss").format(invoice.date));
			invoiceElem.addChildElement("OznSlijed", myNamespace).addTextNode("N");
			SOAPElement invoiceNumberElem = invoiceElem.addChildElement("BrRac", myNamespace);
			invoiceNumberElem.addChildElement("BrOznRac", myNamespace).addTextNode("" + invoice.invoiceNumber);
			invoiceNumberElem.addChildElement("OznPosPr", myNamespace).addTextNode("" + invoice.officeTag);
			invoiceNumberElem.addChildElement("OznNapUr", myNamespace).addTextNode("" + invoice.cashRegisterNumber);
                        
			// Taxes
			InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			SOAPElement taxesElem = invoiceElem.addChildElement("Pdv", myNamespace);
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(invoiceTaxes.isConsumpionTax.get(i))
					continue;
				
				SOAPElement taxElem = taxesElem.addChildElement("Porez", myNamespace);
				taxElem.addChildElement("Stopa", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				taxElem.addChildElement("Osnovica", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)));
				taxElem.addChildElement("Iznos", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)));
			}
			
			SOAPElement consTaxesElem = null;
			for (int i = 0; i < invoiceTaxes.taxRates.size(); ++i){
				if(!invoiceTaxes.isConsumpionTax.get(i))
					continue;
				
				if(consTaxesElem == null){
					consTaxesElem = invoiceElem.addChildElement("Pnp", myNamespace);
				}
				
				SOAPElement taxElem = consTaxesElem.addChildElement("Porez", myNamespace);
				taxElem.addChildElement("Stopa", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxRates.get(i)));
				taxElem.addChildElement("Osnovica", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(i)));
				taxElem.addChildElement("Iznos", myNamespace).addTextNode(ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(i)));
			}
			
			// Packaging refunds
			PackagingRefunds packagingRefunds = ClientAppUtils.CalculatePackagingRefunds(invoice);
			SOAPElement refundsElem = null;
			for (int i = 0; i < packagingRefunds.refundValues.size(); ++i){
				if(packagingRefunds.refundAmounts.get(i) == 0f)
					continue;
				
				if(refundsElem == null){
					refundsElem = invoiceElem.addChildElement("Naknade", myNamespace);
				}
				
				SOAPElement refundElem = refundsElem.addChildElement("Naknada", myNamespace);
				refundElem.addChildElement("NazivN", myNamespace).addTextNode("Povratna naknada " + ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(i)));
				refundElem.addChildElement("IznosN", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(i) * packagingRefunds.refundAmounts.get(i)));
			}
			
			// Invoice data 2
			float totalPrice = ClientAppUtils.FloatToPriceFloat(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
			invoiceElem.addChildElement("IznosUkupno", myNamespace).addTextNode(ClientAppUtils.FloatToPriceString(totalPrice));
			invoiceElem.addChildElement("NacinPlac", myNamespace).addTextNode(GetPaymentMethodCode(invoice));
			invoiceElem.addChildElement("OibOper", myNamespace).addTextNode(invoice.staffOib);
			invoiceElem.addChildElement("ZastKod", myNamespace).addTextNode(invoice.zki);
			invoiceElem.addChildElement("NakDost", myNamespace).addTextNode(isNow ? "false" : "true");
			
			// Special invoice
			if(specialJir || specialZki){
				SOAPElement specialElem = invoiceElem.addChildElement("PrateciDokument", myNamespace);
				if(specialJir){
					specialElem.addChildElement("JirPD", myNamespace).addTextNode(invoice.specialJir);
				} else if(specialZki){
					specialElem.addChildElement("ZastKodPD", myNamespace).addTextNode(invoice.specialZki);
				}
			}
			
			// Save message
			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();
			
			// Sign the message
			SignMessage(rootElem, "#ProvjeraZahtjev", !invoice.isTest);
			soapMessage.saveChanges();
			
			System.out.println("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println("\n\n");
		
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
			//SOAPMessage soapResponse = soapConnection.call(soapMessage, FISK_URL_TEST);
			
			final int timeout;
			if (isNow){
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME.ordinal());
			} else {
				timeout = 1000 * ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_FISCALISATION_WAITTIME_REPEAT.ordinal());
			}
			URL endpoint = new URL(new URL(invoice.isTest ? FISK_URL_TEST : FISK_URL_PROD), "", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
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
				if("tns:EchoResponse".equals(node.getNodeName())){
					return node.getTextContent();
				}
			}*/
			
            soapConnection.close();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	private static String GetPaymentMethodCode(Invoice invoice){
		if(invoice.paymentMethodType2 != -1 && invoice.paymentAmount2 != 0f){
			return "O";
		}
		
		if (invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_CASH){
			return "G";
		} else if (invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_CREDIT_CARD){
			return "K";
		} else if (invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_CHECK){
			return "C";
		} else if (invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
			return "T";
		}
		
		return "O";
	}
	
	private static void SignMessage(Element element, String referenceString, boolean isProduction){
		try {
			XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance();
			List transformsList = new ArrayList();
			transformsList.add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null));
			transformsList.add(sigFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null));
			Reference ref = sigFactory.newReference(referenceString, sigFactory.newDigestMethod(DigestMethod.SHA1, null), transformsList, null, null);
			SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
				CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null), sigFactory
				.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
			
			X509Certificate cert = GetPrivateCertificate(isProduction);
			KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
			List x509Content = new ArrayList();
			x509Content.add(cert.getSubjectX500Principal().getName());
			x509Content.add(cert);
			x509Content.add(kif.newX509IssuerSerial(cert.getIssuerX500Principal().getName(), cert.getSerialNumber()));
			X509Data xData = kif.newX509Data(x509Content);
			KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(xData));

			DOMSignContext sigContext = new DOMSignContext(GetPrivateKey(isProduction), element);
			sigContext.putNamespacePrefix(XMLSignature.XMLNS, "ds");
			XMLSignature sig = sigFactory.newXMLSignature(signedInfo, keyInfo);
			sig.sign(sigContext);
		} catch (Exception ex){
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	public static String FiscalisationEcho(String message){
		LoadCertificates();
		
		try {
			String myNamespace = "tns";
			String myNamespaceURI = "http://www.apis-it.hr/fin/2012/types/f73";
			
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
			envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("EchoRequest", myNamespace);
			soapBodyElem.addTextNode(message);

			soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMessage.saveChanges();

			//System.out.println("Request SOAP Message:");
			//soapMessage.writeTo(System.out);
			//System.out.println("\n");
		
			boolean isProduction = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_ADMIN_ENVIRONMENT_PRODUCTION.ordinal());
			SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage soapResponse = soapConnection.call(soapMessage, isProduction ? FISK_URL_PROD : FISK_URL_TEST);
			
			//System.out.println("Response SOAP Message:");
            //soapResponse.writeTo(System.out);
            //System.out.println();
			
			Iterator reponseIterator = soapResponse.getSOAPBody().getChildElements();
			while(reponseIterator.hasNext()){
				Node node = (Node) reponseIterator.next();
				if("tns:EchoResponse".equals(node.getNodeName())){
					return node.getTextContent();
				}
			}
			
            soapConnection.close();
		} catch (SOAPException | UnsupportedOperationException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return "Pogreška u komunikaciji.";
	}
	
	private static void LoadCertificates(){
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = KEYSTORE_PASS.toCharArray();
			keyStore.load(null, password);
			
			X509Certificate certificateRoot = CertificateManager.GetCertificate(Values.CERT_DEMO_ROOT_ALIAS);
			keyStore.setCertificateEntry(Values.CERT_DEMO_ROOT_ALIAS, certificateRoot);
			
			X509Certificate certificateSub = CertificateManager.GetCertificate(Values.CERT_DEMO_SUB_ALIAS);
			keyStore.setCertificateEntry(Values.CERT_DEMO_SUB_ALIAS, certificateSub);
			
			X509Certificate certificateProdRoot = CertificateManager.GetCertificate(Values.CERT_PROD_ROOT_ALIAS);
			keyStore.setCertificateEntry(Values.CERT_PROD_ROOT_ALIAS, certificateProdRoot);
			
			X509Certificate certificateProdSub = CertificateManager.GetCertificate(Values.CERT_PROD_SUB_ALIAS);
			keyStore.setCertificateEntry(Values.CERT_PROD_SUB_ALIAS, certificateProdSub);
			
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, KEYSTORE_PASS.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			SSLSocketFactory socketFactory = context.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
	}
	
	public static Key GetPrivateKey(boolean isProduction){
		int certType = isProduction ? Values.CERT_TYPE_PROD : Values.CERT_TYPE_TEST;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
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
		int certType = isProduction ? Values.CERT_TYPE_PROD : Values.CERT_TYPE_TEST;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
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
	
	public static String CalculateZKI(Invoice invoice){
		String returnString = Values.DEFAULT_ZKI;
		String temp = "";
		
		temp += GetFiscOib(!invoice.isTest);
		temp += new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(invoice.date);
		temp += invoice.invoiceNumber;
		temp += invoice.officeTag;
		temp += invoice.cashRegisterNumber;
		temp += ClientAppUtils.FloatToPriceString(invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue);
		
		try {
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign((PrivateKey)GetPrivateKey(!invoice.isTest));
			signature.update(temp.getBytes());
			byte[] signedBytes = signature.sign();
			byte[] digestedBytes = MessageDigest.getInstance("MD5").digest(signedBytes);
			
			char[] hexArray = "0123456789ABCDEF".toCharArray();
			char[] hexChars = new char[digestedBytes.length * 2];
			for (int i = 0; i < digestedBytes.length; i++) {
				int v = digestedBytes[i] & 0xFF;
				hexChars[i * 2] = hexArray[v >>> 4];
				hexChars[i * 2 + 1] = hexArray[v & 0x0F];
			}
			returnString = new String(hexChars).toLowerCase();
		} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException ex) {
			//ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		return returnString;
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
	
	public static String GetFiscOib(boolean isProduction){
		String toReturn = Licence.GetOIB();
		
		if(!isProduction){
			try {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				char[] password = GetPrivateCertificatePass(Values.CERT_TYPE_TEST).toCharArray();
				keyStore.load(new ByteArrayInputStream(GetPrivateCertificateBytes(Values.CERT_TYPE_TEST)), password);
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
	
	public static boolean IsFiscalisationInProgress(){
		return fiscalisationInProgress;
	}
}
