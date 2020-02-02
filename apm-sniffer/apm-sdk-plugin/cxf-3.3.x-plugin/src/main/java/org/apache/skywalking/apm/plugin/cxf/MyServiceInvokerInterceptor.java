package org.apache.skywalking.apm.plugin.cxf;

import org.apache.cxf.message.Message;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @Author: zhaixg
 * @Date: 2019/12/27 0:22
 * @Version 1.0
 */
public class MyServiceInvokerInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        ContextCarrier contextCarrier = new ContextCarrier();
        //messageimpl
        Message message = (Message)allArguments[0];
        String m = (String) message.get(Message.HTTP_REQUEST_METHOD);
        URI uri = URI.create((String) message.get(Message.REQUEST_URL));
        CarrierItem next = contextCarrier.items();

        while (next.hasNext()) {
            next = next.next();
            TreeMap<String, ArrayList<String>> headers = (TreeMap<String, ArrayList<String>>)message.get(Message.PROTOCOL_HEADERS);
            next.setHeadValue(headers.get(next.getHeadKey()).get(0));
        }
        AbstractSpan span = ContextManager.createEntrySpan(uri.getPath(), contextCarrier);
        span.setComponent(new OfficialComponent(100, "ZXG-CXF"));
        new StringTag(101,"cxf.method").set(span,m);
        new StringTag(102, "cxf").set(span,uri.toString());
        SpanLayer.asRPCFramework(span);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        if(ContextManager.isActive()){
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred().log(t);
    }
}
