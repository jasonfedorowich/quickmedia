package com.media.quickmedia.metadata;

import com.media.quickmedia.metadata.error.MetaDataException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MetaDataParser {

    private final Parser parser;
    private final ContentHandler contentHandler;
    private final ParseContext parseContext;

    public Metadata parse(InputStream inputStream) {
        Metadata metadata = new Metadata();
        try{
            parser.parse(inputStream, contentHandler, metadata, parseContext);
            return metadata;
        }catch(Exception exception){
            throw new MetaDataException(exception.getMessage());
        }

    }
}
