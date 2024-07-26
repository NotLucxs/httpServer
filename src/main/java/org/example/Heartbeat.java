package org.example;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Heartbeat{

    private final ConcurrentHashMap<SocketChannel, Long> heartbeats;

    public Heartbeat(ConcurrentHashMap<SocketChannel, Long> client) {
        this.heartbeats = client;
    }


    public void run() {
        Iterator<Map.Entry<SocketChannel, Long>> iterator = heartbeats.entrySet().iterator();
        while(iterator.hasNext()) {
            System.out.println("Iterating...");
            Map.Entry<SocketChannel, Long> entry = iterator.next();
            System.out.println(entry.getValue());
            if(System.currentTimeMillis() - entry.getValue() > 15000L) {
                try {
                    System.out.println("Closing client: " + entry.getKey());
                    entry.getKey().close();
                    iterator.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
