/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.print;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.BitSet;
import javax.imageio.ImageIO;

/**
 *
 * @author Matej
 */
public class PrintLogoUtils {
	
	private static class BitmapData {
		public BitSet dots;
		public int height;
		public int width;
	}
	
	private static BitmapData GetBitmapData(String bmpFileName){
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(bmpFileName));
		} catch (IOException ex) {}
		
		if(image == null)
			return null;
		
		int threshold = 127;
		int index = 0;
		BitSet dots = new BitSet(image.getHeight() * image.getWidth());
		
		for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++){
                Color color = new Color(image.getRGB(x, y));
                int luminance = (int)(color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11);
                dots.set(index, luminance < threshold);
                index++;
            }
        }
		
		BitmapData bitmapData = new BitmapData();
		bitmapData.width = image.getWidth();
		bitmapData.height = image.getHeight();
		bitmapData.dots = dots;
		return bitmapData;
	}
	
	public static String GetLogoString(String path){		
        if (!(new File(path).exists()))
            return "";
		
		BitmapData data = GetBitmapData(path);
		if(data == null)
			return "";
		
		BitSet dots = data.dots;
		int offset = 0;
		ByteArrayOutputStream bw = new ByteArrayOutputStream();

		//bw.write((char)0x1B);
        //bw.write('@');
		
		bw.write((char)0x1B);
        bw.write('3');
        bw.write((byte)24);
		
		while (offset < data.height){
			bw.write((char)0x1B);
			bw.write('*');         // bit-image mode
			bw.write((byte)33);    // 24-dot double-density
			bw.write((byte)((data.width ) % 256));  // width low byte
			bw.write((byte)((data.width ) / 256));  // width high byte
			for (int x = 0; x < data.width; ++x){
				for (int k = 0; k < 3; ++k)
				{
					byte slice = 0;
					for (int b = 0; b < 8; ++b)
					{
						int y = (((offset / 8) + k) * 8) + b;
						// Calculate the location of the pixel we want in the bit array.
						// It'll be at (y * width) + x.
						int i = (y * data.width) + x;

						// If the image is shorter than 24 dots, pad with zero.
						boolean v = false;
						if (i < dots.size()){
							v = dots.get(i);
						}
						slice |= (byte)((v ? 1 : 0) << (7 - b));
					}

					bw.write(slice);
				}
			}
			offset += 24;
			bw.write((byte) 10);
		}
		bw.write((char)0x1B);
        bw.write('3');
        bw.write((byte)30);
		
		/*bw.write((byte) 10);
		bw.write((byte) 10);
		bw.write((byte) 10);
		bw.write((byte) 10);
		bw.write((byte) 10);
		bw.write((byte) 10);
		
        bw.write((byte)29);
        bw.write((byte)86);
        bw.write((byte)66);
        bw.write((byte)0);*/

		byte[] bytes = bw.toByteArray();
		return new String(bytes);
	}
}
