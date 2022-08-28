package com.media.quickmedia.restcontroller;

import com.media.quickmedia.model.Video;
import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import com.media.quickmedia.service.VideoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestVideoControllerTest {

    @Mock
    VideoService videoService;

    RestVideoController restVideoController;

    @BeforeEach
    void setUp() {
        restVideoController = new RestVideoController(videoService);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void when_addNewVideo_success_thenSucceed() {
        FilePart filePart = mock(FilePart.class);
        Video video = Video.builder()
                .build();
        when(videoService.saveVideo(filePart)).thenReturn(Mono.just(video));

        StepVerifier.create(restVideoController.addNewVideo(Mono.just(filePart)))
                .consumeNextWith(video1 -> {
                    assertEquals(video, video1);
                }).verifyComplete();

    }

    @Test
    void when_addNewVideo_fails_thenThrows(){
        FilePart filePart = mock(FilePart.class);
        when(videoService.saveVideo(filePart)).thenThrow(new RuntimeException());

        StepVerifier.create(restVideoController.addNewVideo(Mono.just(filePart)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });
    }

    @Test
    void when_getVideo_success_thenSucceed() {
        var bytes = new byte[]{1,2,3};
        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));
        when(videoService.getVideo(any())).thenReturn(Mono.just(inputStreamResource));

        StepVerifier.create(restVideoController.getVideo("hadouken"))
                .consumeNextWith(inputStreamResourceResponseEntity -> {
                    assertTrue(inputStreamResourceResponseEntity.getStatusCode().is2xxSuccessful());
                    assertTrue(Objects.requireNonNull(inputStreamResourceResponseEntity.getBody()).exists());
                }).verifyComplete();
    }

    @Test
    void when_getVideo_fails_thenThrows(){
        var bytes = new byte[]{1,2,3};
        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));
        when(videoService.getVideo(any())).thenThrow(new RuntimeException());

        StepVerifier.create(restVideoController.getVideo("hadouken"))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });

    }

    @Test
    void when_deleteVideo_success_thenReturns(){
        when(videoService.delete(anyString())).thenReturn(Mono.just("my-id"));

        StepVerifier.create(restVideoController.deleteVideo("my-id"))
                .consumeNextWith(response->{
                    assertEquals("my-id", response);
                }).verifyComplete();

    }

    @Test
    void when_deleteVideo_fails_thenThrows(){
        when(videoService.delete(anyString())).thenThrow(new RuntimeException());

        StepVerifier.create(restVideoController.deleteVideo("my-id"))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });
    }
}