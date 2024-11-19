//package com.sky.aspect;
//
//import com.sky.annotation.ReadLock;
//import com.sky.annotation.WriteLock;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.redisson.api.RReadWriteLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.expression.EvaluationContext;
//import org.springframework.expression.Expression;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
//@Aspect
//@Component
//public class RedissonLockAspect {
//
//    @Autowired
//    private RedissonClient redissonClient;
//
//    private ExpressionParser parser = new SpelExpressionParser();
//
//    @Around("@annotation(readLock)")
//    public Object aroundReadLock(ProceedingJoinPoint joinPoint, ReadLock readLock) throws Throwable {
//        String key = parseKey(readLock.key(), joinPoint);
//        RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
//        boolean isLocked = false;
//        try {
//            isLocked = rwLock.readLock().tryLock(10, 5, TimeUnit.SECONDS);
//            if (isLocked) {
//                return joinPoint.proceed();
//            } else {
//                throw new RuntimeException("无法获取读锁，方法执行失败");
//            }
//        } finally {
//            if (isLocked) {
//                rwLock.readLock().unlock();
//            }
//        }
//    }
//
//    @Around("@annotation(writeLock)")
//    public Object aroundWriteLock(ProceedingJoinPoint joinPoint, WriteLock writeLock) throws Throwable {
//        String key = parseKey(writeLock.key(), joinPoint);
//        RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
//        boolean isLocked = false;
//        try {
//            isLocked = rwLock.writeLock().tryLock(10, 5, TimeUnit.SECONDS);
//            if (isLocked) {
//                return joinPoint.proceed();
//            } else {
//                throw new RuntimeException("无法获取写锁，方法执行失败");
//            }
//        } finally {
//            if (isLocked) {
//                rwLock.writeLock().unlock();
//            }
//        }
//    }
//
//    /**
//     * 解析锁的 key，支持 SpEL 表达式
//     */
//    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
//        Expression expression = parser.parseExpression(key, new org.springframework.expression.ParserContext() {
//            @Override
//            public boolean isTemplate() {
//                return true;
//            }
//
//            @Override
//            public String getExpressionPrefix() {
//                return "${";
//            }
//
//            @Override
//            public String getExpressionSuffix() {
//                return "}";
//            }
//        });
//
//        EvaluationContext context = new StandardEvaluationContext();
//        Object[] args = joinPoint.getArgs();
//        // 将方法参数绑定到上下文中，假设方法参数名可用
//        // 若使用编译参数，如 -parameters，需要确保方法参数名可用
//        String[] paramNames = org.aspectj.lang.reflect.MethodSignature.class.cast(joinPoint.getSignature()).getParameterNames();
//        if (paramNames != null) {
//            for (int i = 0; i < paramNames.length; i++) {
//                context.setVariable(paramNames[i], args[i]);
//            }
//        }
//        return expression.getValue(context, String.class);
//    }
//}
