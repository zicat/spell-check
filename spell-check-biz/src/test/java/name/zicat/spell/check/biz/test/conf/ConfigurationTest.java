package name.zicat.spell.check.biz.test.conf;

import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zicat
 * @date 2017/06/22
 */
public class ConfigurationTest {

    @Test
    public void testLoadConfig() throws NoSuchAlgorithmException, JAXBException, URISyntaxException, IOException {
        Configuration configuration = ConfigurationManager.getSpellCheckConfig();
        Assert.assertNotNull(configuration);
    }
}
