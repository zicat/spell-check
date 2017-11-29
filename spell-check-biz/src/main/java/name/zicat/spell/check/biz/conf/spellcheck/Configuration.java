package name.zicat.spell.check.biz.conf.spellcheck;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import name.zicat.spell.check.client.model.ServiceInstance;

/**
 * @author zicat
 * @date 2017/06/22
 */
@XmlRootElement(name = "configuration")
public class Configuration {

    public static final String ROOT_FILE = "spell-check-configuration.xml";
    private ZookeeperConfig zookeeperConfig;
    private IndexConfig indexConfig;
    private volatile ServiceInstance serviceInstace;
    
    private int servicePort;

    @XmlElement(name = "zookeeperConfig")
    public ZookeeperConfig getZookeeperConfig() {
        return zookeeperConfig;
    }

    public void setZookeeperConfig(ZookeeperConfig zookeeperConfig) {
        this.zookeeperConfig = zookeeperConfig;
    }

    @XmlElement(name = "servicePort")
    public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	@XmlElement(name = "indexConfig")
    public IndexConfig getIndexConfig() {
        return indexConfig;
    }

    public void setIndexConfig(IndexConfig indexConfig) {
        this.indexConfig = indexConfig;
    }

    /**
     *
     * @return
     * @throws UnknownHostException
     */
    public ServiceInstance create() throws UnknownHostException {
    	
    	if(serviceInstace != null)
    		return serviceInstace;
    	
    	synchronized (this) {
    		if(serviceInstace != null)
        		return serviceInstace;
    		serviceInstace = new ServiceInstance(InetAddress.getLocalHost().getHostAddress(), servicePort);
		}
    	return serviceInstace;
    }
}
