package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.*;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.crypto.Data;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final ReactiveGridFsTemplate gridFsTemplate;

    public Mono<ObjectId> saveLarge(FilePart filePart){
        return gridFsTemplate.store(filePart.content(), filePart.filename());
    }

    public Flux<Void> downloadLarge(String id, ServerWebExchange exchange){
        return gridFsTemplate.findOne(query(where("_id").is(id)))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }

    public Mono<ObjectId> saveLarge(ByteString bytes, String fileName){
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(bytes.asReadOnlyByteBuffer());
        return gridFsTemplate.store(Flux.just(dataBuffer), fileName);
    }

    public Mono<InputStream> downloadLarge(String id){
        return gridFsTemplate.findOne(query(where("_id").is(id)))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMap(ReactiveGridFsResource::getInputStream);
    }


    public Flux<byte[]> downloadStream(String id) {
        return gridFsTemplate.findOne(query(where("_id").is(id)))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(ReactiveGridFsResource::getDownloadStream)
                .flatMap(dataBuffer -> {
                    byte[] bytes = dataBuffer.asByteBuffer().array();
                    return Flux.just(bytes);
                });

    }

    public Mono<ObjectId> uploadStream(Flux<ByteString> bytes, String fileName){
        return bytes.map(ByteString::toByteArray)
        .map(bytes1 -> {
            DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
            return dataBufferFactory.wrap(bytes1);
        })
                .collectList()
                .flatMap(defaultDataBuffers -> {
                    DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    var dataBuffer = dataBufferFactory.join(defaultDataBuffers);
                    return gridFsTemplate.store(Flux.just(dataBuffer), fileName);
                });
    }

}
