package com.media.quickmedia.service.grpc;

import com.google.protobuf.ByteString;
import com.media.quickmedia.service.MediaService;
import com.proto.service.*;
import io.grpc.StatusRuntimeException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcVideoServiceTest {

    GrpcVideoService grpcVideoService;

    @Mock
    MediaService mediaService;

    @BeforeEach
    void init(){
        grpcVideoService = new GrpcVideoService(mediaService);
    }

    @Test
    void when_upload_success_thenReturns() {
        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        var uploadRequest = UploadRequest.newBuilder()
                .setData(
                        DataChunk.newBuilder()
                                .setData(byteString)
                                .build()

                ).setKey(Key.newBuilder()
                        .setKey("my-id").build())
                .build();
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");

        when(mediaService.saveLarge(any(), anyString())).thenReturn(Mono.just(expected));

        StepVerifier.create(grpcVideoService.upload(Mono.just(uploadRequest)))
                .consumeNextWith(uploadResponse -> {
                    assertEquals(expected.toHexString(), uploadResponse.getKey().getKey());
                }).verifyComplete();
    }

    @Test
    void when_upload_fails_thenThrows(){
        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        var uploadRequest = UploadRequest.newBuilder()
                .setData(
                        DataChunk.newBuilder()
                                .setData(byteString)
                                .build()

                ).setKey(Key.newBuilder()
                        .setKey("my-id").build())
                .build();
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");

        when(mediaService.saveLarge(any(), anyString())).thenThrow(new RuntimeException());

        StepVerifier.create(grpcVideoService.upload(Mono.just(uploadRequest)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });

    }

    @Test
    void when_download_success_thenReturns() {
        var downloadRequest = DownloadRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();
        when(mediaService.downloadLarge(any())).thenReturn(Mono.just(new ByteArrayInputStream(new byte[]{1, 2, 3})));
        StepVerifier.create(grpcVideoService.download(Mono.just(downloadRequest)))
                .consumeNextWith(downloadResponse -> {
                    assertTrue(downloadResponse.hasData());
                    assertFalse(downloadResponse.getData().getData().isEmpty());
                }).verifyComplete();
    }

    @Test
    void when_download_fails_thenThrows(){
        var downloadRequest = DownloadRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();
        when(mediaService.downloadLarge(any())).thenThrow(new RuntimeException());
        StepVerifier.create(grpcVideoService.download(Mono.just(downloadRequest)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });


    }

    @Test
    void when_downloadStream_success_thenReturns() {
        var bytes = new byte[]{1, 2, 3};
        when(mediaService.downloadStream(any())).thenReturn(Flux.just(bytes));
        var downloadRequest = DownloadRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();

        StepVerifier.create(grpcVideoService.downloadStream(Mono.just(downloadRequest)))
                .consumeNextWith(downloadResponse -> {
                    assertTrue(downloadResponse.hasData());
                }).verifyComplete();
    }

    @Test
    void when_downloadStream_fails_thenThrows(){
        var bytes = new byte[]{1, 2, 3};
        when(mediaService.downloadStream(any())).thenThrow(new RuntimeException());
        var downloadRequest = DownloadRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();

        StepVerifier.create(grpcVideoService.downloadStream(Mono.just(downloadRequest)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });
    }

    @Test
    void when_uploadStream_success_thenReturns() {
        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");

        when(mediaService.uploadStream(any(), anyString())).thenReturn(Mono.just(expected));
        var uploadRequest = UploadRequest.newBuilder()
                .setData(
                        DataChunk.newBuilder()
                                .setData(byteString)
                                .build()

                ).setKey(Key.newBuilder()
                        .setKey("my-id").build())
                .build();

        StepVerifier.create(grpcVideoService.uploadStream(Flux.just(uploadRequest)))
                .consumeNextWith(uploadResponse -> {
                    assertTrue(uploadResponse.hasKey());
                    assertEquals(expected.toHexString(), uploadResponse.getKey().getKey());
                }).verifyComplete();
    }

    @Test
    void when_uploadStream_fails_thenThrow(){
        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");

        when(mediaService.uploadStream(any(), anyString())).thenThrow(new RuntimeException());
        var uploadRequest = UploadRequest.newBuilder()
                .setData(
                        DataChunk.newBuilder()
                                .setData(byteString)
                                .build()

                ).setKey(Key.newBuilder()
                        .setKey("my-id").build())
                .build();

        StepVerifier.create(grpcVideoService.uploadStream(Flux.just(uploadRequest)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });
    }

    @Test
    void when_deleteVideo_success_thenReturns(){
        ObjectId expected = new ObjectId("62c314e22525c96a4ae223b3");
        when(mediaService.delete(any())).thenReturn(Mono.just(expected));

        var request = DeleteRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("62c314e22525c96a4ae223b3").build())
                .build();

        StepVerifier.create(grpcVideoService.deleteVideo(Mono.just(request)))
                .consumeNextWith(deleteResponse -> {
                    assertEquals("62c314e22525c96a4ae223b3", deleteResponse.getKey().getKey());
                }).verifyComplete();
    }
    @Test
    void when_deleteVideo_fails_thenThrows(){
        when(mediaService.delete(any())).thenThrow(new RuntimeException());

        var request = DeleteRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("62c314e22525c96a4ae223b3").build())
                .build();

        StepVerifier.create(grpcVideoService.deleteVideo(Mono.just(request)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });

    }
}