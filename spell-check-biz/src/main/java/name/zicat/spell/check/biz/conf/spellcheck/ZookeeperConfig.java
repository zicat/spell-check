package name.zicat.spell.check.biz.conf.spellcheck;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author zicat
 * @date 2017/06/22
 */
public class ZookeeperConfig {

    private String hostPort;
    private String rootPath;
    private int soTimeout = 5000;

    @XmlElement(name = "hostPort")
    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    @XmlElement(name = "rootPath")
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    @XmlElement(name = "soTimeout")
	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}
}
