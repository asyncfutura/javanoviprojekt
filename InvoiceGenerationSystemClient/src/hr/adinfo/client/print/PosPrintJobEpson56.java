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
public class PosPrintJobEpson56 extends PosPrintJob {

	@Override
	public void Init() {
		final byte[] initCommand = {27, 64};
		final byte[] setCodePageCommand = {27, 116, 18};
		final byte[] setCharacterSet = {27, 82, 0};
		final byte[] cancelUserDefinedCharacterSet = {27, 37, 0};
		final byte[] fontCommand = {27, 77, 1};
        commandSet += new String(initCommand);
        commandSet += new String(setCodePageCommand);
        commandSet += new String(setCharacterSet);
        commandSet += new String(cancelUserDefinedCharacterSet);
        commandSet += new String(fontCommand);
	}

	@Override
	public void Feed(int lines) {
		final byte[] feedCommand = {27, 100, (byte)lines};
        commandSet += new String(feedCommand);
	}

	@Override
	public void FeedBack(int lines) {
		final byte[] feedBackCommand = {27, 101, (byte)lines};
        commandSet += new String(feedBackCommand);
	}

	@Override
	public void AlignLeft() {
		final byte[] alignLeftCommand = {27, 97, 48};
        commandSet += new String(alignLeftCommand);
	}

	@Override
	public void AlignCenter() {
		final byte[] alignCenterCommand = {27, 97, 49};
        commandSet += new String(alignCenterCommand);
	}

	@Override
	public void AlignRight() {
		final byte[] alignRightCommand = {27, 97, 50};
        commandSet += new String(alignRightCommand);
	}

	@Override
	public void NewLine() {
		final byte[] newLineCommand = {10};
        commandSet += new String(newLineCommand);
	}

	@Override
	public void ReverseColorMode(boolean enabled) {
		final byte[] reverseModeColorOn = {29, 66, 1};
        final byte[] reverseModeColorOff = {29, 66, 0};
		if(enabled){
			commandSet += new String(reverseModeColorOn);
		} else {
			commandSet += new String(reverseModeColorOff);
		}
	}

	@Override
	public void Bold(boolean enabled) {
		final byte[] boldOnCommand = {27, 71, 1};
        final byte[] boldOffCommand = {27, 71, 0};
		if(enabled){
			commandSet += new String(boldOnCommand);
		} else {
			commandSet += new String(boldOffCommand);
		}
	}

	@Override
	public void DoubleSize(boolean enabled) {
		final byte[] boldOnCommand = {29, 33, 17};
        final byte[] boldOffCommand = {29, 33, 0};
		if(enabled){
			commandSet += new String(boldOnCommand);
		} else {
			commandSet += new String(boldOffCommand);
		}
	}
	
	@Override
	public void Underline(boolean enabled) {
		final byte[] underlineOnCommand = {27, 45, 50};
        final byte[] underlineOffCommand = {27, 45, 48};
		if(enabled){
			commandSet += new String(underlineOnCommand);
		} else {
			commandSet += new String(underlineOffCommand);
		}
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
		final byte[] cutCommand = {29, 86, 66, 0};
        commandSet += new String(cutCommand);
	}
	
	@Override
	public int GetLineCharCount(){
		return 56;
	}
	
	@Override
	public String GetCommandSet() {
		String posPrintJobString = commandSet;
		posPrintJobString = posPrintJobString.replace("Ž", Character.toString((char)166));
		posPrintJobString = posPrintJobString.replace("Š", Character.toString((char)230));
		posPrintJobString = posPrintJobString.replace("Đ", Character.toString((char)209));
		posPrintJobString = posPrintJobString.replace("Ć", Character.toString((char)143));
		posPrintJobString = posPrintJobString.replace("Č", Character.toString((char)172));
		posPrintJobString = posPrintJobString.replace("ž", Character.toString((char)167));
		posPrintJobString = posPrintJobString.replace("š", Character.toString((char)231));
		posPrintJobString = posPrintJobString.replace("đ", Character.toString((char)208));
		posPrintJobString = posPrintJobString.replace("ć", Character.toString((char)134));
		posPrintJobString = posPrintJobString.replace("č", Character.toString((char)159));	
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
		commandSet += new String(qrData.getBytes());
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
