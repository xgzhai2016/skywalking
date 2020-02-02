package org.apache.skywalking.apm.plugin.thrift;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryInputTransport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Author: zhaixg
 * @Date: 2020/1/19 14:13
 * @Version 1.0
 */
public class AbstractTProtocolServerAfter1Interceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        //ret == TMessage
        objInst.setSkyWalkingDynamicField(ret);
//        TProtocol protocol = (TProtocol) objInst;
//        TTransport transport = protocol.getTransport();
//        byte[] allBuffer = transport.getBuffer();
//
//        TField tField = protocol.readFieldBegin();
//        if(tField.id == (short)0){
//            String s = protocol.readString();
//            System.out.println(s);
//        }
//        protocol.readFieldEnd();
////        resetTFramedTransport(protocol);
//        int bufferPosition = transport.getBufferPosition();
//        System.out.println(bufferPosition);
//        int bytesRemainingInBuffer = transport.getBytesRemainingInBuffer();
//        System.out.println(bytesRemainingInBuffer);
//        transport.read(allBuffer,0,allBuffer.length);
//
//
//        protocol.readMessageBegin();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

    public void resetTFramedTransport(TProtocol in) {
        try {
            Field readBuffer_ = TFramedTransportFieldsCache.getInstance()
                    .getTFramedTransportReadBuffer();
            Field buf_ = TFramedTransportFieldsCache.getInstance()
                    .getTMemoryInputTransportBuf();

            if (readBuffer_ == null || buf_ == null) {
                return;
            }

            TMemoryInputTransport stream = (TMemoryInputTransport) readBuffer_
                    .get(in.getTransport());
            byte[] buf = (byte[]) (buf_.get(stream));
            stream.reset(buf, 0, buf.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
