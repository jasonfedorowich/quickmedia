package com.media.quickmedia.repository;

import com.media.quickmedia.model.Video;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VideoRepository extends ReactiveMongoRepository<Video, String> {
}
