/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *
 * @author Matej
 */
public class CustomFocusListener implements FocusListener {
	private JTextField jTextField;

	public CustomFocusListener(JTextField jTextField){
		this.jTextField = jTextField;
	}

	@Override
	public void focusGained(FocusEvent e) {
		jTextField.select(0, jTextField.getText().length());
	}

	@Override
	public void focusLost(FocusEvent e) {
		jTextField.select(0, 0);
	}
}