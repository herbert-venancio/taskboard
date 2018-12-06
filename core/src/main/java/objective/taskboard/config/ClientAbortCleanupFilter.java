package objective.taskboard.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientAbortCleanupFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(ClientAbortCleanupFilter.class);

	@Override
	public void init(FilterConfig filterConfig) {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		}catch (ClientAbortException e) {//NOSONAR
			if (request instanceof HttpServletRequest) {
				String url = ((HttpServletRequest)request).getRequestURL().toString();
				log.warn("("+ url + ") ClientAbortException: " + e.getMessage());
			}
			else
				throw e;
		}
	}

	@Override
	public void destroy() {}
}
