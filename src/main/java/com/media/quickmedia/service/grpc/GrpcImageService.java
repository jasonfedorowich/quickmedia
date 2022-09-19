package com.media.quickmedia.service.grpc;

import com.google.protobuf.ByteString;
import com.media.quickmedia.model.Image;
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
import java.util.stream.Collectors;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class GrpcImageService extends ReactorImageServiceGrpc.ImageServiceImplBase {

    //todo addd it tests
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

    @Override
    public Mono<DeleteResponse> deleteImage(Mono<DeleteRequest> request) {
        return request.doOnNext(next->{
            log.info("Received request to delete image {}", next.getKey().getKey());
        })
                .flatMap(deleteRequest -> imageService.removeImage(deleteRequest.getKey().getKey()))
                .flatMap(objectId-> Mono.just(DeleteResponse.newBuilder()
                        .setKey(Key.newBuilder()
                                .setKey(objectId).build()).build()))
                .onErrorMap(ignored->{
                    log.error("Error received from delete: {}", ignored.getMessage());
                    throw new StatusRuntimeException(Status.UNAVAILABLE);
                });
    }

    @Override
    public Mono<BatchUploadResponse> batchUpload(Mono<BatchUploadRequest> request) {
        return request.doOnNext(next->{
            log.info("Received request to batchUpload image");
        })
                .flatMap(imageService::batchUpload)
                .flatMap(images -> {
                    var uploads = images.stream()
                            .map(Image::getId)
                            .map(id-> UploadResponse
                                    .newBuilder()
                                    .setKey(Key.newBuilder()
                                            .setKey(id).build()).build())
                            .toList();

                    return Mono.just(BatchUploadResponse
                            .newBuilder()
                            .addAllUploadResponse(uploads).build());
                })
                .doOnError(ignored->{
                    log.error("Error received from batch upload: {}", ignored.getMessage());
                    throw new StatusRuntimeException(Status.UNAVAILABLE);
                });
    }

    @Override
    public Mono<MetaDataResponse> getMetaData(Mono<MetaDataRequest> request) {
        return request.doOnNext(next-> log.info("Received request to getMetaData image"))
                .flatMap(imageService::getMetaData)
                .doOnError(ignored->{
                    log.error("Error received from get metadata: {}", ignored.getMessage());
                    throw new StatusRuntimeException(Status.UNAVAILABLE);
                });
    }
}
