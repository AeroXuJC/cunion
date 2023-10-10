package com.example.cunion.aop;


import com.example.cunion.common.R;
import com.example.cunion.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class TokenAspect {
    @Resource
    private ThreadLocalToken threadLocalToken;
    @Pointcut("execution(public * com.example.cunion.controller.*.*(..)))")
    public void aspect(){
    }
    @Around("aspect()")
    public Object around(ProceedingJoinPoint point)throws Throwable{
        R r = (R)point.proceed();//方法执行结果
        String token = threadLocalToken.getToken();
        //如果ThreadLocal中存在Token
        if (token != null){
            //往响应中放置token
            r.put("token", token);
            threadLocalToken.clear();
        }
        return r;
    }
}
