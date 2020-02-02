package org.apache.skywalking.apm.plugin.jdk.connection;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.util.StringUtil;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author: zhaixg
 * @Date: 2020/1/11 23:28
 * @Version 1.0
 */
public class HttpURLConnectionInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        HttpURLConnection connection = (HttpURLConnection) objInst;
        URL url = connection.getURL();
        ContextCarrier contextCarrier = new ContextCarrier();
        AbstractSpan span = ContextManager.createExitSpan(getPath(url), contextCarrier, getPeer(url));
        span.setComponent(ComponentsDefine.JDK_HTTP);
        Tags.HTTP.METHOD.set(span, connection.getRequestMethod());
        Tags.URL.set(span, url.toString());
        SpanLayer.asHttp(span);
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            System.out.println("jdkclient :" + next.getHeadKey() + " : " + next.getHeadValue());
//            headers.add(next.getHeadKey(), next.getHeadValue());
            connection.setRequestProperty(next.getHeadKey(),next.getHeadValue());
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        HttpURLConnection connection = (HttpURLConnection) objInst;

        int responseCode = connection.getResponseCode();
        if (responseCode >= 400) {
            AbstractSpan span = ContextManager.activeSpan();
            span.errorOccurred();
            Tags.STATUS_CODE.set(span, Integer.toString(responseCode));
        }

        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred().log(t);
    }

    private String getPeer(URL url) {
        String host = url.getHost();
        if (url.getPort() > 0) {
            return host + ":" + url.getPort();
        }
        return host;
    }
    private String getPath(URL url) {
        return StringUtil.isEmpty(url.getPath()) ? "/" : url.getPath();
    }
}
