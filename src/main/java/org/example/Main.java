package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static ServerSocketChannel SS;
    private static Selector selector;
    private static ConcurrentHashMap<SocketChannel, Long> heartbeats = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws IOException {
        //  Start heartbeat
        Heartbeat heartbeat = new Heartbeat(heartbeats);
        executor.scheduleAtFixedRate(heartbeat::run, 0, 5, TimeUnit.SECONDS);
        // Get the selector
        selector = Selector.open();
        System.out.println("Selector is open for making connection: " + selector.isOpen());
        // Get the server socket channel and register using selector
        SS = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8080);
        SS.bind(hostAddress);
        SS.configureBlocking(false);
        int ops = SS.validOps();
        SelectionKey selectKy = SS.register(selector, ops, null);
        while(true) {
//            System.out.println("Waiting for the select operation...");
            int noOfKeys = selector.select();
//            System.out.println("The Number of selected keys are: " + noOfKeys);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> itr = selectedKeys.iterator();
            while (itr.hasNext()) {
                SelectionKey ky = itr.next();
                if (ky.isAcceptable()) {
                    // The new client connection is accepted
                    acceptable();
                }
                else if (ky.isReadable()) {
                    readable(ky);
                }
                itr.remove();
            } // end of while loop
        } // end of for loop

    }

    //  ACCEPT NEW CLIENT
    public static void acceptable() {
        try {
            SocketChannel client = SS.accept();
            client.configureBlocking(false);
            // The new connection is added to a selector
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("The new connection is accepted from the client: " + client);
            heartbeats.put(client, System.currentTimeMillis());
        } catch(IOException e) {
            // Channel has been closed - possibly by heartbeat
            return;
        }
    }

    //  READ DATA FROM CLIENT
    public static void readable(SelectionKey ky) {
        try {
            SocketChannel client = (SocketChannel) ky.channel();
            ByteBuffer buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            byte[] data = buffer.array();
            if(data[0] != 0) {
                System.out.println("Parsing data...");
                String parsedMessage = HttpParser.parse(new String(data).trim());

                switch (parsedMessage) {
                    case "heartbeat" -> {
                        System.out.println("Heartbeat received...");
                        heartbeats.put(client, System.currentTimeMillis());
                    }
                    case "empty" -> {
                    }
                    default -> System.out.println(parsedMessage);
                }
                ;
            }
        } catch(IOException e) {
            System.out.println("Channel is closed");
        }
    }

}