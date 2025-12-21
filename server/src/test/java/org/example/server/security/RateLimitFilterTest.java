package org.example.server.security;

import org.example.server.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimitFilter - Unit Tests")
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RateLimitFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("doFilter() - Should continue chain if under limit")
    void doFilter_UnderLimit_ShouldContinue() throws Exception {
        when(rateLimitService.isAllowed(request)).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("doFilter() - Should reject request if over limit")
    void doFilter_OverLimit_ShouldReject() throws Exception {
        when(rateLimitService.isAllowed(request)).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertEquals(429, response.getStatus()); // HTTP 429 Too Many Requests
    }
}