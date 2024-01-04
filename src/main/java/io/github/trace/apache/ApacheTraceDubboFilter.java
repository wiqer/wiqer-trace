package io.github.trace.apache;

import io.github.trace.JsonLogWrapper;
import io.github.trace.WiqerLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

/**
 * dubbo隐式传参机制，当web服务调用dubbo服务时，用来接收web端隐式传递的参数
 *
 * @author mengya
 * @date 2021/3/15
 */
@Slf4j
@Activate(group = {"consumer","provider"}, order = -109800)
public class ApacheTraceDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取服务名
        String serviceName = invoker.getInterface().getName();
        // 获取方法名
        String methodName = invocation.getMethodName();
        // 拼接全路径
        String fullPath = serviceName + "." + methodName;
        String wiqerUserId = RpcContext.getContext().getAttachment(WiqerLog.WIQER_USER);
        String wiqerTrace = RpcContext.getContext().getAttachment(WiqerLog.WIQER_TRACE);
        String wiqerOrder = RpcContext.getContext().getAttachment(WiqerLog.WIQER_ORDER);
        long time = System.currentTimeMillis();
        URL url = null;
        try{
            url = RpcContext.getContext().getUrl();
        }catch (Throwable throwable){
        }
        if(url == null) {
            RpcContext.getContext().setUrl(invoker.getUrl());
        }
        if (RpcContext.getContext().isConsumerSide()) {
            wiqerOrder = StringUtils.isNumeric(wiqerOrder) ? String.valueOf(Integer.parseInt(wiqerOrder) + 1) : "1";
            RpcContext.getContext().setAttachment(WiqerLog.WIQER_ORDER, wiqerOrder);
            Result invoke = invoker.invoke(invocation);
            try{
                log.info("consumer side : time consuming : {}ms ,wiqerOrder : {} ,wiqerTrace :{} ,wiqerUserId : {},thread id :{} method : {} --> req args : {},res : {}"
                        , System.currentTimeMillis() - time
                        , wiqerOrder
                        , wiqerTrace
                        , wiqerUserId
                        , Thread.currentThread().getId()
                        , fullPath
                        , new JsonLogWrapper(invocation.getArguments())
                        , new JsonLogWrapper(invoke.getValue()));
            }catch (Throwable e){
                log.error(fullPath, e);
            }
            return invoke;
        } else if (RpcContext.getContext().isProviderSide()) {
            setMDCandLoaclCache(wiqerUserId,wiqerTrace);
            try {
                Result invoke = invoker.invoke(invocation);

                try{
                    log.info("provider side : time consuming : {}ms , wiqerOrder : {} ,wiqerTrace :{} ,wiqerUserId : {},thread id :{} method : {} --> req args : {},res : {}"
                            , System.currentTimeMillis() - time
                            , wiqerOrder
                            , wiqerTrace
                            , wiqerUserId
                            , Thread.currentThread().getId()
                            , fullPath
                            , new JsonLogWrapper(invocation.getArguments())
                            , new JsonLogWrapper(invoke.getValue()));
                }catch (Throwable e){
                    log.error(fullPath, e);
                }
                return invoke;
            } finally {
                clear();
            }

        }else {

            try{
                setMDCandLoaclCache(wiqerUserId,wiqerTrace);
                return invoker.invoke(invocation);
            }finally {
                clear();
            }
        }
    }

    private static void clear() {
        MDC.remove(WiqerLog.WIQER_USER);
        MDC.remove(WiqerLog.WIQER_TRACE);
    }

    private static void setMDCandLoaclCache(String wiqerUserId, String wiqerTrace) {
        if (StringUtils.isNotEmpty(wiqerTrace)){
            MDC.put(WiqerLog.WIQER_TRACE, wiqerTrace);
        }
        if (StringUtils.isNotEmpty(wiqerUserId)){
            MDC.put(WiqerLog.WIQER_USER, wiqerUserId);
        }
    }
}
