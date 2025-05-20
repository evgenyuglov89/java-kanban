package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import exception.TaskScheduleConflictException;
import exception.TaskTimeOverlapException;
import manager.TaskManager;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.util.regex.Pattern;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {
    private static final Pattern SUBTASKS_PATTERN = Pattern.compile("^/subtasks$");
    private static final Pattern SUBTASK_ID_PATTERN = Pattern.compile("^/subtasks/\\d+$");

    private final TaskManager taskManager;

    public SubTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            HttpRequestMethods method = HttpRequestMethods.valueOf(httpExchange.getRequestMethod());
            switch (method) {
                case GET -> handleGet(httpExchange);
                case POST -> handlePost(httpExchange);
                case DELETE -> handleDelete(httpExchange);
                default -> sendMethodNotAllowed(httpExchange);
            }
        } catch (TaskTimeOverlapException e) {
            System.out.println(e.getMessage());
            sendHasOverlap(httpExchange);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(httpExchange, e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, NotFoundException {
        String path = exchange.getRequestURI().getPath();

        if (SUBTASKS_PATTERN.matcher(path).matches()) {
            sendResponse(exchange, getGson().toJson(taskManager.getAllSubTasks()));
            return;
        }

        if (SUBTASK_ID_PATTERN.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/subtasks/", ""));
            Task subTask = taskManager.getById(id);
            if (subTask != null) {
                sendResponse(exchange, getGson().toJson(subTask));
            } else {
                sendNotFound(exchange, "SubTask with id=" + id + " not found");
            }
            return;
        }

        sendMethodNotAllowed(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException, TaskTimeOverlapException {
        String path = exchange.getRequestURI().getPath();

        if (!SUBTASKS_PATTERN.matcher(path).matches()) {
            sendMethodNotAllowed(exchange);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        Subtask subTask = getGson().fromJson(body, Subtask.class);

        try {
            if (subTask.getId() == 0) {
                taskManager.createTask(subTask);
            } else {
                taskManager.updateTask(subTask);
            }
        } catch (TaskScheduleConflictException e) {
            sendHasOverlap(exchange);
        }
        exchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (SUBTASKS_PATTERN.matcher(path).matches()) {
            taskManager.deleteAllSubTasks();
            exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }

        if (SUBTASK_ID_PATTERN.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/subtasks/", ""));
            taskManager.deleteById(id);
            exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }

        sendMethodNotAllowed(exchange);
    }
}

