/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils.licence;

import hr.adinfo.utils.Values;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Base64;
import java.util.Date;

/**
 *
 * @author Matej
 */
public class UniqueComputerID {
	public static String GetUniqueID(){
		File file = new File(Values.PATH_UNIQUE_COMPUTER_ID);
		file.getParentFile().mkdirs();
		
		if(file.exists()){
			try {
				byte[] idBytes = Base64.getDecoder().decode(Files.readAllBytes(file.toPath()));
				String idString = new String(idBytes);
				BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				//System.err.println("idString:     " + idString);
				//System.err.println("created:      " + attributes.creationTime().toMillis());
				//System.err.println("lastModified: " + attributes.lastModifiedTime().toMillis());
				//System.err.println("lastAccess:   " + attributes.lastAccessTime().toMillis());
				Long lastModifiedTime = attributes.lastModifiedTime().toMillis();
				Long createdTime = attributes.creationTime().toMillis();
				if (Long.toString(createdTime).equals(idString) && Long.toString(lastModifiedTime).equals(idString)){
					return idString;
				}
			} catch (Exception ex) { }
		}
		
		// Id invalid - delete file and create new
		try {
			Files.deleteIfExists(file.toPath());
		} catch (Exception ex) { }
		
		Date currentDate = new Date((new Date().getTime() / new Long(1000)) * new Long(1000));
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(Base64.getEncoder().encode(Long.toString(currentDate.getTime()).getBytes()));
			fos.flush();
			fos.close();

			BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(file.getPath()), BasicFileAttributeView.class);
			FileTime time = FileTime.fromMillis(currentDate.getTime());
			attributes.setTimes(time, time, time);

			//file.setLastModified(currentDate.getTime());
			return Long.toString(currentDate.getTime());
		} catch(Exception e){}
		
		return "---------";
	}
}
