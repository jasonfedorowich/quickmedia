package com.media.quickmedia.restcontroller;

import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import com.media.quickmedia.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@Slf4j
public class LargeMediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/large-upload")
    public Mono<String> addLargeImage(@RequestPart("file") Mono<FilePart> filePartMono){
        return filePartMono.doOnNext(filePart -> {
                    log.info("Received new file with name {}", filePart.filename());
                }).flatMap(mediaService::saveLarge)
                .flatMap(objectId -> Mono.just(objectId.toHexString()))
                .onErrorMap(error->{
                    throw new RestControllerRequestException(error);
                });


    }

    @GetMapping(value = "/large-download/{id}",
            produces = APPLICATION_OCTET_STREAM_VALUE)
    public Flux<Void> getLargeImage(@PathVariable("id") String id, ServerWebExchange serverWebExchange) {
        return Mono.just(id)
                .flatMapMany(_id-> {
                    return mediaService.downloadLarge(_id, serverWebExchange);
                })
                .log()
                .onErrorMap(error->{
                    throw new RestControllerRequestException(error);
                });
    }

    //todo add name validation ie. png jpeg or movie
    //add a endpoint to get the metadata from an image

    @DeleteMapping("/{id}")
    public Mono<String> delete(@PathVariable("id") String id){
        return Mono.just(id)
                .flatMap(_id-> mediaService.delete(new ObjectId(_id)))
                .onErrorMap(error->{
                    log.error("Failed to delete file: {}", error.getMessage());
                    throw new RestControllerRequestException(error);
                })
                .log()
                .thenReturn(id);
    }


}
