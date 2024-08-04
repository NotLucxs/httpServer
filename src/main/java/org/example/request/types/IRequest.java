package org.example.request.types;

import java.util.List;
import java.util.Map;

public interface IRequest {

    String getEndpoint();

    Map<String, List<String>> getHeaders();

    RequestType getRequestType();

    default String getHeaderValue(Map<String, List<String>> headers, HttpRequestHeader header) {
        return headers.get(header.getHeaderName()).getFirst();
    }

}
