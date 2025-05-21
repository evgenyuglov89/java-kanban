package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private static final Pattern PRIORITIZED_PATTERN = Pattern.compile("^/prioritized$");

    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            HttpRequestMethods method = HttpRequestMethods.valueOf(httpExchange.getRequestMethod());
            switch (method) {
                case GET -> handleGet(httpExchange);
                default -> {
                    System.out.println("Метод запроса не поддерживается: " + method);
                    sendMethodNotAllowed(httpExchange);
                }
            }
        } catch (Exception e) {
            sendServerError(httpExchange, e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (PRIORITIZED_PATTERN.matcher(path).matches()) {
            String response = getGson().toJson(taskManager.getSortedTasksByTime());
            sendResponse(exchange, response);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }
}

