package com.media.quickmedia.service;

import com.media.quickmedia.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final ImageRepository imageRepository;
    private final ReactiveGridFsTemplate gridFsTemplate;

    public Mono<ObjectId> saveLarge(FilePart filePart){
        return gridFsTemplate.store(filePart.content(), filePart.filename());
    }

    public Flux<Void> downloadLarge(String id, ServerWebExchange exchange){
        return gridFsTemplate.findOne(query(where("_id").is(id)))
                .log()
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }


}
