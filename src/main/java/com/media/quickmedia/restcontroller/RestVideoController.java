package com.media.quickmedia.restcontroller;

import com.media.quickmedia.model.Image;
import com.media.quickmedia.model.Video;
import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import com.media.quickmedia.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
@Slf4j
public class RestVideoController {

    private final VideoService videoService;

    @PostMapping(value = "/upload")
    public Mono<Video> addNewVideo(@RequestPart("file") Mono<FilePart> filePartMono){
        return filePartMono.doOnNext(filePart -> {
            log.info("Received new file with name {}", filePart.filename());

        }).flatMap(videoService::saveVideo)
                .onErrorMap(error->{
                    throw new RestControllerRequestException(error);
                });
    }

    @GetMapping(value = "/download/{id}",
            produces = APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<InputStreamResource>> getVideo(@PathVariable("id") String id) {
        return Mono.just(id)
                .flatMap(videoService::getVideo)
                .flatMap(inputStreamResource ->
                        Mono.just(new ResponseEntity<>(inputStreamResource, HttpStatus.OK)))
                .onErrorMap(error->{
                    throw new RestControllerRequestException(error);
                });

    }

}
