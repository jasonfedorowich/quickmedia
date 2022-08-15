package com.media.quickmedia.restcontroller;

import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import com.media.quickmedia.service.MediaService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class LargeMediaControllerTest {

    private LargeMediaController largeMediaController;

    @Mock
    private MediaService mediaService;

    @BeforeEach
    void init(){
        largeMediaController = new LargeMediaController(mediaService);
    }

    @Test
    void when_addLarge_success_thenSuccess() {
        var expected = new ObjectId("62c314e22525c96a4ae223b3");

        FilePart filePart = mock(FilePart.class);

        when(mediaService.saveLarge(any())).thenReturn(Mono.just(expected));

        StepVerifier.create(largeMediaController.addLargeImage(Mono.just(filePart)))
                .consumeNextWith(next->{
                    assertEquals(expected.toHexString(), next);
                }).verifyComplete();
    }

    @Test
    void when_addLage_fails_thenThrows(){
        FilePart filePart = mock(FilePart.class);
        when(mediaService.saveLarge(any())).thenThrow(new RuntimeException());

        StepVerifier.create(largeMediaController.addLargeImage(Mono.just(filePart)))
                .verifyErrorSatisfies(error->{
                   assertTrue(error instanceof RestControllerRequestException);
                });
    }

    @Test
    void when_getLarge_success_thenSucceed() {
        var id = "test-id";
        ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
        when(mediaService.downloadLarge(id, serverWebExchange)).thenReturn(Flux.empty());

        StepVerifier.create(largeMediaController.getLargeImage(id, serverWebExchange))
                .verifyComplete();
    }

    @Test
    void when_getLarge_fails_thenThrows(){
        var id = "test-id";
        ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
        when(mediaService.downloadLarge(id, serverWebExchange)).thenThrow(new RuntimeException());

        StepVerifier.create(largeMediaController.getLargeImage(id, serverWebExchange))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });
    }
}