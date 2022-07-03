package com.media.quickmedia.service.grpc.server;

import com.google.common.util.concurrent.UncaughtExceptionHandlers;
import com.media.quickmedia.service.grpc.GrpcImageService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcImageServer implements CommandLineRunner {

    private Server server;
    private final GrpcImageService grpcImageService;


    @Override
    public void run(String... args) throws Exception {

       server = NettyServerBuilder.forPort(9999)
                .addService(grpcImageService)
                .executor(getServerExecuter(1))
                .build();

        server.start();
        log.info("Grpc service started...");
    }

    private Executor getServerExecuter(int numOfThreads){
        return new ForkJoinPool(numOfThreads,
                new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                    final AtomicInteger integer = new AtomicInteger();
                    @Override
                    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                        var thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory
                                .newThread(pool);
                        thread.setDaemon(true);
                        thread.setName("grpc-server");
                        return thread;
                    }
                } , UncaughtExceptionHandlers.systemExit(), true);
    }
}
