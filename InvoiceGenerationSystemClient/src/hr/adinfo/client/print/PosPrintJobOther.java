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
public class PosPrintJobOther extends PosPrintJob {

	private int lineCharCount;
	
	public PosPrintJobOther(int lineCharCount){
		this.lineCharCount = lineCharCount;
	}
	
	@Override
	public void Init() {
		
	}

	@Override
	public void Feed(int lines) {
		for(int i = 0; i < lines; ++i){
			NewLine();
		}
	}

	@Override
	public void FeedBack(int lines) {
		
	}

	@Override
	public void AlignLeft() {
		
	}

	@Override
	public void AlignCenter() {
		
	}

	@Override
	public void AlignRight() {
		
	}

	@Override
	public void NewLine() {
        commandSet += System.lineSeparator();
	}

	@Override
	public void ReverseColorMode(boolean enabled) {
		
	}

	@Override
	public void Bold(boolean enabled) {
		
	}
	
	@Override
	public void DoubleSize(boolean enabled) {
		
	}

	@Override
	public void Underline(boolean enabled) {
		
	}

	@Override
	public void LineSeparator() {
		StringBuilder lineBuilder = new StringBuilder();
			for (int i = 0; i < GetLineCharCount(); i++){
				lineBuilder.append("-");
			}
        commandSet += lineBuilder.toString();
		
		NewLine();
	}
	
	@Override
	public void DoubleLineSeparator() {
		StringBuilder lineBuilder = new StringBuilder();
			for (int i = 0; i < GetLineCharCount(); i++){
				lineBuilder.append("=");
			}
        commandSet += lineBuilder.toString();
		
		NewLine();
	}

	@Override
	public void Cut() {
		Feed(7);
	}
	
	@Override
	public int GetLineCharCount(){
		return lineCharCount;
	}
	
	@Override
	public String GetCommandSet() {
		String posPrintJobString = commandSet;
		posPrintJobString = posPrintJobString.replace("Ž", "Z");
		posPrintJobString = posPrintJobString.replace("Š", "S");
		posPrintJobString = posPrintJobString.replace("Đ", "D");
		posPrintJobString = posPrintJobString.replace("Ć", "C");
		posPrintJobString = posPrintJobString.replace("Č", "C");
		posPrintJobString = posPrintJobString.replace("ž", "z");
		posPrintJobString = posPrintJobString.replace("š", "s");
		posPrintJobString = posPrintJobString.replace("đ", "d");
		posPrintJobString = posPrintJobString.replace("ć", "c");
		posPrintJobString = posPrintJobString.replace("č", "c");
		
		return posPrintJobString;
	};
        
    @Override
	public void PrintQRCode(String qrData) {
		byte[] content = qrData.getBytes();
		int len = content.length + 3;
		byte pL = (byte) (len % 256);
		byte pH = (byte) (len / 256);

		final byte[] f1 = {29, 40, 107, 4, 49, 65, 50, 0};
		commandSet += new String(f1);
		final byte[] f2 = {29, 40, 107, 3, 0, 49, 67, 5};
		commandSet += new String(f2);
		final byte[] f3 = {29, 40, 107, 3, 0, 49, 69, 48};
		commandSet += new String(f3);
		final byte[] f4 = {29, 40, 107, pL, pH, 49, 80, 48};
		commandSet += new String(f4);
		commandSet += new String(content);
		final byte[] f5 = {29, 40, 107, 3, 0, 49, 81, 48};
		commandSet += new String(f5);
		NewLine();
	}
        
        @Override
        public void OpenCaddy(){
            final byte[] openCommand = {(byte)27, (byte)112, (byte)0, (byte)25, (byte)250};
            commandSet += new String(openCommand);
        }
}
