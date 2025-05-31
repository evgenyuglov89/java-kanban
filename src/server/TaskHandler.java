package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import exception.TaskScheduleConflictException;
import manager.TaskManager;
import task.Task;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.regex.Pattern;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private static final Pattern COLLECTION_PATH = Pattern.compile("^/tasks$");
    private static final Pattern ITEM_PATH = Pattern.compile("^/tasks/\\d+$");

    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            HttpRequestMethods requestMethod = HttpRequestMethods.valueOf(httpExchange.getRequestMethod());

            switch (requestMethod) {
                case GET -> handleGet(httpExchange);
                case POST -> handlePost(httpExchange);
                case DELETE -> handleDelete(httpExchange);
                default -> sendMethodNotAllowed(httpExchange);
            }

        } catch (TaskScheduleConflictException e) {
            System.out.println(e.getMessage());
            sendHasOverlap(httpExchange);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            System.err.println("Internal error: " + e.getMessage());
            sendServerError(httpExchange, e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (COLLECTION_PATH.matcher(path).matches()) {
            taskManager.deleteAllTasks();
            exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }

        if (ITEM_PATH.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/tasks/", ""));
            if (id != -1) {
                taskManager.deleteById(id);
                exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
                return;
            }
        }

        sendMethodNotAllowed(exchange);
    }

    private void handleGet(HttpExchange exchange) throws IOException, NotFoundException {
        String path = exchange.getRequestURI().getPath();

        if (COLLECTION_PATH.matcher(path).matches()) {
            sendJson(exchange, taskManager.getAllTasks());
            return;
        }

        if (ITEM_PATH.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/tasks/", ""));
            if (id != -1) {
                Task task = taskManager.getById(id);
                if (task != null) {
                    sendJson(exchange, task);
                } else {
                    sendNotFound(exchange, "Task with id=" + id + " not found");
                }
                return;
            }
        }

        sendMethodNotAllowed(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException, TaskScheduleConflictException {
        String path = exchange.getRequestURI().getPath();

        if (COLLECTION_PATH.matcher(path).matches()) {
            String body = readRequestBody(exchange);
            Task task = getGson().fromJson(body, Task.class);

            if (task.getId() == 0) {
                taskManager.createTask(task);
            } else {
                taskManager.updateTask(task);
            }

            exchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void sendJson(HttpExchange exchange, Object data) throws IOException {
        String json = getGson().toJson(data);
        sendResponse(exchange, json);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}