package org.example;

import org.example.request.types.Request;
import org.example.request.types.RequestParser;
import org.example.request.types.get.Get;
import org.example.request.types.post.Post;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Main {

    private static ServerSocketChannel SS;
    private static Selector selector;


    public static void main(String[] args) throws IOException {
        //  Get the selector
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
            selector.select();
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
        } catch(IOException e) {
            // Channel has been closed
        }
    }

    //  READ DATA FROM CLIENT
    public static void readable(SelectionKey ky) {
        try {
            SocketChannel client = (SocketChannel) ky.channel();
            ByteBuffer buffer = ByteBuffer.allocate(256);

            StringBuilder requestBuilder = new StringBuilder();
            while(true) {
                int bytesRead = client.read(buffer);
                if(bytesRead == -1) {
                    client.close();
                    break;
                }
                buffer.flip();

                requestBuilder.append(new String(buffer.array(), 0, buffer.limit()));
                buffer.clear();

                if(isRequestComplete(requestBuilder.toString())) {
                    System.out.println("Parsing data...");
                    Request req = RequestParser.parseRequest(requestBuilder.toString());
                    switch(req.getRequestType()) {
                        case GET -> System.out.println("GET REQUEST");
                        case POST -> System.out.println("POST REQUEST");
                        case PUT -> System.out.println("PUT REQUEST");
                        default -> System.out.println("UNKNOWN REQUEST TYPE");
                    }
                    String endpoint = switch(req) {
                        case Get g -> g.getEndpoint();
                        case Post p -> p.getEndpoint();
                        default -> null;
                    };
                    System.out.println(endpoint);
                    break;
                }

            }
        } catch(IOException e) {
            System.out.println("Channel is closed");
        }
    }

    public static boolean isRequestComplete(String request) {
        int headersEndIndex = request.indexOf("\r\n\r\n");
        if(headersEndIndex == -1) {
            return false;   //Not finished parsing
        }

        String headers = request.substring(0, headersEndIndex);
        if(headers.startsWith("POST ") || headers.startsWith("PUT ") || headers.startsWith("PATCH ")) {
            String[] headerLines = headers.split("\r\n");
            for(String line : headerLines) {
                if(line.toLowerCase().startsWith("content-length:")) {
                    int contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                    //Check if full body received
                    int bodyStartIndex = headersEndIndex + 4; //    Skip \r\n\r\n
                    return request.length() - bodyStartIndex >= contentLength;
                }
            }
            return false;
        }
        return true;
    }
}