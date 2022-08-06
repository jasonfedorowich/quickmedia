package com.media.quickmedia.config;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import org.springframework.context.annotation.Bean;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
@Configuration
public class MongoDbTestConfiguration {

    private static final String IP = "localhost";
    private static final int PORT = 9595;

    @Bean
    public ImmutableMongodConfig embeddedMongoConfiguration() throws IOException {
        return MongodConfig
                .builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(IP, PORT, Network.localhostIsIPv6()))
                .build();
    }
}
