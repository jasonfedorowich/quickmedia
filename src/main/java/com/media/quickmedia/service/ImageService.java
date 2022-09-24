package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.metadata.MetaDataParser;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.repository.ImageRepository;
import com.media.quickmedia.service.error.RepositoryException;
import com.proto.service.BatchUploadRequest;
import com.proto.service.ImageMetaData;
import com.proto.service.MetaDataRequest;
import com.proto.service.MetaDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final MetaDataParser metaDataParser;

    public Mono<Image> saveImage(FilePart filePart){
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> Mono.just(dataBuffer.asByteBuffer().array()))
                .flatMap(bytes -> {
                    log.info("Saving file with size: {}", bytes.length);

                    return Mono.just(Image.builder()
                            .name(filePart.filename())
                            .content(bytes)
                            .build());
                })
                .flatMap(imageRepository::save)
                .doOnError(error->{
                    throw new RepositoryException("Cannot save filepart: "+ filePart.filename());
                });

    }
    public Mono<Image> saveImage(ByteString bytes, String name){
        return Mono.just(Image.builder()
                .name(name)
                .content(bytes.toByteArray())
                        .build())
                .flatMap(imageRepository::save)
                .doOnError(error->{
                    throw new RepositoryException("Cannot save file: "+ name);
                });
    }

    public Mono<InputStreamResource> getImage(String id){
        return imageRepository.findById(id)
                .flatMap(image -> {
                    InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(image.getContent()));
                    return Mono.just(inputStreamResource);
                }).switchIfEmpty(Mono.error(new RepositoryException(String.format("Cannot find image by id %s", id))));
    }

    public Mono<ByteArrayInputStream> getImageStream(String id){
        return imageRepository.findById(id)
                .flatMap(image -> Mono.just(new ByteArrayInputStream(image.getContent())))
                .switchIfEmpty(Mono.error(new RepositoryException(String.format("Cannot find image by id %s", id))));
    }

    public Mono<String> removeImage(String id){
        return Mono.just(id)
                .flatMap(imageRepository::deleteById)
                .doOnError(error->{
                    throw new RepositoryException(String.format("Failed to remove image with id: %s", id));
                })
                .thenReturn(id);
    }

    @Transactional
    public Mono<List<Image>> batchUpload(BatchUploadRequest batchUploadRequest) {
        return Mono.just(batchUploadRequest)
                .flatMapIterable(BatchUploadRequest::getUploadRequestsList)
                .flatMap(uploadRequest -> {
                    var image = Image.builder()
                            .content(uploadRequest.getData().getData().toByteArray())
                            .name(uploadRequest.getKey().getKey())
                            .build();
                    return imageRepository.save(image);
                })
                .collectList()
                .doOnError(error->{
                    throw new RepositoryException(String.format("Failed to batch upload image with id: %s", batchUploadRequest.toString()));
                }
                );
    }

    public Mono<MetaDataResponse> getMetaData(MetaDataRequest metaDataRequest) {
        return Mono.just(metaDataRequest)
                .flatMap(request-> Mono.just(request.getKey().getKey()))
                .flatMap(imageRepository::findById)
                .flatMap(image->{
                    InputStream inputStream = new ByteArrayInputStream(image.getContent());
                    return Mono.just(metaDataParser.parse(inputStream));
                })
                .flatMap(metadata -> {
                    var metaDataBuilder = MetaDataResponse.newBuilder();
                    Arrays.stream(metadata.names())
                            .forEach(name -> metaDataBuilder.addMetaData(ImageMetaData.newBuilder()
                                    .setValue(metadata.get(name))
                                    .setKey(name).build()));
                    return Mono.just(metaDataBuilder.build());
                })
                .doOnError(error->{
                    throw new RepositoryException(String.format("Failed to get meta image with id: %s", metaDataRequest.toString()));
                });
    }
}
