package com.media.quickmedia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
@Data
@Builder
public class Video {

    @Id
    private String id;
    private String name;
    @JsonIgnore
    private byte[] content;
}
