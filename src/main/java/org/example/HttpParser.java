package org.example;

public class HttpParser {

    public static String parse(String request) {
        System.out.println("Request: "+request);
        if(request.trim().equals("heartbeat")) {
            return request;
        }
        String[] lines = request.split("\n");
        String requestType = lines[0].split(" ")[0];

        return switch (requestType) {
            case "GET" -> getRequest(lines);
            case "POST" -> postRequest(lines);
            default -> "empty";
        };
    }

    private static String getRequest(String[] request) {
        return "Received a GET request";
    }

    private static String postRequest(String[] request) {
        return "Received a POST request";
    }
}
