package name.zicat.spell.check.biz.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import name.zicat.utils.io.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 
 * @author zicat
 *
 */
public class SecurityUtils {
	
	/**
	 * 
	 * @param file
	 * @param algorith
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static final String md5(final File file) throws IOException {
		
		if(file == null) {
			throw new NullPointerException("file is null");
		}
		
		if(!file.isFile() || !file.exists()) {
			throw new IOException(file.getName() + " is not a file or not exist");
		}
		
		 FileInputStream fileInputStream = null;
		 try {
			 fileInputStream = new FileInputStream(file);
			 return DigestUtils.md5Hex(fileInputStream);
		 } finally {
			 IOUtils.closeQuietly(fileInputStream);
		 }
	}
}
