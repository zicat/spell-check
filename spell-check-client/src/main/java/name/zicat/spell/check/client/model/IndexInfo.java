package name.zicat.spell.check.client.model;

/**
 * @author zicat
 */
public class IndexInfo {

    private ServiceInstance serviceInstance;
    private long version;

    public IndexInfo(ServiceInstance serviceInstance, long version) {
        this.serviceInstance = serviceInstance;
        this.version = version;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static boolean idValid(String id) {
    	try {
    		Long.valueOf(id);
    		return true;
    	} catch(NumberFormatException e) {
    		return false;
    	}
    }
    
    @Override
    public String toString() {
    	return version + "@" + serviceInstance.toString();
    }
    
    @Override
    public int hashCode() {
    	return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object other){
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof IndexInfo)) {
            return false;
        }
        IndexInfo indexInfo = (IndexInfo) other;
        return this.toString().equals(indexInfo.toString());
    }
}
