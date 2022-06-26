package com.media.quickmedia.service;

import com.media.quickmedia.model.User;
import com.media.quickmedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> createNewUser(Mono<User> userMono){
        return userMono.flatMap(userRepository::save);
    }
}
