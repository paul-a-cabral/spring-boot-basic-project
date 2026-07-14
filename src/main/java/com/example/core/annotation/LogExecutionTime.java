package com.example.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Specifies this annotation can only be placed on methods
@Retention(RetentionPolicy.RUNTIME) // Makes the annotation available at runtime for Spring to read
public @interface LogExecutionTime {}
