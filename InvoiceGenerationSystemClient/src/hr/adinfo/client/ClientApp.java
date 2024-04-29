/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client;

import hr.adinfo.client.print.PrintUtils;
import hr.adinfo.utils.certificates.CertificateManager;
import hr.adinfo.client.services.ClientAppLocalInvoiceUploadService;
import hr.adinfo.client.services.ClientAppConnectionCheckService;
import hr.adinfo.client.services.ClientAppInvoiceFiscalizationService;
import hr.adinfo.client.services.ClientAppLicenceCertificateExpiryCheckService;
import hr.adinfo.client.services.ClientAppUpdater;
import hr.adinfo.client.services.LocalServerNotificationsService;
import hr.adinfo.client.services.LocalServerServerAppPingService;
import hr.adinfo.client.services.MasterLocalServerServerAppSync;
import hr.adinfo.client.ui.ClientAppActivationWindow;
import hr.adinfo.client.ui.ClientAppDataCheckWindow;
import hr.adinfo.client.ui.ClientAppLoginDialog;
import hr.adinfo.client.ui.ClientAppMainWindow;
import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Utils;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import hr.adinfo.utils.extensions.DummyFrame;
import hr.adinfo.utils.licence.Licence;
import hr.adinfo.utils.licence.LicenceQuery;
import hr.adinfo.utils.licence.LicenceQueryResponse;
import hr.adinfo.utils.licence.UniqueComputerID;
import hr.adinfo.utils.licence.UniqueComputerIdentifier;
import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Matej
 */
public class ClientApp {
	private static ClientApp clientApp;
	private static ServerSocket clientAppLock;
	
	public static final Object databaseTransactionLock = new Object();
	public static boolean appClosing;
	
	private LoadingDialog loadingDialog;
	
	private ClientApp(){
		appClosing = false;
		loadingDialog = new LoadingDialog(null, true);
		
		TryStartClientApp();
	}
	
	public void TryStartClientApp(){
		ShowLoadingDialog();
		
		try {
			StartClientApp();
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
			HideLoadingDialog();
			JOptionPane.showMessageDialog(null, "Došlo je do pogreške tijekom pokretanja aplikacije." + System.lineSeparator() 
					+ "Molimo pokušajte ponovno pokrenuti aplikaciju.");
			OnAppClose();
		}
	}
	
	private void StartClientApp() throws Exception {
		ClientAppUpdater.Init();
		ClientAppUpdater.WaitForFirstUpdateCheck();
		
		ClientAppServerAppClient.Init();
		RefreshLicence(false);
		PrintUtils.ClearPrintFolder();
                		
		if(!Licence.IsLicenceActivated()){
			ShowActivatonWindow();
		} else if(!Licence.IsLicenseComputerIdValid()){
			ShowLicenceIdNotValidWindow();
		} else if(!Licence.IsLicenseDateValid()){
			ShowLicenceExpiredWindow();
		} else if(UpdatesAvailable()){
			// TODO
		} else {
			InitClientAppComponents();
			ShowLoginWindow();
			ClientAppConnectionCheckService.Init();
			ClientAppLicenceCertificateExpiryCheckService.Init();
		}
	}
	
	public void RefreshLicence(boolean shutdownOnChange){
		if(Licence.IsLicenceActivated()	&& Licence.IsControlApp() && Licence.GetActivationKey().equals("ACTIVATIONKEY")){
			return;
		}
		
		boolean oldIsMaster = Licence.IsMasterLocalServer();
		boolean oldIsLocal = Licence.IsLocalServerButNoMasterLocalServer();
		
		String activationKey = Licence.GetActivationKey();
		if(!activationKey.matches("\\w{4}-\\w{4}-\\w{4}-\\w{4}")){
			try {
				Licence.SaveLicence("".getBytes());
			} catch (Exception ex) {}
			return;
		}
		
		String uniqueId = UniqueComputerID.GetUniqueID();
		String message = activationKey + Values.LICENCE_SPLIT_STRING + uniqueId;
		
		final JDialog loadingDialog = new LoadingDialog(null, true);
		
		LicenceQuery licenceQuery = new LicenceQuery(Values.LICENCE_QUERY_REFRESH, message);
		ServerQueryTask serverQueryTask = new ServerQueryTask(loadingDialog, licenceQuery, ClientAppServerAppClient.GetInstance(), ClientAppLogger.GetInstance());

		serverQueryTask.execute();
		
		//loadingDialog.setVisible(true);
		while(!serverQueryTask.isDone()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
		
		if(!serverQueryTask.isDone()){
			serverQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = serverQueryTask.get();
				byte[] licence = null;
				int errorCode = -1;
				if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
					licence = ((LicenceQueryResponse) serverResponse).licenceBytes;
					errorCode = ((LicenceQueryResponse) serverResponse).licenceErrorCode;
					
					if(errorCode == Values.LICENCE_ERROR_CODE_REFRESH_SUCCESS && licence != null){
						try {
							Licence.SaveLicence(licence);
							CertificateManager.SaveCertificate(Values.CERT_DEMO_ROOT_ALIAS, ((LicenceQueryResponse) serverResponse).certDemoRootBytes);
							CertificateManager.SaveCertificate(Values.CERT_DEMO_SUB_ALIAS, ((LicenceQueryResponse) serverResponse).certDemoSubBytes);
							CertificateManager.SaveCertificate(Values.CERT_PROD_ROOT_ALIAS, ((LicenceQueryResponse) serverResponse).certProdRootBytes);
							CertificateManager.SaveCertificate(Values.CERT_PROD_SUB_ALIAS, ((LicenceQueryResponse) serverResponse).certProdSubBytes);	
						} catch (Exception ex) {}
					} else {
						try {
							Licence.SaveLicence("".getBytes());
						} catch (Exception ex) {}
					}
				}
			} catch (InterruptedException | ExecutionException ex) {}
		}
				
		if(shutdownOnChange && ((oldIsMaster && !Licence.IsMasterLocalServer()) || (oldIsLocal && !Licence.IsLocalServerButNoMasterLocalServer()))){
			JOptionPane.showMessageDialog(null, "Došlo je do promjene tipa vaše licence. Molimo ponovno pokrenite aplikaciju.");
			OnAppClose();
		}
	}
	
	private void ShowActivatonWindow(){
		HideLoadingDialog();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Window licenceActivationWindow = new ClientAppActivationWindow();
				licenceActivationWindow.setVisible(true);
			}
		});
	}
	
	private void ShowLicenceIdNotValidWindow(){
		HideLoadingDialog();
		
		ClientAppLogger.GetInstance().ShowMessage("Licenca ne odgovara ovom računalu. Molimo unesite valjani aktivacijski ključ.");
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Window licenceActivationWindow = new ClientAppActivationWindow();
				licenceActivationWindow.setVisible(true);
			}
		});
	}
	
	private void ShowLicenceExpiredWindow(){
		HideLoadingDialog();
		
		ClientAppLogger.GetInstance().ShowMessage("Licenca je istekla. Molimo unesite novi aktivacijski ključ, ili produžite trenutnu licencu.");
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Window licenceActivationWindow = new ClientAppActivationWindow();
				licenceActivationWindow.setVisible(true);
			}
		});
	}
	
	public void OnLicenceActivation(){
		new Thread(() -> {
			TryStartClientApp();
		}).start();
	}
	
	private void ShowLoginWindow() {
		HideLoadingDialog();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				ClientAppLoginDialog dialog = new ClientAppLoginDialog(new DummyFrame(""), true, true, -1);
				dialog.setVisible(true);
				if (dialog.loginSuccess) {
					if (dialog.getParent() != null && dialog.getParent() instanceof DummyFrame) {
						((DummyFrame) dialog.getParent()).dispose();
					}
					long dayDiff = getDayFromStart();
					if (dayDiff < 5) {
						new ClientAppDataCheckWindow().setVisible(true);
					} else {
						Utils.DisposeDialog(dialog);
						java.awt.EventQueue.invokeLater(new Runnable() {
							public void run() {
								new ClientAppMainWindow().setVisible(true);
							}
						});
					}
				} else {
					OnAppClose();
				}
			}
		});
	}
        
    private long getDayFromStart() {
		long difDay = 0;

		DatabaseQuery databaseQuery = new DatabaseQuery("SELECT I_DATE FROM USER1.LOCAL_INVOICES FETCH FIRST 1 ROWS ONLY");
		ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

		databaseQueryTask.execute();
		loadingDialog.setVisible(true);
		if (!databaseQueryTask.isDone()) {
			databaseQueryTask.cancel(true);
		} else {
			try {
				ServerResponse serverResponse = databaseQueryTask.get();
				DatabaseQueryResult databaseQueryResult = null;
				if (serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS) {
					databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
				}
				if (databaseQueryResult != null) {
					if (databaseQueryResult.next()) {
						String datum = databaseQueryResult.getString(0);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Date date = dateFormat.parse(datum);
						Date dtToday = new Date();
						Instant d1 = date.toInstant();
						Instant d2 = dtToday.toInstant();
						difDay = ChronoUnit.DAYS.between(d1, d2);
						//Utils.DisposeDialog(this);
					}
				}
			} catch (Exception ex) {
				ClientAppLogger.GetInstance().ShowErrorLog(ex);
			}
		}

		return difDay;
	}
	
	private void ShowLoadingDialog(){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				loadingDialog.setVisible(true);
			}
		});
	}
	private void HideLoadingDialog(){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				loadingDialog.setVisible(false);
			}
		});
	}
	
	private void InitClientAppComponents() throws Exception {
		if(Licence.IsMasterLocalServer()){
			MasterLocalServer.Init();
			MasterLocalServerServerAppSync.Init();
		}

		if(Licence.IsLocalServer()){
			LocalServer.Init();
			LocalServerServerAppPingService.Init();
			LocalServerNotificationsService.Init();
			ClientAppLocalInvoiceUploadService.Init();
			ClientAppInvoiceFiscalizationService.Init();
		}

		ClientAppLocalServerClient.Init();
		ClientAppSettings.InitCurrentYear();
	}
	
	private boolean UpdatesAvailable(){
		// TODO
		return false;	
	}
	
	private static void Init(){
		if(clientApp == null){
			// Check multiple instances
			if(clientAppLock != null){
				ClientAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
			clientAppLock = Utils.CheckMultipleInstances(Values.CLIENT_APP_LOCK_PORT);
			if(clientAppLock == null || (clientAppLock != null && !clientAppLock.isBound())){
				ClientAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
			
			clientApp = new ClientApp();
		}
	}
	
	public static ClientApp GetInstance(){
		return clientApp;
	}
	
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			/*for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					break;
				}
			}*/
			/*javax.swing.UIManager.setLookAndFeel(new WindowsLookAndFeel(){
				 @Override
				 public boolean isSupportedLookAndFeel() {
					return true;
				}
			});*/
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		//</editor-fold>
		
		// Set focus traversal keys
		Set<AWTKeyStroke> setForward = new HashSet<AWTKeyStroke>(KeyboardFocusManager
			.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		Set<AWTKeyStroke> setBackward = new HashSet<AWTKeyStroke>(KeyboardFocusManager
			.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		setForward.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		setForward.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		setBackward.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		setBackward.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalKeys(
			KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalKeys(
			KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward);
		
		// Set select text on focus
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener(){
			public void propertyChange(final PropertyChangeEvent e){
				if (e.getNewValue() instanceof JTextField){
					//  invokeLater needed for JFormattedTextField
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							JTextField textField = (JTextField)e.getNewValue();
							textField.selectAll();
						}
					});
				}
			}
		});
		
		ClientAppLogger.GetInstance().LogMessage("Init start");
		ClientApp.Init();
		ClientAppLogger.GetInstance().LogMessage("Init finish - " + Licence.GetCurrentLicence());
	}
	
	public void OnAppClose(){
		if(appClosing)
			return;
		
		ClientAppLogger.GetInstance().LogMessage("Shutdown start");
		
		appClosing = true;
		
		if(ClientAppServerAppClient.GetInstance() != null)
			ClientAppServerAppClient.GetInstance().OnAppClose();

		if(ClientAppLocalServerClient.GetInstance() != null)
			ClientAppLocalServerClient.GetInstance().OnAppClose();
		
		if(LocalServer.GetInstance() != null)
			LocalServer.GetInstance().OnAppClose();
		
		if(LocalServerMasterLocalServerClient.GetInstance() != null)
			LocalServerMasterLocalServerClient.GetInstance().OnAppClose();
		
		if(MasterLocalServer.GetInstance() != null)
			MasterLocalServer.GetInstance().OnAppClose();
		
		LocalServerHost.OnAppClose();
		MasterLocalServerHost.OnAppClose();
		
		try {
			if(clientAppLock != null){
				clientAppLock.close();
				clientAppLock = null;
			}
		} catch (Exception ex) {
			ClientAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		ClientAppLogger.GetInstance().LogMessage("Shutdown finish");
		
		System.exit(0);
	}
}
