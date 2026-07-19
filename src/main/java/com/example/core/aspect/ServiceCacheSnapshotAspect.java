package com.example.core.aspect;

import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "app.cache.snapshot", name = "enabled", havingValue = "true")
public class ServiceCacheSnapshotAspect {

  private static final Logger log = LoggerFactory.getLogger(ServiceCacheSnapshotAspect.class);

  private final CacheManager cacheManager;

  public ServiceCacheSnapshotAspect(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Around("execution(public * com.example.service.*Service.*(..))")
  public Object logCacheSnapshot(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info(
        "Service cache before {}: \n{}", joinPoint.getSignature().toShortString(), snapshot());
    Object result = joinPoint.proceed();
    log.info("Service cache after {}: \n{}", joinPoint.getSignature().toShortString(), snapshot());
    return result;
  }

  private String snapshot() {
    return cacheManager.getCacheNames().stream()
        .map(
            cacheName -> {
              Cache cache = cacheManager.getCache(cacheName);
              if (cache == null) {
                return cacheName + "=<missing>";
              }

              Object nativeCache = cache.getNativeCache();
              if (nativeCache instanceof java.util.Map<?, ?> map) {
                return cacheName + "=" + map;
              }

              return cacheName + "=" + String.valueOf(nativeCache);
            })
        .collect(Collectors.joining(",\n"));
  }
}