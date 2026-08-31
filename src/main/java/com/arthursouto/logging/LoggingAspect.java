package com.arthursouto.logging;

import com.arthursouto.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Loga entrada/saída de Controllers, Services e Repositories automaticamente,
 * sem precisar espalhar log.info(...) manualmente pelo código.
 *
 * Formato do log:
 *   -> AssetService.updateAsset(args=[...])
 *   <- AssetService.updateAsset returned=[...] (12ms)
 *   x  AssetService.updateAsset threw ResourceNotFoundException: Asset not found (3ms)
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class LoggingAspect {

    @Around("within(com.arthursouto.controller..*) || " +
            "within(com.arthursouto.service..*) || " +
            "within(com.arthursouto.repository..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = LoggingUtils.resolveClassName(joinPoint.getTarget());
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String args = LoggingUtils.toSafeJson(joinPoint.getArgs());

        // usar org.slf4j.MDC aqui (via CorrelationIdFilter) já injeta o requestId automaticamente
        log.info("-> {}.{}(args={})", className, methodName, args);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long tookMs = System.currentTimeMillis() - start;

            log.info("<- {}.{} returned={} ({}ms)", className, methodName,
                    LoggingUtils.toSafeJson(result), tookMs);

            return result;
        } catch (Throwable ex) {
            long tookMs = System.currentTimeMillis() - start;
            log.warn("x  {}.{} threw {}: {} ({}ms)", className, methodName,
                    ex.getClass().getSimpleName(), ex.getMessage(), tookMs);
            throw ex;
        }
    }
}