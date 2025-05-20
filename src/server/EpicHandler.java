package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import exception.TaskTimeOverlapException;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private static final Pattern EPICS_PATTERN = Pattern.compile("^/epics$");
    private static final Pattern EPIC_ID_PATTERN = Pattern.compile("^/epics/\\d+$");
    private static final Pattern EPIC_SUBTASKS_PATTERN = Pattern.compile("^/epics/\\d+/subtasks$");

    protected final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
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
        } catch (TaskTimeOverlapException e) {
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

    private void handleGet(HttpExchange exchange) throws IOException, NotFoundException {
        String path = exchange.getRequestURI().getPath();

        if (EPICS_PATTERN.matcher(path).matches()) {
            sendResponse(exchange, getGson().toJson(taskManager.getAllEpics()));
            return;
        }

        if (EPIC_ID_PATTERN.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/epics/", ""));
            Epic epic = (Epic) taskManager.getById(id);
            if (epic != null) {
                sendResponse(exchange, getGson().toJson(epic));
            } else {
                sendNotFound(exchange, "Epic with id=" + id + " not found");
            }
            return;
        }

        if (EPIC_SUBTASKS_PATTERN.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/epics/", "")
                            .replaceFirst("/subtasks", ""));
            Epic epic = (Epic) taskManager.getById(id);
            sendResponse(exchange, getGson().toJson(epic.getSubTasks()));
            return;
        }

        sendMethodNotAllowed(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException, TaskTimeOverlapException {
        String path = exchange.getRequestURI().getPath();
        if (!EPICS_PATTERN.matcher(path).matches()) {
            sendMethodNotAllowed(exchange);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epicTask = getGson().fromJson(body, Epic.class);

        if (epicTask.getId() == 0) {
            taskManager.createTask(epicTask);
        } else {
            taskManager.updateTask(epicTask);
        }

        exchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (EPICS_PATTERN.matcher(path).matches()) {
            taskManager.deleteAllEpics();
            exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }

        if (EPIC_ID_PATTERN.matcher(path).matches()) {
            int id = parseTaskId(path.replaceFirst("/epics/", ""));
            taskManager.deleteById(id);
            exchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }

        sendMethodNotAllowed(exchange);
    }
}