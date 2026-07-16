package com.example.core.aspect;

import com.example.core.annotation.ValidatePathId;
import com.example.core.dto.EmployeeDto;
import java.lang.reflect.Parameter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class PathIdValidationAspect {

  @Before("@annotation(validatePathId)")
  public void validate(JoinPoint joinPoint, ValidatePathId validatePathId) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Parameter[] parameters = signature.getMethod().getParameters();
    Object[] args = joinPoint.getArgs();

    Long pathId = null;
    EmployeeDto dto = null;

    // Extract both the path ID and the request body DTO
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].isAnnotationPresent(PathVariable.class)) {
        PathVariable pv = parameters[i].getAnnotation(PathVariable.class);
        String name = pv.value().isEmpty() ? parameters[i].getName() : pv.value();
        if (name.equals(validatePathId.pathVariableName())) {
          pathId = (Long) args[i];
        }
      } else if (args[i] instanceof EmployeeDto) {
        dto = (EmployeeDto) args[i];
      }
    }

    // Apply validation rule if both are found
    if (pathId != null && dto != null && dto.getId() != null) {
      if (!pathId.equals(dto.getId())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Mismatch: URL ID is " + pathId + " but body ID is " + dto.getId());
      }
    }
  }
}
