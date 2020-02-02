package org.apache.skywalking.apm.plugin.thrift;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.DeclaredInstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @Author: zhaixg
 * @Date: 2020/1/16 11:07
 * @Version 1.0
 */
public class AbstractTProtocolInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.apache.thrift.protocol.TProtocol";
    private static final String CLIENT_BEFORE_MEHTOD_NAME = "writeFieldStop";
    private static final String CLIENT_AFTER_METHOD_NAME ="writeMessageBegin";
    private static final String CLIENT_INTERCEPT_BEFORE_CLASS = "org.apache.skywalking.apm.plugin.thrift.AbstractTProtocolClientBeforeInterceptor";
    private static final String CLIENT_INTERCEPT_AFTER_CLASS = "org.apache.skywalking.apm.plugin.thrift.AbstractTProtocolClientAfterInterceptor";

    private static final String SERVER_AFTER1_METHOD_NAME="readMessageBegin";
    private static final String SERVER_AFTER2_METHOD_NAME="readFieldBegin";
    private static final String SERVER_INTERCEPT_AFTER1_CLASS="org.apache.skywalking.apm.plugin.thrift.AbstractTProtocolServerAfter1Interceptor";
    private static final String SERVER_INTERCEPT_AFTER2_CLASS="org.apache.skywalking.apm.plugin.thrift.AbstractTProtocolServerAfter2Interceptor";

//    private static final String SERVER_SKIP_METHOD_NAME = "readBinary";
//    private static final String SERVER_INTERCEPT_SKIP_CLASS = "org.apache.skywalking.apm.plugin.thrift.AbstractTProtocolServerAfter2Interceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS});
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{new DeclaredInstanceMethodsInterceptPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return named(CLIENT_BEFORE_MEHTOD_NAME);
            }

            @Override
            public String getMethodsInterceptor() {
                return CLIENT_INTERCEPT_BEFORE_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        },new DeclaredInstanceMethodsInterceptPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return named(CLIENT_AFTER_METHOD_NAME);
            }

            @Override
            public String getMethodsInterceptor() {
                return CLIENT_INTERCEPT_AFTER_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        },new DeclaredInstanceMethodsInterceptPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return named(SERVER_AFTER2_METHOD_NAME);
            }

            @Override
            public String getMethodsInterceptor() {
                return SERVER_INTERCEPT_AFTER2_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        },new DeclaredInstanceMethodsInterceptPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return named(SERVER_AFTER1_METHOD_NAME);
            }

            @Override
            public String getMethodsInterceptor() {
                return SERVER_INTERCEPT_AFTER1_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        }};
    }
}
