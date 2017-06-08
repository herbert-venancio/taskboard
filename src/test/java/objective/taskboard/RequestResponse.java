package objective.taskboard;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RequestResponse implements Serializable {
	private static final long serialVersionUID = -9121943094727486673L;
	public final int responseCode;
	public final String content;
	public final Map<String, List<String>> headers;

	public RequestResponse(int status, String content, Map<String, List<String>> headers) {
		this.responseCode = status;
		this.content = content;
		this.headers = headers;
	}
	
	public String getContentsJson() {
		return content;
	}
}
