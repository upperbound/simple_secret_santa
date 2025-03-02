package com.github.upperbound.secret_santa.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * <p> Provides an implementation for processing various aspects. </p>
 * @author Vladislav Tsukanov
 */
@Aspect
@Component
public class AspectProcessor {
    @Around("@annotation(MDCLog)")
    public Object mdcLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        MDCLog mdcLog = method.getAnnotation(MDCLog.class);
        String mdcKey = mdcLog.mdcKey();
        String mdcValue = "ANNOTATED_METHOD_NAME".equals(mdcLog.mdcValue()) ? method.getName() : mdcLog.mdcValue();
        Logger logger = LoggerFactory.getLogger("ANNOTATED_METHOD_CLASS_NAME".equals(mdcLog.loggerName()) ?
                method.getDeclaringClass().getName() :
                mdcLog.loggerName()
        );
        String mdcOld = MDC.get(mdcKey);
        long start = System.currentTimeMillis();
        try {
            if (mdcLog.executionTime())
                logger.info(">>> started");
            MDC.put(mdcKey, mdcOld == null ? mdcValue : mdcOld + ";" + mdcValue);
            return joinPoint.proceed();
        } catch (Throwable e) {
            logger.error(mdcLog.exceptionMessage(), e.getMessage());
            throw e;
        } finally {
            if (mdcLog.executionTime())
                logger.info("<<< completed in {} ms", System.currentTimeMillis() - start);
            MDC.put(mdcKey, mdcOld);
        }
    }
}
