package com.brsanthu.googleanalytics.httpclient;

import static com.brsanthu.googleanalytics.internal.GaUtils.isNotEmpty;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;

public class ApacheHttpClientImpl implements HttpClient {
    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClientImpl.class);

    private CloseableHttpClient apacheHttpClient;
    protected final ExecutorService executor;

    public ApacheHttpClientImpl(GoogleAnalyticsConfig config) {
        this.apacheHttpClient = createHttpClient(config);
        this.executor = createExecutor(config);
    }

    @Override
    public void close() {
        try {
            apacheHttpClient.close();
        } catch (IOException e) {
            // ignore
        }
    }

    protected CloseableHttpClient createHttpClient(GoogleAnalyticsConfig config) {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(Math.max(config.getMaxHttpConnectionsPerRoute(), 1));

        HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager);

        if (isNotEmpty(config.getUserAgent())) {
            builder.setUserAgent(config.getUserAgent());
        }

        if (isNotEmpty(config.getProxyHost())) {
            builder.setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()));

            if (isNotEmpty(config.getProxyUserName())) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(config.getProxyHost(), config.getProxyPort()),
                        new UsernamePasswordCredentials(config.getProxyUserName(), config.getProxyPassword()));
                builder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        return builder.build();
    }

    @Override
    public boolean isBatchSupported() {
        return true;
    }

    protected CloseableHttpResponse execute(String url, HttpEntity entity) throws ClientProtocolException, IOException {

        HttpPost httpPost = new HttpPost(url);

        httpPost.setEntity(entity);

        return apacheHttpClient.execute(httpPost);
    }

    protected List<NameValuePair> createNameValuePairs(HttpRequest req) {
        List<NameValuePair> params = new CopyOnWriteArrayList<>();
        req.getBodyParams().forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
        return params;
    }

    @Override
    public CompletableFuture<HttpResponse> post(HttpRequest req) {
        return CompletableFuture.supplyAsync(() -> {
            HttpResponse resp = new HttpResponse();
            CloseableHttpResponse httpResp = null;

            try {

                httpResp = execute(req.getUrl(), new UrlEncodedFormEntity(createNameValuePairs(req), StandardCharsets.UTF_8));
                resp.setStatusCode(httpResp.getStatusLine().getStatusCode());

            } catch (Exception e) {
                if (e instanceof UnknownHostException) {
                    logger.warn("Couldn't connect to Google Analytics. Internet may not be available. " + e.toString());
                } else {
                    logger.warn("Exception while sending the Google Analytics tracker request " + req, e);
                }

            } finally {
                EntityUtils.consumeQuietly(httpResp.getEntity());
                try {
                    httpResp.close();
                } catch (Exception e2) {
                    // ignore
                }
            }

            return (resp);
        });
    }

    @Override
    public CompletableFuture<HttpBatchResponse> postBatch(HttpBatchRequest req) {
        return CompletableFuture.supplyAsync(() -> {
            HttpBatchResponse resp = new HttpBatchResponse();
            CloseableHttpResponse httpResp = null;

            try {
                List<HttpRequest> reqs = new CopyOnWriteArrayList<HttpRequest>(req.getRequests());
                List<List<NameValuePair>> listOfReqPairs = reqs.stream().map(this::createNameValuePairs).collect(Collectors.toList());
                httpResp = execute(req.getUrl(), new BatchUrlEncodedFormEntity(listOfReqPairs));
                resp.setStatusCode(httpResp.getStatusLine().getStatusCode());

            } catch (Exception e) {
                if (e instanceof UnknownHostException) {
                    logger.warn("Couldn't connect to Google Analytics. Internet may not be available. " + e.toString());
                } else {
                    logger.warn("Exception while sending the Google Analytics tracker request " + req, e);
                }

            } finally {
                EntityUtils.consumeQuietly(httpResp.getEntity());
                try {
                    httpResp.close();
                } catch (Exception e2) {
                    // ignore
                }
            }

            return resp;
        });
    }

    protected ExecutorService createExecutor(GoogleAnalyticsConfig config) {
        if (executor != null) {
            return executor;
        }
        return new ThreadPoolExecutor(config.getMinThreads(), config.getMaxThreads(), config.getThreadTimeoutSecs(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(config.getThreadQueueSize()), createThreadFactory(config), new ThreadPoolExecutor.CallerRunsPolicy());
    }
    protected ThreadFactory createThreadFactory(GoogleAnalyticsConfig config) {
        return new GoogleAnalyticsThreadFactory(config.getThreadNameFormat());
    }
}
