/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Matej
 */
public class CustomCellRenderer extends DefaultListCellRenderer {
	
	private ArrayList<Color> colors;
	
	@Override
	public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
		Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
		if(index < colors.size()){
			c.setBackground(colors.get(index));
		} else {
			c.setBackground(Color.white);
		}
		return c;
	}
	
	public void SetColors(ArrayList<Color> colors){
		this.colors = colors;
	}
	
	public Color GetColor(int index){
		if(index < colors.size()){
			return colors.get(index);
		} else {
			return Color.white;
		}
	}
}
