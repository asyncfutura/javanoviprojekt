/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.extensions;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Matej
 */
public class CustomTableModel extends DefaultTableModel {
	
	int editableColumnIndex = -1;
	
	public CustomTableModel(){
		super();
	}
	
	public CustomTableModel(int editableColumnIndex){
		super();
		this.editableColumnIndex = editableColumnIndex;
	}
	
	@Override
	public boolean isCellEditable(int row, int column){
		return (column == editableColumnIndex);
	}
}
