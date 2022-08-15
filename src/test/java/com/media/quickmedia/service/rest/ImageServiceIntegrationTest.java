package com.media.quickmedia.service.rest;

import com.google.gson.Gson;
import com.media.quickmedia.QuickmediaApplication;
import com.media.quickmedia.config.MongoDbTestConfiguration;
import com.media.quickmedia.model.Image;
import com.media.quickmedia.repository.ImageRepository;
import com.media.quickmedia.repository.config.GridFsConfiguration;
import com.media.quickmedia.restcontroller.RestImageController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Import({MongoDbTestConfiguration.class})
@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@Slf4j
public class ImageServiceIntegrationTest {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    RestImageController restImageController;

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

    @Test
    public void when_uploadImage_success_thenDownload_imageSuccess_thenSucceed(){
        var image = uploadFile();
        assertFalse(image.getId().isEmpty());
        downloadImage(image.getId());

    }

    private void downloadImage(String id) {
        var res = webTestClient.get()
                .uri("images/download/" + id)
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

    private Image uploadFile(){
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", bytes, MULTIPART_FORM_DATA).filename("my-file.txt");
        var res = webTestClient
                .post()
                .uri("/images/upload")
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
        Image image = gson.fromJson(json, Image.class);
        return image;


    }



}
