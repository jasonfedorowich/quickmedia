package com.media.quickmedia.restcontroller;

import com.media.quickmedia.model.Image;
import com.media.quickmedia.restcontroller.error.RestControllerRequestException;
import com.media.quickmedia.service.ImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class RestImageControllerTest {

    @Mock
    ImageService imageService;

    RestImageController restImageController;

    @BeforeEach
    void setUp() {
        restImageController = new RestImageController(imageService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void when_addNewImage_success_thenSucceed() {
        FilePart filePart = mock(FilePart.class);
        Image image = Image.builder().build();
        when(imageService.saveImage(filePart)).thenReturn(Mono.just(image));

        StepVerifier.create(restImageController.addNewImage(Mono.just(filePart)))
                .consumeNextWith(response->{
                    assertEquals(image, response);
                }).verifyComplete();
    }

    @Test
    void when_addNewImage_fails_thenThrows(){
        FilePart filePart = mock(FilePart.class);
        Image image = Image.builder().build();
        when(imageService.saveImage(filePart)).thenThrow(new RuntimeException());

        StepVerifier.create(restImageController.addNewImage(Mono.just(filePart)))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });

    }

    @Test
    void when_getImage_success_thenSucceed(){
        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();
        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(image.getContent()));

        when(imageService.getImage(any())).thenReturn(Mono.just(inputStreamResource));

        StepVerifier.create(restImageController.getImage("id")).consumeNextWith(inputStreamResourceResponseEntity -> {
            assertEquals(inputStreamResource, inputStreamResourceResponseEntity.getBody());
            assertEquals(HttpStatus.OK, inputStreamResourceResponseEntity.getStatusCode());
        }).verifyComplete();
    }

    @Test
    void when_getImage_fails_thenThrows() {
        when(imageService.getImage("hello")).thenThrow(new RuntimeException());

        StepVerifier.create(restImageController.getImage("hello"))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof RestControllerRequestException);
        });
    }

    @Test
    void when_deleteImage_success_thenReturns(){
        when(imageService.removeImage(anyString())).thenReturn(Mono.just("my-id"));

        StepVerifier.create(restImageController.deleteImage("my-id"))
                .consumeNextWith(response->{
                    assertEquals("my-id", response);
                }).verifyComplete();
    }

    @Test
    void when_deleteImage_fails_thenThrows(){
        when(imageService.removeImage(anyString())).thenThrow(new RuntimeException());

        StepVerifier.create(restImageController.deleteImage("my-id"))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RestControllerRequestException);
                });
    }


}