package com.media.quickmedia.service;

import com.google.protobuf.ByteString;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.repository.ImageRepository;
import com.media.quickmedia.service.error.RepositoryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {


    @Mock
    private ImageRepository imageRepository;

    private ImageService imageService;

    @BeforeEach
    public void init(){
        imageService = new ImageService(imageRepository);
    }


    @Test
    void when_save_imageThenSuccess() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));

        Image image = Image.builder()
                        .name("test")
                                .content(new byte[]{1, 2, 3})
                                        .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.save(image)).thenReturn(Mono.just(image));

        StepVerifier.create(imageService.saveImage(filePart)).consumeNextWith(image1 -> {
            assertEquals(image1, image);
        }).verifyComplete();

    }

    @Test
    void when_saveImage_fails_imageThenThrows() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));

        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.save(image)).thenThrow(new RuntimeException());

        StepVerifier.create(imageService.saveImage(filePart)).verifyErrorSatisfies(error -> {
            assertTrue(error instanceof RepositoryException);
        });

    }

    @Test
    void when_saveImage_byteString_then_success() {
        var data = new byte[]{1, 2, 3};
        ByteString byteString = ByteString.copyFrom(data);

        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.save(image)).thenReturn(Mono.just(image2));

        StepVerifier.create(imageService.saveImage(byteString, "test")).consumeNextWith(image1 -> {
            assertEquals(image2, image1);
        }).verifyComplete();

    }
    @Test
    void when_saveImageFails_byteString_thenThrows() {
        var data = new byte[]{1, 2, 3};
        ByteString byteString = ByteString.copyFrom(data);

        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.save(image)).thenThrow(new RuntimeException());

        StepVerifier.create(imageService.saveImage(byteString, "test")).verifyErrorSatisfies(error -> {
            assertTrue(error instanceof RepositoryException);
        });
    }
    @Test
    void when_getImage_exists_thenSuccess() {
        var data = new byte[]{1, 2, 3};
        ByteString byteString = ByteString.copyFrom(data);

        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.findById("test")).thenReturn(Mono.just(image));

        StepVerifier.create(imageService.getImage("test")).consumeNextWith(inputStreamResource -> {
            assertTrue(inputStreamResource.exists());
        }).verifyComplete();

    }

    @Test
    void when_getImage_dne_thenThrows() {
        when(imageRepository.findById("test")).thenReturn(Mono.empty());

        StepVerifier.create(imageService.getImage("test")).verifyErrorSatisfies(error -> {
            assertTrue(error instanceof RepositoryException);
        });

    }


    @Test
    void when_getImageStreamStreamSuccess_thenSuccess() {
        var data = new byte[]{1, 2, 3};
        ByteString byteString = ByteString.copyFrom(data);

        Image image = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Image image2 = Image.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(imageRepository.findById("test")).thenReturn(Mono.just(image));

        StepVerifier.create(imageService.getImageStream("test"))
                .consumeNextWith(Assertions::assertNotNull)
                .verifyComplete();

    }

    @Test
    void when_getImageStreamDNE_thenSuccess() {

        when(imageRepository.findById("test")).thenReturn(Mono.empty());

        StepVerifier.create(imageService.getImageStream("test"))
                .verifyErrorSatisfies(error->{
                    assertTrue(error instanceof RepositoryException);
                });

    }
}