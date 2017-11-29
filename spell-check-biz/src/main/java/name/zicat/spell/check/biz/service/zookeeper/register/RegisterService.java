package name.zicat.spell.check.biz.service.zookeeper.register;

/**
 * @author zicat
 * @date 2017/06/24
 */
public interface RegisterService<T> {

    /**
     *
     * @param t
     * @throws Exception
     */
    void register(T t, byte[] datas) throws Exception;
    
    /**
     * 
     * @param t
     * @throws Exception
     */
    void cancell(T t) throws Exception;

    /**
     *
     * @param t
     * @return
     * @throws Exception
     */
    boolean isRegistered(T t) throws Exception;
}
