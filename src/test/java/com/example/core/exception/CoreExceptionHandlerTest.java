package com.example.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;

class CoreExceptionHandlerTest {

  private final CoreExceptionHandler coreExceptionHandler = new CoreExceptionHandler();

  @Test
  void testHandleMissingParam() {
    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("id", "Long");

    Map<String, String> result = coreExceptionHandler.handleMissingParam(ex);

    assertEquals(
        "[Core Error Handler] Required query parameter 'id' is missing", result.get("error"));
  }
}
