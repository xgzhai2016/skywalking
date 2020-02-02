package org.apache.skywalking.apm.plugin.thrift;

import java.lang.reflect.Field;

/**
 * @Author: zhaixg
 * @Date: 2020/1/20 11:24
 * @Version 1.0
 */

@Deprecated
public class TFramedTransportFieldsCache {
    private static TFramedTransportFieldsCache instance;
    private final Field readBuffer_;
    private final Field buf_;
    private final String TFramedTransport_readBuffer_ = "readBuffer_";
    private final String TMemoryInputTransport_buf_ = "buf_";

    private TFramedTransportFieldsCache() throws Exception {
        readBuffer_ = org.apache.thrift.transport.TFramedTransport.class
                .getDeclaredField(TFramedTransport_readBuffer_);
        readBuffer_.setAccessible(true);
        buf_ = org.apache.thrift.transport.TMemoryInputTransport.class
                .getDeclaredField(TMemoryInputTransport_buf_);
        buf_.setAccessible(true);
    }

    public static TFramedTransportFieldsCache getInstance()
            throws Exception {
        if (instance == null) {
            synchronized (TFramedTransportFieldsCache.class) {
                if (instance == null) {
                    instance = new TFramedTransportFieldsCache();
                }
            }
        }
        return instance;
    }

    public Field getTFramedTransportReadBuffer() {
        return readBuffer_;
    }

    public Field getTMemoryInputTransportBuf() {
        return buf_;
    }
}
