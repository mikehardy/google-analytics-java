package com.brsanthu.googleanalytics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GoogleAnalyticsConfigTest {

    @Test
    public void testDefaultConfig() throws Exception {
        GoogleAnalyticsConfig config = new GoogleAnalyticsConfig();
        assertEquals("googleanalyticsjava-thread-{0}", config.getThreadNameFormat());
        assertEquals(100, config.getSamplePercentage());
        assertEquals(0, config.getMinThreads());
        assertEquals(5, config.getMaxThreads());
        assertEquals("http://www.google-analytics.com/collect", config.getHttpUrl());
        assertEquals("https://www.google-analytics.com/collect", config.getHttpsUrl());
        assertEquals(80, config.getProxyPort());
        assertEquals(true, config.isDiscoverRequestParameters());
        assertEquals(false, config.isGatherStats());
    }

    @Test
    public void testNonDefaultConfig() throws Exception {
        GoogleAnalyticsConfig initialConfig = new GoogleAnalyticsConfig()
                .setThreadNameFormat("new format")
                .setSamplePercentage(50)
                .setMinThreads(1)
                .setMaxThreads(2)
                .setHttpUrl("test http url")
                .setHttpsUrl("test https url")
                .setProxyPort(42)
                .setDiscoverRequestParameters(false)
                .setGatherStats(true);
        GoogleAnalytics analytics = GoogleAnalytics.builder()
                .withConfig(initialConfig).build();

        GoogleAnalyticsConfig config = analytics.getConfig();
        assertEquals("new format", config.getThreadNameFormat());
        assertEquals(50, config.getSamplePercentage());
        assertEquals(1, config.getMinThreads());
        assertEquals(2, config.getMaxThreads());
        assertEquals("test http url", config.getHttpUrl());
        assertEquals("test https url", config.getHttpsUrl());
        assertEquals(42, config.getProxyPort());
        assertEquals(false, config.isDiscoverRequestParameters());
        assertEquals(true, config.isGatherStats());
    }
}
