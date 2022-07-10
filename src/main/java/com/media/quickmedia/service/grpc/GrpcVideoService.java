package com.media.quickmedia.service.grpc;

import com.google.protobuf.ByteString;
import com.media.quickmedia.service.MediaService;
import com.proto.service.*;
import com.proto.service.DataChunk;
import com.proto.service.DownloadRequest;
import com.proto.service.DownloadResponse;
import com.proto.service.Key;
import com.proto.service.ReactorVideoServiceGrpc;
import com.proto.service.UploadRequest;
import com.proto.service.UploadResponse;
import com.salesforce.grpc.contrib.spring.GrpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class GrpcVideoService extends ReactorVideoServiceGrpc.VideoServiceImplBase {

    private final MediaService mediaService;

    @Override
    public Mono<UploadResponse> upload(Mono<UploadRequest> request) {
        return request.doOnNext(uploadRequest -> {
                    log.info("Received request: {}", uploadRequest.getKey().getKey());
                })
                .flatMap(uploadRequest ->
                mediaService.saveLarge(uploadRequest.getData().getData(), uploadRequest.getKey().getKey()))
                .flatMap(objectId -> Mono.just(
                        UploadResponse.newBuilder()
                        .setKey(
                                Key.newBuilder()
                                .setKey(objectId.toHexString()).build()).build()));
    }

    @Override
    public Mono<DownloadResponse> download(Mono<DownloadRequest> request) {
        return request.doOnNext(downloadRequest -> {
            log.info("Recieved request: {}", downloadRequest.getKey().getKey());
        })
                .flatMap(downloadRequest -> mediaService.downloadLarge(downloadRequest.getKey().getKey()))
                .flatMap(inputStream -> {
                    try {
                        return Mono.just(inputStream.readAllBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(bytes -> {
                    ByteString byteString = ByteString.copyFrom(bytes);
                    return Mono.just(byteString);
                })
                .flatMap(bytes -> Mono.just(DownloadResponse.newBuilder()
                        .setData(
                                DataChunk.newBuilder()
                                        .setData(bytes)
                                        .build()
                        )
                        .build()));

    }
}
