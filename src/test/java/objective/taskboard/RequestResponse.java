package objective.taskboard;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

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
