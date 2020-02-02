package org.apache.skywalking.apm.plugin.thrift;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;
import org.apache.thrift.protocol.*;

import java.lang.reflect.Method;

/**
 * @Author: zhaixg
 * @Date: 2020/1/19 14:10
 * @Version 1.0
 */
public class AbstractTProtocolServerAfter2Interceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {

        TMessage msg = (TMessage) objInst.getSkyWalkingDynamicField();
        if(msg.name.equals("heartBeat") || msg.name.equals("regiserNode") || msg.name.equals("taskComplete")){
            return ret;
        }
        if(msg.type == TMessageType.CALL || msg.type == TMessageType.ONEWAY){
            TField schemeField = (TField) ret;
            if(schemeField.id == (short) 100){
                ContextCarrier contextCarrier = new ContextCarrier();
                CarrierItem next = contextCarrier.items();
                TProtocol protocol = (TProtocol) objInst;
                TMap tMap = protocol.readMapBegin();

                while (next.hasNext()) {
                    next = next.next();
                    for (int i =0 ;i<tMap.size;++i){
                        String key = protocol.readString();
                        String value = protocol.readString();
                        if(key.equals(next.getHeadKey())){
                            next.setHeadValue(value);
                        }
                    }
                }
                protocol.readMapEnd();
                protocol.readFieldEnd();

                AbstractSpan span = ContextManager.createEntrySpan(msg.name, contextCarrier);
                span.setComponent(new OfficialComponent(201, "Thrift-Server"));
                Tags.URL.set(span, "thrift://" + msg.name);
                new StringTag(201, "Thrift").set(span,msg.name);
                SpanLayer.asRPCFramework(span);

                //必须重新readFieldBegin，用来解析stop
                TField tField = protocol.readFieldBegin();

                if(msg.type == TMessageType.ONEWAY) {
                    ContextManager.stopSpan();
                }
                return tField;
            }else{
                System.out.println("method para");
            }
        }else if(msg.type == TMessageType.REPLY){
            TField schemeField = (TField) ret;
            if(schemeField.type == TType.STOP) {
                ContextManager.stopSpan();
            }
        }else if(msg.type == TMessageType.EXCEPTION){
            TField schemeField = (TField) ret;
            if(schemeField.type == TType.STOP) {
                AbstractSpan span = ContextManager.activeSpan();
                span.errorOccurred();
                Tags.STATUS_CODE.set(span, "error");
                ContextManager.stopSpan();
            }
        }else{
            // 结束，总共就四种方式
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred().log(t);
    }
}
