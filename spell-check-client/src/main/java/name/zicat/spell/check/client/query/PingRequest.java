package name.zicat.spell.check.client.query;

import com.google.gson.reflect.TypeToken;
import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;
import name.zicat.spell.check.client.model.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author zicat
 */
public class PingRequest extends HttpRequest {

    public PingRequest(String url, String version, int timeout) {
        super(url, version, timeout);
    }

    public PingRequest(String url, int timeout) {
        super(url, timeout);
    }

    public PingRequest(String url) {
        super(url);
    }

    /**
     *
     * @param path
     * @return
     * @throws IOException
     */
    private Response<String> pingRequest(String path) throws IOException {
        IHttpClient client = new HttpClient(url, timeOut);
        String values = client.get(path, null, null, StandardCharsets.UTF_8);
        Type jsonType = new TypeToken<Response<String>>() {}.getType();
        return GSON.fromJson(values, jsonType);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public Response<String> ping() throws IOException {
        return pingRequest(buildPath(PING_PATH, version));
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public Response<String> pingTurnOn() throws IOException {
        return pingRequest(buildPath(PING_TURN_ON_PATH, version));
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public Response<String> pingTurnOff() throws IOException {
        return pingRequest(buildPath(PING_TURN_OFF_PATH, version));
    }
}
