package br.kuhn.dev.springboot._core.logger;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

import br.kuhn.dev.springboot._common.util.GetServletRequestIp;
import br.kuhn.dev.springboot._core.user.entity.User;

/**
 * 
 * @author Jardel Kuhn (jkuhn2@universo.univates.br)
 */
@Aspect
@Slf4j
@Component
public class ControllerLogger {

    @Autowired
    private GetServletRequestIp getServletRequestIp;

    @Around("execution(* br.kuhn.dev.springboot.*.controller.*.*(..))")
    public Object aroundControllers(
            ProceedingJoinPoint theProceedingJoinPoint) throws Throwable {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String method = theProceedingJoinPoint.getSignature().toShortString();
        StringBuilder incoming = new StringBuilder("User:")
                .append(user.getId())
                .append("@")
                .append(method)
                .append(":")
                .append(getServletRequestIp.parse(request))
                .append(",")
                .append(request.getRequestURL().toString());

        for (Object arg : theProceedingJoinPoint.getArgs()) {
            if (arg != null)
                incoming.append(arg.toString());
        }

        log.info(incoming.toString());

        long begin = System.currentTimeMillis();

        Object result = null;

        try {
            result = theProceedingJoinPoint.proceed();
        } catch (Exception e) {
            log.warn(e.getMessage());

            throw e;
        }

        long end = System.currentTimeMillis();

        long duration = end - begin;
        log.info("Duration: " + duration / 1000.0 + " seconds");

        return result;
    }
}
