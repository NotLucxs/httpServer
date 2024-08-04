package org.example.request.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestType {
    GET("Get"),
    POST("Post"),
    PUT("Put");

    private final String type;

    @Override
    public String toString() {
        return type;
    }
}
