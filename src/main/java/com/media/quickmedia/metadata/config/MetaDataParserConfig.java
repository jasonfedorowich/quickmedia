package com.media.quickmedia.metadata.config;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.ContentHandler;

@Configuration
public class MetaDataParserConfig {


    @Bean
    public ContentHandler contentHandler(){
        return new BodyContentHandler();
    }

    @Bean
    public ParseContext parseContext(){
        return new ParseContext();
    }

    @Bean
    public Parser parser(){
        return new AutoDetectParser();
    }

}
