package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.repository.ImageRepository;
import com.media.quickmedia.service.error.RepositoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

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
}
