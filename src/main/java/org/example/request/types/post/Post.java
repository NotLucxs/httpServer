package org.example.request.types.post;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.request.types.Request;

@SuperBuilder
@Getter
public class Post extends Request {
    public String body;

    public static Post fromRequest(Request request, String body) {
        return Post.builder().endpoint(request.getEndpoint())
                .requestType(request.getRequestType())
                .version(request.getVersion())
                .headers(request.getHeaders())
                .build();
    }
}
