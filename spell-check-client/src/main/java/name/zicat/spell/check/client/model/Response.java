package name.zicat.spell.check.client.model;

import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 * @param <T>
 */
public class Response<T> {

	private int code = Constants.HTTP_STATUS_OK;
	private String message;
	
	private T body;
	
	public Response() {
		super();
	}

	public Response(int code, String message, T body) {
		super();
		this.code = code;
		this.message = message;
		this.body = body;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}
}
