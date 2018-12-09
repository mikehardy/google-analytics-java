package com.brsanthu.googleanalytics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brsanthu.googleanalytics.httpclient.HttpClient;
import com.brsanthu.googleanalytics.internal.GoogleAnalyticsImpl;
import com.brsanthu.googleanalytics.request.ScreenViewHit;

public class SamplingTest {
    private static final Logger logger = LoggerFactory.getLogger(SamplingTest.class);
    
    private HttpClient mockHttpClient = null;
    
    @Before
    public void beforeEachTest() {
        // construct a mock HttpClient so we can check hit results
        mockHttpClient = Mockito.mock(HttpClient.class);
    }
    
    @Test
    public void testSampleElectionDefault() {

        // With the default 100% sample everyone should be in
        GoogleAnalytics analytics = getDefaultAnalytics();
        assertTrue(analytics.inSample());
    }
    
    @Test
    public void testSampleElectionZero() {
        // Make sure that if we set a zero percentage we are not in the sample
        GoogleAnalytics analytics = getOutOfSampleAnalytics();
        assertFalse(analytics.inSample());    
    }
    
    @Test
    public void testInSampleSend() {        
        GoogleAnalyticsImpl analytics = (GoogleAnalyticsImpl)getDefaultAnalytics();
        ScreenViewHit hit = analytics.screenView().screenName("test screen");
        try {
            hit.send();
        } catch (Exception e) {
            // this throws now, but will still verify that we got past our in-sample check
        }
        Mockito.verify(mockHttpClient).post(ArgumentMatchers.any());
    }        

    @Test
    public void testNotInSampleHit() {
        GoogleAnalytics analytics = getOutOfSampleAnalytics();
        ScreenViewHit hit = analytics.screenView().screenName("test screen");
        hit.send();
        Mockito.verifyZeroInteractions(mockHttpClient);
    }        
    
    @Test
    public void fuzzyTestRandomness() {
        GoogleAnalytics fiftyPercentAnalytics = GoogleAnalytics.builder()
                .withConfig(new GoogleAnalyticsConfig().setSamplePercentage(50))
                .build();
        int inSampleCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (fiftyPercentAnalytics.performSamplingElection()) {
                assertTrue(fiftyPercentAnalytics.inSample());
                inSampleCount++;
            } else {
                assertFalse(fiftyPercentAnalytics.inSample());
            }
        }
        assertTrue("Possibly flaky - sample out of range: " + inSampleCount, 450 <= inSampleCount && inSampleCount <= 550);
    }
    
    private GoogleAnalytics getDefaultAnalytics() {
        return GoogleAnalytics.builder().withHttpClient(mockHttpClient).build();
    }
    
    private GoogleAnalytics getOutOfSampleAnalytics() {
        return GoogleAnalytics.builder()
                .withConfig(new GoogleAnalyticsConfig().setSamplePercentage(0))
                .withHttpClient(mockHttpClient)
                .build();
    }
}
