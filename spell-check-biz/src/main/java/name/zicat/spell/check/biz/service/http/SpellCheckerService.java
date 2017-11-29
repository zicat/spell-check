package name.zicat.spell.check.biz.service.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import name.zicat.spell.check.client.utils.Constants;
import name.zicat.spell.check.core.NSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.service.zookeeper.RandomAvailableInstanceByVersion;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.model.SpellCheckModel;
import name.zicat.spell.check.client.query.SpellCheckRequest;

/**
 * 
 * @author lz31
 *
 */
public class SpellCheckerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpellCheckerService.class);
	
	public static final Random random = new Random();
	public static final float score = 0.7f;
	
	private String keyword;
	private Integer row;
	private Long version;
	
	public SpellCheckerService(String keyword, Integer row, Long version) {
		
		this.keyword = keyword;
		this.row = row == null || row < 1? 1: row;
		this.version = version;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Response<SpellCheckModel> spellCheck() {
		
		Response<SpellCheckModel> response = new Response<>();
		NSpellChecker spellCheck = null;
		try {
			if(version != null) 
				spellCheck = ZookeeperLocalCache.getCache().getSpellChecker(version);
			else {
				spellCheck = ZookeeperLocalCache.getCache().getCurrentSpellChecker();
			}
			
			/** Local Contains, Direct use **/
			if(spellCheck != null) {
				
				SpellCheckModel model = new SpellCheckModel();
				String[] result = spellCheck.suggestSimilar(keyword, row, SuggestMode.SUGGEST_MORE_POPULAR, score);
				model.setSuggestions(Arrays.asList(result));
				response.setMessage("Instace:" + ConfigurationManager.getSpellCheckConfig().create());
				response.setBody(model);
				return response;
			}
			
			if(version == null)
				throw new IOException("Get Current Spell Checker Error, Current Spell Checker is Null");
			
			/** Get Available Version from Other Instances by Zookeeper **/
			RandomAvailableInstanceByVersion service = new RandomAvailableInstanceByVersion();
			String ipAndPort = service.random(version);
			SpellCheckRequest request = new SpellCheckRequest(ipAndPort);
			return request.spellCheck(keyword, row, version);
		} catch(Exception e) {
			response.setCode(Constants.HTTP_STATUS_ERROR);
			response.setMessage(e.toString());
			LOG.error("spell check service exception, version = " + version, e);
		}
		return response;
	}
}
