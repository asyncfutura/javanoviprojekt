/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.control;

import javax.swing.JOptionPane;
import hr.adinfo.utils.LoggerInterface;

/**
 *
 * @author Matej
 */
public class ControlAppLogger implements LoggerInterface {

	private static ControlAppLogger controlAppLogger;
	
	public static ControlAppLogger GetInstance(){
		if(controlAppLogger == null){
			controlAppLogger = new ControlAppLogger();
		}
		
		return controlAppLogger;
	}
	
	@Override
	public void ShowMessage(String message) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message);
			}
		});
	}

	@Override
	public void LogMessage(String message) {
		// TODO log to file
	}
	
	@Override
	public void LogMessageDebug(String message) {
		// TODO
	}
	
	@Override
	public void ShowErrorLog(Exception ex) {
		int depth = 20;
		String stackTraceMessage = "";
		for(int i = 0; i < ex.getStackTrace().length && i < depth; ++i){
			stackTraceMessage = stackTraceMessage.concat(ex.getStackTrace()[i].toString() + System.lineSeparator());
		}
		
		stackTraceMessage = stackTraceMessage.concat(System.lineSeparator());
		
		if(ex.getCause() != null){
			for(int i = 0; i < ex.getCause().getStackTrace().length && i < depth; ++i){
				stackTraceMessage = stackTraceMessage.concat(ex.getCause().getStackTrace()[i].toString() + System.lineSeparator());
			}
		}
		
		final String message = stackTraceMessage;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message + System.lineSeparator() + ex.getMessage());
			}
		});
		
		LogError(ex);
	}
	
	@Override
	public void LogError(Exception ex) {
		// TODO log to file
	}
}
