package com.github.upperbound.secret_santa.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> This annotation provides {@link org.slf4j.MDC MDC} support for a declared method by adding key information
 * into the diagnostic context. At the end of an invocation of this method {@code MDC} will be cleared. </p>
 * @author Vladislav Tsukanov
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MDCLog {
    /**
     * Key value for {@link org.slf4j.MDC MDC}
     * @see org.slf4j.MDC#put(String, String) MDC.put
     */
    String mdcKey();

    /**
     * Param value for {@link org.slf4j.MDC MDC}. By default, the name of the declared method
     * @see org.slf4j.MDC#put(String, String) MDC.put
     */
    String mdcValue() default "ANNOTATED_METHOD_NAME";

    /**
     * The name of a logger to be used in case of any exception within an invocation of the declared method or if
     * {@link #executionTime() executionTime} defined. By default, the full name of the class of this method
     */
    String loggerName() default "ANNOTATED_METHOD_CLASS_NAME";

    /**
     * Provides the support for measuring the execution time of the declared method by printing the message
     * with milliseconds into a logger at the end of an invocation of this method
     */
    boolean executionTime() default false;

    /**
     * Message that will be printed into a logger in case of any exception
     */
    String exceptionMessage() default "unable to execute method: {}";
}
