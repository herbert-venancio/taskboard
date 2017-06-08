package objective.taskboard;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class to create requests with a fluent and simple interface.
 * 
 * Usage:
 * 		RequestBuilder.url("a url")
 * 					  .. add headers and other options
 *                    .get|post|put|delete|patch
 * 
 * @author takeuchi
 *
 */
public class RequestBuilder {
	private HttpURLConnection conn;
	private String body;
	private int lowerAcceptableStatus = 200;
	private int upperAcceptableStatus = 399;
	private StringBuilder signatureContent = new StringBuilder();
	private long enableCacheForMs = 0;

	public static RequestBuilder url(String url) {
		return new RequestBuilder(url);
	}
	
	public RequestBuilder() {}

	public RequestBuilder(String url) {
		signatureContent.append(url);
		try {
			this.conn = (HttpURLConnection) new URL(url).openConnection();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Add a header entry
	 * 
	 * @param key   header key
	 * @param value header value
	 * @return the builder
	 */
	public RequestBuilder header(String key, String value) {
		signatureContent.append(key+":"+value);
		conn.addRequestProperty(key, value);
		return this;
	}

	/**
	 * Add basic authorization based from a jenkins credentials given its id
	 * 
	 * @param credentialsId the id of the credential to add
	 * @return the builder
	 */
	public RequestBuilder credentials(String user, String password) {
	    String auth;
        try {
            auth = Base64.encodeBase64String((user + ":" + password).getBytes("UTF-8"));
            conn.addRequestProperty("Authorization", "Basic " + auth);
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
	}
	
	/**
	 * Adds the request body.
	 * 
	 * @param body the body contents
	 * @return the builder
	 */
	public RequestBuilder body(Object body) {
		if (body instanceof String)
			this.body = body.toString();
		else
			this.body = gson.toJson(body);
		signatureContent.append(this.body);
		return this;
	}
	
	/**
	 * Specify the acceptable range of response codes. If the response code is out of this
	 * range, the request will throw an exception. The default acceptable range is 200-399
	 * 
	 * @param from
	 * @param to
	 */
	public void acceptableRange(int from, int to) {
		this.lowerAcceptableStatus = from;
		this.upperAcceptableStatus = to;
	}
	
	/**
	 * Perform a post request. 
	 * 
	 * @return the response
	 */
	public RequestResponse post() {
		return doRequest("POST");
	}
	
	/**
	 * Perform a put request
	 * 
	 * @return the response
	 */
	public RequestResponse put() {
		return doRequest("PUT");
	}
	
	/**
	 * Perform a patch request
	 * 
	 * @return the response
	 */
	public RequestResponse patch() {
		return doRequest("PATCH");
	}
	
	/**
	 * Perform a get request.
	 *  
	 * @return the response
	 */
	public RequestResponse get() {
		return doRequest("GET");
	}
	
	/**
	 * Perform a delete request.
	 * 
	 * @return the response
	 */
	public RequestResponse delete() {
		return doRequest("DELETE");
	}

	private RequestResponse doRequest(final String method) {
		signatureContent.append(method);
		if (enableCacheForMs == 0)
			return doActualRequest(method);
		
		return doActualRequest(method);
	}

	private RequestResponse doActualRequest(String method) {
		OutputStreamWriter writer = null;
		try {
			conn.setRequestMethod(method);
			
			if (!StringUtils.isEmpty(body)) {
				conn.setDoOutput(true);
				writer = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
				writer.write(body);
				writer.flush();
			}
			
			int responseCode = conn.getResponseCode();
			String content;
			
			try {
				content = IOUtils.toString(conn.getInputStream(), "UTF-8");
			}catch(IOException e) {
				content = e.getMessage();
			}
			
			if (responseCode < lowerAcceptableStatus || responseCode > upperAcceptableStatus)
				throw new IllegalStateException("Failed : HTTP error code : " + responseCode);
			
			return new RequestResponse(responseCode, content, conn.getHeaderFields());
			
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
