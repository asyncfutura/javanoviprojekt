/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/**
 *
 * @author Matej
 */
public class KeyAdapterFloatField extends KeyAdapter {
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if(c == '.'){
			String text = ((JTextField) (e.getComponent())).getText();
			for (int i = 0; i < text.length(); i++) {
				if (',' == text.charAt(i)){
					java.awt.Toolkit.getDefaultToolkit().beep();
					e.consume();
					break;
				}
			}
			
			e.setKeyChar(',');
		} else if (c == ','){
			String text = ((JTextField) (e.getComponent())).getText();
			for (int i = 0; i < text.length(); i++) {
				if (',' == text.charAt(i)){
					java.awt.Toolkit.getDefaultToolkit().beep();
					e.consume();
					break;
				}
			}
		} else if (!Character.isDigit(c) && c != ','){
			java.awt.Toolkit.getDefaultToolkit().beep();
			e.consume();
		}
	}
}
