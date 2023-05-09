package com.example.grpcclient;

import com.example.HelloRequest;
import com.example.HelloResponse;
import com.example.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcClientApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(GrpcClientApplication.class, args);

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.sayHello(HelloRequest.newBuilder()
                .setName("Mj")
                .setAge("100")
                .build());
        System.out.println(helloResponse.getMessage());
        System.out.println("Done Phase 1");

        System.out.println("Starting Phase 2");
        StockService stockService = new StockService(channel);
        stockService.clientSideStreamingGetStatisticsOfStocks();

        System.out.println("Done Phase 2");

        channel.shutdown();
    }

}
