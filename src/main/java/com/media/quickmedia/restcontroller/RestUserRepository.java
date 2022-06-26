package com.media.quickmedia.restcontroller;

import com.media.quickmedia.model.User;
import com.media.quickmedia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class RestUserRepository {

    private final UserService userService;

    @PostMapping
    public Mono<User> createNewUser(@RequestBody Mono<User> userMono){
        return userService.createNewUser(userMono);
    }

}
