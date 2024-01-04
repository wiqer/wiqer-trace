package io.github.trace;

import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author admin
 * @version 1.0.0
 * @description
 * @date 2022/2/11 10:05
 */
@Component
@Order(999)
@Slf4j
public class WebLogInterceptor implements HandlerInterceptor {


    private boolean alibabaDubboLoaded = false;
    private boolean apacheDubboLoaded = false;

    private boolean firstLoad = true;

    public WebLogInterceptor() {
        systemLoader();
        currentThreadLoader();
    }

    private void currentThreadLoader() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("com.alibaba.dubbo.rpc.RpcContext");
            alibabaDubboLoaded = true;
        } catch (ClassNotFoundException ignored) {
            alibabaDubboLoaded = false;

        }
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.apache.dubbo.rpc.RpcContext");
            apacheDubboLoaded = true;
        } catch (ClassNotFoundException ignored) {
            apacheDubboLoaded = false;
        }
    }

    private void systemLoader() {
        try {
            ClassLoader.getSystemClassLoader().loadClass("com.alibaba.dubbo.rpc.RpcContext");
            alibabaDubboLoaded = true;
        } catch (ClassNotFoundException ignored) {
            alibabaDubboLoaded = false;

        }
        try {
            ClassLoader.getSystemClassLoader().loadClass("org.apache.dubbo.rpc.RpcContext");
            apacheDubboLoaded = true;
        } catch (ClassNotFoundException ignored) {
            apacheDubboLoaded = false;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(firstLoad){
            systemLoader();
            currentThreadLoader();
            firstLoad = false;
        }
        String traceId = request.getHeader(WiqerLog.WIQER_TRACE);
        String userId = request.getHeader(WiqerLog.WIQER_USER);
        if(StringUtils.isNotEmpty(traceId)) {
            MDC.put(WiqerLog.WIQER_TRACE, traceId);
        }
        if(StringUtils.isNotEmpty(traceId)) {
            MDC.put(WiqerLog.WIQER_USER, userId);
        }
        if(alibabaDubboLoaded){
            RpcContext.getContext().setAttachment(WiqerLog.WIQER_ORDER, String.valueOf(1));
            RpcContext.getContext().setAttachment(WiqerLog.WIQER_TRACE, traceId);
            RpcContext.getContext().setAttachment(WiqerLog.WIQER_USER, userId);
        }
        if(apacheDubboLoaded){
            org.apache.dubbo.rpc.RpcContext.getContext().setAttachment(WiqerLog.WIQER_ORDER, String.valueOf(1));
            org.apache.dubbo.rpc.RpcContext.getContext().setAttachment(WiqerLog.WIQER_TRACE, traceId);
            org.apache.dubbo.rpc.RpcContext.getContext().setAttachment(WiqerLog.WIQER_USER, userId);
        }

        try {
            log.info("preHandle , order 1, traceId :{} ,url:{},body:{}", traceId, request.getRequestURL(),getBody(request));
        } catch (IOException e) {
            log.error("preHandle ",e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try{
            HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        }finally {
            MDC.remove(WiqerLog.WIQER_TRACE);
            if(alibabaDubboLoaded){
                RpcContext.getContext().remove(WiqerLog.WIQER_ORDER);
                RpcContext.getContext().remove(WiqerLog.WIQER_TRACE);
            }
            if(apacheDubboLoaded){
                org.apache.dubbo.rpc.RpcContext.getContext().remove(WiqerLog.WIQER_ORDER);
                org.apache.dubbo.rpc.RpcContext.getContext().remove(WiqerLog.WIQER_TRACE);
            }
        }
    }
    private String getBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        return stringBuilder.toString();
    }
}

