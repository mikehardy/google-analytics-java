package com.brsanthu.googleanalytics;

import com.brsanthu.googleanalytics.discovery.DefaultRequestParameterDiscoverer;
import com.brsanthu.googleanalytics.discovery.RequestParameterDiscoverer;
import com.brsanthu.googleanalytics.httpclient.ApacheHttpClientImpl;
import com.brsanthu.googleanalytics.httpclient.HttpClient;
import com.brsanthu.googleanalytics.internal.GaUtils;
import com.brsanthu.googleanalytics.internal.GoogleAnalyticsImpl;
import com.brsanthu.googleanalytics.request.DefaultRequest;

public class GoogleAnalyticsBuilder {
    private GoogleAnalyticsConfig config = new GoogleAnalyticsConfig();
    private DefaultRequest defaultRequest = new DefaultRequest();
    private HttpClient httpClient;

    public GoogleAnalyticsBuilder withConfig(GoogleAnalyticsConfig config) {
        this.config = GaUtils.firstNotNull(config, new GoogleAnalyticsConfig());
        return this;
    }

    public GoogleAnalyticsBuilder withTrackingId(String trackingId) {
        defaultRequest.trackingId(trackingId);
        return this;
    }

    public GoogleAnalyticsBuilder withAppName(String value) {
        defaultRequest.applicationName(value);
        return this;
    }

    public GoogleAnalyticsBuilder withAppVersion(String value) {
        defaultRequest.applicationVersion(value);
        return this;
    }

    public GoogleAnalyticsBuilder withDefaultRequest(DefaultRequest defaultRequest) {
        this.defaultRequest = GaUtils.firstNotNull(defaultRequest, new DefaultRequest());
        return this;
    }

    public GoogleAnalyticsBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public GoogleAnalytics build() {
        if (config.isDiscoverRequestParameters()) {
            RequestParameterDiscoverer discoverer = GaUtils.firstNotNull(config.getRequestParameterDiscoverer(),
                    DefaultRequestParameterDiscoverer.INSTANCE);

            discoverer.discoverParameters(config, defaultRequest);
        }

        return new GoogleAnalyticsImpl(config, defaultRequest, createHttpClient());
    }

    protected HttpClient createHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }

        return new ApacheHttpClientImpl(config);
    }

}
