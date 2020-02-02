package org.apache.skywalking.apm.plugin.thrift;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;

/**
 * @Author: zhaixg
 * @Date: 2020/1/16 11:22
 * @Version 1.0
 */
public class AbstractTProtocolClientBeforeInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TMessage msg =  (TMessage) objInst.getSkyWalkingDynamicField();
        if(msg.name.equals("heartBeat") || msg.name.equals("regiserNode") || msg.name.equals("taskComplete")){
            return;
        }
        if (msg.type == TMessageType.CALL || msg.type == TMessageType.ONEWAY){
            TProtocol protocol = (TProtocol) objInst;
            String methodName =  msg.name;
            TTransport transport = protocol.getTransport();

            String peer = "";
            if(transport instanceof TSocket){
                //同步阻塞模式
                Socket socket = ((TSocket) transport).getSocket();
                InetSocketAddress remoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
                peer = remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort();
            }else if(transport instanceof TFramedTransport){
                //同步非阻塞模式
                TFramedTransport tFramedTransport = (TFramedTransport) transport;
                Field transport_ = TFramedTransport.class.getDeclaredField("transport_");
                transport_.setAccessible(true);
                TSocket tsocket = (TSocket)transport_.get(tFramedTransport);
                Socket socket = tsocket.getSocket();
                InetSocketAddress remoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
                peer = remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort();
            }else{
                //暂时知道这两种transport方式
                peer = "127.0.0.1:9898";
            }
            ContextCarrier contextCarrier = new ContextCarrier();
            AbstractSpan span = ContextManager.createExitSpan(methodName, contextCarrier, peer);
            span.setComponent(new OfficialComponent(200, "Thrift-client"));
            Tags.URL.set(span, "thrift://" + peer + "/" + methodName);
            SpanLayer.asRPCFramework(span);

            CarrierItem next = contextCarrier.items();
            while (next.hasNext()) {
                next = next.next();
                Map<String, String> skyWalkingPara = Collections.singletonMap(next.getHeadKey(), next.getHeadValue());
                protocol.writeFieldBegin(new TField("zxg-para",TType.MAP,(short)100));
                protocol.writeMapBegin(new TMap(TType.STRING,TType.STRING,skyWalkingPara.size()));
                for(Map.Entry<String,String> entry : skyWalkingPara.entrySet()){
                    protocol.writeString(entry.getKey());
                    protocol.writeString(entry.getValue());
                }
                protocol.writeMapEnd();
                protocol.writeFieldEnd();
            }
        }else if (msg.type == TMessageType.REPLY){
            ContextManager.stopSpan();
        }else if (msg.type == TMessageType.EXCEPTION){
            AbstractSpan span = ContextManager.activeSpan();
            span.errorOccurred();
            Tags.STATUS_CODE.set(span, "error");
            ContextManager.stopSpan();
        }else{
            //结束，总共就是四类
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        TMessage msg =  (TMessage) objInst.getSkyWalkingDynamicField();
        if(msg.type == TMessageType.ONEWAY){
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