package com.media.quickmedia.service;

import com.media.quickmedia.model.Image;
import com.media.quickmedia.model.Video;
import com.media.quickmedia.repository.VideoRepository;
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
public class VideoService {

    private final VideoRepository videoRepository;

    public Mono<Video> saveVideo(FilePart filePart){
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> Mono.just(dataBuffer.asByteBuffer().array()))
                .flatMap(bytes -> {
                    log.info("Saving file with size: {}", bytes.length);

                    return Mono.just(Video.builder()
                            .name(filePart.filename())
                            .content(bytes)
                            .build());
                })
                .flatMap(videoRepository::save)
                .doOnError(ignored->{
                    throw new RepositoryException(String.format("Failed to save video: %s", filePart.filename()));
                });

    }

    public Mono<InputStreamResource> getVideo(String id){
        return videoRepository.findById(id)
                .flatMap(image -> {
                    InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(image.getContent()));
                    return Mono.just(inputStreamResource);
                })
                .switchIfEmpty(Mono.error(new RepositoryException(String.format("Unable to find file with id: %s", id))));
    }

    public Mono<String> delete(String id){
        return Mono.just(id)
                .flatMap(videoRepository::deleteById)
                .doOnError(error->{
                    throw new RepositoryException(String.format("Failed to remove video with id: %s", id));
                })
                .thenReturn(id);
    }
}
