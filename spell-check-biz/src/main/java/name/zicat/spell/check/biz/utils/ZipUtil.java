package name.zicat.spell.check.biz.utils;

import name.zicat.spell.check.client.utils.Constants;

import name.zicat.utils.file.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Lz31
 * @date 2017/06/23
 */
public class ZipUtil {

    public static final String SUFFIX_ZIP = ".zip";
    
    /**
     * 
     * @param files
     * @param zipFile
     * @throws IOException
     */
    public static void compressFiles2Zip(File[] files, File zipFile) throws IOException {

        if (files == null || files.length == 0) {
            throw new IOException("No file to compress.");
        }

        if (!isZipFile(zipFile.getName())) {
            throw new IOException("Destination File Name must end with zip,Error Name = " + zipFile.getName());
        }

        FileUtils.createDirIfNeed(zipFile.getCanonicalFile().getParentFile());
        
        ZipArchiveOutputStream zaos = null;
        try {
        	zaos = new ZipArchiveOutputStream(zipFile);
            zaos.setUseZip64(Zip64Mode.AsNeeded);
            for (File file : files) {
                if (file != null) {
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getName());
                    zaos.putArchiveEntry(zipArchiveEntry);
                    InputStream is = null;
                    FileInputStream fis = null;
                    try {
                    	fis = new FileInputStream(file);
                    	is = new BufferedInputStream(fis);
                    	IOUtils.copy(is, zaos);
                    } finally {
                    	try {
                    		if(fis != null)
                    			fis.close();
                    	} finally {
                    		if(is != null)
                    			is.close();
                    	}
                    }
                    zaos.closeArchiveEntry();
                }
            }
            zaos.finish();
        } finally {
        	if(zaos != null)
        		zaos.close();
        }
    }
    
    /**
     * 
     * @param zipFile
     * @param saveFileDir
     * @throws IOException
     */
    public static void decompressZip(File zipFile, File saveFileDir) throws IOException {

        if (!isZipFile(zipFile.getName())) {
            throw new IOException("Source file name error.");
        }

        FileUtils.createDirIfNeed(saveFileDir.getCanonicalFile().getParentFile());

        if (!saveFileDir.isDirectory()) {
            throw new IOException("Destination is not a directory.");
        }

        if (!zipFile.exists()) {
            throw new FileNotFoundException("Source file not found.");
        }
        
        ZipArchiveInputStream zais = null;
        InputStream is = null;
        try{
        	is = new FileInputStream(zipFile);
        	zais = new ZipArchiveInputStream(is);
            ArchiveEntry archiveEntry ;
            while ((archiveEntry = zais.getNextEntry()) != null) {
                String entryFileName = archiveEntry.getName();
                String entryFilePath = saveFileDir.getPath() + Constants.SLASH + entryFileName;
                File entryFile = new File(entryFilePath);
                OutputStream os = null;
                FileOutputStream fos = null;
                try {
                	fos = new FileOutputStream(entryFile);
                	os = new BufferedOutputStream(fos);
                    IOUtils.copy(zais, os);
                    os.flush();
                } finally {
                	try {
                		if(fos != null)
                			fos.close();
                	} finally {
                		if(os != null)
                			os.close();
                	}
                }
            }
        } finally {
        	try {
        		if(is != null)
        			is.close();
        	} finally {
        		if(zais != null)
        			zais.close();
        	}
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static boolean isZipFile(String fileName) {
        return fileName != null && !fileName.isEmpty() && fileName.toLowerCase().endsWith(SUFFIX_ZIP);
    }
}
