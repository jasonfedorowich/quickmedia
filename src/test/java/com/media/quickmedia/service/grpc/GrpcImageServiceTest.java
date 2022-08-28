package com.media.quickmedia.service.grpc;

import com.google.protobuf.ByteString;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.service.ImageService;
import com.proto.service.*;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcImageServiceTest {

    GrpcImageService grpcImageService;

    @Mock
    ImageService imageService;

    @BeforeEach
    void init(){
        grpcImageService = new GrpcImageService(imageService);
    }

    @Test
    void when_upload_success_thenSuccess() {
        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        var image = Image.builder().name("test")
                        .content(new byte[]{1, 2, 3})
                .id("my-id")
                                .build();

        when(imageService.saveImage(any(), anyString())).thenReturn(Mono.just(image));
        var request = UploadRequest.newBuilder()
                        .setData(DataChunk.newBuilder()
                                .setData(byteString)
                                .build())
                                .setKey(Key
                                        .newBuilder()
                                        .setKey("my-test").build()).build();

        StepVerifier.create(grpcImageService.upload(Mono.just(request)))
                .consumeNextWith(uploadResponse -> {
                    assertEquals("my-id", uploadResponse.getKey().getKey());
                }).verifyComplete();
    }

    @Test
    void when_upload_fails_thenThrows(){
        when(imageService.saveImage(any(), anyString())).thenThrow(new RuntimeException());

        var byteString = ByteString.copyFrom(new byte[]{1, 2, 3});
        var request = UploadRequest.newBuilder()
                .setData(DataChunk.newBuilder()
                        .setData(byteString)
                        .build())
                .setKey(Key
                        .newBuilder()
                        .setKey("my-test").build()).build();

        StepVerifier.create(grpcImageService.upload(Mono.just(request)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });
    }

    @Test
    void when_download_success_thenSuccess() {
        var byteArrayInputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(imageService.getImageStream(any())).thenReturn(Mono.just(byteArrayInputStream));
        var downloadRequest = DownloadRequest.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey("my-id").build()).build();

        StepVerifier.create(grpcImageService.download(Mono.just(downloadRequest)))
                .consumeNextWith(downloadResponse -> {
                    assertTrue(downloadResponse.hasData());
                }).verifyComplete();
    }

    @Test
    void when_download_fails_thenThrows(){
        var byteArrayInputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(imageService.getImageStream(any())).thenThrow(new RuntimeException());
        var downloadRequest = DownloadRequest.newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();

        StepVerifier.create(grpcImageService.download(Mono.just(downloadRequest)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof StatusRuntimeException);
                });
    }

    @Test
    void when_delete_success_thenReturns(){
        when(imageService.removeImage(anyString())).thenReturn(Mono.just("my-id"));

        var request = DeleteRequest
                .newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();
        StepVerifier.create(grpcImageService.deleteImage(Mono.just(request)))
                .consumeNextWith(deleteResponse -> {
                    assertEquals("my-id", deleteResponse.getKey().getKey());
                }).verifyComplete();
    }

    @Test
    void when_delete_fails_thenThrows(){
        when(imageService.removeImage(anyString())).thenThrow(new RuntimeException());

        var request = DeleteRequest
                .newBuilder()
                .setKey(Key.newBuilder()
                        .setKey("my-id").build()).build();
        StepVerifier.create(grpcImageService.deleteImage(Mono.just(request)))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof StatusRuntimeException);
                });
    }


}