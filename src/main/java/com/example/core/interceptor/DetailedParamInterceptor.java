package com.example.core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DetailedParamInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(DetailedParamInterceptor.class);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      String methodName = handlerMethod.getMethod().getName();
      String className = handlerMethod.getBeanType().getSimpleName();
      logger.debug("[DetailedParamInterceptor] Handling request for {}.{}", className, methodName);

      boolean isPresent = request.getParameterMap().containsKey("detailed");
      System.out.println(
          "444444 [DetailedParamInterceptor] 'detailed' query parameter present: " + isPresent);
      RequestContext.setDetailedError(isPresent);

      logger.debug(
          "[DetailedParamInterceptor] Detailed error logging is set to: {}",
          RequestContext.isDetailedError());
    }
    return true; // Continue processing the request
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    RequestContext.clear();
  }
}
