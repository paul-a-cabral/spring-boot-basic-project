package com.example.core.interceptor;

public class RequestContext {
  private static final ThreadLocal<Boolean> isDetailedError =
      ThreadLocal.withInitial(() -> Boolean.FALSE);

  public static boolean isDetailedError() {
    return isDetailedError.get();
  }

  public static void setDetailedError(boolean detailedError) {
    isDetailedError.set(detailedError);
  }

  public static void clear() {
    isDetailedError.remove();
  }
}
