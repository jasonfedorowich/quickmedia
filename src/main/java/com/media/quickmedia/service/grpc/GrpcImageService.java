package com.media.quickmedia.service.grpc;

import com.proto.service.*;
import com.proto.service.DownloadRequest;
import com.proto.service.DownloadResponse;
import com.proto.service.ReactorImageServiceGrpc;
import com.proto.service.UploadRequest;
import com.proto.service.UploadResponse;
import com.salesforce.grpc.contrib.spring.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class GrpcImageService extends ReactorImageServiceGrpc.ImageServiceImplBase {
    @Override
    public Mono<UploadResponse> upload(Mono<UploadRequest> request) {
        return super.upload(request);
    }

    @Override
    public Mono<DownloadResponse> download(Mono<DownloadRequest> request) {
        return super.download(request);
    }
}
