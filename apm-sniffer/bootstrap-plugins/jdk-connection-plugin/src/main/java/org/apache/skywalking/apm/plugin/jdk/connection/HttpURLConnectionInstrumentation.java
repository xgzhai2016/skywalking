package org.apache.skywalking.apm.plugin.jdk.connection;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.DeclaredInstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @Author: zhaixg
 * @Date: 2020/1/11 23:15
 * @Version 1.0
 */
public class HttpURLConnectionInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String ENHANCE_CLASS = "java.net.HttpURLConnection";
    private static final String INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.jdk.connection.HttpURLConnectionInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS});
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[0];
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
                return named("connect");
            }

            @Override
            public String getMethodsInterceptor() {
                return INTERCEPT_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        }};
    }
    @Override
    public boolean isBootstrapInstrumentation() {
        return true;
    }
}
