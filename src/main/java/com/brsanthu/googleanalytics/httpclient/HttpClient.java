package com.brsanthu.googleanalytics.httpclient;

import java8.util.concurrent.CompletableFuture;

public interface HttpClient extends AutoCloseable {
    CompletableFuture<HttpResponse> post(HttpRequest req);

    boolean isBatchSupported();

    CompletableFuture<HttpBatchResponse> postBatch(HttpBatchRequest req);
}
