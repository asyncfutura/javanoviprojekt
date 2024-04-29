/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils;

/**
 *
 * @author Matej
 */
public interface LoggerInterface {
	public void ShowMessage(String message);
	public void LogMessage(String message);
	public void LogMessageDebug(String message);
	public void ShowErrorLog(Exception ex);
	public void LogError(Exception ex);
}
