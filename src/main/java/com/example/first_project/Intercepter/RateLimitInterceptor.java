package com.example.first_project.Intercepter;
import com.example.first_project.Annotation.RateLimit;
import com.example.first_project.exception.BaseApiException;
import com.example.first_project.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitingService rateLimitingService;
    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }
        int maxRequests = rateLimit.maxRequests();
        int timeWindow = rateLimit.timeWindowMinutes();
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        boolean isAllowed = rateLimitingService.isAllowed(key, maxRequests, timeWindow);
        if (!isAllowed) {
            throw new BaseApiException("You have exceeded the number of attempts allowed. Please try again later.", HttpStatus.TOO_MANY_REQUESTS,"TOO_MANY_REQUESTS");
        }
        return true;
    }
}
