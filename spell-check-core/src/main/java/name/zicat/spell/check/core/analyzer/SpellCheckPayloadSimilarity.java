package name.zicat.spell.check.core.analyzer;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

/**
 *  @author zicat
 */
public class SpellCheckPayloadSimilarity extends ClassicSimilarity {

	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		return PayloadHelper.decodeInt(payload.bytes, payload.offset);
	}

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		return 1f;
	}
}
