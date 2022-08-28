package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.service.error.RepositoryException;
import com.media.quickmedia.service.utils.DataBufferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final ReactiveGridFsTemplate gridFsTemplate;
    private final DataBufferService dataBufferService;

    public Mono<ObjectId> saveLarge(FilePart filePart){
        return Mono.just(filePart)
                .flatMap(fp -> gridFsTemplate.store(fp.content(), fp.filename()))
                .doOnError(ignored->{
                    throw new RepositoryException(String.format("Unable to save file: %s", filePart.filename()));
                });
    }

    public Flux<Void> downloadLarge(String id, ServerWebExchange exchange){
        return Mono.just(id)
                .log()
                .flatMap(_id-> gridFsTemplate.findOne(query(where("_id").is(_id))))
                .switchIfEmpty(Mono.error(new RepositoryException(String.format("No file found with id: %s", id))))
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(r->exchange.getResponse().writeWith(r.getDownloadStream()))
                .doOnError(ignored-> {
                    throw new RepositoryException(String.format("Unable to get file: %s", id));
                });
    }

    public Mono<ObjectId> saveLarge(ByteString bytes, String fileName){
        return Mono.just(bytes)
                .log()
                .flatMap(byteString -> gridFsTemplate.store(Flux.just(dataBufferService
                                .makeDataBuffer(byteString.asReadOnlyByteBuffer())),
                        fileName))
                .log()
                .doOnError(ignored->{
                    throw new RepositoryException(String.format("Unable to save file: %s", fileName));
                });
    }

    public Mono<InputStream> downloadLarge(String id){
        return Mono.just(id)
                .log()
                .flatMap(_id->gridFsTemplate.findOne(query(where("_id").is(_id))))
                .switchIfEmpty(Mono.error(new RepositoryException(String.format("No file found with id: %s", id))))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMap(ReactiveGridFsResource::getInputStream)
                .doOnError(ignored->{
                    throw new RepositoryException(String.format("Unable to get file: %s", id));
                });
    }


    public Flux<byte[]> downloadStream(String id) {
        return Mono.just(id)
                .flatMap(_id-> gridFsTemplate.findOne(query(where("_id").is(id))))
                .switchIfEmpty(Mono.error(new RepositoryException(String.format("No file found with id: %s", id))))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(ReactiveGridFsResource::getDownloadStream)
                .flatMap(dataBuffer -> {
                    byte[] bytes = dataBuffer.asByteBuffer().array();
                    return Flux.just(bytes);
                }).doOnError(ignored->{
                    throw new RepositoryException(String.format("Unable to get file: %s", id));
                });
    }

    public Mono<ObjectId> uploadStream(Flux<ByteString> bytes, String fileName){
        return bytes.map(ByteString::toByteArray)
                .map(dataBufferService::makeDataBuffer)
                .collectList()
                .flatMap(defaultDataBuffers -> {
                    var dataBuffer = dataBufferService.joinDataBuffers(defaultDataBuffers);
                    return gridFsTemplate.store(Flux.just(dataBuffer), fileName);
                }).doOnError(ignored->{
                    throw new RepositoryException(String.format("Unable to save file: %s", fileName));
                });
    }

    public Mono<ObjectId> delete(ObjectId objectId){
        return Mono.just(objectId)
                .log()
                .flatMap(_id-> gridFsTemplate.delete(query(where("_id").is(_id))))
                .doOnError(error->{
                    throw new RepositoryException(String.format("Cannot delete file with id: %s", objectId.toHexString()));
                })
                .thenReturn(objectId);
    }

}
