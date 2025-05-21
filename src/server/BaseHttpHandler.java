package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import server.adapter.DurationAdapter;
import server.adapter.LocalDateTimeAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    protected Gson getGson() {
        return gson;
    }

    protected void sendResponse(HttpExchange exchange, String body) throws IOException {
        sendRawResponse(exchange, HttpCodeResponse.OK, body, true);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendRawResponse(exchange, HttpCodeResponse.NOT_FOUND, message, true);
    }

    protected void sendHasOverlap(HttpExchange exchange) throws IOException {
        sendRawResponse(exchange, HttpCodeResponse.OVERLAP, "", false);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendRawResponse(exchange, HttpCodeResponse.NOT_ALLOWED, "", false);
    }

    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        sendRawResponse(exchange, HttpCodeResponse.SERVER_ERROR, message, true);
    }

    private void sendRawResponse(HttpExchange exchange, HttpCodeResponse status, String body, boolean isJson)
            throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        if (isJson) {
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        }
        exchange.getResponseHeaders().add("X-Status-Reason", status.getReason());
        exchange.sendResponseHeaders(status.getCode(), response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

    protected Integer parseTaskId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}