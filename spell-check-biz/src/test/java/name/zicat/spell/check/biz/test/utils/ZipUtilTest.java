package name.zicat.spell.check.biz.test.utils;

import name.zicat.spell.check.biz.utils.ZipUtil;
import name.zicat.utils.file.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Jack.Z.Tang
 * @date 2017/06/23
 */
public class ZipUtilTest {
	
    @Test
    public void testCompressFiles2Zip() throws IOException {
    	
        File file = new File("1234/test.txt");
        File zipFile = new File("1234/test.zip");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("test");
            fw.flush();
        }
        ZipUtil.compressFiles2Zip(new File[]{file}, zipFile);

        Assert.assertTrue(zipFile.exists());

        file.delete();
        Assert.assertFalse(file.exists());
        ZipUtil.decompressZip(zipFile, new File("1234"));
        Assert.assertTrue(file.exists());
        File dir = new File("1234");
        FileUtils.cleanUpDir(dir);
        dir.delete();
        
        Assert.assertTrue(!ZipUtil.isZipFile(null));
        Assert.assertTrue(!ZipUtil.isZipFile(""));
        Assert.assertTrue(!ZipUtil.isZipFile("aa.bb"));
        Assert.assertTrue(!ZipUtil.isZipFile(".zip.a"));
        Assert.assertTrue(ZipUtil.isZipFile("a.zip"));
    }
}
