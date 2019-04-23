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
package objective.taskboard.jira.endpoint;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;

import objective.taskboard.config.converter.TaskboardJacksonModule;
import objective.taskboard.jira.config.JiraClientProperties;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

@Component
public class JiraEndpoint {

    private static final Logger log = LoggerFactory.getLogger(JiraEndpoint.class);

    @Autowired 
    private JiraClientProperties jiraProperties;

    public <S> S request(Class<S> service, String username, String password, Headers headers) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setConnectTimeout(60, TimeUnit.SECONDS);
        client.interceptors().add(new AuthenticationInterceptor(username, password));
        client.interceptors().add(new RequestRetryerInterceptor(3));

        if (headers != null)
            client.interceptors().add(new RequestHeadersInterceptor(headers));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TaskboardJacksonModule());
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint(jiraProperties.getUrl())
                .setConverter(new JacksonConverter(objectMapper))
                .setClient(new OkClient(client))
                .build();

        return retrofit.create(service);
    }

    public <S> S request(Class<S> service, String username, String password) {
        return request(service, username, password, null);
    }

    public byte[] readBytesFromURL(URL url, String username, String password) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", Credentials.basic(username, password));
        try (InputStream inputStream = connection.getInputStream()) {
            byte[] bytes = new byte[connection.getContentLength()];
            inputStream.read(bytes);
            return bytes;
        } catch (Exception e) {
            log.error("Error reading bytes from url: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public class AuthenticationInterceptor implements Interceptor {

        private String authToken;

        public AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        public AuthenticationInterceptor(String username, String password) {
            this(Credentials.basic(username, password));
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .header("Authorization", authToken);

            Request request = builder.build();
            return chain.proceed(request);
        }
    }

    public class RequestHeadersInterceptor implements Interceptor {

        private final Headers headers;

        public RequestHeadersInterceptor(Headers headers) {
            this.headers = headers;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Builder requestBuilder = chain.request().newBuilder();

            headers.names().forEach(headerName -> {
                requestBuilder.addHeader(headerName, headers.get(headerName));
            });

            return chain.proceed(requestBuilder.build());
        }

    }

    public class RequestRetryerInterceptor implements Interceptor {

        private final int attempts;

        public RequestRetryerInterceptor(int attempts) {
            this.attempts = attempts;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            int retryCount = 0;
            while (response.code() == HttpStatus.GATEWAY_TIMEOUT.value() && retryCount < attempts) {
                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
                response = chain.proceed(request);
                retryCount++;
            }
            if (!response.isSuccessful())
                log.error(request.urlString() + " request failed.");

            return response;
        }
    }

}
