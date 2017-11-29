package name.zicat.spell.check.biz.service.http;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.client.model.Response;

import java.util.concurrent.atomic.AtomicBoolean;

import name.zicat.spell.check.core.NSpellChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zicat
 */
public class PingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PingService.class);
    public static final String PING_KEYWORD = "Ping";

    private static final AtomicBoolean isTurnOn = new AtomicBoolean(true);

    /**
     *
     * @return
     */
    public Response<String> turnOn() {
        isTurnOn.set(true);
        return new Response<>(200, "turn on success", null);
    }

    /**
     *
     * @return
     */
    public Response<String> turnOff() {
        isTurnOn.set(false);
        return new Response<>(200, "turn off success", null);
    }

    /**
     *
     * @return
     */
    public Response<String> ping() {

        Response<String> response = new Response<>();
        if(!isTurnOn.get()) {
            response.setCode(500);
            response.setMessage("Ping On Closed, May need  to use Ping Api to turn on");
        } else {
            NSpellChecker spellChecker = ZookeeperLocalCache.getCache().getCurrentSpellChecker();
            if(spellChecker == null) {
                response.setCode(500);
                response.setMessage("Current Spell Checker is null");
            } else {
                try {
                    // Not Care Result
                    spellChecker.suggestSimilar(PING_KEYWORD, 1);
                } catch (Exception e) {
                    response.setCode(500);
                    response.setMessage(e.toString());
                    LOG.error("ping  service exception", e);
                }
            }
        }
        return response;
    }
}
