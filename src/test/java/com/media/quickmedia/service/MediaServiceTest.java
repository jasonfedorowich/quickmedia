package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.service.error.RepositoryException;
import com.media.quickmedia.service.utils.DataBufferService;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.proto.service.BatchUploadRequest;
import com.proto.service.Key;
import com.proto.service.UploadRequest;
import org.bson.BsonValue;
import org.bson.ByteBuf;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private ReactiveGridFsTemplate fsTemplate;

    @Mock
    private DataBufferService dataBufferService;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(fsTemplate, dataBufferService);
    }

    @Test
    void when_saveLarge_success_thenSucceed() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));


        when(fsTemplate.store(filePart.content(), filePart.filename())).thenReturn(Mono.just(new ObjectId("62c314e22525c96a4ae223b3")));
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");

        StepVerifier.create(mediaService.saveLarge(filePart)).consumeNextWith(objectId1 -> {
            assertEquals(expected, objectId1);
        }).verifyComplete();

    }

    @Test
    void when_saveLarge_fails_thenThrows() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));


        when(fsTemplate.store(any(), anyString()))
                .thenThrow(new RuntimeException());

        StepVerifier.create(mediaService.saveLarge(filePart))
                .verifyErrorSatisfies(error -> {

        });

    }

    @Test
    void when_downloadLarge_success_thenSucceed() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
        ServerHttpResponse serverHttpResponse = mock(ServerHttpResponse.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.just(gridFSFile));
        when(fsTemplate.getResource(gridFSFile)).thenReturn(Mono.just(reactiveGridFsResource));
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(reactiveGridFsResource.getDownloadStream()).thenReturn(Flux.just(db));
        when(serverHttpResponse.writeWith(reactiveGridFsResource.getDownloadStream())).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.downloadLarge("test-id", serverWebExchange))
                .verifyComplete();

    }

    @Test
    void when_downloadLarge_fails_thenThrows() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
        ServerHttpResponse serverHttpResponse = mock(ServerHttpResponse.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.empty());


        StepVerifier.create(mediaService.downloadLarge("test-id", serverWebExchange)).verifyErrorSatisfies(error->{
            assertTrue(error instanceof RepositoryException);
        });

    }
    @Test
    void when_testSaveLargeBS_success_thenSucceed() {
        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(bs.asReadOnlyByteBuffer())).thenReturn(db);
        when(fsTemplate.store(any(), anyString())).thenReturn(Mono.just(oi));


        StepVerifier.create(mediaService.saveLarge(bs, "some-file-name")).consumeNextWith(noi->{
            assertEquals(oi, noi);
        }).verifyComplete();

    }

    @Test
    void when_testSaveLargeBS_fails_thenThrows() {
        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(bs.asReadOnlyByteBuffer())).thenThrow(new RuntimeException());

        StepVerifier.create(mediaService.saveLarge(bs, "some-file-name")).verifyErrorSatisfies(error->{
            assertTrue(error instanceof RepositoryException);
        });

    }

    @Test
    void when_testDownloadLargeById_success_thenSucceed() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.just(gridFSFile));
        when(fsTemplate.getResource(gridFSFile)).thenReturn(Mono.just(reactiveGridFsResource));
        when(reactiveGridFsResource.getInputStream()).thenReturn(Mono.just(db.asInputStream()));

        StepVerifier.create(mediaService.downloadLarge("test-id")).consumeNextWith(Assertions::assertNotNull)
                .verifyComplete();


    }
    @Test
    void when_testDownloadLargeById_fails_thenThrows() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.downloadLarge("test-id")).verifyErrorSatisfies(error->{
            assertTrue(error instanceof RepositoryException);
        });


    }

    @Test
    void when_downloadStreamSuccess_thenSucceed() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.just(gridFSFile));
        when(fsTemplate.getResource(gridFSFile)).thenReturn(Mono.just(reactiveGridFsResource));
        when(reactiveGridFsResource.getDownloadStream()).thenReturn(Flux.just(db));

        StepVerifier.create(mediaService.downloadStream("test-id")).consumeNextWith(Assertions::assertNotNull)
                .verifyComplete();

    }

    @Test
    void when_downloadStreamFails_thenThrows() {
        BsonValue bsonValue = mock(BsonValue.class);
        GridFSFile gridFSFile = new GridFSFile(bsonValue, "fcuk", 1, 1, new Date(), null);
        ReactiveGridFsResource reactiveGridFsResource = mock(ReactiveGridFsResource.class);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db=  dataBufferFactory.wrap(new byte[]{1, 2, 3});

        when(fsTemplate.findOne(query(where("_id").is("test-id")))).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.downloadStream("test-id")).verifyErrorSatisfies(error->{
            assertTrue(error instanceof RepositoryException);
                });


    }


    @Test
    void when_uploadStream_success_thenSucceed() {
        var stream = Flux.just(ByteString.copyFrom(new byte[]{1, 2, 3}));
        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(bs.toByteArray())).thenReturn(db);
        when(dataBufferService.joinDataBuffers(any())).thenReturn(db);
        when(fsTemplate.store(any(), anyString())).thenReturn(Mono.just(oi));

        StepVerifier.create(mediaService.uploadStream(stream, "my-file")).consumeNextWith(
                result->{
                    assertEquals(oi, result);
                }
        ).verifyComplete();
    }

    @Test
    void when_uploadStream_fails_thenThrows() {
        var stream = Flux.just(ByteString.copyFrom(new byte[]{1, 2, 3}));
        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(bs.toByteArray())).thenReturn(db);
        when(dataBufferService.joinDataBuffers(any())).thenReturn(db);
        when(fsTemplate.store(any(), anyString())).thenThrow(new RuntimeException());

        StepVerifier.create(mediaService.uploadStream(stream, "my-file")).verifyErrorSatisfies(
                error->{
                    assertTrue(error instanceof RepositoryException);
                }
        );
    }

    @Test
    void when_deleteFile_success_thenReturns(){
        when(fsTemplate.delete(any()))
                .thenReturn(Mono.empty());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        StepVerifier.create(mediaService.delete(oi))
                .consumeNextWith(objectId -> {
                    assertEquals(oi, objectId);
                }).verifyComplete();
    }

    @Test
    void when_deleteFile_fails_thenThrows(){
        when(fsTemplate.delete(any()))
                .thenThrow(new RuntimeException());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        StepVerifier.create(mediaService.delete(oi))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RepositoryException);
                });
    }

    @Test
    void when_batchUpload_success_thenReturns(){
        var uploadRequest = List.of(UploadRequest.newBuilder()
                        .setKey(Key
                                .newBuilder()
                                .setKey("hello").build()).build(),
                UploadRequest.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey("world").build()).build());
        var request = BatchUploadRequest.newBuilder()
                .addAllUploadRequests(uploadRequest).build();

        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(any(ByteBuffer.class))).thenReturn(db);
        when(fsTemplate.store(any(), anyString())).thenReturn(Mono.just(oi));


        StepVerifier.create(mediaService.batchUpload(request))
                .consumeNextWith(response->{
                    assertEquals(2, response.size());
                }).verifyComplete();
    }

    @Test
    void when_batchUpload_fails_cantConvert_thenThrows(){
        var uploadRequest = List.of(UploadRequest.newBuilder()
                        .setKey(Key
                                .newBuilder()
                                .setKey("hello").build()).build(),
                UploadRequest.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey("world").build()).build());
        var request = BatchUploadRequest.newBuilder()
                .addAllUploadRequests(uploadRequest).build();

        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(any(ByteBuffer.class))).thenThrow(new RuntimeException());

        StepVerifier.create(mediaService.batchUpload(request))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RepositoryException);
                });
    }

    @Test
    void when_batchUpload_fails_cantStore_thenThrows(){
        var uploadRequest = List.of(UploadRequest.newBuilder()
                        .setKey(Key
                                .newBuilder()
                                .setKey("hello").build()).build(),
                UploadRequest.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey("world").build()).build());
        var request = BatchUploadRequest.newBuilder()
                .addAllUploadRequests(uploadRequest).build();

        byte[] bytes = new byte[]{1, 2, 3};
        var bs = ByteString.copyFrom(bytes);
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        var db = dataBufferFactory.wrap(bs.asReadOnlyByteBuffer());

        var oi = new ObjectId("62c314e22525c96a4ae223b3");
        when(dataBufferService.makeDataBuffer(any(ByteBuffer.class))).thenThrow(new RuntimeException());


        StepVerifier.create(mediaService.batchUpload(request))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RepositoryException);
                });
    }
}