package com.media.quickmedia.repository;

import com.media.quickmedia.model.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ImageRepository extends ReactiveMongoRepository<Image, String> {
    //todo make pageable list response
}
