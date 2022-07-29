package com.media.quickmedia.service;

import com.media.quickmedia.model.Image;
import com.media.quickmedia.model.Video;
import com.media.quickmedia.repository.VideoRepository;
import com.media.quickmedia.service.error.RepositoryException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoService = new VideoService(videoRepository);
    }

    @Test
    void when_saveVideo_success_thenSucceed() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));

        Video video = Video.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        Video video2 = Video.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .id("my-id")
                .build();

        when(videoRepository.save(video)).thenReturn(Mono.just(video2));

        StepVerifier.create(videoService.saveVideo(filePart)).consumeNextWith(video1 -> {
            assertEquals(video2, video1);
        }).verifyComplete();
    }

    @Test
    void when_saveVideo_fail_thenThrows() {
        var data = new byte[]{1, 2, 3};
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test");
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(data)));

        Video video = Video.builder()
                .name("test")
                .content(new byte[]{1, 2, 3})
                .build();

        when(videoRepository.save(video)).thenThrow(new RuntimeException());

        StepVerifier.create(videoService.saveVideo(filePart)).verifyErrorSatisfies(error -> {
            assertTrue(error instanceof RepositoryException);
        });
    }

    @Test
    void when_getVideo_success_thenSucceed(){
        Video video = Video.builder()
                .name("test")
                .id("my-id")
                .content(new byte[]{1, 2, 3})
                .build();

        when(videoRepository.findById("my-id")).thenReturn(Mono.just(video));

        StepVerifier.create(videoService.getVideo("my-id")).consumeNextWith(inputStreamResource -> {
           assertTrue(inputStreamResource.exists());
        }).verifyComplete();


    }

    @Test
    void when_getVideo_DNE_thenThrows() {
        when(videoRepository.findById("my-id")).thenReturn(Mono.empty());

        StepVerifier.create(videoService.getVideo("my-id")).verifyErrorSatisfies(error -> {
            assertTrue(error instanceof RepositoryException);
        });
    }
}