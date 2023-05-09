package com.example.grpcclient;

import com.example.Stock;
import com.example.StockQuote;
import com.example.StockQuoteProviderGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class.getName());

    private StockQuoteProviderGrpc.StockQuoteProviderBlockingStub blockingStub;
    private StockQuoteProviderGrpc.StockQuoteProviderStub nonBlockingStub;
    private List<Stock> stocks;

    public StockService(Channel channel) {
        blockingStub = StockQuoteProviderGrpc.newBlockingStub(channel);
        nonBlockingStub = StockQuoteProviderGrpc.newStub(channel);
        this.stocks = Arrays.asList(Stock.newBuilder()
                        .setTickerSymbol("AU")
                        .setCompanyName("Auburn Corp")
                        .setDescription("Aptitude Intel")
                        .build()
                , Stock.newBuilder()
                        .setTickerSymbol("BAS")
                        .setCompanyName("Bassel Corp")
                        .setDescription("Business Intel")
                        .build()
                , Stock.newBuilder()
                        .setTickerSymbol("COR")
                        .setCompanyName("Corvine Corp")
                        .setDescription("Corporate Intel")
                        .build()
                , Stock.newBuilder()
                        .setTickerSymbol("DIA")
                        .setCompanyName("Dialogic Corp")
                        .setDescription("Development Intel")
                        .build()
                , Stock.newBuilder()
                        .setTickerSymbol("EUS")
                        .setCompanyName("Euskaltel Corp")
                        .setDescription("English Intel")
                        .build());
    }

    public void serverSideStreamingListOfStockPrices() {
        Stock request = Stock.newBuilder()
                .setTickerSymbol("AU")
                .setCompanyName("Austich")
                .setDescription("server streaming example")
                .build();
        Iterator<StockQuote> stockQuotes;
        try {
            logInfo("REQUEST - ticker symbol {0}", request.getTickerSymbol());
            stockQuotes = blockingStub.serverSideStreamingGetListStockQuotes(request);
            for (int i = 1; stockQuotes.hasNext(); i++) {
                StockQuote stockQuote = stockQuotes.next();
                logInfo("RESPONSE - Price #" + i + ": {0}", stockQuote.getPrice());
            }
        } catch (StatusRuntimeException e) {
            logInfo("RPC failed: {0}", e.getStatus());
        }
    }

    public void clientSideStreamingGetStatisticsOfStocks() throws InterruptedException {
        StreamObserver<StockQuote> responseObserver = new StreamObserver<StockQuote>() {
            @Override
            public void onNext(StockQuote summary) {
                System.out.println("received ");
                logInfo("RESPONSE, got stock statistics - Average Price: {0}, description: {1}", summary.getPrice(), summary.getDescription());
            }

            @Override
            public void onError(Throwable t) {
                logInfo("error clientSideStreamingGetStatisticsOfStocks");
            }

            @Override
            public void onCompleted() {
                logInfo("Finished clientSideStreamingGetStatisticsOfStocks");
            }

        };

        StreamObserver<Stock> requestObserver = nonBlockingStub.clientSideStreamingGetStatisticsOfStocks(responseObserver);
        try {
            for (Stock stock : stocks) {
                logInfo("REQUEST: {0}, {1}", stock.getTickerSymbol(), stock.getCompanyName());
                requestObserver.onNext(stock);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
    }

    private void logInfo(String txt, Object... args) {
        for (int i = 0; i < args.length; i++) {
            txt = txt.replace("{" + i + "}", args[i].toString());
        }
        logger.info(txt);
    }
}
