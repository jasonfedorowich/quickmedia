package com.media.quickmedia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "images")
@Data
@Builder
@ToString
public class Image {

    @Id
    private String id;
    @JsonIgnore
    private byte[] content;
    private String name;

}
