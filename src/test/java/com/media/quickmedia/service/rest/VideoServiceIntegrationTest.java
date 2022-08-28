package com.media.quickmedia.service.rest;

import com.google.gson.Gson;
import com.media.quickmedia.config.MongoDbTestConfiguration;
import com.media.quickmedia.model.Video;
import com.media.quickmedia.repository.VideoRepository;
import com.media.quickmedia.repository.config.GridFsConfiguration;
import com.media.quickmedia.restcontroller.RestVideoController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Import({MongoDbTestConfiguration.class})
@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@Slf4j
public class VideoServiceIntegrationTest {
    @Autowired
    VideoRepository videoRepository;

    @Autowired
    RestVideoController restVideoController;

    @Autowired
    GridFsConfiguration gridFsConfiguration;

    WebTestClient webTestClient;

    byte[] bytes = new byte[]{1, 2, 3};

    @BeforeEach
    void init(){
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:9000").build();
    }

    @AfterEach
    void tearDown(){
    }

    @Test
    public void when_uploadVideo_success_thenDownload_videoSuccess_thenSucceed(){
        var video = uploadFile();
        assertFalse(video.getId().isEmpty());
        downloadVideo(video.getId());
        deleteVideo(video.getId());

    }

    private void downloadVideo(String id) {
        var res = webTestClient.get()
                .uri("videos/download/" + id)
                .exchange()
                .expectBody()
                .returnResult();
        var bytesFromRequest = res.getResponseBody();

        assertTrue(res.getStatus().is2xxSuccessful());
        assertNotNull(bytesFromRequest);
        assertEquals(bytes.length, bytesFromRequest.length);
        assertEquals(bytes[0], bytesFromRequest[0]);
        assertEquals(bytes[1], bytesFromRequest[1]);
        assertEquals(bytes[2], bytesFromRequest[2]);


    }

    private Video uploadFile(){
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", bytes, MULTIPART_FORM_DATA).filename("my-file.mkv");
        var res = webTestClient
                .post()
                .uri("/videos/upload")
                .contentType(MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                ;

        var bytes = res.expectBody()
                .returnResult()
                .getResponseBody();
        Assertions.assertNotNull(bytes);
        var json = new String(bytes);
        Gson gson = new Gson();
        Video video = gson.fromJson(json, Video.class);
        return video;


    }

    private void deleteVideo(String id){
        var res = webTestClient
                .delete()
                .uri("/videos/" + id)
                .exchange()
                ;
        var bytesFromResponse = res.expectBody()
                .returnResult()
                .getResponseBody();

        assertNotNull(bytesFromResponse);
        assertTrue(res.expectBody().returnResult().getStatus().is2xxSuccessful());
        assertEquals(id, new String(bytesFromResponse));
    }

}
