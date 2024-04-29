/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control;

import hr.adinfo.control.ui.ControlAppLoginDialog;
import hr.adinfo.control.ui.ControlAppMainWindow;
import static hr.adinfo.utils.Utils.CheckMultipleInstances;
import static hr.adinfo.utils.Utils.DisposeDialog;
import static hr.adinfo.utils.Values.*;
import hr.adinfo.utils.extensions.DummyFrame;
import java.awt.event.WindowAdapter;
import java.net.ServerSocket;

/**
 *
 * @author Matej
 */
public class ControlApp {
	private static ControlApp licencesUpdatesControlApp;
	private static ServerSocket controlAppLock;
	private ControlAppMainWindow licencesUpdatesControlAppWindow;
	
	public static boolean appClosing;
	public static int controlAppUserType;
	
	private ControlApp(){
		appClosing = false;
		controlAppUserType = -1;
		
		try {
			StartLicencesUpdatesControlApp();
		} catch (Exception ex) {
			ControlAppLogger.GetInstance().ShowErrorLog(ex);
			
			if(licencesUpdatesControlAppWindow != null)
				DisposeDialog(licencesUpdatesControlAppWindow);
		}
	}
	
	private void StartLicencesUpdatesControlApp() throws Exception {
		ControlAppServerAppClient.Init();
		ControlAppUpdater.Init();

		licencesUpdatesControlAppWindow = new ControlAppMainWindow();
		licencesUpdatesControlAppWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				OnAppClose();
			}

			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				OnAppClose();
			}
		});
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				ControlAppLoginDialog dialog = new ControlAppLoginDialog(new DummyFrame(""), true);
				dialog.setVisible(true);
				if(dialog.loginSuccess){
					if (dialog.getParent() != null && dialog.getParent() instanceof DummyFrame) {
						((DummyFrame)dialog.getParent()).dispose();
					}
					licencesUpdatesControlAppWindow.RefreshUserPermissions();
					licencesUpdatesControlAppWindow.setVisible(true);
				} else {
					OnAppClose();
				}
			}
		});
	}
	
	private static void Init(){
		if(licencesUpdatesControlApp == null){
			// Check multiple instances
			if(controlAppLock != null){
				ControlAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
			controlAppLock = CheckMultipleInstances(CONTROL_APP_LOCK_PORT);
			if(controlAppLock == null || (controlAppLock != null && !controlAppLock.isBound())){
				ControlAppLogger.GetInstance().ShowMessage("Aplikacija je već pokrenuta!");
				return;
			}
			
			licencesUpdatesControlApp = new ControlApp();
		}
	}
	
	public static ControlApp GetInstance(){
		return licencesUpdatesControlApp;
	}
	
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		/*try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
				//if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(ControlApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(ControlApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(ControlApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ControlApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}*/
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ControlAppLogger.GetInstance().ShowErrorLog(ex);
		}
		//</editor-fold>
		
		ControlApp.Init();
	}
	
	public void OnAppClose(){
		appClosing = true;
		
		if(ControlAppServerAppClient.GetInstance() != null)
			ControlAppServerAppClient.GetInstance().OnAppClose();
		
		try {
			if(controlAppLock != null){
				controlAppLock.close();
				controlAppLock = null;
			}
		} catch (Exception ex) {
			ControlAppLogger.GetInstance().ShowErrorLog(ex);
		}
		
		System.exit(0);
	}
}