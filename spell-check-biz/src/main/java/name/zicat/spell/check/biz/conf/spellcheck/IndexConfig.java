package name.zicat.spell.check.biz.conf.spellcheck;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author zicat
 * @date 2017/06/23
 */
public class IndexConfig {

    private String indexPath;
    private String tempPath;

    @XmlElement(name = "indexPath")
    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    @XmlElement(name = "tempPath")
    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }
}
