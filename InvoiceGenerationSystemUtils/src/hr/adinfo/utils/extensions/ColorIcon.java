/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *
 * @author Matej
 */
public class ColorIcon implements Icon {

	private Color color;
    private int width;
    private int height;
	
	public ColorIcon(Color color, int width, int height){
		this.color = color;
        this.width = width;
        this.height = height;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
        g.fillRect(x, y, width, height);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
}
