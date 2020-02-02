package org.apache.skywalking.apm.plugin.thrift;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

/**
 * @Author: zhaixg
 * @Date: 2020/1/16 16:45
 * @Version 1.0
 */
public class AbstractTProtocolClientAfterInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        //TMessage
        objInst.setSkyWalkingDynamicField(allArguments[0]);
//        TMessage msg = (TMessage) allArguments[0];
//        if (msg.type == TMessageType.CALL){
//            TProtocol protocol = (TProtocol) objInst;
//            protocol.writeFieldBegin(new TField("zxg-para", TType.STRING,(short)0));
//            protocol.writeString("zxg-value");
//            protocol.writeFieldEnd();
//        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
