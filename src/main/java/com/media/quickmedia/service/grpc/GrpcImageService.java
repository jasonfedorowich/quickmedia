package com.media.quickmedia.service.grpc;

import com.google.protobuf.ByteString;
import com.media.quickmedia.service.ImageService;
import com.proto.service.*;
import com.proto.service.DataChunk;
import com.proto.service.DownloadRequest;
import com.proto.service.DownloadResponse;
import com.proto.service.Key;
import com.proto.service.ReactorImageServiceGrpc;
import com.proto.service.UploadRequest;
import com.proto.service.UploadResponse;
import com.salesforce.grpc.contrib.spring.GrpcService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class GrpcImageService extends ReactorImageServiceGrpc.ImageServiceImplBase {

    private final ImageService imageService;
    @Override
    public Mono<UploadResponse> upload(Mono<UploadRequest> request) {
        return request.doOnNext(uploadRequest -> log.info("Received request: {}", uploadRequest.getKey().getKey()))
                .flatMap(uploadRequest -> imageService.saveImage(uploadRequest
                                .getData()
                                .getData(),

                                uploadRequest
                                .getKey()
                                .getKey())
                .flatMap(image -> Mono.just(UploadResponse.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey(image.getId()).build())
                        .build())))
                .onErrorMap(ignored-> {
                    log.error("Error received from upload: {}", ignored.getMessage());
                    throw new StatusRuntimeException(Status.UNAVAILABLE);
                });
    }

    @Override
    public Mono<DownloadResponse> download(Mono<DownloadRequest> request) {
        return request.doOnNext(downloadRequest -> {
            log.info("Received request: {}", downloadRequest.getKey().getKey());
        })
                .flatMap(downloadRequest -> imageService.getImageStream(downloadRequest
                        .getKey()
                        .getKey()))
                .flatMap(inputStream -> {
                    try {
                        return Mono.just(DownloadResponse
                                .newBuilder()
                                .setData(DataChunk
                                        .newBuilder()
                                        .setData(ByteString.readFrom(inputStream))
                                        .build())
                                .build());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                })
                .onErrorMap(ignored->{
                    log.error("Error received from download: {}", ignored.getMessage());
                    throw new StatusRuntimeException(Status.UNAVAILABLE);
                });
    }
}
