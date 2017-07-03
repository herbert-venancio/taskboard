package objective.taskboard.it;

import objective.taskboard.followup.UpdateFollowUpServiceTest;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by herbert on 03/07/17.
 */
public class UploadTemplateIT {
    @Test
    public void uploadOkTemplate() throws IOException, URISyntaxException, InterruptedException {
        HttpClient client = HttpClientBuilder.create().build();

        Header[] session = doLogin(client);

        File file = okTemplate();
        HttpPost post = new HttpPost();
        post.setURI(new URI("http://localhost:8900/ws/followup"));
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        post.setHeaders(session);
        post.setEntity(entity);

        HttpResponse response = client.execute(post);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    // ---

    private static Header[] doLogin(HttpClient client) throws URISyntaxException, IOException {
        HttpPost post = new HttpPost();
        post.setURI(new URI("http://localhost:8900/login"));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", "bla"));
        params.add(new BasicNameValuePair("password", "bla"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);

        post.setEntity(entity);

        return client.execute(post).getHeaders("Set-Cookie");
    }

    private static File okTemplate() throws URISyntaxException, IOException {
        return new File(UpdateFollowUpServiceTest.class.getResource("OkFollowupTemplate.xlsm").toURI());
    }

}
