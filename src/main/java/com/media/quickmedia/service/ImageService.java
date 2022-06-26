package com.media.quickmedia.service;

import com.media.quickmedia.model.Image;
import com.media.quickmedia.repository.ImageRepository;
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
                .flatMap(imageRepository::save);

    }

    public Mono<InputStreamResource> getImage(String id){
        return imageRepository.findById(id)
                .flatMap(image -> {
                    InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(image.getContent()));
                    return Mono.just(inputStreamResource);
                });
    }
}
