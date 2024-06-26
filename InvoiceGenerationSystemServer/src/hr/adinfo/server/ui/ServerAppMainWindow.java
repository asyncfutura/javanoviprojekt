/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.server.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Matej
 */
public class ServerAppMainWindow extends javax.swing.JFrame {
	/**
	 * Creates new form ServerAppWindow
	 */
	
	private final int TEXT_AREA_MAX_LINES = 200;
	
	public ServerAppMainWindow() {
		initComponents();
		
		((DefaultCaret)jTextArea1.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
	
	public void PrintLog(String message){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				jTextArea1.append(message + System.lineSeparator());
				RefreshTextArea();
			}
		});
		
		
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		String filePathString = Paths.get("").toAbsolutePath() + File.separator + "log" + File.separator + year + File.separator + month + File.separator + day + ".txt";
		Path path = Paths.get(filePathString);
		
		try {
			Files.createDirectories(path.getParent());
			if(Files.notExists(path)){
				Files.createFile(path);
			}
			Files.write(path, (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {}
	}
	
	public void RefreshTextArea(){
		int numLinesToTrunk = jTextArea1.getLineCount() - TEXT_AREA_MAX_LINES;
		if(numLinesToTrunk > 0) {
			try {
				int posOfLastLineToTrunk = jTextArea1.getLineEndOffset(numLinesToTrunk - 1);
				jTextArea1.replaceRange("", 0, posOfLastLineToTrunk);
			} catch (BadLocationException ex) {}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabelVersion = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setFocusable(false);

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(jTextArea1);

        jLabelVersion.setText("Verzija 1.0.61");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelVersion)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                        .addGap(25, 25, 25))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelVersion)
                .addGap(6, 6, 6))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
