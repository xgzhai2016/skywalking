package org.apache.skywalking.apm.plugin.thrift;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.Method;

/**
 * @Author: zhaixg
 * @Date: 2020/1/19 15:51
 * @Version 1.0
 */

@Deprecated
public class AbstractTProtocolServerSkipInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        //TField
//        objInst.setSkyWalkingDynamicField(ret);
//        return ret;

        TMessage msg = (TMessage) objInst.getSkyWalkingDynamicField();
        if(msg.type == TMessageType.CALL){
            TField schemeField = (TField) ret;
            if(schemeField.id == (short) 100){
                TProtocol protocol = (TProtocol) objInst;
                String value = protocol.readString();
                System.out.println(value);
            }else{
                System.out.println("method para");
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
