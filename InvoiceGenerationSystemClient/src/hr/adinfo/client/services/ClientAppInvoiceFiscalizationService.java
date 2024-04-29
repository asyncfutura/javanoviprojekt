/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.fiscalization.Fiscalization;
import hr.adinfo.utils.licence.Licence;

/**
 *
 * @author Matej
 */
public class ClientAppInvoiceFiscalizationService {
	
	private static final int SERVICE_LOOP_DELAY_SECONDS = 5;
	private static final int INVOICES_PER_LOOP = 10;
	
	public static void Init(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					CheckUnfiscalizedInvoices();
					
					try {
						Thread.sleep(1000 * SERVICE_LOOP_DELAY_SECONDS);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static void CheckUnfiscalizedInvoices(){
		if(Licence.IsControlApp()){
			return;
		}
		
		synchronized(Fiscalization.fiscalizationInvoiceUploadLock){
			if(Fiscalization.IsFiscalisationInProgress())
				return;
			
			for (int i = 0; i < INVOICES_PER_LOOP; ++i){
				Invoice invoice = ClientAppUtils.GetUnfiscalizedInvoice(true, true);
				
				if(invoice == null)
					break;
				
				if(Fiscalization.IsFiscalisationInProgress())
					return;
				
				Fiscalization.FiscalizeInvoiceUnsynchronized(invoice, false);
			}
			
			for (int i = 0; i < INVOICES_PER_LOOP; ++i){
				Invoice invoice = ClientAppUtils.GetUnfiscalizedInvoice(false, true);
				
				if(invoice == null)
					break;
				
				if(Fiscalization.IsFiscalisationInProgress())
					return;
				
				Fiscalization.FiscalizeInvoiceUnsynchronized(invoice, false);
			}
			
			if(Licence.IsMasterLocalServer()){
				for (int i = 0; i < INVOICES_PER_LOOP; ++i){
					Invoice invoice = ClientAppUtils.GetUnfiscalizedInvoice(true, false);

					if(invoice == null)
						break;

					if(Fiscalization.IsFiscalisationInProgress())
						return;
					
					Fiscalization.FiscalizeInvoiceUnsynchronized(invoice, false);
				}

				for (int i = 0; i < INVOICES_PER_LOOP; ++i){
					Invoice invoice = ClientAppUtils.GetUnfiscalizedInvoice(false, false);

					if(invoice == null)
						break;
					
					if(Fiscalization.IsFiscalisationInProgress())
						return;
					
					Fiscalization.FiscalizeInvoiceUnsynchronized(invoice, false);
				}
			}
		}
	}
}
