package com.media.quickmedia.service.rest;

import com.google.gson.Gson;
import com.media.quickmedia.config.MongoDbTestConfiguration;
import com.media.quickmedia.repository.config.GridFsConfiguration;
import com.media.quickmedia.restcontroller.LargeMediaController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
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
public class MediaServiceIntegrationTest {

    @Autowired
    private ReactiveGridFsTemplate reactiveGridFsTemplate;

    @Autowired
    GridFsConfiguration gridFsConfiguration;

    @Autowired
    LargeMediaController largeMediaController;

    WebTestClient webTestClient;

    byte[] bytes = new byte[]{1, 2, 3};


    @BeforeEach
    public void init(){
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:9000").build();
    }

    @Test
    public void when_successUpload_thenDownload_thenReturns(){
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", bytes, MULTIPART_FORM_DATA).filename("my-file.mkv");

        var res = webTestClient
                .post()
                .uri("/media/large-upload")
                .contentType(MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                ;

        var bytes = res.expectBody()
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(bytes);
        Gson gson = new Gson();
        var objectId = new String(bytes);

        var res2 = webTestClient.get()
                .uri("media/large-download/" + objectId)
                .exchange()
                .expectBody()
                .returnResult();

        var bytesFromRequest = res2.getResponseBody();

        assertTrue(res2.getStatus().is2xxSuccessful());
        assertNotNull(bytesFromRequest);
        assertEquals(this.bytes.length, bytesFromRequest.length);
        assertEquals(this.bytes[0], bytesFromRequest[0]);
        assertEquals(this.bytes[1], bytesFromRequest[1]);
        assertEquals(this.bytes[2], bytesFromRequest[2]);

        deleteMedia(objectId);


    }

    private void deleteMedia(String id){
        var res = webTestClient
                .delete()
                .uri("/media/" + id)
                .exchange()
                ;
        var bytesFromResponse = res.expectBody()
                .returnResult()
                .getResponseBody();

        assertTrue(res.expectBody().returnResult().getStatus().is2xxSuccessful());
        assertNotNull(bytesFromResponse);
        String responseId = new String(bytesFromResponse);
        assertEquals(id, responseId);


    }
}
