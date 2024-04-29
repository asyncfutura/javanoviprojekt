/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Scanner;
import javax.swing.JTextField;

/**
 *
 * @author Matej
 */
public class CustomKeyAdapter extends KeyAdapter {
	private JTextField jTextField;
	private int minValue, maxValue;

	public CustomKeyAdapter(JTextField jTextField, int minValue, int maxValue){
		this.jTextField = jTextField;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		String rawText = jTextField.getText();
		if(jTextField.getSelectionStart() != jTextField.getSelectionEnd()){
			String part1 = rawText.substring(0, jTextField.getSelectionStart());
			String part2 = rawText.substring(jTextField.getSelectionEnd(), rawText.length());
			rawText = part1 + part2;
		}
		String text = rawText.trim() + e.getKeyChar();
		int number = 0;
		Scanner scanner = new Scanner(text).useDelimiter("\\D+");
		if(scanner.hasNextInt()){
			number = scanner.nextInt();
			if(number < minValue)
				number = minValue;
			if(number > maxValue)
				number = maxValue;

			jTextField.setText(String.valueOf(number));
		} else {
			jTextField.setText("");
		}

		e.consume();
	}
}