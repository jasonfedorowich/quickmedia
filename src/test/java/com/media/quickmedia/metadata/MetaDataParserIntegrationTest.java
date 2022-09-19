package com.media.quickmedia.metadata;

import com.media.quickmedia.config.MongoDbTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.apache.tika.metadata.Metadata;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@Slf4j
@Import({MongoDbTestConfiguration.class})
@AutoConfigureDataMongo
public class MetaDataParserIntegrationTest {

    @Autowired
    private MetaDataParser metaDataParser;

    @Test
    void when_parse_thenSuccess(){
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("test_photo.jpg");
        Metadata metaData = metaDataParser.parse(inputStream);
        Assertions.assertNotNull(metaData);
    }
}
