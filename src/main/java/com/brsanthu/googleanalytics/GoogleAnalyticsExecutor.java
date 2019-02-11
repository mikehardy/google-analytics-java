package com.brsanthu.googleanalytics;

import java.util.concurrent.CompletableFuture;

import com.brsanthu.googleanalytics.request.GoogleAnalyticsRequest;
import com.brsanthu.googleanalytics.request.GoogleAnalyticsResponse;

public interface GoogleAnalyticsExecutor {
    GoogleAnalyticsResponse post(GoogleAnalyticsRequest<?> request);

    CompletableFuture<GoogleAnalyticsResponse> postAsync(GoogleAnalyticsRequest<?> request);
}
