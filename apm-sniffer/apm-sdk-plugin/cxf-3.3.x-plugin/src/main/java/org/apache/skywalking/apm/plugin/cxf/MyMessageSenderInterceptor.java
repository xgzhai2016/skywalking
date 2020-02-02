package org.apache.skywalking.apm.plugin.cxf;

import org.apache.cxf.jaxrs.impl.MetadataMap;
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

/**
 * @Author: zhaixg
 * @Date: 2019/12/27 14:46
 * @Version 1.0
 */
public class MyMessageSenderInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        Message message = (Message)allArguments[0];
        Object isClient = message.get(Message.REQUESTOR_ROLE);
        String m = (String) message.get(Message.HTTP_REQUEST_METHOD);
        if(isClient!=null && (Boolean)isClient){
            URI uri = URI.create((String)message.get(Message.REQUEST_URI));
            String remotePeer = uri.getHost() + ":" + uri.getPort();
            ContextCarrier contextCarrier = new ContextCarrier();
            AbstractSpan span = ContextManager.createExitSpan(uri.getPath(), contextCarrier,remotePeer);
            span.setComponent(new OfficialComponent(100, "ZXG-CXF"));
            new StringTag(101,"cxf.method").set(span,m);
            new StringTag(102, "cxf").set(span,uri.toString());
            SpanLayer.asRPCFramework(span);

            CarrierItem next = contextCarrier.items();
            while (next.hasNext()){
                next = next.next();
                System.out.println("messagesender key :" + next.getHeadKey() + " value : " + next.getHeadValue());
                MetadataMap<String,String> header = (MetadataMap<String, String>)message.get(Message.PROTOCOL_HEADERS);
                header.putSingle(next.getHeadKey(),next.getHeadValue());
            }
        }
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

    }
}
