package com.media.quickmedia.service.utils;

import com.google.protobuf.ByteString;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class DataBufferService {

    public DataBuffer makeDataBuffer(ByteBuffer bytes){
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        return dataBufferFactory.wrap(bytes);
    }
    public DataBuffer makeDataBuffer(byte[] bytes){
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        return dataBufferFactory.wrap(bytes);
    }

    public DataBuffer joinDataBuffers(List<DataBuffer> dataBuffers){
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        return dataBufferFactory.join(dataBuffers);
    }

}
