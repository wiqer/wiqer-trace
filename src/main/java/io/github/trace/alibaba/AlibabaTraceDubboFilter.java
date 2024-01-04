package io.github.trace.alibaba;

import io.github.trace.JsonLogWrapper;
import io.github.trace.WiqerLog;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;


/**
 * dubbo隐式传参机制，当web服务调用dubbo服务时，用来接收web端隐式传递的参数
 * @author: zsy
 * @create: 2021-03-24 10:51
 */
@Slf4j
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = -109800)
public class AlibabaTraceDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取服务名
        String serviceName = invoker.getInterface().getName();
        // 获取方法名
        String methodName = invocation.getMethodName();
        // 拼接全路径
        String fullPath = serviceName + "." + methodName;
        long time = System.currentTimeMillis();
        String wiqerUserId =RpcContext.getContext().getAttachment(WiqerLog.WIQER_USER);
        String wiqerTrace =RpcContext.getContext().getAttachment(WiqerLog.WIQER_TRACE);
        String wiqerOrder =RpcContext.getContext().getAttachment(WiqerLog.WIQER_ORDER);
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
                log.info("consumer side : time consuming : {}ms , wiqerOrder : {} ,wiqerTrace :{} ,wiqerUserId : {},thread id :{} method : {} --> req args : {},res : {}"
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
