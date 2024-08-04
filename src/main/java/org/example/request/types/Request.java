package org.example.request.types;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Getter
public class Request implements IRequest{
    public RequestType requestType;
    public String endpoint;
    public String version;
    public Map<String, List<String>> headers;
}
