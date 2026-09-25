package com.mysci4k.shortener.ratelimit;

import com.mysci4k.shortener.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final ProxyManager<String> proxyManager;

    @Around("@annotation(com.mysci4k.shortener.ratelimit.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        String clientKey = resolveClientKey();
        String bucketKey = clientKey + ":" + method.getDeclaringClass().getSimpleName() + "." + method.getName();

        int capacity = rateLimited.capacity();
        int refill = rateLimited.refill() > 0 ? rateLimited.refill() : capacity;
        Duration duration = toDuration(rateLimited.duration(), rateLimited.timeUnit());

        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(capacity)
                                .refillGreedy(refill, duration)
                                .build()
                ).build();

        Bucket bucket = proxyManager.builder().build(bucketKey, config);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return joinPoint.proceed();
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
        if (waitSeconds < 1) {
            waitSeconds = 1;
        }

        throw new RateLimitExceededException(waitSeconds);
    }

    private String resolveClientKey() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private Duration toDuration(long amount, TimeUnit unit) {
        return switch (unit) {
            case SECONDS -> Duration.ofSeconds(amount);
            case HOURS -> Duration.ofHours(amount);
            case DAYS -> Duration.ofDays(amount);
            default -> Duration.ofMinutes(amount);
        };
    }
}
