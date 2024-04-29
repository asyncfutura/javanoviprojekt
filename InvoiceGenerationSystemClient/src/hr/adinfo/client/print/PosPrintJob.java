/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.print;

/**
 *
 * @author Matej
 */
public abstract class PosPrintJob {
	public String commandSet = "";
		
	public abstract void Init();
	public abstract void Feed(int lines);
	public abstract void FeedBack(int lines);
	public abstract void AlignLeft();
	public abstract void AlignCenter();
	public abstract void AlignRight();
	public abstract void NewLine();
	public abstract void ReverseColorMode(boolean enabled);
	public abstract void Bold(boolean enabled);
	public abstract void DoubleSize(boolean enabled);
	public abstract void Underline(boolean enabled);
	public abstract void LineSeparator();
	public abstract void DoubleLineSeparator();
	public abstract void Cut();
	public abstract int GetLineCharCount();
	public abstract String GetCommandSet();
        public abstract void PrintQRCode(String qrData);
        public abstract void OpenCaddy();
	
	public void Reset() { commandSet = ""; }
	public void AddText(String text, boolean newLine) {
		commandSet += text;
		if(newLine){
			NewLine();
		}
	}
}
