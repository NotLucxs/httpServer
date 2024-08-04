package org.example.request.types;

import org.example.request.types.post.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.request.types.HttpRequestHeader.CONTENT_LENGTH;

public class RequestParser {

    public static Request parseRequest(String request) {
        String[] lines = request.split("\r\n");

        return parseBaseRequest(lines);
    }

    private static Request parseBaseRequest(String[] lines) {
        String[] requestLine = lines[0].split(" ");
        Request.RequestBuilder<?, ?> requestBuilder = Request.builder();
        //  Request Type, Endpoint and HTTP Version
        System.out.println("Type: "+RequestType.valueOf(requestLine[0]));
        requestBuilder.requestType(RequestType.valueOf(requestLine[0])).endpoint(requestLine[1]).version(requestLine[2]);
        //  Headers
        Map<String, List<String>> headers = new HashMap<>();
        int headersEndIndex = -1;
        for(int i=1; i<lines.length; i++) {
            if(lines[i].isEmpty()) {
                if(lines[i-1].isEmpty()) {
                    headersEndIndex = i;
                    break;
                }
                continue;
            }

            int index = lines[i].indexOf(":");
            String header = lines[i].substring(0, index);
            List<String> values = new ArrayList<>();
            String value = lines[i].substring(index+1);

            if(value.contains(",")) {
                String[] subValues = value.split(",");
                for(String v : subValues) {
                    values.add(v.trim());
                }
            } else {
                values.add(value.trim());
            }
            headers.put(header, values);
        }
        requestBuilder.headers(headers);
        if(headers.containsKey(CONTENT_LENGTH.getHeaderName())) {
            String body = parseBody(headersEndIndex, lines);
            return Post.fromRequest(requestBuilder.build(), body);
        }
        return requestBuilder.build();
    }

    private static String parseBody(int index, String[] lines) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = index; i<lines.length; i++) {
            stringBuilder.append(lines[i]);
        }
        return stringBuilder.toString();
    }
}
