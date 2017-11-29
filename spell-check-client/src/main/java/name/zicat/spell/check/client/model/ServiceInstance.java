package name.zicat.spell.check.client.model;

import name.zicat.spell.check.client.utils.Constants;

/**
 * @author zicat
 * @date 2017/06/22
 */
public class ServiceInstance {

    private String id;
    private String host;
    private int port;

    public ServiceInstance(String host, int port) {
    	
        this.id = host + Constants.COLON + port;
        this.host = host;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
    	return id;
    }
    
    @Override
    public boolean equals(Object other){
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof ServiceInstance)) {
            return false;
        }
        return this.toString().equals(other.toString());
    }
    
    @Override
    public int hashCode() {
    	return toString().hashCode();
    }
}
