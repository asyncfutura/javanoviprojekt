/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.services;

import hr.adinfo.client.ClientApp;
import hr.adinfo.client.ClientAppServerAppClient;
import hr.adinfo.client.ClientAppUtils;

/**
 *
 * @author Matej
 */
public class LocalServerServerAppPingService {
	
	private static final int SERVICE_LOOP_SUCCESS_DELAY_SECONDS = 60 * 10;
	
	private static boolean pingServerApp;
	private static int counter;
	
	public static void Init(){
		pingServerApp = true;
		counter = 0;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 3);
				} catch (InterruptedException ex) {}
				
				while(true){
					if(ClientApp.appClosing)
						break;
					
					if(pingServerApp){
						boolean success = TryPingServerApp();
						if(success){
							pingServerApp = false;
							counter = 0;
						}
                                                
                                                TryPingClientApp();
					}
					
					++counter;
					if(counter > SERVICE_LOOP_SUCCESS_DELAY_SECONDS){
						pingServerApp = true;
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {}
				}
			}
		}).start();
	}
	
	private static boolean TryPingServerApp(){
		boolean success = false;
		
		boolean haveUnfiscalizedInvoices = ClientAppUtils.HaveUnfiscalizedInvoices(true) || ClientAppUtils.HaveUnfiscalizedInvoices(false);
		success = ClientAppServerAppClient.GetInstance().ServerAppPing(haveUnfiscalizedInvoices);
		
		return success;
	}
        
        private static Integer TryPingClientApp(){
		Integer haveTotal = 0;
		
		haveTotal = ClientAppUtils.GetTotalValue();
		
		return haveTotal;
	}
	
	public static void PingServerApp(){
		pingServerApp = true;
	}
}
